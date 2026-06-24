#!/usr/bin/env bash
# Apply gulimall-secrets from environment variables (GitHub Actions secrets).
# Does not print secret values. Run once or on workflow_dispatch only — not every CD push.
set -euo pipefail

NAMESPACE="${NAMESPACE:-gulimall}"
SECRET_NAME="${SECRET_NAME:-gulimall-secrets}"

: "${RDS_ENDPOINT:?RDS_ENDPOINT missing}"
: "${RDS_USERNAME:?RDS_USERNAME missing}"
: "${RDS_PASSWORD:?RDS_PASSWORD missing}"

RABBIT_USER="${SPRING_RABBITMQ_USERNAME:-guest}"
RABBIT_PASS="${SPRING_RABBITMQ_PASSWORD:-guest}"
STRIPE_KEY="${ORDER_STRIPE_SECRET_KEY:-}"
STRIPE_WH="${ORDER_STRIPE_WEBHOOK_SECRET:-}"
AWS_KEY="${AWS_ACCESS_KEY_ID:-}"
AWS_SECRET="${AWS_SECRET_ACCESS_KEY:-}"
GOOGLE_ID="${GOOGLE_CLIENT_ID:-}"
GOOGLE_SECRET="${GOOGLE_CLIENT_SECRET:-}"

kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${NAMESPACE}" create secret generic "${SECRET_NAME}" \
  --from-literal=rds-endpoint="${RDS_ENDPOINT}" \
  --from-literal=rds-username="${RDS_USERNAME}" \
  --from-literal=rds-password="${RDS_PASSWORD}" \
  --from-literal=rabbitmq-username="${RABBIT_USER}" \
  --from-literal=rabbitmq-password="${RABBIT_PASS}" \
  --from-literal=order-stripe-secret-key="${STRIPE_KEY}" \
  --from-literal=order-stripe-webhook-secret="${STRIPE_WH}" \
  --from-literal=aws-access-key-id="${AWS_KEY}" \
  --from-literal=aws-secret-access-key="${AWS_SECRET}" \
  --from-literal=google-client-id="${GOOGLE_ID}" \
  --from-literal=google-client-secret="${GOOGLE_SECRET}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Secret ${SECRET_NAME} applied in namespace ${NAMESPACE}"
