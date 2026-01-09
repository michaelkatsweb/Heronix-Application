package com.heronix.security;

import com.heronix.service.JwtTokenService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter
 *
 * Spring Security filter that authenticates requests using JWT tokens.
 * Extracts JWT from Authorization: Bearer header and validates it.
 *
 * Authentication Flow:
 * 1. Extract JWT token from Authorization header
 * 2. Validate JWT signature and expiration
 * 3. Extract user ID, roles, and permissions from token claims
 * 4. Create Spring Security authentication
 * 5. Set authentication in SecurityContext
 *
 * Header Format:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * Token Claims:
 * - sub: User ID
 * - roles: User roles (ADMIN, TEACHER, STUDENT, PARENT)
 * - permissions: API permissions (read:students, write:grades, etc.)
 * - iat: Issued at timestamp
 * - exp: Expiration timestamp
 *
 * Spring Security Integration:
 * - Principal: User ID from token
 * - Authorities: Roles (ROLE_ADMIN, etc.) + Permissions (SCOPE_read:students, etc.)
 * - Details: Request details (IP, session)
 *
 * Priority:
 * - This filter checks for JWT tokens
 * - ApiKeyAuthenticationFilter checks for API keys
 * - If JWT is present, it takes precedence
 * - If no JWT, API key authentication may be attempted
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            Optional<String> jwtToken = extractJwtToken(request);

            if (jwtToken.isPresent()) {
                String token = jwtToken.get();

                // Validate JWT token
                if (jwtTokenService.validateToken(token)) {
                    // Extract claims from token
                    String userId = jwtTokenService.getUserIdFromToken(token);
                    String[] roles = jwtTokenService.getRolesFromToken(token);
                    String[] permissions = jwtTokenService.getPermissionsFromToken(token);

                    // Create authorities list
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    // Add roles as ROLE_ authorities
                    if (roles != null) {
                        Arrays.stream(roles)
                            .forEach(role -> authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + role)));
                    }

                    // Add permissions as SCOPE_ authorities
                    if (permissions != null) {
                        Arrays.stream(permissions)
                            .forEach(permission -> authorities.add(
                                new SimpleGrantedAuthority("SCOPE_" + permission)));
                    }

                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                        );

                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated request with JWT token for user: {}", userId);
                } else {
                    log.warn("Invalid or expired JWT token from IP: {}", request.getRemoteAddr());
                }
            }
        } catch (Exception e) {
            log.error("Error during JWT authentication: {}", e.getMessage(), e);
            // Don't block the request - let it proceed without authentication
            // Other filters or security config will handle authorization
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization: Bearer header
     *
     * @param request HTTP request
     * @return Optional containing JWT token if present
     */
    private Optional<String> extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            // Check if it's a JWT token (not an API key)
            // JWT tokens start with "eyJ" (base64-encoded JSON header)
            if (token.startsWith("eyJ")) {
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }
}
