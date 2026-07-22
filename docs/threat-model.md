# SceneChain threat model

Status: reviewed for the frozen SceneChain protocol. Review again when implementation or
deployment architecture diverges from the documented design.

## Protection target

SceneChain is a research authentication system. It gates only participant study
tasks and result views, not valuable, financial, medical, administrative, or
safety-critical functions. This limits harm while the new authenticator is being
evaluated.

The security objective is to accept only a claimant who knows the complete
enrolled visual passphrase while preventing partial-stage feedback, online guess
scaling, replay, account enumeration, secret leakage, and invalid research data.

SceneChain is one knowledge factor. It is not phishing-resistant and is not MFA.

## Assets

- authenticated participant sessions;
- graphical credential verifiers and encrypted canonical credentials;
- password, passkey, and recovery authenticators;
- attempt nonces, CSRF tokens, session identifiers, and rate-limit state;
- verifier pepper, metadata-encryption keys, and pseudonymization keys;
- immutable scene assets, cell masks, policy versions, and manifests;
- consent records, participant measurements, and aggregate hotspot counts;
- integrity and reproducibility of the experiment.

## Trust boundaries

The browser and every submitted value are untrusted. TLS protects transport but
does not make the browser trustworthy. PostgreSQL, Redis, static asset storage,
the API process, the reverse proxy, research administration, and exported data
are separate boundaries.

PostgreSQL compromise is modeled without application keys. Redis compromise is
modeled as disclosure or manipulation of temporary state without durable
credentials. A complete application-host compromise can capture or decrypt
credentials and is not prevented by SceneChain.

## Attacker profiles

### Online guesser

Knows or guesses account identifiers, rotates network sources, automates attempt
creation and completion, and uses public scene manifests and measured hotspot
distributions to order guesses.

Controls: complete-chain verification, independent account/network limits,
bounded verifier capacity, generic behavior, unrestricted enrollment with measured
hotspots, and measured rather than theoretical strength claims.

### Opportunistic observer

Sees one login but cannot revisit the session. Direct mode exposes the credential
when the interaction is clearly visible. Shielded mode exposes actions and one
marker-defined candidate set per stage.

Enrollment observation exposes the direct locations and actions and is treated
as full credential compromise. Researchers do not observe or record enrollment
input.

### Targeted or repeated recording observer

Records or repeatedly observes sessions and can inspect screen content and input.
Direct mode is compromised after one clear observation. Repeated observation is
outside shielded mode's protection claim and is expected to shrink candidate sets
quickly. The confirmatory observer task therefore uses exactly one live view.

### Phishing or relay attacker

Replicates the public scenes or relays challenges to the real service. SceneChain
does not provide origin-bound cryptographic authentication. Passkeys are the
phishing-resistant alternative.

### Database attacker

Obtains PostgreSQL records, salts, Argon2id parameters, HMAC verifiers, encrypted
metadata, scene cues, and research records, but not separate application keys.

Controls: unique salts, memory-hard hashing, separate HMAC pepper, AEAD metadata,
key separation, aggregate-only hotspot data, and minimal participant linkage.

### Redis attacker

Reads, modifies, creates, or deletes temporary attempt and throttle state.

Controls: no credential responses in Redis, strict TTLs, authenticated network
access, least privilege, atomic completion, server validation, and treating lost
throttles as a security incident. A Redis compromise can enable denial of service
or weaken rate limits and must not be treated as harmless cache loss.

### Malicious client

Modifies JavaScript, skips stages, submits invalid cells/actions, replays attempts,
changes viewport claims, runs parallel attempts, or sends oversized payloads.

Controls: server-owned state, fixed-width canonicalization, manifest validation,
range and size limits, nonce binding, CSRF/Origin checks, replay prevention, and
server-side quantization.

### Application-origin or supply-chain attacker

Uses XSS, a compromised dependency, remote analytics, clickjacking, or malicious
asset replacement to capture credentials.

Controls: no third-party code on authentication pages, restrictive CSP, frame
protection, self-hosted immutable assets, dependency locking, integrity checks,
output encoding, and OWASP ASVS Level 2 verification. A fully compromised origin
can capture credentials and is a credential-rotation event.

### Research insider

Uses administrative access or exports to identify participants, inspect consented
recordings, or infer credentials.

Controls: role separation, reauthentication, export allowlists, pseudonymous
research identifiers, aggregate hotspot counters, audit logs, minimum-count
publication rules, and defined retention and deletion.

## Principal threats

