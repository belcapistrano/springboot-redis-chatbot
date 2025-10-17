package com.example.chatbotcache.model.dto;

public class CompressionRequest {
    private Integer contextWindowSize;
    private Boolean preserveSystemMessages;
    private Integer minRecentMessages;

    public CompressionRequest() {}

    public CompressionRequest(Integer contextWindowSize, Boolean preserveSystemMessages, Integer minRecentMessages) {
        this.contextWindowSize = contextWindowSize;
        this.preserveSystemMessages = preserveSystemMessages;
        this.minRecentMessages = minRecentMessages;
    }

    public Integer getContextWindowSize() { return contextWindowSize; }
    public void setContextWindowSize(Integer contextWindowSize) { this.contextWindowSize = contextWindowSize; }

    public Boolean getPreserveSystemMessages() { return preserveSystemMessages; }
    public void setPreserveSystemMessages(Boolean preserveSystemMessages) { this.preserveSystemMessages = preserveSystemMessages; }

    public Integer getMinRecentMessages() { return minRecentMessages; }
    public void setMinRecentMessages(Integer minRecentMessages) { this.minRecentMessages = minRecentMessages; }
}