# SceneChain study plan and preregistration draft

Status: ready for institutional review; pilot assumptions, registration
identifier, and written approvals remain pending.

## Research design

SceneChain compares a study-only password with direct and shielded graphical
authentication in a within-participant crossover. The six counterbalanced
orders are `PDS`, `PSD`, `DPS`, `DSP`, `SPD`, and `SDP`. The server assigns the
least-used sequence with cryptographically random tie-breaking.

Each condition contains two successful practice logins, three measured
immediate trials, raw NASA-TLX ratings, and one delayed trial seven days later.
The confirmatory study is limited to consenting adults using a desktop or
laptop viewport of at least 1024 by 600 CSS pixels. Pilot participants are not
included in the confirmatory analysis.

All 384 cells are selectable. Participants are not shown recommended
locations. A graphical credential uses five scenes from distinct families and
one of four cardinal directions per scene. Recognition uses the complete public
48-scene pool. Shielded mode is evaluated against one opportunistic live
observation and is not claimed to withstand repeated recordings.

## Outcomes and hypothesis

The primary estimand is the ratio of direct to password geometric-mean
completion time across each participant's immediate measured trials. Time
starts when the server issues an attempt and ends on atomic completion or at
180 seconds. Every issued trial contributes; failures and timeouts receive the
180-second ceiling.

Direct SceneChain is non-inferior when the upper bound of the two-sided 95%
confidence interval for the direct/password ratio is below 1.25. The primary
one-sided alpha is 0.025. The model uses log time with condition, period,
sequence, and trial as fixed effects and participant as a random intercept.
The direct-versus-password contrast is the only primary contrast. Superiority
is considered only if non-inferiority succeeds and the confidence interval is
entirely below 1.00.

Secondary outcomes include first-attempt success, attempts required for two
practice successes, shielded-versus-direct completion time, workload, delayed
retention, enrollment time, recovery, lockout behavior, accessibility barriers,
one-observation complete-chain success, and marginal cell/action
concentration. Holm correction applies separately to usability, retention, and
security outcome families. Estimates and 95% confidence intervals are reported
regardless of significance.

## Sample size

The planning approximation aggregates each participant's three trials per
condition into a paired direct-minus-password log-time difference. With a
standard deviation of 0.50, a non-inferiority margin of `log(1.25) = 0.2231`,
one-sided alpha 0.025, and 80% power:

```text
n = ((z(0.975) + z(0.80)) * 0.50 / log(1.25))^2
  = ((1.960 + 0.842) * 0.50 / 0.2231)^2
  = 39.4 paired participants
```

The target is 72 analyzable paired participants, which divides evenly across
the six orders and provides a conservative allowance for model
misspecification, capped outcomes, and incomplete trials. Recruitment may
include up to 90 adults to allow approximately 20% loss.

The 0.50 standard-deviation assumption, the 1.25 margin, and the expected
session duration must be checked in an approved pilot before registration. If
the pilot contradicts the assumptions, this plan and the target sample must be
revised and registered before confirmatory recruitment.

## Missingness, exclusions, and stopping

Exclude a participant only for withdrawal, confirmed ineligibility, pilot or
duplicate participation, or a system failure that makes the outcome
unknowable. Do not exclude legitimate failed, timed-out, slow, outlying,
accommodated, or accessibility-affected trials. No outcome values are imputed.

Recruitment stops after at least 72 analyzable participants complete and the
approved target has been reached, or when 90 participants have enrolled. There
is no significance-based interim stopping. Privacy, safety, or critical
integrity incidents may pause the study.

## Data and reporting

The server owns sequence, period, phase, trial, and transition state. Research
rows use random study-subject identifiers and allowlisted fields. Secrets, raw
clicks, scene choices, actions, markers, passwords, recordings, research IP
addresses, full user-agent strings, and third-party telemetry are excluded from
the research export.

Report assignment counts, attrition, exclusions, deviations, failures,
distribution plots, medians, model estimates, confidence intervals,
preregistered outcomes, and adverse events. Do not claim phishing resistance,
multi-factor authentication, repeated-recording resistance, or universal
accessibility.

## Registration and release

The final version must be registered before confirmatory recruitment. Record
the registration identifier in the participant materials and release gate.
Recruitment remains disabled until the pilot, institutional fields, participant
materials, written ethics and data-protection decisions, and technical
verification are complete.
