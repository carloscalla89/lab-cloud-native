# artifact/

Scripts para provisionar Artifact Registry y publicar las imagenes.

## Uso

```bash
bash artifact/create-repo.sh        # crea el repo Docker + docker login
bash artifact/build-push.sh         # mvn package + docker build + push (x3)
bash artifact/grant-node-reader.sh  # roles/artifactregistry.reader al node SA
```

Normalmente no los ejecutas a mano: `scripts-gcp/execute-all.sh` los orquesta.

## Variables

`artifact/config.env`: `PROJECT_ID`, `REGION`, `REPO`, `TAG`, `SERVICES`.

> `PROJECT_ID` y `REGION` deben coincidir con `gke/config.env`.

## Relacion con los manifiestos

El `REGISTRY` resultante es:

```
${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}
```

Debe coincidir con las imagenes de `k8s-gcp-apps/deployment-*.yaml`
(reemplaza el placeholder `PROJECT_ID` en esos manifiestos).
