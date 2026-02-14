package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CompositeAiSecurityPolicy implements AiSecurityPolicyPort {

    private final List<SecurityRule> rules;

    public CompositeAiSecurityPolicy(List<SecurityRule> rules) {
        this.rules = List.copyOf(rules == null ? List.of() : rules);
    }

    @Override
    public Decision evaluate(ActorContext actor, PolicyInput input) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(input, "input");

        Decision.Outcome outcome = Decision.Outcome.ALLOW;
        Sanitization sanitization = Sanitization.none();
        List<String> reasons = new ArrayList<>();

        for (SecurityRule rule : rules) {
            var opt = rule.evaluate(actor, input);
            if (opt.isEmpty()) continue;

            SecurityRule.RuleResult res = opt.get();
            reasons.addAll(res.reasons());

            if (res.outcome() == Decision.Outcome.BLOCK) {
                return new Decision(Decision.Outcome.BLOCK, reasons, Sanitization.none());
            }
            if (res.outcome() == Decision.Outcome.ALLOW_WITH_SANITIZATION) {
                outcome = Decision.Outcome.ALLOW_WITH_SANITIZATION;
            }
            if (res.sanitization().stripInjectionPatterns() || res.sanitization().redactSecretsLikePatterns()) {
                sanitization = new Sanitization(
                        sanitization.stripInjectionPatterns() || res.sanitization().stripInjectionPatterns(),
                        sanitization.redactSecretsLikePatterns() || res.sanitization().redactSecretsLikePatterns()
                );
            }
        }

        if (outcome == Decision.Outcome.ALLOW_WITH_SANITIZATION
                && !sanitization.stripInjectionPatterns()
                && !sanitization.redactSecretsLikePatterns()) {
            sanitization = Sanitization.defaultHardening();
        }

        return new Decision(outcome, reasons, sanitization);
    }
}
