#!/bin/bash
# 在 EC2 上执行：拉取最新代码、确保 sys_captcha 表存在、重建并重启 renren-fast
# 使用: 在 EC2 上 cd ~/gulimall && bash scripts/deploy-renren-fast-ec2.sh

set -e
cd "$(dirname "$0")/.."

echo "1. Stashing local changes (if any)..."
git stash || true

echo "2. Pulling latest code..."
git pull origin main

echo "3. Creating sys_captcha table if missing..."
if [ -f .env ]; then
  set -a
  source .env
  set +a
fi
if [ -n "$RDS_ENDPOINT" ] && [ -n "$RDS_USERNAME" ] && [ -n "$RDS_PASSWORD" ]; then
  export PGPASSWORD="$RDS_PASSWORD"
  psql -h "$RDS_ENDPOINT" -U "$RDS_USERNAME" -d ecommerce_admin -p 5432 -f renren-fast/db/init-sys_captcha.sql 2>/dev/null || echo "psql not installed or connection failed, skipping table init"
fi

echo "4. Building renren-fast..."
mvn -q -pl renren-fast package -DskipTests 2>/dev/null || true

echo "5. Rebuilding and restarting renren-fast container..."
docker compose -f docker-compose.app.yml build renren-fast --no-cache 2>/dev/null || docker-compose -f docker-compose.app.yml build renren-fast --no-cache 2>/dev/null || true
docker compose -f docker-compose.app.yml up -d renren-fast --force-recreate 2>/dev/null || docker-compose -f docker-compose.app.yml up -d renren-fast --force-recreate 2>/dev/null || true

echo "Done. Wait ~30s then test: curl -o /tmp/c.jpg 'http://localhost:88/api/captcha.jpg?uuid=test' && file /tmp/c.jpg"
