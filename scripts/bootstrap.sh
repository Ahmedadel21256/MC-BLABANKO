#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="$ROOT/shabab-network"
if [[ ! -d "$ROOT/scripts" ]]; then
  echo "Unexpected project layout" >&2
  exit 1
fi
if ! command -v java >/dev/null 2>&1; then
  echo "Java is required (Java 21 or newer for Paper 1.21.4)." >&2
  exit 1
fi
if ! command -v python3 >/dev/null 2>&1; then
  echo "Python 3 is required." >&2
  exit 1
fi
python3 "$ROOT/scripts/configure_network.py" "$BASE"
python3 "$ROOT/scripts/download_servers.py" "$BASE"
python3 "$ROOT/scripts/validate_network.py" "$BASE"
echo "Prepared $BASE. No existing minecraft-server files were modified."
echo "Review and accept the Mojang Minecraft EULA for each instance before launching."
