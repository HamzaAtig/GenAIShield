package org.hat.genaishield.core.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ActorContext {
    private final Identity identity;
    private final Set<String> roles;

    public ActorContext(Identity identity, Set<String> roles) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(roles == null ? Set.of() : roles));
    }

    public Identity identity() {
        return identity;
    }

    public Set<String> roles() {
        return roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
