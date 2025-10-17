package com.example.chatbotcache.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class RedisClusterConfig {

    @Value("${spring.data.redis.cluster.nodes:}")
    private String clusterNodes;

    @Value("${spring.data.redis.cluster.max-redirects:3}")
    private int maxRedirects;

    @Value("${spring.data.redis.sentinel.nodes:}")
    private String sentinelNodes;

    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String sentinelMaster;

    @Value("${spring.data.redis.timeout:5000}")
    private Duration commandTimeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.data.redis.lettuce.shutdown-timeout:100}")
    private Duration shutdownTimeout;

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.cluster.enabled", havingValue = "true")
    @Primary
    public RedisConnectionFactory redisClusterConnectionFactory() {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            Set<String> nodes = new HashSet<>(Arrays.asList(clusterNodes.split(",")));
            clusterConfiguration.setClusterNodes(
                nodes.stream()
                    .map(node -> {
                        String[] parts = node.trim().split(":");
                        return new org.springframework.data.redis.connection.RedisNode(
                            parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 6379);
                    })
                    .collect(java.util.stream.Collectors.toSet())
            );
        }

        clusterConfiguration.setMaxRedirects(maxRedirects);

        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .shutdownTimeout(shutdownTimeout)
                .readFrom(ReadFrom.REPLICA_PREFERRED) // Read from replica when possible
                .build();

        return new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.sentinel.enabled", havingValue = "true")
    public RedisConnectionFactory redisSentinelConnectionFactory() {
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        sentinelConfiguration.setMaster(sentinelMaster);

        if (sentinelNodes != null && !sentinelNodes.isEmpty()) {
            Set<String> nodes = new HashSet<>(Arrays.asList(sentinelNodes.split(",")));
            for (String node : nodes) {
                String[] parts = node.trim().split(":");
                sentinelConfiguration.sentinel(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 26379);
            }
        }

        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .shutdownTimeout(shutdownTimeout)
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        return new LettuceConnectionFactory(sentinelConfiguration, clientConfiguration);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.cluster.enabled", havingValue = "true")
    public RedisTemplate<String, Object> clusterRedisTemplate(RedisConnectionFactory redisClusterConnectionFactory) {
        return createRedisTemplate(redisClusterConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.sentinel.enabled", havingValue = "true")
    public RedisTemplate<String, Object> sentinelRedisTemplate(RedisConnectionFactory redisSentinelConnectionFactory) {
        return createRedisTemplate(redisSentinelConnectionFactory);
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(Object.class);

        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.setVisibility(com.fasterxml.jackson.annotation.PropertyAccessor.ALL,
            com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
            com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.cluster.enabled", havingValue = "true")
    public RedisMessageListenerContainer clusterMessageListenerContainer(RedisConnectionFactory redisClusterConnectionFactory) {
        return createMessageListenerContainer(redisClusterConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.sentinel.enabled", havingValue = "true")
    public RedisMessageListenerContainer sentinelMessageListenerContainer(RedisConnectionFactory redisSentinelConnectionFactory) {
        return createMessageListenerContainer(redisSentinelConnectionFactory);
    }

    private RedisMessageListenerContainer createMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Configure container for high availability
        container.setTaskExecutor(new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor());
        container.setErrorHandler(throwable -> {
            System.err.println("Redis message listener error: " + throwable.getMessage());
            // Log but don't stop the container
        });

        // Recovery settings
        container.setRecoveryInterval(Duration.ofSeconds(5).toMillis());

        return container;
    }
}