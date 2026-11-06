#!/usr/bin/env python3
"""Copy existing local Minecraft plugin jars to a fresh SHABAB SMP network.

No downloads, overwrites, deletions, old config migration, or server restarts.
Default = preview. --apply only copies unambiguous local jars.
Incompatibility warnings are not an indication of successful plugin loading.
"""
from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
NETWORK = ROOT / "shabab-network"
DEFAULT_SOURCE = ROOT / "minecraft-server" / "plugins"

# Prefixes below correspond to filenames visible in the user's existing
# plugins directory. Only choose filenames, never the config directories.
# Each destination is a Paper backend, NOT the Velocity proxy.
PLUGIN_SPECS: dict[str, tuple[tuple[str, ...], tuple[str, ...]]] = {
    "AdvancedBan": (("AdvancedBan-",), ("lobby", "survival", "bedwars", "skywars")),
    "AdvancedRegionMarket": (("advancedregionmarket-",), ("survival",)),
    "AFKPlus": (("AFKPlus-",), ("survival",)),
    "ArmShopBridge": (("armshopbridge",), ("survival",)),
    "BedWars": (("BedWars-",), ("bedwars",)),
    "ClearLag": (("Clearlag",), ("survival",)),
    "DeluxeHub": (("DeluxeHub-",), ("lobby",)),
    "DiscordSRV": (("DiscordSRV-Build-",), ("lobby",)),
    "EssentialsX": (("EssentialsX-",), ("survival",)),
    "Floodgate": (("floodgate-spigot",), ("lobby",)), # review proxy architecture before enabling
    "Geyser": (("Geyser-Spigot",), ("lobby",)), # review proxy architecture before enabling
    "JoinCore": (("JoinCore",), ("lobby",)),
    "LuckPerms": (("LuckPerms-Bukkit-", "LuckPerms-bukkit-"), ("lobby", "survival", "bedwars", "skywars")),
    "Multiverse-Core": (("multiverse-core-",), ("survival",)),
    "MySkinRestorer": (("MySkinRestorer-",), ("lobby",)),
    "PlaceholderAPI": (("PlaceholderAPI-",), ("lobby", "survival")),
    "PlayTime": (("PlayTime-", "playtime-"), ("survival",)),
    "SBA": (("SBA-",), ("survival",)), # plugin functionality not yet verified
    "SetLobby": (("SetLobby_",), ("lobby",)),
    "SkinsRestorer": (("SkinsRestorer.jar",), ("lobby",)),
    "SkyWarsReloaded": (("SkyWarsReloaded ",), ("skywars",)),
    "SuperLobby": (("SuperLobby.jar",), ("lobby",)),
    "TAB": (("TAB v", "TAB v6", "TAB-"), ("lobby",)),
    "Vault": (("Vault.jar",), ("survival",)),
    "voicechat": (("voicechat-bukkit-",), ("survival",)),
    "WorldEdit": (("worldedit-bukkit-",), ("survival",)),
    "WorldGuard": (("worldguard-bukkit-",), ("survival",)),
    "WorldGuardExtraFlags": (("worldguard-extra-flags-plus-",), ("survival",)),
    "ZNPCs": (("znpcs-",), ("lobby",)),
}

# These plugins can overlap or need network-specific configuration.
# They must be manually reviewed rather than copied without thought.
REVIEW_ONLY = {
    "Floodgate", "Geyser", "JoinCore", "MySkinRestorer",
    "SetLobby", "SuperLobby", "SBA", "SkinsRestorer",
    "WorldGuardExtraFlags", "ArmShopBridge",
}
# The old game plugins may be incompatible with 1.21.4.
GAME_REVIEW = {"BedWars", "SkyWarsReloaded"}

def checksum(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        while chunk := f.read(1024 * 1024):
            digest.update(chunk)
    return digest.hexdigest()

def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE,
                        help="Existing Paper plugins directory (defaults to minecraft-server/plugins)")
    parser.add_argument("--apply", action="store_true", help="Copy non-conflicting jars")
    parser.add_argument("--include-game-plugins", action="store_true",
                        help="Include BedWars/SkyWars jars after compatibility review")
    args = parser.parse_args()
    source = args.source.resolve()
    if not source.is_dir():
        print(f"ERROR: source plugins folder not found: {source}", file=sys.stderr)
        return 2
    if source == NETWORK.resolve() or NETWORK.resolve() in source.parents:
        print("ERROR: source must be outside new network", file=sys.stderr)
        return 2
    available = sorted((p for p in source.iterdir() if p.is_file() and p.suffix.lower() == ".jar"),
                       key=lambda p: p.name.lower())
    found = 0
    missing = []
    conflicts = []
    reviewed = []
    copied = []
    print("MODE:", "COPY (no overwrites)" if args.apply else "DRY RUN")
    print("SOURCE:", source)
    for label, (prefixes, targets) in PLUGIN_SPECS.items():
        matches = [p for p in available
                   if any(p.name.casefold().startswith(prefix.casefold()) for prefix in prefixes)]
        if not matches:
            missing.append(label)
            print(f"SKIP missing: {label}")
            continue
        if len(matches) > 1:
            conflicts.append(label)
            print(f"REVIEW duplicate {label}: {', '.join(p.name for p in matches)}")
            continue
        candidate = matches[0]
        if label in REVIEW_ONLY or (label in GAME_REVIEW and not args.include_game_plugins):
            reviewed.append(label)
            print(f"REVIEW before install {label}: {candidate.name}")
            continue
        found += 1
        for target in targets:
            destination = NETWORK / target / "plugins" / candidate.name
            # Never follow symlink targets or overwrite files.
            if destination.exists() or destination.is_symlink():
                if destination.is_file() and checksum(candidate) == checksum(destination):
                    print(f"ALREADY installed: {target}/{candidate.name}")
                else:
                    conflicts.append(f"{label}@{target}")
                    print(f"REVIEW existing destination: {destination}")
                continue
            print(f"{'COPY' if args.apply else 'WOULD COPY'}: {candidate.name} -> {target}")
            if args.apply:
                destination.parent.mkdir(parents=True, exist_ok=True)
                with candidate.open("rb") as input_file, destination.open("xb") as output:
                    shutil.copyfileobj(input_file, output)
                copied.append(str(destination.relative_to(ROOT)))
    print(f"Selected plugin types: {found}; copies made: {len(copied)}")
    print("Missing:", ", ".join(missing) if missing else "none")
    print("Duplicate/conflict review:", ", ".join(conflicts) if conflicts else "none")
    print("Compatibility review required:", ", ".join(reviewed) if reviewed else "none")
    print("No server was started. Successful copying does not prove plugin compatibility.")
    if not args.apply:
        print("This was only a preview. Use --apply only after checking the plan.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
