#!/usr/bin/env python3
"""Collect individually verified CC0 scene candidates from Wikimedia Commons."""

from __future__ import annotations

import argparse
import json
import subprocess
import urllib.parse
from pathlib import Path

API = "https://commons.wikimedia.org/w/api.php"
QUERIES = {
    "urban": ["street", "plaza", "courtyard architecture"],
    "workshop": ["workshop interior", "tool bench", "makerspace"],
    "library": ["library interior", "reading room", "bookshelves interior"],
    "garden": ["garden landscape", "greenhouse interior", "botanical garden"],
    "kitchen": ["kitchen interior", "cafe interior", "restaurant interior"],
    "harbor": ["harbor", "port", "train station interior"],
    "museum": ["museum interior", "gallery interior", "exhibition room"],
    "market": ["market interior", "market hall", "shop interior"],
}


def get_json(parameters: dict[str, str | int]) -> dict:
    url = API + "?" + urllib.parse.urlencode(parameters)
    result = subprocess.run(
        ["curl", "-fsSL", "-A", "SceneChain-Curation/0.1", "--retry", "3", "--max-time", "60", url],
        check=True, capture_output=True,
    )
    return json.loads(result.stdout)


def text_value(metadata: dict, key: str) -> str:
    return str(metadata.get(key, {}).get("value", ""))


def discover(limit: int, only_family: str | None, extra_queries: list[str]) -> dict[str, list[dict]]:
    inventory: dict[str, list[dict]] = {}
    globally_seen: set[int] = set()
    for family, terms in QUERIES.items():
        if only_family and family != only_family:
            continue
        if extra_queries:
            terms = extra_queries
        rows: list[dict] = []
        for term in terms:
            response = get_json({
                "action": "query", "format": "json", "formatversion": 2,
                "generator": "search", "gsrnamespace": 6, "gsrlimit": 50,
                "gsrsearch": f"{term} incategory:CC-Zero",
                "prop": "imageinfo", "iiprop": "url|size|sha1|extmetadata",
                "iiurlwidth": 640,
            })
            for page in response.get("query", {}).get("pages", []):
                if len(rows) >= limit or page["pageid"] in globally_seen:
                    continue
                info = (page.get("imageinfo") or [{}])[0]
                metadata = info.get("extmetadata", {})
                license_name = text_value(metadata, "LicenseShortName").lower()
                if "cc0" not in license_name and "cc zero" not in license_name:
                    continue
                width, height = info.get("width", 0), info.get("height", 0)
                if width < 2400 or height < 1600:
                    continue
                title = page["title"].removeprefix("File:")
                description = text_value(metadata, "ImageDescription")
                if any(word in (title + " " + description).lower() for word in
                       ("portrait", "logo", "diagram", "map", "aerial", "drone")):
                    continue
                rows.append({
                    "family": family,
                    "pageId": page["pageid"],
                    "title": title,
                    "creator": text_value(metadata, "Artist"),
                    "description": description,
                    "width": width, "height": height,
                    "sourcePage": info.get("descriptionurl"),
                    "primaryImage": info.get("url"),
                    "thumbnail": info.get("thumburl"),
                    "originalSha1": info.get("sha1"),
                    "license": "CC0-1.0",
                    "licenseEvidence": info.get("descriptionurl"),
                    "licenseShortName": text_value(metadata, "LicenseShortName"),
                    "credit": text_value(metadata, "Credit"),
                })
                globally_seen.add(page["pageid"])
            if len(rows) >= limit:
                break
        inventory[family] = rows
        print(f"{family}: {len(rows)} candidates")
    return inventory


def download(inventory: dict[str, list[dict]], root: Path) -> None:
    for family, rows in inventory.items():
        directory = root / family
        directory.mkdir(parents=True, exist_ok=True)
        for index, row in enumerate(rows, 1):
            target = directory / f"{index:02d}-{row['pageId']}.jpg"
            subprocess.run([
                "curl", "-fsSL", "-A", "SceneChain-Curation/0.1", "--retry", "3",
                "--max-time", "90", "-o", str(target), row["thumbnail"],
            ], check=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, default=16)
    parser.add_argument("--only-family", choices=sorted(QUERIES))
    parser.add_argument("--query", action="append", default=[])
    parser.add_argument("--output", type=Path, default=Path("protocol/manifests/commons-candidates.json"))
    parser.add_argument("--thumbnails", type=Path, default=Path("scene-pack/review/commons-thumbnails"))
    args = parser.parse_args()
    inventory = discover(args.limit, args.only_family, args.query)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps({"source": "Wikimedia Commons", "families": inventory}, indent=2), encoding="utf-8")
    download(inventory, args.thumbnails)


if __name__ == "__main__":
    main()
