#!/usr/bin/env bash
# Install AWS Load Balancer Controller with IRSA (required for Ingress → ALB on EKS).
set -euo pipefail

AWS_REGION="${EKS_AWS_REGION:-us-west-2}"
export AWS_REGION AWS_DEFAULT_REGION="${AWS_REGION}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-gulimall-prod-eks}"
CONTROLLER_VERSION="${CONTROLLER_VERSION:-v2.7.2}"
SA_NAMESPACE="kube-system"
SA_NAME="aws-load-balancer-controller"
ROLE_NAME="${ALB_CONTROLLER_ROLE_NAME:-AmazonEKSLoadBalancerControllerRole}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "error: $1 required" >&2; exit 1; }
}

require_cmd helm
require_cmd aws
require_cmd kubectl

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
VPC_ID="$(aws eks describe-cluster --name "${EKS_CLUSTER_NAME}" --region "${AWS_REGION}" \
  --query 'cluster.resourcesVpcConfig.vpcId' --output text)"
OIDC_ISSUER="$(aws eks describe-cluster --name "${EKS_CLUSTER_NAME}" --region "${AWS_REGION}" \
  --query 'cluster.identity.oidc.issuer' --output text)"
OIDC_PROVIDER="${OIDC_ISSUER#https://}"

echo "==> IAM policy (skip if already exists)"
POLICY_ARN="arn:aws:iam::${ACCOUNT_ID}:policy/AWSLoadBalancerControllerIAMPolicy"
if ! aws iam get-policy --policy-arn "${POLICY_ARN}" >/dev/null 2>&1; then
  curl -sS -o /tmp/iam_policy.json \
    "https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/${CONTROLLER_VERSION}/docs/install/iam_policy.json"
  aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file:///tmp/iam_policy.json
fi

echo "==> OIDC provider for IRSA"
OIDC_ARN="arn:aws:iam::${ACCOUNT_ID}:oidc-provider/${OIDC_PROVIDER}"
if ! aws iam get-open-id-connect-provider --open-id-connect-provider-arn "${OIDC_ARN}" >/dev/null 2>&1; then
  # EKS OIDC root CA thumbprint (valid for all EKS clusters in a region)
  aws iam create-open-id-connect-provider \
    --url "${OIDC_ISSUER}" \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list A84554E91B2F69F6035A36B183074726B12FFA9C
  echo "Created OIDC provider ${OIDC_ARN}"
else
  echo "OIDC provider already exists"
fi

echo "==> IRSA role ${ROLE_NAME}"
TRUST_POLICY="$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "${OIDC_ARN}"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "${OIDC_PROVIDER}:aud": "sts.amazonaws.com",
          "${OIDC_PROVIDER}:sub": "system:serviceaccount:${SA_NAMESPACE}:${SA_NAME}"
        }
      }
    }
  ]
}
EOF
)"

if ! aws iam get-role --role-name "${ROLE_NAME}" >/dev/null 2>&1; then
  aws iam create-role \
    --role-name "${ROLE_NAME}" \
    --assume-role-policy-document "${TRUST_POLICY}"
  aws iam attach-role-policy \
    --role-name "${ROLE_NAME}" \
    --policy-arn "${POLICY_ARN}"
  echo "Created role ${ROLE_NAME}"
else
  aws iam update-assume-role-policy \
    --role-name "${ROLE_NAME}" \
    --policy-document "${TRUST_POLICY}"
  echo "Role ${ROLE_NAME} already exists (trust policy updated)"
fi

ROLE_ARN="arn:aws:iam::${ACCOUNT_ID}:role/${ROLE_NAME}"

helm repo add eks https://aws.github.io/eks-charts 2>/dev/null || true
helm repo update

echo "==> Helm install aws-load-balancer-controller (IRSA)"
helm upgrade --install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n "${SA_NAMESPACE}" \
  --set clusterName="${EKS_CLUSTER_NAME}" \
  --set serviceAccount.create=true \
  --set serviceAccount.name="${SA_NAME}" \
  --set serviceAccount.annotations."eks\.amazonaws\.com/role-arn"="${ROLE_ARN}" \
  --set region="${AWS_REGION}" \
  --set vpcId="${VPC_ID}" \
  --version 1.7.2

echo "ALB controller installed with IRSA ${ROLE_ARN}"
echo "Verify: kubectl -n ${SA_NAMESPACE} get deployment aws-load-balancer-controller"
