#!/usr/bin/env bash
# Otorga roles/artifactregistry.reader al node service account del cluster,
# necesario para que GKE pueda hacer pull de las imagenes.
set -euo pipefail
source "$(dirname "$0")/config.env"

PN="$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')"
SA="${PN}-compute@developer.gserviceaccount.com"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:${SA}" \
  --role="roles/artifactregistry.reader" \
  --condition=None --quiet

echo "roles/artifactregistry.reader otorgado a ${SA}."
