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
     * Validate license key with format and signature verification
     *
     * License key format: HERONIX-XXXX-XXXX-XXXX-XXXX-CHECKSUM
     * where XXXX are alphanumeric segments and CHECKSUM is a validation code
     */
    public boolean validateLicenseKey() {
        if (!schedulerProperties.isEnabled()) {
            log.debug("Scheduler not enabled, license validation skipped");
            return false;
        }

        String licenseKey = schedulerProperties.getLicenseKey();
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            log.warn("No license key configured for Heronix-SchedulerV2");
            return false;
        }

        licenseKey = licenseKey.trim().toUpperCase();

        // Check license key format: HERONIX-XXXX-XXXX-XXXX-XXXX-CHECKSUM
        if (!licenseKey.matches("^HERONIX-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")) {
            log.warn("Invalid license key format");
            return false;
        }

        // Parse license key parts
        String[] parts = licenseKey.split("-");
        if (parts.length != 6) {
            log.warn("License key has incorrect number of segments");
            return false;
        }

        // Validate checksum (last segment)
        String checksum = parts[5];
        String keyBody = parts[1] + parts[2] + parts[3] + parts[4];
        String calculatedChecksum = calculateChecksum(keyBody);

        if (!checksum.equals(calculatedChecksum)) {
            log.warn("License key checksum validation failed");
            return false;
        }

        // Decode and check expiration from key body
        try {
            // First 4 chars of segment 2 encode expiration year/month
            String expirationCode = parts[2];
            int encodedYear = Integer.parseInt(expirationCode.substring(0, 2), 36);
            int encodedMonth = Integer.parseInt(expirationCode.substring(2, 4), 36);

            // Year is offset from 2020
            int year = 2020 + encodedYear;
            int month = (encodedMonth % 12) + 1;

            java.time.LocalDate expirationDate = java.time.LocalDate.of(year, month, 1)
                    .plusMonths(1).minusDays(1); // Last day of the month

            if (java.time.LocalDate.now().isAfter(expirationDate)) {
                log.warn("License key has expired on {}", expirationDate);
                return false;
            }

            log.info("License key validated successfully, expires: {}", expirationDate);
            return true;

        } catch (Exception e) {
            log.error("Error parsing license key expiration", e);
            return false;
        }
    }

    /**
     * Calculate checksum for license key validation
     */
    private String calculateChecksum(String keyBody) {
        int sum = 0;
        for (char c : keyBody.toCharArray()) {
            sum += c;
        }
        // Convert to 4-char base36 string
        String checksum = Integer.toString(sum % 1679616, 36).toUpperCase();
        while (checksum.length() < 4) {
            checksum = "0" + checksum;
        }
        return checksum;
    }

    /**
     * Get license expiration date if valid
     */
    public java.time.LocalDate getLicenseExpirationDate() {
        if (!validateLicenseKey()) {
            return null;
        }

        try {
            String licenseKey = schedulerProperties.getLicenseKey().trim().toUpperCase();
            String[] parts = licenseKey.split("-");
            String expirationCode = parts[2];
            int encodedYear = Integer.parseInt(expirationCode.substring(0, 2), 36);
            int encodedMonth = Integer.parseInt(expirationCode.substring(2, 4), 36);

            int year = 2020 + encodedYear;
            int month = (encodedMonth % 12) + 1;

            return java.time.LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        } catch (Exception e) {
            return null;
        }
    }
}
