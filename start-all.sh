#!/bin/bash
# Gulimall 本地一键启动: Consul(Docker) + 各微服务(Maven) + 可选前端
# 用法: ./start-all.sh          # 只启动后端
#       ./start-all.sh --frontend  # 再启动前端(最后前台运行)
# 数据库: 有 .env 时自动加载，renren-fast 连 RDS(dev-rds)；无 .env 则连本地 localhost:5432

set -e
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

# 需要 Consul，先检查 Docker 是否在运行
if ! docker info &>/dev/null; then
  echo "Docker 未运行。请先启动 Docker Desktop，再执行本脚本。"
  exit 1
fi

# 各服务端口（不含 8500 Consul）
PORTS=(88 8080 12000 14000 8000 10000 11000 20000 30000 9000 12001)

kill_port() {
  local port=$1
  local pids
  pids=$(lsof -ti:$port 2>/dev/null || true)
  if [ -n "$pids" ]; then
    echo "Killing process on port $port: $pids"
    echo "$pids" | xargs kill -9 2>/dev/null || true
    sleep 1
  fi
}

# 加载 .env（RDS 等），供 renren-fast 使用
if [ -f "$PROJECT_ROOT/.env" ]; then
  set -a
  source "$PROJECT_ROOT/.env"
  set +a
  echo "=== Loaded .env (RDS_ENDPOINT etc.) ==="
fi

echo "=== Killing processes on conflicting ports ==="
for port in "${PORTS[@]}"; do
  kill_port "$port"
done

echo ""
echo "=== Starting Consul (Docker) ==="
docker compose -f "$PROJECT_ROOT/docker-compose.yml" up -d consul 2>/dev/null || \
  docker-compose -f "$PROJECT_ROOT/docker-compose.yml" up -d consul 2>/dev/null || {
  echo "Warning: docker compose failed. Start Consul manually: docker run -d -p 8500:8500 hashicorp/consul:1.17 agent -server -ui -bootstrap-expect=1 -client=0.0.0.0"
}

echo "Waiting for Consul on 8500..."
for i in {1..30}; do
  if curl -s -o /dev/null -w "%{http_code}" http://localhost:8500/v1/status/leader 2>/dev/null | grep -q '200'; then
    echo "Consul is ready."
    break
  fi
  if [ $i -eq 30 ]; then
    echo "Warning: Consul did not become ready in time. Microservices may fail to register."
  fi
  sleep 1
done
sleep 2

echo ""
echo "=== Starting renren-fast, Gateway, and microservices (from project root) ==="

start_service() {
  local module=$1
  local name=$2
  local extra_profiles="${3:-}"
  (
    cd "$PROJECT_ROOT"
    echo "[$name] Starting..."
    if [ -n "$extra_profiles" ]; then
      mvn -q -pl "$module" spring-boot:run -Dspring-boot.run.profiles="$extra_profiles"
    else
      mvn -q -pl "$module" spring-boot:run
    fi
  ) &
}

# renren-fast: 有 RDS 用 dev,dev-rds；否则只用 dev。端口 8080 避免与 gateway(88) 冲突
if [ -n "${RDS_ENDPOINT:-}" ]; then
  ( cd "$PROJECT_ROOT" && echo "[renren-fast] Starting (port 8080, RDS)..." && mvn -q -pl renren-fast spring-boot:run -Dspring-boot.run.profiles=dev,dev-rds -Dspring-boot.run.arguments="--server.port=8080" ) &
else
  ( cd "$PROJECT_ROOT" && echo "[renren-fast] Starting (port 8080)..." && mvn -q -pl renren-fast spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=8080" ) &
fi
sleep 5
start_service "gulimall-gateway" "gateway" "dev"
sleep 2

start_service "gulimall-auth-server" "auth-server" "dev"
start_service "gulimall-coupon" "coupon" "dev"
start_service "gulimall-seckill" "seckill" "dev"
start_service "gulimall-member" "member" "dev"
start_service "gulimall-product" "product" "dev"
start_service "gulimall-ware" "ware" "dev"
start_service "gulimall-third-party" "third-party" "dev"
start_service "gulimall-order" "order" "dev"
start_service "gulimall-search" "search" "dev"

echo ""
echo "=== All services starting in background ==="
echo "Consul UI:    http://localhost:8500"
echo "Gateway:      http://localhost:88"
echo "renren-fast:  http://localhost:8080/api  (Gateway 88 会转发到此处)"
echo "order:        http://localhost:9000"
echo "search:       http://localhost:12001"
echo "auth-server:  http://localhost:20000  (Consul: gulimall-auth-server)"
echo ""
echo "Press Ctrl+C to stop all (or run: pkill -f spring-boot:run)"
echo ""

START_FRONTEND=false
for arg in "$@"; do
  [ "$arg" = "--frontend" ] && START_FRONTEND=true && break
done

if [ "$START_FRONTEND" = true ]; then
  echo "=== Starting frontend (http://localhost:8001) ==="
  ( cd "$PROJECT_ROOT/renren-fast-vue" && npm run dev ) &
  wait
else
  wait
fi
