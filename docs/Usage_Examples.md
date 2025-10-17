# Usage Examples and Curl Commands

This document provides practical examples of how to use the Chatbot Context Cache API with curl commands and code snippets.

## Getting Started

### 1. Basic Chat Flow

#### Step 1: Create a Session
```bash
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "demo-user-123",
    "preferences": {
      "language": "en",
      "theme": "light"
    }
  }'
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "userId": "demo-user-123",
  "createdAt": "2023-12-01T10:30:00Z",
  "lastActivity": "2023-12-01T10:30:00Z",
  "messageCount": 0,
  "status": "ACTIVE"
}
```

#### Step 2: Send a Message
```bash
SESSION_ID="sess_abc123def456"

curl -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello! Can you explain Redis caching?",
    "metadata": {
      "source": "web",
      "timestamp": "2023-12-01T10:31:00Z"
    }
  }'
```

**Response:**
```json
{
  "userMessage": {
    "messageId": "msg_user_001",
    "content": "Hello! Can you explain Redis caching?",
    "role": "USER",
    "timestamp": "2023-12-01T10:31:00Z",
    "tokenCount": 8
  },
  "assistantMessage": {
    "messageId": "msg_asst_001",
    "content": "Hello! Redis caching is an in-memory data structure store that significantly improves application performance by storing frequently accessed data in RAM. Key benefits include: 1) Sub-millisecond response times, 2) Reduced database load, 3) Better scalability, 4) Support for complex data types like lists, sets, and streams.",
    "role": "ASSISTANT",
    "timestamp": "2023-12-01T10:31:02Z",
    "tokenCount": 52
  },
  "cacheHit": false,
  "responseTime": 145,
  "sessionUpdated": true
}
```

#### Step 3: Continue the Conversation
```bash
curl -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How does it compare to database queries?"
  }'
```

#### Step 4: Get Message History
```bash
curl -X GET "http://localhost:8080/api/sessions/${SESSION_ID}/messages?page=0&size=10"
```

### 2. User Preferences Management

#### Set User Preferences
```bash
USER_ID="demo-user-123"

curl -X PUT "http://localhost:8080/api/users/${USER_ID}/preferences" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "John Developer",
    "language": "en",
    "theme": "dark",
    "responseLength": "detailed",
    "responseTone": "technical",
    "enableNotifications": true,
    "autoSaveHistory": true,
    "maxHistorySize": 100,
    "contextWindowSize": 4000,
    "customSettings": {
      "showTimestamps": true,
      "soundEnabled": false,
      "preferredTopics": ["technology", "programming"]
    }
  }'
```

#### Get User Preferences
```bash
curl -X GET "http://localhost:8080/api/users/${USER_ID}/preferences"
```

### 3. Context Management

#### Check Session Context
```bash
curl -X GET "http://localhost:8080/api/sessions/${SESSION_ID}/context"
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "tokenCount": 156,
  "messageCount": 4,
  "contextWindow": 4000,
  "compressionApplied": false,
  "summary": null,
  "recentMessages": 4,
  "oldestMessageAge": "2023-12-01T10:30:00Z"
}
```

#### Force Context Compression
```bash
curl -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/compress"
```

### 4. Cache Operations

#### Get Cache Statistics
```bash
curl -X GET "http://localhost:8080/api/cache/stats"
```

**Response:**
```json
{
  "totalKeys": 245,
  "hitCount": 1834,
  "missCount": 456,
  "hitRate": 80.12,
  "memoryUsage": "12.5 MB",
  "responsesCached": 178,
  "sessionsCached": 67,
  "avgResponseTime": 23.5,
  "cacheSize": 245,
  "topCachedResponses": [
    {
      "content": "Hello! How can I help you today?",
      "hits": 34
    },
    {
      "content": "Redis is an in-memory data structure store...",
      "hits": 28
    }
  ]
}
```

#### Clear Specific Cache
```bash
# Clear all response cache
curl -X POST "http://localhost:8080/api/cache/clear/responses"

# Clear session cache
curl -X POST "http://localhost:8080/api/cache/clear/sessions"

# Clear cache for specific user
curl -X POST "http://localhost:8080/api/cache/clear/user:demo-user-123"
```

