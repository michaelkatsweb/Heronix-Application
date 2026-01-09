package com.heronix.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Heronix-SchedulerV2 integration
 *
 * This enables the SIS to detect and integrate with the optional
 * Heronix-SchedulerV2 add-on for advanced schedule generation.
 *
 * @author Heronix Development Team
 * @since 2025-01-06
 */
@Configuration
@ConfigurationProperties(prefix = "heronix.scheduler")
@Data
public class SchedulerIntegrationProperties {

    /**
     * Enable/disable Heronix-SchedulerV2 integration
     * Default: false (disabled unless explicitly enabled)
     */
    private boolean enabled = false;

    /**
     * License key for Heronix-SchedulerV2
     * Required when enabled=true
     */
    private String licenseKey;

    /**
     * URL where Heronix-SchedulerV2 is running
     * Example: http://localhost:9090 or https://scheduler.school.edu
     */
    private String url = "http://localhost:9090";

    /**
     * Port where Heronix-SchedulerV2 is listening
     * Used for availability detection
     * Default: 9090
     */
    private int port = 9090;

    /**
     * Integration mode:
     * - SHARED_DB: Both applications share the same database
     * - API: SchedulerV2 calls SIS REST APIs to fetch data
     * - IMPORT: Data is exported from SIS and imported into SchedulerV2 (CSV/JSON)
     * - SYNC: Real-time synchronization between systems
     */
    private IntegrationMode mode = IntegrationMode.API;

    /**
     * Authentication mode:
     * - SSO: Single sign-on with token passing
     * - SEPARATE: Separate login for SchedulerV2
     */
    private AuthMode authMode = AuthMode.SSO;

    /**
     * SSO token secret for secure token generation
     * Required when authMode=SSO
     */
    private String ssoTokenSecret;

    /**
     * SSO token expiration in seconds
     * Default: 3600 (1 hour)
     */
    private int ssoTokenExpiration = 3600;

    /**
     * Enable automatic data synchronization
     * Only applies when mode=SYNC
     */
    private boolean autoSync = false;

    /**
     * Sync interval in seconds
     * Default: 300 (5 minutes)
     */
    private int syncInterval = 300;

    /**
     * Health check endpoint for SchedulerV2
     * Used to verify if SchedulerV2 is running
     */
    private String healthCheckEndpoint = "/api/health";

    /**
     * Connection timeout in milliseconds
     * Default: 5000 (5 seconds)
     */
    private int connectionTimeout = 5000;

    /**
     * Integration mode enumeration
     */
    public enum IntegrationMode {
        SHARED_DB,  // Shared database
        API,        // API integration
        IMPORT,     // CSV/JSON import
        SYNC        // Real-time sync
    }

    /**
     * Authentication mode enumeration
     */
    public enum AuthMode {
        SSO,        // Single sign-on with token
        SEPARATE    // Separate authentication
    }

    /**
     * Check if scheduler integration is properly configured
     */
    public boolean isProperlyConfigured() {
        if (!enabled) {
            return false;
        }

        // License key is required
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }

        // URL is required
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // SSO mode requires token secret
        if (authMode == AuthMode.SSO &&
            (ssoTokenSecret == null || ssoTokenSecret.trim().isEmpty())) {
            return false;
        }

        return true;
    }

    /**
     * Get the full health check URL
     */
    public String getHealthCheckUrl() {
        return url + healthCheckEndpoint;
    }
}
