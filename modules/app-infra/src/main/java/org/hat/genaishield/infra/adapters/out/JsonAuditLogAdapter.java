package org.hat.genaishield.infra.adapters.out;

import net.logstash.logback.argument.StructuredArguments;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AuditLogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class JsonAuditLogAdapter implements AuditLogPort {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Override
    public void logEvent(ActorContext actor, String eventType, Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("tenantId", actor.identity().tenantId());
        payload.put("userId", actor.identity().userId());
        payload.put("roles", actor.roles());
        if (data != null) payload.putAll(data);

        AUDIT.info("audit",
                StructuredArguments.entries(payload)
        );
    }
}
