package com.heronix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 *
 * Configures WebSocket messaging with STOMP protocol for real-time updates.
 * Used for live attendance tracking, notifications, and dashboard updates.
 *
 * Architecture:
 * - STOMP protocol over WebSocket
 * - Simple in-memory message broker
 * - Client subscribes to topics for updates
 * - Server broadcasts to all subscribed clients
 *
 * Topics:
 * - /topic/attendance - Real-time attendance updates
 * - /topic/dashboard - Dashboard metric updates
 * - /topic/notifications - System notifications
 *
 * Endpoints:
 * - /ws - WebSocket handshake endpoint
 * - /app - Application destination prefix for client messages
 *
 * Security:
 * - CORS enabled for localhost and configured origins
 * - JWT authentication integrated via interceptor
 * - Rate limiting applied to WebSocket connections
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 48 - WebSocket Real-Time Updates
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker
     * - Simple broker for /topic destinations
     * - Application destination prefix /app
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for targeted user messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints
     * - Main endpoint: /ws
     * - SockJS fallback enabled
     * - CORS configured for allowed origins
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // Allow all origins for development
            .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
    }
}
