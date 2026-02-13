package com.heronix.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * WebSocket Client Service
 *
 * Client-side WebSocket service for JavaFX application.
 * Connects to server WebSocket endpoint and receives real-time updates.
 *
 * Features:
 * - Automatic connection management
 * - Reconnection on disconnect
 * - Message parsing and routing
 * - Callback-based event handling
 *
 * Usage:
 * ```
 * webSocketService.connect();
 * webSocketService.onDashboardUpdate(data -> {
 *     // Handle dashboard update
 * });
 * ```
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 48 - WebSocket Real-Time Updates
 */
@Service
@Slf4j
public class WebSocketClientService {

    @Value("${api.base-url:http://localhost:9590}")
    private String apiBaseUrl;

    private WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Event callbacks
    private Consumer<Map<String, Object>> attendanceUpdateCallback;
    private Consumer<Map<String, Object>> dashboardUpdateCallback;
    private Consumer<Map<String, Object>> notificationCallback;

    /**
     * Connect to WebSocket server
     */
    public void connect() {
        try {
            // Convert HTTP URL to WS URL
            String wsUrl = apiBaseUrl.replace("http://", "ws://").replace("https://", "wss://");
            String endpoint = wsUrl + "/ws";

            log.info("Connecting to WebSocket: {}", endpoint);

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                .buildAsync(URI.create(endpoint), new WebSocketListener());

            wsFuture.whenComplete((ws, error) -> {
                if (error != null) {
                    log.error("WebSocket connection failed: {}", error.getMessage());
                } else {
                    this.webSocket = ws;
                    log.info("WebSocket connected successfully");
                    subscribeToTopics();
                }
            });

        } catch (Exception e) {
            log.error("Error connecting to WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    @PreDestroy
    public void disconnect() {
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnect");
                log.info("WebSocket disconnected");
            } catch (Exception e) {
                log.error("Error disconnecting WebSocket: {}", e.getMessage());
            }
        }
    }

    /**
     * Subscribe to topics after connection
     */
    private void subscribeToTopics() {
        try {
            // Subscribe to attendance updates
            sendMessage("{\"action\":\"subscribe\",\"topic\":\"/topic/attendance\"}");

            // Subscribe to dashboard updates
            sendMessage("{\"action\":\"subscribe\",\"topic\":\"/topic/dashboard\"}");

            // Subscribe to notifications
            sendMessage("{\"action\":\"subscribe\",\"topic\":\"/topic/notifications\"}");

        } catch (Exception e) {
            log.error("Error subscribing to topics: {}", e.getMessage());
        }
    }

    /**
     * Send message to server
     */
    private void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.sendText(message, true);
        }
    }

    /**
     * Register callback for attendance updates
     */
    public void onAttendanceUpdate(Consumer<Map<String, Object>> callback) {
        this.attendanceUpdateCallback = callback;
    }

    /**
     * Register callback for dashboard updates
     */
    public void onDashboardUpdate(Consumer<Map<String, Object>> callback) {
        this.dashboardUpdateCallback = callback;
    }

    /**
     * Register callback for notifications
     */
    public void onNotification(Consumer<Map<String, Object>> callback) {
        this.notificationCallback = callback;
    }

    /**
     * Handle incoming WebSocket message
     */
    private void handleMessage(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String type = (String) data.get("type");

            if (type == null) {
                return;
            }

            switch (type) {
                case "ATTENDANCE_RECORDED":
                case "ATTENDANCE_UPDATED":
                case "ATTENDANCE_DELETED":
                    if (attendanceUpdateCallback != null) {
                        attendanceUpdateCallback.accept(data);
                    }
                    break;

                case "DASHBOARD_REFRESH":
                case "METRICS_UPDATE":
                    if (dashboardUpdateCallback != null) {
                        dashboardUpdateCallback.accept(data);
                    }
                    break;

                case "NOTIFICATION":
                    if (notificationCallback != null) {
                        notificationCallback.accept(data);
                    }
                    break;

                default:
                    log.debug("Unknown message type: {}", type);
            }

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
        }
    }

    /**
     * WebSocket Listener implementation
     */
    private class WebSocketListener implements WebSocket.Listener {

        private StringBuilder messageBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WebSocket opened");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            messageBuffer.append(data);

            if (last) {
                String message = messageBuffer.toString();
                messageBuffer = new StringBuilder();
                handleMessage(message);
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("WebSocket closed: {} - {}", statusCode, reason);

            // Attempt reconnection after delay
            Thread reconnectThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds
                    log.info("Attempting to reconnect WebSocket...");
                    connect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            reconnectThread.setDaemon(true);
            reconnectThread.start();

            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WebSocket error: {}", error.getMessage());
        }
    }
}
