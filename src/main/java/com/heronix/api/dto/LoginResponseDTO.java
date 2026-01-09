package com.heronix.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 *
 * Data transfer object for successful authentication responses.
 * Contains JWT access token, refresh token, and user information.
 *
 * Token Usage:
 * - accessToken: Include in Authorization: Bearer header for API requests
 * - refreshToken: Use to obtain new access token when expired
 * - expiresIn: Access token validity in seconds (typically 3600 = 1 hour)
 *
 * Example Response:
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 3600,
 *   "userId": "john.doe",
 *   "roles": ["TEACHER", "ADMIN"]
 * }
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /**
     * JWT access token for API authentication
     * Valid for 1 hour
     */
    private String accessToken;

    /**
     * JWT refresh token for obtaining new access tokens
     * Valid for 7 days
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds
     * Typically 3600 (1 hour)
     */
    @Builder.Default
    private Long expiresIn = 3600L;

    /**
     * Authenticated user's ID
     */
    private String userId;

    /**
     * User's assigned roles
     */
    private String[] roles;
}
