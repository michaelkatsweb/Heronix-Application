package com.heronix.client;

import com.heronix.api.dto.LoginRequestDTO;
import com.heronix.api.dto.TokenRefreshRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication API Service
 *
 * Service layer for authentication operations from JavaFX UI.
 * Handles login, logout, token refresh, and session management.
 *
 * Authentication Flow:
 * 1. User enters credentials in login screen
 * 2. Service calls POST /api/auth/login
 * 3. On success, tokens stored via TokenManager
 * 4. Access token auto-injected into subsequent requests
 * 5. Refresh token used to get new access token when expired
 *
 * Error Handling:
 * - Returns AuthenticationResult with success/failure status
 * - Logs errors for debugging
 * - Clears stored tokens on logout
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 46 - Frontend Dashboard Integration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationApiService {

    private final RestTemplate restTemplate;
    private final TokenManager tokenManager;

    /**
     * Authentication result wrapper
     */
    public record AuthenticationResult(
        boolean success,
        String errorMessage,
        String userId,
        String[] roles
    ) {
        public static AuthenticationResult success(String userId, String[] roles) {
            return new AuthenticationResult(true, null, userId, roles);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, errorMessage, null, null);
        }
    }

    /**
     * Authenticate user with username and password
     *
     * @param username User's username
     * @param password User's password
     * @return Authentication result
     */
    public AuthenticationResult login(String username, String password) {
        try {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setUsername(username);
            request.setPassword(password);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/api/auth/login",
                    request,
                    Map.class
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                if (Boolean.TRUE.equals(body.get("success"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) body.get("data");

                    if (data != null) {
                        String accessToken = (String) data.get("accessToken");
                        String refreshToken = (String) data.get("refreshToken");
                        String userId = (String) data.get("userId");

                        @SuppressWarnings("unchecked")
                        java.util.List<String> rolesList = (java.util.List<String>) data.get("roles");
                        String[] roles = rolesList != null ? rolesList.toArray(new String[0]) : new String[0];

                        // Store tokens with default 1 hour expiry for access token
                        tokenManager.saveTokens(accessToken, refreshToken, 3600, userId);

                        log.info("User logged in successfully: {}", userId);
                        return AuthenticationResult.success(userId, roles);
                    }
                }

                String errorMsg = (String) body.get("error");
                log.warn("Login failed: {}", errorMsg);
                return AuthenticationResult.failure(errorMsg != null ? errorMsg : "Login failed");
            }

            log.warn("Login failed with status: {}", response.getStatusCode());
            return AuthenticationResult.failure("Login failed with status: " + response.getStatusCode());

        } catch (RestClientException e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return AuthenticationResult.failure("Unable to connect to server. Please check your connection.");
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return AuthenticationResult.failure("An unexpected error occurred during login.");
        }
    }

    /**
     * Refresh access token using refresh token
     *
     * @return true if refresh successful
     */
    public boolean refreshAccessToken() {
        try {
            String refreshToken = tokenManager.getRefreshToken();
            if (refreshToken == null) {
                log.warn("No refresh token available");
                return false;
            }

            TokenRefreshRequestDTO request = new TokenRefreshRequestDTO();
            request.setRefreshToken(refreshToken);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/api/auth/refresh",
                    request,
                    Map.class
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                if (Boolean.TRUE.equals(body.get("success"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) body.get("data");

                    if (data != null) {
                        String newAccessToken = (String) data.get("accessToken");
                        String newRefreshToken = (String) data.get("refreshToken");
                        String userId = (String) data.get("userId");

                        // Update stored tokens
                        tokenManager.saveTokens(newAccessToken, newRefreshToken, 3600, userId);

                        log.debug("Access token refreshed successfully");
                        return true;
                    }
                }
            }

            log.warn("Token refresh failed: {}", response.getStatusCode());
            return false;

        } catch (RestClientException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error refreshing token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Logout user and clear tokens
     *
     * @return true if logout successful
     */
    public boolean logout() {
        try {
            // Try to call logout endpoint to invalidate tokens on server
            String accessToken = tokenManager.getAccessToken();
            if (accessToken != null) {
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("refreshToken", tokenManager.getRefreshToken());

                    restTemplate.postForEntity(
                        "/api/auth/logout",
                        params,
                        Map.class
                    );
                } catch (Exception e) {
                    log.warn("Error calling logout endpoint: {}", e.getMessage());
                    // Continue with local logout even if server call fails
                }
            }

            // Clear local tokens
            tokenManager.clearTokens();
            log.info("User logged out successfully");
            return true;

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            // Still clear tokens even if there was an error
            tokenManager.clearTokens();
            return true; // Return true since local logout succeeded
        }
    }

    /**
     * Check if user is currently authenticated
     *
     * @return true if authenticated with valid token
     */
    public boolean isAuthenticated() {
        return tokenManager.isAuthenticated();
    }

    /**
     * Get current user ID
     *
     * @return User ID or null if not authenticated
     */
    public String getCurrentUserId() {
        return tokenManager.getUserId();
    }

    /**
     * Check API connectivity
     *
     * @return true if API is reachable
     */
    public boolean checkApiConnection() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("API health check failed: {}", e.getMessage());
            return false;
        }
    }
}
