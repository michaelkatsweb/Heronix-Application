package com.heronix.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * HTTPS Redirect Configuration for Heronix-SIS
 *
 * This configuration ensures that all HTTP traffic is automatically redirected
 * to HTTPS in production, enforcing encrypted communication.
 *
 * HOW IT WORKS:
 * 1. Application listens on TWO ports:
 *    - Port 8080 (HTTP) - accepts connections but immediately redirects
 *    - Port 8443 (HTTPS) - the actual application endpoint
 *
 * 2. When a request arrives on port 8080:
 *    - Tomcat immediately sends HTTP 301 (Permanent Redirect)
 *    - Redirect location: https://[same-host]:8443/[same-path]
 *    - Browser automatically follows redirect to HTTPS
 *
 * 3. This works in combination with HSTS headers to ensure:
 *    - First visit: HTTP → 301 redirect → HTTPS
 *    - Subsequent visits: Browser goes directly to HTTPS (HSTS)
 *
 * PRODUCTION DEPLOYMENT:
 * - In production with reverse proxy (nginx/Apache):
 *   - Proxy handles HTTP → HTTPS redirect
 *   - Application only listens on HTTPS (port 8443)
 *   - This configuration can be disabled
 *
 * - In standalone deployment (no reverse proxy):
 *   - This configuration ensures HTTPS enforcement
 *   - Firewall should allow ports 8080 (HTTP) and 8443 (HTTPS)
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Configuration
@Profile("prod")
public class HttpsRedirectConfig {

    @Value("${server.http.port:8080}")
    private int httpPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    /**
     * Configures Tomcat to listen on both HTTP and HTTPS ports.
     * HTTP port automatically redirects all requests to HTTPS.
     *
     * @return WebServerFactoryCustomizer for Tomcat
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpsRedirectCustomizer() {
        return factory -> {
            // Add HTTP connector that redirects to HTTPS
            factory.addAdditionalTomcatConnectors(createHttpConnector());
        };
    }

    /**
     * Creates an HTTP connector that redirects all traffic to HTTPS.
     *
     * @return configured Connector
     */
    private Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);

        // Configure HTTP port
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);

        // Configure redirect to HTTPS
        connector.setRedirectPort(httpsPort);

        return connector;
    }
}
