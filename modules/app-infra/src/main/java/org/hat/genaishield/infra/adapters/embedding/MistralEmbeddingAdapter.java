package org.hat.genaishield.infra.adapters.embedding;

import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.ports.out.EmbeddingPort;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

public final class MistralEmbeddingAdapter implements EmbeddingPort {

    private final EmbeddingModel embeddingModel;

    public MistralEmbeddingAdapter(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public AiProviderId providerId() {
        return AiProviderId.MISTRAL;
    }

    @Override
    public float[] embed(String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        Object output = response.getResult().getOutput();

        if (output instanceof float[] floats) return floats;

        if (output instanceof List<?> list) {
            float[] out = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object v = list.get(i);
                if (!(v instanceof Number n)) {
                    throw new IllegalStateException("Unexpected embedding element type: " + v.getClass());
                }
                out[i] = n.floatValue();
            }
            return out;
        }

        throw new IllegalStateException("Unsupported embedding output type: " +
                (output == null ? "null" : output.getClass()));
    }
}
