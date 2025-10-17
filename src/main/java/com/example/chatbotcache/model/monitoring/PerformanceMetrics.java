package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PerformanceMetrics {
    private LocalDateTime timestamp;
    private Map<String, Double> averageLatencies;
    private Map<String, Double> p95Latencies;
    private Map<String, Double> p99Latencies;
    private Map<String, Long> minLatencies;
    private Map<String, Long> maxLatencies;
    private double throughput;
}