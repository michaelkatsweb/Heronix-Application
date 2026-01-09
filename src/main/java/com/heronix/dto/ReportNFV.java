package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Report Network Function Virtualization (NFV) & Software-Defined Networking (SDN) DTO
 *
 * Represents NFV and SDN implementation with virtualized network functions,
 * software-defined controllers, network slicing, and programmable networking.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 152 - Network Function Virtualization & Software-Defined Networking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportNFV {

    private Long nfvId;
    private String nfvName;
    private String description;

    // Platform Configuration
    private String platform; // OPNFV, OSM, ONAP, VMWARE_NSX, CISCO_ACI
    private Boolean nfvEnabled;
    private String environment; // PRODUCTION, STAGING, DEVELOPMENT, TEST
    private Map<String, Object> platformConfig;

    // SDN Configuration
    private Boolean sdnEnabled;
    private String sdnController; // OPENDAYLIGHT, ONOS, RYU, FLOODLIGHT, POX
    private String controllerVersion;
    private Boolean controllerHighAvailability;
    private Integer totalControllers;
    private Integer activeControllers;
    private Map<String, Object> sdnConfig;

    // NFV Infrastructure (NFVI)
    private Boolean nfviEnabled;
    private Integer totalComputeNodes;
    private Integer activeComputeNodes;
    private Integer totalStorageNodes;
    private Integer totalNetworkNodes;
    private Map<String, Object> nfviConfig;

    // Virtual Network Functions (VNF)
    private Boolean vnfEnabled;
    private Integer totalVNFs;
    private Integer activeVNFs;
    private Integer failedVNFs;
    private List<VirtualNetworkFunction> vnfs;
    private Map<String, Object> vnfConfig;

    // VNF Types
    private Integer virtualFirewalls;
    private Integer virtualLoadBalancers;
    private Integer virtualRouters;
    private Integer virtualSwitches;
    private Integer virtualIDS_IPS;
    private Integer virtualWANOptimizers;
    private Map<String, Integer> vnfsByType;

    // VNF Manager (VNFM)
    private Boolean vnfmEnabled;
    private String vnfmType; // TACKER, CLOUDIFY, OPEN_BATON
    private Long totalVNFDeployments;
    private Long successfulDeployments;
    private Long failedDeployments;
    private Map<String, Object> vnfmConfig;

    // NFV Orchestrator (NFVO)
    private Boolean nfvoEnabled;
    private String nfvoType; // OSM, ONAP, OPEN_BATON
    private Integer totalServiceChains;
    private Integer activeServiceChains;
    private Map<String, Object> nfvoConfig;

    // Network Service Descriptors
    private Boolean nsdEnabled;
    private Integer totalNSDs;
    private Integer activeNSDs;
    private List<String> nsdTemplates;
    private Map<String, Object> nsdConfig;

    // Service Function Chaining (SFC)
    private Boolean sfcEnabled;
    private Integer activeSFCs;
    private List<ServiceChain> serviceChains;
    private Map<String, Object> sfcConfig;

    // Network Slicing
    private Boolean networkSlicingEnabled;
    private Integer totalNetworkSlices;
    private Integer activeNetworkSlices;
    private List<NetworkSlice> networkSlices;
    private Map<String, Object> slicingConfig;

    // OpenFlow Protocol
    private Boolean openFlowEnabled;
    private String openFlowVersion; // 1.0, 1.3, 1.4, 1.5
    private Long totalFlowRules;
    private Long activeFlowRules;
    private Map<String, Object> openFlowConfig;

    // Virtual Switches
    private Boolean virtualSwitchEnabled;
    private String vSwitchType; // OVS, VPP, DPDK
    private Integer totalVSwitches;
    private Integer activeVSwitches;
    private Map<String, Object> vSwitchConfig;

    // Network Overlays
    private Boolean overlayNetworkEnabled;
    private String overlayProtocol; // VXLAN, GRE, GENEVE, STT
    private Integer totalOverlays;
    private Integer activeOverlays;
    private Map<String, Object> overlayConfig;

    // Traffic Engineering
    private Boolean trafficEngineeringEnabled;
    private String teAlgorithm; // SHORTEST_PATH, LOAD_BALANCED, QOS_AWARE
    private Long totalTrafficFlows;
    private Double averageLatencyMs;
    private Double averageThroughputGbps;
    private Map<String, Object> trafficEngineering;

    // Quality of Service (QoS)
    private Boolean qosEnabled;
    private Integer totalQoSPolicies;
    private Integer activeQoSPolicies;
    private List<String> qosClasses; // GOLD, SILVER, BRONZE
    private Map<String, Object> qosConfig;

    // Network Security
    private Boolean networkSecurityEnabled;
    private Integer totalSecurityPolicies;
    private Integer activeSecurityPolicies;
    private Boolean microSegmentationEnabled;
    private Map<String, Object> securityConfig;

    // Performance Monitoring
    private Boolean performanceMonitoringEnabled;
    private Long totalPacketsProcessed;
    private Long droppedPackets;
    private Double packetLossRate;
    private Double networkUtilization;
    private Map<String, Object> performanceMetrics;

    // Fault Management
    private Boolean faultManagementEnabled;
    private Integer totalFaults;
    private Integer activeFaults;
    private Integer resolvedFaults;
    private Map<String, Object> faultConfig;

    // Auto-Scaling
    private Boolean autoScalingEnabled;
    private Integer scaleUpThreshold;
    private Integer scaleDownThreshold;
    private Integer totalScaleOperations;
    private Map<String, Object> autoScalingConfig;

    // Multi-Tenancy
    private Boolean multiTenancyEnabled;
    private Integer totalTenants;
    private Integer activeTenants;
    private Map<String, Object> tenancyConfig;

    // Edge Computing Integration
    private Boolean edgeIntegrationEnabled;
    private Integer totalEdgeNodes;
    private Integer activeEdgeNodes;
    private Map<String, Object> edgeConfig;

    // 5G Integration
    private Boolean fiveGIntegrationEnabled;
    private Boolean networkSlicing5G;
    private Integer total5GSlices;
    private Map<String, Object> fiveGConfig;

    // Analytics & AI
    private Boolean analyticsEnabled;
    private Boolean aiOptimizationEnabled;
    private Long totalAnalysisRuns;
    private Map<String, Object> analyticsConfig;

    // Orchestration
    private Boolean orchestrationEnabled;
    private Long totalOrchestrationTasks;
    private Long successfulTasks;
    private Long failedTasks;
    private Map<String, Object> orchestrationConfig;

    // Resource Management
    private Boolean resourceManagementEnabled;
    private Double cpuUtilization;
    private Double memoryUtilization;
    private Double storageUtilization;
    private Double networkBandwidthUtilization;
    private Map<String, Object> resourceMetrics;

    // Cost Management
    private Boolean costManagementEnabled;
    private Double monthlyInfrastructureCost;
    private Double monthlyLicenseCost;
    private Double monthlyOperationalCost;
    private Double totalMonthlyCost;
    private Map<String, Object> costConfig;

    // Compliance
    private Boolean complianceEnabled;
    private List<String> complianceStandards; // ETSI_NFV, MEF, TMF
    private Map<String, Object> complianceConfig;

    // Status
    private String nfvStatus; // INITIALIZING, ACTIVE, DEGRADED, MAINTENANCE, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastOrchestrationAt;
    private LocalDateTime lastHealthCheckAt;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    /**
     * Virtual Network Function
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirtualNetworkFunction {
        private String vnfId;
        private String vnfName;
        private String vnfType; // FIREWALL, LOAD_BALANCER, ROUTER, IDS_IPS
        private String status; // INSTANTIATED, RUNNING, STOPPED, FAILED
        private Integer instances;
        private Double cpuUsage;
        private Double memoryUsage;
        private LocalDateTime deployedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Service Chain
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceChain {
        private String chainId;
        private String chainName;
        private List<String> vnfSequence;
        private String status; // ACTIVE, INACTIVE
        private Integer priority;
        private Long totalTraffic;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Network Slice
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkSlice {
        private String sliceId;
        private String sliceName;
        private String sliceType; // EMBB, URLLC, MMTC
        private Integer bandwidthMbps;
        private Integer latencyMs;
        private String status; // ACTIVE, INACTIVE
        private Integer connectedDevices;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    // Helper Methods

    /**
     * Add VNF
     */
    public void addVNF(VirtualNetworkFunction vnf) {
        if (this.vnfs == null) {
            this.vnfs = new ArrayList<>();
        }
        this.vnfs.add(vnf);
        this.totalVNFs = (this.totalVNFs != null ? this.totalVNFs : 0) + 1;
        if ("RUNNING".equals(vnf.getStatus())) {
            this.activeVNFs = (this.activeVNFs != null ? this.activeVNFs : 0) + 1;
        } else if ("FAILED".equals(vnf.getStatus())) {
            this.failedVNFs = (this.failedVNFs != null ? this.failedVNFs : 0) + 1;
        }
    }

    /**
     * Add service chain
     */
    public void addServiceChain(ServiceChain chain) {
        if (this.serviceChains == null) {
            this.serviceChains = new ArrayList<>();
        }
        this.serviceChains.add(chain);
        this.totalServiceChains = (this.totalServiceChains != null ? this.totalServiceChains : 0) + 1;
        if ("ACTIVE".equals(chain.getStatus())) {
            this.activeSFCs = (this.activeSFCs != null ? this.activeSFCs : 0) + 1;
        }
    }

    /**
     * Add network slice
     */
    public void addNetworkSlice(NetworkSlice slice) {
        if (this.networkSlices == null) {
            this.networkSlices = new ArrayList<>();
        }
        this.networkSlices.add(slice);
        this.totalNetworkSlices = (this.totalNetworkSlices != null ? this.totalNetworkSlices : 0) + 1;
        if ("ACTIVE".equals(slice.getStatus())) {
            this.activeNetworkSlices = (this.activeNetworkSlices != null ? this.activeNetworkSlices : 0) + 1;
        }
    }

    /**
     * Record VNF deployment
     */
    public void recordVNFDeployment(boolean successful) {
        this.totalVNFDeployments = (this.totalVNFDeployments != null ? this.totalVNFDeployments : 0L) + 1L;
        if (successful) {
            this.successfulDeployments = (this.successfulDeployments != null ? this.successfulDeployments : 0L) + 1L;
        } else {
            this.failedDeployments = (this.failedDeployments != null ? this.failedDeployments : 0L) + 1L;
        }
    }

    /**
     * Record orchestration task
     */
    public void recordOrchestrationTask(boolean successful) {
        this.totalOrchestrationTasks = (this.totalOrchestrationTasks != null ? this.totalOrchestrationTasks : 0L) + 1L;
        if (successful) {
            this.successfulTasks = (this.successfulTasks != null ? this.successfulTasks : 0L) + 1L;
        } else {
            this.failedTasks = (this.failedTasks != null ? this.failedTasks : 0L) + 1L;
        }
        this.lastOrchestrationAt = LocalDateTime.now();
    }

    /**
     * Record fault
     */
    public void recordFault(boolean resolved) {
        if (!resolved) {
            this.totalFaults = (this.totalFaults != null ? this.totalFaults : 0) + 1;
            this.activeFaults = (this.activeFaults != null ? this.activeFaults : 0) + 1;
        } else {
            if (this.activeFaults != null && this.activeFaults > 0) {
                this.activeFaults = this.activeFaults - 1;
            }
            this.resolvedFaults = (this.resolvedFaults != null ? this.resolvedFaults : 0) + 1;
        }
    }

    /**
     * Get VNF deployment success rate
     */
    public Double getVNFDeploymentSuccessRate() {
        if (this.totalVNFDeployments == null || this.totalVNFDeployments == 0L) {
            return 0.0;
        }
        long successful = this.successfulDeployments != null ? this.successfulDeployments : 0L;
        return (successful * 100.0) / this.totalVNFDeployments;
    }

    /**
     * Get orchestration success rate
     */
    public Double getOrchestrationSuccessRate() {
        if (this.totalOrchestrationTasks == null || this.totalOrchestrationTasks == 0L) {
            return 0.0;
        }
        long successful = this.successfulTasks != null ? this.successfulTasks : 0L;
        return (successful * 100.0) / this.totalOrchestrationTasks;
    }

    /**
     * Get total cost
     */
    public Double getTotalCost() {
        double infrastructure = this.monthlyInfrastructureCost != null ? this.monthlyInfrastructureCost : 0.0;
        double license = this.monthlyLicenseCost != null ? this.monthlyLicenseCost : 0.0;
        double operational = this.monthlyOperationalCost != null ? this.monthlyOperationalCost : 0.0;
        this.totalMonthlyCost = infrastructure + license + operational;
        return this.totalMonthlyCost;
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.nfvStatus) &&
               this.activeFaults != null && this.activeFaults == 0 &&
               this.packetLossRate != null && this.packetLossRate < 1.0;
    }

    /**
     * Get active VNFs list
     */
    public List<VirtualNetworkFunction> getActiveVNFsList() {
        if (this.vnfs == null) {
            return new ArrayList<>();
        }
        return this.vnfs.stream()
                .filter(vnf -> "RUNNING".equals(vnf.getStatus()))
                .toList();
    }

    /**
     * Get active service chains list
     */
    public List<ServiceChain> getActiveServiceChainsList() {
        if (this.serviceChains == null) {
            return new ArrayList<>();
        }
        return this.serviceChains.stream()
                .filter(chain -> "ACTIVE".equals(chain.getStatus()))
                .toList();
    }

    /**
     * Get active network slices list
     */
    public List<NetworkSlice> getActiveNetworkSlicesList() {
        if (this.networkSlices == null) {
            return new ArrayList<>();
        }
        return this.networkSlices.stream()
                .filter(slice -> "ACTIVE".equals(slice.getStatus()))
                .toList();
    }

    /**
     * Check if scaling needed
     */
    public boolean isScalingNeeded() {
        if (!Boolean.TRUE.equals(this.autoScalingEnabled)) {
            return false;
        }
        return (this.cpuUtilization != null && this.cpuUtilization > this.scaleUpThreshold) ||
               (this.memoryUtilization != null && this.memoryUtilization > this.scaleUpThreshold);
    }
}
