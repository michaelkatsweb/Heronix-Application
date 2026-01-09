package com.heronix.controller;

import com.heronix.dto.ReportEdge;
import com.heronix.service.ReportEdgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Edge Computing & CDN API Controller
 *
 * REST API endpoints for edge network and content delivery management.
 *
 * Endpoints:
 * - POST /api/edge - Create edge network
 * - GET /api/edge/{id} - Get edge network
 * - POST /api/edge/{id}/deploy - Deploy network
 * - POST /api/edge/{id}/node - Add edge node
 * - POST /api/edge/{id}/cache - Cache content
 * - POST /api/edge/{id}/workload - Submit edge workload
 * - POST /api/edge/{id}/invalidation - Create invalidation
 * - POST /api/edge/{id}/origin - Add origin
 * - POST /api/edge/{id}/routing - Add routing rule
 * - POST /api/edge/{id}/ssl - Configure SSL
 * - GET /api/edge/{id}/nodes/online - Get online nodes
 * - GET /api/edge/{id}/nodes/region/{region} - Get nodes by region
 * - PUT /api/edge/{id}/metrics - Update metrics
 * - DELETE /api/edge/{id} - Delete network
 * - GET /api/edge/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 106 - Report Edge Computing & CDN
 */
@RestController
@RequestMapping("/api/edge")
@RequiredArgsConstructor
@Slf4j
public class ReportEdgeApiController {

    private final ReportEdgeService edgeService;

