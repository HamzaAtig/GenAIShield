package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.AiProviderId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface EmbeddingPort {

    AiProviderId providerId();

    float[] embed(String text);

    default List<float[]> embedAll(List<String> texts) {
        Objects.requireNonNull(texts, "texts");
        return texts.stream().map(this::embed).toList();
    }

    default Map<String, Object> metadata() {
        return Map.of();
    }
}
