package org.hat.genaishield.infra.config;

import org.hat.genaishield.core.ports.out.AiGenerationPort;
import org.hat.genaishield.core.ports.out.EmbeddingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class StartupDiagnosticsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(StartupDiagnosticsConfig.class);

    @Bean
    @ConditionalOnProperty(name = "genaishield.diagnostics.startup-log", havingValue = "true", matchIfMissing = true)
    ApplicationRunner startupDiagnostics(Environment env,
                                         ObjectProvider<AiGenerationPort> generationPorts,
                                         ObjectProvider<EmbeddingPort> embeddingPorts,
                                         ObjectProvider<ChatModel> chatModels,
                                         ObjectProvider<EmbeddingModel> embeddingModels) {
        return args -> {
            List<String> gen = generationPorts.orderedStream()
                    .map(p -> p.providerId() + ":" + p.getClass().getSimpleName())
                    .collect(Collectors.toList());
            List<String> emb = embeddingPorts.orderedStream()
                    .map(p -> p.providerId() + ":" + p.getClass().getSimpleName())
                    .collect(Collectors.toList());
            List<String> chat = chatModels.orderedStream()
                    .map(m -> m.getClass().getName())
                    .collect(Collectors.toList());
            List<String> embModels = embeddingModels.orderedStream()
                    .map(m -> m.getClass().getName())
                    .collect(Collectors.toList());

            boolean mistralKeySet = isSet(env.getProperty("spring.ai.mistralai.api-key"));
            String primaryEmbedding = env.getProperty("genaishield.embedding.primary", "MISTRAL");
            String mistralChatEnabled = env.getProperty("spring.ai.mistralai.chat.enabled", "true");
            String mistralEmbeddingEnabled = env.getProperty("spring.ai.mistralai.embedding.enabled", "true");
            String ollamaChatEnabled = env.getProperty("spring.ai.ollama.chat.enabled", "true");
            String ollamaEmbeddingEnabled = env.getProperty("spring.ai.ollama.embedding.enabled", "false");

            LOG.info("Startup diagnostics: mistralApiKeySet={}, primaryEmbedding={}, flags[mistral.chat={}, mistral.embedding={}, ollama.chat={}, ollama.embedding={}]",
                    mistralKeySet, primaryEmbedding, mistralChatEnabled, mistralEmbeddingEnabled, ollamaChatEnabled, ollamaEmbeddingEnabled);
            LOG.info("Startup diagnostics: AiGenerationPort beans={}", gen);
            LOG.info("Startup diagnostics: EmbeddingPort beans={}", emb);
            LOG.info("Startup diagnostics: ChatModel beans={}", chat);
            LOG.info("Startup diagnostics: EmbeddingModel beans={}", embModels);
        };
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
