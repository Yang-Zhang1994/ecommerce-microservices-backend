output "alarm_names" {
  value = [
    aws_cloudwatch_metric_alarm.alb_target_5xx.alarm_name,
    aws_cloudwatch_metric_alarm.alb_elb_5xx.alarm_name,
    aws_cloudwatch_metric_alarm.target_unhealthy.alarm_name,
    aws_cloudwatch_metric_alarm.waf_blocked.alarm_name,
  ]
}
