# scripts-gcp/

Orquestador de los recursos de GCP. Cada script delega en los bloques atómicos
de `gke/` y `artifact/`.

## Scripts

```bash
bash scripts-gcp/execute-all.sh    # crea toda la infra GCP (GKE, CSM, Artifact, IP)
bash scripts-gcp/deploy-apps.sh    # aplica los manifiestos k8s en el cluster
bash scripts-gcp/delete-all.sh     # borra todos los recursos GCP (con confirmacion)
```

## Flujo tipico

```bash
bash scripts-gcp/execute-all.sh
bash scripts-gcp/deploy-apps.sh
# ... trabajar ...
bash scripts-gcp/delete-all.sh
```

## Orden interno de execute-all

1. APIs (`gke/enable-apis.sh`)
2. Artifact Registry (`artifact/create-repo.sh`)
3. Cluster GKE (`gke/create-cluster.sh`)
4. Build + push (`artifact/build-push.sh`)
5. Permiso de pull (`artifact/grant-node-reader.sh`)
6. Cloud Service Mesh (`gke/enable-csm.sh`, espera ACTIVE)
7. kubeconfig (`gke/get-credentials.sh`)
8. IP estatica (`gke/reserve-ip.sh`)

## Notas

- `execute-all` NO aplica manifiestos k8s; usa `deploy-apps.sh` aparte.
- Todo es idempotente (reejecutable).
- `delete-all` borra el cluster (y con el los recursos k8s), IP, repo y membership.
- En Windows/git bash, invocar con `bash`.
