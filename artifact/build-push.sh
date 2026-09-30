#!/usr/bin/env bash
# Compila (mvn package) y publica las imagenes directamente en Artifact Registry.
# Usa `buildx build --push` para exportar DIRECTO al registry, evitando el paso
# local "unpacking to ..." del containerd image store de Docker Desktop.
set -euo pipefail
source "$(dirname "$0")/config.env"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}"

for svc in $SERVICES; do
  echo "==> $svc -> $REGISTRY/$svc:$TAG"
  ( cd "$ROOT/quarkus-apps/$svc" && mvn -q package )
  docker buildx build \
    --platform "${PLATFORM:-linux/amd64}" \
    -f "$ROOT/quarkus-apps/$svc/src/main/docker/Dockerfile.jvm" \
    -t "$REGISTRY/$svc:$TAG" \
    --push \
    "$ROOT/quarkus-apps/$svc"
done

echo "Imagenes publicadas en $REGISTRY (tag $TAG)."
echo "Verifica que el PROJECT_ID de k8s-gcp-apps/*.yaml coincida."
