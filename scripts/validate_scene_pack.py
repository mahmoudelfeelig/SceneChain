#!/usr/bin/env python3
"""Validate frozen SceneChain manifests without modifying source assets."""

from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path


def fail(message: str) -> None:
    raise ValueError(message)


def validate(path: Path) -> None:
    document = json.loads(path.read_text(encoding="utf-8"))
    scenes = document.get("scenes")
    if not isinstance(scenes, list) or len(scenes) != 48:
        fail("formal pack must contain exactly 48 scenes")
    ids: set[int] = set()
    families: dict[str, int] = {}
    for scene in scenes:
        scene_id = scene["sceneId"]
        if scene_id in ids:
            fail(f"duplicate scene id {scene_id}")
        ids.add(scene_id)
        families[scene["family"]] = families.get(scene["family"], 0) + 1
        if scene["source"]["license"] != "CC0-1.0":
            fail(f"scene {scene_id} is not explicitly CC0")
        if not scene["source"].get("licenseEvidence"):
            fail(f"scene {scene_id} has no saved license evidence")
        cells = scene["eligibleCells"]
        if cells != list(range(384)):
            fail(f"scene {scene_id} has invalid eligible-cell count")
        recommended = scene["recommendedCells"]
        if len(recommended) != len(set(recommended)) or not 192 <= len(recommended) <= 240:
            fail(f"scene {scene_id} has invalid recommended-cell count")
        windows = scene["enrollmentWindows"]
        expected = {(c, r) for r in (0, 4, 8, 12) for c in (0, 6, 12, 18)}
        actual = {(item["column"], item["row"]) for item in windows}
        if actual != expected:
            fail(f"scene {scene_id} does not contain the fixed 16-window partition")
        for column, row in expected:
            count = sum(1 for cell in recommended if column <= cell % 24 < column + 6 and row <= cell // 24 < row + 4)
            if not 12 <= count <= 15:
                fail(f"scene {scene_id} window {column},{row} exposes {count} cells")
        for kind in ("canonical", "delivery", "thumbnail"):
            asset = (path.parent / scene[kind]["path"]).resolve()
            if not asset.is_file():
                fail(f"scene {scene_id} {kind} asset is missing")
            digest = hashlib.sha256(asset.read_bytes()).hexdigest()
            if digest != scene[kind]["sha256"]:
                fail(f"scene {scene_id} {kind} hash does not match")
    if len(families) != 8 or set(families.values()) != {6}:
        fail(f"expected eight families with six scenes each, got {families}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("usage: validate_scene_pack.py MANIFEST.json", file=sys.stderr)
        raise SystemExit(2)
    try:
        validate(Path(sys.argv[1]))
    except (OSError, KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        print(f"scene-pack validation failed: {error}", file=sys.stderr)
        raise SystemExit(1)
    print("scene-pack validation passed")
