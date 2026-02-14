package org.hat.genaishield.infra.adapters.out;

import org.hat.genaishield.core.ports.out.PromptTemplatePort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClasspathPromptTemplateAdapter implements PromptTemplatePort {

    private final String basePath;

    public ClasspathPromptTemplateAdapter(String basePath) {
        this.basePath = (basePath == null || basePath.isBlank()) ? "prompts" : basePath;
    }

    @Override
    public String render(String templateId, Map<String, Object> variables) {
        String raw = load(templateId);
        return applyVariables(raw, variables);
    }

    private String load(String templateId) {
        try {
            // templateId "chat.system" -> "prompts/chat.system.txt"
            String path = basePath + "/" + templateId + ".txt";
            ClassPathResource r = new ClassPathResource(path);
            if (!r.exists()) throw new IllegalStateException("Missing template: " + path);
            return StreamUtils.copyToString(r.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load prompt template: " + templateId, e);
        }
    }

    private String applyVariables(String raw, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) return raw;
        String out = raw;
        for (Map.Entry<String, Object> e : variables.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            out = out.replace(key, e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        return out;
    }
}
