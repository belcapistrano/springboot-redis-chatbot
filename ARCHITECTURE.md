# Spring Boot Redis Chatbot - Architecture & Process Flow Documentation

## Table of Contents
1. [System Architecture Overview](#system-architecture-overview)
2. [Component Layer Architecture](#component-layer-architecture)
3. [Data Flow Diagrams](#data-flow-diagrams)
4. [Process Flow Documentation](#process-flow-documentation)
5. [Redis Integration Patterns](#redis-integration-patterns)
6. [Fallback Mechanisms](#fallback-mechanisms)
7. [Performance Optimization](#performance-optimization)

---

## System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Web Browser (Client)                        │
├─────────────────────────────────────────────────────────────────┤
│                       index.html                               │
│  • Real-time status monitoring                                 │
│  • Chat interface                                              │
│  • Cache statistics display                                    │
└─────────────────────┬───────────────────────────────────────────┘
                      │ HTTP/REST API
┌─────────────────────▼───────────────────────────────────────────┐
│               Spring Boot Application                          │
│                    (Port 8080)                                 │
├─────────────────────────────────────────────────────────────────┤
│                  Controller Layer                              │
│  ┌─────────────┬─────────────┬─────────────┬─────────────────┐ │
│  │ Chat        │ Session     │ Cache       │ Health          │ │
│  │ Controller  │ Controller  │ Controller  │ Controller      │ │
│  └─────────────┴─────────────┴─────────────┴─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Service Layer                                │
│  ┌─────────────┬─────────────┬─────────────┬─────────────────┐ │
│  │ Chat        │ Message     │ Cache       │ MockLLM         │ │
│  │ Session     │ Service     │ Service     │ Service         │ │
│  │ Service     │             │             │                 │ │
│  └─────────────┴─────────────┴─────────────┴─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                 Repository Layer                               │
│  ┌─────────────┬─────────────┬─────────────────────────────────┐ │
│  │ Chat        │ Message     │ User Preferences                │ │
│  │ Session     │ Repository  │ Repository                      │ │
│  │ Repository  │             │                                 │ │
│  └─────────────┴─────────────┴─────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │ Spring Data Redis
┌─────────────────────▼───────────────────────────────────────────┐
│                    Redis Server                                │
│                   (Port 6379)                                  │
│  • Session storage                                             │
│  • Message caching                                             │
│  • Response caching                                            │
│  • User preferences                                            │
│  • Activity tracking                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Core Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Backend Framework** | Spring Boot | 3.2.0 | Core application framework |
| **Data Access** | Spring Data Redis | 3.2.0 | Redis integration |
| **Cache Provider** | Redis | 7.x | Primary data store and cache |
| **Redis Client** | Jedis | 4.x | Java Redis client |
| **Build Tool** | Maven | 3.6+ | Dependency management |
| **Runtime** | Java | 17+ | Application runtime |
| **Frontend** | Vanilla JavaScript | ES6 | Web interface |

---

## Component Layer Architecture

### 1. Presentation Layer

#### Web Interface (`index.html`)
```javascript
// Key Components:
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend Components                         │
├─────────────────────────────────────────────────────────────────┤
│ • Chat Container (Message display)                             │
│ • Input Interface (Message composition)                        │
│ • Status Monitor (Redis connectivity)                          │
│ • Cache Statistics Display                                     │
│ • Session Management UI                                        │
│ • Real-time Updates (30s intervals)                           │
└─────────────────────────────────────────────────────────────────┘
```

**Key Features:**
- Real-time Redis status monitoring every 30 seconds
- Immediate status updates after Redis operations
- Responsive chat interface with message history
- Cache statistics with live updates
- Session creation and management

### 2. Controller Layer

#### API Endpoints Overview
```
/api/
├── sessions/                    # Session Management
│   ├── POST /                  # Create new session
│   ├── GET /{id}               # Get session details
│   ├── PUT /{id}               # Update session
│   ├── DELETE /{id}            # Delete session
│   └── /{id}/chat              # Send chat message
├── cache/                      # Cache Management
│   ├── GET /stats              # Cache statistics
│   ├── GET /status             # System status
│   ├── POST /clear/responses   # Clear response cache
│   └── GET /activity/sessions  # Session activity
├── messages/                   # Message Management
│   ├── GET /session/{id}       # Get session messages
│   ├── POST /                  # Send message
│   └── DELETE /{id}            # Delete message
└── health/                     # Health Monitoring
    ├── GET /redis              # Redis health check
    └── GET /redis/info         # Redis connection info
```

### 3. Service Layer Architecture

#### Core Services

```
Service Layer Components:
┌─────────────────────────────────────────────────────────────────┐
│                     Service Components                         │
├─────────────────────────────────────────────────────────────────┤
│ ChatSessionService                                              │
│ ├── Session lifecycle management                               │
│ ├── User context tracking                                      │
│ └── Session statistics                                         │
├─────────────────────────────────────────────────────────────────┤
│ MessageService                                                 │
│ ├── Message persistence                                        │
│ ├── Message retrieval with pagination                          │
│ └── Message cleanup operations                                 │
├─────────────────────────────────────────────────────────────────┤
│ CacheService                                                   │
│ ├── Response caching strategies                                │
│ ├── Cache statistics tracking                                  │
│ ├── Cache cleanup and management                               │
│ └── Activity monitoring                                        │
├─────────────────────────────────────────────────────────────────┤
│ MockLLMService                                                 │
│ ├── AI response simulation                                     │
│ ├── Topic classification                                       │
│ └── Response time simulation                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 4. Data Layer

#### Repository Pattern Implementation
```java
// Repository Hierarchy
@Repository
interface ChatSessionRepository extends CrudRepository<ChatSession, String> {
    // Custom session queries
}

@Repository
interface ChatMessageRepository extends CrudRepository<ChatMessage, String> {
    // Message pagination and filtering
}

@Repository
interface UserPreferencesRepository extends CrudRepository<UserPreferences, String> {
    // User settings management
}
```

---

## Data Flow Diagrams

### 1. Chat Message Flow

```
User Input → Frontend → Controller → Service → Cache Check → LLM Service → Response
     ↓         ↓           ↓           ↓          ↓            ↓           ↓
  [Browser] [index.html] [Chat    ] [Message ] [Redis   ] [Mock    ] [Database]
             [JS API  ]  [Controller] [Service ] [Cache   ] [LLM     ] [Storage ]
                                                 ↓                      ↓
                                            [Hit/Miss]              [Persist]
                                                 ↓                      ↓
                                            [Return              ] [Return  ]
                                             Cached              ] [New     ]
                                             Response]           ] [Response]
```

### 2. Session Management Flow

```
Session Creation:
Browser → ChatController → ChatSessionService → Repository → Redis
   ↓           ↓                ↓                  ↓           ↓
[Create    ] [POST       ] [Create         ] [Save      ] [Store   ]
[Session   ] [/sessions  ] [Session        ] [Entity    ] [Data    ]
[Request   ] [Endpoint   ] [Object         ] [Redis     ] [Persist ]

Session Retrieval:
Browser → ChatController → ChatSessionService → Repository → Redis
   ↓           ↓                ↓                  ↓           ↓
[Get       ] [GET        ] [Find           ] [Query     ] [Fetch   ]
[Session   ] [/sessions  ] [By ID          ] [Redis     ] [Data    ]
[Info      ] [/{id}      ] [                ] [Store     ] [Return  ]
```

### 3. Cache Status Monitoring Flow

```
Real-time Status Check:
Frontend → CacheController → RedisTemplate → Redis Server → Status Response
   ↓            ↓               ↓              ↓               ↓
[Status     ] [GET         ] [Ping       ] [Connection  ] [Connected  ]
[Request    ] [/cache/     ] [Test       ] [Test        ] [/Disconnect]
[Timer      ] [status      ] [Operation  ] [             ] [Response   ]
```

---

## Process Flow Documentation

### 1. Chat Message Processing

#### Complete Message Flow
```mermaid
graph TD
    A[User Types Message] --> B[Frontend Validation]
    B --> C[Session Check]
    C --> D{Session Exists?}
    D -->|No| E[Create Session First]
    D -->|Yes| F[Send POST Request]
    F --> G[ChatController.sendMessage()]
    G --> H[MessageService.processMessage()]
    H --> I[Cache Check]
    I --> J{Cache Hit?}
    J -->|Yes| K[Return Cached Response]
    J -->|No| L[Call MockLLMService]
    L --> M[Generate AI Response]
    M --> N[Cache Response]
    N --> O[Save to Database]
    O --> P[Return Response]
    P --> Q[Update Frontend]
    Q --> R[Display Message]
```

#### Detailed Process Steps

1. **Message Input Phase**
   - User types message in frontend interface
   - Frontend validates input (non-empty, session exists)
   - Message sent via POST to `/api/sessions/{sessionId}/chat`

2. **Backend Processing Phase**
   ```java
   // ChatController.sendMessage()
   @PostMapping("/sessions/{sessionId}/chat")
   public ResponseEntity<ChatResponse> sendMessage(
       @PathVariable String sessionId,
       @RequestBody ChatRequest request) {

       // 1. Validate session exists
       // 2. Create message entity
       // 3. Check cache for response
       // 4. Generate or retrieve response
       // 5. Update cache and statistics
       // 6. Return response
   }
   ```

3. **Cache Strategy Phase**
   - Generate cache key from message content and model
   - Check Redis for existing response
   - If cache miss, generate new response via MockLLM
   - Cache new response with TTL
   - Update cache statistics

4. **Response Generation Phase**
   ```java
   // MockLLMService response generation
   public String generateResponse(String message, String model, double temperature) {
       // 1. Simulate processing time (100-500ms)
       // 2. Classify message topic
       // 3. Generate contextual response
       // 4. Add response metadata
       return aiResponse;
   }
   ```

### 2. Session Lifecycle Management

#### Session Creation Process
```
1. Frontend Session Request
   ↓
2. ChatSessionController.createSession()
   ├── Validate request data
   ├── Generate unique session ID
   └── Create session object
   ↓
3. ChatSessionService.createSession()
   ├── Set session metadata
   ├── Initialize empty message list
   └── Set creation timestamp
   ↓
4. Repository.save()
   ├── Persist to Redis
   ├── Set Redis TTL (if configured)
   └── Update session statistics
   ↓
5. Response to Frontend
   ├── Return session details
   ├── Update UI with session ID
   └── Enable chat interface
```

#### Session Data Structure
```json
{
  "sessionId": "sess_uuid_123456",
  "userId": "user_123",
  "title": "Chat Session Title",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastActiveAt": "2024-01-15T11:45:00Z",
  "messageCount": 15,
  "status": "ACTIVE",
  "metadata": {
    "userAgent": "Browser Info",
    "ipAddress": "Client IP"
  }
}
```

### 3. Redis Connectivity Monitoring

#### Status Check Process
```java
// CacheController.getSystemStatus()
@GetMapping("/status")
public ResponseEntity<Map<String, Object>> getSystemStatus() {
    try {
        // 1. Immediate connectivity test
        redisTemplate.opsForValue().set("health:ping", "test", Duration.ofSeconds(1));
        String result = redisTemplate.opsForValue().get("health:ping");
        redisTemplate.delete("health:ping");

        // 2. Evaluate connection status
        boolean connected = "test".equals(result);

        // 3. Build status response
        return buildStatusResponse(connected);
    } catch (Exception e) {
        // 4. Handle connection failures
        return buildErrorResponse(e);
    }
}
```

#### Frontend Status Monitoring
```javascript
// Real-time status monitoring
async function checkSystemStatus() {
    try {
        const response = await fetch(`${baseUrl}/cache/status`);
        const status = await response.json();

        // Update status indicator
        updateStatusDisplay(status.redis.connected);

        // Schedule next check
        setTimeout(checkSystemStatus, 30000); // 30 seconds
    } catch (error) {
        displayConnectionError(error);
    }
}
```

---

## Redis Integration Patterns

### 1. Data Storage Patterns

#### Key Naming Convention
```
Application Prefix: chatbot:
├── Sessions: chatbot:session:{sessionId}
├── Messages: chatbot:message:{messageId}
├── Users: chatbot:user:{userId}
├── Cache: chatbot:cache:response:{hash}
├── Stats: chatbot:stats:{type}
└── Activity: chatbot:activity:{type}:{id}
```

#### Data Serialization
```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // JSON serialization for complex objects
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }
}
```

### 2. Caching Strategies

#### Response Caching
```java
// Cache key generation
private String generateCacheKey(String message, String model, double temperature) {
    String content = message + "|" + model + "|" + temperature;
    return "cache:response:" + DigestUtils.md5Hex(content);
}

// Cache with TTL
public void cacheResponse(String key, String response) {
    redisTemplate.opsForValue().set(key, response, Duration.ofHours(24));
}
```

#### Session Caching
```java
// Session persistence with automatic expiration
@RedisHash(value = "session", timeToLive = 86400) // 24 hours
public class ChatSession {
    @Id
    private String sessionId;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    // ... other fields
}
```

### 3. Performance Optimization

#### Connection Pooling
```yaml
spring:
  data:
    redis:
      jedis:
        pool:
          max-active: 8    # Maximum active connections
          max-idle: 8      # Maximum idle connections
          min-idle: 0      # Minimum idle connections
          max-wait: -1ms   # Maximum wait time for connection
```

#### Batch Operations
```java
// Batch message retrieval
public List<ChatMessage> getSessionMessages(String sessionId, int page, int size) {
    String pattern = "message:" + sessionId + ":*";
    Set<String> keys = redisTemplate.keys(pattern);

    // Use pipeline for efficient batch retrieval
    List<Object> messages = redisTemplate.executePipelined(
        (RedisCallback<Object>) connection -> {
            keys.forEach(key -> connection.get(key.getBytes()));
            return null;
        }
    );

    return processMessages(messages);
}
```

---

## Fallback Mechanisms

### 1. Redis Unavailability Handling

#### Fallback Service Architecture
```java
@Service
public class FallbackChatSessionService {
    // In-memory storage when Redis is unavailable
    private final Map<String, ChatSession> sessionStore = new ConcurrentHashMap<>();

    public ChatSession createSession(CreateSessionRequest request) {
        // Use local storage instead of Redis
        ChatSession session = new ChatSession();
        session.setSessionId(generateSessionId());
        sessionStore.put(session.getSessionId(), session);
        return session;
    }
}
```

#### Automatic Fallback Detection
```java
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FallbackCacheService fallbackService;

    public Map<String, Object> getCacheStats() {
        try {
            // Try Redis first
            return getRedisStats();
        } catch (Exception e) {
            // Automatic fallback to in-memory
            logger.warn("Redis unavailable, using fallback: {}", e.getMessage());
            return fallbackService.getStats();
        }
    }
}
```

### 2. Graceful Degradation

#### Feature Availability Matrix
| Feature | Redis Available | Redis Unavailable |
|---------|----------------|-------------------|
| **Chat Sessions** | ✅ Persistent | ⚠️ Memory-only |
| **Message History** | ✅ Persistent | ⚠️ Session-only |
| **Response Caching** | ✅ Full caching | ❌ No caching |
| **User Preferences** | ✅ Persistent | ⚠️ Default values |
| **Analytics** | ✅ Full tracking | ⚠️ Basic counters |
| **Real-time Status** | ✅ Live updates | ✅ Fallback mode |

#### Frontend Fallback Indicators
```javascript
// Status display logic
function updateStatusDisplay(redisConnected) {
    const statusElement = document.getElementById('systemStatus');

    if (redisConnected) {
        statusElement.innerHTML = '<span class="status-redis">🟢 Redis Connected</span>';
    } else {
        statusElement.innerHTML = '<span class="status-fallback">🟡 Fallback Mode</span>';
        showFallbackWarning();
    }
}

function showFallbackWarning() {
    addMessage('system', '⚠️ Running in fallback mode - some features limited');
}
```

---

## Performance Optimization

### 1. Caching Strategy Optimization

#### Multi-Level Caching
```
Level 1: Application Cache (Local)
    ├── Recent responses (LRU, 100 items)
    ├── Session metadata (5 minutes TTL)
    └── User preferences (10 minutes TTL)

Level 2: Redis Cache (Distributed)
    ├── Response cache (24 hours TTL)
    ├── Session data (24 hours TTL)
    ├── Message history (7 days TTL)
    └── Analytics data (30 days TTL)
```

#### Cache Warming Strategies
```java
@EventListener(ApplicationReadyEvent.class)
public void warmupCache() {
    // Pre-populate frequent responses
    List<String> commonQueries = getCommonQueries();
    commonQueries.forEach(this::generateAndCacheResponse);

    // Load recent session data
    loadRecentSessions();
}
```

### 2. Database Query Optimization

#### Efficient Key Patterns
```java
// Optimized key scanning
public Set<String> getSessionKeys(String userId) {
    String pattern = "session:" + userId + ":*";
    return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
        Set<String> keys = new HashSet<>();
        Cursor<byte[]> cursor = connection.scan(
            ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build()
        );
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        return keys;
    });
}
```

#### Pagination Optimization
```java
// Efficient message pagination
public MessagesPageResponse getMessages(String sessionId, int page, int size) {
    String key = "session:messages:" + sessionId;

    // Use Redis sorted sets for efficient pagination
    long start = (long) page * size;
    long end = start + size - 1;

    Set<ZSetOperations.TypedTuple<Object>> messages =
        redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);

    return buildPageResponse(messages, page, size);
}
```

### 3. Connection Management

#### Connection Pool Tuning
```yaml
spring:
  data:
    redis:
      timeout: 2000ms
      connect-timeout: 2000ms
      jedis:
        pool:
          max-active: 10    # Tune based on load
          max-idle: 5       # Keep some connections ready
          min-idle: 1       # Minimum ready connections
          max-wait: 2000ms  # Connection timeout
