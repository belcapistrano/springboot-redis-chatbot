# Deployment Guide

## Overview

This guide covers various deployment strategies for the Chatbot Context Cache API, from local development to production environments including cloud platforms and containerization.

## Prerequisites

### System Requirements

- **Java**: OpenJDK 17 or later
- **Redis**: Version 6.0 or later (7.0+ recommended)
- **Memory**: Minimum 1GB RAM (2GB+ recommended for production)
- **Storage**: Minimum 1GB available disk space
- **Network**: Ports 8080 (app) and 6379 (Redis) available

### Development Tools

- **Maven**: 3.6.0 or later
- **Docker**: 20.10 or later (for containerized deployment)
- **Git**: For source code management

## Local Development Deployment

### Quick Start

1. **Clone Repository:**
   ```bash
   git clone https://github.com/your-repo/springboot-with-redis.git
   cd springboot-with-redis
   ```

2. **Start Redis:**
   ```bash
   # Using Docker
   docker run -d -p 6379:6379 --name redis redis:7-alpine

   # Using Homebrew (macOS)
   brew services start redis

   # Using apt (Ubuntu/Debian)
   sudo apt install redis-server
   sudo systemctl start redis-server
   ```

3. **Build and Run Application:**
   ```bash
   # Build the application
   mvn clean install

   # Run with development profile
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Verify Deployment:**
   ```bash
   # Check health
   curl http://localhost:8080/actuator/health

   # Test API
   curl -X POST http://localhost:8080/api/sessions \
     -H "Content-Type: application/json" \
     -d '{"userId": "test-user"}'
   ```

### Development Profile Configuration

Create `application-dev.yml`:
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

app:
  demo:
    enable-endpoints: true
  monitoring:
    performance-logging: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## Docker Deployment

### Single Container Deployment

1. **Create Dockerfile:**
   ```dockerfile
   FROM openjdk:17-jre-slim

   LABEL maintainer="your-email@example.com"
   LABEL description="Chatbot Context Cache API"

   # Install curl for health checks
   RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

   # Create app directory
   WORKDIR /app

   # Copy application JAR
   COPY target/chatbot-context-cache-*.jar app.jar

   # Create logs directory
   RUN mkdir -p /var/log/chatbot-cache

   # Expose port
   EXPOSE 8080

   # Health check
   HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
     CMD curl -f http://localhost:8080/actuator/health || exit 1

   # Run application
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **Build Docker Image:**
   ```bash
   # Build application first
   mvn clean package -DskipTests

   # Build Docker image
   docker build -t chatbot-cache:latest .
   ```

3. **Run Container:**
   ```bash
   # Start Redis first
   docker run -d --name redis redis:7-alpine

   # Start application
   docker run -d \
     --name chatbot-cache \
     --link redis:redis \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e REDIS_HOST=redis \
     chatbot-cache:latest
   ```

### Docker Compose Deployment

1. **Create docker-compose.yml:**
   ```yaml
   version: '3.8'

   services:
     app:
       build: .
       container_name: chatbot-cache-app
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
       restart: unless-stopped
       healthcheck:
         test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
         interval: 30s
         timeout: 10s
         retries: 3
         start_period: 60s

     redis:
       image: redis:7-alpine
       container_name: chatbot-cache-redis
       ports:
         - "6379:6379"
       volumes:
         - redis_data:/data
         - ./redis.conf:/usr/local/etc/redis/redis.conf
       command: redis-server /usr/local/etc/redis/redis.conf
       networks:
         - chatbot-network
       restart: unless-stopped
       healthcheck:
         test: ["CMD", "redis-cli", "ping"]
         interval: 30s
         timeout: 10s
         retries: 3

   volumes:
     redis_data:

   networks:
     chatbot-network:
       driver: bridge
   ```

2. **Deploy with Docker Compose:**
   ```bash
   # Build and start all services
   docker-compose up -d

   # View logs
   docker-compose logs -f

   # Scale application (if needed)
   docker-compose up -d --scale app=3

   # Stop services
   docker-compose down
   ```

### Multi-Stage Docker Build

