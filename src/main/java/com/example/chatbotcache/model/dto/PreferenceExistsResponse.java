package com.example.chatbotcache.model.dto;

public class PreferenceExistsResponse {

    private String userId;
    private boolean hasCustomPreferences;

    public PreferenceExistsResponse() {}

    public PreferenceExistsResponse(String userId, boolean hasCustomPreferences) {
        this.userId = userId;
        this.hasCustomPreferences = hasCustomPreferences;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isHasCustomPreferences() {
        return hasCustomPreferences;
    }

    public void setHasCustomPreferences(boolean hasCustomPreferences) {
        this.hasCustomPreferences = hasCustomPreferences;
    }

    @Override
    public String toString() {
        return "PreferenceExistsResponse{" +
                "userId='" + userId + '\'' +
                ", hasCustomPreferences=" + hasCustomPreferences +
                '}';
    }
}