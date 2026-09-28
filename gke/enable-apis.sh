#!/usr/bin/env bash
# Habilita las APIs necesarias en el proyecto GCP.
set -euo pipefail
source "$(dirname "$0")/config.env"

gcloud services enable \
  container.googleapis.com \
  gkehub.googleapis.com \
  mesh.googleapis.com \
  compute.googleapis.com \
  artifactregistry.googleapis.com \
  --project "$PROJECT_ID"

echo "APIs habilitadas en $PROJECT_ID."
