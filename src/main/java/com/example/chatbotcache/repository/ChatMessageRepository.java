package com.example.chatbotcache.repository;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, String> {

    /**
     * Find all messages for a specific session
     */
    List<ChatMessage> findBySessionId(String sessionId);

    /**
     * Find messages for a session ordered by timestamp
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    /**
     * Find messages for a session ordered by timestamp descending
     */
    List<ChatMessage> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find messages by role for a specific session
     */
    List<ChatMessage> findBySessionIdAndRole(String sessionId, MessageRole role);

    /**
     * Find messages after a specific timestamp
     */
    List<ChatMessage> findBySessionIdAndTimestampAfter(String sessionId, LocalDateTime timestamp);

    /**
     * Find messages before a specific timestamp
     */
    List<ChatMessage> findBySessionIdAndTimestampBefore(String sessionId, LocalDateTime timestamp);

    /**
     * Find messages between two timestamps
     */
    List<ChatMessage> findBySessionIdAndTimestampBetween(String sessionId, LocalDateTime start, LocalDateTime end);

    /**
     * Find the most recent message in a session
     */
    Optional<ChatMessage> findTopBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find the first message in a session
     */
    Optional<ChatMessage> findTopBySessionIdOrderByTimestampAsc(String sessionId);

    /**
     * Count total messages in a session
     */
    long countBySessionId(String sessionId);

    /**
     * Count messages by role in a session
     */
    long countBySessionIdAndRole(String sessionId, MessageRole role);

    /**
     * Find messages containing specific text (case insensitive)
     */
    List<ChatMessage> findBySessionIdAndContentContainingIgnoreCase(String sessionId, String content);

    /**
     * Find messages with token count greater than specified value
     */
    List<ChatMessage> findBySessionIdAndTokenCountGreaterThan(String sessionId, Integer tokenCount);

    /**
     * Find recent messages (limit by count)
     */
    List<ChatMessage> findTop10BySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find recent messages with custom limit
     */
    List<ChatMessage> findTopNBySessionIdOrderByTimestampDesc(String sessionId, int limit);

    /**
     * Delete all messages for a specific session
     */
    void deleteBySessionId(String sessionId);

    /**
     * Delete messages older than specified date
     */
    void deleteBySessionIdAndTimestampBefore(String sessionId, LocalDateTime timestamp);

    /**
     * Sum token count for all messages in a session
     */
    default Integer getTotalTokenCountBySessionId(String sessionId) {
        return findBySessionId(sessionId).stream()
                .mapToInt(message -> message.getTokenCount() != null ? message.getTokenCount() : 0)
                .sum();
    }
}