#!/usr/bin/env bash
# Full stack on EKS: Terraform (cluster + ECR + RDS SG) → push images → ALB controller → Helm.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

TERRAFORM_DIR="${ROOT_DIR}/infra/terraform/environments/prod"
# EKS / ECR / ACM for this stack are in us-west-2 (see terraform.tfvars aws_region).
export AWS_REGION="${EKS_AWS_REGION:-us-west-2}"
export AWS_DEFAULT_REGION="${AWS_REGION}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-gulimall-prod-eks}"
SKIP_TERRAFORM="${SKIP_TERRAFORM:-false}"
SKIP_ECR_PUSH="${SKIP_ECR_PUSH:-false}"
SKIP_ALB_CONTROLLER="${SKIP_ALB_CONTROLLER:-false}"

require_cmd terraform
require_cmd aws
require_cmd kubectl
require_cmd helm
require_cmd docker

if [[ "${SKIP_TERRAFORM}" != "true" ]]; then
  echo "==> Terraform apply (enable_eks=true, full-stack ECR + RDS SG rule)"
  (
    cd "${TERRAFORM_DIR}"
    terraform apply \
      -var="enable_eks=true" \
      -var="rds_security_group_id=${RDS_SECURITY_GROUP_ID:-sg-0b90bf2ba9a126496}" \
      -var="eks_node_instance_types=[\"t3.large\"]" \
      -var="eks_node_desired_size=2" \
      -var="eks_node_min_size=1" \
      -var="eks_node_max_size=3" \
      -var="eks_cluster_version=${EKS_CLUSTER_VERSION:-1.31}" \
      -auto-approve
  )
  HELM_CERTIFICATE_ARN="$(cd "${TERRAFORM_DIR}" && terraform output -raw certificate_arn_in_use 2>/dev/null || true)"
  export HELM_CERTIFICATE_ARN
  if [[ -n "${HELM_CERTIFICATE_ARN}" ]]; then
    echo "ACM certificate: ${HELM_CERTIFICATE_ARN}"
  else
    echo "warn: no ACM certificate_arn_in_use — Ingress HTTPS may fail until cert is issued"
  fi
fi

if [[ -z "${HELM_CERTIFICATE_ARN:-}" ]]; then
  HELM_CERTIFICATE_ARN="$(cd "${TERRAFORM_DIR}" && terraform output -raw certificate_arn_in_use 2>/dev/null || true)"
  export HELM_CERTIFICATE_ARN
fi

echo "==> Configure kubectl"
aws eks update-kubeconfig --region "${AWS_REGION}" --name "${EKS_CLUSTER_NAME}"

if [[ "${SKIP_ALB_CONTROLLER}" != "true" ]]; then
  if ! kubectl get deployment -n kube-system aws-load-balancer-controller >/dev/null 2>&1; then
    echo "==> Install AWS Load Balancer Controller"
    "${SCRIPT_DIR}/eks-install-alb-controller.sh"
  else
    echo "==> ALB controller already installed"
  fi
fi

if [[ "${SKIP_ECR_PUSH}" != "true" ]]; then
  echo "==> Build and push all images to ECR"
  "${SCRIPT_DIR}/ecr-push-all.sh"
fi

echo "==> Kubernetes secrets (from .env)"
"${SCRIPT_DIR}/k8s-create-secrets.sh"

echo "==> Helm deploy (values-eks.yaml — full stack)"
helm_deploy_eks

echo ""
echo "==> Pods"
kubectl get pods -n "${HELM_NAMESPACE}"
echo ""
echo "==> Ingress (ALB may take 2–3 minutes)"
kubectl get ingress -n "${HELM_NAMESPACE}"
echo ""
echo "Site: https://www.yangzhangtech.online (after ALB + DNS)"
echo "Scale down nodes: ${SCRIPT_DIR}/eks-down.sh scale"
