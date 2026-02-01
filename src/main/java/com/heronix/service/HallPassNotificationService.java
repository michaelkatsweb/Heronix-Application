package com.heronix.service;

import com.heronix.model.domain.HallPassSession;
import com.heronix.model.domain.Notification;
import com.heronix.model.domain.ParentGuardian;
import com.heronix.model.domain.Student;
import com.heronix.repository.ParentGuardianRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hall Pass Notification Service
 *
 * Handles routing of hall pass notifications to parents through various channels:
 * - In-app notifications (via NotificationService)
 * - Email notifications
 * - Push notifications (via Heronix-Talk)
 * - SMS notifications (premium feature)
 *
 * Integrates with Heronix-Talk to fetch parent preferences and deliver notifications.
 *
 * @author Heronix Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class HallPassNotificationService {

    private final NotificationService notificationService;
    private final ParentGuardianRepository parentGuardianRepository;
    private final RestTemplate restTemplate;

    public HallPassNotificationService(
            NotificationService notificationService,
            ParentGuardianRepository parentGuardianRepository,
            @Qualifier("schedulerRestTemplate") RestTemplate restTemplate) {
        this.notificationService = notificationService;
        this.parentGuardianRepository = parentGuardianRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${heronix.talk.url:http://localhost:9680}")
    private String talkBaseUrl;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.push.enabled:true}")
    private boolean pushEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    /**
     * Send hall pass departure notification to all parents of the student
     */
    @Async
    public void sendDepartureNotification(Student student, HallPassSession session) {
        if (student == null || session == null) {
            log.warn("Cannot send departure notification - student or session is null");
            return;
        }

        log.info("Sending departure notification for student {} to parents", student.getId());

        String title = "Hall Pass - Departure";
        String message = buildDepartureMessage(student, session);

        // Get all parents for this student
        List<Long> parentIds = getParentIdsForStudent(student.getId());

        for (Long parentId : parentIds) {
            try {
                // Check parent preferences
                Map<String, Object> preferences = getParentPreferences(parentId.toString(), student.getId().toString());

                if (shouldNotifyOnDeparture(preferences)) {
                    // Create in-app notification
                    createParentNotification(parentId, title, message, session, 2);

                    // Send push notification if enabled
                    if (shouldSendPush(preferences)) {
                        sendPushNotification(parentId, title, message, "DEPARTURE");
                    }

                    // Send email if enabled
                    if (shouldSendEmail(preferences)) {
                        sendEmailNotification(parentId, title, message, student, session);
                    }

                    // Send SMS if enabled (premium)
                    if (shouldSendSms(preferences)) {
                        sendSmsNotification(parentId, buildSmsMessage(student, session, "departed"));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send departure notification to parent {}", parentId, e);
            }
        }
    }

    /**
     * Send hall pass return notification to all parents of the student
     */
    @Async
    public void sendReturnNotification(Student student, HallPassSession session) {
        if (student == null || session == null) {
            log.warn("Cannot send return notification - student or session is null");
            return;
        }

        log.info("Sending return notification for student {} to parents", student.getId());

        String title = "Hall Pass - Return";
        String message = buildReturnMessage(student, session);

        List<Long> parentIds = getParentIdsForStudent(student.getId());

        for (Long parentId : parentIds) {
            try {
                Map<String, Object> preferences = getParentPreferences(parentId.toString(), student.getId().toString());

                if (shouldNotifyOnReturn(preferences)) {
                    createParentNotification(parentId, title, message, session, 1);

                    if (shouldSendPush(preferences)) {
                        sendPushNotification(parentId, title, message, "RETURN");
                    }

                    if (shouldSendEmail(preferences)) {
                        sendEmailNotification(parentId, title, message, student, session);
                    }

                    if (shouldSendSms(preferences)) {
                        sendSmsNotification(parentId, buildSmsMessage(student, session, "returned"));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send return notification to parent {}", parentId, e);
            }
        }
    }

    /**
     * Send hall pass overdue alert to all parents and admins
     */
    @Async
    public void sendOverdueAlert(Student student, HallPassSession session) {
        if (student == null || session == null) {
            log.warn("Cannot send overdue alert - student or session is null");
            return;
        }

        log.info("Sending OVERDUE alert for student {} to parents and admins", student.getId());

        String title = "Hall Pass - OVERDUE Alert";
        String message = buildOverdueMessage(student, session);

        // Notify parents
        List<Long> parentIds = getParentIdsForStudent(student.getId());

        for (Long parentId : parentIds) {
            try {
                Map<String, Object> preferences = getParentPreferences(parentId.toString(), student.getId().toString());

                if (shouldNotifyOnOverdue(preferences)) {
                    createParentNotification(parentId, title, message, session, 4); // High priority

                    // Always send push for overdue (unless quiet hours)
                    if (shouldSendPush(preferences) && !isInQuietHours(preferences)) {
                        sendPushNotification(parentId, title, message, "OVERDUE");
                    }

                    // Always send email for overdue
                    if (shouldSendEmail(preferences)) {
                        sendEmailNotification(parentId, title, message, student, session);
                    }

                    // Send SMS for overdue if enabled
                    if (shouldSendSms(preferences)) {
                        sendSmsNotification(parentId, buildSmsMessage(student, session, "OVERDUE"));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send overdue notification to parent {}", parentId, e);
            }
        }

        // Notify admins
        notificationService.createNotificationForRole(
                Notification.NotificationType.CAPACITY_WARNING,
                title,
                message,
                "ADMIN"
        );

        // Notify teacher if assigned
        if (session.getTeacher() != null) {
            notificationService.createNotification(
                    Notification.NotificationType.CAPACITY_WARNING,
                    title,
                    message,
                    session.getTeacher().getId()
            );
        }
    }

    /**
     * Send excessive usage alert to parents
     */
    @Async
    public void sendExcessiveUsageAlert(Student student, int passCount, String period) {
        if (student == null) return;

        log.info("Sending excessive usage alert for student {} ({} passes {})",
                student.getId(), passCount, period);

        String title = "Hall Pass - Usage Alert";
        String message = String.format(
                "%s %s has used %d hall passes %s. This exceeds the recommended threshold.",
                student.getFirstName(),
                student.getLastName(),
                passCount,
                period
        );

        List<Long> parentIds = getParentIdsForStudent(student.getId());

        for (Long parentId : parentIds) {
            try {
                Map<String, Object> preferences = getParentPreferences(parentId.toString(), student.getId().toString());

                // Check threshold settings
                int threshold = period.contains("day") ?
                        getIntPreference(preferences, "dailyPassThreshold", 5) :
                        getIntPreference(preferences, "weeklyPassThreshold", 15);

                if (passCount >= threshold) {
                    createParentNotification(parentId, title, message, null, 3);

                    if (shouldSendEmail(preferences)) {
                        sendEmailNotification(parentId, title, message, student, null);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send excessive usage alert to parent {}", parentId, e);
            }
        }
    }

    // ==================== Helper Methods ====================

    private List<Long> getParentIdsForStudent(Long studentId) {
        try {
            List<ParentGuardian> parents = parentGuardianRepository.findByStudentId(studentId);
            return parents.stream()
                    .map(ParentGuardian::getId)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to get parent IDs for student {}: {}", studentId, e.getMessage());
            return List.of();
        }
    }

    private Map<String, Object> getParentPreferences(String parentId, String studentId) {
        try {
            String url = talkBaseUrl + "/api/hall-pass/parent/child/" + studentId + "/preferences";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object prefsObj = response.getBody().get("preferences");
                if (prefsObj instanceof Map) {
                    return (Map<String, Object>) prefsObj;
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch preferences from Talk, using defaults: {}", e.getMessage());
        }

        // Return default preferences
        return getDefaultPreferences();
    }

    private Map<String, Object> getDefaultPreferences() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("notifyOnDeparture", true);
        defaults.put("notifyOnReturn", false);
        defaults.put("notifyOnOverdue", true);
        defaults.put("pushNotificationsEnabled", true);
        defaults.put("emailNotificationsEnabled", true);
        defaults.put("smsNotificationsEnabled", false);
        defaults.put("dailyPassThreshold", 5);
        defaults.put("weeklyPassThreshold", 15);
        return defaults;
    }

    private boolean shouldNotifyOnDeparture(Map<String, Object> prefs) {
        return getBooleanPreference(prefs, "notifyOnDeparture", true);
    }

    private boolean shouldNotifyOnReturn(Map<String, Object> prefs) {
        return getBooleanPreference(prefs, "notifyOnReturn", false);
    }

    private boolean shouldNotifyOnOverdue(Map<String, Object> prefs) {
        return getBooleanPreference(prefs, "notifyOnOverdue", true);
    }

    private boolean shouldSendPush(Map<String, Object> prefs) {
        return pushEnabled && getBooleanPreference(prefs, "pushNotificationsEnabled", true);
    }

    private boolean shouldSendEmail(Map<String, Object> prefs) {
        return emailEnabled && getBooleanPreference(prefs, "emailNotificationsEnabled", true);
    }

    private boolean shouldSendSms(Map<String, Object> prefs) {
        return smsEnabled && getBooleanPreference(prefs, "smsNotificationsEnabled", false);
    }

    private boolean isInQuietHours(Map<String, Object> prefs) {
        String start = (String) prefs.get("quietHoursStart");
        String end = (String) prefs.get("quietHoursEnd");

        if (start == null || end == null) return false;

        try {
            java.time.LocalTime now = java.time.LocalTime.now();
            java.time.LocalTime startTime = java.time.LocalTime.parse(start);
            java.time.LocalTime endTime = java.time.LocalTime.parse(end);

            if (startTime.isBefore(endTime)) {
                return now.isAfter(startTime) && now.isBefore(endTime);
            } else {
                return now.isAfter(startTime) || now.isBefore(endTime);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean getBooleanPreference(Map<String, Object> prefs, String key, boolean defaultValue) {
        if (prefs == null || !prefs.containsKey(key)) return defaultValue;
        Object value = prefs.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        return defaultValue;
    }

    private int getIntPreference(Map<String, Object> prefs, String key, int defaultValue) {
        if (prefs == null || !prefs.containsKey(key)) return defaultValue;
        Object value = prefs.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        return defaultValue;
    }

    // ==================== Message Builders ====================
    // PRIVACY: Parent notifications use ONLY student first name and last name
    // All other PII is managed by Heronix-Guardian and never included in notifications
    // This ensures parents receive tokenized information as per data privacy policy

    /**
     * Build departure notification message for parents
     * TOKENIZED: Uses only student first/last name per Guardian privacy policy
     */
    private String buildDepartureMessage(Student student, HallPassSession session) {
        // Only include first name + last name (tokenized data for parents)
        return String.format(
                "%s %s left class at %s to go to %s",
                student.getFirstName(),
                student.getLastName(),
                session.getDepartureTime() != null ?
                        session.getDepartureTime().toLocalTime().toString() : "Unknown",
                session.getDestination() != null ?
                        session.getDestination().getDisplayName() : "Unknown"
        );
    }

    /**
     * Build return notification message for parents
     * TOKENIZED: Uses only student first/last name per Guardian privacy policy
     */
    private String buildReturnMessage(Student student, HallPassSession session) {
        return String.format(
                "%s %s returned to class at %s (Duration: %s)",
                student.getFirstName(),
                student.getLastName(),
                session.getReturnTime() != null ?
                        session.getReturnTime().toLocalTime().toString() : "Unknown",
                session.getFormattedDuration() != null ?
                        session.getFormattedDuration() : "Unknown"
        );
    }

    /**
     * Build overdue alert message for parents
     * TOKENIZED: Uses only student first/last name per Guardian privacy policy
     */
    private String buildOverdueMessage(Student student, HallPassSession session) {
        long minutes = session.calculateDuration();
        return String.format(
                "ALERT: %s %s hall pass is overdue. Departed to %s at %s (%d minutes ago)",
                student.getFirstName(),
                student.getLastName(),
                session.getDestination() != null ?
                        session.getDestination().getDisplayName() : "Unknown",
                session.getDepartureTime() != null ?
                        session.getDepartureTime().toLocalTime().toString() : "Unknown",
                minutes
        );
    }

    /**
     * Build SMS message for parents (short format)
     * TOKENIZED: Uses only student first name per Guardian privacy policy
     */
    private String buildSmsMessage(Student student, HallPassSession session, String action) {
        // SMS uses only first name due to character limits
        if ("OVERDUE".equals(action)) {
            return String.format("ALERT: %s hall pass OVERDUE - %d min",
                    student.getFirstName(), session.calculateDuration());
        }
        return String.format("%s %s - %s",
                student.getFirstName(),
                action,
                session.getDestination() != null ?
                        session.getDestination().getDisplayName() : "");
    }

    // ==================== Notification Delivery ====================

    private void createParentNotification(Long parentId, String title, String message,
                                          HallPassSession session, int priority) {
        notificationService.createDetailedNotification(
                Notification.NotificationType.SYSTEM_ALERT,
                title,
                message,
                priority,
                parentId,
                "PARENT",
                "HALL_PASS",
                session != null ? session.getId() : null,
                null
        );
    }

    /**
     * Send push notification to parent
     * TOKENIZED: Push payloads contain only first/last name, no internal IDs
     */
    private void sendPushNotification(Long parentId, String title, String message, String eventType) {
        try {
            String url = talkBaseUrl + "/api/notifications/push";
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", parentId);
            payload.put("title", title);
            payload.put("body", message); // Already tokenized by message builders
            payload.put("tag", "hall-pass-" + eventType.toLowerCase());
            // TOKENIZED: Only event type in data, no student internal IDs
            payload.put("data", Map.of(
                    "type", "HALL_PASS",
                    "event", eventType,
                    "_tokenized", true
            ));

            restTemplate.postForEntity(url, payload, Map.class);
            log.debug("Push notification sent to parent {} (tokenized)", parentId);

        } catch (Exception e) {
            log.warn("Failed to send push notification to parent {}: {}", parentId, e.getMessage());
        }
    }

    /**
     * Send email notification to parent
     * TOKENIZED: Email payloads contain only first/last name, no internal IDs or PII
     */
    private void sendEmailNotification(Long parentId, String title, String message,
                                       Student student, HallPassSession session) {
        try {
            String url = talkBaseUrl + "/api/notifications/email";
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", parentId);
            payload.put("subject", "[Heronix] " + title);
            payload.put("body", message); // Already tokenized by message builders
            payload.put("template", "hall-pass-notification");
            // TOKENIZED: Only include first/last name in template data
            // NO student IDs, grade levels, or other PII
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("studentFirstName", student.getFirstName());
            templateData.put("studentLastName", student.getLastName());
            templateData.put("studentDisplayName", student.getFirstName() + " " + student.getLastName());
            templateData.put("message", message);
            templateData.put("timestamp", LocalDateTime.now().toString());
            templateData.put("_tokenized", true);
            templateData.put("_dataPolicy", "EXTERNAL_NOTIFICATION");
            // Add activity info if session exists (no internal IDs)
            if (session != null) {
                templateData.put("destination", session.getDestination() != null ?
                        session.getDestination().getDisplayName() : "Unknown");
                templateData.put("departureTime", session.getDepartureTime() != null ?
                        session.getDepartureTime().toLocalTime().toString() : null);
                templateData.put("returnTime", session.getReturnTime() != null ?
                        session.getReturnTime().toLocalTime().toString() : null);
            }
            payload.put("templateData", templateData);

            restTemplate.postForEntity(url, payload, Map.class);
            log.debug("Email notification sent to parent {} (tokenized)", parentId);

        } catch (Exception e) {
            log.warn("Failed to send email notification to parent {}: {}", parentId, e.getMessage());
        }
    }

    /**
     * Send SMS notification to parent
     * TOKENIZED: SMS payloads contain only first name (due to character limits), no IDs
     */
    private void sendSmsNotification(Long parentId, String message) {
        try {
            String url = talkBaseUrl + "/api/notifications/sms";
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", parentId);
            payload.put("message", message); // Already tokenized by buildSmsMessage
            payload.put("_tokenized", true);

            restTemplate.postForEntity(url, payload, Map.class);
            log.debug("SMS notification sent to parent {} (tokenized)", parentId);

        } catch (Exception e) {
            log.warn("Failed to send SMS notification to parent {}: {}", parentId, e.getMessage());
        }
    }
}
