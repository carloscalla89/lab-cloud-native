#!/usr/bin/env bash
# Borra todos los recursos GCP creados por execute-all.sh:
# IP estatica, cluster (arrastra los recursos k8s), membership/CSM y Artifact Registry.
# No borra el proyecto ni las APIs. Idempotente.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
source "$ROOT/gke/config.env"
source "$ROOT/artifact/config.env"

read -r -p "Esto borra CLUSTER, CSM, REPO e IP en '$PROJECT_ID'. Continuar? [y/N] " ok
[[ "$ok" =~ ^[yY]$ ]] || { echo "cancelado"; exit 0; }

echo "==> IP estatica '$GATEWAY_IP_NAME'"
gcloud compute addresses delete "$GATEWAY_IP_NAME" \
  --global --project "$PROJECT_ID" --quiet 2>/dev/null || true

echo "==> Cluster '$CLUSTER_NAME' (arrastra los recursos k8s)"
gcloud container clusters delete "$CLUSTER_NAME" \
  --region "$REGION" --project "$PROJECT_ID" --quiet 2>/dev/null || true

echo "==> Fleet membership / Cloud Service Mesh"
gcloud container fleet memberships unregister "$CLUSTER_NAME" \
  --location "$REGION" --project "$PROJECT_ID" 2>/dev/null || true

echo "==> Artifact Registry '$REPO'"
gcloud artifacts repositories delete "$REPO" \
  --location "$REGION" --project "$PROJECT_ID" --quiet 2>/dev/null || true

echo "Recursos GCP eliminados."
