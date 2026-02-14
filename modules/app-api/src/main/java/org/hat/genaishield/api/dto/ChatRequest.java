package org.hat.genaishield.api.dto;

import org.hat.genaishield.api.privacy.Sensitive;
import org.hat.genaishield.api.privacy.SensitiveAction;
import org.hat.genaishield.api.privacy.SensitiveType;

public class ChatRequest {
    public String sessionId;

    @Sensitive(type = SensitiveType.FREE_TEXT, action = SensitiveAction.MASK)
    public String question;

    public String provider;     // "MISTRAL" or "OLLAMA"

    public String documentId;   // optional
}
