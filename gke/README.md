# gke/

Scripts para provisionar el cluster GKE y Cloud Service Mesh gestionado.
Cada script es **idempotente** (se puede reejecutar).

## Uso

```bash
bash gke/enable-apis.sh       # habilita container/gkehub/mesh/compute/artifactregistry
bash gke/create-cluster.sh    # crea el cluster Standard
bash gke/get-credentials.sh   # configura kubectl
bash gke/enable-csm.sh        # fleet + mesh gestionado (espera ACTIVE)
bash gke/reserve-ip.sh        # IP estatica global para el Gateway
```

Normalmente no los ejecutas a mano: `scripts-gcp/execute-all.sh` los orquesta.

## Variables

`gke/config.env`: `PROJECT_ID`, `REGION`, `CLUSTER_NAME`, `RELEASE_CHANNEL`,
`NODE_COUNT`, `MACHINE_TYPE`, `GATEWAY_IP_NAME`.

> `PROJECT_ID` y `REGION` deben coincidir con `artifact/config.env`.

## Notas

- La revision de CSM (`istio.io/rev`) que use `k8s-gcp-db/namespace-apps.yaml`
  debe coincidir con la que reporte `gcloud container fleet mesh describe`.
- Si reservaste IP, referenciala en `csm/gateway.yaml` con `spec.addresses`
  (`type: NamedAddress`).
