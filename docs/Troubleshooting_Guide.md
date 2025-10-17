# Troubleshooting Guide

## Overview

This guide provides solutions to common issues encountered when running the Chatbot Context Cache API. Issues are organized by category with step-by-step resolution instructions.

## Quick Diagnostics

### Health Check Endpoints

```bash
# Check overall application health
curl http://localhost:8080/actuator/health

# Check Redis connectivity
curl http://localhost:8080/actuator/health/redis

# Check system metrics
curl http://localhost:8080/api/monitoring/metrics
```

### Log Analysis

```bash
# View application logs
tail -f logs/chatbot-cache.log

# Search for errors
grep -i error logs/chatbot-cache.log

# Check Redis operations
grep -i redis logs/chatbot-cache.log
```

## Common Issues

### 1. Redis Connection Issues

#### Issue: "Unable to connect to Redis"

**Symptoms:**
- Application fails to start
- Error: `io.lettuce.core.RedisConnectionException`
- Health endpoint shows Redis as DOWN

**Diagnostic Commands:**
```bash
# Check if Redis is running
redis-cli ping

# Check Redis server status
redis-cli info server

# Test connection with specific host/port
redis-cli -h localhost -p 6379 ping

# Check if Redis is listening
netstat -tulpn | grep 6379
```

**Solutions:**

1. **Start Redis Server:**
   ```bash
   # On macOS with Homebrew
   brew services start redis

   # On Ubuntu/Debian
   sudo systemctl start redis-server

   # Using Docker
   docker run -d -p 6379:6379 redis:7-alpine
   ```

2. **Check Configuration:**
   ```yaml
   # application.yml
   spring:
     data:
       redis:
         host: localhost  # Verify correct hostname
         port: 6379       # Verify correct port
         timeout: 2000ms  # Increase if needed
   ```

3. **Network Issues:**
   ```bash
   # Check firewall rules
   sudo ufw status

   # Test telnet connection
   telnet localhost 6379
   ```

#### Issue: "Redis authentication failed"

**Solutions:**
```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD:}  # Set correct password
```

#### Issue: "Redis SSL/TLS connection failed"

**Solutions:**
```yaml
spring:
  data:
    redis:
      ssl: true
      host: your-redis-ssl-host
      port: 6380  # Usually different port for SSL
```

### 2. Application Startup Issues

#### Issue: "Port already in use"

**Symptoms:**
- Error: `java.net.BindException: Address already in use`

**Solutions:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process using port
kill -9 <PID>

# Use different port
java -jar app.jar --server.port=8081
```

#### Issue: "OutOfMemoryError during startup"

**Solutions:**
```bash
# Increase heap size
java -Xms512m -Xmx1024m -jar app.jar

# Check current memory usage
free -h
```

#### Issue: "Bean creation failed"

**Symptoms:**
- Error: `org.springframework.beans.factory.BeanCreationException`

**Solutions:**
1. Check dependency versions in `pom.xml`
2. Verify configuration properties
3. Check for circular dependencies

### 3. Performance Issues

#### Issue: "Slow response times"

**Diagnostic Commands:**
```bash
# Check cache hit rates
curl http://localhost:8080/api/cache/stats

# Monitor performance metrics
curl http://localhost:8080/api/monitoring/metrics

# Run performance test
curl -X POST http://localhost:8080/api/demo/load-test?testType=latency
```

**Solutions:**

1. **Optimize Redis Configuration:**
   ```conf
   # redis.conf
   maxmemory-policy allkeys-lru
   maxmemory 256mb
   tcp-keepalive 300
   ```

2. **Tune Connection Pool:**
   ```yaml
   spring:
     data:
       redis:
         lettuce:
           pool:
             max-active: 20
             max-idle: 10
             min-idle: 5
   ```

3. **Enable Caching:**
   ```yaml
   app:
     cache:
       response-ttl: 3600
       enable-statistics: true
   ```

#### Issue: "High memory usage"

**Solutions:**
```bash
# Monitor memory usage
curl http://localhost:8080/api/monitoring/metrics | jq '.system.heapMemoryUsed'

