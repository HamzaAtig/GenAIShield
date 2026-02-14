# GenAIShield

GenAIShield is a security-by-design GenAI backend for regulated environments (banking, finance, enterprise systems).

## What it provides

- Multi-provider routing (Mistral, Ollama)
- RAG over PostgreSQL + PGVector
- Tenant-isolated retrieval boundaries
- Chat endpoint with citations
- Document ingestion endpoint (chunking + embeddings + vector upsert)
- Extensible server-side AI policy engine
- Local moderation (no external moderation model call)
- Rate limiting on API endpoints
- Structured JSON audit logging to dedicated files
- Correlation ID propagation (`X-Correlation-Id`) with MDC logging
- Privacy/anonymization layer on DTO inputs + free-text patterns
- End-to-end integration tests with Testcontainers (PGVector + attack scenarios)
- Hexagonal architecture (core decoupled from Spring AI and storage)

## Architecture

- `modules/app-core`: domain, ports, policies, use cases
- `modules/app-infra`: provider adapters, vector store adapter, infra wiring
- `modules/app-api`: REST controllers, DTOs, runtime configuration
- `modules/app-tests`: test module (reserved for broader integration/security tests)

Core rule: business logic depends on ports only, not on provider SDKs.

## Main APIs

- `POST /api/v1/chat`
- `POST /api/v1/ingest` (`multipart/form-data`, file upload)
- `GET /actuator/health`

## Security highlights

- Tenant filter enforced at vector query level
- Prompt-injection hardening (retrieved context treated as untrusted)
- Exfiltration blocking (system prompt/secrets extraction attempts)
- Local moderation scoring integrated in policy decisions
- Default sanitization path for retrieved context
- Request throttling with Bucket4j
- Audit trail as structured JSON logs (`logs/audit.log`)
- Correlation ID echo in responses + MDC enrichment in logs
- Annotation-driven and regex-based input anonymization

## Quickstart

### 1) Start PGVector

```bash
docker compose -f docker/docker-compose.yml up -d
```

### 2) Build modules

```bash
./mvnw -pl modules/app-api -am install -DskipTests
```

### 3) Run API

```bash
./mvnw spring-boot:run
```

### 4) Health check

```bash
curl http://localhost:8080/actuator/health
```

### 5) Provider mode (Mistral-only example)

```bash
export MISTRAL_API_KEY="YOUR_KEY"
export GENAISHIELD_PRIMARY_EMBEDDING=MISTRAL
export MISTRAL_CHAT_ENABLED=true
export MISTRAL_EMBEDDING_ENABLED=true
export OLLAMA_CHAT_ENABLED=false
export OLLAMA_EMBEDDING_ENABLED=false
```

Important:
- In IntelliJ, set these in `Environment variables` (not VM options).
- Use one variable per entry (`KEY=value`), not Java args.

### 6) E2E Tests (Testcontainers)

```bash
./mvnw -pl modules/app-tests -am -Dtest=ApiE2EIT test
```

Coverage includes:
- health and PGVector extension checks
- chat and ingest happy paths
- rate-limiting behavior (`429`)
- exfiltration and moderation attack attempts
- RAG poisoning scenario
- obfuscated payload baseline (documents current regex limitation)

## Documentation

- Getting Started: `docs/getting-started.md`
- IntelliJ Setup: `docs/intellij-setup.md`
- Curl Test Cases: `docs/curl-test-cases.md`
- Privacy Anonymization: `docs/privacy-anonymization.md`
- Security Controls: `docs/security-controls.md`
- Threat Model: `docs/threat-model.md`
