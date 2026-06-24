#!/bin/bash
# 在项目根执行。先改 baseUrl 为 /api，再构建前端，供 docker 镜像 COPY dist 使用。
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/renren-fast-vue"
if [ -f static/config/index.js ]; then
  sed -i.bak 's|http://localhost:88/api|/api|g' static/config/index.js
fi
npm run build
echo "Done. dist/ is ready for docker build."
