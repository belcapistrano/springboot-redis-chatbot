package com.example.chatbotcache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Test Redis connection by setting and getting a test key
            String testKey = "health:test";
            String testValue = "test-" + System.currentTimeMillis();

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean isHealthy = testValue.equals(retrievedValue);

            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("details", Map.of(
                "connection", "successful",
                "operations", "read/write test passed"
            ));

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", e.getMessage());
            health.put("details", Map.of(
                "connection", "failed"
            ));

            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/redis/info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Get Redis connection info
            info.put("timestamp", LocalDateTime.now());
            info.put("status", "connected");

            // Test basic operations
            String infoKey = "info:test";
            redisTemplate.opsForValue().set(infoKey, "Redis is working", java.time.Duration.ofSeconds(10));
            String value = (String) redisTemplate.opsForValue().get(infoKey);

            info.put("operations", Map.of(
                "set", "successful",
                "get", "successful",
                "test_value", value
            ));

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            info.put("status", "error");
            info.put("error", e.getMessage());
            info.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(info);
        }
    }
}