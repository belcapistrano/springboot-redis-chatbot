package com.example.chatbotcache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RedisPubSubService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;

    private final Map<String, CopyOnWriteArrayList<NotificationListener>> channelListeners = new ConcurrentHashMap<>();

    // Channel constants
    public static final String CHAT_MESSAGES_CHANNEL = "chat:messages";
    public static final String SESSION_EVENTS_CHANNEL = "session:events";
    public static final String USER_PRESENCE_CHANNEL = "user:presence";
    public static final String SYSTEM_ALERTS_CHANNEL = "system:alerts";
    public static final String CACHE_EVENTS_CHANNEL = "cache:events";

    @PostConstruct
    public void initialize() {
        setupChannelListeners();
    }

    private void setupChannelListeners() {
        // Setup message listener for each channel
        setupChannelListener(CHAT_MESSAGES_CHANNEL);
        setupChannelListener(SESSION_EVENTS_CHANNEL);
        setupChannelListener(USER_PRESENCE_CHANNEL);
        setupChannelListener(SYSTEM_ALERTS_CHANNEL);
        setupChannelListener(CACHE_EVENTS_CHANNEL);
    }

    private void setupChannelListener(String channel) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(new ChannelMessageListener(channel), "onMessage");
        messageListenerContainer.addMessageListener(listenerAdapter, new ChannelTopic(channel));
        channelListeners.put(channel, new CopyOnWriteArrayList<>());
    }

    public void publishChatMessage(String sessionId, String userId, String messageId, String content, String role) {
        Map<String, Object> message = new HashMap<>();
        message.put("sessionId", sessionId);
        message.put("userId", userId);
        message.put("messageId", messageId);
        message.put("content", content);
        message.put("role", role);
        message.put("timestamp", LocalDateTime.now().toString());

        publishToChannel(CHAT_MESSAGES_CHANNEL, message);
    }

    public void publishSessionEvent(String sessionId, String userId, String eventType, Map<String, Object> eventData) {
        Map<String, Object> event = new HashMap<>();
        event.put("sessionId", sessionId);
        event.put("userId", userId);
        event.put("eventType", eventType);
        event.put("timestamp", LocalDateTime.now().toString());
        if (eventData != null) {
            event.putAll(eventData);
        }

        publishToChannel(SESSION_EVENTS_CHANNEL, event);
    }

    public void publishUserPresence(String userId, String status, Map<String, Object> metadata) {
        Map<String, Object> presence = new HashMap<>();
        presence.put("userId", userId);
        presence.put("status", status); // online, offline, away, busy
        presence.put("timestamp", LocalDateTime.now().toString());
        if (metadata != null) {
            presence.putAll(metadata);
        }

        publishToChannel(USER_PRESENCE_CHANNEL, presence);
    }

    public void publishSystemAlert(String alertType, String message, String severity, Map<String, Object> details) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("alertType", alertType);
        alert.put("message", message);
        alert.put("severity", severity); // info, warning, error, critical
        alert.put("timestamp", LocalDateTime.now().toString());
        if (details != null) {
            alert.putAll(details);
        }

        publishToChannel(SYSTEM_ALERTS_CHANNEL, alert);
    }

    public void publishCacheEvent(String eventType, String key, Object value, Map<String, Object> metadata) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType); // set, get, delete, expire, hit, miss
        event.put("key", key);
        event.put("timestamp", LocalDateTime.now().toString());
        if (value != null) {
            event.put("value", value.toString());
        }
        if (metadata != null) {
            event.putAll(metadata);
        }

        publishToChannel(CACHE_EVENTS_CHANNEL, event);
    }

    private void publishToChannel(String channel, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, jsonMessage);
        } catch (Exception e) {
            System.err.println("Failed to publish message to channel " + channel + ": " + e.getMessage());
        }
    }

    public void subscribe(String channel, NotificationListener listener) {
        CopyOnWriteArrayList<NotificationListener> listeners = channelListeners.get(channel);
        if (listeners != null) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(String channel, NotificationListener listener) {
        CopyOnWriteArrayList<NotificationListener> listeners = channelListeners.get(channel);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public interface NotificationListener {
        void onNotification(String channel, Map<String, Object> message);
    }

    private class ChannelMessageListener {
        private final String channel;

        public ChannelMessageListener(String channel) {
            this.channel = channel;
        }

        public void onMessage(String message) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageMap = objectMapper.readValue(message, Map.class);

                CopyOnWriteArrayList<NotificationListener> listeners = channelListeners.get(channel);
                if (listeners != null) {
                    for (NotificationListener listener : listeners) {
                        try {
                            listener.onNotification(channel, messageMap);
                        } catch (Exception e) {
                            System.err.println("Error in notification listener for channel " + channel + ": " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to process message from channel " + channel + ": " + e.getMessage());
            }
        }
    }

    // Convenience methods for specific event types

    public void publishSessionCreated(String sessionId, String userId, String title) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", title);
        publishSessionEvent(sessionId, userId, "SESSION_CREATED", eventData);
    }

    public void publishSessionDeleted(String sessionId, String userId) {
        publishSessionEvent(sessionId, userId, "SESSION_DELETED", null);
    }

    public void publishSessionActivated(String sessionId, String userId) {
        publishSessionEvent(sessionId, userId, "SESSION_ACTIVATED", null);
    }

    public void publishSessionDeactivated(String sessionId, String userId) {
        publishSessionEvent(sessionId, userId, "SESSION_DEACTIVATED", null);
    }

    public void publishUserOnline(String userId, String sessionId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionId", sessionId);
        publishUserPresence(userId, "online", metadata);
    }

    public void publishUserOffline(String userId) {
        publishUserPresence(userId, "offline", null);
    }

    public void publishCacheHit(String key, String value) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("hit", true);
        publishCacheEvent("CACHE_HIT", key, value, metadata);
    }

    public void publishCacheMiss(String key) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("hit", false);
        publishCacheEvent("CACHE_MISS", key, null, metadata);
    }

    public void publishCacheSet(String key, Object value, long ttl) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ttl", ttl);
        publishCacheEvent("CACHE_SET", key, value, metadata);
    }

    public void publishCacheDelete(String key) {
        publishCacheEvent("CACHE_DELETE", key, null, null);
    }

    public void publishSystemStartup() {
        publishSystemAlert("SYSTEM_STARTUP", "Application started successfully", "info", null);
    }

    public void publishSystemShutdown() {
        publishSystemAlert("SYSTEM_SHUTDOWN", "Application is shutting down", "info", null);
    }

    public void publishRedisConnectionLost() {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        publishSystemAlert("REDIS_CONNECTION_LOST", "Lost connection to Redis server", "error", details);
    }

    public void publishRedisConnectionRestored() {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        publishSystemAlert("REDIS_CONNECTION_RESTORED", "Connection to Redis server restored", "info", details);
    }
}