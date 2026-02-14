package org.hat.genaishield.infra.config;

import org.hat.genaishield.core.ports.out.AiGenerationPort;
import org.hat.genaishield.core.ports.out.EmbeddingPort;
import org.hat.genaishield.core.ports.out.VectorStorePort;
import org.hat.genaishield.infra.adapters.embedding.MistralEmbeddingAdapter;
import org.hat.genaishield.infra.adapters.out.chat.MistralGenerationAdapter;
import org.hat.genaishield.infra.adapters.out.chat.OllamaGenerationAdapter;
import org.hat.genaishield.infra.adapters.embedding.OllamaEmbeddingAdapter;
import org.hat.genaishield.infra.adapters.out.vector.PgVectorStoreAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAdaptersConfig {

    // --- Ports ---
    @Bean
    @ConditionalOnBean(type = "org.springframework.ai.mistralai.MistralAiChatModel")
    public AiGenerationPort mistralGenerationPort(MistralAiChatModel model) {
        return new MistralGenerationAdapter(ChatClient.builder(model).build());
    }

    @Bean
    @ConditionalOnBean(type = "org.springframework.ai.ollama.OllamaChatModel")
    public AiGenerationPort ollamaGenerationPort(OllamaChatModel model) {
        return new OllamaGenerationAdapter(ChatClient.builder(model).build());
    }

    @Bean
    @ConditionalOnBean(type = "org.springframework.ai.mistralai.MistralAiEmbeddingModel")
    public EmbeddingPort mistralEmbeddingPort(MistralAiEmbeddingModel model) {
        return new MistralEmbeddingAdapter(model);
    }

    @Bean
    @ConditionalOnBean(type = "org.springframework.ai.ollama.OllamaEmbeddingModel")
    public EmbeddingPort ollamaEmbeddingPort(OllamaEmbeddingModel model) {
        return new OllamaEmbeddingAdapter(model);
    }

    @Bean
    public VectorStorePort vectorStorePort(VectorStore vectorStore) {
        return new PgVectorStoreAdapter(vectorStore);
    }
}
