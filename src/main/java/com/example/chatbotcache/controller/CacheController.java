package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.dto.CacheSizeResponse;
import com.example.chatbotcache.model.dto.CacheStatsResponse;
import com.example.chatbotcache.model.dto.ClearCacheResponse;
import com.example.chatbotcache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<CacheStatsResponse> getCacheStats() {
        try {
            Map<String, Object> stats = cacheService.getCacheStats();

            // Get additional cache information
            long responseCacheSize = cacheService.getCacheSize("cache:response:*");
            Map<String, Double> mostActiveSessions = cacheService.getMostActiveSessions(10);
            Map<String, Double> mostActiveUsers = cacheService.getMostActiveUsers(10);

            CacheStatsResponse response = new CacheStatsResponse();
            response.setCacheHits(toLong(stats.get("cache_hits")));
            response.setCacheMisses(toLong(stats.get("cache_misses")));
            response.setResponsesCached(toLong(stats.get("responses_cached")));
            response.setHitRatio(toDouble(stats.get("hit_ratio")));
            response.setResponseCacheSize(responseCacheSize);
            response.setMostActiveSessions(mostActiveSessions);
            response.setMostActiveUsers(mostActiveUsers);
            response.setTimestamp((String) stats.get("timestamp"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in getCacheStats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear response cache
     */
    @PostMapping("/clear/responses")
    public ResponseEntity<ClearCacheResponse> clearResponseCache() {
        try {
            int clearedCount = cacheService.clearResponseCache();
            ClearCacheResponse response = new ClearCacheResponse("responses", clearedCount, "Response cache cleared successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear cache by pattern
     */
    @PostMapping("/clear/{pattern}")
    public ResponseEntity<ClearCacheResponse> clearCacheByPattern(@PathVariable String pattern) {
        try {
            // Validate pattern to prevent clearing critical data
            if (pattern.contains("session:") || pattern.contains("user:") || pattern.contains("chat_")) {
                ClearCacheResponse response = new ClearCacheResponse(pattern, 0, "Pattern not allowed for security reasons");
                return ResponseEntity.badRequest().body(response);
            }

            int clearedCount = cacheService.clearCacheByPattern(pattern);
            ClearCacheResponse response = new ClearCacheResponse(pattern, clearedCount, "Cache cleared successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clean up inactive sessions
     */
    @PostMapping("/cleanup/sessions")
    public ResponseEntity<ClearCacheResponse> cleanupInactiveSessions(
            @RequestParam(defaultValue = "60") int inactiveMinutes) {
        try {
            Duration threshold = Duration.ofMinutes(inactiveMinutes);
            int cleanedCount = cacheService.cleanupInactiveSessions(threshold);

            ClearCacheResponse response = new ClearCacheResponse(
                "inactive_sessions",
                cleanedCount,
                "Inactive sessions cleaned up successfully"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get most active sessions
     */
    @GetMapping("/activity/sessions")
    public ResponseEntity<Map<String, Double>> getMostActiveSessions(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Double> sessions = cacheService.getMostActiveSessions(limit);
            return ResponseEntity.ok(sessions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get most active users
     */
    @GetMapping("/activity/users")
    public ResponseEntity<Map<String, Double>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Double> users = cacheService.getMostActiveUsers(limit);
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cache size by pattern
     */
    @GetMapping("/size")
    public ResponseEntity<CacheSizeResponse> getCacheSize(@RequestParam String pattern) {
        try {
            long size = cacheService.getCacheSize(pattern);
            CacheSizeResponse response = new CacheSizeResponse(pattern, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get system status including Redis connectivity
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> status = new java.util.HashMap<>();

            // Check Redis connectivity with immediate ping test
            boolean redisConnected = false;
            String redisStatus = "disconnected";
            try {
                // Use a simple Redis operation to test connectivity immediately
                redisTemplate.opsForValue().set("health:ping", "test", java.time.Duration.ofSeconds(1));
                String result = (String) redisTemplate.opsForValue().get("health:ping");
                redisTemplate.delete("health:ping");

                if ("test".equals(result)) {
                    redisConnected = true;
                    redisStatus = "connected";
                } else {
                    redisConnected = false;
                    redisStatus = "ping_failed";
                }
            } catch (Exception e) {
                // Redis is down or having issues
                redisConnected = false;
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("Cannot get Jedis connection") ||
                                       errorMsg.contains("Connection refused") ||
                                       errorMsg.contains("Unable to connect") ||
                                       errorMsg.contains("redis") ||
                                       errorMsg.contains("Redis"))) {
                    redisStatus = "disconnected";
                } else {
                    redisStatus = "error: " + errorMsg;
                }
            }

            status.put("redis", Map.of(
                "connected", redisConnected,
                "status", redisStatus
            ));

            status.put("fallbackMode", !redisConnected);
            status.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            Map<String, Object> errorStatus = new java.util.HashMap<>();
            errorStatus.put("redis", Map.of(
                "connected", false,
                "status", "error: " + e.getMessage()
            ));
            errorStatus.put("fallbackMode", true);
            errorStatus.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(errorStatus);
        }
    }

    /**
     * Stop Redis server for testing
     */
    @PostMapping("/redis/stop")
    public ResponseEntity<Map<String, Object>> stopRedis() {
        try {
            ProcessBuilder pb = new ProcessBuilder("redis-cli", "shutdown");
            Process process = pb.start();
            int exitCode = process.waitFor();

            Map<String, Object> response = new java.util.HashMap<>();
            if (exitCode == 0) {
                response.put("success", true);
                response.put("message", "Redis shutdown command sent successfully");
            } else {
                response.put("success", false);
                response.put("message", "Redis shutdown command failed with exit code: " + exitCode);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Error stopping Redis: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Restart Redis server for testing
     */
    @PostMapping("/redis/restart")
    public ResponseEntity<Map<String, Object>> restartRedis() {
        try {
            ProcessBuilder pb = new ProcessBuilder("brew", "services", "restart", "redis");
            Process process = pb.start();
            int exitCode = process.waitFor();

            Map<String, Object> response = new java.util.HashMap<>();
            if (exitCode == 0) {
                response.put("success", true);
                response.put("message", "Redis restart command sent successfully");
            } else {
                response.put("success", false);
                response.put("message", "Redis restart command failed with exit code: " + exitCode);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Error restarting Redis: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Helper method to safely convert Object to Long
     */
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return Long.valueOf(value.toString());
    }

    /**
     * Helper method to safely convert Object to Double
     */
    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return Double.valueOf(value.toString());
    }
}