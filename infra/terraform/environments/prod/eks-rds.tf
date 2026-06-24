# Allow EKS worker nodes (in-VPC) to reach RDS — no laptop public IP required.
# Nodes use the EKS-managed cluster security group, not the custom node SG.
resource "aws_security_group_rule" "rds_from_eks_nodes" {
  count = var.enable_eks && trimspace(var.rds_security_group_id) != "" ? 1 : 0

  type                     = "ingress"
  description              = "PostgreSQL from EKS cluster SG (worker nodes)"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = var.rds_security_group_id
  source_security_group_id = module.eks[0].cluster_security_group_id
}
