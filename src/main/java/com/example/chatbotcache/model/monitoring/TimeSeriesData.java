package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TimeSeriesData {
    private String metric;
    private Duration period;
    private List<LocalDateTime> timestamps;
    private List<Double> values;
    private int dataPoints;
    private double minValue;
    private double maxValue;
    private double averageValue;
    private double standardDeviation;
}