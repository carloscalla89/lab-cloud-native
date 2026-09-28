#!/usr/bin/env bash
# Aplica los manifiestos k8s en GKE (requiere haber corrido execute-all.sh).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> k8s-gcp-db   (namespaces + Postgres + secretos)"
kubectl apply -k "$ROOT/k8s-gcp-db/"
echo "==> k8s-gcp-sa   (ServiceAccounts)"
kubectl apply -k "$ROOT/k8s-gcp-sa/"
echo "==> k8s-gcp-apps (microservicios)"
kubectl apply -k "$ROOT/k8s-gcp-apps/"
echo "==> csm          (Gateway gestionado + routing + policies)"
kubectl apply -k "$ROOT/csm/"

echo
echo "Manifiestos aplicados. Verifica:"
echo "  kubectl get gateway,httproute -A"
echo "  kubectl -n apps get pods"
