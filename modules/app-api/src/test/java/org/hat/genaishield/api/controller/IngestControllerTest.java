package org.hat.genaishield.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hat.genaishield.api.privacy.SensitiveDataSanitizer;
import org.hat.genaishield.core.domain.DocumentRef;
import org.hat.genaishield.core.ports.in.IngestDocumentUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IngestControllerTest {

    @Test
    void sanitizes_attributes_before_ingest_call() throws Exception {
        AtomicReference<IngestDocumentUseCase.IngestCommand> captured = new AtomicReference<>();
        IngestDocumentUseCase ingest = (actor, command) -> {
            captured.set(command);
            return new IngestDocumentUseCase.IngestResult(new DocumentRef("doc-1"));
        };

        IngestController controller = new IngestController(ingest, new ObjectMapper(), new SensitiveDataSanitizer());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "hello".getBytes()
        );

        var res = controller.ingest(
                file,
                "MISTRAL",
                "INTERNAL",
                "USER",
                "{\"email\":\"john.doe@example.com\",\"token\":\"apiKey=abc123\"}",
                "tenant-a",
                "alice",
                "USER"
        );

        assertEquals("doc-1", res.documentId);
        assertNotNull(captured.get());
        assertEquals("[EMAIL]", captured.get().attributes().get("email"));
        assertTrue(captured.get().attributes().get("token").contains("[SECRET]"));
    }
}
