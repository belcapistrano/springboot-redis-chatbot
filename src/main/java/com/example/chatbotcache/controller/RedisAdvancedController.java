package com.example.chatbotcache.controller;

import com.example.chatbotcache.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis/advanced")
public class RedisAdvancedController {

    @Autowired
    private RedisStreamService streamService;

    @Autowired
    private RedisPubSubService pubSubService;

    @Autowired
    private RedisScriptService scriptService;

    @Autowired
    private RedisHealthService healthService;

    // Stream endpoints

    @GetMapping("/streams/chat/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getChatStream(
            @PathVariable String sessionId,
            @RequestParam(required = false) String fromId,
            @RequestParam(defaultValue = "50") int count) {
        List<Map<String, Object>> messages = streamService.readChatMessages(sessionId, fromId, count);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/streams/activity/sessions")
    public ResponseEntity<List<Map<String, Object>>> getSessionActivity(
            @RequestParam(required = false) String fromId,
            @RequestParam(defaultValue = "100") int count) {
        List<Map<String, Object>> activity = streamService.readSessionActivity(fromId, count);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/streams/activity/users")
    public ResponseEntity<List<Map<String, Object>>> getUserActivity(
            @RequestParam(required = false) String fromId,
            @RequestParam(defaultValue = "100") int count) {
        List<Map<String, Object>> activity = streamService.readUserActivity(fromId, count);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/streams/events/system")
    public ResponseEntity<List<Map<String, Object>>> getSystemEvents(
            @RequestParam(required = false) String fromId,
            @RequestParam(defaultValue = "100") int count) {
        List<Map<String, Object>> events = streamService.readSystemEvents(fromId, count);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/streams")
    public ResponseEntity<List<String>> getAllChatStreams() {
        List<String> streams = streamService.getAllChatStreams();
        return ResponseEntity.ok(streams);
    }

    @PostMapping("/streams/{streamKey}/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupStream(
            @PathVariable String streamKey,
            @RequestParam(defaultValue = "7") int maxAgeDays) {
        try {
            streamService.cleanupOldStreams(Duration.ofDays(maxAgeDays));
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stream cleanup completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/streams/{streamKey}")
    public ResponseEntity<Map<String, Object>> deleteStream(@PathVariable String streamKey) {
        try {
            streamService.deleteStream(streamKey);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stream deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Pub/Sub endpoints

    @PostMapping("/pubsub/user-presence")
    public ResponseEntity<Map<String, Object>> publishUserPresence(
            @RequestParam String userId,
            @RequestParam String status,
            @RequestBody(required = false) Map<String, Object> metadata) {
        try {
            pubSubService.publishUserPresence(userId, status, metadata);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User presence published");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/pubsub/system-alert")
    public ResponseEntity<Map<String, Object>> publishSystemAlert(
            @RequestParam String alertType,
            @RequestParam String message,
            @RequestParam String severity,
            @RequestBody(required = false) Map<String, Object> details) {
        try {
            pubSubService.publishSystemAlert(alertType, message, severity, details);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "System alert published");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Script execution endpoints

    @PostMapping("/scripts/rate-limit/check")
    public ResponseEntity<Map<String, Object>> checkRateLimit(
            @RequestParam String userId,
            @RequestParam(defaultValue = "60") int requestsPerMinute) {
        boolean isLimited = scriptService.isRateLimited(userId, requestsPerMinute);
        Map<String, Object> response = new HashMap<>();
        response.put("rateLimited", isLimited);
        response.put("userId", userId);
        response.put("limit", requestsPerMinute);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scripts/sessions/{sessionId}/atomic-update")
    public ResponseEntity<Map<String, Object>> atomicSessionUpdate(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int messageCount,
            @RequestParam(defaultValue = "0") int tokenCount,
            @RequestParam String lastActivity,
            @RequestParam(defaultValue = "7200") int ttl) {
        try {
            String sessionKey = "session:" + sessionId;
            Long result = scriptService.atomicSessionUpdate(sessionKey, messageCount, tokenCount, lastActivity, ttl);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newMessageCount", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/scripts/sessions/{sessionId}/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(@PathVariable String sessionId) {
        try {
            Map<String, Object> stats = scriptService.getComprehensiveSessionStats(sessionId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/scripts/cleanup/sessions")
    public ResponseEntity<Map<String, Object>> cleanupOldSessions(
            @RequestParam(defaultValue = "72") int maxAgeHours) {
        try {
            Long deletedCount = scriptService.cleanupOldSessions(maxAgeHours);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("maxAgeHours", maxAgeHours);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/scripts/cleanup/messages")
    public ResponseEntity<Map<String, Object>> cleanupOldMessages(
            @RequestParam(defaultValue = "168") int maxAgeHours) {
        try {
            Long deletedCount = scriptService.cleanupOldMessages(maxAgeHours);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("maxAgeHours", maxAgeHours);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/scripts/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDelete(
            @RequestParam String pattern,
            @RequestParam(defaultValue = "100") int batchSize) {
        try {
            Long deletedCount = scriptService.bulkDelete(pattern, batchSize);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("pattern", pattern);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Health monitoring endpoints

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getRedisHealth() {
        Map<String, Object> healthSummary = healthService.getHealthSummary();
        return ResponseEntity.ok(healthSummary);
    }

    @PostMapping("/health/check")
    public ResponseEntity<Map<String, Object>> performHealthCheck() {
        try {
            RedisHealthService.RedisHealthStatus status = healthService.checkRedisHealth();
            Map<String, Object> response = new HashMap<>();
            response.put("healthy", status.isHealthy());
            response.put("responseTime", status.getResponseTime());
            response.put("operationsWorking", status.isOperationsWorking());
            response.put("usedMemory", status.getUsedMemory());
            response.put("maxMemory", status.getMaxMemory());
            response.put("clusterMode", status.isClusterMode());

            if (status.isClusterMode()) {
                response.put("clusterState", status.getClusterState());
                response.put("clusterSlotsAssigned", status.getClusterSlotsAssigned());
                response.put("clusterSlotsOk", status.getClusterSlotsOk());
                response.put("clusterKnownNodes", status.getClusterKnownNodes());
            }

            if (status.getError() != null) {
                response.put("error", status.getError());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("healthy", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/health/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("isHealthy", healthService.isHealthy());
        summary.put("lastCheckTime", healthService.getLastHealthCheckTime());
        summary.put("consecutiveFailures", healthService.getConsecutiveFailures());
        summary.put("clusterMode", healthService.isClusterMode());
        summary.put("fullStatus", healthService.getHealthSummary());
        return ResponseEntity.ok(summary);
    }

    // Utility endpoints

    @PostMapping("/test/performance")
    public ResponseEntity<Map<String, Object>> performanceTest(
            @RequestParam(defaultValue = "1000") int operations,
            @RequestParam(defaultValue = "10") int concurrency) {
        try {
            long startTime = System.currentTimeMillis();

            // Simulate concurrent operations
            for (int i = 0; i < operations; i++) {
                String key = "perf:test:" + i;
                String value = "test_value_" + i;
                scriptService.cacheWithTtl(key, value, 300);
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("operations", operations);
            response.put("totalTimeMs", totalTime);
            response.put("operationsPerSecond", (double) operations / (totalTime / 1000.0));
            response.put("averageLatencyMs", (double) totalTime / operations);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}