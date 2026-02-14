package org.hat.genaishield.core.policy;

import org.hat.genaishield.core.ports.out.AiSecurityPolicyPort;

import java.util.List;

public final class DefaultAiSecurityPolicy implements AiSecurityPolicyPort {

    private final CompositeAiSecurityPolicy delegate;

    public DefaultAiSecurityPolicy() {
        this(2.0, 4.0);
    }

    public DefaultAiSecurityPolicy(double warnThreshold, double blockThreshold) {
        this.delegate = new CompositeAiSecurityPolicy(List.of(
                new ExfiltrationRule(),
                new ModerationRule(LocalModerationEngine.defaultEngine(), warnThreshold, blockThreshold),
                new PromptInjectionRule(),
                new DefaultSanitizationRule()
        ));
    }

    @Override
    public Decision evaluate(org.hat.genaishield.core.domain.ActorContext actor, PolicyInput input) {
        return delegate.evaluate(actor, input);
    }
}
