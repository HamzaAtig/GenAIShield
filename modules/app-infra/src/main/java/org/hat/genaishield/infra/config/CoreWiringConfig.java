package org.hat.genaishield.infra.config;

import org.hat.genaishield.core.policy.DefaultAiSecurityPolicy;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.hat.genaishield.core.ports.out.*;
import org.hat.genaishield.core.usecase.DefaultChatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CoreWiringConfig {

    @Bean
    AiSecurityPolicyPort aiSecurityPolicyPort() {
        return new DefaultAiSecurityPolicy();
    }

    @Bean
    DefaultChatService.ProviderRouter providerRouter(List<AiGenerationPort> gens,
                                                     List<EmbeddingPort> embeds) {
        Map<?, AiGenerationPort> genMap = gens.stream()
                .collect(Collectors.toMap(AiGenerationPort::providerId, Function.identity()));

        Map<?, EmbeddingPort> embMap = embeds.stream()
                .collect(Collectors.toMap(EmbeddingPort::providerId, Function.identity()));

        return new DefaultChatService.ProviderRouter() {
            @Override
            public AiGenerationPort generation(org.hat.genaishield.core.domain.AiProviderId providerId) {
                AiGenerationPort p = genMap.get(providerId);
                if (p == null) throw new IllegalArgumentException("No generation provider for " + providerId);
                return p;
            }

            @Override
            public EmbeddingPort embedding(org.hat.genaishield.core.domain.AiProviderId providerId) {
                EmbeddingPort p = embMap.get(providerId);
                if (p == null) throw new IllegalArgumentException("No embedding provider for " + providerId);
                return p;
            }
        };
    }

    @Bean
    ChatUseCase chatUseCase(DefaultChatService.ProviderRouter router,
                            VectorStorePort vectorStorePort,
                            AiSecurityPolicyPort securityPolicy,
                            PromptTemplatePort promptTemplatePort) {
        return new org.hat.genaishield.core.usecase.DefaultChatService(
                router, vectorStorePort, securityPolicy, promptTemplatePort);
    }

    @Bean
    PromptTemplatePort promptTemplatePort() {
        return new org.hat.genaishield.infra.adapters.out.ClasspathPromptTemplateAdapter("prompts");
    }

}
