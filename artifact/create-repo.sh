#!/usr/bin/env bash
# Crea el repositorio Docker en Artifact Registry y configura docker login.
# Idempotente: no falla si el repo ya existe.
set -euo pipefail
source "$(dirname "$0")/config.env"

if gcloud artifacts repositories describe "$REPO" \
     --location "$REGION" --project "$PROJECT_ID" >/dev/null 2>&1; then
  echo "El repo '$REPO' ya existe en $REGION."
else
  gcloud artifacts repositories create "$REPO" \
    --repository-format=docker \
    --location "$REGION" \
    --project "$PROJECT_ID" \
    --description="Imagenes de los microservicios (order/tracking/experience)"
fi

gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

echo "Registry: ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}"
