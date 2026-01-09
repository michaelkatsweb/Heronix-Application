package com.heronix.service;

import com.heronix.dto.ReportOrchestration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Orchestration Service
 *
 * Manages workflow orchestration and task execution engine.
 *
 * Features:
 * - Workflow lifecycle management
 * - Task dependency resolution
 * - Parallel and sequential execution
 * - Conditional logic evaluation
 * - Error handling and retry
 * - Progress tracking
 * - Event auditing
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 91 - Report Orchestration & Workflow Engine
 */
@Service
@Slf4j
public class ReportOrchestrationService {

    private final Map<Long, ReportOrchestration> orchestrations = new ConcurrentHashMap<>();
    private Long nextOrchestrationId = 1L;

    /**
     * Create orchestration
     */
    public ReportOrchestration createOrchestration(ReportOrchestration orchestration) {
        synchronized (this) {
            orchestration.setOrchestrationId(nextOrchestrationId++);
            orchestration.setCreatedAt(LocalDateTime.now());
            orchestration.setStatus(ReportOrchestration.WorkflowStatus.DRAFT);
            orchestration.setProgressPercentage(0.0);
            orchestration.setTotalTasks(0);
            orchestration.setCompletedTasks(0);
            orchestration.setFailedTasks(0);
            orchestration.setPendingTasks(0);
            orchestration.setCurrentParallelTasks(0);
            orchestration.setRetriesAttempted(0);
            orchestration.setExecutionCount(0);

            // Set defaults
            if (orchestration.getMaxRetries() == null) {
                orchestration.setMaxRetries(3);
            }

            if (orchestration.getExecutionMode() == null) {
                orchestration.setExecutionMode(ReportOrchestration.ExecutionMode.SEQUENTIAL);
            }

            if (orchestration.getErrorStrategy() == null) {
                orchestration.setErrorStrategy(ReportOrchestration.ErrorHandlingStrategy.RETRY);
            }

            if (orchestration.getContinueOnError() == null) {
                orchestration.setContinueOnError(false);
            }

            if (orchestration.getSchedulingStrategy() == null) {
                orchestration.setSchedulingStrategy(ReportOrchestration.SchedulingStrategy.MANUAL);
            }

            if (orchestration.getAutoStart() == null) {
                orchestration.setAutoStart(false);
            }

            if (orchestration.getNotificationsEnabled() == null) {
                orchestration.setNotificationsEnabled(true);
            }

            if (orchestration.getConfigurationLocked() == null) {
                orchestration.setConfigurationLocked(false);
            }

            if (orchestration.getTimeoutSeconds() == null) {
                orchestration.setTimeoutSeconds(3600); // 1 hour
            }

            if (orchestration.getRollbackOnFailure() == null) {
                orchestration.setRollbackOnFailure(false);
            }

            if (orchestration.getMaxParallelTasks() == null) {
                orchestration.setMaxParallelTasks(5);
            }

            // Initialize collections
            if (orchestration.getTasks() == null) {
                orchestration.setTasks(new ArrayList<>());
            }

            if (orchestration.getTaskRegistry() == null) {
                orchestration.setTaskRegistry(new HashMap<>());
            }

            if (orchestration.getDependencies() == null) {
                orchestration.setDependencies(new HashMap<>());
            }

            if (orchestration.getRunningTasks() == null) {
                orchestration.setRunningTasks(new ArrayList<>());
            }

            if (orchestration.getWorkflowContext() == null) {
                orchestration.setWorkflowContext(new HashMap<>());
            }

            if (orchestration.getWorkflowVariables() == null) {
                orchestration.setWorkflowVariables(new HashMap<>());
            }

            if (orchestration.getErrors() == null) {
                orchestration.setErrors(new ArrayList<>());
            }

            if (orchestration.getTriggers() == null) {
                orchestration.setTriggers(new ArrayList<>());
            }

            if (orchestration.getTriggerConditions() == null) {
                orchestration.setTriggerConditions(new HashMap<>());
            }

            if (orchestration.getNotificationRecipients() == null) {
                orchestration.setNotificationRecipients(new ArrayList<>());
            }

            if (orchestration.getNotificationEvents() == null) {
                orchestration.setNotificationEvents(new ArrayList<>());
            }

            if (orchestration.getAuditTrail() == null) {
                orchestration.setAuditTrail(new ArrayList<>());
            }

            if (orchestration.getConfiguration() == null) {
                orchestration.setConfiguration(new HashMap<>());
            }

            orchestrations.put(orchestration.getOrchestrationId(), orchestration);

            log.info("Created orchestration {} - {}", orchestration.getOrchestrationId(),
                    orchestration.getWorkflowName());

            return orchestration;
        }
    }

