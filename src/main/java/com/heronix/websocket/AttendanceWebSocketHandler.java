package com.heronix.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Attendance WebSocket Handler
 *
 * Handles real-time broadcasting of attendance updates to connected clients.
 * Sends messages when attendance records are created, updated, or deleted.
 *
 * Message Types:
 * - ATTENDANCE_RECORDED - New attendance record created
 * - ATTENDANCE_UPDATED - Existing record modified
 * - ATTENDANCE_DELETED - Record removed
 * - DASHBOARD_REFRESH - Trigger dashboard data refresh
 *
 * Topics:
 * - /topic/attendance - Individual attendance updates
 * - /topic/dashboard - Aggregated dashboard updates
 *
 * Payload Format:
 * {
 *   "type": "ATTENDANCE_RECORDED",
 *   "timestamp": "2025-12-30T14:30:00",
 *   "studentId": 12345,
 *   "studentName": "John Doe",
 *   "status": "PRESENT",
 *   "date": "2025-12-30"
 * }
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 48 - WebSocket Real-Time Updates
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast attendance record creation
     *
     * @param studentId Student ID
     * @param studentName Student name
     * @param status Attendance status
     * @param date Attendance date
     */
    public void broadcastAttendanceRecorded(Long studentId, String studentName, String status, String date) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDANCE_RECORDED");
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("studentId", studentId);
        message.put("studentName", studentName);
        message.put("status", status);
        message.put("date", date);

        sendToTopic("/topic/attendance", message);
        triggerDashboardRefresh();
    }

    /**
     * Broadcast attendance record update
     *
     * @param studentId Student ID
     * @param studentName Student name
     * @param oldStatus Previous status
     * @param newStatus New status
     * @param date Attendance date
     */
    public void broadcastAttendanceUpdated(Long studentId, String studentName,
                                          String oldStatus, String newStatus, String date) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDANCE_UPDATED");
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("studentId", studentId);
        message.put("studentName", studentName);
        message.put("oldStatus", oldStatus);
        message.put("newStatus", newStatus);
        message.put("date", date);

        sendToTopic("/topic/attendance", message);
        triggerDashboardRefresh();
    }

    /**
     * Broadcast attendance record deletion
     *
     * @param studentId Student ID
     * @param studentName Student name
     * @param date Attendance date
     */
    public void broadcastAttendanceDeleted(Long studentId, String studentName, String date) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDANCE_DELETED");
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("studentId", studentId);
        message.put("studentName", studentName);
        message.put("date", date);

        sendToTopic("/topic/attendance", message);
        triggerDashboardRefresh();
    }

    /**
     * Trigger dashboard refresh for all connected clients
     */
    public void triggerDashboardRefresh() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DASHBOARD_REFRESH");
        message.put("timestamp", LocalDateTime.now().toString());

        sendToTopic("/topic/dashboard", message);
    }

    /**
     * Broadcast dashboard metrics update
     *
     * @param metrics Dashboard metrics data
     */
    public void broadcastDashboardMetrics(Map<String, Object> metrics) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "METRICS_UPDATE");
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("metrics", metrics);

        sendToTopic("/topic/dashboard", message);
    }

    /**
     * Send notification to all clients
     *
     * @param title Notification title
     * @param content Notification content
     * @param level Notification level (INFO, WARNING, ERROR)
     */
    public void broadcastNotification(String title, String content, String level) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NOTIFICATION");
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("title", title);
        message.put("content", content);
        message.put("level", level);

        sendToTopic("/topic/notifications", message);
    }

    /**
     * Send message to topic
     *
     * @param topic Topic destination
     * @param message Message payload
     */
    private void sendToTopic(String topic, Object message) {
        try {
            messagingTemplate.convertAndSend(topic, message);
            log.debug("Sent WebSocket message to {}: {}", topic, message);
        } catch (Exception e) {
            log.error("Error sending WebSocket message to {}: {}", topic, e.getMessage(), e);
        }
    }
}
