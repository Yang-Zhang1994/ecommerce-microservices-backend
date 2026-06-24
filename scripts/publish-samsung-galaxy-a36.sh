#!/usr/bin/env bash
# Publish Samsung Galaxy A36 via API: upload desktop images, save SPU (4 SKUs), list to ES.
set -euo pipefail

BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"
DESKTOP="${DESKTOP:-$HOME/Desktop}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export BASE DESKTOP ROOT
exec python3 "${ROOT}/scripts/publish-samsung-galaxy-a36.py"
