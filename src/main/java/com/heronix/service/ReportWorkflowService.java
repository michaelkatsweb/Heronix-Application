package com.heronix.service;

import com.heronix.dto.ReportWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Workflow Service
 *
 * Manages automated workflows and business processes for reports.
 *
 * Features:
 * - Multi-step workflow execution
 * - Approval chains
 * - Task automation
 * - SLA tracking
 * - Error handling and retry
 * - Notification integration
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 80 - Report Workflows & Automation
 */
@Service
@Slf4j
public class ReportWorkflowService {

    private final Map<Long, ReportWorkflow> workflows = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportWorkflow.WorkflowExecutionLog>> executionLogs = new ConcurrentHashMap<>();
    private Long nextWorkflowId = 1L;

    /**
     * Create workflow
     */
    public ReportWorkflow createWorkflow(ReportWorkflow workflow) {
        synchronized (this) {
            workflow.setWorkflowId(nextWorkflowId++);
            workflow.setCreatedAt(LocalDateTime.now());
            workflow.setStatus(ReportWorkflow.WorkflowStatus.DRAFT);
            workflow.setExecutionCount(0);
            workflow.setSuccessCount(0);
            workflow.setFailureCount(0);
            workflow.setTriggerCount(0);

            // Initialize step counts
            if (workflow.getSteps() != null) {
                workflow.setTotalSteps(workflow.getSteps().size());
                workflow.setCompletedSteps(0);
                workflow.setFailedSteps(0);
                workflow.setSkippedSteps(0);
            }

            workflows.put(workflow.getWorkflowId(), workflow);

            log.info("Created workflow {} - Type: {}, Steps: {}",
                    workflow.getWorkflowId(),
                    workflow.getWorkflowType(),
                    workflow.getTotalSteps());

            logExecution(workflow.getWorkflowId(), null, "WORKFLOW_CREATED",
                    "Workflow created", "INFO");

            // Auto-start if configured
            if (Boolean.TRUE.equals(workflow.getAutoStart())) {
                startWorkflow(workflow.getWorkflowId());
            }

            return workflow;
        }
    }

    /**
     * Get workflow
     */
    public Optional<ReportWorkflow> getWorkflow(Long workflowId) {
        return Optional.ofNullable(workflows.get(workflowId));
    }

    /**
     * Get workflows by report
     */
    public List<ReportWorkflow> getWorkflowsByReport(Long reportId) {
        return workflows.values().stream()
                .filter(w -> reportId.equals(w.getReportId()))
                .collect(Collectors.toList());
    }