    /**
     * Create edge network
     */
    @PostMapping
    public ResponseEntity<ReportEdge> createNetwork(@RequestBody ReportEdge network) {
        log.info("POST /api/edge - Creating edge network: {}", network.getNetworkName());

        try {
            ReportEdge created = edgeService.createNetwork(network);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating edge network", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get edge network
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEdge> getNetwork(@PathVariable Long id) {
        log.info("GET /api/edge/{}", id);

        try {
            return edgeService.getNetwork(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching edge network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy network
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployNetwork(@PathVariable Long id) {
        log.info("POST /api/edge/{}/deploy", id);

        try {
            edgeService.deployNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Edge network deployed");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying edge network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add edge node
     */
    @PostMapping("/{id}/node")
    public ResponseEntity<ReportEdge.EdgeNode> addNode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/node", id);

        try {
            String nodeName = (String) request.get("nodeName");
            String region = (String) request.get("region");
            String city = (String) request.get("city");
            String country = (String) request.get("country");
            Double latitude = request.get("latitude") != null ?
                    ((Number) request.get("latitude")).doubleValue() : 0.0;
            Double longitude = request.get("longitude") != null ?
                    ((Number) request.get("longitude")).doubleValue() : 0.0;

            ReportEdge.EdgeNode node = edgeService.addNode(
                    id, nodeName, region, city, country, latitude, longitude
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding node to edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cache content
     */
    @PostMapping("/{id}/cache")
    public ResponseEntity<ReportEdge.CachedContent> cacheContent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/cache", id);

        try {
            String contentKey = (String) request.get("contentKey");
            String contentType = (String) request.get("contentType");
            Long contentSize = request.get("contentSize") != null ?
                    ((Number) request.get("contentSize")).longValue() : 0L;
            String originUrl = (String) request.get("originUrl");
            Integer ttlSeconds = request.get("ttlSeconds") != null ?
                    ((Number) request.get("ttlSeconds")).intValue() : 3600;

            ReportEdge.CachedContent content = edgeService.cacheContent(
                    id, contentKey, contentType, contentSize, originUrl, ttlSeconds
            );

            return ResponseEntity.ok(content);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error caching content in edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit edge workload
     */
    @PostMapping("/{id}/workload")
    public ResponseEntity<ReportEdge.EdgeWorkload> submitWorkload(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/workload", id);

        try {
            String workloadName = (String) request.get("workloadName");
            String workloadTypeStr = (String) request.get("workloadType");
            String functionCode = (String) request.get("functionCode");
            @SuppressWarnings("unchecked")
            Map<String, Object> input = (Map<String, Object>) request.get("input");

            ReportEdge.WorkloadType workloadType = ReportEdge.WorkloadType.valueOf(workloadTypeStr);

            ReportEdge.EdgeWorkload workload = edgeService.submitWorkload(
                    id, workloadName, workloadType, functionCode, input
            );

            return ResponseEntity.ok(workload);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting workload to edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create invalidation
     */
    @PostMapping("/{id}/invalidation")
    public ResponseEntity<ReportEdge.InvalidationRequest> createInvalidation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/invalidation", id);

        try {
            @SuppressWarnings("unchecked")
            List<String> contentKeys = (List<String>) request.get("contentKeys");
            @SuppressWarnings("unchecked")
            List<String> pathPatterns = (List<String>) request.get("pathPatterns");
            String reason = (String) request.get("reason");

            ReportEdge.InvalidationRequest invalidation = edgeService.createInvalidation(
                    id, contentKeys, pathPatterns, reason
            );

            return ResponseEntity.ok(invalidation);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating invalidation in edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add origin
     */
    @PostMapping("/{id}/origin")
    public ResponseEntity<ReportEdge.Origin> addOrigin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/origin", id);

        try {
            String originName = (String) request.get("originName");
            String originUrl = (String) request.get("originUrl");
            Boolean isPrimary = request.get("isPrimary") != null ?
                    (Boolean) request.get("isPrimary") : false;
            Integer priority = request.get("priority") != null ?
                    ((Number) request.get("priority")).intValue() : 100;

            ReportEdge.Origin origin = edgeService.addOrigin(
                    id, originName, originUrl, isPrimary, priority
            );

            return ResponseEntity.ok(origin);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding origin to edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add routing rule
     */
    @PostMapping("/{id}/routing")
    public ResponseEntity<ReportEdge.RoutingRule> addRoutingRule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/routing", id);

        try {
            String ruleName = (String) request.get("ruleName");
            Integer priority = request.get("priority") != null ?
                    ((Number) request.get("priority")).intValue() : 100;
            String pathPattern = (String) request.get("pathPattern");
            String targetOriginId = (String) request.get("targetOriginId");

            ReportEdge.RoutingRule rule = edgeService.addRoutingRule(
                    id, ruleName, priority, pathPattern, targetOriginId
            );

            return ResponseEntity.ok(rule);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding routing rule to edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Configure SSL
     */
    @PostMapping("/{id}/ssl")
    public ResponseEntity<ReportEdge.SSLConfiguration> configureSSL(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/edge/{}/ssl", id);

        try {
            String domainName = (String) request.get("domainName");
            @SuppressWarnings("unchecked")
            List<String> subjectAlternativeNames = (List<String>) request.get("subjectAlternativeNames");

            ReportEdge.SSLConfiguration sslConfig = edgeService.configureSSL(
                    id, domainName, subjectAlternativeNames
            );

            return ResponseEntity.ok(sslConfig);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error configuring SSL for edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get online nodes
     */
    @GetMapping("/{id}/nodes/online")
    public ResponseEntity<Map<String, Object>> getOnlineNodes(@PathVariable Long id) {
        log.info("GET /api/edge/{}/nodes/online", id);

        try {
            return edgeService.getNetwork(id)
                    .map(network -> {
                        List<ReportEdge.EdgeNode> onlineNodes = network.getOnlineNodes();
                        Map<String, Object> response = new HashMap<>();
                        response.put("networkId", id);
                        response.put("nodes", onlineNodes);
                        response.put("count", onlineNodes.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching online nodes for edge network: {}", id, e);
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
        log.info("GET /api/edge/{}/nodes/region/{}", id, region);

        try {
            return edgeService.getNetwork(id)
                    .map(network -> {
                        List<ReportEdge.EdgeNode> regionNodes = network.getNodesByRegion(region);
                        Map<String, Object> response = new HashMap<>();
                        response.put("networkId", id);
                        response.put("region", region);
                        response.put("nodes", regionNodes);
                        response.put("count", regionNodes.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching nodes by region for edge network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/edge/{}/metrics", id);

        try {
            edgeService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Edge network not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for edge network: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete network
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNetwork(@PathVariable Long id) {
        log.info("DELETE /api/edge/{}", id);

        try {
            edgeService.deleteNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Edge network deleted");
            response.put("networkId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting edge network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/edge/stats");

        try {
            Map<String, Object> stats = edgeService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching edge statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