# Clear cache if needed
curl -X POST http://localhost:8080/api/cache/clear/responses

# Restart with more memory
java -Xmx2048m -jar app.jar
```

### 4. Cache-Related Issues

#### Issue: "Cache miss rate too high"

**Diagnostic:**
```bash
# Check cache statistics
curl http://localhost:8080/api/cache/stats
```

**Solutions:**
1. **Increase TTL:**
   ```yaml
   app:
     cache:
       response-ttl: 7200  # Increase from 3600
   ```

2. **Optimize Cache Keys:**
   - Ensure consistent key generation
   - Check for key conflicts

3. **Warm Up Cache:**
   ```bash
   # Generate demo data to populate cache
   curl -X POST "http://localhost:8080/api/demo/generate-data?userCount=10&sessionsPerUser=5"
   ```

#### Issue: "Cache eviction happening too frequently"

**Solutions:**
```conf
# redis.conf
maxmemory 512mb           # Increase memory limit
maxmemory-policy allkeys-lru  # Use LRU eviction
```

### 5. Session Management Issues

#### Issue: "Sessions expiring too quickly"

**Solutions:**
```yaml
app:
  session:
    default-timeout: 7200  # Increase timeout (2 hours)
```

#### Issue: "Session not found errors"

**Diagnostic:**
```bash
# Check session exists
curl http://localhost:8080/api/sessions/{sessionId}

# List all sessions for user
redis-cli keys "session:*"
```

**Solutions:**
1. **Check Session ID Format:**
   - Ensure session ID is correctly formatted
   - Verify session creation response

2. **Increase Session TTL:**
   ```yaml
   app:
     cache:
       session-ttl: 14400  # 4 hours
   ```

### 6. Message Processing Issues

#### Issue: "Message too long errors"

**Solutions:**
```yaml
app:
  session:
    max-message-length: 20000  # Increase limit
```

#### Issue: "Context window overflow"

**Solutions:**
```yaml
app:
  session:
    context-window-size: 8000    # Increase window
    enable-compression: true     # Enable compression
    compression-threshold: 6000  # Lower threshold
```

### 7. Load Testing Issues

#### Issue: "Load tests failing or timing out"

**Solutions:**
1. **Increase Timeouts:**
   ```yaml
   spring:
     data:
       redis:
         timeout: 5000ms
   ```

2. **Adjust Test Parameters:**
   ```bash
   # Reduce load test intensity
   curl -X POST http://localhost:8080/api/demo/load-test \
     -H "Content-Type: application/json" \
     -d '{"iterations": 50, "concurrentUsers": 5}'
   ```

3. **Monitor System Resources:**
   ```bash
   # Check CPU and memory during tests
   top
   htop
   ```

### 8. Database/Persistence Issues

#### Issue: "Redis data persistence problems"

**Solutions:**
1. **Enable AOF:**
   ```conf
   # redis.conf
   appendonly yes
   appendfilename "chatbot-cache.aof"
   appendfsync everysec
   ```

2. **Configure RDB Snapshots:**
   ```conf
   save 900 1
   save 300 10
   save 60 10000
   ```

3. **Check Disk Space:**
   ```bash
   df -h
   ```

### 9. Docker-Related Issues

#### Issue: "Docker container fails to start"

**Solutions:**
1. **Check Docker Logs:**
   ```bash
   docker logs chatbot-cache-app
   docker logs chatbot-cache-redis
   ```

2. **Verify Network Connectivity:**
   ```bash
   # Test Redis connectivity from app container
   docker exec chatbot-cache-app ping redis
   ```

3. **Check Environment Variables:**
   ```bash
   docker exec chatbot-cache-app env | grep REDIS
   ```

### 10. API-Specific Issues

#### Issue: "404 Not Found errors"

**Solutions:**
1. **Check Base Path:**
   ```yaml
   server:
     servlet:
       context-path: /  # Ensure correct base path
   ```

2. **Verify Endpoint Mappings:**
   ```bash
   # Check available endpoints
   curl http://localhost:8080/actuator/mappings
   ```

#### Issue: "Rate limiting issues"

**Solutions:**
```yaml
app:
  rate-limit:
    enabled: false  # Disable for testing
    messages-per-minute: 120  # Increase limits
