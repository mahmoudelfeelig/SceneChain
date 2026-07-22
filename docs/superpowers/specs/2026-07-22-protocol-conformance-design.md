# SceneChain protocol-conformance design

Status: approved for implementation by the project owner on 2026-07-22.

## Objective

SceneChain will be a research instrument whose server-enforced behavior matches one normative protocol. It compares a study-only manual password, direct graphical authentication, and a usable shielded presentation. It never protects a valuable account. The confirmatory study is limited to adults using desktop or laptop viewports of at least 1024 CSS pixels. Public information pages remain responsive, while an undersized task viewport pauses rather than changes credential geometry.

Shielded mode is evaluated only against one opportunistic observation. Direct mode is expected to be disclosed by one clear observation. Neither mode is presented as phishing resistant, multi-factor, or suitable for repeated targeted surveillance.

## Normative study design

Every participant completes all three conditions in one of the six permutations of password, direct, and shielded. The server assigns the least-used sequence with cryptographically random tie-breaking and persists it before any credential enrollment. Participants cannot select or reorder conditions.

Each condition has a standardized enrollment or setup phase, two successful practice trials, three measured immediate trials, a workload questionnaire, and a delayed-retention trial after a declared interval. The application records unsuccessful trials instead of excluding them. The primary estimand is the direct-versus-password difference in log completion time for all measured trials, with unsuccessful trials represented by the prespecified timeout ceiling rather than conditioning on success. First-attempt success is a co-primary usability outcome. Period, sequence, participant, viewport, input method, browser family, deviations, and system failures are recorded from allowlisted values.

The observer task uses a separate study-only target credential and exactly one clear login observation. It records complete-chain observer success without storing video, raw clicks, secret cells, markers, or actions. The lockout task is scripted against a disposable study credential and measures policy behavior without teaching participants another person's credential.

## Graphical credential and scene presentation

The frozen pack is exactly `scene-pack/v1/manifest.json` with the recorded manifest SHA-256. Startup fails closed for research enrollment if the manifest, canonical images, delivery images, or thumbnails do not match their recorded hashes. The backend exposes the pinned pack status; no production fallback pack is permitted.

Five scenes from distinct families are assigned by a cryptographically secure generator. All 48 scenes are returned for known and synthetic accounts. Every scene retains all 384 selectable cells. Recommended-cell sets remain frozen internal metadata for QA and aggregate analysis but are never visibly highlighted or used to restrict formal enrollment.

The complete five-stage chain is entered initially and then confirmed twice. The server retains only a keyed confirmation tag in short-lived state, accepts each complete repetition only when it matches in constant time, and stores no credential until the second confirmation succeeds. The browser cannot reduce the number of submissions.

Direct login accepts scene, cell, and direction at each stage and verifies only after the complete chain is submitted. Shielded login displays a fresh balanced 12 by 8 marker overlay, accepts marker and direction responses, and verifies only after all five stages. The protocol claims resistance only to a single opportunistic view; repeated-view resistance is an explicit non-goal.

## Server-owned study state

A study session table owns consent version, eligibility, assigned sequence, current period, current phase, condition, trial number, attempt number, timestamps, completion, withdrawal, deviations, and failure flags. Every transition is validated transactionally. Authentication events are accepted only when accompanied by a one-time server-issued trial token matching the participant, condition, phase, and active attempt. Free-form public event ingestion is removed.

The frontend renders the server state and cannot choose a condition or advance a phase independently. It uses a monotonic timer for user feedback, but authoritative elapsed time is computed by the server from trial-token issuance to atomic consumption. Stage times may be submitted as bounded optional measurements and are checked against the total.

## Research data and privacy

Consent is versioned and records granular agreement to participation and required research-data processing. Optional future-contact or recording purposes are absent from this study. Participant information, consent, privacy notice, retention, deletion, and withdrawal copy share the same controller, investigator, DPO, processor, hosting, compensation, duration, legal-basis, and ethics-reference fields. Recruitment remains technically disabled until a signed release-gate record states that all mandatory institutional fields and external ethics/data-protection decisions are complete.

Research events use a closed enum and condition-specific payload columns. Event rows reference a dedicated random study-subject identifier rather than the account UUID. The account-to-subject link is separately encrypted and deletable. Credential-choice observations are stored only as thresholded aggregate counters. Raw pointer data, secret chains, passwords, overlay seeds, network addresses, user agents, and third-party telemetry are prohibited research data.

Verified deletion revokes sessions, deletes credentials and linkage, and either deletes identifiable research rows before anonymisation or records that previously anonymised aggregates cannot be reversed. A scheduled retention job applies the earlier of 24 months after collection closes or six months after final publication. Consent withdrawal is an auditable state transition, not a client-only message.

## Authentication and operational security

Production startup rejects missing, example, short, reused, or inactive-version cryptographic keys. Verifier pepper, metadata encryption, research pseudonymisation, audit identifiers, and export authorization are purpose-separated and versioned. Rotation supports active-write and retained-read key versions.

Sessions have a one-hour absolute lifetime and a shorter idle lifetime; activity cannot extend the absolute expiry. Disabled, withdrawn, or deleted participants cannot use `/me`. Attempt and trial tokens are random, cookie-bound, same-origin, CSRF-protected, expiring, and atomically single-use.

Rate limiting uses trusted-proxy-aware client addresses only when the request came from a configured proxy. It combines account, network, endpoint, and server-wide Argon2 concurrency limits, returns `Retry-After`, and applies bounded increasing delays. Unknown accounts receive synthetic challenges and equivalent verifier work.

Research export moves behind a separate administrative identity with reauthentication on every request, audit records, row and date limits, HTTPS enforcement, and streaming download. Export pseudonyms use a dedicated key. Participant credentials cannot reach export routes. The application uses a least-privilege database role in production.

## Accessibility and performance

The 24 by 16 grid is one keyboard widget with roving focus and arrow-key navigation, not 384 sequential tab stops. Cells expose row, column, selection state, instructions, and a visible focus indicator. Mode controls use radio semantics, forms support Enter, phase changes restore focus and announce status, and reduced-motion and high-contrast behavior remain usable. A standardized alternative-input accommodation is recorded as a protocol deviation instead of silently altering the task.

The gallery uses small immutable hashed thumbnails with lazy image decoding. Only the selected full-resolution scene is loaded for interaction. WebP and other frozen assets receive immutable caching while authentication and participant responses remain `no-store`. Benchmarks cover cold and warm gallery load, interaction readiness, memory use, authentication latency, Argon2 saturation, and export limits against declared budgets.

## Conformance and release gates

A machine-readable conformance matrix maps every normative protocol statement to an implementation owner and automated or manual evidence. Shared credential vectors are consumed by both Java and TypeScript tests. CI validates the exact manifest hash, every asset hash, migrations, frontend types/lint/tests, backend unit/integration/security tests, accessibility checks, and performance budgets.

Recruitment is disabled by default. Enabling it requires the frozen protocol and pack identifiers, completed institutional fields, preregistration identifier, written ethics decision, written data-protection decision, approved participant documents, passing release checks, and an auditable administrator action. These external decisions remain outside the software's ability to grant.

## Migration and rollback

Schema changes are additive until the new state machine is deployed. Existing prototype accounts are marked non-study and cannot enter confirmatory sessions. The old free-form event endpoint and header-only export are removed rather than kept as compatibility paths. Rollback disables recruitment and authentication trials while retaining encrypted data; it never silently re-enables the previous weaker paths.