```dockerfile
# Multi-stage build for smaller production image
FROM maven:3.8.6-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/chatbot-context-cache-*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Cloud Deployment

### AWS Deployment

#### Using AWS Elastic Beanstalk

1. **Prepare Application:**
   ```bash
   # Package application
   mvn clean package

   # Create deployment package
   zip -j chatbot-cache.zip target/chatbot-context-cache-*.jar
   ```

2. **Deploy to Elastic Beanstalk:**
   ```bash
   # Install EB CLI
   pip install awsebcli

   # Initialize EB application
   eb init

   # Create environment
   eb create production

   # Deploy application
   eb deploy
   ```

3. **Configure Environment Variables:**
   ```bash
   eb setenv SPRING_PROFILES_ACTIVE=prod
   eb setenv REDIS_HOST=your-redis-host
   eb setenv REDIS_PASSWORD=your-redis-password
   ```

#### Using Amazon ECS

1. **Create Task Definition:**
   ```json
   {
     "family": "chatbot-cache",
     "taskRoleArn": "arn:aws:iam::account:role/ecsTaskRole",
     "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
     "networkMode": "awsvpc",
     "requiresCompatibilities": ["FARGATE"],
     "cpu": "1024",
     "memory": "2048",
     "containerDefinitions": [
       {
         "name": "chatbot-cache",
         "image": "your-account.dkr.ecr.region.amazonaws.com/chatbot-cache:latest",
         "portMappings": [
           {
             "containerPort": 8080,
             "protocol": "tcp"
           }
         ],
         "environment": [
           {
             "name": "SPRING_PROFILES_ACTIVE",
             "value": "prod"
           },
           {
             "name": "REDIS_HOST",
             "value": "your-elasticache-endpoint"
           }
         ],
         "logConfiguration": {
           "logDriver": "awslogs",
           "options": {
             "awslogs-group": "/ecs/chatbot-cache",
             "awslogs-region": "us-west-2",
             "awslogs-stream-prefix": "ecs"
           }
         }
       }
     ]
   }
   ```

2. **Deploy with CloudFormation:**
   ```yaml
   # cloudformation-template.yml
   AWSTemplateFormatVersion: '2010-09-09'
   Resources:
     ECSCluster:
       Type: AWS::ECS::Cluster
       Properties:
         ClusterName: chatbot-cache-cluster

     ECSService:
       Type: AWS::ECS::Service
       Properties:
         Cluster: !Ref ECSCluster
         TaskDefinition: !Ref TaskDefinition
         DesiredCount: 2
         LaunchType: FARGATE
   ```

### Google Cloud Platform

#### Using Cloud Run

1. **Build and Push to Container Registry:**
   ```bash
   # Configure Docker for GCP
   gcloud auth configure-docker

   # Build and tag image
   docker build -t gcr.io/PROJECT-ID/chatbot-cache:latest .

   # Push image
   docker push gcr.io/PROJECT-ID/chatbot-cache:latest
   ```

2. **Deploy to Cloud Run:**
   ```bash
   gcloud run deploy chatbot-cache \
     --image gcr.io/PROJECT-ID/chatbot-cache:latest \
     --platform managed \
     --region us-central1 \
     --set-env-vars SPRING_PROFILES_ACTIVE=prod \
     --set-env-vars REDIS_HOST=redis-ip \
     --memory 2Gi \
     --cpu 2 \
     --max-instances 10
   ```

### Microsoft Azure

#### Using Azure Container Instances

1. **Create Resource Group:**
   ```bash
   az group create --name chatbot-cache-rg --location eastus
   ```

2. **Deploy Container:**
   ```bash
   az container create \
     --resource-group chatbot-cache-rg \
     --name chatbot-cache \
     --image chatbot-cache:latest \
     --ports 8080 \
     --environment-variables \
       SPRING_PROFILES_ACTIVE=prod \
       REDIS_HOST=redis-host \
     --cpu 2 \
     --memory 4
   ```

## Kubernetes Deployment

### Basic Kubernetes Manifests

1. **Namespace:**
   ```yaml
   # namespace.yml
   apiVersion: v1
   kind: Namespace
   metadata:
     name: chatbot-cache
   ```

2. **ConfigMap:**
   ```yaml
   # configmap.yml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: chatbot-cache-config
     namespace: chatbot-cache
   data:
     SPRING_PROFILES_ACTIVE: "prod"
     REDIS_HOST: "redis-service"
     REDIS_PORT: "6379"
   ```

3. **Secret:**
   ```yaml
   # secret.yml
   apiVersion: v1
   kind: Secret
   metadata:
     name: chatbot-cache-secret
     namespace: chatbot-cache
   type: Opaque
   data:
     redis-password: <base64-encoded-password>
   ```

4. **Deployment:**
   ```yaml
   # deployment.yml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: chatbot-cache
     namespace: chatbot-cache
   spec:
     replicas: 3
     selector:
       matchLabels:
         app: chatbot-cache
     template:
       metadata:
         labels:
           app: chatbot-cache
       spec:
         containers:
         - name: chatbot-cache
           image: chatbot-cache:latest
           ports:
           - containerPort: 8080
           envFrom:
           - configMapRef:
               name: chatbot-cache-config
           env:
           - name: REDIS_PASSWORD
             valueFrom:
               secretKeyRef:
                 name: chatbot-cache-secret
                 key: redis-password
           resources:
             requests:
               memory: "1Gi"
               cpu: "500m"
             limits:
               memory: "2Gi"
               cpu: "1000m"
           livenessProbe:
             httpGet:
               path: /actuator/health
               port: 8080
             initialDelaySeconds: 60
             periodSeconds: 30
           readinessProbe:
             httpGet:
               path: /actuator/health
               port: 8080
             initialDelaySeconds: 30
             periodSeconds: 10
   ```

5. **Service:**
   ```yaml
   # service.yml
   apiVersion: v1
   kind: Service
   metadata:
     name: chatbot-cache-service
     namespace: chatbot-cache
   spec:
     selector:
       app: chatbot-cache
     ports:
     - protocol: TCP
       port: 80
       targetPort: 8080
     type: LoadBalancer
   ```

6. **Redis Deployment:**
   ```yaml
   # redis-deployment.yml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: redis
     namespace: chatbot-cache
   spec:
     replicas: 1
     selector:
       matchLabels:
         app: redis
     template:
       metadata:
         labels:
           app: redis
       spec:
         containers:
         - name: redis
           image: redis:7-alpine
           ports:
           - containerPort: 6379
           volumeMounts:
           - name: redis-data
             mountPath: /data
         volumes:
         - name: redis-data
           persistentVolumeClaim:
             claimName: redis-pvc
   ```

7. **Apply Manifests:**
   ```bash
   kubectl apply -f namespace.yml
   kubectl apply -f configmap.yml
   kubectl apply -f secret.yml
   kubectl apply -f deployment.yml
   kubectl apply -f service.yml
   kubectl apply -f redis-deployment.yml
   ```

### Helm Chart Deployment

1. **Create Helm Chart:**
   ```bash
   helm create chatbot-cache
   ```

2. **Configure values.yml:**
   ```yaml
   # values.yml
   replicaCount: 3

   image:
     repository: chatbot-cache
     tag: latest
     pullPolicy: IfNotPresent

   service:
     type: LoadBalancer
     port: 80
     targetPort: 8080

   ingress:
     enabled: true
     annotations:
       kubernetes.io/ingress.class: nginx
     hosts:
       - host: chatbot-cache.example.com
         paths:
           - path: /
             pathType: Prefix

   resources:
     limits:
       cpu: 1000m
       memory: 2Gi
     requests:
       cpu: 500m
       memory: 1Gi

   redis:
     enabled: true
     host: redis-service
     port: 6379

   config:
     springProfiles: prod
   ```

3. **Deploy with Helm:**
   ```bash
   helm install chatbot-cache ./chatbot-cache
   ```

## Production Configuration

### Environment Variables

```bash
# Required
export SPRING_PROFILES_ACTIVE=prod
export REDIS_HOST=production-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=secure-password

