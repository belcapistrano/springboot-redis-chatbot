package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CacheMetrics {
    private LocalDateTime timestamp;
    private Map<String, Double> hitRates;
    private Map<String, Long> hitCounts;
    private Map<String, Long> missCounts;
    private long totalHits;
    private long totalMisses;
}