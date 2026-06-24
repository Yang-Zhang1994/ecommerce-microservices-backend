output "zone_id" {
  value = aws_route53_zone.this.zone_id
}

output "zone_name" {
  value = aws_route53_zone.this.name
}

output "name_servers" {
  description = "Set these as NS at your domain registrar (replaces Cloudflare NS)"
  value       = aws_route53_zone.this.name_servers
}
