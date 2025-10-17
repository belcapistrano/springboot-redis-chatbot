package com.example.chatbotcache.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MonitoringService {

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public void recordRequest(String endpoint) {
        requestCount.incrementAndGet();
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", requestCount.get());
        metrics.put("cacheHits", cacheHits.get());
        metrics.put("cacheMisses", cacheMisses.get());
        double hitRate = cacheHits.get() + cacheMisses.get() > 0
            ? (double) cacheHits.get() / (cacheHits.get() + cacheMisses.get())
            : 0.0;
        metrics.put("cacheHitRate", hitRate);
        return metrics;
    }

    public void reset() {
        requestCount.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
    }
}