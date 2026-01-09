package com.heronix.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token Refresh Request DTO
 *
 * Data transfer object for access token refresh requests.
 * Contains refresh token to obtain a new access token.
 *
 * Validation Rules:
 * - Refresh token: Required
 *
 * Usage:
 * - Client sends refresh token when access token expires
 * - Server validates refresh token
 * - Server generates new access token
 * - Client receives new access token (same refresh token)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequestDTO {

    /**
     * JWT refresh token
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
