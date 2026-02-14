package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface SecurityRule {

    Optional<RuleResult> evaluate(ActorContext actor, AiSecurityPolicyPort.PolicyInput input);

    final class RuleResult {
        private final AiSecurityPolicyPort.Decision.Outcome outcome;
        private final List<String> reasons;
        private final AiSecurityPolicyPort.Sanitization sanitization;

        public RuleResult(AiSecurityPolicyPort.Decision.Outcome outcome,
                          List<String> reasons,
                          AiSecurityPolicyPort.Sanitization sanitization) {
            this.outcome = Objects.requireNonNull(outcome, "outcome");
            this.reasons = List.copyOf(reasons == null ? List.of() : reasons);
            this.sanitization = sanitization == null ? AiSecurityPolicyPort.Sanitization.none() : sanitization;
        }

        public AiSecurityPolicyPort.Decision.Outcome outcome() {
            return outcome;
        }

        public List<String> reasons() {
            return reasons;
        }

        public AiSecurityPolicyPort.Sanitization sanitization() {
            return sanitization;
        }

        public static RuleResult block(String reason) {
            return new RuleResult(AiSecurityPolicyPort.Decision.Outcome.BLOCK, List.of(reason), AiSecurityPolicyPort.Sanitization.none());
        }

        public static RuleResult allowWithSanitization(String reason, AiSecurityPolicyPort.Sanitization s) {
            return new RuleResult(AiSecurityPolicyPort.Decision.Outcome.ALLOW_WITH_SANITIZATION, List.of(reason), s);
        }

        public static RuleResult allow(String reason) {
            return new RuleResult(AiSecurityPolicyPort.Decision.Outcome.ALLOW, List.of(reason), AiSecurityPolicyPort.Sanitization.none());
        }
    }
}