### 5. Monitoring and Metrics

#### Get All Metrics
```bash
curl -X GET "http://localhost:8080/api/monitoring/metrics"
```

#### Get Specific Metric Types
```bash
# Application metrics
curl -X GET "http://localhost:8080/api/monitoring/metrics/application"

# Cache metrics
curl -X GET "http://localhost:8080/api/monitoring/metrics/cache"

# Performance metrics
curl -X GET "http://localhost:8080/api/monitoring/metrics/performance"

# System metrics
curl -X GET "http://localhost:8080/api/monitoring/metrics/system"
```

#### Record Custom Metrics
```bash
# Record operation
curl -X POST "http://localhost:8080/api/monitoring/record/operation?operationType=custom-operation"

# Record error
curl -X POST "http://localhost:8080/api/monitoring/record/error?errorType=custom-error"

# Record latency
curl -X POST "http://localhost:8080/api/monitoring/record/latency?operation=custom-operation&latencyMs=150"

# Record cache hit
curl -X POST "http://localhost:8080/api/monitoring/record/cache-hit?cacheType=custom-cache"
```

### 6. Performance Testing

#### Generate Demo Data
```bash
curl -X POST "http://localhost:8080/api/demo/generate-data?userCount=10&sessionsPerUser=3&messagesPerSession=15"
```

#### Run Cache Performance Test
```bash
curl -X POST "http://localhost:8080/api/demo/load-test?testType=cache" \
  -H "Content-Type: application/json" \
  -d '{
    "iterations": 100
  }'
```

#### Run Concurrent User Test
```bash
curl -X POST "http://localhost:8080/api/demo/load-test?testType=concurrent" \
  -H "Content-Type: application/json" \
  -d '{
    "users": 25,
    "messagesPerUser": 10
  }'
```

#### Run Stress Test
```bash
curl -X POST "http://localhost:8080/api/demo/stress-test?maxUsers=50&durationSeconds=30"
```

#### Performance Comparison
```bash
curl -X GET "http://localhost:8080/api/demo/performance-comparison?iterations=200"
```

### 7. Redis Advanced Features

#### Publish Event via Pub/Sub
```bash
curl -X POST "http://localhost:8080/api/redis/pubsub/publish" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "chat.events",
    "message": {
      "type": "user_joined",
      "userId": "demo-user-123",
      "sessionId": "sess_abc123def456",
      "timestamp": "2023-12-01T10:35:00Z"
    }
  }'
```

#### Add Entry to Redis Stream
```bash
curl -X POST "http://localhost:8080/api/redis/streams/add" \
  -H "Content-Type: application/json" \
  -d '{
    "stream": "session:sess_abc123def456:activity",
    "data": {
      "event": "message_sent",
      "messageId": "msg_asst_001",
      "timestamp": "2023-12-01T10:31:02Z",
      "metadata": {
        "responseTime": 145,
        "cacheHit": false
      }
    }
  }'
```

#### Execute Lua Script
```bash
curl -X POST "http://localhost:8080/api/redis/scripts/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "scriptName": "updateSessionWithMessage",
    "keys": ["session:sess_abc123def456", "messages:sess_abc123def456"],
    "args": ["msg_user_002", "Follow-up question", "USER", "1701434567"]
  }'
```

### 8. Health Checks and Status

#### System Health Check
```bash
curl -X GET "http://localhost:8080/actuator/health"
```

#### Detailed Health Information
```bash
curl -X GET "http://localhost:8080/actuator/health/redis"
```

#### Application Info
```bash
curl -X GET "http://localhost:8080/actuator/info"
```

#### Prometheus Metrics
```bash
curl -X GET "http://localhost:8080/actuator/prometheus"
```

## Complete Workflow Examples

### Example 1: Customer Support Simulation

