package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.Optional;

public final class DefaultSanitizationRule implements SecurityRule {

    @Override
    public Optional<RuleResult> evaluate(ActorContext actor, AiSecurityPolicyPort.PolicyInput input) {
        if (input.retrievedChunks().isEmpty()) return Optional.empty();
        return Optional.of(RuleResult.allowWithSanitization(
                "Apply default sanitization for retrieved context",
                AiSecurityPolicyPort.Sanitization.defaultHardening()
        ));
    }
}
