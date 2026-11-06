#!/usr/bin/env python3
"""Static validation. This does NOT claim server/plugin runtime compatibility."""
from pathlib import Path
import sys
import tomllib

base = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / "shabab-network"
proxy_path = base / "proxy" / "velocity.toml"
secret_path = base / "proxy" / "forwarding.secret"
assert proxy_path.exists(), "Proxy configuration missing"
with proxy_path.open("rb") as file:
    proxy = tomllib.load(file)
assert proxy.get("online-mode") is False, "Local development proxy expected to use offline mode"
assert proxy.get("bind") == "127.0.0.1:25565", "Offline-mode proxy MUST remain localhost-only"
assert proxy.get("player-info-forwarding-mode") == "modern"
assert proxy.get("forwarding-secret-file") == "forwarding.secret"
assert proxy.get("servers", {}).get("try") == ["lobby"]
secret = secret_path.read_text(encoding="utf-8").strip()
assert len(secret) >= 32, "Forwarding secret is too short"
for name, port in (("lobby", 25566), ("survival", 25567), ("bedwars", 25568), ("skywars", 25569)):
    props = (base / name / "server.properties").read_text(encoding="utf-8")
    items = dict(line.split("=", 1) for line in props.splitlines() if "=" in line and not line.startswith("#"))
    assert items.get("server-ip") == "127.0.0.1", name
    assert items.get("server-port") == str(port), name
    assert items.get("online-mode") == "false", name
    assert items.get("white-list") == "false", name
    assert proxy["servers"][name] == f"127.0.0.1:{port}", name
    paper = (base / name / "config" / "paper-global.yml").read_text(encoding="utf-8")
    assert "enabled: true" in paper and "online-mode: false" in paper
    assert "secret: '" + secret + "'" in paper, name
print("PASS: proxy ports, local-only offline mode, and forwarding secrets agree.")
print("NOTICE: This is a static config check, not a running Minecraft test.")
