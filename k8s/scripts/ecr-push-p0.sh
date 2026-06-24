#!/usr/bin/env bash
# Backward-compatible alias — pushes full stack (not only P0).
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/ecr-push-all.sh" "$@"
