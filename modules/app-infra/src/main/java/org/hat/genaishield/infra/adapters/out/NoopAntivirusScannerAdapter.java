package org.hat.genaishield.infra.adapters.out;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.ports.out.AntivirusScannerPort;

public final class NoopAntivirusScannerAdapter implements AntivirusScannerPort {

    @Override
    public ScanResult scan(ActorContext actor, ScanRequest request) {
        return new ScanResult(ScanResult.Verdict.CLEAN, null);
    }
}
