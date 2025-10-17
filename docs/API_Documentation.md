# Chatbot Context Cache API Documentation

## Overview

The Chatbot Context Cache API is a comprehensive Spring Boot application that demonstrates Redis capabilities through a chatbot caching system. This API provides session management, intelligent response caching, real-time features, and performance monitoring.

## Base URL

```
Local Development: http://localhost:8080
Production: https://your-app.herokuapp.com
```

## Authentication

Currently, this API does not require authentication for demonstration purposes. In production, you would implement appropriate security measures.

## Core Concepts

### Sessions
- Chat sessions represent individual conversations
- Each session has a unique ID and belongs to a user
- Sessions automatically expire after 2 hours of inactivity
- Session data is cached in Redis for performance

### Messages
- Messages are stored within sessions in chronological order
- Message history is limited to 50 messages per session
- Older messages are automatically compressed into summaries
- All messages include token counts for context management

### Caching
- Responses are cached using SHA-256 content hashing
- Cache keys include session context for personalization
- Default TTL is 1 hour for cached responses
- Cache hit rates typically exceed 60% in normal usage

## API Endpoints

### 1. Session Management

#### Create Session
```http
POST /api/sessions
Content-Type: application/json

{
  "userId": "user-123",
  "preferences": {
    "language": "en",
    "theme": "light"
  }
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "userId": "user-123",
  "createdAt": "2023-12-01T10:30:00Z",
  "lastActivity": "2023-12-01T10:30:00Z",
  "messageCount": 0,
  "status": "ACTIVE"
}
```

#### Get Session
```http
GET /api/sessions/{sessionId}
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "userId": "user-123",
  "createdAt": "2023-12-01T10:30:00Z",
  "lastActivity": "2023-12-01T11:45:00Z",
  "messageCount": 15,
  "status": "ACTIVE",
  "tokenCount": 2450,
  "contextSummary": "Discussion about API design and Redis caching strategies..."
}
```

#### Delete Session
```http
DELETE /api/sessions/{sessionId}
```

**Response:**
```json
{
  "message": "Session deleted successfully",
  "sessionId": "sess_abc123def456"
}
```

### 2. Messages

#### Send Message
```http
POST /api/sessions/{sessionId}/chat
Content-Type: application/json

{
  "message": "How does Redis caching improve performance?",
  "metadata": {
    "timestamp": "2023-12-01T11:45:00Z",
    "source": "web"
  }
}
```

**Response:**
```json
{
  "userMessage": {
    "messageId": "msg_user_789",
    "content": "How does Redis caching improve performance?",
    "role": "USER",
    "timestamp": "2023-12-01T11:45:00Z",
    "tokenCount": 8
  },
  "assistantMessage": {
    "messageId": "msg_asst_790",
    "content": "Redis caching improves performance by storing frequently accessed data in memory, reducing database queries and API calls. Key benefits include: 1) Sub-millisecond response times, 2) Reduced backend load, 3) Better user experience, 4) Scalability improvements.",
    "role": "ASSISTANT",
    "timestamp": "2023-12-01T11:45:02Z",
    "tokenCount": 45
  },
  "cacheHit": false,
  "responseTime": 127,
  "sessionUpdated": true
}
```

#### Get Messages
```http
GET /api/sessions/{sessionId}/messages?page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "messageId": "msg_user_789",
      "content": "How does Redis caching improve performance?",
      "role": "USER",
      "timestamp": "2023-12-01T11:45:00Z",
      "tokenCount": 8
    },
    {
      "messageId": "msg_asst_790",
      "content": "Redis caching improves performance by...",
      "role": "ASSISTANT",
      "timestamp": "2023-12-01T11:45:02Z",
      "tokenCount": 45
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 3. User Preferences

#### Get User Preferences
```http
GET /api/users/{userId}/preferences
```

**Response:**
```json
{
  "userId": "user-123",
  "displayName": "John Doe",
  "language": "en",
  "theme": "dark",
  "responseLength": "detailed",
  "responseTone": "professional",
  "enableNotifications": true,
  "autoSaveHistory": true,
  "maxHistorySize": 100,
  "contextWindowSize": 4000,
  "customSettings": {
    "showTimestamps": true,
    "soundEnabled": false
  }
}
```

#### Update User Preferences
```http
PUT /api/users/{userId}/preferences
Content-Type: application/json

