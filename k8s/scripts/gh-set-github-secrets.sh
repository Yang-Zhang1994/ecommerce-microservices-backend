#!/usr/bin/env bash
# Push GitHub Actions secrets from project-root .env (and Terraform CD outputs).
# Does not print secret values. Requires: gh auth login, .env with RDS/Stripe/Google.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env}"
REPO="${GITHUB_REPOSITORY:-Yang-Zhang1994/ecommerce-microservices-backend}"
TF_DIR="${ROOT_DIR}/infra/terraform/environments/prod"

if ! command -v gh >/dev/null 2>&1; then
  echo "error: gh CLI not found. Install: brew install gh && gh auth login" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "error: not logged in. Run: gh auth login" >&2
  exit 1
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "error: ${ENV_FILE} not found. Copy .env.example and fill values." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

AWS_ROLE="${AWS_ROLE_TO_ASSUME:-}"
if [[ -z "${AWS_ROLE}" ]] && [[ -d "${TF_DIR}" ]]; then
  AWS_ROLE="$(cd "${TF_DIR}" && terraform output -raw github_actions_cd_role_arn 2>/dev/null || true)"
fi
AWS_ROLE="${AWS_ROLE:-arn:aws:iam::466297333400:role/gulimall-github-actions-cd}"

HELM_CERT="${HELM_CERTIFICATE_ARN:-arn:aws:acm:us-west-2:466297333400:certificate/db83fba4-4af8-4d09-a469-9555c933ff94}"

set_secret() {
  local name="$1"
  local value="$2"
  if [[ -z "${value}" ]]; then
    echo "skip ${name} (empty)"
    return 0
  fi
  printf '%s' "${value}" | gh secret set "${name}" --repo "${REPO}"
  echo "set ${name}"
}

echo "Repository: ${REPO}"
echo "Source: ${ENV_FILE}"
echo ""

# --- CD (always) ---
set_secret AWS_ROLE_TO_ASSUME "${AWS_ROLE}"
set_secret HELM_CERTIFICATE_ARN "${HELM_CERT}"

# --- sync_secrets workflow (from .env) ---
: "${RDS_ENDPOINT:?RDS_ENDPOINT missing in .env}"
: "${RDS_USERNAME:?RDS_USERNAME missing in .env}"
: "${RDS_PASSWORD:?RDS_PASSWORD missing in .env}"

set_secret RDS_ENDPOINT "${RDS_ENDPOINT}"
set_secret RDS_USERNAME "${RDS_USERNAME}"
set_secret RDS_PASSWORD "${RDS_PASSWORD}"
set_secret ORDER_STRIPE_SECRET_KEY "${ORDER_STRIPE_SECRET_KEY:-}"
set_secret ORDER_STRIPE_WEBHOOK_SECRET "${ORDER_STRIPE_WEBHOOK_SECRET:-}"
set_secret GOOGLE_CLIENT_ID "${GOOGLE_CLIENT_ID:-}"
set_secret GOOGLE_CLIENT_SECRET "${GOOGLE_CLIENT_SECRET:-}"
set_secret AWS_ACCESS_KEY_ID "${AWS_ACCESS_KEY_ID:-}"
set_secret AWS_SECRET_ACCESS_KEY "${AWS_SECRET_ACCESS_KEY:-}"

echo ""
echo "Done. Verify: gh secret list --repo ${REPO}"
echo "Next: gh workflow run cd-eks.yml --repo ${REPO} -f sync_secrets=true"
