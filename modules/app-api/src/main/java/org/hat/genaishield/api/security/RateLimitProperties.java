package org.hat.genaishield.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {
    private long capacityPerMinute = 60;

    public long getCapacityPerMinute() {
        return capacityPerMinute;
    }

    public void setCapacityPerMinute(long capacityPerMinute) {
        this.capacityPerMinute = capacityPerMinute;
    }
}
