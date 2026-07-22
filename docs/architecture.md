# Application architecture

Status: selected pre-implementation architecture.

## Stack

- React, TypeScript, and Vite for the browser application;
- Java 21, Spring Boot, and Spring Security for the API;
- PostgreSQL for durable accounts, credentials, policies, and research records;
- Redis for short-lived attempts and rate-limit state;
- versioned local static scene assets initially;
- Docker Compose for the reproducible local environment;
- JUnit and Testcontainers for backend integration tests;
- Vitest and Testing Library for frontend unit tests;
- Playwright for supported-viewport and end-to-end tests.

Frontend and API are exposed through one origin in deployed and test environments.
The reverse proxy serves immutable scene assets and forwards `/api` to Spring.

## Repository shape

```text
frontend/src/
  auth/
  enrollment/
  experiment/
  scene-engine/
  accessibility/

backend/src/main/java/.../
  protocol/
  credentials/
  enrollment/
  authentication/
  attempts/
  ratelimit/
  research/
  security/

protocol/
  schemas/
  manifests/
  test-vectors/
```

The backend protocol package is independent of HTTP and persistence. It owns
binary credential encoding, quantization, manifest validation, challenge
generation, and verification. TypeScript implements only presentation and local
feedback. Shared published test vectors prevent Java/TypeScript drift.

## Durable data

PostgreSQL contains accounts, graphical credential verifiers, encrypted graphical
metadata, password verifiers, scene-manifest versions, versioned consent records,
study subjects, crossover state, allowlisted study events, workload responses,
release-gate records, export audit events, and aggregate hotspot counters.
Production migration and runtime accounts use separate database roles.

## Ephemeral data

Redis contains attempt state, per-account throttles, per-network throttles, and
server-wide verification capacity counters. Redis is not a credential database.
Persistence is disabled for secret-bearing temporary state, network access is
restricted, and keys have explicit TTLs.

## Key separation

At minimum, independent keys exist for verifier HMAC, metadata encryption,
synthetic unknown-account cues, keyed account identifiers, and research subject
pseudonyms. Development keys may come from local environment files excluded from
version control. Deployment keys come from a managed secret mechanism.

## Deployment boundary

The initial deployment is one frontend/reverse-proxy service, one API service,
PostgreSQL, and Redis. Administrative exports require separate credentials,
per-request reauthentication, HTTPS, bounds, and audit; participant credentials
cannot authorize them. The application does not require third-party image,
font, analytics, or authentication services.
