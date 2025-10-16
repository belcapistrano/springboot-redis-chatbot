package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class TokenCountingService {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[.,!?;:\"'()\\[\\]{}]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private static final double AVERAGE_TOKENS_PER_WORD = 1.3;
    private static final int PUNCTUATION_TOKEN_COUNT = 1;

    public int estimateTokenCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        text = text.trim();

        long wordCount = WORD_PATTERN.matcher(text).results().count();
        long punctuationCount = PUNCTUATION_PATTERN.matcher(text).results().count();

        int estimatedTokens = (int) Math.ceil(wordCount * AVERAGE_TOKENS_PER_WORD) +
                             (int) punctuationCount * PUNCTUATION_TOKEN_COUNT;

        return Math.max(estimatedTokens, 1);
    }

    public int estimateTokenCount(ChatMessage message) {
        if (message == null || message.getContent() == null) {
            return 0;
        }

        int contentTokens = estimateTokenCount(message.getContent());
        int roleTokens = 3;

        return contentTokens + roleTokens;
    }

    public int estimateTokenCount(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        return messages.stream()
                .mapToInt(this::estimateTokenCount)
                .sum();
    }

    public TokenSummary analyzeSession(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new TokenSummary(0, 0, 0, 0, 0);
        }

        int totalTokens = 0;
        int userTokens = 0;
        int assistantTokens = 0;
        int messageCount = messages.size();

        for (ChatMessage message : messages) {
            int tokens = estimateTokenCount(message);
            totalTokens += tokens;

            if (message.getRole() != null) {
                switch (message.getRole()) {
                    case USER:
                        userTokens += tokens;
                        break;
                    case ASSISTANT:
                        assistantTokens += tokens;
                        break;
                }
            }
        }

        double averageTokensPerMessage = messageCount > 0 ? (double) totalTokens / messageCount : 0;

        return new TokenSummary(totalTokens, userTokens, assistantTokens, messageCount, averageTokensPerMessage);
    }

    public boolean exceedsContextWindow(List<ChatMessage> messages, int contextWindowSize) {
        return estimateTokenCount(messages) > contextWindowSize;
    }

    public int getTokensToTrim(List<ChatMessage> messages, int contextWindowSize, int targetTokens) {
        int totalTokens = estimateTokenCount(messages);

        if (totalTokens <= targetTokens) {
            return 0;
        }

        return totalTokens - targetTokens;
    }

    public static class TokenSummary {
        private final int totalTokens;
        private final int userTokens;
        private final int assistantTokens;
        private final int messageCount;
        private final double averageTokensPerMessage;

        public TokenSummary(int totalTokens, int userTokens, int assistantTokens,
                           int messageCount, double averageTokensPerMessage) {
            this.totalTokens = totalTokens;
            this.userTokens = userTokens;
            this.assistantTokens = assistantTokens;
            this.messageCount = messageCount;
            this.averageTokensPerMessage = averageTokensPerMessage;
        }

        public int getTotalTokens() { return totalTokens; }
        public int getUserTokens() { return userTokens; }
        public int getAssistantTokens() { return assistantTokens; }
        public int getMessageCount() { return messageCount; }
        public double getAverageTokensPerMessage() { return averageTokensPerMessage; }

        public double getTokenEfficiencyRatio() {
            return totalTokens > 0 ? (double) messageCount / totalTokens : 0;
        }
    }
}