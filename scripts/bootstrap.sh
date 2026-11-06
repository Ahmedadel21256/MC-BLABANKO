#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="$ROOT/shabab-network"
command -v java >/dev/null 2>&1 || { echo "Install Java 25 before bootstrap." >&2; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "Python 3.11+ is required." >&2; exit 1; }
JAVA_MAJOR="$(java -version 2>&1 | sed -n '1s/.*version "\([0-9][0-9]*\).*/\1/p')"
if [[ -z "$JAVA_MAJOR" || "$JAVA_MAJOR" -lt 25 ]]; then
  echo "Velocity 4.3.1 requires Java 25+. Selected Java: $(java -version 2>&1 | head -1)" >&2
  exit 1
fi
python3 -c 'import tomllib' || { echo "Python 3.11+ with tomllib required" >&2; exit 1; }
python3 "$ROOT/scripts/configure_network.py" "$BASE"
python3 "$ROOT/scripts/download_servers.py" "$BASE"
python3 "$ROOT/scripts/validate_network.py" "$BASE"
echo "Prepared $BASE. No existing minecraft-server files were modified."
echo "Review and accept the Mojang Minecraft EULA for each instance before launching."
