variable "name_prefix" {
  type        = string
  description = "Prefix for security group names, e.g. gulimall-dev"
}

variable "vpc_id" {
  type        = string
  description = "VPC where ALB and app instances run"
}

variable "app_port" {
  type        = number
  default     = 88
  description = "Gateway listen port on EC2/ECS targets"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Common tags"
}
