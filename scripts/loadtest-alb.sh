#!/usr/bin/env bash
# Lightweight load test for ALB/gateway (requires hey: brew install hey)
set -euo pipefail

BASE="${1:-http://ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com}"
URL="${BASE}/actuator/health"
CONCURRENCY="${2:-20}"
DURATION="${3:-30s}"

if ! command -v hey >/dev/null 2>&1; then
  echo "Install hey: brew install hey"
  exit 1
fi

echo "Load test ${URL} -c ${CONCURRENCY} -z ${DURATION}"
hey -c "$CONCURRENCY" -z "$DURATION" "$URL"
