# Scene-pack and cell policy

Status: frozen scene-pack policy.

## Pack composition

The formal pack contains 48 immutable scenes across eight families, with six
approved scenes per family:

- urban streets and plazas;
- workshops and tool benches;
- libraries and study rooms;
- gardens and greenhouses;
- kitchens and cafés;
- harbors and transport areas;
- museums and display rooms;
- markets and organized interiors.

An account receives five scenes from five distinct families. Scene IDs and order
are stable cues and are not counted as credential entropy.

## Sources and licensing

Only assets whose individual source record explicitly carries CC0 are eligible.
Preferred sources are Smithsonian Open Access, CC0-designated Wikimedia Commons
files, and The Met Open Access collection. An aggregator's license filter is not
sufficient evidence; the original institutional or file record is checked.

CC0 addresses copyright but may not remove privacy, publicity, trademark, moral-
rights, or cultural-property concerns. Scenes containing identifiable people,
license plates, addresses, prominent brands, or sensitive cultural objects are
excluded even when the digital image is labeled CC0.

Every source record contains:

```text
source URL and institution
source title and creator when supplied
CC0 declaration URL or captured metadata
download timestamp
original dimensions and media type
original SHA-256
```

The project keeps a local copy and does not hotlink authentication assets.

## Canonical derivative

Source images must be at least 2400 by 1600 pixels and support a human-reviewed
3:2 composition. A single canonical 1920 by 1280 derivative is created before
the scene is admitted. Any crop, exposure adjustment, or redaction occurs once
during curation and is recorded. The canonical pixels never change afterward.

AVIF, WebP, and JPEG delivery derivatives are generated from that canonical
image. The manifest stores hashes for the canonical and every delivery asset.
A visual change produces a new scene version and cannot replace an issued one.

## Scene acceptance

A reviewer scores landmark density, spatial distribution, distinctiveness,
visual quality, cultural familiarity, low-vision usability, and hotspot risk.
Scenes are rejected when they contain large blank areas, one dominant landmark,
repeating indistinguishable objects, text-dependent landmarks, color-only
distinctions, unstable fine detail, or important content near an unsafe crop.

Automated analysis may propose face/text masks, edge density, local contrast,
perceptual duplicates, and low-detail regions. Human review is authoritative.
AI-generated scenes are excluded from the formal pack.

## Grid and selectable cells

Every canonical scene uses a 24-column by 16-row grid and all 384 cells are
selectable. Human review records a frozen recommended-cell set for curation QA
and later descriptive comparison, but the formal interface never highlights,
suggests, ranks, or restricts cells. Runtime saliency or computer vision does not
change enrollment.

## Internal curation partitions

The grid remains partitioned into 16 fixed six-by-four regions solely so curators
can check that recommended metadata is spatially distributed. These partitions
are never presented as enrollment windows and do not affect participant choice.

## Manifest

```json
{
  "sceneId": 1003,
  "version": 1,
  "family": "workshop",
  "difficulty": "medium",
  "canonical": {
    "width": 1920,
    "height": 1280,
    "sha256": "..."
  },
  "source": {
    "institution": "...",
    "url": "...",
    "license": "CC0-1.0",
    "licenseEvidence": "...",
    "downloadedAt": "2026-07-10T00:00:00Z"
  },
  "grid": { "columns": 24, "rows": 16 },
  "eligibleCells": [0, 1, 2, "...", 383],
  "enrollmentWindows": [{ "column": 0, "row": 0 }],
  "derivatives": [{ "format": "avif", "sha256": "..." }]
}
```

The actual schema is machine-readable and validated in CI. Cell lists above are
illustrative only.

## Assignment

Assignment is uniform across approved scenes subject to distinct-family and
difficulty-balancing constraints. It must not use participant preferences,
demographics, device characteristics, or previous participant choices. Formal
study assignment is reproducible from a stored randomization record that contains
scene IDs but no credential cells or actions.

## Pilot and freeze

A separate pilot evaluates selection heatmaps, cell misses, scene recall,
and cultural ambiguity. Scene rejection thresholds and the pilot sample-size rule
are written before the pilot analysis. Pilot participants and credentials are not
included in the formal confirmatory dataset.

The formal pack is frozen by manifest digest. A scene, selectable-cell policy, or
delivery-asset change increments the internal compatibility version and requires
affected credentials to be re-enrolled.

## Source guidance

- [Smithsonian Open Access FAQ](https://www.si.edu/openaccess/faq)
- [Wikimedia Commons reuse guidance](https://commons.wikimedia.org/wiki/Commons%3AReusing_content_outside_Wikimedia/en)
- [The Met Open Access policy](https://www.metmuseum.org/policies/image-resources)
