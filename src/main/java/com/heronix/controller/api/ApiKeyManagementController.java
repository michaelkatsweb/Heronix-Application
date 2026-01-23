package com.heronix.controller.api;

import com.heronix.api.dto.ApiKeyCreateRequestDTO;
import com.heronix.api.dto.ApiKeyResponseDTO;
import com.heronix.model.domain.ApiKey;
import com.heronix.security.ApiPermissions;
import com.heronix.service.ApiKeyService;
import com.heronix.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API Key Management Controller
 *
 * REST endpoints for API key lifecycle management.
 * Allows users to create, view, update, rotate, and revoke API keys.
 *
 * Security:
 * - Users can only manage their own API keys
 * - Admins can manage all API keys
 * - API key creation requires write:api-keys permission
 * - Plain text key only shown once during creation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 44 - API Key Management Endpoints & Rate Limiting
 */
// @RestController  // Disabled - duplicate of ApiKeyManagementApiController
// @RequestMapping("/api/api-keys")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "API Keys", description = "API key lifecycle management and access control")
public class ApiKeyManagementController {

    private final ApiKeyService apiKeyService;
    private final SecurityAuditService auditService;

    @Operation(summary = "Create new API key", description = "Generate a new API key with specified scopes and rate limit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "API key created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_" + ApiPermissions.WRITE_API_KEYS + "')")
    public ResponseEntity<Map<String, Object>> createApiKey(
            @Valid @RequestBody ApiKeyCreateRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = authentication.getName();
        String ipAddress = getClientIp(httpRequest);

        try {
            // Generate API key
            ApiKeyService.ApiKeyGenerationResult result = apiKeyService.generateApiKey(
                userId,
                request.getName(),
                request.getDescription(),
                request.getScopes(),
                request.getRateLimit(),
                request.getExpiresAt(),
                request.getIpWhitelist(),
                request.getIsTestKey()
            );

            // Audit logging
            auditService.logApiKeyCreated(
                result.apiKeyEntity().getId(),
                result.apiKeyEntity().getName(),
                userId,
                request.getScopes(),
                ipAddress
            );

            // Convert to response DTO
            ApiKeyResponseDTO responseDTO = convertToResponseDTO(result.apiKeyEntity());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", responseDTO);
            response.put("plainTextKey", result.plainTextKey()); // Only shown once!
            response.put("message", "API key created successfully. Save the key now - it won't be shown again.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating API key for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "List user's API keys", description = "Get all API keys for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API keys retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_" + ApiPermissions.READ_API_KEYS + "')")
    public ResponseEntity<Map<String, Object>> listApiKeys(Authentication authentication) {

        String userId = authentication.getName();

        try {
            List<ApiKey> apiKeys = apiKeyService.getUserApiKeys(userId);

            List<ApiKeyResponseDTO> responseDTOs = apiKeys.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKeys", responseDTOs);
            response.put("count", responseDTOs.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error listing API keys for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to list API keys: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get API key details", description = "Get details of a specific API key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key details retrieved"),
        @ApiResponse(responseCode = "404", description = "API key not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_" + ApiPermissions.READ_API_KEYS + "')")
    public ResponseEntity<Map<String, Object>> getApiKey(
            @Parameter(description = "API key ID") @PathVariable Long id,
            Authentication authentication) {

        String userId = authentication.getName();

        try {
            ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(id))
                .findFirst()
                .orElse(null);

            if (apiKey == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "API key not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            ApiKeyResponseDTO responseDTO = convertToResponseDTO(apiKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", responseDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting API key {} for user {}: {}", id, userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Revoke API key", description = "Revoke an API key (set to inactive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key revoked successfully"),
        @ApiResponse(responseCode = "404", description = "API key not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_" + ApiPermissions.DELETE_API_KEYS + "')")
    public ResponseEntity<Map<String, Object>> revokeApiKey(
            @Parameter(description = "API key ID") @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = authentication.getName();
        String ipAddress = getClientIp(httpRequest);

        try {
            // Verify ownership
            ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(id))
                .findFirst()
                .orElse(null);

            if (apiKey == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "API key not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Revoke the key
            apiKeyService.revokeApiKey(id);

            // Audit logging
            auditService.logApiKeyRevoked(id, apiKey.getName(), userId, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API key revoked successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error revoking API key {} for user {}: {}", id, userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to revoke API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Rotate API key", description = "Revoke old key and generate a new one with same settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key rotated successfully"),
        @ApiResponse(responseCode = "404", description = "API key not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{id}/rotate")
    @PreAuthorize("hasAuthority('SCOPE_" + ApiPermissions.WRITE_API_KEYS + "')")
    public ResponseEntity<Map<String, Object>> rotateApiKey(
            @Parameter(description = "API key ID") @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = authentication.getName();
        String ipAddress = getClientIp(httpRequest);

        try {
            // Verify ownership
            ApiKey apiKey = apiKeyService.getUserApiKeys(userId).stream()
                .filter(key -> key.getId().equals(id))
                .findFirst()
                .orElse(null);

            if (apiKey == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "API key not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Rotate the key
            ApiKeyService.ApiKeyGenerationResult result = apiKeyService.rotateApiKey(id);

            // Audit logging
            auditService.logApiKeyRevoked(id, apiKey.getName(), userId, ipAddress);
            auditService.logApiKeyCreated(
                result.apiKeyEntity().getId(),
                result.apiKeyEntity().getName(),
                userId,
                result.apiKeyEntity().getScopes(),
                ipAddress
            );

            ApiKeyResponseDTO responseDTO = convertToResponseDTO(result.apiKeyEntity());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiKey", responseDTO);
            response.put("plainTextKey", result.plainTextKey()); // Only shown once!
            response.put("message", "API key rotated successfully. Save the new key - it won't be shown again.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error rotating API key {} for user {}: {}", id, userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to rotate API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Convert ApiKey entity to response DTO
     */
    private ApiKeyResponseDTO convertToResponseDTO(ApiKey apiKey) {
        return ApiKeyResponseDTO.builder()
            .id(apiKey.getId())
            .name(apiKey.getName())
            .description(apiKey.getDescription())
            .maskedKey(apiKey.getKeyPrefix() + "_..." + "****") // Masked display
            .keyPrefix(apiKey.getKeyPrefix())
            .scopes(apiKey.getScopes())
            .ipWhitelist(apiKey.getIpWhitelist())
            .rateLimit(apiKey.getRateLimit())
            .active(apiKey.getActive())
            .createdAt(apiKey.getCreatedAt())
            .updatedAt(apiKey.getUpdatedAt())
            .lastUsedAt(apiKey.getLastUsedAt())
            .requestCount(apiKey.getRequestCount())
            .expiresAt(apiKey.getExpiresAt())
            .expired(apiKey.isExpired())
            .build();
    }

    /**
     * Extract client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
