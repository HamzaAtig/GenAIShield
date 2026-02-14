package org.hat.genaishield.infra.adapters.out;

import org.hat.genaishield.core.ports.out.TextSplitterPort;

import java.util.ArrayList;
import java.util.List;

public final class SimpleTextSplitterAdapter implements TextSplitterPort {

    @Override
    public List<String> split(String text, SplitConfig config) {
        if (text == null || text.isBlank()) return List.of();
        int chunkSize = Math.max(1, config.chunkSize());
        int overlap = Math.max(0, config.overlap());

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + chunkSize);
            String chunk = text.substring(start, end).trim();
            if (!chunk.isBlank()) chunks.add(chunk);
            if (end == text.length()) break;
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }
}
