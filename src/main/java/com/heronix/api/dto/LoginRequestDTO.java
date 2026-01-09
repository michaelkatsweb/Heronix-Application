package com.heronix.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 *
 * Data transfer object for user authentication requests.
 * Contains username and password for JWT token generation.
 *
 * Validation Rules:
 * - Username: Required, 3-50 characters
 * - Password: Required, 8-100 characters
 *
 * Security:
 * - Password transmitted over HTTPS only
 * - Password not logged or stored in plain text
 * - Failed login attempts logged for security monitoring
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /**
     * Username or email for authentication
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * User password
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}
