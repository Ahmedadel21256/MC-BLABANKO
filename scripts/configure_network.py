#!/usr/bin/env python3
"""Scaffold new proxy and Paper configurations without overwriting existing files."""
from pathlib import Path
import secrets
import sys

base = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / "shabab-network"
base.mkdir(parents=True, exist_ok=True)
secret_file = base / "proxy" / "forwarding.secret"
secret_file.parent.mkdir(parents=True, exist_ok=True)
if not secret_file.exists():
    secret_file.write_text(secrets.token_urlsafe(48) + "\n", encoding="utf-8")
    secret_file.chmod(0o600)
secret = secret_file.read_text(encoding="utf-8").strip()
if not secret:
    raise RuntimeError("Empty forwarding secret")


def write_new(path, contents):
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists():
        print("Preserved existing", path.relative_to(base))
    else:
        path.write_text(contents, encoding="utf-8")
        print("Created", path.relative_to(base))


write_new(base / "proxy" / "velocity.toml", """config-version = "2.7"
bind = "127.0.0.1:25565"
motd = "<gold>SHABAB SMP</gold> <gray>| Lobby, Survival, BedWars, SkyWars"
show-max-players = 100
online-mode = false
force-key-authentication = false
player-info-forwarding-mode = "modern"
forwarding-secret-file = "forwarding.secret"
announce-forge = false
kick-existing-players = false
ping-passthrough = "DISABLED"
enable-player-address-logging = false

[servers]
lobby = "127.0.0.1:25566"
survival = "127.0.0.1:25567"
bedwars = "127.0.0.1:25568"
skywars = "127.0.0.1:25569"
try = ["lobby"]

[forced-hosts]
""")

for name, port in (("lobby", 25566), ("survival", 25567), ("bedwars", 25568), ("skywars", 25569)):
    location = base / name
    write_new(location / "server.properties", f"""server-port={port}
server-ip=127.0.0.1
motd=SHABAB SMP | {name.title()}
online-mode=false
enforce-secure-profile=false
white-list=false
enforce-whitelist=false
spawn-protection=16
enable-command-block=false
view-distance=6
simulation-distance=4
max-players=30
level-name=world
""")
    # Paper merges missing options with its built-in defaults on initial startup.
    write_new(location / "config" / "paper-global.yml", f"""proxies:
  velocity:
    enabled: true
    online-mode: false
    secret: '{secret}'
""")
    (location / "plugins").mkdir(parents=True, exist_ok=True)
print("LOCAL TEST ONLY: Proxy uses offline mode, and all servers bind to 127.0.0.1. DO NOT expose it publicly.")
print("No EULA has been accepted and no server has been launched.")
