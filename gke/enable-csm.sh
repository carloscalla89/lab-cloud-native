#!/usr/bin/env bash
# Registra el cluster en el fleet y habilita Cloud Service Mesh gestionado.
# Idempotente: el register/mesh enable se pueden reejecutar sin problema.
set -euo pipefail
source "$(dirname "$0")/config.env"

# Ubicacion del cluster (zona si es zonal, region si es regional).
CLUSTER_LOCATION="${ZONE:-$REGION}"

# 1) Registrar la membership en el fleet.
if gcloud container fleet memberships describe "$CLUSTER_NAME" \
      --location "$REGION" --project "$PROJECT_ID" >/dev/null 2>&1; then
  echo "Membership '$CLUSTER_NAME' ya registrada en el fleet."
else
  gcloud container fleet memberships register "$CLUSTER_NAME" \
    --gke-cluster "${CLUSTER_LOCATION}/${CLUSTER_NAME}" \
    --location "$REGION" --project "$PROJECT_ID"
fi

# 2) Habilitar / actualizar el mesh gestionado.
gcloud container fleet mesh enable --project "$PROJECT_ID"
gcloud container fleet mesh update \
  --management automatic \
  --memberships "$CLUSTER_NAME" \
  --location "$REGION" \
  --project "$PROJECT_ID"

# 3) Esperar a que el control plane gestionado este ACTIVE.
echo "Esperando a que Cloud Service Mesh este ACTIVE..."
for i in $(seq 1 40); do
  state="$(gcloud container fleet mesh describe --project "$PROJECT_ID" --format=json 2>/dev/null \
    | grep -o '"state":[[:space:]]*"ACTIVE"' | head -1 || true)"
  echo "  intento $i: ${state:-estado aun no ACTIVE}"
  if [ -n "$state" ]; then
    echo "Cloud Service Mesh ACTIVE."
    exit 0
  fi
  sleep 15
done

echo "ERROR: Cloud Service Mesh no llego a ACTIVE." >&2
echo "Revisa 'gcloud container fleet mesh describe --project $PROJECT_ID' y reintenta." >&2
exit 1
