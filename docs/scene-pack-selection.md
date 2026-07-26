# Scene-pack selection record

Status: frozen 48-scene CC0 pack.

On 2026-07-11, the first full-resolution crop review replaced a contemporary
checkpoint photograph because of identifiable people and removed two
Unsplash-labelled Commons uploads because upstream relicensing provenance was
not sufficiently strong. The replacement-safe originals and their canonical,
delivery, and thumbnail derivatives were subsequently reviewed, approved,
hashed, and frozen in `scene-pack/v1/manifest.json`.

## Selection result

The shortlist contains six sources in each protocol family: urban, workshop,
library, garden, kitchen, harbor, museum, and market. Scene IDs 2001 through
2048 are reserved in family order. The machine-readable record is
`protocol/manifests/selected-sources.json`.

The review began with 80 Met Open Access candidates and 128 Wikimedia Commons
candidates. Targeted searches added 20 garden and 20 harbor candidates. The Met
was retained where historical harbor scenes were stronger; Commons was favored
for literal modern interiors and outdoor environments.

## Admission decisions

Every selected record currently declares CC0 at its individual institutional or
file page. The Met records also carry `isPublicDomain: true`; Commons records
carry their per-file `LicenseShortName`, source page, original dimensions, image
URL, and source SHA-1. Aggregator search labels were not accepted as evidence.

The visual pass rejected portraits, isolated objects, diagrams, maps, aerial
views, weak family matches, large blank regions, obvious duplicates, and scenes
with insufficient landmark distribution. Near-duplicate library, garden, and
harbor results were deliberately reduced. Historical artwork was used where it
avoids privacy concerns and provides stronger spatial structure than available
photographs.

## Freeze result

Every source record preserves its source URL, license evidence, dimensions, and
source digest. Each canonical 1920 by 1280 derivative, delivery image, and
thumbnail has an independently verified SHA-256 digest in the frozen manifest.
The full-resolution privacy, text, brand, plate, cultural-property, low-vision,
and hotspot-risk reviews are recorded in the pack metadata.

All 384 canonical cells are selectable during enrollment. Recommended-cell
metadata remains available only for offline analysis: every scene contains 12
to 15 recommendations in each fixed six-by-four analysis window, but neither
the participant interface nor authentication reveals those recommendations.
The frozen pack passes `scripts/validate_scene_pack.py`.

## Reproduction

The reviewed discovery inputs are under `protocol/manifests/`. The shortlist can
be rebuilt deterministically with:

```bash
python3 scripts/build_selected_sources.py
```

Candidate discovery scripts are retained for provenance and future replacement
searches. The local `scene-pack/review/` cache is deliberately ignored. Running
discovery again can change search ordering, so generated output must never
silently replace the frozen manifest.

## Operator review application

`scripts/review_scene_pack.py` serves a loopback-only review application on port
8091. It displays the canonical crop beneath the 24 by 16 grid, permits cell
toggles, calculates all sixteen window counts live, and stores crop and cell
approval separately. Pack approval requires exactly 48 scenes with both reviews.

The Spring backend does not trust that UI alone. At startup it verifies the
configured manifest digest, approval status, unique scene IDs, 48 explicit CC0
records, eight balanced families, every eligible and recommended cell, all
analysis windows, and the canonical, delivery, and thumbnail asset hashes. A
configured formal pack fails closed if any validation differs, and the resulting
state is exposed through `/api/pack/status`.
