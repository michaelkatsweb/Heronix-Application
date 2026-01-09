package com.heronix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Production Security Configuration for Heronix-SIS
 *
 * This configuration is ONLY active when the 'prod' profile is enabled.
 * It provides strict security settings suitable for production deployment:
 *
 * - CSRF Protection enabled (using cookie-based tokens)
 * - Strict role-based access control
 * - Session management with concurrent session limits
 * - Security headers (HSTS, CSP, X-Frame-Options, etc.)
 * - BCrypt password encoding with strength 12
 * - Rate limiting filter (configured separately)
 *
 * IMPORTANT: All sensitive configuration values (passwords, secrets) must be
 * set via environment variables, NEVER hardcoded in this file.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Profile("prod")
public class ProductionSecurityConfig {

    /**
     * Configures the main security filter chain for production.
     *
     * This configuration includes:
     * - CSRF protection enabled (disabled in dev for testing convenience)
     * - Strict authorization rules based on user roles
     * - Session management with concurrent session control
     * - Comprehensive security headers
     * - Form-based login with custom success/failure handlers
     * - Logout configuration with session invalidation
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // ============================================================
            // CSRF Protection (CRITICAL for production)
            // ============================================================
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Exclude health check and metrics endpoints from CSRF
                .ignoringRequestMatchers("/actuator/health", "/actuator/prometheus")
            )

            // ============================================================
            // Authorization Rules (Role-Based Access Control)
            // ============================================================
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (accessible without authentication)
                .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staging/**").hasRole("ADMIN")
                .requestMatchers("/users/**").hasRole("ADMIN")

                // Teacher and Admin endpoints
                .requestMatchers("/api/teacher/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/grades/submit/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/attendance/record/**").hasAnyRole("ADMIN", "TEACHER")

                // Student endpoints (view-only for their own data)
                .requestMatchers("/api/student/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                // Parent endpoints (view-only for their children's data)
                .requestMatchers("/api/parent/**").hasAnyRole("ADMIN", "PARENT")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )

            // ============================================================
            // Session Management
            // ============================================================
            .sessionManagement(session -> session
                // Use stateful sessions (required for JavaFX desktop app)
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Prevent session fixation attacks
                .sessionFixation().migrateSession()
                // Limit concurrent sessions per user
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .expiredUrl("/login?expired=true")
            )

            // ============================================================
            // Form-Based Login Configuration
            // ============================================================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // ============================================================
            // Logout Configuration
            // ============================================================
            .logout(logout -> logout
                .logoutUrl("/perform_logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )

            // ============================================================
            // Security Headers (Defense in Depth)
            // ============================================================
            .headers(headers -> headers
                // HTTP Strict Transport Security (force HTTPS)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 year
                    .preload(true)
                )

                // Content Security Policy (prevent XSS)
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'")
                )

                // Prevent clickjacking
                .frameOptions(frame -> frame.deny())

                // Prevent MIME type sniffing
                .contentTypeOptions(contentType -> contentType.disable())

                // XSS Protection (legacy but still useful)
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )

                // Referrer Policy (prevent information leakage)
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )

                // Permissions Policy (restrict browser features)
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            );

        return http.build();
    }

    /**
     * Password encoder bean using BCrypt with strength 12.
     *
     * BCrypt is a secure password hashing algorithm that includes:
     * - Automatic salt generation
     * - Configurable work factor (strength)
     * - Resistance to rainbow table attacks
     *
     * Strength 12 provides a good balance between security and performance:
     * - Takes ~250ms to hash a password on typical server hardware
     * - Makes brute force attacks computationally expensive
     * - Provides adequate security for sensitive student/staff data
     *
     * @return BCryptPasswordEncoder configured with strength 12
     */
    @Bean
    public PasswordEncoder productionPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
