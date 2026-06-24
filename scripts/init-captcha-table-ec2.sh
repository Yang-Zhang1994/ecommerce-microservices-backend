#!/bin/bash
# 在 EC2 上执行，为 ecommerce_admin 创建 sys_captcha 表，修复验证码 500
# 使用: 在 EC2 上 cd ~/gulimall && bash scripts/init-captcha-table-ec2.sh
# 依赖: EC2 已安装 psql（sudo apt-get install -y postgresql-client）或通过 docker 执行

set -e
cd "$(dirname "$0")/.."
if [ -f .env ]; then
  set -a
  source .env
  set +a
fi
if [ -z "$RDS_ENDPOINT" ] || [ -z "$RDS_USERNAME" ] || [ -z "$RDS_PASSWORD" ]; then
  echo "Missing RDS_ENDPOINT/RDS_USERNAME/RDS_PASSWORD in .env"
  exit 1
fi
export PGPASSWORD="$RDS_PASSWORD"
psql -h "$RDS_ENDPOINT" -U "$RDS_USERNAME" -d ecommerce_admin -p 5432 -f renren-fast/db/init-sys_captcha.sql
echo "Done. sys_captcha table created or already exists."
