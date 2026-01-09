package com.heronix.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object for API Key Creation Requests
 *
 * Defines the structure and validation rules for creating API keys.
 * API keys provide secure programmatic access to Heronix REST APIs.
 *
 * Validation Rules:
 * - Name: Required, 3-100 characters
 * - Scopes: At least one scope must be granted
 * - Rate Limit: 1-10,000 requests per hour
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyRequestDTO {

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
     * Examples: read:students, write:grades, read:attendance
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
     * If not specified, key does not expire
     */
    @Future(message = "Expiration date must be in the future")
    private LocalDate expiresAt;

    /**
     * IP addresses allowed to use this API key (CIDR notation)
     * If empty, all IPs are allowed
     */
    private Set<@Pattern(
        regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}(/[0-9]{1,2})?)$",
        message = "Invalid IP address or CIDR notation"
    ) String> ipWhitelist;
}
