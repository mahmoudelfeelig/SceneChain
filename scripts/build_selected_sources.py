#!/usr/bin/env python3
"""Materialize the human-reviewed 48-source shortlist from candidate inventories."""

from __future__ import annotations

import hashlib
import json
from datetime import UTC, datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SELECTIONS = {
    "urban": [("commons", index) for index in (6, 9, 11, 12, 13, 16)],
    "workshop": [("commons", index) for index in (1, 2, 12, 14, 15, 16)],
    "library": [("commons", index) for index in (1, 2, 3, 4, 6, 9)],
    "garden": [("commons-garden", index) for index in (1, 2, 4, 5, 13, 19)],
    "kitchen": [("commons", index) for index in (1, 4, 5, 9, 15, 16)],
    "harbor": [("met", index) for index in (1, 2, 3, 4)] + [("commons-harbor", index) for index in (1, 2)],
    "museum": [("commons", index) for index in (1, 2, 3, 4, 6, 8)],
    "market": [("commons", index) for index in (1, 2, 7, 8, 9, 10)],
}
FILES = {
    "commons": "protocol/manifests/commons-candidates.json",
    "commons-garden": "protocol/manifests/commons-garden-candidates.json",
    "commons-harbor": "protocol/manifests/commons-harbor-candidates.json",
    "met": "protocol/manifests/met-candidates.json",
}


def main() -> None:
    inventories = {name: json.loads((ROOT / path).read_text(encoding="utf-8"))["families"] for name, path in FILES.items()}
    scenes: list[dict] = []
    scene_id = 2001
    for family, choices in SELECTIONS.items():
        for source, one_based_index in choices:
            row = dict(inventories[source][family][one_based_index - 1])
            row["sceneId"] = scene_id
            row["sceneVersion"] = 1
            row["family"] = family
            row["inventory"] = FILES[source]
            row["inventoryIndex"] = one_based_index
            row["reviewStatus"] = "selected-pending-original-and-crop-validation"
            scenes.append(row)
            scene_id += 1
    document = {
        "packVersion": 1,
        "status": "shortlist-not-frozen",
        "generatedAt": datetime.now(UTC).isoformat(),
        "selectionPolicy": "Six visually reviewed, non-duplicate CC0 sources in each of eight scene families.",
        "licensePolicy": "Each source must retain an individual CC0 record; final admission requires original hash, 2400x1600 minimum, crop review, cell review, and pilot.",
        "scenes": scenes,
    }
    encoded = json.dumps(document, indent=2, ensure_ascii=False) + "\n"
    target = ROOT / "protocol/manifests/selected-sources.json"
    target.write_text(encoded, encoding="utf-8")
    print(f"wrote {len(scenes)} selected sources to {target}")
    print(f"shortlist sha256 {hashlib.sha256(encoded.encode()).hexdigest()}")


if __name__ == "__main__":
    main()
