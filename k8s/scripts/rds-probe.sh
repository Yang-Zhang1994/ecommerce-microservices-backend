#!/usr/bin/env bash
# Quick RDS reachability check from your Mac (same path kind pods use).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
NAMESPACE="${HELM_NAMESPACE:-gulimall}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

RDS_HOST="${RDS_ENDPOINT:-ecommerce-db-oregon.cbaoegqoumfp.us-west-2.rds.amazonaws.com}"
PUBLIC_IP="$(curl -sf --max-time 5 https://checkip.amazonaws.com 2>/dev/null | tr -d '[:space:]')"

echo "==> Public IP (for RDS security group): ${PUBLIC_IP:-unknown}"
echo "==> RDS host: ${RDS_HOST}"
echo "==> TCP 5432 from this machine:"
if nc -zv -G 6 "${RDS_HOST}" 5432 2>&1; then
  echo "OK: port 5432 reachable"
else
  echo "FAIL: cannot reach ${RDS_HOST}:5432"
  echo "    - Add ${PUBLIC_IP}/32 to RDS SG sg-0b90bf2ba9a126496 (or run k8s/scripts/rds-allow-my-ip.sh)"
  echo "    - Some networks block outbound 5432; try phone hotspot / VPN"
fi

if command -v kubectl >/dev/null 2>&1 && kubectl get ns "${NAMESPACE}" >/dev/null 2>&1; then
  echo "==> K8s pods needing RDS:"
  kubectl get pods -n "${NAMESPACE}" -o custom-columns=NAME:.metadata.name,READY:.status.containerStatuses[0].ready,RESTARTS:.status.containerStatuses[0].restartCount 2>/dev/null \
    | grep -E 'product|member|order|ware|coupon|renren' || true
fi
