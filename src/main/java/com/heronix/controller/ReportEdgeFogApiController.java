package com.heronix.controller;

import com.heronix.dto.ReportEdgeFog;
import com.heronix.service.ReportEdgeFogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Edge & Fog Computing API Controller
 *
 * REST API endpoints for edge computing infrastructure and IoT device management.
 *
 * Endpoints:
 * - POST /api/edge-fog - Create edge infrastructure
 * - GET /api/edge-fog/{id} - Get edge infrastructure
 * - POST /api/edge-fog/{id}/deploy - Deploy infrastructure
 * - POST /api/edge-fog/{id}/edge-node - Register edge node
 * - POST /api/edge-fog/{id}/fog-node - Register fog node
 * - POST /api/edge-fog/{id}/device - Register IoT device
 * - POST /api/edge-fog/{id}/task - Schedule edge task
 * - POST /api/edge-fog/{id}/task/complete - Complete task
 * - POST /api/edge-fog/{id}/stream - Start data stream
 * - POST /api/edge-fog/{id}/deployment - Deploy service
 * - POST /api/edge-fog/{id}/network-link - Create network link
 * - POST /api/edge-fog/{id}/service - Register edge service
 * - POST /api/edge-fog/{id}/sync - Perform sync operation
 * - DELETE /api/edge-fog/{id} - Delete edge infrastructure
 * - GET /api/edge-fog/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 117 - Report Edge & Fog Computing
 */
@RestController
@RequestMapping("/api/edge-fog")
@RequiredArgsConstructor
@Slf4j
public class ReportEdgeFogApiController {

    private final ReportEdgeFogService edgeFogService;

