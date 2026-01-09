package com.heronix.controller;

import com.heronix.dto.ReportEdgeComputing;
import com.heronix.service.ReportEdgeComputingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Edge Computing API Controller
 *
 * REST API endpoints for edge computing and CDN management.
 *
 * Endpoints:
 * - POST /api/edge-computing - Create network
 * - GET /api/edge-computing/{id} - Get network
 * - POST /api/edge-computing/{id}/start - Start network
 * - POST /api/edge-computing/{id}/stop - Stop network
 * - POST /api/edge-computing/{id}/node - Register edge node
 * - PUT /api/edge-computing/{id}/node/{nodeId}/status - Update node status
 * - POST /api/edge-computing/{id}/content - Register content
 * - POST /api/edge-computing/{id}/content/{contentId}/cache - Cache content
 * - DELETE /api/edge-computing/{id}/content/{contentId}/cache - Invalidate cache
 * - POST /api/edge-computing/{id}/request - Record request
 * - PUT /api/edge-computing/{id}/metrics - Update metrics
 * - GET /api/edge-computing/{id}/nodes/active - Get active nodes
 * - GET /api/edge-computing/{id}/nodes/region/{region} - Get nodes by region
 * - DELETE /api/edge-computing/{id} - Delete network
 * - GET /api/edge-computing/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 98 - Report Edge Computing & CDN
 */
@RestController
@RequestMapping("/api/edge-computing")
@RequiredArgsConstructor
@Slf4j
public class ReportEdgeComputingApiController {

    private final ReportEdgeComputingService edgeComputingService;

