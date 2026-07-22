# SceneChain a priori power analysis

The primary hypothesis is non-inferiority of direct SceneChain to manual password entry for capped log completion time. The non-inferiority margin is a direct/password geometric-mean ratio of 1.25, equivalent to `log(1.25) = 0.2231`. The one-sided alpha is 0.025.

For the planning approximation, each participant contributes a paired direct-minus-password log-time difference after aggregating the three trials per condition. Assuming a standard deviation of paired log differences of 0.50, 80% power requires approximately:

```text
n = ((z(0.975) + z(0.80)) * 0.50 / log(1.25))^2
  = ((1.960 + 0.842) * 0.50 / 0.2231)^2
  = 39.4 paired participants
```

The target of 72 analyzable paired participants is deliberately conservative for model misspecification, period/sequence adjustment, non-normal capped outcomes, and missing individual trials, and it divides evenly across six orders. Under the planning approximation it provides about 96.5% power. Recruiting up to 90 allows 20% loss while retaining 72.

The 0.50 standard-deviation assumption and 1.25 margin must be evaluated for plausibility in a separate pilot before registration. The margin represents the largest slowdown considered practically acceptable for the proposed usability claim; it was not selected from confirmatory outcomes. If the pilot contradicts either assumption, the analysis and target must be amended and re-registered before recruitment. Pilot participants are excluded from confirmation.

Secondary success, workload, retention, accessibility, lockout, and one-observation outcomes are estimation-focused and are not used to retrospectively justify the sample. Their uncertainty is reported with confidence intervals. No post-hoc power calculation will reinterpret a null result.
