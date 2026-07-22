# Retention and deletion plan

## Schedule

- Live attempts and sessions: Redis TTL of at most one hour; attempt state at most five minutes.
- Operational logs: 14 days, with no handles, cells, actions, markers, or submitted credential material.
- Consent and pseudonymous study events: through analysis and peer review, then no later than six months after final publication or 24 months after collection closes, whichever comes first.
- Administrative exports: encrypted at rest, access logged, deleted on the same deadline.
- Aggregate results meeting the minimum disclosure threshold: retained as research output without a re-linking key.
- Audio/video: not collected by the implemented protocol. A separate approved schedule is mandatory if later introduced.

## Participant deletion

The investigator verifies the study handle, disables the account, deletes account-linked credentials, assignments, and study events in one controlled operation, records only the deletion date and reason category, and removes the handle from active export files. Backups expire through normal rotation within 30 days and are not restored except for disaster recovery.

## End-of-study deletion

Revoke export credentials, delete raw exports, delete account-linked database rows, destroy any re-linking material, rotate study keys, and retain only thresholded aggregates and reproducible analysis code. Two people record the deletion check where institutional policy permits.
