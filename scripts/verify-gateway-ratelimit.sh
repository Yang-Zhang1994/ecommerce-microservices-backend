#!/usr/bin/env bash
# Expect 429 from gateway Redis rate limiter after burst (default test: 5/s, burst 10 on EC2 deploy).
set -euo pipefail

BASE="${1:-http://ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com}"
PATH_API="${2:-/api/auth/ping}"
TOTAL="${3:-25}"

echo "Target: ${BASE}${PATH_API} (${TOTAL} requests)"
ok=0
too_many=0
other=0

for i in $(seq 1 "$TOTAL"); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "${BASE}${PATH_API}" || echo "000")
  case "$code" in
    200|404|401) ok=$((ok + 1)) ;;
    429) too_many=$((too_many + 1)) ;;
    *) other=$((other + 1)); echo "  #$i -> $code" ;;
  esac
done

echo "Results: ok-ish=$ok 429=$too_many other=$other"
if [[ "$too_many" -gt 0 ]]; then
  echo "PASS: rate limiter returned 429"
  exit 0
fi
echo "WARN: no 429 observed (limits may be high or Redis limiter inactive)"
exit 1
