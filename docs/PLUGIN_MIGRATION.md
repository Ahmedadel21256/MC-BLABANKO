# Existing plugin migration — SHABAB SMP

## Source
The screenshots show the old Paper plugins directory containing jar files and configuration folders. They are **not present in the GitHub repository**. The GitHub connector cannot access the Codespaces file system or copy those binaries by itself.

`scripts/migrate_plugins.py` inspects **minecraft-server/plugins** (or a specified source folder) in Codespaces, and maps available jars to the right new Paper backend. The program starts in preview mode; `--apply` copies only unambiguous local jars without deleting or overwriting anything. It never updates old configs, never accepts the Minecraft EULA, never starts the server, and never copies Discord tokens.

## Planned destination
| Destination | Candidate plugins from existing folder |
| --- | --- |
| Lobby | DeluxeHub, ZNPCs, TAB, PlaceholderAPI, LuckPerms, DiscordSRV, AdvancedBan |
| Survival | EssentialsX, Vault, WorldEdit, WorldGuard, Multiverse-Core, AdvancedRegionMarket, AFKPlus, PlayTime, ClearLag, PlaceholderAPI, LuckPerms, AdvancedBan, voicechat |
| BedWars | BedWars, LuckPerms, AdvancedBan |
| SkyWars | SkyWarsReloaded, LuckPerms, AdvancedBan |

### Important
- The screenshot includes **two LuckPerms jars**, **two WorldGuard jars**, **two TAB jars** and **two PlayTime jars**. The migration script refuses to guess; it reports duplicates instead of installing multiple variants on the same server.
- Some plugins have overlapping features: **DeluxeHub vs SuperLobby/SetLobby**, **ZNPCs vs other NPC options**, **MySkinRestorer vs SkinsRestorer**. The script leaves the overlapping ones for review.
- The screenshot's BedWars jar is **BedWars-0.2.37.jar** and SkyWars jar is **SkyWarsReloaded 5.6.34.jar**. They must be checked against the current Paper backend version (1.21.4) before you trust them; merely copying them will not create a playable arena. For deliberate testing, `--include-game-plugins` allows a local copy, but runtime compatibility remains unconfirmed.
- Geyser and Floodgate placement/forwarding requires a network architecture decision; don't blindly install their Spigot builds behind Velocity.
- Do not place every plugin into every backend. Many plugins are server-role-specific.
- **Avoid enabling conflicting lobby spawn/compass commands simultaneously** from DeluxeHub, SuperLobby, SetLobby and ShababCore; configure the chosen primary lobby plugin.
- The `I_luca1` offline-name OP settings are **local development only**. Never expose a proxy that gives OP to an unauthenticated username.

## How to finish the migration
Open Codespaces in a terminal-capable agent, have it run the script in preview mode, inspect the duplicate / missing / compatibility warnings, then let it apply the safe subset. Install/configure game plugins one by one, start in an isolated test and inspect Paper logs for each plugin. As of this commit, **no jar has been moved into Codespaces by ChatGPT**.
