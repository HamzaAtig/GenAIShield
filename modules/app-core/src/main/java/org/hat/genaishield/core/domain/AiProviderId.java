package org.hat.genaishield.core.domain;

import java.util.Locale;

public enum AiProviderId {
    MISTRAL,
    OLLAMA;

    public static AiProviderId from(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("provider id is required");
        return AiProviderId.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
