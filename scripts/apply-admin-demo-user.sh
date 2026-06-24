#!/usr/bin/env bash
# Create/update read-only admin user `demo` in ecommerce_admin (RDS).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env}"
SQL_FILE="${ROOT_DIR}/docs/sql/admin_demo_reviewer_user.sql"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "error: ${ENV_FILE} not found" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

: "${RDS_ENDPOINT:?RDS_ENDPOINT missing}"
: "${RDS_USERNAME:?RDS_USERNAME missing}"
: "${RDS_PASSWORD:?RDS_PASSWORD missing}"

export PGPASSWORD="${RDS_PASSWORD}"

echo "==> Apply demo reviewer user (ecommerce_admin @ ${RDS_ENDPOINT})"
psql "host=${RDS_ENDPOINT} port=5432 dbname=ecommerce_admin user=${RDS_USERNAME} sslmode=require" \
  -v ON_ERROR_STOP=1 \
  -f "${SQL_FILE}"

echo "Done. Admin login: demo / Demo2025! (read-only role)"
