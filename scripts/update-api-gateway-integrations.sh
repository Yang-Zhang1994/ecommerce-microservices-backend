#!/usr/bin/env bash
# Update AWS API Gateway integrations to point at a single gateway URL (e.g. ngrok).
# Use when running gulimall-gateway locally and exposing it via ngrok.
#
# Usage:
#   export GATEWAY_URL="https://YOUR-NGROK-URL.ngrok-free.app"   # no trailing slash
#   ./scripts/update-api-gateway-integrations.sh
#
# Or inline:
#   GATEWAY_URL="https://abc123.ngrok-free.app" ./scripts/update-api-gateway-integrations.sh

set -e

API_ID="${API_ID:-3gdalo7qok}"
REGION="${AWS_REGION:-us-east-2}"

if [ -z "$GATEWAY_URL" ]; then
  echo "Error: GATEWAY_URL is required (e.g. https://xxx.ngrok-free.app)"
  echo "  export GATEWAY_URL=\"https://YOUR-NGROK-URL.ngrok-free.app\""
  exit 1
fi

# Remove trailing slash
GATEWAY_URL="${GATEWAY_URL%/}"

echo "Updating API $API_ID integrations in $REGION to use $GATEWAY_URL"

# Integration ID -> new URI (route mapping: captcha, product, coupon, etc.)
update_one() {
  local int_id="$1"
  local uri="$2"
  echo "  Updating $int_id -> $uri"
  aws apigatewayv2 update-integration \
    --api-id "$API_ID" \
    --integration-id "$int_id" \
    --integration-uri "$uri" \
    --region "$REGION" \
    --output text --query 'IntegrationUri' 2>/dev/null || {
    echo "  Failed to update $int_id"
    exit 1
  }
}

update_one "x2859e8" "${GATEWAY_URL}/captcha.jpg"
update_one "08cfa0g" "${GATEWAY_URL}/api/product/{proxy}"
update_one "2u0ugup" "${GATEWAY_URL}/api/coupon/{proxy}"
update_one "zi1l8ii" "${GATEWAY_URL}/api/member/{proxy}"
update_one "gumgmpd" "${GATEWAY_URL}/api/order/{proxy}"
update_one "5zhj5br" "${GATEWAY_URL}/api/ware/{proxy}"
update_one "0yln13g" "${GATEWAY_URL}/api/third-party/{proxy}"
update_one "gnnsi8n" "${GATEWAY_URL}/api/{proxy}"

echo ""
echo "Done. API Gateway invoke URL: https://${API_ID}.execute-api.${REGION}.amazonaws.com/default"
echo "Point your Vue admin base URL to: https://${API_ID}.execute-api.${REGION}.amazonaws.com/default"
