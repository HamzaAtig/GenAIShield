package org.hat.genaishield.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.DocumentRef;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IngestControllerMockMvcTest {

    @Test
    void ingest_endpoint_sanitizes_attributes_and_returns_document_id() throws Exception {
        AtomicReference<IngestDocumentUseCase.IngestCommand> captured = new AtomicReference<>();
        IngestDocumentUseCase ingestUseCase = new IngestDocumentUseCase() {
            @Override
            public IngestResult ingest(ActorContext actor, IngestCommand command) {
                captured.set(command);
                return new IngestResult(new DocumentRef("doc-1"));
            }
        };

        IngestController controller = new IngestController(ingestUseCase, new ObjectMapper(), new SensitiveDataSanitizer());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest")
                        .file(file)
                        .param("provider", "MISTRAL")
                        .param("classification", "INTERNAL")
                        .param("allowedRoles", "USER,ANALYST")
                        .param("attributes", "{\"email\":\"john.doe@example.com\",\"token\":\"apiKey=abc\"}")
                        .header("X-Tenant-Id", "tenant-a")
                        .header("X-User-Id", "alice")
                        .header("X-Roles", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId", is("doc-1")));

        assertEquals("[EMAIL]", captured.get().attributes().get("email"));
        assertEquals("[SECRET]", captured.get().attributes().get("token"));
    }
}
