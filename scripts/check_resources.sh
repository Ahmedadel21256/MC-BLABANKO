#!/usr/bin/env bash
# SHABAB SMP Codespaces resource diagnostics; read-only.
set -euo pipefail
echo "=== SHABAB SMP Codespaces resources ==="
if command -v nproc >/dev/null; then echo "CPUs: $(nproc)"; fi
if command -v free >/dev/null; then free -h; fi
if [[ -r /sys/fs/cgroup/memory.max ]]; then
  limit="$(cat /sys/fs/cgroup/memory.max)"
  if [[ "$limit" == "max" ]]; then
    echo "Container memory cgroup: no explicit limit"
  else
    awk -v n="$limit" 'BEGIN {printf "Container memory cgroup limit: %.2f GiB\n", n/1073741824}'
  fi
fi
echo
echo "=== Minecraft Java processes ==="
pgrep -af '[j]ava.*(paper.jar|velocity.jar)' || echo "None detected by standard Paper/Velocity process names"
echo
echo "=== Server statuses in existing logs (not guaranteed live) ==="
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
for server in proxy lobby survival bedwars skywars; do
  log="$ROOT/shabab-network/run-logs/$server.log"
  echo "[$server]"
  if [[ -r "$log" ]]; then
    grep -E 'Done \(|OutOfMemoryError|Exception|Created .*starter' "$log" | tail -n 4 || true
  else
    echo "No log yet"
  fi
done
echo
echo "Codespaces machine size must be selected in GitHub; a script cannot add more physical RAM."
echo "Never expose the existing offline-mode local-only proxy to a public tunnel."
