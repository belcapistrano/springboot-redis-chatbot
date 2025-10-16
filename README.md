# Spring Boot Redis Chatbot

A modern Spring Boot application demonstrating Redis-based response caching, session management, and real-time connectivity monitoring with automatic fallback capabilities.

## 🚀 Features

- **Redis Integration**: High-performance caching with Redis
- **Automatic Fallback**: Seamless transition to in-memory storage when Redis is unavailable
- **Real-time Status Monitoring**: Live Redis connectivity status with immediate updates
- **Response Caching**: Intelligent caching of chatbot responses for improved performance
- **Session Management**: Persistent chat sessions with user context
- **User Preferences**: Configurable user settings stored in Redis
- **RESTful API**: Comprehensive REST endpoints for all functionality
- **Clean Web Interface**: Modern, responsive chatbot UI
- **Cache Analytics**: Detailed statistics and monitoring capabilities

## 🏗️ Architecture

### Core Components

- **Controllers**: REST API endpoints for chat, sessions, cache management
- **Services**: Business logic with Redis and fallback implementations
- **Models**: Data entities for chat messages, sessions, and user preferences
- **Configuration**: Redis setup with connection pooling and serialization
- **Web Interface**: Single-page application with real-time status updates

### Technology Stack

- **Backend**: Spring Boot 3.2.0, Spring Data Redis
- **Cache**: Redis with Jedis client
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Build Tool**: Maven
- **Java Version**: 17+

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server (optional - app works without it)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/belcapistrano/springboot-redis-chatbot.git
cd springboot-redis-chatbot
```

### 2. Install Redis (Optional)

**macOS (Homebrew):**
```bash
brew install redis
brew services start redis
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

**Windows:**
Download and install from [Redis Windows releases](https://github.com/microsoftarchive/redis/releases)

### 3. Build and Run

```bash
# Build the application
mvn clean install

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the JAR file
java -jar target/chatbot-context-cache-1.0.0.jar
```

## 🌐 Usage

### Web Interface

1. Open your browser and navigate to: `http://localhost:8080`
2. Click "New Session" to start a chat session
3. Type messages and interact with the chatbot
4. Monitor Redis status in the top-right corner:
   - 🟢 **Redis Connected**: Full functionality with caching
   - 🟡 **Fallback Mode**: Redis unavailable, using in-memory storage

### API Endpoints

#### Chat Operations
```bash
# Create a new chat session
POST /api/sessions
Content-Type: application/json
{
  "userId": "user123",
  "title": "My Chat Session"
}

# Send a message
POST /api/sessions/{sessionId}/chat
Content-Type: application/json
{
  "message": "Hello, how are you?",
  "model": "mock-llm",
  "temperature": 0.7
}

# Get session messages
GET /api/sessions/{sessionId}/messages?page=0&size=20
```

#### Cache Management
```bash
# Get cache statistics
GET /api/cache/stats

# Check system status
GET /api/cache/status

# Clear response cache
POST /api/cache/clear/responses
```

#### Health Monitoring
```bash
# Redis health check
GET /api/health/redis

# Get Redis connection info
GET /api/health/redis/info
```

## ⚙️ Configuration

### Application Properties

The application uses `application.yml` for configuration:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 10
        max-idle: 5
        min-idle: 1

server:
  port: 8080

logging:
  level:
    com.example.chatbotcache: DEBUG
```

### Environment Profiles

- **Development**: `application.yml` (default)
- **Production**: Create `application-prod.yml` for production settings

### Redis Configuration

The application automatically configures:
- Connection pooling with Jedis
- JSON serialization for complex objects
- Automatic failover to in-memory storage
- Connection timeout and retry logic

## 📊 Monitoring & Analytics

### Cache Statistics

The application provides detailed metrics:

- **Cache Hits/Misses**: Track cache efficiency
- **Hit Ratio**: Percentage of successful cache retrievals
- **Active Sessions**: Current concurrent users
- **Response Cache Size**: Number of cached responses
- **Most Active Users/Sessions**: Usage patterns

### Redis Status Monitoring

Real-time monitoring includes:
- Connection status with immediate detection
- Automatic reconnection handling
- Performance metrics
- Error tracking and logging

## 🔧 Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/chatbotcache/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── model/               # Data models
│   │   ├── repository/          # Data access
│   │   ├── config/              # Configuration
│   │   └── ChatbotContextCacheApplication.java
│   └── resources/
│       ├── static/index.html    # Web interface
│       └── application.yml      # Configuration
└── test/                        # Unit tests
```

### Key Classes

- **CacheController**: Cache management and status endpoints
- **ChatController**: Chat session and messaging
- **CacheService**: Redis operations with fallback logic
- **RedisConfig**: Redis connection and serialization setup
- **FallbackService**: In-memory alternatives when Redis is down

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RepositoryIntegrationTest

# Run with coverage
mvn test jacoco:report
```

## 🚀 Deployment

### Docker (Optional)

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/chatbot-context-cache-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t springboot-redis-chatbot .
docker run -p 8080:8080 springboot-redis-chatbot
```

### Production Considerations

1. **Redis Configuration**: Use Redis Cluster for high availability
2. **Security**: Add authentication and HTTPS
3. **Monitoring**: Integrate with monitoring tools (Prometheus, Grafana)
4. **Scaling**: Configure session affinity for multi-instance deployments
5. **Backup**: Implement Redis persistence and backup strategies

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Redis team for the powerful caching solution
- OpenAI for inspiration on chatbot interfaces

---

**Repository**: https://github.com/belcapistrano/springboot-redis-chatbot

**Live Demo**: `http://localhost:8080` (when running locally)

Built with ❤️ using Spring Boot and Redis