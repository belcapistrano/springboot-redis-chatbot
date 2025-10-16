package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final int MAX_MESSAGES_PER_SESSION = 50;
    private static final String MESSAGE_LIST_KEY_PREFIX = "messages:";

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * Add a message to a session using Redis Lists for ordering
     */
    public ChatMessage addMessage(String sessionId, MessageRole role, String content, Integer tokenCount) {
        validateSessionId(sessionId);
        validateContent(content);

        // Create message
        String messageId = generateMessageId();
        ChatMessage message = new ChatMessage(messageId, sessionId, role, content, tokenCount);

        // Sanitize content
        message.setContent(sanitizeContent(content));

        // Save message to repository
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Add to Redis list for ordering and fast retrieval
        String listKey = getMessageListKey(sessionId);
        redisTemplate.opsForList().rightPush(listKey, messageId);

        // Trim list to max size (keep most recent messages)
        redisTemplate.opsForList().trim(listKey, -MAX_MESSAGES_PER_SESSION, -1);

        // Update session
        chatSessionService.incrementMessageCount(sessionId);
        if (tokenCount != null && tokenCount > 0) {
            chatSessionService.addTokensToSession(sessionId, tokenCount);
        }

        return savedMessage;
    }

    /**
     * Add a message with automatic token counting
     */
    public ChatMessage addMessage(String sessionId, MessageRole role, String content) {
        int estimatedTokens = estimateTokenCount(content);
        return addMessage(sessionId, role, content, estimatedTokens);
    }

    /**
     * Get messages for a session with pagination
     */
    public List<ChatMessage> getMessages(String sessionId, int page, int size) {
        validateSessionId(sessionId);
        validatePagination(page, size);

        // Get message IDs from Redis list in reverse order (newest first)
        String listKey = getMessageListKey(sessionId);
        long start = (long) page * size;
        long end = start + size - 1;

        List<Object> messageIds = redisTemplate.opsForList().range(listKey, -end - 1, -start - 1);

        if (messageIds == null || messageIds.isEmpty()) {
            // Fallback to repository if Redis list is empty
            return chatMessageRepository.findBySessionIdOrderByTimestampDesc(sessionId)
                    .stream()
                    .skip(start)
                    .limit(size)
                    .collect(Collectors.toList());
        }

        // Fetch messages from repository by IDs
        return messageIds.stream()
                .map(id -> chatMessageRepository.findById(id.toString()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get all messages for a session ordered by timestamp
     */
    public List<ChatMessage> getAllMessages(String sessionId) {
        validateSessionId(sessionId);
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    /**
     * Get recent messages (default last 10)
     */
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        validateSessionId(sessionId);
        if (limit <= 0 || limit > MAX_MESSAGES_PER_SESSION) {
            limit = 10;
        }

        String listKey = getMessageListKey(sessionId);
        List<Object> messageIds = redisTemplate.opsForList().range(listKey, -limit, -1);

        if (messageIds == null || messageIds.isEmpty()) {
            return chatMessageRepository.findBySessionIdOrderByTimestampDesc(sessionId)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return messageIds.stream()
                .map(id -> chatMessageRepository.findById(id.toString()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get message count for a session
     */
    public long getMessageCount(String sessionId) {
        validateSessionId(sessionId);

        String listKey = getMessageListKey(sessionId);
        Long count = redisTemplate.opsForList().size(listKey);

        if (count == null || count == 0) {
            // Fallback to repository count
            return chatMessageRepository.countBySessionId(sessionId);
        }

        return count;
    }

    /**
     * Get messages by role for a session
     */
    public List<ChatMessage> getMessagesByRole(String sessionId, MessageRole role) {
        validateSessionId(sessionId);
        return chatMessageRepository.findBySessionIdAndRole(sessionId, role);
    }

    /**
     * Get messages after a specific timestamp
     */
    public List<ChatMessage> getMessagesAfter(String sessionId, LocalDateTime timestamp) {
        validateSessionId(sessionId);
        return chatMessageRepository.findBySessionIdAndTimestampAfter(sessionId, timestamp);
    }

    /**
     * Search messages by content
     */
    public List<ChatMessage> searchMessages(String sessionId, String searchTerm) {
        validateSessionId(sessionId);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return chatMessageRepository.findBySessionIdAndContentContainingIgnoreCase(sessionId, searchTerm.trim());
    }

    /**
     * Get the last message in a session
     */
    public Optional<ChatMessage> getLastMessage(String sessionId) {
        validateSessionId(sessionId);
        return chatMessageRepository.findTopBySessionIdOrderByTimestampDesc(sessionId);
    }

    /**
     * Get total token count for a session
     */
    public int getTotalTokenCount(String sessionId) {
        validateSessionId(sessionId);
        Integer total = chatMessageRepository.getTotalTokenCountBySessionId(sessionId);
        return total != null ? total : 0;
    }

    /**
     * Delete all messages for a session
     */
    public void deleteSessionMessages(String sessionId) {
        validateSessionId(sessionId);

        // Delete from repository
        chatMessageRepository.deleteBySessionId(sessionId);

        // Delete Redis list
        String listKey = getMessageListKey(sessionId);
        redisTemplate.delete(listKey);
    }

    /**
     * Clean up old messages (keep only recent ones)
     */
    public int cleanupOldMessages(String sessionId) {
        validateSessionId(sessionId);

        String listKey = getMessageListKey(sessionId);
        Long currentSize = redisTemplate.opsForList().size(listKey);

        if (currentSize != null && currentSize > MAX_MESSAGES_PER_SESSION) {
            // Get message IDs to delete
            List<Object> toDelete = redisTemplate.opsForList().range(listKey, 0, currentSize - MAX_MESSAGES_PER_SESSION - 1);

            if (toDelete != null && !toDelete.isEmpty()) {
                // Delete from repository
                for (Object messageId : toDelete) {
                    chatMessageRepository.deleteById(messageId.toString());
                }

                // Trim Redis list
                redisTemplate.opsForList().trim(listKey, -MAX_MESSAGES_PER_SESSION, -1);

                return toDelete.size();
            }
        }

        return 0;
    }

    // Private helper methods

    private String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String getMessageListKey(String sessionId) {
        return MESSAGE_LIST_KEY_PREFIX + sessionId;
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException("Message content cannot exceed 10,000 characters");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            return null;
        }
        // Basic sanitization - remove potentially harmful content
        return content.trim()
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", ""); // Remove HTML tags
    }

    private int estimateTokenCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // Simple token estimation: ~4 characters per token
        return Math.max(1, content.length() / 4);
    }
}