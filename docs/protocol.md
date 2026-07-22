# SceneChain protocol

Status: frozen technical and study protocol pending external ethics and data-protection decisions. Recruitment is disabled.

## Research question and scope

SceneChain is a study-only knowledge-factor authenticator. The confirmatory experiment asks whether direct graphical authentication has completion performance that is not meaningfully worse than a manual study password, while describing success, workload, delayed retention, accessibility barriers, lockout behavior, choice concentration, and resistance to one opportunistic observation.

It protects no valuable account and is not phishing resistant, multi-factor authentication, a passkey replacement, or protection against malware, coercion, targeted surveillance, or repeated recordings.

## Participants and device eligibility

Participants are consenting adults aged 18 or older who did not take part in the pilot and can complete the study instructions. The confirmatory task is desktop/laptop only. At the start of a session the application requires a viewport of at least 1024 by 600 CSS pixels and records an allowlisted browser family and primary input method. Information and privacy pages remain responsive on smaller screens. If the viewport becomes unsupported during a task, the task pauses and the deviation is recorded; credential geometry is never silently changed.

Approved accessibility accommodations are recorded as deviations. Investigators do not infer disability or collect diagnoses. People who encounter an access barrier may stop without penalty, and the barrier remains a reportable outcome rather than an exclusion chosen after seeing performance.

## Design and assignment

The study is a within-participant, three-condition crossover. The six sequences are `PDS`, `PSD`, `DPS`, `DSP`, `SPD`, and `SDP`, where `P` is password, `D` is direct SceneChain, and `S` is shielded SceneChain. The server assigns the least-used sequence with cryptographically random tie-breaking and stores it before trials begin. Participants cannot select or reorder conditions.

For each condition the application enforces:

- setup or enrollment using a study-only credential;
- practice until two successful complete logins, retaining failed attempts;
- three measured immediate trials, retaining successes, failures, and timeouts;
- the six-item raw NASA-TLX workload rating on 0–20 scales;
- a delayed-retention trial seven days later for each condition.

The retention task unlocks seven days after immediate conditions finish. A return outside the requested seven-day plus-or-minus-two-day window is retained and flagged as a timing deviation. Instructions, browser, viewport, network environment, and breaks are standardized and documented. System failures are recorded separately from participant outcomes.

## Credentials and actions

Each graphical credential contains five ordered stages. Every stage contains one server-assigned scene, one chosen cell in a 24-column by 16-row grid, and one chosen action:

```text
0 = north/up
1 = east/right
2 = south/down
3 = west/left
```

`cell-id = row * 24 + column`, from 0 through 383. All 384 cells are available. Recommended-cell sets are frozen internal curation metadata and are not displayed, suggested, or used to restrict enrollment.

The participant enters the initial complete chain and then repeats it twice. The server stores only a keyed confirmation tag between submissions, rejects any mismatching complete repetition without stage feedback, and stores a credential only after two matching confirmations. The client cannot reduce this requirement.

The canonical fixed-width credential is protected with Argon2id and a purpose-separated HMAC pepper. Shielded verification additionally uses AEAD-encrypted canonical metadata. Exact cell and action equality is required; neighbor tolerance is excluded.

## Frozen scene pack and private recognition

The formal pack contains 48 verified CC0 scenes across eight families. Five scenes from distinct families are assigned using a cryptographically secure generator. The frozen manifest SHA-256 is `99bc78510a377e6b7712cd120a76df844a9cb311616f682aa6046047e5bdfb58`.

The manifest records hashes for every canonical image, delivery image, and thumbnail. Startup verifies the exact manifest digest and every asset digest. Recruitment fails closed on any mismatch; production never substitutes development scenes.

Both known and synthetic accounts receive the complete shuffled 48-scene public pool. The participant privately recognizes the five scenes in enrollment order. Public responses never label account scenes or expose stage correctness. Gallery thumbnails are immutable delivery assets; only a selected scene loads at interaction resolution.

## Direct and shielded presentations

In direct mode the participant selects the remembered cell and action. Selection feedback indicates input only. One clear observation is assumed capable of disclosing the graphical credential.

In shielded mode a fresh balanced 12-by-8 overlay covers the 24-by-16 grid. Eight non-color-only markers occur exactly twelve times and each marker tile covers four cells. The participant mentally locates the secret cell, reports the covering marker, and enters the secret action. The system evaluates all five responses together.

Shielded mode is designed for usability and evaluated only against one opportunistic observation. It does not claim resistance to two or more recordings. Actions remain visible and the one-view experiment measures complete-chain observer success, not per-stage guesses.

## Timing and outcomes

An authentication attempt begins when the server issues its single-use attempt state and ends when that state is atomically consumed or the 180-second timeout is reached. The server clock is authoritative. Client monotonic stage times are optional bounded supporting measurements and may not exceed the total.

The primary outcome is the participant-level geometric-mean ratio of direct to password completion time across the three measured immediate trials. Every issued measured trial contributes: unsuccessful or timed-out trials receive the prespecified 180-second ceiling. The primary hypothesis is non-inferiority of direct SceneChain with a ratio margin of 1.25. The one-sided test uses alpha 0.025; the confidence-interval presentation is two-sided 95%.

