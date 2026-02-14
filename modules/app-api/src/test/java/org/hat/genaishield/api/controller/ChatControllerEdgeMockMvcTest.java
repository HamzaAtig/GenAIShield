package org.hat.genaishield.api.controller;

import org.hat.genaishield.api.error.ApiExceptionHandler;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerEdgeMockMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatUseCase useCase = new ChatUseCase() {
            @Override
            public ChatResult chat(ActorContext actor, ChatCommand command) {
                return new ChatResult("ok", List.of());
            }
        };
        ChatController controller = new ChatController(useCase, new SensitiveDataSanitizer());
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returns_400_when_provider_is_invalid() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"s1","provider":"BAD","question":"hello"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void returns_400_when_question_missing() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"s1","provider":"MISTRAL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void uses_default_headers_when_absent() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"s1","provider":"MISTRAL","question":"hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", is("ok")));
    }
}
