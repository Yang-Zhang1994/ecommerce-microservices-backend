#!/usr/bin/env bash
# Create gulimall-secrets from project-root .env (RDS + Stripe). Does not print secrets.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env}"
NAMESPACE="${NAMESPACE:-gulimall}"
SECRET_NAME="${SECRET_NAME:-gulimall-secrets}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "error: ${ENV_FILE} not found. Copy .env.example and fill RDS_* and ORDER_STRIPE_*." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

: "${RDS_ENDPOINT:?RDS_ENDPOINT missing in .env}"
: "${RDS_USERNAME:?RDS_USERNAME missing in .env}"
: "${RDS_PASSWORD:?RDS_PASSWORD missing in .env}"

RABBIT_USER="${SPRING_RABBITMQ_USERNAME:-guest}"
RABBIT_PASS="${SPRING_RABBITMQ_PASSWORD:-guest}"
STRIPE_KEY="${ORDER_STRIPE_SECRET_KEY:-}"
STRIPE_WH="${ORDER_STRIPE_WEBHOOK_SECRET:-}"
AWS_KEY="${AWS_ACCESS_KEY_ID:-}"
AWS_SECRET="${AWS_SECRET_ACCESS_KEY:-}"
GOOGLE_ID="${GOOGLE_CLIENT_ID:-local-kind-placeholder}"
GOOGLE_SECRET="${GOOGLE_CLIENT_SECRET:-local-kind-placeholder}"

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