| Threat | Impact | Required response | Residual risk |
| --- | --- | --- | --- |
| Correctness-dependent next scene | Independent stage discovery | Always show the same five cue scenes; final-only verification | Regression testing must preserve the no-branching invariant |
| Human hotspot selection | Reduced effective strength | No visible recommendations, pilot heatmaps, aggregate counts, measured guess models | Human choices remain correlated |
| One direct recording | Credential disclosure | Explicit non-claim and credential rotation | Direct mode remains observable |
| Enrollment observation | Credential disclosure before first use | Private enrollment, no researcher viewing or recording, re-enroll after exposure | Endpoint recording or malware remains |
| One shielded recording | Actions leak; locations narrow | Eight balanced markers and measured candidate sets | One-observation online success may remain non-negligible without throttling |
| Repeated shielded recordings | Candidate intersection | Explicit non-claim and study-only use | Repeated recordings can reveal locations |
| Account enumeration | Target discovery | Stable synthetic cues, dummy hash work, generic responses, timing tests | Large-sample timing differences may remain |
| Replay | Session compromise | Single-use cookie-bound attempt, CSRF, Origin, expiry, atomic completion | Compromised live browser can submit once |
| Offline verifier guessing | Credential recovery | Argon2id, salt, HMAC pepper, measured choice distribution | Pepper loss exposes human-selected space to guessing |
| Metadata/key compromise | Exact credential disclosure | AEAD, separated keys, access control, rotation | DB plus path key reveals credentials |
| XSS/supply-chain capture | Exact credential disclosure | Self-hosted strict authentication surface and ASVS verification | Malicious browser extensions and endpoint malware remain |
| Asset mutation | Credential failure or capture | Immutable hashes, signed pack freeze, new versions only | Compromised origin remains capable of substitution |
| Lockout abuse | Denial of service | Delay and temporary suspension, never permanent attacker lock | Targeted nuisance remains possible |
| Telemetry leakage | Credential or identity inference | Separate data planes and allowlisted schemas | Pseudonymous timing remains personal data |
| Biased or underpowered study | Invalid conclusions | Separate pilot, power analysis, counterbalancing, preregistration | Results may not generalize beyond the sample |
| Accessibility exclusion | Participant cannot use graphical method | Keyboard grid, recorded accommodations, withdrawal without penalty, desktop/laptop scope | Graphical condition is not universally accessible |

## Enumeration and timing

Known and unknown identifiers must receive stable five-scene sequences with the
same policy and public response structure. Unknown sequences are derived using a
server key, not returned randomly. Completion for unknown accounts performs dummy
Argon2id work. Tests compare status, headers, body size, cache behavior, request
count, and timing distributions rather than checking only the visible message.

## Privacy and research-data model

Authentication identifiers and research participant identifiers are separate.
Pseudonymous data remains personal data when a re-linking key exists. The linking
table uses a separate random subject identifier, cascades on deletion, and is
accessible only to approved study staff.

Authentication logs contain no credential-derived values. Participant-linked
events contain timings and outcomes but no locations, directions, markers, scene
sequence, or curation-region position. Hotspot analysis is an unlinkable aggregate counter
with no participant, account, attempt, session, timestamp, or network field.
The counter is write-only during collection and is not queryable until a declared
cohort closes, preventing researchers from correlating a live increment with the
participant currently enrolling.

An optional joint choice dataset may contain complete revoked study-only chains
for guess-model research. It is a separate high-risk research asset: separate
consent, no participant or performance linkage, batch shuffling, research-only
encryption, access logging, and scheduled deletion are mandatory. It must never
contain a credential that still protects an account or real data.

The observation task is live and unrecorded and uses ephemeral target credentials.
Any later recording requires a protocol amendment, separate consent, and new
ethics/data-protection review.

Before recruitment, the study documents the data controller, processors, legal
basis, purposes, fields, recipients, retention, withdrawal and deletion behavior,
security measures, international transfers if any, and contact path. Institutional
ethics and data-protection review is required before formal collection.

## Assumptions and exclusions

- The formal study uses the frozen desktop viewport and scene pack.
- TLS, operating system, browser, Java runtime, and cryptographic libraries are
  maintained and correctly configured.
- The participant endpoint is not already controlled by malware.
- Denial of all network or hosting service is outside protocol availability.
- Passkey security is evaluated as an alternative authenticator, not attributed
  to SceneChain.
- Results from the research sandbox do not justify production deployment without
  a new risk assessment.

## Allowed claims

- The configured space can be calculated from manifest eligible-cell counts.
- Marginal hotspot behavior can be estimated from unlinkable aggregate counts.
- Full-chain guess resistance can be modeled only when the separately consented
  joint dataset is sufficient to measure cross-stage correlations.
- Effective guess resistance can be estimated from aggregate choice distributions.
- Direct and shielded observation success can be compared for the exact study
  procedure and observation count.
- Client, network, database, and hash costs can be benchmarked against the manual
  password condition.

## Prohibited claims without new evidence

- “More secure than passwords” without a named attacker model and metric.
- “Shoulder-surfing-proof” or “recording-proof.”
- “58-bit security” or any configured-space value presented as effective entropy.
- “Phishing-resistant,” “multi-factor,” or “password replacement.”
- “Accessible” based only on alternative login availability.
- “Production ready” based on a research prototype.

## Validation gates before participants

- Publish canonicalization and challenge-generation test vectors.
- Verify coordinates at every supported viewport and zoom configuration.
- Test unknown-account enumeration statistically, not by visual inspection.
- Test replay, CSRF, session fixation, parallel attempts, invalid fields, stale
  versions, oversized inputs, and Redis state manipulation.
- Verify authentication payloads never enter logs, traces, analytics, crash
  reports, database query logs, or proxy logs.
- Test account/network/server-capacity throttles and IP rotation.
- Complete the ASVS Level 2 mapping and security review.
- Complete scene pilot, power analysis, preregistration, consent, ethics, and
  data-protection review.

## References

- [GDPR Article 5 principles](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679)
- [EDPB pseudonymisation overview](https://www.edpb.europa.eu/topics/ai-and-technology/anonymisationpseudonymisation_en)
- [W3C Accessible Authentication](https://www.w3.org/WAI/WCAG22/Understanding/accessible-authentication-minimum.html)
