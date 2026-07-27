# SceneChain ethics and data-protection package

Status: consolidated submission draft; written institutional decisions are
required before recruitment.

## Project and participants

SceneChain compares a study-only password with direct and shielded graphical
authentication. It measures completion time, success, delayed retention,
workload, accessibility barriers, recovery and lockout behavior, and
complete-chain observer success. It does not protect real assets or request
real passwords.

Recruit up to 90 consenting adults to retain at least 72 analyzable
participants. Exclude minors, pilot participants, investigators, duplicate
participants, and people unable to provide informed consent. Recruitment
material must state eligibility, expected duration, compensation, voluntary
withdrawal, and complaint routes without coercive wording.

## Procedures and risk

Participants complete three counterbalanced conditions, standardized practice,
timed trials, workload questions, and delayed retention. Observation tasks use
separate disposable credentials and exactly one uninterrupted live view.
Recording is not part of the implemented protocol.

The study presents no valuable account, but possible harms include frustration,
fatigue, accessibility difficulty, accidental credential disclosure, and the
privacy risk of pseudonymous behavioral data. Controls include breaks,
withdrawal, study-only warnings, password alternatives, data minimisation,
pseudonymisation, encryption, throttling, restricted export, and deletion.

## Processing and necessity

Pseudonymous timing and outcome data are necessary for the declared outcomes.
Names, emails, raw clicks, credentials, recordings, third-party analytics, and
unrestricted free text are not necessary and are excluded. The responsible
institution must confirm the controller and GDPR legal basis; online consent
must not be assumed to be the sole legal basis for institutional research.

PostgreSQL separates account records from random research-subject identifiers.
Redis holds only expiring attempt, session, confirmation, and rate-limit state.
Administrative export requires separate credentials, reauthentication, HTTPS,
bounded dates and rows, purpose-separated pseudonymisation, streaming delivery,
and an audit event. Published cell/action statistics use a minimum-count rule.

## Principal risks and controls

| Risk | Control | Residual risk |
|---|---|---|
| Credential disclosure | Study-only warning, encrypted metadata, Argon2id/HMAC verifier | Direct-mode credentials can be copied after observation |
| Re-identification | No direct identity fields, keyed export pseudonyms, separate compensation records | Timing remains pseudonymous personal data |
| Unauthorized export | Separate administrator, reauthentication, HTTPS, limits and audit | Host compromise remains possible |
| Excess collection | Fixed request and database fields | Future changes require review |
| Sparse hotspot disclosure | Thresholded aggregate export | Small groups may remain unsuitable for publication |
| Retention drift | Fixed deadlines, named owner and deletion record | Backups require operational audit |
| Enumeration | Public decoy pool, synthetic accounts, final-only result and rate limits | Deployment timing requires continued monitoring |

## Hosting and institutional fields

Before submission, record:

- principal and student investigators;
- institutional sponsor and data controller;
- data-protection contact and competent supervisory authority;
- ethics committee;
- GDPR legal basis;
- Hetzner datacenter country and data-processing terms;
- Cloudflare's role, processing locations, and transfer safeguards;
- data recipients and access roles;
- encrypted institutional export-storage location;
- compensation handling;
- complaint and incident routes.

## Retention and deletion

- Attempts and sessions: Redis TTL no longer than one hour; attempt state no
  longer than five minutes.
- Operational logs: 14 days, without handles or credential material.
- Consent and pseudonymous events: no later than six months after final
  publication or 24 months after collection closes, whichever comes first.
- Administrative exports: encrypted, access-controlled, audited, and deleted
  by the same deadline.
- Backups containing linked data: expire through rotation within 30 days.
- Thresholded aggregates without re-linking material: may remain as research
  outputs.
- Audio and video: not collected.

Before anonymisation, an authenticated participant may delete their account,
credentials, assignments, and linked events. At study end, revoke export
credentials, delete raw exports and linked database rows, destroy re-linking
material, rotate study keys, and retain only approved aggregates and
reproducible analysis code. Record completion of the deletion procedure.

## Required submission materials

The institutional package consists of:

- [protocol.md](protocol.md);
- [study-plan.md](study-plan.md);
- [participant-materials.md](participant-materials.md);
- this ethics and data-protection package;
- [researcher-runbook.md](researcher-runbook.md);
- recruitment and compensation wording;
- investigator qualifications and institutional sponsor information.

The responsible committee and data-protection office determine the final forms
and supporting material. Repository documentation cannot substitute for their
written decisions.

## Release decision

Recruitment remains blocked until institutional fields are complete, the pilot
and preregistration are complete, participant materials are approved, written
ethics and data-protection references are available, technical checks pass, and
an authorized release is recorded.
