locals {
  alb_dimension = "app/${var.alb_arn_suffix}"
  tg_dimension  = "targetgroup/${var.target_group_arn_suffix}"
}

resource "aws_cloudwatch_metric_alarm" "alb_target_5xx" {
  alarm_name          = "${var.name_prefix}-alb-target-5xx"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "HTTPCode_Target_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 10
  treat_missing_data  = "notBreaching"
  alarm_description   = "ALB target 5xx count high (gulimall gateway/backend)"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = local.alb_dimension
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "alb_elb_5xx" {
  alarm_name          = "${var.name_prefix}-alb-elb-5xx"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "HTTPCode_ELB_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 5
  treat_missing_data  = "notBreaching"
  alarm_description   = "ALB ELB-generated 5xx (capacity/config)"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = local.alb_dimension
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "target_unhealthy" {
  alarm_name          = "${var.name_prefix}-tg-unhealthy-hosts"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  treat_missing_data  = "notBreaching"
  alarm_description   = "At least one unhealthy target in ecommerce-gateway-tg"
  alarm_actions       = var.alarm_actions

  dimensions = {
    TargetGroup  = local.tg_dimension
    LoadBalancer = local.alb_dimension
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "waf_blocked" {
  alarm_name          = "${var.name_prefix}-waf-blocked-requests"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "BlockedRequests"
  namespace           = "AWS/WAFV2"
  period              = 300
  statistic           = "Sum"
  threshold           = 500
  treat_missing_data  = "notBreaching"
  alarm_description   = "WAF blocked many requests in 5 minutes (attack or tight rules)"
  alarm_actions       = var.alarm_actions

  dimensions = {
    WebACL = var.web_acl_name
    Region = var.aws_region
    Rule   = "ALL"
  }

  tags = var.tags
}
