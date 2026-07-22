# SceneChain implementation checklist

This file is the source of truth for progress. Checked items are present and
reviewable; unchecked items are not implemented or not yet approved.

## Protocol and architecture

- [x] Complete the pre-implementation protocol review and record its decisions.
- [x] Define password, direct SceneChain, and shielded SceneChain conditions.
- [x] Define a versioned five-stage location-and-action credential.
- [x] Define fixed-point coordinates and exact 24 by 16 cell matching.
- [x] Remove correctness-dependent scene branching and partial-stage evaluation.
- [x] Define stable synthetic cue sequences for unknown accounts.
- [x] Define the eight-marker shielded challenge and its leakage limits.
- [x] Define verifier, encrypted metadata, key separation, replay, and recovery.
- [x] Select React/Vite, Spring Boot, PostgreSQL, Redis, and Docker Compose.
- [x] Write machine-readable protocol and manifest schemas.
- [x] Publish canonicalization and challenge-generation test vectors.

## Scene pack

- [x] Define 48 CC0 scenes across eight families.
- [x] Define source evidence, immutable derivatives, and manifest hashes.
- [x] Define all-cell eligibility and separate non-binding recommended-cell metadata.
- [x] Define assignment, pilot, and pack-freeze requirements.
- [x] Build the CC0 candidate-source inventory with saved license evidence.
- [x] Curate and hash 48 canonical 1920 by 1280 derivatives.
- [x] Review all-cell eligibility and recommended-cell metadata.
- [x] Freeze the approved 48-scene pack and manifest.

## Threat model and security baseline

- [x] Identify assets, trust boundaries, attackers, and residual risks.
- [x] Cover observation, guessing, phishing, enumeration, replay, database/Redis
  compromise, XSS, supply chain, telemetry, research access, and lockout abuse.
- [x] Target OWASP ASVS 5.0 Level 2.
- [x] Define separate authentication, participant-event, and aggregate-hotspot
  data planes.
- [x] Map applicable ASVS requirements to tests and evidence.
- [x] Review the threat model against the implemented architecture.

## Application foundation

- [x] Scaffold the selected frontend, backend, protocol, and deployment structure.
- [x] Add reproducible Docker Compose development services.
- [x] Add formatting, linting, and passing backend/frontend unit-test commands.
- [x] Add integration smoke-test commands and a CI stack test.
- [x] Keep Redis internal, ephemeral, and TTL-bound in the development stack.
- [x] Separate PostgreSQL application and migration roles in the deployment overlay.
- [x] Lock frontend dependencies and clear npm high/critical advisories.
- [x] Add CI build, test, dependency-audit, and container-scan gates.

## Protocol core

- [x] Implement fixed-point coordinate normalization and server quantization.
- [x] Implement fixed-width credential encoding and decoding.
- [x] Implement formal scene-manifest validation tooling.
- [x] Implement uniform development-scene assignment and guided-window selection.
- [x] Implement four-direction actions and keyboard-accessible controls.
- [x] Implement balanced 12 by 8 marker overlays.
- [x] Pass Java and TypeScript protocol test vectors locally.
- [ ] Run shared protocol vectors in CI.

## Enrollment

- [x] Implement five-scene assignment and two-confirmation enrollment.
- [x] Implement unrestricted 384-cell enrollment for the frozen study condition.
- [x] Enforce unsupported viewport rejection without changing credential policy.
- [x] Store verifier and AEAD-protected canonical metadata atomically.
- [x] Increment hotspot aggregates in a separate transaction without participant identifiers.
- [x] Keep hotspot collection write-only with no application read endpoint.
- [x] Add a separate non-recorded practice attempt.

## Authentication

- [x] Implement stable synthetic unknown-account cue sequences.
- [x] Implement cookie-bound, CSRF-protected, expiring attempt creation.
- [x] Return the complete shuffled public scene pool for real and synthetic accounts.
- [x] Implement final-only direct verification.
- [x] Implement final-only shielded verification without per-stage early exit.
- [x] Implement Argon2id, HMAC pepper, AEAD metadata, and key versioning.
- [x] Make Redis sliding-window limits atomic under parallel requests.
- [x] Complete account, network, endpoint, and global-capacity limits.
- [ ] Complete generic-behavior and statistical enumeration tests.
- [x] Implement opaque session creation, expiry, and logout.
- [ ] Add explicit reauthentication for sensitive operations.
- [x] Add password-manager-compatible password authentication.
- [ ] Add passkey authentication and recovery.

## Web and operational security

- [x] Self-host all authentication assets with no third-party runtime code.
- [x] Configure CSP, frame protection, and no-store responses.
- [x] Provide a TLS reverse-proxy example and require secure cookies in the deployment profile.
- [x] Add strict DTO validation, cookie-bound CSRF tokens, Origin checks, and Fetch Metadata rejection.
- [x] Verify application telemetry has a fixed allowlist and no secret-bearing research fields.
- [ ] Verify no secret-bearing payload enters deployment proxy logs, traces, analytics, crash
  reports, proxy logs, browser storage, or URLs.
- [x] Add automated negative tests for cross-site requests, invalid input, replay, consent bypass, and export access.
- [ ] Independently test XSS, clickjacking, parallel attempts, session fixation,
  invalid versions, oversized fields, IDOR, and rate-limit bypass.

## User experience and accessibility

- [x] Design enrollment, direct login, shielded login, failure, and
  alternative-password screens.
- [x] Add a dedicated practice screen.
- [ ] Add dedicated delay and recovery screens.
- [x] Preserve a 768-pixel canonical scene surface in a responsive scroll region.
- [x] Support keyboard input, visible focus, high contrast, zoom, and reduced motion in the prototype.
- [x] Avoid color-only markers and instructions.
- [x] Provide a password alternative at enrollment and login.
- [ ] Provide passkey enrollment, login, and recovery.
- [x] Complete responsive Playwright UI audit; real-device and assistive-technology pilot remains external.

## Research readiness

- [x] Define confirmatory questions, primary outcomes, and data separation.
- [x] Define the observation attacker procedure at one, two, and three views.
- [x] Complete the a priori power analysis and freeze recruitment/analyzable targets.
- [x] Prepare preregistration hypotheses, exclusions, models, multiplicity, and stopping rules.
- [x] Complete participant information, consent, retention, deletion, and
  recording documents.
- [ ] Obtain appropriate ethics and data-protection review.
- [x] Implement allowlisted research events and keyed administrative export controls.
- [ ] Decide through ethics review whether the optional revoked-chain joint
  dataset is necessary and permitted.
- [ ] Verify pseudonym separation and minimum-count aggregate publication.

## Release gates

- [ ] Complete protocol and manifest test vectors.
- [x] Freeze protocol, scene-pack, and policy version 1 internally with an unversioned user-facing name.
- [ ] Complete ASVS mapping and security review.
- [x] Define performance budgets and add reproducible HTTP/asset benchmark tooling.
- [x] Complete responsive accessibility/UX audit; screen-reader and participant usability review remain external.
- [ ] Complete ethics, data-protection, and preregistration requirements.
- [ ] Begin formal recruitment only after every preceding release gate passes.
