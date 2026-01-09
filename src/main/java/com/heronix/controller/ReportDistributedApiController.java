package com.heronix.controller;

import com.heronix.dto.ReportDistributed;
import com.heronix.service.ReportDistributedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Distributed Computing API Controller
 *
 * REST API endpoints for distributed computing and grid systems.
 *
 * Endpoints:
 * - POST /api/distributed - Create grid
 * - GET /api/distributed/{id} - Get grid
 * - POST /api/distributed/{id}/start - Start grid
 * - POST /api/distributed/{id}/stop - Stop grid
 * - POST /api/distributed/{id}/node - Register compute node
 * - POST /api/distributed/{id}/job - Submit compute job
 * - POST /api/distributed/{id}/partition - Create data partitions
 * - POST /api/distributed/{id}/mapreduce - Submit MapReduce job
 * - POST /api/distributed/{id}/pool - Create resource pool
 * - GET /api/distributed/{id}/nodes/idle - Get idle nodes
 * - PUT /api/distributed/{id}/metrics - Update metrics
 * - DELETE /api/distributed/{id} - Delete grid
 * - GET /api/distributed/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 102 - Report Distributed Computing & Grid Systems
 */
@RestController
@RequestMapping("/api/distributed")
@RequiredArgsConstructor
@Slf4j
public class ReportDistributedApiController {

    private final ReportDistributedService distributedService;

    /**
     * Create grid
     */
    @PostMapping
    public ResponseEntity<ReportDistributed> createGrid(@RequestBody ReportDistributed grid) {
        log.info("POST /api/distributed - Creating grid: {}", grid.getGridName());

        try {
            ReportDistributed created = distributedService.createGrid(grid);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating grid", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get grid
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDistributed> getGrid(@PathVariable Long id) {
        log.info("GET /api/distributed/{}", id);

        try {
            return distributedService.getGrid(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching grid: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start grid
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startGrid(@PathVariable Long id) {
        log.info("POST /api/distributed/{}/start", id);

        try {
            distributedService.startGrid(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Grid started");
            response.put("gridId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting grid: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop grid
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopGrid(@PathVariable Long id) {
        log.info("POST /api/distributed/{}/stop", id);

        try {
            distributedService.stopGrid(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Grid stopped");
            response.put("gridId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping grid: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register compute node
     */
    @PostMapping("/{id}/node")
    public ResponseEntity<ReportDistributed.ComputeNode> registerNode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/distributed/{}/node", id);

        try {
            String nodeName = (String) request.get("nodeName");
            String ipAddress = (String) request.get("ipAddress");
            Integer cpuCores = ((Number) request.get("cpuCores")).intValue();
            Long memoryMb = ((Number) request.get("memoryMb")).longValue();
            Long diskGb = ((Number) request.get("diskGb")).longValue();

            ReportDistributed.ComputeNode node = distributedService.registerNode(
                    id, nodeName, ipAddress, cpuCores, memoryMb, diskGb
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering node in grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit compute job
     */
    @PostMapping("/{id}/job")
    public ResponseEntity<ReportDistributed.ComputeJob> submitJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/distributed/{}/job", id);

        try {
            String jobName = (String) request.get("jobName");
            String jobType = (String) request.get("jobType");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");
            Integer numTasks = request.get("numTasks") != null ?
                    ((Number) request.get("numTasks")).intValue() : 1;

            // Parse resource requirements if provided
            ReportDistributed.ResourceRequirements requirements = null;
            if (request.containsKey("resourceRequirements")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> reqMap = (Map<String, Object>) request.get("resourceRequirements");
                requirements = ReportDistributed.ResourceRequirements.builder()
                        .cpuCores(reqMap.get("cpuCores") != null ?
                                ((Number) reqMap.get("cpuCores")).intValue() : 1)
                        .memoryMb(reqMap.get("memoryMb") != null ?
                                ((Number) reqMap.get("memoryMb")).longValue() : 1024L)
                        .build();
            }

            ReportDistributed.ComputeJob job = distributedService.submitJob(
                    id, jobName, jobType, inputData, numTasks, requirements
            );

            return ResponseEntity.ok(job);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting job to grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create data partitions
     */
    @PostMapping("/{id}/partition")
    public ResponseEntity<Map<String, Object>> createPartitions(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/distributed/{}/partition", id);

        try {
            String datasetId = (String) request.get("datasetId");
            Integer numPartitions = ((Number) request.get("numPartitions")).intValue();
            String strategyStr = (String) request.get("strategy");

            ReportDistributed.PartitionStrategy strategy =
                    ReportDistributed.PartitionStrategy.valueOf(strategyStr);

            List<ReportDistributed.DataPartition> partitions = distributedService.createPartitions(
                    id, datasetId, numPartitions, strategy
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Partitions created");
            response.put("gridId", id);
            response.put("partitions", partitions);
            response.put("count", partitions.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating partitions in grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit MapReduce job
     */
    @PostMapping("/{id}/mapreduce")
    public ResponseEntity<ReportDistributed.MapReduceJob> submitMapReduceJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/distributed/{}/mapreduce", id);

        try {
            String jobName = (String) request.get("jobName");
            String inputDataset = (String) request.get("inputDataset");
            String outputDataset = (String) request.get("outputDataset");
            Integer mapTasks = ((Number) request.get("mapTasks")).intValue();
            Integer reduceTasks = ((Number) request.get("reduceTasks")).intValue();

            ReportDistributed.MapReduceJob mrJob = distributedService.submitMapReduceJob(
                    id, jobName, inputDataset, outputDataset, mapTasks, reduceTasks
            );

            return ResponseEntity.ok(mrJob);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting MapReduce job to grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create resource pool
     */
    @PostMapping("/{id}/pool")
    public ResponseEntity<ReportDistributed.ResourcePool> createResourcePool(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/distributed/{}/pool", id);

        try {
            String poolName = (String) request.get("poolName");
            @SuppressWarnings("unchecked")
            List<String> nodeIds = (List<String>) request.get("nodeIds");

            ReportDistributed.ResourcePool pool = distributedService.createResourcePool(
                    id, poolName, nodeIds
            );

            return ResponseEntity.ok(pool);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating resource pool in grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get idle nodes
     */
    @GetMapping("/{id}/nodes/idle")
    public ResponseEntity<Map<String, Object>> getIdleNodes(@PathVariable Long id) {
        log.info("GET /api/distributed/{}/nodes/idle", id);

        try {
            return distributedService.getGrid(id)
                    .map(grid -> {
                        List<ReportDistributed.ComputeNode> idleNodes = grid.getIdleNodes();
                        Map<String, Object> response = new HashMap<>();
                        response.put("gridId", id);
                        response.put("nodes", idleNodes);
                        response.put("count", idleNodes.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching idle nodes for grid: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/distributed/{}/metrics", id);

        try {
            distributedService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("gridId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Grid not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for grid: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete grid
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGrid(@PathVariable Long id) {
        log.info("DELETE /api/distributed/{}", id);

        try {
            distributedService.deleteGrid(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Grid deleted");
            response.put("gridId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting grid: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/distributed/stats");

        try {
            Map<String, Object> stats = distributedService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching distributed computing statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
