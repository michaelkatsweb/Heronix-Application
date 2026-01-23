package com.heronix.controller;

import com.heronix.dto.ReportAR;
import com.heronix.service.ReportARService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report AR API Controller
 *
 * REST API endpoints for augmented reality visualization.
 *
 * Endpoints:
 * - POST /api/ar - Create AR environment
 * - GET /api/ar/{id} - Get AR environment
 * - POST /api/ar/{id}/start - Start environment
 * - POST /api/ar/{id}/stop - Stop environment
 * - POST /api/ar/{id}/scene - Create AR scene
 * - POST /api/ar/{id}/visualization - Create 3D visualization
 * - POST /api/ar/{id}/anchor - Create spatial anchor
 * - POST /api/ar/{id}/element - Add interactive element
 * - POST /api/ar/{id}/session - Start AR session
 * - POST /api/ar/{id}/annotation - Add annotation
 * - POST /api/ar/{id}/gesture - Register gesture
 * - POST /api/ar/{id}/voice - Register voice command
 * - PUT /api/ar/{id}/metrics - Update metrics
 * - DELETE /api/ar/{id} - Delete environment
 * - GET /api/ar/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 104 - Report Augmented Reality & Visualization
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/ar")
@RequiredArgsConstructor
@Slf4j
public class ReportARApiController {

    private final ReportARService arService;

