package com.heronix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

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
@RequiredArgsConstructor
public class ApiAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final SecurityAuditService auditService;
    private final UserDetailsService userDetailsService;

    // In-memory token blacklist - production should use Redis
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

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
     * Add token to blacklist
     *
     * STUB IMPLEMENTATION: Uses in-memory HashSet
     *
     * Production Redis Setup:
     * =======================
     *
     * 1. Add Spring Data Redis dependency to pom.xml:
     * <dependency>
     *     <groupId>org.springframework.boot</groupId>
     *     <artifactId>spring-boot-starter-data-redis</artifactId>
     * </dependency>
     *
     * 2. Configure Redis in application.properties:
     * spring.redis.host=localhost
     * spring.redis.port=6379
     * spring.redis.password=yourRedisPassword
     * spring.redis.timeout=60000
     * spring.redis.jedis.pool.max-active=8
     * spring.redis.jedis.pool.max-idle=8
     * spring.redis.jedis.pool.min-idle=0
     *
     * 3. Inject RedisTemplate:
     * @Autowired
     * private RedisTemplate<String, String> redisTemplate;
     *
     * 4. Production implementation:
     * long ttl = jwtTokenService.getExpirationFromToken(token)
     *     .toInstant().getEpochSecond() - System.currentTimeMillis() / 1000;
     * redisTemplate.opsForValue().set("blacklist:" + token, "1",
     *     ttl, TimeUnit.SECONDS);
     *
     * Alternative: Use @Cacheable annotation with Redis:
     * @Configuration
     * @EnableCaching
     * public class CacheConfig {
     *     @Bean
     *     public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
     *         return RedisCacheManager.builder(factory).build();
     *     }
     * }
     */
    private void blacklistToken(String token) {
        log.debug("Blacklisting token (in-memory mode): {}", token.substring(0, Math.min(10, token.length())) + "...");
        blacklistedTokens.add(token);

        // Stub: In-memory storage
        // Production: Use Redis as documented above
    }

    /**
     * Check if token is blacklisted
     *
     * STUB IMPLEMENTATION: Checks in-memory HashSet
     *
     * Production implementation with Redis:
     * return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
     *
     * Or with caching:
     * @Cacheable(value = "tokenBlacklist", key = "#token")
     * public boolean isTokenBlacklisted(String token) {
     *     return redisTemplate.hasKey("blacklist:" + token);
     * }
     */
    private boolean isTokenBlacklisted(String token) {
        boolean isBlacklisted = blacklistedTokens.contains(token);
        log.debug("Token blacklist check (in-memory mode): {}", isBlacklisted);

        // Stub: In-memory storage
        // Production: Use Redis as documented in blacklistToken method
        return isBlacklisted;
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
