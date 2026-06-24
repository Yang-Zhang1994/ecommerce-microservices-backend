variable "zone_id" {
  type = string
}

variable "domain_name" {
  type        = string
  description = "Zone apex, e.g. ecommerce.com"
}

variable "alb_dns_name" {
  type = string
}

variable "alb_zone_id" {
  type = string
}

variable "create_www_record" {
  type    = bool
  default = true
}

variable "create_apex_record" {
  type    = bool
  default = true
}

variable "evaluate_target_health" {
  type    = bool
  default = true
}
