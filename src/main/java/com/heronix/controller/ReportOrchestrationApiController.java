package com.heronix.controller;

import com.heronix.dto.ReportOrchestration;
import com.heronix.service.ReportOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Orchestration API Controller
 *
 * REST API endpoints for workflow orchestration and task execution.
 *
 * Endpoints:
 * - POST /api/orchestration - Create orchestration
 * - GET /api/orchestration/{id} - Get orchestration
 * - POST /api/orchestration/{id}/task - Add task
 * - POST /api/orchestration/{id}/task/{taskId}/dependency - Add task dependency
 * - POST /api/orchestration/{id}/validate - Validate workflow
 * - POST /api/orchestration/{id}/start - Start workflow
 * - POST /api/orchestration/{id}/pause - Pause workflow
 * - POST /api/orchestration/{id}/resume - Resume workflow
 * - POST /api/orchestration/{id}/cancel - Cancel workflow
 * - POST /api/orchestration/{id}/task/{taskId}/execute - Execute task
 * - POST /api/orchestration/{id}/task/{taskId}/complete - Complete task
 * - PUT /api/orchestration/{id}/task/{taskId}/config - Update task configuration
 * - POST /api/orchestration/{id}/trigger - Add trigger
 * - GET /api/orchestration/{id}/tasks/ready - Get ready tasks
 * - GET /api/orchestration/{id}/tasks/running - Get running tasks
 * - GET /api/orchestration/{id}/tasks/failed - Get failed tasks
 * - GET /api/orchestration/{id}/events - Get audit trail
 * - DELETE /api/orchestration/{id} - Delete orchestration
 * - GET /api/orchestration/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 91 - Report Orchestration & Workflow Engine
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/orchestration")
@RequiredArgsConstructor
@Slf4j
public class ReportOrchestrationApiController {

    private final ReportOrchestrationService orchestrationService;

