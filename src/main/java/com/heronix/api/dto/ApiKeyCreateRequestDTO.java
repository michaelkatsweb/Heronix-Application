package com.heronix.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * API Key Creation Request DTO
 *
 * Enhanced version of ApiKeyRequestDTO with additional fields for creation.
 * Used specifically for POST /api/api-keys endpoint.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyCreateRequestDTO {

    /**
     * Human-readable name for the API key
     */
    @NotBlank(message = "API key name is required")
    @Size(min = 3, max = 100, message = "API key name must be between 3 and 100 characters")
    private String name;

    /**
     * Description of API key purpose
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Permission scopes granted to this API key
     */
    @NotEmpty(message = "At least one scope must be specified")
    @Size(min = 1, max = 20, message = "Must grant 1-20 scopes")
    private Set<@NotBlank(message = "Scope cannot be blank") String> scopes;

    /**
     * Maximum requests per hour allowed for this key
     */
    @NotNull(message = "Rate limit must be specified")
    @Min(value = 1, message = "Rate limit must be at least 1 request per hour")
    @Max(value = 10000, message = "Rate limit cannot exceed 10,000 requests per hour")
    private Integer rateLimit;

    /**
     * Optional expiration date for the API key
     */
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    /**
     * IP addresses allowed to use this API key (CIDR notation)
     */
    private Set<@Pattern(
        regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}(/[0-9]{1,2})?)$",
        message = "Invalid IP address or CIDR notation"
    ) String> ipWhitelist;

    /**
     * Whether this is a test environment key
     */
    @Builder.Default
    private Boolean isTestKey = false;
}
