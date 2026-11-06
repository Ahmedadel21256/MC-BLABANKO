#!/usr/bin/env bash
# Install current repository's ShababCore onto the 4 Codespaces backend plugin folders.
# Does not sync Git itself, overwrite worlds, touch server properties, or restart servers.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="$ROOT/shabab-network"
cd "$ROOT"

if [[ ! -d "$BASE" ]]; then
  echo "ERROR: shabab-network missing. Network must be bootstrapped first." >&2
  exit 2
fi
if ! command -v mvn >/dev/null 2>&1; then
  echo "ERROR: Maven not installed." >&2
  exit 2
fi

# Installing plugin jars with Paper running is unsafe; require a clean shutdown.
if pgrep -fa '[j]ava.*(paper\.jar|velocity\.jar)' >/dev/null; then
  echo "ERROR: Minecraft Java processes still running. Shut down the entire network safely before installing updates." >&2
  echo "No plugin files have been changed."
  exit 3
fi
for name in lobby survival bedwars skywars; do
  if [[ ! -d "$BASE/$name/plugins" ]]; then
    echo "ERROR: $BASE/$name/plugins missing. No changes made." >&2
    exit 2
  fi
done

echo "Building latest ShababCore source..."
mvn --batch-mode -q -f "$ROOT/plugins/shabab-core/pom.xml" -DskipTests package
JAR="$ROOT/plugins/shabab-core/target/shabab-core-1.0.0.jar"
test -s "$JAR" || { echo "ERROR: no built ShababCore jar" >&2; exit 2; }

# Stage only copies of the JAR. Keep one backup of previously installed JARs.
BACKUP="$BASE/plugin-backups/$(date -u +%Y%m%dT%H%M%SZ)"
for name in lobby survival bedwars skywars; do
  if [[ -f "$BASE/$name/plugins/ShababCore.jar" ]]; then
    mkdir -p "$BACKUP/$name"
    cp -p "$BASE/$name/plugins/ShababCore.jar" "$BACKUP/$name/ShababCore.jar"
  fi
done
for name in lobby survival bedwars skywars; do
  target="$BASE/$name/plugins/ShababCore.jar"
  staged="$BASE/$name/plugins/.ShababCore.jar.new"
  cp "$JAR" "$staged"
  mv -f "$staged" "$target"
  echo "UPDATED: $name -> ShababCore.jar"
done
echo "SUCCESS: current ShababCore code deployed to four backends."
echo "Previously running worlds/configs were preserved."
echo "Saved previous jars to $BACKUP (when present)."
echo "The new starter maps are constructed on next backend startup only where the target area is empty."
echo "BedWars/SkyWars islands are NOT full playable minigames. Verify logs after starting."
