package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.Optional;

public final class ModerationRule implements SecurityRule {

    private final LocalModerationEngine engine;
    private final double blockThreshold;
    private final double warnThreshold;

    public ModerationRule(LocalModerationEngine engine, double warnThreshold, double blockThreshold) {
        this.engine = engine == null ? LocalModerationEngine.defaultEngine() : engine;
        this.warnThreshold = warnThreshold <= 0 ? 2.0 : warnThreshold;
        this.blockThreshold = blockThreshold <= 0 ? 4.0 : blockThreshold;
    }

    @Override
    public Optional<RuleResult> evaluate(ActorContext actor, AiSecurityPolicyPort.PolicyInput input) {
        var res = engine.evaluate(input.userPrompt());
        if (res.isSevere(blockThreshold)) {
            return Optional.of(RuleResult.block("Prompt flagged by moderation (score=" + res.score() + ")"));
        }
        if (res.isSevere(warnThreshold)) {
            return Optional.of(RuleResult.allowWithSanitization(
                    "Prompt flagged by moderation (score=" + res.score() + ")",
                    AiSecurityPolicyPort.Sanitization.defaultHardening()
            ));
        }
        return Optional.empty();
    }
}
