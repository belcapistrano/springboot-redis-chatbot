package com.example.chatbotcache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Cache key prefixes
    private static final String RESPONSE_CACHE_PREFIX = "cache:response:";
    private static final String SESSION_ACTIVITY_PREFIX = "activity:session:";
    private static final String CACHE_STATS_KEY = "cache:stats";
    private static final String USER_ACTIVITY_SORTED_SET = "activity:users:sorted";
    private static final String SESSION_ACTIVITY_SORTED_SET = "activity:sessions:sorted";

    // TTL configurations
    private static final Duration RESPONSE_CACHE_TTL = Duration.ofHours(1);
    private static final Duration ACTIVE_SESSION_TTL = Duration.ofHours(2);
    private static final Duration INACTIVE_SESSION_TTL = Duration.ofMinutes(30);
    private static final Duration USER_PREFERENCE_TTL = Duration.ofDays(30);

    /**
     * Cache a response with content-based hashing
     */
    public void cacheResponse(String sessionId, String userInput, String response,
                             String model, Double temperature) {
        try {
            String cacheKey = generateCacheKey(userInput, model, temperature);

            Map<String, Object> cachedData = new HashMap<>();
            cachedData.put("response", response);
            cachedData.put("sessionId", sessionId);
            cachedData.put("model", model);
            cachedData.put("temperature", temperature);
            cachedData.put("cachedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            cachedData.put("hitCount", 0);

            redisTemplate.opsForValue().set(cacheKey, cachedData, RESPONSE_CACHE_TTL);

            // Update cache statistics
            incrementCacheStats("responses_cached");

        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to cache response: " + e.getMessage());
        }
    }

    /**
     * Lookup cached response
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedResponse(String userInput, String model, Double temperature) {
        try {
            String cacheKey = generateCacheKey(userInput, model, temperature);
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                // Increment hit count
                Integer hitCount = (Integer) cachedData.getOrDefault("hitCount", 0);
                cachedData.put("hitCount", hitCount + 1);
                redisTemplate.opsForValue().set(cacheKey, cachedData, RESPONSE_CACHE_TTL);

                // Update cache statistics
                incrementCacheStats("cache_hits");
                return cachedData;
            } else {
                // Update cache statistics
                incrementCacheStats("cache_misses");
                return null;
            }

        } catch (Exception e) {
            // Log error and treat as cache miss
            System.err.println("Failed to lookup cached response: " + e.getMessage());
            incrementCacheStats("cache_misses");
            return null;
        }
    }

    /**
     * Track session activity with sorted sets for ranking
     */
    public void trackSessionActivity(String sessionId, String userId) {
        try {
            double timestamp = System.currentTimeMillis();

            // Add to session activity sorted set
            redisTemplate.opsForZSet().add(SESSION_ACTIVITY_SORTED_SET, sessionId, timestamp);

            // Add to user activity sorted set
            redisTemplate.opsForZSet().add(USER_ACTIVITY_SORTED_SET, userId, timestamp);

            // Set session activity timestamp
            String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(activityKey, timestamp, ACTIVE_SESSION_TTL);

        } catch (Exception e) {
            System.err.println("Failed to track session activity: " + e.getMessage());
        }
    }

    /**
     * Get most active sessions
     */
    public Map<String, Double> getMostActiveSessions(int limit) {
        try {
            var sessions = redisTemplate.opsForZSet().reverseRangeWithScores(
                SESSION_ACTIVITY_SORTED_SET, 0, limit - 1);

            Map<String, Double> result = new HashMap<>();
            if (sessions != null) {
                sessions.forEach(tuple ->
                    result.put((String) tuple.getValue(), tuple.getScore()));
            }
            return result;

        } catch (Exception e) {
            System.err.println("Failed to get most active sessions: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get most active users
     */
    public Map<String, Double> getMostActiveUsers(int limit) {
        try {
            var users = redisTemplate.opsForZSet().reverseRangeWithScores(
                USER_ACTIVITY_SORTED_SET, 0, limit - 1);

            Map<String, Double> result = new HashMap<>();
            if (users != null) {
                users.forEach(tuple ->
                    result.put((String) tuple.getValue(), tuple.getScore()));
            }
            return result;

        } catch (Exception e) {
            System.err.println("Failed to get most active users: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Clean up inactive sessions
     */
    public int cleanupInactiveSessions(Duration inactiveThreshold) {
        try {
            double cutoffTime = System.currentTimeMillis() - inactiveThreshold.toMillis();

            // Remove from sorted sets
            Long removedSessions = redisTemplate.opsForZSet().removeRangeByScore(
                SESSION_ACTIVITY_SORTED_SET, 0, cutoffTime);

            Long removedUsers = redisTemplate.opsForZSet().removeRangeByScore(
                USER_ACTIVITY_SORTED_SET, 0, cutoffTime);

            return removedSessions != null ? removedSessions.intValue() : 0;

        } catch (Exception e) {
            System.err.println("Failed to cleanup inactive sessions: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Set TTL for active session
     */
    public void setActiveSessionTTL(String sessionId) {
        try {
            String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
            redisTemplate.expire(activityKey, ACTIVE_SESSION_TTL);
        } catch (Exception e) {
            System.err.println("Failed to set active session TTL: " + e.getMessage());
        }
    }

    /**
     * Set TTL for inactive session
     */
    public void setInactiveSessionTTL(String sessionId) {
        try {
            String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
            redisTemplate.expire(activityKey, INACTIVE_SESSION_TTL);
        } catch (Exception e) {
            System.err.println("Failed to set inactive session TTL: " + e.getMessage());
        }
    }

    /**
     * Check if session is active
     */
    public boolean isSessionActive(String sessionId) {
        try {
            String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
            return redisTemplate.hasKey(activityKey);
        } catch (Exception e) {
            System.err.println("Failed to check session activity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get cache statistics
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCacheStats() {
        try {
            Map<Object, Object> rawStats = redisTemplate.opsForHash()
                .entries(CACHE_STATS_KEY);
            Map<String, Object> stats = new HashMap<>();
            rawStats.forEach((k, v) -> stats.put((String) k, v));

            if (stats.isEmpty()) {
                // Initialize default stats
                Map<String, Object> defaultStats = initializeDefaultStats();
                stats.putAll(defaultStats);
            }

            // Add runtime statistics
            stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Calculate hit ratio
            long hits = Long.parseLong(stats.getOrDefault("cache_hits", "0").toString());
            long misses = Long.parseLong(stats.getOrDefault("cache_misses", "0").toString());
            double hitRatio = (hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0;
            stats.put("hit_ratio", Math.round(hitRatio * 10000.0) / 100.0); // Percentage with 2 decimals

            return stats;

        } catch (Exception e) {
            System.err.println("Failed to get cache stats: " + e.getMessage());
            return initializeDefaultStats();
        }
    }

    /**
     * Clear cache by pattern
     */
    public int clearCacheByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                return keys.size();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Failed to clear cache by pattern: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Clear all response cache
     */
    public int clearResponseCache() {
        return clearCacheByPattern(RESPONSE_CACHE_PREFIX + "*");
    }

    /**
     * Get cache size by pattern
     */
    public long getCacheSize(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            System.err.println("Failed to get cache size: " + e.getMessage());
            return 0;
        }
    }

    // Private helper methods

    private String generateCacheKey(String userInput, String model, Double temperature) {
        try {
            String content = userInput + "|" + model + "|" + (temperature != null ? temperature : "0.7");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return RESPONSE_CACHE_PREFIX + hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return RESPONSE_CACHE_PREFIX + Math.abs((userInput + model + temperature).hashCode());
        }
    }

    private void incrementCacheStats(String statName) {
        try {
            redisTemplate.opsForHash().increment(CACHE_STATS_KEY, statName, 1);
        } catch (Exception e) {
            System.err.println("Failed to increment cache stats: " + e.getMessage());
        }
    }

    private Map<String, Object> initializeDefaultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cache_hits", 0L);
        stats.put("cache_misses", 0L);
        stats.put("responses_cached", 0L);
        stats.put("hit_ratio", 0.0);
        stats.put("initialized_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return stats;
    }
}