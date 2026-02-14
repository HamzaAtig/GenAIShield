package org.hat.genaishield.infra.adapters.out.vector;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.VectorStorePort;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.*;

public final class PgVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;

    public PgVectorStoreAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void upsert(ActorContext actor, List<EmbeddedChunk> chunks) {
        List<Document> docs = new ArrayList<>();
        for (EmbeddedChunk c : chunks) {
            Map<String, Object> md = new HashMap<>();
            md.put("tenantId", actor.identity().tenantId());
            md.put("documentId", c.document().id());
            md.put("chunkId", c.chunkId());
            md.put("allowedRoles", c.metadata().allowedRoles());
            md.put("classification", c.metadata().classification().name());
            md.putAll(c.metadata().attributes());

            // content = chunk text ; metadata = filters + citations
            docs.add(new Document(c.text(), md));
        }
        vectorStore.add(docs);
    }

    @Override
    public List<ScoredChunk> search(ActorContext actor, VectorQuery query) {
        // Filter BEFORE ranking (tenant boundary)
        String filter = "tenantId == '" + actor.identity().tenantId() + "'";
        if (query.restrictToDocument().isPresent()) {
            filter += " && documentId == '" + query.restrictToDocument().get().id() + "'";
        }

        SearchRequest req = SearchRequest.builder()
                .query(query.queryText())
                .topK(query.topK())
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(req).stream()
                .map(d -> {
                    String docId = String.valueOf(d.getMetadata().getOrDefault("documentId", "unknown"));
                    String chunkId = String.valueOf(d.getMetadata().getOrDefault("chunkId", "unknown"));
                    double score = extractScore(d);

                    EmbeddedChunk chunk = new EmbeddedChunk(
                            new org.hat.genaishield.core.domain.DocumentRef(docId),
                            chunkId,
                            d.getText(), // ✅ correct pour ton API
                            new float[0],
                            new ChunkMetadata(actor.identity().tenantId(), null, List.of(), Map.of())
                    );
                    return new ScoredChunk(chunk, score);
                })
                .toList();

    }

    private static double extractScore(Document d) {
        Object v = d.getMetadata().get("score");
        if (v instanceof Number n) return n.doubleValue();
        v = d.getMetadata().get("distance");
        if (v instanceof Number n) return n.doubleValue();
        return 0d;
    }

}
