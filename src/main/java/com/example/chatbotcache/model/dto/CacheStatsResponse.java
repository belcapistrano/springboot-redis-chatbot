package com.example.chatbotcache.model.dto;

import java.util.Map;

public class CacheStatsResponse {

    private Long cacheHits;
    private Long cacheMisses;
    private Long responsesCached;
    private Double hitRatio;
    private Long responseCacheSize;
    private Map<String, Double> mostActiveSessions;
    private Map<String, Double> mostActiveUsers;
    private String timestamp;

    public CacheStatsResponse() {}

    // Getters and Setters
    public Long getCacheHits() {
        return cacheHits;
    }

    public void setCacheHits(Long cacheHits) {
        this.cacheHits = cacheHits;
    }

    public Long getCacheMisses() {
        return cacheMisses;
    }

    public void setCacheMisses(Long cacheMisses) {
        this.cacheMisses = cacheMisses;
    }

    public Long getResponsesCached() {
        return responsesCached;
    }

    public void setResponsesCached(Long responsesCached) {
        this.responsesCached = responsesCached;
    }

    public Double getHitRatio() {
        return hitRatio;
    }

    public void setHitRatio(Double hitRatio) {
        this.hitRatio = hitRatio;
    }

    public Long getResponseCacheSize() {
        return responseCacheSize;
    }

    public void setResponseCacheSize(Long responseCacheSize) {
        this.responseCacheSize = responseCacheSize;
    }

    public Map<String, Double> getMostActiveSessions() {
        return mostActiveSessions;
    }

    public void setMostActiveSessions(Map<String, Double> mostActiveSessions) {
        this.mostActiveSessions = mostActiveSessions;
    }

    public Map<String, Double> getMostActiveUsers() {
        return mostActiveUsers;
    }

    public void setMostActiveUsers(Map<String, Double> mostActiveUsers) {
        this.mostActiveUsers = mostActiveUsers;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CacheStatsResponse{" +
                "cacheHits=" + cacheHits +
                ", cacheMisses=" + cacheMisses +
                ", responsesCached=" + responsesCached +
                ", hitRatio=" + hitRatio +
                ", responseCacheSize=" + responseCacheSize +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}