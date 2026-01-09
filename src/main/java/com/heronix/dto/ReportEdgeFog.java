package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Edge & Fog Computing DTO
 *
 * Manages edge computing infrastructure, fog nodes, distributed processing, and IoT device management
 * for educational smart campus, classroom IoT sensors, and decentralized learning systems.
 *
 * Educational Use Cases:
 * - Smart classroom IoT sensor management and data processing
 * - Campus-wide distributed learning content delivery
 * - Real-time student attendance tracking via edge devices
 * - Local processing of educational video content
 * - Edge-based student performance analytics
 * - IoT-enabled lab equipment monitoring and control
 * - Decentralized exam proctoring systems
 * - Smart campus energy and resource management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 117 - Report Edge & Fog Computing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEdgeFog {

    // Basic Information
    private Long edgeId;
    private String edgeName;
    private String description;
    private EdgeStatus status;
    private String organizationId;
    private String topology;

    // Configuration
    private Integer nodeCount;
    private Integer maxNodes;
    private String loadBalancingStrategy;
    private Boolean autoScaling;
    private Integer replicationFactor;

    // State
    private Boolean isActive;
    private Boolean isDistributed;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastSyncAt;
    private String createdBy;

    // Edge Nodes
    private List<EdgeNode> edgeNodes;
    private Map<String, EdgeNode> nodeRegistry;

    // Fog Nodes
    private List<FogNode> fogNodes;
    private Map<String, FogNode> fogRegistry;

    // IoT Devices
    private List<IoTDevice> iotDevices;
    private Map<String, IoTDevice> deviceRegistry;

    // Processing Tasks
    private List<EdgeTask> tasks;
    private Map<String, EdgeTask> taskRegistry;

    // Data Streams
    private List<DataStream> dataStreams;
    private Map<String, DataStream> streamRegistry;

    // Deployments
    private List<EdgeDeployment> deployments;
    private Map<String, EdgeDeployment> deploymentRegistry;

    // Network Links
    private List<NetworkLink> networkLinks;
    private Map<String, NetworkLink> linkRegistry;

    // Services
    private List<EdgeService> services;
    private Map<String, EdgeService> serviceRegistry;

    // Sync Operations
    private List<SyncOperation> syncOperations;
    private Map<String, SyncOperation> syncRegistry;

    // Metrics
    private Long totalNodes;
    private Long activeNodes;
    private Long totalDevices;
    private Long activeDevices;
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;
    private Long totalDataStreams;
    private Long activeStreams;
    private Long totalDeployments;
    private Long successfulDeployments;
    private Double averageLatency; // milliseconds
    private Double networkUtilization; // percentage
    private Long totalDataProcessed; // bytes
    private Long totalSyncOperations;

    // Events
    private List<EdgeEvent> events;

    /**
     * Edge status enumeration
     */
    public enum EdgeStatus {
        INITIALIZING,
        DEPLOYING,
        ACTIVE,
        SCALING,
        SYNCING,
        DEGRADED,
        MAINTENANCE,
        OFFLINE
    }

    /**
     * Node status enumeration
     */
    public enum NodeStatus {
        PROVISIONING,
        ONLINE,
        BUSY,
        IDLE,
        OVERLOADED,
        OFFLINE,
        FAILED
    }

    /**
     * Task status enumeration
     */
    public enum TaskStatus {
        PENDING,
        SCHEDULED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Device type enumeration
     */
    public enum DeviceType {
        SENSOR,
        CAMERA,
        ACTUATOR,
        GATEWAY,
        BEACON,
        RFID_READER,
        SMART_BOARD,
        ENVIRONMENTAL_MONITOR,
        ACCESS_CONTROL
    }

    /**
     * Stream type enumeration
     */
    public enum StreamType {
        SENSOR_DATA,
        VIDEO,
        AUDIO,
        TELEMETRY,
        LOGS,
        METRICS,
        EVENTS,
        ANALYTICS
    }

    /**
     * Deployment status enumeration
     */
    public enum DeploymentStatus {
        PREPARING,
        DEPLOYING,
        ACTIVE,
        UPDATING,
        ROLLING_BACK,
        FAILED,
        TERMINATED
    }

    /**
     * Edge node data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeNode {
        private String nodeId;
        private String nodeName;
        private NodeStatus status;
        private String location;
        private String ipAddress;
        private Integer port;
        private Integer cpuCores;
        private Long memoryMb;
        private Long storageMb;
        private Double cpuUsage;
        private Double memoryUsage;
        private Double storageUsage;
        private Integer taskCount;
        private Integer deviceCount;
        private LocalDateTime registeredAt;
        private LocalDateTime lastHeartbeat;
        private Map<String, Object> capabilities;
        private List<String> connectedDevices;
    }

    /**
     * Fog node data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FogNode {
        private String fogId;
        private String fogName;
        private NodeStatus status;
        private String region;
        private List<String> edgeNodeIds;
        private Integer edgeNodeCount;
        private Long aggregatedMemoryMb;
        private Long aggregatedStorageMb;
        private Double loadBalanceScore;
        private LocalDateTime createdAt;
        private LocalDateTime lastSyncAt;
        private Map<String, Object> configuration;
    }

    /**
     * IoT device data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTDevice {
        private String deviceId;
        private String deviceName;
        private DeviceType deviceType;
        private String manufacturer;
        private String modelNumber;
        private String firmwareVersion;
        private String edgeNodeId;
        private String location;
        private Boolean isOnline;
        private Integer batteryLevel;
        private LocalDateTime lastSeen;
        private LocalDateTime registeredAt;
        private Map<String, Object> sensorData;
        private Map<String, Object> metadata;
    }

    /**
     * Edge task data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeTask {
        private String taskId;
        private String taskName;
        private TaskStatus status;
        private String taskType;
        private String assignedNodeId;
        private Integer priority;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private Long executionTime; // milliseconds
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }

    /**
     * Data stream data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStream {
        private String streamId;
        private String streamName;
        private StreamType streamType;
        private String sourceDeviceId;
        private String sourceNodeId;
        private String destinationNodeId;
        private Boolean isActive;
        private Long dataRate; // bytes per second
        private Long totalBytes;
        private Integer packetCount;
        private Double latency; // milliseconds
        private LocalDateTime startedAt;
        private LocalDateTime lastUpdateAt;
        private Map<String, Object> streamConfig;
    }

    /**
     * Edge deployment data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeDeployment {
        private String deploymentId;
        private String deploymentName;
        private DeploymentStatus status;
        private String serviceId;
        private List<String> targetNodeIds;
        private String version;
        private String containerImage;
        private Map<String, String> environment;
        private Integer replicas;
        private LocalDateTime deployedAt;
        private LocalDateTime lastUpdatedAt;
        private Boolean healthCheck;
        private Map<String, Object> deploymentConfig;
    }

    /**
     * Network link data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkLink {
        private String linkId;
        private String sourceNodeId;
        private String targetNodeId;
        private String linkType;
        private Double bandwidth; // Mbps
        private Double latency; // milliseconds
        private Double packetLoss; // percentage
        private Boolean isActive;
        private Long bytesTransferred;
        private LocalDateTime establishedAt;
        private LocalDateTime lastActiveAt;
    }

    /**
     * Edge service data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeService {
        private String serviceId;
        private String serviceName;
        private String serviceType;
        private String version;
        private List<String> deployedNodeIds;
        private Integer instanceCount;
        private Boolean isRunning;
        private Long requestCount;
        private Double averageResponseTime; // milliseconds
        private LocalDateTime deployedAt;
        private Map<String, Object> configuration;
    }

    /**
     * Sync operation data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncOperation {
        private String syncId;
        private String syncType;
        private String sourceNodeId;
        private String targetNodeId;
        private Long dataSizeByte;
        private Boolean successful;
        private Long duration; // milliseconds
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }

    /**
     * Edge event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeEvent {
        private String eventId;
        private String eventType;
        private String description;
        private String targetType;
        private String targetId;
        private LocalDateTime timestamp;
        private String triggeredBy;
        private Map<String, Object> eventData;
    }

    // Helper methods

    /**
     * Deploy edge infrastructure
     */
    public void deployInfrastructure() {
        this.status = EdgeStatus.ACTIVE;
        this.isActive = true;
        this.deployedAt = LocalDateTime.now();
        recordEvent("INFRASTRUCTURE_DEPLOYED", "Edge infrastructure deployed", "INFRASTRUCTURE",
                edgeId != null ? edgeId.toString() : null);
    }

    /**
     * Register edge node
     */
    public void registerEdgeNode(EdgeNode node) {
        if (edgeNodes == null) {
            edgeNodes = new ArrayList<>();
        }
        edgeNodes.add(node);

        if (nodeRegistry == null) {
            nodeRegistry = new HashMap<>();
        }
        nodeRegistry.put(node.getNodeId(), node);

        totalNodes = (totalNodes != null ? totalNodes : 0L) + 1;
        if (node.getStatus() == NodeStatus.ONLINE) {
            activeNodes = (activeNodes != null ? activeNodes : 0L) + 1;
        }

        recordEvent("NODE_REGISTERED", "Edge node registered", "NODE", node.getNodeId());
    }

    /**
     * Register IoT device
     */
    public void registerDevice(IoTDevice device) {
        if (iotDevices == null) {
            iotDevices = new ArrayList<>();
        }
        iotDevices.add(device);

        if (deviceRegistry == null) {
            deviceRegistry = new HashMap<>();
        }
        deviceRegistry.put(device.getDeviceId(), device);

        totalDevices = (totalDevices != null ? totalDevices : 0L) + 1;
        if (Boolean.TRUE.equals(device.getIsOnline())) {
            activeDevices = (activeDevices != null ? activeDevices : 0L) + 1;
        }

        recordEvent("DEVICE_REGISTERED", "IoT device registered", "DEVICE", device.getDeviceId());
    }

    /**
     * Schedule task
     */
    public void scheduleTask(EdgeTask task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);

        if (taskRegistry == null) {
            taskRegistry = new HashMap<>();
        }
        taskRegistry.put(task.getTaskId(), task);

        totalTasks = (totalTasks != null ? totalTasks : 0L) + 1;

        recordEvent("TASK_SCHEDULED", "Edge task scheduled", "TASK", task.getTaskId());
    }

    /**
     * Complete task
     */
    public void completeTask(String taskId, boolean success) {
        EdgeTask task = taskRegistry != null ? taskRegistry.get(taskId) : null;
        if (task != null) {
            task.setStatus(success ? TaskStatus.COMPLETED : TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());

            if (task.getStartedAt() != null) {
                task.setExecutionTime(
                    java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                completedTasks = (completedTasks != null ? completedTasks : 0L) + 1;
            } else {
                failedTasks = (failedTasks != null ? failedTasks : 0L) + 1;
            }
        }
    }

    /**
     * Start data stream
     */
    public void startDataStream(DataStream stream) {
        if (dataStreams == null) {
            dataStreams = new ArrayList<>();
        }
        dataStreams.add(stream);

        if (streamRegistry == null) {
            streamRegistry = new HashMap<>();
        }
        streamRegistry.put(stream.getStreamId(), stream);

        totalDataStreams = (totalDataStreams != null ? totalDataStreams : 0L) + 1;
        if (Boolean.TRUE.equals(stream.getIsActive())) {
            activeStreams = (activeStreams != null ? activeStreams : 0L) + 1;
        }

        recordEvent("STREAM_STARTED", "Data stream started", "STREAM", stream.getStreamId());
    }

    /**
     * Deploy service
     */
    public void deployService(EdgeDeployment deployment) {
        if (deployments == null) {
            deployments = new ArrayList<>();
        }
        deployments.add(deployment);

        if (deploymentRegistry == null) {
            deploymentRegistry = new HashMap<>();
        }
        deploymentRegistry.put(deployment.getDeploymentId(), deployment);

        totalDeployments = (totalDeployments != null ? totalDeployments : 0L) + 1;
        if (deployment.getStatus() == DeploymentStatus.ACTIVE) {
            successfulDeployments = (successfulDeployments != null ? successfulDeployments : 0L) + 1;
        }

        recordEvent("SERVICE_DEPLOYED", "Service deployed to edge", "DEPLOYMENT", deployment.getDeploymentId());
    }

    /**
     * Perform sync operation
     */
    public void performSync(SyncOperation sync) {
        if (syncOperations == null) {
            syncOperations = new ArrayList<>();
        }
        syncOperations.add(sync);

        if (syncRegistry == null) {
            syncRegistry = new HashMap<>();
        }
        syncRegistry.put(sync.getSyncId(), sync);

        totalSyncOperations = (totalSyncOperations != null ? totalSyncOperations : 0L) + 1;
        lastSyncAt = LocalDateTime.now();

        recordEvent("SYNC_COMPLETED", "Synchronization operation completed", "SYNC", sync.getSyncId());
    }

    /**
     * Record edge event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        EdgeEvent event = EdgeEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .timestamp(LocalDateTime.now())
                .triggeredBy(createdBy)
                .build();

        events.add(event);
    }
}