```

#### Health Check Optimization
```java
// Efficient health checks
@Scheduled(fixedRate = 30000) // Every 30 seconds
public void scheduleHealthCheck() {
    try {
        // Quick ping operation
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            return connection.ping();
        });

        updateHealthStatus("connected");
    } catch (Exception e) {
        updateHealthStatus("disconnected");
    }
}
```

---

## Monitoring and Observability

### 1. Metrics Collection

#### Key Performance Indicators
```java
@Component
public class MetricsCollector {

    // Cache performance metrics
    private final Counter cacheHits = Counter.build()
        .name("cache_hits_total")
        .help("Total cache hits")
        .register();

    private final Counter cacheMisses = Counter.build()
        .name("cache_misses_total")
        .help("Total cache misses")
        .register();

    // Response time metrics
    private final Histogram responseTime = Histogram.build()
        .name("response_time_seconds")
        .help("Response generation time")
        .register();
}
```

### 2. Logging Strategy

#### Structured Logging
```java
// Application-wide logging configuration
@Slf4j
public class ChatController {

    @PostMapping("/sessions/{sessionId}/chat")
    public ResponseEntity<ChatResponse> sendMessage(
        @PathVariable String sessionId,
        @RequestBody ChatRequest request) {

        MDC.put("sessionId", sessionId);
        MDC.put("userId", request.getUserId());

        try {
            log.info("Processing chat message: messageLength={}",
                request.getMessage().length());

            ChatResponse response = messageService.processMessage(request);

            log.info("Message processed successfully: responseTime={}ms",
                response.getProcessingTimeMs());

            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }
}
```

---

## Security Considerations

### 1. Input Validation
```java
@Valid
public class ChatRequest {
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message too long")
    private String message;

    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid model name")
    private String model;

    @DecimalMin(value = "0.0", message = "Temperature must be positive")
    @DecimalMax(value = "2.0", message = "Temperature too high")
    private double temperature;
}
```

### 2. Redis Security
```yaml
spring:
  data:
    redis:
      # Connection security
      ssl: true
      password: ${REDIS_PASSWORD}

      # Connection limits
      jedis:
        pool:
          max-active: 10  # Prevent connection exhaustion
```

---

This architecture documentation provides a comprehensive overview of the Spring Boot Redis Chatbot system, covering all major components, data flows, and operational considerations. The system is designed for scalability, reliability, and maintainability with robust fallback mechanisms and performance optimizations.