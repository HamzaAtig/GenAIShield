package org.hat.genaishield.infra.config;

import org.hat.genaishield.core.ports.out.AiGenerationPort;
import org.hat.genaishield.core.ports.out.EmbeddingPort;
import org.hat.genaishield.core.ports.out.VectorStorePort;
import org.hat.genaishield.infra.adapters.out.chat.MistralGenerationAdapter;
import org.hat.genaishield.infra.adapters.out.chat.OllamaGenerationAdapter;
import org.hat.genaishield.infra.adapters.embedding.OllamaEmbeddingAdapter;
import org.hat.genaishield.infra.adapters.out.vector.PgVectorStoreAdapter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAdaptersConfig {

    // --- ChatClients bound to each provider ---
    @Bean("mistralChatClient")
    public ChatClient mistralChatClient(@Qualifier("mistralAiChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(@Qualifier("ollamaChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    // --- Ports ---
    @Bean
    public AiGenerationPort mistralGenerationPort(@Qualifier("mistralChatClient") ChatClient client) {
        return new MistralGenerationAdapter(client);
    }

    @Bean
    public AiGenerationPort ollamaGenerationPort(@Qualifier("ollamaChatClient") ChatClient client) {
        return new OllamaGenerationAdapter(client);
    }

    @Bean
    public EmbeddingPort ollamaEmbeddingPort(@Qualifier("ollamaEmbeddingModel") EmbeddingModel model) {
        return new OllamaEmbeddingAdapter(model);
    }

    @Bean
    public VectorStorePort vectorStorePort(VectorStore vectorStore) {
        return new PgVectorStoreAdapter(vectorStore);
    }
}
