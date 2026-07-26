# SceneChain design QA

**Source visual truth**

- `/mnt/c/Users/mahmo/.codex/generated_images/019f9ee2-b2d4-7d42-ae99-19db73f48f3a/call_olYwaxwV8GkE24l28MCrPvoD.png`
- Source pixels: 1487 × 1058.
- Selected state: public landing page, desktop.

**Rendered implementation**

- `docs/ui-redesign-2026-07-26/release/landing-desktop.png`
- Implementation pixels: 1440 × 1172.
- CSS viewport: 1440 × 1000 at device scale factor 1; full-page capture.
- Responsive evidence: `landing-laptop.png` at 1024 × 768 and `landing-phone-info-only.png` at 390 × 844.
- Flow evidence: `practice-keyboard-grid.png` and `privacy-desktop.png`.
- Density normalization: source and implementation are both CSS-density raster captures. The full-view comparison scales both to 720 pixels wide and pads the shorter image without cropping.
- State: recruitment closed, public scene pack preview, first practice stage.

**Full-view comparison evidence**

- `docs/ui-redesign-2026-07-26/release/reference-comparison.png`
- The implementation preserves the source hierarchy: three-part masthead, orange research warning, two-line black headline, cobalt rule and CTA, five-step photographic journey, and four-module route directory.
- Intentional product constraints: the release-gated Join study and Enrollment controls remain disabled while recruitment is closed, and the footer states service availability. The implementation uses the repository's approved CC0 photographs instead of the generated reference photographs.

**Focused-region evidence**

- `docs/ui-redesign-2026-07-26/release/practice-keyboard-grid.png` confirms the photographic 24 × 16 interaction surface, visible keyboard focus, direction controls, and final-only-verification messaging.
- `docs/ui-redesign-2026-07-26/release/privacy-desktop.png` confirms consistent masthead, typography, panels, and legal-content rhythm.
- `docs/ui-redesign-2026-07-26/release/landing-phone-info-only.png` confirms the hero, journey, and route modules reflow without horizontal page overflow.

**Required fidelity surfaces**

- Fonts and typography: self-host-safe system sans stack, strong display weight, compact headline tracking, readable body line height, and consistent UI weights. The final desktop lockup matches the source's two-line headline.
- Spacing and layout rhythm: consistent header, warning, hero, journey, and directory spacing; compact radii and restrained elevation; no page-level overflow at tested breakpoints.
- Colors and visual tokens: coherent near-white, black, cobalt, muted slate, and safety-orange token set with accessible interaction contrast.
- Image quality and asset fidelity: five sharp 1920 × 1280 CC0 scene photographs are served locally and cropped consistently. No placeholder, CSS-drawn, or hotlinked imagery is used.
- Copy and content: research-only scope, no-password-reuse warning, five-stage chain, final-only verification, participant rights, and recruitment state remain explicit.

**Findings**

- No actionable P0, P1, or P2 findings remain.

**Comparison history**

- Initial P2: the desktop title wrapped into four lines and the study label appeared above it, weakening fidelity to the selected source.
- Fix: moved the study label below the cobalt rule, rebalanced hero columns, and reduced the display scale.
- Intermediate P2: the first phrase still wrapped at 1440 pixels.
- Fix: tuned the display clamp to preserve the selected two-line lockup.
- Post-fix evidence: `docs/ui-redesign-2026-07-26/release/landing-desktop.png`; Playwright smoke passed after a fresh server restart.

**Primary interactions tested**

- Route navigation to `/privacy` and `/practice`.
- Browser Back from privacy to `/`.
- Exit flow from practice to `/`.
- Keyboard arrow movement inside the 24 × 16 scene grid.
- Disabled recruitment controls.
- Desktop, laptop, and phone overflow checks.
- Five real landing-scene images present.
- Browser console contained only expected local API proxy failures because the backend was not running during isolated frontend QA; no frontend runtime exceptions were observed.

**Follow-up polish**

- None required for handoff.

final result: passed
