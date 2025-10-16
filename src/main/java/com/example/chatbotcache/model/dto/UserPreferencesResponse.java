package com.example.chatbotcache.model.dto;

import com.example.chatbotcache.model.UserPreferences;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class UserPreferencesResponse {

    private String userId;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private String systemPrompt;
    private Integer contextWindow;
    private Boolean enableCaching;
    private Boolean enableLogging;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public UserPreferencesResponse() {}

    public UserPreferencesResponse(UserPreferences preferences) {
        this.userId = preferences.getUserId();
        this.model = preferences.getModel();
        this.temperature = preferences.getTemperature();
        this.maxTokens = preferences.getMaxTokens();
        this.systemPrompt = preferences.getSystemPrompt();
        this.contextWindow = preferences.getContextWindow();
        this.enableCaching = preferences.getEnableCaching();
        this.enableLogging = preferences.getEnableLogging();
        this.createdAt = preferences.getCreatedAt();
        this.updatedAt = preferences.getUpdatedAt();
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
    public String toString() {
        return "UserPreferencesResponse{" +
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