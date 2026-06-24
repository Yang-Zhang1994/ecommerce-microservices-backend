#!/usr/bin/env bash
# Stop Compose (frees RAM), delete kind cluster, rebuild images, deploy full mall API stack.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Stopping docker-compose.app.yml (if running)"
if docker compose -f "${ROOT_DIR}/docker-compose.app.yml" ps -q 2>/dev/null | grep -q .; then
  docker compose -f "${ROOT_DIR}/docker-compose.app.yml" down
else
  echo "compose not running"
fi

echo "==> Deleting kind cluster '${KIND_CLUSTER_NAME:-gulimall}'"
if kind get clusters 2>/dev/null | grep -qx "${KIND_CLUSTER_NAME:-gulimall}"; then
  kind delete cluster --name "${KIND_CLUSTER_NAME:-gulimall}"
else
  echo "no kind cluster to delete"
fi

"${SCRIPT_DIR}/kind-up.sh"
"${SCRIPT_DIR}/k8s-create-secrets.sh"
"${SCRIPT_DIR}/kind-deploy.sh"

echo ""
echo "==> Waiting for gateway (up to 3 min)"
for i in $(seq 1 36); do
  if curl -sf http://localhost:3088/actuator/health >/dev/null 2>&1; then
    echo "Gateway OK: http://localhost:3088/actuator/health"
    kubectl get pods -n gulimall
    exit 0
  fi
  sleep 5
done

echo "Gateway not ready yet — check: kubectl get pods -n gulimall"
kubectl get pods -n gulimall
exit 1
