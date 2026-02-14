package org.hat.genaishield.core.usecase;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.hat.genaishield.core.ports.out.*;

import java.util.*;
import java.util.stream.Collectors;

public final class DefaultChatService implements ChatUseCase {

    private final ProviderRouter router;
    private final VectorStorePort vectorStore;
    private final AiSecurityPolicyPort securityPolicy;
    private final PromptTemplatePort promptTemplates;
    private final AuditLogPort auditLog;

    public DefaultChatService(ProviderRouter router,
                              VectorStorePort vectorStore,
                              AiSecurityPolicyPort securityPolicy,
                              PromptTemplatePort promptTemplates,
                              AuditLogPort auditLog) {
        this.router = Objects.requireNonNull(router, "router");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy");
        this.promptTemplates = Objects.requireNonNull(promptTemplates, "promptTemplates");
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog");
    }

    @Override
    public ChatResult chat(ActorContext actor, ChatCommand command) {
        VectorStorePort.VectorQuery query = new VectorStorePort.VectorQuery(
                command.question(),
                new float[0],
                6,
                command.restrictToDocument().orElse(null)
        );

        List<VectorStorePort.ScoredChunk> retrieved = vectorStore.search(actor, query);

        List<String> retrievedTexts = retrieved.stream().map(sc -> sc.chunk().text()).toList();

        AiSecurityPolicyPort.Decision decision = securityPolicy.evaluate(
                actor,
                new AiSecurityPolicyPort.PolicyInput(command.question(), retrievedTexts, Map.of())
        );

        auditLog.logEvent(actor, "chat.policy", Map.of(
                "provider", command.provider().name(),
                "sessionId", command.sessionId(),
                "outcome", decision.outcome().name(),
                "reasons", decision.reasons()
        ));

        if (decision.outcome() == AiSecurityPolicyPort.Decision.Outcome.BLOCK) {
            return new ChatResult(
                    "I can’t comply with that request due to security policy constraints.",
                    List.of()
            );
        }

        List<VectorStorePort.ScoredChunk> effectiveChunks = retrieved;
        if (decision.outcome() == AiSecurityPolicyPort.Decision.Outcome.ALLOW_WITH_SANITIZATION) {
            effectiveChunks = sanitizeRetrieved(retrieved, decision.sanitization());
        }

        String context = effectiveChunks.stream()
                .map(DefaultChatService::formatEvidence)
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> vars = new HashMap<>();
        vars.put("question", command.question());
        vars.put("context", context);
        vars.put("tenantId", actor.identity().tenantId());
        vars.put("userId", actor.identity().userId());
        vars.put("provider", command.provider().name());
        vars.put("sessionId", command.sessionId());

        String system = promptTemplates.render("chat.system", vars);
        String user = promptTemplates.render("chat.user", vars);

        AiGenerationPort gen = router.generation(command.provider());
        AiGenerationPort.GenerationResult out = gen.generate(
                new AiGenerationPort.GenerationRequest(system, user, Map.of("sessionId", command.sessionId()), null)
        );

        List<Citation> citations = effectiveChunks.stream()
                .map(sc -> new Citation(sc.chunk().document().id(), sc.chunk().chunkId(), sc.score()))
                .toList();

        auditLog.logEvent(actor, "chat.response", Map.of(
                "provider", command.provider().name(),
                "sessionId", command.sessionId(),
                "citations", citations.size()
        ));

        return new ChatResult(out.content(), citations);
    }

    private static String formatEvidence(VectorStorePort.ScoredChunk sc) {
        var c = sc.chunk();
        return "[doc=%s chunk=%s score=%.4f]\n%s".formatted(c.document().id(), c.chunkId(), sc.score(), c.text());
    }

    private static List<VectorStorePort.ScoredChunk> sanitizeRetrieved(
            List<VectorStorePort.ScoredChunk> retrieved,
            AiSecurityPolicyPort.Sanitization s
    ) {
        return retrieved.stream()
                .map(sc -> {
                    String t = sc.chunk().text();
                    if (s.stripInjectionPatterns()) t = stripInjectionHints(t);
                    if (s.redactSecretsLikePatterns()) t = redactSecretsHints(t);

                    VectorStorePort.EmbeddedChunk c = new VectorStorePort.EmbeddedChunk(
                            sc.chunk().document(),
                            sc.chunk().chunkId(),
                            t,
                            sc.chunk().embedding(),
                            sc.chunk().metadata()
                    );
                    return new VectorStorePort.ScoredChunk(c, sc.score());
                })
                .toList();
    }

    private static String stripInjectionHints(String t) {
        return t.replaceAll("(?i)ignore (all|previous) instructions", "[REMOVED]")
                .replaceAll("(?i)system prompt", "[REMOVED]")
                .replaceAll("(?i)reveal.*(secret|key|token)", "[REMOVED]");
    }

    private static String redactSecretsHints(String t) {
        return t.replaceAll("(?i)(api[_-]?key\\s*[:=]\\s*)(\\S+)", "$1[REDACTED]")
                .replaceAll("(?i)(bearer\\s+)(\\S+)", "$1[REDACTED]");
    }

    public interface ProviderRouter {
        AiGenerationPort generation(AiProviderId providerId);
        EmbeddingPort embedding(AiProviderId providerId);
    }
}
