#!/usr/bin/env bash
# k6 load tests against the public gateway (EKS ALB or kind NodePort).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOAD_DIR="${SCRIPT_DIR}/load-test"
RESULTS_DIR="${RESULTS_DIR:-${LOAD_DIR}/results}"
BASE_URL="${BASE_URL:-https://www.yangzhangtech.online}"

if ! command -v k6 >/dev/null 2>&1; then
  echo "Install k6: brew install k6"
  exit 1
fi

mkdir -p "${RESULTS_DIR}"
TS="$(date +%Y%m%d-%H%M%S)"

echo "==> Preflight ${BASE_URL}/actuator/health"
HTTP_CODE="$(curl -s -o /dev/null -w "%{http_code}" --max-time 20 "${BASE_URL}/actuator/health" || echo "000")"
if [[ "${HTTP_CODE}" != "200" ]]; then
  echo "error: gateway returned HTTP ${HTTP_CODE}."
  echo "  EKS: ./k8s/scripts/eks-up.sh  (or scale node group) and wait for pods Ready."
  echo "  kind: ./k8s/scripts/kind-up.sh && BASE_URL=http://localhost:30888 $0"
  exit 1
fi

export BASE_URL

run_one() {
  local script="$1"
  local base
  base="$(basename "${script}" .js)"
  echo ""
  echo "==> k6 run ${base} (BASE_URL=${BASE_URL})"
  k6 run \
    --summary-export="${RESULTS_DIR}/${TS}-${base}-summary.json" \
    "${script}"
}

run_one "${LOAD_DIR}/k6-smoke.js"
run_one "${LOAD_DIR}/k6-search-sustained.js"
run_one "${LOAD_DIR}/k6-product-read.js"

echo ""
echo "Done. JSON summaries: ${RESULTS_DIR}/${TS}-*-summary.json"
echo "Update docs/load-test.md § Recorded results with http_reqs rate and p(95) from the k6 summary."
