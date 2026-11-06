#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BASE="$ROOT/shabab-network"
command -v mvn >/dev/null 2>&1 || { echo "Maven is required to build ShababCore" >&2; exit 1; }
mvn -q -f "$ROOT/plugins/shabab-core/pom.xml" -DskipTests package
JAR="$ROOT/plugins/shabab-core/target/shabab-core-1.0.0.jar"
test -s "$JAR" || { echo "Build did not produce the expected jar" >&2; exit 1; }
for name in lobby survival bedwars skywars; do
  mkdir -p "$BASE/$name/plugins"
  cp "$JAR" "$BASE/$name/plugins/ShababCore.jar"
  mkdir -p "$BASE/$name/plugins/ShababCore"
  if [[ ! -f "$BASE/$name/plugins/ShababCore/config.yml" ]]; then
    printf 'server-name: %s\nshop-enabled: %s\n' "$name" "$([[ "$name" == "survival" ]] && echo true || echo false)" > "$BASE/$name/plugins/ShababCore/config.yml"
  fi
done
echo "ShababCore built and installed into four BACKEND folders."
echo "Restart the servers to load the plugin; do not run twice concurrently."
