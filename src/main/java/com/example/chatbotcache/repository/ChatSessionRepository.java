package com.example.chatbotcache.repository;

import com.example.chatbotcache.model.ChatSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends CrudRepository<ChatSession, String> {

    /**
     * Find all sessions for a specific user
     */
    List<ChatSession> findByUserId(String userId);

    /**
     * Find active sessions for a specific user
     */
    List<ChatSession> findByUserIdAndActiveTrue(String userId);

    /**
     * Find sessions created after a specific date
     */
    List<ChatSession> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find sessions with last activity after a specific date
     */
    List<ChatSession> findByLastActivityAfter(LocalDateTime date);

    /**
     * Find sessions by user ID and active status
     */
    List<ChatSession> findByUserIdAndActive(String userId, Boolean active);

    /**
     * Find sessions by title containing text (case insensitive)
     */
    List<ChatSession> findByTitleContainingIgnoreCase(String title);

    /**
     * Count total sessions for a user
     */
    long countByUserId(String userId);

    /**
     * Count active sessions for a user
     */
    long countByUserIdAndActiveTrue(String userId);

    /**
     * Find the most recent session for a user
     */
    Optional<ChatSession> findTopByUserIdOrderByLastActivityDesc(String userId);

    /**
     * Find sessions that haven't been active since a specific date
     */
    List<ChatSession> findByLastActivityBefore(LocalDateTime date);

    /**
     * Delete all sessions for a specific user
     */
    void deleteByUserId(String userId);

    /**
     * Delete inactive sessions before a specific date
     */
    void deleteByActiveFalseAndLastActivityBefore(LocalDateTime date);
}