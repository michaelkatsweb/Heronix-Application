package com.heronix.service;

import com.heronix.dto.ReportSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Security Service
 *
 * Manages security and access control for reports.
 *
 * Features:
 * - Role-based access control (RBAC)
 * - Attribute-based access control (ABAC)
 * - Data masking and encryption
 * - Audit logging
 * - Threat detection
 * - Security policies
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 79 - Report Security & Access Control
 */
@Service
@Slf4j
public class ReportSecurityService {

    private final Map<Long, ReportSecurity> securityConfigs = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportSecurity.AccessControlEntry>> accessControlLists = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportSecurity.SecurityEvent>> securityEvents = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportSecurity.SecurityVulnerability>> vulnerabilities = new ConcurrentHashMap<>();
    private Long nextSecurityId = 1L;

    /**
     * Create security configuration
     */
    public ReportSecurity createSecurityConfig(ReportSecurity security) {
        synchronized (this) {
            security.setSecurityId(nextSecurityId++);
            security.setCreatedAt(LocalDateTime.now());
            security.setAccessCount(0L);
            security.setFailedAccessAttempts(0);
            security.setSuspiciousActivityCount(0);
            security.setVulnerabilityCount(0);

            // Set defaults
            if (security.getAccessControlEnabled() == null) {
                security.setAccessControlEnabled(true);
            }

            if (security.getAuditLoggingEnabled() == null) {
                security.setAuditLoggingEnabled(true);
            }

            // Calculate initial security score
            security.determineSecurityStatus();

            securityConfigs.put(security.getSecurityId(), security);

            log.info("Created security config {} for report {}",
                    security.getSecurityId(), security.getReportId());

            logSecurityEvent(security.getSecurityId(), "SECURITY_CONFIG_CREATED",
                    security.getCreatedBy(), null, "Security configuration created", true);

            return security;
        }
    }

    /**
     * Get security configuration
     */
    public Optional<ReportSecurity> getSecurityConfig(Long securityId) {
        return Optional.ofNullable(securityConfigs.get(securityId));
    }

    /**
     * Get security configuration by report ID
     */
    public Optional<ReportSecurity> getSecurityConfigByReportId(Long reportId) {
        return securityConfigs.values().stream()
                .filter(s -> reportId.equals(s.getReportId()))
                .findFirst();
    }

    /**
     * Update security configuration
     */
    public ReportSecurity updateSecurityConfig(Long securityId, ReportSecurity updatedSecurity) {
        ReportSecurity existing = securityConfigs.get(securityId);
        if (existing == null) {
            throw new IllegalArgumentException("Security config not found: " + securityId);
        }

        updatedSecurity.setSecurityId(securityId);
        updatedSecurity.setCreatedAt(existing.getCreatedAt());
        updatedSecurity.setCreatedBy(existing.getCreatedBy());
        updatedSecurity.setUpdatedAt(LocalDateTime.now());

        // Recalculate security score
        updatedSecurity.determineSecurityStatus();

        securityConfigs.put(securityId, updatedSecurity);

        log.info("Updated security config {}", securityId);

        logSecurityEvent(securityId, "SECURITY_CONFIG_UPDATED",
                "system", null, "Security configuration updated", true);

        return updatedSecurity;
    }

