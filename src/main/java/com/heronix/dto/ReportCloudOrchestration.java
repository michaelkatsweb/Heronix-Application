package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Cloud Orchestration DTO
 *
 * Represents cloud-native orchestration configurations including
 * container orchestration, service mesh, auto-scaling, and deployment strategies.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 132 - Cloud-Native Orchestration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCloudOrchestration {

    private Long orchestrationId;
    private String orchestrationName;
    private String description;

    // Orchestration Configuration
    private String orchestrationType; // KUBERNETES, DOCKER_SWARM, NOMAD, ECS, etc.
    private String namespace;
    private Integer replicas;
    private String deploymentStrategy; // ROLLING_UPDATE, BLUE_GREEN, CANARY, RECREATE

    // Container Configuration
    private String containerImage;
    private String containerRegistry;
    private String imageTag;
    private Map<String, String> containerLabels;
    private List<String> containerPorts;

    // Resource Management
    private String cpuRequest;
    private String cpuLimit;
    private String memoryRequest;
    private String memoryLimit;
    private Map<String, String> resourceQuotas;

    // Auto-Scaling Configuration
    private Boolean autoScalingEnabled;
    private Integer minReplicas;
    private Integer maxReplicas;
    private Integer targetCpuUtilization;
    private Integer targetMemoryUtilization;
    private List<Map<String, Object>> customMetrics;

    // Health Checks
    private Map<String, Object> livenessProbe;
    private Map<String, Object> readinessProbe;
    private Map<String, Object> startupProbe;
    private Integer healthCheckInterval;

    // Service Configuration
    private String serviceType; // CLUSTER_IP, NODE_PORT, LOAD_BALANCER, EXTERNAL_NAME
    private Integer servicePort;
    private Integer targetPort;
    private Map<String, String> serviceAnnotations;
    private List<String> serviceEndpoints;

    // Network Configuration
    private String networkPolicy;
    private List<String> ingressRules;
    private List<String> egressRules;
    private Map<String, String> dnsConfig;
    private String serviceDiscovery;

    // Storage Configuration
    private List<Map<String, Object>> volumeMounts;
    private List<Map<String, Object>> persistentVolumeClaims;
    private String storageClass;
    private String volumeSize;

    // Security Configuration
    private Map<String, String> secrets;
    private Map<String, String> configMaps;
    private String serviceAccount;
    private List<String> securityContexts;
    private Boolean runAsNonRoot;

    // Deployment Configuration
    private String rolloutStrategy;
    private Integer maxSurge;
    private Integer maxUnavailable;
    private Integer revisionHistoryLimit;
    private Integer progressDeadlineSeconds;

    // Monitoring & Observability
    private Boolean metricsEnabled;
    private Boolean loggingEnabled;
    private Boolean tracingEnabled;
    private String metricsEndpoint;
    private String logsCollector;
    private String tracingBackend;

    // Load Balancing
    private String loadBalancerType;
    private String loadBalancerAlgorithm; // ROUND_ROBIN, LEAST_CONN, IP_HASH, etc.
    private Map<String, Object> loadBalancerConfig;
    private List<String> loadBalancerIPs;

    // Affinity & Placement
    private Map<String, Object> nodeAffinity;
    private Map<String, Object> podAffinity;
    private Map<String, Object> podAntiAffinity;
    private List<String> nodeSelectorLabels;
    private List<Map<String, Object>> tolerations;

    // Environment Configuration
    private Map<String, String> environmentVariables;
    private List<String> envFromSecrets;
    private List<String> envFromConfigMaps;

    // Execution Status
    private String orchestrationStatus; // PENDING, DEPLOYING, RUNNING, FAILED, STOPPED
    private LocalDateTime lastDeployedAt;
    private LocalDateTime lastScaledAt;
    private Integer currentReplicas;
    private Integer availableReplicas;
    private Integer unavailableReplicas;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods
    public void addContainerPort(String port) {
        if (this.containerPorts != null) {
            this.containerPorts.add(port);
        }
    }

    public void addEnvironmentVariable(String key, String value) {
        if (this.environmentVariables != null) {
            this.environmentVariables.put(key, value);
        }
    }

    public void addSecret(String key, String value) {
        if (this.secrets != null) {
            this.secrets.put(key, value);
        }
    }

    public void addIngressRule(String rule) {
        if (this.ingressRules != null) {
            this.ingressRules.add(rule);
        }
    }

    public void addVolumeMount(Map<String, Object> volumeMount) {
        if (this.volumeMounts != null) {
            this.volumeMounts.add(volumeMount);
        }
    }

    public boolean isHealthy() {
        return "RUNNING".equals(orchestrationStatus) &&
               currentReplicas != null &&
               currentReplicas.equals(replicas);
    }

    public boolean isAutoScaling() {
        return Boolean.TRUE.equals(autoScalingEnabled);
    }

    public double getResourceUtilization() {
        if (currentReplicas == null || maxReplicas == null || maxReplicas == 0) {
            return 0.0;
        }
        return (currentReplicas * 100.0) / maxReplicas;
    }
}
