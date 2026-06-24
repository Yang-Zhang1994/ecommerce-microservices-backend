output "lb_arn" {
  value = aws_lb.this.arn
}

output "lb_dns_name" {
  value = aws_lb.this.dns_name
}

output "lb_zone_id" {
  value = aws_lb.this.zone_id
}

output "target_group_arn" {
  value = aws_lb_target_group.gateway.arn
}

output "target_group_name" {
  value = aws_lb_target_group.gateway.name
}

output "https_listener_arn" {
  value = try(aws_lb_listener.https[0].arn, null)
}

output "http_listener_arn" {
  value = try(coalesce(aws_lb_listener.http_forward[0].arn, aws_lb_listener.http_redirect[0].arn), null)
}
