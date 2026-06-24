variable "cluster_name" {
  type        = string
  description = "EKS cluster name"
}

variable "cluster_version" {
  type    = string
  default = "1.31"
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs for control plane and nodes (typically public subnets for this demo)"
}

variable "node_instance_types" {
  type    = list(string)
  default = ["t3.medium"]
}

variable "capacity_type" {
  type        = string
  default     = "SPOT"
  description = "ON_DEMAND or SPOT"
}

variable "node_desired_size" {
  type    = number
  default = 1
}

variable "node_min_size" {
  type    = number
  default = 1
}

variable "node_max_size" {
  type    = number
  default = 2
}

variable "tags" {
  type    = map(string)
  default = {}
}
