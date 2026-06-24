#!/bin/bash
# Start local Nginx for ecommerce.com domains (macOS).
# Usage:
#   ./scripts/start-nginx-local.sh       # port 80 (needs sudo + password)
#   ./scripts/start-nginx-local.sh 8080  # port 8080 (no sudo): http://www.ecommerce.com:8080/
# Prerequisites: brew install nginx; hosts in scripts/local-hosts-auth-ecommerce.txt
# Backend: gateway :88, gulimall-mall :3001, renren-fast-vue :8001

set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NGINX_BIN="${NGINX_BIN:-/opt/homebrew/opt/nginx/bin/nginx}"
PREFIX="$ROOT/nginx-local"
PORT="${1:-80}"
CONF="$PREFIX/nginx-${PORT}.conf"
SITE_CONF="$PREFIX/ecommerce-sites.conf"

mkdir -p "$PREFIX/logs"

if [ ! -x "$NGINX_BIN" ]; then
  echo "nginx not found. Install: brew install nginx"
  exit 1
fi

sed "s/listen 80;/listen ${PORT};/g" "$ROOT/docs/nginx-ecommerce-local.conf" > "$SITE_CONF"

cat > "$CONF" <<EOF
worker_processes 1;
error_log logs/error.log;
pid logs/nginx.pid;
events { worker_connections 1024; }
http {
    include /opt/homebrew/etc/nginx/mime.types;
    default_type application/octet-stream;
    sendfile on;
    keepalive_timeout 65;
    access_log logs/access.log;
    include $SITE_CONF;
}
EOF

run_nginx() {
  local use_sudo=$1
  if [ "$use_sudo" = "1" ]; then
    sudo "$NGINX_BIN" -p "$PREFIX" -c "$CONF" "${@:2}"
  else
    "$NGINX_BIN" -p "$PREFIX" -c "$CONF" "${@:2}"
  fi
}

if lsof -ti:"$PORT" >/dev/null 2>&1; then
  pid=$(lsof -ti:"$PORT" | head -1)
  if [ -f "$PREFIX/logs/nginx.pid" ] && [ "$(cat "$PREFIX/logs/nginx.pid")" = "$pid" ]; then
    echo "Reloading nginx on port $PORT (pid $pid)..."
    if [ "$PORT" = "80" ]; then run_nginx 1 -s reload; else run_nginx 0 -s reload; fi
    exit 0
  fi
  echo "Port $PORT is in use by pid $pid. Stop it first."
  exit 1
fi

echo "Testing config (port $PORT)..."
if [ "$PORT" = "80" ]; then
  run_nginx 1 -t
  echo "Starting nginx on port 80..."
  run_nginx 1
  echo "OK: http://www.ecommerce.com/"
else
  run_nginx 0 -t
  echo "Starting nginx on port $PORT (no sudo)..."
  run_nginx 0
  echo "OK: http://www.ecommerce.com:${PORT}/"
fi
echo "Stop: $NGINX_BIN -p $PREFIX -c $CONF -s stop"
if [ "$PORT" = "80" ]; then echo "(prefix stop may need sudo)"; fi
