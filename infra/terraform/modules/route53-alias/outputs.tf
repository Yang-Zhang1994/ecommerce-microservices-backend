output "www_fqdn" {
  value = var.create_www_record ? aws_route53_record.www[0].fqdn : null
}

output "apex_fqdn" {
  value = var.create_apex_record ? aws_route53_record.apex[0].fqdn : null
}