    /**
     * Create edge infrastructure
     */
    @PostMapping
    public ResponseEntity<ReportEdgeFog> createEdgeInfrastructure(@RequestBody ReportEdgeFog edge) {
        log.info("POST /api/edge-fog - Creating edge infrastructure: {}", edge.getEdgeName());

        try {
            ReportEdgeFog created = edgeFogService.createEdgeInfrastructure(edge);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating edge infrastructure", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get edge infrastructure
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEdgeFog> getEdgeInfrastructure(@PathVariable Long id) {
        log.info("GET /api/edge-fog/{}", id);

        try {
            return edgeFogService.getEdgeInfrastructure(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching edge infrastructure: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy infrastructure
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployInfrastructure(@PathVariable Long id) {
        log.info("POST /api/edge-fog/{}/deploy", id);

        try {
            edgeFogService.deployInfrastructure(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Edge infrastructure deployed");
            response.put("edgeId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying edge infrastructure: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register edge node
     */
    @PostMapping("/{id}/edge-node")
    public ResponseEntity<ReportEdgeFog.EdgeNode> registerEdgeNode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/edge-node", id);

        try {
            String nodeName = (String) request.get("nodeName");
            String location = (String) request.get("location");
            String ipAddress = (String) request.get("ipAddress");
            Integer cpuCores = request.get("cpuCores") != null ?
                    ((Number) request.get("cpuCores")).intValue() : 4;
            Long memoryMb = request.get("memoryMb") != null ?
                    ((Number) request.get("memoryMb")).longValue() : 8192L;

            ReportEdgeFog.EdgeNode node = edgeFogService.registerEdgeNode(
                    id, nodeName, location, ipAddress, cpuCores, memoryMb
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering edge node: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register fog node
     */
    @PostMapping("/{id}/fog-node")
    public ResponseEntity<ReportEdgeFog.FogNode> registerFogNode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/fog-node", id);

        try {
            String fogName = (String) request.get("fogName");
            String region = (String) request.get("region");
            @SuppressWarnings("unchecked")
            List<String> edgeNodeIds = (List<String>) request.get("edgeNodeIds");

            ReportEdgeFog.FogNode fogNode = edgeFogService.registerFogNode(
                    id, fogName, region, edgeNodeIds
            );

            return ResponseEntity.ok(fogNode);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering fog node: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register IoT device
     */
    @PostMapping("/{id}/device")
    public ResponseEntity<ReportEdgeFog.IoTDevice> registerDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/edge-fog/{}/device", id);

        try {
            String deviceName = request.get("deviceName");
            String deviceTypeStr = request.get("deviceType");
            String edgeNodeId = request.get("edgeNodeId");
            String location = request.get("location");

            ReportEdgeFog.DeviceType deviceType =
                    ReportEdgeFog.DeviceType.valueOf(deviceTypeStr);

            ReportEdgeFog.IoTDevice device = edgeFogService.registerDevice(
                    id, deviceName, deviceType, edgeNodeId, location
            );

            return ResponseEntity.ok(device);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering IoT device: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Schedule edge task
     */
    @PostMapping("/{id}/task")
    public ResponseEntity<ReportEdgeFog.EdgeTask> scheduleTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/task", id);

        try {
            String taskName = (String) request.get("taskName");
            String taskType = (String) request.get("taskType");
            String assignedNodeId = (String) request.get("assignedNodeId");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");

            ReportEdgeFog.EdgeTask task = edgeFogService.scheduleTask(
                    id, taskName, taskType, assignedNodeId, inputData
            );

            return ResponseEntity.ok(task);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error scheduling edge task: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete task
     */
    @PostMapping("/{id}/task/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/task/complete", id);

        try {
            String taskId = (String) request.get("taskId");
            Boolean success = (Boolean) request.get("success");

            edgeFogService.completeTask(id, taskId, success != null && success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task completed");
            response.put("taskId", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing task: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start data stream
     */
    @PostMapping("/{id}/stream")
    public ResponseEntity<ReportEdgeFog.DataStream> startDataStream(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/edge-fog/{}/stream", id);

        try {
            String streamName = request.get("streamName");
            String streamTypeStr = request.get("streamType");
            String sourceDeviceId = request.get("sourceDeviceId");
            String sourceNodeId = request.get("sourceNodeId");

            ReportEdgeFog.StreamType streamType =
                    ReportEdgeFog.StreamType.valueOf(streamTypeStr);

            ReportEdgeFog.DataStream stream = edgeFogService.startDataStream(
                    id, streamName, streamType, sourceDeviceId, sourceNodeId
            );

            return ResponseEntity.ok(stream);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting data stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deploy service
     */
    @PostMapping("/{id}/deployment")
    public ResponseEntity<ReportEdgeFog.EdgeDeployment> deployService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/deployment", id);

        try {
            String deploymentName = (String) request.get("deploymentName");
            String serviceId = (String) request.get("serviceId");
            @SuppressWarnings("unchecked")
            List<String> targetNodeIds = (List<String>) request.get("targetNodeIds");
            String containerImage = (String) request.get("containerImage");

            ReportEdgeFog.EdgeDeployment deployment = edgeFogService.deployService(
                    id, deploymentName, serviceId, targetNodeIds, containerImage
            );

            return ResponseEntity.ok(deployment);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying service: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create network link
     */
    @PostMapping("/{id}/network-link")
    public ResponseEntity<ReportEdgeFog.NetworkLink> createNetworkLink(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/network-link", id);

        try {
            String sourceNodeId = (String) request.get("sourceNodeId");
            String targetNodeId = (String) request.get("targetNodeId");
            String linkType = (String) request.get("linkType");
            Double bandwidth = request.get("bandwidth") != null ?
                    ((Number) request.get("bandwidth")).doubleValue() : 100.0;

            ReportEdgeFog.NetworkLink link = edgeFogService.createNetworkLink(
                    id, sourceNodeId, targetNodeId, linkType, bandwidth
            );

            return ResponseEntity.ok(link);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating network link: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register edge service
     */
    @PostMapping("/{id}/service")
    public ResponseEntity<ReportEdgeFog.EdgeService> registerEdgeService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/service", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String serviceType = (String) request.get("serviceType");
            @SuppressWarnings("unchecked")
            List<String> deployedNodeIds = (List<String>) request.get("deployedNodeIds");

            ReportEdgeFog.EdgeService service = edgeFogService.registerEdgeService(
                    id, serviceName, serviceType, deployedNodeIds
            );

            return ResponseEntity.ok(service);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering edge service: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform sync operation
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<ReportEdgeFog.SyncOperation> performSync(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-fog/{}/sync", id);

        try {
            String syncType = (String) request.get("syncType");
            String sourceNodeId = (String) request.get("sourceNodeId");
            String targetNodeId = (String) request.get("targetNodeId");
            Long dataSizeByte = request.get("dataSizeByte") != null ?
                    ((Number) request.get("dataSizeByte")).longValue() : 1024L;

            ReportEdgeFog.SyncOperation sync = edgeFogService.performSync(
                    id, syncType, sourceNodeId, targetNodeId, dataSizeByte
            );

            return ResponseEntity.ok(sync);

        } catch (IllegalArgumentException e) {
            log.error("Edge infrastructure not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing sync operation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete edge infrastructure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEdgeInfrastructure(@PathVariable Long id) {
        log.info("DELETE /api/edge-fog/{}", id);

        try {
            edgeFogService.deleteEdgeInfrastructure(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Edge infrastructure deleted");
            response.put("edgeId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting edge infrastructure: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/edge-fog/stats");

        try {
            Map<String, Object> stats = edgeFogService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching edge infrastructure statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
