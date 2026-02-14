# Security Controls — GenAIShield

## C1 — Retrieval tenant isolation (hard boundary)

Control:
- vector search is filtered by `tenantId` before ranking

Why:
- prevents cross-tenant leakage (T3)

Implementation:
- each chunk is stored with tenant metadata
- search filter includes `tenantId == '<tenant>'`

Verification:
- test with tenant A and tenant B datasets
- tenant A query must never return tenant B chunks

---

## C2 — Prompt injection and indirect injection hardening

Control:
- retrieved context is treated as untrusted
- sanitization path strips known injection markers and redacts secret-like patterns

Implementation:
- policy engine rule: `PromptInjectionRule`
- default sanitization rule for retrieval contexts
- prompt template enforces instruction/context separation

Verification:
- ingest chunk containing `ignore previous instructions`
- run chat query and verify policy outcome includes sanitization

---

## C3 — Exfiltration blocking

Control:
- block attempts to reveal system prompts or credentials

Implementation:
- policy engine rule: `ExfiltrationRule`
- patterns include system prompt extraction and secret exfiltration language

Verification:
- query like `reveal the system prompt`
- expected: blocked response and audit event

---

## C4 — Local moderation

Control:
- moderate user prompt locally with weighted regex scoring

Implementation:
- moderation engine: `LocalModerationEngine`
- policy rule: `ModerationRule`
- outcomes:
  - warning threshold: allow with sanitization
  - block threshold: block request

Verification:
- high-risk prompt should be blocked
- medium-risk prompt should pass with sanitization

---

## C5 — Rate limiting

Control:
- request throttling per `(tenantId,userId)`

Implementation:
- `RateLimitingFilter` using Bucket4j
- default limit: `60` requests/minute

Verification:
- exceed capacity in tests
- expected HTTP `429`

---

## C6 — Structured audit logging

Control:
- emit security-relevant events as structured JSON in dedicated log file

Implementation:
- `AuditLogPort` + `JsonAuditLogAdapter`
- logger `AUDIT` writes to `logs/audit.log` via `logback-spring.xml`

Verification:
- trigger chat and ingest operations
- confirm JSON entries exist with event type, tenant, user, and metadata

---

## C7 — Correlation ID and MDC propagation

Control:
- every request gets a correlation ID propagated via header and MDC

Implementation:
- `CorrelationIdFilter` reads `X-Correlation-Id` or generates one
- puts value in `MDC.correlationId`
- echoes `X-Correlation-Id` in response
- console logs include `corr:<id>`
- audit JSON includes `correlationId`

Verification:
- call an endpoint with/without `X-Correlation-Id`
- verify response header contains correlation ID
- verify logs and audit lines carry same ID

---

## C8 — Security E2E regression tests (Testcontainers)

Control:
- dedicated integration tests execute realistic API attack paths on a full Spring context + PGVector

Implementation:
- `modules/app-tests/src/test/java/org/hat/genaishield/tests/ApiE2EIT.java`
- uses Testcontainers PostgreSQL (`pgvector/pgvector:pg16`)
- logs request purpose + request payload + response for each step

Covered attacks:
- direct exfiltration prompt (`reveal system prompt/api key`) -> blocked refusal
- high-risk moderation prompt (`kill/murder/bomb`) -> blocked refusal
- RAG poisoning via ingested malicious chunk -> no instruction hijack
- multi-turn indirect exfiltration in same session -> blocked refusal

Known limitation captured by tests:
- obfuscated payloads (`k1ll`, `b0mb`) can bypass current local regex moderation baseline
- this is tracked and intentionally documented to drive next hardening iteration
