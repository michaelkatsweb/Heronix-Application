package com.heronix.controller;

import com.heronix.dto.ReportChaosEngineering;
import com.heronix.service.ReportChaosEngineeringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Chaos Engineering API Controller
 *
 * REST API endpoints for chaos engineering experiments, fault injection,
 * resilience testing, and system reliability validation.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 147 - Chaos Engineering & Resilience Testing
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/chaos-engineering")
@RequiredArgsConstructor
public class ReportChaosEngineeringApiController {

    private final ReportChaosEngineeringService chaosService;

    /**
     * Create new chaos configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChaos(
            @RequestBody ReportChaosEngineering chaos) {
        try {
            ReportChaosEngineering created = chaosService.createChaos(chaos);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chaos configuration created successfully");
            response.put("chaosId", created.getChaosId());
            response.put("chaosName", created.getChaosName());
            response.put("platform", created.getChaosPlatform());
            response.put("environment", created.getEnvironment());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create chaos configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get chaos configuration by ID
     */
    @GetMapping("/{chaosId}")
    public ResponseEntity<Map<String, Object>> getChaos(@PathVariable Long chaosId) {
        try {
            ReportChaosEngineering chaos = chaosService.getChaos(chaosId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chaos", chaos);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate chaos engineering
     */
    @PostMapping("/{chaosId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long chaosId) {
        try {
            ReportChaosEngineering chaos = chaosService.activate(chaosId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chaos engineering activated successfully");
            response.put("chaosStatus", chaos.getChaosStatus());
            response.put("activatedAt", chaos.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Deactivate chaos engineering
     */
    @PostMapping("/{chaosId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivate(@PathVariable Long chaosId) {
        try {
            ReportChaosEngineering chaos = chaosService.deactivate(chaosId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chaos engineering deactivated successfully");
            response.put("chaosStatus", chaos.getChaosStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Start chaos experiment
     */
    @PostMapping("/{chaosId}/experiments/start")
    public ResponseEntity<Map<String, Object>> startExperiment(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> experimentConfig) {
        try {
            Map<String, Object> result = chaosService.startExperiment(chaosId, experimentConfig);
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
     * Complete chaos experiment
     */
    @PostMapping("/{chaosId}/experiments/complete")
    public ResponseEntity<Map<String, Object>> completeExperiment(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean successful = request.getOrDefault("successful", true);
            ReportChaosEngineering chaos = chaosService.completeExperiment(chaosId, successful);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Experiment completed");
            response.put("experimentStatus", chaos.getExperimentStatus());
            response.put("successful", successful);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Inject fault
     */
    @PostMapping("/{chaosId}/faults/inject")
    public ResponseEntity<Map<String, Object>> injectFault(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> faultData) {
        try {
            Map<String, Object> result = chaosService.injectFault(chaosId, faultData);
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
     * Inject network fault
     */
    @PostMapping("/{chaosId}/faults/network")
    public ResponseEntity<Map<String, Object>> injectNetworkFault(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> networkData) {
        try {
            Map<String, Object> result = chaosService.injectNetworkFault(chaosId, networkData);
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
     * Kill service
     */
    @PostMapping("/{chaosId}/services/kill")
    public ResponseEntity<Map<String, Object>> killService(
            @PathVariable Long chaosId,
            @RequestBody Map<String, String> request) {
        try {
            String serviceName = request.get("serviceName");
            if (serviceName == null || serviceName.isEmpty()) {
                throw new IllegalArgumentException("Service name is required");
            }

            Map<String, Object> result = chaosService.killService(chaosId, serviceName);
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
     * Stress CPU
     */
    @PostMapping("/{chaosId}/stress/cpu")
    public ResponseEntity<Map<String, Object>> stressCpu(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Integer> request) {
        try {
            int percentage = request.getOrDefault("percentage", 50);
            int durationSeconds = request.getOrDefault("durationSeconds", 60);

            Map<String, Object> result = chaosService.stressCpu(chaosId, percentage, durationSeconds);
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
     * Stress memory
     */
    @PostMapping("/{chaosId}/stress/memory")
    public ResponseEntity<Map<String, Object>> stressMemory(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Integer> request) {
        try {
            int megabytes = request.getOrDefault("megabytes", 512);
            int durationSeconds = request.getOrDefault("durationSeconds", 60);

            Map<String, Object> result = chaosService.stressMemory(chaosId, megabytes, durationSeconds);
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
     * Record recovery
     */
    @PostMapping("/{chaosId}/recovery")
    public ResponseEntity<Map<String, Object>> recordRecovery(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> recoveryData) {
        try {
            Map<String, Object> result = chaosService.recordRecovery(chaosId, recoveryData);
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
     * Validate steady state
     */
    @PostMapping("/{chaosId}/steady-state/validate")
    public ResponseEntity<Map<String, Object>> validateSteadyState(@PathVariable Long chaosId) {
        try {
            Map<String, Object> result = chaosService.validateSteadyState(chaosId);
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
     * Emergency stop
     */
    @PostMapping("/{chaosId}/emergency-stop")
    public ResponseEntity<Map<String, Object>> emergencyStop(
            @PathVariable Long chaosId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Emergency stop triggered");
            ReportChaosEngineering chaos = chaosService.emergencyStop(chaosId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emergency stop executed");
            response.put("chaosStatus", chaos.getChaosStatus());
            response.put("reason", reason);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Start game day
     */
    @PostMapping("/{chaosId}/gameday/start")
    public ResponseEntity<Map<String, Object>> startGameDay(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> gameDayConfig) {
        try {
            Map<String, Object> result = chaosService.startGameDay(chaosId, gameDayConfig);
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
     * Validate hypothesis
     */
    @PostMapping("/{chaosId}/hypothesis/validate")
    public ResponseEntity<Map<String, Object>> validateHypothesis(
            @PathVariable Long chaosId,
            @RequestBody Map<String, Object> request) {
        try {
            String hypothesis = (String) request.get("hypothesis");
            boolean validated = (Boolean) request.getOrDefault("validated", false);

            if (hypothesis == null || hypothesis.isEmpty()) {
                throw new IllegalArgumentException("Hypothesis is required");
            }

            Map<String, Object> result = chaosService.validateHypothesis(chaosId, hypothesis, validated);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get resilience metrics
     */
    @GetMapping("/{chaosId}/metrics/resilience")
    public ResponseEntity<Map<String, Object>> getResilienceMetrics(@PathVariable Long chaosId) {
        try {
            Map<String, Object> metrics = chaosService.getResilienceMetrics(chaosId);
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
     * Get all chaos configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllChaos() {
        List<ReportChaosEngineering> configs = chaosService.getAllChaos();
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
        List<ReportChaosEngineering> configs = chaosService.getActiveConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get running experiments
     */
    @GetMapping("/experiments/running")
    public ResponseEntity<Map<String, Object>> getRunningExperiments() {
        List<ReportChaosEngineering> experiments = chaosService.getRunningExperiments();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("experiments", experiments);
        response.put("count", experiments.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete chaos configuration
     */
    @DeleteMapping("/{chaosId}")
    public ResponseEntity<Map<String, Object>> deleteChaos(@PathVariable Long chaosId) {
        try {
            chaosService.deleteChaos(chaosId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chaos configuration deleted successfully");
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
        Map<String, Object> stats = chaosService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
