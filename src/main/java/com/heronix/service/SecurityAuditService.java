package com.heronix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Audit Logging Service
 *
 * Provides comprehensive security event logging and auditing for the Heronix API.
 * Tracks authentication attempts, authorization failures, and security-sensitive operations.
 *
 * Audit Event Types:
 * - Authentication: Login attempts, API key usage, JWT token validation
 * - Authorization: Permission denials, role checks, scope violations
 * - API Access: Request tracking, rate limit violations
 * - Data Access: Sensitive data retrieval, modification attempts
 * - Security Events: Suspicious activity, brute force attempts, anomalies
 *
 * Logged Information:
 * - Timestamp (ISO 8601 format)
 * - Event type and severity
 * - User/API key identifier
 * - IP address and user agent
 * - Resource accessed
 * - Action attempted
 * - Success/failure status
 * - Error details (if applicable)
 *
 * Log Levels:
 * - INFO: Successful operations
 * - WARN: Authorization failures, unusual activity
 * - ERROR: Authentication failures, security violations
 *
 * Storage:
 * - Currently logs to application log files
 * - Future: Database storage for advanced querying
 * - Integration with SIEM systems via syslog
 *
 * Compliance:
 * - FERPA: Student data access tracking
 * - SOC 2: Security event monitoring
 * - PCI DSS: Access control logging (if payment data)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Service
@Slf4j
public class SecurityAuditService {

