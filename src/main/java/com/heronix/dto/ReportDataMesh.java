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
 * Report Data Mesh & Distributed Data Architecture DTO
 *
 * Represents data mesh architecture implementation with domain-oriented data ownership,
 * data as a product, self-serve data infrastructure, and federated computational governance.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 150 - Data Mesh & Distributed Data Architecture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataMesh {

    private Long dataMeshId;
    private String dataMeshName;
    private String description;

    // Data Mesh Principles
    private Boolean domainOrientedOwnership;
    private Boolean dataAsProductEnabled;
    private Boolean selfServeInfrastructure;
    private Boolean federatedGovernance;

    // Platform Configuration
    private String platform; // CUSTOM, AWS_DATA_MESH, AZURE_PURVIEW, GOOGLE_DATAPLEX
    private Boolean dataMeshEnabled;
    private String environment; // PRODUCTION, STAGING, DEVELOPMENT, TEST
    private Map<String, Object> platformConfig;

    // Domain Management
    private Boolean domainManagementEnabled;
    private Integer totalDomains;
    private Integer activeDomains;
    private List<String> domainNames;
    private Map<String, Object> domainConfig;
    private List<DataDomain> domains;

    // Data Product Management
    private Boolean dataProductEnabled;
    private Integer totalDataProducts;
    private Integer activeDataProducts;
    private Integer publishedDataProducts;
    private Integer draftDataProducts;
    private Long totalDataProductConsumers;
    private Map<String, Object> dataProductConfig;
    private List<DataProduct> dataProducts;

    // Data Product Catalog
    private Boolean catalogEnabled;
    private String catalogType; // OPEN_METADATA, AMUNDSEN, DATAHUB, ATLAS
    private Integer catalogedAssets;
    private Long totalSearches;
    private Long totalDiscoveries;
    private Map<String, Object> catalogConfig;

    // Data Quality
    private Boolean dataQualityEnabled;
    private Double overallQualityScore; // 0-100
    private Integer totalQualityRules;
    private Integer activeQualityRules;
    private Long qualityChecksPassed;
    private Long qualityChecksFailed;
    private Map<String, Object> qualityConfig;

    // Data Contracts
    private Boolean dataContractsEnabled;
    private Integer totalContracts;
    private Integer activeContracts;
    private Integer violatedContracts;
    private Map<String, Object> contractConfig;
    private List<DataContract> dataContracts;

    // Data Lineage
    private Boolean dataLineageEnabled;
    private String lineageTool; // APACHE_ATLAS, AMUNDSEN, MARQUEZ, OPEN_LINEAGE
    private Integer totalLineageNodes;
    private Integer totalLineageEdges;
    private Map<String, Object> lineageConfig;

    // Schema Management
    private Boolean schemaManagementEnabled;
    private String schemaRegistry; // CONFLUENT, APICURIO, AWS_GLUE, PULSAR
    private Integer totalSchemas;
    private Integer schemaVersions;
    private Boolean schemaEvolutionEnabled;
    private Map<String, Object> schemaConfig;

    // Data Ownership
    private Boolean ownershipEnabled;
    private Integer totalDataOwners;
    private Integer totalDataStewards;
    private Map<String, List<String>> domainOwners;
    private Map<String, Object> ownershipConfig;

    // Self-Serve Platform
    private Boolean selfServePlatformEnabled;
    private Integer totalPlatformServices;
    private Integer activePlatformServices;
    private List<String> platformServices; // DATA_INGESTION, TRANSFORMATION, QUALITY, DISCOVERY
    private Map<String, Object> platformServicesConfig;

    // Data Access Control
    private Boolean accessControlEnabled;
    private String accessControlModel; // RBAC, ABAC, PBAC
    private Integer totalAccessPolicies;
    private Integer activeAccessPolicies;
    private Long accessGranted;
    private Long accessDenied;
    private Map<String, Object> accessControlConfig;

    // Data Discovery
    private Boolean discoveryEnabled;
    private Integer totalDiscoveryQueries;
    private Double averageDiscoveryTimeSeconds;
    private List<String> discoveryMethods; // SEARCH, BROWSE, RECOMMENDATION
    private Map<String, Object> discoveryConfig;

    // Data Observability
    private Boolean observabilityEnabled;
    private List<String> observabilityMetrics; // FRESHNESS, VOLUME, SCHEMA, QUALITY
    private Integer totalAlerts;
    private Integer activeIncidents;
    private Map<String, Object> observabilityConfig;

    // SLA Management
    private Boolean slaManagementEnabled;
    private Integer totalSLAs;
    private Integer slasMet;
    private Integer slasViolated;
    private Double slaComplianceRate; // percentage
    private Map<String, Object> slaConfig;

    // Data Pipeline Management
    private Boolean pipelineManagementEnabled;
    private Integer totalPipelines;
    private Integer activePipelines;
    private Integer failedPipelines;
    private Long totalPipelineRuns;
    private Map<String, Object> pipelineConfig;

    // API Management
    private Boolean apiManagementEnabled;
    private Integer totalDataAPIs;
    private Integer activeDataAPIs;
    private Long totalAPIRequests;
    private Long apiRequestsSuccess;
    private Long apiRequestsFailed;
    private Map<String, Object> apiConfig;

    // Data Sharing
    private Boolean dataSharingEnabled;
    private Integer totalDataShares;
    private Integer activeDataShares;
    private List<String> sharingMethods; // API, FILE, STREAM, DATABASE
    private Map<String, Object> sharingConfig;

    // Federated Governance
    private Boolean federatedGovernanceEnabled;
    private Integer totalGovernancePolicies;
    private Integer activeGovernancePolicies;
    private List<String> governanceDomains; // SECURITY, PRIVACY, COMPLIANCE, QUALITY
    private Map<String, Object> governanceConfig;

    // Privacy & Compliance
    private Boolean privacyEnabled;
    private List<String> complianceFrameworks; // GDPR, CCPA, HIPAA, SOC2
    private Boolean piiDetectionEnabled;
    private Integer piiFieldsDetected;
    private Boolean dataAnonymizationEnabled;
    private Map<String, Object> privacyConfig;

    // Data Encryption
    private Boolean encryptionEnabled;
    private Boolean encryptionAtRest;
    private Boolean encryptionInTransit;
    private String encryptionAlgorithm; // AES_256, RSA_2048
    private Map<String, Object> encryptionConfig;

    // Metadata Management
    private Boolean metadataManagementEnabled;
    private String metadataStore; // HIVE_METASTORE, AWS_GLUE, GOOGLE_DATAPLEX
    private Integer totalMetadataEntries;
    private LocalDateTime lastMetadataSync;
    private Map<String, Object> metadataConfig;

    // Data Versioning
    private Boolean versioningEnabled;
    private String versioningStrategy; // SEMANTIC, TIMESTAMP, INCREMENTAL
    private Integer totalVersions;
    private Map<String, Object> versioningConfig;

    // Cost Management
    private Boolean costManagementEnabled;
    private Double monthlyStorageCost;
    private Double monthlyComputeCost;
    private Double monthlyTransferCost;
    private Double totalMonthlyCost;
    private Map<String, Double> costPerDomain;
    private Map<String, Object> costConfig;

    // Performance Metrics
    private Double averageQueryLatencyMs;
    private Double averageIngestionRateMBps;
    private Long totalDataVolumeGB;
    private Long totalDataProcessedGB;
    private Map<String, Object> performanceMetrics;

    // Usage Analytics
    private Boolean usageAnalyticsEnabled;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalQueries;
    private Map<String, Long> queriesByDomain;
    private Map<String, Long> queriesByUser;
    private Map<String, Object> usageMetrics;

    // Data Marketplace
    private Boolean marketplaceEnabled;
    private Integer totalMarketplaceListings;
    private Integer activeListings;
    private Long totalSubscriptions;
    private Map<String, Object> marketplaceConfig;

    // Integration
    private Boolean integrationEnabled;
    private List<String> integratedSystems;
    private Integer totalIntegrations;
    private Integer activeIntegrations;
    private Map<String, Object> integrationConfig;

    // Monitoring & Alerts
    private Boolean monitoringEnabled;
    private Integer totalMonitors;
    private Integer activeMonitors;
    private Integer triggeredAlerts;
    private Map<String, Object> monitoringConfig;

    // Backup & Recovery
    private Boolean backupEnabled;
    private Integer totalBackups;
    private LocalDateTime lastBackupAt;
    private Long backupSizeGB;
    private Map<String, Object> backupConfig;

    // Status
    private String dataMeshStatus; // INITIALIZING, ACTIVE, DEGRADED, MAINTENANCE, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastSyncAt;
    private LocalDateTime lastHealthCheckAt;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    /**
     * Data Domain
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDomain {
        private String domainId;
        private String domainName;
        private String description;
        private String owner;
        private List<String> stewards;
        private Integer dataProductCount;
        private Boolean active;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Data Product
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataProduct {
        private String productId;
        private String productName;
        private String description;
        private String domainId;
        private String owner;
        private String status; // DRAFT, PUBLISHED, DEPRECATED
        private String version;
        private List<String> tags;
        private Integer consumerCount;
        private Double qualityScore;
        private String slaLevel; // GOLD, SILVER, BRONZE
        private LocalDateTime publishedAt;
        private LocalDateTime lastUpdated;
        private Map<String, Object> metadata;
    }

    /**
     * Data Contract
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataContract {
        private String contractId;
        private String contractName;
        private String dataProductId;
        private String provider;
        private String consumer;
        private String schemaVersion;
        private List<String> slaTerms;
        private Boolean active;
        private LocalDateTime effectiveDate;
        private LocalDateTime expiryDate;
        private Integer violations;
        private Map<String, Object> metadata;
    }

    // Helper Methods

    /**
     * Add data domain
     */
    public void addDataDomain(DataDomain domain) {
        if (this.domains == null) {
            this.domains = new ArrayList<>();
        }
        this.domains.add(domain);
        this.totalDomains = (this.totalDomains != null ? this.totalDomains : 0) + 1;
        if (Boolean.TRUE.equals(domain.getActive())) {
            this.activeDomains = (this.activeDomains != null ? this.activeDomains : 0) + 1;
        }
    }

    /**
     * Add data product
     */
    public void addDataProduct(DataProduct product) {
        if (this.dataProducts == null) {
            this.dataProducts = new ArrayList<>();
        }
        this.dataProducts.add(product);
        this.totalDataProducts = (this.totalDataProducts != null ? this.totalDataProducts : 0) + 1;

        if ("PUBLISHED".equals(product.getStatus())) {
            this.publishedDataProducts = (this.publishedDataProducts != null ? this.publishedDataProducts : 0) + 1;
            this.activeDataProducts = (this.activeDataProducts != null ? this.activeDataProducts : 0) + 1;
        } else if ("DRAFT".equals(product.getStatus())) {
            this.draftDataProducts = (this.draftDataProducts != null ? this.draftDataProducts : 0) + 1;
        }
    }

    /**
     * Add data contract
     */
    public void addDataContract(DataContract contract) {
        if (this.dataContracts == null) {
            this.dataContracts = new ArrayList<>();
        }
        this.dataContracts.add(contract);
        this.totalContracts = (this.totalContracts != null ? this.totalContracts : 0) + 1;
        if (Boolean.TRUE.equals(contract.getActive())) {
            this.activeContracts = (this.activeContracts != null ? this.activeContracts : 0) + 1;
        }
    }

    /**
     * Record quality check
     */
    public void recordQualityCheck(boolean passed) {
        if (passed) {
            this.qualityChecksPassed = (this.qualityChecksPassed != null ? this.qualityChecksPassed : 0L) + 1L;
        } else {
            this.qualityChecksFailed = (this.qualityChecksFailed != null ? this.qualityChecksFailed : 0L) + 1L;
        }
        updateQualityScore();
    }

    /**
     * Update quality score
     */
    private void updateQualityScore() {
        long total = (this.qualityChecksPassed != null ? this.qualityChecksPassed : 0L) +
                     (this.qualityChecksFailed != null ? this.qualityChecksFailed : 0L);
        if (total > 0) {
            long passed = this.qualityChecksPassed != null ? this.qualityChecksPassed : 0L;
            this.overallQualityScore = (passed * 100.0) / total;
        } else {
            this.overallQualityScore = 0.0;
        }
    }

    /**
     * Record SLA check
     */
    public void recordSLACheck(boolean met) {
        if (met) {
            this.slasMet = (this.slasMet != null ? this.slasMet : 0) + 1;
        } else {
            this.slasViolated = (this.slasViolated != null ? this.slasViolated : 0) + 1;
        }
        updateSLAComplianceRate();
    }

    /**
     * Update SLA compliance rate
     */
    private void updateSLAComplianceRate() {
        int total = (this.slasMet != null ? this.slasMet : 0) +
                    (this.slasViolated != null ? this.slasViolated : 0);
        if (total > 0) {
            int met = this.slasMet != null ? this.slasMet : 0;
            this.slaComplianceRate = (met * 100.0) / total;
        } else {
            this.slaComplianceRate = 0.0;
        }
    }

    /**
     * Record API request
     */
    public void recordAPIRequest(boolean success) {
        this.totalAPIRequests = (this.totalAPIRequests != null ? this.totalAPIRequests : 0L) + 1L;
        if (success) {
            this.apiRequestsSuccess = (this.apiRequestsSuccess != null ? this.apiRequestsSuccess : 0L) + 1L;
        } else {
            this.apiRequestsFailed = (this.apiRequestsFailed != null ? this.apiRequestsFailed : 0L) + 1L;
        }
    }

    /**
     * Record discovery query
     */
    public void recordDiscoveryQuery() {
        this.totalDiscoveryQueries = (this.totalDiscoveryQueries != null ? this.totalDiscoveryQueries : 0) + 1;
        this.totalQueries = (this.totalQueries != null ? this.totalQueries : 0L) + 1L;
    }

    /**
     * Record contract violation
     */
    public void recordContractViolation(String contractId) {
        this.violatedContracts = (this.violatedContracts != null ? this.violatedContracts : 0) + 1;

        if (this.dataContracts != null) {
            for (DataContract contract : this.dataContracts) {
                if (contract.getContractId().equals(contractId)) {
                    contract.setViolations((contract.getViolations() != null ? contract.getViolations() : 0) + 1);
                    break;
                }
            }
        }
    }

    /**
     * Get API success rate
     */
    public Double getAPISuccessRate() {
        if (this.totalAPIRequests == null || this.totalAPIRequests == 0L) {
            return 0.0;
        }
        long success = this.apiRequestsSuccess != null ? this.apiRequestsSuccess : 0L;
        return (success * 100.0) / this.totalAPIRequests;
    }

    /**
     * Get quality pass rate
     */
    public Double getQualityPassRate() {
        long total = (this.qualityChecksPassed != null ? this.qualityChecksPassed : 0L) +
                     (this.qualityChecksFailed != null ? this.qualityChecksFailed : 0L);
        if (total == 0L) {
            return 0.0;
        }
        long passed = this.qualityChecksPassed != null ? this.qualityChecksPassed : 0L;
        return (passed * 100.0) / total;
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.dataMeshStatus) &&
               this.overallQualityScore != null && this.overallQualityScore >= 80.0 &&
               this.slaComplianceRate != null && this.slaComplianceRate >= 95.0;
    }

    /**
     * Get total cost
     */
    public Double getTotalCost() {
        double storage = this.monthlyStorageCost != null ? this.monthlyStorageCost : 0.0;
        double compute = this.monthlyComputeCost != null ? this.monthlyComputeCost : 0.0;
        double transfer = this.monthlyTransferCost != null ? this.monthlyTransferCost : 0.0;
        this.totalMonthlyCost = storage + compute + transfer;
        return this.totalMonthlyCost;
    }

    /**
     * Get active data products list
     */
    public List<DataProduct> getActiveDataProductsList() {
        if (this.dataProducts == null) {
            return new ArrayList<>();
        }
        return this.dataProducts.stream()
                .filter(p -> "PUBLISHED".equals(p.getStatus()))
                .toList();
    }

    /**
     * Get active contracts list
     */
    public List<DataContract> getActiveContractsList() {
        if (this.dataContracts == null) {
            return new ArrayList<>();
        }
        return this.dataContracts.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .toList();
    }

    /**
     * Check if governance compliant
     */
    public boolean isGovernanceCompliant() {
        return Boolean.TRUE.equals(this.federatedGovernanceEnabled) &&
               this.violatedContracts != null && this.violatedContracts == 0 &&
               this.slaComplianceRate != null && this.slaComplianceRate >= 95.0;
    }
}
