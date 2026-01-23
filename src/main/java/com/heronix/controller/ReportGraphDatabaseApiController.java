package com.heronix.controller;

import com.heronix.dto.ReportGraphDatabase;
import com.heronix.service.ReportGraphDatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Graph Database API Controller
 *
 * REST API endpoints for graph database management, knowledge graphs, graph traversal,
 * and semantic network operations.
 *
 * Endpoints:
 * - POST /api/graph-database - Create graph database
 * - GET /api/graph-database/{id} - Get graph database
 * - POST /api/graph-database/{id}/deploy - Deploy graph database
 * - POST /api/graph-database/{id}/vertex - Add vertex
 * - POST /api/graph-database/{id}/edge - Add edge
 * - POST /api/graph-database/{id}/schema - Create schema
 * - POST /api/graph-database/{id}/query - Execute query
 * - POST /api/graph-database/{id}/traversal - Execute traversal
 * - POST /api/graph-database/{id}/shortest-path - Find shortest path
 * - POST /api/graph-database/{id}/pattern - Create pattern
 * - POST /api/graph-database/{id}/index - Create index
 * - POST /api/graph-database/{id}/knowledge-graph - Create knowledge graph
 * - POST /api/graph-database/{id}/knowledge-graph/{kgId}/entity - Add entity
 * - POST /api/graph-database/{id}/knowledge-graph/{kgId}/relationship - Add relationship
 * - DELETE /api/graph-database/{id} - Delete graph database
 * - GET /api/graph-database/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 125 - Report Graph Database & Knowledge Graph
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/graph-database")
@RequiredArgsConstructor
@Slf4j
public class ReportGraphDatabaseApiController {

    private final ReportGraphDatabaseService graphDatabaseService;

