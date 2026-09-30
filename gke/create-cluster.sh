#!/usr/bin/env bash
# Crea el cluster GKE (Standard). Zonal si ZONE esta definido, regional si no.
# Idempotente y con fallback de machine-type ante falta de capacidad.
set -euo pipefail
source "$(dirname "$0")/config.env"

# Ubicacion del cluster: zonal (ZONE) o regional (REGION).
if [ -n "${ZONE:-}" ]; then
  LOC_ARGS=(--zone "$ZONE")
  AT="$ZONE"
else
  LOC_ARGS=(--region "$REGION")
  AT="$REGION"
fi

if gcloud container clusters describe "$CLUSTER_NAME" \
     "${LOC_ARGS[@]}" --project "$PROJECT_ID" >/dev/null 2>&1; then
  echo "El cluster '$CLUSTER_NAME' ya existe en $AT. Nada que hacer."
  exit 0
fi

EXTRA=()
[ -n "${NODE_LOCATIONS:-}" ] && EXTRA+=(--node-locations "$NODE_LOCATIONS")
[ "${SPOT:-false}" = "true" ] && EXTRA+=(--spot)

# Fallback de machine-type ante ZONE_RESOURCE_POOL_EXHAUSTED.
for mt in "$MACHINE_TYPE" ${MACHINE_TYPES_FALLBACK:-}; do
  echo "==> creando cluster en $AT con machine-type=$mt"
  if gcloud container clusters create "$CLUSTER_NAME" \
       --project "$PROJECT_ID" \
       "${LOC_ARGS[@]}" \
       --num-nodes "$NODE_COUNT" \
       --machine-type "$mt" \
       --release-channel "$RELEASE_CHANNEL" \
       --workload-pool "${PROJECT_ID}.svc.id.goog" \
       --enable-ip-alias \
       "${EXTRA[@]}"; then
    echo "Cluster '$CLUSTER_NAME' creado con $mt en $AT."
    exit 0
  fi
  echo "  sin capacidad con $mt; probando el siguiente..."
done

echo "ERROR: no se pudo crear el cluster (posible falta de capacidad)." >&2
echo "Prueba otra ZONE o REGION en gke/config.env." >&2
exit 1
