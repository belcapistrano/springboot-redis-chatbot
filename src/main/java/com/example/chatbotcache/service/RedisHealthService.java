package com.example.chatbotcache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RedisHealthService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired(required = false)
    private RedisPubSubService pubSubService;

    private final AtomicBoolean isHealthy = new AtomicBoolean(true);
    private final AtomicLong lastHealthCheck = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private final AtomicBoolean isClusterMode = new AtomicBoolean(false);

    private volatile RedisHealthStatus lastHealthStatus;

    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    public void performHealthCheck() {
        try {
            RedisHealthStatus status = checkRedisHealth();
            updateHealthStatus(status);

            if (status.isHealthy()) {
                consecutiveFailures.set(0);
                if (!isHealthy.get()) {
                    // Redis connection restored
                    isHealthy.set(true);
                    if (pubSubService != null) {
                        pubSubService.publishRedisConnectionRestored();
                    }
                }
            } else {
                long failures = consecutiveFailures.incrementAndGet();
                if (failures >= 3 && isHealthy.get()) {
                    // Mark as unhealthy after 3 consecutive failures
                    isHealthy.set(false);
                    if (pubSubService != null) {
                        pubSubService.publishRedisConnectionLost();
                    }
                }
            }

        } catch (Exception e) {
            consecutiveFailures.incrementAndGet();
            if (isHealthy.get() && consecutiveFailures.get() >= 3) {
                isHealthy.set(false);
                if (pubSubService != null) {
                    pubSubService.publishRedisConnectionLost();
                }
            }
        } finally {
            lastHealthCheck.set(System.currentTimeMillis());
        }
    }

    public RedisHealthStatus checkRedisHealth() {
        RedisHealthStatus.Builder statusBuilder = new RedisHealthStatus.Builder();

        try {
            long startTime = System.currentTimeMillis();

            // Test basic connectivity
            redisTemplate.execute((RedisConnection connection) -> {
                Properties info = connection.info();
                statusBuilder.withServerInfo(info);

                // Check if this is a cluster
                String clusterEnabled = info.getProperty("cluster_enabled");
                isClusterMode.set("1".equals(clusterEnabled));
                statusBuilder.withClusterMode(isClusterMode.get());

                return null;
            });

            // Test basic operations
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "ping";

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean operationsWork = testValue.equals(retrievedValue);
            statusBuilder.withOperationsWorking(operationsWork);

            long responseTime = System.currentTimeMillis() - startTime;
            statusBuilder.withResponseTime(responseTime);

            // Get memory info
            Properties info = redisTemplate.execute((RedisConnection connection) -> connection.info("memory"));
            if (info != null) {
                String usedMemory = info.getProperty("used_memory");
                String maxMemory = info.getProperty("maxmemory");
                if (usedMemory != null) {
                    statusBuilder.withUsedMemory(Long.parseLong(usedMemory));
                }
                if (maxMemory != null && !"0".equals(maxMemory)) {
                    statusBuilder.withMaxMemory(Long.parseLong(maxMemory));
                }
            }

            // Check cluster health if in cluster mode
            if (isClusterMode.get()) {
                checkClusterHealth(statusBuilder);
            }

            statusBuilder.withHealthy(operationsWork && responseTime < 5000);

        } catch (Exception e) {
            statusBuilder.withHealthy(false)
                        .withError(e.getMessage());
        }

        return statusBuilder.build();
    }

    private void checkClusterHealth(RedisHealthStatus.Builder statusBuilder) {
        try {
            redisTemplate.execute((RedisConnection connection) -> {
                Properties clusterInfo = connection.info("cluster");
                if (clusterInfo != null) {
                    String clusterState = clusterInfo.getProperty("cluster_state");
                    String clusterSlotsAssigned = clusterInfo.getProperty("cluster_slots_assigned");
                    String clusterSlotsOk = clusterInfo.getProperty("cluster_slots_ok");
                    String clusterKnownNodes = clusterInfo.getProperty("cluster_known_nodes");

                    statusBuilder.withClusterState(clusterState)
                                .withClusterSlotsAssigned(Integer.parseInt(clusterSlotsAssigned != null ? clusterSlotsAssigned : "0"))
                                .withClusterSlotsOk(Integer.parseInt(clusterSlotsOk != null ? clusterSlotsOk : "0"))
                                .withClusterKnownNodes(Integer.parseInt(clusterKnownNodes != null ? clusterKnownNodes : "0"));
                }
                return null;
            });
        } catch (Exception e) {
            statusBuilder.withClusterHealthy(false);
        }
    }

    private void updateHealthStatus(RedisHealthStatus status) {
        this.lastHealthStatus = status;
    }

    public boolean isHealthy() {
        return isHealthy.get();
    }

    public long getLastHealthCheckTime() {
        return lastHealthCheck.get();
    }

    public long getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public RedisHealthStatus getLastHealthStatus() {
        return lastHealthStatus;
    }

    public boolean isClusterMode() {
        return isClusterMode.get();
    }

    public Map<String, Object> getHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("healthy", isHealthy.get());
        summary.put("lastCheckTime", LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastHealthCheck.get()),
            java.time.ZoneId.systemDefault()));
        summary.put("consecutiveFailures", consecutiveFailures.get());
        summary.put("clusterMode", isClusterMode.get());

        if (lastHealthStatus != null) {
            summary.put("responseTime", lastHealthStatus.getResponseTime());
            summary.put("usedMemory", lastHealthStatus.getUsedMemory());
            summary.put("maxMemory", lastHealthStatus.getMaxMemory());
            summary.put("operationsWorking", lastHealthStatus.isOperationsWorking());

            if (lastHealthStatus.getError() != null) {
                summary.put("error", lastHealthStatus.getError());
            }

            if (isClusterMode.get()) {
                summary.put("clusterState", lastHealthStatus.getClusterState());
                summary.put("clusterSlotsAssigned", lastHealthStatus.getClusterSlotsAssigned());
                summary.put("clusterSlotsOk", lastHealthStatus.getClusterSlotsOk());
                summary.put("clusterKnownNodes", lastHealthStatus.getClusterKnownNodes());
            }
        }

        return summary;
    }

    public static class RedisHealthStatus {
        private final boolean healthy;
        private final long responseTime;
        private final boolean operationsWorking;
        private final long usedMemory;
        private final long maxMemory;
        private final String error;
        private final boolean clusterMode;
        private final boolean clusterHealthy;
        private final String clusterState;
        private final int clusterSlotsAssigned;
        private final int clusterSlotsOk;
        private final int clusterKnownNodes;
        private final Properties serverInfo;

        private RedisHealthStatus(Builder builder) {
            this.healthy = builder.healthy;
            this.responseTime = builder.responseTime;
            this.operationsWorking = builder.operationsWorking;
            this.usedMemory = builder.usedMemory;
            this.maxMemory = builder.maxMemory;
            this.error = builder.error;
            this.clusterMode = builder.clusterMode;
            this.clusterHealthy = builder.clusterHealthy;
            this.clusterState = builder.clusterState;
            this.clusterSlotsAssigned = builder.clusterSlotsAssigned;
            this.clusterSlotsOk = builder.clusterSlotsOk;
            this.clusterKnownNodes = builder.clusterKnownNodes;
            this.serverInfo = builder.serverInfo;
        }

        // Getters
        public boolean isHealthy() { return healthy; }
        public long getResponseTime() { return responseTime; }
        public boolean isOperationsWorking() { return operationsWorking; }
        public long getUsedMemory() { return usedMemory; }
        public long getMaxMemory() { return maxMemory; }
        public String getError() { return error; }
        public boolean isClusterMode() { return clusterMode; }
        public boolean isClusterHealthy() { return clusterHealthy; }
        public String getClusterState() { return clusterState; }
        public int getClusterSlotsAssigned() { return clusterSlotsAssigned; }
        public int getClusterSlotsOk() { return clusterSlotsOk; }
        public int getClusterKnownNodes() { return clusterKnownNodes; }
        public Properties getServerInfo() { return serverInfo; }

        public static class Builder {
            private boolean healthy = false;
            private long responseTime = 0;
            private boolean operationsWorking = false;
            private long usedMemory = 0;
            private long maxMemory = 0;
            private String error;
            private boolean clusterMode = false;
            private boolean clusterHealthy = true;
            private String clusterState;
            private int clusterSlotsAssigned = 0;
            private int clusterSlotsOk = 0;
            private int clusterKnownNodes = 0;
            private Properties serverInfo;

            public Builder withHealthy(boolean healthy) { this.healthy = healthy; return this; }
            public Builder withResponseTime(long responseTime) { this.responseTime = responseTime; return this; }
            public Builder withOperationsWorking(boolean operationsWorking) { this.operationsWorking = operationsWorking; return this; }
            public Builder withUsedMemory(long usedMemory) { this.usedMemory = usedMemory; return this; }
            public Builder withMaxMemory(long maxMemory) { this.maxMemory = maxMemory; return this; }
            public Builder withError(String error) { this.error = error; return this; }
            public Builder withClusterMode(boolean clusterMode) { this.clusterMode = clusterMode; return this; }
            public Builder withClusterHealthy(boolean clusterHealthy) { this.clusterHealthy = clusterHealthy; return this; }
            public Builder withClusterState(String clusterState) { this.clusterState = clusterState; return this; }
            public Builder withClusterSlotsAssigned(int clusterSlotsAssigned) { this.clusterSlotsAssigned = clusterSlotsAssigned; return this; }
            public Builder withClusterSlotsOk(int clusterSlotsOk) { this.clusterSlotsOk = clusterSlotsOk; return this; }
            public Builder withClusterKnownNodes(int clusterKnownNodes) { this.clusterKnownNodes = clusterKnownNodes; return this; }
            public Builder withServerInfo(Properties serverInfo) { this.serverInfo = serverInfo; return this; }

            public RedisHealthStatus build() {
                return new RedisHealthStatus(this);
            }
        }
    }
}