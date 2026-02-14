# Getting Started — GenAIShield

This guide is a full setup path for a new machine.

## 1) Prerequisites

Required:
- Java 21
- Docker Desktop
- `curl`

Optional:
- Ollama (if you want local model usage)

Note:
- Maven install is not required. Use `./mvnw`.

## 2) Install Java 21 (macOS)

```bash
brew install openjdk@21
```

Use Java 21 in current shell:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Persist in shell profile:

```bash
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@21' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

## 3) Install and start Docker Desktop

Install:

```bash
brew install --cask docker
```

Start app:

```bash
open -a Docker
```

Wait until Docker is fully started, then verify:

```bash
docker --version
docker compose version
docker ps
```

## 4) Configure provider environment

For Mistral mode (recommended default):

```bash
export MISTRAL_API_KEY="YOUR_KEY"
export GENAISHIELD_PRIMARY_EMBEDDING=MISTRAL
export MISTRAL_CHAT_ENABLED=true
export MISTRAL_EMBEDDING_ENABLED=true
export OLLAMA_CHAT_ENABLED=false
export OLLAMA_EMBEDDING_ENABLED=false
```

For Ollama mode:

```bash
export GENAISHIELD_PRIMARY_EMBEDDING=OLLAMA
export OLLAMA_CHAT_ENABLED=true
export OLLAMA_EMBEDDING_ENABLED=true
```

## 5) Start project dependencies

From repo root:

```bash
docker compose -f docker/docker-compose.yml up -d
docker ps
```

## 6) Build and run API

From repo root:

```bash
./mvnw -pl modules/app-api -am install -DskipTests
./mvnw spring-boot:run
```

API URL:
- `http://localhost:8080`

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## 7) Smoke tests

Chat:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: demo" \
  -H "X-User-Id: demo-user" \
  -H "X-Roles: USER" \
  -d '{"sessionId":"s1","provider":"MISTRAL","question":"What is GenAIShield?"}'
```

Ingest:

```bash
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "X-Tenant-Id: demo" \
  -H "X-User-Id: demo-user" \
  -H "X-Roles: USER" \
  -F "file=@/tmp/sample.txt;type=text/plain" \
  -F "provider=MISTRAL" \
  -F "classification=INTERNAL" \
  -F "allowedRoles=USER,ANALYST" \
  -F 'attributes={"source":"manual-upload"}'
```

## 8) IntelliJ run configuration

Use a Spring Boot run config on `GenAIShieldApplication`.

Set:
- JRE: Java 21
- Module classpath: `app-api`
- Environment variables:
  - `MISTRAL_API_KEY=...`
  - `GENAISHIELD_PRIMARY_EMBEDDING=MISTRAL`
  - `MISTRAL_CHAT_ENABLED=true`
  - `MISTRAL_EMBEDDING_ENABLED=true`
  - `OLLAMA_CHAT_ENABLED=false`
  - `OLLAMA_EMBEDDING_ENABLED=false`

Important:
- Do not put these values in `VM options`.
- Do not put these values in `Program arguments`.
- Do not separate with `;` inside VM options. Use dedicated environment variables.

## 9) Tests

Run all tests:

```bash
./mvnw test
```

Run API tests only:

```bash
./mvnw -f modules/app-api/pom.xml test
```

Run core tests only:

```bash
./mvnw -f modules/app-core/pom.xml test
```

Run end-to-end tests (Docker required):

```bash
./mvnw -pl modules/app-tests -am -Dtest=ApiE2EIT test
```

Notes:
- E2E tests use Testcontainers (`pgvector/pgvector:pg16`).
- Tests log request purpose, request body, and response status/body in console.
- `-DskipTests=true` skips execution only; tests still compile.
- Use `-Dmaven.test.skip=true` to skip test compile + execution.

## 10) Troubleshooting

- `release version 21 not supported`
  - Java is not 21. Fix `JAVA_HOME` and IntelliJ JRE.

- `Unable to find a suitable main class`
  - Start from API module (`./mvnw spring-boot:run` is already wrapper-fixed).

- `Mistral API key must be set`
  - `MISTRAL_API_KEY` is missing in the process environment.

- `ClassNotFoundException: MISTRAL_API_KEY=...`
  - Variables were passed as Java args, not environment vars.

- `No qualifying bean of type EmbeddingModel ... found 2`
  - Set `GENAISHIELD_PRIMARY_EMBEDDING` (`MISTRAL` or `OLLAMA`).

- `more than one 'primary' bean found among candidates`
  - In tests, disable provider model auto-configs or ensure a single primary `EmbeddingModel`.

- `Failed to obtain JDBC Connection`
  - PostgreSQL/PGVector is not running. Start Docker and compose stack.

- `Mapped port can only be obtained after the container is started`
  - Testcontainers datasource was read too early. Ensure container starts before resolving dynamic properties.

- `distance-type ... No enum constant ... cosine`
  - Use enum names only (already fixed in repo config).

## 11) Related docs

- IntelliJ setup: `docs/intellij-setup.md`
- Extended curl tests: `docs/curl-test-cases.md`
- Privacy anonymization: `docs/privacy-anonymization.md`
