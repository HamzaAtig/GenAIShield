package org.hat.genaishield.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hat.genaishield.api.error.ApiExceptionHandler;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.DocumentRef;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IngestControllerEdgeMockMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        IngestDocumentUseCase ingestUseCase = new IngestDocumentUseCase() {
            @Override
            public IngestResult ingest(ActorContext actor, IngestCommand command) {
                if ("application/pdf".equals(command.contentType())) {
                    throw new IllegalArgumentException("Unsupported content type");
                }
                return new IngestResult(new DocumentRef("doc-ok"));
            }
        };

        IngestController controller = new IngestController(
                ingestUseCase,
                new ObjectMapper(),
                new SensitiveDataSanitizer()
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returns_400_when_classification_invalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "text/plain", "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest")
                        .file(file)
                        .param("provider", "MISTRAL")
                        .param("classification", "NOT_VALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void returns_400_when_attributes_json_invalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "text/plain", "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest")
                        .file(file)
                        .param("provider", "MISTRAL")
                        .param("classification", "INTERNAL")
                        .param("attributes", "{\"broken\":}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void returns_400_when_content_type_not_allowed() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.pdf", "application/pdf", "binary".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ingest")
                        .file(file)
                        .param("provider", "MISTRAL")
                        .param("classification", "INTERNAL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void returns_400_when_file_missing() throws Exception {
        mockMvc.perform(multipart("/api/v1/ingest")
                        .param("provider", "MISTRAL")
                        .param("classification", "INTERNAL"))
                .andExpect(status().isBadRequest());
    }
}
