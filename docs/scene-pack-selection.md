# Scene-pack selection record

Status: 48-source shortlist selected; originals and formal derivatives are not yet frozen.

On 2026-07-11, the first full-resolution crop review replaced a contemporary
checkpoint photograph because of identifiable people and removed two
Unsplash-labelled Commons uploads because upstream relicensing provenance was
not sufficiently strong. All 48 replacement-safe originals and derivatives are
now staged with individual provenance records, and the structural pack validator
passes. Crop and eligible-cell approvals remain intentionally separate.

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

## Required finalization

Shortlisting is not pack freeze. Each original still needs to be downloaded and
hashed, decoded to verify at least 2400 by 1600 pixels, reviewed at full
resolution, and cropped once to the canonical 1920 by 1280 derivative. The
license page or API metadata response must be saved beside each original.

After cropping, every scene needs a 24 by 16 cell review. Each of the sixteen
six-by-four enrollment windows must retain 12 to 15 eligible cells. Face, text,
brand, plate, privacy, cultural-property, low-vision, and hotspot-risk checks
remain mandatory. The resulting pack must pass `scripts/validate_scene_pack.py`
and a separate participant pilot before the manifest can be signed and frozen.

## Reproduction

The discovery inputs and review thumbnails are under `protocol/manifests/` and
`scene-pack/review/`. The shortlist can be rebuilt deterministically with:

```bash
python3 scripts/build_selected_sources.py
```

Candidate discovery scripts are retained for provenance and future replacement
searches. Running them again can change search ordering, so their output must not
silently replace the reviewed shortlist.

## Operator review application

`scripts/review_scene_pack.py` serves a loopback-only review application on port
8091. It displays the canonical crop beneath the 24 by 16 grid, permits cell
toggles, calculates all sixteen window counts live, and stores crop and cell
approval separately. Pack approval requires exactly 48 scenes with both reviews.

The Spring backend does not trust that UI alone. At startup it loads a formal
pack only when the manifest status is `approved`, all scene IDs are unique, all
48 scenes explicitly use CC0, every family contains six scenes, every eligible
cell is in range, and every window contains 12 to 15 cells. Any failure selects
the development pack and exposes that state through `/api/pack/status`.
