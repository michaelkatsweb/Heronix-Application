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
 * Report Serverless DTO
 *
 * Represents serverless computing and Function-as-a-Service for report services.
 *
 * Features:
 * - Function deployment and execution
 * - Event-driven triggers
 * - Auto-scaling
 * - Cold start optimization
 * - Resource management
 * - Execution monitoring
 * - Cost tracking
 * - Integration with cloud providers
 *
 * Function Status:
 * - DEPLOYING - Function being deployed
 * - ACTIVE - Function active and ready
 * - INACTIVE - Function inactive
 * - UPDATING - Function being updated
 * - FAILED - Deployment failed
 * - DELETED - Function deleted
 *
 * Execution Status:
 * - PENDING - Execution pending
 * - RUNNING - Execution in progress
 * - SUCCEEDED - Execution succeeded
 * - FAILED - Execution failed
 * - TIMEOUT - Execution timed out
 * - THROTTLED - Execution throttled
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 97 - Report Serverless & Function-as-a-Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportServerless {

    private Long platformId;
    private String platformName;
    private String description;
    private String version;

    // Platform status
    private PlatformStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isActive;

    // Provider
    private CloudProvider cloudProvider;
    private String region;
    private String accountId;
    private Map<String, String> providerConfig;

    // Functions
    private List<Function> functions;
    private Integer totalFunctions;
    private Integer activeFunctions;
    private Integer failedFunctions;
    private Map<String, Function> functionRegistry;

    // Executions
    private List<Execution> executions;
    private Long totalExecutions;
    private Long successfulExecutions;
    private Long failedExecutions;
    private Map<String, Execution> executionRegistry;

    // Triggers
    private List<Trigger> triggers;
    private Integer totalTriggers;
    private Integer activeTriggers;
    private Map<String, Trigger> triggerRegistry;

    // Layers
    private List<Layer> layers;
    private Map<String, Layer> layerRegistry;

    // Auto-scaling
    private Boolean autoScalingEnabled;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer targetConcurrency;
    private ScalingPolicy scalingPolicy;

    // Resource limits
    private ResourceLimits defaultResourceLimits;
    private Map<String, ResourceLimits> functionResourceLimits;

    // Cold start optimization
    private Boolean coldStartOptimizationEnabled;
    private Integer warmInstanceCount;
    private Integer warmInstanceTtlMinutes;
    private List<String> preWarmedFunctions;

    // Monitoring
    private Boolean monitoringEnabled;
    private Integer logRetentionDays;
    private List<ExecutionLog> executionLogs;
    private LocalDateTime lastExecutionAt;

    // Cost tracking
    private Boolean costTrackingEnabled;
    private CostMetrics costMetrics;
    private Map<String, FunctionCostMetrics> functionCosts;

    // Integration
    private List<Integration> integrations;
    private Map<String, String> apiGatewayEndpoints;

    // Metrics
    private PlatformMetrics metrics;
    private List<FunctionMetrics> functionMetricsList;

    // Events
    private List<PlatformEvent> events;
    private LocalDateTime lastEventAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Platform Status
     */
    public enum PlatformStatus {
        INITIALIZING,   // Platform initializing
        ACTIVE,         // Active and operational
        DEGRADED,       // Degraded performance
        MAINTENANCE,    // Under maintenance
        ERROR,          // Error state
        SHUTDOWN        // Shutting down
    }

    /**
     * Function Status
     */
    public enum FunctionStatus {
        DEPLOYING,      // Function being deployed
        ACTIVE,         // Function active and ready
        INACTIVE,       // Function inactive
        UPDATING,       // Function being updated
        FAILED,         // Deployment failed
        DELETED         // Function deleted
    }

    /**
     * Execution Status
     */
    public enum ExecutionStatus {
        PENDING,        // Execution pending
        RUNNING,        // Execution in progress
        SUCCEEDED,      // Execution succeeded
        FAILED,         // Execution failed
        TIMEOUT,        // Execution timed out
        THROTTLED       // Execution throttled
    }

    /**
     * Cloud Provider
     */
    public enum CloudProvider {
        AWS_LAMBDA,     // AWS Lambda
        AZURE_FUNCTIONS,// Azure Functions
        GOOGLE_CLOUD,   // Google Cloud Functions
        OPENWHISK,      // Apache OpenWhisk
        OPENFAAS,       // OpenFaaS
        KNATIVE         // Knative
    }

    /**
     * Trigger Type
     */
    public enum TriggerType {
        HTTP,           // HTTP request
        SCHEDULED,      // Cron/scheduled
        EVENT,          // Event-driven
        QUEUE,          // Message queue
        STORAGE,        // Storage event
        DATABASE,       // Database change
        STREAM          // Data stream
    }

    /**
     * Runtime
     */
    public enum Runtime {
        JAVA_17,
        JAVA_11,
        PYTHON_3_11,
        PYTHON_3_9,
        NODEJS_18,
        NODEJS_16,
        DOTNET_6,
        GO_1_19,
        RUBY_3_0
    }

    /**
     * Function
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Function {
        private String functionId;
        private String functionName;
        private String description;
        private FunctionStatus status;
        private Runtime runtime;

        // Code
        private String handler;
        private String codeLocation; // S3, blob storage, etc.
        private Long codeSizeMb;
        private String codeHash;

        // Configuration
        private Integer timeoutSeconds;
        private Integer memoryMb;
        private Map<String, String> environmentVariables;
        private List<String> layers;

        // Networking
        private String vpcId;
        private List<String> securityGroups;
        private List<String> subnets;

        // Permissions
        private String executionRole;
        private List<String> permissions;

        // Versioning
        private String version;
        private List<String> aliases;
        private Boolean publishedVersion;

        // Concurrency
        private Integer reservedConcurrency;
        private Integer provisionedConcurrency;
        private Integer currentConcurrency;

        // Lifecycle
        private LocalDateTime createdAt;
        private LocalDateTime lastModifiedAt;
        private LocalDateTime lastInvokedAt;
        private String lastInvokedBy;

        // Execution stats
        private Long totalInvocations;
        private Long successfulInvocations;
        private Long failedInvocations;
        private Double averageDurationMs;
        private Long totalDurationMs;

        // Cold starts
        private Long coldStarts;
        private Long warmStarts;
        private Double averageColdStartMs;

        // Metadata
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Execution
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Execution {
        private String executionId;
        private String functionId;
        private String functionName;
        private ExecutionStatus status;

        // Invocation
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationMs;
        private String triggeredBy;
        private TriggerType triggerType;

        // Input/Output
        private Map<String, Object> input;
        private Map<String, Object> output;
        private Long inputSizeBytes;
        private Long outputSizeBytes;

        // Resources
        private Integer memoryUsedMb;
        private Integer memoryAllocatedMb;
        private Double cpuUsagePercent;

        // Results
        private Boolean success;
        private String errorMessage;
        private String errorType;
        private String stackTrace;

        // Performance
        private Boolean coldStart;
        private Long initDurationMs;
        private Long billedDurationMs;

        // Logs
        private String logGroupName;
        private String logStreamName;
        private List<String> logMessages;

        // Cost
        private Double costUsd;

        // Metadata
        private String requestId;
        private Map<String, Object> metadata;
    }

    /**
     * Trigger
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trigger {
        private String triggerId;
        private String triggerName;
        private TriggerType triggerType;
        private String functionId;
        private Boolean enabled;

        // HTTP trigger
        private String httpMethod;
        private String httpPath;
        private String apiGatewayId;

        // Scheduled trigger
        private String cronExpression;
        private String scheduleExpression;
        private LocalDateTime nextRunAt;

        // Event trigger
        private String eventSource;
        private String eventType;
        private Map<String, String> eventFilter;

        // Queue trigger
        private String queueName;
        private Integer batchSize;

        // Storage trigger
        private String bucketName;
        private String objectPrefix;
        private String objectSuffix;

        // Stream trigger
        private String streamName;
        private String startingPosition; // LATEST, TRIM_HORIZON

        // Configuration
        private Integer maxRetries;
        private Integer retryDelaySeconds;
        private Map<String, Object> configuration;

        // Stats
        private Long totalTriggers;
        private Long successfulTriggers;
        private Long failedTriggers;
        private LocalDateTime lastTriggeredAt;

        // Lifecycle
        private LocalDateTime createdAt;
        private LocalDateTime lastModifiedAt;
    }

    /**
     * Layer
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Layer {
        private String layerId;
        private String layerName;
        private String description;
        private Integer version;

        // Content
        private String codeLocation;
        private Long sizeMb;
        private String codeHash;

        // Compatible runtimes
        private List<Runtime> compatibleRuntimes;

        // Usage
        private List<String> usedByFunctions;
        private Integer functionCount;

        // Lifecycle
        private LocalDateTime createdAt;
        private String createdBy;

        // Metadata
        private Map<String, String> tags;
    }

    /**
     * Scaling Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScalingPolicy {
        private String policyId;
        private String policyName;

        // Thresholds
        private Integer minInstances;
        private Integer maxInstances;
        private Integer targetConcurrency;
        private Integer scaleUpThreshold;
        private Integer scaleDownThreshold;

        // Timing
        private Integer scaleUpCooldownSeconds;
        private Integer scaleDownCooldownSeconds;
        private Integer evaluationPeriodSeconds;

        // Metrics
        private Integer currentInstances;
        private LocalDateTime lastScaledAt;
        private String lastScaleAction; // SCALE_UP, SCALE_DOWN

        // Status
        private Boolean enabled;
    }

    /**
     * Resource Limits
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceLimits {
        private Integer maxMemoryMb;
        private Integer maxTimeoutSeconds;
        private Integer maxConcurrency;
        private Integer maxCodeSizeMb;
        private Integer maxEnvironmentVariables;
    }

    /**
     * Integration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Integration {
        private String integrationId;
        private String integrationType; // API_GATEWAY, EVENT_BRIDGE, SNS, SQS
        private String targetService;
        private Map<String, String> configuration;
        private Boolean enabled;
        private LocalDateTime createdAt;
    }

    /**
     * Execution Log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionLog {
        private String logId;
        private String executionId;
        private LocalDateTime timestamp;
        private String level; // INFO, WARN, ERROR, DEBUG
        private String message;
        private Map<String, Object> context;
    }

    /**
     * Cost Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostMetrics {
        private Double totalCostUsd;
        private Double computeCostUsd;
        private Double requestCostUsd;
        private Double storageCostUsd;
        private Double dataTransferCostUsd;
        private Long totalRequests;
        private Long totalGbSeconds;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
    }

    /**
     * Function Cost Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCostMetrics {
        private String functionId;
        private String functionName;
        private Double costUsd;
        private Long invocations;
        private Long gbSeconds;
        private LocalDateTime measuredAt;
    }

    /**
     * Platform Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformMetrics {
        private Integer totalFunctions;
        private Integer activeFunctions;
        private Long totalExecutions;
        private Long successfulExecutions;
        private Long failedExecutions;
        private Double successRate;
        private Double averageDurationMs;
        private Double p50DurationMs;
        private Double p95DurationMs;
        private Double p99DurationMs;
        private Long totalColdStarts;
        private Double coldStartRate;
        private Double totalCostUsd;
        private LocalDateTime measuredAt;
    }

    /**
     * Function Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionMetrics {
        private String functionId;
        private String functionName;
        private Long invocations;
        private Long successCount;
        private Long errorCount;
        private Double successRate;
        private Double averageDurationMs;
        private Long coldStarts;
        private Integer currentConcurrency;
        private Double costUsd;
        private LocalDateTime measuredAt;
    }

    /**
     * Platform Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String resourceType; // FUNCTION, TRIGGER, LAYER, EXECUTION
        private String resourceId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void registerFunction(Function function) {
        if (functions == null) {
            functions = new ArrayList<>();
        }
        functions.add(function);

        if (functionRegistry == null) {
            functionRegistry = new HashMap<>();
        }
        functionRegistry.put(function.getFunctionId(), function);

        totalFunctions = (totalFunctions != null ? totalFunctions : 0) + 1;
        if (function.getStatus() == FunctionStatus.ACTIVE) {
            activeFunctions = (activeFunctions != null ? activeFunctions : 0) + 1;
        } else if (function.getStatus() == FunctionStatus.FAILED) {
            failedFunctions = (failedFunctions != null ? failedFunctions : 0) + 1;
        }

        recordEvent("FUNCTION_REGISTERED", "Function registered: " + function.getFunctionName(),
                "FUNCTION", function.getFunctionId());
    }

    public void updateFunctionStatus(String functionId, FunctionStatus newStatus) {
        Function function = functionRegistry != null ? functionRegistry.get(functionId) : null;
        if (function == null) {
            return;
        }

        FunctionStatus oldStatus = function.getStatus();
        function.setStatus(newStatus);
        function.setLastModifiedAt(LocalDateTime.now());

        // Update counts
        if (oldStatus == FunctionStatus.ACTIVE && activeFunctions != null && activeFunctions > 0) {
            activeFunctions--;
        } else if (oldStatus == FunctionStatus.FAILED && failedFunctions != null && failedFunctions > 0) {
            failedFunctions--;
        }

        if (newStatus == FunctionStatus.ACTIVE) {
            activeFunctions = (activeFunctions != null ? activeFunctions : 0) + 1;
        } else if (newStatus == FunctionStatus.FAILED) {
            failedFunctions = (failedFunctions != null ? failedFunctions : 0) + 1;
        }

        recordEvent("FUNCTION_STATUS_UPDATED",
                "Function status updated: " + function.getFunctionName() + " -> " + newStatus,
                "FUNCTION", functionId);
    }

    public void recordExecution(Execution execution) {
        if (executions == null) {
            executions = new ArrayList<>();
        }
        executions.add(execution);

        if (executionRegistry == null) {
            executionRegistry = new HashMap<>();
        }
        executionRegistry.put(execution.getExecutionId(), execution);

        totalExecutions = (totalExecutions != null ? totalExecutions : 0L) + 1;
        if (Boolean.TRUE.equals(execution.getSuccess())) {
            successfulExecutions = (successfulExecutions != null ? successfulExecutions : 0L) + 1;
        } else {
            failedExecutions = (failedExecutions != null ? failedExecutions : 0L) + 1;
        }

        lastExecutionAt = LocalDateTime.now();

        // Update function stats
        Function function = functionRegistry != null ? functionRegistry.get(execution.getFunctionId()) : null;
        if (function != null) {
            function.setLastInvokedAt(execution.getStartedAt());
            function.setTotalInvocations((function.getTotalInvocations() != null ? function.getTotalInvocations() : 0L) + 1);

            if (Boolean.TRUE.equals(execution.getSuccess())) {
                function.setSuccessfulInvocations((function.getSuccessfulInvocations() != null ?
                        function.getSuccessfulInvocations() : 0L) + 1);
            } else {
                function.setFailedInvocations((function.getFailedInvocations() != null ?
                        function.getFailedInvocations() : 0L) + 1);
            }

            // Update cold start stats
            if (Boolean.TRUE.equals(execution.getColdStart())) {
                function.setColdStarts((function.getColdStarts() != null ? function.getColdStarts() : 0L) + 1);
            } else {
                function.setWarmStarts((function.getWarmStarts() != null ? function.getWarmStarts() : 0L) + 1);
            }
        }
    }

    public void registerTrigger(Trigger trigger) {
        if (triggers == null) {
            triggers = new ArrayList<>();
        }
        triggers.add(trigger);

        if (triggerRegistry == null) {
            triggerRegistry = new HashMap<>();
        }
        triggerRegistry.put(trigger.getTriggerId(), trigger);

        totalTriggers = (totalTriggers != null ? totalTriggers : 0) + 1;
        if (Boolean.TRUE.equals(trigger.getEnabled())) {
            activeTriggers = (activeTriggers != null ? activeTriggers : 0) + 1;
        }

        recordEvent("TRIGGER_REGISTERED", "Trigger registered: " + trigger.getTriggerName(),
                "TRIGGER", trigger.getTriggerId());
    }

    public void registerLayer(Layer layer) {
        if (layers == null) {
            layers = new ArrayList<>();
        }
        layers.add(layer);

        if (layerRegistry == null) {
            layerRegistry = new HashMap<>();
        }
        layerRegistry.put(layer.getLayerId(), layer);

        recordEvent("LAYER_REGISTERED", "Layer registered: " + layer.getLayerName(),
                "LAYER", layer.getLayerId());
    }

    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        PlatformEvent event = PlatformEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    public void startPlatform() {
        status = PlatformStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("PLATFORM_STARTED", "Serverless platform started", "PLATFORM", platformId.toString());
    }

    public void stopPlatform() {
        status = PlatformStatus.SHUTDOWN;
        isActive = false;
        recordEvent("PLATFORM_STOPPED", "Serverless platform stopped", "PLATFORM", platformId.toString());
    }

    public Function getFunction(String functionId) {
        return functionRegistry != null ? functionRegistry.get(functionId) : null;
    }

    public List<Function> getActiveFunctions() {
        if (functions == null) {
            return new ArrayList<>();
        }
        return functions.stream()
                .filter(f -> f.getStatus() == FunctionStatus.ACTIVE)
                .toList();
    }

    public List<Execution> getSuccessfulExecutions() {
        if (executions == null) {
            return new ArrayList<>();
        }
        return executions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.SUCCEEDED)
                .toList();
    }

    public boolean isHealthy() {
        return status == PlatformStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == PlatformStatus.DEGRADED || status == PlatformStatus.ERROR ||
               (failedFunctions != null && failedFunctions > 0);
    }
}
