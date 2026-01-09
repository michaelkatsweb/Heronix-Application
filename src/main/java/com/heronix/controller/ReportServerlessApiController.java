package com.heronix.controller;

import com.heronix.dto.ReportServerless;
import com.heronix.service.ReportServerlessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Serverless API Controller
 *
 * REST API endpoints for serverless and FaaS management.
 *
 * Endpoints:
 * - POST /api/serverless - Create platform
 * - GET /api/serverless/{id} - Get platform
 * - POST /api/serverless/{id}/start - Start platform
 * - POST /api/serverless/{id}/stop - Stop platform
 * - POST /api/serverless/{id}/function - Deploy function
 * - PUT /api/serverless/{id}/function/{functionId}/status - Update function status
 * - POST /api/serverless/{id}/function/{functionId}/invoke - Invoke function
 * - PUT /api/serverless/{id}/function/{functionId}/config - Update function config
 * - POST /api/serverless/{id}/trigger - Create trigger
 * - POST /api/serverless/{id}/layer - Create layer
 * - PUT /api/serverless/{id}/metrics - Update metrics
 * - GET /api/serverless/{id}/functions/active - Get active functions
 * - GET /api/serverless/{id}/executions/successful - Get successful executions
 * - DELETE /api/serverless/{id} - Delete platform
 * - GET /api/serverless/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 97 - Report Serverless & Function-as-a-Service
 */
@RestController
@RequestMapping("/api/serverless")
@RequiredArgsConstructor
@Slf4j
public class ReportServerlessApiController {

    private final ReportServerlessService serverlessService;

