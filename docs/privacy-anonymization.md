# Privacy Anonymization Layer

This project now includes a server-side anonymization layer before LLM usage.

## Goals

- Reduce PII/secrets leakage risk before model calls.
- Keep a deterministic and auditable policy path.
- Avoid relying only on prompt instructions for privacy.

## Design

Three mechanisms are combined:

1. Annotation-based protection on DTO fields.
2. Rule-based free-text sanitizer for unstructured text.
3. Prompt rule reminding the model not to infer hidden values.
4. Request-scoped token mapping for transparent restore on response.

## Components

- Annotation: `@Sensitive`
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/privacy/Sensitive.java`
- Supported types: `SensitiveType`
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/privacy/SensitiveType.java`
- Supported actions: `SensitiveAction`
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/privacy/SensitiveAction.java`
- Sanitizer implementation:
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/privacy/SensitiveDataSanitizer.java`

## Where it is applied

- Chat request:
  - `ChatController` sanitizes annotated fields before building `ChatCommand`.
  - sensitive fragments are replaced by request tokens (e.g. `[[GS_EMAIL_1]]`).
  - after LLM response, tokens are restored before sending API response.
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/controller/ChatController.java`
- Ingest request:
  - `IngestController` sanitizes `attributes` before calling ingest use case.
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/java/org/hat/genaishield/api/controller/IngestController.java`
- Prompt behavior:
  - `chat.system.txt` includes explicit rule for anonymized placeholders.
  - File: `/Users/olfaallani/git/GenAIShield/modules/app-api/src/main/resources/prompts/chat.system.txt`

## Current DTO coverage

DTOs in API module:
- `ChatRequest`
- `ChatResponse`
- `IngestResponse`

Annotated field today:
- `ChatRequest.question` as `FREE_TEXT + MASK`

Notes:
- `documentId` is intentionally not anonymized to keep retrieval restriction semantics intact.
- `ChatResponse` is output, not input. Output scanning can be added as a next layer.

## Action semantics

- `REDACT`: replace with `[TYPE_REDACTED]`
- `MASK`: partial masking preserving minimal shape
- `PSEUDONYMIZE`: deterministic hash-based placeholder (`[TYPE_xxxxxxxx]`)

## Free-text patterns sanitized

- email -> `[EMAIL]`
- phone -> `[PHONE]`
- IBAN -> `[IBAN]`
- payment card -> `[CARD_PAN]`
- secret-like key-value (`apiKey`, `token`, `password`, `secret`) -> `[SECRET]`

Note:
- internal request/response path uses request-scoped tokens `[[GS_TYPE_N]]` for reversible anonymization.
- generic placeholders above still apply for non-contextual sanitization paths.

## Limits and next steps

Current layer is rule-based and conservative:
- false positives are possible on number-like text
- obfuscated content (`k1ll`, `b0mb`) may bypass simple regex scoring
- does not yet enforce output-side DLP blocking
- does not yet keep reversible secure mapping for pseudonyms

Recommended next steps:
1. Add output scanning before returning response.
2. Add configurable field-policy registry per endpoint/domain.
3. Add red-team tests with realistic PII corpora.
