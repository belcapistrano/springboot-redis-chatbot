package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApplicationMetrics {
    private LocalDateTime timestamp;
    private long totalOperations;
    private long totalErrors;
    private double errorRate;
    private Map<String, Long> operationBreakdown;
    private Map<String, Long> errorBreakdown;
    private String uptime;
}