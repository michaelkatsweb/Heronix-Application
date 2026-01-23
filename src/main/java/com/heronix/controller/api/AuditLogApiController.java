package com.heronix.controller.api;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.AuditLog.AuditAction;
import com.heronix.model.domain.AuditLog.AuditSeverity;
import com.heronix.repository.AuditLogRepository;
import com.heronix.service.AuditLogExportService;
import com.heronix.service.AuditLogExportService.ExportFormat;
import com.heronix.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller for Audit Logging and Security Monitoring
 *
 * Provides endpoints for querying audit logs, security incidents, and
 * compliance reporting (FERPA, authentication tracking, etc.).
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogApiController {

    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogExportService auditLogExportService;

    // ==================== Query Audit Logs ====================

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getAuditLogsForUser(@PathVariable String username) {
        List<AuditLog> logs = auditService.getAuditLogsForUser(username);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        List<AuditLog> logs = auditService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs(
            @RequestParam(defaultValue = "100") int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<AuditLog> logs = auditLogRepository.findAll(pageRequest).getContent();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.valueOf(action));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AuditLog>> getAuditLogsBySeverity(@PathVariable String severity) {
        List<AuditLog> logs = auditLogRepository.findBySeverityOrderByTimestampDesc(AuditSeverity.valueOf(severity));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
        return ResponseEntity.ok(logs);
    }

    // ==================== Authentication Logs ====================

    @GetMapping("/authentication/failed-logins/{username}")
    public ResponseEntity<List<AuditLog>> getFailedLoginAttempts(
            @PathVariable String username,
            @RequestParam(defaultValue = "30") int lastMinutes) {
        List<AuditLog> logs = auditService.getFailedLoginAttempts(username, lastMinutes);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/authentication/failed-logins/{username}/count")
    public ResponseEntity<Map<String, Object>> countFailedLoginAttempts(
            @PathVariable String username,
            @RequestParam(defaultValue = "30") int lastMinutes) {
        long count = auditService.countFailedLoginAttempts(username, lastMinutes);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("failedAttempts", count);
        response.put("lastMinutes", lastMinutes);
        response.put("threshold", 5);
        response.put("isLocked", count >= 5);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/authentication/failed-logins/ip/{ipAddress}/count")
    public ResponseEntity<Map<String, Object>> countFailedLoginAttemptsByIp(
            @PathVariable String ipAddress,
            @RequestParam(defaultValue = "30") int lastMinutes) {
        long count = auditService.countFailedLoginAttemptsByIp(ipAddress, lastMinutes);

        Map<String, Object> response = new HashMap<>();
        response.put("ipAddress", ipAddress);
        response.put("failedAttempts", count);
        response.put("lastMinutes", lastMinutes);
        response.put("threshold", 10);
        response.put("isBlocked", count >= 10);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/authentication/recent-logins")
    public ResponseEntity<List<AuditLog>> getRecentLogins(
            @RequestParam(defaultValue = "50") int limit) {
        List<AuditLog> allLogins = auditLogRepository.findByActionOrderByTimestampDesc(AuditAction.LOGIN_SUCCESS);
        List<AuditLog> recentLogins = allLogins.stream().limit(limit).collect(Collectors.toList());
        return ResponseEntity.ok(recentLogins);
    }

    // ==================== Security Incident Logs ====================

    @GetMapping("/security/incidents")
    public ResponseEntity<List<AuditLog>> getSecurityIncidents(
            @RequestParam(defaultValue = "24") int lastHours) {
        List<AuditLog> logs = auditService.getSecurityIncidents(lastHours);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/security/incidents/count")
    public ResponseEntity<Map<String, Object>> getSecurityIncidentsCount(
            @RequestParam(defaultValue = "24") int lastHours) {
        List<AuditLog> logs = auditService.getSecurityIncidents(lastHours);

        Map<String, Object> response = new HashMap<>();
        response.put("count", logs.size());
        response.put("lastHours", lastHours);
        response.put("incidents", logs);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/security/critical")
    public ResponseEntity<List<AuditLog>> getCriticalSecurityEvents(
            @RequestParam(defaultValue = "168") int lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours);
        List<AuditLog> logs = auditLogRepository.findBySeverityOrderByTimestampDesc(AuditSeverity.CRITICAL).stream()
            .filter(log -> log.getTimestamp().isAfter(since))
            .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/security/failed-actions")
    public ResponseEntity<List<AuditLog>> getFailedActions(
            @RequestParam(defaultValue = "24") int lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours);
        List<AuditLog> logs = auditLogRepository.findFailedActions(since);
        return ResponseEntity.ok(logs);
    }

    // ==================== FERPA Compliance Logs ====================

    @GetMapping("/compliance/student-access/{studentId}")
    public ResponseEntity<List<AuditLog>> getStudentAccessLogs(@PathVariable Long studentId) {
        List<AuditLog> logs = auditService.getAuditLogsForEntity("Student", studentId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/compliance/student-access/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getStudentAccessSummary(@PathVariable Long studentId) {
        List<AuditLog> logs = auditService.getAuditLogsForEntity("Student", studentId);

        long viewCount = logs.stream()
            .filter(log -> log.getAction() == AuditAction.STUDENT_VIEW)
            .count();

        long updateCount = logs.stream()
            .filter(log -> log.getAction() == AuditAction.STUDENT_UPDATE)
            .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("totalAccessEvents", logs.size());
        summary.put("viewCount", viewCount);
        summary.put("updateCount", updateCount);
        summary.put("recentAccess", logs.stream().limit(10).collect(Collectors.toList()));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/compliance/grade-modifications")
    public ResponseEntity<List<AuditLog>> getGradeModifications(
            @RequestParam(defaultValue = "30") int lastDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findGradeModifications(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/compliance/attendance-modifications")
    public ResponseEntity<List<AuditLog>> getAttendanceModifications(
            @RequestParam(defaultValue = "30") int lastDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findAttendanceModifications(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    // ==================== Statistics ====================

    @GetMapping("/stats/by-action")
    public ResponseEntity<Map<String, Long>> getStatsByAction(
            @RequestParam(defaultValue = "7") int lastDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        Map<String, Long> stats = new HashMap<>();
        for (AuditAction action : AuditAction.values()) {
            long count = logs.stream()
                .filter(log -> log.getAction() == action)
                .count();
            if (count > 0) {
                stats.put(action.name(), count);
            }
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/by-user")
    public ResponseEntity<Map<String, Long>> getStatsByUser(
            @RequestParam(defaultValue = "7") int lastDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        Map<String, Long> stats = new HashMap<>();
        logs.forEach(log -> {
            String username = log.getUsername();
            stats.put(username, stats.getOrDefault(username, 0L) + 1);
        });

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/by-severity")
    public ResponseEntity<Map<String, Long>> getStatsBySeverity(
            @RequestParam(defaultValue = "7") int lastDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        Map<String, Long> stats = new HashMap<>();
        for (AuditSeverity severity : AuditSeverity.values()) {
            long count = logs.stream()
                .filter(log -> log.getSeverity() == severity)
                .count();
            stats.put(severity.name(), count);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/user-actions/{username}")
    public ResponseEntity<Map<String, Object>> getUserActionStats(
            @PathVariable String username,
            @RequestParam(defaultValue = "7") int lastDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(lastDays);
        long count = auditLogRepository.countUserActions(username, since);

        Map<String, Object> stats = new HashMap<>();
        stats.put("username", username);
        stats.put("actionCount", count);
        stats.put("periodDays", lastDays);

        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        // Recent activity (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime now = LocalDateTime.now();
        List<AuditLog> recentLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(last24Hours, now);

        dashboard.put("totalEvents24h", recentLogs.size());

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<AuditLog> recent20 = auditLogRepository.findAll(pageRequest).getContent();
        dashboard.put("recentEvents", recent20);

        // Security incidents (last 24 hours)
        List<AuditLog> securityIncidents = auditService.getSecurityIncidents(24);
        dashboard.put("securityIncidents24h", securityIncidents.size());

        // Failed login attempts (last 30 minutes)
        LocalDateTime last30Min = LocalDateTime.now().minusMinutes(30);
        long failedLogins = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(last30Min, now).stream()
            .filter(log -> log.getAction() == AuditAction.LOGIN_FAILURE)
            .count();
        dashboard.put("failedLogins30m", failedLogins);

        // Critical events (last 7 days)
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        long criticalEvents = auditLogRepository.findHighSeverityLogs(last7Days, now).stream()
            .filter(log -> log.getSeverity() == AuditSeverity.CRITICAL)
            .count();
        dashboard.put("criticalEvents7d", criticalEvents);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/security-summary")
    public ResponseEntity<Map<String, Object>> getSecuritySummary() {
        Map<String, Object> summary = new HashMap<>();

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime now = LocalDateTime.now();

        // Security incidents
        List<AuditLog> incidents = auditService.getSecurityIncidents(24);
        summary.put("securityIncidents", incidents.size());
        summary.put("incidentDetails", incidents);

        // Authentication events
        List<AuditLog> recentLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(last24Hours, now);

        long failedLogins = recentLogs.stream()
            .filter(log -> log.getAction() == AuditAction.LOGIN_FAILURE)
            .count();
        long successfulLogins = recentLogs.stream()
            .filter(log -> log.getAction() == AuditAction.LOGIN_SUCCESS)
            .count();

        summary.put("failedLogins24h", failedLogins);
        summary.put("successfulLogins24h", successfulLogins);

        // Critical events
        List<AuditLog> criticalEvents = auditLogRepository.findHighSeverityLogs(last24Hours, now).stream()
            .filter(log -> log.getSeverity() == AuditSeverity.CRITICAL)
            .collect(Collectors.toList());
        summary.put("criticalEvents", criticalEvents.size());
        summary.put("criticalEventDetails", criticalEvents);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/user-activity/{username}")
    public ResponseEntity<Map<String, Object>> getUserActivityDashboard(@PathVariable String username) {
        Map<String, Object> dashboard = new HashMap<>();

        List<AuditLog> userLogs = auditService.getAuditLogsForUser(username);

        dashboard.put("username", username);
        dashboard.put("totalEvents", userLogs.size());
        dashboard.put("recentActivity", userLogs.stream().limit(20).collect(Collectors.toList()));

        // Activity by action type
        Map<String, Long> actionBreakdown = new HashMap<>();
        userLogs.forEach(log -> {
            String action = log.getAction().name();
            actionBreakdown.put(action, actionBreakdown.getOrDefault(action, 0L) + 1);
        });
        dashboard.put("actionBreakdown", actionBreakdown);

        // Failed actions
        long failedActions = userLogs.stream()
            .filter(log -> !log.isSuccess())
            .count();
        dashboard.put("failedActions", failedActions);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/compliance-report")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @RequestParam(defaultValue = "30") int lastDays) {
        Map<String, Object> report = new HashMap<>();

        LocalDateTime startDate = LocalDateTime.now().minusDays(lastDays);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        // Student data access (FERPA compliance)
        long studentViewCount = logs.stream()
            .filter(log -> log.getAction() == AuditAction.STUDENT_VIEW)
            .count();
        long studentUpdateCount = logs.stream()
            .filter(log -> log.getAction() == AuditAction.STUDENT_UPDATE)
            .count();

        report.put("reportPeriodDays", lastDays);
        report.put("studentViews", studentViewCount);
        report.put("studentUpdates", studentUpdateCount);

        // Grade modifications
        long gradeUpdates = logs.stream()
            .filter(log -> log.getAction() == AuditAction.GRADE_UPDATE)
            .count();
        report.put("gradeModifications", gradeUpdates);

        // Authentication activity
        long loginSuccess = logs.stream()
            .filter(log -> log.getAction() == AuditAction.LOGIN_SUCCESS)
            .count();
        long loginFailure = logs.stream()
            .filter(log -> log.getAction() == AuditAction.LOGIN_FAILURE)
            .count();

        report.put("successfulLogins", loginSuccess);
        report.put("failedLogins", loginFailure);

        // Security events
        long securityEvents = logs.stream()
            .filter(log -> log.getSeverity() == AuditSeverity.CRITICAL ||
                          log.getSeverity() == AuditSeverity.ERROR)
            .count();
        report.put("securityEvents", securityEvents);

        return ResponseEntity.ok(report);
    }

    // ==================== Export Endpoints ====================

    /**
     * Export audit logs to CSV format
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCSV(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String severity) {
        try {
            LocalDateTime start = parseDate(startDate, true);
            LocalDateTime end = parseDate(endDate, false);
            AuditAction auditAction = action != null && !action.isEmpty() ? AuditAction.valueOf(action) : null;
            AuditSeverity auditSeverity = severity != null && !severity.isEmpty() ? AuditSeverity.valueOf(severity) : null;

            byte[] data = auditLogExportService.exportWithFilters(start, end, username, auditAction, auditSeverity, ExportFormat.CSV);
            String filename = auditLogExportService.generateFilename(ExportFormat.CSV);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
        } catch (Exception e) {
            log.error("Failed to export audit logs to CSV", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export audit logs to JSON format
     */
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportToJSON(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String severity) {
        try {
            LocalDateTime start = parseDate(startDate, true);
            LocalDateTime end = parseDate(endDate, false);
            AuditAction auditAction = action != null && !action.isEmpty() ? AuditAction.valueOf(action) : null;
            AuditSeverity auditSeverity = severity != null && !severity.isEmpty() ? AuditSeverity.valueOf(severity) : null;

            byte[] data = auditLogExportService.exportWithFilters(start, end, username, auditAction, auditSeverity, ExportFormat.JSON);
            String filename = auditLogExportService.generateFilename(ExportFormat.JSON);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
        } catch (Exception e) {
            log.error("Failed to export audit logs to JSON", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export audit logs to PDF format
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPDF(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String severity) {
        try {
            LocalDateTime start = parseDate(startDate, true);
            LocalDateTime end = parseDate(endDate, false);
            AuditAction auditAction = action != null && !action.isEmpty() ? AuditAction.valueOf(action) : null;
            AuditSeverity auditSeverity = severity != null && !severity.isEmpty() ? AuditSeverity.valueOf(severity) : null;

            byte[] data = auditLogExportService.exportWithFilters(start, end, username, auditAction, auditSeverity, ExportFormat.PDF);
            String filename = auditLogExportService.generateFilename(ExportFormat.PDF);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
        } catch (Exception e) {
            log.error("Failed to export audit logs to PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get export preview (count of records that would be exported)
     */
    @GetMapping("/export/preview")
    public ResponseEntity<Map<String, Object>> getExportPreview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String severity) {
        try {
            LocalDateTime start = parseDate(startDate, true);
            LocalDateTime end = parseDate(endDate, false);
            AuditAction auditAction = action != null && !action.isEmpty() ? AuditAction.valueOf(action) : null;
            AuditSeverity auditSeverity = severity != null && !severity.isEmpty() ? AuditSeverity.valueOf(severity) : null;

            long count = auditLogExportService.getFilteredCount(start, end, username, auditAction, auditSeverity);

            Map<String, Object> preview = new HashMap<>();
            preview.put("recordCount", count);
            preview.put("filters", Map.of(
                "startDate", start != null ? start.toString() : "none",
                "endDate", end != null ? end.toString() : "none",
                "username", username != null ? username : "all",
                "action", action != null ? action : "all",
                "severity", severity != null ? severity : "all"
            ));

            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Failed to get export preview", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper method to parse date strings
     */
    private LocalDateTime parseDate(String dateStr, boolean isStartDate) {
        if (dateStr == null || dateStr.isEmpty()) {
            return isStartDate ? LocalDateTime.now().minusDays(30) : LocalDateTime.now();
        }
        try {
            // Try full datetime format first
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            try {
                // Try date-only format
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                return isStartDate ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
            } catch (Exception e2) {
                return isStartDate ? LocalDateTime.now().minusDays(30) : LocalDateTime.now();
            }
        }
    }
}
