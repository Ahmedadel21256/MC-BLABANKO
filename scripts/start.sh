#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="$ROOT/shabab-network"
python3 "$ROOT/scripts/validate_network.py" "$BASE"
if [[ -e "$BASE/.network-running" ]]; then
  echo "A start script marker already exists. Check processes before launching again." >&2
  exit 1
fi
for item in lobby survival bedwars skywars; do
  if [[ ! -f "$BASE/$item/eula.txt" ]] || ! grep -Eq '^eula=true\s*$' "$BASE/$item/eula.txt"; then
    echo "Read Mojang's EULA and accept it individually in $BASE/$item/eula.txt first." >&2
    exit 1
  fi
  [[ -f "$BASE/$item/paper.jar" ]] || { echo "Missing $item/paper.jar" >&2; exit 1; }
done
[[ -f "$BASE/proxy/velocity.jar" ]] || { echo "Missing velocity.jar" >&2; exit 1; }
command -v java >/dev/null || { echo "Java not installed" >&2; exit 1; }
pids=()
cleanup() {
  trap - EXIT INT TERM
  for pid in "${pids[@]}"; do kill -TERM "$pid" 2>/dev/null || true; done
  wait 2>/dev/null || true
  rm -f "$BASE/.network-running"
}
trap cleanup EXIT INT TERM
printf '%s\n' "$$" > "$BASE/.network-running"
mkdir -p "$BASE/run-logs"
for item in lobby survival bedwars skywars; do
  (
    cd "$BASE/$item"
    exec java -Xms256M -Xmx768M -jar paper.jar --nogui
  ) >> "$BASE/run-logs/$item.log" 2>&1 &
  pids+=("$!")
done
(
  cd "$BASE/proxy"
  exec java -Xms128M -Xmx256M -jar velocity.jar
) >> "$BASE/run-logs/proxy.log" 2>&1 &
pids+=("$!")
echo "Started processes. Logs: $BASE/run-logs/"
echo "This is a development stack; game plugins and maps are NOT yet installed."
wait
