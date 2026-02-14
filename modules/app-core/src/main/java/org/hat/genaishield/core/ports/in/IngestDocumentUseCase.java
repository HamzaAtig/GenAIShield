package org.hat.genaishield.core.ports.in;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.Classification;
import org.hat.genaishield.core.domain.DocumentRef;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface IngestDocumentUseCase {

    IngestResult ingest(ActorContext actor, IngestCommand command);

    final class IngestCommand {
        private final String filename;
        private final String contentType;
        private final long sizeBytes;
        private final AiProviderId provider;
        private final Classification classification;
        private final List<String> allowedRoles;
        private final Map<String, String> attributes;
        private final InputStream stream;

        public IngestCommand(String filename,
                             String contentType,
                             long sizeBytes,
                             AiProviderId provider,
                             Classification classification,
                             List<String> allowedRoles,
                             Map<String, String> attributes,
                             InputStream stream) {
            this.filename = requireNonBlank(filename, "filename");
            this.contentType = requireNonBlank(contentType, "contentType");
            this.sizeBytes = sizeBytes;
            this.provider = Objects.requireNonNullElse(provider, AiProviderId.MISTRAL);
            this.classification = Objects.requireNonNullElse(classification, Classification.INTERNAL);
            this.allowedRoles = List.copyOf(allowedRoles == null ? List.of() : allowedRoles);
            this.attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
            this.stream = Objects.requireNonNull(stream, "stream");
        }

        public String filename() {
            return filename;
        }

        public String contentType() {
            return contentType;
        }

        public long sizeBytes() {
            return sizeBytes;
        }

        public AiProviderId provider() {
            return provider;
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

        public InputStream stream() {
            return stream;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class IngestResult {
        private final DocumentRef document;

        public IngestResult(DocumentRef document) {
            this.document = Objects.requireNonNull(document, "document");
        }

        public DocumentRef document() {
            return document;
        }
    }
}
