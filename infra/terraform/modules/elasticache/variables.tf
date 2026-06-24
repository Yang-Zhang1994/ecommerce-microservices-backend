variable "name_prefix" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "app_security_group_ids" {
  type = list(string)
}

variable "node_type" {
  type    = string
  default = "cache.t4g.micro"
}

variable "engine_version" {
  type    = string
  default = "7.1"
}

variable "num_cache_clusters" {
  type    = number
  default = 2
}

variable "automatic_failover_enabled" {
  type    = bool
  default = true
}

variable "tags" {
  type    = map(string)
  default = {}
}
