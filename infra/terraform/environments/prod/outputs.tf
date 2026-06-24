output "alb_dns_name" {
  value = module.alb.lb_dns_name
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

output "cloudwatch_alarm_names" {
  value = module.monitoring.alarm_names
}

output "route53_name_servers" {
  description = "Replace Cloudflare NS with these 4 names at your registrar, then re-run terraform apply"
  value       = var.manage_dns_in_route53 ? module.dns_zone[0].name_servers : []
}

output "route53_zone_id" {
  value = var.manage_dns_in_route53 ? module.dns_zone[0].zone_id : null
}

output "site_fqdns" {
  description = "After NS cutover and apply"
  value = var.manage_dns_in_route53 ? {
    www  = module.dns_alias[0].www_fqdn
    apex = module.dns_alias[0].apex_fqdn
  } : {}
}

output "acm_validation_records" {
  description = "Only when manage_dns_in_route53=false — manual CNAME at external DNS"
  value = !var.manage_dns_in_route53 && var.request_acm_certificate && trimspace(var.certificate_arn) == "" ? try(module.acm[0].domain_validation_options, []) : []
}

output "certificate_arn_in_use" {
  value = local.cert_arn
}

output "acm_status_hint" {
  value = var.manage_dns_in_route53 ? "ACM validates via Route53 records after NS points to route53_name_servers" : "Add acm_validation_records at Cloudflare, then set certificate_arn"
}

output "elasticache_redis_primary" {
  value = var.enable_elasticache ? module.elasticache[0].primary_endpoint_address : null
}

output "app_asg_name" {
  value = var.enable_app_asg ? module.app_asg[0].autoscaling_group_name : null
}

output "eks_cluster_name" {
  value = var.enable_eks ? module.eks[0].cluster_name : null
}

output "eks_cluster_endpoint" {
  value = var.enable_eks ? module.eks[0].cluster_endpoint : null
}

output "eks_node_security_group_id" {
  description = "Custom Terraform node SG (not attached to managed node group ENIs)"
  value       = var.enable_eks ? module.eks[0].node_security_group_id : null
}

output "eks_cluster_security_group_id" {
  description = "EKS-managed cluster SG on worker nodes — use for RDS/ElastiCache ingress"
  value       = var.enable_eks ? module.eks[0].cluster_security_group_id : null
}

output "ecr_repository_urls" {
  description = "Map of ECR repository name → repository URL (for docker push)"
  value       = var.enable_eks ? { for k, repo in aws_ecr_repository.gulimall : k => repo.repository_url } : {}
}

output "ecr_registry" {
  description = "Docker registry host (account.dkr.ecr.region.amazonaws.com)"
  value       = var.enable_eks ? "${var.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com" : null
}

output "github_actions_cd_role_arn" {
  description = "IAM role ARN for GitHub Actions OIDC (set as AWS_ROLE_TO_ASSUME secret)"
  value       = var.enable_eks && var.enable_github_actions_cd ? aws_iam_role.github_actions_cd[0].arn : null
}
