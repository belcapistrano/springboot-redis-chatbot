package com.example.chatbotcache.model.monitoring;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemMetrics {
    private LocalDateTime timestamp;
    private long heapMemoryUsed;
    private long heapMemoryMax;
    private long nonHeapMemoryUsed;
    private int threadCount;
    private int peakThreadCount;
    private int availableProcessors;
    private long freeMemory;
    private long totalMemory;
    private long garbageCollectionCount;
    private long garbageCollectionTime;
}