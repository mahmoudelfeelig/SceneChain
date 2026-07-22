# SceneChain preregistration

Status: final text to register before recruitment; registration identifier pending.

## Design

This is a within-participant crossover comparing password, direct SceneChain, and shielded SceneChain in the six counterbalanced orders `PDS`, `PSD`, `DPS`, `DSP`, `SPD`, and `SDP`. The server assigns the least-used sequence with random tie-breaking. Each condition includes two successful practice logins, three measured immediate trials, raw NASA-TLX ratings, and one delayed trial seven days later. The confirmatory sample is adults using desktop/laptop viewports of at least 1024 by 600 CSS pixels.

## Primary estimand and hypothesis

The primary estimand is the ratio of direct to password geometric-mean completion time across each participant's three immediate measured trials. Time begins when the server issues an attempt and ends on atomic completion or at 180 seconds. Every issued trial contributes; failures and timeouts receive the 180-second ceiling.

Direct SceneChain is non-inferior when the upper bound of the two-sided 95% confidence interval for the direct/password ratio is below 1.25. The primary one-sided alpha is 0.025. The model uses log time with condition, period, sequence, and trial as fixed effects and participant as a random intercept. The direct-versus-password contrast is the only primary contrast. A sensitivity analysis uses a participant-level paired geometric mean and a paired confidence interval. Superiority is considered only if non-inferiority succeeds and the confidence interval is entirely below 1.00.

## Secondary outcomes

Secondary outcomes are first-attempt success; practice attempts to two successes; shielded versus direct time and raw NASA-TLX ratings; delayed-retention success and time; enrollment time; recovery and lockout behavior; reported accessibility barriers; observer complete-chain success after exactly one observation; and marginal cell/action concentration. Binary repeated outcomes use mixed-effects logistic models when estimable and paired exact methods otherwise. Holm correction applies separately to usability, retention, and security outcome families. Estimates and 95% confidence intervals are reported regardless of significance.

Observer allocation is balanced between direct and shielded conditions. Each
observer receives exactly one uninterrupted live view and one complete-chain
attempt; no recording is made. Lockout and participant-report results use the
closed fields in the researcher runbook and dedicated outcome tables. These
secondary analyses are estimation-focused and do not alter the primary sample
size or stopping rule.

## Missingness and exclusions

Exclude a participant only for withdrawal, confirmed ineligibility, pilot or duplicate participation, or a system failure that makes the outcome unknowable. The pre-consent understanding check gates enrollment and is not an outcome-based exclusion. Do not exclude legitimate failed, timed-out, slow, outlying, accommodated, or accessibility-affected trials. No outcome values are imputed. The primary paired analysis requires at least one issued direct and password measured trial; the mixed model uses every available issued trial. Reasons and counts are reported before outcome analysis.

## Sample and stopping

Recruit up to 90 adults to retain at least 72 analyzable paired participants. The approximation and assumptions are declared in `power-analysis.md`. Stop only when at least 72 analyzable participants have completed and the approved cap has been reached as needed, or when 90 participants have enrolled. There is no efficacy or significance-based interim analysis. A privacy, safety, or critical integrity incident may pause recruitment and will be reported.

## Data integrity and reporting

The analysis uses the frozen protocol and scene-pack identifiers. Report assignment counts, attrition, exclusions, deviations, system failures, distribution plots, medians, model estimates, confidence intervals, all preregistered outcomes, and adverse events. Do not infer effective entropy from marginal hotspots or claim phishing resistance, MFA, repeated-recording resistance, or universal accessibility.
