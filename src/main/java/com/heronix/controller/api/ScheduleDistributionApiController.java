package com.heronix.controller.api;

import com.heronix.service.ScheduleDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Schedule Distribution
 *
 * Provides endpoints for distributing student schedules through multiple channels:
 * - Publication workflow (publish/unpublish schedules)
 * - Student portal notifications
 * - Email distribution with PDF attachments
 * - Parent portal integration
 * - Bulk distribution operations
 *
 * Distribution Channels:
 * 1. Student Portal - Always enabled, creates in-portal notifications
 * 2. Email - Optional, sends schedule PDF to student email
 * 3. Parent Portal - Optional, exports schedules to parent portal API
 *
 * Workflow:
 * 1. Publish schedule → makes it visible to students
 * 2. Notify students → portal/email notifications
 * 3. Export to parent portal → sync with parent portal system
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 30 - December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-distribution")
@RequiredArgsConstructor
public class ScheduleDistributionApiController {

    private final ScheduleDistributionService scheduleDistributionService;

    // ==================== Schedule Publication ====================

    @PostMapping("/schedules/{scheduleId}/publish")
    public ResponseEntity<Map<String, Object>> publishSchedule(@PathVariable Long scheduleId) {
        try {
            ScheduleDistributionService.SchedulePublicationResult result =
                scheduleDistributionService.publishSchedule(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("scheduleId", result.getScheduleId());
            response.put("scheduleName", result.getScheduleName());
            response.put("publishedDate", result.getPublishedDate());
            response.put("studentsAffected", result.getStudentsAffected());
            response.put("message", result.getMessage());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to publish schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/schedules/{scheduleId}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishSchedule(@PathVariable Long scheduleId) {
        try {
            ScheduleDistributionService.SchedulePublicationResult result =
                scheduleDistributionService.unpublishSchedule(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("scheduleId", result.getScheduleId());
            response.put("scheduleName", result.getScheduleName());
            response.put("message", result.getMessage());
            response.put("note", "Schedule is now hidden from students");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to unpublish schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Student Notification ====================

    @PostMapping("/schedules/{scheduleId}/notify-students")
    public ResponseEntity<Map<String, Object>> notifyStudents(@PathVariable Long scheduleId) {
        try {
            ScheduleDistributionService.NotificationResult result =
                scheduleDistributionService.notifyStudents(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("totalStudents", result.getTotalStudents());
            response.put("successCount", result.getSuccessCount());
            response.put("failureCount", result.getFailureCount());
            response.put("message", result.getMessage());

            if (result.getFailureCount() > 0) {
                response.put("errors", result.getErrors());
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to notify students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Email Distribution ====================

    @PostMapping("/students/{studentId}/schedules/{scheduleId}/send-email")
    public ResponseEntity<Map<String, Object>> sendScheduleEmail(
            @PathVariable Long studentId,
            @PathVariable Long scheduleId) {

        try {
            ScheduleDistributionService.EmailResult result =
                scheduleDistributionService.sendScheduleEmail(studentId, scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                response.put("recipient", result.getRecipient());
                response.put("note", "Schedule PDF attached");
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Parent Portal Integration ====================

    @PostMapping("/schedules/{scheduleId}/export-to-parent-portal")
    public ResponseEntity<Map<String, Object>> exportToParentPortal(@PathVariable Long scheduleId) {
        try {
            ScheduleDistributionService.ParentPortalExportResult result =
                scheduleDistributionService.exportScheduleToParentPortal(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("scheduleId", result.getScheduleId());
            response.put("studentsExported", result.getStudentsExported());
            response.put("exportTimestamp", result.getExportTimestamp());
            response.put("message", result.getMessage());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to export to parent portal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Bulk Distribution ====================

    @PostMapping("/schedules/{scheduleId}/distribute")
    public ResponseEntity<Map<String, Object>> distributeScheduleToAllStudents(@PathVariable Long scheduleId) {
        try {
            ScheduleDistributionService.DistributionResult result =
                scheduleDistributionService.distributeScheduleToAllStudents(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("scheduleId", result.getScheduleId());
            response.put("publishedSuccessfully", result.isPublishedSuccessfully());
            response.put("studentsNotified", result.getStudentsNotified());
            response.put("totalStudents", result.getTotalStudents());
            response.put("parentPortalExported", result.isParentPortalExported());
            response.put("distributionTimestamp", result.getDistributionTimestamp());
            response.put("message", result.getMessage());

            // Summary breakdown
            Map<String, Object> summary = new HashMap<>();
            summary.put("published", result.isPublishedSuccessfully());
            summary.put("portalNotifications", result.getStudentsNotified() + " students");
            summary.put("parentPortal", result.isParentPortalExported() ? "Exported" : "Not exported");

            response.put("summary", summary);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to distribute schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("distributionChannels", Map.of(
            "studentPortal", Map.of(
                "name", "Student Portal Notifications",
                "status", "Always enabled",
                "description", "Creates in-portal notifications for students"
            ),
            "email", Map.of(
                "name", "Email Distribution",
                "status", "Configurable",
                "description", "Sends schedule PDF via email",
                "requiresStudentEmail", true
            ),
            "parentPortal", Map.of(
                "name", "Parent Portal Integration",
                "status", "Configurable",
                "description", "Syncs schedules to parent portal API",
                "autoSync", false
            )
        ));

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Publish Schedule",
                "endpoint", "POST /schedules/{id}/publish",
                "description", "Make schedule visible to students"
            ),
            Map.of(
                "name", "Notify Students",
                "endpoint", "POST /schedules/{id}/notify-students",
                "description", "Send notifications via all enabled channels"
            ),
            Map.of(
                "name", "Full Distribution",
                "endpoint", "POST /schedules/{id}/distribute",
                "description", "Publish + Notify + Parent Portal export in one operation"
            )
        ));

        dashboard.put("workflow", List.of(
            "1. Publish schedule → makes visible to students",
            "2. Notify students → portal/email notifications",
            "3. Export to parent portal → sync with parent system",
            "OR use /distribute for all-in-one operation"
        ));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/configuration")
    public ResponseEntity<Map<String, Object>> getDistributionConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("channels", Map.of(
            "studentPortal", Map.of(
                "enabled", true,
                "readonly", true,
                "note", "Always enabled - primary distribution channel"
            ),
            "email", Map.of(
                "enabled", "Configurable via heronix.schedule.distribution.email-enabled",
                "default", false,
                "note", "Requires student email addresses"
            ),
            "parentPortal", Map.of(
                "enabled", "Configurable via heronix.schedule.distribution.parent-portal-enabled",
                "default", true,
                "note", "Requires parent portal API integration"
            )
        ));

        config.put("configurationProperties", List.of(
            "heronix.schedule.distribution.email-enabled (default: false)",
            "heronix.schedule.distribution.parent-portal-enabled (default: true)",
            "heronix.schedule.distribution.auto-publish (default: false)"
        ));

        return ResponseEntity.ok(config);
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/channels")
    public ResponseEntity<Map<String, Object>> getDistributionChannels() {
        Map<String, Object> channels = new HashMap<>();

        channels.put("channels", List.of(
            Map.of(
                "id", "STUDENT_PORTAL",
                "name", "Student Portal",
                "description", "In-portal notifications visible when students log in",
                "alwaysEnabled", true,
                "requiresConfiguration", false
            ),
            Map.of(
                "id", "EMAIL",
                "name", "Email Distribution",
                "description", "Send schedule PDF to student email address",
                "alwaysEnabled", false,
                "requiresConfiguration", true,
                "requirements", List.of("Email enabled in config", "Student has email address")
            ),
            Map.of(
                "id", "PARENT_PORTAL",
                "name", "Parent Portal",
                "description", "Export schedules to parent portal API for parent viewing",
                "alwaysEnabled", false,
                "requiresConfiguration", true,
                "requirements", List.of("Parent portal enabled in config", "Parent portal API integration")
            )
        ));

        return ResponseEntity.ok(channels);
    }

    @GetMapping("/reference/workflow")
    public ResponseEntity<Map<String, Object>> getWorkflow() {
        Map<String, Object> workflow = new HashMap<>();

        workflow.put("standardWorkflow", List.of(
            Map.of(
                "step", 1,
                "action", "Publish Schedule",
                "endpoint", "POST /schedules/{id}/publish",
                "description", "Mark schedule as published and visible to students"
            ),
            Map.of(
                "step", 2,
                "action", "Notify Students",
                "endpoint", "POST /schedules/{id}/notify-students",
                "description", "Send notifications through all enabled channels"
            ),
            Map.of(
                "step", 3,
                "action", "Export to Parent Portal (Optional)",
                "endpoint", "POST /schedules/{id}/export-to-parent-portal",
                "description", "Sync student schedules to parent portal system"
            )
        ));

        workflow.put("quickDistribution", Map.of(
            "endpoint", "POST /schedules/{id}/distribute",
            "description", "Executes all distribution steps in one operation",
            "includes", List.of("Publish", "Notify", "Parent Portal Export")
        ));

        workflow.put("individualEmail", Map.of(
            "endpoint", "POST /students/{studentId}/schedules/{scheduleId}/send-email",
            "description", "Send schedule PDF to individual student via email",
            "useCase", "Re-send to specific student or send after schedule changes"
        ));

        return ResponseEntity.ok(workflow);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 30");
        metadata.put("category", "Schedule Distribution");
        metadata.put("description", "Multi-channel schedule distribution to students and parents");

        metadata.put("capabilities", List.of(
            "Publish/unpublish schedules for student visibility",
            "Notify students via portal and email channels",
            "Send individual schedule PDFs via email",
            "Export schedules to parent portal system",
            "Bulk distribution with all channels",
            "Track notification success/failures"
        ));

        metadata.put("endpoints", Map.of(
            "publication", List.of("POST /schedules/{id}/publish", "POST /schedules/{id}/unpublish"),
            "notification", List.of("POST /schedules/{id}/notify-students"),
            "email", List.of("POST /students/{studentId}/schedules/{scheduleId}/send-email"),
            "parentPortal", List.of("POST /schedules/{id}/export-to-parent-portal"),
            "bulk", List.of("POST /schedules/{id}/distribute")
        ));

        metadata.put("distributionChannels", 3);
        metadata.put("configurable", true);

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Schedule Distribution Management API");

        help.put("commonWorkflows", Map.of(
            "fullDistribution", List.of(
                "1. POST /api/schedule-distribution/schedules/{scheduleId}/distribute",
                "2. This single call: publishes, notifies students, and exports to parent portal",
                "3. Check response for success counts and any errors"
            ),
            "stepByStep", List.of(
                "1. POST /api/schedule-distribution/schedules/{id}/publish",
                "2. POST /api/schedule-distribution/schedules/{id}/notify-students",
                "3. POST /api/schedule-distribution/schedules/{id}/export-to-parent-portal",
                "4. Review notification results for any failures"
            ),
            "individualEmail", List.of(
                "1. POST /api/schedule-distribution/students/{studentId}/schedules/{scheduleId}/send-email",
                "2. Use for re-sending to specific students or after schedule changes"
            ),
            "unpublish", List.of(
                "1. POST /api/schedule-distribution/schedules/{id}/unpublish",
                "2. Use to hide schedule from students (e.g., for major revisions)"
            )
        ));

        help.put("endpoints", Map.of(
            "publish", "POST /api/schedule-distribution/schedules/{id}/publish",
            "unpublish", "POST /api/schedule-distribution/schedules/{id}/unpublish",
            "notifyStudents", "POST /api/schedule-distribution/schedules/{id}/notify-students",
            "sendEmail", "POST /api/schedule-distribution/students/{studentId}/schedules/{scheduleId}/send-email",
            "exportToParentPortal", "POST /api/schedule-distribution/schedules/{id}/export-to-parent-portal",
            "distributeAll", "POST /api/schedule-distribution/schedules/{id}/distribute",
            "dashboard", "GET /api/schedule-distribution/dashboard"
        ));

        help.put("examples", Map.of(
            "quickDistribution", "curl -X POST http://localhost:9590/api/schedule-distribution/schedules/1/distribute",
            "publishOnly", "curl -X POST http://localhost:9590/api/schedule-distribution/schedules/1/publish",
            "notifyStudents", "curl -X POST http://localhost:9590/api/schedule-distribution/schedules/1/notify-students",
            "sendIndividualEmail", "curl -X POST http://localhost:9590/api/schedule-distribution/students/123/schedules/1/send-email",
            "viewChannels", "curl http://localhost:9590/api/schedule-distribution/reference/channels"
        ));

        help.put("configuration", Map.of(
            "emailEnabled", "Set heronix.schedule.distribution.email-enabled=true in application.properties",
            "parentPortalEnabled", "Set heronix.schedule.distribution.parent-portal-enabled=true",
            "autoPublish", "Set heronix.schedule.distribution.auto-publish=true for automatic publishing",
            "note", "Student portal notifications are always enabled and cannot be disabled"
        ));

        help.put("notes", Map.of(
            "studentPortal", "Always sends in-portal notifications - cannot be disabled",
            "emailRequirements", "Email distribution requires: 1) Email enabled in config, 2) Student has email address",
            "parentPortalIntegration", "Requires parent portal API integration and configuration",
            "notificationResults", "Check successCount and errors in notification results",
            "bulkOperation", "Use /distribute for one-call distribution to all channels"
        ));

        return ResponseEntity.ok(help);
    }
}
