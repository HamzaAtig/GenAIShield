package org.hat.genaishield.api.controller;

import org.hat.genaishield.api.dto.ChatRequest;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ChatControllerTest {

    @Test
    void sanitizes_question_before_use_case_call() {
        AtomicReference<ChatUseCase.ChatCommand> captured = new AtomicReference<>();
        ChatUseCase useCase = (actor, command) -> {
            captured.set(command);
            return new ChatUseCase.ChatResult("ok", List.of());
        };

        ChatController controller = new ChatController(useCase, new SensitiveDataSanitizer());
        ChatRequest req = new ChatRequest();
        req.sessionId = "s1";
        req.provider = "MISTRAL";
        req.question = "email me at john.doe@example.com";

        var res = controller.chat(req, "tenant-a", "alice", "USER");

        assertEquals("ok", res.answer);
        assertNotNull(captured.get());
        assertFalse(captured.get().question().contains("john.doe@example.com"));
        assertTrue(captured.get().question().contains("[EMAIL]"));
    }
}
