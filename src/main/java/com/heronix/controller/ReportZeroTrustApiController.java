package com.heronix.controller;

import com.heronix.dto.ReportZeroTrust;
import com.heronix.service.ReportZeroTrustService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Zero Trust API Controller
 *
 * REST API endpoints for zero trust architecture, continuous verification,
 * micro-segmentation, and identity-based access control.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 138 - Zero Trust Architecture
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/zero-trust")
@RequiredArgsConstructor
public class ReportZeroTrustApiController {

    private final ReportZeroTrustService zeroTrustService;

    /**
     * Create new zero trust policy
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createZeroTrustPolicy(
            @RequestBody ReportZeroTrust zeroTrust) {
        try {
            ReportZeroTrust created = zeroTrustService.createZeroTrustPolicy(zeroTrust);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Zero Trust policy created successfully");
            response.put("zeroTrustId", created.getZeroTrustId());
            response.put("policyName", created.getPolicyName());
            response.put("policyStatus", created.getPolicyStatus());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create Zero Trust policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get zero trust policy by ID
     */
    @GetMapping("/{zeroTrustId}")
    public ResponseEntity<Map<String, Object>> getZeroTrustPolicy(@PathVariable Long zeroTrustId) {
        try {
            ReportZeroTrust zeroTrust = zeroTrustService.getZeroTrustPolicy(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("policy", zeroTrust);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate zero trust policy
     */
    @PostMapping("/{zeroTrustId}/activate")
    public ResponseEntity<Map<String, Object>> activatePolicy(@PathVariable Long zeroTrustId) {
        try {
            ReportZeroTrust zeroTrust = zeroTrustService.activatePolicy(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Zero Trust policy activated successfully");
            response.put("policyStatus", zeroTrust.getPolicyStatus());
            response.put("activatedAt", zeroTrust.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Deactivate zero trust policy
     */
    @PostMapping("/{zeroTrustId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivatePolicy(@PathVariable Long zeroTrustId) {
        try {
            ReportZeroTrust zeroTrust = zeroTrustService.deactivatePolicy(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Zero Trust policy deactivated successfully");
            response.put("policyStatus", zeroTrust.getPolicyStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Set policy to enforcing mode
     */
    @PostMapping("/{zeroTrustId}/enforce")
    public ResponseEntity<Map<String, Object>> enforcePolicy(@PathVariable Long zeroTrustId) {
        try {
            ReportZeroTrust zeroTrust = zeroTrustService.enforcePolicy(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Zero Trust policy set to ENFORCING mode");
            response.put("policyStatus", zeroTrust.getPolicyStatus());
            response.put("enforcementMode", zeroTrust.getEnforcementMode());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Verify access request
     */
    @PostMapping("/{zeroTrustId}/verify-access")
    public ResponseEntity<Map<String, Object>> verifyAccess(
            @PathVariable Long zeroTrustId,
            @RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String deviceId = (String) request.get("deviceId");
            String resourceId = (String) request.get("resourceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", new HashMap<>());

            if (userId == null || deviceId == null || resourceId == null) {
                throw new IllegalArgumentException("userId, deviceId, and resourceId are required");
            }

            Map<String, Object> verificationResult = zeroTrustService.verifyAccess(
                    zeroTrustId, userId, deviceId, resourceId, context);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("verification", verificationResult);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add micro-segmentation segment
     */
    @PostMapping("/{zeroTrustId}/segment")
    public ResponseEntity<Map<String, Object>> addSegment(
            @PathVariable Long zeroTrustId,
            @RequestBody Map<String, Object> segment) {
        try {
            zeroTrustService.addSegment(zeroTrustId, segment);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Micro-segment added successfully");
            response.put("segmentName", segment.get("segmentName"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Update user trust score
     */
    @PutMapping("/{zeroTrustId}/trust-score/user/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserTrustScore(
            @PathVariable Long zeroTrustId,
            @PathVariable String userId,
            @RequestBody Map<String, Double> request) {
        try {
            Double score = request.get("score");
            if (score == null) {
                throw new IllegalArgumentException("Trust score is required");
            }

            zeroTrustService.updateUserTrustScore(zeroTrustId, userId, score);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User trust score updated successfully");
            response.put("userId", userId);
            response.put("score", score);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update device trust score
     */
    @PutMapping("/{zeroTrustId}/trust-score/device/{deviceId}")
    public ResponseEntity<Map<String, Object>> updateDeviceTrustScore(
            @PathVariable Long zeroTrustId,
            @PathVariable String deviceId,
            @RequestBody Map<String, Double> request) {
        try {
            Double score = request.get("score");
            if (score == null) {
                throw new IllegalArgumentException("Trust score is required");
            }

            zeroTrustService.updateDeviceTrustScore(zeroTrustId, deviceId, score);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device trust score updated successfully");
            response.put("deviceId", deviceId);
            response.put("score", score);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update user risk score
     */
    @PutMapping("/{zeroTrustId}/risk-score/user/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserRiskScore(
            @PathVariable Long zeroTrustId,
            @PathVariable String userId,
            @RequestBody Map<String, Double> request) {
        try {
            Double score = request.get("score");
            if (score == null) {
                throw new IllegalArgumentException("Risk score is required");
            }

            ReportZeroTrust zeroTrust = zeroTrustService.updateUserRiskScore(zeroTrustId, userId, score);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User risk score updated successfully");
            response.put("userId", userId);
            response.put("score", score);
            response.put("overallRiskScore", zeroTrust.getOverallRiskScore());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Record policy violation
     */
    @PostMapping("/{zeroTrustId}/violation")
    public ResponseEntity<Map<String, Object>> recordPolicyViolation(
            @PathVariable Long zeroTrustId,
            @RequestBody Map<String, Object> violation) {
        try {
            zeroTrustService.recordPolicyViolation(zeroTrustId, violation);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Policy violation recorded successfully");
            response.put("violationType", violation.get("violationType"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Detect threat
     */
    @PostMapping("/{zeroTrustId}/threat")
    public ResponseEntity<Map<String, Object>> detectThreat(
            @PathVariable Long zeroTrustId,
            @RequestBody Map<String, Object> threat) {
        try {
            Boolean blocked = (Boolean) threat.getOrDefault("blocked", false);
            zeroTrustService.detectThreat(zeroTrustId, threat, blocked);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Threat detection recorded successfully");
            response.put("threatType", threat.get("threatType"));
            response.put("blocked", blocked);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get policy statistics
     */
    @GetMapping("/{zeroTrustId}/statistics")
    public ResponseEntity<Map<String, Object>> getPolicyStatistics(@PathVariable Long zeroTrustId) {
        try {
            Map<String, Object> stats = zeroTrustService.getPolicyStatistics(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all zero trust policies
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllZeroTrustPolicies() {
        List<ReportZeroTrust> policies = zeroTrustService.getAllZeroTrustPolicies();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("policies", policies);
        response.put("count", policies.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active policies
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActivePolicies() {
        List<ReportZeroTrust> policies = zeroTrustService.getActivePolicies();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("policies", policies);
        response.put("count", policies.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete zero trust policy
     */
    @DeleteMapping("/{zeroTrustId}")
    public ResponseEntity<Map<String, Object>> deleteZeroTrustPolicy(@PathVariable Long zeroTrustId) {
        try {
            zeroTrustService.deleteZeroTrustPolicy(zeroTrustId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Zero Trust policy deleted successfully");
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
        Map<String, Object> stats = zeroTrustService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
