package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Integration DTO
 *
 * Represents external system integration configuration for reports.
 *
 * Features:
 * - External API integration
 * - Webhook support
 * - Data synchronization
 * - Authentication management
 * - Integration monitoring
 *
 * Supported Integration Types:
 * - REST API
 * - SOAP
 * - GraphQL
 * - Webhooks
 * - File-based (FTP, SFTP)
 * - Database connectors
 * - Cloud services (AWS, Azure, GCP)
 * - Third-party platforms (Salesforce, Google, Microsoft)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 76 - Report Integration & External APIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIntegration {

    /**
     * Integration type enumeration
     */
    public enum IntegrationType {
        REST_API,           // REST API integration
        SOAP,               // SOAP web service
        GRAPHQL,            // GraphQL API
        WEBHOOK,            // Webhook callback
        FTP,                // FTP file transfer
        SFTP,               // SFTP file transfer
        DATABASE,           // Direct database connection
        AWS_S3,             // AWS S3 storage
        AZURE_BLOB,         // Azure Blob storage
        GCP_STORAGE,        // Google Cloud Storage
        SALESFORCE,         // Salesforce integration
        GOOGLE_DRIVE,       // Google Drive
        MICROSOFT_365,      // Microsoft 365
        SLACK,              // Slack messaging
        EMAIL,              // Email delivery
        CUSTOM              // Custom integration
    }

    /**
     * Authentication type enumeration
     */
    public enum AuthenticationType {
        NONE,               // No authentication
        BASIC,              // Basic authentication
        BEARER_TOKEN,       // Bearer token
        API_KEY,            // API key
        OAUTH2,             // OAuth 2.0
        JWT,                // JSON Web Token
        HMAC,               // HMAC signature
        CERTIFICATE,        // SSL/TLS certificate
        AWS_SIGNATURE,      // AWS signature v4
        CUSTOM              // Custom authentication
    }

    /**
     * Integration status enumeration
     */
    public enum IntegrationStatus {
        ACTIVE,             // Active and working
        INACTIVE,           // Temporarily disabled
        PENDING,            // Pending setup
        FAILED,             // Failed connection
        TESTING,            // In testing mode
        DISABLED            // Permanently disabled
    }

    /**
     * Sync direction enumeration
     */
    public enum SyncDirection {
        PUSH,               // Push data to external system
        PULL,               // Pull data from external system
        BIDIRECTIONAL,      // Two-way sync
        NONE                // No sync
    }

    /**
     * Data format enumeration
     */
    public enum DataFormat {
        JSON,               // JSON format
        XML,                // XML format
        CSV,                // CSV format
        EXCEL,              // Excel format
        PDF,                // PDF format
        PLAIN_TEXT,         // Plain text
        BINARY,             // Binary data
        CUSTOM              // Custom format
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Integration ID
     */
    private Long integrationId;

    /**
     * Integration name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Integration type
     */
    private IntegrationType integrationType;

    /**
     * Integration status
     */
    private IntegrationStatus status;

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
     * Updated by
     */
    private String updatedBy;

    /**
     * Report ID (if specific to a report)
     */
    private Long reportId;

    /**
     * Global integration (applies to all reports)
     */
    private Boolean isGlobal;

    // ============================================================
    // Connection Configuration
    // ============================================================

    /**
     * Endpoint URL
     */
    private String endpointUrl;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * API version
     */
    private String apiVersion;

    /**
     * Timeout (milliseconds)
     */
    private Integer timeoutMs;

    /**
     * Retry attempts
     */
    private Integer retryAttempts;

    /**
     * Retry delay (milliseconds)
     */
    private Integer retryDelayMs;

    /**
     * Connection pool size
     */
    private Integer connectionPoolSize;

    /**
     * Custom headers
     */
    private Map<String, String> headers;

    /**
     * Query parameters
     */
    private Map<String, String> queryParams;

    // ============================================================
    // Authentication
    // ============================================================

    /**
     * Authentication type
     */
    private AuthenticationType authenticationType;

    /**
     * Username (for basic auth)
     */
    private String username;

    /**
     * Password (encrypted)
     */
    private String password;

    /**
     * API key
     */
    private String apiKey;

    /**
     * API secret
     */
    private String apiSecret;

    /**
     * Access token
     */
    private String accessToken;

    /**
     * Refresh token
     */
    private String refreshToken;

    /**
     * Token expires at
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * OAuth client ID
     */
    private String oauthClientId;

    /**
     * OAuth client secret
     */
    private String oauthClientSecret;

    /**
     * OAuth scopes
     */
    private List<String> oauthScopes;

    /**
     * Certificate path
     */
    private String certificatePath;

    /**
     * Certificate password
     */
    private String certificatePassword;

    // ============================================================
    // Data Synchronization
    // ============================================================

    /**
     * Sync direction
     */
    private SyncDirection syncDirection;

    /**
     * Sync enabled
     */
    private Boolean syncEnabled;

    /**
     * Auto sync enabled
     */
    private Boolean autoSyncEnabled;

    /**
     * Sync interval (minutes)
     */
    private Integer syncIntervalMinutes;

    /**
     * Last sync at
     */
    private LocalDateTime lastSyncAt;

    /**
     * Next sync at
     */
    private LocalDateTime nextSyncAt;

    /**
     * Sync status
     */
    private String syncStatus;

    /**
     * Records synced
     */
    private Long recordsSynced;

    /**
     * Sync errors
     */
    private Integer syncErrors;

    /**
     * Data format
     */
    private DataFormat dataFormat;

    /**
     * Field mappings
     */
    private Map<String, String> fieldMappings;

    /**
     * Transform script
     */
    private String transformScript;

    // ============================================================
    // Webhooks
    // ============================================================

    /**
     * Webhook URL
     */
    private String webhookUrl;

    /**
     * Webhook secret
     */
    private String webhookSecret;

    /**
     * Webhook events
     */
    private List<String> webhookEvents;

    /**
     * Webhook enabled
     */
    private Boolean webhookEnabled;

    /**
     * Last webhook at
     */
    private LocalDateTime lastWebhookAt;

    /**
     * Webhook failures
     */
    private Integer webhookFailures;

    // ============================================================
    // Monitoring & Health
    // ============================================================

    /**
     * Health status
     */
    private String healthStatus;

    /**
     * Last health check
     */
    private LocalDateTime lastHealthCheck;

    /**
     * Response time (ms)
     */
    private Long responseTimeMs;

    /**
     * Success rate (percentage)
     */
    private Double successRate;

    /**
     * Total requests
     */
    private Long totalRequests;

    /**
     * Successful requests
     */
    private Long successfulRequests;

    /**
     * Failed requests
     */
    private Long failedRequests;

    /**
     * Last error message
     */
    private String lastErrorMessage;

    /**
     * Last error at
     */
    private LocalDateTime lastErrorAt;

    /**
     * Uptime percentage
     */
    private Double uptimePercentage;

    // ============================================================
    // Rate Limiting
    // ============================================================

    /**
     * Rate limit enabled
     */
    private Boolean rateLimitEnabled;

    /**
     * Max requests per minute
     */
    private Integer maxRequestsPerMinute;

    /**
     * Max requests per hour
     */
    private Integer maxRequestsPerHour;

    /**
     * Max requests per day
     */
    private Integer maxRequestsPerDay;

    /**
     * Current minute requests
     */
    private Integer currentMinuteRequests;

    /**
     * Current hour requests
     */
    private Integer currentHourRequests;

    /**
     * Current day requests
     */
    private Integer currentDayRequests;

    /**
     * Rate limit reset at
     */
    private LocalDateTime rateLimitResetAt;

    // ============================================================
    // Scheduling
    // ============================================================

    /**
     * Schedule enabled
     */
    private Boolean scheduleEnabled;

    /**
     * Cron expression
     */
    private String cronExpression;

    /**
     * Schedule timezone
     */
    private String scheduleTimezone;

    /**
     * Last scheduled run
     */
    private LocalDateTime lastScheduledRun;

    /**
     * Next scheduled run
     */
    private LocalDateTime nextScheduledRun;

    /**
     * Schedule failures
     */
    private Integer scheduleFailures;

    // ============================================================
    // Security
    // ============================================================

    /**
     * SSL/TLS enabled
     */
    private Boolean sslEnabled;

    /**
     * Verify SSL certificate
     */
    private Boolean verifySslCertificate;

    /**
     * IP whitelist
     */
    private List<String> ipWhitelist;

    /**
     * Encryption enabled
     */
    private Boolean encryptionEnabled;

    /**
     * Encryption algorithm
     */
    private String encryptionAlgorithm;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Configuration
     */
    private Map<String, Object> configuration;

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
     * Integration request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntegrationRequest {
        private String requestId;
        private LocalDateTime requestedAt;
        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;
        private Integer statusCode;
        private String response;
        private Long responseTimeMs;
        private Boolean successful;
        private String errorMessage;
    }

    /**
     * Integration event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntegrationEvent {
        private String eventId;
        private String eventType;
        private LocalDateTime eventTime;
        private String description;
        private Map<String, Object> eventData;
        private String severity;
    }

    /**
     * Field mapping
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMapping {
        private String sourceField;
        private String targetField;
        private String dataType;
        private String defaultValue;
        private Boolean required;
        private String transformFunction;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if integration is active
     */
    public boolean isActive() {
        return status == IntegrationStatus.ACTIVE;
    }

    /**
     * Check if authentication is required
     */
    public boolean requiresAuthentication() {
        return authenticationType != null && authenticationType != AuthenticationType.NONE;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        if (tokenExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    /**
     * Check if sync is due
     */
    public boolean isSyncDue() {
        if (!Boolean.TRUE.equals(autoSyncEnabled) || !Boolean.TRUE.equals(syncEnabled)) {
            return false;
        }

        if (nextSyncAt == null) {
            return true;
        }

        return LocalDateTime.now().isAfter(nextSyncAt);
    }

    /**
     * Calculate next sync time
     */
    public void calculateNextSync() {
        if (syncIntervalMinutes != null && syncIntervalMinutes > 0) {
            nextSyncAt = LocalDateTime.now().plusMinutes(syncIntervalMinutes);
        }
    }

    /**
     * Check if rate limit exceeded
     */
    public boolean isRateLimitExceeded() {
        if (!Boolean.TRUE.equals(rateLimitEnabled)) {
            return false;
        }

        if (maxRequestsPerMinute != null && currentMinuteRequests != null) {
            if (currentMinuteRequests >= maxRequestsPerMinute) {
                return true;
            }
        }

        if (maxRequestsPerHour != null && currentHourRequests != null) {
            if (currentHourRequests >= maxRequestsPerHour) {
                return true;
            }
        }

        if (maxRequestsPerDay != null && currentDayRequests != null) {
            if (currentDayRequests >= maxRequestsPerDay) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update health status
     */
    public void updateHealthStatus(boolean healthy, String message) {
        healthStatus = healthy ? "HEALTHY" : "UNHEALTHY";
        lastHealthCheck = LocalDateTime.now();

        if (!healthy) {
            lastErrorMessage = message;
            lastErrorAt = LocalDateTime.now();
        }
    }

    /**
     * Calculate success rate
     */
    public void calculateSuccessRate() {
        if (totalRequests != null && totalRequests > 0 && successfulRequests != null) {
            successRate = (successfulRequests.doubleValue() / totalRequests) * 100.0;
        }
    }

    /**
     * Increment request counters
     */
    public void incrementRequestCounters(boolean successful) {
        totalRequests = (totalRequests != null ? totalRequests : 0) + 1;

        if (successful) {
            successfulRequests = (successfulRequests != null ? successfulRequests : 0) + 1;
        } else {
            failedRequests = (failedRequests != null ? failedRequests : 0) + 1;
        }

        if (Boolean.TRUE.equals(rateLimitEnabled)) {
            currentMinuteRequests = (currentMinuteRequests != null ? currentMinuteRequests : 0) + 1;
            currentHourRequests = (currentHourRequests != null ? currentHourRequests : 0) + 1;
            currentDayRequests = (currentDayRequests != null ? currentDayRequests : 0) + 1;
        }

        calculateSuccessRate();
    }

    /**
     * Reset rate limit counters
     */
    public void resetRateLimitCounters() {
        currentMinuteRequests = 0;
        currentHourRequests = 0;
        currentDayRequests = 0;
        rateLimitResetAt = LocalDateTime.now();
    }

    /**
     * Check if needs health check
     */
    public boolean needsHealthCheck() {
        if (lastHealthCheck == null) {
            return true;
        }

        // Health check every 5 minutes
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return lastHealthCheck.isBefore(fiveMinutesAgo);
    }

    /**
     * Get connection URL
     */
    public String getConnectionUrl() {
        if (endpointUrl != null && !endpointUrl.isEmpty()) {
            return endpointUrl;
        }

        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }

        return null;
    }

    /**
     * Check if supports webhooks
     */
    public boolean supportsWebhooks() {
        return integrationType == IntegrationType.WEBHOOK ||
               (webhookUrl != null && !webhookUrl.isEmpty());
    }

    /**
     * Check if supports sync
     */
    public boolean supportsSync() {
        return syncDirection != null && syncDirection != SyncDirection.NONE;
    }
}
