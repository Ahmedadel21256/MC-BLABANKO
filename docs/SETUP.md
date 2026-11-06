# SHABAB SMP — GitHub Codespaces deployment

## What exists
This repository provides a source-controlled **network setup scaffold**, not a finished or running Minecraft network. The existing `minecraft-server` folder is intentionally left alone. GitHub connector access does not provide Codespaces terminal access.

## Software
- Java **25 or newer** for current Velocity 4.3.1; this can also run the older Paper 1.21.4 servers.
- Python 3.11+ (`tomllib`).
- Internet for PaperMC's official Fill v3 downloads.
- Adequate Codespaces memory; five Java processes may exceed small plans.

## Setup
On the Codespaces checkout after syncing the repository, run the version-checked `scripts/bootstrap.sh` yourself through a terminal-capable environment. It creates `shabab-network` and does **not** overwrite `minecraft-server`, your old worlds, or existing settings. Server downloads are validated by SHA-256 hashes. Network configuration is generated once; future runs preserve edited configs. The only entry point is the Velocity proxy on 25565; four Paper backends bind to 127.0.0.1.

Read and accept the [Minecraft EULA](https://www.minecraft.net/eula) for **each** backend. The start script refuses to run until every instance has its own `eula=true` file, created manually after review. Do not commit those EULA files.

## Gameplay not yet built
- Lobby: import/build a hub map, add NPC portals/game selection and Scoreboard.
- Survival: decide on claims, protections, permissions, shop/economy and starter kits; review compatible plugin licenses.
- BedWars: install and test a version-compatible minigame plugin, configure teams, spawns, shops, beds and generators.
- SkyWars: install and test a version-compatible plugin and configure islands, chest loot and scoring.
- Discord: install DiscordSRV with bot token kept only in secrets/local config; linking is not a complete offline-account authentication system.

The script makes new backend directories and worlds on first launch but will **not** magically supply minigame arenas or gameplay logic.

## Security
The Velocity proxy is online-mode=true and authenticates official Minecraft accounts. Its backends use online-mode=false **only behind** modern Velocity forwarding, a shared random secret, and loopback binding. Do not publish their ports. The previous TLauncher-only setup needs a separately reviewed approach; turning the public proxy to offline-mode without secured player authentication permits impersonation.

## Operations
Codespaces can sleep, its free ports may be unsuitable for native game clients, and it is not a guaranteed 24/7 Minecraft host. Test game connections with a Minecraft client only after verifying that an appropriate TCP access path exists. Test RAM usage and each backend's startup logs. `scripts/validate_network.py` is a static config test, not a live connectivity test.
