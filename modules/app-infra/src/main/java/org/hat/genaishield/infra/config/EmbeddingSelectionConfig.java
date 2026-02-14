package org.hat.genaishield.infra.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Locale;

@Configuration
public class EmbeddingSelectionConfig {

    @Bean
    static BeanFactoryPostProcessor embeddingPrimarySelector(Environment env) {
        return factory -> {
            String mode = env.getProperty("genaishield.embedding.primary", "MISTRAL")
                    .toUpperCase(Locale.ROOT)
                    .trim();

            String primaryBean = "OLLAMA".equals(mode) ? "ollamaEmbeddingModel" : "mistralAiEmbeddingModel";
            String secondaryBean = "OLLAMA".equals(mode) ? "mistralAiEmbeddingModel" : "ollamaEmbeddingModel";

            if (factory.containsBeanDefinition(primaryBean)) {
                factory.getBeanDefinition(primaryBean).setPrimary(true);
            }
            if (factory.containsBeanDefinition(secondaryBean)) {
                factory.getBeanDefinition(secondaryBean).setPrimary(false);
            }
        };
    }
}