{
  "displayName": "John Doe",
  "language": "en",
  "theme": "dark",
  "responseLength": "concise",
  "responseTone": "casual",
  "enableNotifications": false
}
```

### 4. Context Management

#### Get Session Context
```http
GET /api/sessions/{sessionId}/context
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "tokenCount": 2450,
  "messageCount": 15,
  "contextWindow": 4000,
  "compressionApplied": true,
  "summary": "User asking about Redis performance benefits and caching strategies...",
  "recentMessages": 8,
  "oldestMessageAge": "2023-12-01T10:30:00Z"
}
```

#### Compress Context
```http
POST /api/sessions/{sessionId}/compress
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "compressionApplied": true,
  "tokensBefore": 4200,
  "tokensAfter": 2450,
  "messagesBefore": 25,
  "messagesAfter": 15,
  "summary": "Generated summary of compressed conversation context..."
}
```

### 5. Cache Management

#### Get Cache Statistics
```http
GET /api/cache/stats
```

**Response:**
```json
{
  "totalKeys": 1547,
  "hitCount": 8234,
  "missCount": 2156,
  "hitRate": 79.25,
  "memoryUsage": "45.2 MB",
  "responsesCached": 1245,
  "sessionsCached": 302,
  "avgResponseTime": 23.5,
  "topCachedResponses": [
    {
      "content": "Hello! How can I help you today?",
      "hits": 156
    }
  ]
}
```

#### Clear Cache
```http
POST /api/cache/clear/{pattern}
```

Examples:
- `/api/cache/clear/responses` - Clear all response cache
- `/api/cache/clear/sessions` - Clear all session cache
- `/api/cache/clear/user:123` - Clear cache for specific user

### 6. Monitoring & Metrics

#### Get System Metrics
```http
GET /api/monitoring/metrics
```

**Response:**
```json
{
  "application": {
    "totalOperations": 15423,
    "totalErrors": 23,
    "errorRate": 0.15,
    "uptime": "2 days, 14 hours, 30 minutes"
  },
  "cache": {
    "hitRates": {
      "responses": 79.25,
      "sessions": 95.12
    },
    "totalHits": 8234,
    "totalMisses": 2156
  },
  "performance": {
    "averageLatencies": {
      "chat": 45.2,
      "session-create": 12.8
    },
    "p95Latencies": {
      "chat": 125.0,
      "session-create": 28.5
    },
    "throughput": 156.7
  },
  "system": {
    "heapMemoryUsed": 512,
    "heapMemoryMax": 1024,
    "threadCount": 25,
    "availableProcessors": 8
  }
}
```

### 7. Demo & Performance Testing

#### Generate Demo Data
```http
POST /api/demo/generate-data?userCount=10&sessionsPerUser=2&messagesPerSession=15
```

**Response:**
```json
{
  "usersCreated": 10,
  "sessionsCreated": 20,
  "messagesCreated": 300,
  "userIds": ["demo-user-1", "demo-user-2", ...],
  "sessionIds": ["sess_demo_1", "sess_demo_2", ...],
  "timestamp": 1701434567890,
  "status": "success"
}
```

#### Run Performance Test
```http
POST /api/demo/load-test?testType=cache
Content-Type: application/json

{
  "iterations": 100
}
```

**Response:**
```json
{
  "testType": "cache",
  "status": "completed",
  "iterations": 100,
  "cacheHitRate": 65.5,
  "averageNoCacheTime": 127.3,
  "averageCacheTime": 8.2,
  "speedImprovement": "15.52x",
  "timestamp": 1701434567890
}
```

### 8. Redis Advanced Features

#### Publish Event
```http
POST /api/redis/pubsub/publish
Content-Type: application/json

{
  "channel": "chat.events",
  "message": {
    "type": "message_sent",
    "sessionId": "sess_abc123",
    "userId": "user-123",
    "timestamp": "2023-12-01T11:45:00Z"
  }
}
```

#### Add to Stream
```http
POST /api/redis/streams/add
Content-Type: application/json

{
  "stream": "session:sess_abc123:messages",
  "data": {
    "messageId": "msg_790",
    "content": "Hello Redis Streams!",
    "role": "USER"
  }
}
```

#### Execute Lua Script
```http
POST /api/redis/scripts/execute
Content-Type: application/json

{
  "scriptName": "updateSessionWithMessage",
  "keys": ["session:sess_abc123", "messages:sess_abc123"],
  "args": ["msg_790", "Hello from Lua!", "USER", "1701434567"]
}
```

### 9. Health Checks

#### System Health
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.5"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000
      }
    },
    "custom": {
      "status": "UP",
      "details": {
        "activeUsers": 45,
        "totalMessages": 1547,
        "cacheHitRate": "79.25%",
        "errorRate": "0.15%",
        "uptime": "2 days, 14 hours, 30 minutes"
      }
    }
  }
}
```

## Error Handling

### Standard Error Response Format

