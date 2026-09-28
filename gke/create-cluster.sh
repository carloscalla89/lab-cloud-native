#!/usr/bin/env bash
# Crea el cluster GKE (Standard). Idempotente: no hace nada si ya existe.
set -euo pipefail
source "$(dirname "$0")/config.env"

if gcloud container clusters describe "$CLUSTER_NAME" \
     --region "$REGION" --project "$PROJECT_ID" >/dev/null 2>&1; then
  echo "El cluster '$CLUSTER_NAME' ya existe en $REGION. Nada que hacer."
  exit 0
fi

gcloud container clusters create "$CLUSTER_NAME" \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --num-nodes "$NODE_COUNT" \
  --machine-type "$MACHINE_TYPE" \
  --release-channel "$RELEASE_CHANNEL" \
  --workload-pool "${PROJECT_ID}.svc.id.goog" \
  --enable-ip-alias

echo "Cluster '$CLUSTER_NAME' creado en $REGION."
