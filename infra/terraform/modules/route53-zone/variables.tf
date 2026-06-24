variable "domain_name" {
  type        = string
  description = "Public hosted zone name, e.g. ecommerce.com (trailing dot optional)"
}

variable "comment" {
  type    = string
  default = "Gulimall production DNS"
}

variable "tags" {
  type    = map(string)
  default = {}
}