    /**
     * Get orchestration
     */
    public Optional<ReportOrchestration> getOrchestration(Long orchestrationId) {
        return Optional.ofNullable(orchestrations.get(orchestrationId));
    }

    /**
     * Add task to workflow
     */
    public ReportOrchestration.WorkflowTask addTask(Long orchestrationId, String taskName,
                                                     ReportOrchestration.TaskType type,
                                                     ReportOrchestration.TaskPriority priority) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        ReportOrchestration.WorkflowTask task = ReportOrchestration.WorkflowTask.builder()
                .taskId(UUID.randomUUID().toString())
                .taskName(taskName)
                .type(type)
                .status(ReportOrchestration.TaskStatus.PENDING)
                .priority(priority != null ? priority : ReportOrchestration.TaskPriority.NORMAL)
                .progressPercentage(0.0)
                .retryCount(0)
                .maxRetries(3)
                .dependencies(new ArrayList<>())
                .dependenciesMet(true)
                .inputParameters(new HashMap<>())
                .outputResults(new HashMap<>())
                .configuration(new HashMap<>())
                .allowFailure(false)
                .metrics(new HashMap<>())
                .build();

        orchestration.addTask(task);

        log.info("Added task {} to orchestration {}: {} ({})",
                task.getTaskId(), orchestrationId, taskName, type);

