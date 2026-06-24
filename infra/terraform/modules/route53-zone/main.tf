resource "aws_route53_zone" "this" {
  name          = var.domain_name
  force_destroy = false
  comment       = var.comment

  tags = merge(var.tags, { Name = var.domain_name })
}