    /**
     * Create platform
     */
    @PostMapping
    public ResponseEntity<ReportServerless> createPlatform(@RequestBody ReportServerless platform) {
        log.info("POST /api/serverless - Creating platform: {}", platform.getPlatformName());

        try {
            ReportServerless created = serverlessService.createPlatform(platform);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating platform", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get platform
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportServerless> getPlatform(@PathVariable Long id) {
        log.info("GET /api/serverless/{}", id);

        try {
            return serverlessService.getPlatform(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start platform
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startPlatform(@PathVariable Long id) {
        log.info("POST /api/serverless/{}/start", id);

        try {
            serverlessService.startPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Platform started");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop platform
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopPlatform(@PathVariable Long id) {
        log.info("POST /api/serverless/{}/stop", id);

        try {
            serverlessService.stopPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Platform stopped");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy function
     */
    @PostMapping("/{id}/function")
    public ResponseEntity<ReportServerless.Function> deployFunction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/serverless/{}/function", id);

        try {
            String functionName = request.get("functionName");
            String runtimeStr = request.get("runtime");
            String handler = request.get("handler");

            ReportServerless.Runtime runtime = ReportServerless.Runtime.valueOf(runtimeStr);

            ReportServerless.Function function = serverlessService.deployFunction(
                    id, functionName, runtime, handler
            );

            return ResponseEntity.ok(function);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying function in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update function status
     */
    @PutMapping("/{id}/function/{functionId}/status")
    public ResponseEntity<Map<String, Object>> updateFunctionStatus(
            @PathVariable Long id,
            @PathVariable String functionId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/serverless/{}/function/{}/status", id, functionId);

        try {
            String statusStr = request.get("status");
            ReportServerless.FunctionStatus status = ReportServerless.FunctionStatus.valueOf(statusStr);

            serverlessService.updateFunctionStatus(id, functionId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Function status updated");
            response.put("platformId", id);
            response.put("functionId", functionId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Platform or function not found: {}, {}", id, functionId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating function status in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Invoke function
     */
    @PostMapping("/{id}/function/{functionId}/invoke")
    public ResponseEntity<ReportServerless.Execution> invokeFunction(
            @PathVariable Long id,
            @PathVariable String functionId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/serverless/{}/function/{}/invoke", id, functionId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> input = (Map<String, Object>) request.get("input");

            String triggerTypeStr = (String) request.get("triggerType");
            ReportServerless.TriggerType triggerType = triggerTypeStr != null ?
                    ReportServerless.TriggerType.valueOf(triggerTypeStr) :
                    ReportServerless.TriggerType.HTTP;

            ReportServerless.Execution execution = serverlessService.invokeFunction(
                    id, functionId, input, triggerType
            );

            return ResponseEntity.ok(execution);

        } catch (IllegalArgumentException e) {
            log.error("Platform or function not found: {}, {}", id, functionId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error invoking function in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update function config
     */
    @PutMapping("/{id}/function/{functionId}/config")
    public ResponseEntity<Map<String, Object>> updateFunctionConfig(
            @PathVariable Long id,
            @PathVariable String functionId,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/serverless/{}/function/{}/config", id, functionId);

        try {
            Integer timeoutSeconds = request.get("timeoutSeconds") != null ?
                    ((Number) request.get("timeoutSeconds")).intValue() : null;
            Integer memoryMb = request.get("memoryMb") != null ?
                    ((Number) request.get("memoryMb")).intValue() : null;

            @SuppressWarnings("unchecked")
            Map<String, String> environmentVariables = (Map<String, String>) request.get("environmentVariables");

            serverlessService.updateFunctionConfiguration(id, functionId,
                    timeoutSeconds, memoryMb, environmentVariables);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Function configuration updated");
            response.put("platformId", id);
            response.put("functionId", functionId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Platform or function not found: {}, {}", id, functionId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating function config in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create trigger
     */
    @PostMapping("/{id}/trigger")
    public ResponseEntity<ReportServerless.Trigger> createTrigger(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/serverless/{}/trigger", id);

        try {
            String triggerName = request.get("triggerName");
            String triggerTypeStr = request.get("triggerType");
            String functionId = request.get("functionId");

            ReportServerless.TriggerType triggerType = ReportServerless.TriggerType.valueOf(triggerTypeStr);

            ReportServerless.Trigger trigger = serverlessService.createTrigger(
                    id, triggerName, triggerType, functionId
            );

            return ResponseEntity.ok(trigger);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating trigger in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create layer
     */
    @PostMapping("/{id}/layer")
    public ResponseEntity<ReportServerless.Layer> createLayer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/serverless/{}/layer", id);

        try {
            String layerName = (String) request.get("layerName");

            @SuppressWarnings("unchecked")
            List<String> runtimeStrings = (List<String>) request.get("compatibleRuntimes");

            List<ReportServerless.Runtime> compatibleRuntimes = null;
            if (runtimeStrings != null) {
                compatibleRuntimes = runtimeStrings.stream()
                        .map(ReportServerless.Runtime::valueOf)
                        .toList();
            }

            ReportServerless.Layer layer = serverlessService.createLayer(
                    id, layerName, compatibleRuntimes
            );

            return ResponseEntity.ok(layer);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating layer in platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/serverless/{}/metrics", id);

        try {
            serverlessService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active functions
     */
    @GetMapping("/{id}/functions/active")
    public ResponseEntity<Map<String, Object>> getActiveFunctions(@PathVariable Long id) {
        log.info("GET /api/serverless/{}/functions/active", id);

        try {
            return serverlessService.getPlatform(id)
                    .map(platform -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("platformId", id);
                        response.put("functions", platform.getActiveFunctions());
                        response.put("count", platform.getActiveFunctions().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active functions for platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get successful executions
     */
    @GetMapping("/{id}/executions/successful")
    public ResponseEntity<Map<String, Object>> getSuccessfulExecutions(@PathVariable Long id) {
        log.info("GET /api/serverless/{}/executions/successful", id);

        try {
            return serverlessService.getPlatform(id)
                    .map(platform -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("platformId", id);
                        response.put("executions", platform.getSuccessfulExecutions());
                        response.put("count", platform.getSuccessfulExecutions().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching successful executions for platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete platform
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlatform(@PathVariable Long id) {
        log.info("DELETE /api/serverless/{}", id);

        try {
            serverlessService.deletePlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Platform deleted");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/serverless/stats");

        try {
            Map<String, Object> stats = serverlessService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching serverless statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