    /**
     * Create network
     */
    @PostMapping
    public ResponseEntity<ReportEdgeComputing> createNetwork(@RequestBody ReportEdgeComputing network) {
        log.info("POST /api/edge-computing - Creating network: {}", network.getNetworkName());

        try {
            ReportEdgeComputing created = edgeComputingService.createNetwork(network);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating network", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get network
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEdgeComputing> getNetwork(@PathVariable Long id) {
        log.info("GET /api/edge-computing/{}", id);

        try {
            return edgeComputingService.getNetwork(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start network
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startNetwork(@PathVariable Long id) {
        log.info("POST /api/edge-computing/{}/start", id);

        try {
            edgeComputingService.startNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Network started");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop network
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopNetwork(@PathVariable Long id) {
        log.info("POST /api/edge-computing/{}/stop", id);

        try {
            edgeComputingService.stopNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Network stopped");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register edge node
     */
    @PostMapping("/{id}/node")
    public ResponseEntity<ReportEdgeComputing.EdgeNode> registerNode(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/edge-computing/{}/node", id);

        try {
            String nodeName = request.get("nodeName");
            String region = request.get("region");
            String country = request.get("country");
            String city = request.get("city");

            ReportEdgeComputing.EdgeNode node = edgeComputingService.registerNode(
                    id, nodeName, region, country, city
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering node in network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update node status
     */
    @PutMapping("/{id}/node/{nodeId}/status")
    public ResponseEntity<Map<String, Object>> updateNodeStatus(
            @PathVariable Long id,
            @PathVariable String nodeId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/edge-computing/{}/node/{}/status", id, nodeId);

        try {
            String statusStr = request.get("status");
            ReportEdgeComputing.EdgeNodeStatus status = ReportEdgeComputing.EdgeNodeStatus.valueOf(statusStr);

            edgeComputingService.updateNodeStatus(id, nodeId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Node status updated");
            response.put("networkId", id);
            response.put("nodeId", nodeId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network or node not found: {}, {}", id, nodeId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating node status in network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register content
     */
    @PostMapping("/{id}/content")
    public ResponseEntity<ReportEdgeComputing.Content> registerContent(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/edge-computing/{}/content", id);

        try {
            String contentPath = request.get("contentPath");
            String contentType = request.get("contentType");
            String originUrl = request.get("originUrl");

            ReportEdgeComputing.Content content = edgeComputingService.registerContent(
                    id, contentPath, contentType, originUrl
            );

            return ResponseEntity.ok(content);

        } catch (IllegalArgumentException e) {
            log.error("Network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering content in network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cache content
     */
    @PostMapping("/{id}/content/{contentId}/cache")
    public ResponseEntity<Map<String, Object>> cacheContent(
            @PathVariable Long id,
            @PathVariable String contentId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-computing/{}/content/{}/cache", id, contentId);

        try {
            @SuppressWarnings("unchecked")
            List<String> nodeIds = (List<String>) request.get("nodeIds");

            edgeComputingService.cacheContent(id, contentId, nodeIds);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Content cached");
            response.put("networkId", id);
            response.put("contentId", contentId);
            response.put("nodeCount", nodeIds != null ? nodeIds.size() : 0);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network or content not found: {}, {}", id, contentId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error caching content in network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Invalidate cache
     */
    @DeleteMapping("/{id}/content/{contentId}/cache")
    public ResponseEntity<Map<String, Object>> invalidateCache(
            @PathVariable Long id,
            @PathVariable String contentId) {
        log.info("DELETE /api/edge-computing/{}/content/{}/cache", id, contentId);

        try {
            edgeComputingService.invalidateCache(id, contentId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache invalidated");
            response.put("networkId", id);
            response.put("contentId", contentId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network or content not found: {}, {}", id, contentId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error invalidating cache in network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record request
     */
    @PostMapping("/{id}/request")
    public ResponseEntity<Map<String, Object>> recordRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge-computing/{}/request", id);

        try {
            String clientIp = (String) request.get("clientIp");
            String method = (String) request.get("method");
            String path = (String) request.get("path");
            String nodeId = (String) request.get("nodeId");
            Integer statusCode = ((Number) request.get("statusCode")).intValue();
            Long responseTimeMs = ((Number) request.get("responseTimeMs")).longValue();

            String cacheStatusStr = (String) request.get("cacheStatus");
            ReportEdgeComputing.CacheStatus cacheStatus = cacheStatusStr != null ?
                    ReportEdgeComputing.CacheStatus.valueOf(cacheStatusStr) :
                    ReportEdgeComputing.CacheStatus.MISS;

            edgeComputingService.recordRequest(id, clientIp, method, path,
                    nodeId, statusCode, responseTimeMs, cacheStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request recorded");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording request for network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/edge-computing/{}/metrics", id);

        try {
            edgeComputingService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active nodes
     */
    @GetMapping("/{id}/nodes/active")
    public ResponseEntity<Map<String, Object>> getActiveNodes(@PathVariable Long id) {
        log.info("GET /api/edge-computing/{}/nodes/active", id);

        try {
            return edgeComputingService.getNetwork(id)
                    .map(network -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("networkId", id);
                        response.put("nodes", network.getActiveNodes());
                        response.put("count", network.getActiveNodes().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active nodes for network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get nodes by region
     */
    @GetMapping("/{id}/nodes/region/{region}")
    public ResponseEntity<Map<String, Object>> getNodesByRegion(
            @PathVariable Long id,
            @PathVariable String region) {
        log.info("GET /api/edge-computing/{}/nodes/region/{}", id, region);

        try {
            return edgeComputingService.getNetwork(id)
                    .map(network -> {
                        List<ReportEdgeComputing.EdgeNode> nodes = network.getNodesByRegion(region);
                        Map<String, Object> response = new HashMap<>();
                        response.put("networkId", id);
                        response.put("region", region);
                        response.put("nodes", nodes);
                        response.put("count", nodes.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching nodes by region for network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete network
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNetwork(@PathVariable Long id) {
        log.info("DELETE /api/edge-computing/{}", id);

        try {
            edgeComputingService.deleteNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Network deleted");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/edge-computing/stats");

        try {
            Map<String, Object> stats = edgeComputingService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching edge computing statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
