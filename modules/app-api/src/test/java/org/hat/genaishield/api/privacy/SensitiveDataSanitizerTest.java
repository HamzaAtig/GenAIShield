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
    void contextual_sanitization_restores_original_values_on_output() {
        SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();
        ChatRequest req = new ChatRequest();
        req.question = "Contact john.doe@example.com and +33 612345678";

        SensitiveDataSanitizer.SanitizationResult<ChatRequest> result = sanitizer.sanitizeAnnotatedWithContext(req);

        assertNotNull(result.value().question);
        assertFalse(result.value().question.contains("john.doe@example.com"));
        assertFalse(result.value().question.contains("612345678"));
        assertTrue(result.value().question.contains("[[GS_EMAIL_1]]"));
        assertTrue(result.value().question.contains("[[GS_PHONE_1]]"));

        String llmOutput = "User is [[GS_EMAIL_1]] and phone is [[GS_PHONE_1]].";
        String restored = result.context().restore(llmOutput);
        assertEquals("User is john.doe@example.com and phone is +33 612345678.", restored);
    }

    @Test
    void contextual_sanitization_preserves_international_phone_format_on_restore() {
        SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();
        ChatRequest req = new ChatRequest();
        req.question = "Call me on +1 (415) 555-0199";

        SensitiveDataSanitizer.SanitizationResult<ChatRequest> result = sanitizer.sanitizeAnnotatedWithContext(req);

        assertTrue(result.value().question.contains("[[GS_PHONE_1]]"));
        String restored = result.context().restore("Dial [[GS_PHONE_1]] now.");
        assertEquals("Dial +1 (415) 555-0199 now.", restored);
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
