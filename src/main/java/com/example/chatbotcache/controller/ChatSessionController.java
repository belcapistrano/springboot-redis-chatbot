package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.ChatSession;
import com.example.chatbotcache.model.dto.CreateSessionRequest;
import com.example.chatbotcache.model.dto.SessionResponse;
import com.example.chatbotcache.model.dto.SessionStatsResponse;
import com.example.chatbotcache.model.dto.UpdateSessionRequest;
import com.example.chatbotcache.service.FallbackChatSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
public class ChatSessionController {

    @Autowired
    private FallbackChatSessionService chatSessionService;

    /**
     * Create a new chat session
     */
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        try {
            ChatSession session;
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                session = chatSessionService.createSession(request.getUserId(), request.getTitle());
            } else {
                session = chatSessionService.createSession(request.getUserId());
            }

            SessionResponse response = new SessionResponse(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String sessionId) {
        try {
            Optional<ChatSession> session = chatSessionService.getSession(sessionId);

            if (session.isPresent()) {
                SessionResponse response = new SessionResponse(session.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a session
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable String sessionId,
            @Valid @RequestBody UpdateSessionRequest request) {
        try {
            Optional<ChatSession> existingSession = chatSessionService.getSession(sessionId);
            if (!existingSession.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ChatSession session = existingSession.get();

            // Update title if provided
            if (request.getTitle() != null) {
                session = chatSessionService.updateSessionTitle(sessionId, request.getTitle());
            }

            // Update active status if provided
            if (request.getActive() != null) {
                if (request.getActive()) {
                    session = chatSessionService.reactivateSession(sessionId);
                } else {
                    session = chatSessionService.deactivateSession(sessionId);
                }
            }

            SessionResponse response = new SessionResponse(session);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a session
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        try {
            boolean deleted = chatSessionService.deleteSession(sessionId);

            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all sessions for a user
     */
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getUserSessions(@RequestParam String userId) {
        try {
            List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
            List<SessionResponse> responses = sessions.stream()
                    .map(SessionResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active sessions for a user
     */
    @GetMapping("/active")
    public ResponseEntity<List<SessionResponse>> getActiveUserSessions(@RequestParam String userId) {
        try {
            List<ChatSession> sessions = chatSessionService.getActiveUserSessions(userId);
            List<SessionResponse> responses = sessions.stream()
                    .map(SessionResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update last activity for a session
     */
    @PostMapping("/{sessionId}/activity")
    public ResponseEntity<SessionResponse> updateLastActivity(@PathVariable String sessionId) {
        try {
            ChatSession session = chatSessionService.updateLastActivity(sessionId);
            SessionResponse response = new SessionResponse(session);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get session statistics for a user
     */
    @GetMapping("/stats")
    public ResponseEntity<SessionStatsResponse> getUserSessionStats(@RequestParam String userId) {
        try {
            long totalSessions = chatSessionService.countUserSessions(userId);
            long activeSessions = chatSessionService.countActiveUserSessions(userId);

            SessionStatsResponse response = new SessionStatsResponse(userId, totalSessions, activeSessions);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}