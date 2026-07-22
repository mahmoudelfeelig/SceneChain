# SceneChain pack version 1 staging area

This directory is resumable build output, not a frozen research pack.

Current state on 2026-07-11: all 48 selected originals have passed download,
source-hash or local-hash capture, minimum-dimension validation, canonical
1920x1280 generation, WebP generation, and provenance-record creation. The
draft manifest passes structural scene-pack validation.

Run `python3 -u scripts/finalize_scene_pack.py` from the repository root to
reproduce or verify derivatives. Existing verified files and reviewed masks are
reused. Changed source selections are moved to `rejected/` instead of being
silently overwritten, and their approval state is invalidated.

`manifest-draft.json` deliberately remains invalid as a formal pack until all 48
sources exist. Generated cell masks are evenly distributed placeholders and
must receive human landmark, privacy, accessibility, and hotspot review before
pack freeze.
