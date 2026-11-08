#!/usr/bin/env bash
# Loads k8s/helm/gulimall/consul-config/<name>.yml into Consul KV at config/<name>/data.
# Local:  docker compose up -d consul && k8s/scripts/seed-consul-config.sh
# Remote: CONSUL_HTTP_ADDR=http://localhost:8500 (e.g. after kubectl port-forward svc/consul 8500)
set -euo pipefail

CONSUL_HTTP_ADDR="${CONSUL_HTTP_ADDR:-http://localhost:8500}"
CONFIG_DIR="$(cd "$(dirname "$0")/../helm/gulimall/consul-config" && pwd)"

for file in "$CONFIG_DIR"/*.yml; do
  name="$(basename "$file" .yml)"
  curl -fsS -X PUT --data-binary @"$file" "$CONSUL_HTTP_ADDR/v1/kv/config/$name/data" >/dev/null
  echo "seeded config/$name/data"
done
