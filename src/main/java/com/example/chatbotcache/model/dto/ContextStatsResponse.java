package com.example.chatbotcache.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ContextStatsResponse {
    private int totalTokens;
    private int userTokens;
    private int assistantTokens;
    private int messageCount;
    private double averageTokensPerMessage;
    private boolean needsCompression;
    private int potentialSavings;
    private String recommendation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAnalyzed;

    public ContextStatsResponse() {}

    public ContextStatsResponse(int totalTokens, int userTokens, int assistantTokens,
                               int messageCount, double averageTokensPerMessage,
                               boolean needsCompression, int potentialSavings,
                               String recommendation, LocalDateTime lastAnalyzed) {
        this.totalTokens = totalTokens;
        this.userTokens = userTokens;
        this.assistantTokens = assistantTokens;
        this.messageCount = messageCount;
        this.averageTokensPerMessage = averageTokensPerMessage;
        this.needsCompression = needsCompression;
        this.potentialSavings = potentialSavings;
        this.recommendation = recommendation;
        this.lastAnalyzed = lastAnalyzed;
    }

    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    public int getUserTokens() { return userTokens; }
    public void setUserTokens(int userTokens) { this.userTokens = userTokens; }

    public int getAssistantTokens() { return assistantTokens; }
    public void setAssistantTokens(int assistantTokens) { this.assistantTokens = assistantTokens; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public double getAverageTokensPerMessage() { return averageTokensPerMessage; }
    public void setAverageTokensPerMessage(double averageTokensPerMessage) { this.averageTokensPerMessage = averageTokensPerMessage; }

    public boolean isNeedsCompression() { return needsCompression; }
    public void setNeedsCompression(boolean needsCompression) { this.needsCompression = needsCompression; }

    public int getPotentialSavings() { return potentialSavings; }
    public void setPotentialSavings(int potentialSavings) { this.potentialSavings = potentialSavings; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
    public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
}