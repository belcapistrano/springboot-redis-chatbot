package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class StatisticsSummary {
    private LocalDateTime timestamp;
    private Duration period;
    private Map<String, Object> currentMetrics;
    private Map<String, TimeSeriesData> timeSeriesData;
    private Map<String, Double> aggregates;
    private TrendAnalysis trendAnalysis;
    private Map<String, Object> insights;
}