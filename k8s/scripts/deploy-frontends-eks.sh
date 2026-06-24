#!/usr/bin/env bash
# Build & push mall + admin UI images to ECR, deploy Ingress on EKS.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

export AWS_REGION="${EKS_AWS_REGION:-us-west-2}"
export AWS_DEFAULT_REGION="${AWS_REGION}"
ACCOUNT_ID="${ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}"
REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
TAG="${TAG:-latest}"
API_BASE="${NEXT_PUBLIC_API_BASE:-https://www.yangzhangtech.online}"

require_cmd aws docker kubectl npm

echo "==> ECR login"
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${REGISTRY}"

for repo in gulimall-mall renren-fast-vue; do
  aws ecr describe-repositories --repository-names "${repo}" --region "${AWS_REGION}" >/dev/null 2>&1 \
    || aws ecr create-repository --repository-name "${repo}" --region "${AWS_REGION}"
done

echo "==> Build gulimall-mall (amd64, API=${API_BASE})"
docker build --platform linux/amd64 \
  --build-arg NEXT_PUBLIC_API_BASE="${API_BASE}" \
  -t gulimall-mall:local "${ROOT_DIR}/gulimall-mall"
docker tag gulimall-mall:local "${REGISTRY}/gulimall-mall:${TAG}"
docker push "${REGISTRY}/gulimall-mall:${TAG}"

echo "==> Build renren-fast-vue"
docker build --platform linux/amd64 -t renren-fast-vue:local "${ROOT_DIR}/renren-fast-vue"
docker tag renren-fast-vue:local "${REGISTRY}/renren-fast-vue:${TAG}"
docker push "${REGISTRY}/renren-fast-vue:${TAG}"

echo "==> Apply k8s manifests"
kubectl apply -f "${ROOT_DIR}/k8s/eks/frontends.yaml"

echo "Done. After ALB provisions:"
echo "  Mall:  https://mall.yangzhangtech.online"
echo "  Admin: https://admin.yangzhangtech.online"
echo "Add Route53 A-alias records for mall + admin subdomains to new ALB hostnames (see kubectl get ingress -n gulimall)."
