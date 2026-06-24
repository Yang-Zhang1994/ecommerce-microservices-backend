#!/usr/bin/env bash
# Build all Java/Docker images and push to ECR (full gulimall stack).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

AWS_REGION="${EKS_AWS_REGION:-us-west-2}"
export AWS_REGION AWS_DEFAULT_REGION="${AWS_REGION}"
ACCOUNT_ID="${ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}"
REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
TAG="${TAG:-latest}"

require_cmd aws
require_cmd docker

echo "==> ECR login (${REGISTRY})"
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${REGISTRY}"

build_all_images

echo "==> Tag and push (${#DOCKER_IMAGES[@]} images, tag=${TAG})"
for img in "${DOCKER_IMAGES[@]}"; do
  remote="${REGISTRY}/${img}:${TAG}"
  docker tag "${img}:local" "${remote}"
  docker push "${remote}"
  echo "  pushed ${remote}"
done

echo "Done. Registry: ${REGISTRY}"
