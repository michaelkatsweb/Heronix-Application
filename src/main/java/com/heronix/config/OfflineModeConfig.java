package com.heronix.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Offline Mode Configuration
 *
 * Ensures complete network isolation for secure offline operation.
 * When enabled, blocks all external HTTP calls and enforces local-only resources.
 *
 * Security Features:
 * - Blocks external HTTP/HTTPS calls
 * - Enforces local-only static assets
 * - Disables external webhooks
 * - Disables email notifications
 * - Restricts to localhost communications only
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Production Hardening
 */
@Configuration
@ConfigurationProperties(prefix = "heronix.offline")
@Getter
@Setter
@Slf4j
public class OfflineModeConfig {

    /**
     * Enable strict offline mode - blocks all external network calls
     */
    private boolean enabled = true;

    /**
     * Block external HTTP calls (webhooks, external APIs)
     */
    private boolean blockExternalHttp = true;

    /**
     * Disable external state education department lookups
     */
    private boolean disableStateLookups = true;

    /**
     * Use local static assets only (no CDN)
     */
    private boolean useLocalAssets = true;

    /**
     * Disable email notifications
     */
    private boolean disableEmail = true;

    /**
     * Disable webhook delivery to external systems
     */
    private boolean disableWebhooks = true;

    /**
     * Local AI only (no cloud AI services)
     */
    private boolean localAiOnly = true;

    /**
     * Allowed hosts for HTTP calls (localhost variations)
     */
    private static final Set<String> ALLOWED_HOSTS = Set.of(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "::1",
        "host.docker.internal"
    );

    /**
     * Allowed host patterns (for internal Docker networking)
     */
    private static final List<String> ALLOWED_HOST_PATTERNS = List.of(
        "heronix-",      // Docker container names
        "172.28.",       // Docker network subnet
        "192.168.",      // Local network
        "10."            // Private network
    );

    /**
     * Configure RestTemplate with offline mode interceptor
     */
    @Bean
    public RestTemplate offlineRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        if (enabled && blockExternalHttp) {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(
                restTemplate.getInterceptors()
            );
            interceptors.add((request, body, execution) -> {
                URI uri = request.getURI();
                String host = uri.getHost();

                if (!isAllowedHost(host)) {
                    log.warn("OFFLINE MODE: Blocked external HTTP call to: {}", uri);
                    throw new SecurityException(
                        "External HTTP calls are blocked in offline mode. Target: " + uri
                    );
                }

                log.debug("OFFLINE MODE: Allowed internal call to: {}", uri);
                return execution.execute(request, body);
            });
            restTemplate.setInterceptors(interceptors);

            log.info("OFFLINE MODE: RestTemplate configured to block external calls");
        }

        return restTemplate;
    }

    /**
     * Check if a host is allowed for HTTP calls
     */
    public boolean isAllowedHost(String host) {
        if (host == null) {
            return false;
        }

        String hostLower = host.toLowerCase();

        // Check exact matches
        if (ALLOWED_HOSTS.contains(hostLower)) {
            return true;
        }

        // Check patterns
        for (String pattern : ALLOWED_HOST_PATTERNS) {
            if (hostLower.startsWith(pattern)) {
                return true;
            }
        }

        // Check if it's a numeric IP in private range
        if (isPrivateIP(host)) {
            return true;
        }

        return false;
    }

    /**
     * Check if IP address is in private range
     */
    private boolean isPrivateIP(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);

            // 10.0.0.0 - 10.255.255.255
            if (first == 10) return true;

            // 172.16.0.0 - 172.31.255.255
            if (first == 172 && second >= 16 && second <= 31) return true;

            // 192.168.0.0 - 192.168.255.255
            if (first == 192 && second == 168) return true;

            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if webhooks should be delivered
     */
    public boolean shouldDeliverWebhook(String targetUrl) {
        if (!enabled) {
            return true;
        }

        if (disableWebhooks) {
            log.debug("OFFLINE MODE: Webhook delivery disabled");
            return false;
        }

        try {
            URI uri = URI.create(targetUrl);
            return isAllowedHost(uri.getHost());
        } catch (Exception e) {
            log.warn("OFFLINE MODE: Invalid webhook URL: {}", targetUrl);
            return false;
        }
    }

    /**
     * Check if email should be sent
     */
    public boolean shouldSendEmail() {
        if (!enabled) {
            return true;
        }
        return !disableEmail;
    }

    /**
     * Check if state education lookups are allowed
     */
    public boolean shouldLookupStateEducation() {
        if (!enabled) {
            return true;
        }
        return !disableStateLookups;
    }

    /**
     * Log offline mode status on startup
     */
    @jakarta.annotation.PostConstruct
    public void logOfflineModeStatus() {
        if (enabled) {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("OFFLINE MODE ENABLED - Network isolation active");
            log.info("═══════════════════════════════════════════════════════════");
            log.info("  Block external HTTP: {}", blockExternalHttp);
            log.info("  Disable state lookups: {}", disableStateLookups);
            log.info("  Use local assets: {}", useLocalAssets);
            log.info("  Disable email: {}", disableEmail);
            log.info("  Disable webhooks: {}", disableWebhooks);
            log.info("  Local AI only: {}", localAiOnly);
            log.info("═══════════════════════════════════════════════════════════");
        } else {
            log.info("OFFLINE MODE: Disabled - external network calls allowed");
        }
    }
}
