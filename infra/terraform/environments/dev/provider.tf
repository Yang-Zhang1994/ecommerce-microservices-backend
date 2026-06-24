provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(
      {
        Project     = "gulimall"
        Environment = var.environment
        ManagedBy   = "terraform"
      },
      var.tags
    )
  }
}
