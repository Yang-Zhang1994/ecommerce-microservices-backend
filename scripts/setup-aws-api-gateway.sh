#!/usr/bin/env bash
# Create AWS API Gateway HTTP API matching gulimall-gateway routes.
# Requires: AWS CLI, credentials configured (aws configure or env vars).
#
# Set backend base URLs (no trailing slash) before running, e.g.:
#   export RENREN_FAST_URL="https://your-alb.us-east-2.elb.amazonaws.com"
#   export COUPON_URL="https://coupon-alb..."
#   export MEMBER_URL="https://member-alb..."
#   export ORDER_URL="https://order-alb..."
#   export WARE_URL="https://ware-alb..."
#   export PRODUCT_URL="https://product-alb..."
#   export THIRD_PARTY_URL="https://third-party-alb..."
#   export AWS_REGION="us-east-2"
#   ./scripts/setup-aws-api-gateway.sh

set -e
REGION="${AWS_REGION:-us-east-2}"
API_NAME="${API_NAME:-ecommerce-api}"

# Placeholder URLs (valid format so API Gateway accepts; replace in console with real backends)
RENREN_FAST_URL="${RENREN_FAST_URL:-https://placeholder.example.com}"
COUPON_URL="${COUPON_URL:-https://placeholder.example.com}"
MEMBER_URL="${MEMBER_URL:-https://placeholder.example.com}"
ORDER_URL="${ORDER_URL:-https://placeholder.example.com}"
WARE_URL="${WARE_URL:-https://placeholder.example.com}"
PRODUCT_URL="${PRODUCT_URL:-https://placeholder.example.com}"
THIRD_PARTY_URL="${THIRD_PARTY_URL:-https://placeholder.example.com}"

echo "Creating HTTP API: $API_NAME in $REGION"

# 1. Create API with CORS (* origins, * headers, all methods, maxAge 3600; AllowCredentials=false when origin is *)
CORS='{"AllowOrigins":["*"],"AllowMethods":["GET","POST","PUT","PATCH","DELETE","OPTIONS","HEAD"],"AllowHeaders":["*"],"AllowCredentials":false,"MaxAge":3600}'
API_ID=$(aws apigatewayv2 create-api \
  --name "$API_NAME" \
  --protocol-type HTTP \
  --cors-configuration "$CORS" \
  --region "$REGION" \
  --query 'ApiId' \
  --output text)

echo "Created API: $API_ID"

# 2. Create integrations (HTTP_PROXY to backends)
INTEGRATION_CAPTCHA=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${RENREN_FAST_URL}/renren-fast/captcha.jpg" \
  --integration-method GET \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_RENREN=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${RENREN_FAST_URL}/renren-fast/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_COUPON=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${COUPON_URL}/coupon/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_MEMBER=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${MEMBER_URL}/member/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_ORDER=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${ORDER_URL}/order/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_WARE=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${WARE_URL}/ware/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_PRODUCT=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${PRODUCT_URL}/product/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

INTEGRATION_THIRD_PARTY=$(aws apigatewayv2 create-integration \
  --api-id "$API_ID" \
  --integration-type HTTP_PROXY \
  --integration-uri "${THIRD_PARTY_URL}/third-party/{proxy}" \
  --integration-method ANY \
  --payload-format-version "1.0" \
  --region "$REGION" \
  --query 'IntegrationId' --output text)

# 3. Create routes (order: specific first, catch-all last)
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "GET /captcha.jpg"        --target "integrations/$INTEGRATION_CAPTCHA"     --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/coupon/{proxy+}" --target "integrations/$INTEGRATION_COUPON"      --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/member/{proxy+}" --target "integrations/$INTEGRATION_MEMBER"      --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/order/{proxy+}"  --target "integrations/$INTEGRATION_ORDER"       --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/ware/{proxy+}"   --target "integrations/$INTEGRATION_WARE"        --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/product/{proxy+}" --target "integrations/$INTEGRATION_PRODUCT"    --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/third-party/{proxy+}" --target "integrations/$INTEGRATION_THIRD_PARTY" --region "$REGION" > /dev/null
aws apigatewayv2 create-route --api-id "$API_ID" --route-key "ANY /api/{proxy+}"       --target "integrations/$INTEGRATION_RENREN"      --region "$REGION" > /dev/null

echo "Routes created."

# 4. Create stage
aws apigatewayv2 create-stage \
  --api-id "$API_ID" \
  --stage-name "default" \
  --auto-deploy \
  --region "$REGION" > /dev/null

INVOKE_URL="https://${API_ID}.execute-api.${REGION}.amazonaws.com/default"
echo ""
echo "Done. API Gateway invoke URL: $INVOKE_URL"
echo "Update your front-end to use: $INVOKE_URL"
echo ""
echo "If you used REPLACE_* URLs, update integrations in AWS Console (API Gateway -> Integrations) with real backend URLs."