```

## Debugging Tools

### Enable Debug Logging

```yaml
logging:
  level:
    com.example.chatbotcache: DEBUG
    org.springframework.data.redis: DEBUG
    org.springframework.cache: DEBUG
```

### JVM Debugging

```bash
# Enable remote debugging
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar
```

### Redis Debugging

```bash
# Monitor Redis commands
redis-cli monitor

# Check Redis slow log
redis-cli slowlog get 10

# Analyze memory usage
redis-cli --bigkeys
```

### Performance Profiling

```bash
# Enable JVM profiling
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=app-profile.jfr -jar app.jar
```

## Monitoring and Alerting

### Key Metrics to Monitor

1. **Redis Metrics:**
   - Connection count
   - Memory usage
   - Cache hit rate
   - Command latency

2. **Application Metrics:**
   - Response times
   - Error rates
   - Active sessions
   - Thread count

3. **System Metrics:**
   - CPU usage
   - Memory usage
   - Disk space
   - Network I/O

### Sample Monitoring Queries

```bash
# Prometheus queries for alerting
rate(http_requests_total[5m]) > 100  # High request rate
redis_up == 0                        # Redis down
heap_memory_usage > 0.8              # High memory usage
```

## Recovery Procedures

### Redis Recovery

1. **Restart Redis:**
   ```bash
   sudo systemctl restart redis-server
   ```

2. **Restore from Backup:**
   ```bash
   # Stop Redis
   sudo systemctl stop redis-server

   # Restore RDB file
   cp backup.rdb /var/lib/redis/dump.rdb

   # Start Redis
   sudo systemctl start redis-server
   ```

### Application Recovery

1. **Graceful Restart:**
   ```bash
   # Send SIGTERM for graceful shutdown
   kill -TERM <PID>

   # Restart application
   java -jar app.jar
   ```

2. **Clear Corrupted Cache:**
   ```bash
   curl -X POST http://localhost:8080/api/cache/clear/all
   ```

## Prevention Best Practices

### Monitoring

1. Set up health checks
2. Monitor key metrics
3. Configure alerting
4. Regular backup testing

### Capacity Planning

1. Monitor growth trends
2. Plan for peak loads
3. Set resource limits
4. Regular performance testing

### Maintenance

1. Regular Redis maintenance
2. Log rotation
3. Cache cleanup
4. Performance tuning

## Getting Help

### Log Collection Script

```bash
#!/bin/bash
# collect-logs.sh

mkdir -p troubleshooting/$(date +%Y%m%d_%H%M%S)
cd troubleshooting/$(date +%Y%m%d_%H%M%S)

# Application logs
cp ../../logs/chatbot-cache.log .

# System info
uname -a > system-info.txt
free -h >> system-info.txt
df -h >> system-info.txt

# Redis info
redis-cli info > redis-info.txt
redis-cli config get "*" > redis-config.txt

# Application health
curl -s http://localhost:8080/actuator/health > app-health.json
curl -s http://localhost:8080/api/monitoring/metrics > app-metrics.json

echo "Logs collected in $(pwd)"
```

### Support Information Template

```
Issue Description:
[Describe the problem]

Environment:
- OS: [Operating System]
- Java Version: [Java version]
- Redis Version: [Redis version]
- Application Version: [App version]

Steps to Reproduce:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected Behavior:
[What should happen]

Actual Behavior:
[What actually happens]

Error Messages:
[Include full error messages]

Logs:
[Attach relevant log files]

Configuration:
[Include relevant configuration]
```

### Emergency Contacts

- **Application Issues:** Check GitHub issues
- **Infrastructure Issues:** Contact system administrator
- **Redis Issues:** Consult Redis documentation
- **Performance Issues:** Run built-in diagnostics first

Remember to always check the most basic issues first (Redis running, correct configuration, network connectivity) before diving into complex debugging procedures.