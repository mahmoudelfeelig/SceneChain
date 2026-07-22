#!/usr/bin/env python3
"""Generate reproducible detail-ranked candidate cells for human pack review."""

from __future__ import annotations

import json
import math
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "scene-pack/v1"
WIDTH, HEIGHT = 192, 128
CELL_W, CELL_H = 8, 8


def grayscale(path: Path) -> bytes:
    result = subprocess.run([
        "ffmpeg", "-hide_banner", "-loglevel", "error", "-i", str(path),
        "-vf", f"scale={WIDTH}:{HEIGHT},format=gray", "-f", "rawvideo", "-",
    ], check=True, capture_output=True)
    if len(result.stdout) != WIDTH * HEIGHT:
        raise ValueError(f"unexpected grayscale output for {path}")
    return result.stdout


def score_cell(pixels: bytes, cell: int) -> float:
    row, column = divmod(cell, 24)
    x0, y0 = column * CELL_W, row * CELL_H
    values = [pixels[(y0 + y) * WIDTH + x0 + x] for y in range(CELL_H) for x in range(CELL_W)]
    mean = sum(values) / len(values)
    variance = sum((value - mean) ** 2 for value in values) / len(values)
    edges = 0
    for y in range(CELL_H):
        for x in range(CELL_W):
            value = pixels[(y0 + y) * WIDTH + x0 + x]
            if x + 1 < CELL_W:
                edges += abs(value - pixels[(y0 + y) * WIDTH + x0 + x + 1])
            if y + 1 < CELL_H:
                edges += abs(value - pixels[(y0 + y + 1) * WIDTH + x0 + x])
    local_contrast = math.sqrt(variance)
    border_penalty = 12 if row in {0, 15} or column in {0, 23} else 0
    return local_contrast + edges / 112 - border_penalty


def candidate_mask(path: Path) -> list[int]:
    pixels = grayscale(path)
    scores = {cell: score_cell(pixels, cell) for cell in range(384)}
    selected: list[int] = []
    for window_row in range(4):
        for window_column in range(4):
            cells = [
                row * 24 + column
                for row in range(window_row * 4, window_row * 4 + 4)
                for column in range(window_column * 6, window_column * 6 + 6)
            ]
            selected.extend(sorted(cells, key=lambda cell: scores[cell], reverse=True)[:12])
    return sorted(selected)


def main() -> None:
    manifest_path = PACK / "manifest-draft.json"
    review_path = PACK / "review-state.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    review = json.loads(review_path.read_text(encoding="utf-8")) if review_path.exists() else {}
    updated = 0
    for scene in manifest["scenes"]:
        scene_id = str(scene["sceneId"])
        if review.get(scene_id, {}).get("cellsApproved"):
            continue
        scene["recommendedCells"] = candidate_mask(PACK / scene["canonical"]["path"])
        scene["eligibleCells"] = list(range(384))
        review.setdefault(scene_id, {"cropApproved": False, "cellsApproved": False})["cellsApproved"] = False
        updated += 1
    manifest["status"] = "draft-crops-and-generated-masks-require-human-review"
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    review_path.write_text(json.dumps(review, indent=2) + "\n", encoding="utf-8")
    print(f"generated detail-ranked candidate masks for {updated} scenes")


if __name__ == "__main__":
    main()
