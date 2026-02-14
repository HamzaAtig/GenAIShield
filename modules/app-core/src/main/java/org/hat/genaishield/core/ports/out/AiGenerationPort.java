package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.AiProviderId;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface AiGenerationPort {

    AiProviderId providerId();

    GenerationResult generate(GenerationRequest request);

    final class GenerationRequest {
        private final String systemPrompt;
        private final String userPrompt;
        private final Map<String, Object> attributes;
        private final Optional<Duration> timeout;

        public GenerationRequest(String systemPrompt,
                                 String userPrompt,
                                 Map<String, Object> attributes,
                                 Duration timeout) {
            this.systemPrompt = requireNonBlank(systemPrompt, "systemPrompt");
            this.userPrompt = requireNonBlank(userPrompt, "userPrompt");
            this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
            this.timeout = Optional.ofNullable(timeout);
        }

        public String systemPrompt() {
            return systemPrompt;
        }

        public String userPrompt() {
            return userPrompt;
        }

        public Map<String, Object> attributes() {
            return attributes;
        }

        public Optional<Duration> timeout() {
            return timeout;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class GenerationResult {
        private final String content;
        private final Map<String, Object> usage; // tokens, cost, etc (provider-specific)

        public GenerationResult(String content, Map<String, Object> usage) {
            this.content = requireNonBlank(content, "content");
            this.usage = usage == null ? Map.of() : Map.copyOf(usage);
        }

        public String content() {
            return content;
        }

        public Map<String, Object> usage() {
            return usage;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }
}
