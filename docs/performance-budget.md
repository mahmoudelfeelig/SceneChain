# SceneChain performance budgets

These are release budgets rather than claims about every deployment.

The 2026-07-22 local baseline uses 48 immutable 480-by-320 thumbnails totaling
1,014,532 bytes. Their estimated decoded RGBA footprint is 29,491,200 bytes; one
selected 1920-by-1280 scene raises the estimate to 39,321,600 bytes. This replaces
the previous full-gallery estimate of roughly 472 MiB. Warm Docker-to-host bridge
p95 was 9.17 ms for the landing document and 17.16 ms for pack status across 50 samples.
These are engineering checks, not production-network claims; see
`benchmark-results.json`.

| Measure | Budget |
|---|---:|
| Landing HTML/API p95 on deployment host | 250 ms |
| Pack-status API p95 on deployment host | 250 ms |
| Authentication attempt creation p95 excluding network | 500 ms |
| Argon2id verification p95 on deployment host | 1,000 ms |
| Complete 48-thumbnail gallery transfer, cold cache | 1.5 MB |
| Estimated decoded gallery plus one selected scene | 48 MB |
| Median canonical WebP | 300 KB |
| Redis attempt/session operation p95 | 20 ms |
| PostgreSQL enrollment transaction p95 | 500 ms |
| Largest Contentful Paint on target study laptop | 2.5 s |
| Cumulative Layout Shift | 0.1 |

Run `python3 scripts/benchmark.py` against the deployed stack and save its JSON output with the release evidence. Browser render budgets require the Playwright audit on the standardized study device. A failed budget blocks recruitment until investigated or explicitly amended before outcome collection.
