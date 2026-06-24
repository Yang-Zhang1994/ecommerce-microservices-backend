locals {
  common_tags      = var.tags
  cache_subnet_ids = length(var.cache_subnet_ids) > 0 ? var.cache_subnet_ids : var.public_subnet_ids
  app_subnet_ids   = length(var.app_subnet_ids) > 0 ? var.app_subnet_ids : var.public_subnet_ids
  alb_sg_ids  = var.create_security_groups ? [module.security_groups[0].alb_security_group_id] : var.alb_security_group_ids
  alb_suffix  = "${var.alb_name}/b205eff04a246026"
  tg_suffix   = "${var.target_group_name}/e9a1878d6f471c8a"
  cert_arn = trimspace(var.certificate_arn) != "" ? var.certificate_arn : (
    var.request_acm_certificate ? try(module.acm[0].certificate_arn, "") : ""
  )
}

module "security_groups" {
  count  = var.create_security_groups ? 1 : 0
  source = "../../modules/security-groups"

  name_prefix = var.name_prefix
  vpc_id      = var.vpc_id
  app_port    = var.target_port
  tags        = local.common_tags
}

# Step 1 of NS migration: create public hosted zone (outputs 4 name servers)
module "dns_zone" {
  count  = var.manage_dns_in_route53 ? 1 : 0
  source = "../../modules/route53-zone"

  domain_name = var.dns_zone_name
  tags        = local.common_tags
}

module "acm" {
  count  = var.request_acm_certificate && trimspace(var.certificate_arn) == "" ? 1 : 0
  source = "../../modules/acm"

  domain_name               = var.acm_domain_name
  subject_alternative_names = var.acm_subject_alternative_names
  route53_zone_id           = var.manage_dns_in_route53 ? module.dns_zone[0].zone_id : ""
  tags                      = local.common_tags
}

module "alb" {
  source = "../../modules/alb"

  name                = var.alb_name
  vpc_id              = var.vpc_id
  subnet_ids          = var.public_subnet_ids
  security_group_ids  = local.alb_sg_ids
  target_group_name   = var.target_group_name
  target_port         = var.target_port
  health_check_path   = var.health_check_path
  certificate_arn     = local.cert_arn
  target_instance_ids = var.enable_eks || var.enable_app_asg ? [] : var.target_instance_ids
  tags                = local.common_tags
}

module "elasticache" {
  count  = var.enable_elasticache ? 1 : 0
  source = "../../modules/elasticache"

  name_prefix            = var.name_prefix
  vpc_id                 = var.vpc_id
  subnet_ids             = local.cache_subnet_ids
  app_security_group_ids = var.app_security_group_ids
  node_type              = var.elasticache_node_type
  tags                   = local.common_tags
}

data "aws_instance" "asg_reference" {
  count       = var.enable_app_asg && trimspace(var.asg_reference_instance_id) != "" ? 1 : 0
  instance_id = var.asg_reference_instance_id
}

module "app_asg" {
  count  = var.enable_app_asg ? 1 : 0
  source = "../../modules/app-asg"

  name_prefix      = var.name_prefix
  ami_id           = trimspace(var.asg_ami_id) != "" ? var.asg_ami_id : data.aws_instance.asg_reference[0].ami
  instance_type    = var.asg_instance_type
  key_name         = var.asg_key_name
  subnet_ids       = local.app_subnet_ids
  security_group_ids = var.app_security_group_ids
  target_group_arn = module.alb.target_group_arn
  min_size         = var.asg_min_size
  max_size         = var.asg_max_size
  desired_capacity = var.asg_desired_capacity
  user_data        = var.asg_user_data
  tags             = local.common_tags
}

# Email / verification — create before NS cutover (see docs/route53-ns-migration.md)
module "dns_preserved" {
  count  = var.manage_dns_in_route53 && var.preserve_mail_dns_records ? 1 : 0
  source = "../../modules/route53-preserved"

  zone_id     = module.dns_zone[0].zone_id
  domain_name = var.dns_zone_name
}

# Step 3: after NS cutover + cert ISSUED — point www + apex to ALB
module "dns_alias" {
  count  = var.manage_dns_in_route53 ? 1 : 0
  source = "../../modules/route53-alias"

  zone_id      = module.dns_zone[0].zone_id
  domain_name  = var.dns_zone_name
  alb_dns_name = module.alb.lb_dns_name
  alb_zone_id  = module.alb.lb_zone_id
}

module "waf" {
  source = "../../modules/waf"

  name                       = var.waf_web_acl_name
  alb_arn                    = module.alb.lb_arn
  rate_limit                 = var.rate_limit_api_auth
  rate_limit_api_wide        = var.waf_rate_limit_api_wide
  enable_api_wide_rate_limit = true
  associate_alb              = var.waf_associate_alb
  tags                       = local.common_tags
}

module "monitoring" {
  source = "../../modules/monitoring"

  name_prefix             = var.name_prefix
  alb_arn_suffix          = local.alb_suffix
  target_group_arn_suffix = local.tg_suffix
  web_acl_name            = var.waf_web_acl_name
  web_acl_id              = "8808d7cc-130b-44f3-89db-8e2c4f84e44f"
  aws_region              = var.aws_region
  alarm_actions           = var.cloudwatch_alarm_actions
  tags                    = local.common_tags
}

module "eks" {
  count  = var.enable_eks ? 1 : 0
  source = "../../modules/eks"

  cluster_name        = var.eks_cluster_name
  cluster_version     = var.eks_cluster_version
  vpc_id              = var.vpc_id
  subnet_ids          = var.public_subnet_ids
  node_instance_types = var.eks_node_instance_types
  capacity_type       = var.eks_node_capacity_type
  node_desired_size   = var.eks_node_desired_size
  node_min_size       = var.eks_node_min_size
  node_max_size       = var.eks_node_max_size
  tags                = local.common_tags
}

