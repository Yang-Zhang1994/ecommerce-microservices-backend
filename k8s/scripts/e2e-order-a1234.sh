#!/usr/bin/env bash
# Full mall order flow via gateway (kind NodePort 3088). Requires: curl, python3, optional playwright for Stripe UI.
set -euo pipefail

API="${API_BASE:-http://localhost:3088}"
MALL="${MALL_ORIGIN:-http://localhost:3001}"
COOKIE_JAR="$(mktemp)"
trap 'rm -f "$COOKIE_JAR"' EXIT

# Optional: hit ALB by DNS name with Host header (when public DNS is stale locally).
CURL_EXTRA=()
if [[ -n "${GATEWAY_HOST:-}" ]]; then
  CURL_EXTRA+=(-H "Host: ${GATEWAY_HOST}")
fi
if [[ -n "${API_RESOLVE:-}" ]]; then
  CURL_EXTRA+=(--resolve "${API_RESOLVE}")
fi

api_curl() {
  curl "${CURL_EXTRA[@]}" "$@"
}

json_field() {
  python3 -c "import sys,json; d=json.load(sys.stdin); print($1)" 2>/dev/null || true
}

echo "==> Login a1234"
LOGIN=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "$API/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"a1234","password":"123456"}')
echo "$LOGIN" | python3 -m json.tool | head -8
CODE=$(echo "$LOGIN" | json_field "d['code']")
if [[ "$CODE" != "0" ]]; then echo "login failed"; exit 1; fi

echo "==> Add SKU 1 to cart"
api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" "$API/api/cart/add?skuId=1&num=1" -o /dev/null -w "cart add: %{http_code}\n"

echo "==> Order confirm"
CONFIRM=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" "$API/api/order/confirm")
TOKEN=$(echo "$CONFIRM" | json_field "d['data']['orderToken']")
ADDR=$(echo "$CONFIRM" | json_field "d['data']['addresses'][0]['id']")
echo "orderToken=$TOKEN addressId=$ADDR"

echo "==> Submit order"
SUBMIT=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "$API/api/order/submit" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: e2e-$(date +%s)" \
  -d "{\"orderToken\":\"$TOKEN\",\"addressId\":$ADDR,\"payType\":1,\"note\":\"e2e\"}")
ORDER_SN=$(echo "$SUBMIT" | json_field "d['data']['orderSn']")
echo "$SUBMIT" | python3 -m json.tool | head -12
echo "orderSn=$ORDER_SN"

echo "==> Cart after submit"
CART=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" "$API/api/cart/current")
echo "$CART" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data') or {}; items=d.get('items') or []; print('cart items',len(items))"

echo "==> Stripe checkout session"
CHECKOUT=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "$API/api/order/pay/stripe/checkout-session" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: stripe-$(date +%s)" \
  -d "{\"orderSn\":\"$ORDER_SN\"}")
CHECKOUT_URL=$(echo "$CHECKOUT" | json_field "d['data']['checkoutUrl']")
echo "checkoutUrl=$CHECKOUT_URL"

if [[ -n "${SKIP_STRIPE_UI:-}" ]]; then
  echo "SKIP_STRIPE_UI set — stop before card UI"
  exit 0
fi

if command -v npx >/dev/null 2>&1 && [[ -d gulimall-mall ]]; then
  echo "==> Playwright Stripe test card"
  (cd gulimall-mall && CHECKOUT_URL="$CHECKOUT_URL" npx playwright test e2e/stripe-checkout-once.spec.ts --reporter=line)
else
  echo "Run manually: open $CHECKOUT_URL and pay with 4242 4242 4242 4242"
  exit 0
fi

echo "==> Order detail"
DETAIL=$(api_curl -sf -c "$COOKIE_JAR" -b "$COOKIE_JAR" "$API/api/order/detail/$ORDER_SN")
echo "$DETAIL" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print('status',d.get('status'),'paymentTime',d.get('paymentTime'))"

echo "==> Search list"
api_curl -sf "$API/api/search/product/list?pageNum=1&pageSize=3" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print('search total',d['total'])"

echo "==> Admin login"
api_curl -sf -X POST "$API/api/sys/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}' | python3 -c "import sys,json; r=json.load(sys.stdin); print('admin',r.get('code'),r.get('token','')[:16])"

echo "DONE orderSn=$ORDER_SN"
