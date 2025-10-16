package com.example.chatbotcache.service;

import com.example.chatbotcache.model.UserPreferences;
import com.example.chatbotcache.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserPreferencesService {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    /**
     * Get user preferences by user ID, with default fallback
     */
    public UserPreferences getUserPreferences(String userId) {
        validateUserId(userId);
        return userPreferencesRepository.findByUserIdOrDefault(userId);
    }

    /**
     * Get user preferences by user ID, returning Optional
     */
    public Optional<UserPreferences> findUserPreferences(String userId) {
        validateUserId(userId);
        return userPreferencesRepository.findById(userId);
    }

    /**
     * Save or update user preferences
     */
    public UserPreferences saveUserPreferences(UserPreferences preferences) {
        validatePreferences(preferences);
        return userPreferencesRepository.saveAndUpdateTimestamp(preferences);
    }

    /**
     * Update specific preference fields
     */
    public UserPreferences updatePreferences(String userId, String model, Double temperature,
                                           Integer maxTokens, String systemPrompt) {
        UserPreferences preferences = getUserPreferences(userId);

        if (model != null) preferences.setModel(model);
        if (temperature != null) preferences.setTemperature(temperature);
        if (maxTokens != null) preferences.setMaxTokens(maxTokens);
        if (systemPrompt != null) preferences.setSystemPrompt(systemPrompt);

        return saveUserPreferences(preferences);
    }

    /**
     * Update caching preferences
     */
    public UserPreferences updateCachingPreferences(String userId, Boolean enableCaching, Boolean enableLogging) {
        UserPreferences preferences = getUserPreferences(userId);

        if (enableCaching != null) preferences.setEnableCaching(enableCaching);
        if (enableLogging != null) preferences.setEnableLogging(enableLogging);

        return saveUserPreferences(preferences);
    }

    /**
     * Reset preferences to defaults
     */
    public UserPreferences resetToDefaults(String userId) {
        validateUserId(userId);
        UserPreferences defaults = new UserPreferences(userId);
        return userPreferencesRepository.save(defaults);
    }

    /**
     * Delete user preferences
     */
    public boolean deleteUserPreferences(String userId) {
        validateUserId(userId);

        if (userPreferencesRepository.existsById(userId)) {
            userPreferencesRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * Check if user has custom preferences
     */
    public boolean hasCustomPreferences(String userId) {
        validateUserId(userId);
        return userPreferencesRepository.existsByUserId(userId);
    }

    /**
     * Get all users with specific model preference
     */
    public List<UserPreferences> getUsersByModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        return userPreferencesRepository.findByModel(model);
    }

    /**
     * Get users with caching enabled
     */
    public List<UserPreferences> getUsersWithCachingEnabled() {
        return userPreferencesRepository.findByEnableCachingTrue();
    }

    /**
     * Get users with logging enabled
     */
    public List<UserPreferences> getUsersWithLoggingEnabled() {
        return userPreferencesRepository.findByEnableLoggingTrue();
    }

    /**
     * Count users by model
     */
    public long countUsersByModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        return userPreferencesRepository.countByModel(model);
    }

    /**
     * Count users with caching enabled
     */
    public long countUsersWithCaching() {
        return userPreferencesRepository.countByEnableCachingTrue();
    }

    /**
     * Get recently updated preferences
     */
    public List<UserPreferences> getRecentlyUpdated() {
        return userPreferencesRepository.findAllByOrderByUpdatedAtDesc();
    }

    // Private helper methods

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }

    private void validatePreferences(UserPreferences preferences) {
        if (preferences == null) {
            throw new IllegalArgumentException("User preferences cannot be null");
        }

        if (preferences.getUserId() == null || preferences.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID in preferences cannot be null or empty");
        }

        // Validate temperature range
        if (preferences.getTemperature() != null) {
            double temp = preferences.getTemperature();
            if (temp < 0.0 || temp > 2.0) {
                throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
            }
        }

        // Validate max tokens
        if (preferences.getMaxTokens() != null) {
            int tokens = preferences.getMaxTokens();
            if (tokens < 1 || tokens > 8192) {
                throw new IllegalArgumentException("Max tokens must be between 1 and 8192");
            }
        }

        // Validate context window
        if (preferences.getContextWindow() != null) {
            int contextWindow = preferences.getContextWindow();
            if (contextWindow < 1 || contextWindow > 32768) {
                throw new IllegalArgumentException("Context window must be between 1 and 32768");
            }
        }
    }
}