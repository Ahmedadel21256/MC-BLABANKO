# SHABAB SMP feature inventory

## ShababCore source now in the repository

The Java plugin code is in `plugins/shabab-core`. It has not yet been confirmed to compile or run on Codespaces.

- `/games` opens a four-entry network selector GUI.
- `/hub` connects back to Velocity's `lobby` backend.
- `/shop` opens a simple starter trade GUI: emeralds for bread, cooked beef, wood and iron.
- `/shababnpc games` and `/shababnpc shop` let an operator spawn persistent villager menu NPCs.
- The shop has its own config toggle and is enabled on the Survival backend by `scripts/build_core.sh`.
- Network transfers use Velocity's BungeeCord-compatible plugin message channel. Ensure your proxy supports it and verify actual connections.

### Not finished

The current plugin does not implement BedWars beds/team logic, SkyWars combat/loot, arenas, economy currency balances, rank permissions, claims, player statistics or a sophisticated map. Backend placeholders do not constitute playable minigames.

`scripts/build_core.sh` packages and places the compiled plugin on each backend only in an environment with Maven and the Paper dependency accessible. CI should be used to check compilation; GitHub Actions does not operate on the live Codespaces server.

Authentication is set to online-mode on the public Velocity proxy. Previous TLauncher/offline-mode users will need a safe reviewed approach before joining; do not disable proxy authentication casually.
