# Rule order aligned with console (ecommerce-prod-waf): IP reputation → rate → Core → KnownBadInputs

resource "aws_wafv2_web_acl" "this" {
  name        = var.name
  description = var.description
  scope       = "REGIONAL"

  default_action {
    allow {}
  }

  dynamic "rule" {
    for_each = var.enable_ip_reputation ? [1] : []
    content {
      name     = "AWS-AWSManagedRulesAmazonIpReputationList"
      priority = 0

      override_action {
        none {}
      }

      statement {
        managed_rule_group_statement {
          name        = "AWSManagedRulesAmazonIpReputationList"
          vendor_name = "AWS"
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AWS-AWSManagedRulesAmazonIpReputationList"
        sampled_requests_enabled   = true
      }
    }
  }

  rule {
    name     = var.rate_limit_rule_name
    priority = 1

    action {
      block {
        custom_response {
          response_code = var.rate_limit_block_http_code
        }
      }
    }

    statement {
      rate_based_statement {
        limit                   = var.rate_limit
        aggregate_key_type      = "IP"
        evaluation_window_sec   = var.rate_limit_evaluation_window_sec

        scope_down_statement {
          or_statement {
            statement {
              byte_match_statement {
                positional_constraint = "STARTS_WITH"
                search_string         = "/api/auth"

                field_to_match {
                  uri_path {}
                }

                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
            statement {
              byte_match_statement {
                positional_constraint = "STARTS_WITH"
                search_string         = "/api/order/pay/stripe/webhook"

                field_to_match {
                  uri_path {}
                }

                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = var.rate_limit_rule_name
      sampled_requests_enabled   = true
    }
  }

  dynamic "rule" {
    for_each = var.enable_api_wide_rate_limit ? [1] : []
    content {
      name     = var.api_wide_rate_limit_rule_name
      priority = 2

      action {
        block {
          custom_response {
            response_code = var.rate_limit_block_http_code
          }
        }
      }

      statement {
        rate_based_statement {
          limit                 = var.rate_limit_api_wide
          aggregate_key_type    = "IP"
          evaluation_window_sec = var.rate_limit_evaluation_window_sec

          scope_down_statement {
            byte_match_statement {
              positional_constraint = "STARTS_WITH"
              search_string         = "/api/"

              field_to_match {
                uri_path {}
              }

              text_transformation {
                priority = 0
                type     = "NONE"
              }
            }
          }
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = var.api_wide_rate_limit_rule_name
        sampled_requests_enabled   = true
      }
    }
  }

  dynamic "rule" {
    for_each = var.enable_common_rule_set ? [1] : []
    content {
      name     = "AWS-AWSManagedRulesCommonRuleSet"
      priority = 3

      override_action {
        none {}
      }

      statement {
        managed_rule_group_statement {
          name        = "AWSManagedRulesCommonRuleSet"
          vendor_name = "AWS"
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AWS-AWSManagedRulesCommonRuleSet"
        sampled_requests_enabled   = true
      }
    }
  }

  dynamic "rule" {
    for_each = var.enable_known_bad_inputs ? [1] : []
    content {
      name     = "AWS-AWSManagedRulesKnownBadInputsRuleSet"
      priority = 4

      override_action {
        none {}
      }

      statement {
        managed_rule_group_statement {
          name        = "AWSManagedRulesKnownBadInputsRuleSet"
          vendor_name = "AWS"
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "AWS-AWSManagedRulesKnownBadInputsRuleSet"
        sampled_requests_enabled   = true
      }
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = var.name
    sampled_requests_enabled   = true
  }

  tags = var.tags
}

resource "aws_wafv2_web_acl_association" "alb" {
  count = var.associate_alb ? 1 : 0

  resource_arn = var.alb_arn
  web_acl_arn  = aws_wafv2_web_acl.this.arn
}