Secondary outcomes are first-attempt success, eventual practice success, shielded time and workload, enrollment time, seven-day retention success and time, recovery use, lockout behavior, accessibility barriers, complete-chain observer success after exactly one observation, and aggregate cell/action concentration. Secondary analyses report effect estimates and confidence intervals; Holm adjustment applies within declared outcome families. Exploratory analyses are labelled.

## Observer and lockout tasks

The observer sees one clear login to a separate disposable study account, cannot pause or replay it, and then receives one complete-chain attempt. No video, audio, screenshot, raw click, marker, action, or secret is retained. Direct and shielded observers use equivalent viewing conditions and never target another participant's durable credential.

The lockout task uses a disposable scripted account. The participant follows fixed incorrect-attempt instructions until the documented throttle response appears, records the communicated wait behavior, and stops. It never requests attacks against a real or another participant's credential.

The frozen researcher runbook standardizes both tasks. Observer, lockout,
accessibility, and recovery results are entered only through separately
authenticated administrator endpoints. The participant handle is used
transiently to resolve the random study-subject identifier and is not retained
in those outcome rows. Dedicated database constraints enforce one observation,
one observer attempt, no recording, a disposable lockout target, bounded
throttle values, and closed accessibility codes.

## Attempts, sessions, and abuse resistance

Attempt, enrollment, and session tokens contain at least 128 random bits, use Secure, HttpOnly, SameSite=Strict, host-only cookies in production, and remain server-side. Attempts are cookie-bound, same-origin, CSRF-protected, expiring, and atomically single-use. Sessions have a one-hour absolute lifetime; activity does not extend it. Disabled, withdrawn, and deleted accounts cannot use authenticated endpoints.

Unknown handles receive synthetic cues, a full shuffled pool, generic responses, and dummy Argon2 work. Account, network, endpoint, and global rate limits apply. Throttle responses include `Retry-After`; global Argon2 concurrency is bounded. The reverse proxy is the only production route to the backend and supplies the trusted client-address chain.

## Research data and privacy

Research events use fixed columns and closed values. They contain a random study-subject identifier, condition, phase, period, trial number, success/failure/timeout, total time, up to five stage times, retry count, viewport class, input method, browser family, deviation code, system-failure flag, and timestamp. The account-to-subject relationship is stored separately and is deleted with the account.

Raw pointer streams, graphical secrets, scene choices, actions, markers, overlay seeds, typed passwords, names, emails, IP addresses, user-agent strings, recordings, and third-party analytics are prohibited research data. Cell/action observations are write-only aggregate counters and publication applies a minimum-count threshold.

Participant deletion requires an authenticated session, password reauthentication, and explicit confirmation. It revokes the session and cascades through credentials, assignments, linkage, and still-pseudonymous event rows. Irreversibly anonymised published aggregates cannot be reversed. The scheduled retention rule deletes linked data at the earlier of 24 months after collection closes or six months after final publication.

Research export requires separate administrator credentials on HTTPS, reauthentication on every request, a bounded date range, a maximum of 10,000 rows, streaming delivery, a purpose-separated pseudonymisation key, and an audit event. Participant credentials cannot authorize export.

Authentication events and secondary outcome forms have separate bounded CSV
exports. Both replace internal subject identifiers with the same keyed export
pseudonym, and every data-entry, denied-entry, and export operation is audited.

## Consent and recruitment gate

Consent is explicit, versioned, and covers participation and required research-data processing. Recording and future-contact consent are not bundled because the implemented protocol collects neither. Participant information identifies the controller, investigator, DPO, processor/host, legal basis, recipients, retention, withdrawal method, compensation, duration, ethics reference, and complaint routes before recruitment.

Before consent can be submitted, the participant must affirm that the information and privacy notice were read and correctly answer two client-side understanding questions: the account protects nothing valuable, and participation may stop at any time without penalty. Incorrect answers prevent enrollment but are not transmitted, retained, or treated as outcome-based exclusions. The accepted consent timestamp and frozen consent identifier are stored when enrollment begins.

Recruitment is disabled by default. It may be enabled only after the protocol and manifest hashes are recorded, all institutional fields are completed, the preregistration identifier is recorded, written ethics and data-protection decisions are recorded, participant documents are approved, verification gates pass, and an authorized administrator records the release. Software cannot issue those external decisions.

## Deviations, exclusions, and stopping

Exclude only withdrawal, ineligibility discovered after enrollment, pilot participation, duplicate participation, or a documented system failure that makes the assigned outcome unknowable. Do not exclude legitimate slow, failed, timed-out, accessibility-affected, or outlying trials. Report every exclusion by reason before condition outcomes are unblinded.

Recruit up to 90 participants to retain at least 72 analyzable paired participants. Recruitment stops only when both 72 analyzable participants have completed and the approved cap has not been exceeded, or when the cap of 90 is reached. There is no significance-based interim stopping. Privacy, safety, or critical integrity incidents may pause the study.

## Frozen artifacts and amendments

The credential format, five stages, four actions, exact matching, 24-by-16 grid, all-cell enrollment, 48-scene pool, two confirmations, six condition sequences, trial counts, timing ceiling, seven-day retention interval, primary estimand, non-inferiority margin, retention/deletion rule, and manifest digest are frozen before recruitment.

Any change after recruitment begins requires an amendment describing the reason, affected participants, compatibility identifier, security review, ethics/data-protection implications, and analysis treatment. The user-facing document remains named “SceneChain protocol”; internal identifiers exist only for safe migrations and interpretation.
