package com.example.chatbotcache.repository;

import com.example.chatbotcache.model.UserPreferences;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends CrudRepository<UserPreferences, String> {

    /**
     * Find preferences by model name
     */
    List<UserPreferences> findByModel(String model);

    /**
     * Find preferences by temperature range
     */
    List<UserPreferences> findByTemperatureBetween(Double minTemp, Double maxTemp);

    /**
     * Find preferences with caching enabled
     */
    List<UserPreferences> findByEnableCachingTrue();

    /**
     * Find preferences with logging enabled
     */
    List<UserPreferences> findByEnableLoggingTrue();

    /**
     * Find preferences by caching and logging settings
     */
    List<UserPreferences> findByEnableCachingAndEnableLogging(Boolean caching, Boolean logging);

    /**
     * Find preferences updated after a specific date
     */
    List<UserPreferences> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Find preferences created after a specific date
     */
    List<UserPreferences> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find preferences by max tokens range
     */
    List<UserPreferences> findByMaxTokensBetween(Integer minTokens, Integer maxTokens);

    /**
     * Find preferences by context window size
     */
    List<UserPreferences> findByContextWindow(Integer contextWindow);

    /**
     * Find preferences with context window greater than specified value
     */
    List<UserPreferences> findByContextWindowGreaterThan(Integer contextWindow);

    /**
     * Find preferences containing specific text in system prompt
     */
    List<UserPreferences> findBySystemPromptContainingIgnoreCase(String text);

    /**
     * Check if preferences exist for a user
     */
    boolean existsByUserId(String userId);

    /**
     * Find preferences that haven't been updated for a while
     */
    List<UserPreferences> findByUpdatedAtBefore(LocalDateTime date);

    /**
     * Count preferences by model
     */
    long countByModel(String model);

    /**
     * Count preferences with caching enabled
     */
    long countByEnableCachingTrue();

    /**
     * Find preferences ordered by last update
     */
    List<UserPreferences> findAllByOrderByUpdatedAtDesc();

    /**
     * Find preferences ordered by creation date
     */
    List<UserPreferences> findAllByOrderByCreatedAtDesc();

    /**
     * Delete preferences that haven't been updated for a long time
     */
    void deleteByUpdatedAtBefore(LocalDateTime date);

    /**
     * Get preferences with default fallback
     */
    default UserPreferences findByUserIdOrDefault(String userId) {
        Optional<UserPreferences> preferences = findById(userId);
        return preferences.orElse(new UserPreferences(userId));
    }

    /**
     * Update preferences timestamp
     */
    default UserPreferences saveAndUpdateTimestamp(UserPreferences preferences) {
        preferences.updateTimestamp();
        return save(preferences);
    }
}