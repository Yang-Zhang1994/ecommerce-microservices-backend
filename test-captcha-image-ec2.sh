#!/bin/bash
# Test 验证码 (captcha) and image/OSS on EC2. Usage: ./test-captcha-image-ec2.sh <BASE_URL>
# Example: ./test-captcha-image-ec2.sh http://your-ec2-host:88
# Or with ALB: ./test-captcha-image-ec2.sh https://your-alb-domain

BASE_URL="${1:-http://localhost:88}"
echo "=== Testing base URL: $BASE_URL ==="

echo ""
echo "--- 1. Captcha (验证码) GET /api/captcha.jpg?uuid=test-uuid-123 ---"
CAPTCHA_RESP=$(curl -sS -w "\n%{http_code}" -o /tmp/captcha_out.jpg "$BASE_URL/api/captcha.jpg?uuid=test-uuid-123")
HTTP_CODE=$(echo "$CAPTCHA_RESP" | tail -n1)
echo "HTTP status: $HTTP_CODE"
if [ -f /tmp/captcha_out.jpg ]; then
  SIZE=$(wc -c < /tmp/captcha_out.jpg)
  echo "Response size: $SIZE bytes"
  file /tmp/captcha_out.jpg
  if [ "$SIZE" -lt 100 ]; then
    echo "Body (first 500 chars):"
    head -c 500 /tmp/captcha_out.jpg | xxd
  fi
fi

echo ""
echo "--- 2. Captcha without uuid (should error) GET /api/captcha.jpg ---"
curl -sS -w "\nHTTP: %{http_code}\n" -o /tmp/captcha_no_uuid.txt "$BASE_URL/api/captcha.jpg"
echo "Body:"
[ -f /tmp/captcha_no_uuid.txt ] && head -c 400 /tmp/captcha_no_uuid.txt || echo "(no body)"

echo ""
echo "--- 3. Image/OSS list (needs auth) GET /api/sys/oss/list ---"
curl -sS -w "\nHTTP: %{http_code}\n" "$BASE_URL/api/sys/oss/list"
