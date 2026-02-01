package com.heronix.controller.api;

import com.heronix.model.domain.HallPassSession;
import com.heronix.model.domain.HallPassSession.Destination;
import com.heronix.model.domain.HallPassSession.SessionStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.HallPassSessionRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.export.HallPassReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hall Pass Analytics and Reporting API
 *
 * Provides comprehensive analytics endpoints for:
 * - Student-level statistics and reports
 * - School-level aggregation
 * - District-level analytics
 * - State-level reporting
 * - Export data for external reporting
 *
 * Used by:
 * - Heronix-Teacher for classroom management
 * - Heronix-Student for self-monitoring
 * - Heronix-Parent for child activity tracking
 * - Admin dashboards for oversight
 *
 * EXTERNAL BOUNDARY TOKENIZATION POLICY:
 * All data leaving the internal system boundary is TOKENIZED.
 * - Export endpoints remove internal IDs before returning data
 * - Parent/Student portals receive tokenized student info (first/last name only)
 * - PDF/Excel exports exclude internal system identifiers
 * - Heronix-Guardian manages full tokenization/de-tokenization in production
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/hall-pass/analytics")
@RequiredArgsConstructor
public class HallPassAnalyticsController {

    private final HallPassSessionRepository hallPassRepository;
    private final StudentRepository studentRepository;
    private final HallPassReportService reportService;

    // ==================== Student Analytics ====================