```json
{
  "timestamp": "2023-12-01T11:45:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Session not found",
  "path": "/api/sessions/invalid-session",
  "details": {
    "sessionId": "invalid-session",
    "suggestion": "Create a new session using POST /api/sessions"
  }
}
```

### Common HTTP Status Codes

- **200 OK**: Successful operation
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request parameters
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource already exists
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server error
- **503 Service Unavailable**: Redis connection unavailable (fallback mode active)

### Error Codes

| Code | Description | Resolution |
|------|-------------|------------|
| `SESSION_NOT_FOUND` | Session ID does not exist | Create new session or check session ID |
| `SESSION_EXPIRED` | Session has expired | Create new session |
| `MESSAGE_TOO_LONG` | Message exceeds length limit | Reduce message length (max 10,000 chars) |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Wait before making more requests |
| `REDIS_UNAVAILABLE` | Redis connection failed | System running in fallback mode |
| `CONTEXT_OVERFLOW` | Token limit exceeded | Context compression will be applied |

## Rate Limiting

The API implements rate limiting to ensure fair usage:

- **Messages**: 60 requests per minute per session
- **Session Creation**: 10 requests per minute per user
- **Cache Operations**: 100 requests per minute per user
- **Performance Tests**: 5 requests per hour per IP

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1701434567
```

## WebSocket Support

Real-time features are available via WebSocket connections:

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/session/sess_abc123');

ws.onmessage = function(event) {
  const data = JSON.parse(event.data);
  console.log('Real-time update:', data);
};
```

### WebSocket Message Types

- `message_received`: New message in session
- `typing_indicator`: User typing status
- `session_updated`: Session metadata changed
- `cache_cleared`: Cache has been cleared
- `system_alert`: System notifications

## Performance Considerations

### Optimal Usage Patterns

1. **Session Management**
   - Reuse sessions for related conversations
   - Clean up inactive sessions regularly
   - Monitor session token counts

2. **Caching**
   - Allow cache warming for common queries
   - Monitor cache hit rates (target >60%)
   - Use appropriate TTL values

3. **Context Management**
   - Enable automatic compression
   - Set reasonable context window sizes
   - Monitor token usage

### Scaling Recommendations

- **Redis Clustering**: Use Redis Cluster for horizontal scaling
- **Connection Pooling**: Configure appropriate pool sizes
- **Monitoring**: Set up alerts for key metrics
- **Load Balancing**: Distribute requests across multiple app instances

## SDK and Client Libraries

### JavaScript/TypeScript
```javascript
import { ChatbotCacheClient } from 'chatbot-cache-sdk';

const client = new ChatbotCacheClient({
  baseUrl: 'http://localhost:8080',
  apiKey: 'your-api-key' // if authentication is enabled
});

const session = await client.sessions.create({
  userId: 'user-123'
});

const response = await client.messages.send(session.sessionId, {
  message: 'Hello, world!'
});
```

### Python
```python
from chatbot_cache import ChatbotCacheClient

client = ChatbotCacheClient(base_url='http://localhost:8080')

session = client.sessions.create(user_id='user-123')
response = client.messages.send(session.session_id, 'Hello, world!')
```

### Java
```java
ChatbotCacheClient client = new ChatbotCacheClient("http://localhost:8080");

Session session = client.sessions().create("user-123");
MessageResponse response = client.messages().send(session.getSessionId(), "Hello, world!");
```

## Testing

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Performance tests
curl -X POST http://localhost:8080/api/demo/load-test?testType=cache

# Load testing with custom parameters
curl -X POST http://localhost:8080/api/demo/stress-test \
  -H "Content-Type: application/json" \
  -d '{"maxUsers": 100, "durationSeconds": 60}'
```

### Test Data Setup
```bash
# Generate demo data
curl -X POST "http://localhost:8080/api/demo/generate-data?userCount=10&sessionsPerUser=3&messagesPerSession=20"

# Clear test data
curl -X POST http://localhost:8080/api/cache/clear/demo
```

## Changelog

### Version 1.0.0 (Current)
- Initial release with core chat functionality
- Redis caching implementation
- Context management and compression
- Performance monitoring and metrics
- Demo and testing features
- Comprehensive API documentation

### Planned Features
- Authentication and authorization
- Multi-language support
- Advanced analytics
- Export/import functionality
- Third-party integrations

## Support

For questions, issues, or feature requests:

- **Documentation**: Check this API documentation
- **Issues**: Create an issue in the GitHub repository
- **Email**: support@example.com
- **Discord**: Join our Discord server for community support

## License

This project is licensed under the MIT License. See the LICENSE file for details.