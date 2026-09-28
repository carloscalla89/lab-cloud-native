#!/usr/bin/env bash
# Compila (mvn package), construye y publica las imagenes de los microservicios.
set -euo pipefail
source "$(dirname "$0")/config.env"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}"

for svc in $SERVICES; do
  echo "==> $svc"
  ( cd "$ROOT/quarkus-apps/$svc" && mvn -q package )
  docker build \
    -f "$ROOT/quarkus-apps/$svc/src/main/docker/Dockerfile.jvm" \
    -t "$REGISTRY/$svc:$TAG" \
    "$ROOT/quarkus-apps/$svc"
  docker push "$REGISTRY/$svc:$TAG"
done

echo "Imagenes publicadas en $REGISTRY (tag $TAG)."
echo "Verifica que el PROJECT_ID de k8s-gcp-apps/*.yaml coincida."
