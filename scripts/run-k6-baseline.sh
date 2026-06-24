#!/usr/bin/env bash
# Run k6 baseline against prod domain or ALB. Requires: brew install k6
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="${BASE_URL:-https://www.yangzhangtech.online}"
DURATION="${K6_DURATION:-2m}"

if ! command -v k6 >/dev/null 2>&1; then
  echo "Install k6: brew install k6"
  exit 1
fi

echo "Baseline load test → ${BASE} (duration=${DURATION})"
echo "Thresholds: error rate <1%, p95 <800ms (see scripts/k6/baseline.js)"
BASE_URL="$BASE" K6_DURATION="$DURATION" k6 run "$ROOT/scripts/k6/baseline.js"
