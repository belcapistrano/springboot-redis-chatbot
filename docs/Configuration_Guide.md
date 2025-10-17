# Configuration Guide

## Overview

This guide covers all configuration options for the Chatbot Context Cache API. The application uses Spring Boot's configuration system with support for multiple environments and externalized configuration.

## Configuration Files

### Primary Configuration

**application.yml** - Main configuration file
```yaml
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true
    mime-types: application/json,text/html,text/xml,text/plain,application/javascript,text/css

spring:
  application:
    name: chatbot-context-cache
  profiles:
    active: dev

  # Redis Configuration
  data:
    redis:
      host: localhost
      port: 6379
      password: # Leave empty for no password
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
        shutdown-timeout: 100ms

  # Cache Configuration
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1 hour in milliseconds
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "chatbot:cache:"

  # Jackson Configuration for JSON
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    deserialization:
      fail-on-unknown-properties: false
    time-zone: UTC

# Application-specific Configuration
app:
  # Session Configuration
  session:
    default-timeout: 7200 # 2 hours in seconds
    max-message-history: 50
    context-window-size: 4000
    enable-compression: true
    compression-threshold: 3000

  # Cache Configuration
  cache:
    response-ttl: 3600 # 1 hour in seconds
    session-ttl: 7200 # 2 hours in seconds
    user-preferences-ttl: 86400 # 24 hours in seconds
    enable-statistics: true

  # Rate Limiting
  rate-limit:
    messages-per-minute: 60
    sessions-per-minute: 10
    cache-operations-per-minute: 100
    performance-tests-per-hour: 5
    enabled: true

  # Performance Monitoring
  monitoring:
    enable-metrics: true
    metrics-interval: 30s
    enable-health-checks: true
    performance-logging: true

  # Demo Features
  demo:
    enable-endpoints: true
    max-demo-users: 100
    max-demo-sessions: 1000
    cleanup-interval: 3600s # 1 hour

# Management/Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    redis:
      enabled: true

# Logging Configuration
logging:
  level:
    com.example.chatbotcache: INFO
    org.springframework.data.redis: WARN
    redis.clients.lettuce: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/chatbot-cache.log
    max-size: 10MB
    max-history: 30
```

### Environment-Specific Configurations

**application-dev.yml** - Development environment
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

logging:
  level:
    com.example.chatbotcache: DEBUG
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG

app:
  demo:
    enable-endpoints: true
  monitoring:
    performance-logging: true

# Enable all actuator endpoints in dev
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

**application-test.yml** - Testing environment
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 1 # Use different database for tests

app:
  session:
    default-timeout: 300 # 5 minutes for faster tests
  cache:
    response-ttl: 60 # 1 minute for tests
  rate-limit:
    enabled: false # Disable rate limiting in tests

logging:
  level:
    com.example.chatbotcache: DEBUG
```

**application-prod.yml** - Production environment
```yaml
server:
  port: ${PORT:8080}
  forward-headers-strategy: native

spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      ssl: ${REDIS_SSL:false}

logging:
  level:
    com.example.chatbotcache: INFO
    root: WARN
  file:
    name: /var/log/chatbot-cache/application.log

app:
  demo:
    enable-endpoints: false # Disable demo endpoints in production
  monitoring:
    performance-logging: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## Environment Variables

### Required Environment Variables (Production)

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `REDIS_HOST` | Redis server hostname | localhost | redis.example.com |
| `REDIS_PORT` | Redis server port | 6379 | 6379 |
| `REDIS_PASSWORD` | Redis server password | (empty) | myredispassword |
| `REDIS_DATABASE` | Redis database number | 0 | 0 |
| `PORT` | Application server port | 8080 | 8080 |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_SSL` | Enable Redis SSL | false |
| `REDIS_TIMEOUT` | Redis connection timeout | 2000ms |
| `APP_SESSION_TIMEOUT` | Default session timeout | 7200s |
| `APP_CACHE_TTL` | Default cache TTL | 3600s |
| `APP_RATE_LIMIT_ENABLED` | Enable rate limiting | true |
| `MANAGEMENT_ENDPOINTS_INCLUDE` | Actuator endpoints to expose | health,info,metrics |

## Docker Configuration

### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - redis
    volumes:
      - ./logs:/var/log/chatbot-cache
    networks:
      - chatbot-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - chatbot-network

volumes:
  redis_data:

networks:
  chatbot-network:
    driver: bridge
```

