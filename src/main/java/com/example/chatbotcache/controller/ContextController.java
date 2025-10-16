package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.dto.*;
import com.example.chatbotcache.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ContextController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TokenCountingService tokenCountingService;

    @Autowired
    private ContextCompressionService contextCompressionService;

    @Autowired
    private UserSessionTrackingService userSessionTrackingService;

    @Autowired
    private ChatSessionService chatSessionService;

    @GetMapping("/sessions/{sessionId}/context")
    public ResponseEntity<ContextStatsResponse> getSessionContext(@PathVariable String sessionId) {
        try {
            List<ChatMessage> messages = messageService.getMessages(sessionId, 0, 1000);

            TokenCountingService.TokenSummary tokenSummary = tokenCountingService.analyzeSession(messages);
            ContextCompressionService.ContextAnalysis analysis = contextCompressionService.analyzeContext(messages);

            ContextStatsResponse response = new ContextStatsResponse(
                tokenSummary.getTotalTokens(),
                tokenSummary.getUserTokens(),
                tokenSummary.getAssistantTokens(),
                tokenSummary.getMessageCount(),
                tokenSummary.getAverageTokensPerMessage(),
                analysis.needsCompression(),
                analysis.getPotentialSavings(),
                analysis.getRecommendation(),
                LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/compress")
    public ResponseEntity<CompressionResponse> compressSessionContext(
            @PathVariable String sessionId,
            @RequestBody(required = false) CompressionRequest request) {
        try {
            List<ChatMessage> messages = messageService.getMessages(sessionId, 0, 1000);

            if (messages.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            int contextWindowSize = (request != null && request.getContextWindowSize() != null) ?
                    request.getContextWindowSize() : 4000;

            int originalTokenCount = tokenCountingService.estimateTokenCount(messages);
            ContextCompressionService.CompressionResult result =
                contextCompressionService.compressContext(messages, contextWindowSize);

            if (result.wasCompressed()) {
                try {
                    messageService.replaceSessionMessages(sessionId, result.getCompressedMessages(), result.getConversationSummary());
                } catch (Exception e) {
                }
            }

            CompressionResponse response = new CompressionResponse(
                result.getCompressedMessages(),
                result.getConversationSummary(),
                result.wasCompressed(),
                originalTokenCount,
                result.getFinalTokenCount(),
                result.getTokensRemoved(),
                result.getCompressionRatio(),
                messages.size() - result.getCompressedMessages().size(),
                result.getCompressedMessages().size(),
                LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{userId}/sessions")
    public ResponseEntity<UserSessionsResponse> getUserSessions(@PathVariable String userId) {
        try {
            List<UserSessionTrackingService.UserSessionInfo> sessionInfos =
                userSessionTrackingService.getUserSessionsWithDetails(userId);

            List<UserSessionsResponse.UserSessionInfo> responseInfos = sessionInfos.stream()
                .map(info -> new UserSessionsResponse.UserSessionInfo(
                    info.getSessionId(),
                    info.getTitle(),
                    info.getCreatedAt(),
                    info.getLastActivity(),
                    info.getLastTrackingActivity(),
                    info.isActive()
                ))
                .collect(Collectors.toList());

            long activeSessions = sessionInfos.stream().mapToLong(info -> info.isActive() ? 1 : 0).sum();

            UserSessionsResponse response = new UserSessionsResponse(
                userId,
                sessionInfos.size(),
                (int) activeSessions,
                responseInfos,
                LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{userId}/sessions/cleanup")
    public ResponseEntity<CleanupResponse> cleanupUserSessions(@PathVariable String userId) {
        try {
            List<UserSessionTrackingService.UserSessionInfo> beforeCleanup =
                userSessionTrackingService.getUserSessionsWithDetails(userId);

            userSessionTrackingService.cleanupInactiveSessions(userId);

            List<UserSessionTrackingService.UserSessionInfo> afterCleanup =
                userSessionTrackingService.getUserSessionsWithDetails(userId);

            CleanupResponse response = new CleanupResponse(
                userId,
                beforeCleanup.size() - afterCleanup.size(),
                "User sessions cleaned up successfully"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/context/activity")
    public ResponseEntity<UserSessionTrackingService.SessionActivitySummary> getSessionActivity() {
        try {
            UserSessionTrackingService.SessionActivitySummary summary =
                userSessionTrackingService.getSessionActivitySummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/context/cleanup")
    public ResponseEntity<CleanupResponse> cleanupOldActivityRecords() {
        try {
            userSessionTrackingService.cleanupOldActivityRecords();

            CleanupResponse response = new CleanupResponse(
                "global",
                0,
                "Old activity records cleaned up successfully"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/context/active-sessions")
    public ResponseEntity<List<String>> getActiveSessions() {
        try {
            List<String> activeSessions = userSessionTrackingService.getActiveSessions();
            return ResponseEntity.ok(activeSessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/track-activity")
    public ResponseEntity<Void> trackSessionActivity(@PathVariable String sessionId) {
        try {
            chatSessionService.getSessionOrThrow(sessionId);
            userSessionTrackingService.updateSessionActivity(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}