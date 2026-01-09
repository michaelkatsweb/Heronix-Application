package com.heronix.controller;

import com.heronix.dto.ReportGraphProcessing;
import com.heronix.service.ReportGraphProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Graph Processing API Controller
 *
 * REST API endpoints for graph processing operations including vertex/edge management,
 * algorithm execution, traversals, queries, and network analysis.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 131 - Report Graph Processing & Analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/graph-processing")
@RequiredArgsConstructor
public class ReportGraphProcessingApiController {

    private final ReportGraphProcessingService graphService;

    /**
     * Create new graph
     * POST /api/graph-processing
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGraph(
            @RequestBody ReportGraphProcessing graph) {
        try {
            ReportGraphProcessing created = graphService.createGraph(graph);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Graph created successfully");
            response.put("graphId", created.getGraphId());
            response.put("graphName", created.getGraphName());
            response.put("graphEngine", created.getGraphEngine());
            response.put("graphType", created.getGraphType());
            response.put("status", created.getStatus());
            response.put("createdAt", created.getCreatedAt());

            log.info("Graph created via API: {}", created.getGraphId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create graph: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get graph by ID
     * GET /api/graph-processing/{graphId}
     */
    @GetMapping("/{graphId}")
    public ResponseEntity<Map<String, Object>> getGraph(@PathVariable Long graphId) {
        try {
            ReportGraphProcessing graph = graphService.getGraph(graphId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("graph", graph);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Initialize graph
     * POST /api/graph-processing/{graphId}/initialize
     */
    @PostMapping("/{graphId}/initialize")
    public ResponseEntity<Map<String, Object>> initializeGraph(@PathVariable Long graphId) {
        try {
            ReportGraphProcessing graph = graphService.initializeGraph(graphId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Graph initialized successfully");
            response.put("graphId", graph.getGraphId());
            response.put("status", graph.getStatus());
            response.put("isActive", graph.getIsActive());

            log.info("Graph initialized via API: {}", graphId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Add vertex to graph
     * POST /api/graph-processing/{graphId}/vertex
     */
    @PostMapping("/{graphId}/vertex")
    public ResponseEntity<Map<String, Object>> addVertex(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> vertexRequest) {
        try {
            String label = (String) vertexRequest.get("label");
            String vertexType = (String) vertexRequest.get("vertexType");
            Map<String, Object> properties = (Map<String, Object>) vertexRequest.get("properties");

            ReportGraphProcessing.GraphVertex vertex = graphService.addVertex(
                    graphId,
                    label,
                    ReportGraphProcessing.VertexType.valueOf(vertexType),
                    properties);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vertex added successfully");
            response.put("vertexId", vertex.getVertexId());
            response.put("label", vertex.getLabel());
            response.put("vertexType", vertex.getVertexType());
            response.put("createdAt", vertex.getCreatedAt());

            log.info("Vertex added via API: {} (graph: {})", vertex.getVertexId(), graphId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add vertex: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add edge to graph
     * POST /api/graph-processing/{graphId}/edge
     */
    @PostMapping("/{graphId}/edge")
    public ResponseEntity<Map<String, Object>> addEdge(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> edgeRequest) {
        try {
            String label = (String) edgeRequest.get("label");
            String edgeType = (String) edgeRequest.get("edgeType");
            String sourceVertexId = (String) edgeRequest.get("sourceVertexId");
            String targetVertexId = (String) edgeRequest.get("targetVertexId");
            Double weight = edgeRequest.get("weight") != null ?
                    ((Number) edgeRequest.get("weight")).doubleValue() : null;
            Boolean directed = (Boolean) edgeRequest.get("directed");

            ReportGraphProcessing.GraphEdge edge = graphService.addEdge(
                    graphId,
                    label,
                    ReportGraphProcessing.EdgeType.valueOf(edgeType),
                    sourceVertexId,
                    targetVertexId,
                    weight,
                    directed);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge added successfully");
            response.put("edgeId", edge.getEdgeId());
            response.put("label", edge.getLabel());
            response.put("edgeType", edge.getEdgeType());
            response.put("sourceVertexId", edge.getSourceVertexId());
            response.put("targetVertexId", edge.getTargetVertexId());
            response.put("weight", edge.getWeight());
            response.put("createdAt", edge.getCreatedAt());

            log.info("Edge added via API: {} (graph: {}, {} -> {})",
                    edge.getEdgeId(), graphId, sourceVertexId, targetVertexId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add edge: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Execute graph algorithm
     * POST /api/graph-processing/{graphId}/algorithm
     */
    @PostMapping("/{graphId}/algorithm")
    public ResponseEntity<Map<String, Object>> executeAlgorithm(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> algorithmRequest) {
        try {
            String algorithmName = (String) algorithmRequest.get("algorithmName");
            String algorithmType = (String) algorithmRequest.get("algorithmType");
            Map<String, Object> parameters = (Map<String, Object>) algorithmRequest.get("parameters");

            ReportGraphProcessing.GraphAlgorithm algorithm = graphService.executeAlgorithm(
                    graphId,
                    algorithmName,
                    ReportGraphProcessing.AlgorithmType.valueOf(algorithmType),
                    parameters);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Algorithm executed successfully");
            response.put("algorithmId", algorithm.getAlgorithmId());
            response.put("algorithmName", algorithm.getAlgorithmName());
            response.put("algorithmType", algorithm.getAlgorithmType());
            response.put("status", algorithm.getStatus());
            response.put("executionTime", algorithm.getExecutionTime());
            response.put("results", algorithm.getResults());
            response.put("completedAt", algorithm.getCompletedAt());

            log.info("Algorithm executed via API: {} (graph: {}, type: {})",
                    algorithm.getAlgorithmId(), graphId, algorithmType);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to execute algorithm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform graph traversal
     * POST /api/graph-processing/{graphId}/traversal
     */
    @PostMapping("/{graphId}/traversal")
    public ResponseEntity<Map<String, Object>> performTraversal(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> traversalRequest) {
        try {
            String traversalName = (String) traversalRequest.get("traversalName");
            String strategy = (String) traversalRequest.get("strategy");
            String startVertexId = (String) traversalRequest.get("startVertexId");
            String targetVertexId = (String) traversalRequest.get("targetVertexId");
            Integer maxDepth = traversalRequest.get("maxDepth") != null ?
                    ((Number) traversalRequest.get("maxDepth")).intValue() : null;

            ReportGraphProcessing.GraphTraversal traversal = graphService.performTraversal(
                    graphId,
                    traversalName,
                    strategy,
                    startVertexId,
                    targetVertexId,
                    maxDepth);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traversal performed successfully");
            response.put("traversalId", traversal.getTraversalId());
            response.put("strategy", traversal.getStrategy());
            response.put("visitedVertices", traversal.getVisitedVertices());
            response.put("visitedEdges", traversal.getVisitedEdges());
            response.put("executionTime", traversal.getExecutionTime());
            response.put("executedAt", traversal.getExecutedAt());

            log.info("Traversal performed via API: {} (graph: {}, strategy: {})",
                    traversal.getTraversalId(), graphId, strategy);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to perform traversal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Execute graph query
     * POST /api/graph-processing/{graphId}/query
     */
    @PostMapping("/{graphId}/query")
    public ResponseEntity<Map<String, Object>> executeQuery(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> queryRequest) {
        try {
            String queryName = (String) queryRequest.get("queryName");
            String queryLanguage = (String) queryRequest.get("queryLanguage");
            String queryString = (String) queryRequest.get("queryString");
            Map<String, Object> parameters = (Map<String, Object>) queryRequest.get("parameters");

            ReportGraphProcessing.GraphQuery query = graphService.executeQuery(
                    graphId,
                    queryName,
                    queryLanguage,
                    queryString,
                    parameters);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Query executed successfully");
            response.put("queryId", query.getQueryId());
            response.put("queryLanguage", query.getQueryLanguage());
            response.put("results", query.getResults());
            response.put("resultCount", query.getResultCount());
            response.put("executionTime", query.getExecutionTime());
            response.put("executedAt", query.getExecutedAt());

            log.info("Query executed via API: {} (graph: {}, language: {})",
                    query.getQueryId(), graphId, queryLanguage);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to execute query: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Create graph pattern
     * POST /api/graph-processing/{graphId}/pattern
     */
    @PostMapping("/{graphId}/pattern")
    public ResponseEntity<Map<String, Object>> createPattern(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> patternRequest) {
        try {
            String patternName = (String) patternRequest.get("patternName");
            String description = (String) patternRequest.get("description");
            List<String> vertexPattern = (List<String>) patternRequest.get("vertexPattern");
            List<String> edgePattern = (List<String>) patternRequest.get("edgePattern");
            Map<String, Object> constraints = (Map<String, Object>) patternRequest.get("constraints");

            ReportGraphProcessing.GraphPattern pattern = graphService.createPattern(
                    graphId,
                    patternName,
                    description,
                    vertexPattern,
                    edgePattern,
                    constraints);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pattern created successfully");
            response.put("patternId", pattern.getPatternId());
            response.put("patternName", pattern.getPatternName());
            response.put("vertexPattern", pattern.getVertexPattern());
            response.put("edgePattern", pattern.getEdgePattern());
            response.put("createdAt", pattern.getCreatedAt());

            log.info("Pattern created via API: {} (graph: {})", pattern.getPatternId(), graphId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create pattern: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Detect community
     * POST /api/graph-processing/{graphId}/community
     */
    @PostMapping("/{graphId}/community")
    public ResponseEntity<Map<String, Object>> detectCommunity(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> communityRequest) {
        try {
            String communityName = (String) communityRequest.get("communityName");
            String detectionAlgorithm = (String) communityRequest.get("detectionAlgorithm");
            List<String> memberVertexIds = (List<String>) communityRequest.get("memberVertexIds");

            ReportGraphProcessing.Community community = graphService.detectCommunity(
                    graphId,
                    communityName,
                    detectionAlgorithm,
                    memberVertexIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Community detected successfully");
            response.put("communityId", community.getCommunityId());
            response.put("communityName", community.getCommunityName());
            response.put("detectionAlgorithm", community.getDetectionAlgorithm());
            response.put("memberCount", community.getMemberCount());
            response.put("modularity", community.getModularity());
            response.put("density", community.getDensity());
            response.put("detectedAt", community.getDetectedAt());

            log.info("Community detected via API: {} (graph: {}, members: {})",
                    community.getCommunityId(), graphId, community.getMemberCount());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to detect community: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Calculate centrality measure
     * POST /api/graph-processing/{graphId}/centrality
     */
    @PostMapping("/{graphId}/centrality")
    public ResponseEntity<Map<String, Object>> calculateCentrality(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> centralityRequest) {
        try {
            String measureType = (String) centralityRequest.get("measureType");
            String vertexId = (String) centralityRequest.get("vertexId");
            Map<String, Object> parameters = (Map<String, Object>) centralityRequest.get("parameters");

            ReportGraphProcessing.CentralityMeasure measure = graphService.calculateCentrality(
                    graphId,
                    measureType,
                    vertexId,
                    parameters);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Centrality calculated successfully");
            response.put("measureId", measure.getMeasureId());
            response.put("measureType", measure.getMeasureType());
            response.put("vertexId", measure.getVertexId());
            response.put("score", measure.getScore());
            response.put("rank", measure.getRank());
            response.put("calculatedAt", measure.getCalculatedAt());

            log.info("Centrality calculated via API: {} (graph: {}, type: {}, vertex: {})",
                    measure.getMeasureId(), graphId, measureType, vertexId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to calculate centrality: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Find shortest path
     * POST /api/graph-processing/{graphId}/shortest-path
     */
    @PostMapping("/{graphId}/shortest-path")
    public ResponseEntity<Map<String, Object>> findShortestPath(
            @PathVariable Long graphId,
            @RequestBody Map<String, Object> pathRequest) {
        try {
            String pathName = (String) pathRequest.get("pathName");
            String algorithm = (String) pathRequest.get("algorithm");
            String sourceVertexId = (String) pathRequest.get("sourceVertexId");
            String targetVertexId = (String) pathRequest.get("targetVertexId");

            ReportGraphProcessing.GraphPath path = graphService.findShortestPath(
                    graphId,
                    pathName,
                    algorithm,
                    sourceVertexId,
                    targetVertexId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shortest path calculated successfully");
            response.put("pathId", path.getPathId());
            response.put("algorithm", path.getAlgorithm());
            response.put("sourceVertexId", path.getSourceVertexId());
            response.put("targetVertexId", path.getTargetVertexId());
            response.put("vertexPath", path.getVertexPath());
            response.put("pathLength", path.getPathLength());
            response.put("totalWeight", path.getTotalWeight());
            response.put("calculatedAt", path.getCalculatedAt());

            log.info("Shortest path calculated via API: {} (graph: {}, {} -> {})",
                    path.getPathId(), graphId, sourceVertexId, targetVertexId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to calculate shortest path: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete graph
     * DELETE /api/graph-processing/{graphId}
     */
    @DeleteMapping("/{graphId}")
    public ResponseEntity<Map<String, Object>> deleteGraph(@PathVariable Long graphId) {
        try {
            graphService.deleteGraph(graphId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Graph deleted successfully");
            response.put("graphId", graphId);
            response.put("deletedAt", LocalDateTime.now());

            log.info("Graph deleted via API: {}", graphId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all graphs statistics
     * GET /api/graph-processing/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = graphService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);

        return ResponseEntity.ok(response);
    }
}
