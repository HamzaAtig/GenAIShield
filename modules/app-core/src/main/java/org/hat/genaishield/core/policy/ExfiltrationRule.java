package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.Locale;
import java.util.Optional;

public final class ExfiltrationRule implements SecurityRule {

    @Override
    public Optional<RuleResult> evaluate(ActorContext actor, AiSecurityPolicyPort.PolicyInput input) {
        String q = input.userPrompt().toLowerCase(Locale.ROOT);

        boolean asksSystem = q.contains("system prompt") || q.contains("prompt system") || q.contains("system message");
        boolean asksSecrets = q.contains("secret") || q.contains("token") || q.contains("api key") || q.contains("apikey")
                || q.contains("password") || q.contains("credentials");
        boolean triesReveal = q.contains("reveal") || q.contains("show") || q.contains("leak") || q.contains("exfiltrate");

        if (asksSystem || (triesReveal && asksSecrets)) {
            return Optional.of(RuleResult.block("Prompt asks for system prompt or secrets"));
        }

        return Optional.empty();
    }
}
