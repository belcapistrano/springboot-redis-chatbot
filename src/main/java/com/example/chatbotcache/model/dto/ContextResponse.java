package com.example.chatbotcache.model.dto;

public class ContextResponse {

    private String sessionId;
    private int contextMessages;
    private long totalMessages;
    private int totalTokens;
    private String currentTopic;
    private boolean hasContext;

    public ContextResponse() {}

    public ContextResponse(String sessionId, int contextMessages, long totalMessages,
                          int totalTokens, String currentTopic, boolean hasContext) {
        this.sessionId = sessionId;
        this.contextMessages = contextMessages;
        this.totalMessages = totalMessages;
        this.totalTokens = totalTokens;
        this.currentTopic = currentTopic;
        this.hasContext = hasContext;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getContextMessages() {
        return contextMessages;
    }

    public void setContextMessages(int contextMessages) {
        this.contextMessages = contextMessages;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    public String getCurrentTopic() {
        return currentTopic;
    }

    public void setCurrentTopic(String currentTopic) {
        this.currentTopic = currentTopic;
    }

    public boolean isHasContext() {
        return hasContext;
    }

    public void setHasContext(boolean hasContext) {
        this.hasContext = hasContext;
    }

    @Override
    public String toString() {
        return "ContextResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", contextMessages=" + contextMessages +
                ", totalMessages=" + totalMessages +
                ", totalTokens=" + totalTokens +
                ", currentTopic='" + currentTopic + '\'' +
                ", hasContext=" + hasContext +
                '}';
    }
}