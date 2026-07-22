# Protocol-to-implementation conformance

This matrix is a release artifact. “Automated” identifies repeatable evidence;
“external” remains a recruitment gate rather than an implementation claim.

| Protocol requirement | Implementation owner | Evidence | Status |
|---|---|---|---|
| Five stages, 24×16 grid, four actions, exact matching | `Protocol.java`, `protocol.ts` | Java and TypeScript shared-vector tests | Implemented |
| All 384 cells selectable; no visible recommendations | `SceneBoard.tsx`, `SceneService.java` | Playwright class/keyboard assertions; pack validator | Implemented |
| Initial chain plus two server-enforced confirmations | `EnrollmentController`, `AttemptStore` | Formal integration smoke test | Implemented |
| Versioned consent and two-question understanding gate | Consent UI, `EnrollmentController`, `AccountRepository` | UI and request validation; timestamp persisted | Implemented |
| 48-scene full-pool recognition and five distinct families | `SceneService`, `AuthController` | Pack validator and smoke test | Implemented |
| Pinned manifest and canonical/delivery/thumbnail hashes | `SceneService`, manifest | Validator, startup status, exact SHA-256 | Implemented |
| Direct and fresh balanced shielded presentations | `MarkerOverlay`, auth flow | Marker unit tests and integration smoke | Implemented |
| One-observation shielded claim only | Protocol and participant copy | Documentation review | Implemented |
| Six counterbalanced server-assigned sequences | `StudySessionRepository` | Migration constraints and integration transition | Implemented |
| Two successful practice and three measured trials per condition | `StudySessionRepository` | State-transition tests/integration | Implemented |
| Six-item raw NASA-TLX after every condition | `StudyController`, `Workload` | Database constraints and UI form | Implemented |
| Seven-day retention across all three conditions | `StudySessionRepository` | State and due-time constraints | Implemented |
| Desktop/laptop viewport at least 1024×600 | `StudyController`, `StudyFlow` | DTO validation and resize-pause UI | Implemented |
| Server-authoritative 180-second attempt timing | `AuthController`, `AttemptStore` | Integration flow and timeout code review | Implemented |
| Password measured through the same server attempt boundary | Password attempt endpoints | Integration smoke branch | Implemented |
| Final-only verification and atomic attempt consumption | Auth controllers, Redis `GETDEL` | Negative replay test | Implemented |
| Absolute one-hour sessions and disabled-account check | `SessionStore`, `/me` | Unit/build and code review | Implemented |
| Separate study-subject IDs and cascading deletion | Migration V3, repositories | Fresh Flyway migration and deletion endpoint | Implemented |
| Scheduled earlier-of retention rule | `RetentionService` | Unit/build; operational dates required | Implemented; configuration pending |
| Bounded, audited, separately pseudonymised export | Export controllers, `ResearchAdminAccess` | Negative test plus authenticated local export and audit readback | Implemented |
| Formal recruitment gate with protocol/pack hashes and written references | `release_gate`, `ReleaseGateRepository` | Closed-gate test and labelled local-only smoke record | Implemented; external approvals pending |
| Roving keyboard grid, selected state, focus restoration, radio semantics | React components | Playwright smoke and screenshot review | Implemented |
| Lazy hashed thumbnails and immutable WebP caching | Manifest, Nginx, gallery | Header check and benchmark | Implemented |
| One-view observer procedure and constrained outcome collection | Runbook, outcome controller, `observer_outcomes` | DTO/database constraints and authenticated readback | Implemented; task is researcher-run |
| Lockout, accessibility, and recovery collection | Runbook, outcome controller, constrained tables | DTO/database constraints and authenticated readback | Implemented; tasks are researcher-run |
| Controller, investigator, DPO, host, legal basis, compensation, duration | Participant documents | Institutional completion | External; recruitment blocked |
| Ethics and institutional data-protection decisions | Release gate | Written decisions and references | External; not obtained |

## Frozen identifiers

- Protocol SHA-256: `4c8b6fcccad045a4916f72a0116a01da9dfe60b63fde88c0b7b6459c938bd4d6`
- Scene manifest SHA-256: `99bc78510a377e6b7712cd120a76df844a9cb311616f682aa6046047e5bdfb58`
- Consent identifier: `scenechain-consent-2026-07-22`

If `protocol.md` changes, recompute its digest, update this matrix, update the
deployment secret, obtain any necessary amendment, and record the same digest in
the database release gate. A digest mismatch keeps recruitment closed.
