#!/usr/bin/env bash
# Create kind cluster, build/load all Java service images. Run kind-deploy.sh after secrets.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_cmd kind
require_cmd kubectl

KIND_CONFIG="${ROOT_DIR}/k8s/kind/kind-config.yaml"

if kind get clusters 2>/dev/null | grep -qx "${KIND_CLUSTER_NAME}"; then
  echo "kind cluster '${KIND_CLUSTER_NAME}' already exists"
else
  echo "==> Creating kind cluster '${KIND_CLUSTER_NAME}'"
  kind create cluster --name "${KIND_CLUSTER_NAME}" --config "${KIND_CONFIG}"
fi

build_all_images
load_images_into_kind

echo ""
echo "Next:"
echo "  ./k8s/scripts/k8s-create-secrets.sh"
echo "  ./k8s/scripts/kind-deploy.sh"
echo ""
echo "Gateway: http://localhost:3088/actuator/health"
