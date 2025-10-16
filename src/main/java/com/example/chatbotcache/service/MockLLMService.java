package com.example.chatbotcache.service;

import com.example.chatbotcache.model.ChatMessage;
import com.example.chatbotcache.model.MessageRole;
import com.example.chatbotcache.model.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class MockLLMService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserPreferencesService userPreferencesService;

    @Autowired
    private CacheService cacheService;

    // Response patterns for different types of user input
    private final Map<String, List<String>> responsePatterns = Map.of(
        "greeting", Arrays.asList(
            "Hello! How can I help you today?",
            "Hi there! What can I do for you?",
            "Greetings! I'm here to assist you.",
            "Hello! It's great to chat with you.",
            "Hi! How are you doing today?"
        ),
        "question", Arrays.asList(
            "That's an interesting question. Let me think about that...",
            "Great question! Here's what I think:",
            "I'd be happy to help you with that.",
            "Let me consider that for a moment...",
            "That's a thoughtful inquiry. My perspective is:",
            "I can definitely help you understand that better."
        ),
        "goodbye", Arrays.asList(
            "Goodbye! Have a great day!",
            "See you later! Take care!",
            "Farewell! It was nice chatting with you.",
            "Until next time! Have a wonderful day!",
            "Goodbye! Feel free to come back anytime."
        ),
        "thanks", Arrays.asList(
            "You're very welcome!",
            "Happy to help!",
            "My pleasure!",
            "Glad I could assist you!",
            "You're welcome! Anything else I can help with?"
        ),
        "help", Arrays.asList(
            "I'm here to help! What do you need assistance with?",
            "Of course! How can I assist you?",
            "I'd be glad to help. What's on your mind?",
            "What can I help you with today?",
            "I'm ready to assist! What would you like to know?"
        ),
        "default", Arrays.asList(
            "I understand. Could you tell me more about that?",
            "That's helpful to know. Please continue.",
            "Interesting. What else would you like to discuss?",
            "I see. How can I help you with that?",
            "Thank you for sharing that. What would you like to explore next?",
            "I appreciate you telling me that. Is there anything specific you'd like help with?"
        )
    );

    // Pattern matching for different input types
    private final Map<String, Pattern> inputPatterns = Map.of(
        "greeting", Pattern.compile("(?i)\\b(hello|hi|hey|greetings|good morning|good afternoon|good evening)\\b"),
        "question", Pattern.compile("(?i)\\b(what|how|why|when|where|who|which|can you|could you|\\?)"),
        "goodbye", Pattern.compile("(?i)\\b(goodbye|bye|farewell|see you|talk soon|until next time)\\b"),
        "thanks", Pattern.compile("(?i)\\b(thank|thanks|appreciate|grateful)\\b"),
        "help", Pattern.compile("(?i)\\b(help|assist|support|guide)\\b")
    );

    // Topics for conversation tracking
    private final List<String> conversationTopics = Arrays.asList(
        "technology", "science", "programming", "data", "artificial intelligence",
        "web development", "databases", "cloud computing", "security", "general"
    );

    // Configuration for error simulation
    private final double errorRate = 0.05; // 5% chance of errors
    private final double timeoutRate = 0.02; // 2% chance of timeout
    private final double rateLimitRate = 0.03; // 3% chance of rate limit

    /**
     * Generate a response to user input with context awareness and caching
     */
    public String generateResponse(String sessionId, String userInput, UserPreferences preferences) {
        String model = preferences != null ? preferences.getModel() : "mock-llm-v1";
        Double temperature = preferences != null ? preferences.getTemperature() : 0.7;

        // Check cache first if caching is enabled
        if (preferences == null || preferences.getEnableCaching() == null || preferences.getEnableCaching()) {
            Map<String, Object> cachedResponse = cacheService.getCachedResponse(userInput, model, temperature);
            if (cachedResponse != null) {
                return (String) cachedResponse.get("response");
            }
        }

        // Simulate processing delay
        simulateDelay(preferences);

        // Simulate errors occasionally
        if (shouldSimulateError()) {
            throw new RuntimeException("Simulated LLM service error");
        }

        if (shouldSimulateTimeout()) {
            try {
                Thread.sleep(5000); // Simulate timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Request timeout");
        }

        if (shouldSimulateRateLimit()) {
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        }

        // Get conversation context
        List<ChatMessage> recentMessages = getConversationContext(sessionId);

        // Determine response pattern based on input
        String patternType = determinePatternType(userInput);

        // Generate context-aware response
        String response = generateContextAwareResponse(patternType, userInput, recentMessages, preferences);

        // Cache the response if caching is enabled
        if (preferences == null || preferences.getEnableCaching() == null || preferences.getEnableCaching()) {
            cacheService.cacheResponse(sessionId, userInput, response, model, temperature);
        }

        return response;
    }

    /**
     * Get conversation context from recent messages
     */
    public List<ChatMessage> getConversationContext(String sessionId) {
        return messageService.getRecentMessages(sessionId, 10);
    }

    /**
     * Determine the pattern type based on user input
     */
    private String determinePatternType(String input) {
        for (Map.Entry<String, Pattern> entry : inputPatterns.entrySet()) {
            if (entry.getValue().matcher(input).find()) {
                return entry.getKey();
            }
        }
        return "default";
    }

    /**
     * Generate context-aware response based on conversation history
     */
    private String generateContextAwareResponse(String patternType, String userInput,
                                              List<ChatMessage> context, UserPreferences preferences) {
        List<String> baseResponses = responsePatterns.get(patternType);
        String baseResponse = getRandomResponse(baseResponses);

        // Add context awareness
        if (!context.isEmpty()) {
            baseResponse = addContextualElements(baseResponse, context, userInput);
        }

        // Add personalization based on preferences
        baseResponse = personalizeResponse(baseResponse, preferences);

        // Add conversation length variation
        baseResponse = addLengthVariation(baseResponse, context.size());

        return baseResponse;
    }

    /**
     * Add contextual elements based on conversation history
     */
    private String addContextualElements(String response, List<ChatMessage> context, String userInput) {
        StringBuilder contextualResponse = new StringBuilder(response);

        // Reference previous topics occasionally
        if (ThreadLocalRandom.current().nextDouble() < 0.3 && context.size() > 2) {
            String previousTopic = extractTopic(context);
            if (previousTopic != null) {
                contextualResponse.append(" Building on our earlier discussion about ")
                    .append(previousTopic).append("...");
            }
        }

        // Reference user's previous questions
        if (ThreadLocalRandom.current().nextDouble() < 0.4) {
            Optional<ChatMessage> lastUserMessage = context.stream()
                .filter(msg -> msg.getRole() == MessageRole.USER)
                .findFirst();

            if (lastUserMessage.isPresent() && !lastUserMessage.get().getContent().equals(userInput)) {
                contextualResponse.append(" I remember you asked about ")
                    .append(summarizeMessage(lastUserMessage.get().getContent())).append(" earlier.");
            }
        }

        return contextualResponse.toString();
    }

    /**
     * Personalize response based on user preferences
     */
    private String personalizeResponse(String response, UserPreferences preferences) {
        if (preferences == null) {
            return response;
        }

        // Adjust formality based on temperature setting
        if (preferences.getTemperature() != null) {
            if (preferences.getTemperature() > 0.8) {
                // High temperature = more casual
                response = makeCasual(response);
            } else if (preferences.getTemperature() < 0.3) {
                // Low temperature = more formal
                response = makeFormal(response);
            }
        }

        return response;
    }

    /**
     * Add response variation based on conversation length
     */
    private String addLengthVariation(String response, int messageCount) {
        if (messageCount > 20) {
            // Long conversation - add familiarity
            return "As we've been chatting, " + response.toLowerCase();
        } else if (messageCount > 10) {
            // Medium conversation - add continuity
            return "Continuing our conversation, " + response.toLowerCase();
        } else if (messageCount < 3) {
            // New conversation - be welcoming
            return "Welcome! " + response;
        }

        return response;
    }

    /**
     * Estimate token count for the response
     */
    public int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // Simple estimation: ~4 characters per token
        return Math.max(1, text.length() / 4);
    }

    /**
     * Get current conversation topic
     */
    public String getCurrentTopic(String sessionId) {
        List<ChatMessage> context = getConversationContext(sessionId);
        return extractTopic(context);
    }

    /**
     * Simulate realistic processing delays
     */
    private void simulateDelay(UserPreferences preferences) {
        int baseDelay = 100; // Base delay in ms
        int maxDelay = 2000; // Max delay in ms

        // Adjust delay based on response complexity (temperature as proxy)
        if (preferences != null && preferences.getTemperature() != null) {
            double complexity = preferences.getTemperature();
            baseDelay = (int) (baseDelay + (complexity * 500));
        }

        int delay = ThreadLocalRandom.current().nextInt(baseDelay, Math.min(maxDelay, baseDelay + 1000));

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Helper methods

    private String getRandomResponse(List<String> responses) {
        if (responses.isEmpty()) {
            return "I understand.";
        }
        return responses.get(ThreadLocalRandom.current().nextInt(responses.size()));
    }

    private String extractTopic(List<ChatMessage> context) {
        // Simple topic extraction based on keywords
        Map<String, Integer> topicCounts = new HashMap<>();

        for (ChatMessage message : context) {
            String content = message.getContent().toLowerCase();
            for (String topic : conversationTopics) {
                if (content.contains(topic)) {
                    topicCounts.merge(topic, 1, Integer::sum);
                }
            }
        }

        return topicCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("general topics");
    }

    private String summarizeMessage(String message) {
        // Simple message summarization
        if (message.length() > 50) {
            return message.substring(0, 47) + "...";
        }
        return message;
    }

    private String makeCasual(String response) {
        return response.replace("I would", "I'd")
                      .replace("you are", "you're")
                      .replace("that is", "that's")
                      .replace("Hello!", "Hey!")
                      .replace("Greetings!", "Hi there!");
    }

    private String makeFormal(String response) {
        return response.replace("Hey!", "Good day!")
                      .replace("Hi there!", "Hello!")
                      .replace("I'd", "I would")
                      .replace("you're", "you are")
                      .replace("that's", "that is");
    }

    private boolean shouldSimulateError() {
        return ThreadLocalRandom.current().nextDouble() < errorRate;
    }

    private boolean shouldSimulateTimeout() {
        return ThreadLocalRandom.current().nextDouble() < timeoutRate;
    }

    private boolean shouldSimulateRateLimit() {
        return ThreadLocalRandom.current().nextDouble() < rateLimitRate;
    }

    /**
     * Generate response with metadata
     */
    public Map<String, Object> generateResponseWithMetadata(String sessionId, String userInput, UserPreferences preferences) {
        long startTime = System.currentTimeMillis();

        try {
            String response = generateResponse(sessionId, userInput, preferences);
            long processingTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("tokenCount", estimateTokenCount(response));
            result.put("processingTimeMs", processingTime);
            result.put("model", preferences != null ? preferences.getModel() : "mock-llm-v1");
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("topic", getCurrentTopic(sessionId));

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("processingTimeMs", processingTime);
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return result;
        }
    }
}