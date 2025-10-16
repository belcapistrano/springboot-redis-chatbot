package com.example.chatbotcache.controller;

import com.example.chatbotcache.model.UserPreferences;
import com.example.chatbotcache.model.dto.PreferenceExistsResponse;
import com.example.chatbotcache.model.dto.PreferenceStatsResponse;
import com.example.chatbotcache.model.dto.UserPreferencesRequest;
import com.example.chatbotcache.model.dto.UserPreferencesResponse;
import com.example.chatbotcache.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
public class UserPreferencesController {

    @Autowired
    private UserPreferencesService userPreferencesService;

    /**
     * Get user preferences
     */
    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getUserPreferences(@PathVariable String userId) {
        try {
            UserPreferences preferences = userPreferencesService.getUserPreferences(userId);
            UserPreferencesResponse response = new UserPreferencesResponse(preferences);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user preferences
     */
    @PutMapping
    public ResponseEntity<UserPreferencesResponse> updateUserPreferences(
            @PathVariable String userId,
            @Valid @RequestBody UserPreferencesRequest request) {
        try {
            UserPreferences preferences = userPreferencesService.updatePreferences(
                userId,
                request.getModel(),
                request.getTemperature(),
                request.getMaxTokens(),
                request.getSystemPrompt()
            );

            // Update caching preferences if provided
            if (request.getEnableCaching() != null || request.getEnableLogging() != null) {
                preferences = userPreferencesService.updateCachingPreferences(
                    userId,
                    request.getEnableCaching(),
                    request.getEnableLogging()
                );
            }

            UserPreferencesResponse response = new UserPreferencesResponse(preferences);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset preferences to defaults
     */
    @PostMapping("/reset")
    public ResponseEntity<UserPreferencesResponse> resetPreferences(@PathVariable String userId) {
        try {
            UserPreferences preferences = userPreferencesService.resetToDefaults(userId);
            UserPreferencesResponse response = new UserPreferencesResponse(preferences);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete user preferences
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteUserPreferences(@PathVariable String userId) {
        try {
            boolean deleted = userPreferencesService.deleteUserPreferences(userId);

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
     * Check if user has custom preferences
     */
    @GetMapping("/exists")
    public ResponseEntity<PreferenceExistsResponse> hasCustomPreferences(@PathVariable String userId) {
        try {
            boolean hasCustom = userPreferencesService.hasCustomPreferences(userId);
            PreferenceExistsResponse response = new PreferenceExistsResponse(userId, hasCustom);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

// Global preferences endpoints
@RestController
@RequestMapping("/api/preferences")
class GlobalPreferencesController {

    @Autowired
    private UserPreferencesService userPreferencesService;

    /**
     * Get users by model
     */
    @GetMapping("/by-model/{model}")
    public ResponseEntity<List<UserPreferencesResponse>> getUsersByModel(@PathVariable String model) {
        try {
            List<UserPreferences> preferences = userPreferencesService.getUsersByModel(model);
            List<UserPreferencesResponse> responses = preferences.stream()
                    .map(UserPreferencesResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get users with caching enabled
     */
    @GetMapping("/caching-enabled")
    public ResponseEntity<List<UserPreferencesResponse>> getUsersWithCaching() {
        try {
            List<UserPreferences> preferences = userPreferencesService.getUsersWithCachingEnabled();
            List<UserPreferencesResponse> responses = preferences.stream()
                    .map(UserPreferencesResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get preference statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<PreferenceStatsResponse> getPreferenceStats() {
        try {
            long totalWithCaching = userPreferencesService.countUsersWithCaching();
            List<UserPreferences> recent = userPreferencesService.getRecentlyUpdated();

            PreferenceStatsResponse response = new PreferenceStatsResponse(totalWithCaching, recent.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}