package com.heronix.security;

import com.heronix.model.domain.ApiKey;
import com.heronix.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API Key Authentication Filter
 *
 * Spring Security filter that authenticates requests using API keys.
 * Supports two authentication headers:
 * - X-API-Key: Custom header for API key (recommended)
 * - Authorization: Bearer {api_key} format (standard)
 *
 * Authentication Flow:
 * 1. Extract API key from request headers
 * 2. Validate API key using ApiKeyService
 * 3. Check if key is active and not expired
 * 4. Verify IP whitelist (if configured)
 * 5. Create Spring Security authentication
 * 6. Record API key usage
 *
 * Security Features:
 * - IP whitelist validation
 * - Expiration checking
 * - Active status verification
 * - Usage tracking
 * - Scope-based authorities
 *
 * Header Examples:
 * - X-API-Key: hx_live_a1b2c3d4e5f6g7h8i9j0
 * - Authorization: Bearer hx_live_a1b2c3d4e5f6g7h8i9j0
 *
 * Authentication Object:
 * - Principal: User ID from API key
 * - Authorities: Scopes as granted authorities (e.g., SCOPE_read:students)
 * - Details: Request details (IP, session)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract API key from headers
            Optional<String> apiKey = extractApiKey(request);

            if (apiKey.isPresent()) {
                // Validate API key
                Optional<ApiKey> validatedKey = apiKeyService.validateApiKey(apiKey.get());

                if (validatedKey.isPresent()) {
                    ApiKey key = validatedKey.get();

                    // Check IP whitelist
                    if (!isIpAllowed(request, key)) {
                        log.warn("API key '{}' used from unauthorized IP: {}",
                            key.getName(), getClientIp(request));
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "API key not authorized from this IP address");
                        return;
                    }

                    // Create authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        key.getUserId(),
                        null,
                        convertScopesToAuthorities(key.getScopes())
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Record usage asynchronously to avoid performance impact
                    recordUsageAsync(key.getId());

                    log.debug("Authenticated request with API key: {} (User: {})",
                        key.getName(), key.getUserId());
                } else {
                    log.warn("Invalid API key attempted from IP: {}", getClientIp(request));
                }
            }
        } catch (Exception e) {
            log.error("Error during API key authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract API key from request headers
     * Checks both X-API-Key and Authorization: Bearer headers
     */
    private Optional<String> extractApiKey(HttpServletRequest request) {
        // Check X-API-Key header first (recommended)
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return Optional.of(apiKey.trim());
        }

        // Check Authorization: Bearer header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            // Check if it's an API key (starts with hx_)
            if (token.startsWith("hx_")) {
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }

    /**
     * Check if request IP is allowed by API key whitelist
     */
    private boolean isIpAllowed(HttpServletRequest request, ApiKey apiKey) {
        // If no IP whitelist configured, allow all IPs
        if (apiKey.getIpWhitelist() == null || apiKey.getIpWhitelist().isEmpty()) {
            return true;
        }

        String clientIp = getClientIp(request);

        // Check if client IP matches any whitelisted IP/CIDR
        for (String whitelistedIp : apiKey.getIpWhitelist()) {
            if (ipMatches(clientIp, whitelistedIp)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can contain multiple IPs, use the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Check if IP matches whitelist entry (supports exact match and CIDR notation)
     *
     * STUB IMPLEMENTATION: Only supports exact IP matching
     *
     * Production CIDR Notation Implementation:
     * =========================================
     *
     * Option 1: Apache Commons IP Math (Recommended)
     * -----------------------------------------------
     * Add to pom.xml:
     * <dependency>
     *     <groupId>com.github.seancfoley</groupId>
     *     <artifactId>ipaddress</artifactId>
     *     <version>5.4.0</version>
     * </dependency>
     *
     * Implementation:
     * import inet.ipaddr.IPAddress;
     * import inet.ipaddr.IPAddressString;
     *
     * private boolean ipMatches(String clientIp, String whitelistedIp) {
     *     // Exact match
     *     if (clientIp.equals(whitelistedIp)) {
     *         return true;
     *     }
     *
     *     // CIDR notation (e.g., 192.168.1.0/24)
     *     if (whitelistedIp.contains("/")) {
     *         try {
     *             IPAddressString subnet = new IPAddressString(whitelistedIp);
     *             IPAddress clientAddress = new IPAddressString(clientIp).toAddress();
     *             return subnet.contains(clientAddress);
     *         } catch (Exception e) {
     *             log.error("Invalid CIDR notation: {}", whitelistedIp, e);
     *             return false;
     *         }
     *     }
     *     return false;
     * }
     *
     *
     * Option 2: Manual CIDR Implementation (No Dependencies)
     * -------------------------------------------------------
     * For IPv4 only, without external libraries:
     *
     * private boolean ipMatches(String clientIp, String whitelistedIp) {
     *     if (clientIp.equals(whitelistedIp)) {
     *         return true;
     *     }
     *
     *     if (whitelistedIp.contains("/")) {
     *         return matchesCidr(clientIp, whitelistedIp);
     *     }
     *     return false;
     * }
     *
     * private boolean matchesCidr(String ip, String cidr) {
     *     String[] parts = cidr.split("/");
     *     String subnet = parts[0];
     *     int prefixLength = Integer.parseInt(parts[1]);
     *
     *     try {
     *         long ipLong = ipToLong(ip);
     *         long subnetLong = ipToLong(subnet);
     *         long mask = -1L << (32 - prefixLength);
     *
     *         return (ipLong & mask) == (subnetLong & mask);
     *     } catch (Exception e) {
     *         log.error("CIDR matching error for {} in {}", ip, cidr, e);
     *         return false;
     *     }
     * }
     *
     * private long ipToLong(String ipAddress) {
     *     String[] octets = ipAddress.split("\\.");
     *     long result = 0;
     *     for (int i = 0; i < 4; i++) {
     *         result |= (Long.parseLong(octets[i]) << (24 - (8 * i)));
     *     }
     *     return result;
     * }
     *
     *
     * Option 3: Spring Framework IpAddressMatcher
     * --------------------------------------------
     * Spring Security already includes IP matching:
     *
     * import org.springframework.security.web.util.matcher.IpAddressMatcher;
     *
     * private boolean ipMatches(String clientIp, String whitelistedIp) {
     *     if (clientIp.equals(whitelistedIp)) {
     *         return true;
     *     }
     *
     *     try {
     *         IpAddressMatcher matcher = new IpAddressMatcher(whitelistedIp);
     *         return matcher.matches(clientIp);
     *     } catch (Exception e) {
     *         log.error("IP matching error for {} against {}", clientIp, whitelistedIp, e);
     *         return false;
     *     }
     * }
     *
     * Example CIDR patterns supported:
     * - 192.168.1.100 (exact IP)
     * - 192.168.1.0/24 (subnet: 192.168.1.0 - 192.168.1.255)
     * - 10.0.0.0/8 (large subnet: 10.0.0.0 - 10.255.255.255)
     * - 172.16.0.0/12 (private network range)
     * - 2001:db8::/32 (IPv6 support in Options 1 and 3)
     */
    private boolean ipMatches(String clientIp, String whitelistedIp) {
        // Exact match
        if (clientIp.equals(whitelistedIp)) {
            return true;
        }

        // CIDR notation support using Spring Security's IpAddressMatcher
        // Supports both IPv4 and IPv6, including CIDR patterns like:
        // - 192.168.1.0/24 (subnet: 192.168.1.0 - 192.168.1.255)
        // - 10.0.0.0/8 (large subnet: 10.0.0.0 - 10.255.255.255)
        // - 2001:db8::/32 (IPv6 support)
        try {
            org.springframework.security.web.util.matcher.IpAddressMatcher matcher =
                new org.springframework.security.web.util.matcher.IpAddressMatcher(whitelistedIp);
            boolean matches = matcher.matches(clientIp);
            if (matches) {
                log.debug("Client IP {} matched whitelist entry {} (CIDR notation)", clientIp, whitelistedIp);
            }
            return matches;
        } catch (IllegalArgumentException e) {
            log.error("Invalid IP address pattern in whitelist: {}. Error: {}", whitelistedIp, e.getMessage());
            return false;
        }
    }

    /**
     * Convert API key scopes to Spring Security authorities
     */
    private List<SimpleGrantedAuthority> convertScopesToAuthorities(java.util.Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }

        return scopes.stream()
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
            .collect(Collectors.toList());
    }

    /**
     * Record API key usage asynchronously
     */
    private void recordUsageAsync(Long apiKeyId) {
        // Simple async execution - production should use @Async or thread pool
        CompletableFutureHelper.runAsync(() -> apiKeyService.recordUsage(apiKeyId));
    }

    /**
     * Helper class for async execution
     */
    private static class CompletableFutureHelper {
        static void runAsync(Runnable task) {
            new Thread(task).start();
        }
    }
}
