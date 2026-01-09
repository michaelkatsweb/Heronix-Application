package com.heronix.service;

import com.heronix.config.SchedulerIntegrationProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing integration with Heronix-SchedulerV2
 *
 * This service handles:
 * - Detecting if SchedulerV2 is available
 * - Validating license keys
 * - Generating SSO tokens
 * - Managing data synchronization
 *
 * @author Heronix Development Team
 * @since 2025-01-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerIntegrationService {

    private final SchedulerIntegrationProperties schedulerProperties;

    // Cache for availability check (avoid checking every time)
    private Boolean cachedAvailability = null;
    private long lastAvailabilityCheck = 0;
    private static final long AVAILABILITY_CACHE_MS = 30000; // 30 seconds

    /**
     * Check if Heronix-SchedulerV2 is enabled and properly configured
     */
    public boolean isSchedulerEnabled() {
        return schedulerProperties.isEnabled() && schedulerProperties.isProperlyConfigured();
    }

    /**
     * Check if Heronix-SchedulerV2 is available (running and responding)
     *
     * This method checks if the SchedulerV2 application is actually running
     * by attempting to connect to its health check endpoint.
     *
     * Results are cached for 30 seconds to avoid excessive network calls.
     */
    public boolean isSchedulerAvailable() {
        if (!isSchedulerEnabled()) {
            return false;
        }

        // Return cached result if still fresh
        long now = System.currentTimeMillis();
        if (cachedAvailability != null && (now - lastAvailabilityCheck) < AVAILABILITY_CACHE_MS) {
            return cachedAvailability;
        }

        // Perform actual availability check
        boolean available = checkSchedulerAvailability();
        cachedAvailability = available;
        lastAvailabilityCheck = now;

        return available;
    }

    /**
     * Actually perform the availability check by connecting to SchedulerV2
     */
    private boolean checkSchedulerAvailability() {
        try {
            String healthUrl = schedulerProperties.getHealthCheckUrl();
            log.debug("Checking SchedulerV2 availability at: {}", healthUrl);

            URL url = URI.create(healthUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(schedulerProperties.getConnectionTimeout());
            connection.setReadTimeout(schedulerProperties.getConnectionTimeout());

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            boolean available = (responseCode >= 200 && responseCode < 300);
            log.debug("SchedulerV2 availability check result: {} (HTTP {})", available, responseCode);

            return available;

        } catch (IOException e) {
            log.debug("SchedulerV2 is not available: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error checking SchedulerV2 availability", e);
            return false;
        }
    }

    /**
     * Force refresh of availability status (clear cache)
     */
    public void refreshAvailability() {
        cachedAvailability = null;
        lastAvailabilityCheck = 0;
    }

    /**
     * Get the URL for launching/redirecting to Heronix-SchedulerV2
     *
     * @param username Current logged-in user's username
     * @param userRole Current user's role
     * @return URL to redirect to SchedulerV2 (with SSO token if configured)
     */
    public String getSchedulerLaunchUrl(String username, String userRole) {
        if (!isSchedulerEnabled()) {
            return null;
        }

        String baseUrl = schedulerProperties.getUrl();

        // If using SSO authentication, append token
        if (schedulerProperties.getAuthMode() == SchedulerIntegrationProperties.AuthMode.SSO) {
            String token = generateSsoToken(username, userRole);
            return baseUrl + "/sso?token=" + token;
        }

        // Otherwise, just return the base URL (separate login)
        return baseUrl;
    }

    /**
     * Generate SSO token for seamless authentication to SchedulerV2
     *
     * @param username User's username
     * @param userRole User's role
     * @return JWT token for SSO
     */
    private String generateSsoToken(String username, String userRole) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", username);
            claims.put("role", userRole);
            claims.put("source", "heronix-sis");

            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(schedulerProperties.getSsoTokenExpiration());

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .signWith(SignatureAlgorithm.HS256, schedulerProperties.getSsoTokenSecret())
                    .compact();

        } catch (Exception e) {
            log.error("Error generating SSO token", e);
            throw new RuntimeException("Failed to generate SSO token", e);
        }
    }

    /**
     * Get status message for UI display
     */
    public String getStatusMessage() {
        if (!schedulerProperties.isEnabled()) {
            return "Schedule Generation requires Heronix-SchedulerV2 add-on.\n" +
                   "Contact your administrator or visit https://heronix.com to purchase.";
        }

        if (!schedulerProperties.isProperlyConfigured()) {
            return "Heronix-SchedulerV2 is enabled but not properly configured.\n" +
                   "Please check your application.properties configuration.";
        }

        if (!isSchedulerAvailable()) {
            return "Heronix-SchedulerV2 is configured but not currently running.\n" +
                   "Please start the SchedulerV2 application at: " + schedulerProperties.getUrl();
        }

        return "Heronix-SchedulerV2 is available and ready to use.";
    }

    /**
     * Get integration mode
     */
    public SchedulerIntegrationProperties.IntegrationMode getIntegrationMode() {
        return schedulerProperties.getMode();
    }

    /**
     * Get authentication mode
     */
    public SchedulerIntegrationProperties.AuthMode getAuthMode() {
        return schedulerProperties.getAuthMode();
    }

    /**
     * Validate license key (placeholder - implement actual validation logic)
     */
    public boolean validateLicenseKey() {
        if (!schedulerProperties.isEnabled()) {
            return false;
        }

        String licenseKey = schedulerProperties.getLicenseKey();
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }

        // TODO: Implement actual license validation logic
        // This could involve:
        // - Checking key format
        // - Verifying with license server
        // - Checking expiration date
        // - Validating signature

        // For now, just check if key is present and has minimum length
        return licenseKey.length() >= 20;
    }
}