    /**
     * Check access permission
     */
    public boolean checkAccess(Long securityId, String username, List<String> userRoles, String ipAddress) {
        ReportSecurity security = securityConfigs.get(securityId);
        if (security == null) {
            log.warn("Security config not found: {}", securityId);
            return false;
        }

        // Check if expired
        if (security.isExpired()) {
            log.warn("Report expired: {}", security.getReportId());
            logSecurityEvent(securityId, "ACCESS_DENIED", username, ipAddress,
                    "Report expired", false);
            return false;
        }

        // Check IP address
        if (!security.isIPAllowed(ipAddress)) {
            log.warn("IP address blocked: {}", ipAddress);
            security.incrementFailedAccess();
            logSecurityEvent(securityId, "ACCESS_DENIED", username, ipAddress,
                    "IP address not allowed", false);
            return false;
        }

        // Check user access
        if (!security.hasAccess(username, userRoles)) {
            log.warn("Access denied for user: {}", username);
            security.incrementFailedAccess();
            logSecurityEvent(securityId, "ACCESS_DENIED", username, ipAddress,
                    "Insufficient permissions", false);
            return false;
        }

        // Access granted
        security.incrementAccessCount();
        security.setLastAccessBy(username);
        security.resetFailedAccess();

        logSecurityEvent(securityId, "ACCESS_GRANTED", username, ipAddress,
                "Access granted", true);

        log.info("Access granted to user {} for report {}", username, security.getReportId());

        return true;
    }

    /**
     * Mask sensitive data
     */
    public String maskData(Long securityId, String fieldName, String value) {
        ReportSecurity security = securityConfigs.get(securityId);
        if (security == null || !Boolean.TRUE.equals(security.getDataMaskingEnabled())) {
            return value;
        }

        if (security.getMaskedFields() == null || !security.getMaskedFields().contains(fieldName)) {
            return value;
        }

        ReportSecurity.MaskingStrategy strategy = security.getMaskingStrategy();
        if (strategy == null) {
            strategy = ReportSecurity.MaskingStrategy.PARTIAL;
        }

        return applyMasking(value, strategy);
    }

