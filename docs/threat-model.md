# Threat Model — GenAIShield

## Overview

GenAIShield is a backend service to integrate LLMs with RAG while enforcing server-side security controls.

This document describes:
- key assets
- trust boundaries
- major threat scenarios
- implemented mitigations

## Assets to protect

Sensitive data:
- user prompts
- retrieved documents/chunks
- embeddings

Secrets:
- provider API keys
- DB credentials
- runtime internal configuration

Security artifacts:
- prompt templates
- policy rules
- vector metadata and tenant boundaries

## Trust boundaries

1. External user input
- always untrusted

2. Retrieved vector content
- may include malicious instructions

3. External LLM providers
- model output is advisory only; server enforces policy

## Threat scenarios and mitigations

### T1 — Prompt injection

Threat:
- user tries to override instructions (`ignore previous instructions`)

Mitigations:
- policy engine with injection-focused rules
- sanitization before generation
- strict system prompt constraints

### T2 — Indirect prompt injection via RAG

Threat:
- malicious instructions inside stored chunks

Mitigations:
- context treated as untrusted
- explicit sanitization and prompt firewall

### T3 — Cross-tenant data leakage

Threat:
- retrieval returns chunks from another tenant

Mitigations:
- tenant filter at vector query level
- tenant metadata persisted per chunk

### T4 — Secret/system prompt exfiltration

Threat:
- requests to reveal secrets, credentials, or system prompts

Mitigations:
- explicit exfiltration blocking rules
- refusal on blocked decisions
- audit logs for policy decisions

### T5 — Abuse and unsafe content generation

Threat:
- unsafe or abusive prompts intended to misuse the system

Mitigations:
- local moderation engine (weighted regex scoring)
- threshold-based policy outcomes (sanitize or block)

### T6 — API flooding / resource exhaustion

Threat:
- repeated calls to degrade service or increase cost

Mitigations:
- per-user/per-tenant rate limiting
- deterministic `429` responses when exceeded

## Security principles

- Treat all inputs as untrusted
- Enforce security in application layer, not in model behavior
- Use defense in depth
- Minimize secret exposure and sensitive logging

Key rule:
- The application decides security outcomes. The LLM does not.

## Residual risks and next steps

- No external antivirus engine yet (current adapter is no-op)
- Moderation is local heuristic, not ML classifier-based
- Add integration tests for full ingest-to-chat threat paths
- Add anomaly detection and alerting on audit log streams
