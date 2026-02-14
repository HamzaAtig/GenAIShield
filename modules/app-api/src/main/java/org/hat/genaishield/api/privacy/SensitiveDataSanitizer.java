package org.hat.genaishield.api.privacy;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class SensitiveDataSanitizer {

    private static final Pattern EMAIL = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern PHONE = Pattern.compile("(?<!\\w)(?:\\+\\d{1,3}[ -]?)?(?:\\(?\\d{2,4}\\)?[ -]?)?\\d{3,4}[ -]?\\d{3,4}(?!\\w)");
    private static final Pattern IBAN = Pattern.compile("\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b");
    private static final Pattern CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");
    private static final Pattern API_KEY = Pattern.compile("(?i)\\b(api[_-]?key|token|secret|password)\\b\\s*[:=]\\s*\\S+");

    public record SanitizationResult<T>(T value, AnonymizationContext context) {}

    public <T> T sanitizeAnnotated(T target) {
        return sanitizeAnnotated(target, null);
    }

    public <T> SanitizationResult<T> sanitizeAnnotatedWithContext(T target) {
        AnonymizationContext context = new AnonymizationContext();
        T sanitized = sanitizeAnnotated(target, context);
        return new SanitizationResult<>(sanitized, context);
    }

    private <T> T sanitizeAnnotated(T target, AnonymizationContext context) {
        if (target == null) return null;
        for (Field f : target.getClass().getDeclaredFields()) {
            Sensitive meta = f.getAnnotation(Sensitive.class);
            if (meta == null || f.getType() != String.class) continue;
            f.setAccessible(true);
            try {
                String value = (String) f.get(target);
                if (value == null || value.isBlank()) continue;
                f.set(target, sanitizeValue(value, meta.type(), meta.action(), context));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to sanitize field: " + f.getName(), e);
            }
        }
        return target;
    }

    public Map<String, String> sanitizeAttributes(Map<String, String> attrs) {
        if (attrs == null || attrs.isEmpty()) return Map.of();
        Map<String, String> out = new HashMap<>();
        attrs.forEach((k, v) -> out.put(k, sanitizeFreeText(v)));
        return out;
    }

    public String sanitizeFreeText(String text) {
        return sanitizeFreeText(text, null);
    }

    public String sanitizeFreeText(String text, AnonymizationContext context) {
        if (text == null || text.isBlank()) return text;
        String out = text;
        out = replaceWithTokenOrLiteral(out, EMAIL, SensitiveType.EMAIL, "[EMAIL]", context);
        out = replaceWithTokenOrLiteral(out, PHONE, SensitiveType.PHONE, "[PHONE]", context);
        out = replaceWithTokenOrLiteral(out, IBAN, SensitiveType.IBAN, "[IBAN]", context);
        out = replaceWithTokenOrLiteral(out, CARD, SensitiveType.CARD_PAN, "[CARD_PAN]", context);
        out = replaceWithTokenOrLiteral(out, API_KEY, SensitiveType.API_KEY, "[SECRET]", context);
        return out;
    }

    private String sanitizeValue(String value, SensitiveType type, SensitiveAction action, AnonymizationContext context) {
        if (type == SensitiveType.FREE_TEXT) {
            return sanitizeFreeText(value, context);
        }
        return switch (action) {
            case REDACT -> "[" + type.name() + "_REDACTED]";
            case MASK -> maskValue(value, type);
            case PSEUDONYMIZE -> pseudonym(type, value);
        };
    }

    private static String replaceWithTokenOrLiteral(String input,
                                                    Pattern pattern,
                                                    SensitiveType type,
                                                    String fallback,
                                                    AnonymizationContext context) {
        if (context == null) {
            return pattern.matcher(input).replaceAll(fallback);
        }

        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group();
            String token = context.register(type, Objects.requireNonNullElse(matched, ""));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskValue(String value, SensitiveType type) {
        return switch (type) {
            case EMAIL -> value.replaceAll("(^.).*(@.*$)", "$1***$2");
            case PHONE -> value.replaceAll("\\d(?=\\d{2})", "*");
            case IBAN, CARD_PAN, API_KEY, PERSON_NAME, FREE_TEXT ->
                    value.length() <= 4 ? "****" : value.substring(0, 2) + "***" + value.substring(value.length() - 2);
        };
    }

    private static String pseudonym(SensitiveType type, String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return "[" + type.name() + "_" + sb + "]";
        } catch (Exception e) {
            return "[" + type.name() + "_PSEUDO]";
        }
    }
}
