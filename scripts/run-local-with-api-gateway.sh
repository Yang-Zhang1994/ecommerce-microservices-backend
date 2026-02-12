#!/usr/bin/env bash
# Run local setup: ngrok tunnel to gulimall-gateway (port 88) and update API Gateway integrations.
#
# Prerequisites:
#   - gulimall-gateway running on port 88
#   - Consul running (localhost:8500) with services registered (renren-fast, gulimall-product, etc.)
#   - ngrok installed (brew install ngrok)
#   - AWS CLI configured (for update-api-gateway-integrations.sh)
#
# Usage:
#   1. Start gulimall-gateway: ./mvnw spring-boot:run -pl gulimall-gateway
#   2. Start ngrok in another terminal: ngrok http 88
#   3. Run this script with your ngrok URL:
#        GATEWAY_URL="https://YOUR-NGROK-URL.ngrok-free.app" ./scripts/run-local-with-api-gateway.sh
#
# Or use auto-detect (requires ngrok already running):
#   ./scripts/run-local-with-api-gateway.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z "$GATEWAY_URL" ]; then
  # Try to get ngrok URL from local API (http://127.0.0.1:4040/api/tunnels)
  if command -v curl >/dev/null 2>&1; then
    NGROK_JSON=$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null || true)
    if [ -n "$NGROK_JSON" ] && echo "$NGROK_JSON" | grep -q '"public_url"'; then
      GATEWAY_URL=$(echo "$NGROK_JSON" | grep -o '"public_url":"https://[^"]*"' | head -1 | cut -d'"' -f4)
    fi
  fi
  if [ -z "$GATEWAY_URL" ]; then
    echo "Error: GATEWAY_URL not set and could not auto-detect from ngrok."
    echo ""
    echo "1. Start ngrok: ngrok http 88"
    echo "2. Copy the https URL (e.g. https://abc123.ngrok-free.app)"
    echo "3. Run: GATEWAY_URL=\"https://YOUR-URL.ngrok-free.app\" $0"
    exit 1
  fi
fi

echo "Using gateway URL: $GATEWAY_URL"
export GATEWAY_URL
"$SCRIPT_DIR/update-api-gateway-integrations.sh"
