package com.example.chatbotcache.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.Objects;

@RedisHash("chat_message")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    @Id
    @NotBlank(message = "Message ID cannot be blank")
    private String messageId;

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotNull(message = "Message role cannot be null")
    private MessageRole role;

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 10000, message = "Message content cannot exceed 10,000 characters")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Integer tokenCount;

    private String metadata;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String messageId, String sessionId, MessageRole role, String content) {
        this();
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String messageId, String sessionId, MessageRole role, String content, Integer tokenCount) {
        this(messageId, sessionId, role, content);
        this.tokenCount = tokenCount;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", role=" + role +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", tokenCount=" + tokenCount +
                '}';
    }
}