```bash
#!/bin/bash

# Customer Support Workflow
echo "=== Customer Support Simulation ==="

# 1. Create customer session
RESPONSE=$(curl -s -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "customer-001",
    "preferences": {
      "language": "en",
      "responseTone": "helpful"
    }
  }')

SESSION_ID=$(echo $RESPONSE | jq -r '.sessionId')
echo "Created session: $SESSION_ID"

# 2. Customer asks for help
curl -s -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I am having trouble with my account login"
  }' | jq '.assistantMessage.content'

# 3. Follow-up question
curl -s -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I tried resetting my password but did not receive an email"
  }' | jq '.assistantMessage.content'

# 4. Check session context
curl -s -X GET "http://localhost:8080/api/sessions/${SESSION_ID}/context" | jq '.'

echo "=== Support session completed ==="
```

### Example 2: Performance Benchmarking

```bash
#!/bin/bash

# Performance Benchmarking Workflow
echo "=== Performance Benchmarking ==="

# 1. Generate baseline data
echo "Generating demo data..."
curl -s -X POST "http://localhost:8080/api/demo/generate-data?userCount=20&sessionsPerUser=2&messagesPerSession=10"

# 2. Run cache performance test
echo "Testing cache performance..."
CACHE_RESULT=$(curl -s -X POST "http://localhost:8080/api/demo/load-test?testType=cache" \
  -H "Content-Type: application/json" \
  -d '{"iterations": 200}')

echo "Cache hit rate: $(echo $CACHE_RESULT | jq '.cacheHitRate')%"
echo "Speed improvement: $(echo $CACHE_RESULT | jq -r '.speedImprovement')"

# 3. Test concurrent users
echo "Testing concurrent users..."
CONCURRENT_RESULT=$(curl -s -X POST "http://localhost:8080/api/demo/load-test?testType=concurrent" \
  -H "Content-Type: application/json" \
  -d '{"users": 50, "messagesPerUser": 5}')

echo "Success rate: $(echo $CONCURRENT_RESULT | jq '.successCount')/$(echo $CONCURRENT_RESULT | jq '.userCount')"
echo "Average response time: $(echo $CONCURRENT_RESULT | jq '.avgResponseTime')ms"

# 4. Check system metrics
echo "Current system metrics:"
curl -s -X GET "http://localhost:8080/api/monitoring/metrics/system" | jq '{
  heapMemoryUsed: .heapMemoryUsed,
  threadCount: .threadCount,
  availableProcessors: .availableProcessors
}'

echo "=== Benchmarking completed ==="
```

### Example 3: Multi-Session Management

```bash
#!/bin/bash

# Multi-Session Management
echo "=== Multi-Session Demo ==="

USER_ID="multi-user-001"

# Set user preferences
curl -s -X PUT "http://localhost:8080/api/users/${USER_ID}/preferences" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Multi Session User",
    "language": "en",
    "maxHistorySize": 50
  }'

# Create multiple sessions for the same user
for i in {1..3}; do
  echo "Creating session $i..."
  RESPONSE=$(curl -s -X POST http://localhost:8080/api/sessions \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"${USER_ID}\",
      \"preferences\": {
        \"sessionType\": \"session-${i}\"
      }
    }")

  SESSION_ID=$(echo $RESPONSE | jq -r '.sessionId')
  echo "Session $i ID: $SESSION_ID"

  # Send different types of messages to each session
  case $i in
    1)
      curl -s -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
        -H "Content-Type: application/json" \
        -d '{"message": "Tell me about Redis data structures"}' > /dev/null
      ;;
    2)
      curl -s -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
        -H "Content-Type: application/json" \
        -d '{"message": "How to optimize database performance?"}' > /dev/null
      ;;
    3)
      curl -s -X POST "http://localhost:8080/api/sessions/${SESSION_ID}/chat" \
        -H "Content-Type: application/json" \
        -d '{"message": "Explain caching strategies"}' > /dev/null
      ;;
  esac
done

# Get user sessions
echo "Getting all sessions for user..."
curl -s -X GET "http://localhost:8080/api/users/${USER_ID}/sessions" | jq '.'

echo "=== Multi-session demo completed ==="
```

## Language-Specific Examples

### JavaScript/Node.js Example

