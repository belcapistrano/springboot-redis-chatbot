package com.example.chatbotcache.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.Objects;

@RedisHash("user_preferences")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferences {

    @Id
    @NotBlank(message = "User ID cannot be blank")
    @Size(max = 100, message = "User ID cannot exceed 100 characters")
    private String userId;

    @Size(max = 50, message = "Model name cannot exceed 50 characters")
    private String model;

    @DecimalMin(value = "0.0", message = "Temperature must be between 0.0 and 2.0")
    @DecimalMax(value = "2.0", message = "Temperature must be between 0.0 and 2.0")
    private Double temperature;

    @Min(value = 1, message = "Max tokens must be at least 1")
    @Max(value = 8192, message = "Max tokens cannot exceed 8192")
    private Integer maxTokens;

    @Size(max = 1000, message = "System prompt cannot exceed 1000 characters")
    private String systemPrompt;

    @Min(value = 1, message = "Context window must be at least 1")
    @Max(value = 32768, message = "Context window cannot exceed 32768")
    private Integer contextWindow;

    private Boolean enableCaching;

    private Boolean enableLogging;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public UserPreferences() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        setDefaults();
    }

    public UserPreferences(String userId) {
        this();
        this.userId = userId;
    }

    private void setDefaults() {
        if (this.model == null) {
            this.model = "gpt-3.5-turbo";
        }
        if (this.temperature == null) {
            this.temperature = 0.7;
        }
        if (this.maxTokens == null) {
            this.maxTokens = 2048;
        }
        if (this.contextWindow == null) {
            this.contextWindow = 4096;
        }
        if (this.enableCaching == null) {
            this.enableCaching = true;
        }
        if (this.enableLogging == null) {
            this.enableLogging = true;
        }
        if (this.systemPrompt == null) {
            this.systemPrompt = "You are a helpful AI assistant.";
        }
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Integer getContextWindow() {
        return contextWindow;
    }

    public void setContextWindow(Integer contextWindow) {
        this.contextWindow = contextWindow;
    }

    public Boolean getEnableCaching() {
        return enableCaching;
    }

    public void setEnableCaching(Boolean enableCaching) {
        this.enableCaching = enableCaching;
    }

    public Boolean getEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(Boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferences that = (UserPreferences) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "userId='" + userId + '\'' +
                ", model='" + model + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", contextWindow=" + contextWindow +
                ", enableCaching=" + enableCaching +
                ", enableLogging=" + enableLogging +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}