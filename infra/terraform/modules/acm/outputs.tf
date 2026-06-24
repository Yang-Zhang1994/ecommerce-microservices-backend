output "certificate_arn" {
  description = "Non-empty only when ACM status is ISSUED (keeps ALB on HTTP until validation completes)"
  value       = aws_acm_certificate.this.status == "ISSUED" ? aws_acm_certificate.this.arn : ""
}

output "certificate_arn_pending" {
  value = aws_acm_certificate.this.arn
}

output "domain_validation_options" {
  value = aws_acm_certificate.this.domain_validation_options
}
