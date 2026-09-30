# gke/

Scripts para provisionar el cluster GKE y Cloud Service Mesh gestionado.
Cada script es **idempotente** (se puede reejecutar).

## Uso

```bash
bash gke/enable-apis.sh       # habilita container/gkehub/mesh/compute/artifactregistry
bash gke/create-cluster.sh    # crea el cluster Standard (zonal o regional)
bash gke/get-credentials.sh   # configura kubectl
bash gke/enable-csm.sh        # fleet + mesh gestionado (espera ACTIVE)
bash gke/reserve-ip.sh        # IP estatica global para el Gateway
```

Normalmente no los ejecutas a mano: `scripts-gcp/execute-all.sh` los orquesta.

## Variables

`gke/config.env`:

| Variable | Descripcion |
| --- | --- |
| `PROJECT_ID` | proyecto GCP (sincronizar con `artifact/config.env`) |
| `REGION` | region de recursos (Artifact Registry, IP global) |
| `ZONE` | si esta definida -> cluster **zonal** en esa zona; vacia -> **regional** |
| `NODE_LOCATIONS` | solo regional, ej. `us-central1-a,us-central1-b` |
| `CLUSTER_NAME` | nombre del cluster |
| `RELEASE_CHANNEL` | canal de release de GKE |
| `NODE_COUNT` | nodos (por zona) |
| `MACHINE_TYPE` | tipo de maquina principal |
| `MACHINE_TYPES_FALLBACK` | tipos alternativos si falta capacidad |
| `SPOT` | `true` -> nodos spot (mas baratos, desalojables) |
| `GATEWAY_IP_NAME` | nombre de la IP estatica del Gateway |

## Cluster zonal vs regional

- **Zonal** (`ZONE` definida): 1 zona, `NODE_COUNT` es el total. Mas barato y
  menos expuesto a que una zona sin capacidad aborte la creacion.
- **Regional** (`ZONE` vacia): multi-zona; `NODE_COUNT` es **por zona**.

## Falta de capacidad (ZONE_RESOURCE_POOL_EXHAUSTED)

`create-cluster.sh` reintenta con `MACHINE_TYPE` y luego con cada valor de
`MACHINE_TYPES_FALLBACK` (por defecto `e2-standard-4 n2-standard-2`). Si todos
fallan, cambia `ZONE` o `REGION` en `config.env` y reintenta.

Para ver zonas con disponibilidad:
```bash
gcloud compute machine-types list \
  --filter="name=e2-standard-2 AND zone:us-central1-*" --format='value(zone)'
```

## Notas

- La revision de CSM (`istio.io/rev`) que use `k8s-gcp-db/namespace-apps.yaml`
  debe coincidir con la que reporte `gcloud container fleet mesh describe`.
- Si reservaste IP, referenciala en `csm/gateway.yaml` con `spec.addresses`
  (`type: NamedAddress`).
- Si cambias `REGION`, alinea tambien `artifact/config.env`.
