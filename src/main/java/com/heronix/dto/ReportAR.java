package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Augmented Reality DTO
 *
 * Manages AR/VR visualization and immersive report experiences.
 *
 * Features:
 * - Augmented Reality overlays
 * - Virtual Reality environments
 * - 3D data visualization
 * - Interactive dashboards
 * - Spatial anchors
 * - Gesture controls
 * - Voice commands
 * - Multi-user collaboration
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 104 - Report Augmented Reality & Visualization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAR {

    // Environment Information
    private Long environmentId;
    private String environmentName;
    private String description;
    private EnvironmentStatus status;
    private Boolean isActive;

    // AR/VR Platform
    private ARPlatform platform;
    private String platformVersion;
    private RenderingEngine renderingEngine;
    private Boolean multiUserEnabled;
    private Integer maxConcurrentUsers;

    // AR Scenes
    private List<ARScene> scenes;
    private Map<String, ARScene> sceneRegistry;
    private Integer totalScenes;
    private Integer activeScenes;

    // 3D Visualizations
    private List<Visualization3D> visualizations;
    private Map<String, Visualization3D> visualizationRegistry;
    private Integer totalVisualizations;

    // AR Anchors
    private List<SpatialAnchor> anchors;
    private Map<String, SpatialAnchor> anchorRegistry;
    private Integer totalAnchors;

    // Interactive Elements
    private List<InteractiveElement> interactiveElements;
    private Map<String, InteractiveElement> elementRegistry;
    private Integer totalElements;

    // User Sessions
    private List<ARSession> sessions;
    private Map<String, ARSession> sessionRegistry;
    private Long totalSessions;
    private Integer activeSessions;

    // Gestures
    private List<GestureMapping> gestures;
    private Map<String, GestureMapping> gestureRegistry;

    // Voice Commands
    private List<VoiceCommand> voiceCommands;
    private Map<String, VoiceCommand> voiceCommandRegistry;

    // Annotations
    private List<ARAnnotation> annotations;
    private Map<String, ARAnnotation> annotationRegistry;
    private Long totalAnnotations;

    // Metrics
    private ARMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<AREvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime lastSessionAt;

    /**
     * Environment Status
     */
    public enum EnvironmentStatus {
        INITIALIZING,
        READY,
        RUNNING,
        PAUSED,
        ERROR,
        MAINTENANCE
    }

    /**
     * AR Platform
     */
    public enum ARPlatform {
        UNITY_AR_FOUNDATION,
        ARKIT,
        ARCORE,
        VUFORIA,
        HOLOLENS,
        MAGIC_LEAP,
        OCULUS_QUEST,
        WEBXR,
        CUSTOM
    }

    /**
     * Rendering Engine
     */
    public enum RenderingEngine {
        UNITY,
        UNREAL_ENGINE,
        THREE_JS,
        BABYLON_JS,
        A_FRAME,
        CUSTOM
    }

    /**
     * Scene Type
     */
    public enum SceneType {
        AR_OVERLAY,
        VR_IMMERSIVE,
        MIXED_REALITY,
        HOLOGRAPHIC,
        PORTAL
    }

    /**
     * Visualization Type
     */
    public enum VisualizationType {
        BAR_CHART_3D,
        PIE_CHART_3D,
        LINE_GRAPH_3D,
        SCATTER_PLOT_3D,
        HEATMAP_3D,
        NETWORK_GRAPH,
        TREE_MAP,
        GLOBE,
        CUSTOM
    }

    /**
     * Interaction Type
     */
    public enum InteractionType {
        TAP,
        SWIPE,
        PINCH,
        ROTATE,
        DRAG,
        VOICE,
        GAZE,
        CONTROLLER
    }

    /**
     * Session Status
     */
    public enum SessionStatus {
        CONNECTING,
        ACTIVE,
        PAUSED,
        DISCONNECTED,
        ERROR
    }

    /**
     * AR Scene
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ARScene {
        private String sceneId;
        private String sceneName;
        private SceneType sceneType;
        private String reportId;
        private String reportName;
        private Boolean active;
        private List<String> visualizationIds;
        private List<String> anchorIds;
        private Vector3 cameraPosition;
        private Vector3 cameraRotation;
        private Double scale;
        private String skyboxTexture;
        private String environmentLighting;
        private Integer renderQuality;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessedAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * 3D Visualization
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Visualization3D {
        private String visualizationId;
        private String visualizationName;
        private VisualizationType visualizationType;
        private String sceneId;
        private Vector3 position;
        private Vector3 rotation;
        private Vector3 scale;
        private Map<String, Object> dataSource;
        private List<DataPoint3D> dataPoints;
        private String colorScheme;
        private Boolean interactive;
        private Boolean animated;
        private Integer animationDuration;
        private String tooltip;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Data Point 3D
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint3D {
        private String label;
        private Double value;
        private Vector3 position;
        private String color;
        private Double size;
        private Map<String, Object> properties;
    }

    /**
     * Vector3
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vector3 {
        private Double x;
        private Double y;
        private Double z;
    }

    /**
     * Spatial Anchor
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpatialAnchor {
        private String anchorId;
        private String anchorName;
        private String sceneId;
        private Vector3 position;
        private Vector3 rotation;
        private Boolean persistent;
        private String cloudAnchorId;
        private List<String> attachedElements;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Interactive Element
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveElement {
        private String elementId;
        private String elementName;
        private String elementType;
        private String sceneId;
        private Vector3 position;
        private Vector3 scale;
        private List<InteractionType> supportedInteractions;
        private String actionOnInteract;
        private Boolean enabled;
        private String visualState;
        private Long totalInteractions;
        private LocalDateTime lastInteractedAt;
        private Map<String, Object> metadata;
    }

    /**
     * AR Session
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ARSession {
        private String sessionId;
        private String userId;
        private String userName;
        private SessionStatus status;
        private String sceneId;
        private String deviceType;
        private String deviceModel;
        private Vector3 headPosition;
        private Vector3 headRotation;
        private Integer frameRate;
        private Boolean trackingQuality;
        private LocalDateTime connectedAt;
        private LocalDateTime disconnectedAt;
        private Long sessionDuration;
        private Long totalInteractions;
        private Map<String, Object> metadata;
    }

    /**
     * Gesture Mapping
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GestureMapping {
        private String gestureId;
        private String gestureName;
        private InteractionType interactionType;
        private String actionCommand;
        private String description;
        private Boolean enabled;
        private Long usageCount;
        private Map<String, Object> parameters;
    }

    /**
     * Voice Command
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceCommand {
        private String commandId;
        private String commandPhrase;
        private List<String> alternativePhrases;
        private String actionCommand;
        private String description;
        private Boolean enabled;
        private Long usageCount;
        private Double confidenceThreshold;
        private Map<String, Object> parameters;
    }

    /**
     * AR Annotation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ARAnnotation {
        private String annotationId;
        private String sceneId;
        private String visualizationId;
        private String text;
        private Vector3 position;
        private String color;
        private Double fontSize;
        private String fontStyle;
        private Boolean billboard;
        private String createdBy;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * AR Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ARMetrics {
        private Integer totalScenes;
        private Integer activeScenes;
        private Integer totalVisualizations;
        private Long totalSessions;
        private Integer activeSessions;
        private Integer concurrentUsers;
        private Double averageSessionDuration;
        private Long totalInteractions;
        private Double averageFrameRate;
        private Integer totalAnchors;
        private Long totalAnnotations;
        private Double userEngagementScore;
        private LocalDateTime measuredAt;
    }

    /**
     * AR Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AREvent {
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
     * Start environment
     */
    public void startEnvironment() {
        this.status = EnvironmentStatus.INITIALIZING;
        this.isActive = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("ENVIRONMENT_STARTED", "AR environment started", "ENVIRONMENT",
                environmentId != null ? environmentId.toString() : null);

        this.status = EnvironmentStatus.READY;
    }

    /**
     * Stop environment
     */
    public void stopEnvironment() {
        this.status = EnvironmentStatus.MAINTENANCE;
        this.isActive = false;
        this.stoppedAt = LocalDateTime.now();

        recordEvent("ENVIRONMENT_STOPPED", "AR environment stopped", "ENVIRONMENT",
                environmentId != null ? environmentId.toString() : null);
    }

    /**
     * Create scene
     */
    public void createScene(ARScene scene) {
        if (scenes == null) {
            scenes = new java.util.ArrayList<>();
        }
        scenes.add(scene);

        if (sceneRegistry == null) {
            sceneRegistry = new java.util.HashMap<>();
        }
        sceneRegistry.put(scene.getSceneId(), scene);

        totalScenes = (totalScenes != null ? totalScenes : 0) + 1;
        if (scene.getActive()) {
            activeScenes = (activeScenes != null ? activeScenes : 0) + 1;
        }

        recordEvent("SCENE_CREATED", "AR scene created: " + scene.getSceneName(),
                "SCENE", scene.getSceneId());
    }

    /**
     * Add visualization
     */
    public void addVisualization(Visualization3D visualization) {
        if (visualizations == null) {
            visualizations = new java.util.ArrayList<>();
        }
        visualizations.add(visualization);

        if (visualizationRegistry == null) {
            visualizationRegistry = new java.util.HashMap<>();
        }
        visualizationRegistry.put(visualization.getVisualizationId(), visualization);

        totalVisualizations = (totalVisualizations != null ? totalVisualizations : 0) + 1;

        recordEvent("VISUALIZATION_ADDED", "3D visualization added: " + visualization.getVisualizationName(),
                "VISUALIZATION", visualization.getVisualizationId());
    }

    /**
     * Create anchor
     */
    public void createAnchor(SpatialAnchor anchor) {
        if (anchors == null) {
            anchors = new java.util.ArrayList<>();
        }
        anchors.add(anchor);

        if (anchorRegistry == null) {
            anchorRegistry = new java.util.HashMap<>();
        }
        anchorRegistry.put(anchor.getAnchorId(), anchor);

        totalAnchors = (totalAnchors != null ? totalAnchors : 0) + 1;

        recordEvent("ANCHOR_CREATED", "Spatial anchor created: " + anchor.getAnchorName(),
                "ANCHOR", anchor.getAnchorId());
    }

    /**
     * Add interactive element
     */
    public void addInteractiveElement(InteractiveElement element) {
        if (interactiveElements == null) {
            interactiveElements = new java.util.ArrayList<>();
        }
        interactiveElements.add(element);

        if (elementRegistry == null) {
            elementRegistry = new java.util.HashMap<>();
        }
        elementRegistry.put(element.getElementId(), element);

        totalElements = (totalElements != null ? totalElements : 0) + 1;
    }

    /**
     * Start session
     */
    public void startSession(ARSession session) {
        if (sessions == null) {
            sessions = new java.util.ArrayList<>();
        }
        sessions.add(session);

        if (sessionRegistry == null) {
            sessionRegistry = new java.util.HashMap<>();
        }
        sessionRegistry.put(session.getSessionId(), session);

        totalSessions = (totalSessions != null ? totalSessions : 0L) + 1;
        activeSessions = (activeSessions != null ? activeSessions : 0) + 1;
        lastSessionAt = LocalDateTime.now();

        recordEvent("SESSION_STARTED", "AR session started for user: " + session.getUserName(),
                "SESSION", session.getSessionId());
    }

    /**
     * End session
     */
    public void endSession(String sessionId) {
        ARSession session = sessionRegistry != null ? sessionRegistry.get(sessionId) : null;
        if (session != null) {
            session.setStatus(SessionStatus.DISCONNECTED);
            session.setDisconnectedAt(LocalDateTime.now());

            if (session.getConnectedAt() != null) {
                session.setSessionDuration(
                    java.time.Duration.between(session.getConnectedAt(), session.getDisconnectedAt()).toSeconds()
                );
            }

            if (activeSessions != null && activeSessions > 0) {
                activeSessions--;
            }
        }
    }

    /**
     * Add annotation
     */
    public void addAnnotation(ARAnnotation annotation) {
        if (annotations == null) {
            annotations = new java.util.ArrayList<>();
        }
        annotations.add(annotation);

        if (annotationRegistry == null) {
            annotationRegistry = new java.util.HashMap<>();
        }
        annotationRegistry.put(annotation.getAnnotationId(), annotation);

        totalAnnotations = (totalAnnotations != null ? totalAnnotations : 0L) + 1;
    }

    /**
     * Register gesture
     */
    public void registerGesture(GestureMapping gesture) {
        if (gestures == null) {
            gestures = new java.util.ArrayList<>();
        }
        gestures.add(gesture);

        if (gestureRegistry == null) {
            gestureRegistry = new java.util.HashMap<>();
        }
        gestureRegistry.put(gesture.getGestureId(), gesture);
    }

    /**
     * Register voice command
     */
    public void registerVoiceCommand(VoiceCommand command) {
        if (voiceCommands == null) {
            voiceCommands = new java.util.ArrayList<>();
        }
        voiceCommands.add(command);

        if (voiceCommandRegistry == null) {
            voiceCommandRegistry = new java.util.HashMap<>();
        }
        voiceCommandRegistry.put(command.getCommandId(), command);
    }

    /**
     * Get scene by ID
     */
    public ARScene getScene(String sceneId) {
        return sceneRegistry != null ? sceneRegistry.get(sceneId) : null;
    }

    /**
     * Get visualization by ID
     */
    public Visualization3D getVisualization(String visualizationId) {
        return visualizationRegistry != null ? visualizationRegistry.get(visualizationId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        AREvent event = AREvent.builder()
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
     * Check if environment is healthy
     */
    public boolean isHealthy() {
        return status == EnvironmentStatus.READY || status == EnvironmentStatus.RUNNING;
    }

    /**
     * Get active scenes
     */
    public List<ARScene> getActiveScenes() {
        if (scenes == null) {
            return new java.util.ArrayList<>();
        }
        return scenes.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active sessions
     */
    public List<ARSession> getActiveSessions() {
        if (sessions == null) {
            return new java.util.ArrayList<>();
        }
        return sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
    }
}
