#!/usr/bin/env bash
# Levanta TODA la infraestructura GCP: APIs, Artifact Registry, GKE,
# Cloud Service Mesh gestionado e IP estatica.
# NO aplica los manifiestos k8s (para eso: scripts-gcp/deploy-apps.sh).
# Idempotente: se puede reejecutar.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> 1/8  APIs"
bash "$ROOT/gke/enable-apis.sh"
echo "==> 2/8  Artifact Registry"
bash "$ROOT/artifact/create-repo.sh"
echo "==> 3/8  Cluster GKE"
bash "$ROOT/gke/create-cluster.sh"
echo "==> 4/8  Build + push de imagenes"
bash "$ROOT/artifact/build-push.sh"
echo "==> 5/8  Permiso de pull (node SA)"
bash "$ROOT/artifact/grant-node-reader.sh"
echo "==> 6/8  Cloud Service Mesh gestionado"
bash "$ROOT/gke/enable-csm.sh"
echo "==> 7/8  kubeconfig"
bash "$ROOT/gke/get-credentials.sh"
echo "==> 8/8  IP estatica"
bash "$ROOT/gke/reserve-ip.sh"

echo
echo "Infra GCP lista."
echo "Siguiente: bash $ROOT/scripts-gcp/deploy-apps.sh   (aplica k8s-gcp-* + csm)"
