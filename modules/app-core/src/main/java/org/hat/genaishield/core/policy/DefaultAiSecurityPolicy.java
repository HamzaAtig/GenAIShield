package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DefaultAiSecurityPolicy implements AiSecurityPolicyPort {

    @Override
    public Decision evaluate(ActorContext actor, PolicyInput input) {
        String q = input.userPrompt().toLowerCase(Locale.ROOT);

        List<String> reasons = new ArrayList<>();

        // Hard blocks: explicit exfil / system prompt extraction attempts
        if (q.contains("system prompt") || q.contains("reveal") && (q.contains("key") || q.contains("token") || q.contains("secret"))) {
            reasons.add("Prompt asks for secrets/system prompt");
            return new Decision(Decision.Outcome.BLOCK, reasons, Sanitization.none());
        }

        // Allow but sanitize by default (anti indirect prompt injection)
        return new Decision(Decision.Outcome.ALLOW_WITH_SANITIZATION, reasons, Sanitization.defaultHardening());
    }
}
