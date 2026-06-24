variable "domain_name" {
  type = string
}

variable "subject_alternative_names" {
  type    = list(string)
  default = []
}

variable "route53_zone_id" {
  type        = string
  default     = ""
  description = "When set, create DNS validation records in Route53 and wait for ISSUED"
}

variable "tags" {
  type    = map(string)
  default = {}
}
