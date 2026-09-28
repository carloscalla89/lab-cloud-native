#!/usr/bin/env bash
# Configura el contexto de kubectl para el cluster.
set -euo pipefail
source "$(dirname "$0")/config.env"

gcloud container clusters get-credentials "$CLUSTER_NAME" \
  --region "$REGION" --project "$PROJECT_ID"

echo "Contexto actual: $(kubectl config current-context)"
