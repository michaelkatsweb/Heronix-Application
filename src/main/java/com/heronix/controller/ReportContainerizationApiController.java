package com.heronix.controller;

import com.heronix.dto.ReportContainerization;
import com.heronix.service.ReportContainerizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Containerization API Controller
 *
 * REST API endpoints for containerization and orchestration management.
 *
 * Endpoints:
 * - POST /api/containerization - Create cluster
 * - GET /api/containerization/{id} - Get cluster
 * - POST /api/containerization/{id}/start - Start cluster
 * - POST /api/containerization/{id}/stop - Stop cluster
 * - POST /api/containerization/{id}/node - Register node
 * - POST /api/containerization/{id}/container - Create container
 * - POST /api/containerization/{id}/container/{containerId}/start - Start container
 * - POST /api/containerization/{id}/container/{containerId}/stop - Stop container
 * - POST /api/containerization/{id}/container/{containerId}/restart - Restart container
 * - POST /api/containerization/{id}/deployment - Create deployment
 * - PUT /api/containerization/{id}/deployment/{deploymentId}/scale - Scale deployment
 * - POST /api/containerization/{id}/service - Create service
 * - POST /api/containerization/{id}/image - Register image
 * - POST /api/containerization/{id}/health-check/{containerId} - Perform health check
 * - PUT /api/containerization/{id}/metrics - Update metrics
 * - GET /api/containerization/{id}/containers/running - Get running containers
 * - GET /api/containerization/{id}/nodes/healthy - Get healthy nodes
 * - DELETE /api/containerization/{id} - Delete cluster
 * - GET /api/containerization/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 96 - Report Containerization & Orchestration
 */
@RestController
@RequestMapping("/api/containerization")
@RequiredArgsConstructor
@Slf4j
public class ReportContainerizationApiController {

    private final ReportContainerizationService containerizationService;

