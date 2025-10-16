package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.model.dto.CleanupResponse;
import com.example.chatbotcache.model.dto.MessageCountResponse;
import com.example.chatbotcache.model.dto.MessageRequest;
import com.example.chatbotcache.model.dto.MessageResponse;
import com.example.chatbotcache.model.dto.MessagesPageResponse;
import com.example.chatbotcache.service.FallbackMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions/{sessionId}/messages")
public class MessageController {

    @Autowired
    private FallbackMessageService messageService;

    /**
     * Add a new message to a session
     */
    @PostMapping
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody MessageRequest request) {
        try {
            ChatMessage message = messageService.addMessage(
                    sessionId,
                    request.getRole(),
                    request.getContent(),
                    request.getTokenCount()
            );

            // Set metadata if provided
            if (request.getMetadata() != null) {
                message.setMetadata(request.getMetadata());
            }

            MessageResponse response = new MessageResponse(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get messages for a session with pagination
     */
    @GetMapping
    public ResponseEntity<MessagesPageResponse> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<ChatMessage> messages = messageService.getMessages(sessionId, page, size);
            long totalMessages = messageService.getMessageCount(sessionId);

            List<MessageResponse> messageResponses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            MessagesPageResponse response = new MessagesPageResponse(messageResponses, page, size, totalMessages);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all messages for a session
     */
    @GetMapping("/all")
    public ResponseEntity<List<MessageResponse>> getAllMessages(@PathVariable String sessionId) {
        try {
            List<ChatMessage> messages = messageService.getAllMessages(sessionId);
            List<MessageResponse> responses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get recent messages (last N messages)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<MessageResponse>> getRecentMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ChatMessage> messages = messageService.getRecentMessages(sessionId, limit);
            List<MessageResponse> responses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get messages by role
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<MessageResponse>> getMessagesByRole(
            @PathVariable String sessionId,
            @PathVariable MessageRole role) {
        try {
            List<ChatMessage> messages = messageService.getMessagesByRole(sessionId, role);
            List<MessageResponse> responses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search messages by content
     */
    @GetMapping("/search")
    public ResponseEntity<List<MessageResponse>> searchMessages(
            @PathVariable String sessionId,
            @RequestParam String query) {
        try {
            List<ChatMessage> messages = messageService.searchMessages(sessionId, query);
            List<MessageResponse> responses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the last message in a session
     */
    @GetMapping("/last")
    public ResponseEntity<MessageResponse> getLastMessage(@PathVariable String sessionId) {
        try {
            Optional<ChatMessage> message = messageService.getLastMessage(sessionId);

            if (message.isPresent()) {
                MessageResponse response = new MessageResponse(message.get());
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
     * Get message count for a session
     */
    @GetMapping("/count")
    public ResponseEntity<MessageCountResponse> getMessageCount(@PathVariable String sessionId) {
        try {
            long messageCount = messageService.getMessageCount(sessionId);
            int totalTokens = (int) messageService.getTotalTokenCount(sessionId);

            MessageCountResponse response = new MessageCountResponse(sessionId, messageCount, totalTokens);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get messages after a specific timestamp
     */
    @GetMapping("/after")
    public ResponseEntity<List<MessageResponse>> getMessagesAfter(
            @PathVariable String sessionId,
            @RequestParam String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp);
            List<ChatMessage> messages = messageService.getMessagesAfter(sessionId, dateTime);
            List<MessageResponse> responses = messages.stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete all messages for a session
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllMessages(@PathVariable String sessionId) {
        try {
            messageService.deleteSessionMessages(sessionId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clean up old messages (keep only recent ones)
     */
    @PostMapping("/cleanup")
    public ResponseEntity<CleanupResponse> cleanupOldMessages(@PathVariable String sessionId) {
        try {
            int deletedCount = messageService.cleanupOldMessages(sessionId);

            CleanupResponse response = new CleanupResponse(sessionId, deletedCount, "Cleanup completed");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}