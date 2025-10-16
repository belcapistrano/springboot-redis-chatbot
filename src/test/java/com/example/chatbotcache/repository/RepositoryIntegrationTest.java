package com.example.chatbotcache.repository;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.ChatSession;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.model.UserPreferences;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
public class RepositoryIntegrationTest {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Test
    public void testChatSessionCrudOperations() {
        // Create
        String sessionId = "test-session-" + UUID.randomUUID();
        String userId = "test-user-" + UUID.randomUUID();

        ChatSession session = new ChatSession(sessionId, userId, "Test Session");
        ChatSession savedSession = chatSessionRepository.save(session);

        assertNotNull(savedSession);
        assertEquals(sessionId, savedSession.getSessionId());
        assertEquals(userId, savedSession.getUserId());
        assertEquals("Test Session", savedSession.getTitle());

        // Read
        Optional<ChatSession> foundSession = chatSessionRepository.findById(sessionId);
        assertTrue(foundSession.isPresent());
        assertEquals(sessionId, foundSession.get().getSessionId());

        // Update
        savedSession.setTitle("Updated Test Session");
        savedSession.updateLastActivity();
        ChatSession updatedSession = chatSessionRepository.save(savedSession);
        assertEquals("Updated Test Session", updatedSession.getTitle());

        // Delete
        chatSessionRepository.deleteById(sessionId);
        Optional<ChatSession> deletedSession = chatSessionRepository.findById(sessionId);
        assertFalse(deletedSession.isPresent());
    }

    @Test
    public void testChatMessageCrudOperations() {
        // Create
        String messageId = "test-message-" + UUID.randomUUID();
        String sessionId = "test-session-" + UUID.randomUUID();

        ChatMessage message = new ChatMessage(messageId, sessionId, MessageRole.USER, "Hello, world!");
        message.setTokenCount(3);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        assertNotNull(savedMessage);
        assertEquals(messageId, savedMessage.getMessageId());
        assertEquals(sessionId, savedMessage.getSessionId());
        assertEquals(MessageRole.USER, savedMessage.getRole());
        assertEquals("Hello, world!", savedMessage.getContent());
        assertEquals(3, savedMessage.getTokenCount());

        // Read
        Optional<ChatMessage> foundMessage = chatMessageRepository.findById(messageId);
        assertTrue(foundMessage.isPresent());
        assertEquals(messageId, foundMessage.get().getMessageId());

        // Update
        savedMessage.setContent("Updated message content");
        savedMessage.setTokenCount(5);
        ChatMessage updatedMessage = chatMessageRepository.save(savedMessage);
        assertEquals("Updated message content", updatedMessage.getContent());
        assertEquals(5, updatedMessage.getTokenCount());

        // Delete
        chatMessageRepository.deleteById(messageId);
        Optional<ChatMessage> deletedMessage = chatMessageRepository.findById(messageId);
        assertFalse(deletedMessage.isPresent());
    }

    @Test
    public void testUserPreferencesCrudOperations() {
        // Create
        String userId = "test-user-" + UUID.randomUUID();

        UserPreferences preferences = new UserPreferences(userId);
        preferences.setModel("gpt-4");
        preferences.setTemperature(0.8);
        preferences.setMaxTokens(1000);
        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);

        assertNotNull(savedPreferences);
        assertEquals(userId, savedPreferences.getUserId());
        assertEquals("gpt-4", savedPreferences.getModel());
        assertEquals(0.8, savedPreferences.getTemperature());
        assertEquals(1000, savedPreferences.getMaxTokens());

        // Read
        Optional<UserPreferences> foundPreferences = userPreferencesRepository.findById(userId);
        assertTrue(foundPreferences.isPresent());
        assertEquals(userId, foundPreferences.get().getUserId());

        // Update
        savedPreferences.setTemperature(0.9);
        savedPreferences.setMaxTokens(2000);
        savedPreferences.updateTimestamp();
        UserPreferences updatedPreferences = userPreferencesRepository.save(savedPreferences);
        assertEquals(0.9, updatedPreferences.getTemperature());
        assertEquals(2000, updatedPreferences.getMaxTokens());

        // Delete
        userPreferencesRepository.deleteById(userId);
        Optional<UserPreferences> deletedPreferences = userPreferencesRepository.findById(userId);
        assertFalse(deletedPreferences.isPresent());
    }

    @Test
    public void testRepositoryCustomMethods() {
        // Test custom repository methods
        String userId = "test-user-custom-" + UUID.randomUUID();
        String sessionId = "test-session-custom-" + UUID.randomUUID();

        // Create session
        ChatSession session = new ChatSession(sessionId, userId, "Custom Test");
        chatSessionRepository.save(session);

        // Test findByUserId
        var userSessions = chatSessionRepository.findByUserId(userId);
        assertFalse(userSessions.isEmpty());
        assertEquals(1, userSessions.size());

        // Test countByUserId
        long sessionCount = chatSessionRepository.countByUserId(userId);
        assertEquals(1, sessionCount);

        // Create message
        String messageId = "test-message-custom-" + UUID.randomUUID();
        ChatMessage message = new ChatMessage(messageId, sessionId, MessageRole.ASSISTANT, "Custom response");
        chatMessageRepository.save(message);

        // Test findBySessionId
        var sessionMessages = chatMessageRepository.findBySessionId(sessionId);
        assertFalse(sessionMessages.isEmpty());
        assertEquals(1, sessionMessages.size());

        // Test countBySessionId
        long messageCount = chatMessageRepository.countBySessionId(sessionId);
        assertEquals(1, messageCount);

        // Cleanup
        chatMessageRepository.deleteById(messageId);
        chatSessionRepository.deleteById(sessionId);
    }
}