```javascript
const axios = require('axios');

class ChatbotCacheClient {
  constructor(baseUrl = 'http://localhost:8080') {
    this.baseUrl = baseUrl;
    this.axios = axios.create({
      baseURL: baseUrl,
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  async createSession(userId, preferences = {}) {
    const response = await this.axios.post('/api/sessions', {
      userId,
      preferences
    });
    return response.data;
  }

  async sendMessage(sessionId, message, metadata = {}) {
    const response = await this.axios.post(`/api/sessions/${sessionId}/chat`, {
      message,
      metadata
    });
    return response.data;
  }

  async getMessages(sessionId, page = 0, size = 20) {
    const response = await this.axios.get(`/api/sessions/${sessionId}/messages`, {
      params: { page, size }
    });
    return response.data;
  }

  async getCacheStats() {
    const response = await this.axios.get('/api/cache/stats');
    return response.data;
  }

  async runPerformanceTest(testType, parameters = {}) {
    const response = await this.axios.post(`/api/demo/load-test?testType=${testType}`, parameters);
    return response.data;
  }
}

// Usage example
async function demonstrateAPI() {
  const client = new ChatbotCacheClient();

  // Create session
  const session = await client.createSession('demo-user-js', {
    language: 'en',
    theme: 'dark'
  });

  console.log('Session created:', session.sessionId);

  // Send message
  const response = await client.sendMessage(session.sessionId, 'Hello from JavaScript!');
  console.log('Assistant response:', response.assistantMessage.content);

  // Get cache stats
  const cacheStats = await client.getCacheStats();
  console.log('Cache hit rate:', cacheStats.hitRate + '%');

  // Run performance test
  const perfTest = await client.runPerformanceTest('cache', { iterations: 50 });
  console.log('Performance test completed:', perfTest.speedImprovement);
}

demonstrateAPI().catch(console.error);
```

### Python Example

```python
import requests
import json
from typing import Dict, Any, Optional

class ChatbotCacheClient:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def create_session(self, user_id: str, preferences: Optional[Dict] = None) -> Dict[str, Any]:
        """Create a new chat session"""
        data = {"userId": user_id}
        if preferences:
            data["preferences"] = preferences

        response = self.session.post(f"{self.base_url}/api/sessions", json=data)
        response.raise_for_status()
        return response.json()

    def send_message(self, session_id: str, message: str, metadata: Optional[Dict] = None) -> Dict[str, Any]:
        """Send a message to a session"""
        data = {"message": message}
        if metadata:
            data["metadata"] = metadata

        response = self.session.post(f"{self.base_url}/api/sessions/{session_id}/chat", json=data)
        response.raise_for_status()
        return response.json()

    def get_messages(self, session_id: str, page: int = 0, size: int = 20) -> Dict[str, Any]:
        """Get message history for a session"""
        params = {"page": page, "size": size}
        response = self.session.get(f"{self.base_url}/api/sessions/{session_id}/messages", params=params)
        response.raise_for_status()
        return response.json()

    def get_cache_stats(self) -> Dict[str, Any]:
        """Get cache statistics"""
        response = self.session.get(f"{self.base_url}/api/cache/stats")
        response.raise_for_status()
        return response.json()

    def run_performance_test(self, test_type: str, parameters: Optional[Dict] = None) -> Dict[str, Any]:
        """Run a performance test"""
        params = {"testType": test_type}
        data = parameters or {}

        response = self.session.post(f"{self.base_url}/api/demo/load-test", params=params, json=data)
        response.raise_for_status()
        return response.json()

# Usage example
def demonstrate_api():
    client = ChatbotCacheClient()

    # Create session
    session = client.create_session("demo-user-python", {
        "language": "en",
        "responseTone": "technical"
    })
    print(f"Session created: {session['sessionId']}")

    # Send message
    response = client.send_message(session["sessionId"], "Hello from Python!")
    print(f"Assistant response: {response['assistantMessage']['content']}")

    # Get cache stats
    cache_stats = client.get_cache_stats()
    print(f"Cache hit rate: {cache_stats['hitRate']}%")

    # Run performance test
    perf_test = client.run_performance_test("latency", {"iterations": 30})
    print(f"Average latency: {perf_test['avgLatency']}ms")

if __name__ == "__main__":
    demonstrate_api()
```

