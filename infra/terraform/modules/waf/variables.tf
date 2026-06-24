variable "name" {
  type        = string
  description = "Web ACL name (must match existing when importing)"
}

variable "description" {
  type    = string
  default = "WAF protection pack for Gulimall application load balancer, tracking core rules and IP reputation."
}

variable "alb_arn" {
  type        = string
  description = "ARN of Application Load Balancer to associate"
}

variable "associate_alb" {
  type        = bool
  default     = true
  description = "Create Web ACL association; set false if already associated outside Terraform"
}

variable "rate_limit" {
  type        = number
  default     = 1000
  description = "Max requests per evaluation window per IP (rate-based rule)"
}

variable "rate_limit_evaluation_window_sec" {
  type    = number
  default = 300
}

variable "rate_limit_rule_name" {
  type    = string
  default = "ecommerce-auth-webhook-ratelimit"
}

variable "enable_api_wide_rate_limit" {
  type        = bool
  default     = true
  description = "WAF rate limit for all paths starting with /api/"
}

variable "rate_limit_api_wide" {
  type        = number
  default     = 3000
  description = "Max requests per 5 minutes per IP for /api/* (broader than auth-only rule)"
}

variable "api_wide_rate_limit_rule_name" {
  type    = string
  default = "ecommerce-api-wide-ratelimit"
}

variable "rate_limit_block_http_code" {
  type    = number
  default = 429
}

variable "enable_ip_reputation" {
  type    = bool
  default = true
}

variable "enable_common_rule_set" {
  type    = bool
  default = true
}

variable "enable_known_bad_inputs" {
  type    = bool
  default = true
}

variable "tags" {
  type    = map(string)
  default = {}
}
