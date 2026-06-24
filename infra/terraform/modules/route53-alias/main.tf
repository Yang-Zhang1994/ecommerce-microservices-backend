resource "aws_route53_record" "www" {
  count = var.create_www_record ? 1 : 0

  zone_id = var.zone_id
  name    = "www.${var.domain_name}"
  type    = "A"

  alias {
    name                   = var.alb_dns_name
    zone_id                = var.alb_zone_id
    evaluate_target_health = var.evaluate_target_health
  }
}

resource "aws_route53_record" "apex" {
  count = var.create_apex_record ? 1 : 0

  zone_id = var.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = var.alb_dns_name
    zone_id                = var.alb_zone_id
    evaluate_target_health = var.evaluate_target_health
  }
}