    /**
     * Create AR environment
     */
    @PostMapping
    public ResponseEntity<ReportAR> createEnvironment(@RequestBody ReportAR environment) {
        log.info("POST /api/ar - Creating AR environment: {}", environment.getEnvironmentName());

        try {
            ReportAR created = arService.createEnvironment(environment);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating AR environment", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get AR environment
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportAR> getEnvironment(@PathVariable Long id) {
        log.info("GET /api/ar/{}", id);

        try {
            return arService.getEnvironment(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching AR environment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start environment
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startEnvironment(@PathVariable Long id) {
        log.info("POST /api/ar/{}/start", id);

        try {
            arService.startEnvironment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AR environment started");
            response.put("environmentId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting AR environment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop environment
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopEnvironment(@PathVariable Long id) {
        log.info("POST /api/ar/{}/stop", id);

        try {
            arService.stopEnvironment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AR environment stopped");
            response.put("environmentId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping AR environment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create AR scene
     */
    @PostMapping("/{id}/scene")
    public ResponseEntity<ReportAR.ARScene> createScene(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ar/{}/scene", id);

        try {
            String sceneName = request.get("sceneName");
            String sceneTypeStr = request.get("sceneType");
            String reportId = request.get("reportId");
            String reportName = request.get("reportName");

            ReportAR.SceneType sceneType = ReportAR.SceneType.valueOf(sceneTypeStr);

            ReportAR.ARScene scene = arService.createScene(
                    id, sceneName, sceneType, reportId, reportName
            );

            return ResponseEntity.ok(scene);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating scene in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create 3D visualization
     */
    @PostMapping("/{id}/visualization")
    public ResponseEntity<ReportAR.Visualization3D> create3DVisualization(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ar/{}/visualization", id);

        try {
            String sceneId = (String) request.get("sceneId");
            String visualizationName = (String) request.get("visualizationName");
            String visualizationTypeStr = (String) request.get("visualizationType");
            @SuppressWarnings("unchecked")
            Map<String, Object> dataSource = (Map<String, Object>) request.get("dataSource");

            ReportAR.VisualizationType visualizationType =
                    ReportAR.VisualizationType.valueOf(visualizationTypeStr);

            ReportAR.Visualization3D visualization = arService.create3DVisualization(
                    id, sceneId, visualizationName, visualizationType, dataSource
            );

            return ResponseEntity.ok(visualization);

        } catch (IllegalArgumentException e) {
            log.error("AR environment or scene not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating 3D visualization in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create spatial anchor
     */
    @PostMapping("/{id}/anchor")
    public ResponseEntity<ReportAR.SpatialAnchor> createAnchor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ar/{}/anchor", id);

        try {
            String sceneId = (String) request.get("sceneId");
            String anchorName = (String) request.get("anchorName");
            Boolean persistent = request.get("persistent") != null ?
                    (Boolean) request.get("persistent") : false;

            @SuppressWarnings("unchecked")
            Map<String, Double> positionMap = (Map<String, Double>) request.get("position");
            ReportAR.Vector3 position = ReportAR.Vector3.builder()
                    .x(positionMap.get("x"))
                    .y(positionMap.get("y"))
                    .z(positionMap.get("z"))
                    .build();

            ReportAR.SpatialAnchor anchor = arService.createAnchor(
                    id, sceneId, anchorName, position, persistent
            );

            return ResponseEntity.ok(anchor);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating spatial anchor in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add interactive element
     */
    @PostMapping("/{id}/element")
    public ResponseEntity<ReportAR.InteractiveElement> addInteractiveElement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ar/{}/element", id);

        try {
            String sceneId = (String) request.get("sceneId");
            String elementName = (String) request.get("elementName");
            String elementType = (String) request.get("elementType");

            @SuppressWarnings("unchecked")
            List<String> interactionStrs = (List<String>) request.get("interactions");
            List<ReportAR.InteractionType> interactions = interactionStrs != null ?
                    interactionStrs.stream()
                            .map(ReportAR.InteractionType::valueOf)
                            .collect(java.util.stream.Collectors.toList()) : null;

            ReportAR.InteractiveElement element = arService.addInteractiveElement(
                    id, sceneId, elementName, elementType, interactions
            );

            return ResponseEntity.ok(element);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding interactive element in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start AR session
     */
    @PostMapping("/{id}/session")
    public ResponseEntity<ReportAR.ARSession> startSession(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ar/{}/session", id);

        try {
            String userId = request.get("userId");
            String userName = request.get("userName");
            String sceneId = request.get("sceneId");
            String deviceType = request.get("deviceType");

            ReportAR.ARSession session = arService.startSession(
                    id, userId, userName, sceneId, deviceType
            );

            return ResponseEntity.ok(session);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting session in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add annotation
     */
    @PostMapping("/{id}/annotation")
    public ResponseEntity<ReportAR.ARAnnotation> addAnnotation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ar/{}/annotation", id);

        try {
            String sceneId = (String) request.get("sceneId");
            String visualizationId = (String) request.get("visualizationId");
            String text = (String) request.get("text");
            String createdBy = (String) request.get("createdBy");

            @SuppressWarnings("unchecked")
            Map<String, Double> positionMap = (Map<String, Double>) request.get("position");
            ReportAR.Vector3 position = ReportAR.Vector3.builder()
                    .x(positionMap.get("x"))
                    .y(positionMap.get("y"))
                    .z(positionMap.get("z"))
                    .build();

            ReportAR.ARAnnotation annotation = arService.addAnnotation(
                    id, sceneId, visualizationId, text, position, createdBy
            );

            return ResponseEntity.ok(annotation);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding annotation in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register gesture
     */
    @PostMapping("/{id}/gesture")
    public ResponseEntity<ReportAR.GestureMapping> registerGesture(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ar/{}/gesture", id);

        try {
            String gestureName = request.get("gestureName");
            String interactionTypeStr = request.get("interactionType");
            String actionCommand = request.get("actionCommand");

            ReportAR.InteractionType interactionType =
                    ReportAR.InteractionType.valueOf(interactionTypeStr);

            ReportAR.GestureMapping gesture = arService.registerGesture(
                    id, gestureName, interactionType, actionCommand
            );

            return ResponseEntity.ok(gesture);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering gesture in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register voice command
     */
    @PostMapping("/{id}/voice")
    public ResponseEntity<ReportAR.VoiceCommand> registerVoiceCommand(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ar/{}/voice", id);

        try {
            String commandPhrase = (String) request.get("commandPhrase");
            String actionCommand = (String) request.get("actionCommand");
            @SuppressWarnings("unchecked")
            List<String> alternatives = (List<String>) request.get("alternatives");

            ReportAR.VoiceCommand voiceCommand = arService.registerVoiceCommand(
                    id, commandPhrase, actionCommand, alternatives
            );

            return ResponseEntity.ok(voiceCommand);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering voice command in AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/ar/{}/metrics", id);

        try {
            arService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("environmentId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AR environment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for AR environment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete environment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEnvironment(@PathVariable Long id) {
        log.info("DELETE /api/ar/{}", id);

        try {
            arService.deleteEnvironment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AR environment deleted");
            response.put("environmentId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting AR environment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/ar/stats");

        try {
            Map<String, Object> stats = arService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching AR statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
