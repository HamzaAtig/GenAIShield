package org.hat.genaishield.api.controller;

import org.hat.genaishield.api.dto.ChatRequest;
import org.hat.genaishield.api.dto.ChatResponse;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.DocumentRef;
import org.hat.genaishield.core.domain.Identity;
import org.hat.genaishield.core.ports.in.ChatUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final SensitiveDataSanitizer sanitizer;

    public ChatController(ChatUseCase chatUseCase, SensitiveDataSanitizer sanitizer) {
        this.chatUseCase = chatUseCase;
        this.sanitizer = sanitizer;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest req,
                             @RequestHeader(value = "X-Tenant-Id", defaultValue = "demo") String tenantId,
                             @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                             @RequestHeader(value = "X-Roles", defaultValue = "USER") String rolesCsv) {
        ChatRequest sanitizedReq = sanitizer.sanitizeAnnotated(req);

        ActorContext actor = new ActorContext(
                new Identity(userId, tenantId),
                Set.of(rolesCsv.split(","))
        );

        var cmd = new ChatUseCase.ChatCommand(
                sanitizedReq.sessionId == null ? "default" : sanitizedReq.sessionId,
                sanitizedReq.question,
                AiProviderId.from(sanitizedReq.provider == null ? "MISTRAL" : sanitizedReq.provider),
                (sanitizedReq.documentId == null || sanitizedReq.documentId.isBlank()) ? null : new DocumentRef(sanitizedReq.documentId)
        );

        var res = chatUseCase.chat(actor, cmd);

        ChatResponse out = new ChatResponse();
        out.answer = res.answer();
        out.citations = res.citations().stream().map(c -> {
            ChatResponse.CitationDto dto = new ChatResponse.CitationDto();
            dto.documentId = c.documentId();
            dto.chunkId = c.chunkId();
            dto.score = c.score();
            return dto;
        }).toList();

        return out;
    }
}