    /**
     * Log successful authentication
     *
     * @param userId User identifier
     * @param authMethod Authentication method (API_KEY, JWT, PASSWORD)
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     */
    public void logAuthenticationSuccess(String userId, String authMethod,
                                        String ipAddress, String userAgent) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "AUTHENTICATION_SUCCESS");
        details.put("userId", userId);
        details.put("authMethod", authMethod);
        details.put("ipAddress", ipAddress);
        details.put("userAgent", userAgent);

        log.info("Authentication success: user={}, method={}, ip={}",
            userId, authMethod, ipAddress);
    }

    /**
     * Log failed authentication attempt
     *
     * @param identifier User/API key identifier (may be invalid)
     * @param authMethod Authentication method attempted
     * @param ipAddress Client IP address
     * @param reason Failure reason
     */
    public void logAuthenticationFailure(String identifier, String authMethod,
                                        String ipAddress, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "AUTHENTICATION_FAILURE");
        details.put("identifier", identifier);
        details.put("authMethod", authMethod);
        details.put("ipAddress", ipAddress);
        details.put("reason", reason);

        log.warn("Authentication failure: identifier={}, method={}, ip={}, reason={}",
            identifier, authMethod, ipAddress, reason);
    }

    /**
     * Log authorization denial
     *
     * @param userId User identifier
     * @param resource Resource being accessed
     * @param action Action attempted
     * @param requiredPermission Permission required
     * @param ipAddress Client IP address
     */
    public void logAuthorizationDenied(String userId, String resource, String action,
                                      String requiredPermission, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "AUTHORIZATION_DENIED");
        details.put("userId", userId);
        details.put("resource", resource);
        details.put("action", action);
        details.put("requiredPermission", requiredPermission);
        details.put("ipAddress", ipAddress);

        log.warn("Authorization denied: user={}, resource={}, action={}, permission={}, ip={}",
            userId, resource, action, requiredPermission, ipAddress);
    }

    /**
     * Log API key usage
     *
     * @param apiKeyId API key ID
     * @param apiKeyName API key name
     * @param userId User who owns the key
     * @param endpoint API endpoint accessed
     * @param httpMethod HTTP method (GET, POST, etc.)
     * @param ipAddress Client IP address
     * @param statusCode HTTP response status code
     */
    public void logApiKeyUsage(Long apiKeyId, String apiKeyName, String userId,
                              String endpoint, String httpMethod, String ipAddress,
                              int statusCode) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "API_KEY_USAGE");
        details.put("apiKeyId", apiKeyId);
        details.put("apiKeyName", apiKeyName);
        details.put("userId", userId);
        details.put("endpoint", endpoint);
        details.put("httpMethod", httpMethod);
        details.put("ipAddress", ipAddress);
        details.put("statusCode", statusCode);

        log.info("API key usage: key={}, user={}, endpoint={} {}, ip={}, status={}",
            apiKeyName, userId, httpMethod, endpoint, ipAddress, statusCode);
    }

    /**
     * Log rate limit violation
     *
     * @param identifier User/API key identifier
     * @param endpoint Endpoint accessed
     * @param currentCount Current request count
     * @param limit Rate limit
     * @param ipAddress Client IP address
     */
    public void logRateLimitViolation(String identifier, String endpoint,
                                     long currentCount, long limit, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "RATE_LIMIT_VIOLATION");
        details.put("identifier", identifier);
        details.put("endpoint", endpoint);
        details.put("currentCount", currentCount);
        details.put("limit", limit);
        details.put("ipAddress", ipAddress);

        log.warn("Rate limit violated: identifier={}, endpoint={}, count={}/{}, ip={}",
            identifier, endpoint, currentCount, limit, ipAddress);
    }

    /**
     * Log sensitive data access
     *
     * @param userId User accessing data
     * @param dataType Type of sensitive data (STUDENT_RECORD, GRADE, etc.)
     * @param recordId Record identifier
     * @param action Action performed (VIEW, EXPORT, MODIFY)
     * @param ipAddress Client IP address
     */
    public void logSensitiveDataAccess(String userId, String dataType, String recordId,
                                      String action, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "SENSITIVE_DATA_ACCESS");
        details.put("userId", userId);
        details.put("dataType", dataType);
        details.put("recordId", recordId);
        details.put("action", action);
        details.put("ipAddress", ipAddress);

        log.info("Sensitive data access: user={}, type={}, record={}, action={}, ip={}",
            userId, dataType, recordId, action, ipAddress);
    }

    /**
     * Log security event (anomaly, suspicious activity)
     *
     * @param eventType Type of security event
     * @param severity Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     * @param description Event description
     * @param identifier Associated user/API key
     * @param ipAddress Client IP address
     */
    public void logSecurityEvent(String eventType, String severity, String description,
                                String identifier, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("eventType", "SECURITY_EVENT");
        details.put("securityEventType", eventType);
        details.put("severity", severity);
        details.put("description", description);
        details.put("identifier", identifier);
        details.put("ipAddress", ipAddress);

        switch (severity.toUpperCase()) {
            case "CRITICAL", "HIGH" ->
                log.error("Security event [{}]: {} - identifier={}, ip={}",
                    severity, description, identifier, ipAddress);
            case "MEDIUM" ->
                log.warn("Security event [{}]: {} - identifier={}, ip={}",
                    severity, description, identifier, ipAddress);
            default ->
                log.info("Security event [{}]: {} - identifier={}, ip={}",
                    severity, description, identifier, ipAddress);
        }
    }

    /**
     * Log API key creation
     *
     * @param apiKeyId Created API key ID
     * @param apiKeyName API key name
     * @param createdBy User who created the key
     * @param scopes Granted scopes
     * @param ipAddress Client IP address
     */
    public void logApiKeyCreated(Long apiKeyId, String apiKeyName, String createdBy,
                                java.util.Set<String> scopes, String ipAddress) {
        log.info("API key created: id={}, name={}, by={}, scopes={}, ip={}",
            apiKeyId, apiKeyName, createdBy, scopes.size(), ipAddress);
    }

    /**
     * Log API key revocation
     *
     * @param apiKeyId Revoked API key ID
     * @param apiKeyName API key name
     * @param revokedBy User who revoked the key
     * @param ipAddress Client IP address
     */
    public void logApiKeyRevoked(Long apiKeyId, String apiKeyName, String revokedBy,
                                String ipAddress) {
        log.warn("API key revoked: id={}, name={}, by={}, ip={}",
            apiKeyId, apiKeyName, revokedBy, ipAddress);
    }

    /**
     * Log IP whitelist violation
     *
     * @param apiKeyName API key name
     * @param attemptedIp IP address that attempted access
     * @param allowedIps Whitelisted IP addresses
     */
    public void logIpWhitelistViolation(String apiKeyName, String attemptedIp,
                                       java.util.Set<String> allowedIps) {
        log.warn("IP whitelist violation: key={}, attempted_ip={}, allowed_ips={}",
            apiKeyName, attemptedIp, allowedIps);
    }
}
