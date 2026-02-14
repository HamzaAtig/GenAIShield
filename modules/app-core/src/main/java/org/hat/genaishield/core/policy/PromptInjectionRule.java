package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PromptInjectionRule implements SecurityRule {

    private static final List<String> MARKERS = List.of(
            "ignore previous instructions",
            "ignore all instructions",
            "system prompt",
            "reveal the system",
            "developer message",
            "jailbreak"
    );

    @Override
    public Optional<RuleResult> evaluate(ActorContext actor, AiSecurityPolicyPort.PolicyInput input) {
        for (String chunk : input.retrievedChunks()) {
            String t = chunk.toLowerCase(Locale.ROOT);
            for (String m : MARKERS) {
                if (t.contains(m)) {
                    return Optional.of(RuleResult.allowWithSanitization(
                            "Detected possible prompt-injection markers in retrieved context",
                            AiSecurityPolicyPort.Sanitization.defaultHardening()
                    ));
                }
            }
        }

        return Optional.empty();
    }
}
