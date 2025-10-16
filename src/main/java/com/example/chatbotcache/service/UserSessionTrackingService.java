package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatSession;
import com.example.chatbotcache.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSessionTrackingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    private static final String ACTIVE_SESSIONS_KEY = "sessions:active";
    private static final String SESSION_ACTIVITY_PREFIX = "session:activity:";
    private static final int MAX_SESSIONS_PER_USER = 10;
    private static final int SESSION_ACTIVITY_WINDOW_MINUTES = 30;

    public void trackUserSession(String userId, String sessionId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;

        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, java.time.Duration.ofDays(30));

        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(ACTIVE_SESSIONS_KEY, sessionId, timestamp);

        updateSessionActivity(sessionId);

        enforceSessionLimit(userId);
    }

    public void removeUserSession(String userId, String sessionId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;

        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        redisTemplate.opsForZSet().remove(ACTIVE_SESSIONS_KEY, sessionId);

        String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
        redisTemplate.delete(activityKey);
    }

    public Set<String> getUserSessions(String userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<Object> sessions = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessions == null) {
            return new HashSet<>();
        }

        return sessions.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public List<UserSessionInfo> getUserSessionsWithDetails(String userId) {
        Set<String> sessionIds = getUserSessions(userId);
        List<UserSessionInfo> sessionInfos = new ArrayList<>();

        for (String sessionId : sessionIds) {
            try {
                Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
                if (sessionOpt.isPresent()) {
                    ChatSession session = sessionOpt.get();
                    boolean isActive = isSessionActive(sessionId);
                    LocalDateTime lastActivity = getLastSessionActivity(sessionId);

                    sessionInfos.add(new UserSessionInfo(
                        sessionId,
                        session.getTitle(),
                        session.getCreatedAt(),
                        session.getLastActivity(),
                        lastActivity,
                        isActive
                    ));
                }
            } catch (Exception e) {
                removeUserSession(userId, sessionId);
            }
        }

        return sessionInfos.stream()
                .sorted((a, b) -> b.getLastActivity().compareTo(a.getLastActivity()))
                .collect(Collectors.toList());
    }

    public void updateSessionActivity(String sessionId) {
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(ACTIVE_SESSIONS_KEY, sessionId, timestamp);

        String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(activityKey, timestamp);
        redisTemplate.expire(activityKey, java.time.Duration.ofHours(2));
    }

    public boolean isSessionActive(String sessionId) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(SESSION_ACTIVITY_WINDOW_MINUTES);
        long cutoffTimestamp = cutoff.toEpochSecond(ZoneOffset.UTC);

        Double score = redisTemplate.opsForZSet().score(ACTIVE_SESSIONS_KEY, sessionId);
        return score != null && score >= cutoffTimestamp;
    }

    public LocalDateTime getLastSessionActivity(String sessionId) {
        String activityKey = SESSION_ACTIVITY_PREFIX + sessionId;
        Object timestamp = redisTemplate.opsForValue().get(activityKey);

        if (timestamp instanceof Number) {
            return LocalDateTime.ofEpochSecond(((Number) timestamp).longValue(), 0, ZoneOffset.UTC);
        }

        Double score = redisTemplate.opsForZSet().score(ACTIVE_SESSIONS_KEY, sessionId);
        if (score != null) {
            return LocalDateTime.ofEpochSecond(score.longValue(), 0, ZoneOffset.UTC);
        }

        return LocalDateTime.now().minusHours(1);
    }

    public void cleanupInactiveSessions(String userId) {
        Set<String> userSessions = getUserSessions(userId);
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        for (String sessionId : userSessions) {
            LocalDateTime lastActivity = getLastSessionActivity(sessionId);
            if (lastActivity.isBefore(cutoff)) {
                try {
                    chatSessionRepository.deleteById(sessionId);
                    removeUserSession(userId, sessionId);
                } catch (Exception e) {
                    removeUserSession(userId, sessionId);
                }
            }
        }
    }

    private void enforceSessionLimit(String userId) {
        Set<String> userSessions = getUserSessions(userId);

        if (userSessions.size() <= MAX_SESSIONS_PER_USER) {
            return;
        }

        List<UserSessionInfo> sessionInfos = getUserSessionsWithDetails(userId);

        List<String> sessionsToRemove = sessionInfos.stream()
                .filter(info -> !info.isActive())
                .sorted((a, b) -> a.getLastActivity().compareTo(b.getLastActivity()))
                .limit(userSessions.size() - MAX_SESSIONS_PER_USER)
                .map(UserSessionInfo::getSessionId)
                .collect(Collectors.toList());

        for (String sessionId : sessionsToRemove) {
            try {
                chatSessionRepository.deleteById(sessionId);
            } catch (Exception e) {
            }
            removeUserSession(userId, sessionId);
        }
    }

    public SessionActivitySummary getSessionActivitySummary() {
        long totalSessions = redisTemplate.opsForZSet().zCard(ACTIVE_SESSIONS_KEY);

        LocalDateTime now = LocalDateTime.now();
        long activeThreshold = now.minusMinutes(SESSION_ACTIVITY_WINDOW_MINUTES).toEpochSecond(ZoneOffset.UTC);
        long recentThreshold = now.minusHours(1).toEpochSecond(ZoneOffset.UTC);
        long todayThreshold = now.minusHours(24).toEpochSecond(ZoneOffset.UTC);

        Long activeSessions = redisTemplate.opsForZSet().count(ACTIVE_SESSIONS_KEY, activeThreshold, Double.MAX_VALUE);
        Long recentSessions = redisTemplate.opsForZSet().count(ACTIVE_SESSIONS_KEY, recentThreshold, Double.MAX_VALUE);
        Long todaySessions = redisTemplate.opsForZSet().count(ACTIVE_SESSIONS_KEY, todayThreshold, Double.MAX_VALUE);

        return new SessionActivitySummary(
            totalSessions,
            activeSessions != null ? activeSessions : 0,
            recentSessions != null ? recentSessions : 0,
            todaySessions != null ? todaySessions : 0
        );
    }

    public List<String> getActiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(SESSION_ACTIVITY_WINDOW_MINUTES);
        long cutoffTimestamp = cutoff.toEpochSecond(ZoneOffset.UTC);

        Set<Object> activeSessions = redisTemplate.opsForZSet()
                .rangeByScore(ACTIVE_SESSIONS_KEY, cutoffTimestamp, Double.MAX_VALUE);

        if (activeSessions == null) {
            return new ArrayList<>();
        }

        return activeSessions.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public void cleanupOldActivityRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        long cutoffTimestamp = cutoff.toEpochSecond(ZoneOffset.UTC);

        redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_SESSIONS_KEY, 0, cutoffTimestamp);
    }

    public static class UserSessionInfo {
        private final String sessionId;
        private final String title;
        private final LocalDateTime createdAt;
        private final LocalDateTime lastActivity;
        private final LocalDateTime lastTrackingActivity;
        private final boolean active;

        public UserSessionInfo(String sessionId, String title, LocalDateTime createdAt,
                             LocalDateTime lastActivity, LocalDateTime lastTrackingActivity, boolean active) {
            this.sessionId = sessionId;
            this.title = title;
            this.createdAt = createdAt;
            this.lastActivity = lastActivity;
            this.lastTrackingActivity = lastTrackingActivity;
            this.active = active;
        }

        public String getSessionId() { return sessionId; }
        public String getTitle() { return title; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public LocalDateTime getLastTrackingActivity() { return lastTrackingActivity; }
        public boolean isActive() { return active; }
    }

    public static class SessionActivitySummary {
        private final long totalSessions;
        private final long activeSessions;
        private final long recentSessions;
        private final long todaySessions;

        public SessionActivitySummary(long totalSessions, long activeSessions, long recentSessions, long todaySessions) {
            this.totalSessions = totalSessions;
            this.activeSessions = activeSessions;
            this.recentSessions = recentSessions;
            this.todaySessions = todaySessions;
        }

        public long getTotalSessions() { return totalSessions; }
        public long getActiveSessions() { return activeSessions; }
        public long getRecentSessions() { return recentSessions; }
        public long getTodaySessions() { return todaySessions; }
    }
}