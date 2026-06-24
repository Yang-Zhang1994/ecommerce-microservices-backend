# One-time import for existing prod resources (Terraform >= 1.5).
# Resources are already in state — import blocks commented out to avoid re-import errors.

# import {
#   to = module.alb.aws_lb.this
#   id = "arn:aws:elasticloadbalancing:us-west-2:466297333400:loadbalancer/app/ecommerce-prod-alb/b205eff04a246026"
# }

# import {
#   to = module.alb.aws_lb_target_group.gateway
#   id = "arn:aws:elasticloadbalancing:us-west-2:466297333400:targetgroup/ecommerce-gateway-tg/e9a1878d6f471c8a"
# }

# import {
#   to = module.alb.aws_lb_listener.https[0]
#   id = "arn:aws:elasticloadbalancing:us-west-2:466297333400:listener/app/ecommerce-prod-alb/b205eff04a246026/..."
# }

# import {
#   to = module.waf.aws_wafv2_web_acl.this
#   id = "8808d7cc-130b-44f3-89db-8e2c4f84e44f/ecommerce-prod-waf/REGIONAL"
# }
