#!/usr/bin/env python3
"""Fetch current official Paper 1.21.4 and Velocity builds using PaperMC downloads API."""
import json
import pathlib
import sys
import urllib.request

base = pathlib.Path(sys.argv[1])
headers = {"User-Agent": "SHABAB-SMP-setup/1.0", "Accept": "application/json"}

def get_json(url):
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.load(resp)

def download(url, destination):
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=120) as response, destination.open("wb") as out:
        import shutil
        shutil.copyfileobj(response, out)

def latest_build(project, version):
    builds = get_json(f"https://api.papermc.io/v2/projects/{project}/versions/{version}/builds")["builds"]
    stable = [b for b in builds if b.get("channel") == "default"]
    build = (stable or builds)[-1]
    name = build["downloads"]["application"]["name"]
    return f"https://api.papermc.io/v2/projects/{project}/versions/{version}/builds/{build['build']}/downloads/{name}"

paper = latest_build("paper", "1.21.4")
for server in ("lobby", "survival", "bedwars", "skywars"):
    target = base / server / "paper.jar"
    if not target.exists():
        print("Downloading Paper for", server)
        download(paper, target)

velocity_versions = get_json("https://api.papermc.io/v2/projects/velocity")["versions"]
velocity = latest_build("velocity", velocity_versions[-1])
target = base / "proxy" / "velocity.jar"
if not target.exists():
    print("Downloading Velocity")
    download(velocity, target)
print("Downloads complete. Configure proxy forwarding and authentication before starting.")