    /**
     * Get workflows by status
     */
    public List<ReportWorkflow> getWorkflowsByStatus(ReportWorkflow.WorkflowStatus status) {
        return workflows.values().stream()
                .filter(w -> w.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Get workflows by type
     */
    public List<ReportWorkflow> getWorkflowsByType(ReportWorkflow.WorkflowType type) {
        return workflows.values().stream()
                .filter(w -> w.getWorkflowType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Start workflow
     */
    public void startWorkflow(Long workflowId) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        if (!Boolean.TRUE.equals(workflow.getEnabled())) {
            throw new IllegalStateException("Workflow is not enabled");
        }

        workflow.start();
        workflow.setTriggerCount((workflow.getTriggerCount() != null ? workflow.getTriggerCount() : 0) + 1);
        workflow.setLastTriggeredAt(LocalDateTime.now());

        log.info("Started workflow {}", workflowId);

        logExecution(workflowId, null, "WORKFLOW_STARTED",
                "Workflow execution started", "INFO");

        // Execute first step
        if (workflow.getSteps() != null && !workflow.getSteps().isEmpty()) {
            executeNextStep(workflowId);
        } else {
            workflow.complete();
            log.info("Workflow {} completed (no steps)", workflowId);
        }
    }

    /**
     * Execute next step
     */
    private void executeNextStep(Long workflowId) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null || !workflow.isActive()) {
            return;
        }

        if (workflow.getCurrentStepIndex() == null || workflow.getCurrentStepIndex() >= workflow.getTotalSteps()) {
            // All steps completed
            workflow.complete();
            log.info("Workflow {} completed successfully", workflowId);

            logExecution(workflowId, null, "WORKFLOW_COMPLETED",
                    "Workflow execution completed successfully", "INFO");
            return;
        }

        ReportWorkflow.WorkflowStep step = workflow.getSteps().get(workflow.getCurrentStepIndex());
        executeStep(workflowId, step);
    }

    /**
     * Execute workflow step
     */
    private void executeStep(Long workflowId, ReportWorkflow.WorkflowStep step) {
        ReportWorkflow workflow = workflows.get(workflowId);

        step.setStatus(ReportWorkflow.StepStatus.IN_PROGRESS);
        step.setStartedAt(LocalDateTime.now());

        log.info("Executing workflow {} step: {} ({})", workflowId, step.getStepName(), step.getActionType());

        logExecution(workflowId, step.getStepId(), "STEP_STARTED",
                "Step execution started: " + step.getStepName(), "INFO");

        try {
            // Check condition if specified
            if (step.getCondition() != null && !evaluateCondition(step.getCondition(), workflow)) {
                step.setStatus(ReportWorkflow.StepStatus.SKIPPED);
                workflow.setSkippedSteps((workflow.getSkippedSteps() != null ? workflow.getSkippedSteps() : 0) + 1);

                log.info("Skipped step {} due to condition", step.getStepName());

                logExecution(workflowId, step.getStepId(), "STEP_SKIPPED",
                        "Step skipped: " + step.getStepName(), "INFO");

                workflow.moveToNextStep();
                executeNextStep(workflowId);
                return;
            }

            // Execute action based on type
            boolean success = executeAction(step, workflow);

            if (success) {
                step.setStatus(ReportWorkflow.StepStatus.COMPLETED);
                step.setCompletedAt(LocalDateTime.now());
                step.setDurationMs(java.time.Duration.between(step.getStartedAt(), step.getCompletedAt()).toMillis());

                workflow.setCompletedSteps((workflow.getCompletedSteps() != null ? workflow.getCompletedSteps() : 0) + 1);
                workflow.calculateProgress();

                log.info("Completed step {} in {} ms", step.getStepName(), step.getDurationMs());

                logExecution(workflowId, step.getStepId(), "STEP_COMPLETED",
                        "Step completed: " + step.getStepName(), "INFO");

                // Move to next step
                workflow.moveToNextStep();
                executeNextStep(workflowId);

            } else {
                handleStepFailure(workflowId, step, "Step execution failed");
            }

        } catch (Exception e) {
            handleStepFailure(workflowId, step, e.getMessage());
        }
    }

    /**
     * Execute action
     */
    private boolean executeAction(ReportWorkflow.WorkflowStep step, ReportWorkflow workflow) {
        // Simulate action execution (in real implementation, execute actual actions)
        log.debug("Executing action: {} for step: {}", step.getActionType(), step.getStepName());

        return switch (step.getActionType()) {
            case APPROVE, REVIEW, PUBLISH, DISTRIBUTE, NOTIFY, VALIDATE -> true;
            case EXECUTE_SCRIPT -> executeScript(step);
            case CALL_API -> callExternalAPI(step);
            case SEND_EMAIL -> sendEmail(step);
            default -> true;
        };
    }

    /**
     * Execute script action
     */
    private boolean executeScript(ReportWorkflow.WorkflowStep step) {
        log.info("Executing script for step: {}", step.getStepName());
        // Simulate script execution
        return true;
    }

    /**
     * Call external API
     */
    private boolean callExternalAPI(ReportWorkflow.WorkflowStep step) {
        log.info("Calling external API for step: {}", step.getStepName());
        // Simulate API call
        return true;
    }

    /**
     * Send email
     */
    private boolean sendEmail(ReportWorkflow.WorkflowStep step) {
        log.info("Sending email for step: {}", step.getStepName());
        // Simulate email sending
        return true;
    }

    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(String condition, ReportWorkflow workflow) {
        // Simplified condition evaluation (in real implementation, use expression evaluator)
        log.debug("Evaluating condition: {}", condition);
        return true;
    }

    /**
     * Handle step failure
     */
    private void handleStepFailure(Long workflowId, ReportWorkflow.WorkflowStep step, String errorMessage) {
        ReportWorkflow workflow = workflows.get(workflowId);

        step.setStatus(ReportWorkflow.StepStatus.FAILED);
        step.setErrorMessage(errorMessage);
        step.setCompletedAt(LocalDateTime.now());

        workflow.setFailedSteps((workflow.getFailedSteps() != null ? workflow.getFailedSteps() : 0) + 1);

        log.error("Step {} failed: {}", step.getStepName(), errorMessage);

        logExecution(workflowId, step.getStepId(), "STEP_FAILED",
                "Step failed: " + step.getStepName() + " - " + errorMessage, "ERROR");

        // Check retry
        if (Boolean.TRUE.equals(workflow.getAllowRetry())) {
            int currentRetries = step.getRetryAttempts() != null ? step.getRetryAttempts() : 0;
            int maxRetries = workflow.getMaxRetryAttempts() != null ? workflow.getMaxRetryAttempts() : 0;

            if (currentRetries < maxRetries) {
                step.setRetryAttempts(currentRetries + 1);
                step.setStatus(ReportWorkflow.StepStatus.PENDING);

                log.info("Retrying step {} (attempt {}/{})", step.getStepName(), currentRetries + 1, maxRetries);

                logExecution(workflowId, step.getStepId(), "STEP_RETRY",
                        "Retrying step: " + step.getStepName(), "WARNING");

                executeStep(workflowId, step);
                return;
            }
        }

        // Check continue on error
        if (Boolean.TRUE.equals(workflow.getContinueOnError())) {
            log.warn("Continuing workflow despite step failure");
            workflow.moveToNextStep();
            executeNextStep(workflowId);
        } else {
            // Fail entire workflow
            workflow.fail(errorMessage);

            log.error("Workflow {} failed due to step failure", workflowId);

            logExecution(workflowId, null, "WORKFLOW_FAILED",
                    "Workflow failed: " + errorMessage, "ERROR");
        }
    }

    /**
     * Pause workflow
     */
    public void pauseWorkflow(Long workflowId) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        workflow.pause();

        log.info("Paused workflow {}", workflowId);

        logExecution(workflowId, null, "WORKFLOW_PAUSED",
                "Workflow paused", "INFO");
    }

