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
 * Report Multi-Cloud Management & Cloud Orchestration DTO
 *
 * Represents multi-cloud infrastructure management with unified orchestration,
 * cross-cloud deployments, cost optimization, and cloud-agnostic operations.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 154 - Multi-Cloud Management & Cloud Orchestration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMultiCloud {

    private Long multiCloudId;
    private String multiCloudName;
    private String description;

    // Platform Configuration
    private String orchestrationPlatform; // TERRAFORM, ANSIBLE, CLOUDFORMATION, PULUMI, CROSSPLANE
    private Boolean multiCloudEnabled;
    private String environment; // PRODUCTION, STAGING, DEVELOPMENT, TEST
    private Map<String, Object> platformConfig;

    // Cloud Providers
    private List<CloudProvider> cloudProviders;
    private Integer totalProviders;
    private Integer activeProviders;
    private List<String> providerNames; // AWS, AZURE, GCP, ALIBABA, ORACLE, IBM

    // Resource Management
    private Boolean resourceManagementEnabled;
    private Long totalResources;
    private Long activeResources;
    private Map<String, Long> resourcesByProvider;
    private Map<String, Long> resourcesByType;
    private Map<String, Object> resourceConfig;

    // Workload Distribution
    private Boolean workloadDistributionEnabled;
    private String distributionStrategy; // COST_OPTIMIZED, PERFORMANCE, GEOGRAPHIC, COMPLIANCE
    private Integer totalWorkloads;
    private Integer activeWorkloads;
    private Map<String, Integer> workloadsByProvider;
    private Map<String, Object> distributionConfig;

    // Cost Management
    private Boolean costManagementEnabled;
    private Double totalMonthlyCost;
    private Map<String, Double> costByProvider;
    private Map<String, Double> costByService;
    private Double costSavings;
    private Double budgetThreshold;
    private Map<String, Object> costConfig;

    // Cloud Migration
    private Boolean migrationEnabled;
    private Integer totalMigrations;
    private Integer completedMigrations;
    private Integer inProgressMigrations;
    private Integer failedMigrations;
    private Double migrationSuccessRate;
    private Map<String, Object> migrationConfig;

    // Hybrid Cloud
    private Boolean hybridCloudEnabled;
    private Integer onPremiseResources;
    private Integer cloudResources;
    private Boolean hybridConnectivityEnabled;
    private Map<String, Object> hybridConfig;

    // Cloud Bursting
    private Boolean cloudBurstingEnabled;
    private Integer burstingThreshold;
    private Integer totalBurstEvents;
    private Map<String, Object> burstingConfig;

    // Disaster Recovery
    private Boolean disasterRecoveryEnabled;
    private String drStrategy; // ACTIVE_ACTIVE, ACTIVE_PASSIVE, PILOT_LIGHT, BACKUP_RESTORE
    private Integer rtoMinutes; // Recovery Time Objective
    private Integer rpoMinutes; // Recovery Point Objective
    private Integer totalDRTests;
    private LocalDateTime lastDRTest;
    private Map<String, Object> drConfig;

    // Data Replication
    private Boolean dataReplicationEnabled;
    private String replicationType; // SYNC, ASYNC, MULTI_REGION
    private Long totalDataReplicated; // GB
    private Map<String, Object> replicationConfig;

    // Load Balancing
    private Boolean globalLoadBalancingEnabled;
    private String loadBalancingMethod; // DNS, ANYCAST, GEOGRAPHIC
    private Integer totalLoadBalancers;
    private Integer activeLoadBalancers;
    private Map<String, Object> loadBalancingConfig;

    // Network Management
    private Boolean networkManagementEnabled;
    private Boolean sdwanEnabled;
    private Integer totalVPCs;
    private Integer totalVPNConnections;
    private Integer totalDirectConnects;
    private Map<String, Object> networkConfig;

    // Security & Compliance
    private Boolean securityEnabled;
    private Boolean crossCloudEncryption;
    private Boolean identityFederation;
    private List<String> complianceStandards; // GDPR, HIPAA, SOC2, PCI_DSS
    private Integer securityPolicies;
    private Map<String, Object> securityConfig;

    // Service Catalog
    private Boolean serviceCatalogEnabled;
    private Integer totalServices;
    private Integer activeServices;
    private List<String> serviceTypes;
    private Map<String, Object> catalogConfig;

    // Automation & Orchestration
    private Boolean orchestrationEnabled;
    private Long totalOrchestrations;
    private Long successfulOrchestrations;
    private Long failedOrchestrations;
    private Double orchestrationSuccessRate;
    private Map<String, Object> orchestrationConfig;

    // Infrastructure as Code
    private Boolean iacEnabled;
    private String iacTool; // TERRAFORM, PULUMI, CLOUDFORMATION, ARM_TEMPLATES
    private Integer totalTemplates;
    private Integer activeTemplates;
    private Map<String, Object> iacConfig;

    // Monitoring & Observability
    private Boolean monitoringEnabled;
    private List<String> monitoringTools; // DATADOG, NEW_RELIC, DYNATRACE, PROMETHEUS
    private Integer totalMetrics;
    private Integer totalAlerts;
    private Map<String, Object> monitoringConfig;

    // Performance Optimization
    private Boolean performanceOptimizationEnabled;
    private Double averageLatencyMs;
    private Double averageThroughputMbps;
    private Double resourceUtilization;
    private Map<String, Object> performanceMetrics;

    // Governance & Policy
    private Boolean governanceEnabled;
    private Integer totalPolicies;
    private Integer activePolicies;
    private Integer policyViolations;
    private Map<String, Object> governanceConfig;

    // Billing & Invoicing
    private Boolean billingEnabled;
    private Boolean chargebackEnabled;
    private Boolean showbackEnabled;
    private Map<String, Double> chargebackByDepartment;
    private Map<String, Object> billingConfig;

    // Resource Tagging
    private Boolean taggingEnabled;
    private Integer totalTags;
    private Map<String, Integer> resourcesByTag;
    private Map<String, Object> taggingConfig;

    // Capacity Planning
    private Boolean capacityPlanningEnabled;
    private Integer utilizationThreshold;
    private Boolean autoScalingEnabled;
    private Map<String, Object> capacityConfig;

    // Vendor Management
    private Boolean vendorManagementEnabled;
    private Integer totalVendors;
    private Integer activeVendors;
    private Map<String, Object> vendorConfig;

    // API Management
    private Boolean apiManagementEnabled;
    private Long totalAPIRequests;
    private Long successfulAPIRequests;
    private Long failedAPIRequests;
    private Map<String, Object> apiConfig;

    // Status
    private String multiCloudStatus; // INITIALIZING, ACTIVE, DEGRADED, MAINTENANCE, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastOrchestrationAt;
    private LocalDateTime lastHealthCheckAt;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    /**
     * Cloud Provider
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloudProvider {
        private String providerId;
        private String providerName; // AWS, AZURE, GCP, etc.
        private String region;
        private Boolean enabled;
        private String status; // CONNECTED, DISCONNECTED, ERROR
        private Long totalResources;
        private Double monthlyCost;
        private LocalDateTime connectedAt;
        private LocalDateTime lastSyncAt;
        private Map<String, Object> credentials;
        private Map<String, Object> metadata;
    }

    // Helper Methods

    /**
     * Add cloud provider
     */
    public void addCloudProvider(CloudProvider provider) {
        if (this.cloudProviders == null) {
            this.cloudProviders = new ArrayList<>();
        }
        this.cloudProviders.add(provider);
        this.totalProviders = (this.totalProviders != null ? this.totalProviders : 0) + 1;
        if (Boolean.TRUE.equals(provider.getEnabled())) {
            this.activeProviders = (this.activeProviders != null ? this.activeProviders : 0) + 1;
        }
    }

    /**
     * Record migration
     */
    public void recordMigration(boolean successful) {
        this.totalMigrations = (this.totalMigrations != null ? this.totalMigrations : 0) + 1;
        if (successful) {
            this.completedMigrations = (this.completedMigrations != null ? this.completedMigrations : 0) + 1;
        } else {
            this.failedMigrations = (this.failedMigrations != null ? this.failedMigrations : 0) + 1;
        }
        updateMigrationSuccessRate();
    }

    /**
     * Update migration success rate
     */
    private void updateMigrationSuccessRate() {
        if (this.totalMigrations != null && this.totalMigrations > 0) {
            int completed = this.completedMigrations != null ? this.completedMigrations : 0;
            this.migrationSuccessRate = (completed * 100.0) / this.totalMigrations;
        } else {
            this.migrationSuccessRate = 0.0;
        }
    }

    /**
     * Record orchestration
     */
    public void recordOrchestration(boolean successful) {
        this.totalOrchestrations = (this.totalOrchestrations != null ? this.totalOrchestrations : 0L) + 1L;
        if (successful) {
            this.successfulOrchestrations = (this.successfulOrchestrations != null ? this.successfulOrchestrations : 0L) + 1L;
        } else {
            this.failedOrchestrations = (this.failedOrchestrations != null ? this.failedOrchestrations : 0L) + 1L;
        }
        updateOrchestrationSuccessRate();
        this.lastOrchestrationAt = LocalDateTime.now();
    }

    /**
     * Update orchestration success rate
     */
    private void updateOrchestrationSuccessRate() {
        if (this.totalOrchestrations != null && this.totalOrchestrations > 0L) {
            long successful = this.successfulOrchestrations != null ? this.successfulOrchestrations : 0L;
            this.orchestrationSuccessRate = (successful * 100.0) / this.totalOrchestrations;
        } else {
            this.orchestrationSuccessRate = 0.0;
        }
    }

    /**
     * Record cloud burst event
     */
    public void recordCloudBurstEvent() {
        this.totalBurstEvents = (this.totalBurstEvents != null ? this.totalBurstEvents : 0) + 1;
    }

    /**
     * Get total cost
     */
    public Double calculateTotalCost() {
        if (this.costByProvider == null || this.costByProvider.isEmpty()) {
            this.totalMonthlyCost = 0.0;
        } else {
            this.totalMonthlyCost = this.costByProvider.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
        return this.totalMonthlyCost;
    }

    /**
     * Get API success rate
     */
    public Double getAPISuccessRate() {
        if (this.totalAPIRequests == null || this.totalAPIRequests == 0L) {
            return 0.0;
        }
        long successful = this.successfulAPIRequests != null ? this.successfulAPIRequests : 0L;
        return (successful * 100.0) / this.totalAPIRequests;
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.multiCloudStatus) &&
               this.activeProviders != null && this.activeProviders > 0 &&
               this.policyViolations != null && this.policyViolations == 0;
    }

    /**
     * Get connected providers list
     */
    public List<CloudProvider> getConnectedProvidersList() {
        if (this.cloudProviders == null) {
            return new ArrayList<>();
        }
        return this.cloudProviders.stream()
                .filter(p -> "CONNECTED".equals(p.getStatus()))
                .toList();
    }

    /**
     * Get active providers list
     */
    public List<CloudProvider> getActiveProvidersList() {
        if (this.cloudProviders == null) {
            return new ArrayList<>();
        }
        return this.cloudProviders.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
                .toList();
    }

    /**
     * Check if cost optimized
     */
    public boolean isCostOptimized() {
        return Boolean.TRUE.equals(this.costManagementEnabled) &&
               this.costSavings != null && this.costSavings > 0.0 &&
               this.totalMonthlyCost != null && this.budgetThreshold != null &&
               this.totalMonthlyCost <= this.budgetThreshold;
    }

    /**
     * Check if requires migration
     */
    public boolean requiresMigration() {
        return Boolean.TRUE.equals(this.migrationEnabled) &&
               this.inProgressMigrations != null && this.inProgressMigrations > 0;
    }
}
