package org.hat.genaishield.api.controller;

import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerMockMvcTest {

    @Test
    void chat_endpoint_sanitizes_and_returns_answer() throws Exception {
        AtomicReference<ChatUseCase.ChatCommand> captured = new AtomicReference<>();
        ChatUseCase useCase = new ChatUseCase() {
            @Override
            public ChatResult chat(ActorContext actor, ChatCommand command) {
                captured.set(command);
                return new ChatResult("ok-answer", new ArrayList<>(List.of(new Citation("doc-1", "c1", 0.9))));
            }
        };

        ChatController controller = new ChatController(useCase, new SensitiveDataSanitizer());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Tenant-Id", "tenant-a")
                        .header("X-User-Id", "alice")
                        .header("X-Roles", "USER")
                        .content("""
                                {"sessionId":"s1","provider":"MISTRAL","question":"Contact me at john.doe@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", is("ok-answer")))
                .andExpect(jsonPath("$.citations[0].documentId", is("doc-1")));

        assertEquals("Contact me at [EMAIL]", captured.get().question());
    }
}