    /**
     * Create graph database
     */
    @PostMapping
    public ResponseEntity<ReportGraphDatabase> createGraphDatabase(@RequestBody ReportGraphDatabase graphDatabase) {
        log.info("POST /api/graph-database - Creating graph database: {}", graphDatabase.getDatabaseName());

        try {
            ReportGraphDatabase created = graphDatabaseService.createGraphDatabase(graphDatabase);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating graph database", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get graph database
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportGraphDatabase> getGraphDatabase(@PathVariable Long id) {
        log.info("GET /api/graph-database/{}", id);

        try {
            return graphDatabaseService.getGraphDatabase(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching graph database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy graph database
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployGraphDatabase(@PathVariable Long id) {
        log.info("POST /api/graph-database/{}/deploy", id);

        try {
            graphDatabaseService.deployGraphDatabase(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Graph database deployed");
            response.put("graphDatabaseId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying graph database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add vertex
     */
    @PostMapping("/{id}/vertex")
    public ResponseEntity<ReportGraphDatabase.GraphVertex> addVertex(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/vertex", id);

        try {
            String vertexLabel = (String) request.get("vertexLabel");
            String vertexType = (String) request.get("vertexType");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");

            ReportGraphDatabase.GraphVertex vertex = graphDatabaseService.addVertex(
                    id, vertexLabel, vertexType, properties
            );

            return ResponseEntity.ok(vertex);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding vertex: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add edge
     */
    @PostMapping("/{id}/edge")
    public ResponseEntity<ReportGraphDatabase.GraphEdge> addEdge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/edge", id);

        try {
            String edgeLabel = (String) request.get("edgeLabel");
            String edgeType = (String) request.get("edgeType");
            String sourceVertexId = (String) request.get("sourceVertexId");
            String targetVertexId = (String) request.get("targetVertexId");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");
            Double weight = request.get("weight") != null ?
                    ((Number) request.get("weight")).doubleValue() : 1.0;
            Boolean directed = request.get("directed") != null ?
                    (Boolean) request.get("directed") : true;

            ReportGraphDatabase.GraphEdge edge = graphDatabaseService.addEdge(
                    id, edgeLabel, edgeType, sourceVertexId, targetVertexId, properties, weight, directed
            );

            return ResponseEntity.ok(edge);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding edge: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create schema
     */
    @PostMapping("/{id}/schema")
    public ResponseEntity<ReportGraphDatabase.GraphSchema> createSchema(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/schema", id);

        try {
            String schemaName = (String) request.get("schemaName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> vertexSchemasData = (List<Map<String, Object>>) request.get("vertexSchemas");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edgeSchemasData = (List<Map<String, Object>>) request.get("edgeSchemas");

            List<ReportGraphDatabase.VertexSchema> vertexSchemas = null;
            if (vertexSchemasData != null) {
                vertexSchemas = vertexSchemasData.stream()
                        .map(this::mapToVertexSchema)
                        .toList();
            }

            List<ReportGraphDatabase.EdgeSchema> edgeSchemas = null;
            if (edgeSchemasData != null) {
                edgeSchemas = edgeSchemasData.stream()
                        .map(this::mapToEdgeSchema)
                        .toList();
            }

            ReportGraphDatabase.GraphSchema schema = graphDatabaseService.createSchema(
                    id, schemaName, description, vertexSchemas, edgeSchemas
            );

            return ResponseEntity.ok(schema);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating schema: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute query
     */
    @PostMapping("/{id}/query")
    public ResponseEntity<ReportGraphDatabase.GraphQuery> executeQuery(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/query", id);

        try {
            String queryName = (String) request.get("queryName");
            String queryLanguage = (String) request.get("queryLanguage");
            String queryString = (String) request.get("queryString");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String executedBy = (String) request.get("executedBy");

            ReportGraphDatabase.GraphQuery query = graphDatabaseService.executeQuery(
                    id, queryName, queryLanguage, queryString, parameters, executedBy
            );

            return ResponseEntity.ok(query);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing query: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute traversal
     */
    @PostMapping("/{id}/traversal")
    public ResponseEntity<ReportGraphDatabase.GraphTraversal> executeTraversal(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/traversal", id);

        try {
            String traversalName = (String) request.get("traversalName");
            String traversalTypeStr = (String) request.get("traversalType");
            String startVertexId = (String) request.get("startVertexId");
            String endVertexId = (String) request.get("endVertexId");
            Integer maxDepth = request.get("maxDepth") != null ?
                    ((Number) request.get("maxDepth")).intValue() : 10;

            ReportGraphDatabase.TraversalType traversalType =
                    ReportGraphDatabase.TraversalType.valueOf(traversalTypeStr);

            ReportGraphDatabase.GraphTraversal traversal = graphDatabaseService.executeTraversal(
                    id, traversalName, traversalType, startVertexId, endVertexId, maxDepth
            );

            return ResponseEntity.ok(traversal);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing traversal: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Find shortest path
     */
    @PostMapping("/{id}/shortest-path")
    public ResponseEntity<ReportGraphDatabase.GraphPath> findShortestPath(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/graph-database/{}/shortest-path", id);

        try {
            String startVertexId = request.get("startVertexId");
            String endVertexId = request.get("endVertexId");

            ReportGraphDatabase.GraphPath path = graphDatabaseService.findShortestPath(
                    id, startVertexId, endVertexId
            );

            return ResponseEntity.ok(path);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error finding shortest path: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create pattern
     */
    @PostMapping("/{id}/pattern")
    public ResponseEntity<ReportGraphDatabase.GraphPattern> createPattern(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/pattern", id);

        try {
            String patternName = (String) request.get("patternName");
            String description = (String) request.get("description");
            String patternQuery = (String) request.get("patternQuery");
            @SuppressWarnings("unchecked")
            List<String> vertexLabels = (List<String>) request.get("vertexLabels");
            @SuppressWarnings("unchecked")
            List<String> edgeLabels = (List<String>) request.get("edgeLabels");

            ReportGraphDatabase.GraphPattern pattern = graphDatabaseService.createPattern(
                    id, patternName, description, patternQuery, vertexLabels, edgeLabels
            );

            return ResponseEntity.ok(pattern);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating pattern: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create index
     */
    @PostMapping("/{id}/index")
    public ResponseEntity<ReportGraphDatabase.GraphIndex> createIndex(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/index", id);

        try {
            String indexName = (String) request.get("indexName");
            String indexType = (String) request.get("indexType");
            String targetType = (String) request.get("targetType");
            @SuppressWarnings("unchecked")
            List<String> indexedLabels = (List<String>) request.get("indexedLabels");
            @SuppressWarnings("unchecked")
            List<String> indexedProperties = (List<String>) request.get("indexedProperties");
            Boolean unique = request.get("unique") != null ?
                    (Boolean) request.get("unique") : false;

            ReportGraphDatabase.GraphIndex index = graphDatabaseService.createIndex(
                    id, indexName, indexType, targetType, indexedLabels, indexedProperties, unique
            );

            return ResponseEntity.ok(index);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating index: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create knowledge graph
     */
    @PostMapping("/{id}/knowledge-graph")
    public ResponseEntity<ReportGraphDatabase.KnowledgeGraph> createKnowledgeGraph(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/graph-database/{}/knowledge-graph", id);

        try {
            String knowledgeGraphName = request.get("knowledgeGraphName");
            String description = request.get("description");
            String domain = request.get("domain");

            ReportGraphDatabase.KnowledgeGraph knowledgeGraph = graphDatabaseService.createKnowledgeGraph(
                    id, knowledgeGraphName, description, domain
            );

            return ResponseEntity.ok(knowledgeGraph);

        } catch (IllegalArgumentException e) {
            log.error("Graph database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating knowledge graph: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add entity to knowledge graph
     */
    @PostMapping("/{id}/knowledge-graph/{kgId}/entity")
    public ResponseEntity<ReportGraphDatabase.Entity> addEntity(
            @PathVariable Long id,
            @PathVariable String kgId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/knowledge-graph/{}/entity", id, kgId);

        try {
            String entityType = (String) request.get("entityType");
            String entityName = (String) request.get("entityName");
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) request.get("attributes");

            ReportGraphDatabase.Entity entity = graphDatabaseService.addEntity(
                    id, kgId, entityType, entityName, attributes
            );

            return ResponseEntity.ok(entity);

        } catch (IllegalArgumentException e) {
            log.error("Graph database or knowledge graph not found: {} / {}", id, kgId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding entity: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add relationship to knowledge graph
     */
    @PostMapping("/{id}/knowledge-graph/{kgId}/relationship")
    public ResponseEntity<ReportGraphDatabase.Relationship> addRelationship(
            @PathVariable Long id,
            @PathVariable String kgId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/graph-database/{}/knowledge-graph/{}/relationship", id, kgId);

        try {
            String relationshipType = (String) request.get("relationshipType");
            String sourceEntityId = (String) request.get("sourceEntityId");
            String targetEntityId = (String) request.get("targetEntityId");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");

            ReportGraphDatabase.Relationship relationship = graphDatabaseService.addRelationship(
                    id, kgId, relationshipType, sourceEntityId, targetEntityId, properties
            );

            return ResponseEntity.ok(relationship);

        } catch (IllegalArgumentException e) {
            log.error("Graph database or knowledge graph not found: {} / {}", id, kgId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding relationship: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete graph database
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGraphDatabase(@PathVariable Long id) {
        log.info("DELETE /api/graph-database/{}", id);

        try {
            graphDatabaseService.deleteGraphDatabase(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Graph database deleted");
            response.put("graphDatabaseId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting graph database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/graph-database/stats");

        try {
            Map<String, Object> stats = graphDatabaseService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods

    private ReportGraphDatabase.VertexSchema mapToVertexSchema(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        Map<String, String> propertyTypes = (Map<String, String>) map.get("propertyTypes");
        @SuppressWarnings("unchecked")
        List<String> requiredProperties = (List<String>) map.get("requiredProperties");
        @SuppressWarnings("unchecked")
        List<String> indexedProperties = (List<String>) map.get("indexedProperties");

        return ReportGraphDatabase.VertexSchema.builder()
                .label((String) map.get("label"))
                .propertyTypes(propertyTypes != null ? propertyTypes : new HashMap<>())
                .requiredProperties(requiredProperties != null ? requiredProperties : new java.util.ArrayList<>())
                .indexedProperties(indexedProperties != null ? indexedProperties : new java.util.ArrayList<>())
                .primaryKey((String) map.get("primaryKey"))
                .build();
    }

    private ReportGraphDatabase.EdgeSchema mapToEdgeSchema(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        Map<String, String> propertyTypes = (Map<String, String>) map.get("propertyTypes");
        @SuppressWarnings("unchecked")
        List<String> requiredProperties = (List<String>) map.get("requiredProperties");

        return ReportGraphDatabase.EdgeSchema.builder()
                .label((String) map.get("label"))
                .sourceLabel((String) map.get("sourceLabel"))
                .targetLabel((String) map.get("targetLabel"))
                .propertyTypes(propertyTypes != null ? propertyTypes : new HashMap<>())
                .requiredProperties(requiredProperties != null ? requiredProperties : new java.util.ArrayList<>())
                .directed(map.get("directed") != null ? (Boolean) map.get("directed") : true)
                .multiplicity((String) map.get("multiplicity"))
                .build();
    }
}
