package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatSession;
import com.example.chatbotcache.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private UserSessionTrackingService userSessionTrackingService;

    /**
     * Create a new chat session
     */
    public ChatSession createSession(String userId, String title) {
        validateUserId(userId);

        String sessionId = generateSessionId();
        ChatSession session = new ChatSession(sessionId, userId, title);

        ChatSession savedSession = chatSessionRepository.save(session);

        // Track the session for the user
        userSessionTrackingService.trackUserSession(userId, sessionId);

        return savedSession;
    }

    /**
     * Create a new chat session with default title
     */
    public ChatSession createSession(String userId) {
        return createSession(userId, "New Chat Session");
    }

    /**
     * Get a session by ID with error handling
     */
    public Optional<ChatSession> getSessionOptional(String sessionId) {
        validateSessionId(sessionId);

        try {
            return chatSessionRepository.findById(sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving session: " + sessionId, e);
        }
    }

    /**
     * Get a session by ID, throwing exception if not found
     */
    public ChatSession getSessionOrThrow(String sessionId) {
        return getSessionOptional(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    /**
     * Get a session by ID directly (for compatibility)
     */
    public ChatSession getSession(String sessionId) {
        validateSessionId(sessionId);
        return getSessionOrThrow(sessionId);
    }

    /**
     * Update the last activity timestamp for a session
     */
    public ChatSession updateLastActivity(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.updateLastActivity();

        // Track activity in cache for sorted sets and TTL management
        cacheService.trackSessionActivity(sessionId, session.getUserId());
        cacheService.setActiveSessionTTL(sessionId);

        // Track activity in user session tracking
        userSessionTrackingService.updateSessionActivity(sessionId);

        return chatSessionRepository.save(session);
    }

    /**
     * Delete a session by ID
     */
    public boolean deleteSession(String sessionId) {
        validateSessionId(sessionId);

        try {
            if (chatSessionRepository.existsById(sessionId)) {
                // Get the session to find the user ID
                Optional<ChatSession> sessionOpt = getSessionOptional(sessionId);
                if (sessionOpt.isPresent()) {
                    String userId = sessionOpt.get().getUserId();

                    // Remove from user session tracking
                    userSessionTrackingService.removeUserSession(userId, sessionId);
                }

                chatSessionRepository.deleteById(sessionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting session: " + sessionId, e);
        }
    }

    /**
     * Get all sessions for a user
     */
    public List<ChatSession> getUserSessions(String userId) {
        validateUserId(userId);
        return chatSessionRepository.findByUserId(userId);
    }

    /**
     * Get active sessions for a user
     */
    public List<ChatSession> getActiveUserSessions(String userId) {
        validateUserId(userId);
        return chatSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Deactivate a session (soft delete)
     */
    public ChatSession deactivateSession(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setActive(false);
        session.updateLastActivity();

        // Set shorter TTL for inactive sessions
        cacheService.setInactiveSessionTTL(sessionId);

        return chatSessionRepository.save(session);
    }

    /**
     * Reactivate a session
     */
    public ChatSession reactivateSession(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setActive(true);
        session.updateLastActivity();
        return chatSessionRepository.save(session);
    }

    /**
     * Update session title
     */
    public ChatSession updateSessionTitle(String sessionId, String title) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setTitle(title);
        session.updateLastActivity();
        return chatSessionRepository.save(session);
    }

    /**
     * Increment message count for a session
     */
    public ChatSession incrementMessageCount(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.incrementMessageCount();
        session.updateLastActivity();
        return chatSessionRepository.save(session);
    }

    /**
     * Add tokens to session count
     */
    public ChatSession addTokensToSession(String sessionId, int tokens) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.addTokens(tokens);
        session.updateLastActivity();
        return chatSessionRepository.save(session);
    }

    /**
     * Set session setting
     */
    public ChatSession setSessionSetting(String sessionId, String key, Object value) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setSetting(key, value);
        session.updateLastActivity();
        return chatSessionRepository.save(session);
    }

    /**
     * Get session setting
     */
    public Object getSessionSetting(String sessionId, String key) {
        ChatSession session = getSessionOrThrow(sessionId);
        return session.getSetting(key);
    }

    /**
     * Count total sessions for a user
     */
    public long countUserSessions(String userId) {
        validateUserId(userId);
        return chatSessionRepository.countByUserId(userId);
    }

    /**
     * Count active sessions for a user
     */
    public long countActiveUserSessions(String userId) {
        validateUserId(userId);
        return chatSessionRepository.countByUserIdAndActiveTrue(userId);
    }

    /**
     * Cleanup inactive sessions older than specified hours
     */
    public int cleanupInactiveSessions(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        List<ChatSession> oldSessions = chatSessionRepository.findByLastActivityBefore(cutoffTime);

        int deletedCount = 0;
        for (ChatSession session : oldSessions) {
            if (!session.getActive()) {
                chatSessionRepository.delete(session);
                deletedCount++;
            }
        }

        return deletedCount;
    }

    // Private helper methods

    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }

    private void validateTitle(String title) {
        if (title != null && title.length() > 255) {
            throw new IllegalArgumentException("Session title cannot exceed 255 characters");
        }
    }
}