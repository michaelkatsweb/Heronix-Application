package com.heronix.service;

import com.heronix.model.domain.ApiKey;
import com.heronix.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API Key Management Service
 *
 * Manages API key lifecycle, authentication, and access control.
 *
 * Features:
 * - Secure API key generation
 * - Key hashing and storage
 * - Scope-based permissions
 * - Rate limiting
 * - IP whitelisting
 * - Usage tracking
 * - Key rotation and revocation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 37 - API Security & Authentication
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyManagementService {

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Create new API key
     */
    @Transactional
    public Map<String, Object> createApiKey(String name, String description, String userId,
                                           Set<String> scopes, Integer expiresInDays,
                                           Integer rateLimit, Set<String> ipWhitelist) {

        // Generate cryptographically secure API key
        String apiKey = generateApiKey();
        String keyPrefix = apiKey.substring(0, 10) + "...";
        String keyHash = passwordEncoder.encode(apiKey);

        // Calculate expiration
        LocalDateTime expiresAt = null;
        if (expiresInDays != null && expiresInDays > 0) {
            expiresAt = LocalDateTime.now().plusDays(expiresInDays);
        }

        // Create API key entity
        ApiKey key = ApiKey.builder()
                .name(name)
                .description(description)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .userId(userId)
                .scopes(scopes != null ? scopes : new HashSet<>())
                .ipWhitelist(ipWhitelist != null ? ipWhitelist : new HashSet<>())
                .rateLimit(rateLimit != null ? rateLimit : 1000)
                .active(true)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .requestCount(0L)
                .build();

        ApiKey saved = apiKeyRepository.save(key);

        log.info("Created API key: {} for user: {}", name, userId);

        // Return key data (API key only shown once!)
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("name", saved.getName());
        result.put("key", apiKey); // ONLY SHOWN ONCE
        result.put("keyPrefix", saved.getKeyPrefix());
        result.put("scopes", saved.getScopes());
        result.put("rateLimit", saved.getRateLimit());
        result.put("expiresAt", saved.getExpiresAt());
        result.put("createdAt", saved.getCreatedAt());

        return result;
    }

    /**
     * List all API keys for a user
     */
    public List<Map<String, Object>> listApiKeys(String userId) {
        List<ApiKey> keys = apiKeyRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return keys.stream().map(key -> {
            Map<String, Object> keyData = new HashMap<>();
            keyData.put("id", key.getId());
            keyData.put("name", key.getName());
            keyData.put("description", key.getDescription());
            keyData.put("keyPrefix", key.getKeyPrefix());
            keyData.put("scopes", key.getScopes());
            keyData.put("active", key.getActive());
            keyData.put("rateLimit", key.getRateLimit());
            keyData.put("createdAt", key.getCreatedAt());
            keyData.put("lastUsedAt", key.getLastUsedAt());
            keyData.put("expiresAt", key.getExpiresAt());
            keyData.put("requestCount", key.getRequestCount());
            keyData.put("isExpired", isExpired(key));
            return keyData;
        }).collect(Collectors.toList());
    }

    /**
     * Get API key by ID
     */
    public Optional<Map<String, Object>> getApiKey(Long keyId, String userId) {
        Optional<ApiKey> keyOpt = apiKeyRepository.findById(keyId);

        if (keyOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiKey key = keyOpt.get();

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        Map<String, Object> keyData = new HashMap<>();
        keyData.put("id", key.getId());
        keyData.put("name", key.getName());
        keyData.put("description", key.getDescription());
        keyData.put("keyPrefix", key.getKeyPrefix());
        keyData.put("scopes", key.getScopes());
        keyData.put("ipWhitelist", key.getIpWhitelist());
        keyData.put("active", key.getActive());
        keyData.put("rateLimit", key.getRateLimit());
        keyData.put("createdAt", key.getCreatedAt());
        keyData.put("updatedAt", key.getUpdatedAt());
        keyData.put("lastUsedAt", key.getLastUsedAt());
        keyData.put("expiresAt", key.getExpiresAt());
        keyData.put("requestCount", key.getRequestCount());
        keyData.put("isExpired", isExpired(key));

        return Optional.of(keyData);
    }

    /**
     * Update API key
     */
    @Transactional
    public void updateApiKey(Long keyId, String userId, String name, String description,
                            Set<String> scopes, Integer rateLimit, Set<String> ipWhitelist) {

        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        if (name != null) key.setName(name);
        if (description != null) key.setDescription(description);
        if (scopes != null) key.setScopes(scopes);
        if (rateLimit != null) key.setRateLimit(rateLimit);
        if (ipWhitelist != null) key.setIpWhitelist(ipWhitelist);

        key.setUpdatedAt(LocalDateTime.now());

        apiKeyRepository.save(key);

        log.info("Updated API key: {} (ID: {})", key.getName(), keyId);
    }

    /**
     * Revoke API key
     */
    @Transactional
    public void revokeApiKey(Long keyId, String userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        key.setActive(false);
        key.setUpdatedAt(LocalDateTime.now());

        apiKeyRepository.save(key);

        log.info("Revoked API key: {} (ID: {})", key.getName(), keyId);
    }

    /**
     * Rotate API key (generate new key, revoke old)
     */
    @Transactional
    public Map<String, Object> rotateApiKey(Long keyId, String userId) {
        ApiKey oldKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!oldKey.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        // Create new key with same settings
        Map<String, Object> newKey = createApiKey(
                oldKey.getName() + " (Rotated)",
                oldKey.getDescription(),
                userId,
                oldKey.getScopes(),
                null, // No expiration by default
                oldKey.getRateLimit(),
                oldKey.getIpWhitelist()
        );

        // Revoke old key
        oldKey.setActive(false);
        oldKey.setUpdatedAt(LocalDateTime.now());
        apiKeyRepository.save(oldKey);

        log.info("Rotated API key: {} (Old ID: {}, New ID: {})",
                oldKey.getName(), keyId, newKey.get("id"));

        newKey.put("oldKeyId", keyId);
        return newKey;
    }

    /**
     * Get API key usage statistics
     */
    public Map<String, Object> getApiKeyUsage(Long keyId, String userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        Map<String, Object> usage = new HashMap<>();
        usage.put("keyId", key.getId());
        usage.put("keyName", key.getName());
        usage.put("totalRequests", key.getRequestCount());
        usage.put("lastUsedAt", key.getLastUsedAt());
        usage.put("createdAt", key.getCreatedAt());
        usage.put("active", key.getActive());
        usage.put("rateLimit", key.getRateLimit());

        // Calculate usage statistics
        if (key.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                    key.getCreatedAt(), LocalDateTime.now()).toDays();
            usage.put("daysSinceCreation", daysSinceCreation);

            if (daysSinceCreation > 0) {
                usage.put("avgRequestsPerDay", key.getRequestCount() / daysSinceCreation);
            }
        }

        return usage;
    }

    /**
     * Get API key logs (stub - would query audit logs)
     */
    public List<Map<String, Object>> getApiKeyLogs(Long keyId, String userId, int limit) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        // Stub implementation - would query audit log table
        List<Map<String, Object>> logs = new ArrayList<>();

        log.info("Retrieved {} logs for API key: {}", limit, keyId);

        return logs;
    }

    /**
     * Get rate limit status
     */
    public Map<String, Object> getRateLimitStatus(Long keyId, String userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        Map<String, Object> status = new HashMap<>();
        status.put("keyId", key.getId());
        status.put("rateLimit", key.getRateLimit());
        status.put("rateLimitPeriod", "hour");

        // Stub - would calculate from recent requests
        status.put("requestsInCurrentPeriod", 0);
        status.put("remainingRequests", key.getRateLimit());
        status.put("resetAt", LocalDateTime.now().plusHours(1));

        return status;
    }

    /**
     * Validate API key
     */
    public boolean validateApiKey(String apiKey, String ipAddress) {
        // Extract prefix for lookup
        String prefix = apiKey.length() >= 10 ? apiKey.substring(0, 10) + "..." : apiKey;

        List<ApiKey> keys = apiKeyRepository.findByKeyPrefix(prefix);

        for (ApiKey key : keys) {
            // Check if hash matches
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                // Check if active
                if (!Boolean.TRUE.equals(key.getActive())) {
                    log.warn("Inactive API key used: {}", key.getName());
                    return false;
                }

                // Check if expired
                if (isExpired(key)) {
                    log.warn("Expired API key used: {}", key.getName());
                    return false;
                }

                // Check IP whitelist
                if (key.getIpWhitelist() != null && !key.getIpWhitelist().isEmpty()) {
                    if (!key.getIpWhitelist().contains(ipAddress)) {
                        log.warn("IP not whitelisted for API key: {} (IP: {})", key.getName(), ipAddress);
                        return false;
                    }
                }

                // Update usage stats
                updateUsageStats(key);

                return true;
            }
        }

        return false;
    }

    /**
     * Check if API key has permission
     */
    public boolean hasPermission(String apiKey, String requiredScope) {
        String prefix = apiKey.length() >= 10 ? apiKey.substring(0, 10) + "..." : apiKey;

        List<ApiKey> keys = apiKeyRepository.findByKeyPrefix(prefix);

        for (ApiKey key : keys) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                return key.getScopes() != null && key.getScopes().contains(requiredScope);
            }
        }

        return false;
    }

    /**
     * Update usage statistics
     */
    @Transactional
    private void updateUsageStats(ApiKey key) {
        key.setLastUsedAt(LocalDateTime.now());
        key.setRequestCount(key.getRequestCount() + 1);
        apiKeyRepository.save(key);
    }

    /**
     * Check if key is expired
     */
    private boolean isExpired(ApiKey key) {
        if (key.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(key.getExpiresAt());
    }

    /**
     * Generate cryptographically secure API key
     */
    private String generateApiKey() {
        return "hx_live_" + UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Get all active API keys count for user
     */
    public long getActiveKeyCount(String userId) {
        return apiKeyRepository.findByUserIdAndActive(userId, true).size();
    }

    /**
     * Delete API key permanently
     */
    @Transactional
    public void deleteApiKey(Long keyId, String userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        // Verify ownership
        if (!key.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to API key");
        }

        apiKeyRepository.delete(key);

        log.info("Deleted API key: {} (ID: {})", key.getName(), keyId);
    }
}
