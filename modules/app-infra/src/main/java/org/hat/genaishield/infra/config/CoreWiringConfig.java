package org.hat.genaishield.infra.config;

import org.hat.genaishield.core.policy.DefaultAiSecurityPolicy;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.hat.genaishield.core.ports.out.*;
import org.hat.genaishield.core.usecase.DefaultChatService;
import org.hat.genaishield.core.usecase.DefaultIngestService;
import org.hat.genaishield.infra.adapters.embedding.MistralEmbeddingAdapter;
import org.hat.genaishield.infra.adapters.embedding.OllamaEmbeddingAdapter;
import org.hat.genaishield.infra.adapters.out.chat.MistralGenerationAdapter;
import org.hat.genaishield.infra.adapters.out.chat.OllamaGenerationAdapter;
import org.hat.genaishield.infra.adapters.out.JsonAuditLogAdapter;
import org.hat.genaishield.infra.adapters.out.NoopAntivirusScannerAdapter;
import org.hat.genaishield.infra.adapters.out.SimpleTextSplitterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CoreWiringConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CoreWiringConfig.class);

    @Bean
    AiSecurityPolicyPort aiSecurityPolicyPort() {
        return new DefaultAiSecurityPolicy();
    }

    @Bean
    AuditLogPort auditLogPort() {
        return new JsonAuditLogAdapter();
    }

    @Bean
    TextSplitterPort textSplitterPort() {
        return new SimpleTextSplitterAdapter();
    }

    @Bean
    AntivirusScannerPort antivirusScannerPort() {
        return new NoopAntivirusScannerAdapter();
    }

    @Bean
    DefaultChatService.ProviderRouter providerRouter(List<AiGenerationPort> gens,
                                                     List<EmbeddingPort> embeds,
                                                     ObjectProvider<MistralAiChatModel> mistralChatModel,
                                                     ObjectProvider<OllamaChatModel> ollamaChatModel,
                                                     ObjectProvider<MistralAiEmbeddingModel> mistralEmbeddingModel,
                                                     ObjectProvider<OllamaEmbeddingModel> ollamaEmbeddingModel,
                                                     Environment env) {
        Map<AiProviderId, AiGenerationPort> genMap = gens.stream()
                .collect(Collectors.toMap(AiGenerationPort::providerId, Function.identity()));

        Map<AiProviderId, EmbeddingPort> embMap = embeds.stream()
                .collect(Collectors.toMap(EmbeddingPort::providerId, Function.identity()));

        // Fallback wiring if adapter beans are missing for any reason.
        MistralAiChatModel mistralChat = mistralChatModel.getIfAvailable();
        if (mistralChat != null && !genMap.containsKey(AiProviderId.MISTRAL)) {
            genMap.put(AiProviderId.MISTRAL, new MistralGenerationAdapter(ChatClient.builder(mistralChat).build()));
        }
        OllamaChatModel ollamaChat = ollamaChatModel.getIfAvailable();
        if (ollamaChat != null && !genMap.containsKey(AiProviderId.OLLAMA)) {
            genMap.put(AiProviderId.OLLAMA, new OllamaGenerationAdapter(ChatClient.builder(ollamaChat).build()));
        }

        EmbeddingModel mistralEmb = mistralEmbeddingModel.getIfAvailable();
        if (mistralEmb != null && !embMap.containsKey(AiProviderId.MISTRAL)) {
            embMap.put(AiProviderId.MISTRAL, new MistralEmbeddingAdapter(mistralEmb));
        }
        EmbeddingModel ollamaEmb = ollamaEmbeddingModel.getIfAvailable();
        if (ollamaEmb != null && !embMap.containsKey(AiProviderId.OLLAMA)) {
            embMap.put(AiProviderId.OLLAMA, new OllamaEmbeddingAdapter(ollamaEmb));
        }

        String configuredPrimary = env.getProperty("genaishield.embedding.primary", "MISTRAL");
        AiProviderId primaryEmbedding = parseProvider(configuredPrimary, AiProviderId.MISTRAL);
        String mistralApiKey = env.getProperty("spring.ai.mistralai.api-key", "");
        boolean mistralApiKeySet = mistralApiKey != null && !mistralApiKey.isBlank();
        LOG.info("Provider wiring - generation providers: {}, embedding providers: {}, primary embedding: {}, mistralApiKeySet: {}",
                genMap.keySet(), embMap.keySet(), primaryEmbedding, mistralApiKeySet);

        return new DefaultChatService.ProviderRouter() {
            @Override
            public AiGenerationPort generation(org.hat.genaishield.core.domain.AiProviderId providerId) {
                AiGenerationPort p = genMap.get(providerId);
                if (p != null) return p;

                if (genMap.size() == 1) {
                    AiGenerationPort single = genMap.values().iterator().next();
                    LOG.warn("No generation provider for {}. Falling back to single available generation provider {}.",
                            providerId, single.providerId());
                    return single;
                }
                if (genMap.isEmpty()) {
                    throw new IllegalArgumentException("No generation provider registered. Check provider flags and API keys.");
                }
                throw new IllegalArgumentException("No generation provider for " + providerId + ". Available: " + genMap.keySet());
            }

            @Override
            public EmbeddingPort embedding(org.hat.genaishield.core.domain.AiProviderId providerId) {
                EmbeddingPort p = embMap.get(providerId);
                if (p != null) return p;

                EmbeddingPort primary = embMap.get(primaryEmbedding);
                if (primary != null) {
                    LOG.warn("No embedding provider for {}. Falling back to configured primary embedding provider {}.",
                            providerId, primaryEmbedding);
                    return primary;
                }

                if (embMap.size() == 1) {
                    EmbeddingPort single = embMap.values().iterator().next();
                    LOG.warn("No embedding provider for {}. Falling back to single available embedding provider {}.",
                            providerId, single.providerId());
                    return single;
                }

                if (embMap.isEmpty()) {
                    throw new IllegalArgumentException("No embedding provider registered. Check provider flags and API keys.");
                }
                throw new IllegalArgumentException("No embedding provider for " + providerId + ". Available: " + embMap.keySet());
            }
        };
    }

    private static AiProviderId parseProvider(String raw, AiProviderId fallback) {
        try {
            return AiProviderId.from(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    @Bean
    ChatUseCase chatUseCase(DefaultChatService.ProviderRouter router,
                            VectorStorePort vectorStorePort,
                            AiSecurityPolicyPort securityPolicy,
                            PromptTemplatePort promptTemplatePort,
                            AuditLogPort auditLogPort) {
        return new org.hat.genaishield.core.usecase.DefaultChatService(
                router, vectorStorePort, securityPolicy, promptTemplatePort, auditLogPort);
    }

    @Bean
    PromptTemplatePort promptTemplatePort() {
        return new org.hat.genaishield.infra.adapters.out.ClasspathPromptTemplateAdapter("prompts");
    }

    @Bean
    DefaultIngestService.EmbeddingRouter ingestEmbeddingRouter(List<EmbeddingPort> embeds,
                                                               Environment env) {
        Map<?, EmbeddingPort> embMap = embeds.stream()
                .collect(Collectors.toMap(EmbeddingPort::providerId, Function.identity()));
        AiProviderId primaryEmbedding = parseProvider(
                env.getProperty("genaishield.embedding.primary", "MISTRAL"),
                AiProviderId.MISTRAL
        );
        return providerId -> {
            EmbeddingPort p = embMap.get(providerId);
            if (p != null) return p;
            EmbeddingPort primary = embMap.get(primaryEmbedding);
            if (primary != null) return primary;
            if (embMap.size() == 1) return embMap.values().iterator().next();
            if (embMap.isEmpty()) throw new IllegalArgumentException("No embedding provider registered. Check provider flags and API keys.");
            throw new IllegalArgumentException("No embedding provider for " + providerId + ". Available: " + embMap.keySet());
        };
    }

    @Bean
    IngestDocumentUseCase ingestDocumentUseCase(DefaultIngestService.EmbeddingRouter router,
                                                TextSplitterPort textSplitterPort,
                                                VectorStorePort vectorStorePort,
                                                AntivirusScannerPort antivirusScannerPort,
                                                AuditLogPort auditLogPort) {
        return new DefaultIngestService(
                router,
                textSplitterPort,
                vectorStorePort,
                antivirusScannerPort,
                auditLogPort,
                10 * 1024 * 1024,
                Set.of("text/plain", "text/markdown", "application/json"),
                new TextSplitterPort.SplitConfig(800, 100)
        );
    }

}
