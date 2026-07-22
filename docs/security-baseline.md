# Web and operational security baseline

Status: implementation requirements, targeting OWASP ASVS 5.0 Level 2.

## Browser surface

- HTTPS is mandatory outside isolated local development.
- Authentication pages are same-origin and contain no third-party scripts,
  analytics, remote fonts, advertisements, or user HTML.
- A restrictive Content Security Policy disallows unapproved script, frame,
  object, and connection sources; `frame-ancestors 'none'` prevents clickjacking.
- Authentication and authenticated responses use `Cache-Control: no-store`.
- Sensitive values never appear in URLs, browser history, DOM data attributes,
  client logs, or persistent browser storage.
- Dependencies are locked, reviewed, and updated through intentional changes.

An XSS or malicious browser extension can observe graphical input. Preventing
application-origin XSS is mandatory; defending against a fully compromised user
endpoint is outside the protocol's guarantees.

## Input and API controls

- Every request has strict type, length, range, and state-machine validation.
- Only manifest-known scene and policy versions are accepted.
- SQL access uses parameterized queries and least-privilege database roles.
- CORS is disabled for the same-origin deployment.
- State-changing requests validate CSRF tokens and Origin.
- API responses use generic public errors and private structured diagnostics that
  contain no secret-derived values.

## Authentication and attempts

- Attempt and session identifiers contain at least 128 bits of randomness.
- Attempt state expires, is single-use, and is atomically consumed.
- Unknown accounts receive synthetic stable cues and dummy Argon2id work.
- Rate limits use independent account, network, endpoint, and server-capacity
  buckets; they are tested against IP rotation and boundary bursts.
- Authentication success rotates session state. Logout and expiry invalidate it.
- Sensitive account changes require recent reauthentication.

## Cryptography and secrets

- Only maintained platform or well-reviewed libraries implement Argon2id, HMAC,
  AEAD, and random generation.
- Algorithms, parameters, key IDs, and credential formats are versioned.
- AEAD nonces are unique per key and ciphertext.
- Keys are separated by purpose and never stored in PostgreSQL or logs.
- Comparisons of verifiers and complete results do not exit on the first differing
  stage.
- Production-like environments prohibit default or example secrets.

## Logging and telemetry

The logging schema is allowlisted. Request bodies at authentication, enrollment,
recovery, and participant-data endpoints are never logged. Distributed tracing is disabled
for secret-bearing payloads. Error reporters receive scrubbed exceptions only.

Security audit events record account lifecycle and administrative actions using
keyed identifiers. They do not contain graphical stages, markers, scene sequences,
passwords, or recovery material.

## Research administration

Research exports require separate administrator credentials, reauthentication on
every request, HTTPS, bounded dates and rows, a purpose-separated pseudonym key,
streaming delivery, and an audit event. Participant credentials cannot authorize
them. Aggregate hotspot exports enforce the predefined minimum-count rule.

## Verification gates

- Map applicable OWASP ASVS 5.0 Level 2 requirements to automated or manual tests.
- Run dependency, secret, static-analysis, and container scans in CI.
- Test enumeration by status, body, size, cache behavior, and timing distribution.
- Test XSS, CSRF, clickjacking, session fixation, replay, parallel attempts,
  malformed credentials, IDOR, and rate-limit bypass.
- Review PostgreSQL, Redis, proxy, cookie, TLS, backup, and key permissions.
- Perform a final security review before participant recruitment.

## References

- [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP Bot and Anti-Automation guidance](https://cheatsheetseries.owasp.org/cheatsheets/Bot_Management_and_Anti-Automation_Cheat_Sheet.html)
