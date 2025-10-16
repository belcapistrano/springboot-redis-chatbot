package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextCompressionService {

    @Autowired
    private TokenCountingService tokenCountingService;

    private static final int DEFAULT_CONTEXT_WINDOW = 4000;
    private static final int COMPRESSION_TARGET_TOKENS = 2000;
    private static final int MIN_RECENT_MESSAGES = 10;
    private static final int MAX_SUMMARY_TOKENS = 500;

    public CompressionResult compressContext(List<ChatMessage> messages, int contextWindowSize) {
        if (messages == null || messages.isEmpty()) {
            return new CompressionResult(messages, "", false, 0, 0);
        }

        int totalTokens = tokenCountingService.estimateTokenCount(messages);

        if (totalTokens <= contextWindowSize) {
            return new CompressionResult(messages, "", false, totalTokens, 0);
        }

        int targetTokens = Math.min(COMPRESSION_TARGET_TOKENS, contextWindowSize - MAX_SUMMARY_TOKENS);

        List<ChatMessage> recentMessages = preserveRecentMessages(messages, targetTokens);
        String conversationSummary = generateConversationSummary(messages, recentMessages);

        int finalTokenCount = tokenCountingService.estimateTokenCount(recentMessages) +
                             tokenCountingService.estimateTokenCount(conversationSummary);
        int tokensRemoved = totalTokens - finalTokenCount;

        return new CompressionResult(recentMessages, conversationSummary, true, finalTokenCount, tokensRemoved);
    }

    public CompressionResult compressContext(List<ChatMessage> messages) {
        return compressContext(messages, DEFAULT_CONTEXT_WINDOW);
    }

    private List<ChatMessage> preserveRecentMessages(List<ChatMessage> messages, int targetTokens) {
        if (messages.size() <= MIN_RECENT_MESSAGES) {
            return new ArrayList<>(messages);
        }

        List<ChatMessage> recentMessages = new ArrayList<>();
        int currentTokens = 0;
        int messagesPreserved = 0;

        for (int i = messages.size() - 1; i >= 0 && messagesPreserved < MIN_RECENT_MESSAGES; i--) {
            ChatMessage message = messages.get(i);
            int messageTokens = tokenCountingService.estimateTokenCount(message);

            if (currentTokens + messageTokens <= targetTokens || messagesPreserved < MIN_RECENT_MESSAGES) {
                recentMessages.add(0, message);
                currentTokens += messageTokens;
                messagesPreserved++;
            } else {
                break;
            }
        }

        return recentMessages;
    }

    private String generateConversationSummary(List<ChatMessage> allMessages, List<ChatMessage> recentMessages) {
        List<ChatMessage> messagesToSummarize = new ArrayList<>();

        for (ChatMessage message : allMessages) {
            if (!recentMessages.contains(message)) {
                messagesToSummarize.add(message);
            }
        }

        if (messagesToSummarize.isEmpty()) {
            return "";
        }

        return createSummary(messagesToSummarize);
    }

    private String createSummary(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return "";
        }

        Map<String, Integer> topicCounts = new HashMap<>();
        List<String> keyExchanges = new ArrayList<>();
        int userMessages = 0;
        int assistantMessages = 0;

        for (ChatMessage message : messages) {
            if (message.getRole() == MessageRole.USER) {
                userMessages++;
                extractTopics(message.getContent(), topicCounts);

                if (isImportantMessage(message.getContent())) {
                    keyExchanges.add("User: " + truncateContent(message.getContent(), 50));
                }
            } else if (message.getRole() == MessageRole.ASSISTANT) {
                assistantMessages++;

                if (isImportantMessage(message.getContent())) {
                    keyExchanges.add("Assistant: " + truncateContent(message.getContent(), 50));
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Previous conversation summary (%d messages):\n", messages.size()));
        summary.append(String.format("- %d user messages, %d assistant messages\n", userMessages, assistantMessages));

        if (!topicCounts.isEmpty()) {
            String topTopics = topicCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(", "));
            summary.append("- Main topics: ").append(topTopics).append("\n");
        }

        if (!keyExchanges.isEmpty()) {
            summary.append("- Key exchanges:\n");
            keyExchanges.stream().limit(5).forEach(exchange ->
                summary.append("  ").append(exchange).append("\n"));
        }

        return summary.toString();
    }

    private void extractTopics(String content, Map<String, Integer> topicCounts) {
        if (content == null) return;

        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+");

        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "been", "be", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them", "my", "your", "his", "her", "its", "our", "their", "this", "that", "these", "those");

        for (String word : words) {
            if (word.length() > 3 && !stopWords.contains(word)) {
                topicCounts.put(word, topicCounts.getOrDefault(word, 0) + 1);
            }
        }
    }

    private boolean isImportantMessage(String content) {
        if (content == null) return false;

        String lower = content.toLowerCase();
        return lower.contains("?") ||
               lower.contains("how") ||
               lower.contains("what") ||
               lower.contains("why") ||
               lower.contains("when") ||
               lower.contains("where") ||
               lower.contains("important") ||
               lower.contains("problem") ||
               lower.contains("error") ||
               lower.contains("issue") ||
               content.length() > 100;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    public ContextAnalysis analyzeContext(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ContextAnalysis(0, 0, false, 0, "");
        }

        TokenCountingService.TokenSummary tokenSummary = tokenCountingService.analyzeSession(messages);
        boolean needsCompression = tokenSummary.getTotalTokens() > DEFAULT_CONTEXT_WINDOW;
        int compressionSavings = needsCompression ?
            tokenSummary.getTotalTokens() - COMPRESSION_TARGET_TOKENS : 0;

        String recommendation = generateRecommendation(tokenSummary, needsCompression);

        return new ContextAnalysis(
            tokenSummary.getTotalTokens(),
            tokenSummary.getMessageCount(),
            needsCompression,
            compressionSavings,
            recommendation
        );
    }

    private String generateRecommendation(TokenCountingService.TokenSummary summary, boolean needsCompression) {
        if (summary.getTotalTokens() < 1000) {
            return "Context is small, no optimization needed.";
        } else if (summary.getTotalTokens() < 2000) {
            return "Context size is moderate, monitoring recommended.";
        } else if (needsCompression) {
            return "Context size is large, compression recommended to improve performance.";
        } else {
            return "Context size is approaching limits, consider compression soon.";
        }
    }

    public static class CompressionResult {
        private final List<ChatMessage> compressedMessages;
        private final String conversationSummary;
        private final boolean wasCompressed;
        private final int finalTokenCount;
        private final int tokensRemoved;

        public CompressionResult(List<ChatMessage> compressedMessages, String conversationSummary,
                               boolean wasCompressed, int finalTokenCount, int tokensRemoved) {
            this.compressedMessages = compressedMessages;
            this.conversationSummary = conversationSummary;
            this.wasCompressed = wasCompressed;
            this.finalTokenCount = finalTokenCount;
            this.tokensRemoved = tokensRemoved;
        }

        public List<ChatMessage> getCompressedMessages() { return compressedMessages; }
        public String getConversationSummary() { return conversationSummary; }
        public boolean wasCompressed() { return wasCompressed; }
        public int getFinalTokenCount() { return finalTokenCount; }
        public int getTokensRemoved() { return tokensRemoved; }
        public double getCompressionRatio() {
            return tokensRemoved > 0 ? (double) tokensRemoved / (finalTokenCount + tokensRemoved) : 0;
        }
    }

    public static class ContextAnalysis {
        private final int totalTokens;
        private final int messageCount;
        private final boolean needsCompression;
        private final int potentialSavings;
        private final String recommendation;

        public ContextAnalysis(int totalTokens, int messageCount, boolean needsCompression,
                             int potentialSavings, String recommendation) {
            this.totalTokens = totalTokens;
            this.messageCount = messageCount;
            this.needsCompression = needsCompression;
            this.potentialSavings = potentialSavings;
            this.recommendation = recommendation;
        }

        public int getTotalTokens() { return totalTokens; }
        public int getMessageCount() { return messageCount; }
        public boolean needsCompression() { return needsCompression; }
        public int getPotentialSavings() { return potentialSavings; }
        public String getRecommendation() { return recommendation; }
    }
}