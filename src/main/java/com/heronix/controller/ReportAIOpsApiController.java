package com.heronix.controller;

import com.heronix.dto.ReportAIOps;
import com.heronix.service.ReportAIOpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report AIOps API Controller
 *
 * REST API endpoints for AI-powered operations, intelligent automation,
 * predictive analytics, anomaly detection, and self-healing systems.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 141 - Intelligent Automation & AI Operations (AIOps)
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/aiops")
@RequiredArgsConstructor
public class ReportAIOpsApiController {

    private final ReportAIOpsService aiopsService;

    /**
     * Create new AIOps configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAIOps(@RequestBody ReportAIOps aiops) {
        try {
            ReportAIOps created = aiopsService.createAIOps(aiops);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AIOps configuration created successfully");
            response.put("aiopsId", created.getAiopsId());
            response.put("aiopsName", created.getAiopsName());
            response.put("mlFramework", created.getMlFramework());
            response.put("modelType", created.getModelType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create AIOps configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get AIOps configuration by ID
     */
    @GetMapping("/{aiopsId}")
    public ResponseEntity<Map<String, Object>> getAIOps(@PathVariable Long aiopsId) {
        try {
            ReportAIOps aiops = aiopsService.getAIOps(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("aiops", aiops);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate AIOps
     */
    @PostMapping("/{aiopsId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long aiopsId) {
        try {
            ReportAIOps aiops = aiopsService.activate(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AIOps activated successfully");
            response.put("aiopsStatus", aiops.getAiopsStatus());
            response.put("activatedAt", aiops.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Train ML model
     */
    @PostMapping("/{aiopsId}/train")
    public ResponseEntity<Map<String, Object>> trainModel(
            @PathVariable Long aiopsId,
            @RequestBody Map<String, Object> trainingData) {
        try {
            ReportAIOps aiops = aiopsService.trainModel(aiopsId, trainingData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Model training completed successfully");
            response.put("modelAccuracy", aiops.getModelAccuracy());
            response.put("f1Score", aiops.getF1Score());
            response.put("precision", aiops.getPrecision());
            response.put("recall", aiops.getRecall());
            response.put("lastModelTraining", aiops.getLastModelTraining());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Detect anomalies
     */
    @PostMapping("/{aiopsId}/detect-anomalies")
    public ResponseEntity<Map<String, Object>> detectAnomalies(
            @PathVariable Long aiopsId,
            @RequestBody Map<String, Object> metrics) {
        try {
            Map<String, Object> result = aiopsService.detectAnomalies(aiopsId, metrics);
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
     * Make prediction
     */
    @PostMapping("/{aiopsId}/predict")
    public ResponseEntity<Map<String, Object>> predict(
            @PathVariable Long aiopsId,
            @RequestBody Map<String, Object> inputData) {
        try {
            Map<String, Object> prediction = aiopsService.predict(aiopsId, inputData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediction", prediction);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform root cause analysis
     */
    @PostMapping("/{aiopsId}/root-cause-analysis")
    public ResponseEntity<Map<String, Object>> analyzeRootCause(
            @PathVariable Long aiopsId,
            @RequestBody Map<String, Object> incident) {
        try {
            Map<String, Object> rootCause = aiopsService.analyzeRootCause(aiopsId, incident);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rootCause", rootCause);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform self-healing
     */
    @PostMapping("/{aiopsId}/self-heal")
    public ResponseEntity<Map<String, Object>> selfHeal(
            @PathVariable Long aiopsId,
            @RequestBody Map<String, Object> issue) {
        try {
            Map<String, Object> healingResult = aiopsService.selfHeal(aiopsId, issue);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healingResult", healingResult);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Execute automated remediation
     */
    @PostMapping("/{aiopsId}/remediate/{playbookId}")
    public ResponseEntity<Map<String, Object>> executeRemediation(
            @PathVariable Long aiopsId,
            @PathVariable String playbookId) {
        try {
            Map<String, Object> result = aiopsService.executeRemediation(aiopsId, playbookId);
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
     * Optimize performance
     */
    @PostMapping("/{aiopsId}/optimize-performance")
    public ResponseEntity<Map<String, Object>> optimizePerformance(@PathVariable Long aiopsId) {
        try {
            Map<String, Object> optimizations = aiopsService.optimizePerformance(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimizations", optimizations);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Plan capacity
     */
    @PostMapping("/{aiopsId}/plan-capacity")
    public ResponseEntity<Map<String, Object>> planCapacity(@PathVariable Long aiopsId) {
        try {
            Map<String, Object> capacityPlan = aiopsService.planCapacity(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("capacityPlan", capacityPlan);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get insights
     */
    @GetMapping("/{aiopsId}/insights")
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable Long aiopsId) {
        try {
            Map<String, Object> insights = aiopsService.getInsights(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insights", insights);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all AIOps configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAIOps() {
        List<ReportAIOps> configs = aiopsService.getAllAIOps();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active AIOps configurations
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveAIOps() {
        List<ReportAIOps> configs = aiopsService.getActiveAIOps();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete AIOps configuration
     */
    @DeleteMapping("/{aiopsId}")
    public ResponseEntity<Map<String, Object>> deleteAIOps(@PathVariable Long aiopsId) {
        try {
            aiopsService.deleteAIOps(aiopsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AIOps configuration deleted successfully");
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
        Map<String, Object> stats = aiopsService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
