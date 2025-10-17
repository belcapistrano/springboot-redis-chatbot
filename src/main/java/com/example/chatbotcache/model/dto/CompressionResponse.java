package com.example.chatbotcache.model.dto;

import com.example.chatbotcache.model.ChatMessage;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class CompressionResponse {
    private List<ChatMessage> compressedMessages;
    private String conversationSummary;
    private boolean wasCompressed;
    private int originalTokenCount;
    private int finalTokenCount;
    private int tokensRemoved;
    private double compressionRatio;
    private int messagesRemoved;
    private int messagesPreserved;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime compressedAt;

    public CompressionResponse() {}

    public CompressionResponse(List<ChatMessage> compressedMessages, String conversationSummary,
                              boolean wasCompressed, int originalTokenCount, int finalTokenCount,
                              int tokensRemoved, double compressionRatio, int messagesRemoved,
                              int messagesPreserved, LocalDateTime compressedAt) {
        this.compressedMessages = compressedMessages;
        this.conversationSummary = conversationSummary;
        this.wasCompressed = wasCompressed;
        this.originalTokenCount = originalTokenCount;
        this.finalTokenCount = finalTokenCount;
        this.tokensRemoved = tokensRemoved;
        this.compressionRatio = compressionRatio;
        this.messagesRemoved = messagesRemoved;
        this.messagesPreserved = messagesPreserved;
        this.compressedAt = compressedAt;
    }

    public List<ChatMessage> getCompressedMessages() { return compressedMessages; }
    public void setCompressedMessages(List<ChatMessage> compressedMessages) { this.compressedMessages = compressedMessages; }

    public String getConversationSummary() { return conversationSummary; }
    public void setConversationSummary(String conversationSummary) { this.conversationSummary = conversationSummary; }

    public boolean isWasCompressed() { return wasCompressed; }
    public void setWasCompressed(boolean wasCompressed) { this.wasCompressed = wasCompressed; }

    public int getOriginalTokenCount() { return originalTokenCount; }
    public void setOriginalTokenCount(int originalTokenCount) { this.originalTokenCount = originalTokenCount; }

    public int getFinalTokenCount() { return finalTokenCount; }
    public void setFinalTokenCount(int finalTokenCount) { this.finalTokenCount = finalTokenCount; }

    public int getTokensRemoved() { return tokensRemoved; }
    public void setTokensRemoved(int tokensRemoved) { this.tokensRemoved = tokensRemoved; }

    public double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(double compressionRatio) { this.compressionRatio = compressionRatio; }

    public int getMessagesRemoved() { return messagesRemoved; }
    public void setMessagesRemoved(int messagesRemoved) { this.messagesRemoved = messagesRemoved; }

    public int getMessagesPreserved() { return messagesPreserved; }
    public void setMessagesPreserved(int messagesPreserved) { this.messagesPreserved = messagesPreserved; }

    public LocalDateTime getCompressedAt() { return compressedAt; }
    public void setCompressedAt(LocalDateTime compressedAt) { this.compressedAt = compressedAt; }
}