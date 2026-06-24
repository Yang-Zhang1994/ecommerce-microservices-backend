# Records that must survive NS cutover (exported from Cloudflare via dig, 2026-05-16)

resource "aws_route53_record" "apex_mx" {
  zone_id = var.zone_id
  name    = var.domain_name
  type    = "MX"
  ttl     = 300
  records = ["5 smtp.google.com"]
}

resource "aws_route53_record" "apex_txt" {
  zone_id = var.zone_id
  name    = var.domain_name
  type    = "TXT"
  ttl     = 300
  records = [
    "google-site-verification=Xxz_jP8xViMPqkyoHK2z0wiuM2etDIDIJkJVFCtPE-M",
    "v=spf1 ip4:198.168.112.17/28 ip4:68.68.29.244 ip4:68.68.29.0/24 include:_spf.google.com include:mailgun.org include:servers.mcsv.net ptr mx ~all",
  ]
}

resource "aws_route53_record" "mail_a" {
  zone_id = var.zone_id
  name    = "mail.${var.domain_name}"
  type    = "A"
  ttl     = 300
  records = ["198.168.112.17"]
}
