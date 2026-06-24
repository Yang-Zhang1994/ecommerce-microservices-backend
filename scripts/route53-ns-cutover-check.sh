#!/usr/bin/env bash
# After changing registrar NS to Route53, run this until NS + site look correct.
set -euo pipefail

DOMAIN="${1:-yangzhangtech.online}"
# After terraform apply module.dns_zone — run: terraform output route53_name_servers
EXPECTED_NS=(
  ns-1293.awsdns-33.org
  ns-1826.awsdns-36.co.uk
  ns-234.awsdns-29.com
  ns-852.awsdns-42.net
)

echo "=== NS for $DOMAIN (expect awsdns) ==="
dig NS "$DOMAIN" +short | sort

echo ""
echo "=== www A (expect ALB alias after propagation) ==="
dig www."$DOMAIN" A +short

echo ""
echo "=== HTTP health (ALB) ==="
curl -sS -o /dev/null -w "http://www.%s/actuator/health -> %{http_code}\n" "www.$DOMAIN" || true

echo ""
echo "=== HTTPS health (after terraform apply + cert ISSUED) ==="
curl -sS -o /dev/null -w "https://www.%s/actuator/health -> %{http_code}\n" "www.$DOMAIN" || true

missing=0
for ns in "${EXPECTED_NS[@]}"; do
  if ! dig NS "$DOMAIN" +short | grep -qiF "$ns"; then
    missing=1
  fi
done
if [[ $missing -eq 0 ]]; then
  echo ""
  echo "OK: all four Route53 name servers visible in public DNS."
  exit 0
fi
echo ""
echo "NS not fully on Route53 yet — finish registrar change or wait for propagation."
exit 1
