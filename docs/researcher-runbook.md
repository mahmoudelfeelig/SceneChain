# SceneChain researcher runbook

This runbook is part of the frozen protocol package. It does not authorize
recruitment. Replace every bracketed institutional field, complete the pilot,
preregister the study, and obtain the written ethics and data-protection
decisions before using it with participants.

## Session setup

Use the standardized desktop or laptop, supported browser, viewport of at least
1024 by 600 CSS pixels, and the declared input device. Confirm that recruitment
is open, the formal 48-scene pack is reported, and the protocol and manifest
digests match the release record. Give the participant privacy for enrollment.
Never view, ask for, record, or photograph a graphical cell, action, marker, or
password. Record only the study handle on the approved case-report form.

Read the same instructions verbatim for every participant. Permit the approved
breaks and accommodations. Do not coach a participant toward any image cell.
The application controls sequence assignment, practice, measured trials,
workload, and delayed retention. Treat resizing below 1024 by 600 as a paused
task. Record a deviation before unblinding outcomes if the standardized setup
cannot be restored.

## One-observation task

Use a separate ephemeral target credential prepared for the session; never use
the observer's durable credential or another participant's credential. Assign
the observer to direct or shielded viewing using the preregistered balanced
allocation. Position the observer at the marked location and show exactly one
complete, uninterrupted, live authentication. Do not permit pause, replay,
notes, screenshots, audio, video, or screen recording.

Immediately provide exactly one complete-chain attempt against the ephemeral
target. Success means the server accepts the whole chain. Failure, timeout, or
abandonment is failure. Destroy the ephemeral target after entry. Enter only the
participant handle, assigned condition, complete-chain success, observation
count one, attempt count one, and recording-used false through the authenticated
administrator endpoint. A request with any other observation or attempt count,
or with recording enabled, is rejected.

## Lockout task

Use a separate scripted disposable target. Ask the participant to submit the
fixed incorrect attempt until the first `429` response. Stop immediately at the
first throttle; do not continue or target any real or participant credential.
Record attempts through the throttled request, the integer `Retry-After` value,
and whether the wait was communicated understandably. The data-entry endpoint
requires confirmation that the target was disposable.

## Accessibility and recovery report

After the immediate conditions, ask whether the interaction presented no
barrier, a primarily visual, motor, cognitive, multiple, or other barrier. Do
not collect free text in the application. Record whether the participant used
the alternative password as recovery. Accommodations and detailed notes, if
approved, remain on the institution's access-restricted case-report form and
are not entered into SceneChain.

## Controlled data entry and export

Use separate research-administrator credentials over HTTPS. Do not put the
password in shell history; the commands below show field structure only. The
participant handle is used transiently to resolve the random subject identifier
and is not stored in the outcome row or export.

```text
POST /api/admin/research/observer
{"handle":"SC-....-....","condition":"shielded","completeChainSuccess":false,"observationCount":1,"attemptCount":1,"recordingUsed":false}

POST /api/admin/research/lockout
{"handle":"SC-....-....","attemptsUntilThrottle":10,"retryAfterSeconds":60,"waitCommunicated":true,"disposableAccount":true}

POST /api/admin/research/participant-report
{"handle":"SC-....-....","accessibilityCode":"none","recoveryUsed":false}
```

Export primary authentication rows from `events.csv` and the three secondary
forms from `outcomes.csv`, always with explicit `from`, `to`, and `limit`
parameters. Every accepted, rejected, and exported administrator operation is
audited under a keyed actor pseudonym. Store exports only in the institutionally
approved encrypted location. Apply the minimum-count publication rule and the
retention/deletion schedule.

## End of session

Give the retention appointment and deletion instructions. Confirm that the
participant retained the handle without revealing any credential. Report
adverse events, privacy incidents, or integrity failures through the approved
institutional route and pause recruitment when required by the protocol.
