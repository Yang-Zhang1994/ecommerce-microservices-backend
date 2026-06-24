variable "aws_region" {
  type    = string
  default = "us-west-2"
}

variable "account_id" {
  type        = string
  description = "AWS account ID"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "alb_name" {
  type = string
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
  description = "Issued ACM ARN for HTTPS; leave empty to use request_acm_certificate module output (after DNS validation)"
}

variable "request_acm_certificate" {
  type        = bool
  default     = true
  description = "Request ACM cert for acm_domain_name (DNS validation records in terraform output)"
}

variable "acm_domain_name" {
  type    = string
  default = "www.yangzhangtech.online"
}

variable "acm_subject_alternative_names" {
  type    = list(string)
  default = [
    "yangzhangtech.online",
    "mall.yangzhangtech.online",
    "admin.yangzhangtech.online",
  ]
}

variable "waf_rate_limit_api_wide" {
  type    = number
  default = 3000
}

variable "cloudwatch_alarm_actions" {
  type        = list(string)
  default     = []
  description = "Optional SNS topic ARNs for CloudWatch alarms"
}

variable "target_instance_ids" {
  type        = list(string)
  default     = []
  description = "EC2 instance IDs; CLI showed 0 registered targets on 2026-05-16"
}

variable "create_security_groups" {
  type    = bool
  default = false
}

variable "name_prefix" {
  type    = string
  default = "gulimall-prod"
}

variable "alb_security_group_ids" {
  type = list(string)
}

variable "waf_web_acl_name" {
  type = string
}

variable "rate_limit_api_auth" {
  type    = number
  default = 1000
}

variable "waf_associate_alb" {
  type        = bool
  default     = true
  description = "CLI: WAF not yet associated to ALB — keep true for first apply"
}

variable "manage_dns_in_route53" {
  type        = bool
  default     = false
  description = "true: create Route53 zone, ACM validation records, and ALB aliases (NS must point to Route53)"
}

variable "dns_zone_name" {
  type        = string
  default     = "yangzhangtech.online"
  description = "Route53 public hosted zone (apex)"
}

variable "preserve_mail_dns_records" {
  type        = bool
  default     = false
  description = "Copy MX/TXT/mail A into Route53 (only if migrating an existing mail setup)"
}

variable "tags" {
  type    = map(string)
  default = {}
}

variable "enable_elasticache" {
  type        = bool
  default     = false
  description = "ElastiCache Redis replication group for gateway rate limit (primary + replica)"
}

variable "cache_subnet_ids" {
  type        = list(string)
  default     = []
  description = "Private subnets for ElastiCache; defaults to public_subnet_ids if empty"
}

variable "app_security_group_ids" {
  type        = list(string)
  default     = []
  description = "EC2 app SG ids allowed to reach Redis (e.g. gulimall app sg)"
}

variable "elasticache_node_type" {
  type    = string
  default = "cache.t4g.micro"
}

variable "enable_app_asg" {
  type        = bool
  default     = false
  description = "Auto Scaling Group for app tier (2+ instances in ALB target group)"
}

variable "app_subnet_ids" {
  type        = list(string)
  default     = []
  description = "Subnets for ASG instances"
}

variable "asg_ami_id" {
  type        = string
  default     = ""
  description = "Launch template AMI; empty uses asg_reference_instance_id AMI"
}

variable "asg_reference_instance_id" {
  type        = string
  default     = "i-05b954f202863481b"
  description = "Existing instance to copy AMI from when asg_ami_id is empty"
}

variable "asg_key_name" {
  type        = string
  default     = ""
  description = "EC2 key pair for ASG instances"
}

variable "asg_instance_type" {
  type    = string
  default = "t3.medium"
}

variable "asg_min_size" {
  type    = number
  default = 2
}

variable "asg_max_size" {
  type    = number
  default = 4
}

variable "asg_desired_capacity" {
  type    = number
  default = 2
}

variable "asg_user_data" {
  type        = string
  default     = ""
  description = "Optional cloud-init override"
}

variable "enable_eks" {
  type        = bool
  default     = false
  description = "Create EKS cluster + Spot node group for Helm P0 deploy"
}

variable "eks_cluster_name" {
  type    = string
  default = "gulimall-prod-eks"
}

variable "eks_cluster_version" {
  type    = string
  default = "1.31"
}

variable "eks_node_instance_types" {
  type    = list(string)
  default = ["t3.medium"]
}

variable "eks_node_capacity_type" {
  type    = string
  default = "SPOT"
}

variable "eks_node_desired_size" {
  type    = number
  default = 1
}

variable "eks_node_min_size" {
  type    = number
  default = 1
}

variable "eks_node_max_size" {
  type    = number
  default = 2
}

variable "rds_security_group_id" {
  type        = string
  default     = ""
  description = "RDS VPC security group — allow EKS node SG ingress on 5432 when enable_eks=true"
}

variable "enable_github_actions_cd" {
  type        = bool
  default     = false
  description = "IAM role + EKS access entry for GitHub Actions CD (OIDC)"
}

variable "github_repository" {
  type        = string
  default     = "Yang-Zhang1994/ecommerce-microservices-backend"
  description = "GitHub repo allowed to assume the CD role (org/name)"
}
