package com.example.chatbotcache.model.dto;

public class MessageCountResponse {

    private String sessionId;
    private long messageCount;
    private int totalTokens;

    public MessageCountResponse() {}

    public MessageCountResponse(String sessionId, long messageCount, int totalTokens) {
        this.sessionId = sessionId;
        this.messageCount = messageCount;
        this.totalTokens = totalTokens;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    @Override
    public String toString() {
        return "MessageCountResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", messageCount=" + messageCount +
                ", totalTokens=" + totalTokens +
                '}';
    }
}