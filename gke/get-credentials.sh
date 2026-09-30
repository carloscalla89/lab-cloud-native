#!/usr/bin/env bash
# Configura el contexto de kubectl para el cluster.
set -euo pipefail
source "$(dirname "$0")/config.env"

if [ -n "${ZONE:-}" ]; then LOC_ARGS=(--zone "$ZONE"); else LOC_ARGS=(--region "$REGION"); fi

gcloud container clusters get-credentials "$CLUSTER_NAME" \
  "${LOC_ARGS[@]}" --project "$PROJECT_ID"

echo "Contexto actual: $(kubectl config current-context)"
