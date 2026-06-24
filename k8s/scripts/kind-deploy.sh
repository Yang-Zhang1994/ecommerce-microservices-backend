#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_cmd kubectl

if ! kubectl get secret gulimall-secrets -n "${HELM_NAMESPACE}" >/dev/null 2>&1; then
  echo "warning: secret gulimall-secrets not found — run ./k8s/scripts/k8s-create-secrets.sh first"
fi

helm_deploy_kind

echo ""
echo "==> Pods"
kubectl get pods -n "${HELM_NAMESPACE}"
echo ""
echo "Gateway: http://localhost:3088/actuator/health"
