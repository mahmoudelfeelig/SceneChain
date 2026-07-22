# ASVS 5.0 Level 2 evidence map

Status: implementation evidence map; independent assessment still required.

| Area | Implemented control | Evidence | Remaining verification |
|---|---|---|---|
| Encoding and validation | Fixed-width protocol, version checks, bounded DTOs, exact cell/action ranges | `Protocol.java`, controller validation, protocol tests | Fuzz malformed JSON and oversized requests |
| Authentication | Final-only comparison, stable unknown-account cues, dummy Argon2 work, generic failures | `AuthController.java`, `SceneService.synthetic` | Statistical timing test across larger samples |
| Credential storage | Argon2id, random salts, HMAC pepper, account context, AEAD metadata | `CredentialCrypto.java`, crypto test | Production key-management review and rotation drill |
| Sessions | Opaque 256-bit tokens, server-side Redis records, expiry, rotation on login, logout deletion | `SessionStore.java`, `Cookies.java` | Parallel-device and expiry integration tests |
| Browser security | SameSite cookies, CSP, frame denial, no-store, Origin and Fetch Metadata checks | Nginx config, `SecurityConfig.java`, `SameOriginFilter.java` | TLS deployment header scan |
| Abuse controls | Atomic Redis sliding windows for account and network buckets | `RateLimiter.java` | Distributed capacity test and trusted-proxy configuration |
| Data protection | Authentication inputs omitted from application logs; no third-party runtime assets | logging config, self-hosted frontend | Proxy/crash-report audit in target deployment |
| Supply chain | Exact frontend lockfile, clean high/critical npm audit, pinned container bases | `package-lock.json`, Dockerfiles, CI | Add backend/container SBOM retention and review |
| Scene integrity | Explicit approval state, 48-scene/family/license/cell validation, immutable hashes | finalizer, reviewer, `SceneService` loader | Complete source downloads and independent crop/cell review |

The evidence map does not assert ASVS certification. A reviewer must execute the
remaining checks against the deployed system and record results, versions, and
dates.
