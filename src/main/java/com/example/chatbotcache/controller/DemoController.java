package com.example.chatbotcache.controller;

import com.example.chatbotcache.service.ChatSessionService;
import com.example.chatbotcache.service.MessageService;
import com.example.chatbotcache.service.CacheService;
import com.example.chatbotcache.service.MockLLMService;
import com.example.chatbotcache.model.ChatSession;
import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.model.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private MockLLMService mockLLMService;

    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // System metrics
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("totalSessions", ThreadLocalRandom.current().nextInt(10, 50));
        systemMetrics.put("activeConnections", ThreadLocalRandom.current().nextInt(5, 25));
        systemMetrics.put("cacheHitRate", 0.85 + ThreadLocalRandom.current().nextDouble() * 0.1);
        systemMetrics.put("avgResponseTime", 150 + ThreadLocalRandom.current().nextInt(50));
        systemMetrics.put("uptime", "2h 45m");

        // Performance metrics
        Map<String, Object> performance = new HashMap<>();
        performance.put("requestsPerSecond", 12.5 + ThreadLocalRandom.current().nextDouble() * 5);
        performance.put("memoryUsage", 65 + ThreadLocalRandom.current().nextInt(20));
        performance.put("cpuUsage", 35 + ThreadLocalRandom.current().nextInt(30));

        // Recent activities (mock data)
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "Chat Session");
            activity.put("user", "user-" + (100 + i));
            activity.put("timestamp", LocalDateTime.now().minusMinutes(i * 5).toString());
            activity.put("status", "completed");
            recentActivities.add(activity);
        }

        // Health status
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("components", Map.of(
            "redis", "UP",
            "diskSpace", "UP",
            "ping", "UP"
        ));

        data.put("systemMetrics", systemMetrics);
        data.put("performance", performance);
        data.put("recentActivities", recentActivities);
        data.put("health", health);
        data.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(data);
    }

    @GetMapping("/test-recommendations")
    public ResponseEntity<Map<String, Object>> getTestRecommendations() {
        Map<String, Object> data = new HashMap<>();

        // Add specific recommendation fields expected by the frontend
        data.put("memoryRecommendation", "Memory usage is optimal (73%). Consider monitoring during peak loads.");
        data.put("performanceRecommendation", "Cache hit rate at 85%. Implement cache warming for better performance.");

        // Add recommended test sequence
        List<String> testSequence = Arrays.asList(
            "Start with basic cache performance test",
            "Run concurrent user simulation (50 users)",
            "Execute memory stress test",
            "Perform latency analysis under load",
            "Validate system recovery after stress"
        );
        data.put("recommendedTestSequence", testSequence);

        // Add detailed recommendations for backward compatibility
        List<Map<String, String>> recommendations = new ArrayList<>();

        Map<String, String> rec1 = new HashMap<>();
        rec1.put("type", "Performance");
        rec1.put("title", "Cache Hit Rate Optimization");
        rec1.put("description", "Current cache hit rate is 85%. Consider implementing cache warming strategies.");
        rec1.put("priority", "medium");
        recommendations.add(rec1);

        Map<String, String> rec2 = new HashMap<>();
        rec2.put("type", "Load Testing");
        rec2.put("title", "Concurrent User Testing");
        rec2.put("description", "Test system with 100+ concurrent users to validate scalability.");
        rec2.put("priority", "high");
        recommendations.add(rec2);

        Map<String, String> rec3 = new HashMap<>();
        rec3.put("type", "Memory");
        rec3.put("title", "Memory Usage Monitoring");
        rec3.put("description", "Current memory usage is within normal range. Monitor for memory leaks.");
        rec3.put("priority", "low");
        recommendations.add(rec3);

        data.put("recommendations", recommendations);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/generate-data")
    public ResponseEntity<Map<String, Object>> generateDemoData(
            @RequestParam(defaultValue = "5") int userCount,
            @RequestParam(defaultValue = "2") int sessionsPerUser,
            @RequestParam(defaultValue = "8") int messagesPerSession) {

        Map<String, Object> result = new HashMap<>();
        List<String> createdSessions = new ArrayList<>();
        int totalMessages = 0;

        try {
            for (int u = 1; u <= userCount; u++) {
                String userId = "demo-user-" + u;

                for (int s = 1; s <= sessionsPerUser; s++) {
                    // Create session
                    ChatSession session = chatSessionService.createSession(userId);
                    createdSessions.add(session.getSessionId());

                    // Add messages to session
                    for (int m = 1; m <= messagesPerSession; m++) {
                        String userMessage = "Demo message " + m + " from " + userId;
                        messageService.addMessage(session.getSessionId(), MessageRole.USER, userMessage);

                        // Generate AI response
                        UserPreferences prefs = new UserPreferences();
                        String aiResponse = mockLLMService.generateResponse(session.getSessionId(), userMessage, prefs);
                        messageService.addMessage(session.getSessionId(), MessageRole.ASSISTANT, aiResponse);

                        totalMessages += 2; // user + AI message
                    }
                }
            }

            result.put("success", true);
            result.put("usersCreated", userCount);
            result.put("sessionsCreated", createdSessions.size());
            result.put("messagesCreated", totalMessages);
            result.put("sessionIds", createdSessions);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("timestampMs", System.currentTimeMillis());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/performance-comparison")
    public ResponseEntity<Map<String, Object>> performanceComparison(
            @RequestParam(defaultValue = "100") int iterations) {

        Map<String, Object> result = new HashMap<>();

        // Mock performance comparison data
        Map<String, Object> withCache = new HashMap<>();
        withCache.put("avgResponseTime", 150 + ThreadLocalRandom.current().nextInt(50));
        withCache.put("minResponseTime", 120);
        withCache.put("maxResponseTime", 300);
        withCache.put("throughput", 65.5);
        withCache.put("cacheHitRate", 0.87);

        Map<String, Object> withoutCache = new HashMap<>();
        withoutCache.put("avgResponseTime", 850 + ThreadLocalRandom.current().nextInt(200));
        withoutCache.put("minResponseTime", 600);
        withoutCache.put("maxResponseTime", 1500);
        withoutCache.put("throughput", 12.3);
        withoutCache.put("cacheHitRate", 0.0);

        result.put("withCache", withCache);
        result.put("withoutCache", withoutCache);
        result.put("iterations", iterations);
        result.put("improvementFactor", 5.7);
        result.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/stress-test")
    public ResponseEntity<Map<String, Object>> stressTest(
            @RequestParam(defaultValue = "50") int maxUsers,
            @RequestParam(defaultValue = "30") int durationSeconds) {

        Map<String, Object> result = new HashMap<>();

        // Simulate stress test execution
        try {
            Thread.sleep(1000); // Simulate test execution time

            result.put("success", true);
            result.put("maxUsers", maxUsers);
            result.put("durationSeconds", durationSeconds);
            result.put("totalRequests", maxUsers * 15);
            result.put("successfulRequests", maxUsers * 14);
            result.put("failedRequests", maxUsers);
            result.put("avgResponseTime", 280 + ThreadLocalRandom.current().nextInt(100));
            result.put("peakMemoryUsage", 78 + ThreadLocalRandom.current().nextInt(15));
            result.put("peakCpuUsage", 85 + ThreadLocalRandom.current().nextInt(10));
            result.put("overallStressScore", 85 + ThreadLocalRandom.current().nextInt(10)); // Score out of 100

            // Add recommendations array
            List<String> recommendations = Arrays.asList(
                "Consider implementing connection pooling for better resource management",
                "Monitor memory usage during peak loads",
                "Implement circuit breaker pattern for external service calls",
                "Consider horizontal scaling for handling more concurrent users"
            );
            result.put("recommendations", recommendations);
            result.put("timestamp", LocalDateTime.now().toString());

        } catch (InterruptedException e) {
            result.put("success", false);
            result.put("error", "Test interrupted");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/load-test")
    public ResponseEntity<Map<String, Object>> loadTest(
            @RequestParam String testType) {

        Map<String, Object> result = new HashMap<>();

        switch (testType.toLowerCase()) {
            case "cache":
                result.put("testType", "Cache Performance");
                result.put("status", "completed");
                result.put("iterations", 100);
                result.put("cacheHitRate", 89.0); // JavaScript expects this name
                result.put("speedImprovement", "5.6x faster");
                result.put("averageNoCacheTime", 250.0);
                result.put("averageCacheTime", 5.0);
                // Keep original fields for backward compatibility
                result.put("hitRate", 0.89);
                result.put("missRate", 0.11);
                result.put("avgCacheTime", 5);
                result.put("avgDbTime", 250);
                break;

            case "concurrent":
                result.put("testType", "Concurrent Users");
                result.put("status", "completed");
                result.put("userCount", 100); // JavaScript expects this name
                result.put("successCount", 95);
                result.put("errorCount", 5);
                result.put("avgResponseTime", 320.0);
                result.put("throughput", 45.2);
                // Keep original fields for backward compatibility
                result.put("maxConcurrentUsers", 100);
                break;

            case "latency":
                result.put("testType", "Latency Analysis");
                result.put("status", "completed");
                result.put("iterations", 200);
                result.put("avgLatency", 165.0); // JavaScript expects this name
                result.put("minLatency", 50);
                result.put("maxLatency", 450);
                result.put("p95Latency", 380.0);
                // Keep original fields for backward compatibility
                result.put("p50", 150);
                result.put("p90", 280);
                result.put("p99", 450);
                break;

            case "memory":
                result.put("testType", "Memory Usage");
                result.put("status", "completed");
                result.put("sessionCount", 50); // JavaScript expects this name
                result.put("memoryUsed", 768); // JavaScript expects this name
                result.put("avgMemoryPerSession", 15.36); // JavaScript expects this name
                // Keep original fields for backward compatibility
                result.put("initialMemory", 512);
                result.put("peakMemory", 1024);
                result.put("finalMemory", 768);
                result.put("memoryLeaks", false);
                break;

            default:
                result.put("status", "failed");
                result.put("error", "Unknown test type: " + testType);
        }

        result.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/demo-scenario/{scenarioName}")
    public ResponseEntity<Map<String, Object>> runDemoScenario(@PathVariable String scenarioName) {
        Map<String, Object> result = new HashMap<>();

        switch (scenarioName.toLowerCase()) {
            case "basic-chat":
                result.put("scenario", "Basic Chat Flow");
                result.put("steps", Arrays.asList("Create session", "Send message", "Receive response"));
                result.put("success", true);
                result.put("duration", 1250);
                break;

            case "multi-user":
                result.put("scenario", "Multi-User Simulation");
                result.put("users", 10);
                result.put("totalMessages", 50);
                result.put("success", true);
                result.put("duration", 5500);
                break;

            case "cache-validation":
                result.put("scenario", "Cache Validation");
                result.put("cacheHits", 23);
                result.put("cacheMisses", 7);
                result.put("success", true);
                result.put("duration", 2100);
                break;

            default:
                result.put("error", "Unknown scenario: " + scenarioName);
                result.put("success", false);
        }

        result.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(result);
    }
}