package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * API Key Entity
 *
 * Represents an API key for programmatic access to Heronix REST APIs.
 * API keys provide secure, trackable access without requiring user credentials.
 *
 * Security Features:
 * - Keys are hashed using BCrypt before storage (never store plain text)
 * - Each key has a unique identifier for tracking
 * - Keys can be revoked instantly
 * - Keys can have expiration dates
 * - Keys are scoped to specific permissions
 *
 * Usage Tracking:
 * - Last used timestamp
 * - Total request count
 * - Rate limit enforcement
 *
 * Access Control:
 * - Permission scopes (read:students, write:grades, etc.)
 * - IP whitelist (CIDR notation)
 * - Rate limits (requests per hour)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key_hash", columnList = "keyHash"),
    @Index(name = "idx_api_key_active", columnList = "active"),
    @Index(name = "idx_api_key_user", columnList = "userId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name for the API key
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the API key purpose
     */
    @Column(length = 500)
    private String description;

    /**
     * BCrypt hash of the API key
     * Never store the actual key in plain text
     */
    @Column(nullable = false, unique = true, length = 60, name = "key_hash")
    private String keyHash;

    /**
     * Prefix of the API key for identification (e.g., "hx_live_")
     * Allows users to identify which key is being used
     */
    @Column(nullable = false, length = 20)
    private String keyPrefix;

    /**
     * User ID who owns this API key
     */
    @Column(nullable = false, name = "user_id")
    private String userId;

    /**
     * Permission scopes granted to this API key
     * Examples: read:students, write:grades, read:attendance
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "scope")
    @Builder.Default
    private Set<String> scopes = new HashSet<>();

    /**
     * IP addresses allowed to use this key (CIDR notation)
     * Empty set means all IPs are allowed
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "api_key_ip_whitelist", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "ip_address")
    @Builder.Default
    private Set<String> ipWhitelist = new HashSet<>();

    /**
     * Maximum requests per hour for this API key
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer rateLimit = 1000;

    /**
     * Whether the API key is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * When the API key was created
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * When the API key was last updated
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * When the API key was last used
     */
    private LocalDateTime lastUsedAt;

    /**
     * Total number of requests made with this key
     */
    @Builder.Default
    private Long requestCount = 0L;

    /**
     * When the API key expires (null = no expiration)
     */
    private LocalDateTime expiresAt;

    /**
     * Automatically update the updatedAt timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if API key is expired
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if API key is valid (active and not expired)
     */
    public boolean isValid() {
        return active && !isExpired();
    }

    /**
     * Record API key usage
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.requestCount++;
    }
}
