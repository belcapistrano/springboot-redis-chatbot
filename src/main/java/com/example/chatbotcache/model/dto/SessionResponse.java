package com.example.chatbotcache.model.dto;

import com.example.chatbotcache.model.ChatSession;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public class SessionResponse {

    private String sessionId;
    private String userId;
    private String title;
    private Boolean active;
    private Integer messageCount;
    private Integer tokenCount;
    private Map<String, Object> settings;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivity;

    public SessionResponse() {}

    public SessionResponse(ChatSession session) {
        this.sessionId = session.getSessionId();
        this.userId = session.getUserId();
        this.title = session.getTitle();
        this.active = session.getActive();
        this.messageCount = session.getMessageCount();
        this.tokenCount = session.getTokenCount();
        this.settings = session.getSettings();
        this.createdAt = session.getCreatedAt();
        this.lastActivity = session.getLastActivity();
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public String toString() {
        return "SessionResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", active=" + active +
                ", messageCount=" + messageCount +
                ", tokenCount=" + tokenCount +
                ", createdAt=" + createdAt +
                ", lastActivity=" + lastActivity +
                '}';
    }
}