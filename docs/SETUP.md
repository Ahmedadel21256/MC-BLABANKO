# SHABAB SMP — Setup and safety

## Codespaces
Run `bash scripts/bootstrap.sh` in the repository root with Java 21 and Python 3 installed. It creates `shabab-network` separately from the existing `minecraft-server`.

The download script uses PaperMC's API and needs outbound internet. If the API changes, update the downloader; don't use unverified jar download sites.

## Networking
Configure Velocity on port 25565, and Paper backends on ports 25566–25569, bound to loopback where possible. Use modern Velocity forwarding with a random secret, and configure the **same** secret in each Paper backend's Velocity settings. Do not expose backend ports.

## Games
- Lobby: import/build hub world; install a compatible NPC plugin, game selector and scoreboard.
- Survival: new survival world; configure permissions, claims, economy, shops, and anti-grief.
- BedWars: install a maintained Paper-compatible BedWars plugin and configure tested arenas, team spawns, beds, generators and shops.
- SkyWars: install a maintained Paper-compatible SkyWars plugin and configure tested islands, loot and match logic.

## Authentication
TLauncher/offline-mode accounts can impersonate names. DiscordSRV account linking alone does not securely authenticate those names. Decide on an authentication approach and test it before any public release. Keep Discord bot tokens outside GitHub and never paste them in chat.

## Capacity
Codespaces may suspend and is not guaranteed always-on hosting. Four Paper servers plus a proxy can exceed small Codespaces memory budgets. Start components one at a time and monitor RAM.

## Existing files
Do not delete or overwrite the original `minecraft-server` or worlds. Make a separate backup before migrating anything.
