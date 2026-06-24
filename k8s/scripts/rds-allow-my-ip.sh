#!/usr/bin/env bash
# Add your current public IP to the Oregon RDS security group (PostgreSQL 5432).
set -euo pipefail

REGION="${AWS_REGION:-us-west-2}"
SG_ID="${RDS_SECURITY_GROUP_ID:-sg-0b90bf2ba9a126496}"
PUBLIC_IP="$(curl -sf --max-time 8 https://checkip.amazonaws.com | tr -d '[:space:]')"

if [[ -z "${PUBLIC_IP}" ]]; then
  echo "error: could not detect public IP" >&2
  exit 1
fi

CIDR="${PUBLIC_IP}/32"
echo "==> Allowing ${CIDR} on ${SG_ID}:5432 (${REGION})"

if aws ec2 authorize-security-group-ingress \
  --region "${REGION}" \
  --group-id "${SG_ID}" \
  --protocol tcp \
  --port 5432 \
  --cidr "${CIDR}" 2>&1; then
  echo "Added rule for ${CIDR}"
else
  echo "Rule may already exist; continuing..."
fi

"$(dirname "$0")/rds-probe.sh"
