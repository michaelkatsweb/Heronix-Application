package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Zero Trust Architecture DTO
 *
 * Represents zero trust security model implementation,
 * continuous verification, micro-segmentation, and identity-based access control.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 138 - Zero Trust Architecture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportZeroTrust {

    private Long zeroTrustId;
    private String policyName;
    private String description;

    // Zero Trust Principles
    private Boolean neverTrustEnabled;
    private Boolean alwaysVerifyEnabled;
    private Boolean assumeBreachEnabled;
    private Boolean leastPrivilegeEnabled;
    private Boolean explicitVerifyEnabled;

    // Identity Verification
    private Boolean identityVerificationEnabled;
    private String identityProvider; // OKTA, AZURE_AD, AUTH0, CUSTOM
    private Boolean multiFactorAuthRequired;
    private List<String> mfaMethods; // TOTP, SMS, PUSH, BIOMETRIC, HARDWARE_KEY
    private Boolean continuousAuthentication;
    private Integer reAuthenticationIntervalMinutes;

    // Device Trust
    private Boolean deviceTrustEnabled;
    private Boolean devicePostureCheck;
    private List<String> requiredDeviceAttributes; // ENCRYPTED, PATCHED, MANAGED, COMPLIANT
    private Boolean deviceInventoryEnabled;
    private Map<String, Object> trustedDevices;

    // Context-Based Access
    private Boolean contextualAccessEnabled;
    private List<String> contextFactors; // LOCATION, TIME, IP, BEHAVIOR, RISK_SCORE
    private Map<String, Object> accessPolicies;
    private Boolean adaptiveAuthenticationEnabled;
    private Map<String, Object> riskEngineConfig;

    // Micro-Segmentation
    private Boolean microSegmentationEnabled;
    private List<Map<String, Object>> segments;
    private String segmentationStrategy; // NETWORK, APPLICATION, DATA, USER
    private Boolean dynamicSegmentation;
    private Map<String, Object> segmentationRules;

    // Network Security
    private Boolean networkSegmentationEnabled;
    private List<String> networkZones;
    private Boolean softwareDefinedPerimeter; // SDP
    private Boolean vpnLessAccessEnabled;
    private Map<String, Object> networkPolicies;

    // Application Security
    private Boolean applicationSegmentationEnabled;
    private List<String> protectedApplications;
    private Boolean applicationWhitelisting;
    private Map<String, List<String>> appAccessRules;
    private Boolean apiGatewayIntegration;

    // Data Security
    private Boolean dataSegmentationEnabled;
    private List<String> dataClassifications; // PUBLIC, INTERNAL, CONFIDENTIAL, SECRET
    private Map<String, List<String>> dataAccessPolicies;
    private Boolean dataEncryptionEnforced;
    private Boolean dlpEnabled; // Data Loss Prevention

    // Access Control
    private String accessControlModel; // RBAC, ABAC, PBAC (Policy-Based)
    private Boolean justInTimeAccessEnabled;
    private Boolean justEnoughAccessEnabled;
    private Integer temporaryAccessDuration;
    private Map<String, Object> accessRules;

    // Session Management
    private Boolean sessionMonitoringEnabled;
    private Integer maxSessionDuration;
    private Boolean idleSessionTimeout;
    private Integer idleTimeoutMinutes;
    private Boolean sessionRecordingEnabled;
    private Map<String, Object> activeSessions;

    // Continuous Monitoring
    private Boolean continuousMonitoringEnabled;
    private List<String> monitoredMetrics;
    private Boolean behaviorAnalyticsEnabled;
    private Boolean anomalyDetectionEnabled;
    private Map<String, Object> monitoringConfig;

    // Risk Assessment
    private Boolean riskScoringEnabled;
    private String riskCalculationMethod;
    private Map<String, Double> userRiskScores;
    private Map<String, Double> deviceRiskScores;
    private Map<String, Double> applicationRiskScores;
    private Double overallRiskScore;

    // Trust Scoring
    private Boolean trustScoringEnabled;
    private Map<String, Double> userTrustScores;
    private Map<String, Double> deviceTrustScores;
    private Double trustThreshold; // Minimum score to grant access
    private Map<String, Object> trustScoringConfig;

    // Policy Enforcement
    private Boolean policyEnforcementEnabled;
    private List<Map<String, Object>> enforcementPolicies;
    private String enforcementMode; // PERMISSIVE, ENFORCING, AUDIT_ONLY
    private Integer policyViolations;
    private Map<String, Object> enforcementActions;

    // Encryption
    private Boolean endToEndEncryption;
    private Boolean tlsInspectionEnabled;
    private String encryptionStandard; // TLS_1_3, AES_256
    private Boolean certificateValidation;
    private Map<String, Object> encryptionConfig;

    // Lateral Movement Prevention
    private Boolean lateralMovementPrevention;
    private Boolean eastWestTrafficFiltering;
    private List<String> allowedConnections;
    private List<String> blockedConnections;
    private Map<String, Object> movementPolicies;

    // Privileged Access Management
    private Boolean pamEnabled;
    private List<String> privilegedAccounts;
    private Boolean privilegedSessionMonitoring;
    private Boolean passwordVaultingEnabled;
    private Map<String, Object> pamConfig;

    // Compliance Integration
    private List<String> complianceFrameworks;
    private Boolean complianceReportingEnabled;
    private LocalDateTime lastComplianceCheck;
    private Map<String, Object> complianceStatus;

    // Threat Detection
    private Boolean threatDetectionEnabled;
    private List<String> threatIndicators;
    private Integer detectedThreats;
    private Integer blockedThreats;
    private Map<String, Object> threatIntelligence;

    // Incident Response
    private Boolean autoResponseEnabled;
    private List<Map<String, Object>> responsePlaybooks;
    private Integer triggeredResponses;
    private Map<String, Object> incidentConfig;

    // Integration
    private Boolean siemIntegration;
    private Boolean socIntegration;
    private List<String> integratedTools;
    private Map<String, Object> integrationConfig;

    // Logging & Audit
    private Boolean comprehensiveLogging;
    private String logRetentionDays;
    private Long totalAccessAttempts;
    private Long grantedAccesses;
    private Long deniedAccesses;
    private Map<String, Long> accessByUser;
    private Map<String, Long> accessByResource;

    // Performance Metrics
    private Double averageVerificationTimeMs;
    private Integer activeVerifications;
    private Long totalVerifications;
    private Long successfulVerifications;
    private Long failedVerifications;
    private Map<String, Object> performanceMetrics;

    // Status
    private String policyStatus; // ACTIVE, INACTIVE, LEARNING, ENFORCING
    private LocalDateTime activatedAt;
    private LocalDateTime lastPolicyUpdate;
    private LocalDateTime lastVerification;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods
    public void addSegment(Map<String, Object> segment) {
        if (this.segments != null) {
            this.segments.add(segment);
        }
    }

    public void incrementAccessAttempt(boolean granted) {
        this.totalAccessAttempts = (this.totalAccessAttempts != null ? this.totalAccessAttempts : 0L) + 1;

        if (granted) {
            this.grantedAccesses = (this.grantedAccesses != null ? this.grantedAccesses : 0L) + 1;
        } else {
            this.deniedAccesses = (this.deniedAccesses != null ? this.deniedAccesses : 0L) + 1;
        }
    }

    public void incrementVerification(boolean successful) {
        this.totalVerifications = (this.totalVerifications != null ? this.totalVerifications : 0L) + 1;

        if (successful) {
            this.successfulVerifications =
                (this.successfulVerifications != null ? this.successfulVerifications : 0L) + 1;
        } else {
            this.failedVerifications =
                (this.failedVerifications != null ? this.failedVerifications : 0L) + 1;
        }
    }

    public void incrementPolicyViolation() {
        this.policyViolations = (this.policyViolations != null ? this.policyViolations : 0) + 1;
    }

    public void incrementThreatDetection(boolean blocked) {
        this.detectedThreats = (this.detectedThreats != null ? this.detectedThreats : 0) + 1;

        if (blocked) {
            this.blockedThreats = (this.blockedThreats != null ? this.blockedThreats : 0) + 1;
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(policyStatus) || "ENFORCING".equals(policyStatus);
    }

    public double getAccessGrantRate() {
        if (totalAccessAttempts == null || totalAccessAttempts == 0) {
            return 0.0;
        }
        Long granted = grantedAccesses != null ? grantedAccesses : 0L;
        return (granted * 100.0) / totalAccessAttempts;
    }

    public double getAccessDenyRate() {
        if (totalAccessAttempts == null || totalAccessAttempts == 0) {
            return 0.0;
        }
        Long denied = deniedAccesses != null ? deniedAccesses : 0L;
        return (denied * 100.0) / totalAccessAttempts;
    }

    public double getVerificationSuccessRate() {
        if (totalVerifications == null || totalVerifications == 0) {
            return 0.0;
        }
        Long successful = successfulVerifications != null ? successfulVerifications : 0L;
        return (successful * 100.0) / totalVerifications;
    }

    public double getThreatBlockRate() {
        if (detectedThreats == null || detectedThreats == 0) {
            return 0.0;
        }
        Integer blocked = blockedThreats != null ? blockedThreats : 0;
        return (blocked * 100.0) / detectedThreats;
    }

    public boolean isUserTrusted(String userId) {
        if (!Boolean.TRUE.equals(trustScoringEnabled) || userTrustScores == null) {
            return false;
        }
        Double score = userTrustScores.get(userId);
        return score != null && trustThreshold != null && score >= trustThreshold;
    }

    public boolean isDeviceTrusted(String deviceId) {
        if (!Boolean.TRUE.equals(deviceTrustEnabled) || deviceTrustScores == null) {
            return false;
        }
        Double score = deviceTrustScores.get(deviceId);
        return score != null && trustThreshold != null && score >= trustThreshold;
    }

    public boolean requiresStepUp() {
        return overallRiskScore != null && overallRiskScore > 70.0;
    }
}
