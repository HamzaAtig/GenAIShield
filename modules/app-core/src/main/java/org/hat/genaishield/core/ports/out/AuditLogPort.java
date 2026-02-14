package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.ActorContext;

import java.util.Map;

public interface AuditLogPort {

    void logEvent(ActorContext actor, String eventType, Map<String, Object> data);
}
