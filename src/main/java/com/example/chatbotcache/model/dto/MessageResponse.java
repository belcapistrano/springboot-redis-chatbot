package com.example.chatbotcache.model.dto;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MessageResponse {

    private String messageId;
    private String sessionId;
    private MessageRole role;
    private String content;
    private Integer tokenCount;
    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public MessageResponse() {}

    public MessageResponse(ChatMessage message) {
        this.messageId = message.getMessageId();
        this.sessionId = message.getSessionId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.tokenCount = message.getTokenCount();
        this.metadata = message.getMetadata();
        this.timestamp = message.getTimestamp();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "messageId='" + messageId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", role=" + role +
                ", content='" + content + '\'' +
                ", tokenCount=" + tokenCount +
                ", timestamp=" + timestamp +
                '}';
    }
}