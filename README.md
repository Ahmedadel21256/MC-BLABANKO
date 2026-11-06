# SHABAB SMP

Minecraft network development project for **GitHub Codespaces**, assembled around Velocity and separate Paper backends.

## Current state
- GitHub source and scripts: available.
- Minecraft server jars: downloaded **only when** bootstrap runs inside Codespaces.
- Fresh Lobby, Survival, BedWars, SkyWars backends: configured by the bootstrap script, **not launched or play-tested**.
- Lobby map, NPC game selectors, economy shops, BedWars/SkyWars gameplay and arenas: **not yet installed**.
- DiscordSRV: requires a privately supplied Discord bot token and testing.
- Authentication: **LOCAL TEST ONLY**: the new scaffold uses offline-mode for TLauncher testing and binds the Velocity proxy to 127.0.0.1. Do not forward port 25565 or publish this configuration: usernames can be impersonated.

## Repository contents
- `scripts/bootstrap.sh` prepares a separate `shabab-network/` folder; it never overwrites the existing `minecraft-server/` folder.
- `scripts/download_servers.py` fetches SHA-256-verified official Paper and Velocity jars.
- `scripts/configure_network.py` creates new proxy and four Paper backend configs.
- `scripts/validate_network.py` checks ports and forwarding settings statically.
- `scripts/start.sh` can launch development processes **after** the operator has reviewed and accepted Minecraft's EULA for each server.

## Architecture

| Component | Local port | Public |
| --- | --- | --- |
| Velocity proxy | 25565 | Localhost only (offline-mode test) |
| Paper Lobby | 25566 | No, loopback |
| Paper Survival | 25567 | No, loopback |
| Paper BedWars | 25568 | No, loopback |
| Paper SkyWars | 25569 | No, loopback |

## Boundaries
Codespaces may suspend and cannot guarantee 24/7 uptime. Its available memory may be insufficient for five Java processes; 8 GB+ is recommended for experimentation. Read [setup and outstanding work](docs/SETUP.md) before attempting to invite players. No server has been started through this GitHub connector, which cannot access Codespaces' live terminal.
