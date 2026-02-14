# IntelliJ Setup (No Screenshots)

This page describes a stable IntelliJ configuration for running GenAIShield locally.

## 1) Prerequisites

- Project opened from: `/Users/olfaallani/git/GenAIShield`
- Java 21 installed
- Docker Desktop running

## 2) Project SDK and Maven SDK

Set both to Java 21:

1. `File > Project Structure > Project`
- `Project SDK`: Java 21
- `Project language level`: 21

2. `Settings > Build, Execution, Deployment > Build Tools > Maven`
- `JDK for importer`: Java 21
- `Runner > JRE`: Java 21

3. Reload Maven projects.

## 3) Spring Boot Run Configuration

Create or edit a Spring Boot config for:
- Main class: `org.hat.genaishield.api.GenAIShieldApplication`
- Module classpath: `app-api`
- JRE: Java 21

Environment variables:

```text
MISTRAL_API_KEY=YOUR_KEY
GENAISHIELD_PRIMARY_EMBEDDING=MISTRAL
MISTRAL_CHAT_ENABLED=true
MISTRAL_EMBEDDING_ENABLED=true
OLLAMA_CHAT_ENABLED=false
OLLAMA_EMBEDDING_ENABLED=false
```

Important:
- Put variables in `Environment variables` only.
- Keep `VM options` for JVM flags only.
- Keep `Program arguments` empty unless explicitly needed.
- Do not prefix them before `org.hat.genaishield.api.GenAIShieldApplication` in command line.
- Do not put them as one `;`-separated string in VM options.

## 4) Start Dependencies

From project root:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Verify:

```bash
docker ps
```

## 5) Build and Run

From project root:

```bash
./mvnw -pl modules/app-api -am install -DskipTests
./mvnw spring-boot:run
```

Or run directly from IntelliJ using the Spring Boot configuration.

## 6) Quick Verification

```bash
curl http://localhost:8080/actuator/health
```

Expected:
- HTTP 200
- JSON status `UP`

## 7) Frequent IntelliJ Mistakes

- Using Java 25 in Run Config
- Putting `MISTRAL_API_KEY=...` in VM options
- Putting all variables in one VM-options line (causes `ClassNotFoundException`)
- Launching app while Docker Desktop is stopped
- Not reloading Maven after changing project configuration

## 8) Run E2E Integration Test in IntelliJ

- Maven goal:
  - `-pl modules/app-tests -am -Dtest=ApiE2EIT test`
- Requirements:
  - Docker running
  - Java 21 for Maven runner
- What this test validates:
  - health, ingest, chat, rate-limit
  - policy attacks (exfiltration, moderation, RAG poisoning)
  - request/response logs per test step in console
