output "cluster_name" {
  value = aws_eks_cluster.this.name
}

output "cluster_arn" {
  value = aws_eks_cluster.this.arn
}

output "cluster_endpoint" {
  value = aws_eks_cluster.this.endpoint
}

output "cluster_certificate_authority_data" {
  value = aws_eks_cluster.this.certificate_authority[0].data
}

output "node_security_group_id" {
  description = "Custom node SG (launch template); pods use cluster_security_group_id for outbound ENI rules"
  value       = aws_security_group.nodes.id
}

output "cluster_security_group_id" {
  description = "EKS-managed cluster SG attached to worker nodes — use for RDS/ElastiCache ingress"
  value       = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}

output "oidc_issuer_url" {
  value = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

output "node_group_name" {
  value = aws_eks_node_group.this.node_group_name
}