    /**
     * Create orchestration
     */
    @PostMapping
    public ResponseEntity<ReportOrchestration> createOrchestration(
            @RequestBody ReportOrchestration orchestration) {
        log.info("POST /api/orchestration - Creating orchestration: {}", orchestration.getWorkflowName());

        try {
            ReportOrchestration created = orchestrationService.createOrchestration(orchestration);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating orchestration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get orchestration
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportOrchestration> getOrchestration(@PathVariable Long id) {
        log.info("GET /api/orchestration/{}", id);

        try {
            return orchestrationService.getOrchestration(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add task to workflow
     */
    @PostMapping("/{id}/task")
    public ResponseEntity<ReportOrchestration.WorkflowTask> addTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/orchestration/{}/task", id);

        try {
            String taskName = (String) request.get("taskName");
            String typeStr = (String) request.get("type");
            String priorityStr = (String) request.get("priority");

            ReportOrchestration.TaskType type = ReportOrchestration.TaskType.valueOf(typeStr);
            ReportOrchestration.TaskPriority priority = priorityStr != null ?
                    ReportOrchestration.TaskPriority.valueOf(priorityStr) : null;

            ReportOrchestration.WorkflowTask task = orchestrationService.addTask(
                    id, taskName, type, priority
            );

            return ResponseEntity.ok(task);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding task to orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add task dependency
     */
    @PostMapping("/{id}/task/{taskId}/dependency")
    public ResponseEntity<Map<String, Object>> addTaskDependency(
            @PathVariable Long id,
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/orchestration/{}/task/{}/dependency", id, taskId);

        try {
            String dependsOnTaskId = request.get("dependsOnTaskId");

            orchestrationService.addTaskDependency(id, taskId, dependsOnTaskId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task dependency added");
            response.put("orchestrationId", id);
            response.put("taskId", taskId);
            response.put("dependsOnTaskId", dependsOnTaskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration or task not found: {}, {}", id, taskId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding task dependency in orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate workflow
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateWorkflow(@PathVariable Long id) {
        log.info("POST /api/orchestration/{}/validate", id);

        try {
            boolean valid = orchestrationService.validateWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", valid);
            response.put("orchestrationId", id);

            if (valid) {
                response.put("message", "Workflow validated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Workflow validation failed");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error validating workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start workflow
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startWorkflow(@PathVariable Long id) {
        log.info("POST /api/orchestration/{}/start", id);

        try {
            orchestrationService.startWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow started");
            response.put("orchestrationId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid workflow state for start: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error starting workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Pause workflow
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseWorkflow(@PathVariable Long id) {
        log.info("POST /api/orchestration/{}/pause", id);

        try {
            orchestrationService.pauseWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow paused");
            response.put("orchestrationId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error pausing workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resume workflow
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resumeWorkflow(@PathVariable Long id) {
        log.info("POST /api/orchestration/{}/resume", id);

        try {
            orchestrationService.resumeWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow resumed");
            response.put("orchestrationId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resuming workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel workflow
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelWorkflow(@PathVariable Long id) {
        log.info("POST /api/orchestration/{}/cancel", id);

        try {
            orchestrationService.cancelWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow cancelled");
            response.put("orchestrationId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error cancelling workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute task
     */
    @PostMapping("/{id}/task/{taskId}/execute")
    public ResponseEntity<Map<String, Object>> executeTask(
            @PathVariable Long id,
            @PathVariable String taskId) {
        log.info("POST /api/orchestration/{}/task/{}/execute", id, taskId);

        try {
            orchestrationService.executeTask(id, taskId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task execution started");
            response.put("orchestrationId", id);
            response.put("taskId", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration or task not found: {}, {}", id, taskId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing task in orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete task
     */
    @PostMapping("/{id}/task/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable Long id,
            @PathVariable String taskId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/orchestration/{}/task/{}/complete", id, taskId);

        try {
            Boolean success = (Boolean) request.get("success");
            if (success == null) {
                success = true;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> results = (Map<String, Object>) request.get("results");

            orchestrationService.completeTask(id, taskId, success, results);

            Map<String, Object> response = new HashMap<>();
            response.put("message", success ? "Task completed successfully" : "Task failed");
            response.put("orchestrationId", id);
            response.put("taskId", taskId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration or task not found: {}, {}", id, taskId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing task in orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update task configuration
     */
    @PutMapping("/{id}/task/{taskId}/config")
    public ResponseEntity<Map<String, Object>> updateTaskConfiguration(
            @PathVariable Long id,
            @PathVariable String taskId,
            @RequestBody Map<String, Object> configuration) {
        log.info("PUT /api/orchestration/{}/task/{}/config", id, taskId);

        try {
            orchestrationService.updateTaskConfiguration(id, taskId, configuration);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task configuration updated");
            response.put("orchestrationId", id);
            response.put("taskId", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration or task not found: {}, {}", id, taskId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating task configuration in orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add trigger
     */
    @PostMapping("/{id}/trigger")
    public ResponseEntity<Map<String, Object>> addTrigger(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/orchestration/{}/trigger", id);

        try {
            String triggerName = request.get("triggerName");
            String triggerType = request.get("triggerType");
            String expression = request.get("expression");

            orchestrationService.addTrigger(id, triggerName, triggerType, expression);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Trigger added");
            response.put("orchestrationId", id);
            response.put("triggerName", triggerName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Orchestration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding trigger to orchestration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get ready tasks
     */
    @GetMapping("/{id}/tasks/ready")
    public ResponseEntity<Map<String, Object>> getReadyTasks(@PathVariable Long id) {
        log.info("GET /api/orchestration/{}/tasks/ready", id);

        try {
            return orchestrationService.getOrchestration(id)
                    .map(orchestration -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orchestrationId", id);
                        response.put("tasks", orchestration.getReadyTasks());
                        response.put("count", orchestration.getReadyTasks().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching ready tasks for orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get running tasks
     */
    @GetMapping("/{id}/tasks/running")
    public ResponseEntity<Map<String, Object>> getRunningTasks(@PathVariable Long id) {
        log.info("GET /api/orchestration/{}/tasks/running", id);

        try {
            return orchestrationService.getOrchestration(id)
                    .map(orchestration -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orchestrationId", id);
                        response.put("tasks", orchestration.getRunningTasks());
                        response.put("count", orchestration.getRunningTasks().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching running tasks for orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get failed tasks
     */
    @GetMapping("/{id}/tasks/failed")
    public ResponseEntity<Map<String, Object>> getFailedTasks(@PathVariable Long id) {
        log.info("GET /api/orchestration/{}/tasks/failed", id);

        try {
            return orchestrationService.getOrchestration(id)
                    .map(orchestration -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orchestrationId", id);
                        response.put("tasks", orchestration.getFailedTasks());
                        response.put("count", orchestration.getFailedTasks().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching failed tasks for orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit trail
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<Map<String, Object>> getAuditTrail(@PathVariable Long id) {
        log.info("GET /api/orchestration/{}/events", id);

        try {
            return orchestrationService.getOrchestration(id)
                    .map(orchestration -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orchestrationId", id);
                        response.put("events", orchestration.getAuditTrail());
                        response.put("count", orchestration.getAuditTrail() != null ?
                                orchestration.getAuditTrail().size() : 0);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching audit trail for orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete orchestration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrchestration(@PathVariable Long id) {
        log.info("DELETE /api/orchestration/{}", id);

        try {
            orchestrationService.deleteOrchestration(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Orchestration deleted");
            response.put("orchestrationId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting orchestration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/orchestration/stats");

        try {
            Map<String, Object> stats = orchestrationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching orchestration statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
