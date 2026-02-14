# Getting Started — GenAIShield

This guide explains how to:
1) set up prerequisites  
2) configure providers  
3) start local dependencies (PGVector)  
4) run the API  
5) test the system  

---

## 1) Prerequisites

### Required
- **Java 21 (JDK)**
- **Maven 3.9+**
- **Docker Desktop**

### Optional
- **Ollama** (for local LLM and embeddings)

### Recommended
- IntelliJ IDEA or VS Code

---

## 2) Configuration

GenAIShield uses environment variables to configure providers and database access.

You can either:
- set environment variables manually
- or use a `.env` file (recommended)

---

### Option A — Using environment variables

#### Windows (PowerShell)
```powershell
setx MISTRAL_API_KEY "YOUR_KEY"
````

Restart the terminal after running `setx`.

#### Linux / macOS

```bash
export MISTRAL_API_KEY="YOUR_KEY"
```

---

### Option B — Using a .env file (recommended)

Copy the example file:

```bash
cp .env.example .env
```

Edit the values:

```
MISTRAL_API_KEY=YOUR_KEY
OLLAMA_BASE_URL=http://localhost:11434
```

---

## 3) Optional: Using Ollama locally

Install Ollama and pull required models:

```bash
ollama pull llama3.1:8b
ollama pull nomic-embed-text
```

Check models:

```bash
ollama list
```

---

## 4) Optional: Using HashiCorp Vault for Secrets

In production environments, secrets should be retrieved from a secure secret manager such as:

* HashiCorp Vault
* Azure Key Vault
* AWS Secrets Manager

GenAIShield is compatible with Spring Cloud Vault.

---

### Run Vault locally (development mode)

```bash
docker run --cap-add=IPC_LOCK \
  -e 'VAULT_DEV_ROOT_TOKEN_ID=root' \
  -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' \
  -p 8200:8200 \
  hashicorp/vault
```

Vault UI:

```
http://localhost:8200
```

Token:

```
root
```

---

### Store a secret in Vault

```bash
vault kv put secret/genaishield MISTRAL_API_KEY=your_key_here
```

Spring Boot will automatically resolve:

```
${MISTRAL_API_KEY}
```

---

## 5) Start PGVector

From the project root:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Verify:

```bash
docker ps
```

---

## 6) Run the API

From the project root:

```bash
mvn -pl modules/app-api spring-boot:run
```

API runs at:

```
http://localhost:8080
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

---

## 7) Smoke Test — Chat endpoint

### Using Ollama

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: demo" \
  -H "X-User-Id: demo-user" \
  -H "X-Roles: USER" \
  -d "{\"sessionId\":\"s1\",\"provider\":\"OLLAMA\",\"question\":\"What is GenAIShield?\"}"
```

### Using Mistral

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: demo" \
  -H "X-User-Id: demo-user" \
  -H "X-Roles: USER" \
  -d "{\"sessionId\":\"s1\",\"provider\":\"MISTRAL\",\"question\":\"Summarize GenAIShield in two sentences.\"}"
```

---

## 8) Running Tests

Run unit tests:

```bash
mvn test
```

Run integration tests:

```bash
mvn -pl modules/app-tests verify
```

Docker must be running for integration tests.

---

## 9) Troubleshooting

### API fails to start

* Ensure Docker is running
* Ensure PGVector container is running
* Ensure port 5432 is available

### Mistral provider errors

* Verify `MISTRAL_API_KEY` is set
* Restart terminal after `setx` on Windows

### Ollama provider errors

* Ensure Ollama is running:

```
ollama ps
```

* Ensure models are installed:

```
ollama list
```

---

## 10) Project Structure

```
modules/
  app-core     → domain, ports, policies
  app-infra    → Spring AI adapters, PGVector
  app-api      → REST controllers, config, prompts
  app-tests    → integration and security tests
```

---

## 11) Next Steps

Planned improvements:

* Document ingestion endpoint
* Red-team security tests
* Audit logging
* Tool invocation policies

```

---

# Très important (à faire juste après)

Créer le fichier :

```

.env.example

```

Contenu :

```

MISTRAL_API_KEY=
OLLAMA_BASE_URL=[http://localhost:11434](http://localhost:11434)

```

Et ajouter dans `.gitignore` :

```

.env
