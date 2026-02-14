package org.hat.genaishield.api.dto;

public class ChatRequest {
    public String sessionId;
    public String question;
    public String provider;     // "MISTRAL" or "OLLAMA"
    public String documentId;   // optional
}
