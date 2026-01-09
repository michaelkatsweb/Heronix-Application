package com.heronix.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * API Key Response DTO
 *
 * Data transfer object for API key information in responses.
 * Does not include the actual key value for security.
 *
 * Security:
 * - Plain text key only returned once during creation
 * - Subsequent requests return masked key (prefix + last 4 chars)
 * - Key hash never exposed to clients
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponseDTO {

    /**
     * API key ID
     */
    private Long id;

    /**
     * Human-readable name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Masked key for display (e.g., "hx_live_...a1b2")
     * Full key only shown once during creation
     */
    private String maskedKey;

    /**
     * Key prefix (hx_live or hx_test)
     */
    private String keyPrefix;

    /**
     * Permission scopes
     */
    private Set<String> scopes;

    /**
     * IP whitelist
     */
    private Set<String> ipWhitelist;

    /**
     * Rate limit (requests per hour)
     */
    private Integer rateLimit;

    /**
     * Whether key is active
     */
    private Boolean active;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Last used timestamp
     */
    private LocalDateTime lastUsedAt;

    /**
     * Total request count
     */
    private Long requestCount;

    /**
     * Expiration timestamp
     */
    private LocalDateTime expiresAt;

    /**
     * Whether key is expired
     */
    private Boolean expired;
}
