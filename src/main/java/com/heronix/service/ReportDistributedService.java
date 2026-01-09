package com.heronix.service;

import com.heronix.dto.ReportDistributed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Distributed Computing Service
 *
 * Manages distributed computing and grid systems for report processing.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 102 - Report Distributed Computing & Grid Systems
 */
@Service
@Slf4j
public class ReportDistributedService {

    private final Map<Long, ReportDistributed> grids = new ConcurrentHashMap<>();
    private Long nextGridId = 1L;

    /**
     * Create grid
     */
    public ReportDistributed createGrid(ReportDistributed grid) {
        synchronized (this) {
            grid.setGridId(nextGridId++);
        }

        grid.setStatus(ReportDistributed.GridStatus.INITIALIZING);
        grid.setCreatedAt(LocalDateTime.now());
        grid.setIsActive(false);

        // Initialize collections
        if (grid.getNodes() == null) {
            grid.setNodes(new ArrayList<>());
        }
        if (grid.getJobs() == null) {
            grid.setJobs(new ArrayList<>());
        }
        if (grid.getTasks() == null) {
            grid.setTasks(new ArrayList<>());
        }
        if (grid.getPartitions() == null) {
            grid.setPartitions(new ArrayList<>());
        }
        if (grid.getMapReduceJobs() == null) {
            grid.setMapReduceJobs(new ArrayList<>());
        }
        if (grid.getResourcePools() == null) {
            grid.setResourcePools(new ArrayList<>());
        }
        if (grid.getEvents() == null) {
            grid.setEvents(new ArrayList<>());
        }

        // Initialize registries
        grid.setNodeRegistry(new ConcurrentHashMap<>());
        grid.setJobRegistry(new ConcurrentHashMap<>());
        grid.setTaskRegistry(new ConcurrentHashMap<>());
        grid.setPartitionRegistry(new ConcurrentHashMap<>());
        grid.setMapReduceRegistry(new ConcurrentHashMap<>());
        grid.setPoolRegistry(new ConcurrentHashMap<>());

        // Initialize counters
        grid.setTotalNodes(0);
        grid.setActiveNodes(0);
        grid.setIdleNodes(0);
        grid.setBusyNodes(0);
        grid.setFailedNodes(0);
        grid.setTotalJobs(0L);
        grid.setCompletedJobs(0L);
        grid.setFailedJobs(0L);
        grid.setRunningJobs(0L);
        grid.setQueuedJobs(0L);
        grid.setTotalTasks(0L);
        grid.setCompletedTasks(0L);
        grid.setFailedTasks(0L);
        grid.setTotalPartitions(0);
        grid.setTotalMapReduceJobs(0L);
        grid.setCompletedMapReduceJobs(0L);
        grid.setTotalPools(0);

        grids.put(grid.getGridId(), grid);
        log.info("Created grid: {} (ID: {})", grid.getGridName(), grid.getGridId());

        return grid;
    }

    /**
     * Get grid
     */
    public Optional<ReportDistributed> getGrid(Long gridId) {
        return Optional.ofNullable(grids.get(gridId));
    }

