# SHABAB SMP features and player setup

## Player profile

The network's featured player is **I_luca1** (TLauncher username), displayed in the Lobby welcome message. A tagged compass is offered on joining the Lobby; right-clicking it opens the network selector. `I_luca1` is **not** automatically granted operator status or permissions.

Offline-mode usernames are not verifiable identities. Any visitor with access to an offline-mode server can impersonate a username, so **do not assign OP to I_luca1 merely by name** while that configuration is in use.

## ShababCore

The Java source in `plugins/shabab-core` includes:
- `/hub` to request a Velocity transfer to Lobby.
- `/games` for Lobby, Survival, BedWars and SkyWars selectors.
- `/shop` for a simple emerald-based starter trade menu.
- `/shababnpc games` and `/shababnpc shop` for operator-controlled villager menu NPC placement.
- Lobby welcome message referencing `I_luca1`, without automatic administrator rights.

The plugin is configured separately per backend by `scripts/build_core.sh`. It needs to be built and installed on the running Codespaces instances; GitHub source changes do not auto-install themselves.

## Still outstanding

BedWars and SkyWars **do not yet have actual game logic, maps or arenas**. Survival protection, advanced economy, NPC map construction and DiscordSRV still need installation and runtime tests. Local-only offline-mode proxy is a temporary development setup; do not expose it publicly.

The Java plugin previously passed CI compilation. Subsequent changes should be checked by new GitHub Actions runs.

## I_luca1 development privileges
`bootstrap.sh` now runs `scripts/grant_local_op.py` to create `ops.json` operator records at level 4 for the offline UUID of `I_luca1` on each of the four *new* Paper backend directories. It validates that the proxy is offline-mode and bound to `127.0.0.1` and that each backend is loopback only. These are not online-account identities; **do not expose this setup to the internet**, because an impersonator using the same username would gain OP. The runtime files are gitignored and not generated in Codespaces merely by committing GitHub source.
