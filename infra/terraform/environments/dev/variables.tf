variable "aws_region" {
  type    = string
  default = "us-west-2"
}

variable "account_id" {
  type        = string
  description = "AWS account ID (for docs/import ARNs)"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

# --- ALB (names must match console when importing) ---
variable "alb_name" {
  type        = string
  description = "Existing ALB name, e.g. ecommerce-alb"
}

variable "target_group_name" {
  type    = string
  default = "ecommerce-gateway-tg"
}

variable "target_port" {
  type    = number
  default = 88
}

variable "health_check_path" {
  type    = string
  default = "/actuator/health"
}

variable "certificate_arn" {
  type        = string
  default     = ""
  description = "ACM ARN for HTTPS; leave empty if only HTTP listener exists today"
}

variable "target_instance_ids" {
  type        = list(string)
  default     = []
  description = "EC2 instances for TG attachments; omit if already registered in console"
}

# --- Security groups ---
variable "create_security_groups" {
  type        = bool
  default     = false
  description = "false when importing existing ALB/EC2 security groups"
}

variable "name_prefix" {
  type    = string
  default = "gulimall-dev"
}

variable "alb_security_group_ids" {
  type        = list(string)
  description = "SGs on ALB (required when create_security_groups = false)"
}

# --- WAF ---
variable "waf_web_acl_name" {
  type        = string
  description = "Existing Web ACL name in console"
}

variable "rate_limit_api_auth" {
  type    = number
  default = 1000
}

variable "waf_associate_alb" {
  type        = bool
  default     = true
  description = "Set true to attach Web ACL to ALB; false if already associated manually"
}

variable "tags" {
  type    = map(string)
  default = {}
}
