package com.heronix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transfer Authorization API Service
 *
 * RestTemplate-based client for HSTP (Heronix Secure Transfer Protocol) endpoints.
 * Follows the StudentApiService pattern for consistency.
 *
 * API Endpoints:
 * - POST /api/transfer-authorization - Create authorization
 * - POST /api/transfer-authorization/{id}/submit - Submit for signatures
 * - POST /api/transfer-authorization/{id}/sign - Add signature
 * - POST /api/transfer-authorization/{id}/generate-package - Generate package
 * - GET  /api/transfer-authorization/{id}/download-package - Download .heronix file
 * - GET  /api/transfer-authorization - List authorizations
 * - GET  /api/transfer-authorization/{id} - Get authorization details
 * - POST /api/transfer-keys/generate - Generate signing key pair
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransferAuthorizationApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    private static final String BASE_PATH = "/api/transfer-authorization";
    private static final String KEY_PATH = "/api/transfer-keys";

    // ========================================================================
    // AUTHORIZATION OPERATIONS
    // ========================================================================

    /**
     * Create a new transfer authorization.
     */
    public Map<String, Object> createAuthorization(Map<String, Object> request) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(BASE_PATH, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Create transfer authorization");
    }

    /**
     * Submit authorization for signatures.
     */
    public Map<String, Object> submitForSignatures(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/submit", null, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Submit for signatures");
    }

    /**
     * Get authorization with full details.
     */
    public Map<String, Object> getAuthorization(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_PATH + "/" + authorizationId,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Get authorization");
    }

    /**
     * List all authorizations with optional status filter.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listAuthorizations(String status) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = status != null ? BASE_PATH + "?status=" + status : BASE_PATH;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return List.of();
        }, "List authorizations");
    }

    /**
     * Get authorizations pending the current user's signature.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPendingForMe() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_PATH + "/pending-for-me", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return List.of();
        }, "Get pending authorizations");
    }

    // ========================================================================
    // SIGNING OPERATIONS
    // ========================================================================

    /**
     * Add an Ed25519 signature (server-side signing mode).
     */
    public Map<String, Object> addSignatureServerSide(Long authorizationId, Long signerUserId,
                                                       String privateKeyBase64, String keyFingerprint,
                                                       String remarks) {
        Map<String, Object> request = new HashMap<>();
        request.put("signerUserId", signerUserId);
        request.put("keyFingerprint", keyFingerprint);
        request.put("privateKeyBase64", privateKeyBase64);
        request.put("remarks", remarks);

        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/sign", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Add signature");
    }

    /**
     * Add a pre-computed Ed25519 signature (client-side signing mode).
     */
    public Map<String, Object> addSignatureClientSide(Long authorizationId, Long signerUserId,
                                                       String signatureBase64, String keyFingerprint,
                                                       String remarks) {
        Map<String, Object> request = new HashMap<>();
        request.put("signerUserId", signerUserId);
        request.put("keyFingerprint", keyFingerprint);
        request.put("signatureBase64", signatureBase64);
        request.put("remarks", remarks);

        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/sign", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Add pre-computed signature");
    }

    /**
     * Verify all signatures cryptographically.
     */
    public Map<String, Object> verifySignatures(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/verify-signatures", null, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Verify signatures");
    }

    // ========================================================================
    // PACKAGING & DELIVERY
    // ========================================================================

    /**
     * Generate the encrypted transfer package.
     */
    public Map<String, Object> generatePackage(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/generate-package", null, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Generate package");
    }

    /**
     * Download the .heronix encrypted package as byte array.
     */
    public byte[] downloadPackage(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    BASE_PATH + "/" + authorizationId + "/download-package",
                    HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new byte[0];
        }, "Download package");
    }

    /**
     * Mark authorization as delivered.
     */
    public Map<String, Object> markDelivered(Long authorizationId, String method, String details) {
        Map<String, Object> request = new HashMap<>();
        request.put("method", method);
        request.put("confirmationDetails", details);

        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/mark-delivered", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Mark delivered");
    }

    /**
     * Confirm receipt of the package.
     */
    public Map<String, Object> confirmReceipt(Long authorizationId, String confirmationMethod) {
        Map<String, Object> request = Map.of("confirmationMethod", confirmationMethod);

        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            BASE_PATH + "/" + authorizationId + "/confirm-receipt", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Confirm receipt");
    }

    // ========================================================================
    // PDF DOCUMENTATION
    // ========================================================================

    /**
     * Download a PDF document for an authorization.
     */
    public byte[] downloadPdf(Long authorizationId, String pdfType) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    BASE_PATH + "/" + authorizationId + "/pdf/" + pdfType,
                    HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new byte[0];
        }, "Download PDF: " + pdfType);
    }

    /**
     * Download all documentation as ZIP.
     */
    public byte[] downloadAllDocumentation(Long authorizationId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    BASE_PATH + "/" + authorizationId + "/pdf/all-documentation",
                    HttpMethod.GET, null, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new byte[0];
        }, "Download all documentation");
    }

    // ========================================================================
    // KEY MANAGEMENT
    // ========================================================================

    /**
     * Generate a new Ed25519 key pair on the server.
     */
    public Map<String, Object> generateKeyPair(Long userId, String role, String label) {
        Map<String, String> request = new HashMap<>();
        request.put("userId", userId.toString());
        request.put("role", role);
        request.put("label", label);

        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)
                    (ResponseEntity<?>) restTemplate.postForEntity(
                            KEY_PATH + "/generate", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Generate key pair");
    }

    /**
     * Get transfer statistics for dashboard.
     */
    public Map<String, Object> getStatistics() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_PATH + "/statistics", HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Get statistics");
    }

    // ========================================================================
    // KEY QUERIES & REVOCATION
    // ========================================================================

    /**
     * Get active signing keys for a specific user.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getKeysForUser(Long userId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    KEY_PATH + "/user/" + userId, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return List.of();
        }, "Get keys for user");
    }

    /**
     * Get all authorizers with their key status (grouped by role).
     */
    public Map<String, Object> getAuthorizers() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KEY_PATH + "/authorizers", HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Get authorizers");
    }

    /**
     * Revoke a signing key.
     */
    public Map<String, Object> revokeKey(Long keyId, String reason) {
        Map<String, String> request = new HashMap<>();
        request.put("reason", reason);

        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KEY_PATH + "/" + keyId, HttpMethod.DELETE,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new HashMap<>();
        }, "Revoke key");
    }
}
