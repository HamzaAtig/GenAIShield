package org.hat.genaishield.core.ports.out;

import java.util.List;

public interface TextSplitterPort {

    List<String> split(String text, SplitConfig config);

    final class SplitConfig {
        private final int chunkSize;
        private final int overlap;

        public SplitConfig(int chunkSize, int overlap) {
            this.chunkSize = chunkSize <= 0 ? 800 : chunkSize;
            this.overlap = Math.max(0, overlap);
        }

        public int chunkSize() { return chunkSize; }
        public int overlap() { return overlap; }
    }
}
