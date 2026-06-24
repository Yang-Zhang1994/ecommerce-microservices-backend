# GitHub Actions OIDC → IAM role for ECR push + EKS Helm deploy (no long-lived AWS keys in repo).
# Enable: terraform apply -var="enable_eks=true" -var="enable_github_actions_cd=true"

data "tls_certificate" "github_actions" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0
  url   = "https://token.actions.githubusercontent.com"
}

resource "aws_iam_openid_connect_provider" "github_actions" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0

  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.github_actions[0].certificates[0].sha1_fingerprint]

  tags = local.common_tags
}

resource "aws_iam_role" "github_actions_cd" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0

  name = "gulimall-github-actions-cd"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github_actions[0].arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_repository}:*"
          }
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy" "github_actions_cd" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0

  name = "gulimall-github-actions-cd"
  role = aws_iam_role.github_actions_cd[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ECRAuth"
        Effect = "Allow"
        Action = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Sid    = "ECRPush"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:CompleteLayerUpload",
          "ecr:InitiateLayerUpload",
          "ecr:PutImage",
          "ecr:UploadLayerPart",
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer",
        ]
        Resource = [
          for repo in local.gulimall_ecr_repositories :
          "arn:aws:ecr:${var.aws_region}:${var.account_id}:repository/${repo}"
        ]
      },
      {
        Sid    = "EKSDescribe"
        Effect = "Allow"
        Action = ["eks:DescribeCluster"]
        Resource = module.eks[0].cluster_arn
      }
    ]
  })
}

resource "aws_eks_access_entry" "github_actions_cd" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0

  cluster_name  = module.eks[0].cluster_name
  principal_arn = aws_iam_role.github_actions_cd[0].arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "github_actions_cd" {
  count = var.enable_eks && var.enable_github_actions_cd ? 1 : 0

  cluster_name  = module.eks[0].cluster_name
  principal_arn = aws_iam_role.github_actions_cd[0].arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }
}
