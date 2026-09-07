#!/usr/bin/env bash
set -euo pipefail

# ======================================================================
# Despliega SOLO order-service en el cluster Kind:
#   1. mvn package        (genera el JAR en target/quarkus-app/)
#   2. docker build       (imagen local order-service:jvm)
#   3. kind load          (carga la imagen en el cluster Kind)
#   4. kubectl apply       (Deployment + Service en el namespace kcd)
#   5. rollout status     (espera a que el Deployment este disponible)
#
# Requisitos previos (ver k8s/README.md): namespace kcd, ConfigMap
# otel-endpoint y PostgreSQL (base 'orders') ya desplegados.
# ======================================================================

# --- Compatibilidad Windows (Git Bash / MSYS2) ---
# NO exportar MSYS_NO_PATHCONV globalmente: rompe Maven (plexus-classworlds).
# Se aplica solo a los comandos que lo necesitan (kubectl, helm, kind) via wrapper.
if [[ "${OSTYPE:-}" == msys* || "${OSTYPE:-}" == cygwin* || "${OSTYPE:-}" == mingw* ]]; then
    _IS_WINDOWS=true
else
    _IS_WINDOWS=false
fi

_k()  { MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*" kubectl "$@"; }
_h()  { MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*" helm "$@"; }
_ki() { MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*" kind "$@"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${K8S_DIR}/.." && pwd)"
if command -v cygpath &>/dev/null; then
    K8S_DIR="$(cygpath -m "$K8S_DIR")"
    REPO_ROOT="$(cygpath -m "$REPO_ROOT")"
fi

# --- Parametros de esta app ---
SERVICE_NAME="order-service"
APP_DIR="${REPO_ROOT}/k8s-apps/${SERVICE_NAME}"
MANIFEST="${K8S_DIR}/20-order-service.yaml"
IMAGE="${SERVICE_NAME}:jvm"

# --- Parametros configurables por variable de entorno ---
NAMESPACE="${KCD_NAMESPACE:-apps}"
CLUSTER_NAME="${KIND_CLUSTER_NAME:-k8s-demos-cluster}"
SKIP_TESTS="${SKIP_TESTS:-true}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

check_deps() {
    for cmd in mvn docker kind kubectl; do
        command -v "$cmd" &>/dev/null || error "'$cmd' no encontrado. Instalalo antes de continuar."
    done
    [[ -d "${APP_DIR}" ]] || error "No se encontro el directorio de la app: ${APP_DIR}"
    [[ -f "${MANIFEST}" ]] || error "No se encontro el manifiesto: ${MANIFEST}"
    _k cluster-info &>/dev/null || error "No hay conexion con ningun cluster de Kubernetes (revisa kubectl config current-context)."
    _k get namespace "${NAMESPACE}" &>/dev/null \
        || warn "El namespace '${NAMESPACE}' no existe. Aplica primero 00-namespace.yaml / 01-otel-endpoint-configmap.yaml / 10-postgres.yaml."
    info "Dependencias OK | contexto: $(_k config current-context)"
}

build_image() {
    info "Construyendo JAR de ${SERVICE_NAME} (mvn package, skipTests=${SKIP_TESTS})..."
    ( cd "${APP_DIR}" && mvn -q package -DskipTests="${SKIP_TESTS}" )

    info "Construyendo imagen Docker '${IMAGE}'..."
    ( cd "${APP_DIR}" && docker build -f src/main/docker/Dockerfile.jvm -t "${IMAGE}" . )
}

load_image() {
    info "Cargando '${IMAGE}' en el cluster Kind '${CLUSTER_NAME}'..."
    _ki load docker-image "${IMAGE}" --name "${CLUSTER_NAME}"
}

deploy() {
    info "Aplicando manifiesto en namespace '${NAMESPACE}'..."
    _k apply -f "${MANIFEST}"

    info "Reiniciando el rollout para usar la imagen recien cargada..."
    _k -n "${NAMESPACE}" rollout restart "deployment/${SERVICE_NAME}"

    info "Esperando a que el Deployment este disponible..."
    _k -n "${NAMESPACE}" rollout status "deployment/${SERVICE_NAME}" --timeout=180s
}

summary() {
    echo
    info "=== ${SERVICE_NAME} desplegado en '${NAMESPACE}' ==="
    _k -n "${NAMESPACE}" get pods,svc -l "app.kubernetes.io/name=${SERVICE_NAME}"
    echo
    info "Probar (port-forward):"
    info "  kubectl -n ${NAMESPACE} port-forward svc/${SERVICE_NAME} 8080:8080"
    info "  curl http://localhost:8080/q/health"
}

main() {
    info "=== Despliegue de ${SERVICE_NAME} ==="
    info "App      : ${APP_DIR}"
    info "Manifiesto: ${MANIFEST}"
    info "Cluster   : ${CLUSTER_NAME}"
    echo
    check_deps
    build_image
    load_image
    deploy
    summary
}

main "$@"
