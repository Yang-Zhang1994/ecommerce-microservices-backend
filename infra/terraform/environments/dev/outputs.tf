output "alb_dns_name" {
  description = "Point Route53 / CNAME here"
  value       = module.alb.lb_dns_name
}

output "alb_arn" {
  value = module.alb.lb_arn
}

output "target_group_arn" {
  value = module.alb.target_group_arn
}

output "waf_web_acl_arn" {
  value = module.waf.web_acl_arn
}

output "import_hint" {
  description = "After first apply/import, run: terraform plan until no unexpected changes"
  value       = "See infra/terraform/README.md#import-existing-resources"
}
