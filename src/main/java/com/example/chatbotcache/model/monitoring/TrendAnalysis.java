package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TrendAnalysis {
    private LocalDateTime timestamp;
    private Map<String, String> trends;
    private Map<String, Double> changeRates;
    private Map<String, Double> volatility;
    private List<String> alerts;
}