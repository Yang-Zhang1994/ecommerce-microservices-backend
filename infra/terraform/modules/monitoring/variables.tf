variable "name_prefix" {
  type = string
}

variable "alb_arn_suffix" {
  type        = string
  description = "ALB ARN suffix for CloudWatch dimension, e.g. app/ecommerce-prod-alb/b205eff04a246026"
}

variable "target_group_arn_suffix" {
  type        = string
  description = "Target group ARN suffix, e.g. targetgroup/ecommerce-gateway-tg/e9a1878d6f471c8a"
}

variable "web_acl_name" {
  type = string
}

variable "web_acl_id" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "alarm_actions" {
  type        = list(string)
  default     = []
  description = "SNS topic ARNs for alarm notifications (optional)"
}

variable "tags" {
  type    = map(string)
  default = {}
}
