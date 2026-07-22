#!/usr/bin/env python3
"""Build a reviewable CC0 candidate inventory from The Met Open Access API."""

from __future__ import annotations

import argparse
import json
import subprocess
import time
import urllib.parse
from pathlib import Path

API = "https://collectionapi.metmuseum.org/public/collection/v1"
FAMILIES = {
    "urban": ["city street painting", "town square painting", "street scene"],
    "workshop": ["workshop interior", "artist studio interior", "blacksmith workshop"],
    "library": ["library interior", "scholar study interior", "reading room"],
    "garden": ["garden landscape", "flower garden painting", "greenhouse interior"],
    "kitchen": ["kitchen interior painting", "cafe interior", "dining room interior"],
    "harbor": ["harbor painting", "port scene", "railway station painting"],
    "museum": ["gallery interior painting", "museum interior", "collection room interior"],
    "market": ["market scene painting", "bazaar painting", "shop interior painting"],
}
SCENE_MEDIA = ("painting", "drawing", "photograph", "print", "watercolor", "pastel")
REJECT_WORDS = ("portrait", "head", "bust", "fragment", "vase", "plate", "bowl", "coin", "medal")


def get_json(url: str) -> dict:
    last_error: Exception | None = None
    for delay in (0, 2, 5, 10, 20):
        if delay:
            time.sleep(delay)
        try:
            result = subprocess.run(
                ["curl", "-fsSL", "-A", "Mozilla/5.0 SceneChain-Curation", "--max-time", "45", url],
                check=True, capture_output=True,
            )
            return json.loads(result.stdout)
        except (subprocess.CalledProcessError, json.JSONDecodeError) as error:
            last_error = error
    raise RuntimeError(f"Met API unavailable after backoff: {url}") from last_error


def discover(limit: int, checkpoint: Path) -> dict[str, list[dict]]:
    selected: dict[str, list[dict]] = {}
    seen_global: set[int] = set()
    for family, queries in FAMILIES.items():
        family_rows: list[dict] = []
        seen_family: set[int] = set()
        for query in queries:
            try:
                search = get_json(f"{API}/search?hasImages=true&q={urllib.parse.quote(query)}")
            except RuntimeError as error:
                print(f"search skipped after backoff: {error}")
                continue
            for object_id in (search.get("objectIDs") or [])[:120]:
                if len(family_rows) >= limit or object_id in seen_family or object_id in seen_global:
                    continue
                seen_family.add(object_id)
                try:
                    item = get_json(f"{API}/objects/{object_id}")
                except Exception as error:
                    print(f"skip {object_id}: {error}")
                    continue
                description = " ".join(str(item.get(key, "")) for key in
                    ("title", "objectName", "classification", "medium")).lower()
                if not item.get("isPublicDomain") or not item.get("primaryImage"):
                    continue
                if not any(word in description for word in SCENE_MEDIA):
                    continue
                if any(word in str(item.get("title", "")).lower() for word in REJECT_WORDS):
                    continue
                family_rows.append({
                    "family": family,
                    "objectId": object_id,
                    "title": item.get("title"),
                    "creator": item.get("artistDisplayName") or "Unknown",
                    "date": item.get("objectDate"),
                    "medium": item.get("medium"),
                    "department": item.get("department"),
                    "objectUrl": item.get("objectURL"),
                    "primaryImage": item.get("primaryImage"),
                    "thumbnail": item.get("primaryImageSmall") or item.get("primaryImage"),
                    "isPublicDomain": True,
                    "license": "CC0-1.0",
                    "licenseEvidence": "https://www.metmuseum.org/policies/image-resources",
                })
                seen_global.add(object_id)
                time.sleep(0.35)
            if len(family_rows) >= limit:
                break
        selected[family] = family_rows
        print(f"{family}: {len(family_rows)} candidates")
        checkpoint.parent.mkdir(parents=True, exist_ok=True)
        checkpoint.write_text(json.dumps({"source": "The Metropolitan Museum of Art", "families": selected}, indent=2), encoding="utf-8")
    return selected


def download_thumbnails(inventory: dict[str, list[dict]], output: Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    for family, rows in inventory.items():
        directory = output / family
        directory.mkdir(exist_ok=True)
        for index, row in enumerate(rows, 1):
            target = directory / f"{index:02d}-{row['objectId']}.jpg"
            try:
                subprocess.run(
                    ["curl", "-fsSL", "-A", "Mozilla/5.0 SceneChain-Curation", "--retry", "3", "--max-time", "60", "-o", str(target), row["thumbnail"]],
                    check=True,
                )
            except Exception as error:
                print(f"thumbnail {row['objectId']} failed: {error}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, default=12)
    parser.add_argument("--output", type=Path, default=Path("protocol/manifests/met-candidates.json"))
    parser.add_argument("--thumbnails", type=Path, default=Path("scene-pack/review/met-thumbnails"))
    args = parser.parse_args()
    inventory = discover(args.limit, args.output)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps({"source": "The Metropolitan Museum of Art", "families": inventory}, indent=2), encoding="utf-8")
    download_thumbnails(inventory, args.thumbnails)


if __name__ == "__main__":
    main()
