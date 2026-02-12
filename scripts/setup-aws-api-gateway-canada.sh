#!/usr/bin/env bash
# Run API Gateway setup using Canadian-convention base URL.
# Uses scripts/config/canada-base-url.env (edit BASE_URL there, then run this).
#
# Usage:
#   1. Edit scripts/config/canada-base-url.env and set BASE_URL to your real URL.
#   2. ./scripts/setup-aws-api-gateway-canada.sh

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG="$SCRIPT_DIR/config/canada-base-url.env"

if [[ -f "$CONFIG" ]]; then
  set -a
  source "$CONFIG"
  set +a
fi

# Single base URL for all services (Canadian convention)
BASE="${BASE_URL:-https://api-ecommerce.aurora.ca}"
export RENREN_FAST_URL="$BASE"
export COUPON_URL="$BASE"
export MEMBER_URL="$BASE"
export ORDER_URL="$BASE"
export WARE_URL="$BASE"
export PRODUCT_URL="$BASE"
export THIRD_PARTY_URL="$BASE"

# Canada region
export AWS_REGION="${AWS_REGION:-ca-central-1}"

# API name for Canada deployment
export API_NAME="ecommerce-api"

exec "$SCRIPT_DIR/setup-aws-api-gateway.sh"
