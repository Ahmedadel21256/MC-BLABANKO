#!/usr/bin/env python3
"""Download selected stable PaperMC jars using the official Fill v3 API."""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import shutil
import sys
import tempfile
from urllib.request import Request, urlopen

BASE = Path(sys.argv[1]).resolve()
API = "https://fill.papermc.io/v3"
HEADERS = {
    "User-Agent": "SHABAB-SMP-setup/1.0 (https://github.com/Ahmedadel21256/MC-BLABANKO)",
    "Accept": "application/json",
}


def get_json(url: str):
    with urlopen(Request(url, headers=HEADERS), timeout=45) as response:
        return json.load(response)


def stable_build(project: str, version: str):
    builds = get_json(f"{API}/projects/{project}/versions/{version}/builds")
    if not isinstance(builds, list):
        raise RuntimeError(f"Unexpected PaperMC API response for {project} {version}")
    stable = [build for build in builds if build.get("channel") == "STABLE"]
    if not stable:
        raise RuntimeError(f"No STABLE build available for {project} {version}")
    build = stable[0]
    item = build.get("downloads", {}).get("server:default")
    if not item or not item.get("url") or not item.get("checksums", {}).get("sha256"):
        raise RuntimeError(f"Missing download or checksum for {project} {version}")
    return item


def download_verified(source, target: Path):
    expected = source["checksums"]["sha256"].lower()
    if target.exists():
        current = hashlib.file_digest(target.open("rb"), "sha256").hexdigest()
        if current == expected:
            print(f"Verified existing {target}")
            return
        # Do not overwrite a jar that an existing instance might be using.
        raise RuntimeError(f"{target} exists but does not match the selected build; review before replacing.")
    target.parent.mkdir(parents=True, exist_ok=True)
    fd, temporary = tempfile.mkstemp(prefix=".download-", suffix=".jar", dir=target.parent)
    try:
        digest = hashlib.sha256()
        with os.fdopen(fd, "wb") as output:
            with urlopen(Request(source["url"], headers=HEADERS), timeout=180) as response:
                while chunk := response.read(1024 * 1024):
                    output.write(chunk)
                    digest.update(chunk)
        if digest.hexdigest() != expected:
            raise RuntimeError(f"SHA-256 mismatch downloading {target}")
        os.replace(temporary, target)
        print(f"Downloaded and verified {target}")
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


paper = stable_build("paper", "1.21.4")
velocity = stable_build("velocity", "4.3.1")
for name in ("lobby", "survival", "bedwars", "skywars"):
    download_verified(paper, BASE / name / "paper.jar")
download_verified(velocity, BASE / "proxy" / "velocity.jar")
print("Official server binaries downloaded and checksum-verified.")
