package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Workflow DTO
 *
 * Represents automated workflow and business process for reports.
 *
 * Features:
 * - Multi-step workflows
 * - Approval chains
 * - Automated actions
 * - Conditional logic
 * - Task assignment
 * - Notifications
 * - SLA tracking
 *
 * Workflow Types:
 * - Approval workflow
 * - Review workflow
 * - Publishing workflow
 * - Distribution workflow
 * - Archival workflow
 * - Custom workflow
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 80 - Report Workflows & Automation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportWorkflow {

    /**
     * Workflow type enumeration
     */
    public enum WorkflowType {
        APPROVAL,           // Approval workflow
        REVIEW,             // Review workflow
        PUBLISHING,         // Publishing workflow
        DISTRIBUTION,       // Distribution workflow
        ARCHIVAL,           // Archival workflow
        NOTIFICATION,       // Notification workflow
        VALIDATION,         // Validation workflow
        TRANSFORMATION,     // Data transformation workflow
        CUSTOM              // Custom workflow
    }

    /**
     * Workflow status enumeration
     */
    public enum WorkflowStatus {
        DRAFT,              // Draft state
        ACTIVE,             // Active and running
        PAUSED,             // Paused
        COMPLETED,          // Completed successfully
        FAILED,             // Failed
        CANCELLED,          // Cancelled
        EXPIRED             // Expired
    }

    /**
     * Step status enumeration
     */
    public enum StepStatus {
        PENDING,            // Pending execution
        IN_PROGRESS,        // Currently executing
        COMPLETED,          // Completed successfully
        FAILED,             // Failed
        SKIPPED,            // Skipped
        CANCELLED           // Cancelled
    }

    /**
     * Action type enumeration
     */
    public enum ActionType {
        APPROVE,            // Approve action
        REJECT,             // Reject action
        REVIEW,             // Review action
        PUBLISH,            // Publish action
        DISTRIBUTE,         // Distribute action
        NOTIFY,             // Notify action
        TRANSFORM,          // Transform data
        VALIDATE,           // Validate data
        EXECUTE_SCRIPT,     // Execute script
        CALL_API,           // Call external API
        SEND_EMAIL,         // Send email
        CUSTOM              // Custom action
    }

    /**
     * Trigger type enumeration
     */
    public enum TriggerType {
        MANUAL,             // Manual trigger
        SCHEDULED,          // Scheduled trigger
        EVENT,              // Event-based trigger
        THRESHOLD,          // Threshold-based trigger
        COMPLETION,         // On completion trigger
        FAILURE,            // On failure trigger
        CUSTOM              // Custom trigger
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Workflow ID
     */
    private Long workflowId;

    /**
     * Workflow name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Workflow type
     */
    private WorkflowType workflowType;

    /**
     * Status
     */
    private WorkflowStatus status;

    /**
     * Version
     */
    private String version;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Report ID (if specific to a report)
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    // ============================================================
    // Workflow Configuration
    // ============================================================

    /**
     * Enabled
     */
    private Boolean enabled;

    /**
     * Auto-start
     */
    private Boolean autoStart;

    /**
     * Steps
     */
    private List<WorkflowStep> steps;

    /**
     * Current step index
     */
    private Integer currentStepIndex;

    /**
     * Current step
     */
    private WorkflowStep currentStep;

    /**
     * Parallel execution enabled
     */
    private Boolean parallelExecutionEnabled;

    /**
     * Allow step skip
     */
    private Boolean allowSkipSteps;

    /**
     * Allow step retry
     */
    private Boolean allowRetry;

    /**
     * Max retry attempts
     */
    private Integer maxRetryAttempts;

    /**
     * Retry delay (minutes)
     */
    private Integer retryDelayMinutes;

    // ============================================================
    // Triggers
    // ============================================================

    /**
     * Trigger type
     */
    private TriggerType triggerType;

    /**
     * Trigger condition
     */
    private String triggerCondition;

    /**
     * Cron expression (for scheduled trigger)
     */
    private String cronExpression;

    /**
     * Event name (for event trigger)
     */
    private String eventName;

    /**
     * Threshold value (for threshold trigger)
     */
    private Double thresholdValue;

    /**
     * Last triggered at
     */
    private LocalDateTime lastTriggeredAt;

    /**
     * Next trigger at
     */
    private LocalDateTime nextTriggerAt;

    /**
     * Trigger count
     */
    private Integer triggerCount;

    // ============================================================
    // Execution Tracking
    // ============================================================

    /**
     * Started at
     */
    private LocalDateTime startedAt;

    /**
     * Completed at
     */
    private LocalDateTime completedAt;

    /**
     * Duration (milliseconds)
     */
    private Long durationMs;

    /**
     * Progress percentage
     */
    private Double progressPercentage;

    /**
     * Completed steps
     */
    private Integer completedSteps;

    /**
     * Total steps
     */
    private Integer totalSteps;

    /**
     * Failed steps
     */
    private Integer failedSteps;

    /**
     * Skipped steps
     */
    private Integer skippedSteps;

    /**
     * Execution count
     */
    private Integer executionCount;

    /**
     * Success count
     */
    private Integer successCount;

    /**
     * Failure count
     */
    private Integer failureCount;

    // ============================================================
    // SLA & Deadlines
    // ============================================================

    /**
     * SLA enabled
     */
    private Boolean slaEnabled;

    /**
     * SLA duration (hours)
     */
    private Integer slaDurationHours;

    /**
     * Due date
     */
    private LocalDateTime dueDate;

    /**
     * Is overdue
     */
    private Boolean isOverdue;

    /**
     * Time remaining (minutes)
     */
    private Long timeRemainingMinutes;

    /**
     * Escalation enabled
     */
    private Boolean escalationEnabled;

    /**
     * Escalation time (hours)
     */
    private Integer escalationTimeHours;

    /**
     * Escalated
     */
    private Boolean escalated;

    /**
     * Escalated at
     */
    private LocalDateTime escalatedAt;

    /**
     * Escalated to
     */
    private String escalatedTo;

    // ============================================================
    // Approvals
    // ============================================================

    /**
     * Requires approval
     */
    private Boolean requiresApproval;

    /**
     * Approvers
     */
    private List<String> approvers;

    /**
     * Approval type (ANY, ALL, MAJORITY)
     */
    private String approvalType;

    /**
     * Approvals received
     */
    private Integer approvalsReceived;

    /**
     * Approvals required
     */
    private Integer approvalsRequired;

    /**
     * Approved by
     */
    private List<String> approvedBy;

    /**
     * Rejected by
     */
    private List<String> rejectedBy;

    /**
     * Approval comments
     */
    private List<ApprovalComment> approvalComments;

    // ============================================================
    // Error Handling
    // ============================================================

    /**
     * Last error
     */
    private String lastError;

    /**
     * Last error at
     */
    private LocalDateTime lastErrorAt;

    /**
     * Error count
     */
    private Integer errorCount;

    /**
     * On error action
     */
    private String onErrorAction;

    /**
     * Rollback on error
     */
    private Boolean rollbackOnError;

    /**
     * Continue on error
     */
    private Boolean continueOnError;

    // ============================================================
    // Notifications
    // ============================================================

    /**
     * Notification enabled
     */
    private Boolean notificationEnabled;

    /**
     * Notify on start
     */
    private Boolean notifyOnStart;

    /**
     * Notify on completion
     */
    private Boolean notifyOnCompletion;

    /**
     * Notify on failure
     */
    private Boolean notifyOnFailure;

    /**
     * Notification recipients
     */
    private List<String> notificationRecipients;

    /**
     * Notification channels
     */
    private List<String> notificationChannels;

    // ============================================================
    // Variables & Context
    // ============================================================

    /**
     * Input variables
     */
    private Map<String, Object> inputVariables;

    /**
     * Output variables
     */
    private Map<String, Object> outputVariables;

    /**
     * Context data
     */
    private Map<String, Object> contextData;

    /**
     * Execution metadata
     */
    private Map<String, Object> executionMetadata;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom attributes
     */
    private Map<String, Object> customAttributes;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Workflow step
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStep {
        private String stepId;
        private String stepName;
        private String description;
        private Integer stepOrder;
        private StepStatus status;
        private ActionType actionType;
        private String actionConfig;
        private Map<String, Object> actionParameters;
        private String assignedTo;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationMs;
        private Boolean required;
        private String condition;
        private Integer retryAttempts;
        private String errorMessage;
        private Map<String, Object> stepData;
    }

    /**
     * Approval comment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalComment {
        private String commentId;
        private String username;
        private String action;
        private String comment;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
    }

    /**
     * Workflow execution log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowExecutionLog {
        private String logId;
        private LocalDateTime timestamp;
        private String stepId;
        private String stepName;
        private String action;
        private String message;
        private String severity;
        private Map<String, Object> logData;
    }

    /**
     * Workflow transition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTransition {
        private String transitionId;
        private String fromStepId;
        private String toStepId;
        private String condition;
        private LocalDateTime transitionedAt;
        private String transitionedBy;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if workflow is active
     */
    public boolean isActive() {
        return status == WorkflowStatus.ACTIVE;
    }

    /**
     * Check if workflow is completed
     */
    public boolean isCompleted() {
        return status == WorkflowStatus.COMPLETED;
    }

    /**
     * Check if workflow is failed
     */
    public boolean isFailed() {
        return status == WorkflowStatus.FAILED;
    }

    /**
     * Check if overdue
     */
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Calculate progress
     */
    public void calculateProgress() {
        if (totalSteps != null && totalSteps > 0 && completedSteps != null) {
            progressPercentage = (completedSteps.doubleValue() / totalSteps) * 100.0;
        }
    }

    /**
     * Calculate time remaining
     */
    public void calculateTimeRemaining() {
        if (dueDate != null) {
            long minutes = java.time.Duration.between(LocalDateTime.now(), dueDate).toMinutes();
            timeRemainingMinutes = minutes > 0 ? minutes : 0;
            isOverdue = minutes <= 0;
        }
    }

    /**
     * Check if needs escalation
     */
    public boolean needsEscalation() {
        if (!Boolean.TRUE.equals(escalationEnabled) || escalationTimeHours == null) {
            return false;
        }

        if (Boolean.TRUE.equals(escalated)) {
            return false;
        }

        if (startedAt == null) {
            return false;
        }

        LocalDateTime escalationTime = startedAt.plusHours(escalationTimeHours);
        return LocalDateTime.now().isAfter(escalationTime);
    }

    /**
     * Start workflow
     */
    public void start() {
        status = WorkflowStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        currentStepIndex = 0;

        if (slaDurationHours != null) {
            dueDate = startedAt.plusHours(slaDurationHours);
        }

        executionCount = (executionCount != null ? executionCount : 0) + 1;
    }

    /**
     * Complete workflow
     */
    public void complete() {
        status = WorkflowStatus.COMPLETED;
        completedAt = LocalDateTime.now();

        if (startedAt != null) {
            durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }

        successCount = (successCount != null ? successCount : 0) + 1;
        calculateProgress();
    }

    /**
     * Fail workflow
     */
    public void fail(String error) {
        status = WorkflowStatus.FAILED;
        completedAt = LocalDateTime.now();
        lastError = error;
        lastErrorAt = LocalDateTime.now();
        errorCount = (errorCount != null ? errorCount : 0) + 1;
        failureCount = (failureCount != null ? failureCount : 0) + 1;

        if (startedAt != null) {
            durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Cancel workflow
     */
    public void cancel() {
        status = WorkflowStatus.CANCELLED;
        completedAt = LocalDateTime.now();

        if (startedAt != null) {
            durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Pause workflow
     */
    public void pause() {
        status = WorkflowStatus.PAUSED;
    }

    /**
     * Resume workflow
     */
    public void resume() {
        if (status == WorkflowStatus.PAUSED) {
            status = WorkflowStatus.ACTIVE;
        }
    }

    /**
     * Escalate workflow
     */
    public void escalate(String escalateTo) {
        escalated = true;
        escalatedAt = LocalDateTime.now();
        escalatedTo = escalateTo;
    }

    /**
     * Add approval
     */
    public void addApproval(String username, boolean approved, String comment) {
        if (approved) {
            if (approvedBy == null) {
                approvedBy = new java.util.ArrayList<>();
            }
            approvedBy.add(username);
            approvalsReceived = (approvalsReceived != null ? approvalsReceived : 0) + 1;
        } else {
            if (rejectedBy == null) {
                rejectedBy = new java.util.ArrayList<>();
            }
            rejectedBy.add(username);
        }

        ApprovalComment approvalComment = ApprovalComment.builder()
                .commentId(java.util.UUID.randomUUID().toString())
                .username(username)
                .action(approved ? "APPROVED" : "REJECTED")
                .comment(comment)
                .timestamp(LocalDateTime.now())
                .build();

        if (approvalComments == null) {
            approvalComments = new java.util.ArrayList<>();
        }
        approvalComments.add(approvalComment);
    }

    /**
     * Check if approval requirement met
     */
    public boolean isApprovalRequirementMet() {
        if (!Boolean.TRUE.equals(requiresApproval)) {
            return true;
        }

        if (approvalsReceived == null || approvalsRequired == null) {
            return false;
        }

        if ("ANY".equals(approvalType)) {
            return approvalsReceived >= 1;
        } else if ("MAJORITY".equals(approvalType)) {
            return approvalsReceived > (approvalsRequired / 2);
        } else { // ALL
            return approvalsReceived >= approvalsRequired;
        }
    }

    /**
     * Move to next step
     */
    public void moveToNextStep() {
        if (currentStepIndex != null && steps != null && currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            currentStep = steps.get(currentStepIndex);
        }
    }

    /**
     * Get workflow duration in seconds
     */
    public Long getDurationInSeconds() {
        if (durationMs == null) {
            return null;
        }
        return durationMs / 1000;
    }

    /**
     * Get success rate
     */
    public Double getSuccessRate() {
        if (executionCount == null || executionCount == 0) {
            return 0.0;
        }
        int success = successCount != null ? successCount : 0;
        return (success * 100.0) / executionCount;
    }
}