    /**
     * Apply masking strategy
     */
    private String applyMasking(String value, ReportSecurity.MaskingStrategy strategy) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (strategy) {
            case FULL -> "*".repeat(value.length());
            case PARTIAL -> {
                int len = value.length();
                if (len <= 4) yield "*".repeat(len);
                int visibleChars = Math.max(2, len / 4);
                yield value.substring(0, visibleChars) + "*".repeat(len - visibleChars);
            }
            case FIRST_LAST -> {
                int len = value.length();
                if (len <= 2) yield value;
                yield value.charAt(0) + "*".repeat(len - 2) + value.charAt(len - 1);
            }
            case EMAIL -> {
                int atIndex = value.indexOf('@');
                if (atIndex <= 0) yield value;
                String username = value.substring(0, atIndex);
                String domain = value.substring(atIndex);
                if (username.length() <= 2) yield username + domain;
                yield username.substring(0, 2) + "*".repeat(username.length() - 2) + domain;
            }
            case PHONE -> {
                String digits = value.replaceAll("[^0-9]", "");
                if (digits.length() < 4) yield value;
                yield "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
            }
            case SSN -> {
                String digits = value.replaceAll("[^0-9]", "");
                if (digits.length() != 9) yield value;
                yield "***-**-" + digits.substring(5);
            }
            case CREDIT_CARD -> {
                String digits = value.replaceAll("[^0-9]", "");
                if (digits.length() < 4) yield value;
                yield "*".repeat(digits.length() - 4) + " " + digits.substring(digits.length() - 4);
            }
            default -> value;
        };
    }

    /**
     * Encrypt data
     */
    public String encryptData(Long securityId, String data) {
        ReportSecurity security = securityConfigs.get(securityId);
        if (security == null || !Boolean.TRUE.equals(security.getEncryptionEnabled())) {
            return data;
        }

        // Simulate encryption (in real implementation, use actual encryption)
        String encrypted = Base64.getEncoder().encodeToString(data.getBytes());

        log.debug("Encrypted data using {}", security.getEncryptionAlgorithm());

        return encrypted;
    }

    /**
     * Decrypt data
     */
    public String decryptData(Long securityId, String encryptedData) {
        ReportSecurity security = securityConfigs.get(securityId);
        if (security == null || !Boolean.TRUE.equals(security.getEncryptionEnabled())) {
            return encryptedData;
        }

        // Simulate decryption (in real implementation, use actual decryption)
        String decrypted = new String(Base64.getDecoder().decode(encryptedData));

        log.debug("Decrypted data using {}", security.getEncryptionAlgorithm());

        return decrypted;
    }

    /**
     * Add access control entry
     */
    public void addAccessControlEntry(Long securityId, ReportSecurity.AccessControlEntry ace) {
        accessControlLists.computeIfAbsent(securityId, k -> new ArrayList<>()).add(ace);

        log.info("Added ACE for {} with access level {}", ace.getPrincipal(), ace.getAccessLevel());

        logSecurityEvent(securityId, "ACE_ADDED", "system", null,
                "Access control entry added for " + ace.getPrincipal(), true);
    }

    /**
     * Remove access control entry
     */
    public void removeAccessControlEntry(Long securityId, String aceId) {
        List<ReportSecurity.AccessControlEntry> acl = accessControlLists.get(securityId);
        if (acl != null) {
            acl.removeIf(ace -> ace.getAceId().equals(aceId));

            log.info("Removed ACE {}", aceId);

            logSecurityEvent(securityId, "ACE_REMOVED", "system", null,
                    "Access control entry removed", true);
        }
    }

    /**
     * Get access control list
     */
    public List<ReportSecurity.AccessControlEntry> getAccessControlList(Long securityId) {
        return accessControlLists.getOrDefault(securityId, new ArrayList<>());
    }

    /**
     * Log security event
     */
    private void logSecurityEvent(Long securityId, String eventType, String username,
                                   String ipAddress, String action, boolean success) {
        ReportSecurity.SecurityEvent event = ReportSecurity.SecurityEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .username(username)
                .ipAddress(ipAddress)
                .action(action)
                .success(success)
                .severity(success ? "INFO" : "WARNING")
                .build();

        securityEvents.computeIfAbsent(securityId, k -> new ArrayList<>()).add(event);

        // Keep only last 1000 events
        List<ReportSecurity.SecurityEvent> events = securityEvents.get(securityId);
        if (events.size() > 1000) {
            events.remove(0);
        }
    }

    /**
     * Get security events
     */
    public List<ReportSecurity.SecurityEvent> getSecurityEvents(Long securityId) {
        return securityEvents.getOrDefault(securityId, new ArrayList<>());
    }

    /**
     * Add vulnerability
     */
    public void addVulnerability(Long securityId, ReportSecurity.SecurityVulnerability vulnerability) {
        vulnerabilities.computeIfAbsent(securityId, k -> new ArrayList<>()).add(vulnerability);

        ReportSecurity security = securityConfigs.get(securityId);
        if (security != null) {
            security.setVulnerabilityCount(
                    (security.getVulnerabilityCount() != null ? security.getVulnerabilityCount() : 0) + 1);
            security.determineSecurityStatus();
        }

        log.warn("Vulnerability detected: {} ({})", vulnerability.getVulnerabilityType(),
                vulnerability.getSeverity());

        logSecurityEvent(securityId, "VULNERABILITY_DETECTED", "system", null,
                "Vulnerability: " + vulnerability.getVulnerabilityType(), false);
    }

    /**
     * Resolve vulnerability
     */
    public void resolveVulnerability(Long securityId, String vulnerabilityId) {
        List<ReportSecurity.SecurityVulnerability> vulns = vulnerabilities.get(securityId);
        if (vulns != null) {
            vulns.stream()
                    .filter(v -> v.getVulnerabilityId().equals(vulnerabilityId))
                    .findFirst()
                    .ifPresent(v -> {
                        v.setResolved(true);
                        v.setResolvedAt(LocalDateTime.now());

                        ReportSecurity security = securityConfigs.get(securityId);
                        if (security != null) {
                            security.setVulnerabilityCount(
                                    Math.max(0, security.getVulnerabilityCount() - 1));
                            security.determineSecurityStatus();
                        }

                        log.info("Resolved vulnerability: {}", vulnerabilityId);

                        logSecurityEvent(securityId, "VULNERABILITY_RESOLVED", "system", null,
                                "Vulnerability resolved", true);
                    });
        }
    }

    /**
     * Get vulnerabilities
     */
    public List<ReportSecurity.SecurityVulnerability> getVulnerabilities(Long securityId) {
        return vulnerabilities.getOrDefault(securityId, new ArrayList<>());
    }

    /**
     * Get unresolved vulnerabilities
     */
    public List<ReportSecurity.SecurityVulnerability> getUnresolvedVulnerabilities(Long securityId) {
        return getVulnerabilities(securityId).stream()
                .filter(v -> !Boolean.TRUE.equals(v.getResolved()))
                .collect(Collectors.toList());
    }

    /**
     * Perform security scan
     */
    public Map<String, Object> performSecurityScan(Long securityId) {
        ReportSecurity security = securityConfigs.get(securityId);
        if (security == null) {
            throw new IllegalArgumentException("Security config not found: " + securityId);
        }

        Map<String, Object> scanResults = new HashMap<>();

        // Check encryption
        boolean encryptionIssue = security.requiresEncryption() &&
                                  !Boolean.TRUE.equals(security.getEncryptionEnabled());
        if (encryptionIssue) {
            addVulnerability(securityId, ReportSecurity.SecurityVulnerability.builder()
                    .vulnerabilityId(UUID.randomUUID().toString())
                    .vulnerabilityType("MISSING_ENCRYPTION")
                    .severity("HIGH")
                    .description("Sensitive data not encrypted")
                    .detectedAt(LocalDateTime.now())
                    .resolved(false)
                    .remediation("Enable encryption for sensitive data")
                    .build());
        }

        // Check access control
        boolean aclIssue = !Boolean.TRUE.equals(security.getAccessControlEnabled());
        if (aclIssue) {
            addVulnerability(securityId, ReportSecurity.SecurityVulnerability.builder()
                    .vulnerabilityId(UUID.randomUUID().toString())
                    .vulnerabilityType("MISSING_ACCESS_CONTROL")
                    .severity("CRITICAL")
                    .description("Access control not enabled")
                    .detectedAt(LocalDateTime.now())
                    .resolved(false)
                    .remediation("Enable access control")
                    .build());
        }

        // Update security score
        security.determineSecurityStatus();
        security.setLastSecurityReview(LocalDateTime.now());
        security.setNextSecurityReview(LocalDateTime.now().plusMonths(3));

        scanResults.put("securityId", securityId);
        scanResults.put("scanTime", LocalDateTime.now());
        scanResults.put("securityScore", security.getSecurityScore());
        scanResults.put("securityStatus", security.getStatus());
        scanResults.put("vulnerabilitiesFound", security.getVulnerabilityCount());
        scanResults.put("encryptionIssue", encryptionIssue);
        scanResults.put("aclIssue", aclIssue);

        log.info("Security scan completed for security config {}: Score {}, Status {}",
                securityId, security.getSecurityScore(), security.getStatus());

        logSecurityEvent(securityId, "SECURITY_SCAN", "system", null,
                "Security scan completed", true);

        return scanResults;
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalConfigs", securityConfigs.size());

        long secureConfigs = securityConfigs.values().stream()
                .filter(ReportSecurity::isSecure).count();
        long vulnerableConfigs = securityConfigs.values().stream()
                .filter(ReportSecurity::isVulnerable).count();

        stats.put("secureConfigs", secureConfigs);
        stats.put("vulnerableConfigs", vulnerableConfigs);

        double avgSecurityScore = securityConfigs.values().stream()
                .filter(s -> s.getSecurityScore() != null)
                .mapToDouble(ReportSecurity::getSecurityScore)
                .average()
                .orElse(0.0);

        stats.put("averageSecurityScore", avgSecurityScore);

        long totalVulnerabilities = vulnerabilities.values().stream()
                .flatMap(List::stream)
                .filter(v -> !Boolean.TRUE.equals(v.getResolved()))
                .count();

        stats.put("totalUnresolvedVulnerabilities", totalVulnerabilities);

        return stats;
    }
}
