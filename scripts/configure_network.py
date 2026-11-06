#!/usr/bin/env python3
"""Create fresh, loopback-only Paper/Velocity configuration for SHABAB SMP."""
from pathlib import Path
import secrets
import sys

base=Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]/"shabab-network"
base.mkdir(parents=True,exist_ok=True)
secret_file=base/"proxy"/"forwarding.secret"
secret_file.parent.mkdir(parents=True,exist_ok=True)
if not secret_file.exists():
    secret_file.write_text(secrets.token_urlsafe(48)+"\n",encoding="utf-8")
secret=secret_file.read_text(encoding="utf-8").strip()
def write_new(path,text):
    path.parent.mkdir(parents=True,exist_ok=True)
    if not path.exists():
        path.write_text(text,encoding="utf-8")
        print("Created",path.relative_to(base))
    else:
        print("Preserved existing",path.relative_to(base))

write_new(base/"proxy"/"velocity.toml",'''config-version = "2.7"
bind = "0.0.0.0:25565"
motd = "<gold>SHABAB SMP</gold> <gray>| Lobby, Survival, BedWars, SkyWars"
show-max-players = 100
online-mode = true
player-info-forwarding-mode = "modern"
forwarding-secret-file = "forwarding.secret"
announce-forge = false
kick-existing-players = false
ping-passthrough = "DISABLED"
enable-player-address-logging = true

[servers]
lobby = "127.0.0.1:25566"
survival = "127.0.0.1:25567"
bedwars = "127.0.0.1:25568"
skywars = "127.0.0.1:25569"
try = ["lobby"]

[forced-hosts]
''')
for name,port in (("lobby",25566),("survival",25567),("bedwars",25568),("skywars",25569)):
    d=base/name
    write_new(d/"server.properties",f"""server-port={port}
server-ip=127.0.0.1
motd=SHABAB SMP | {name.title()}
online-mode=false
enforce-secure-profile=false
white-list=true
enforce-whitelist=true
spawn-protection=16
enable-command-block=false
view-distance=6
simulation-distance=4
max-players=30
""")
    write_new(d/"config"/"paper-global.yml",f"""proxies:
  velocity:
    enabled: true
    online-mode: true
    secret: '{secret}'
""")
print("Proxy is online-mode=true: requires authenticated Minecraft accounts.")
print("No EULA acceptance was generated. Read and accept Mojang EULA yourself before launching.")