    /**
     * Create cluster
     */
    @PostMapping
    public ResponseEntity<ReportContainerization> createCluster(@RequestBody ReportContainerization cluster) {
        log.info("POST /api/containerization - Creating cluster: {}", cluster.getClusterName());

        try {
            ReportContainerization created = containerizationService.createCluster(cluster);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating cluster", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get cluster
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportContainerization> getCluster(@PathVariable Long id) {
        log.info("GET /api/containerization/{}", id);

        try {
            return containerizationService.getCluster(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start cluster
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startCluster(@PathVariable Long id) {
        log.info("POST /api/containerization/{}/start", id);

        try {
            containerizationService.startCluster(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cluster started");
            response.put("clusterId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop cluster
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopCluster(@PathVariable Long id) {
        log.info("POST /api/containerization/{}/stop", id);

        try {
            containerizationService.stopCluster(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cluster stopped");
            response.put("clusterId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register node
     */
    @PostMapping("/{id}/node")
    public ResponseEntity<ReportContainerization.Node> registerNode(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/containerization/{}/node", id);

        try {
            String nodeName = request.get("nodeName");
            String hostName = request.get("hostName");
            String ipAddress = request.get("ipAddress");
            String role = request.get("role");

            ReportContainerization.Node node = containerizationService.registerNode(
                    id, nodeName, hostName, ipAddress, role
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering node in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create container
     */
    @PostMapping("/{id}/container")
    public ResponseEntity<ReportContainerization.Container> createContainer(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/containerization/{}/container", id);

        try {
            String containerName = request.get("containerName");
            String imageName = request.get("imageName");
            String nodeId = request.get("nodeId");

            ReportContainerization.Container container = containerizationService.createContainer(
                    id, containerName, imageName, nodeId
            );

            return ResponseEntity.ok(container);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating container in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start container
     */
    @PostMapping("/{id}/container/{containerId}/start")
    public ResponseEntity<Map<String, Object>> startContainer(
            @PathVariable Long id,
            @PathVariable String containerId) {
        log.info("POST /api/containerization/{}/container/{}/start", id, containerId);

        try {
            containerizationService.startContainer(id, containerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Container started");
            response.put("clusterId", id);
            response.put("containerId", containerId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster or container not found: {}, {}", id, containerId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting container in cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop container
     */
    @PostMapping("/{id}/container/{containerId}/stop")
    public ResponseEntity<Map<String, Object>> stopContainer(
            @PathVariable Long id,
            @PathVariable String containerId) {
        log.info("POST /api/containerization/{}/container/{}/stop", id, containerId);

        try {
            containerizationService.stopContainer(id, containerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Container stopped");
            response.put("clusterId", id);
            response.put("containerId", containerId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster or container not found: {}, {}", id, containerId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping container in cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Restart container
     */
    @PostMapping("/{id}/container/{containerId}/restart")
    public ResponseEntity<Map<String, Object>> restartContainer(
            @PathVariable Long id,
            @PathVariable String containerId) {
        log.info("POST /api/containerization/{}/container/{}/restart", id, containerId);

        try {
            containerizationService.restartContainer(id, containerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Container restarted");
            response.put("clusterId", id);
            response.put("containerId", containerId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster or container not found: {}, {}", id, containerId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error restarting container in cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create deployment
     */
    @PostMapping("/{id}/deployment")
    public ResponseEntity<ReportContainerization.Deployment> createDeployment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/containerization/{}/deployment", id);

        try {
            String deploymentName = (String) request.get("deploymentName");
            String imageName = (String) request.get("imageName");
            Integer replicas = ((Number) request.get("replicas")).intValue();

            ReportContainerization.Deployment deployment = containerizationService.createDeployment(
                    id, deploymentName, imageName, replicas
            );

            return ResponseEntity.ok(deployment);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating deployment in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Scale deployment
     */
    @PutMapping("/{id}/deployment/{deploymentId}/scale")
    public ResponseEntity<Map<String, Object>> scaleDeployment(
            @PathVariable Long id,
            @PathVariable String deploymentId,
            @RequestBody Map<String, Integer> request) {
        log.info("PUT /api/containerization/{}/deployment/{}/scale", id, deploymentId);

        try {
            Integer replicas = request.get("replicas");

            containerizationService.scaleDeployment(id, deploymentId, replicas);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deployment scaled");
            response.put("clusterId", id);
            response.put("deploymentId", deploymentId);
            response.put("replicas", replicas);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster or deployment not found: {}, {}", id, deploymentId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error scaling deployment in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create service
     */
    @PostMapping("/{id}/service")
    public ResponseEntity<ReportContainerization.Service> createService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/containerization/{}/service", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String serviceType = (String) request.get("serviceType");

            @SuppressWarnings("unchecked")
            Map<String, String> selector = (Map<String, String>) request.get("selector");

            ReportContainerization.Service service = containerizationService.createService(
                    id, serviceName, serviceType, selector
            );

            return ResponseEntity.ok(service);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating service in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register image
     */
    @PostMapping("/{id}/image")
    public ResponseEntity<ReportContainerization.ContainerImage> registerImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/containerization/{}/image", id);

        try {
            String repository = request.get("repository");
            String tag = request.get("tag");
            String digest = request.get("digest");

            ReportContainerization.ContainerImage image = containerizationService.registerImage(
                    id, repository, tag, digest
            );

            return ResponseEntity.ok(image);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering image in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform health check
     */
    @PostMapping("/{id}/health-check/{containerId}")
    public ResponseEntity<Map<String, Object>> performHealthCheck(
            @PathVariable Long id,
            @PathVariable String containerId) {
        log.info("POST /api/containerization/{}/health-check/{}", id, containerId);

        try {
            containerizationService.performHealthCheck(id, containerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Health check performed");
            response.put("clusterId", id);
            response.put("containerId", containerId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster or container not found: {}, {}", id, containerId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing health check in cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/containerization/{}/metrics", id);

        try {
            containerizationService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("clusterId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cluster not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for cluster: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get running containers
     */
    @GetMapping("/{id}/containers/running")
    public ResponseEntity<Map<String, Object>> getRunningContainers(@PathVariable Long id) {
        log.info("GET /api/containerization/{}/containers/running", id);

        try {
            return containerizationService.getCluster(id)
                    .map(cluster -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("clusterId", id);
                        response.put("containers", cluster.getRunningContainers());
                        response.put("count", cluster.getRunningContainers().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching running containers for cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get healthy nodes
     */
    @GetMapping("/{id}/nodes/healthy")
    public ResponseEntity<Map<String, Object>> getHealthyNodes(@PathVariable Long id) {
        log.info("GET /api/containerization/{}/nodes/healthy", id);

        try {
            return containerizationService.getCluster(id)
                    .map(cluster -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("clusterId", id);
                        response.put("nodes", cluster.getHealthyNodes());
                        response.put("count", cluster.getHealthyNodes().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching healthy nodes for cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete cluster
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCluster(@PathVariable Long id) {
        log.info("DELETE /api/containerization/{}", id);

        try {
            containerizationService.deleteCluster(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cluster deleted");
            response.put("clusterId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting cluster: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/containerization/stats");

        try {
            Map<String, Object> stats = containerizationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching containerization statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