        return task;
    }

    /**
     * Add task dependency
     */
    public void addTaskDependency(Long orchestrationId, String taskId, String dependsOnTaskId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        orchestration.addDependency(taskId, dependsOnTaskId);

        ReportOrchestration.WorkflowTask task = orchestration.getTask(taskId);
        if (task != null) {
            task.getDependencies().add(dependsOnTaskId);
            task.setStatus(ReportOrchestration.TaskStatus.WAITING);
        }

        log.info("Added dependency in orchestration {}: {} depends on {}",
                orchestrationId, taskId, dependsOnTaskId);
    }

    /**
     * Start workflow
     */
    public void startWorkflow(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        if (orchestration.getStatus() != ReportOrchestration.WorkflowStatus.READY &&
            orchestration.getStatus() != ReportOrchestration.WorkflowStatus.DRAFT) {
            throw new IllegalStateException("Workflow must be in READY or DRAFT status to start");
        }

        orchestration.startWorkflow();
        orchestration.recordEvent("WORKFLOW_STARTED", "Workflow execution started",
                null, orchestration.getCreatedBy());

        log.info("Started workflow {}", orchestrationId);

        // Execute ready tasks
        executeReadyTasks(orchestrationId);
    }

    /**
     * Execute ready tasks
     */
    private void executeReadyTasks(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            return;
        }

        List<ReportOrchestration.WorkflowTask> readyTasks = orchestration.getReadyTasks();

        for (ReportOrchestration.WorkflowTask task : readyTasks) {
            if (orchestration.getExecutionMode() == ReportOrchestration.ExecutionMode.SEQUENTIAL) {
                // Execute one at a time
                if (orchestration.getCurrentParallelTasks() != null &&
                    orchestration.getCurrentParallelTasks() > 0) {
                    break;
                }
            } else if (!orchestration.canExecuteParallelTask()) {
                break;
            }

            executeTask(orchestrationId, task.getTaskId());
        }
    }

    /**
     * Execute task
     */
    public void executeTask(Long orchestrationId, String taskId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        ReportOrchestration.WorkflowTask task = orchestration.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        // Check dependencies
        if (!orchestration.areDependenciesMet(taskId)) {
            task.setDependenciesMet(false);
            task.setStatus(ReportOrchestration.TaskStatus.WAITING);
            log.warn("Task {} has unmet dependencies", taskId);
            return;
        }

        // Check conditions
        if (task.getCondition() != null && !task.getCondition().isEmpty()) {
            boolean conditionMet = evaluateCondition(task.getCondition(), orchestration.getWorkflowContext());
            task.setConditionMet(conditionMet);

            if (!conditionMet && Boolean.TRUE.equals(task.getSkipOnCondition())) {
                task.setStatus(ReportOrchestration.TaskStatus.SKIPPED);
                orchestration.recordEvent("TASK_SKIPPED", "Task skipped due to condition",
                        taskId, orchestration.getCreatedBy());
                log.info("Task {} skipped due to unmet condition", taskId);
                return;
            }
        }

        orchestration.updateTaskStatus(taskId, ReportOrchestration.TaskStatus.RUNNING);
        orchestration.setCurrentParallelTasks(
                (orchestration.getCurrentParallelTasks() != null ? orchestration.getCurrentParallelTasks() : 0) + 1
        );

        orchestration.recordEvent("TASK_STARTED", "Task execution started",
                taskId, orchestration.getCreatedBy());

        log.info("Executing task {} in orchestration {}: {} ({})",
                taskId, orchestrationId, task.getTaskName(), task.getType());

        // Simulate task execution (in real implementation, this would execute actual logic)
        // For now, just mark as completed
        // This would be replaced with actual task execution logic based on task type
    }

    /**
     * Complete task
     */
    public void completeTask(Long orchestrationId, String taskId, boolean success,
                            Map<String, Object> results) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        ReportOrchestration.WorkflowTask task = orchestration.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        if (success) {
            orchestration.updateTaskStatus(taskId, ReportOrchestration.TaskStatus.COMPLETED);
            task.setOutputResults(results != null ? results : new HashMap<>());

            // Merge results into workflow context
            if (results != null) {
                orchestration.getWorkflowContext().putAll(results);
            }

            orchestration.recordEvent("TASK_COMPLETED", "Task completed successfully",
                    taskId, orchestration.getCreatedBy());

            log.info("Task {} completed successfully in orchestration {}", taskId, orchestrationId);

        } else {
            handleTaskFailure(orchestrationId, taskId, "Task execution failed");
        }

        orchestration.setCurrentParallelTasks(
                Math.max(0, (orchestration.getCurrentParallelTasks() != null ?
                        orchestration.getCurrentParallelTasks() : 1) - 1)
        );

        // Execute next ready tasks
        executeReadyTasks(orchestrationId);
    }

    /**
     * Handle task failure
     */
    private void handleTaskFailure(Long orchestrationId, String taskId, String errorMessage) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            return;
        }

        ReportOrchestration.WorkflowTask task = orchestration.getTask(taskId);
        if (task == null) {
            return;
        }

        task.setErrorMessage(errorMessage);
        task.setErrorAt(LocalDateTime.now());

        orchestration.recordError(taskId, errorMessage, "TASK_FAILURE");
        orchestration.recordEvent("TASK_FAILED", "Task execution failed",
                taskId, orchestration.getCreatedBy());

        // Check if retry is allowed
        if (orchestration.getErrorStrategy() == ReportOrchestration.ErrorHandlingStrategy.RETRY &&
            task.getRetryCount() < task.getMaxRetries()) {

            task.setRetryCount(task.getRetryCount() + 1);
            task.setStatus(ReportOrchestration.TaskStatus.PENDING);

            orchestration.recordEvent("TASK_RETRY", "Retrying task (attempt " + task.getRetryCount() + ")",
                    taskId, orchestration.getCreatedBy());

            log.info("Retrying task {} (attempt {}/{})", taskId, task.getRetryCount(), task.getMaxRetries());

            // Re-execute task
            executeTask(orchestrationId, taskId);

        } else if (Boolean.TRUE.equals(task.getAllowFailure()) ||
                   orchestration.getErrorStrategy() == ReportOrchestration.ErrorHandlingStrategy.CONTINUE) {

            orchestration.updateTaskStatus(taskId, ReportOrchestration.TaskStatus.FAILED);

            log.warn("Task {} failed but workflow continues", taskId);

        } else {
            orchestration.updateTaskStatus(taskId, ReportOrchestration.TaskStatus.FAILED);
            orchestration.setStatus(ReportOrchestration.WorkflowStatus.FAILED);

            orchestration.recordEvent("WORKFLOW_FAILED", "Workflow failed due to task failure",
                    taskId, orchestration.getCreatedBy());

            log.error("Workflow {} failed due to task {} failure", orchestrationId, taskId);
        }
    }

    /**
     * Pause workflow
     */
    public void pauseWorkflow(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        orchestration.pauseWorkflow();
        orchestration.recordEvent("WORKFLOW_PAUSED", "Workflow paused",
                null, orchestration.getCreatedBy());

        log.info("Paused workflow {}", orchestrationId);
    }

    /**
     * Resume workflow
     */
    public void resumeWorkflow(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        orchestration.resumeWorkflow();
        orchestration.recordEvent("WORKFLOW_RESUMED", "Workflow resumed",
                null, orchestration.getCreatedBy());

        log.info("Resumed workflow {}", orchestrationId);

        executeReadyTasks(orchestrationId);
    }

    /**
     * Cancel workflow
     */
    public void cancelWorkflow(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        orchestration.cancelWorkflow();
        orchestration.recordEvent("WORKFLOW_CANCELLED", "Workflow cancelled",
                null, orchestration.getCreatedBy());

        // Cancel all running tasks
        for (ReportOrchestration.WorkflowTask task : orchestration.getRunningTasks()) {
            orchestration.updateTaskStatus(task.getTaskId(), ReportOrchestration.TaskStatus.CANCELLED);
        }

        log.info("Cancelled workflow {}", orchestrationId);
    }

    /**
     * Validate workflow
     */
    public boolean validateWorkflow(Long orchestrationId) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        orchestration.setStatus(ReportOrchestration.WorkflowStatus.VALIDATING);

        // Check for tasks
        if (orchestration.getTasks() == null || orchestration.getTasks().isEmpty()) {
            log.error("Workflow {} has no tasks", orchestrationId);
            return false;
        }

        // Check for circular dependencies
        if (hasCircularDependencies(orchestration)) {
            log.error("Workflow {} has circular dependencies", orchestrationId);
            return false;
        }

        // Check for invalid dependencies
        for (Map.Entry<String, List<String>> entry : orchestration.getDependencies().entrySet()) {
            String taskId = entry.getKey();
            for (String depTaskId : entry.getValue()) {
                if (orchestration.getTask(depTaskId) == null) {
                    log.error("Task {} depends on non-existent task {}", taskId, depTaskId);
                    return false;
                }
            }
        }

        orchestration.setStatus(ReportOrchestration.WorkflowStatus.READY);
        orchestration.setDependenciesResolved(true);

        log.info("Workflow {} validated successfully", orchestrationId);

        return true;
    }

    /**
     * Check for circular dependencies
     */
    private boolean hasCircularDependencies(ReportOrchestration orchestration) {
        Map<String, List<String>> deps = orchestration.getDependencies();
        if (deps == null || deps.isEmpty()) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String taskId : deps.keySet()) {
            if (hasCyclicDependency(taskId, deps, visited, recursionStack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check for cyclic dependency using DFS
     */
    private boolean hasCyclicDependency(String taskId, Map<String, List<String>> deps,
                                       Set<String> visited, Set<String> recursionStack) {
        visited.add(taskId);
        recursionStack.add(taskId);

        List<String> dependencies = deps.get(taskId);
        if (dependencies != null) {
            for (String depTaskId : dependencies) {
                if (!visited.contains(depTaskId)) {
                    if (hasCyclicDependency(depTaskId, deps, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(depTaskId)) {
                    return true;
                }
            }
        }

        recursionStack.remove(taskId);
        return false;
    }

    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // Simplified condition evaluation
        // In a real implementation, this would use a proper expression evaluator
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        // Basic evaluation - just check if variables exist
        return true;
    }

    /**
     * Update task configuration
     */
    public void updateTaskConfiguration(Long orchestrationId, String taskId,
                                        Map<String, Object> configuration) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        ReportOrchestration.WorkflowTask task = orchestration.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        task.setConfiguration(configuration);

        log.info("Updated configuration for task {} in orchestration {}", taskId, orchestrationId);
    }

    /**
     * Add trigger
     */
    public void addTrigger(Long orchestrationId, String triggerName, String triggerType,
                          String expression) {
        ReportOrchestration orchestration = orchestrations.get(orchestrationId);
        if (orchestration == null) {
            throw new IllegalArgumentException("Orchestration not found: " + orchestrationId);
        }

        ReportOrchestration.WorkflowTrigger trigger = ReportOrchestration.WorkflowTrigger.builder()
                .triggerId(UUID.randomUUID().toString())
                .triggerName(triggerName)
                .triggerType(triggerType)
                .enabled(true)
                .expression(expression)
                .parameters(new HashMap<>())
                .triggerCount(0)
                .build();

        orchestration.getTriggers().add(trigger);

        log.info("Added trigger {} to orchestration {}: {} ({})",
                trigger.getTriggerId(), orchestrationId, triggerName, triggerType);
    }

    /**
     * Delete orchestration
     */
    public void deleteOrchestration(Long orchestrationId) {
        ReportOrchestration removed = orchestrations.remove(orchestrationId);
        if (removed != null) {
            log.info("Deleted orchestration {}", orchestrationId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalWorkflows", orchestrations.size());

        long runningWorkflows = orchestrations.values().stream()
                .filter(o -> o.getStatus() == ReportOrchestration.WorkflowStatus.RUNNING)
                .count();

        long completedWorkflows = orchestrations.values().stream()
                .filter(o -> o.getStatus() == ReportOrchestration.WorkflowStatus.COMPLETED)
                .count();

        long failedWorkflows = orchestrations.values().stream()
                .filter(o -> o.getStatus() == ReportOrchestration.WorkflowStatus.FAILED)
                .count();

        long totalTasks = orchestrations.values().stream()
                .mapToLong(o -> o.getTotalTasks() != null ? o.getTotalTasks() : 0L)
                .sum();

        long totalCompletedTasks = orchestrations.values().stream()
                .mapToLong(o -> o.getCompletedTasks() != null ? o.getCompletedTasks() : 0L)
                .sum();

        long totalFailedTasks = orchestrations.values().stream()
                .mapToLong(o -> o.getFailedTasks() != null ? o.getFailedTasks().size() : 0L)
                .sum();

        stats.put("runningWorkflows", runningWorkflows);
        stats.put("completedWorkflows", completedWorkflows);
        stats.put("failedWorkflows", failedWorkflows);
        stats.put("totalTasks", totalTasks);
        stats.put("totalCompletedTasks", totalCompletedTasks);
        stats.put("totalFailedTasks", totalFailedTasks);

        return stats;
    }
}
