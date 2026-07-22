# Final UI and integration review

The screenshots in this directory were captured by `scripts/ui_smoke.mjs` against
the live Docker Compose stack on 2026-07-22. The test covered 1440×1000,
1024×768, and 390×844 information-page layouts; verified that recruitment was
closed; checked horizontal overflow; opened and returned from privacy information;
confirmed exactly one tabbable grid cell; moved focus with an arrow key; and
confirmed that no recommended-cell class was rendered.

The phone screenshot covers public information only. Formal study tasks are
intentionally limited to desktop/laptop viewports of at least 1024×600.

## Release verification

The final pass also established the following repeatable evidence:

- 13 backend tests passed in the Java 21 Maven container;
- three frontend shared-vector/coordinate tests, ESLint, TypeScript, and the
  production Vite build passed in the Node 24 container;
- Flyway V1 through V5 applied on a clean PostgreSQL 17 database;
- the frozen 48-scene pack and every canonical, delivery, and thumbnail digest
  passed validation;
- formal enrollment, two confirmations, assigned-condition authentication, and
  the first practice transition passed end to end;
- cross-origin, malformed-input, unknown-account, replay, and unauthorized
  primary/secondary export tests passed;
- authenticated primary export, observer, lockout, participant-report, secondary
  export, and audit readback passed with explicitly labelled local-only data;
- the final release gate was restored to closed with no protocol or ethics
  placeholder recorded;
- 50 warm Docker-to-host samples measured 9.17 ms landing p95 and 17.16 ms
  pack-status p95, within the declared local engineering budgets.

The local-only release row used for positive-flow verification was labelled
`LOCAL-SMOKE-NOT-AN-APPROVAL` and removed afterward. It is not an ethics or
data-protection decision.
