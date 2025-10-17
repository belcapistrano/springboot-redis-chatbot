package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisStreamService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CHAT_STREAM_PREFIX = "chat:stream:";
    private static final String SESSION_ACTIVITY_STREAM = "session:activity";
    private static final String USER_ACTIVITY_STREAM = "user:activity";
    private static final String SYSTEM_EVENTS_STREAM = "system:events";

    public void publishChatMessage(String sessionId, ChatMessage message) {
        try {
            String streamKey = CHAT_STREAM_PREFIX + sessionId;

            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", message.getMessageId());
            messageData.put("sessionId", message.getSessionId());
            messageData.put("role", message.getRole().name());
            messageData.put("content", message.getContent());
            messageData.put("timestamp", message.getTimestamp().toString());
            messageData.put("tokenCount", message.getTokenCount());

            // Use Redis list as a simple stream
            redisTemplate.opsForList().rightPush(streamKey, messageData);

            // Trim to keep only recent messages (last 100)
            redisTemplate.opsForList().trim(streamKey, -100, -1);

        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to publish chat message to stream: " + e.getMessage());
        }
    }

    public void publishSessionActivity(String sessionId, String userId, String activityType) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("sessionId", sessionId);
            activityData.put("userId", userId);
            activityData.put("activityType", activityType);
            activityData.put("timestamp", System.currentTimeMillis());

            redisTemplate.opsForList().rightPush(SESSION_ACTIVITY_STREAM, activityData);

            // Trim to keep only recent activities (last 1000)
            redisTemplate.opsForList().trim(SESSION_ACTIVITY_STREAM, -1000, -1);

        } catch (Exception e) {
            System.err.println("Failed to publish session activity: " + e.getMessage());
        }
    }

    public void publishUserActivity(String userId, String activityType, Map<String, Object> metadata) {
        try {
            Map<String, Object> activityData = new HashMap<>();
            activityData.put("userId", userId);
            activityData.put("activityType", activityType);
            activityData.put("timestamp", System.currentTimeMillis());
            if (metadata != null) {
                activityData.putAll(metadata);
            }

            redisTemplate.opsForList().rightPush(USER_ACTIVITY_STREAM, activityData);

            // Trim to keep only recent activities (last 500)
            redisTemplate.opsForList().trim(USER_ACTIVITY_STREAM, -500, -1);

        } catch (Exception e) {
            System.err.println("Failed to publish user activity: " + e.getMessage());
        }
    }

    public void publishSystemEvent(String eventType, String description, Map<String, Object> data) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", eventType);
            eventData.put("description", description);
            eventData.put("timestamp", System.currentTimeMillis());
            if (data != null) {
                eventData.putAll(data);
            }

            redisTemplate.opsForList().rightPush(SYSTEM_EVENTS_STREAM, eventData);

            // Trim to keep only recent events (last 200)
            redisTemplate.opsForList().trim(SYSTEM_EVENTS_STREAM, -200, -1);

        } catch (Exception e) {
            System.err.println("Failed to publish system event: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> readChatMessages(String sessionId, String fromId, int count) {
        try {
            String streamKey = CHAT_STREAM_PREFIX + sessionId;

            List<Object> messages = redisTemplate.opsForList().range(streamKey, -count, -1);
            if (messages == null) {
                return new ArrayList<>();
            }

            return messages.stream()
                    .map(obj -> {
                        if (obj instanceof Map) {
                            Map<String, Object> data = new HashMap<>((Map<String, Object>) obj);
                            data.put("streamId", UUID.randomUUID().toString());
                            return data;
                        }
                        return new HashMap<String, Object>();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Failed to read chat messages from stream: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> readSessionActivity(String fromId, int count) {
        return readFromStream(SESSION_ACTIVITY_STREAM, fromId, count);
    }

    public List<Map<String, Object>> readUserActivity(String fromId, int count) {
        return readFromStream(USER_ACTIVITY_STREAM, fromId, count);
    }

    public List<Map<String, Object>> readSystemEvents(String fromId, int count) {
        return readFromStream(SYSTEM_EVENTS_STREAM, fromId, count);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readFromStream(String streamKey, String fromId, int count) {
        try {
            List<Object> events = redisTemplate.opsForList().range(streamKey, -count, -1);
            if (events == null) {
                return new ArrayList<>();
            }

            return events.stream()
                    .map(obj -> {
                        if (obj instanceof Map) {
                            Map<String, Object> data = new HashMap<>((Map<String, Object>) obj);
                            data.put("streamId", UUID.randomUUID().toString());
                            return data;
                        }
                        return new HashMap<String, Object>();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Failed to read from stream " + streamKey + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Long> getStreamInfo(String streamKey) {
        try {
            Map<String, Long> streamInfo = new HashMap<>();
            Long length = redisTemplate.opsForList().size(streamKey);
            streamInfo.put("length", length != null ? length : 0L);
            streamInfo.put("radixTreeKeys", 0L);
            streamInfo.put("radixTreeNodes", 0L);
            streamInfo.put("groups", 0L);
            streamInfo.put("lastGeneratedId", System.currentTimeMillis());
            return streamInfo;
        } catch (Exception e) {
            System.err.println("Failed to get stream info for " + streamKey + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    public List<String> getAllChatStreams() {
        try {
            Set<String> keys = redisTemplate.keys(CHAT_STREAM_PREFIX + "*");
            return keys != null ? new ArrayList<>(keys) : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Failed to get chat streams: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void cleanupOldStreams(Duration maxAge) {
        try {
            // Clean up chat streams by deleting old ones
            List<String> chatStreams = getAllChatStreams();
            for (String streamKey : chatStreams) {
                try {
                    // Simple cleanup: trim to last 50 items
                    redisTemplate.opsForList().trim(streamKey, -50, -1);
                } catch (Exception e) {
                    System.err.println("Failed to cleanup stream " + streamKey + ": " + e.getMessage());
                }
            }

            // Clean up activity streams
            redisTemplate.opsForList().trim(SESSION_ACTIVITY_STREAM, -500, -1);
            redisTemplate.opsForList().trim(USER_ACTIVITY_STREAM, -250, -1);
            redisTemplate.opsForList().trim(SYSTEM_EVENTS_STREAM, -100, -1);

        } catch (Exception e) {
            System.err.println("Failed to cleanup old streams: " + e.getMessage());
        }
    }

    public void deleteStream(String streamKey) {
        try {
            redisTemplate.delete(streamKey);
        } catch (Exception e) {
            System.err.println("Failed to delete stream " + streamKey + ": " + e.getMessage());
        }
    }

    public static class StreamActivity {
        private final String streamId;
        private final Map<String, Object> data;
        private final long timestamp;

        public StreamActivity(String streamId, Map<String, Object> data, long timestamp) {
            this.streamId = streamId;
            this.data = data;
            this.timestamp = timestamp;
        }

        public String getStreamId() { return streamId; }
        public Map<String, Object> getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
}