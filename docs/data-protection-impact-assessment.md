# SceneChain data-protection assessment

Status: submission-ready draft; institutional DPO review required before recruitment.

## Processing and necessity

The purpose is scientific comparison of authentication usability and observation resistance. Pseudonymous timing and outcome data are necessary for the declared outcomes. Names, emails, raw click coordinates, secrets, recordings, and third-party analytics are unnecessary and excluded. The legal basis and controller must be confirmed by the responsible institution; consent is implemented but should not be assumed to be the only applicable GDPR legal basis for institutional research.

## Data flow

PostgreSQL separates account records from random study-subject identifiers and cascades linked deletion. Redis stores expiring attempts, confirmation tags, sessions, and rate-limit state, but not raw graphical credentials. Administrative export requires a separate administrator identity and reauthentication, HTTPS, a bounded date range and row count, a purpose-separated keyed pseudonym, streaming delivery, and an audit event. Publication uses aggregate cells with a minimum-count rule.

## Main risks and controls

| Risk | Control | Residual risk |
|---|---|---|
| Credential disclosure | Study-only warning, no valuable resources, encrypted metadata, Argon2id/HMAC verifier | Observed direct-mode credentials can be copied |
| Re-identification | No direct identity fields, keyed export pseudonyms, separate compensation records | Behavioral timing remains pseudonymous personal data |
| Unauthorized export | Separate administrator credential, per-request reauthentication, HTTPS, limits, audit, purpose-separated pseudonym key | Application/server compromise remains possible |
| Excess collection | Fixed DTO and fixed database columns; no generic event properties | Future code changes require review |
| Linkage through hotspots | Write-only aggregate counters, no participant linkage | Sparse cells require publication thresholds |
| Retention drift | Scheduled earlier-of deadline enforcement plus named owner and backup procedure | Backup expiry still requires operational audit |
| Enumeration/guessing | Full public decoy pool, synthetic accounts, final-only result, rate limits | Timing equivalence requires deployment testing |

## GDPR principles

The design follows purpose limitation, data minimisation, storage limitation, integrity/confidentiality, and accountability. Article 13 information is represented in the participant materials. Scientific-research safeguards require pseudonymisation and separation where possible. Final controller, processor, hosting region, recipient, transfer, DPO, supervisory-authority, and legal-basis details must be completed before approval.

## Decision

The technical design can support a low-risk adult study protecting no valuable account. Recruitment remains blocked until the institutional DPO confirms the data-protection concept and the responsible ethics committee issues its decision.
