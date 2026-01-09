package com.heronix.controller;

import com.heronix.dto.ReportDevSecOps;
import com.heronix.service.ReportDevSecOpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report DevSecOps API Controller
 *
 * REST API endpoints for DevSecOps pipeline management, security automation,
 * vulnerability scanning, compliance checking, and security gate enforcement.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 143 - DevSecOps & Security Automation
 */
@Slf4j
@RestController
@RequestMapping("/api/devsecops")
@RequiredArgsConstructor
public class ReportDevSecOpsApiController {

    private final ReportDevSecOpsService devsecopsService;

    /**
     * Create new DevSecOps configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDevSecOps(@RequestBody ReportDevSecOps devsecops) {
        try {
            ReportDevSecOps created = devsecopsService.createDevSecOps(devsecops);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "DevSecOps configuration created successfully");
            response.put("devsecopsId", created.getDevsecopsId());
            response.put("devsecopsName", created.getDevsecopsName());
            response.put("securityFramework", created.getSecurityFramework());
            response.put("pipelineType", created.getPipelineType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create DevSecOps configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get DevSecOps configuration by ID
     */
    @GetMapping("/{devsecopsId}")
    public ResponseEntity<Map<String, Object>> getDevSecOps(@PathVariable Long devsecopsId) {
        try {
            ReportDevSecOps devsecops = devsecopsService.getDevSecOps(devsecopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("devsecops", devsecops);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate DevSecOps
     */
    @PostMapping("/{devsecopsId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long devsecopsId) {
        try {
            ReportDevSecOps devsecops = devsecopsService.activate(devsecopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "DevSecOps activated successfully");
            response.put("devsecopsStatus", devsecops.getDevsecopsStatus());
            response.put("activatedAt", devsecops.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Run SAST scan
     */
    @PostMapping("/{devsecopsId}/scan/sast")
    public ResponseEntity<Map<String, Object>> runSastScan(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.runSastScan(devsecopsId);
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
     * Run DAST scan
     */
    @PostMapping("/{devsecopsId}/scan/dast")
    public ResponseEntity<Map<String, Object>> runDastScan(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.runDastScan(devsecopsId);
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
     * Run SCA scan
     */
    @PostMapping("/{devsecopsId}/scan/sca")
    public ResponseEntity<Map<String, Object>> runScaScan(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.runScaScan(devsecopsId);
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
     * Scan container image
     */
    @PostMapping("/{devsecopsId}/scan/container")
    public ResponseEntity<Map<String, Object>> scanContainer(
            @PathVariable Long devsecopsId,
            @RequestBody Map<String, String> request) {
        try {
            String imageName = request.get("imageName");
            if (imageName == null || imageName.isEmpty()) {
                throw new IllegalArgumentException("Image name is required");
            }

            Map<String, Object> result = devsecopsService.scanContainer(devsecopsId, imageName);
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
     * Execute security gate
     */
    @PostMapping("/{devsecopsId}/gate/execute")
    public ResponseEntity<Map<String, Object>> executeSecurityGate(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.executeSecurityGate(devsecopsId);
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
     * Remediate vulnerability
     */
    @PostMapping("/{devsecopsId}/remediate/{vulnerabilityId}")
    public ResponseEntity<Map<String, Object>> remediateVulnerability(
            @PathVariable Long devsecopsId,
            @PathVariable String vulnerabilityId) {
        try {
            ReportDevSecOps devsecops = devsecopsService.remediateVulnerability(devsecopsId, vulnerabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vulnerability remediated successfully");
            response.put("remediatedVulnerabilities", devsecops.getRemediatedVulnerabilities());
            response.put("securityPosture", devsecops.getSecurityPosture());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Detect secret leak
     */
    @PostMapping("/{devsecopsId}/secret/detect-leak")
    public ResponseEntity<Map<String, Object>> detectSecretLeak(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.detectSecretLeak(devsecopsId);
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
     * Run compliance check
     */
    @PostMapping("/{devsecopsId}/compliance/check")
    public ResponseEntity<Map<String, Object>> runComplianceCheck(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> result = devsecopsService.runComplianceCheck(devsecopsId);
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
     * Get security posture
     */
    @GetMapping("/{devsecopsId}/posture")
    public ResponseEntity<Map<String, Object>> getSecurityPosture(@PathVariable Long devsecopsId) {
        try {
            Map<String, Object> posture = devsecopsService.getSecurityPosture(devsecopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("posture", posture);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all DevSecOps configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDevSecOps() {
        List<ReportDevSecOps> configs = devsecopsService.getAllDevSecOps();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get compliant configurations
     */
    @GetMapping("/compliant")
    public ResponseEntity<Map<String, Object>> getCompliantConfigs() {
        List<ReportDevSecOps> configs = devsecopsService.getCompliantConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete DevSecOps configuration
     */
    @DeleteMapping("/{devsecopsId}")
    public ResponseEntity<Map<String, Object>> deleteDevSecOps(@PathVariable Long devsecopsId) {
        try {
            devsecopsService.deleteDevSecOps(devsecopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "DevSecOps configuration deleted successfully");
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
        Map<String, Object> stats = devsecopsService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
