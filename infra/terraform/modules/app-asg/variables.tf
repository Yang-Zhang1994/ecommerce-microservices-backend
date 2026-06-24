variable "name_prefix" {
  type = string
}

variable "ami_id" {
  type        = string
  description = "AMI with Docker/Compose app stack (e.g. current Ecommerce-Server AMI)"
}

variable "instance_type" {
  type    = string
  default = "t3.medium"
}

variable "key_name" {
  type        = string
  description = "EC2 SSH key pair name"
}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_ids" {
  type = list(string)
}

variable "target_group_arn" {
  type = string
}

variable "min_size" {
  type    = number
  default = 2
}

variable "max_size" {
  type    = number
  default = 4
}

variable "desired_capacity" {
  type    = number
  default = 2
}

variable "user_data" {
  type        = string
  default     = "#!/bin/bash\n# Deploy via scripts/deploy-prod-ec2-gateway.sh / SSM after instance launch\n"
  description = "cloud-init; sync JARs from S3 and docker compose up"
}

variable "tags" {
  type    = map(string)
  default = {}
}
