package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Cloud-Native Infrastructure DTO
 *
 * Represents cloud-native infrastructure, container orchestration, Kubernetes management,
 * cloud platforms, serverless computing, and infrastructure automation.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 148 - Cloud-Native Infrastructure & Container Orchestration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCloudNative {

    private Long cloudNativeId;
    private String cloudNativeName;
    private String description;

    // Platform Configuration
    private String platform; // KUBERNETES, EKS, AKS, GKE, OPENSHIFT, RANCHER
    private Boolean cloudNativeEnabled;
    private String environment; // PRODUCTION, STAGING, DEVELOPMENT, TEST
    private Map<String, Object> platformConfig;

    // Cluster Configuration
    private Boolean clusterEnabled;
    private Integer totalClusters;
    private Integer activeClusters;
    private String clusterVersion;
    private List<String> clusterNames;
    private Map<String, Object> clusterConfig;

    // Node Management
    private Boolean nodeManagementEnabled;
    private Integer totalNodes;
    private Integer activeNodes;
    private Integer failedNodes;
    private Integer nodePoolCount;
    private List<String> nodeTypes;
    private Map<String, Integer> nodesByType;
    private Map<String, Object> nodeConfig;

    // Container Management
    private Boolean containerEnabled;
    private Long totalContainers;
    private Long runningContainers;
    private Long stoppedContainers;
    private Long failedContainers;
    private Double containerStartupTime; // seconds
    private Map<String, Object> containerConfig;

    // Pod Management
    private Boolean podManagementEnabled;
    private Long totalPods;
    private Long runningPods;
    private Long pendingPods;
    private Long failedPods;
    private Double averagePodStartTime; // seconds
    private Map<String, Object> podConfig;

    // Deployment Management
    private Boolean deploymentEnabled;
    private Integer totalDeployments;
    private Integer activeDeployments;
    private Integer rollingUpdates;
    private Integer canaryDeployments;
    private Integer blueGreenDeployments;
    private Map<String, Object> deploymentConfig;

    // Service Management
    private Boolean serviceManagementEnabled;
    private Integer totalServices;
    private Integer loadBalancerServices;
    private Integer clusterIPServices;
    private Integer nodePortServices;
    private Map<String, Object> serviceConfig;

    // Ingress Management
    private Boolean ingressEnabled;
    private Integer totalIngresses;
    private Integer activeIngresses;
    private List<String> ingressControllers;
    private Map<String, Object> ingressConfig;

    // ConfigMap & Secrets
    private Boolean configManagementEnabled;
    private Integer totalConfigMaps;
    private Integer totalSecrets;
    private Boolean secretEncryptionEnabled;
    private Map<String, Object> configMapConfig;

    // Persistent Storage
    private Boolean storageEnabled;
    private Integer totalPVCs;
    private Integer totalPVs;
    private Long totalStorageGB;
    private Long usedStorageGB;
    private List<String> storageClasses;
    private Map<String, Object> storageConfig;

    // Namespaces
    private Boolean namespaceManagementEnabled;
    private Integer totalNamespaces;
    private List<String> namespaces;
    private Map<String, Object> namespaceConfig;

    // Scaling
    private Boolean autoscalingEnabled;
    private Boolean horizontalPodAutoscalerEnabled;
    private Boolean verticalPodAutoscalerEnabled;
    private Boolean clusterAutoscalerEnabled;
    private Integer totalHPAs;
    private Integer totalVPAs;
    private Map<String, Object> scalingConfig;

    // Resource Management
    private Boolean resourceManagementEnabled;
    private Double cpuRequestTotal; // cores
    private Double cpuLimitTotal; // cores
    private Double memoryRequestTotal; // GB
    private Double memoryLimitTotal; // GB
    private Double cpuUtilization; // percentage
    private Double memoryUtilization; // percentage
    private Map<String, Object> resourceConfig;

    // Networking
    private Boolean networkingEnabled;
    private String cniPlugin; // CALICO, FLANNEL, WEAVE, CILIUM
    private Boolean networkPoliciesEnabled;
    private Integer totalNetworkPolicies;
    private Map<String, Object> networkingConfig;

    // Service Mesh Integration
    private Boolean serviceMeshEnabled;
    private String serviceMeshType; // ISTIO, LINKERD, CONSUL
    private Integer meshServices;
    private Map<String, Object> serviceMeshConfig;

    // Registry Management
    private Boolean registryEnabled;
    private String registryType; // DOCKER_HUB, ECR, GCR, ACR, HARBOR
    private Long totalImages;
    private Long totalImagePulls;
    private Map<String, Object> registryConfig;

    // Helm Management
    private Boolean helmEnabled;
    private String helmVersion;
    private Integer totalHelmReleases;
    private Integer activeHelmReleases;
    private List<String> helmCharts;
    private Map<String, Object> helmConfig;

    // Operators
    private Boolean operatorsEnabled;
    private Integer totalOperators;
    private Integer activeOperators;
    private List<String> operatorTypes;
    private Map<String, Object> operatorConfig;

    // Serverless
    private Boolean serverlessEnabled;
    private String serverlessPlatform; // KNATIVE, OPENFAAS, KUBELESS, FISSION
    private Integer totalFunctions;
    private Integer activeFunctions;
    private Long functionInvocations;
    private Map<String, Object> serverlessConfig;

    // CI/CD Integration
    private Boolean cicdIntegrationEnabled;
    private List<String> cicdTools; // JENKINS, GITLAB_CI, GITHUB_ACTIONS, ARGOCD
    private Integer totalPipelines;
    private Integer successfulDeployments;
    private Integer failedDeployments;
    private Map<String, Object> cicdConfig;

    // GitOps
    private Boolean gitopsEnabled;
    private String gitopsTools; // ARGOCD, FLUX, JENKINS_X
    private Integer totalApplications;
    private Integer syncedApplications;
    private Map<String, Object> gitopsConfig;

    // Monitoring Integration
    private Boolean monitoringEnabled;
    private List<String> monitoringTools; // PROMETHEUS, GRAFANA, DATADOG
    private Integer totalMetrics;
    private Integer totalAlerts;
    private Map<String, Object> monitoringConfig;

    // Logging
    private Boolean loggingEnabled;
    private String loggingStack; // ELK, LOKI, FLUENTD
    private Long logsCollectedPerDay;
    private Long totalLogVolumeGB;
    private Map<String, Object> loggingConfig;

    // Backup & Recovery
    private Boolean backupEnabled;
    private String backupTool; // VELERO, KASTEN, STASH
    private Integer totalBackups;
    private LocalDateTime lastBackupAt;
    private Map<String, Object> backupConfig;

    // Disaster Recovery
    private Boolean disasterRecoveryEnabled;
    private Integer recoveryPointObjective; // minutes
    private Integer recoveryTimeObjective; // minutes
    private Integer totalDRTests;
    private LocalDateTime lastDRTest;
    private Map<String, Object> drConfig;

    // Cost Management
    private Boolean costManagementEnabled;
    private Double monthlyClusterCost;
    private Double monthlyComputeCost;
    private Double monthlyStorageCost;
    private Double monthlyNetworkCost;
    private Map<String, Object> costConfig;

    // Compliance & Security
    private Boolean complianceEnabled;
    private List<String> complianceStandards; // CIS, PCI_DSS, HIPAA
    private Integer securityScans;
    private Integer vulnerabilities;
    private Integer criticalVulnerabilities;
    private Map<String, Object> complianceConfig;

    // Multi-Cloud
    private Boolean multiCloudEnabled;
    private List<String> cloudProviders; // AWS, AZURE, GCP
    private Integer clustersPerProvider;
    private Map<String, Object> multiCloudConfig;

    // Edge Computing
    private Boolean edgeEnabled;
    private Integer edgeNodes;
    private Integer edgeClusters;
    private Map<String, Object> edgeConfig;

    // Performance Metrics
    private Double apiServerLatency; // milliseconds
    private Double schedulerLatency; // milliseconds
    private Double controllerLatency; // milliseconds
    private Double etcdLatency; // milliseconds
    private Map<String, Object> performanceMetrics;

    // Health Metrics
    private Boolean healthCheckEnabled;
    private Integer healthyComponents;
    private Integer unhealthyComponents;
    private Double clusterHealthScore; // 0-100
    private Map<String, Object> healthMetrics;

    // Status
    private String cloudNativeStatus; // INITIALIZING, ACTIVE, DEGRADED, MAINTENANCE, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastSyncAt;
    private LocalDateTime lastHealthCheckAt;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods

    /**
     * Deploy cluster
     */
    public void deployCluster() {
        this.totalClusters = (this.totalClusters != null ? this.totalClusters : 0) + 1;
        this.activeClusters = (this.activeClusters != null ? this.activeClusters : 0) + 1;
        this.cloudNativeStatus = "ACTIVE";
        this.activatedAt = LocalDateTime.now();
    }

    /**
     * Add node
     */
    public void addNode(String nodeType) {
        this.totalNodes = (this.totalNodes != null ? this.totalNodes : 0) + 1;
        this.activeNodes = (this.activeNodes != null ? this.activeNodes : 0) + 1;

        if (this.nodesByType == null) {
            this.nodesByType = new java.util.HashMap<>();
        }
        Integer count = this.nodesByType.getOrDefault(nodeType, 0);
        this.nodesByType.put(nodeType, count + 1);
    }

    /**
     * Record container start
     */
    public void recordContainerStart(boolean success, double startupTime) {
        this.totalContainers = (this.totalContainers != null ? this.totalContainers : 0L) + 1L;

        if (success) {
            this.runningContainers = (this.runningContainers != null ? this.runningContainers : 0L) + 1L;

            // Update average startup time
            if (this.containerStartupTime == null) {
                this.containerStartupTime = startupTime;
            } else {
                this.containerStartupTime = (this.containerStartupTime + startupTime) / 2.0;
            }
        } else {
            this.failedContainers = (this.failedContainers != null ? this.failedContainers : 0L) + 1L;
        }
    }

    /**
     * Record pod start
     */
    public void recordPodStart(boolean success, double startTime) {
        this.totalPods = (this.totalPods != null ? this.totalPods : 0L) + 1L;

        if (success) {
            this.runningPods = (this.runningPods != null ? this.runningPods : 0L) + 1L;

            // Update average pod start time
            if (this.averagePodStartTime == null) {
                this.averagePodStartTime = startTime;
            } else {
                this.averagePodStartTime = (this.averagePodStartTime + startTime) / 2.0;
            }
        } else {
            this.failedPods = (this.failedPods != null ? this.failedPods : 0L) + 1L;
        }
    }

    /**
     * Record deployment
     */
    public void recordDeployment(String deploymentType) {
        this.totalDeployments = (this.totalDeployments != null ? this.totalDeployments : 0) + 1;
        this.activeDeployments = (this.activeDeployments != null ? this.activeDeployments : 0) + 1;

        if ("ROLLING".equals(deploymentType)) {
            this.rollingUpdates = (this.rollingUpdates != null ? this.rollingUpdates : 0) + 1;
        } else if ("CANARY".equals(deploymentType)) {
            this.canaryDeployments = (this.canaryDeployments != null ? this.canaryDeployments : 0) + 1;
        } else if ("BLUE_GREEN".equals(deploymentType)) {
            this.blueGreenDeployments = (this.blueGreenDeployments != null ? this.blueGreenDeployments : 0) + 1;
        }
    }

    /**
     * Update resource utilization
     */
    public void updateResourceUtilization(double cpuUsed, double memoryUsed) {
        if (this.cpuLimitTotal != null && this.cpuLimitTotal > 0) {
            this.cpuUtilization = (cpuUsed / this.cpuLimitTotal) * 100.0;
        }

        if (this.memoryLimitTotal != null && this.memoryLimitTotal > 0) {
            this.memoryUtilization = (memoryUsed / this.memoryLimitTotal) * 100.0;
        }
    }

    /**
     * Calculate cluster health score
     */
    public Double calculateHealthScore() {
        double score = 100.0;

        // Deduct for unhealthy components
        if (this.unhealthyComponents != null && this.unhealthyComponents > 0) {
            score -= this.unhealthyComponents * 10.0;
        }

        // Deduct for failed pods
        if (this.failedPods != null && this.totalPods != null && this.totalPods > 0) {
            double failureRate = (this.failedPods.doubleValue() / this.totalPods.doubleValue()) * 100.0;
            score -= failureRate * 0.5;
        }

        // Deduct for failed nodes
        if (this.failedNodes != null && this.failedNodes > 0) {
            score -= this.failedNodes * 15.0;
        }

        this.clusterHealthScore = Math.max(score, 0.0);
        return this.clusterHealthScore;
    }

    /**
     * Record CI/CD deployment
     */
    public void recordCICDDeployment(boolean successful) {
        if (successful) {
            this.successfulDeployments = (this.successfulDeployments != null ? this.successfulDeployments : 0) + 1;
        } else {
            this.failedDeployments = (this.failedDeployments != null ? this.failedDeployments : 0) + 1;
        }
    }

    /**
     * Check if scaling needed
     */
    public boolean isScalingNeeded() {
        if (!Boolean.TRUE.equals(this.autoscalingEnabled)) {
            return false;
        }

        // Check if CPU or memory utilization is high
        return (this.cpuUtilization != null && this.cpuUtilization > 80.0) ||
               (this.memoryUtilization != null && this.memoryUtilization > 80.0);
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.cloudNativeStatus) &&
               this.clusterHealthScore != null && this.clusterHealthScore >= 80.0;
    }

    /**
     * Get deployment success rate
     */
    public Double getDeploymentSuccessRate() {
        if (this.totalDeployments == null || this.totalDeployments == 0) {
            return 0.0;
        }

        int successful = this.successfulDeployments != null ? this.successfulDeployments : 0;
        return (successful * 100.0) / this.totalDeployments;
    }

    /**
     * Get storage utilization percentage
     */
    public Double getStorageUtilization() {
        if (this.totalStorageGB == null || this.totalStorageGB == 0L) {
            return 0.0;
        }

        long used = this.usedStorageGB != null ? this.usedStorageGB : 0L;
        return (used * 100.0) / this.totalStorageGB;
    }
}