### Java Example

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class ChatbotCacheClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ChatbotCacheClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .timeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode createSession(String userId, JsonNode preferences) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "userId", userId,
            "preferences", preferences
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/sessions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    public JsonNode sendMessage(String sessionId, String message) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("message", message));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/sessions/" + sessionId + "/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    public JsonNode getCacheStats() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/cache/stats"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    // Usage example
    public static void main(String[] args) throws Exception {
        ChatbotCacheClient client = new ChatbotCacheClient("http://localhost:8080");

        // Create session
        JsonNode preferences = new ObjectMapper().createObjectNode()
                .put("language", "en")
                .put("theme", "light");

        JsonNode session = client.createSession("demo-user-java", preferences);
        String sessionId = session.get("sessionId").asText();
        System.out.println("Session created: " + sessionId);

        // Send message
        JsonNode response = client.sendMessage(sessionId, "Hello from Java!");
        String assistantResponse = response.get("assistantMessage").get("content").asText();
        System.out.println("Assistant response: " + assistantResponse);

        // Get cache stats
        JsonNode cacheStats = client.getCacheStats();
        double hitRate = cacheStats.get("hitRate").asDouble();
        System.out.println("Cache hit rate: " + hitRate + "%");
    }
}
```

## Testing Scripts

### Comprehensive Test Suite

```bash
#!/bin/bash

# comprehensive_test.sh - Complete API testing script

BASE_URL="http://localhost:8080"
USER_ID="test-user-$(date +%s)"

echo "=== Comprehensive API Test Suite ==="
echo "Base URL: $BASE_URL"
echo "User ID: $USER_ID"

# Test 1: User Preferences
echo -e "\n1. Testing User Preferences..."
curl -s -X PUT "$BASE_URL/api/users/$USER_ID/preferences" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Test User",
    "language": "en",
    "theme": "dark"
  }' | jq '.displayName'

# Test 2: Session Creation
echo -e "\n2. Testing Session Creation..."
SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/sessions" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\"
  }")

SESSION_ID=$(echo $SESSION_RESPONSE | jq -r '.sessionId')
echo "Created session: $SESSION_ID"

# Test 3: Message Exchange
echo -e "\n3. Testing Message Exchange..."
MESSAGE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/sessions/$SESSION_ID/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What are the benefits of Redis caching?"
  }')

echo "Cache hit: $(echo $MESSAGE_RESPONSE | jq '.cacheHit')"
echo "Response time: $(echo $MESSAGE_RESPONSE | jq '.responseTime')ms"

# Test 4: Context Management
echo -e "\n4. Testing Context Management..."
curl -s -X GET "$BASE_URL/api/sessions/$SESSION_ID/context" | jq '{
  tokenCount: .tokenCount,
  messageCount: .messageCount,
  compressionApplied: .compressionApplied
}'

# Test 5: Cache Operations
echo -e "\n5. Testing Cache Operations..."
curl -s -X GET "$BASE_URL/api/cache/stats" | jq '{
  hitRate: .hitRate,
  totalKeys: .totalKeys,
  memoryUsage: .memoryUsage
}'

# Test 6: Performance Testing
echo -e "\n6. Testing Performance Features..."
PERF_RESULT=$(curl -s -X POST "$BASE_URL/api/demo/load-test?testType=cache" \
  -H "Content-Type: application/json" \
  -d '{"iterations": 20}')

echo "Performance test completed: $(echo $PERF_RESULT | jq '.status')"
echo "Speed improvement: $(echo $PERF_RESULT | jq -r '.speedImprovement')"

# Test 7: Monitoring
echo -e "\n7. Testing Monitoring..."
curl -s -X GET "$BASE_URL/api/monitoring/metrics/application" | jq '{
  totalOperations: .totalOperations,
  errorRate: .errorRate,
  uptime: .uptime
}'

# Test 8: Health Check
echo -e "\n8. Testing Health Check..."
curl -s -X GET "$BASE_URL/actuator/health" | jq '.status'

# Test 9: Cleanup
echo -e "\n9. Cleanup..."
curl -s -X DELETE "$BASE_URL/api/sessions/$SESSION_ID" | jq '.message'

echo -e "\n=== Test Suite Completed ==="
```

This comprehensive documentation provides practical examples for every aspect of the API, from basic usage to advanced features and performance testing.