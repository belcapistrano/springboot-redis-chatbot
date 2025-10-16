package com.example.chatbotcache.model.dto;

import java.util.List;

public class MessagesPageResponse {

    private List<MessageResponse> messages;
    private int page;
    private int size;
    private long totalMessages;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public MessagesPageResponse() {}

    public MessagesPageResponse(List<MessageResponse> messages, int page, int size, long totalMessages) {
        this.messages = messages;
        this.page = page;
        this.size = size;
        this.totalMessages = totalMessages;
        this.totalPages = (int) Math.ceil((double) totalMessages / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }

    // Getters and Setters
    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "MessagesPageResponse{" +
                "messages=" + messages.size() +
                ", page=" + page +
                ", size=" + size +
                ", totalMessages=" + totalMessages +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}