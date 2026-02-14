package org.hat.genaishield.core.usecase;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.DocumentRef;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.hat.genaishield.core.ports.out.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DefaultIngestService implements IngestDocumentUseCase {

    public interface EmbeddingRouter {
        EmbeddingPort embedding(AiProviderId providerId);
    }

    private final EmbeddingRouter router;
    private final TextSplitterPort splitter;
    private final VectorStorePort vectorStore;
    private final AntivirusScannerPort antivirus;
    private final AuditLogPort auditLog;
    private final long maxBytes;
    private final Set<String> allowedContentTypes;
    private final TextSplitterPort.SplitConfig splitConfig;

    public DefaultIngestService(EmbeddingRouter router,
                                TextSplitterPort splitter,
                                VectorStorePort vectorStore,
                                AntivirusScannerPort antivirus,
                                AuditLogPort auditLog,
                                long maxBytes,
                                Set<String> allowedContentTypes,
                                TextSplitterPort.SplitConfig splitConfig) {
        this.router = Objects.requireNonNull(router, "router");
        this.splitter = Objects.requireNonNull(splitter, "splitter");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore");
        this.antivirus = Objects.requireNonNull(antivirus, "antivirus");
        this.auditLog = Objects.requireNonNull(auditLog, "auditLog");
        this.maxBytes = maxBytes <= 0 ? 10 * 1024 * 1024 : maxBytes;
        this.allowedContentTypes = Set.copyOf(allowedContentTypes == null || allowedContentTypes.isEmpty()
                ? Set.of("text/plain", "text/markdown", "application/json")
                : allowedContentTypes);
        this.splitConfig = Objects.requireNonNullElse(splitConfig, new TextSplitterPort.SplitConfig(800, 100));
    }

    @Override
    public IngestResult ingest(ActorContext actor, IngestCommand command) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(command, "command");

        if (!allowedContentTypes.contains(command.contentType())) {
            throw new IllegalArgumentException("Unsupported content type: " + command.contentType());
        }

        if (command.sizeBytes() > 0 && command.sizeBytes() > maxBytes) {
            throw new IllegalArgumentException("File too large: " + command.sizeBytes());
        }

        byte[] bytes = readAll(command.stream(), maxBytes);

        AntivirusScannerPort.ScanResult scan = antivirus.scan(
                actor,
                new AntivirusScannerPort.ScanRequest(
                        command.filename(), command.contentType(), bytes.length, new ByteArrayInputStream(bytes)
                )
        );
        if (!scan.isAllowed()) {
            auditLog.logEvent(actor, "ingest.rejected", Map.of(
                    "filename", command.filename(),
                    "contentType", command.contentType(),
                    "verdict", scan.verdict().name()
            ));
            throw new IllegalArgumentException("File rejected by antivirus: " + scan.verdict());
        }

        String text = new String(bytes, StandardCharsets.UTF_8).trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Empty document content");
        }

        List<String> chunks = splitter.split(text, splitConfig);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks extracted");
        }

        EmbeddingPort embedding = router.embedding(command.provider());
        List<float[]> vectors = embedding.embedAll(chunks);

        DocumentRef doc = new DocumentRef(UUID.randomUUID().toString());
        List<VectorStorePort.EmbeddedChunk> embedded = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkId = "c" + (i + 1);
            Map<String, String> attrs = new HashMap<>(command.attributes());
            attrs.putIfAbsent("filename", command.filename());
            attrs.putIfAbsent("contentType", command.contentType());

            VectorStorePort.ChunkMetadata md = new VectorStorePort.ChunkMetadata(
                    actor.identity().tenantId(),
                    command.classification(),
                    command.allowedRoles(),
                    attrs
            );

            embedded.add(new VectorStorePort.EmbeddedChunk(
                    doc, chunkId, chunks.get(i), vectors.get(i), md
            ));
        }

        vectorStore.upsert(actor, embedded);

        auditLog.logEvent(actor, "ingest.completed", Map.of(
                "documentId", doc.id(),
                "chunks", embedded.size(),
                "provider", command.provider().name()
        ));

        return new IngestResult(doc);
    }

    private static byte[] readAll(InputStream in, long maxBytes) {
        try {
            byte[] buffer = in.readAllBytes();
            if (buffer.length > maxBytes) {
                throw new IllegalArgumentException("File too large: " + buffer.length);
            }
            return buffer;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read input stream", e);
        }
    }
}
