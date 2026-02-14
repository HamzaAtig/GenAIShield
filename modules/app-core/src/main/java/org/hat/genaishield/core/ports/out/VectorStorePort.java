package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.Classification;
import org.hat.genaishield.core.domain.DocumentRef;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface VectorStorePort {

    void upsert(ActorContext actor, List<EmbeddedChunk> chunks);

    List<ScoredChunk> search(ActorContext actor, VectorQuery query);

    final class EmbeddedChunk {
        private final DocumentRef document;
        private final String chunkId;
        private final String text;
        private final float[] embedding;
        private final ChunkMetadata metadata;

        public EmbeddedChunk(DocumentRef document, String chunkId, String text, float[] embedding, ChunkMetadata metadata) {
            this.document = Objects.requireNonNull(document, "document");
            this.chunkId = requireNonBlank(chunkId, "chunkId");
            this.text = requireNonBlank(text, "text");
            this.embedding = Objects.requireNonNull(embedding, "embedding");
            this.metadata = Objects.requireNonNullElseGet(metadata, ChunkMetadata::empty);
        }

        public DocumentRef document() {
            return document;
        }

        public String chunkId() {
            return chunkId;
        }

        public String text() {
            return text;
        }

        public float[] embedding() {
            return embedding;
        }

        public ChunkMetadata metadata() {
            return metadata;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class ChunkMetadata {
        private final String tenantId;
        private final Classification classification;
        private final List<String> allowedRoles;
        private final Map<String, String> attributes;

        public ChunkMetadata(String tenantId,
                             Classification classification,
                             List<String> allowedRoles,
                             Map<String, String> attributes) {
            this.tenantId = requireNonBlank(tenantId, "tenantId");
            this.classification = Objects.requireNonNullElse(classification, Classification.INTERNAL);
            this.allowedRoles = List.copyOf(allowedRoles == null ? List.of() : allowedRoles);
            this.attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
        }

        public String tenantId() {
            return tenantId;
        }

        public Classification classification() {
            return classification;
        }

        public List<String> allowedRoles() {
            return allowedRoles;
        }

        public Map<String, String> attributes() {
            return attributes;
        }

        public static ChunkMetadata empty() {
            return new ChunkMetadata("UNKNOWN", Classification.INTERNAL, List.of(), Map.of());
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class VectorQuery {
        private final String queryText;
        private final float[] queryEmbedding;
        private final int topK;
        private final Optional<DocumentRef> restrictToDocument;

        public VectorQuery(String queryText, float[] queryEmbedding, int topK, DocumentRef restrictToDocument) {
            this.queryText = requireNonBlank(queryText, "queryText");
            this.queryEmbedding = Objects.requireNonNull(queryEmbedding, "queryEmbedding");
            this.topK = topK <= 0 ? 5 : topK;
            this.restrictToDocument = Optional.ofNullable(restrictToDocument);
        }

        public String queryText() {
            return queryText;
        }

        public float[] queryEmbedding() {
            return queryEmbedding;
        }

        public int topK() {
            return topK;
        }

        public Optional<DocumentRef> restrictToDocument() {
            return restrictToDocument;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class ScoredChunk {
        private final EmbeddedChunk chunk;
        private final double score;

        public ScoredChunk(EmbeddedChunk chunk, double score) {
            this.chunk = Objects.requireNonNull(chunk, "chunk");
            this.score = score;
        }

        public EmbeddedChunk chunk() {
            return chunk;
        }

        public double score() {
            return score;
        }
    }
}
