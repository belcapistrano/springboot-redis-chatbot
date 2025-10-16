package com.example.chatbotcache.model.dto;

public class CleanupResponse {

    private String sessionId;
    private int deletedMessages;
    private String message;

    public CleanupResponse() {}

    public CleanupResponse(String sessionId, int deletedMessages, String message) {
        this.sessionId = sessionId;
        this.deletedMessages = deletedMessages;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getDeletedMessages() {
        return deletedMessages;
    }

    public void setDeletedMessages(int deletedMessages) {
        this.deletedMessages = deletedMessages;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CleanupResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", deletedMessages=" + deletedMessages +
                ", message='" + message + '\'' +
                '}';
    }
}