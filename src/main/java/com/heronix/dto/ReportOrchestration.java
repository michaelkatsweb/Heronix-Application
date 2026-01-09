package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Orchestration DTO
 *
 * Represents workflow orchestration and task execution engine.
 *
 * Features:
 * - Workflow definition and execution
 * - Task dependency management
 * - Parallel and sequential execution
 * - Conditional branching
 * - Error handling and retry logic
 * - Workflow lifecycle management
 * - Task scheduling and prioritization
 * - State management
 *
 * Workflow Status:
 * - DRAFT - Workflow being designed
 * - VALIDATING - Validating workflow definition
 * - READY - Ready for execution
 * - RUNNING - Currently executing
 * - PAUSED - Temporarily paused
 * - COMPLETED - Successfully completed
 * - FAILED - Execution failed
 * - CANCELLED - Cancelled by user
 *
 * Task Type:
 * - DATA_EXTRACTION - Extract data from sources
 * - DATA_TRANSFORMATION - Transform data
 * - REPORT_GENERATION - Generate report
 * - NOTIFICATION - Send notification
 * - APPROVAL - Requires approval
 * - VALIDATION - Validate data/results
 * - INTEGRATION - Call external system
 * - SCRIPT - Execute custom script
 * - CONDITIONAL - Conditional branching
 * - PARALLEL - Parallel task group
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 91 - Report Orchestration & Workflow Engine
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportOrchestration {

    private Long orchestrationId;
    private String workflowName;
    private String workflowDescription;
    private String version;

    // Workflow status
    private WorkflowStatus status;
    private Double progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    private String currentTaskId;
    private Integer retriesAttempted;
    private Integer maxRetries;

    // Tasks
    private List<WorkflowTask> tasks;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer failedTasks;
    private Integer pendingTasks;
    private Map<String, WorkflowTask> taskRegistry;

    // Dependencies
    private Map<String, List<String>> dependencies; // taskId -> list of dependency taskIds
    private List<String> executionOrder;
    private Boolean dependenciesResolved;

    // Execution
    private ExecutionMode executionMode;
    private Integer maxParallelTasks;
    private Integer currentParallelTasks;
    private List<String> runningTasks;
    private Map<String, Object> workflowContext; // Shared data across tasks
    private Map<String, Object> workflowVariables;

    // Error handling
    private ErrorHandlingStrategy errorStrategy;
    private Boolean continueOnError;
    private List<WorkflowError> errors;
    private String lastErrorMessage;
    private LocalDateTime lastErrorAt;

    // Scheduling
    private SchedulingStrategy schedulingStrategy;
    private String cronExpression;
    private LocalDateTime nextExecutionAt;
    private Integer executionCount;
    private LocalDateTime lastExecutionAt;

    // Triggers
    private List<WorkflowTrigger> triggers;
    private Boolean autoStart;
    private Map<String, Object> triggerConditions;

    // Notifications
    private Boolean notificationsEnabled;
    private List<String> notificationRecipients;
    private List<NotificationEvent> notificationEvents;

    // Audit
    private List<WorkflowEvent> auditTrail;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;
    private Integer timeoutSeconds;
    private Boolean rollbackOnFailure;

    /**
     * Workflow Status
     */
    public enum WorkflowStatus {
        DRAFT,          // Workflow being designed
        VALIDATING,     // Validating workflow definition
        READY,          // Ready for execution
        RUNNING,        // Currently executing
        PAUSED,         // Temporarily paused
        COMPLETED,      // Successfully completed
        FAILED,         // Execution failed
        CANCELLED       // Cancelled by user
    }

    /**
     * Task Status
     */
    public enum TaskStatus {
        PENDING,        // Waiting to execute
        WAITING,        // Waiting for dependencies
        READY,          // Ready to execute
        RUNNING,        // Currently executing
        COMPLETED,      // Successfully completed
        FAILED,         // Execution failed
        SKIPPED,        // Skipped due to conditions
        CANCELLED       // Cancelled
    }

    /**
     * Task Type
     */
    public enum TaskType {
        DATA_EXTRACTION,    // Extract data from sources
        DATA_TRANSFORMATION, // Transform data
        REPORT_GENERATION,  // Generate report
        NOTIFICATION,       // Send notification
        APPROVAL,           // Requires approval
        VALIDATION,         // Validate data/results
        INTEGRATION,        // Call external system
        SCRIPT,             // Execute custom script
        CONDITIONAL,        // Conditional branching
        PARALLEL            // Parallel task group
    }

    /**
     * Execution Mode
     */
    public enum ExecutionMode {
        SEQUENTIAL,     // Execute tasks sequentially
        PARALLEL,       // Execute all tasks in parallel
        HYBRID,         // Mix of sequential and parallel
        DYNAMIC         // Determine at runtime
    }

    /**
     * Error Handling Strategy
     */
    public enum ErrorHandlingStrategy {
        FAIL_FAST,      // Stop immediately on error
        CONTINUE,       // Continue despite errors
        RETRY,          // Retry failed tasks
        ROLLBACK,       // Rollback on failure
        COMPENSATE      // Execute compensation logic
    }

    /**
     * Scheduling Strategy
     */
    public enum SchedulingStrategy {
        MANUAL,         // Manual trigger only
        SCHEDULED,      // Scheduled execution
        EVENT_DRIVEN,   // Triggered by events
        CONDITIONAL,    // Based on conditions
        CONTINUOUS      // Continuous execution
    }

    /**
     * Task Priority
     */
    public enum TaskPriority {
        CRITICAL,
        HIGH,
        NORMAL,
        LOW,
        DEFERRED
    }

    /**
     * Notification Event
     */
    public enum NotificationEvent {
        WORKFLOW_STARTED,
        WORKFLOW_COMPLETED,
        WORKFLOW_FAILED,
        TASK_COMPLETED,
        TASK_FAILED,
        APPROVAL_REQUIRED,
        ERROR_OCCURRED
    }

    /**
     * Workflow Task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTask {
        private String taskId;
        private String taskName;
        private String description;
        private TaskType type;
        private TaskStatus status;
        private TaskPriority priority;

        // Execution
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private Integer retryCount;
        private Integer maxRetries;

        // Dependencies
        private List<String> dependencies;
        private Boolean dependenciesMet;

        // Configuration
        private Map<String, Object> inputParameters;
        private Map<String, Object> outputResults;
        private Map<String, Object> configuration;

        // Conditional logic
        private String condition; // Expression to evaluate
        private Boolean conditionMet;
        private Boolean skipOnCondition;

        // Error handling
        private String errorMessage;
        private LocalDateTime errorAt;
        private Boolean allowFailure;
        private String compensationTaskId;

        // Parallel execution
        private String parallelGroup;
        private Integer parallelIndex;

        // Progress
        private Double progressPercentage;
        private String statusMessage;
        private Map<String, Object> metrics;
    }

    /**
     * Workflow Trigger
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTrigger {
        private String triggerId;
        private String triggerName;
        private String triggerType; // SCHEDULE, EVENT, MANUAL, CONDITION
        private Boolean enabled;
        private String expression; // Cron or condition expression
        private Map<String, Object> parameters;
        private LocalDateTime lastTriggeredAt;
        private Integer triggerCount;
    }

    /**
     * Workflow Error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowError {
        private String errorId;
        private String taskId;
        private String errorCode;
        private String errorMessage;
        private String errorType;
        private LocalDateTime occurredAt;
        private String stackTrace;
        private Boolean recovered;
        private String recoveryAction;
        private Map<String, Object> errorContext;
    }

    /**
     * Workflow Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String taskId;
        private String userId;
        private Map<String, Object> eventData;
    }

    /**
     * Task Execution Result
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskExecutionResult {
        private String taskId;
        private Boolean success;
        private String message;
        private Map<String, Object> results;
        private Long executionTimeMs;
        private LocalDateTime completedAt;
    }

    /**
     * Workflow Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowMetrics {
        private Long totalExecutions;
        private Long successfulExecutions;
        private Long failedExecutions;
        private Double successRate;
        private Double averageExecutionTimeMs;
        private Double minExecutionTimeMs;
        private Double maxExecutionTimeMs;
        private Long totalTasksExecuted;
        private Long totalTasksFailed;
        private LocalDateTime measuredAt;
    }

    /**
     * Helper Methods
     */

    public void addTask(WorkflowTask task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);

        if (taskRegistry == null) {
            taskRegistry = new HashMap<>();
        }
        taskRegistry.put(task.getTaskId(), task);

        totalTasks = (totalTasks != null ? totalTasks : 0) + 1;

        if (task.getStatus() == TaskStatus.COMPLETED) {
            completedTasks = (completedTasks != null ? completedTasks : 0) + 1;
        } else if (task.getStatus() == TaskStatus.FAILED) {
            failedTasks = (failedTasks != null ? failedTasks : 0) + 1;
        } else if (task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.WAITING) {
            pendingTasks = (pendingTasks != null ? pendingTasks : 0) + 1;
        }

        updateProgress();
    }

    public void updateTaskStatus(String taskId, TaskStatus status) {
        WorkflowTask task = taskRegistry != null ? taskRegistry.get(taskId) : null;
        if (task == null) {
            return;
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);

        // Update counts
        if (oldStatus == TaskStatus.COMPLETED && completedTasks != null && completedTasks > 0) {
            completedTasks--;
        } else if (oldStatus == TaskStatus.FAILED && failedTasks != null && failedTasks > 0) {
            failedTasks--;
        } else if ((oldStatus == TaskStatus.PENDING || oldStatus == TaskStatus.WAITING) &&
                   pendingTasks != null && pendingTasks > 0) {
            pendingTasks--;
        }

        if (status == TaskStatus.COMPLETED) {
            completedTasks = (completedTasks != null ? completedTasks : 0) + 1;
        } else if (status == TaskStatus.FAILED) {
            failedTasks = (failedTasks != null ? failedTasks : 0) + 1;
        } else if (status == TaskStatus.PENDING || status == TaskStatus.WAITING) {
            pendingTasks = (pendingTasks != null ? pendingTasks : 0) + 1;
        }

        if (status == TaskStatus.RUNNING) {
            currentTaskId = taskId;
            task.setStartedAt(LocalDateTime.now());
        } else if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
            task.setCompletedAt(LocalDateTime.now());
            if (task.getStartedAt() != null) {
                task.setExecutionTimeMs(
                    java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis()
                );
            }
        }

        updateProgress();
        updateWorkflowStatus();
    }

    public void updateProgress() {
        if (totalTasks == null || totalTasks == 0) {
            progressPercentage = 0.0;
            return;
        }

        int completed = completedTasks != null ? completedTasks : 0;
        int failed = failedTasks != null ? failedTasks : 0;
        int total = totalTasks;

        progressPercentage = ((double) (completed + failed) / total) * 100.0;
    }

    public void updateWorkflowStatus() {
        if (totalTasks == null || totalTasks == 0) {
            return;
        }

        int completed = completedTasks != null ? completedTasks : 0;
        int failed = failedTasks != null ? failedTasks : 0;
        int pending = pendingTasks != null ? pendingTasks : 0;

        if (completed == totalTasks) {
            status = WorkflowStatus.COMPLETED;
            completedAt = LocalDateTime.now();
            if (startedAt != null) {
                executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
            }
        } else if (failed > 0 && Boolean.FALSE.equals(continueOnError)) {
            status = WorkflowStatus.FAILED;
            completedAt = LocalDateTime.now();
            if (startedAt != null) {
                executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
            }
        } else if (completed + failed < totalTasks && status == WorkflowStatus.RUNNING) {
            // Still running
            status = WorkflowStatus.RUNNING;
        }
    }

    public void addDependency(String taskId, String dependsOnTaskId) {
        if (dependencies == null) {
            dependencies = new HashMap<>();
        }

        dependencies.computeIfAbsent(taskId, k -> new ArrayList<>()).add(dependsOnTaskId);
    }

    public boolean areDependenciesMet(String taskId) {
        if (dependencies == null || !dependencies.containsKey(taskId)) {
            return true;
        }

        List<String> deps = dependencies.get(taskId);
        if (deps == null || deps.isEmpty()) {
            return true;
        }

        for (String depTaskId : deps) {
            WorkflowTask depTask = taskRegistry != null ? taskRegistry.get(depTaskId) : null;
            if (depTask == null || depTask.getStatus() != TaskStatus.COMPLETED) {
                return false;
            }
        }

        return true;
    }

    public void recordError(String taskId, String errorMessage, String errorType) {
        if (errors == null) {
            errors = new ArrayList<>();
        }

        WorkflowError error = WorkflowError.builder()
                .errorId(java.util.UUID.randomUUID().toString())
                .taskId(taskId)
                .errorMessage(errorMessage)
                .errorType(errorType)
                .occurredAt(LocalDateTime.now())
                .recovered(false)
                .errorContext(new HashMap<>())
                .build();

        errors.add(error);
        lastErrorMessage = errorMessage;
        lastErrorAt = LocalDateTime.now();
    }

    public void recordEvent(String eventType, String description, String taskId, String userId) {
        if (auditTrail == null) {
            auditTrail = new ArrayList<>();
        }

        WorkflowEvent event = WorkflowEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .taskId(taskId)
                .userId(userId)
                .eventData(new HashMap<>())
                .build();

        auditTrail.add(event);
    }

    public List<WorkflowTask> getReadyTasks() {
        if (tasks == null) {
            return new ArrayList<>();
        }

        return tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.WAITING || t.getStatus() == TaskStatus.PENDING)
                .filter(t -> areDependenciesMet(t.getTaskId()))
                .toList();
    }

    public List<WorkflowTask> getRunningTasks() {
        if (tasks == null) {
            return new ArrayList<>();
        }

        return tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.RUNNING)
                .toList();
    }

    public List<WorkflowTask> getFailedTasks() {
        if (tasks == null) {
            return new ArrayList<>();
        }

        return tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.FAILED)
                .toList();
    }

    public WorkflowTask getTask(String taskId) {
        return taskRegistry != null ? taskRegistry.get(taskId) : null;
    }

    public boolean canExecuteParallelTask() {
        if (maxParallelTasks == null) {
            return true;
        }

        int current = currentParallelTasks != null ? currentParallelTasks : 0;
        return current < maxParallelTasks;
    }

    public void startWorkflow() {
        status = WorkflowStatus.RUNNING;
        startedAt = LocalDateTime.now();
        executionCount = (executionCount != null ? executionCount : 0) + 1;
        lastExecutionAt = LocalDateTime.now();
    }

    public void pauseWorkflow() {
        status = WorkflowStatus.PAUSED;
    }

    public void resumeWorkflow() {
        status = WorkflowStatus.RUNNING;
    }

    public void cancelWorkflow() {
        status = WorkflowStatus.CANCELLED;
        completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == WorkflowStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == WorkflowStatus.FAILED;
    }

    public boolean isRunning() {
        return status == WorkflowStatus.RUNNING;
    }

    public boolean shouldRetry() {
        if (maxRetries == null) {
            return false;
        }

        int retries = retriesAttempted != null ? retriesAttempted : 0;
        return retries < maxRetries;
    }
}