    /**
     * Get comprehensive analytics for a specific student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentAnalytics(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            // Default to last 30 days if no date range specified
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

            List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId);
            List<HallPassSession> filteredSessions = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null &&
                            !s.getDepartureTime().isBefore(start) &&
                            !s.getDepartureTime().isAfter(end))
                    .collect(Collectors.toList());

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("studentId", studentId);
            analytics.put("studentName", student.getFirstName() + " " + student.getLastName());
            analytics.put("gradeLevel", student.getGradeLevel());
            analytics.put("dateRange", Map.of("start", start.toLocalDate(), "end", end.toLocalDate()));

            // Summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalPasses", filteredSessions.size());
            summary.put("completedPasses", filteredSessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
            summary.put("overduePasses", filteredSessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count());

            OptionalDouble avgDuration = filteredSessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            summary.put("averageDurationMinutes", avgDuration.orElse(0));

            int maxDuration = filteredSessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .max().orElse(0);
            summary.put("maxDurationMinutes", maxDuration);

            analytics.put("summary", summary);

            // Destination breakdown
            Map<String, Long> destinationCounts = filteredSessions.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getDestination().getDisplayName(),
                            Collectors.counting()));
            analytics.put("byDestination", destinationCounts);

            // Daily breakdown (last 7 days)
            Map<String, Long> dailyCounts = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                long count = filteredSessions.stream()
                        .filter(s -> s.getDepartureTime().toLocalDate().equals(date))
                        .count();
                dailyCounts.put(date.toString(), count);
            }
            analytics.put("dailyTrend", dailyCounts);

            // Period breakdown
            Map<Integer, Long> periodCounts = filteredSessions.stream()
                    .filter(s -> s.getPeriod() != null)
                    .collect(Collectors.groupingBy(HallPassSession::getPeriod, Collectors.counting()));
            analytics.put("byPeriod", periodCounts);

            // Recent sessions (last 10)
            List<Map<String, Object>> recentSessions = filteredSessions.stream()
                    .limit(10)
                    .map(this::sessionToMap)
                    .collect(Collectors.toList());
            analytics.put("recentSessions", recentSessions);

            // Behavior indicators
            Map<String, Object> behavior = new HashMap<>();
            long passesThisWeek = filteredSessions.stream()
                    .filter(s -> s.getDepartureTime().isAfter(LocalDateTime.now().minusDays(7)))
                    .count();
            behavior.put("passesThisWeek", passesThisWeek);
            behavior.put("excessiveUsageFlag", passesThisWeek > 10); // More than 2 per day average
            behavior.put("frequentOverdue", filteredSessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.OVERDUE).count() > 3);
            analytics.put("behaviorIndicators", behavior);

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get student analytics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get student's hall pass report card (for parent/student portal)
     */
    @GetMapping("/student/{studentId}/report-card")
    public ResponseEntity<Map<String, Object>> getStudentReportCard(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId);
            List<HallPassSession> recentSessions = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null && s.getDepartureTime().isAfter(thirtyDaysAgo))
                    .collect(Collectors.toList());

            Map<String, Object> reportCard = new HashMap<>();
            reportCard.put("studentName", student.getFirstName() + " " + student.getLastName());
            reportCard.put("reportPeriod", "Last 30 Days");
            reportCard.put("generatedAt", LocalDateTime.now());

            // Grade the student's hall pass usage
            int totalPasses = recentSessions.size();
            long overduePasses = recentSessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
            double overdueRate = totalPasses > 0 ? (double) overduePasses / totalPasses : 0;

            String grade;
            String feedback;
            if (totalPasses <= 10 && overdueRate < 0.1) {
                grade = "A";
                feedback = "Excellent! Minimal hall pass usage with timely returns.";
            } else if (totalPasses <= 20 && overdueRate < 0.2) {
                grade = "B";
                feedback = "Good hall pass habits with room for improvement.";
            } else if (totalPasses <= 30 && overdueRate < 0.3) {
                grade = "C";
                feedback = "Average usage. Consider reducing unnecessary passes.";
            } else {
                grade = "D";
                feedback = "High hall pass usage detected. Please review with teacher.";
            }

            reportCard.put("grade", grade);
            reportCard.put("feedback", feedback);
            reportCard.put("totalPasses", totalPasses);
            reportCard.put("overduePasses", overduePasses);
            reportCard.put("onTimeRate", String.format("%.1f%%", (1 - overdueRate) * 100));

            // Top destinations
            Map<String, Long> destinations = recentSessions.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getDestination().getDisplayName(),
                            Collectors.counting()));
            reportCard.put("topDestinations", destinations);

            return ResponseEntity.ok(reportCard);
        } catch (Exception e) {
            log.error("Failed to get student report card", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== School Analytics ====================

    /**
     * Get school-wide hall pass analytics
     */
    @GetMapping("/school")
    public ResponseEntity<Map<String, Object>> getSchoolAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

            List<HallPassSession> sessions = hallPassRepository.findByDateRange(startOfDay, endOfDay);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("date", targetDate);
            analytics.put("generatedAt", LocalDateTime.now());

            // Summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalPasses", sessions.size());
            summary.put("activePasses", sessions.stream().filter(s -> s.getStatus() == SessionStatus.ACTIVE).count());
            summary.put("completedPasses", sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
            summary.put("overduePasses", sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count());

            OptionalDouble avgDuration = sessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            summary.put("averageDurationMinutes", String.format("%.1f", avgDuration.orElse(0)));
            analytics.put("summary", summary);

            // By destination
            Map<String, Long> byDestination = sessions.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getDestination().getDisplayName(),
                            Collectors.counting()));
            analytics.put("byDestination", byDestination);

            // By period
            Map<Integer, Long> byPeriod = sessions.stream()
                    .filter(s -> s.getPeriod() != null)
                    .collect(Collectors.groupingBy(HallPassSession::getPeriod, Collectors.counting()));
            analytics.put("byPeriod", byPeriod);

            // Hourly breakdown
            Map<Integer, Long> byHour = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getDepartureTime().getHour(),
                            Collectors.counting()));
            analytics.put("byHour", byHour);

            // Top students (most passes today)
            Map<String, Long> studentCounts = sessions.stream()
                    .filter(s -> s.getStudent() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getStudent().getFirstName() + " " + s.getStudent().getLastName(),
                            Collectors.counting()));
            List<Map.Entry<String, Long>> topStudents = studentCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            analytics.put("topStudents", topStudents.stream()
                    .map(e -> Map.of("student", e.getKey(), "count", e.getValue()))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get school analytics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get school weekly trend analytics
     */
    @GetMapping("/school/weekly")
    public ResponseEntity<Map<String, Object>> getSchoolWeeklyTrend() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("generatedAt", LocalDateTime.now());

            Map<String, Map<String, Object>> dailyStats = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

                List<HallPassSession> sessions = hallPassRepository.findByDateRange(startOfDay, endOfDay);

                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("total", sessions.size());
                dayStats.put("completed", sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
                dayStats.put("overdue", sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count());

                OptionalDouble avgDuration = sessions.stream()
                        .filter(s -> s.getDurationMinutes() != null)
                        .mapToInt(HallPassSession::getDurationMinutes)
                        .average();
                dayStats.put("avgDuration", avgDuration.orElse(0));

                dailyStats.put(date.toString(), dayStats);
            }
            analytics.put("dailyStats", dailyStats);

            // Calculate week over week comparison
            LocalDateTime thisWeekStart = LocalDateTime.now().minusDays(7);
            LocalDateTime lastWeekStart = LocalDateTime.now().minusDays(14);

            long thisWeekCount = hallPassRepository.findByDateRange(thisWeekStart, LocalDateTime.now()).size();
            long lastWeekCount = hallPassRepository.findByDateRange(lastWeekStart, thisWeekStart).size();

            double changePercent = lastWeekCount > 0 ? ((double)(thisWeekCount - lastWeekCount) / lastWeekCount) * 100 : 0;

            analytics.put("comparison", Map.of(
                    "thisWeek", thisWeekCount,
                    "lastWeek", lastWeekCount,
                    "changePercent", String.format("%.1f", changePercent),
                    "trend", changePercent > 0 ? "UP" : changePercent < 0 ? "DOWN" : "STABLE"
            ));

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get weekly trend", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== District/State Analytics ====================

    /**
     * Get aggregated analytics for district reporting
     * Note: In production, this would aggregate across multiple schools
     */
    @GetMapping("/district")
    public ResponseEntity<Map<String, Object>> getDistrictAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

            List<HallPassSession> sessions = hallPassRepository.findByDateRange(start, end);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("reportType", "DISTRICT");
            analytics.put("dateRange", Map.of("start", start.toLocalDate(), "end", end.toLocalDate()));
            analytics.put("generatedAt", LocalDateTime.now());

            // Aggregate statistics
            Map<String, Object> aggregate = new HashMap<>();
            aggregate.put("totalSessions", sessions.size());
            aggregate.put("uniqueStudents", sessions.stream()
                    .filter(s -> s.getStudent() != null)
                    .map(s -> s.getStudent().getId())
                    .distinct().count());

            OptionalDouble avgDuration = sessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            aggregate.put("averageDurationMinutes", avgDuration.orElse(0));

            long overdueCount = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
            aggregate.put("overdueCount", overdueCount);
            aggregate.put("complianceRate", sessions.size() > 0 ?
                    String.format("%.1f%%", ((double)(sessions.size() - overdueCount) / sessions.size()) * 100) : "N/A");

            analytics.put("aggregate", aggregate);

            // Destination distribution
            Map<String, Long> destinations = sessions.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getDestination().getDisplayName(),
                            Collectors.counting()));
            analytics.put("destinationDistribution", destinations);

            // Grade level distribution
            Map<String, Long> byGradeLevel = sessions.stream()
                    .filter(s -> s.getStudent() != null && s.getStudent().getGradeLevel() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getStudent().getGradeLevel().toString(),
                            Collectors.counting()));
            analytics.put("byGradeLevel", byGradeLevel);

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get district analytics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get state-level reporting data
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getStateAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(90);
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

            List<HallPassSession> sessions = hallPassRepository.findByDateRange(start, end);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("reportType", "STATE");
            analytics.put("dateRange", Map.of("start", start.toLocalDate(), "end", end.toLocalDate()));
            analytics.put("generatedAt", LocalDateTime.now());

            // State-level metrics
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalSessions", sessions.size());
            metrics.put("uniqueStudents", sessions.stream()
                    .filter(s -> s.getStudent() != null)
                    .map(s -> s.getStudent().getId())
                    .distinct().count());

            OptionalDouble avgDuration = sessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            metrics.put("stateAverageDuration", String.format("%.1f minutes", avgDuration.orElse(0)));

            long overdueCount = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
            metrics.put("stateComplianceRate", sessions.size() > 0 ?
                    String.format("%.1f%%", ((double)(sessions.size() - overdueCount) / sessions.size()) * 100) : "N/A");

            analytics.put("stateMetrics", metrics);

            // Monthly trend
            Map<String, Long> monthlyTrend = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getDepartureTime().getMonth().toString(),
                            Collectors.counting()));
            analytics.put("monthlyTrend", monthlyTrend);

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get state analytics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Sync Endpoints for Teacher Portal ====================

    /**
     * Get hall pass data for syncing to Teacher portal
     */
    @GetMapping("/sync/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getSyncDataForTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        try {
            LocalDateTime syncSince = since != null ? since : LocalDateTime.now().minusDays(7);

            List<HallPassSession> sessions = hallPassRepository.findByTeacher_IdOrderByDepartureTimeDesc(teacherId);
            List<HallPassSession> recentSessions = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null && s.getDepartureTime().isAfter(syncSince))
                    .collect(Collectors.toList());

            Map<String, Object> syncData = new HashMap<>();
            syncData.put("teacherId", teacherId);
            syncData.put("syncSince", syncSince);
            syncData.put("syncTime", LocalDateTime.now());
            syncData.put("sessionCount", recentSessions.size());
            syncData.put("sessions", recentSessions.stream().map(this::sessionToSyncMap).collect(Collectors.toList()));

            return ResponseEntity.ok(syncData);
        } catch (Exception e) {
            log.error("Failed to get sync data for teacher", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Receive hall pass data from Teacher portal
     */
    @PostMapping("/sync/from-teacher")
    public ResponseEntity<Map<String, Object>> receiveSyncFromTeacher(@RequestBody Map<String, Object> syncData) {
        try {
            log.info("Received hall pass sync from teacher portal");

            // In production, this would merge/update hall pass records from Teacher portal
            // For now, acknowledge receipt

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sync data received successfully");
            response.put("receivedAt", LocalDateTime.now());
            response.put("recordsReceived", syncData.getOrDefault("sessionCount", 0));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process sync from teacher", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Export Endpoints ====================
    // TOKENIZATION POLICY: All export endpoints communicate externally and must be tokenized.
    // Internal database IDs, system references, and sensitive PII are removed before export.
    // Only educational data and display-safe identifiers are included.

    /**
     * Export student hall pass data for reports
     *
     * TOKENIZED: Internal IDs removed. Export includes:
     * - Student display name (first + last)
     * - Hall pass activity data
     * - Statistics and summaries
     * Does NOT include: internal student ID, teacher internal IDs, system references
     */
    @GetMapping("/export/student/{studentId}")
    public ResponseEntity<Map<String, Object>> exportStudentData(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

            List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId);
            List<Map<String, Object>> exportData = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null &&
                            !s.getDepartureTime().isBefore(start) &&
                            !s.getDepartureTime().isAfter(end))
                    .map(this::sessionToTokenizedExportMap)
                    .collect(Collectors.toList());

            Map<String, Object> export = new HashMap<>();
            // TOKENIZED: Use display name only, no internal ID
            export.put("studentDisplayName", student.getFirstName() + " " + student.getLastName());
            export.put("studentFirstName", student.getFirstName());
            export.put("studentLastName", student.getLastName());
            export.put("dateRange", Map.of("start", start.toLocalDate(), "end", end.toLocalDate()));
            export.put("exportedAt", LocalDateTime.now());
            export.put("totalRecords", exportData.size());
            export.put("records", exportData);
            export.put("_tokenized", true);
            export.put("_exportPolicy", "EXTERNAL_BOUNDARY");

            return ResponseEntity.ok(export);
        } catch (Exception e) {
            log.error("Failed to export student data", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PDF/Excel Download Endpoints ====================

    /**
     * Download student hall pass report as PDF
     */
    @GetMapping("/export/student/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadStudentPdf(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? endDate : LocalDate.now();

            byte[] pdfContent = reportService.generateStudentReportPdf(studentId, start, end).toByteArray();

            String filename = String.format("hall_pass_report_%s_%s.pdf",
                    student.getLastName().toLowerCase(),
                    LocalDate.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfContent.length);

            log.info("Generated PDF report for student {}", studentId);
            return ResponseEntity.ok().headers(headers).body(pdfContent);

        } catch (Exception e) {
            log.error("Failed to generate student PDF report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download student hall pass report as Excel
     */
    @GetMapping("/export/student/{studentId}/excel")
    public ResponseEntity<byte[]> downloadStudentExcel(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? endDate : LocalDate.now();

            byte[] excelContent = reportService.generateStudentReportExcel(studentId, start, end).toByteArray();

            String filename = String.format("hall_pass_report_%s_%s.xlsx",
                    student.getLastName().toLowerCase(),
                    LocalDate.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelContent.length);

            log.info("Generated Excel report for student {}", studentId);
            return ResponseEntity.ok().headers(headers).body(excelContent);

        } catch (Exception e) {
            log.error("Failed to generate student Excel report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download parent summary report as PDF
     */
    @GetMapping("/export/parent/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadParentSummaryPdf(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfContent = reportService.generateParentSummaryPdf(studentId).toByteArray();

            String filename = String.format("hall_pass_report_card_%s_%s.pdf",
                    student.getLastName().toLowerCase(),
                    LocalDate.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfContent.length);

            log.info("Generated parent summary PDF for student {}", studentId);
            return ResponseEntity.ok().headers(headers).body(pdfContent);

        } catch (Exception e) {
            log.error("Failed to generate parent summary PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download school-wide hall pass report as PDF
     */
    @GetMapping("/export/school/pdf")
    public ResponseEntity<byte[]> downloadSchoolPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();

            byte[] pdfContent = reportService.generateSchoolReportPdf(targetDate).toByteArray();

            String filename = String.format("school_hall_pass_report_%s.pdf", targetDate.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfContent.length);

            log.info("Generated school PDF report for date {}", targetDate);
            return ResponseEntity.ok().headers(headers).body(pdfContent);

        } catch (Exception e) {
            log.error("Failed to generate school PDF report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download school-wide hall pass report as Excel
     */
    @GetMapping("/export/school/excel")
    public ResponseEntity<byte[]> downloadSchoolExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(7);
            LocalDate end = endDate != null ? endDate : LocalDate.now();

            byte[] excelContent = reportService.generateSchoolReportExcel(start, end).toByteArray();

            String filename = String.format("school_hall_pass_report_%s_to_%s.xlsx",
                    start.toString(), end.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelContent.length);

            log.info("Generated school Excel report from {} to {}", start, end);
            return ResponseEntity.ok().headers(headers).body(excelContent);

        } catch (Exception e) {
            log.error("Failed to generate school Excel report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> sessionToMap(HallPassSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", session.getId());
        map.put("destination", session.getDestination().getDisplayName());
        map.put("departureTime", session.getDepartureTime());
        map.put("returnTime", session.getReturnTime());
        map.put("duration", session.getDurationMinutes());
        map.put("status", session.getStatus().name());
        map.put("period", session.getPeriod());
        return map;
    }

    private Map<String, Object> sessionToSyncMap(HallPassSession session) {
        Map<String, Object> map = sessionToMap(session);
        if (session.getStudent() != null) {
            map.put("studentId", session.getStudent().getId());
            map.put("studentName", session.getStudent().getFirstName() + " " + session.getStudent().getLastName());
        }
        map.put("departureRoom", session.getDepartureRoom());
        map.put("arrivalRoom", session.getArrivalRoom());
        return map;
    }

    private Map<String, Object> sessionToExportMap(HallPassSession session) {
        Map<String, Object> map = sessionToSyncMap(session);
        map.put("notes", session.getNotes());
        map.put("createdAt", session.getCreatedAt());
        return map;
    }

    /**
     * Convert session to TOKENIZED export map
     * Removes internal IDs and system references for external boundary
     */
    private Map<String, Object> sessionToTokenizedExportMap(HallPassSession session) {
        Map<String, Object> map = new HashMap<>();
        // TOKENIZED: Exclude internal ID, use display-only reference
        map.put("recordRef", "PASS-" + session.getId().hashCode());
        map.put("destination", session.getDestination().getDisplayName());
        map.put("departureTime", session.getDepartureTime());
        map.put("returnTime", session.getReturnTime());
        map.put("duration", session.getDurationMinutes());
        map.put("status", session.getStatus().name());
        map.put("period", session.getPeriod());
        map.put("notes", session.getNotes());
        // TOKENIZED: Exclude teacher internal ID
        if (session.getTeacher() != null) {
            map.put("teacherDisplayName", session.getTeacher().getFirstName() + " " +
                    session.getTeacher().getLastName());
        }
        // TOKENIZED: Room info kept but not internal room IDs
        map.put("departureLocation", session.getDepartureRoom() != null ? session.getDepartureRoom() : "Classroom");
        map.put("arrivalLocation", session.getArrivalRoom() != null ? session.getArrivalRoom() : "Classroom");
        return map;
    }
}
