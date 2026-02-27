#!/usr/bin/env bash
# 本机执行：上传新 Dockerfile 到 EC2，在 EC2 上重建前端镜像并重启前端容器。
# 用法：./scripts/update-frontend-on-ec2.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KEY="${EC2_KEY:-$HOME/Downloads/ecommerce-key.pem}"
HOST="${EC2_HOST:-ubuntu@3.145.65.47}"

echo "1. 上传 Dockerfile 到 EC2..."
scp -i "$KEY" -o StrictHostKeyChecking=no \
  "$ROOT/renren-fast-vue/Dockerfile" \
  "$HOST:~/gulimall/renren-fast-vue/Dockerfile"

echo "2. 在 EC2 上重建前端镜像并重启前端..."
ssh -i "$KEY" -o StrictHostKeyChecking=no "$HOST" \
  'cd ~/gulimall && DOCKER_BUILDKIT=0 docker compose -f docker-compose.app.yml build gulimall-frontend && docker compose -f docker-compose.app.yml up -d gulimall-frontend'

echo "3. 完成。请刷新 http://3.145.65.47 查看是否出现登录页。"