    /**
     * Resume workflow
     */
    public void resumeWorkflow(Long workflowId) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        workflow.resume();

        log.info("Resumed workflow {}", workflowId);

        logExecution(workflowId, null, "WORKFLOW_RESUMED",
                "Workflow resumed", "INFO");

        executeNextStep(workflowId);
    }

    /**
     * Cancel workflow
     */
    public void cancelWorkflow(Long workflowId) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        workflow.cancel();

        log.info("Cancelled workflow {}", workflowId);

        logExecution(workflowId, null, "WORKFLOW_CANCELLED",
                "Workflow cancelled", "WARNING");
    }

    /**
     * Add approval
     */
    public void addApproval(Long workflowId, String username, boolean approved, String comment) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        workflow.addApproval(username, approved, comment);

        log.info("Approval {} by {} for workflow {}", approved ? "granted" : "rejected", username, workflowId);

        logExecution(workflowId, null, approved ? "APPROVAL_GRANTED" : "APPROVAL_REJECTED",
                "Approval " + (approved ? "granted" : "rejected") + " by " + username, "INFO");

        // Check if approval requirement met
        if (workflow.isApprovalRequirementMet()) {
            log.info("Approval requirement met for workflow {}", workflowId);

            // Continue workflow execution
            if (workflow.getStatus() == ReportWorkflow.WorkflowStatus.ACTIVE) {
                executeNextStep(workflowId);
            }
        }
    }

    /**
     * Escalate workflow
     */
    public void escalateWorkflow(Long workflowId, String escalateTo) {
        ReportWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        workflow.escalate(escalateTo);

        log.warn("Escalated workflow {} to {}", workflowId, escalateTo);

        logExecution(workflowId, null, "WORKFLOW_ESCALATED",
                "Workflow escalated to " + escalateTo, "WARNING");
    }

    /**
     * Get execution logs
     */
    public List<ReportWorkflow.WorkflowExecutionLog> getExecutionLogs(Long workflowId) {
        return executionLogs.getOrDefault(workflowId, new ArrayList<>());
    }

    /**
     * Log execution
     */
    private void logExecution(Long workflowId, String stepId, String action, String message, String severity) {
        ReportWorkflow.WorkflowExecutionLog log = ReportWorkflow.WorkflowExecutionLog.builder()
                .logId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .stepId(stepId)
                .action(action)
                .message(message)
                .severity(severity)
                .build();

        executionLogs.computeIfAbsent(workflowId, k -> new ArrayList<>()).add(log);

        // Keep only last 1000 logs
        List<ReportWorkflow.WorkflowExecutionLog> logs = executionLogs.get(workflowId);
        if (logs.size() > 1000) {
            logs.remove(0);
        }
    }

    /**
     * Get active workflows
     */
    public List<ReportWorkflow> getActiveWorkflows() {
        return getWorkflowsByStatus(ReportWorkflow.WorkflowStatus.ACTIVE);
    }

    /**
     * Get overdue workflows
     */
    public List<ReportWorkflow> getOverdueWorkflows() {
        return workflows.values().stream()
                .filter(ReportWorkflow::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Get workflows needing escalation
     */
    public List<ReportWorkflow> getWorkflowsNeedingEscalation() {
        return workflows.values().stream()
                .filter(ReportWorkflow::needsEscalation)
                .collect(Collectors.toList());
    }

    /**
     * Delete workflow
     */
    public void deleteWorkflow(Long workflowId) {
        ReportWorkflow removed = workflows.remove(workflowId);
        if (removed != null) {
            executionLogs.remove(workflowId);

            log.info("Deleted workflow {}", workflowId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalWorkflows", workflows.size());
        stats.put("activeWorkflows", getActiveWorkflows().size());
        stats.put("completedWorkflows", getWorkflowsByStatus(ReportWorkflow.WorkflowStatus.COMPLETED).size());
        stats.put("failedWorkflows", getWorkflowsByStatus(ReportWorkflow.WorkflowStatus.FAILED).size());
        stats.put("overdueWorkflows", getOverdueWorkflows().size());

        double avgSuccessRate = workflows.values().stream()
                .filter(w -> w.getSuccessRate() != null)
                .mapToDouble(ReportWorkflow::getSuccessRate)
                .average()
                .orElse(0.0);

        stats.put("averageSuccessRate", avgSuccessRate);

        // Count by type
        Map<ReportWorkflow.WorkflowType, Long> byType = workflows.values().stream()
                .collect(Collectors.groupingBy(ReportWorkflow::getWorkflowType, Collectors.counting()));
        stats.put("workflowsByType", byType);

        return stats;
    }
}