# Optional
export SERVER_PORT=8080
export MANAGEMENT_ENDPOINTS_INCLUDE=health,info,metrics
export APP_SESSION_TIMEOUT=7200
export APP_CACHE_TTL=3600
```

### Production application.yml

```yaml
server:
  port: ${SERVER_PORT:8080}
  tomcat:
    max-threads: 200
    min-spare-threads: 10

spring:
  profiles:
    active: prod
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      ssl: ${REDIS_SSL:false}
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms

logging:
  level:
    com.example.chatbotcache: INFO
    root: WARN
  file:
    name: /var/log/chatbot-cache/application.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_INCLUDE:health,info,metrics}
  metrics:
    export:
      prometheus:
        enabled: true

app:
  demo:
    enable-endpoints: false
  monitoring:
    performance-logging: false
  rate-limit:
    enabled: true
```

## Scaling and Load Balancing

### Horizontal Scaling

1. **Docker Compose Scaling:**
   ```bash
   docker-compose up -d --scale app=5
   ```

2. **Kubernetes Scaling:**
   ```bash
   kubectl scale deployment chatbot-cache --replicas=5
   ```

3. **Auto-scaling (Kubernetes):**
   ```yaml
   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: chatbot-cache-hpa
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: chatbot-cache
     minReplicas: 2
     maxReplicas: 10
     metrics:
     - type: Resource
       resource:
         name: cpu
         target:
           type: Utilization
           averageUtilization: 70
   ```

### Load Balancer Configuration

**Nginx Configuration:**
```nginx
upstream chatbot_cache {
    server app1:8080;
    server app2:8080;
    server app3:8080;
}

