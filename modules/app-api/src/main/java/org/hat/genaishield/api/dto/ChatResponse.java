package org.hat.genaishield.api.dto;

import java.util.List;

public class ChatResponse {
    public String answer;
    public List<CitationDto> citations;

    public static class CitationDto {
        public String documentId;
        public String chunkId;
        public double score;
    }
}
