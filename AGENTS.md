# AGENTS.md

Cloud-native demo: three Quarkus microservices plus Kubernetes manifests (kind,
with Istio). Read the service `README.md` you are touching; the notes below are
the cross-cutting gotchas.

## Repo shape

- Three **independent** Quarkus 3.33 / Java 21 Maven projects under
  `quarkus-apps/` (source code; distinct from the `k8s-apps/` manifests). There
  is **no parent/aggregator pom and no Maven wrapper** — always `cd` into the
  service directory and use the installed `mvn`.
  - `quarkus-apps/order-service/` — orders + PostgreSQL, HTTP 8080
  - `quarkus-apps/tracking-service/` — reverse-geocoding ACL + PostgreSQL, 8081
  - `quarkus-apps/experience-order-tracker-service/` — BFF/orchestrator, no DB, 8082
- All three use base package `com.labcloudnative.*` and groupId
  `com.labcloudnative`; earlier mismatched directory names are gone.
- Poms still diverge: Quarkus BOM `3.33.2.1` for order vs `3.33.1` for
  tracking/experience; `quarkus-junit-mockito` and Lombok exist **only** in
  order-service (others use `quarkus-junit5`).
- No `src/test` trees yet, though test deps are declared. If you add tests:
  `application.*` -> JUnit + `MockitoExtension`; `infrastructure.*` ->
  `@QuarkusTest` (`@TestTransaction` for persistence); `domain.*` -> plain JUnit.

## Commands (run from the service directory)

- Build: `mvn package`
- Dev: `mvn quarkus:dev` (order/tracking: Dev Services starts Postgres; needs Docker)
- Single test: `mvn test -Dtest=ClassNameTest`
- Image: build the jar first, then
  `docker build -f src/main/docker/Dockerfile.jvm -t <service>:jvm .`

## Kubernetes layout (there is NO root kustomization)

Apply per folder:

- `00-namespace.yaml` → namespace `apps` (labeled `istio-injection: enabled`). Apply first.
- `k8s-db/` → PostgreSQL `cloudnative-db` in namespace `databases`; credentials
  source of truth is `k8s-db/postgres-credentials.env`. `kubectl apply -k k8s-db/`
- `k8s-sa/` → one ServiceAccount per service in `apps`. **Must exist before the
  Deployments.** `kubectl apply -k k8s-sa/`
- `k8s-apps/<service>/` → Deployment + Service + its own kustomization.
  `kubectl apply -k k8s-apps/<service>/`
- `istio/base/` → Gateway API `Gateway` + `HTTPRoute`, peer-authentication and
  authorization-policies. `kubectl apply -k istio/base/` (or the per-env overlay
  `istio/overlays/kind/`). Requires the Gateway API CRDs (standard channel).
  `kubectl apply -k istio/` no longer exists.

Order: `00-namespace` → `k8s-db` → `k8s-sa` → `k8s-apps/<service>` → `istio`.
kind images are `carlos89/<service>:jvm` (Docker Hub) with `imagePullPolicy: Always`;
for offline kind, `kind load docker-image carlos89/<service>:jvm --name k8s-demos-cluster`.

GCP/GKE manifests are **separate and self-contained** (no root kustomization):
`csm/` (Cloud Service Mesh + Gateway API), `k8s-gcp-db/`, `k8s-gcp-sa/`,
`k8s-gcp-apps/`. Apply with `kubectl apply -k <folder>/`; order:
`k8s-gcp-db` → `k8s-gcp-sa` → `k8s-gcp-apps` → `csm`. GKE images come from
Artifact Registry
(`us-central1-docker.pkg.dev/training-lab-504513/lab-cloudnative/<service>:1.0.0`).
`istio/` is kind-only (no GCP overlay).

GCP infra provisioning lives in `gke/` (cluster + Cloud Service Mesh),
`artifact/` (Artifact Registry + image push) and `scripts-gcp/` (`execute-all`
to create all resources, `deploy-apps` for the k8s manifests, `delete-all` to
tear down). Configure `gke/config.env` and `artifact/config.env` (keep
`PROJECT_ID`/`REGION` in sync). These scripts are run **manually** — do not
execute them as part of editing.

