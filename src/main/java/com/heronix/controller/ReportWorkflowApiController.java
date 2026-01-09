package com.heronix.controller;

import com.heronix.dto.ReportWorkflow;
import com.heronix.service.ReportWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Workflow API Controller
 *
 * REST API endpoints for workflow management and automation.
 *
 * Endpoints:
 * - POST /api/workflows - Create workflow
 * - GET /api/workflows/{id} - Get workflow
 * - GET /api/workflows - Get all workflows
 * - GET /api/workflows/report/{reportId} - Get workflows by report
 * - GET /api/workflows/status/{status} - Get workflows by status
 * - GET /api/workflows/type/{type} - Get workflows by type
 * - POST /api/workflows/{id}/start - Start workflow
 * - POST /api/workflows/{id}/pause - Pause workflow
 * - POST /api/workflows/{id}/resume - Resume workflow
 * - POST /api/workflows/{id}/cancel - Cancel workflow
 * - POST /api/workflows/{id}/approve - Add approval
 * - POST /api/workflows/{id}/escalate - Escalate workflow
 * - GET /api/workflows/{id}/logs - Get execution logs
 * - GET /api/workflows/active - Get active workflows
 * - GET /api/workflows/overdue - Get overdue workflows
 * - DELETE /api/workflows/{id} - Delete workflow
 * - GET /api/workflows/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 80 - Report Workflows & Automation
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Slf4j
public class ReportWorkflowApiController {

    private final ReportWorkflowService workflowService;

    /**
     * Create workflow
     */
    @PostMapping
    public ResponseEntity<ReportWorkflow> createWorkflow(@RequestBody ReportWorkflow workflow) {
        log.info("POST /api/workflows - Creating workflow: {}", workflow.getName());

        try {
            ReportWorkflow created = workflowService.createWorkflow(workflow);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating workflow", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get workflow
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportWorkflow> getWorkflow(@PathVariable Long id) {
        log.info("GET /api/workflows/{}", id);

        try {
            return workflowService.getWorkflow(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all workflows
     */
    @GetMapping
    public ResponseEntity<List<ReportWorkflow>> getAllWorkflows() {
        log.info("GET /api/workflows");

        try {
            List<ReportWorkflow> workflows = workflowService.getActiveWorkflows();
            return ResponseEntity.ok(workflows);

        } catch (Exception e) {
            log.error("Error fetching workflows", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get workflows by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportWorkflow>> getWorkflowsByReport(@PathVariable Long reportId) {
        log.info("GET /api/workflows/report/{}", reportId);

        try {
            List<ReportWorkflow> workflows = workflowService.getWorkflowsByReport(reportId);
            return ResponseEntity.ok(workflows);

        } catch (Exception e) {
            log.error("Error fetching workflows for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get workflows by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportWorkflow>> getWorkflowsByStatus(@PathVariable String status) {
        log.info("GET /api/workflows/status/{}", status);

        try {
            ReportWorkflow.WorkflowStatus workflowStatus =
                    ReportWorkflow.WorkflowStatus.valueOf(status.toUpperCase());
            List<ReportWorkflow> workflows = workflowService.getWorkflowsByStatus(workflowStatus);
            return ResponseEntity.ok(workflows);

        } catch (IllegalArgumentException e) {
            log.error("Invalid workflow status: {}", status);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching workflows by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get workflows by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportWorkflow>> getWorkflowsByType(@PathVariable String type) {
        log.info("GET /api/workflows/type/{}", type);

        try {
            ReportWorkflow.WorkflowType workflowType =
                    ReportWorkflow.WorkflowType.valueOf(type.toUpperCase());
            List<ReportWorkflow> workflows = workflowService.getWorkflowsByType(workflowType);
            return ResponseEntity.ok(workflows);

        } catch (IllegalArgumentException e) {
            log.error("Invalid workflow type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching workflows by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start workflow
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startWorkflow(@PathVariable Long id) {
        log.info("POST /api/workflows/{}/start", id);

        try {
            workflowService.startWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow started");
            response.put("workflowId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Workflow not enabled: {}", id);
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
        log.info("POST /api/workflows/{}/pause", id);

        try {
            workflowService.pauseWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow paused");
            response.put("workflowId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
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
        log.info("POST /api/workflows/{}/resume", id);

        try {
            workflowService.resumeWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow resumed");
            response.put("workflowId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
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
        log.info("POST /api/workflows/{}/cancel", id);

        try {
            workflowService.cancelWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow cancelled");
            response.put("workflowId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error cancelling workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add approval
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> addApproval(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/workflows/{}/approve", id);

        try {
            String username = (String) request.get("username");
            Boolean approved = (Boolean) request.get("approved");
            String comment = (String) request.get("comment");

            workflowService.addApproval(id, username, approved, comment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Approval recorded");
            response.put("workflowId", id);
            response.put("approved", approved);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding approval: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Escalate workflow
     */
    @PostMapping("/{id}/escalate")
    public ResponseEntity<Map<String, Object>> escalateWorkflow(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/workflows/{}/escalate", id);

        try {
            String escalateTo = request.get("escalateTo");
            workflowService.escalateWorkflow(id, escalateTo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow escalated");
            response.put("workflowId", id);
            response.put("escalatedTo", escalateTo);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Workflow not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error escalating workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get execution logs
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<ReportWorkflow.WorkflowExecutionLog>> getExecutionLogs(@PathVariable Long id) {
        log.info("GET /api/workflows/{}/logs", id);

        try {
            List<ReportWorkflow.WorkflowExecutionLog> logs = workflowService.getExecutionLogs(id);
            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            log.error("Error fetching execution logs: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active workflows
     */
    @GetMapping("/active")
    public ResponseEntity<List<ReportWorkflow>> getActiveWorkflows() {
        log.info("GET /api/workflows/active");

        try {
            List<ReportWorkflow> workflows = workflowService.getActiveWorkflows();
            return ResponseEntity.ok(workflows);

        } catch (Exception e) {
            log.error("Error fetching active workflows", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get overdue workflows
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<ReportWorkflow>> getOverdueWorkflows() {
        log.info("GET /api/workflows/overdue");

        try {
            List<ReportWorkflow> workflows = workflowService.getOverdueWorkflows();
            return ResponseEntity.ok(workflows);

        } catch (Exception e) {
            log.error("Error fetching overdue workflows", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete workflow
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkflow(@PathVariable Long id) {
        log.info("DELETE /api/workflows/{}", id);

        try {
            workflowService.deleteWorkflow(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Workflow deleted");
            response.put("workflowId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting workflow: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/workflows/stats");

        try {
            Map<String, Object> stats = workflowService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching workflow statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
