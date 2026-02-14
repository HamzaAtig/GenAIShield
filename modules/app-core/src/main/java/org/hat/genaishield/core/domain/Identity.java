package org.hat.genaishield.core.domain;

import java.util.Objects;

public final class Identity {
    private final String userId;
    private final String tenantId;

    public Identity(String userId, String tenantId) {
        this.userId = requireNonBlank(userId, "userId");
        this.tenantId = requireNonBlank(tenantId, "tenantId");
    }

    public String userId() { return userId; }
    public String tenantId() { return tenantId; }

    private static String requireNonBlank(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
        return v;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identity other)) return false;
        return userId.equals(other.userId) && tenantId.equals(other.tenantId);
    }

    @Override public int hashCode() { return Objects.hash(userId, tenantId); }
}
