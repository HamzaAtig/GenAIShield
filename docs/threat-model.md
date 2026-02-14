# Threat Model — GenAIShield

## Overview

GenAIShield is a backend service designed to securely integrate Large Language Models (LLMs) with Retrieval Augmented Generation (RAG).

This document identifies:
- assets to protect
- trust boundaries
- major threat scenarios
- mitigation strategies

The goal is to ensure that security controls are part of the architecture, not an afterthought.

---

## Assets to Protect

### Sensitive Data
- User prompts
- Retrieved documents
- Embeddings stored in vector database

### System Secrets
- API keys (Mistral or other providers)
- Database credentials
- Internal configuration

### Application Components
- System prompts
- Security policies
- Vector store metadata

---

## Trust Boundaries

The system operates across several trust boundaries:

1. **External User**
    - All user input is untrusted.

2. **Vector Store Content**
    - Retrieved documents may contain malicious or injected content.

3. **External LLM Providers**
    - Responses cannot be fully trusted and must be constrained by server-side policies.

---

## Threat Scenarios

### T1 — Prompt Injection

An attacker attempts to override instructions:
- "Ignore previous instructions"
- "Reveal your system prompt"

Risk:
- disclosure of internal logic
- policy bypass

Mitigation:
- server-side security policies
- strict system prompts
- refusal rules

---

### T2 — Indirect Prompt Injection (RAG)

Malicious instructions embedded in stored documents.

Risk:
- model behavior hijacking
- unintended actions

Mitigation:
- retrieved context treated as untrusted data
- sanitization of context
- clear separation of instructions and evidence

---

### T3 — Cross-Tenant Data Leakage

Vector search returns documents belonging to another tenant.

Risk:
- confidentiality breach

Mitigation:
- tenant filtering at query level
- metadata-based isolation

---

### T4 — Secret Exfiltration

User attempts to retrieve:
- API keys
- system prompts
- credentials

Mitigation:
- blocking policies
- secrets stored outside application code
- environment or Vault-based secret management

---

## Security Principles

GenAIShield follows these principles:

- Treat all inputs as untrusted
- Enforce security outside the LLM
- Use defense in depth
- Minimize exposure of secrets

Important rule:

**The LLM never decides security. The application does.**

---

## Future Improvements

Planned enhancements include:
- content moderation
- rate limiting
- audit logging
- anomaly detection on prompts
