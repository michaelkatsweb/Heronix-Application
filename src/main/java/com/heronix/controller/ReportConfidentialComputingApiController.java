package com.heronix.controller;

import com.heronix.dto.ReportConfidentialComputing;
import com.heronix.service.ReportConfidentialComputingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Confidential Computing API Controller
 *
 * REST API endpoints for confidential computing, trusted execution environments,
 * secure enclaves, attestation, and hardware-based security.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 151 - Confidential Computing & Trusted Execution Environment
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/confidential-computing")
@RequiredArgsConstructor
public class ReportConfidentialComputingApiController {

    private final ReportConfidentialComputingService confidentialComputingService;

    /**
     * Create new confidential computing configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createConfidentialComputing(
            @RequestBody ReportConfidentialComputing config) {
        try {
            ReportConfidentialComputing created = confidentialComputingService.createConfidentialComputing(config);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Confidential computing configuration created successfully");
            response.put("confidentialComputingId", created.getConfidentialComputingId());
            response.put("confidentialComputingName", created.getConfidentialComputingName());
            response.put("platform", created.getPlatform());
            response.put("environment", created.getEnvironment());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create confidential computing configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get confidential computing configuration by ID
     */
    @GetMapping("/{confidentialComputingId}")
    public ResponseEntity<Map<String, Object>> getConfidentialComputing(
            @PathVariable Long confidentialComputingId) {
        try {
            ReportConfidentialComputing config =
                    confidentialComputingService.getConfidentialComputing(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("confidentialComputing", config);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate confidential computing
     */
    @PostMapping("/{confidentialComputingId}/activate")
    public ResponseEntity<Map<String, Object>> activateConfidentialComputing(
            @PathVariable Long confidentialComputingId) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.activateConfidentialComputing(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Create secure enclave
     */
    @PostMapping("/{confidentialComputingId}/enclaves/create")
    public ResponseEntity<Map<String, Object>> createSecureEnclave(
            @PathVariable Long confidentialComputingId,
            @RequestBody Map<String, Object> enclaveData) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.createSecureEnclave(confidentialComputingId, enclaveData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Initialize enclave
     */
    @PostMapping("/{confidentialComputingId}/enclaves/{enclaveId}/initialize")
    public ResponseEntity<Map<String, Object>> initializeEnclave(
            @PathVariable Long confidentialComputingId,
            @PathVariable String enclaveId) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.initializeEnclave(confidentialComputingId, enclaveId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Start enclave
     */
    @PostMapping("/{confidentialComputingId}/enclaves/{enclaveId}/start")
    public ResponseEntity<Map<String, Object>> startEnclave(
            @PathVariable Long confidentialComputingId,
            @PathVariable String enclaveId) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.startEnclave(confidentialComputingId, enclaveId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform attestation
     */
    @PostMapping("/{confidentialComputingId}/attestation/perform")
    public ResponseEntity<Map<String, Object>> performAttestation(
            @PathVariable Long confidentialComputingId,
            @RequestBody Map<String, Object> attestationData) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.performAttestation(confidentialComputingId, attestationData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Record security event
     */
    @PostMapping("/{confidentialComputingId}/security/record-event")
    public ResponseEntity<Map<String, Object>> recordSecurityEvent(
            @PathVariable Long confidentialComputingId,
            @RequestBody Map<String, Object> eventData) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.recordSecurityEvent(confidentialComputingId, eventData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Record intrusion attempt
     */
    @PostMapping("/{confidentialComputingId}/security/record-intrusion")
    public ResponseEntity<Map<String, Object>> recordIntrusionAttempt(
            @PathVariable Long confidentialComputingId,
            @RequestBody Map<String, Object> attemptData) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.recordIntrusionAttempt(confidentialComputingId, attemptData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Enable memory encryption
     */
    @PostMapping("/{confidentialComputingId}/encryption/enable")
    public ResponseEntity<Map<String, Object>> enableMemoryEncryption(
            @PathVariable Long confidentialComputingId,
            @RequestBody Map<String, Object> encryptionData) {
        try {
            Map<String, Object> result =
                    confidentialComputingService.enableMemoryEncryption(confidentialComputingId, encryptionData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Perform health check
     */
    @PostMapping("/{confidentialComputingId}/health/check")
    public ResponseEntity<Map<String, Object>> performHealthCheck(
            @PathVariable Long confidentialComputingId) {
        try {
            Map<String, Object> result = confidentialComputingService.performHealthCheck(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get enclave metrics
     */
    @GetMapping("/{confidentialComputingId}/metrics/enclaves")
    public ResponseEntity<Map<String, Object>> getEnclaveMetrics(
            @PathVariable Long confidentialComputingId) {
        try {
            Map<String, Object> metrics = confidentialComputingService.getEnclaveMetrics(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get attestation metrics
     */
    @GetMapping("/{confidentialComputingId}/metrics/attestation")
    public ResponseEntity<Map<String, Object>> getAttestationMetrics(
            @PathVariable Long confidentialComputingId) {
        try {
            Map<String, Object> metrics =
                    confidentialComputingService.getAttestationMetrics(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get security metrics
     */
    @GetMapping("/{confidentialComputingId}/metrics/security")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics(
            @PathVariable Long confidentialComputingId) {
        try {
            Map<String, Object> metrics = confidentialComputingService.getSecurityMetrics(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all confidential computing configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfidentialComputing() {
        List<ReportConfidentialComputing> configs = confidentialComputingService.getAllConfidentialComputing();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active configurations
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveConfigs() {
        List<ReportConfidentialComputing> configs = confidentialComputingService.getActiveConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete confidential computing configuration
     */
    @DeleteMapping("/{confidentialComputingId}")
    public ResponseEntity<Map<String, Object>> deleteConfidentialComputing(
            @PathVariable Long confidentialComputingId) {
        try {
            confidentialComputingService.deleteConfidentialComputing(confidentialComputingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Confidential computing configuration deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = confidentialComputingService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
