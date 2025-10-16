package com.example.chatbotcache.model.dto;

public class ClearCacheResponse {

    private String cacheType;
    private int clearedCount;
    private String message;

    public ClearCacheResponse() {}

    public ClearCacheResponse(String cacheType, int clearedCount, String message) {
        this.cacheType = cacheType;
        this.clearedCount = clearedCount;
        this.message = message;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public int getClearedCount() {
        return clearedCount;
    }

    public void setClearedCount(int clearedCount) {
        this.clearedCount = clearedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ClearCacheResponse{" +
                "cacheType='" + cacheType + '\'' +
                ", clearedCount=" + clearedCount +
                ", message='" + message + '\'' +
                '}';
    }
}