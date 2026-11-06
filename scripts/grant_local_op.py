#!/usr/bin/env python3
"""Grant I_luca1 OP level 4 in the new localhost-only test backends.

This is intentionally not suitable for a public offline-mode network:
any player who can join with that name can inherit the permissions.
"""
from __future__ import annotations

import hashlib
import json
from pathlib import Path
import sys
import tomllib
import uuid

PLAYER = "I_luca1"
SERVERS = ("lobby", "survival", "bedwars", "skywars")
base = (Path(sys.argv[1]) if len(sys.argv) > 1 else
        Path(__file__).resolve().parents[1] / "shabab-network").resolve()

velocity_file = base / "proxy" / "velocity.toml"
if not velocity_file.is_file():
    raise SystemExit("Velocity configuration missing; configure network first.")
with velocity_file.open("rb") as handle:
    velocity = tomllib.load(handle)
if velocity.get("online-mode") is not False or velocity.get("bind") != "127.0.0.1:25565":
    raise SystemExit("REFUSING to grant offline-name OP: proxy must be localhost-only and offline.")

offline_uuid = str(uuid.UUID(
    bytes=hashlib.md5(("OfflinePlayer:" + PLAYER).encode("utf-8")).digest(),
    version=3
))
for name in SERVERS:
    server = base / name
    props = server / "server.properties"
    if not props.is_file():
        raise SystemExit(f"Missing backend properties for {name}")
    settings = dict(line.split("=", 1) for line in props.read_text().splitlines()
                    if "=" in line and not line.startswith("#"))
    if settings.get("server-ip") != "127.0.0.1" or settings.get("online-mode") != "false":
        raise SystemExit(f"REFUSING to grant offline-name OP: {name} not localhost/offline.")
    ops_path = server / "ops.json"
    if ops_path.exists():
        operators = json.loads(ops_path.read_text(encoding="utf-8"))
        if not isinstance(operators, list):
            raise SystemExit(f"Unexpected ops.json format: {name}")
    else:
        operators = []
    if not any(item.get("uuid") == offline_uuid for item in operators):
        if any(item.get("name", "").casefold() == PLAYER.casefold() for item in operators):
            raise SystemExit(f"Conflicting operator entry for {PLAYER} in {name}; review manually.")
        operators.append({"uuid": offline_uuid, "name": PLAYER, "level": 4,
                          "bypassesPlayerLimit": True})
        ops_path.write_text(json.dumps(operators, indent=2) + "\n", encoding="utf-8")
        ops_path.chmod(0o600)
        print(f"{name}: granted OP level 4 to {PLAYER} (offline UUID)")
    else:
        print(f"{name}: existing operator entry preserved for {PLAYER}")
print("LOCAL DEVELOPMENT ONLY. Never expose this offline-mode server to other people.")
