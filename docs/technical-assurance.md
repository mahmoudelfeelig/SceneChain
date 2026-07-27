# SceneChain technical assurance

Status: implemented technical controls and release evidence. This is not an
independent security certification.

## Architecture

SceneChain uses React, TypeScript, and Vite in the browser; Java 21, Spring Boot,
and Spring Security in the API; PostgreSQL for durable accounts and research
records; Redis for expiring attempts, sessions, and throttles; and a frozen
local scene pack. Docker Compose provides the deployment boundary.

Java owns credential encoding, quantization, manifest validation, attempt
generation, and verification. TypeScript handles presentation and local input.
Shared test vectors prevent protocol drift. Production migration and runtime
database accounts use separate roles.

Independent secrets protect verifier HMACs, encrypted graphical metadata,
synthetic unknown-account cues, keyed account identifiers, and research export
pseudonyms. Example or repeated production secrets are rejected.

## Security boundary and claims

SceneChain protects study-only authentication knowledge. It does not claim
phishing resistance, multi-factor authentication, protection against a
compromised endpoint, or safety for valuable accounts. Direct mode is
observable. Shielded mode aims to retain location uncertainty after one
opportunistic observation but weakens across repeated observations.

Authentication never branches on partial correctness. Attempts are opaque,
cookie-bound, expiring, atomically single-use, and protected by same-origin and
CSRF controls. Unknown handles receive stable synthetic cues and dummy Argon2id
work. Complete verification folds stage differences into one final result.

The main residual risks are phishing, malware or camera capture, repeated
observation, denial of service, application-host compromise, timing
distinguishability, and research-operator misuse.

## Web and operational controls

- HTTPS, secure same-site cookies, restrictive CSP, frame denial, and
  `Cache-Control: no-store` protect browser surfaces.
- No third-party runtime scripts, analytics, fonts, advertisements, or user HTML
  are loaded on authentication pages.
- Requests use strict state, type, length, range, origin, and manifest-version
  validation.
- SQL is parameterized and database roles are least-privileged.
- Sessions and attempts use high-entropy opaque identifiers, explicit expiry,
  rotation, and server-side invalidation.
- Redis-backed account, network, endpoint, and capacity limits constrain abuse.
- Authentication bodies and credential-derived values are excluded from logs,
  URLs, persistent browser storage, and research exports.
- Research exports require separate administrator credentials,
  reauthentication, bounds, pseudonymisation, streaming, and audit.

## Protocol conformance

Implemented and automatically exercised requirements include:

- five stages, a 24-by-16 grid, four actions, and exact matching;
- all 384 cells selectable with no visible recommendation metadata;
- initial enrollment plus two full confirmations;
- versioned consent and a two-question understanding gate;
- recognition from the complete 48-scene pool;
- direct and fresh shielded presentations;
- six server-assigned counterbalanced sequences;
- practice, immediate, workload, and seven-day retention phases;
- server-authoritative 180-second timing;
- final-only verification and replay rejection;
- separate research-subject identifiers and cascading participant deletion;
- bounded audited exports and thresholded aggregate outcomes;
- keyboard grid navigation, focus restoration, and radio semantics;
- a release gate binding the protocol and scene-pack hashes to external
  approval references.

Institutional identities, legal basis, compensation, duration, ethics, and
data-protection decisions are intentionally external and remain recruitment
gates.

## Verification evidence

The automated pipeline runs:

- Java 21 Maven tests;
- frontend lint, shared protocol tests, TypeScript build, and dependency audit;
- Compose configuration and script syntax validation;
- complete scene-pack and digest validation;
- backend container vulnerability scanning;
- clean-database Flyway migrations;
- closed-gate negative security tests;
- disposable local enrollment and authentication integration;
- replay, malformed-input, origin, unknown-account, and unauthorized-export
  checks.

The latest local release review passed 13 backend tests, three frontend protocol
tests, formal enrollment and authentication, authenticated and unauthorized
export paths, participant deletion, audit readback, keyboard/overflow checks,
and the frozen 48-scene pack. Local warm p95 measurements were 9.17 ms for the
landing document and 17.16 ms for pack status. These are engineering baselines,
not universal production claims.

Current frozen identifiers:

- Protocol SHA-256:
  `4c8b6fcccad045a4916f72a0116a01da9dfe60b63fde88c0b7b6459c938bd4d6`
- Scene manifest SHA-256:
  `99bc78510a377e6b7712cd120a76df844a9cb311616f682aa6046047e5bdfb58`
- Consent identifier: `scenechain-consent-2026-07-22`

Changing the frozen protocol requires a new digest, deployment configuration,
release-gate record, and any institutionally required amendment. A digest
mismatch keeps recruitment closed.

## Remaining pre-recruitment checks

- Review production TLS, proxy headers, key storage, backups, and permissions.
- Confirm timing equivalence across known and unknown accounts with a larger
  deployment sample.
- Perform the standardized-device browser performance check.
- Retain and review backend and container software-bill-of-materials output.
- Complete an independent security review appropriate to the institutional
  risk decision.
