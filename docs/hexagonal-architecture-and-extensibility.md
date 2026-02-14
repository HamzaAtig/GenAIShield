# Architecture hexagonale et extensibilité

Ce document explique le flux d'appels dans GenAIShield et pourquoi l'architecture actuelle est facilement extensible, autant pour les règles de sécurité que pour les providers GenAI.

## 1) Vue d'ensemble

Le projet suit une architecture hexagonale (Ports & Adapters) :

- `app-core` : domaine, use cases, ports (contrats), politiques de sécurité.
- `app-api` : contrôleurs REST, DTO, filtres HTTP (rate-limit, correlation-id), anonymisation d'entrée.
- `app-infra` : adaptateurs techniques (Spring AI providers, PGVector, audit log JSON, wiring Spring).
- `app-tests` : tests E2E/attaque avec Testcontainers.

Principe clé :
- le `core` ne dépend pas de Spring, ni de Mistral/Ollama, ni de PostgreSQL.
- les détails techniques sont branchés via des adaptateurs qui implémentent les ports.

## 2) Flux d'appels (cas chat)

1. Entrée HTTP via `ChatController` (`app-api`).
2. Filtres transverses : `CorrelationIdFilter`, `RateLimitingFilter`.
3. Sanitization/anonymisation des données d'entrée.
4. Appel du use case `ChatUseCase` (`DefaultChatService`, `app-core`).
5. Recherche de contexte via `VectorStorePort` (adaptateur PGVector en `app-infra`).
6. Évaluation sécurité via `AiSecurityPolicyPort` (engine composite + règles).
7. Sélection provider via router (`AiGenerationPort` / `EmbeddingPort`).
8. Génération de réponse puis audit (`AuditLogPort` -> JSON log).
9. Retour API avec citations.

## 3) Flux d'appels (cas ingest)

1. Entrée HTTP via `IngestController`.
2. Validation + sanitization des attributs.
3. Appel use case `IngestDocumentUseCase` (`DefaultIngestService`).
4. Antivirus, split, embeddings, upsert vector store.
5. Audit des événements d'ingestion.
6. Retour `documentId`.

## 4) Extensibilité sécurité (policies/rules)

Le moteur de sécurité est composé de règles :

- `CompositeAiSecurityPolicy`
- `ExfiltrationRule`
- `PromptInjectionRule`
- `ModerationRule`
- `DefaultSanitizationRule`

Pour ajouter une nouvelle règle :

1. Créer une classe qui implémente `SecurityRule` dans `app-core`.
2. Définir la décision (`ALLOW`, `ALLOW_WITH_SANITIZATION`, `BLOCK`) + raisons.
3. L'enregistrer dans `DefaultAiSecurityPolicy` (ordre maîtrisé).
4. Ajouter tests unitaires + E2E ciblés.

Résultat :
- pas de refonte des contrôleurs ni des adaptateurs.
- extension locale au `core` avec impact maîtrisé.

## 5) Extensibilité providers GenAI

Le routage providers repose sur des ports :

- `AiGenerationPort`
- `EmbeddingPort`

Pour ajouter un provider (ex: Azure/OpenAI/Anthropic) :

1. Créer adaptateur génération (`...GenerationAdapter`) qui implémente `AiGenerationPort`.
2. Créer adaptateur embedding (`...EmbeddingAdapter`) qui implémente `EmbeddingPort`.
3. Déclarer le wiring Spring dans `app-infra` (config + conditions/flags).
4. Ajouter l'identifiant provider dans le domaine (`AiProviderId`) et tests.

Résultat :
- le `core` ne change pas dans sa logique métier.
- le nouveau provider est branché via les ports existants.

## 6) Pourquoi c'est robuste pour l'évolution

- Séparation claire métier / technique.
- Testabilité élevée (unitaires, MockMvc, E2E Testcontainers).
- Ajout de règles sécurité sans couplage au web ou au provider SDK.
- Ajout/changement de provider sans toucher aux use cases métier.
- Gouvernance simple : règles et décisions auditées (JSON + correlation-id).

## 7) Points d'attention

- L'ordre des règles de sécurité influence le résultat final : il doit être explicitement revu à chaque ajout.
- La modération locale regex est volontairement simple : documenter ses limites (payload obfusqué).
- Chaque extension doit être accompagnée de tests de non-régression et d'attaque.
