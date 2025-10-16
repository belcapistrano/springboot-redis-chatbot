package com.example.chatbotcache.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UserPreferencesRequest {

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

    public UserPreferencesRequest() {}

    public UserPreferencesRequest(String model, Double temperature, Integer maxTokens) {
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "UserPreferencesRequest{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", contextWindow=" + contextWindow +
                ", enableCaching=" + enableCaching +
                ", enableLogging=" + enableLogging +
                '}';
    }
}