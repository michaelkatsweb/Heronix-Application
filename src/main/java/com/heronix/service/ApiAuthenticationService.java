package com.heronix.service;

import com.heronix.model.domain.AuditLog;
import com.heronix.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Authentication Service
 *
 * Provides authentication and token management functionality for the Heronix REST API.
 * Handles user login, JWT token generation, token refresh, and logout.
 *
 * Authentication Flow:
 * 1. User submits credentials (username/password)
 * 2. Credentials validated via Spring Security AuthenticationManager
 * 3. JWT access token and refresh token generated
 * 4. Tokens returned to client
 * 5. Client uses access token for API requests
 * 6. Access token expires after 1 hour
 * 7. Client uses refresh token to get new access token
 * 8. Refresh token expires after 7 days
 *
 * Token Management:
 * - Access tokens: Short-lived (1 hour), used for API requests
 * - Refresh tokens: Long-lived (7 days), used to obtain new access tokens
 * - Token blacklist: Revoked tokens stored in memory (production: Redis)
 *
 * Security Features:
 * - Password validation via Spring Security
 * - Token revocation on logout
 * - Refresh token rotation (optional)
 * - Security audit logging
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Service
@Slf4j
public class ApiAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final SecurityAuditService auditService;
    private final UserDetailsService userDetailsService;

    @Autowired(required = false)
    private AuditLogRepository auditLogRepository;

    @Value("${heronix.security.token-blacklist.persist-to-db:true}")
    private boolean persistBlacklistToDb;

    // In-memory token blacklist with TTL support
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    // Token blacklist cleanup interval: 1 hour
    private static final long CLEANUP_INTERVAL_MS = 3600000;

    @Autowired
    public ApiAuthenticationService(AuthenticationManager authenticationManager,
                                    JwtTokenService jwtTokenService,
                                    SecurityAuditService auditService,
                                    UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.auditService = auditService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Authenticate user and generate JWT tokens
     *
     * @param username User's username
     * @param password User's password
     * @param ipAddress Client IP address (for audit logging)
     * @param userAgent Client user agent
     * @return Authentication result with tokens
     */
    public AuthenticationResult login(String username, String password,
                                     String ipAddress, String userAgent) {
        try {
            // Authenticate user via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // Extract user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userId = userDetails.getUsername();

            // Extract roles from granted authorities
            String[] roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .toArray(String[]::new);

            // Extract permissions (authorities that aren't roles)
            String[] permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .toArray(String[]::new);

            // Generate JWT tokens
            String accessToken = jwtTokenService.generateAccessToken(userId, roles, permissions);
            String refreshToken = jwtTokenService.generateRefreshToken(userId);

            // Audit logging
            auditService.logAuthenticationSuccess(userId, "PASSWORD", ipAddress, userAgent);

            log.info("User authenticated successfully: {} from IP: {}", userId, ipAddress);

            return AuthenticationResult.success(userId, accessToken, refreshToken, roles);

        } catch (AuthenticationException e) {
            // Audit logging
            auditService.logAuthenticationFailure(username, "PASSWORD", ipAddress,
                "Invalid credentials");

            log.warn("Authentication failed for user: {} from IP: {}", username, ipAddress);

            return AuthenticationResult.failure("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken Refresh token
     * @param ipAddress Client IP address
     * @return New access token
     */
    public AuthenticationResult refreshAccessToken(String refreshToken, String ipAddress) {
        try {
            // Validate refresh token
            if (!jwtTokenService.validateToken(refreshToken)) {
                log.warn("Invalid or expired refresh token from IP: {}", ipAddress);
                return AuthenticationResult.failure("Invalid or expired refresh token");
            }

            // Check if token is blacklisted
            if (isTokenBlacklisted(refreshToken)) {
                log.warn("Attempt to use blacklisted refresh token from IP: {}", ipAddress);
                return AuthenticationResult.failure("Token has been revoked");
            }

            // Extract user ID from refresh token
            String userId = jwtTokenService.getUserIdFromToken(refreshToken);

            // Load user details to get current roles/permissions
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

            // Extract roles and permissions
            String[] roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5))
                .toArray(String[]::new);

            String[] permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .toArray(String[]::new);

            // Generate new access token
            String newAccessToken = jwtTokenService.generateAccessToken(userId, roles, permissions);

            log.info("Access token refreshed for user: {} from IP: {}", userId, ipAddress);

            return AuthenticationResult.success(userId, newAccessToken, refreshToken, roles);

        } catch (Exception e) {
            log.error("Error refreshing token from IP: {}: {}", ipAddress, e.getMessage());
            return AuthenticationResult.failure("Failed to refresh token");
        }
    }

    /**
     * Logout user and blacklist tokens
     *
     * @param accessToken Access token to revoke
     * @param refreshToken Refresh token to revoke
     * @param userId User ID
     * @param ipAddress Client IP address
     */
    public void logout(String accessToken, String refreshToken, String userId, String ipAddress) {
        // Add tokens to blacklist
        if (accessToken != null && !accessToken.isBlank()) {
            blacklistToken(accessToken);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            blacklistToken(refreshToken);
        }

        log.info("User logged out: {} from IP: {}", userId, ipAddress);

        // Note: In production, use Redis with TTL matching token expiration
        // to automatically remove expired tokens from blacklist
    }

    /**
     * Validate access token
     *
     * @param accessToken Access token to validate
     * @return true if token is valid and not blacklisted
     */
    public boolean validateAccessToken(String accessToken) {
        return jwtTokenService.validateToken(accessToken)
            && !isTokenBlacklisted(accessToken);
    }

    /**
     * Get user information from access token
     *
     * @param accessToken Access token
     * @return Map containing user information
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        if (!validateAccessToken(accessToken)) {
            return null;
        }

        String userId = jwtTokenService.getUserIdFromToken(accessToken);
        String[] roles = jwtTokenService.getRolesFromToken(accessToken);
        String[] permissions = jwtTokenService.getPermissionsFromToken(accessToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("roles", roles);
        userInfo.put("permissions", permissions);
        userInfo.put("expiresAt", jwtTokenService.getExpirationFromToken(accessToken));

        return userInfo;
    }

    /**
     * Add token to blacklist with TTL tracking
     *
     * Uses in-memory map with expiration tracking, plus optional database persistence
     * for recovery across application restarts.
     *
     * For high-volume production, consider Redis:
     * spring.redis.host=localhost
     * spring.redis.port=6379
     */
    private void blacklistToken(String token) {
        try {
            // Get token expiration for TTL
            LocalDateTime expiration = LocalDateTime.now().plusHours(24); // Default 24h if can't parse
            try {
                java.util.Date expDate = jwtTokenService.getExpirationFromToken(token);
                if (expDate != null) {
                    expiration = expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                }
            } catch (Exception e) {
                log.debug("Could not parse token expiration, using default TTL");
            }

            // Add to in-memory blacklist with expiration
            blacklistedTokens.put(token, expiration);
            log.debug("Token blacklisted until {}", expiration);

            // Optionally persist to database for recovery across restarts
            if (persistBlacklistToDb && auditLogRepository != null) {
                try {
                    AuditLog auditEntry = new AuditLog();
                    auditEntry.setAction(AuditLog.AuditAction.LOGOUT);
                    auditEntry.setEntityType("JWT_TOKEN");
                    auditEntry.setDetails("Token blacklisted until " + expiration +
                            " | TokenPrefix=" + token.substring(0, Math.min(20, token.length())) + "...");
                    auditEntry.setTimestamp(LocalDateTime.now());
                    auditEntry.setUsername("SYSTEM");
                    auditEntry.setSuccess(true);
                    auditLogRepository.save(auditEntry);
                } catch (Exception e) {
                    log.warn("Could not persist token blacklist to database: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error blacklisting token: {}", e.getMessage());
            // Still add to in-memory as fallback
            blacklistedTokens.put(token, LocalDateTime.now().plusHours(24));
        }
    }

    /**
     * Check if token is blacklisted
     *
     * Checks in-memory map and cleans up expired entries
     */
    private boolean isTokenBlacklisted(String token) {
        LocalDateTime expiration = blacklistedTokens.get(token);

        if (expiration == null) {
            return false;
        }

        // Check if blacklist entry has expired
        if (LocalDateTime.now().isAfter(expiration)) {
            blacklistedTokens.remove(token);
            log.debug("Removed expired blacklist entry for token");
            return false;
        }

        log.debug("Token is blacklisted until {}", expiration);
        return true;
    }

    /**
     * Cleanup expired tokens from blacklist (runs every hour)
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredBlacklistEntries() {
        LocalDateTime now = LocalDateTime.now();
        int removedCount = 0;

        var iterator = blacklistedTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now.isAfter(entry.getValue())) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} expired token blacklist entries", removedCount);
        }
    }

    /**
     * Get current blacklist size (for monitoring)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Authentication result containing tokens and user info
     */
    public record AuthenticationResult(
        boolean success,
        String userId,
        String accessToken,
        String refreshToken,
        String[] roles,
        String errorMessage
    ) {
        public static AuthenticationResult success(String userId, String accessToken,
                                                   String refreshToken, String[] roles) {
            return new AuthenticationResult(true, userId, accessToken, refreshToken,
                roles, null);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, null, null, null, null, errorMessage);
        }
    }
}
