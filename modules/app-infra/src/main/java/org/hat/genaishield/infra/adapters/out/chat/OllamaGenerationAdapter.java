package org.hat.genaishield.infra.adapters.out.chat;

import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.ports.out.AiGenerationPort;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public final class OllamaGenerationAdapter implements AiGenerationPort {

    private final ChatClient chatClient;

    public OllamaGenerationAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public AiProviderId providerId() {
        return AiProviderId.OLLAMA;
    }

    @Override
    public GenerationResult generate(GenerationRequest request) {
        String content = chatClient
                .prompt()
                .system(request.systemPrompt())
                .user(request.userPrompt())
                .call()
                .content();

        return new GenerationResult(content, Map.of());
    }
}
