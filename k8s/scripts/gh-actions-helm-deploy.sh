#!/usr/bin/env bash
# Helm upgrade on EKS with immutable image tag (git SHA). Used by GitHub Actions CD.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

IMAGE_TAG="${IMAGE_TAG:?IMAGE_TAG required}"
AWS_REGION="${AWS_REGION:-us-west-2}"
export AWS_REGION AWS_DEFAULT_REGION="${AWS_REGION}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-gulimall-prod-eks}"
HELM_CERTIFICATE_ARN="${HELM_CERTIFICATE_ARN:-}"
ATOMIC="${HELM_ATOMIC:-true}"

require_cmd aws
require_cmd kubectl
require_cmd helm

echo "==> kubectl context (cluster=${EKS_CLUSTER_NAME})"
aws eks update-kubeconfig --region "${AWS_REGION}" --name "${EKS_CLUSTER_NAME}"

echo "==> Helm upgrade (imageTag=${IMAGE_TAG}, atomic=${ATOMIC})"
helm_args=(
  upgrade --install "${HELM_RELEASE}" "${HELM_CHART}"
  -f "${HELM_CHART}/values.yaml"
  -f "${HELM_CHART}/values-eks.yaml"
  --namespace "${HELM_NAMESPACE}"
  --create-namespace
  --set "global.imageTag=${IMAGE_TAG}"
  --wait --timeout 45m
)

if [[ "${ATOMIC}" == "true" ]]; then
  helm_args+=(--atomic)
fi

if [[ -n "${HELM_CERTIFICATE_ARN}" ]]; then
  helm_args+=(--set-string "ingress.annotations.alb\.ingress\.kubernetes\.io/certificate-arn=${HELM_CERTIFICATE_ARN}")
fi

helm "${helm_args[@]}"

echo ""
echo "==> Rollback (if needed):"
echo "  helm history ${HELM_RELEASE} -n ${HELM_NAMESPACE}"
echo "  helm rollback ${HELM_RELEASE} <revision> -n ${HELM_NAMESPACE}"
kubectl get pods -n "${HELM_NAMESPACE}"
