package com.example.chatbotcache.model.dto;

import com.example.chatbotcache.model.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MessageRequest {

    @NotNull(message = "Message role cannot be null")
    private MessageRole role;

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 10000, message = "Message content cannot exceed 10,000 characters")
    private String content;

    private Integer tokenCount;

    private String metadata;

    public MessageRequest() {}

    public MessageRequest(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public MessageRequest(MessageRole role, String content, Integer tokenCount) {
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
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

    @Override
    public String toString() {
        return "MessageRequest{" +
                "role=" + role +
                ", content='" + content + '\'' +
                ", tokenCount=" + tokenCount +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}