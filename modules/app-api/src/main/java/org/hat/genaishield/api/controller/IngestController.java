package org.hat.genaishield.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hat.genaishield.api.dto.IngestResponse;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.Classification;
import org.hat.genaishield.core.domain.Identity;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final IngestDocumentUseCase ingest;
    private final ObjectMapper objectMapper;
    private final SensitiveDataSanitizer sanitizer;

    public IngestController(IngestDocumentUseCase ingest, ObjectMapper objectMapper, SensitiveDataSanitizer sanitizer) {
        this.ingest = ingest;
        this.objectMapper = objectMapper;
        this.sanitizer = sanitizer;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IngestResponse ingest(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "provider", required = false) String provider,
                                 @RequestParam(value = "classification", required = false) String classification,
                                 @RequestParam(value = "allowedRoles", required = false) String allowedRolesCsv,
                                 @RequestParam(value = "attributes", required = false) String attributesJson,
                                 @RequestHeader(value = "X-Tenant-Id", defaultValue = "demo") String tenantId,
                                 @RequestHeader(value = "X-User-Id", defaultValue = "demo-user") String userId,
                                 @RequestHeader(value = "X-Roles", defaultValue = "USER") String rolesCsv) throws IOException {

        ActorContext actor = new ActorContext(
                new Identity(userId, tenantId),
                Set.of(rolesCsv.split(","))
        );

        List<String> allowedRoles = allowedRolesCsv == null || allowedRolesCsv.isBlank()
                ? List.of()
                : List.of(allowedRolesCsv.split(",")).stream().map(String::trim).filter(s -> !s.isBlank()).toList();

        Map<String, String> attrs = sanitizer.sanitizeAttributes(parseAttributes(attributesJson));

        var cmd = new IngestDocumentUseCase.IngestCommand(
                file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename(),
                file.getContentType() == null ? "text/plain" : file.getContentType(),
                file.getSize(),
                AiProviderId.from(provider == null ? "MISTRAL" : provider),
                classification == null ? Classification.INTERNAL : Classification.valueOf(classification.toUpperCase(Locale.ROOT)),
                allowedRoles,
                attrs,
                file.getInputStream()
        );

        var res = ingest.ingest(actor, cmd);

        IngestResponse out = new IngestResponse();
        out.documentId = res.document().id();
        return out;
    }

    private Map<String, String> parseAttributes(String json) throws IOException {
        if (json == null || json.isBlank()) return Map.of();
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }
}
