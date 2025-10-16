package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Primary
public class FallbackMessageService {

    private final Map<String, List<ChatMessage>> sessionMessages = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES_PER_SESSION = 50;

    public ChatMessage saveMessage(String sessionId, String content, MessageRole role) {
        validateSessionId(sessionId);
        validateContent(content);

        String messageId = generateMessageId();
        ChatMessage message = new ChatMessage(messageId, sessionId, role, content);

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);

        // Limit messages per session
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages.size() > MAX_MESSAGES_PER_SESSION) {
            messages.remove(0); // Remove oldest message
        }

        return message;
    }

    public ChatMessage saveUserMessage(String sessionId, String content) {
        return saveMessage(sessionId, content, MessageRole.USER);
    }

    public ChatMessage saveAssistantMessage(String sessionId, String content) {
        return saveMessage(sessionId, content, MessageRole.ASSISTANT);
    }

    public ChatMessage saveSystemMessage(String sessionId, String content) {
        return saveMessage(sessionId, content, MessageRole.SYSTEM);
    }

    public List<ChatMessage> getSessionMessages(String sessionId) {
        validateSessionId(sessionId);
        return sessionMessages.getOrDefault(sessionId, new ArrayList<>());
    }

    public List<ChatMessage> getSessionMessages(String sessionId, int limit) {
        List<ChatMessage> messages = getSessionMessages(sessionId);
        if (messages.size() <= limit) {
            return messages;
        }
        return messages.subList(Math.max(0, messages.size() - limit), messages.size());
    }

    public List<ChatMessage> getSessionMessagesWithPagination(String sessionId, int page, int size) {
        List<ChatMessage> messages = getSessionMessages(sessionId);
        int start = page * size;
        int end = Math.min(start + size, messages.size());

        if (start >= messages.size()) {
            return new ArrayList<>();
        }

        return messages.subList(start, end);
    }

    public Optional<ChatMessage> getMessage(String messageId) {
        for (List<ChatMessage> messages : sessionMessages.values()) {
            for (ChatMessage message : messages) {
                if (messageId.equals(message.getMessageId())) {
                    return Optional.of(message);
                }
            }
        }
        return Optional.empty();
    }

    public boolean deleteMessage(String messageId) {
        for (List<ChatMessage> messages : sessionMessages.values()) {
            if (messages.removeIf(message -> messageId.equals(message.getMessageId()))) {
                return true;
            }
        }
        return false;
    }

    public int deleteSessionMessages(String sessionId) {
        validateSessionId(sessionId);
        List<ChatMessage> messages = sessionMessages.remove(sessionId);
        return messages != null ? messages.size() : 0;
    }

    public long countSessionMessages(String sessionId) {
        validateSessionId(sessionId);
        return sessionMessages.getOrDefault(sessionId, new ArrayList<>()).size();
    }

    public List<ChatMessage> searchMessages(String sessionId, String searchTerm) {
        validateSessionId(sessionId);
        String lowerSearchTerm = searchTerm.toLowerCase();

        return getSessionMessages(sessionId).stream()
                .filter(message -> message.getContent().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getMessagesByRole(String sessionId, MessageRole role) {
        validateSessionId(sessionId);

        return getSessionMessages(sessionId).stream()
                .filter(message -> message.getRole() == role)
                .collect(Collectors.toList());
    }

    public String getConversationHistory(String sessionId, int maxMessages) {
        List<ChatMessage> messages = getSessionMessages(sessionId, maxMessages);

        return messages.stream()
                .map(msg -> msg.getRole().getValue() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
    }

    public Map<String, Object> getSessionMessageStats(String sessionId) {
        List<ChatMessage> messages = getSessionMessages(sessionId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", messages.size());
        stats.put("userMessages", messages.stream().filter(m -> m.getRole() == MessageRole.USER).count());
        stats.put("assistantMessages", messages.stream().filter(m -> m.getRole() == MessageRole.ASSISTANT).count());
        stats.put("systemMessages", messages.stream().filter(m -> m.getRole() == MessageRole.SYSTEM).count());

        if (!messages.isEmpty()) {
            stats.put("firstMessage", messages.get(0).getTimestamp());
            stats.put("lastMessage", messages.get(messages.size() - 1).getTimestamp());
        }

        return stats;
    }

    // Methods expected by controllers

    public ChatMessage addMessage(String sessionId, MessageRole role, String content, Integer tokenCount) {
        return saveMessage(sessionId, content, role);
    }

    public ChatMessage addMessage(String sessionId, MessageRole role, String content, int tokenCount) {
        return saveMessage(sessionId, content, role);
    }

    public List<ChatMessage> getMessages(String sessionId, int page, int size) {
        return getSessionMessagesWithPagination(sessionId, page, size);
    }

    public long getMessageCount(String sessionId) {
        return countSessionMessages(sessionId);
    }

    public List<ChatMessage> getAllMessages(String sessionId) {
        return getSessionMessages(sessionId);
    }

    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        return getSessionMessages(sessionId, limit);
    }

    public Optional<ChatMessage> getLastMessage(String sessionId) {
        List<ChatMessage> messages = getSessionMessages(sessionId);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(messages.size() - 1));
    }

    public long getTotalTokenCount(String sessionId) {
        return getSessionMessages(sessionId).stream()
                .mapToLong(msg -> msg.getTokenCount() != null ? msg.getTokenCount() : 0)
                .sum();
    }

    public int getTotalTokenCountAsInt(String sessionId) {
        return (int) getTotalTokenCount(sessionId);
    }

    public List<ChatMessage> getMessagesAfter(String sessionId, java.time.LocalDateTime after) {
        return getSessionMessages(sessionId).stream()
                .filter(msg -> msg.getTimestamp().isAfter(after))
                .collect(Collectors.toList());
    }

    public int cleanupOldMessages(String sessionId) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages != null && messages.size() > MAX_MESSAGES_PER_SESSION) {
            int toRemove = messages.size() - MAX_MESSAGES_PER_SESSION;
            for (int i = 0; i < toRemove; i++) {
                messages.remove(0);
            }
            return toRemove;
        }
        return 0;
    }

    // Private helper methods

    private String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
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
            throw new IllegalArgumentException("Message content cannot exceed 10000 characters");
        }
    }
}