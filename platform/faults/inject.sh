#!/usr/bin/env bash
# Break the sandboxed review service in one of three realistic ways, for testing incident triage.
# Usage: platform/faults/inject.sh bad-image|missing-secret|wrong-config
# Undo:  gm sandbox delete && gm sandbox deploy review
set -euo pipefail
NS=gm-sandbox
CTX=kind-gulimall
DEPLOY=gulimall-review

case "${1:-}" in
  bad-image)
    # A release that points at an image tag nobody built (a typo, or CI never pushed it).
    helm --kube-context "$CTX" -n "$NS" upgrade gm-sandbox "$(dirname "$0")/../../k8s/helm/gulimall" \
      --reuse-values --set images.review.tag=sbx-4e1f00c0ffee
    ;;
  missing-secret)
    # The workload now reads a database password from a Secret that was never created in this namespace.
    kubectl --context "$CTX" -n "$NS" patch deployment "$DEPLOY" --type=json -p='[
      {"op": "add", "path": "/spec/template/spec/containers/0/env/-",
       "value": {"name": "REVIEW_DB_PASSWORD", "valueFrom": {"secretKeyRef": {"name": "review-db", "key": "password"}}}}]'
    ;;
  wrong-config)
    # A config change with a typo in the Consul host.
    kubectl --context "$CTX" -n "$NS" set env deployment/"$DEPLOY" SPRING_CLOUD_CONSUL_HOST=consul-server
    ;;
  *)
    echo "usage: $0 bad-image|missing-secret|wrong-config" >&2
    exit 2
    ;;
esac
echo "injected: $1"
