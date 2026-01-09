package com.heronix.controller.api;

import com.heronix.model.domain.Notification;
import com.heronix.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Notification Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    // ==================== Create Operations ====================

    @PostMapping("/user/{userId}")
    public ResponseEntity<Notification> createNotification(
            @PathVariable Long userId,
            @RequestParam Notification.NotificationType type,
            @RequestParam String title,
            @RequestParam String message) {
        Notification notification = notificationService.createNotification(type, title, message, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/role/{role}")
    public ResponseEntity<Notification> createNotificationForRole(
            @PathVariable String role,
            @RequestParam Notification.NotificationType type,
            @RequestParam String title,
            @RequestParam String message) {
        Notification notification = notificationService.createNotificationForRole(type, title, message, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/global")
    public ResponseEntity<Notification> createGlobalNotification(
            @RequestParam Notification.NotificationType type,
            @RequestParam String title,
            @RequestParam String message) {
        Notification notification = notificationService.createGlobalNotification(type, title, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/detailed")
    public ResponseEntity<Notification> createDetailedNotification(
            @RequestParam Notification.NotificationType type,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam Integer priority,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) String targetRole,
            @RequestParam(required = false) String relatedEntityType,
            @RequestParam(required = false) Long relatedEntityId,
            @RequestParam(required = false) String actionUrl) {
        Notification notification = notificationService.createDetailedNotification(
                type, title, message, priority, targetUserId, targetRole,
                relatedEntityType, relatedEntityId, actionUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // ==================== Read Operations ====================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/high-priority")
    public ResponseEntity<List<Notification>> getHighPriorityNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getHighPriorityNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable Notification.NotificationType type) {
        List<Notification> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<Notification>> getNotificationsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<Notification> notifications = notificationService.getNotificationsByEntity(entityType, entityId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecentNotifications() {
        List<Notification> notifications = notificationService.getRecentNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count/unread")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(@PathVariable Long userId) {
        Long count = notificationService.countUnreadNotifications(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    // ==================== Update Operations ====================

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification marked as read");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<Map<String, String>> dismissNotification(@PathVariable Long id) {
        notificationService.dismissNotification(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification dismissed");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/user/{userId}/dismiss-all")
    public ResponseEntity<Map<String, String>> dismissAllForUser(@PathVariable Long userId) {
        notificationService.dismissAllForUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All notifications dismissed");
        return ResponseEntity.ok(response);
    }

    // ==================== Delete Operations ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldNotifications() {
        notificationService.cleanupOldNotifications();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Old notifications cleaned up");
        return ResponseEntity.ok(response);
    }

    // ==================== Schedule Change Notifications ====================

    @PostMapping("/schedule/section-assigned")
    public ResponseEntity<Map<String, String>> notifySectionAssigned(
            @RequestParam Long sectionId,
            @RequestParam Long teacherId,
            @RequestParam String courseName,
            @RequestParam Integer period) {
        notificationService.notifySectionAssigned(sectionId, teacherId, courseName, period);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Section assignment notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/schedule/teacher-changed")
    public ResponseEntity<Map<String, String>> notifyTeacherChanged(
            @RequestParam Long sectionId,
            @RequestParam String courseName,
            @RequestParam String oldTeacher,
            @RequestParam String newTeacher) {
        notificationService.notifyTeacherChanged(sectionId, courseName, oldTeacher, newTeacher);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Teacher change notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/schedule/room-changed")
    public ResponseEntity<Map<String, String>> notifyRoomChanged(
            @RequestParam Long sectionId,
            @RequestParam String courseName,
            @RequestParam String oldRoom,
            @RequestParam String newRoom) {
        notificationService.notifyRoomChanged(sectionId, courseName, oldRoom, newRoom);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Room change notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/schedule/period-changed")
    public ResponseEntity<Map<String, String>> notifyPeriodChanged(
            @RequestParam Long sectionId,
            @RequestParam String courseName,
            @RequestParam Integer oldPeriod,
            @RequestParam Integer newPeriod) {
        notificationService.notifyPeriodChanged(sectionId, courseName, oldPeriod, newPeriod);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Period change notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Conflict Notifications ====================

    @PostMapping("/conflict/detected")
    public ResponseEntity<Map<String, String>> notifyConflictDetected(
            @RequestParam String conflictType,
            @RequestParam String description,
            @RequestParam(required = false) Long relatedEntityId) {
        notificationService.notifyConflictDetected(conflictType, description, relatedEntityId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Conflict notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/conflict/resolved")
    public ResponseEntity<Map<String, String>> notifyConflictResolved(
            @RequestParam String conflictType,
            @RequestParam String description) {
        notificationService.notifyConflictResolved(conflictType, description);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Conflict resolution notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Enrollment Notifications ====================

    @PostMapping("/enrollment/over-enrolled")
    public ResponseEntity<Map<String, String>> notifyOverEnrollment(
            @RequestParam Long sectionId,
            @RequestParam String courseName,
            @RequestParam int enrolled,
            @RequestParam int capacity) {
        notificationService.notifyOverEnrollment(sectionId, courseName, enrolled, capacity);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Over-enrollment notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enrollment/under-enrolled")
    public ResponseEntity<Map<String, String>> notifyUnderEnrollment(
            @RequestParam Long sectionId,
            @RequestParam String courseName,
            @RequestParam int enrolled,
            @RequestParam int minRequired) {
        notificationService.notifyUnderEnrollment(sectionId, courseName, enrolled, minRequired);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Under-enrollment notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enrollment/section-full")
    public ResponseEntity<Map<String, String>> notifySectionFull(
            @RequestParam Long sectionId,
            @RequestParam String courseName) {
        notificationService.notifySectionFull(sectionId, courseName);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Section full notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enrollment/student-enrolled")
    public ResponseEntity<Map<String, String>> notifyStudentEnrolled(
            @RequestParam Long studentId,
            @RequestParam String courseName,
            @RequestParam Integer period) {
        notificationService.notifyStudentEnrolled(studentId, courseName, period);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Student enrollment notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enrollment/student-dropped")
    public ResponseEntity<Map<String, String>> notifyStudentDropped(
            @RequestParam Long studentId,
            @RequestParam String courseName,
            @RequestParam String reason) {
        notificationService.notifyStudentDropped(studentId, courseName, reason);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Student drop notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Administrative Notifications ====================

    @PostMapping("/admin/teacher-overload")
    public ResponseEntity<Map<String, String>> notifyTeacherOverload(
            @RequestParam Long teacherId,
            @RequestParam String teacherName,
            @RequestParam int currentLoad,
            @RequestParam int maxLoad) {
        notificationService.notifyTeacherOverload(teacherId, teacherName, currentLoad, maxLoad);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Teacher overload notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/room-conflict")
    public ResponseEntity<Map<String, String>> notifyRoomConflict(
            @RequestParam Long roomId,
            @RequestParam String roomName,
            @RequestParam Integer period,
            @RequestBody List<String> conflictingSections) {
        notificationService.notifyRoomConflict(roomId, roomName, period, conflictingSections);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Room conflict notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/schedule-published")
    public ResponseEntity<Map<String, String>> notifySchedulePublished(
            @RequestParam String scheduleType,
            @RequestParam String academicYear) {
        notificationService.notifySchedulePublished(scheduleType, academicYear);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Schedule publication notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/validation-error")
    public ResponseEntity<Map<String, String>> notifyValidationError(
            @RequestParam String errorType,
            @RequestParam String description,
            @RequestParam(required = false) Long relatedEntityId) {
        notificationService.notifyValidationError(errorType, description, relatedEntityId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Validation error notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== System Notifications ====================

    @PostMapping("/system/alert")
    public ResponseEntity<Map<String, String>> sendSystemAlert(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam Integer priority) {
        notificationService.sendSystemAlert(title, message, priority);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "System alert sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/system/export-complete")
    public ResponseEntity<Map<String, String>> notifyExportComplete(
            @RequestParam Long userId,
            @RequestParam String exportType,
            @RequestParam String fileName) {
        notificationService.notifyExportComplete(userId, exportType, fileName);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Export completion notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/system/import-complete")
    public ResponseEntity<Map<String, String>> notifyImportComplete(
            @RequestParam Long userId,
            @RequestParam String importType,
            @RequestParam int recordsProcessed,
            @RequestParam int errors) {
        notificationService.notifyImportComplete(userId, importType, recordsProcessed, errors);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Import completion notification sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNotificationDashboard(@PathVariable Long userId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Notification> allNotifications = notificationService.getNotificationsForUser(userId);
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);
        List<Notification> highPriority = notificationService.getHighPriorityNotificationsForUser(userId);
        Long unreadCount = notificationService.countUnreadNotifications(userId);

        dashboard.put("userId", userId);
        dashboard.put("totalNotifications", allNotifications.size());
        dashboard.put("unreadCount", unreadCount);
        dashboard.put("highPriorityCount", highPriority.size());
        dashboard.put("unreadNotifications", unreadNotifications);
        dashboard.put("highPriorityNotifications", highPriority);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getNotificationOverviewDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Notification> recentNotifications = notificationService.getRecentNotifications();

        Map<String, Long> byType = recentNotifications.stream()
                .filter(n -> n.getType() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        n -> n.getType().name(),
                        java.util.stream.Collectors.counting()));

        long unreadCount = recentNotifications.stream()
                .filter(n -> n.getReadAt() == null)
                .count();

        dashboard.put("totalRecentNotifications", recentNotifications.size());
        dashboard.put("unreadCount", unreadCount);
        dashboard.put("notificationsByType", byType);
        dashboard.put("recentNotifications", recentNotifications);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/type-breakdown")
    public ResponseEntity<Map<String, Object>> getNotificationTypeBreakdown() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Notification> recentNotifications = notificationService.getRecentNotifications();

        Map<String, Long> byType = recentNotifications.stream()
                .filter(n -> n.getType() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        n -> n.getType().name(),
                        java.util.stream.Collectors.counting()));

        dashboard.put("totalNotifications", recentNotifications.size());
        dashboard.put("notificationsByType", byType);
        dashboard.put("typeCount", byType.size());

        return ResponseEntity.ok(dashboard);
    }
}
