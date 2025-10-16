package com.example.chatbotcache.model.dto;

import jakarta.validation.constraints.Size;

public class UpdateSessionRequest {

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private Boolean active;

    public UpdateSessionRequest() {}

    public UpdateSessionRequest(String title, Boolean active) {
        this.title = title;
        this.active = active;
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
    public String toString() {
        return "UpdateSessionRequest{" +
                "title='" + title + '\'' +
                ", active=" + active +
                '}';
    }
}