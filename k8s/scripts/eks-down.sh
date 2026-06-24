#!/usr/bin/env bash
# Scale EKS nodes to 0 (save EC2) or destroy the EKS module entirely.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
TERRAFORM_DIR="${ROOT_DIR}/infra/terraform/environments/prod"
MODE="${1:-scale}"

require_cmd terraform

case "${MODE}" in
  scale)
    echo "==> Scaling EKS node group to 0 (control plane ~\$73/mo still applies)"
    (
      cd "${TERRAFORM_DIR}"
      terraform apply -var="enable_eks=true" -var="eks_node_desired_size=0" -var="eks_node_min_size=0"
    )
    ;;
  destroy)
    echo "==> Destroying EKS module (cluster + nodes)"
    (
      cd "${TERRAFORM_DIR}"
      terraform destroy -target=module.eks
    )
    ;;
  *)
    echo "usage: $0 [scale|destroy]" >&2
    exit 1
    ;;
esac
