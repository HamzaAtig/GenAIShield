package org.hat.genaishield.api.security;

import org.hat.genaishield.api.controller.ChatController;
import org.hat.genaishield.api.error.ApiExceptionHandler;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitingFilterMockMvcTest {

    @Test
    void returns_429_after_capacity_exceeded_with_mockmvc() throws Exception {
        ChatUseCase useCase = new ChatUseCase() {
            @Override
            public ChatResult chat(ActorContext actor, ChatCommand command) {
                return new ChatResult("ok", List.of());
            }
        };
        ChatController controller = new ChatController(useCase, new SensitiveDataSanitizer());

        RateLimitProperties props = new RateLimitProperties();
        props.setCapacityPerMinute(1);
        RateLimitingFilter filter = new RateLimitingFilter(props);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .addFilters(filter)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Tenant-Id", "tenant-a")
                        .header("X-User-Id", "alice")
                        .header("X-Roles", "USER")
                        .content("""
                                {"sessionId":"s1","provider":"MISTRAL","question":"hello"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Tenant-Id", "tenant-a")
                        .header("X-User-Id", "alice")
                        .header("X-Roles", "USER")
                        .content("""
                                {"sessionId":"s2","provider":"MISTRAL","question":"hello"}
                                """))
                .andExpect(status().isTooManyRequests());
    }
}
