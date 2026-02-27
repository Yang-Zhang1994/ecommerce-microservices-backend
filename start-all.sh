#!/bin/bash
# Gulimall 一键启动: Consul, Gateway, renren-fast, 各微服务
# 端口冲突时自动 kill 原进程

set -e
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

# 各服务端口（不含 8500 Consul；不含 order 9000、search 12001）
PORTS=(88 8080 12000 8000 10000 11000 30000)

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

echo "=== Killing processes on conflicting ports ==="
for port in "${PORTS[@]}"; do
  kill_port "$port"
done

echo ""
echo "=== Starting Consul (Docker) ==="
docker compose up -d consul 2>/dev/null || docker-compose up -d consul 2>/dev/null || {
  echo "Warning: docker compose failed. Ensure Consul runs on 8500."
}
sleep 3

echo ""
echo "=== Starting Gateway, renren-fast, and microservices ==="

start_service() {
  local module=$1
  local name=$2
  (
    cd "$PROJECT_ROOT/$module"
    echo "[$name] Starting..."
    mvn -q spring-boot:run
  ) &
}

start_service "renren-fast" "renren-fast"
sleep 2
start_service "gulimall-gateway" "gateway"
sleep 2

start_service "gulimall-coupon" "coupon"
start_service "gulimall-member" "member"
start_service "gulimall-product" "product"
start_service "gulimall-ware" "ware"
start_service "gulimall-third-party" "third-party"

echo ""
echo "=== All services starting in background ==="
echo "Consul UI:    http://localhost:8500"
echo "Gateway:      http://localhost:88"
echo "renren-fast:  http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop all (or run: pkill -f spring-boot:run)"
wait
