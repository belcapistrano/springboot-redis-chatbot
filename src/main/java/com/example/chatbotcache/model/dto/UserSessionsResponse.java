package com.example.chatbotcache.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class UserSessionsResponse {
    private String userId;
    private int totalSessions;
    private int activeSessions;
    private List<UserSessionInfo> sessions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime retrievedAt;

    public UserSessionsResponse() {}

    public UserSessionsResponse(String userId, int totalSessions, int activeSessions,
                               List<UserSessionInfo> sessions, LocalDateTime retrievedAt) {
        this.userId = userId;
        this.totalSessions = totalSessions;
        this.activeSessions = activeSessions;
        this.sessions = sessions;
        this.retrievedAt = retrievedAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getActiveSessions() { return activeSessions; }
    public void setActiveSessions(int activeSessions) { this.activeSessions = activeSessions; }

    public List<UserSessionInfo> getSessions() { return sessions; }
    public void setSessions(List<UserSessionInfo> sessions) { this.sessions = sessions; }

    public LocalDateTime getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(LocalDateTime retrievedAt) { this.retrievedAt = retrievedAt; }

    public static class UserSessionInfo {
        private String sessionId;
        private String title;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastActivity;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastTrackingActivity;

        private boolean active;

        public UserSessionInfo() {}

        public UserSessionInfo(String sessionId, String title, LocalDateTime createdAt,
                              LocalDateTime lastActivity, LocalDateTime lastTrackingActivity, boolean active) {
            this.sessionId = sessionId;
            this.title = title;
            this.createdAt = createdAt;
            this.lastActivity = lastActivity;
            this.lastTrackingActivity = lastTrackingActivity;
            this.active = active;
        }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

        public LocalDateTime getLastTrackingActivity() { return lastTrackingActivity; }
        public void setLastTrackingActivity(LocalDateTime lastTrackingActivity) { this.lastTrackingActivity = lastTrackingActivity; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}