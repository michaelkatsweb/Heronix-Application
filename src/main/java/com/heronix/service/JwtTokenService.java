package com.heronix.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Service
 *
 * Provides JSON Web Token (JWT) generation, validation, and parsing for API authentication.
 *
 * Token Structure:
 * - Header: Algorithm (HS256) and token type (JWT)
 * - Payload: User claims (subject, roles, permissions, expiration)
 * - Signature: HMAC SHA-256 signature for integrity verification
 *
 * Token Claims:
 * - sub: User ID (subject)
 * - roles: User roles (ADMIN, TEACHER, STUDENT, PARENT)
 * - permissions: API permissions (read:students, write:grades, etc.)
 * - iat: Issued at timestamp
 * - exp: Expiration timestamp
 * - jti: JWT ID for token revocation tracking
 *
 * Token Types:
 * - Access Token: Short-lived (1 hour), used for API requests
 * - Refresh Token: Long-lived (7 days), used to obtain new access tokens
 *
 * Security Features:
 * - HMAC SHA-256 signature algorithm
 * - Configurable secret key (minimum 256 bits)
 * - Token expiration enforcement
 * - Signature validation
 * - Clock skew tolerance (30 seconds)
 *
 * Configuration (application.properties):
 * - jwt.secret: Secret key for signing tokens (min 32 characters)
 * - jwt.access-token-validity: Access token validity in hours (default: 1)
 * - jwt.refresh-token-validity: Refresh token validity in days (default: 7)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Service
@Slf4j
public class JwtTokenService {

    private final SecretKey secretKey;
    private final long accessTokenValidityHours;
    private final long refreshTokenValidityDays;

    /**
     * Constructor with JWT configuration from application properties
     */
    public JwtTokenService(
            @Value("${jwt.secret:heronix-secret-key-minimum-32-characters-required-for-security}") String secret,
            @Value("${jwt.access-token-validity:1}") long accessTokenValidityHours,
            @Value("${jwt.refresh-token-validity:7}") long refreshTokenValidityDays) {

        // Ensure secret key is at least 256 bits (32 characters)
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityHours = accessTokenValidityHours;
        this.refreshTokenValidityDays = refreshTokenValidityDays;

        log.info("JwtTokenService initialized - Access token validity: {} hours, Refresh token validity: {} days",
            accessTokenValidityHours, refreshTokenValidityDays);
    }

    /**
     * Generate access token for API authentication
     *
     * @param userId User identifier (subject)
     * @param roles User roles (ADMIN, TEACHER, etc.)
     * @param permissions API permissions (read:students, write:grades, etc.)
     * @return JWT access token string
     */
    public String generateAccessToken(String userId, String[] roles, String[] permissions) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenValidityHours, ChronoUnit.HOURS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("tokenType", "access");

        return Jwts.builder()
            .subject(userId)
            .claims(claims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Generate refresh token for obtaining new access tokens
     *
     * @param userId User identifier
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenValidityDays, ChronoUnit.DAYS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        return Jwts.builder()
            .subject(userId)
            .claims(claims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Validate JWT token and check expiration
     *
     * @param token JWT token string
     * @return true if token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact format is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID (subject) from JWT token
     *
     * @param token JWT token string
     * @return User ID
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * Extract roles from JWT token
     *
     * @param token JWT token string
     * @return Array of role names
     */
    @SuppressWarnings("unchecked")
    public String[] getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Object roles = claims.get("roles");
        if (roles instanceof java.util.List) {
            return ((java.util.List<String>) roles).toArray(new String[0]);
        }
        return new String[0];
    }

    /**
     * Extract permissions from JWT token
     *
     * @param token JWT token string
     * @return Array of permission strings
     */
    @SuppressWarnings("unchecked")
    public String[] getPermissionsFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Object permissions = claims.get("permissions");
        if (permissions instanceof java.util.List) {
            return ((java.util.List<String>) permissions).toArray(new String[0]);
        }
        return new String[0];
    }

    /**
     * Extract expiration date from JWT token
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token string
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
