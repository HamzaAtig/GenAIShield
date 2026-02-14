package org.hat.genaishield.api.privacy;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnonymizationContext {

    private final Map<String, String> tokenToOriginal = new LinkedHashMap<>();
    private final Map<SensitiveType, Integer> counters = new LinkedHashMap<>();

    public String register(SensitiveType type, String originalValue) {
        int next = counters.compute(type, (k, v) -> v == null ? 1 : v + 1);
        String token = "[[GS_" + type.name() + "_" + next + "]]";
        tokenToOriginal.put(token, originalValue);
        return token;
    }

    public String restore(String text) {
        if (text == null || text.isBlank() || tokenToOriginal.isEmpty()) {
            return text;
        }
        String out = text;
        for (Map.Entry<String, String> e : tokenToOriginal.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }
}
