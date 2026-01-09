package com.heronix.service;

import com.heronix.model.domain.ApiKey;
import com.heronix.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * API Key Management Service
 *
 * Provides secure generation, validation, and lifecycle management of API keys.
 *
 * Key Generation:
 * - Cryptographically secure random key generation
 * - Format: {prefix}_{random_base64}
 * - Example: hx_live_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
 * - Keys are 32 bytes (256 bits) of entropy
 *
 * Security:
 * - Keys are hashed with BCrypt before storage
 * - Only the hash is stored in database
 * - Key prefix stored separately for identification
 * - Plain text key returned only once during generation
 *
 * Key Lifecycle:
 * - Created: Initial generation
 * - Active: Can be used for API requests
 * - Inactive: Temporarily disabled
 * - Expired: Past expiration date
 * - Revoked: Permanently disabled
 *
 * Features:
 * - Scope-based permissions
 * - IP whitelisting
 * - Rate limiting
 * - Usage tracking
 * - Expiration management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int KEY_LENGTH_BYTES = 32; // 256 bits
    private static final String KEY_PREFIX_LIVE = "hx_live";
    private static final String KEY_PREFIX_TEST = "hx_test";

    /**
     * Generate a new API key with secure random generation
     *
     * @param userId Owner of the API key
     * @param name Human-readable name
     * @param description Purpose description
     * @param scopes Permission scopes
     * @param rateLimit Requests per hour limit
     * @param expiresAt Expiration date (null for no expiration)
     * @param ipWhitelist Allowed IP addresses (empty for all)
     * @param isTestKey Whether this is a test environment key
     * @return Pair of (plain text key, saved ApiKey entity)
     */
    @Transactional
    public ApiKeyGenerationResult generateApiKey(
            String userId,
            String name,
            String description,
            Set<String> scopes,
            Integer rateLimit,
            LocalDateTime expiresAt,
            Set<String> ipWhitelist,
            boolean isTestKey) {

        // Generate cryptographically secure random key
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        secureRandom.nextBytes(keyBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        // Create key with prefix
        String prefix = isTestKey ? KEY_PREFIX_TEST : KEY_PREFIX_LIVE;
        String plainTextKey = prefix + "_" + randomPart;

        // Hash the key for storage
        String keyHash = passwordEncoder.encode(plainTextKey);

        // Create API key entity
        ApiKey apiKey = ApiKey.builder()
            .name(name)
            .description(description)
            .keyHash(keyHash)
            .keyPrefix(prefix)
            .userId(userId)
            .scopes(scopes)
            .ipWhitelist(ipWhitelist != null ? ipWhitelist : Set.of())
            .rateLimit(rateLimit != null ? rateLimit : 1000)
            .expiresAt(expiresAt)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .requestCount(0L)
            .build();

        ApiKey savedKey = apiKeyRepository.save(apiKey);

        log.info("Generated new API key '{}' for user {} with {} scopes",
            name, userId, scopes.size());

        return new ApiKeyGenerationResult(plainTextKey, savedKey);
    }

    /**
     * Validate an API key
     *
     * @param plainTextKey Plain text API key from request
     * @return Optional ApiKey entity if valid
     */
    public Optional<ApiKey> validateApiKey(String plainTextKey) {
        if (plainTextKey == null || plainTextKey.isBlank()) {
            return Optional.empty();
        }

        // Extract prefix for faster lookup (future optimization)
        String prefix = plainTextKey.split("_")[0] + "_" + plainTextKey.split("_")[1];

        // Find all active keys and check hash
        // Note: This is a simple implementation. For production with many keys,
        // consider adding a key ID to the key format for direct lookup
        List<ApiKey> activeKeys = apiKeyRepository.findAll().stream()
            .filter(key -> key.getActive() && !key.isExpired())
            .filter(key -> key.getKeyPrefix().equals(prefix))
            .toList();

        for (ApiKey apiKey : activeKeys) {
            if (passwordEncoder.matches(plainTextKey, apiKey.getKeyHash())) {
                return Optional.of(apiKey);
            }
        }

        return Optional.empty();
    }

    /**
     * Record API key usage and update statistics
     *
     * @param apiKeyId API key ID
     */
    @Transactional
    public void recordUsage(Long apiKeyId) {
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKey -> {
            apiKey.recordUsage();
            apiKeyRepository.save(apiKey);
        });
    }

    /**
     * Revoke an API key (set to inactive)
     *
     * @param apiKeyId API key ID
     */
    @Transactional
    public void revokeApiKey(Long apiKeyId) {
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKey -> {
            apiKey.setActive(false);
            apiKey.setUpdatedAt(LocalDateTime.now());
            apiKeyRepository.save(apiKey);
            log.info("Revoked API key: {} (ID: {})", apiKey.getName(), apiKeyId);
        });
    }

    /**
     * Get all API keys for a user
     *
     * @param userId User ID
     * @return List of API keys
     */
    public List<ApiKey> getUserApiKeys(String userId) {
        return apiKeyRepository.findByUserId(userId);
    }

    /**
     * Get active API keys for a user
     *
     * @param userId User ID
     * @return List of active API keys
     */
    public List<ApiKey> getActiveUserApiKeys(String userId) {
        return apiKeyRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Delete expired API keys
     *
     * @return Number of keys deleted
     */
    @Transactional
    public int deleteExpiredKeys() {
        List<ApiKey> expiredKeys = apiKeyRepository.findExpiredKeys(LocalDateTime.now());
        apiKeyRepository.deleteAll(expiredKeys);
        log.info("Deleted {} expired API keys", expiredKeys.size());
        return expiredKeys.size();
    }

    /**
     * Rotate an API key (revoke old, create new)
     *
     * @param oldApiKeyId Old API key ID
     * @return New API key generation result
     */
    @Transactional
    public ApiKeyGenerationResult rotateApiKey(Long oldApiKeyId) {
        ApiKey oldKey = apiKeyRepository.findById(oldApiKeyId)
            .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Revoke old key
        oldKey.setActive(false);
        oldKey.setUpdatedAt(LocalDateTime.now());
        apiKeyRepository.save(oldKey);

        // Generate new key with same settings
        return generateApiKey(
            oldKey.getUserId(),
            oldKey.getName() + " (Rotated)",
            oldKey.getDescription(),
            oldKey.getScopes(),
            oldKey.getRateLimit(),
            oldKey.getExpiresAt(),
            oldKey.getIpWhitelist(),
            oldKey.getKeyPrefix().equals(KEY_PREFIX_TEST)
        );
    }

    /**
     * Result of API key generation containing both plain text key and entity
     */
    public record ApiKeyGenerationResult(String plainTextKey, ApiKey apiKeyEntity) {
        /**
         * Get masked key for display (shows only prefix and last 4 characters)
         */
        public String getMaskedKey() {
            if (plainTextKey.length() < 12) {
                return plainTextKey.substring(0, plainTextKey.indexOf('_') + 2) + "...";
            }
            String prefix = plainTextKey.substring(0, plainTextKey.indexOf('_', plainTextKey.indexOf('_') + 1));
            String last4 = plainTextKey.substring(plainTextKey.length() - 4);
            return prefix + "_..." + last4;
        }
    }
}
