package org.hat.genaishield.core.domain;

import java.util.Objects;

public final class DocumentRef {
    private final String documentId;

    public DocumentRef(String documentId) {
        this.documentId = requireNonBlank(documentId, "documentId");
    }

    public String id() {
        return documentId;
    }

    private static String requireNonBlank(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentRef other)) return false;
        return documentId.equals(other.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }
}
