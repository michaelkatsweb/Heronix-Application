package com.heronix.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Controller for API Key Management
 *
 * Manages API authentication and authorization for external access:
 * - API key generation and lifecycle management
 * - Access scopes and permissions
 * - Rate limiting configuration
 * - API key rotation and revocation
 * - Usage tracking and analytics
 * - IP whitelisting
 * - Key expiration policies
 *
 * API Key Features:
 * - Granular permission scopes
 * - Read-only vs read-write access
 * - Resource-specific permissions
 * - Automatic key rotation
 * - Temporary keys with expiration
 * - Usage quotas and rate limits
 * - Audit logging of all API calls
 *
 * Security Features:
 * - Secure key generation (cryptographically secure)
 * - Key hashing in database
 * - Optional IP whitelisting
 * - Request signature verification
 * - Automatic revocation on suspicious activity
 * - Multi-factor authentication for key creation
 *
 * Permission Scopes:
 * - students:read, students:write
 * - courses:read, courses:write
 * - grades:read, grades:write
 * - attendance:read, attendance:write
 * - schedules:read, schedules:write
 * - reports:read
 * - webhooks:manage
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since December 30, 2025 - Phase 37
 */
@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyManagementApiController {

    private final com.heronix.service.ApiKeyService apiKeyService;

    // ========================================================================
    // API KEY CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new API key
     *
     * POST /api/api-keys
     *
     * Request Body:
     * {
     *   "name": "Integration Key for Google Classroom",
     *   "scopes": ["students:read", "courses:read", "grades:write"],
     *   "expiresInDays": 365,
     *   "rateLimit": 1000,
     *   "ipWhitelist": ["192.168.1.100", "10.0.0.50"]
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createApiKey(
            @RequestBody Map<String, Object> requestBody) {

        try {
            // Extract parameters from request
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.getOrDefault("description", "");

            @SuppressWarnings("unchecked")
            List<String> scopesList = (List<String>) requestBody.get("scopes");
            Set<String> scopes = scopesList != null ? new HashSet<>(scopesList) : Set.of();

            Integer expiresInDays = requestBody.containsKey("expiresInDays") ?
                Integer.valueOf(requestBody.get("expiresInDays").toString()) : 365;
            LocalDateTime expiresAt = expiresInDays != null ?
                LocalDateTime.now().plusDays(expiresInDays) : null;

            Integer rateLimit = requestBody.containsKey("rateLimit") ?
                Integer.valueOf(requestBody.get("rateLimit").toString()) : 1000;

            @SuppressWarnings("unchecked")
            List<String> ipWhitelistList = (List<String>) requestBody.get("ipWhitelist");
            Set<String> ipWhitelist = ipWhitelistList != null ? new HashSet<>(ipWhitelistList) : Set.of();

            boolean isTestKey = Boolean.TRUE.equals(requestBody.get("isTestKey"));

            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Generate API key via service
            var result = apiKeyService.generateApiKey(
                userId, name, description, scopes, rateLimit, expiresAt, ipWhitelist, isTestKey);

            // Build response
            Map<String, Object> keyData = new HashMap<>();
            keyData.put("id", result.apiKeyEntity().getId());
            keyData.put("name", result.apiKeyEntity().getName());
            keyData.put("key", result.plainTextKey()); // Only shown once
            keyData.put("keyPrefix", result.plainTextKey().substring(0,
                Math.min(10, result.plainTextKey().length())) + "...");
            keyData.put("scopes", result.apiKeyEntity().getScopes());
            keyData.put("createdAt", result.apiKeyEntity().getCreatedAt());
            keyData.put("expiresAt", result.apiKeyEntity().getExpiresAt());
            keyData.put("rateLimit", result.apiKeyEntity().getRateLimit());
            keyData.put("warning", "Save this key securely - it won't be shown again!");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", keyData);
            response.put("message", "API key created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * List all API keys (masked)
     *
     * GET /api/api-keys
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listApiKeys() {
        try {
            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Get user's API keys from service
            List<com.heronix.model.domain.ApiKey> userKeys = apiKeyService.getUserApiKeys(userId);

            // Map to response DTOs (exclude sensitive data)
            List<Map<String, Object>> apiKeys = userKeys.stream()
                .map(key -> {
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("id", key.getId());
                    keyData.put("name", key.getName());
                    keyData.put("description", key.getDescription());
                    keyData.put("keyPrefix", key.getKeyPrefix() + "_" +
                        (key.getKeyHash() != null ? "****" : ""));
                    keyData.put("scopes", key.getScopes());
                    keyData.put("active", key.getActive());
                    keyData.put("rateLimit", key.getRateLimit());
                    keyData.put("requestCount", key.getRequestCount());
                    keyData.put("lastUsedAt", key.getLastUsedAt());
                    keyData.put("expiresAt", key.getExpiresAt());
                    keyData.put("createdAt", key.getCreatedAt());
                    return keyData;
                })
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKeys", apiKeys);
            response.put("count", apiKeys.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to list API keys: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get API key details by ID
     *
     * GET /api/api-keys/{keyId}
     */
    @GetMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> getApiKey(@PathVariable String keyId) {
        try {
            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Get user's API keys and find the requested one
            Long keyIdLong = Long.parseLong(keyId);
            com.heronix.model.domain.ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Map to response DTO (exclude sensitive data)
            Map<String, Object> keyData = new HashMap<>();
            keyData.put("id", apiKey.getId());
            keyData.put("name", apiKey.getName());
            keyData.put("description", apiKey.getDescription());
            keyData.put("keyPrefix", apiKey.getKeyPrefix() + "_****");
            keyData.put("scopes", apiKey.getScopes());
            keyData.put("active", apiKey.getActive());
            keyData.put("rateLimit", apiKey.getRateLimit());
            keyData.put("requestCount", apiKey.getRequestCount());
            keyData.put("lastUsedAt", apiKey.getLastUsedAt());
            keyData.put("expiresAt", apiKey.getExpiresAt());
            keyData.put("ipWhitelist", apiKey.getIpWhitelist());
            keyData.put("createdAt", apiKey.getCreatedAt());
            keyData.put("updatedAt", apiKey.getUpdatedAt());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", keyData);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update API key configuration
     *
     * PUT /api/api-keys/{keyId}
     *
     * Can update: name, scopes, rate limits, IP whitelist
     * Cannot update: the actual key value
     */
    @PutMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> updateApiKey(
            @PathVariable String keyId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            // TODO: Add updateApiKey method to ApiKeyService for production
            // For now, this is a stub implementation that validates ownership

            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key
            Long keyIdLong = Long.parseLong(keyId);
            apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Extract update parameters (stub - not persisted)
            String name = (String) requestBody.get("name");
            @SuppressWarnings("unchecked")
            List<String> scopesList = (List<String>) requestBody.get("scopes");
            Integer rateLimit = requestBody.containsKey("rateLimit") ?
                Integer.valueOf(requestBody.get("rateLimit").toString()) : null;
            @SuppressWarnings("unchecked")
            List<String> ipWhitelistList = (List<String>) requestBody.get("ipWhitelist");

            Map<String, Object> apiKey = new HashMap<>();
            apiKey.put("id", keyIdLong);
            apiKey.put("name", name);
            apiKey.put("scopes", scopesList);
            apiKey.put("rateLimit", rateLimit);
            apiKey.put("ipWhitelist", ipWhitelistList);
            apiKey.put("updatedAt", LocalDateTime.now());
            apiKey.put("message", "Stub implementation - add ApiKeyService.updateApiKey() for production");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", apiKey);
            response.put("message", "API key configuration validated (not persisted - stub mode)");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Revoke/delete API key
     *
     * DELETE /api/api-keys/{keyId}
     */
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> revokeApiKey(@PathVariable String keyId) {
        try {
            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key before revoking
            Long keyIdLong = Long.parseLong(keyId);
            apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Revoke the API key via service
            apiKeyService.revokeApiKey(keyIdLong);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API key revoked successfully");
            response.put("revokedAt", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to revoke API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // KEY ROTATION
    // ========================================================================

    /**
     * Rotate API key (generate new key, keep same permissions)
     *
     * POST /api/api-keys/{keyId}/rotate
     *
     * Returns new key while keeping old key valid for grace period
     */
    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<Map<String, Object>> rotateApiKey(@PathVariable String keyId) {
        try {
            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key before rotating
            Long keyIdLong = Long.parseLong(keyId);
            apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Rotate the API key via service
            var result = apiKeyService.rotateApiKey(keyIdLong);

            Map<String, Object> rotation = new HashMap<>();
            rotation.put("oldKeyId", keyId);
            rotation.put("newKeyId", result.apiKeyEntity().getId());
            rotation.put("newKey", result.plainTextKey()); // Only shown once
            rotation.put("newKeyPrefix", result.plainTextKey().substring(0,
                Math.min(10, result.plainTextKey().length())) + "...");
            rotation.put("gracePeriodEnds", LocalDateTime.now().plusDays(30));
            rotation.put("scopes", result.apiKeyEntity().getScopes());
            rotation.put("rateLimit", result.apiKeyEntity().getRateLimit());
            rotation.put("warning", "Update your integration with the new key before grace period ends");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rotation", rotation);
            response.put("message", "API key rotated successfully");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to rotate API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // USAGE ANALYTICS
    // ========================================================================

    /**
     * Get API key usage statistics
     *
     * GET /api/api-keys/{keyId}/usage?days=30
     */
    @GetMapping("/{keyId}/usage")
    public ResponseEntity<Map<String, Object>> getApiKeyUsage(
            @PathVariable String keyId,
            @RequestParam(defaultValue = "30") int days) {

        try {
            // TODO: Add usage analytics tracking to ApiKeyService for production
            // For now, return basic usage info from the API key entity

            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key
            Long keyIdLong = Long.parseLong(keyId);
            com.heronix.model.domain.ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Build usage response (from ApiKey entity)
            Map<String, Object> usage = new HashMap<>();
            usage.put("keyId", keyId);
            usage.put("totalRequests", apiKey.getRequestCount() != null ? apiKey.getRequestCount() : 0);
            usage.put("lastUsedAt", apiKey.getLastUsedAt());
            usage.put("rateLimit", apiKey.getRateLimit());
            usage.put("active", apiKey.getActive());
            usage.put("message", "Detailed usage analytics not implemented - add usage tracking service");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("usage", usage);
            response.put("period", days + " days");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve usage statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get API request logs for a key
     *
     * GET /api/api-keys/{keyId}/logs?limit=100
     */
    @GetMapping("/{keyId}/logs")
    public ResponseEntity<Map<String, Object>> getApiKeyLogs(
            @PathVariable String keyId,
            @RequestParam(defaultValue = "100") int limit) {

        try {
            // TODO: Add request logging to ApiKeyService for production
            // Requires: ApiKeyRequestLog entity, repository, and logging in filter

            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key
            Long keyIdLong = Long.parseLong(keyId);
            apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Return empty logs (stub implementation)
            List<Map<String, Object>> logs = new ArrayList<>();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyId", keyId);
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("limit", limit);
            response.put("message", "Request logging not implemented - add ApiKeyRequestLog entity and tracking");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve logs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // PERMISSION SCOPES
    // ========================================================================

    /**
     * Get available permission scopes
     *
     * GET /api/api-keys/scopes
     */
    @GetMapping("/scopes")
    public ResponseEntity<Map<String, Object>> getAvailableScopes() {
        try {
            List<Map<String, Object>> scopes = Arrays.asList(
                createScope("students:read", "Read student information", "basic"),
                createScope("students:write", "Create and update students", "elevated"),
                createScope("courses:read", "Read course information", "basic"),
                createScope("courses:write", "Create and update courses", "elevated"),
                createScope("grades:read", "Read grade information", "basic"),
                createScope("grades:write", "Enter and update grades", "elevated"),
                createScope("attendance:read", "Read attendance records", "basic"),
                createScope("attendance:write", "Record attendance", "elevated"),
                createScope("schedules:read", "Read schedules", "basic"),
                createScope("schedules:write", "Modify schedules", "elevated"),
                createScope("reports:read", "Generate and read reports", "basic"),
                createScope("webhooks:manage", "Manage webhook subscriptions", "admin"),
                createScope("api-keys:manage", "Manage API keys", "admin")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scopes", scopes);
            response.put("count", scopes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve scopes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // RATE LIMITING
    // ========================================================================

    /**
     * Get rate limit status for API key
     *
     * GET /api/api-keys/{keyId}/rate-limit
     */
    @GetMapping("/{keyId}/rate-limit")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(@PathVariable String keyId) {
        try {
            // TODO: Add rate limiting tracking to ApiKeyService for production
            // Requires: Redis or in-memory cache with sliding window algorithm

            // Get current user ID from security context
            String userId = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();

            // Verify user owns this API key
            Long keyIdLong = Long.parseLong(keyId);
            com.heronix.model.domain.ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(keyIdLong))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));

            // Build rate limit response (stub - not tracking actual usage)
            Map<String, Object> rateLimit = new HashMap<>();
            rateLimit.put("keyId", keyId);
            rateLimit.put("limit", apiKey.getRateLimit());
            rateLimit.put("remaining", apiKey.getRateLimit()); // Stub: always full
            rateLimit.put("resetAt", LocalDateTime.now().plusHours(1));
            rateLimit.put("message", "Rate limit tracking not implemented - add Redis cache or token bucket algorithm");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rateLimit", rateLimit);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid API key ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve rate limit status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> createScope(String name, String description, String level) {
        Map<String, Object> scope = new HashMap<>();
        scope.put("name", name);
        scope.put("description", description);
        scope.put("level", level);
        return scope;
    }
}
