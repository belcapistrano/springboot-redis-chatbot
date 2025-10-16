package com.example.chatbotcache.model.dto;

public class SessionStatsResponse {

    private String userId;
    private long totalSessions;
    private long activeSessions;
    private long inactiveSessions;

    public SessionStatsResponse() {}

    public SessionStatsResponse(String userId, long totalSessions, long activeSessions) {
        this.userId = userId;
        this.totalSessions = totalSessions;
        this.activeSessions = activeSessions;
        this.inactiveSessions = totalSessions - activeSessions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(long activeSessions) {
        this.activeSessions = activeSessions;
    }

    public long getInactiveSessions() {
        return inactiveSessions;
    }

    public void setInactiveSessions(long inactiveSessions) {
        this.inactiveSessions = inactiveSessions;
    }

    @Override
    public String toString() {
        return "SessionStatsResponse{" +
                "userId='" + userId + '\'' +
                ", totalSessions=" + totalSessions +
                ", activeSessions=" + activeSessions +
                ", inactiveSessions=" + inactiveSessions +
                '}';
    }
}