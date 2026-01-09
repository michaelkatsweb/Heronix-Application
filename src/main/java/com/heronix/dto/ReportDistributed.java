package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Distributed Computing DTO
 *
 * Manages distributed computing and grid systems for report processing.
 *
 * Features:
 * - Distributed computing grid
 * - Compute node management
 * - Job scheduling and distribution
 * - Load balancing across nodes
 * - Fault tolerance and recovery
 * - MapReduce processing
 * - Data partitioning
 * - Parallel processing
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 102 - Report Distributed Computing & Grid Systems
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDistributed {

    // Grid Information
    private Long gridId;
    private String gridName;
    private String description;
    private GridStatus status;
    private Boolean isActive;

    // Grid Platform
    private GridPlatform platform;
    private String platformVersion;
    private SchedulingStrategy schedulingStrategy;
    private LoadBalancingAlgorithm loadBalancingAlgorithm;
    private Boolean faultTolerant;

    // Compute Nodes
    private List<ComputeNode> nodes;
    private Map<String, ComputeNode> nodeRegistry;
    private Integer totalNodes;
    private Integer activeNodes;
    private Integer idleNodes;
    private Integer busyNodes;
    private Integer failedNodes;

    // Compute Jobs
    private List<ComputeJob> jobs;
    private Map<String, ComputeJob> jobRegistry;
    private Long totalJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Long runningJobs;
    private Long queuedJobs;

    // Tasks
    private List<Task> tasks;
    private Map<String, Task> taskRegistry;
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;

    // Data Partitions
    private List<DataPartition> partitions;
    private Map<String, DataPartition> partitionRegistry;
    private Integer totalPartitions;
    private PartitionStrategy partitionStrategy;

    // MapReduce Jobs
    private List<MapReduceJob> mapReduceJobs;
    private Map<String, MapReduceJob> mapReduceRegistry;
    private Long totalMapReduceJobs;
    private Long completedMapReduceJobs;

    // Resource Pools
    private List<ResourcePool> resourcePools;
    private Map<String, ResourcePool> poolRegistry;
    private Integer totalPools;

    // Metrics
    private GridMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<GridEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime lastJobSubmittedAt;

    /**
     * Grid Status
     */
    public enum GridStatus {
        INITIALIZING,
        READY,
        RUNNING,
        SCALING,
        DEGRADED,
        MAINTENANCE,
        ERROR
    }

    /**
     * Grid Platform
     */
    public enum GridPlatform {
        APACHE_SPARK,
        APACHE_HADOOP,
        APACHE_FLINK,
        DASK,
        RAY,
        SLURM,
        KUBERNETES_JOBS,
        CUSTOM
    }

    /**
     * Scheduling Strategy
     */
    public enum SchedulingStrategy {
        FIFO,               // First In First Out
        FAIR_SHARE,         // Fair resource sharing
        PRIORITY_BASED,     // Priority queue
        CAPACITY,           // Capacity scheduler
        GANG_SCHEDULING,    // All tasks together
        BACKFILL,           // Fill gaps
        DEADLINE_BASED      // Deadline-driven
    }

    /**
     * Load Balancing Algorithm
     */
    public enum LoadBalancingAlgorithm {
        ROUND_ROBIN,
        LEAST_LOADED,
        RANDOM,
        WEIGHTED,
        CONSISTENT_HASH,
        POWER_OF_TWO_CHOICES
    }

    /**
     * Node Status
     */
    public enum NodeStatus {
        PROVISIONING,
        IDLE,
        BUSY,
        OVERLOADED,
        DRAINING,
        FAILED,
        OFFLINE
    }

    /**
     * Job Status
     */
    public enum JobStatus {
        SUBMITTED,
        QUEUED,
        SCHEDULED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        TIMEOUT
    }

    /**
     * Task Status
     */
    public enum TaskStatus {
        PENDING,
        ASSIGNED,
        RUNNING,
        COMPLETED,
        FAILED,
        RETRYING
    }

    /**
     * Partition Strategy
     */
    public enum PartitionStrategy {
        HASH,
        RANGE,
        ROUND_ROBIN,
        CUSTOM,
        SIZE_BASED
    }

    /**
     * Compute Node
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComputeNode {
        private String nodeId;
        private String nodeName;
        private NodeStatus status;
        private String ipAddress;
        private Integer port;
        private Integer cpuCores;
        private Integer availableCores;
        private Long memoryMb;
        private Long availableMemoryMb;
        private Long diskGb;
        private Long availableDiskGb;
        private Double cpuUtilization;
        private Double memoryUtilization;
        private Double diskUtilization;
        private Integer runningTasks;
        private Integer maxConcurrentTasks;
        private Long totalTasksProcessed;
        private Long successfulTasks;
        private Long failedTasks;
        private Boolean healthy;
        private LocalDateTime registeredAt;
        private LocalDateTime lastHeartbeatAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Compute Job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComputeJob {
        private String jobId;
        private String jobName;
        private JobStatus status;
        private String jobType;
        private Integer priority;
        private List<String> taskIds;
        private Integer totalTasks;
        private Integer completedTasks;
        private Integer failedTasks;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private ResourceRequirements resourceRequirements;
        private Integer maxRetries;
        private Integer currentRetries;
        private Long deadlineMs;
        private LocalDateTime submittedAt;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        private String taskId;
        private String taskName;
        private TaskStatus status;
        private String jobId;
        private String nodeId;
        private String nodeName;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private Integer partitionId;
        private Integer attemptNumber;
        private LocalDateTime assignedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Resource Requirements
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRequirements {
        private Integer cpuCores;
        private Long memoryMb;
        private Long diskGb;
        private Integer gpuCount;
        private String networkBandwidth;
        private Map<String, Object> customRequirements;
    }

    /**
     * Data Partition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPartition {
        private String partitionId;
        private Integer partitionNumber;
        private PartitionStrategy strategy;
        private String datasetId;
        private Long startOffset;
        private Long endOffset;
        private Long recordCount;
        private Long sizeBytes;
        private List<String> assignedNodes;
        private Boolean replicated;
        private Integer replicationFactor;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * MapReduce Job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapReduceJob {
        private String mrJobId;
        private String jobName;
        private JobStatus status;
        private String inputDataset;
        private String outputDataset;
        private Integer mapTasks;
        private Integer reduceTasks;
        private Integer completedMapTasks;
        private Integer completedReduceTasks;
        private String mapperClass;
        private String reducerClass;
        private String combinerClass;
        private LocalDateTime submittedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private Long inputRecords;
        private Long outputRecords;
        private Map<String, Object> configuration;
        private Map<String, Object> metadata;
    }

    /**
     * Resource Pool
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourcePool {
        private String poolId;
        private String poolName;
        private List<String> nodeIds;
        private Integer totalNodes;
        private Integer totalCpuCores;
        private Long totalMemoryMb;
        private Integer allocatedCores;
        private Long allocatedMemoryMb;
        private Double utilizationPercent;
        private Integer maxConcurrentJobs;
        private Integer runningJobs;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Grid Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GridMetrics {
        private Integer totalNodes;
        private Integer activeNodes;
        private Integer idleNodes;
        private Integer busyNodes;
        private Double averageCpuUtilization;
        private Double averageMemoryUtilization;
        private Long totalJobs;
        private Long completedJobs;
        private Long failedJobs;
        private Double jobSuccessRate;
        private Double averageJobExecutionTimeMs;
        private Long totalTasksProcessed;
        private Double averageTaskExecutionTimeMs;
        private Integer queueDepth;
        private Double throughputJobsPerHour;
        private LocalDateTime measuredAt;
    }

    /**
     * Grid Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GridEvent {
        private String eventId;
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> details;
    }

    // Helper Methods

    /**
     * Start grid
     */
    public void startGrid() {
        this.status = GridStatus.INITIALIZING;
        this.isActive = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("GRID_STARTED", "Grid started", "GRID",
                gridId != null ? gridId.toString() : null);

        this.status = GridStatus.READY;
    }

    /**
     * Stop grid
     */
    public void stopGrid() {
        this.status = GridStatus.MAINTENANCE;
        this.isActive = false;
        this.stoppedAt = LocalDateTime.now();

        recordEvent("GRID_STOPPED", "Grid stopped", "GRID",
                gridId != null ? gridId.toString() : null);
    }

    /**
     * Register compute node
     */
    public void registerNode(ComputeNode node) {
        if (nodes == null) {
            nodes = new java.util.ArrayList<>();
        }
        nodes.add(node);

        if (nodeRegistry == null) {
            nodeRegistry = new java.util.HashMap<>();
        }
        nodeRegistry.put(node.getNodeId(), node);

        totalNodes = (totalNodes != null ? totalNodes : 0) + 1;
        if (node.getStatus() == NodeStatus.IDLE) {
            idleNodes = (idleNodes != null ? idleNodes : 0) + 1;
        }

        recordEvent("NODE_REGISTERED", "Node registered: " + node.getNodeName(),
                "NODE", node.getNodeId());
    }

    /**
     * Update node status
     */
    public void updateNodeStatus(String nodeId, NodeStatus newStatus) {
        ComputeNode node = nodeRegistry != null ? nodeRegistry.get(nodeId) : null;
        if (node != null) {
            NodeStatus oldStatus = node.getStatus();
            node.setStatus(newStatus);

            // Update counters
            if (oldStatus == NodeStatus.IDLE && idleNodes != null && idleNodes > 0) {
                idleNodes--;
            } else if (oldStatus == NodeStatus.BUSY && busyNodes != null && busyNodes > 0) {
                busyNodes--;
            }

            if (newStatus == NodeStatus.IDLE) {
                idleNodes = (idleNodes != null ? idleNodes : 0) + 1;
            } else if (newStatus == NodeStatus.BUSY) {
                busyNodes = (busyNodes != null ? busyNodes : 0) + 1;
            }

            activeNodes = (idleNodes != null ? idleNodes : 0) + (busyNodes != null ? busyNodes : 0);
        }
    }

    /**
     * Submit job
     */
    public void submitJob(ComputeJob job) {
        if (jobs == null) {
            jobs = new java.util.ArrayList<>();
        }
        jobs.add(job);

        if (jobRegistry == null) {
            jobRegistry = new java.util.HashMap<>();
        }
        jobRegistry.put(job.getJobId(), job);

        totalJobs = (totalJobs != null ? totalJobs : 0L) + 1;
        queuedJobs = (queuedJobs != null ? queuedJobs : 0L) + 1;
        lastJobSubmittedAt = LocalDateTime.now();

        recordEvent("JOB_SUBMITTED", "Job submitted: " + job.getJobName(),
                "JOB", job.getJobId());
    }

    /**
     * Start job
     */
    public void startJob(String jobId) {
        ComputeJob job = jobRegistry != null ? jobRegistry.get(jobId) : null;
        if (job != null) {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());

            if (queuedJobs != null && queuedJobs > 0) {
                queuedJobs--;
            }
            runningJobs = (runningJobs != null ? runningJobs : 0L) + 1;
        }
    }

    /**
     * Complete job
     */
    public void completeJob(String jobId, boolean success) {
        ComputeJob job = jobRegistry != null ? jobRegistry.get(jobId) : null;
        if (job != null) {
            job.setStatus(success ? JobStatus.COMPLETED : JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());

            if (job.getStartedAt() != null) {
                job.setExecutionTimeMs(
                    java.time.Duration.between(job.getStartedAt(), job.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                completedJobs = (completedJobs != null ? completedJobs : 0L) + 1;
            } else {
                failedJobs = (failedJobs != null ? failedJobs : 0L) + 1;
            }

            if (runningJobs != null && runningJobs > 0) {
                runningJobs--;
            }
        }
    }

    /**
     * Add task
     */
    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new java.util.ArrayList<>();
        }
        tasks.add(task);

        if (taskRegistry == null) {
            taskRegistry = new java.util.HashMap<>();
        }
        taskRegistry.put(task.getTaskId(), task);

        totalTasks = (totalTasks != null ? totalTasks : 0L) + 1;
    }

    /**
     * Complete task
     */
    public void completeTask(String taskId, boolean success) {
        Task task = taskRegistry != null ? taskRegistry.get(taskId) : null;
        if (task != null) {
            task.setStatus(success ? TaskStatus.COMPLETED : TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());

            if (task.getStartedAt() != null) {
                task.setExecutionTimeMs(
                    java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                completedTasks = (completedTasks != null ? completedTasks : 0L) + 1;
            } else {
                failedTasks = (failedTasks != null ? failedTasks : 0L) + 1;
            }

            // Update node task count
            if (task.getNodeId() != null) {
                ComputeNode node = nodeRegistry != null ? nodeRegistry.get(task.getNodeId()) : null;
                if (node != null) {
                    node.setRunningTasks(node.getRunningTasks() != null && node.getRunningTasks() > 0 ?
                            node.getRunningTasks() - 1 : 0);
                    if (success) {
                        node.setSuccessfulTasks((node.getSuccessfulTasks() != null ?
                                node.getSuccessfulTasks() : 0L) + 1);
                    } else {
                        node.setFailedTasks((node.getFailedTasks() != null ?
                                node.getFailedTasks() : 0L) + 1);
                    }
                    node.setTotalTasksProcessed((node.getTotalTasksProcessed() != null ?
                            node.getTotalTasksProcessed() : 0L) + 1);
                }
            }
        }
    }

    /**
     * Add partition
     */
    public void addPartition(DataPartition partition) {
        if (partitions == null) {
            partitions = new java.util.ArrayList<>();
        }
        partitions.add(partition);

        if (partitionRegistry == null) {
            partitionRegistry = new java.util.HashMap<>();
        }
        partitionRegistry.put(partition.getPartitionId(), partition);

        totalPartitions = (totalPartitions != null ? totalPartitions : 0) + 1;
    }

    /**
     * Submit MapReduce job
     */
    public void submitMapReduceJob(MapReduceJob mrJob) {
        if (mapReduceJobs == null) {
            mapReduceJobs = new java.util.ArrayList<>();
        }
        mapReduceJobs.add(mrJob);

        if (mapReduceRegistry == null) {
            mapReduceRegistry = new java.util.HashMap<>();
        }
        mapReduceRegistry.put(mrJob.getMrJobId(), mrJob);

        totalMapReduceJobs = (totalMapReduceJobs != null ? totalMapReduceJobs : 0L) + 1;
    }

    /**
     * Complete MapReduce job
     */
    public void completeMapReduceJob(String mrJobId, boolean success) {
        MapReduceJob mrJob = mapReduceRegistry != null ? mapReduceRegistry.get(mrJobId) : null;
        if (mrJob != null) {
            mrJob.setStatus(success ? JobStatus.COMPLETED : JobStatus.FAILED);
            mrJob.setCompletedAt(LocalDateTime.now());

            if (mrJob.getStartedAt() != null) {
                mrJob.setExecutionTimeMs(
                    java.time.Duration.between(mrJob.getStartedAt(), mrJob.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                completedMapReduceJobs = (completedMapReduceJobs != null ? completedMapReduceJobs : 0L) + 1;
            }
        }
    }

    /**
     * Add resource pool
     */
    public void addResourcePool(ResourcePool pool) {
        if (resourcePools == null) {
            resourcePools = new java.util.ArrayList<>();
        }
        resourcePools.add(pool);

        if (poolRegistry == null) {
            poolRegistry = new java.util.HashMap<>();
        }
        poolRegistry.put(pool.getPoolId(), pool);

        totalPools = (totalPools != null ? totalPools : 0) + 1;
    }

    /**
     * Get node by ID
     */
    public ComputeNode getNode(String nodeId) {
        return nodeRegistry != null ? nodeRegistry.get(nodeId) : null;
    }

    /**
     * Get job by ID
     */
    public ComputeJob getJob(String jobId) {
        return jobRegistry != null ? jobRegistry.get(jobId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        GridEvent event = GridEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(new java.util.HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if grid is healthy
     */
    public boolean isHealthy() {
        return status == GridStatus.READY || status == GridStatus.RUNNING;
    }

    /**
     * Get idle nodes
     */
    public List<ComputeNode> getIdleNodes() {
        if (nodes == null) {
            return new java.util.ArrayList<>();
        }
        return nodes.stream()
                .filter(n -> n.getStatus() == NodeStatus.IDLE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active nodes
     */
    public List<ComputeNode> getActiveNodes() {
        if (nodes == null) {
            return new java.util.ArrayList<>();
        }
        return nodes.stream()
                .filter(n -> n.getStatus() == NodeStatus.IDLE || n.getStatus() == NodeStatus.BUSY)
                .collect(java.util.stream.Collectors.toList());
    }
}
