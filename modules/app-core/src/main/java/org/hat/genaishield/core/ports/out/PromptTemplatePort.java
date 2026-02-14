package org.hat.genaishield.core.ports.out;

import java.util.Map;

public interface PromptTemplatePort {

    /**
     * Render a named template (e.g. "chat.system", "chat.user") using provided variables.
     */
    String render(String templateId, Map<String, Object> variables);
}
