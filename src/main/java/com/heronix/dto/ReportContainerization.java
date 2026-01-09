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
 * Report Containerization DTO
 *
 * Represents containerization and orchestration for report services.
 *
 * Features:
 * - Docker container management
 * - Kubernetes orchestration
 * - Container lifecycle management
 * - Image registry integration
 * - Auto-scaling and load balancing
 * - Service discovery
 * - Health monitoring
 * - Resource management
 *
 * Container Status:
 * - CREATING - Container being created
 * - RUNNING - Container running
 * - PAUSED - Container paused
 * - STOPPED - Container stopped
 * - FAILED - Container failed
 * - TERMINATED - Container terminated
 *
 * Orchestration Platform:
 * - DOCKER_SWARM - Docker Swarm
 * - KUBERNETES - Kubernetes
 * - DOCKER_COMPOSE - Docker Compose
 * - OPENSHIFT - OpenShift
 * - NOMAD - HashiCorp Nomad
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 96 - Report Containerization & Orchestration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContainerization {

    private Long clusterId;
    private String clusterName;
    private String description;
    private String version;

    // Cluster status
    private ClusterStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isActive;

    // Orchestration
    private OrchestrationPlatform orchestrationPlatform;
    private String masterNodeUrl;
    private Integer nodeCount;
    private List<Node> nodes;
    private Map<String, Node> nodeRegistry;

    // Containers
    private List<Container> containers;
    private Integer totalContainers;
    private Integer runningContainers;
    private Integer failedContainers;
    private Map<String, Container> containerRegistry;

    // Images
    private List<ContainerImage> images;
    private String registryUrl;
    private String registryUsername;
    private String registryPassword;
    private Boolean privateRegistry;
    private Map<String, ContainerImage> imageRegistry;

    // Deployments
    private List<Deployment> deployments;
    private Integer totalDeployments;
    private Integer activeDeployments;
    private Map<String, Deployment> deploymentRegistry;

    // Services
    private List<Service> services;
    private Integer totalServices;
    private Map<String, Service> serviceRegistry;

    // Auto-scaling
    private Boolean autoScalingEnabled;
    private AutoScalingPolicy autoScalingPolicy;
    private Integer minReplicas;
    private Integer maxReplicas;
    private Integer targetCpuPercent;
    private Integer targetMemoryPercent;

    // Load balancing
    private Boolean loadBalancingEnabled;
    private LoadBalancingStrategy loadBalancingStrategy;
    private List<LoadBalancer> loadBalancers;

    // Networking
    private List<Network> networks;
    private String defaultNetwork;
    private Boolean serviceMeshEnabled;
    private Map<String, Network> networkRegistry;

    // Storage
    private List<Volume> volumes;
    private List<PersistentVolume> persistentVolumes;
    private Map<String, Volume> volumeRegistry;

    // Health monitoring
    private Boolean healthMonitoringEnabled;
    private Integer healthCheckIntervalSeconds;
    private List<HealthCheck> healthChecks;
    private LocalDateTime lastHealthCheckAt;

    // Resource limits
    private ResourceLimits defaultResourceLimits;
    private Map<String, ResourceLimits> containerResourceLimits;

    // Metrics
    private ClusterMetrics metrics;
    private List<ContainerMetrics> containerMetricsList;

    // Events
    private List<ClusterEvent> events;
    private LocalDateTime lastEventAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Cluster Status
     */
    public enum ClusterStatus {
        INITIALIZING,   // Cluster initializing
        ACTIVE,         // Active and operational
        DEGRADED,       // Degraded performance
        MAINTENANCE,    // Under maintenance
        SCALING,        // Scaling in progress
        ERROR,          // Error state
        SHUTDOWN        // Shutting down
    }

    /**
     * Container Status
     */
    public enum ContainerStatus {
        CREATING,       // Container being created
        RUNNING,        // Container running
        PAUSED,         // Container paused
        RESTARTING,     // Container restarting
        STOPPED,        // Container stopped
        FAILED,         // Container failed
        TERMINATED      // Container terminated
    }

    /**
     * Orchestration Platform
     */
    public enum OrchestrationPlatform {
        DOCKER_SWARM,   // Docker Swarm
        KUBERNETES,     // Kubernetes
        DOCKER_COMPOSE, // Docker Compose
        OPENSHIFT,      // OpenShift
        NOMAD           // HashiCorp Nomad
    }

    /**
     * Load Balancing Strategy
     */
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        IP_HASH,
        WEIGHTED,
        RANDOM
    }

    /**
     * Restart Policy
     */
    public enum RestartPolicy {
        ALWAYS,
        ON_FAILURE,
        UNLESS_STOPPED,
        NEVER
    }

    /**
     * Node
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String nodeId;
        private String nodeName;
        private String hostName;
        private String ipAddress;
        private String role; // MASTER, WORKER
        private String status; // READY, NOT_READY, UNKNOWN

        // Resources
        private Long totalCpuCores;
        private Long totalMemoryMb;
        private Long totalStorageGb;
        private Long availableCpuCores;
        private Long availableMemoryMb;
        private Long availableStorageGb;

        // Containers
        private Integer runningContainers;
        private List<String> containerIds;

        // Monitoring
        private Double cpuUsagePercent;
        private Double memoryUsagePercent;
        private Double storageUsagePercent;
        private LocalDateTime lastHeartbeatAt;

        // Taints and labels
        private Map<String, String> labels;
        private List<String> taints;

        // Metadata
        private LocalDateTime registeredAt;
        private Map<String, Object> metadata;
    }

    /**
     * Container
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Container {
        private String containerId;
        private String containerName;
        private String imageId;
        private String imageName;
        private ContainerStatus status;

        // Node assignment
        private String nodeId;
        private String nodeName;

        // Networking
        private String ipAddress;
        private List<PortMapping> ports;
        private String networkMode;
        private List<String> networks;

        // Resources
        private Long cpuLimit;
        private Long memoryLimitMb;
        private Double cpuUsagePercent;
        private Long memoryUsageMb;

        // Lifecycle
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime stoppedAt;
        private Integer restartCount;
        private RestartPolicy restartPolicy;

        // Health
        private Boolean healthy;
        private String healthStatus;
        private LocalDateTime lastHealthCheckAt;

        // Environment
        private Map<String, String> environmentVariables;
        private List<String> command;
        private List<String> args;
        private String workingDirectory;

        // Volumes
        private List<VolumeMount> volumeMounts;

        // Logs
        private String logDriver;
        private Map<String, Object> logOptions;

        // Metadata
        private Map<String, String> labels;
        private Map<String, Object> metadata;
    }

    /**
     * Container Image
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerImage {
        private String imageId;
        private String repository;
        private String tag;
        private String fullName; // repository:tag
        private String digest;

        // Build info
        private String dockerfile;
        private LocalDateTime builtAt;
        private String builtBy;
        private Long sizeMb;

        // Registry
        private String registryUrl;
        private Boolean isPushed;
        private LocalDateTime pushedAt;

        // Layers
        private Integer layerCount;
        private List<String> layers;

        // Security
        private Boolean scanned;
        private Integer vulnerabilityCount;
        private String securityStatus; // PASS, WARN, FAIL

        // Usage
        private Integer containerCount;
        private LocalDateTime lastUsedAt;

        // Metadata
        private Map<String, String> labels;
        private Map<String, Object> metadata;
    }

    /**
     * Deployment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Deployment {
        private String deploymentId;
        private String deploymentName;
        private String imageId;
        private String imageName;
        private String status; // DEPLOYING, ACTIVE, FAILED, SCALING

        // Replicas
        private Integer desiredReplicas;
        private Integer currentReplicas;
        private Integer availableReplicas;
        private Integer unavailableReplicas;

        // Update strategy
        private String updateStrategy; // ROLLING_UPDATE, RECREATE
        private Integer maxSurge;
        private Integer maxUnavailable;

        // Selectors
        private Map<String, String> selector;
        private Map<String, String> labels;

        // Resources
        private ResourceLimits resourceLimits;

        // Lifecycle
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private Integer revisionNumber;
        private List<String> containerIds;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Service {
        private String serviceId;
        private String serviceName;
        private String serviceType; // CLUSTER_IP, NODE_PORT, LOAD_BALANCER, EXTERNAL_NAME

        // Selectors
        private Map<String, String> selector;
        private List<String> targetDeployments;

        // Networking
        private String clusterIp;
        private String externalIp;
        private List<ServicePort> ports;

        // Load balancer
        private String loadBalancerIp;
        private String loadBalancerStatus;

        // Session affinity
        private Boolean sessionAffinity;
        private Integer sessionAffinityTimeout;

        // Lifecycle
        private LocalDateTime createdAt;
        private Boolean isActive;

        // Metadata
        private Map<String, String> labels;
        private Map<String, Object> metadata;
    }

    /**
     * Auto-Scaling Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoScalingPolicy {
        private String policyId;
        private String policyName;
        private String targetDeployment;

        // Thresholds
        private Integer minReplicas;
        private Integer maxReplicas;
        private Integer targetCpuPercent;
        private Integer targetMemoryPercent;
        private Integer scaleUpThreshold;
        private Integer scaleDownThreshold;

        // Timing
        private Integer scaleUpCooldownSeconds;
        private Integer scaleDownCooldownSeconds;
        private Integer evaluationPeriodSeconds;

        // Metrics
        private List<String> customMetrics;
        private Integer currentReplicas;
        private LocalDateTime lastScaledAt;
        private String lastScaleAction; // SCALE_UP, SCALE_DOWN

        // Status
        private Boolean enabled;
        private LocalDateTime createdAt;
    }

    /**
     * Load Balancer
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadBalancer {
        private String loadBalancerId;
        private String loadBalancerName;
        private LoadBalancingStrategy strategy;

        // Targets
        private List<String> targetServiceIds;
        private List<String> targetContainerIds;

        // Networking
        private String ipAddress;
        private Integer port;
        private String protocol;

        // Health checks
        private String healthCheckPath;
        private Integer healthCheckPort;
        private Integer healthCheckIntervalSeconds;

        // Status
        private Boolean isActive;
        private LocalDateTime createdAt;

        // Metrics
        private Long totalRequests;
        private Long activeConnections;
    }

    /**
     * Network
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Network {
        private String networkId;
        private String networkName;
        private String driver; // bridge, overlay, host, none
        private String subnet;
        private String gateway;

        // Containers
        private List<String> connectedContainers;
        private Integer containerCount;

        // Options
        private Boolean internal;
        private Boolean attachable;
        private Map<String, String> options;

        // Lifecycle
        private LocalDateTime createdAt;
        private Boolean isActive;

        // Metadata
        private Map<String, String> labels;
    }

    /**
     * Volume
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Volume {
        private String volumeId;
        private String volumeName;
        private String driver;
        private String mountPoint;

        // Size
        private Long sizeGb;
        private Long usedGb;
        private Double usagePercent;

        // Containers
        private List<String> attachedContainers;

        // Lifecycle
        private LocalDateTime createdAt;
        private Boolean isPersistent;

        // Metadata
        private Map<String, String> labels;
        private Map<String, Object> options;
    }

    /**
     * Persistent Volume
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersistentVolume {
        private String pvId;
        private String pvName;
        private String storageClass;
        private Long capacityGb;
        private String accessMode; // READ_WRITE_ONCE, READ_ONLY_MANY, READ_WRITE_MANY

        // Binding
        private String claimName;
        private String status; // AVAILABLE, BOUND, RELEASED, FAILED

        // Reclaim policy
        private String reclaimPolicy; // RETAIN, RECYCLE, DELETE

        // Lifecycle
        private LocalDateTime createdAt;
        private Boolean isActive;
    }

    /**
     * Port Mapping
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortMapping {
        private Integer hostPort;
        private Integer containerPort;
        private String protocol; // TCP, UDP
    }

    /**
     * Volume Mount
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeMount {
        private String volumeName;
        private String containerPath;
        private String hostPath;
        private Boolean readOnly;
    }

    /**
     * Service Port
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicePort {
        private String name;
        private Integer port;
        private Integer targetPort;
        private Integer nodePort;
        private String protocol; // TCP, UDP
    }

    /**
     * Resource Limits
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceLimits {
        private Long cpuLimit;
        private Long cpuRequest;
        private Long memoryLimitMb;
        private Long memoryRequestMb;
        private Long storageLimitGb;
    }

    /**
     * Health Check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheck {
        private String checkId;
        private String containerId;
        private LocalDateTime checkedAt;
        private Boolean healthy;
        private String status; // UP, DOWN, DEGRADED
        private Long responseTimeMs;
        private String errorMessage;
        private Map<String, Object> details;
    }

    /**
     * Cluster Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterMetrics {
        private Integer totalNodes;
        private Integer healthyNodes;
        private Integer totalContainers;
        private Integer runningContainers;
        private Double averageCpuUsagePercent;
        private Double averageMemoryUsagePercent;
        private Long totalRequestsProcessed;
        private Double successRate;
        private Long totalStorageUsedGb;
        private LocalDateTime measuredAt;
    }

    /**
     * Container Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerMetrics {
        private String containerId;
        private String containerName;
        private Double cpuUsagePercent;
        private Long memoryUsageMb;
        private Long networkInMb;
        private Long networkOutMb;
        private Long diskReadMb;
        private Long diskWriteMb;
        private Integer restartCount;
        private LocalDateTime measuredAt;
    }

    /**
     * Cluster Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String resourceType; // NODE, CONTAINER, DEPLOYMENT, SERVICE
        private String resourceId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void registerNode(Node node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        nodes.add(node);

        if (nodeRegistry == null) {
            nodeRegistry = new HashMap<>();
        }
        nodeRegistry.put(node.getNodeId(), node);

        nodeCount = (nodeCount != null ? nodeCount : 0) + 1;

        recordEvent("NODE_REGISTERED", "Node registered: " + node.getNodeName(),
                "NODE", node.getNodeId());
    }

    public void registerContainer(Container container) {
        if (containers == null) {
            containers = new ArrayList<>();
        }
        containers.add(container);

        if (containerRegistry == null) {
            containerRegistry = new HashMap<>();
        }
        containerRegistry.put(container.getContainerId(), container);

        totalContainers = (totalContainers != null ? totalContainers : 0) + 1;
        if (container.getStatus() == ContainerStatus.RUNNING) {
            runningContainers = (runningContainers != null ? runningContainers : 0) + 1;
        } else if (container.getStatus() == ContainerStatus.FAILED) {
            failedContainers = (failedContainers != null ? failedContainers : 0) + 1;
        }

        recordEvent("CONTAINER_REGISTERED", "Container registered: " + container.getContainerName(),
                "CONTAINER", container.getContainerId());
    }

    public void updateContainerStatus(String containerId, ContainerStatus newStatus) {
        Container container = containerRegistry != null ? containerRegistry.get(containerId) : null;
        if (container == null) {
            return;
        }

        ContainerStatus oldStatus = container.getStatus();
        container.setStatus(newStatus);

        // Update counts
        if (oldStatus == ContainerStatus.RUNNING && runningContainers != null && runningContainers > 0) {
            runningContainers--;
        } else if (oldStatus == ContainerStatus.FAILED && failedContainers != null && failedContainers > 0) {
            failedContainers--;
        }

        if (newStatus == ContainerStatus.RUNNING) {
            runningContainers = (runningContainers != null ? runningContainers : 0) + 1;
        } else if (newStatus == ContainerStatus.FAILED) {
            failedContainers = (failedContainers != null ? failedContainers : 0) + 1;
        }

        recordEvent("CONTAINER_STATUS_UPDATED",
                "Container status updated: " + container.getContainerName() + " -> " + newStatus,
                "CONTAINER", containerId);
    }

    public void registerDeployment(Deployment deployment) {
        if (deployments == null) {
            deployments = new ArrayList<>();
        }
        deployments.add(deployment);

        if (deploymentRegistry == null) {
            deploymentRegistry = new HashMap<>();
        }
        deploymentRegistry.put(deployment.getDeploymentId(), deployment);

        totalDeployments = (totalDeployments != null ? totalDeployments : 0) + 1;
        if ("ACTIVE".equals(deployment.getStatus())) {
            activeDeployments = (activeDeployments != null ? activeDeployments : 0) + 1;
        }

        recordEvent("DEPLOYMENT_CREATED", "Deployment created: " + deployment.getDeploymentName(),
                "DEPLOYMENT", deployment.getDeploymentId());
    }

    public void registerService(Service service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

        if (serviceRegistry == null) {
            serviceRegistry = new HashMap<>();
        }
        serviceRegistry.put(service.getServiceId(), service);

        totalServices = (totalServices != null ? totalServices : 0) + 1;

        recordEvent("SERVICE_CREATED", "Service created: " + service.getServiceName(),
                "SERVICE", service.getServiceId());
    }

    public void registerImage(ContainerImage image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);

        if (imageRegistry == null) {
            imageRegistry = new HashMap<>();
        }
        imageRegistry.put(image.getImageId(), image);

        recordEvent("IMAGE_REGISTERED", "Image registered: " + image.getFullName(),
                "IMAGE", image.getImageId());
    }

    public void recordHealthCheck(HealthCheck healthCheck) {
        if (healthChecks == null) {
            healthChecks = new ArrayList<>();
        }
        healthChecks.add(healthCheck);

        lastHealthCheckAt = LocalDateTime.now();

        // Update container health status
        if (healthCheck.getContainerId() != null) {
            Container container = containerRegistry != null ?
                    containerRegistry.get(healthCheck.getContainerId()) : null;
            if (container != null) {
                container.setHealthy(healthCheck.getHealthy());
                container.setHealthStatus(healthCheck.getStatus());
                container.setLastHealthCheckAt(healthCheck.getCheckedAt());
            }
        }
    }

    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        ClusterEvent event = ClusterEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    public void startCluster() {
        status = ClusterStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("CLUSTER_STARTED", "Cluster started", "CLUSTER", clusterId.toString());
    }

    public void stopCluster() {
        status = ClusterStatus.SHUTDOWN;
        isActive = false;
        recordEvent("CLUSTER_STOPPED", "Cluster stopped", "CLUSTER", clusterId.toString());
    }

    public Container getContainer(String containerId) {
        return containerRegistry != null ? containerRegistry.get(containerId) : null;
    }

    public List<Container> getRunningContainers() {
        if (containers == null) {
            return new ArrayList<>();
        }
        return containers.stream()
                .filter(c -> c.getStatus() == ContainerStatus.RUNNING)
                .toList();
    }

    public List<Node> getHealthyNodes() {
        if (nodes == null) {
            return new ArrayList<>();
        }
        return nodes.stream()
                .filter(n -> "READY".equals(n.getStatus()))
                .toList();
    }

    public boolean isHealthy() {
        return status == ClusterStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == ClusterStatus.DEGRADED || status == ClusterStatus.ERROR ||
               (failedContainers != null && failedContainers > 0);
    }
}
