locals {
  common_tags = var.tags
  alb_sg_ids  = var.create_security_groups ? [module.security_groups[0].alb_security_group_id] : var.alb_security_group_ids
}

module "security_groups" {
  count  = var.create_security_groups ? 1 : 0
  source = "../../modules/security-groups"

  name_prefix = var.name_prefix
  vpc_id      = var.vpc_id
  app_port    = var.target_port
  tags        = local.common_tags
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
  certificate_arn     = var.certificate_arn
  target_instance_ids = var.target_instance_ids
  tags                = local.common_tags
}

module "waf" {
  source = "../../modules/waf"

  name         = var.waf_web_acl_name
  alb_arn      = module.alb.lb_arn
  rate_limit   = var.rate_limit_api_auth
  associate_alb = var.waf_associate_alb
  tags         = local.common_tags
}
