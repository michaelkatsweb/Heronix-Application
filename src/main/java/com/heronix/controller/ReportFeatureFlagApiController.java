package com.heronix.controller;

import com.heronix.dto.ReportFeatureFlag;
import com.heronix.service.ReportFeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Feature Flag API Controller
 *
 * REST API endpoints for feature flag management, A/B testing,
 * gradual rollouts, experimentation, and canary deployments.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 145 - Feature Flags & A/B Testing
 */
@Slf4j
@RestController
@RequestMapping("/api/feature-flags")
@RequiredArgsConstructor
public class ReportFeatureFlagApiController {

    private final ReportFeatureFlagService featureFlagService;

    /**
     * Create new feature flag
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFeatureFlag(@RequestBody ReportFeatureFlag flag) {
        try {
            ReportFeatureFlag created = featureFlagService.createFeatureFlag(flag);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feature flag created successfully");
            response.put("flagId", created.getFlagId());
            response.put("flagKey", created.getFlagKey());
            response.put("flagName", created.getFlagName());
            response.put("flagType", created.getFlagType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create feature flag: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get feature flag by ID
     */
    @GetMapping("/{flagId}")
    public ResponseEntity<Map<String, Object>> getFeatureFlag(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.getFeatureFlag(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flag", flag);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get feature flag by key
     */
    @GetMapping("/key/{flagKey}")
    public ResponseEntity<Map<String, Object>> getFeatureFlagByKey(@PathVariable String flagKey) {
        try {
            ReportFeatureFlag flag = featureFlagService.getFeatureFlagByKey(flagKey);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flag", flag);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Enable feature flag
     */
    @PostMapping("/{flagId}/enable")
    public ResponseEntity<Map<String, Object>> enableFlag(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.enableFlag(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feature flag enabled successfully");
            response.put("flagKey", flag.getFlagKey());
            response.put("enabled", flag.getEnabled());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Disable feature flag
     */
    @PostMapping("/{flagId}/disable")
    public ResponseEntity<Map<String, Object>> disableFlag(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.disableFlag(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feature flag disabled successfully");
            response.put("flagKey", flag.getFlagKey());
            response.put("enabled", flag.getEnabled());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Evaluate feature flag for user
     */
    @PostMapping("/{flagId}/evaluate")
    public ResponseEntity<Map<String, Object>> evaluateFlag(
            @PathVariable Long flagId,
            @RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", new HashMap<>());

            Map<String, Object> result = featureFlagService.evaluateFlag(flagId, userId, context);
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
     * Update rollout percentage
     */
    @PutMapping("/{flagId}/rollout")
    public ResponseEntity<Map<String, Object>> updateRollout(
            @PathVariable Long flagId,
            @RequestBody Map<String, Object> request) {
        try {
            double percentage = request.containsKey("percentage")
                    ? ((Number) request.get("percentage")).doubleValue()
                    : 0.0;

            ReportFeatureFlag flag = featureFlagService.updateRollout(flagId, percentage);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rollout percentage updated successfully");
            response.put("rolloutPercentage", flag.getRolloutPercentage());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Increment gradual rollout
     */
    @PostMapping("/{flagId}/rollout/increment")
    public ResponseEntity<Map<String, Object>> incrementRollout(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.incrementRollout(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rollout incremented successfully");
            response.put("rolloutPercentage", flag.getRolloutPercentage());
            response.put("currentStage", flag.getCurrentRolloutStage());
            response.put("totalStages", flag.getTotalRolloutStages());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Start A/B test
     */
    @PostMapping("/{flagId}/abtest/start")
    public ResponseEntity<Map<String, Object>> startAbTest(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.startAbTest(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "A/B test started successfully");
            response.put("experimentId", flag.getExperimentId());
            response.put("experimentStatus", flag.getExperimentStatus());
            response.put("experimentStartDate", flag.getExperimentStartDate());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Complete A/B test
     */
    @PostMapping("/{flagId}/abtest/complete")
    public ResponseEntity<Map<String, Object>> completeAbTest(
            @PathVariable Long flagId,
            @RequestBody Map<String, String> request) {
        try {
            String winningVariant = request.get("winningVariant");
            if (winningVariant == null || winningVariant.isEmpty()) {
                throw new IllegalArgumentException("Winning variant is required");
            }

            ReportFeatureFlag flag = featureFlagService.completeAbTest(flagId, winningVariant);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "A/B test completed successfully");
            response.put("winningVariant", flag.getWinningVariant());
            response.put("experimentStatus", flag.getExperimentStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Record conversion
     */
    @PostMapping("/{flagId}/conversion")
    public ResponseEntity<Map<String, Object>> recordConversion(
            @PathVariable Long flagId,
            @RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String variant = request.get("variant");

            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID is required");
            }

            ReportFeatureFlag flag = featureFlagService.recordConversion(flagId, userId, variant);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conversion recorded successfully");
            response.put("conversionRate", flag.getConversionRate());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Start canary deployment
     */
    @PostMapping("/{flagId}/canary/start")
    public ResponseEntity<Map<String, Object>> startCanary(
            @PathVariable Long flagId,
            @RequestBody Map<String, Object> request) {
        try {
            double percentage = request.containsKey("percentage")
                    ? ((Number) request.get("percentage")).doubleValue()
                    : 10.0;

            ReportFeatureFlag flag = featureFlagService.startCanary(flagId, percentage);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Canary deployment started successfully");
            response.put("canaryPercentage", flag.getCanaryPercentage());
            response.put("canaryStatus", flag.getCanaryStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Promote canary
     */
    @PostMapping("/{flagId}/canary/promote")
    public ResponseEntity<Map<String, Object>> promoteCanary(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.promoteCanary(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Canary promoted successfully");
            response.put("rolloutPercentage", flag.getRolloutPercentage());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Rollback canary
     */
    @PostMapping("/{flagId}/canary/rollback")
    public ResponseEntity<Map<String, Object>> rollbackCanary(
            @PathVariable Long flagId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Manual rollback");
            ReportFeatureFlag flag = featureFlagService.rollbackCanary(flagId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Canary rolled back successfully");
            response.put("canaryStatus", flag.getCanaryStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate kill switch
     */
    @PostMapping("/{flagId}/killswitch/activate")
    public ResponseEntity<Map<String, Object>> activateKillSwitch(
            @PathVariable Long flagId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Emergency kill switch activated");
            ReportFeatureFlag flag = featureFlagService.activateKillSwitch(flagId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kill switch activated successfully");
            response.put("killSwitchEnabled", flag.getKillSwitchEnabled());
            response.put("killSwitchReason", flag.getKillSwitchReason());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Deactivate kill switch
     */
    @PostMapping("/{flagId}/killswitch/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateKillSwitch(@PathVariable Long flagId) {
        try {
            ReportFeatureFlag flag = featureFlagService.deactivateKillSwitch(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kill switch deactivated successfully");
            response.put("killSwitchEnabled", flag.getKillSwitchEnabled());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get experiment results
     */
    @GetMapping("/{flagId}/experiment/results")
    public ResponseEntity<Map<String, Object>> getExperimentResults(@PathVariable Long flagId) {
        try {
            Map<String, Object> results = featureFlagService.getExperimentResults(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get flag statistics
     */
    @GetMapping("/{flagId}/statistics")
    public ResponseEntity<Map<String, Object>> getFlagStatistics(@PathVariable Long flagId) {
        try {
            Map<String, Object> stats = featureFlagService.getFlagStatistics(flagId);
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
     * Get all feature flags
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFeatureFlags() {
        List<ReportFeatureFlag> flags = featureFlagService.getAllFeatureFlags();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("flags", flags);
        response.put("count", flags.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active feature flags
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveFlags() {
        List<ReportFeatureFlag> flags = featureFlagService.getActiveFlags();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("flags", flags);
        response.put("count", flags.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get flags by environment
     */
    @GetMapping("/environment/{environment}")
    public ResponseEntity<Map<String, Object>> getFlagsByEnvironment(@PathVariable String environment) {
        List<ReportFeatureFlag> flags = featureFlagService.getFlagsByEnvironment(environment);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("flags", flags);
        response.put("count", flags.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete feature flag
     */
    @DeleteMapping("/{flagId}")
    public ResponseEntity<Map<String, Object>> deleteFeatureFlag(@PathVariable Long flagId) {
        try {
            featureFlagService.deleteFeatureFlag(flagId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feature flag deleted successfully");
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
        Map<String, Object> stats = featureFlagService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
