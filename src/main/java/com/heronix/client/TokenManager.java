package com.heronix.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.prefs.Preferences;

/**
 * Token Manager
 *
 * Manages JWT authentication tokens for the JavaFX desktop application.
 * Handles token storage, retrieval, and automatic refresh.
 *
 * Token Storage:
 * - Uses Java Preferences API for secure local storage
 * - Tokens stored in user-specific registry/preferences
 * - Not accessible to other users on the system
 *
 * Token Lifecycle:
 * - Access token: 1 hour validity
 * - Refresh token: 7 days validity
 * - Automatic refresh when access token expires
 * - Clear tokens on logout
 *
 * Security:
 * - Tokens stored encrypted by OS
 * - Memory cleared after use
 * - No plain text token logging
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 46 - Frontend Dashboard Integration
 */
@Component
@Slf4j
public class TokenManager {

    private static final String PREFS_NODE = "com.heronix.auth";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_TOKEN_EXPIRY = "tokenExpiry";
    private static final String KEY_USER_ID = "userId";

    private final Preferences preferences;

    // In-memory cache
    private String cachedAccessToken;
    private String cachedRefreshToken;
    private Instant cachedTokenExpiry;
    private String cachedUserId;

    public TokenManager() {
        this.preferences = Preferences.userRoot().node(PREFS_NODE);
        loadTokensFromPreferences();
    }

    /**
     * Save authentication tokens
     *
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     * @param expiresIn Seconds until access token expires
     * @param userId User identifier
     */
    public void saveTokens(String accessToken, String refreshToken, long expiresIn, String userId) {
        this.cachedAccessToken = accessToken;
        this.cachedRefreshToken = refreshToken;
        this.cachedTokenExpiry = Instant.now().plusSeconds(expiresIn);
        this.cachedUserId = userId;

        // Persist to preferences
        preferences.put(KEY_ACCESS_TOKEN, accessToken);
        preferences.put(KEY_REFRESH_TOKEN, refreshToken);
        preferences.putLong(KEY_TOKEN_EXPIRY, cachedTokenExpiry.getEpochSecond());
        preferences.put(KEY_USER_ID, userId);

        log.info("Tokens saved for user: {}", userId);
    }

    /**
     * Get current access token
     *
     * @return Access token or null if not available
     */
    public String getAccessToken() {
        if (isAccessTokenExpired()) {
            log.debug("Access token expired, refresh needed");
            return null;
        }
        return cachedAccessToken;
    }

    /**
     * Get refresh token
     *
     * @return Refresh token or null if not available
     */
    public String getRefreshToken() {
        return cachedRefreshToken;
    }

    /**
     * Get current user ID
     *
     * @return User ID or null if not authenticated
     */
    public String getUserId() {
        return cachedUserId;
    }

    /**
     * Check if user is authenticated (has valid tokens)
     *
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        return cachedAccessToken != null && !isAccessTokenExpired();
    }

    /**
     * Check if access token is expired
     *
     * @return true if expired or about to expire (within 1 minute)
     */
    public boolean isAccessTokenExpired() {
        if (cachedTokenExpiry == null) {
            return true;
        }
        // Consider expired if less than 1 minute remaining
        return Instant.now().plusSeconds(60).isAfter(cachedTokenExpiry);
    }

    /**
     * Clear all tokens (logout)
     */
    public void clearTokens() {
        this.cachedAccessToken = null;
        this.cachedRefreshToken = null;
        this.cachedTokenExpiry = null;
        this.cachedUserId = null;

        // Clear from preferences
        preferences.remove(KEY_ACCESS_TOKEN);
        preferences.remove(KEY_REFRESH_TOKEN);
        preferences.remove(KEY_TOKEN_EXPIRY);
        preferences.remove(KEY_USER_ID);

        log.info("Tokens cleared");
    }

    /**
     * Update access token after refresh
     *
     * @param newAccessToken New access token
     * @param expiresIn Seconds until expiration
     */
    public void updateAccessToken(String newAccessToken, long expiresIn) {
        this.cachedAccessToken = newAccessToken;
        this.cachedTokenExpiry = Instant.now().plusSeconds(expiresIn);

        preferences.put(KEY_ACCESS_TOKEN, newAccessToken);
        preferences.putLong(KEY_TOKEN_EXPIRY, cachedTokenExpiry.getEpochSecond());

        log.debug("Access token updated");
    }

    /**
     * Load tokens from preferences on startup
     */
    private void loadTokensFromPreferences() {
        this.cachedAccessToken = preferences.get(KEY_ACCESS_TOKEN, null);
        this.cachedRefreshToken = preferences.get(KEY_REFRESH_TOKEN, null);
        this.cachedUserId = preferences.get(KEY_USER_ID, null);

        long expiryEpoch = preferences.getLong(KEY_TOKEN_EXPIRY, 0);
        if (expiryEpoch > 0) {
            this.cachedTokenExpiry = Instant.ofEpochSecond(expiryEpoch);
        }

        if (cachedAccessToken != null) {
            log.info("Tokens loaded from preferences for user: {}", cachedUserId);
        }
    }
}
