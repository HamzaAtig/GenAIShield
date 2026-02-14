package org.hat.genaishield.core.ports.in;

import org.hat.genaishield.core.domain.ActorContext;
import org.hat.genaishield.core.domain.AiProviderId;
import org.hat.genaishield.core.domain.DocumentRef;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface ChatUseCase {

    ChatResult chat(ActorContext actor, ChatCommand command);

    final class ChatCommand {
        private final String sessionId;
        private final String question;
        private final AiProviderId provider;
        private final Optional<DocumentRef> restrictToDocument;

        public ChatCommand(String sessionId, String question, AiProviderId provider, DocumentRef restrictToDocument) {
            this.sessionId = requireNonBlank(sessionId, "sessionId");
            this.question = requireNonBlank(question, "question");
            this.provider = Objects.requireNonNull(provider, "provider");
            this.restrictToDocument = Optional.ofNullable(restrictToDocument);
        }

        public String sessionId() {
            return sessionId;
        }

        public String question() {
            return question;
        }

        public AiProviderId provider() {
            return provider;
        }

        public Optional<DocumentRef> restrictToDocument() {
            return restrictToDocument;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class ChatResult {
        private final String answer;
        private final List<Citation> citations;

        public ChatResult(String answer, List<Citation> citations) {
            this.answer = requireNonBlank(answer, "answer");
            this.citations = List.copyOf(citations == null ? List.of() : citations);
        }

        public String answer() {
            return answer;
        }

        public List<Citation> citations() {
            return citations;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }

    final class Citation {
        private final String documentId;
        private final String chunkId;
        private final double score;

        public Citation(String documentId, String chunkId, double score) {
            this.documentId = requireNonBlank(documentId, "documentId");
            this.chunkId = requireNonBlank(chunkId, "chunkId");
            this.score = score;
        }

        public String documentId() {
            return documentId;
        }

        public String chunkId() {
            return chunkId;
        }

        public double score() {
            return score;
        }

        private static String requireNonBlank(String v, String name) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " must be non-blank");
            return v;
        }
    }
}