### Redis Configuration (redis.conf)
```conf
# Redis Configuration for Chatbot Cache

# Network and Security
bind 127.0.0.1
port 6379
protected-mode yes
timeout 300

# Memory Management
maxmemory 256mb
maxmemory-policy allkeys-lru
maxmemory-samples 5

# Persistence
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename chatbot-cache.rdb

# Logging
loglevel notice
logfile /var/log/redis/redis-server.log

# Performance
tcp-keepalive 300
tcp-backlog 511
databases 16

# Append Only File
appendonly yes
appendfilename "chatbot-cache.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# Slow Log
slowlog-log-slower-than 10000
slowlog-max-len 128

# Client Configuration
timeout 0
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60
```

## Advanced Configuration

### Redis Cluster Setup

**application-cluster.yml**
```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - redis-node-1:7000
          - redis-node-2:7000
          - redis-node-3:7000
          - redis-node-4:7000
          - redis-node-5:7000
          - redis-node-6:7000
        max-redirects: 3
      lettuce:
        cluster:
          refresh:
            adaptive: true
            period: 30s
```

### SSL/TLS Configuration

**application-ssl.yml**
```yaml
spring:
  data:
    redis:
      ssl: true
      host: secure-redis.example.com
      port: 6380
      lettuce:
        pool:
          ssl:
            trust-store: classpath:redis-truststore.jks
            trust-store-password: ${REDIS_TRUSTSTORE_PASSWORD}
```

### Custom Cache Configuration

**CacheConfig.java**
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

## Performance Tuning

### JVM Configuration

**application.properties**
```properties
# JVM Memory Settings
-Xms512m
-Xmx1024m
-XX:NewRatio=2
-XX:SurvivorRatio=8

# Garbage Collection
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:+UseStringDeduplication

# Performance Monitoring
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:logs/gc.log
```

### Connection Pool Tuning

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20     # Maximum connections
          max-idle: 10       # Maximum idle connections
          min-idle: 5        # Minimum idle connections
          max-wait: 2000ms   # Maximum wait time for connection
        shutdown-timeout: 200ms
```

### Cache Strategy Configuration

```yaml
app:
  cache:
    strategies:
      responses:
        ttl: 3600s
        max-size: 10000
        eviction-policy: LRU
      sessions:
        ttl: 7200s
        max-size: 1000
        eviction-policy: LFU
      user-preferences:
        ttl: 86400s
        max-size: 5000
        eviction-policy: LRU
```

## Monitoring Configuration

### Prometheus Metrics

**prometheus.yml**
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'chatbot-cache'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### Custom Health Indicators

```java
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Custom Redis health check logic
        return Health.up()
            .withDetail("redis.version", getRedisVersion())
            .withDetail("redis.memory", getRedisMemoryInfo())
            .build();
    }
}
```

## Security Configuration

### Basic Authentication (Optional)

```yaml
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD:admin}
      roles: ADMIN

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: when-authorized
```

### CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

## Configuration Validation

### Custom Validation

```java
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Min(1)
    @Max(3600)
    private int sessionDefaultTimeout = 7200;

    @NotNull
    @Pattern(regexp = "^(redis|memory)$")
    private String cacheType = "redis";

    // getters and setters
}
```

## Troubleshooting Configuration

### Common Configuration Issues

1. **Redis Connection Issues**
   ```yaml
   # Add connection validation
   spring:
     data:
       redis:
         timeout: 5000ms
         lettuce:
           pool:
             test-on-borrow: true
             test-on-return: true
   ```

2. **Memory Configuration**
   ```yaml
   # Prevent out of memory errors
   app:
     cache:
       max-memory-usage: 80%
       eviction-threshold: 90%
   ```

3. **Performance Issues**
   ```yaml
   # Enable performance monitoring
   management:
     metrics:
       distribution:
         percentiles-histogram:
           http.server.requests: true
         percentiles:
           http.server.requests: 0.5,0.95,0.99
   ```

## Configuration Best Practices

1. **Environment Separation**: Always use different Redis databases for different environments
2. **Security**: Never commit passwords or sensitive data to version control
3. **Monitoring**: Enable appropriate monitoring based on environment
4. **Performance**: Tune connection pools based on expected load
5. **Backup**: Configure Redis persistence appropriately
6. **Logging**: Use appropriate log levels for each environment
7. **Resource Limits**: Set memory and connection limits
8. **Graceful Degradation**: Configure fallback mechanisms when Redis is unavailable

## Configuration Templates

### Minimal Configuration
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

app:
  session:
    default-timeout: 7200
  cache:
    response-ttl: 3600
```

### Full Production Configuration
```yaml
# See application-prod.yml above for complete production setup
```

### Development Configuration
```yaml
# See application-dev.yml above for complete development setup
```