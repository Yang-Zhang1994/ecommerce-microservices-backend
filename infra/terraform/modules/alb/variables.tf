variable "name" {
  type        = string
  description = "ALB name (must match existing resource when importing)"
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type        = list(string)
  description = "Public subnets for internet-facing ALB (min 2 AZs)"
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups attached to ALB"
}

variable "target_group_name" {
  type        = string
  description = "Target group name, e.g. ecommerce-gateway-tg"
}

variable "target_port" {
  type    = number
  default = 88
}

variable "health_check_path" {
  type    = string
  default = "/actuator/health"
}

variable "health_check_healthy_threshold" {
  type    = number
  default = 5
}

variable "health_check_unhealthy_threshold" {
  type    = number
  default = 2
}

variable "certificate_arn" {
  type        = string
  default     = ""
  description = "ACM cert ARN for HTTPS listener; empty = HTTP-only listener on 80"
}

variable "enable_http_redirect" {
  type        = bool
  default     = true
  description = "When certificate_arn set, add HTTP:80 listener that redirects to HTTPS"
}

variable "target_instance_ids" {
  type        = list(string)
  default     = []
  description = "Optional EC2 instance IDs to register; empty if registering later or via console"
}

variable "internal" {
  type    = bool
  default = false
}

variable "tags" {
  type    = map(string)
  default = {}
}
