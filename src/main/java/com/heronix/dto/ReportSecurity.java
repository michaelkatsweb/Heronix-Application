package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Security DTO
 *
 * Represents security configuration and access control for reports.
 *
 * Features:
 * - Role-based access control (RBAC)
 * - Attribute-based access control (ABAC)
 * - Data masking and redaction
 * - Encryption and decryption
 * - Audit logging
 * - Security policies
 *
 * Security Layers:
 * - Authentication (who you are)
 * - Authorization (what you can do)
 * - Data protection (encryption, masking)
 * - Audit trail (what you did)
 *
 * Access Levels:
 * - OWNER: Full control
 * - ADMIN: Administrative access
 * - WRITE: Read and modify
 * - READ: View only
 * - NONE: No access
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 79 - Report Security & Access Control
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSecurity {

    /**
     * Access level enumeration
     */
    public enum AccessLevel {
        OWNER,              // Full control
        ADMIN,              // Administrative access
        WRITE,              // Read and write
        READ,               // Read only
        NONE                // No access
    }

    /**
     * Security status enumeration
     */
    public enum SecurityStatus {
        SECURE,             // Fully secured
        PARTIAL,            // Partially secured
        VULNERABLE,         // Has vulnerabilities
        PENDING,            // Pending security review
        DISABLED            // Security disabled
    }

    /**
     * Encryption algorithm enumeration
     */
    public enum EncryptionAlgorithm {
        AES_256,            // AES 256-bit
        AES_128,            // AES 128-bit
        RSA_2048,           // RSA 2048-bit
        RSA_4096,           // RSA 4096-bit
        CHACHA20,           // ChaCha20
        NONE                // No encryption
    }

    /**
     * Masking strategy enumeration
     */
    public enum MaskingStrategy {
        FULL,               // Completely masked
        PARTIAL,            // Partially masked
        FIRST_LAST,         // Show first and last characters
        EMAIL,              // Email masking
        PHONE,              // Phone number masking
        SSN,                // SSN masking
        CREDIT_CARD,        // Credit card masking
        CUSTOM,             // Custom masking
        NONE                // No masking
    }

    /**
     * Security policy type enumeration
     */
    public enum PolicyType {
        ACCESS_CONTROL,     // Access control policy
        DATA_PROTECTION,    // Data protection policy
        RETENTION,          // Data retention policy
        PRIVACY,            // Privacy policy
        COMPLIANCE,         // Compliance policy
        CUSTOM              // Custom policy
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Security ID
     */
    private Long securityId;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Security status
     */
    private SecurityStatus status;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Last security review
     */
    private LocalDateTime lastSecurityReview;

    /**
     * Next security review
     */
    private LocalDateTime nextSecurityReview;

    // ============================================================
    // Access Control
    // ============================================================

    /**
     * Owner
     */
    private String owner;

    /**
     * Access level
     */
    private AccessLevel accessLevel;

    /**
     * Access control enabled
     */
    private Boolean accessControlEnabled;

    /**
     * RBAC enabled (Role-Based Access Control)
     */
    private Boolean rbacEnabled;

    /**
     * ABAC enabled (Attribute-Based Access Control)
     */
    private Boolean abacEnabled;

    /**
     * Allowed roles
     */
    private List<String> allowedRoles;

    /**
     * Denied roles
     */
    private List<String> deniedRoles;

    /**
     * Allowed users
     */
    private List<String> allowedUsers;

    /**
     * Denied users
     */
    private List<String> deniedUsers;

    /**
     * Allowed departments
     */
    private List<String> allowedDepartments;

    /**
     * Allowed groups
     */
    private List<String> allowedGroups;

    /**
     * IP whitelist
     */
    private List<String> ipWhitelist;

    /**
     * IP blacklist
     */
    private List<String> ipBlacklist;

    /**
     * Require MFA (Multi-Factor Authentication)
     */
    private Boolean requireMFA;

    /**
     * Session timeout (minutes)
     */
    private Integer sessionTimeoutMinutes;

    // ============================================================
    // Encryption
    // ============================================================

    /**
     * Encryption enabled
     */
    private Boolean encryptionEnabled;

    /**
     * Encryption algorithm
     */
    private EncryptionAlgorithm encryptionAlgorithm;

    /**
     * Encrypt at rest
     */
    private Boolean encryptAtRest;

    /**
     * Encrypt in transit
     */
    private Boolean encryptInTransit;

    /**
     * Encryption key ID
     */
    private String encryptionKeyId;

    /**
     * Key rotation enabled
     */
    private Boolean keyRotationEnabled;

    /**
     * Key rotation interval (days)
     */
    private Integer keyRotationIntervalDays;

    /**
     * Last key rotation
     */
    private LocalDateTime lastKeyRotation;

    /**
     * Next key rotation
     */
    private LocalDateTime nextKeyRotation;

    // ============================================================
    // Data Masking
    // ============================================================

    /**
     * Data masking enabled
     */
    private Boolean dataMaskingEnabled;

    /**
     * Masking strategy
     */
    private MaskingStrategy maskingStrategy;

    /**
     * Masked fields
     */
    private List<String> maskedFields;

    /**
     * Masking rules
     */
    private Map<String, String> maskingRules;

    /**
     * Redaction enabled
     */
    private Boolean redactionEnabled;

    /**
     * Redacted fields
     */
    private List<String> redactedFields;

    // ============================================================
    // Audit & Logging
    // ============================================================

    /**
     * Audit logging enabled
     */
    private Boolean auditLoggingEnabled;

    /**
     * Log access attempts
     */
    private Boolean logAccessAttempts;

    /**
     * Log modifications
     */
    private Boolean logModifications;

    /**
     * Log downloads
     */
    private Boolean logDownloads;

    /**
     * Log exports
     */
    private Boolean logExports;

    /**
     * Audit retention days
     */
    private Integer auditRetentionDays;

    /**
     * Last access at
     */
    private LocalDateTime lastAccessAt;

    /**
     * Last access by
     */
    private String lastAccessBy;

    /**
     * Access count
     */
    private Long accessCount;

    /**
     * Failed access attempts
     */
    private Integer failedAccessAttempts;

    /**
     * Last failed access
     */
    private LocalDateTime lastFailedAccess;

    // ============================================================
    // Security Policies
    // ============================================================

    /**
     * Applied policies
     */
    private List<SecurityPolicy> appliedPolicies;

    /**
     * Password protected
     */
    private Boolean passwordProtected;

    /**
     * Password hash
     */
    private String passwordHash;

    /**
     * Watermark enabled
     */
    private Boolean watermarkEnabled;

    /**
     * Watermark text
     */
    private String watermarkText;

    /**
     * Download restrictions
     */
    private Boolean downloadRestricted;

    /**
     * Print restrictions
     */
    private Boolean printRestricted;

    /**
     * Copy restrictions
     */
    private Boolean copyRestricted;

    /**
     * Expiry enabled
     */
    private Boolean expiryEnabled;

    /**
     * Expires at
     */
    private LocalDateTime expiresAt;

    /**
     * Auto-delete on expiry
     */
    private Boolean autoDeleteOnExpiry;

    // ============================================================
    // Compliance
    // ============================================================

    /**
     * GDPR compliant
     */
    private Boolean gdprCompliant;

    /**
     * HIPAA compliant
     */
    private Boolean hipaaCompliant;

    /**
     * FERPA compliant
     */
    private Boolean ferpaCompliant;

    /**
     * SOX compliant
     */
    private Boolean soxCompliant;

    /**
     * PCI-DSS compliant
     */
    private Boolean pciDssCompliant;

    /**
     * Contains PII (Personally Identifiable Information)
     */
    private Boolean containsPII;

    /**
     * Contains PHI (Protected Health Information)
     */
    private Boolean containsPHI;

    /**
     * Contains financial data
     */
    private Boolean containsFinancialData;

    /**
     * Data classification
     */
    private String dataClassification;

    // ============================================================
    // Threat Detection
    // ============================================================

    /**
     * Threat detection enabled
     */
    private Boolean threatDetectionEnabled;

    /**
     * Anomaly detection enabled
     */
    private Boolean anomalyDetectionEnabled;

    /**
     * Suspicious activity count
     */
    private Integer suspiciousActivityCount;

    /**
     * Last threat detected
     */
    private LocalDateTime lastThreatDetected;

    /**
     * Blocked IPs
     */
    private List<String> blockedIPs;

    /**
     * Rate limiting enabled
     */
    private Boolean rateLimitingEnabled;

    /**
     * Max requests per minute
     */
    private Integer maxRequestsPerMinute;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Security score (0-100)
     */
    private Double securityScore;

    /**
     * Vulnerability count
     */
    private Integer vulnerabilityCount;

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom attributes
     */
    private Map<String, Object> customAttributes;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Security policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityPolicy {
        private String policyId;
        private String policyName;
        private PolicyType policyType;
        private String description;
        private Boolean enabled;
        private Integer priority;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveUntil;
        private Map<String, Object> policyRules;
    }

    /**
     * Access control entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessControlEntry {
        private String aceId;
        private String principal;
        private String principalType;
        private AccessLevel accessLevel;
        private List<String> permissions;
        private LocalDateTime grantedAt;
        private String grantedBy;
        private LocalDateTime expiresAt;
        private Boolean inherited;
    }

    /**
     * Security event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityEvent {
        private String eventId;
        private String eventType;
        private LocalDateTime eventTime;
        private String username;
        private String ipAddress;
        private String action;
        private Boolean success;
        private String failureReason;
        private String severity;
        private Map<String, Object> eventData;
    }

    /**
     * Security vulnerability
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityVulnerability {
        private String vulnerabilityId;
        private String vulnerabilityType;
        private String severity;
        private String description;
        private LocalDateTime detectedAt;
        private Boolean resolved;
        private LocalDateTime resolvedAt;
        private String remediation;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if user has access
     */
    public boolean hasAccess(String username, List<String> userRoles) {
        if (!Boolean.TRUE.equals(accessControlEnabled)) {
            return true;
        }

        // Check denied users first
        if (deniedUsers != null && deniedUsers.contains(username)) {
            return false;
        }

        // Check allowed users
        if (allowedUsers != null && allowedUsers.contains(username)) {
            return true;
        }

        // Check denied roles
        if (deniedRoles != null && userRoles != null) {
            for (String role : userRoles) {
                if (deniedRoles.contains(role)) {
                    return false;
                }
            }
        }

        // Check allowed roles
        if (allowedRoles != null && userRoles != null) {
            for (String role : userRoles) {
                if (allowedRoles.contains(role)) {
                    return true;
                }
            }
        }

        // Default deny if ACL is configured
        return (allowedUsers == null || allowedUsers.isEmpty()) &&
               (allowedRoles == null || allowedRoles.isEmpty());
    }

    /**
     * Check if IP is allowed
     */
    public boolean isIPAllowed(String ipAddress) {
        // Check blacklist first
        if (ipBlacklist != null && ipBlacklist.contains(ipAddress)) {
            return false;
        }

        // Check whitelist
        if (ipWhitelist != null && !ipWhitelist.isEmpty()) {
            return ipWhitelist.contains(ipAddress);
        }

        return true;
    }

    /**
     * Check if expired
     */
    public boolean isExpired() {
        if (!Boolean.TRUE.equals(expiryEnabled) || expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if needs security review
     */
    public boolean needsSecurityReview() {
        if (nextSecurityReview == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(nextSecurityReview);
    }

    /**
     * Check if encryption is required
     */
    public boolean requiresEncryption() {
        return Boolean.TRUE.equals(containsPII) ||
               Boolean.TRUE.equals(containsPHI) ||
               Boolean.TRUE.equals(containsFinancialData);
    }

    /**
     * Check if needs key rotation
     */
    public boolean needsKeyRotation() {
        if (!Boolean.TRUE.equals(keyRotationEnabled) || nextKeyRotation == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(nextKeyRotation);
    }

    /**
     * Calculate security score
     */
    public void calculateSecurityScore() {
        double score = 100.0;

        // Deduct points for missing security features
        if (!Boolean.TRUE.equals(encryptionEnabled)) score -= 15;
        if (!Boolean.TRUE.equals(accessControlEnabled)) score -= 15;
        if (!Boolean.TRUE.equals(auditLoggingEnabled)) score -= 10;
        if (!Boolean.TRUE.equals(dataMaskingEnabled) && Boolean.TRUE.equals(containsPII)) score -= 10;
        if (!Boolean.TRUE.equals(requireMFA)) score -= 10;
        if (!Boolean.TRUE.equals(threatDetectionEnabled)) score -= 10;

        // Deduct points for vulnerabilities
        if (vulnerabilityCount != null) {
            score -= vulnerabilityCount * 5;
        }

        // Deduct points for failed access attempts
        if (failedAccessAttempts != null && failedAccessAttempts > 10) {
            score -= Math.min(20, failedAccessAttempts);
        }

        securityScore = Math.max(0, score);
    }

    /**
     * Determine security status
     */
    public void determineSecurityStatus() {
        calculateSecurityScore();

        if (securityScore == null) {
            status = SecurityStatus.PENDING;
        } else if (securityScore >= 90) {
            status = SecurityStatus.SECURE;
        } else if (securityScore >= 70) {
            status = SecurityStatus.PARTIAL;
        } else {
            status = SecurityStatus.VULNERABLE;
        }
    }

    /**
     * Increment access count
     */
    public void incrementAccessCount() {
        accessCount = (accessCount != null ? accessCount : 0) + 1;
        lastAccessAt = LocalDateTime.now();
    }

    /**
     * Increment failed access attempts
     */
    public void incrementFailedAccess() {
        failedAccessAttempts = (failedAccessAttempts != null ? failedAccessAttempts : 0) + 1;
        lastFailedAccess = LocalDateTime.now();
    }

    /**
     * Reset failed access attempts
     */
    public void resetFailedAccess() {
        failedAccessAttempts = 0;
    }

    /**
     * Check if secure
     */
    public boolean isSecure() {
        return status == SecurityStatus.SECURE;
    }

    /**
     * Check if vulnerable
     */
    public boolean isVulnerable() {
        return status == SecurityStatus.VULNERABLE;
    }

    /**
     * Get security level
     */
    public String getSecurityLevel() {
        if (securityScore == null) {
            return "UNKNOWN";
        }

        if (securityScore >= 90) return "HIGH";
        if (securityScore >= 70) return "MEDIUM";
        if (securityScore >= 50) return "LOW";
        return "CRITICAL";
    }
}
