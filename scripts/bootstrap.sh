#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="$ROOT/shabab-network"
mkdir -p "$BASE/proxy"
for name in lobby survival bedwars skywars; do
  mkdir -p "$BASE/$name/plugins"
done
python3 "$ROOT/scripts/download_servers.py" "$BASE"
echo "Created fresh server directories in $BASE"
echo "Next: read docs/SETUP.md before starting or exposing any ports."
