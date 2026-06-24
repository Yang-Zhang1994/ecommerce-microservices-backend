output "web_acl_arn" {
  value = aws_wafv2_web_acl.this.arn
}

output "web_acl_id" {
  value = aws_wafv2_web_acl.this.id
}

output "web_acl_capacity" {
  value = aws_wafv2_web_acl.this.capacity
}

output "web_acl_association_id" {
  value = try(aws_wafv2_web_acl_association.alb[0].id, null)
}
