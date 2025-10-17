package com.example.chatbotcache.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chatbot Context Cache API")
                        .version("1.0.0")
                        .description("""
                            # Spring Boot Redis Chatbot Cache API

                            A comprehensive demonstration of Redis capabilities through a chatbot context caching system.
                            This API showcases advanced Redis features including caching, streams, pub/sub, clustering,
                            and performance monitoring.

                            ## Features
                            - **Session Management**: Create and manage chat sessions with Redis backing
                            - **Message Caching**: Intelligent response caching with SHA-256 content hashing
                            - **Context Management**: Smart conversation compression and token counting
                            - **Real-time Features**: Redis Streams and Pub/Sub for live updates
                            - **Performance Monitoring**: Comprehensive metrics and statistics
                            - **Load Testing**: Built-in performance testing and benchmarking
                            - **Fallback Support**: Graceful degradation when Redis is unavailable

                            ## Quick Start
                            1. Start with `/api/sessions` to create a chat session
                            2. Use `/api/sessions/{sessionId}/chat` to send messages
                            3. Monitor performance with `/api/monitoring/metrics`
                            4. Run load tests with `/api/demo/load-test`

                            ## Redis Features Demonstrated
                            - **Caching**: Response caching with TTL management
                            - **Streams**: Real-time message streaming
                            - **Pub/Sub**: Event notifications and presence tracking
                            - **Lua Scripts**: Atomic operations for consistency
                            - **Clustering**: High availability and scalability
                            - **Health Monitoring**: Automatic failover detection
                            """)
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("API Support")
                                .url("https://github.com/your-repo")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://your-app.herokuapp.com")
                                .description("Production Server")))
                .tags(List.of(
                        new Tag()
                                .name("Chat Sessions")
                                .description("Manage chat sessions and conversation lifecycle"),
                        new Tag()
                                .name("Messages")
                                .description("Send and retrieve chat messages with caching"),
                        new Tag()
                                .name("User Preferences")
                                .description("Manage user preferences and personalization"),
                        new Tag()
                                .name("Context Management")
                                .description("Context compression and token management"),
                        new Tag()
                                .name("Cache Management")
                                .description("Cache operations and statistics"),
                        new Tag()
                                .name("Monitoring")
                                .description("System monitoring and performance metrics"),
                        new Tag()
                                .name("Demo")
                                .description("Demo features and performance testing"),
                        new Tag()
                                .name("Redis Advanced")
                                .description("Advanced Redis features: Streams, Pub/Sub, Scripts"),
                        new Tag()
                                .name("Health")
                                .description("System health checks and status monitoring")
                ));
    }
}