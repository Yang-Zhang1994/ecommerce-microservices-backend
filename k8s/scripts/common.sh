#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
KIND_CLUSTER_NAME="${KIND_CLUSTER_NAME:-gulimall}"
HELM_RELEASE="${HELM_RELEASE:-gulimall}"
HELM_NAMESPACE="${HELM_NAMESPACE:-gulimall}"
HELM_CHART="${ROOT_DIR}/k8s/helm/gulimall"

# Maven modules and Docker image names (tag :local).
JAVA_MODULES=(
  gulimall-gateway
  gulimall-auth-server
  gulimall-product
  gulimall-member
  gulimall-cart
  gulimall-order
  gulimall-ware
  gulimall-coupon
  gulimall-search
  gulimall-seckill
  gulimall-third-party
  renren-fast
)

DOCKER_IMAGES=(
  gulimall-gateway
  gulimall-auth-server
  gulimall-product
  gulimall-member
  gulimall-cart
  gulimall-order
  gulimall-ware
  gulimall-coupon
  gulimall-search
  gulimall-seckill
  gulimall-third-party
  renren-fast
)

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "error: required command not found: $1" >&2
    exit 1
  fi
}

build_all_images() {
  echo "==> Maven package (${#JAVA_MODULES[@]} modules)"
  require_cmd docker
  local mvn_modules
  mvn_modules=$(IFS=,; echo "${JAVA_MODULES[*]}")
  (cd "${ROOT_DIR}" && mvn -q -pl "${mvn_modules}" -am package -DskipTests)

  echo "==> Docker build (:local, platform=${DOCKER_PLATFORM:-linux/amd64})"
  for img in "${DOCKER_IMAGES[@]}"; do
    local dir="${ROOT_DIR}/${img}"
    if [[ ! -f "${dir}/Dockerfile" ]]; then
      echo "error: missing Dockerfile for ${img}" >&2
      exit 1
    fi
    docker build --platform "${DOCKER_PLATFORM:-linux/amd64}" -t "${img}:local" "${dir}"
  done
}

load_images_into_kind() {
  require_cmd kind
  echo "==> kind load docker-image"
  for img in "${DOCKER_IMAGES[@]}"; do
    kind load docker-image "${img}:local" --name "${KIND_CLUSTER_NAME}"
  done
}

helm_deploy_kind() {
  require_cmd helm
  if [[ "${HELM_WAIT:-false}" == "true" ]]; then
    helm upgrade --install "${HELM_RELEASE}" "${HELM_CHART}" \
      -f "${HELM_CHART}/values.yaml" \
      -f "${HELM_CHART}/values-kind.yaml" \
      --namespace "${HELM_NAMESPACE}" \
      --create-namespace \
      --wait --timeout 25m
  else
    helm upgrade --install "${HELM_RELEASE}" "${HELM_CHART}" \
      -f "${HELM_CHART}/values.yaml" \
      -f "${HELM_CHART}/values-kind.yaml" \
      --namespace "${HELM_NAMESPACE}" \
      --create-namespace
  fi
}

helm_deploy_eks() {
  require_cmd helm
  local cert_arn="${HELM_CERTIFICATE_ARN:-}"
  local -a helm_args=(
    upgrade --install "${HELM_RELEASE}" "${HELM_CHART}"
    -f "${HELM_CHART}/values.yaml"
    -f "${HELM_CHART}/values-eks.yaml"
    --namespace "${HELM_NAMESPACE}"
    --create-namespace
    --rollback-on-failure
    --server-side=false
    --take-ownership
    --wait --timeout 45m
  )
  if [[ -n "${cert_arn}" ]]; then
    helm_args+=(--set-string "ingress.annotations.alb\.ingress\.kubernetes\.io/certificate-arn=${cert_arn}")
  fi
  helm "${helm_args[@]}"
}

# Backward-compatible alias
build_p0_images() {
  build_all_images
}
