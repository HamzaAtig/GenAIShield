package org.hat.genaishield.api.privacy;

import org.hat.genaishield.api.dto.ChatRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataSanitizerTest {

    @Test
    void sanitizes_annotated_dto_fields() {
        SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();
        ChatRequest req = new ChatRequest();
        req.question = "Contact me at john.doe@example.com and +33 612345678";

        ChatRequest out = sanitizer.sanitizeAnnotated(req);

        assertNotNull(out.question);
        assertFalse(out.question.contains("john.doe@example.com"));
        assertFalse(out.question.contains("612345678"));
        assertTrue(out.question.contains("[EMAIL]"));
        assertTrue(out.question.contains("[PHONE]"));
    }

    @Test
    void sanitizes_free_text_secret_patterns() {
        SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();
        String out = sanitizer.sanitizeFreeText("apiKey=abc123 token=xyz");
        assertTrue(out.contains("[SECRET]"));
    }

    @Test
    void supports_mask_redact_and_pseudonymize_actions() {
        SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();
        ProbeDto dto = new ProbeDto();
        dto.email = "john.doe@example.com";
        dto.phone = "+33612345678";
        dto.name = "John Doe";

        ProbeDto out = sanitizer.sanitizeAnnotated(dto);

        assertTrue(out.email.contains("***@"));
        assertEquals("[PHONE_REDACTED]", out.phone);
        assertTrue(out.name.startsWith("[PERSON_NAME_"));
    }

    static class ProbeDto {
        @Sensitive(type = SensitiveType.EMAIL, action = SensitiveAction.MASK)
        String email;

        @Sensitive(type = SensitiveType.PHONE, action = SensitiveAction.REDACT)
        String phone;

        @Sensitive(type = SensitiveType.PERSON_NAME, action = SensitiveAction.PSEUDONYMIZE)
        String name;
    }
}