    /**
     * Start grid
     */
    public void startGrid(Long gridId) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        grid.startGrid();
        log.info("Started grid: {}", gridId);
    }

    /**
     * Stop grid
     */
    public void stopGrid(Long gridId) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        grid.stopGrid();
        log.info("Stopped grid: {}", gridId);
    }

    /**
     * Register compute node
     */
    public ReportDistributed.ComputeNode registerNode(Long gridId, String nodeName,
                                                       String ipAddress, Integer cpuCores,
                                                       Long memoryMb, Long diskGb) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        ReportDistributed.ComputeNode node = ReportDistributed.ComputeNode.builder()
                .nodeId(UUID.randomUUID().toString())
                .nodeName(nodeName)
                .status(ReportDistributed.NodeStatus.PROVISIONING)
                .ipAddress(ipAddress)
                .port(8080)
                .cpuCores(cpuCores)
                .availableCores(cpuCores)
                .memoryMb(memoryMb)
                .availableMemoryMb(memoryMb)
                .diskGb(diskGb)
                .availableDiskGb(diskGb)
                .cpuUtilization(0.0)
                .memoryUtilization(0.0)
                .diskUtilization(0.0)
                .runningTasks(0)
                .maxConcurrentTasks(cpuCores != null ? cpuCores * 2 : 8)
                .totalTasksProcessed(0L)
                .successfulTasks(0L)
                .failedTasks(0L)
                .healthy(true)
                .registeredAt(LocalDateTime.now())
                .lastHeartbeatAt(LocalDateTime.now())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        // Node becomes idle after provisioning
        node.setStatus(ReportDistributed.NodeStatus.IDLE);

        grid.registerNode(node);
        log.info("Registered compute node {} in grid {}", nodeName, gridId);

        return node;
    }

    /**
     * Submit compute job
     */
    public ReportDistributed.ComputeJob submitJob(Long gridId, String jobName, String jobType,
                                                   Map<String, Object> inputData, Integer numTasks,
                                                   ReportDistributed.ResourceRequirements requirements) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        ReportDistributed.ComputeJob job = ReportDistributed.ComputeJob.builder()
                .jobId(UUID.randomUUID().toString())
                .jobName(jobName)
                .status(ReportDistributed.JobStatus.SUBMITTED)
                .jobType(jobType)
                .priority(5)
                .taskIds(new ArrayList<>())
                .totalTasks(numTasks != null ? numTasks : 1)
                .completedTasks(0)
                .failedTasks(0)
                .inputData(inputData != null ? inputData : new HashMap<>())
                .outputData(new HashMap<>())
                .resourceRequirements(requirements)
                .maxRetries(3)
                .currentRetries(0)
                .submittedAt(LocalDateTime.now())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        grid.submitJob(job);

        // Create tasks for the job
        for (int i = 0; i < job.getTotalTasks(); i++) {
            ReportDistributed.Task task = ReportDistributed.Task.builder()
                    .taskId(UUID.randomUUID().toString())
                    .taskName(jobName + "-task-" + i)
                    .status(ReportDistributed.TaskStatus.PENDING)
                    .jobId(job.getJobId())
                    .inputData(new HashMap<>())
                    .outputData(new HashMap<>())
                    .partitionId(i)
                    .attemptNumber(0)
                    .metadata(new HashMap<>())
                    .build();

            grid.addTask(task);
            job.getTaskIds().add(task.getTaskId());
        }

        // Schedule job
        scheduleJob(grid, job);

        log.info("Submitted job {} with {} tasks to grid {}", jobName, numTasks, gridId);

        return job;
    }

    /**
     * Create data partitions
     */
    public List<ReportDistributed.DataPartition> createPartitions(Long gridId, String datasetId,
                                                                   Integer numPartitions,
                                                                   ReportDistributed.PartitionStrategy strategy) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        List<ReportDistributed.DataPartition> partitions = new ArrayList<>();

        for (int i = 0; i < numPartitions; i++) {
            ReportDistributed.DataPartition partition = ReportDistributed.DataPartition.builder()
                    .partitionId(UUID.randomUUID().toString())
                    .partitionNumber(i)
                    .strategy(strategy)
                    .datasetId(datasetId)
                    .startOffset((long) i * 1000)
                    .endOffset((long) (i + 1) * 1000)
                    .recordCount(1000L)
                    .sizeBytes(1024L * 1024)
                    .assignedNodes(new ArrayList<>())
                    .replicated(false)
                    .replicationFactor(1)
                    .createdAt(LocalDateTime.now())
                    .metadata(new HashMap<>())
                    .build();

            grid.addPartition(partition);
            partitions.add(partition);
        }

        log.info("Created {} partitions for dataset {} in grid {}", numPartitions, datasetId, gridId);

        return partitions;
    }

    /**
     * Submit MapReduce job
     */
    public ReportDistributed.MapReduceJob submitMapReduceJob(Long gridId, String jobName,
                                                              String inputDataset, String outputDataset,
                                                              Integer mapTasks, Integer reduceTasks) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        ReportDistributed.MapReduceJob mrJob = ReportDistributed.MapReduceJob.builder()
                .mrJobId(UUID.randomUUID().toString())
                .jobName(jobName)
                .status(ReportDistributed.JobStatus.SUBMITTED)
                .inputDataset(inputDataset)
                .outputDataset(outputDataset)
                .mapTasks(mapTasks)
                .reduceTasks(reduceTasks)
                .completedMapTasks(0)
                .completedReduceTasks(0)
                .mapperClass("com.heronix.mapper.ReportMapper")
                .reducerClass("com.heronix.reducer.ReportReducer")
                .submittedAt(LocalDateTime.now())
                .configuration(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        grid.submitMapReduceJob(mrJob);

        // Simulate execution
        mrJob.setStatus(ReportDistributed.JobStatus.RUNNING);
        mrJob.setStartedAt(LocalDateTime.now());

        Random random = new Random();
        mrJob.setInputRecords((long) (random.nextInt(10000) + 1000));

        // Simulate completion
        boolean success = random.nextDouble() > 0.05; // 95% success rate
        grid.completeMapReduceJob(mrJob.getMrJobId(), success);

        if (success) {
            mrJob.setOutputRecords((long) (mrJob.getInputRecords() * 0.8));
            mrJob.setCompletedMapTasks(mapTasks);
            mrJob.setCompletedReduceTasks(reduceTasks);
        }

        log.info("Submitted MapReduce job {} to grid {}: {}",
                jobName, gridId, success ? "SUCCESS" : "FAILED");

        return mrJob;
    }

    /**
     * Create resource pool
     */
    public ReportDistributed.ResourcePool createResourcePool(Long gridId, String poolName,
                                                              List<String> nodeIds) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        // Calculate pool resources
        int totalCores = 0;
        long totalMemory = 0L;

        if (nodeIds != null) {
            for (String nodeId : nodeIds) {
                ReportDistributed.ComputeNode node = grid.getNode(nodeId);
                if (node != null) {
                    totalCores += node.getCpuCores() != null ? node.getCpuCores() : 0;
                    totalMemory += node.getMemoryMb() != null ? node.getMemoryMb() : 0L;
                }
            }
        }

        ReportDistributed.ResourcePool pool = ReportDistributed.ResourcePool.builder()
                .poolId(UUID.randomUUID().toString())
                .poolName(poolName)
                .nodeIds(nodeIds != null ? nodeIds : new ArrayList<>())
                .totalNodes(nodeIds != null ? nodeIds.size() : 0)
                .totalCpuCores(totalCores)
                .totalMemoryMb(totalMemory)
                .allocatedCores(0)
                .allocatedMemoryMb(0L)
                .utilizationPercent(0.0)
                .maxConcurrentJobs(10)
                .runningJobs(0)
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        grid.addResourcePool(pool);
        log.info("Created resource pool {} with {} nodes in grid {}", poolName, pool.getTotalNodes(), gridId);

        return pool;
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long gridId) {
        ReportDistributed grid = grids.get(gridId);
        if (grid == null) {
            throw new IllegalArgumentException("Grid not found: " + gridId);
        }

        int totalNodes = grid.getTotalNodes() != null ? grid.getTotalNodes() : 0;
        int activeNodes = grid.getActiveNodes() != null ? grid.getActiveNodes().size() : 0;
        int idleNodes = grid.getIdleNodes() != null ? grid.getIdleNodes().size() : 0;
        int busyNodes = activeNodes - idleNodes;

        // Calculate average utilization
        double avgCpuUtil = grid.getNodes() != null ?
                grid.getNodes().stream()
                        .filter(n -> n.getCpuUtilization() != null)
                        .mapToDouble(ReportDistributed.ComputeNode::getCpuUtilization)
                        .average()
                        .orElse(0.0) : 0.0;

        double avgMemUtil = grid.getNodes() != null ?
                grid.getNodes().stream()
                        .filter(n -> n.getMemoryUtilization() != null)
                        .mapToDouble(ReportDistributed.ComputeNode::getMemoryUtilization)
                        .average()
                        .orElse(0.0) : 0.0;

        long totalJobs = grid.getTotalJobs() != null ? grid.getTotalJobs() : 0L;
        long completedJobs = grid.getCompletedJobs() != null ? grid.getCompletedJobs() : 0L;
        long failedJobs = grid.getFailedJobs() != null ? grid.getFailedJobs() : 0L;

        double jobSuccessRate = totalJobs > 0 ?
                (completedJobs * 100.0 / totalJobs) : 0.0;

        // Calculate average job execution time
        double avgJobTime = grid.getJobs() != null ?
                grid.getJobs().stream()
                        .filter(j -> j.getExecutionTimeMs() != null)
                        .mapToLong(ReportDistributed.ComputeJob::getExecutionTimeMs)
                        .average()
                        .orElse(0.0) : 0.0;

        // Calculate average task execution time
        double avgTaskTime = grid.getTasks() != null ?
                grid.getTasks().stream()
                        .filter(t -> t.getExecutionTimeMs() != null)
                        .mapToLong(ReportDistributed.Task::getExecutionTimeMs)
                        .average()
                        .orElse(0.0) : 0.0;

        long totalTasksProcessed = grid.getCompletedTasks() != null ? grid.getCompletedTasks() : 0L;

        int queueDepth = grid.getQueuedJobs() != null ? grid.getQueuedJobs().intValue() : 0;

        // Calculate throughput (jobs per hour)
        double throughput = completedJobs > 0 && avgJobTime > 0 ?
                (3600000.0 / avgJobTime) : 0.0;

        ReportDistributed.GridMetrics metrics = ReportDistributed.GridMetrics.builder()
                .totalNodes(totalNodes)
                .activeNodes(activeNodes)
                .idleNodes(idleNodes)
                .busyNodes(busyNodes)
                .averageCpuUtilization(avgCpuUtil)
                .averageMemoryUtilization(avgMemUtil)
                .totalJobs(totalJobs)
                .completedJobs(completedJobs)
                .failedJobs(failedJobs)
                .jobSuccessRate(jobSuccessRate)
                .averageJobExecutionTimeMs(avgJobTime)
                .totalTasksProcessed(totalTasksProcessed)
                .averageTaskExecutionTimeMs(avgTaskTime)
                .queueDepth(queueDepth)
                .throughputJobsPerHour(throughput)
                .measuredAt(LocalDateTime.now())
                .build();

        grid.setMetrics(metrics);
        grid.setLastMetricsUpdate(LocalDateTime.now());

        log.debug("Updated metrics for grid {}: {} nodes, {} jobs, {:.1f}% success",
                gridId, totalNodes, totalJobs, jobSuccessRate);
    }

    /**
     * Delete grid
     */
    public void deleteGrid(Long gridId) {
        ReportDistributed grid = grids.get(gridId);
        if (grid != null && grid.isHealthy()) {
            stopGrid(gridId);
        }

        ReportDistributed removed = grids.remove(gridId);
        if (removed != null) {
            log.info("Deleted grid {}", gridId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalGrids", grids.size());

        long activeGrids = grids.values().stream()
                .filter(ReportDistributed::isHealthy)
                .count();

        long totalNodes = grids.values().stream()
                .mapToLong(g -> g.getTotalNodes() != null ? g.getTotalNodes() : 0L)
                .sum();

        long activeNodes = grids.values().stream()
                .mapToLong(g -> g.getActiveNodes() != null ? g.getActiveNodes().size() : 0L)
                .sum();

        long totalJobs = grids.values().stream()
                .mapToLong(g -> g.getTotalJobs() != null ? g.getTotalJobs() : 0L)
                .sum();

        long completedJobs = grids.values().stream()
                .mapToLong(g -> g.getCompletedJobs() != null ? g.getCompletedJobs() : 0L)
                .sum();

        long totalTasks = grids.values().stream()
                .mapToLong(g -> g.getTotalTasks() != null ? g.getTotalTasks() : 0L)
                .sum();

        stats.put("activeGrids", activeGrids);
        stats.put("totalNodes", totalNodes);
        stats.put("activeNodes", activeNodes);
        stats.put("totalJobs", totalJobs);
        stats.put("completedJobs", completedJobs);
        stats.put("totalTasks", totalTasks);

        log.debug("Generated distributed computing statistics: {} grids, {} nodes, {} jobs",
                grids.size(), totalNodes, totalJobs);

        return stats;
    }

    // Helper Methods

    /**
     * Schedule job using configured strategy
     */
    private void scheduleJob(ReportDistributed grid, ReportDistributed.ComputeJob job) {
        job.setStatus(ReportDistributed.JobStatus.SCHEDULED);
        job.setScheduledAt(LocalDateTime.now());

        // Get available nodes based on load balancing algorithm
        List<ReportDistributed.ComputeNode> availableNodes = grid.getIdleNodes();
        if (availableNodes.isEmpty()) {
            availableNodes = grid.getActiveNodes();
        }

        if (availableNodes.isEmpty()) {
            log.warn("No available nodes to schedule job {}", job.getJobId());
            return;
        }

        // Assign tasks to nodes
        List<String> taskIds = job.getTaskIds();
        for (int i = 0; i < taskIds.size(); i++) {
            String taskId = taskIds.get(i);
            ReportDistributed.Task task = grid.getTaskRegistry().get(taskId);

            if (task != null) {
                // Round-robin assignment
                ReportDistributed.ComputeNode node = availableNodes.get(i % availableNodes.size());
                task.setNodeId(node.getNodeId());
                task.setNodeName(node.getNodeName());
                task.setStatus(ReportDistributed.TaskStatus.ASSIGNED);
                task.setAssignedAt(LocalDateTime.now());

                // Update node
                node.setRunningTasks((node.getRunningTasks() != null ? node.getRunningTasks() : 0) + 1);
                if (node.getStatus() == ReportDistributed.NodeStatus.IDLE) {
                    grid.updateNodeStatus(node.getNodeId(), ReportDistributed.NodeStatus.BUSY);
                }
            }
        }

        // Start job
        grid.startJob(job.getJobId());

        // Simulate task execution
        executeTasks(grid, job);
    }

    /**
     * Execute tasks
     */
    private void executeTasks(ReportDistributed grid, ReportDistributed.ComputeJob job) {
        Random random = new Random();

        for (String taskId : job.getTaskIds()) {
            ReportDistributed.Task task = grid.getTaskRegistry().get(taskId);
            if (task != null) {
                task.setStatus(ReportDistributed.TaskStatus.RUNNING);
                task.setStartedAt(LocalDateTime.now());

                // Simulate task execution
                boolean success = random.nextDouble() > 0.05; // 95% success rate
                grid.completeTask(taskId, success);

                if (success) {
                    job.setCompletedTasks(job.getCompletedTasks() + 1);
                } else {
                    job.setFailedTasks(job.getFailedTasks() + 1);
                    task.setErrorMessage("Simulated task failure");
                }
            }
        }

        // Complete job if all tasks are done
        if (job.getCompletedTasks() + job.getFailedTasks() >= job.getTotalTasks()) {
            boolean success = job.getFailedTasks() == 0;
            grid.completeJob(job.getJobId(), success);

            if (!success) {
                job.setErrorMessage("Job failed: " + job.getFailedTasks() + " tasks failed");
            }
        }
    }
}
