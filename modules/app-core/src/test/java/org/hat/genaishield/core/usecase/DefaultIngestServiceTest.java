package org.hat.genaishield.core.usecase;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.Identity;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.hat.genaishield.core.ports.out.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultIngestServiceTest {

    @Test
    void ingests_and_upserts_chunks() {
        var actor = new ActorContext(new Identity("u1", "t1"), Set.of("USER"));

        var vectorStore = new InMemoryVectorStore();
        var ingest = new DefaultIngestService(
                providerId -> new DummyEmbedding(),
                new DummySplitter(),
                vectorStore,
                (a, r) -> new AntivirusScannerPort.ScanResult(AntivirusScannerPort.ScanResult.Verdict.CLEAN, null),
                (a, e, d) -> {},
                1024 * 1024,
                Set.of("text/plain"),
                new TextSplitterPort.SplitConfig(10, 0)
        );

        var cmd = new IngestDocumentUseCase.IngestCommand(
                "doc.txt",
                "text/plain",
                12,
                AiProviderId.MISTRAL,
                null,
                List.of("USER"),
                Map.of(),
                new ByteArrayInputStream("hello world".getBytes())
        );

        var res = ingest.ingest(actor, cmd);
        assertNotNull(res.document());
        assertFalse(vectorStore.upserted.isEmpty());
    }

    static final class DummyEmbedding implements EmbeddingPort {
        @Override
        public AiProviderId providerId() {
            return AiProviderId.MISTRAL;
        }

        @Override
        public float[] embed(String text) {
            return new float[]{0.1f, 0.2f};
        }
    }

    static final class DummySplitter implements TextSplitterPort {
        @Override
        public List<String> split(String text, SplitConfig config) {
            return List.of(text);
        }
    }

    static final class InMemoryVectorStore implements VectorStorePort {
        List<EmbeddedChunk> upserted = new ArrayList<>();

        @Override
        public void upsert(ActorContext actor, List<EmbeddedChunk> chunks) {
            upserted.addAll(chunks);
        }

        @Override
        public List<ScoredChunk> search(ActorContext actor, VectorQuery query) {
            return List.of();
        }
    }
}
