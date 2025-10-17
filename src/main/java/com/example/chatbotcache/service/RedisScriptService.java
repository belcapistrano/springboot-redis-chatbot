package com.example.chatbotcache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RedisScriptService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Pre-compiled scripts for better performance
    private RedisScript<Long> atomicSessionUpdateScript;
    private RedisScript<Long> atomicMessageAddScript;
    private RedisScript<List> rateLimitScript;
    private RedisScript<Long> cacheWithTtlScript;
    private RedisScript<Long> cleanupExpiredKeysScript;
    private RedisScript<Map> sessionStatsScript;
    private RedisScript<Long> bulkDeleteScript;

    @PostConstruct
    public void initializeScripts() {
        // Initialize all Lua scripts
        initializeAtomicSessionUpdateScript();
        initializeAtomicMessageAddScript();
        initializeRateLimitScript();
        initializeCacheWithTtlScript();
        initializeCleanupExpiredKeysScript();
        initializeSessionStatsScript();
        initializeBulkDeleteScript();
    }

    private void initializeAtomicSessionUpdateScript() {
        String luaScript =
            "local sessionKey = KEYS[1]\n" +
            "local messageCount = tonumber(ARGV[1])\n" +
            "local tokenCount = tonumber(ARGV[2])\n" +
            "local lastActivity = ARGV[3]\n" +
            "local ttl = tonumber(ARGV[4])\n" +
            "\n" +
            "-- Update session data atomically\n" +
            "redis.call('HINCRBY', sessionKey, 'messageCount', messageCount)\n" +
            "redis.call('HINCRBY', sessionKey, 'totalTokens', tokenCount)\n" +
            "redis.call('HSET', sessionKey, 'lastActivity', lastActivity)\n" +
            "redis.call('EXPIRE', sessionKey, ttl)\n" +
            "\n" +
            "-- Return updated message count\n" +
            "return redis.call('HGET', sessionKey, 'messageCount')";

        atomicSessionUpdateScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    private void initializeAtomicMessageAddScript() {
        String luaScript =
            "local messageListKey = KEYS[1]\n" +
            "local sessionKey = KEYS[2]\n" +
            "local messageId = ARGV[1]\n" +
            "local maxMessages = tonumber(ARGV[2])\n" +
            "local sessionTtl = tonumber(ARGV[3])\n" +
            "\n" +
            "-- Add message to list\n" +
            "redis.call('RPUSH', messageListKey, messageId)\n" +
            "\n" +
            "-- Trim to max messages\n" +
            "redis.call('LTRIM', messageListKey, -maxMessages, -1)\n" +
            "\n" +
            "-- Update session last activity\n" +
            "redis.call('HSET', sessionKey, 'lastActivity', ARGV[4])\n" +
            "redis.call('EXPIRE', sessionKey, sessionTtl)\n" +
            "\n" +
            "-- Set TTL on message list\n" +
            "redis.call('EXPIRE', messageListKey, sessionTtl)\n" +
            "\n" +
            "-- Return current list length\n" +
            "return redis.call('LLEN', messageListKey)";

        atomicMessageAddScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    private void initializeRateLimitScript() {
        String luaScript =
            "local key = KEYS[1]\n" +
            "local window = tonumber(ARGV[1])\n" +
            "local limit = tonumber(ARGV[2])\n" +
            "local current_time = tonumber(ARGV[3])\n" +
            "\n" +
            "-- Remove expired entries\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window)\n" +
            "\n" +
            "-- Count current requests\n" +
            "local current_requests = redis.call('ZCARD', key)\n" +
            "\n" +
            "if current_requests < limit then\n" +
            "    -- Add current request\n" +
            "    redis.call('ZADD', key, current_time, current_time .. math.random())\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "    return {1, limit - current_requests - 1, window}\n" +
            "else\n" +
            "    return {0, 0, window}\n" +
            "end";

        rateLimitScript = new DefaultRedisScript<>(luaScript, List.class);
    }

    private void initializeCacheWithTtlScript() {
        String luaScript =
            "local key = KEYS[1]\n" +
            "local value = ARGV[1]\n" +
            "local ttl = tonumber(ARGV[2])\n" +
            "local currentTtl = redis.call('TTL', key)\n" +
            "\n" +
            "-- Only set if key doesn't exist or TTL is less than half remaining\n" +
            "if currentTtl == -2 or (currentTtl > 0 and currentTtl < ttl / 2) then\n" +
            "    redis.call('SET', key, value)\n" +
            "    redis.call('EXPIRE', key, ttl)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";

        cacheWithTtlScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    private void initializeCleanupExpiredKeysScript() {
        String luaScript =
            "local pattern = ARGV[1]\n" +
            "local maxAge = tonumber(ARGV[2])\n" +
            "local currentTime = tonumber(ARGV[3])\n" +
            "local batchSize = tonumber(ARGV[4] or 100)\n" +
            "\n" +
            "local keys = redis.call('KEYS', pattern)\n" +
            "local deletedCount = 0\n" +
            "local processed = 0\n" +
            "\n" +
            "for i = 1, #keys do\n" +
            "    if processed >= batchSize then\n" +
            "        break\n" +
            "    end\n" +
            "    \n" +
            "    local key = keys[i]\n" +
            "    local lastActivity = redis.call('HGET', key, 'lastActivity')\n" +
            "    \n" +
            "    if lastActivity and tonumber(lastActivity) < (currentTime - maxAge) then\n" +
            "        redis.call('DEL', key)\n" +
            "        deletedCount = deletedCount + 1\n" +
            "    end\n" +
            "    \n" +
            "    processed = processed + 1\n" +
            "end\n" +
            "\n" +
            "return deletedCount";

        cleanupExpiredKeysScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    private void initializeSessionStatsScript() {
        String luaScript =
            "local sessionKey = KEYS[1]\n" +
            "local messageListKey = KEYS[2]\n" +
            "\n" +
            "local stats = {}\n" +
            "stats['messageCount'] = redis.call('HGET', sessionKey, 'messageCount') or '0'\n" +
            "stats['totalTokens'] = redis.call('HGET', sessionKey, 'totalTokens') or '0'\n" +
            "stats['lastActivity'] = redis.call('HGET', sessionKey, 'lastActivity') or ''\n" +
            "stats['createdAt'] = redis.call('HGET', sessionKey, 'createdAt') or ''\n" +
            "stats['active'] = redis.call('HGET', sessionKey, 'active') or 'true'\n" +
            "stats['messageListLength'] = redis.call('LLEN', messageListKey) or 0\n" +
            "stats['sessionTtl'] = redis.call('TTL', sessionKey) or -1\n" +
            "stats['messageListTtl'] = redis.call('TTL', messageListKey) or -1\n" +
            "\n" +
            "return stats";

        sessionStatsScript = new DefaultRedisScript<>(luaScript, Map.class);
    }

    private void initializeBulkDeleteScript() {
        String luaScript =
            "local pattern = ARGV[1]\n" +
            "local batchSize = tonumber(ARGV[2] or 100)\n" +
            "\n" +
            "local keys = redis.call('KEYS', pattern)\n" +
            "local deletedCount = 0\n" +
            "\n" +
            "for i = 1, math.min(#keys, batchSize) do\n" +
            "    redis.call('DEL', keys[i])\n" +
            "    deletedCount = deletedCount + 1\n" +
            "end\n" +
            "\n" +
            "return deletedCount";

        bulkDeleteScript = new DefaultRedisScript<>(luaScript, Long.class);
    }

    // Public methods to execute scripts

    public Long atomicSessionUpdate(String sessionKey, int messageCount, int tokenCount,
                                   String lastActivity, int ttl) {
        List<String> keys = Collections.singletonList(sessionKey);
        Object[] args = {messageCount, tokenCount, lastActivity, ttl};
        return redisTemplate.execute(atomicSessionUpdateScript, keys, args);
    }

    public Long atomicMessageAdd(String messageListKey, String sessionKey, String messageId,
                                int maxMessages, int sessionTtl, String lastActivity) {
        List<String> keys = Arrays.asList(messageListKey, sessionKey);
        Object[] args = {messageId, maxMessages, sessionTtl, lastActivity};
        return redisTemplate.execute(atomicMessageAddScript, keys, args);
    }

    public List<Long> checkRateLimit(String key, int windowSeconds, int limit) {
        List<String> keys = Collections.singletonList(key);
        Object[] args = {windowSeconds, limit, System.currentTimeMillis()};
        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(rateLimitScript, keys, args);
        return result;
    }

    public Long cacheWithTtl(String key, String value, int ttl) {
        List<String> keys = Collections.singletonList(key);
        Object[] args = {value, ttl};
        return redisTemplate.execute(cacheWithTtlScript, keys, args);
    }

    public Long cleanupExpiredKeys(String pattern, long maxAgeSeconds, int batchSize) {
        List<String> keys = Collections.emptyList();
        Object[] args = {pattern, maxAgeSeconds, System.currentTimeMillis() / 1000, batchSize};
        return redisTemplate.execute(cleanupExpiredKeysScript, keys, args);
    }

    public Map<String, Object> getSessionStats(String sessionKey, String messageListKey) {
        List<String> keys = Arrays.asList(sessionKey, messageListKey);
        Object[] args = {};
        @SuppressWarnings("unchecked")
        Map<String, Object> result = redisTemplate.execute(sessionStatsScript, keys, args);
        return result;
    }

    public Long bulkDelete(String pattern, int batchSize) {
        List<String> keys = Collections.emptyList();
        Object[] args = {pattern, batchSize};
        return redisTemplate.execute(bulkDeleteScript, keys, args);
    }

    // Convenience methods for common operations

    public boolean isRateLimited(String userId, int requestsPerMinute) {
        String key = "rate_limit:user:" + userId;
        List<Long> result = checkRateLimit(key, 60, requestsPerMinute);
        return result != null && result.size() > 0 && result.get(0) == 0;
    }

    public boolean isSessionRateLimited(String sessionId, int requestsPerMinute) {
        String key = "rate_limit:session:" + sessionId;
        List<Long> result = checkRateLimit(key, 60, requestsPerMinute);
        return result != null && result.size() > 0 && result.get(0) == 0;
    }

    public Long cleanupOldSessions(int maxAgeHours) {
        String pattern = "session:*";
        return cleanupExpiredKeys(pattern, maxAgeHours * 3600L, 100);
    }

    public Long cleanupOldMessages(int maxAgeHours) {
        String pattern = "messages:*";
        return cleanupExpiredKeys(pattern, maxAgeHours * 3600L, 100);
    }

    public Long deleteAllSessions() {
        return bulkDelete("session:*", 100);
    }

    public Long deleteAllMessages() {
        return bulkDelete("messages:*", 100);
    }

    public Long deleteAllCacheEntries() {
        return bulkDelete("cache:*", 100);
    }

    // Transaction-like operations using Lua scripts

    public boolean atomicSessionMessageUpdate(String sessionId, String messageId,
                                            int tokenCount, String lastActivity) {
        try {
            String sessionKey = "session:" + sessionId;
            String messageListKey = "messages:" + sessionId;

            Long result = atomicMessageAdd(messageListKey, sessionKey, messageId, 50, 7200, lastActivity);

            if (result != null && result > 0) {
                atomicSessionUpdate(sessionKey, 1, tokenCount, lastActivity, 7200);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to execute atomic session message update: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getComprehensiveSessionStats(String sessionId) {
        String sessionKey = "session:" + sessionId;
        String messageListKey = "messages:" + sessionId;
        return getSessionStats(sessionKey, messageListKey);
    }
}