server {
    listen 80;
    server_name chatbot-cache.example.com;

    location / {
        proxy_pass http://chatbot_cache;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Health check
        location /actuator/health {
            proxy_pass http://chatbot_cache;
            access_log off;
        }
    }
}
```

## Monitoring and Observability

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'chatbot-cache'
    static_configs:
      - targets: ['app:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Chatbot Cache Metrics",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])"
          }
        ]
      },
      {
        "title": "Cache Hit Rate",
        "type": "singlestat",
        "targets": [
          {
            "expr": "cache_hit_rate"
          }
        ]
      }
    ]
  }
}
```

## Backup and Recovery

### Database Backup

```bash
#!/bin/bash
# backup-redis.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/redis"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup Redis data
redis-cli --rdb $BACKUP_DIR/dump_$DATE.rdb

# Backup configuration
cp /etc/redis/redis.conf $BACKUP_DIR/redis_$DATE.conf

# Cleanup old backups (keep 7 days)
find $BACKUP_DIR -name "dump_*.rdb" -mtime +7 -delete
find $BACKUP_DIR -name "redis_*.conf" -mtime +7 -delete
```

### Application Backup

```bash
#!/bin/bash
# backup-app.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/app"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup logs
tar -czf $BACKUP_DIR/logs_$DATE.tar.gz /var/log/chatbot-cache/

# Backup configuration
cp application*.yml $BACKUP_DIR/
```

## Security Considerations

### Network Security

1. **Firewall Rules:**
   ```bash
   # Allow only necessary ports
   ufw allow 22    # SSH
   ufw allow 8080  # Application
   ufw deny 6379   # Redis (internal only)
   ```

2. **Redis Security:**
   ```conf
   # redis.conf
   bind 127.0.0.1
   requirepass your-secure-password
   rename-command FLUSHDB ""
   rename-command FLUSHALL ""
   ```

### SSL/TLS Configuration

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: chatbot-cache
```

## Performance Optimization

### JVM Tuning

```bash
# Production JVM settings
java -Xms1g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:+UseStringDeduplication \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     -Xloggc:gc.log \
     -jar app.jar
```

### Redis Optimization

```conf
# redis.conf optimizations
maxmemory 1gb
maxmemory-policy allkeys-lru
tcp-keepalive 300
tcp-backlog 511
timeout 300
```

## Deployment Checklist

### Pre-Deployment

- [ ] Code reviewed and tested
- [ ] Dependencies updated
- [ ] Configuration validated
- [ ] Backup procedures tested
- [ ] Monitoring configured
- [ ] Security settings verified

### Deployment

- [ ] Application deployed
- [ ] Health checks passing
- [ ] Monitoring active
- [ ] Logs being collected
- [ ] Performance metrics baseline
- [ ] Load balancer configured

### Post-Deployment

- [ ] End-to-end testing
- [ ] Performance validation
- [ ] Error rate monitoring
- [ ] Capacity planning review
- [ ] Documentation updated
- [ ] Team notified

## Rollback Procedures

### Application Rollback

```bash
# Docker rollback
docker-compose down
docker-compose up -d --scale app=0
docker tag chatbot-cache:previous chatbot-cache:latest
docker-compose up -d

# Kubernetes rollback
kubectl rollout undo deployment/chatbot-cache

# Manual rollback
cp app-previous.jar app.jar
systemctl restart chatbot-cache
```

### Database Rollback

```bash
# Redis rollback
redis-cli SHUTDOWN
cp backup/dump_previous.rdb /var/lib/redis/dump.rdb
systemctl start redis-server
```

Remember to always test deployment procedures in a staging environment before applying to production.