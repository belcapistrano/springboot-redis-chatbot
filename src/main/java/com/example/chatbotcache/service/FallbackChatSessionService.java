package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Primary
public class FallbackChatSessionService {

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final CacheService cacheService;

    public FallbackChatSessionService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public ChatSession createSession(String userId, String title) {
        validateUserId(userId);

        String sessionId = generateSessionId();
        ChatSession session = new ChatSession(sessionId, userId, title);
        sessions.put(sessionId, session);

        return session;
    }

    public ChatSession createSession(String userId) {
        return createSession(userId, "New Chat Session");
    }

    public Optional<ChatSession> getSession(String sessionId) {
        validateSessionId(sessionId);
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public ChatSession getSessionOrThrow(String sessionId) {
        return getSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    public ChatSession updateLastActivity(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.updateLastActivity();

        // Track activity in cache for sorted sets and TTL management
        try {
            cacheService.trackSessionActivity(sessionId, session.getUserId());
            cacheService.setActiveSessionTTL(sessionId);
        } catch (Exception e) {
            // Silently fail if Redis is not available
            System.out.println("Cache service unavailable, skipping activity tracking");
        }

        sessions.put(sessionId, session);
        return session;
    }

    public boolean deleteSession(String sessionId) {
        validateSessionId(sessionId);
        return sessions.remove(sessionId) != null;
    }

    public List<ChatSession> getUserSessions(String userId) {
        validateUserId(userId);
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .collect(Collectors.toList());
    }

    public List<ChatSession> getActiveUserSessions(String userId) {
        validateUserId(userId);
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()) && session.getActive())
                .collect(Collectors.toList());
    }

    public ChatSession deactivateSession(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setActive(false);
        session.updateLastActivity();

        try {
            cacheService.setInactiveSessionTTL(sessionId);
        } catch (Exception e) {
            // Silently fail if Redis is not available
        }

        sessions.put(sessionId, session);
        return session;
    }

    public ChatSession reactivateSession(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setActive(true);
        session.updateLastActivity();
        sessions.put(sessionId, session);
        return session;
    }

    public ChatSession updateSessionTitle(String sessionId, String title) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setTitle(title);
        session.updateLastActivity();
        sessions.put(sessionId, session);
        return session;
    }

    public ChatSession incrementMessageCount(String sessionId) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.incrementMessageCount();
        session.updateLastActivity();
        sessions.put(sessionId, session);
        return session;
    }

    public ChatSession addTokensToSession(String sessionId, int tokens) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.addTokens(tokens);
        session.updateLastActivity();
        sessions.put(sessionId, session);
        return session;
    }

    public ChatSession setSessionSetting(String sessionId, String key, Object value) {
        ChatSession session = getSessionOrThrow(sessionId);
        session.setSetting(key, value);
        session.updateLastActivity();
        sessions.put(sessionId, session);
        return session;
    }

    public Object getSessionSetting(String sessionId, String key) {
        ChatSession session = getSessionOrThrow(sessionId);
        return session.getSetting(key);
    }

    public long countUserSessions(String userId) {
        validateUserId(userId);
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .count();
    }

    public long countActiveUserSessions(String userId) {
        validateUserId(userId);
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()) && session.getActive())
                .count();
    }

    public int cleanupInactiveSessions(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ChatSession> entry : sessions.entrySet()) {
            ChatSession session = entry.getValue();
            if (!session.getActive() && session.getLastActivity().isBefore(cutoffTime)) {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(sessions::remove);
        return toRemove.size();
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
}