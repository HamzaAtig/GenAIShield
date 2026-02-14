# GenAIShield

GenAIShield is a **security-by-design GenAI backend** demonstrating production-grade patterns for integrating LLMs in regulated environments (banking, finance, enterprise systems).

- **Spring AI** integration with **multi-provider r**Hexagonal modules**outing** (Mistral + Ollama)
- **RAG** with **PGVector** (PostgreSQL) as vector store
- **AI security controls** against common GenAI attack vectors (prompt injection, indirect injection, tool abuse patterns)
- A **hexagonal architecture** (ports/adapters) to keep domain logic provider-agnostic

---

## Key features

### Multi-provider LLM routing
- Chat generation via **Mistral** or **Ollama**
- Provider selected per request (body/header), without leaking SDK details into core

### RAG with PGVector
- Vector store backed by PostgreSQL + pgvector
- Retrieval is **tenant-filtered** (hard boundary)
- Returns **citations** (documentId + chunkId + score)

### AI security-by-design
- Prompt injection & indirect prompt injection defenses
- Server-side policy enforcement (LLM suggests, server decides)
- Safe prompt templating (externalized templates)

### Clean architecture
- `app-core`: ports, policies, use cases (no Spring AI / no DB coupling)
- `app-infra`: adapters (Spring AI providers, PGVector)
- `app-api`: REST API + config + runtime
- `app-tests`: integration tests + red-team tests (to be added)

---

## Architecture and Design Principles

GenAIShield is designed following **Hexagonal Architecture (Ports & Adapters)** and **SOLID principles** to ensure maintainability, testability, and provider independence.

### Hexagonal Architecture

The system is structured in distinct layers:

- **app-core**
    - Domain models
    - Use cases
    - Ports (interfaces)
    - Security policies

- **app-infra**
    - Adapters for LLM providers (Mistral, Ollama)
    - Vector store integration (PGVector)
    - Prompt templates and configuration

- **app-api**
    - REST controllers
    - DTOs
    - Request validation
    - Error handling

This structure ensures that:
- business logic does not depend on frameworks
- infrastructure can be replaced without impacting core logic
- providers can be swapped easily

---

### SOLID Principles

The design follows key SOLID principles:

**Single Responsibility**
- Use cases, adapters, and policies each have a focused responsibility.

**Open/Closed**
- New providers or vector stores can be added without modifying core logic.

**Liskov Substitution**
- Providers implement common ports and can be substituted transparently.

**Interface Segregation**
- Ports are small and focused (GenerationPort, EmbeddingPort, VectorStorePort).

**Dependency Inversion**
- Core depends only on interfaces, never on concrete infrastructure.


### Project Structure

- `modules/app-core`: domain + ports + use cases + policies
- `modules/app-infra`: adapters for AI providers and vector store
- `modules/app-api`: REST controllers, DTOs, error handling
- `modules/app-tests`: Testcontainers + security test suite

**Core principle:** the domain never depends on Spring AI, only on ports.

---

## Quickstart

### 1) Start PGVector
```bash
docker compose -f docker/docker-compose.yml up -d
```


## Documentation
- Getting Started → docs/getting-started.md
- Threat Model → docs/threat-model.md
- Security Controls → docs/security-controls.md