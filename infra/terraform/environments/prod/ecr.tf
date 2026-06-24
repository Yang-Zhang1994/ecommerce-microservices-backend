# ECR repositories for full-stack Helm deploy (image name = repository name in values.yaml).
locals {
  gulimall_ecr_repositories = toset([
    "gulimall-gateway",
    "gulimall-auth-server",
    "gulimall-product",
    "gulimall-member",
    "gulimall-cart",
    "gulimall-order",
    "gulimall-ware",
    "gulimall-coupon",
    "gulimall-search",
    "gulimall-seckill",
    "gulimall-third-party",
    "renren-fast",
  ])
}

resource "aws_ecr_repository" "gulimall" {
  for_each = var.enable_eks ? local.gulimall_ecr_repositories : toset([])

  name = each.key

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = local.common_tags
}
