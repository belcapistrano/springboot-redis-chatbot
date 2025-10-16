package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.model.UserPreferences;
import com.example.chatbotcache.model.dto.ChatRequest;
import com.example.chatbotcache.model.dto.ChatResponse;
import com.example.chatbotcache.model.dto.ContextResponse;
import com.example.chatbotcache.service.FallbackChatSessionService;
import com.example.chatbotcache.service.FallbackMessageService;
import com.example.chatbotcache.service.MockLLMService;
import com.example.chatbotcache.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions/{sessionId}")
public class ChatController {

    @Autowired
    private FallbackMessageService messageService;

    @Autowired
    private MockLLMService mockLLMService;

    @Autowired
    private FallbackChatSessionService chatSessionService;

    @Autowired
    private UserPreferencesService userPreferencesService;

    /**
     * Send a message and get an AI response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatRequest request) {
        try {
            // Validate session exists
            var session = chatSessionService.getSession(sessionId);
            if (session.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String userId = session.get().getUserId();

            // Get user preferences
            UserPreferences preferences = userPreferencesService.getUserPreferences(userId);

            // Store user message
            ChatMessage userMessage = messageService.addMessage(
                sessionId,
                MessageRole.USER,
                request.getMessage(),
                mockLLMService.estimateTokenCount(request.getMessage())
            );

            // Generate AI response with metadata
            Map<String, Object> responseData = mockLLMService.generateResponseWithMetadata(
                sessionId,
                request.getMessage(),
                preferences
            );

            // Check if generation failed
            if (responseData.containsKey("error")) {
                ChatResponse errorResponse = new ChatResponse();
                errorResponse.setSessionId(sessionId);
                errorResponse.setUserMessage(request.getMessage());
                errorResponse.setError((String) responseData.get("error"));
                errorResponse.setProcessingTimeMs((Long) responseData.get("processingTimeMs"));

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

            // Store assistant response
            String aiResponse = (String) responseData.get("response");
            Integer tokenCount = (Integer) responseData.get("tokenCount");

            ChatMessage assistantMessage = messageService.addMessage(
                sessionId,
                MessageRole.ASSISTANT,
                aiResponse,
                tokenCount
            );

            // Update session activity
            chatSessionService.updateLastActivity(sessionId);

            // Build response
            ChatResponse response = new ChatResponse();
            response.setSessionId(sessionId);
            response.setUserMessage(request.getMessage());
            response.setAiResponse(aiResponse);
            response.setUserMessageId(userMessage.getMessageId());
            response.setAiMessageId(assistantMessage.getMessageId());
            response.setTokenCount(tokenCount);
            response.setProcessingTimeMs((Long) responseData.get("processingTimeMs"));
            response.setModel((String) responseData.get("model"));
            response.setTopic((String) responseData.get("topic"));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Handle LLM service errors
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSessionId(sessionId);
            errorResponse.setUserMessage(request.getMessage());
            errorResponse.setError("Service temporarily unavailable: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Continue conversation with context
     */
    @PostMapping("/chat/continue")
    public ResponseEntity<ChatResponse> continueConversation(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatRequest request) {
        try {
            // This endpoint is identical to /chat but semantically different
            // It explicitly indicates continuation of existing conversation
            return chat(sessionId, request);

        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSessionId(sessionId);
            errorResponse.setUserMessage(request.getMessage());
            errorResponse.setError("Failed to continue conversation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get conversation context summary
     */
    @GetMapping("/context")
    public ResponseEntity<ContextResponse> getConversationContext(@PathVariable String sessionId) {
        try {
            // Validate session exists
            var session = chatSessionService.getSession(sessionId);
            if (session.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var context = mockLLMService.getConversationContext(sessionId);
            String currentTopic = mockLLMService.getCurrentTopic(sessionId);
            long messageCount = messageService.getMessageCount(sessionId);
            int totalTokens = (int) messageService.getTotalTokenCount(sessionId);

            ContextResponse response = new ContextResponse(
                sessionId,
                context.size(),
                messageCount,
                totalTokens,
                currentTopic,
                !context.isEmpty()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}