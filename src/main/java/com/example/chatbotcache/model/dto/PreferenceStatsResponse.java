package com.example.chatbotcache.model.dto;

public class PreferenceStatsResponse {

    private long usersWithCaching;
    private int recentlyUpdated;

    public PreferenceStatsResponse() {}

    public PreferenceStatsResponse(long usersWithCaching, int recentlyUpdated) {
        this.usersWithCaching = usersWithCaching;
        this.recentlyUpdated = recentlyUpdated;
    }

    public long getUsersWithCaching() {
        return usersWithCaching;
    }

    public void setUsersWithCaching(long usersWithCaching) {
        this.usersWithCaching = usersWithCaching;
    }

    public int getRecentlyUpdated() {
        return recentlyUpdated;
    }

    public void setRecentlyUpdated(int recentlyUpdated) {
        this.recentlyUpdated = recentlyUpdated;
    }

    @Override
    public String toString() {
        return "PreferenceStatsResponse{" +
                "usersWithCaching=" + usersWithCaching +
                ", recentlyUpdated=" + recentlyUpdated +
                '}';
    }
}