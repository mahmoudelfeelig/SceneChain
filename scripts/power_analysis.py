#!/usr/bin/env python3
"""Reproduce the frozen paired non-inferiority planning approximation."""

from math import ceil, log, sqrt
from statistics import NormalDist

ONE_SIDED_ALPHA = 0.025
TARGET_POWER = 0.80
NONINFERIORITY_RATIO = 1.25
PAIRED_LOG_SD = 0.50
RETAINED_N = 72
ATTRITION = 0.20

normal = NormalDist()
margin = log(NONINFERIORITY_RATIO)
z_alpha = normal.inv_cdf(1 - ONE_SIDED_ALPHA)
z_power = normal.inv_cdf(TARGET_POWER)
required = ((z_alpha + z_power) * PAIRED_LOG_SD / margin) ** 2
achieved = normal.cdf(sqrt(RETAINED_N) * margin / PAIRED_LOG_SD - z_alpha)
recruit = ceil(RETAINED_N / (1 - ATTRITION))
while recruit % 6:
    recruit += 1

print(f"one-sided alpha: {ONE_SIDED_ALPHA:.3f}")
print(f"non-inferiority ratio margin: {NONINFERIORITY_RATIO:.2f}")
print(f"log margin: {margin:.4f}")
print(f"assumed paired log-difference SD: {PAIRED_LOG_SD:.2f}")
print(f"normal-approximation minimum complete pairs: {required:.1f}")
print(f"frozen analyzable sample: {RETAINED_N}")
print(f"approximate power at N={RETAINED_N}: {achieved:.3f}")
print(f"recruitment target with {ATTRITION:.0%} attrition, rounded to six sequences: {recruit}")
