#!/usr/bin/env bash
# Sync gateway config to prod EC2 via S3, rebuild redis+consul+gateway, smoke-test rate limit.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGION="${AWS_REGION:-us-west-2}"
INSTANCE_ID="${EC2_INSTANCE_ID:-i-05b954f202863481b}"
BUCKET="${AWS_S3_BUCKET:-ecommerce-uploads-oregon}"
PREFIX="deploy/gulimall/$(date +%Y%m%d%H%M%S)"

echo "==> Upload gateway sources to s3://${BUCKET}/${PREFIX}/"
aws s3 sync "${ROOT}/gulimall-gateway/src" "s3://${BUCKET}/${PREFIX}/gulimall-gateway/src" --region "$REGION"
aws s3 cp "${ROOT}/docker-compose.app.yml" "s3://${BUCKET}/${PREFIX}/docker-compose.app.yml" --region "$REGION"
aws s3 cp "${ROOT}/gulimall-gateway/Dockerfile" "s3://${BUCKET}/${PREFIX}/gulimall-gateway/Dockerfile" --region "$REGION" 2>/dev/null || true

REMOTE_SCRIPT=$(cat <<EOF
set -e
cd /home/ubuntu/gulimall
aws s3 sync s3://${BUCKET}/${PREFIX}/gulimall-gateway/src ./gulimall-gateway/src --region ${REGION}
aws s3 cp s3://${BUCKET}/${PREFIX}/docker-compose.app.yml ./docker-compose.app.yml --region ${REGION}
export GATEWAY_RATELIMIT_API_REPLENISH=\${GATEWAY_RATELIMIT_API_REPLENISH:-5}
export GATEWAY_RATELIMIT_API_BURST=\${GATEWAY_RATELIMIT_API_BURST:-10}
docker compose -f docker-compose.app.yml up -d redis consul
docker compose -f docker-compose.app.yml build gulimall-gateway
docker compose -f docker-compose.app.yml up -d gulimall-gateway
sleep 20
curl -s -o /dev/null -w "health:%{http_code}\\n" http://127.0.0.1:88/actuator/health
EOF
)

echo "==> SSM run on ${INSTANCE_ID}"
CMD_ID=$(aws ssm send-command \
  --region "$REGION" \
  --instance-ids "$INSTANCE_ID" \
  --document-name "AWS-RunShellScript" \
  --timeout-seconds 900 \
  --parameters "commands=[$(echo "$REMOTE_SCRIPT" | python3 -c 'import json,sys; print(json.dumps([sys.stdin.read()]))')]" \
  --query 'Command.CommandId' --output text)

echo "CommandId=${CMD_ID} (waiting...)"
for i in $(seq 1 60); do
  STATUS=$(aws ssm get-command-invocation --command-id "$CMD_ID" --instance-id "$INSTANCE_ID" --region "$REGION" --query Status --output text 2>/dev/null || echo Pending)
  echo "  status=$STATUS"
  if [[ "$STATUS" == "Success" || "$STATUS" == "Failed" ]]; then
    aws ssm get-command-invocation --command-id "$CMD_ID" --instance-id "$INSTANCE_ID" --region "$REGION" \
      --query '{Status:Status,Stdout:StandardOutputContent,Stderr:StandardErrorContent}' --output json
    break
  fi
  sleep 10
done

echo "==> Done. Run rate-limit verify: ./scripts/verify-gateway-ratelimit.sh"
