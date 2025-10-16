package com.example.chatbotcache.model.dto;

public class CacheSizeResponse {

    private String pattern;
    private long size;

    public CacheSizeResponse() {}

    public CacheSizeResponse(String pattern, long size) {
        this.pattern = pattern;
        this.size = size;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "CacheSizeResponse{" +
                "pattern='" + pattern + '\'' +
                ", size=" + size +
                '}';
    }
}