## Istio

- Istio **1.31.1** installed (profile `default`, sidecar mode). k8s is 1.36, so
  the old 1.22 is EOL and unsupported — don't use it.
- `istioctl` is global at `D:\programas\istio\istio-1.31.1\bin` (user PATH). An
  already-open shell may still resolve 1.22; fix with
  `export PATH="/d/programas/istio/istio-1.31.1/bin:$PATH"`.
- `PeerAuthentication/default` is **STRICT** in `apps`; `istio/base/authorization-policies.yaml`
  allows experience only from the ingress-gateway SA, and order/tracking only
  from the experience SA (plus `/q/health/*` for probes).
- Ingress uses the **Kubernetes Gateway API** (the Istio `Gateway`/`VirtualService`
  APIs are no longer used): the standard-channel CRDs must be installed,
  `GatewayClass/istio` is created by istiod, the `Gateway` lives in namespace
  `istio-ingress` (no sidecar injection) and Istio auto-provisions its
  `Deployment`/`Service` as `apps-gateway-istio`. The `HTTPRoute` lives in `apps`
  and exposes **only `/experience`** (BFF is the single entry point).
- The ingress-gateway ServiceAccount is
  `cluster.local/ns/istio-ingress/sa/apps-gateway-istio` (used by
  `experience-allow-gateway`); update it if the generated name ever changes.
- Test via `kubectl -n istio-ingress port-forward svc/apps-gateway-istio 8080:80`.
  On kind the Service is `LoadBalancer` with `EXTERNAL-IP <pending>` and the
  Gateway reports `Programmed=False` (`AddressNotAssigned`); this is expected and
  port-forward still works.
- Env-specific bits live in `istio/overlays/<env>/` (only `kind` today; the GKE/CSM
  manifests live in the separate `csm/` folder, not under `istio/`).
- Gotcha: `kubectl port-forward` directly to a service **bypasses** Istio
  mTLS/authorization (localhost is exempt), so it is fine for functional tests
  but does NOT validate policies. To validate, use the gateway or a meshed pod
  with the right ServiceAccount.

## Secrets / DB wiring gotcha

- order/tracking Deployments reference Secret `postgres-credentials-apps`
  (namespace `apps`) for `DB_USERNAME`/`DB_PASSWORD`. That secret was previously
  generated by a root kustomization that no longer exists, so **no current
  manifest creates it** — create it manually or through a generator, or those
  pods fail to start.
- `k8s-db/` generates its own `postgres-credentials` in `databases` from
  `k8s-db/postgres-credentials.env`; both secrets must share the same user/password.

## Config quirks

- order/tracking dev/test use Quarkus Dev Services
  (`%dev`/`%test quarkus.datasource.devservices.enabled=true`); production uses
  `%prod` `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`. tracking `%dev` also loads the
  Flyway seed from `classpath:db/dev`.
- Geocoding provider is chosen at **build time** (`geocoding.provider`,
  `@IfBuildProperty`); `nominatim` is default, `google` needs a rebuild + API key.
- Runtime-only fault injection (off by default, toggled via env without rebuild):
  `FAULT_LATENCY_*` in tracking, `FAULT_DB_LATENCY_*` in order.
- OpenTelemetry is configured in the **manifests** (env `QUARKUS_OTEL_SDK_DISABLED`,
  `OTEL_EXPORTER_OTLP_ENDPOINT`, `QUARKUS_OTEL_METRICS_ENABLED`,
  `QUARKUS_OTEL_LOGS_ENABLED`); `application.properties` only keeps
  `%dev.quarkus.otel.sdk.disabled=true`. The endpoint currently assumes
  `otel-collector.otel.svc.cluster.local:4317` — verify against the real collector Service.
- BFF convention: `shipmentId == orderId`.
- Root `README.md` is **UTF-16LE**; edit with encoding-aware tooling.

## Skills

- `.agents/skills/k8s-database` — scaffold a database (Postgres) on Kubernetes.
- `.agents/skills/k8s-microservice` — generate Service + Deployment (+ kustomization)
  under `k8s-apps/<microservice>/`. It does **not** create ServiceAccounts or
  Secrets, so those remain manual steps.
