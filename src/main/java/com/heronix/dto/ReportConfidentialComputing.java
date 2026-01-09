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
 * Report Confidential Computing & Trusted Execution Environment DTO
 *
 * Represents confidential computing implementation with hardware-based trusted execution
 * environments (TEE), secure enclaves, memory encryption, and attestation mechanisms.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 151 - Confidential Computing & Trusted Execution Environment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportConfidentialComputing {

    private Long confidentialComputingId;
    private String confidentialComputingName;
    private String description;

    // Platform Configuration
    private String platform; // INTEL_SGX, AMD_SEV, ARM_TRUSTZONE, AWS_NITRO, AZURE_CONFIDENTIAL
    private Boolean confidentialComputingEnabled;
    private String environment; // PRODUCTION, STAGING, DEVELOPMENT, TEST
    private Map<String, Object> platformConfig;

    // Trusted Execution Environment (TEE)
    private Boolean teeEnabled;
    private String teeType; // INTEL_SGX, AMD_SEV_SNP, ARM_TRUSTZONE, IBM_SECURE_EXECUTION
    private Integer totalEnclaves;
    private Integer activeEnclaves;
    private Long totalEnclaveSizeBytes;
    private Map<String, Object> teeConfig;

    // Intel SGX Configuration
    private Boolean sgxEnabled;
    private String sgxVersion; // SGX1, SGX2
    private Integer epcSizeMB; // Enclave Page Cache size
    private Boolean sgxAttestationEnabled;
    private Long totalSgxEnclaves;
    private Map<String, Object> sgxConfig;

    // AMD SEV Configuration
    private Boolean sevEnabled;
    private String sevType; // SEV, SEV_ES, SEV_SNP
    private Integer totalSevVMs;
    private Integer activeSevVMs;
    private Boolean sevSnpEnabled;
    private Map<String, Object> sevConfig;

    // ARM TrustZone Configuration
    private Boolean trustZoneEnabled;
    private Integer totalTrustedApps;
    private Integer activeTrustedApps;
    private Map<String, Object> trustZoneConfig;

    // Memory Encryption
    private Boolean memoryEncryptionEnabled;
    private String encryptionType; // AES_XTS_128, AES_XTS_256
    private Boolean runtimeEncryption;
    private Boolean encryptionAtRest;
    private Double encryptionOverheadPercentage;
    private Map<String, Object> memoryEncryptionConfig;

    // Attestation
    private Boolean attestationEnabled;
    private String attestationType; // REMOTE, LOCAL, EPID, DCAP
    private Long totalAttestations;
    private Long successfulAttestations;
    private Long failedAttestations;
    private Double attestationSuccessRate;
    private Map<String, Object> attestationConfig;

    // Remote Attestation
    private Boolean remoteAttestationEnabled;
    private String attestationService; // INTEL_IAS, AZURE_ATTESTATION, GOOGLE_ATTESTATION
    private Boolean quoteVerificationEnabled;
    private Long totalRemoteAttestations;
    private Map<String, Object> remoteAttestationConfig;

    // Sealed Storage
    private Boolean sealedStorageEnabled;
    private Long totalSealedData;
    private Long sealedDataSizeGB;
    private String sealingPolicy; // MRSIGNER, MRENCLAVE
    private Map<String, Object> sealedStorageConfig;

    // Key Management
    private Boolean keyManagementEnabled;
    private String keyDerivationMethod; // HKDF, PBKDF2
    private Integer totalKeys;
    private Integer activeKeys;
    private Boolean keyRotationEnabled;
    private Integer keyRotationDays;
    private Map<String, Object> keyManagementConfig;

    // Secure Communication
    private Boolean secureChannelEnabled;
    private String tlsVersion; // TLS_1_3
    private Boolean mutualTlsEnabled;
    private Boolean perfectForwardSecrecy;
    private Map<String, Object> secureCommunicationConfig;

    // Data Confidentiality
    private Boolean dataConfidentialityEnabled;
    private Boolean dataInUseProtection;
    private Boolean dataInTransitProtection;
    private Boolean dataAtRestProtection;
    private Map<String, Object> dataConfidentialityConfig;

    // Code Integrity
    private Boolean codeIntegrityEnabled;
    private Boolean signedCodeOnly;
    private Integer totalSignedModules;
    private Integer integrityViolations;
    private Map<String, Object> codeIntegrityConfig;

    // Isolation
    private Boolean processIsolationEnabled;
    private Boolean memoryIsolationEnabled;
    private Boolean networkIsolationEnabled;
    private Integer isolatedProcesses;
    private Map<String, Object> isolationConfig;

    // Secure Boot
    private Boolean secureBootEnabled;
    private Boolean measuredBootEnabled;
    private Boolean trustedBootEnabled;
    private Map<String, Object> secureBootConfig;

    // Hardware Security Module Integration
    private Boolean hsmIntegrationEnabled;
    private String hsmType; // THALES, GEMALTO, AWS_CLOUDHSM, AZURE_DEDICATED_HSM
    private Integer totalHsmKeys;
    private Map<String, Object> hsmConfig;

    // Side-Channel Protection
    private Boolean sideChannelProtectionEnabled;
    private List<String> protectionMethods; // CONSTANT_TIME, BLINDING, MASKING
    private Integer detectedSideChannelAttacks;
    private Map<String, Object> sideChannelConfig;

    // Workload Protection
    private Boolean workloadProtectionEnabled;
    private Integer totalProtectedWorkloads;
    private Integer activeProtectedWorkloads;
    private List<String> protectedWorkloadTypes;
    private Map<String, Object> workloadProtectionConfig;

    // Confidential Container
    private Boolean confidentialContainerEnabled;
    private Integer totalConfidentialContainers;
    private Integer activeConfidentialContainers;
    private String containerRuntime; // KATA_CONTAINERS, NABLA_CONTAINERS
    private Map<String, Object> confidentialContainerConfig;

    // Confidential VM
    private Boolean confidentialVmEnabled;
    private Integer totalConfidentialVMs;
    private Integer activeConfidentialVMs;
    private Boolean vTpmEnabled;
    private Map<String, Object> confidentialVmConfig;

    // Secure Multi-Party Computation (MPC)
    private Boolean mpcEnabled;
    private Integer totalMpcSessions;
    private Integer activeMpcSessions;
    private List<String> mpcProtocols; // SPDZ, MASCOT, GARBLED_CIRCUITS
    private Map<String, Object> mpcConfig;

    // Homomorphic Encryption
    private Boolean homomorphicEncryptionEnabled;
    private String heScheme; // BFV, CKKS, TFHE
    private Long totalHeOperations;
    private Double hePerformanceOverhead;
    private Map<String, Object> heConfig;

    // Secure Enclaves
    private List<SecureEnclave> secureEnclaves;
    private Integer totalSecureEnclaves;
    private Integer activeSecureEnclaves;

    // Attestation Reports
    private List<AttestationReport> attestationReports;
    private Integer totalAttestationReports;

    // Performance Metrics
    private Double averageEnclaveLatencyMs;
    private Double averageAttestationTimeMs;
    private Double encryptionThroughputMBps;
    private Double decryptionThroughputMBps;
    private Map<String, Object> performanceMetrics;

    // Security Metrics
    private Long totalSecurityEvents;
    private Long criticalSecurityEvents;
    private Long totalIntrusionAttempts;
    private Long blockedIntrusionAttempts;
    private Map<String, Object> securityMetrics;

    // Compliance
    private Boolean complianceEnabled;
    private List<String> complianceStandards; // FIPS_140_3, COMMON_CRITERIA, PCI_DSS
    private Boolean fips140Certified;
    private Boolean commonCriteriaCertified;
    private Map<String, Object> complianceConfig;

    // Audit & Logging
    private Boolean auditLoggingEnabled;
    private Long totalAuditLogs;
    private Long auditLogSizeGB;
    private Integer auditRetentionDays;
    private Map<String, Object> auditConfig;

    // Monitoring
    private Boolean monitoringEnabled;
    private Integer totalMonitors;
    private Integer activeMonitors;
    private Integer triggeredAlerts;
    private Map<String, Object> monitoringConfig;

    // Integration
    private Boolean cloudIntegrationEnabled;
    private List<String> integratedCloudProviders; // AWS, AZURE, GCP
    private Map<String, Object> integrationConfig;

    // Cost Management
    private Boolean costTrackingEnabled;
    private Double monthlyTeeComputeCost;
    private Double monthlyHsmCost;
    private Double monthlyAttestationCost;
    private Double totalMonthlyCost;
    private Map<String, Object> costConfig;

    // Status
    private String confidentialComputingStatus; // INITIALIZING, ACTIVE, DEGRADED, MAINTENANCE, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastAttestationAt;
    private LocalDateTime lastHealthCheckAt;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    /**
     * Secure Enclave
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecureEnclave {
        private String enclaveId;
        private String enclaveName;
        private String enclaveType; // SGX, SEV, TRUSTZONE
        private Long enclaveSizeBytes;
        private String status; // CREATED, INITIALIZED, RUNNING, TERMINATED
        private Boolean attestationValid;
        private LocalDateTime createdAt;
        private LocalDateTime lastAttestationAt;
        private Map<String, Object> measurements;
        private Map<String, Object> metadata;
    }

    /**
     * Attestation Report
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttestationReport {
        private String reportId;
        private String enclaveId;
        private String attestationType; // REMOTE, LOCAL
        private Boolean verified;
        private String quoteStatus; // OK, GROUP_OUT_OF_DATE, CONFIGURATION_NEEDED
        private LocalDateTime attestationTime;
        private Map<String, Object> measurements;
        private Map<String, Object> metadata;
    }

    // Helper Methods

    /**
     * Add secure enclave
     */
    public void addSecureEnclave(SecureEnclave enclave) {
        if (this.secureEnclaves == null) {
            this.secureEnclaves = new ArrayList<>();
        }
        this.secureEnclaves.add(enclave);
        this.totalSecureEnclaves = (this.totalSecureEnclaves != null ? this.totalSecureEnclaves : 0) + 1;
        if ("RUNNING".equals(enclave.getStatus())) {
            this.activeSecureEnclaves = (this.activeSecureEnclaves != null ? this.activeSecureEnclaves : 0) + 1;
        }
    }

    /**
     * Add attestation report
     */
    public void addAttestationReport(AttestationReport report) {
        if (this.attestationReports == null) {
            this.attestationReports = new ArrayList<>();
        }
        this.attestationReports.add(report);
        this.totalAttestationReports = (this.totalAttestationReports != null ? this.totalAttestationReports : 0) + 1;
    }

    /**
     * Record attestation
     */
    public void recordAttestation(boolean successful) {
        this.totalAttestations = (this.totalAttestations != null ? this.totalAttestations : 0L) + 1L;
        if (successful) {
            this.successfulAttestations = (this.successfulAttestations != null ? this.successfulAttestations : 0L) + 1L;
        } else {
            this.failedAttestations = (this.failedAttestations != null ? this.failedAttestations : 0L) + 1L;
        }
        updateAttestationSuccessRate();
        this.lastAttestationAt = LocalDateTime.now();
    }

    /**
     * Update attestation success rate
     */
    private void updateAttestationSuccessRate() {
        if (this.totalAttestations != null && this.totalAttestations > 0L) {
            long successful = this.successfulAttestations != null ? this.successfulAttestations : 0L;
            this.attestationSuccessRate = (successful * 100.0) / this.totalAttestations;
        } else {
            this.attestationSuccessRate = 0.0;
        }
    }

    /**
     * Record security event
     */
    public void recordSecurityEvent(boolean critical) {
        this.totalSecurityEvents = (this.totalSecurityEvents != null ? this.totalSecurityEvents : 0L) + 1L;
        if (critical) {
            this.criticalSecurityEvents = (this.criticalSecurityEvents != null ? this.criticalSecurityEvents : 0L) + 1L;
        }
    }

    /**
     * Record intrusion attempt
     */
    public void recordIntrusionAttempt(boolean blocked) {
        this.totalIntrusionAttempts = (this.totalIntrusionAttempts != null ? this.totalIntrusionAttempts : 0L) + 1L;
        if (blocked) {
            this.blockedIntrusionAttempts = (this.blockedIntrusionAttempts != null ? this.blockedIntrusionAttempts : 0L) + 1L;
        }
    }

    /**
     * Record integrity violation
     */
    public void recordIntegrityViolation() {
        this.integrityViolations = (this.integrityViolations != null ? this.integrityViolations : 0) + 1;
    }

    /**
     * Get intrusion block rate
     */
    public Double getIntrusionBlockRate() {
        if (this.totalIntrusionAttempts == null || this.totalIntrusionAttempts == 0L) {
            return 0.0;
        }
        long blocked = this.blockedIntrusionAttempts != null ? this.blockedIntrusionAttempts : 0L;
        return (blocked * 100.0) / this.totalIntrusionAttempts;
    }

    /**
     * Get total cost
     */
    public Double getTotalCost() {
        double tee = this.monthlyTeeComputeCost != null ? this.monthlyTeeComputeCost : 0.0;
        double hsm = this.monthlyHsmCost != null ? this.monthlyHsmCost : 0.0;
        double attestation = this.monthlyAttestationCost != null ? this.monthlyAttestationCost : 0.0;
        this.totalMonthlyCost = tee + hsm + attestation;
        return this.totalMonthlyCost;
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.confidentialComputingStatus) &&
               this.attestationSuccessRate != null && this.attestationSuccessRate >= 95.0 &&
               this.integrityViolations != null && this.integrityViolations == 0;
    }

    /**
     * Check if compliant
     */
    public boolean isCompliant() {
        return Boolean.TRUE.equals(this.complianceEnabled) &&
               Boolean.TRUE.equals(this.attestationEnabled) &&
               Boolean.TRUE.equals(this.memoryEncryptionEnabled) &&
               Boolean.TRUE.equals(this.codeIntegrityEnabled);
    }

    /**
     * Get active enclaves list
     */
    public List<SecureEnclave> getActiveEnclavesList() {
        if (this.secureEnclaves == null) {
            return new ArrayList<>();
        }
        return this.secureEnclaves.stream()
                .filter(e -> "RUNNING".equals(e.getStatus()))
                .toList();
    }

    /**
     * Get verified attestation reports
     */
    public List<AttestationReport> getVerifiedAttestationsList() {
        if (this.attestationReports == null) {
            return new ArrayList<>();
        }
        return this.attestationReports.stream()
                .filter(r -> Boolean.TRUE.equals(r.getVerified()))
                .toList();
    }

    /**
     * Check if attestation required
     */
    public boolean requiresAttestation() {
        if (!Boolean.TRUE.equals(this.attestationEnabled)) {
            return false;
        }

        // Require attestation if last one was more than 24 hours ago
        if (this.lastAttestationAt == null) {
            return true;
        }

        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return this.lastAttestationAt.isBefore(yesterday);
    }
}
