package com.heronix.gateway.sanitization;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Notification content for external transmission.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationContent {

    /**
     * Notification type
     */
    private NotificationType type;

    /**
     * Recipient email address
     */
    private String recipientEmail;

    /**
     * Recipient phone number (for SMS)
     */
    private String recipientPhone;

    /**
     * Notification subject/title
     */
    private String subject;

    /**
     * Notification body content
     */
    private String body;

    /**
     * Template ID (if using template)
     */
    private String templateId;

    /**
     * Template variables
     */
    private Map<String, String> templateVariables;

    /**
     * Priority level
     */
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * Attachments (will be sanitized/removed)
     */
    private List<Map<String, Object>> attachments;

    /**
     * Types of notifications
     */
    public enum NotificationType {
        ATTENDANCE_ALERT,
        GRADE_UPDATE,
        EMERGENCY_NOTIFICATION,
        GENERAL_ANNOUNCEMENT,
        SCHEDULE_CHANGE,
        PARENT_REMINDER
    }

    /**
     * Notification priority
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
