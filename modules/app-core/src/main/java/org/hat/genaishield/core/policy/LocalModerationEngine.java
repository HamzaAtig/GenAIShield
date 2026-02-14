package org.hat.genaishield.core.policy;

import java.util.*;
import java.util.regex.Pattern;

public final class LocalModerationEngine {

    public record ModerationResult(double score, Map<String, Integer> categoryHits) {
        public boolean isSevere(double threshold) {
            return score >= threshold;
        }
    }

    private final List<Rule> rules;

    public LocalModerationEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules == null ? List.of() : rules);
    }

    public ModerationResult evaluate(String text) {
        if (text == null || text.isBlank()) return new ModerationResult(0.0, Map.of());

        double score = 0.0;
        Map<String, Integer> hits = new HashMap<>();
        String t = text.toLowerCase(Locale.ROOT);

        for (Rule r : rules) {
            int count = r.countMatches(t);
            if (count > 0) {
                hits.put(r.category, count);
                score += (r.weight * count);
            }
        }

        return new ModerationResult(score, Map.copyOf(hits));
    }

    public static LocalModerationEngine defaultEngine() {
        return new LocalModerationEngine(List.of(
                new Rule("self-harm", 2.0, Pattern.compile("\\b(suicide|kill myself|self harm|cut myself)\\b")),
                new Rule("violence", 1.5, Pattern.compile("\\b(kill|murder|assassinate|shoot|bomb)\\b")),
                new Rule("hate", 1.5, Pattern.compile("\\b(genocide|exterminate)\\b")),
                new Rule("sexual", 1.0, Pattern.compile("\\b(rape|incest|child pornography)\\b")),
                new Rule("abuse", 1.0, Pattern.compile("\\b(threaten|blackmail|extort)\\b"))
        ));
    }

    public static final class Rule {
        private final String category;
        private final double weight;
        private final Pattern pattern;

        public Rule(String category, double weight, Pattern pattern) {
            this.category = Objects.requireNonNull(category, "category");
            this.weight = weight;
            this.pattern = Objects.requireNonNull(pattern, "pattern");
        }

        int countMatches(String text) {
            int count = 0;
            var m = pattern.matcher(text);
            while (m.find()) count++;
            return count;
        }
    }
}
