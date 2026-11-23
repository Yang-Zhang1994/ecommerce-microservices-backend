#!/usr/bin/env bash
# Start the read-only triage MCP server. The first run creates a private virtualenv in the plugin's data
# directory (not the plugin folder, which is replaced on update); later runs start immediately.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DATA="${CLAUDE_PLUGIN_DATA:-${HOME}/.cache/grainmart-triage}"
VENV="${DATA}/venv"
REQS="${ROOT}/server/requirements.txt"
mkdir -p "${DATA}"
# kubectl and helm read ~/.kube/config. Some hosts (for example `claude plugin eval`) run servers with a
# different HOME, so fall back to the real user's kubeconfig rather than silently seeing no cluster.
if [[ -z "${KUBECONFIG:-}" && ! -f "${HOME}/.kube/config" ]]; then
  REAL_HOME="$(eval echo "~$(id -un)")"
  [[ -f "${REAL_HOME}/.kube/config" ]] && export KUBECONFIG="${REAL_HOME}/.kube/config"
fi
if [[ ! -x "${VENV}/bin/python" ]] || ! cmp -s "${REQS}" "${VENV}/requirements.installed"; then
  python3 -m venv "${VENV}" >&2
  "${VENV}/bin/pip" install -q -r "${REQS}" >&2
  cp "${REQS}" "${VENV}/requirements.installed"
fi
exec "${VENV}/bin/python" "${ROOT}/server/platform_mcp.py"
