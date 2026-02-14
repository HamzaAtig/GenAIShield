package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.ActorContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface AiSecurityPolicyPort {

    Decision evaluate(ActorContext actor, PolicyInput input);

    final class PolicyInput {
        private final String userPrompt;
        private final List<String> retrievedChunks; // raw text (pre-sanitize)
        private final Map<String, Object> toolIntent; // optional: tool name + args

        public PolicyInput(String userPrompt, List<String> retrievedChunks, Map<String, Object> toolIntent) {
            this.userPrompt = requireNonBlank(userPrompt, "userPrompt");
            this.retrievedChunks = List.copyOf(retrievedChunks == null ? List.of() : retrievedChunks);
            this.toolIntent = Map.copyOf(toolIntent == null ? Map.of() : toolIntent);
        }

        public String userPrompt() {
            return userPrompt;
        }

        public List<String> retrievedChunks() {
            return retrievedChunks;
        }

        public Map<String, Object> toolIntent() {
            return toolIntent;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class Decision {
        public enum Outcome {ALLOW, BLOCK, ALLOW_WITH_SANITIZATION}

        private final Outcome outcome;
        private final List<String> reasons;
        private final Sanitization sanitization;

        public Decision(Outcome outcome, List<String> reasons, Sanitization sanitization) {
            this.outcome = Objects.requireNonNull(outcome, "outcome");
            this.reasons = List.copyOf(reasons == null ? List.of() : reasons);
            this.sanitization = sanitization == null ? Sanitization.none() : sanitization;
        }

        public Outcome outcome() {
            return outcome;
        }

        public List<String> reasons() {
            return reasons;
        }

        public Sanitization sanitization() {
            return sanitization;
        }
    }

    final class Sanitization {
        private final boolean stripInjectionPatterns;
        private final boolean redactSecretsLikePatterns;

        public Sanitization(boolean stripInjectionPatterns, boolean redactSecretsLikePatterns) {
            this.stripInjectionPatterns = stripInjectionPatterns;
            this.redactSecretsLikePatterns = redactSecretsLikePatterns;
        }

        public boolean stripInjectionPatterns() {
            return stripInjectionPatterns;
        }

        public boolean redactSecretsLikePatterns() {
            return redactSecretsLikePatterns;
        }

        public static Sanitization none() {
            return new Sanitization(false, false);
        }

        public static Sanitization defaultHardening() {
            return new Sanitization(true, true);
        }
    }
}
