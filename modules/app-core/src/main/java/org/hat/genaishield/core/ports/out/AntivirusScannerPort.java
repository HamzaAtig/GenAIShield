package org.hat.genaishield.core.ports.out;

import org.hat.genaishield.core.domain.ActorContext;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public interface AntivirusScannerPort {

    ScanResult scan(ActorContext actor, ScanRequest request);

    final class ScanRequest {
        private final String filename;
        private final String contentType;
        private final long sizeBytes;
        private final InputStream stream;

        public ScanRequest(String filename, String contentType, long sizeBytes, InputStream stream) {
            this.filename = requireNonBlank(filename, "filename");
            this.contentType = requireNonBlank(contentType, "contentType");
            this.sizeBytes = sizeBytes;
            this.stream = Objects.requireNonNull(stream, "stream");
        }

        public String filename() {
            return filename;
        }

        public String contentType() {
            return contentType;
        }

        public long sizeBytes() {
            return sizeBytes;
        }

        public InputStream stream() {
            return stream;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class ScanResult {
        public enum Verdict {CLEAN, SUSPICIOUS, INFECTED, ERROR}

        private final Verdict verdict;
        private final Optional<String> signature;

        public ScanResult(Verdict verdict, String signature) {
            this.verdict = Objects.requireNonNull(verdict, "verdict");
            this.signature = Optional.ofNullable(signature).filter(s -> !s.isBlank());
        }

        public Verdict verdict() {
            return verdict;
        }

        public Optional<String> signature() {
            return signature;
        }

        public boolean isAllowed() {
            return verdict == Verdict.CLEAN || verdict == Verdict.SUSPICIOUS;
        }
    }
}
