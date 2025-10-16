package com.example.chatbotcache.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSessionRequest {

    @NotBlank(message = "User ID cannot be blank")
    @Size(max = 100, message = "User ID cannot exceed 100 characters")
    private String userId;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    public CreateSessionRequest() {}

    public CreateSessionRequest(String userId, String title) {
        this.userId = userId;
        this.title = title;
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

    @Override
    public String toString() {
        return "CreateSessionRequest{" +
                "userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}