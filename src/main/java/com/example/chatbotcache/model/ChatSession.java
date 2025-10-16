package com.example.chatbotcache.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RedisHash("chat_session")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatSession {

    @Id
    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "User ID cannot be blank")
    @Size(max = 100, message = "User ID cannot exceed 100 characters")
    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivity;

    private Map<String, Object> settings;

    private Integer tokenCount;

    private Integer messageCount;

    private String title;

    private Boolean active;

    public ChatSession() {
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.settings = new HashMap<>();
        this.tokenCount = 0;
        this.messageCount = 0;
        this.active = true;
    }

    public ChatSession(String sessionId, String userId) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public ChatSession(String sessionId, String userId, String title) {
        this(sessionId, userId);
        this.title = title;
    }

    // Helper methods
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public void incrementMessageCount() {
        this.messageCount = (this.messageCount == null) ? 1 : this.messageCount + 1;
    }

    public void addTokens(int tokens) {
        this.tokenCount = (this.tokenCount == null) ? tokens : this.tokenCount + tokens;
    }

    public void setSetting(String key, Object value) {
        if (this.settings == null) {
            this.settings = new HashMap<>();
        }
        this.settings.put(key, value);
    }

    public Object getSetting(String key) {
        return this.settings != null ? this.settings.get(key) : null;
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

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", lastActivity=" + lastActivity +
                ", tokenCount=" + tokenCount +
                ", messageCount=" + messageCount +
                ", title='" + title + '\'' +
                ", active=" + active +
                '}';
    }
}