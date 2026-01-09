package com.heronix.controller.api;

import com.heronix.service.StudentSchedulePrintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Student Schedule Printing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/student-schedules")
@RequiredArgsConstructor
public class StudentSchedulePrintApiController {

    private final StudentSchedulePrintService schedulePrintService;

    // ==================== Single Student Schedule Generation ====================

    @GetMapping("/student/{studentId}/schedule/{scheduleId}/pdf")
    public ResponseEntity<byte[]> generateSchedulePDF(
            @PathVariable Long studentId,
            @PathVariable Long scheduleId) {

        byte[] pdf = schedulePrintService.generateSchedulePDF(studentId, scheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "schedule_student_" + studentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/student/{studentId}/schedule/{scheduleId}/html")
    public ResponseEntity<String> generateScheduleHTML(
            @PathVariable Long studentId,
            @PathVariable Long scheduleId) {

        String html = schedulePrintService.generateScheduleHTML(studentId, scheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        return ResponseEntity.ok()
                .headers(headers)
                .body(html);
    }

    @GetMapping("/student/{studentId}/schedule/{scheduleId}/text")
    public ResponseEntity<String> generateScheduleText(
            @PathVariable Long studentId,
            @PathVariable Long scheduleId) {

        String text = schedulePrintService.generateScheduleText(studentId, scheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok()
                .headers(headers)
                .body(text);
    }

    // ==================== Bulk Schedule Generation ====================

    @GetMapping("/grade/{gradeLevel}/schedule/{scheduleId}/pdf")
    public ResponseEntity<Map<String, Object>> generateSchedulesForGrade(
            @PathVariable String gradeLevel,
            @PathVariable Long scheduleId) {

        Map<Long, byte[]> schedules = schedulePrintService.generateSchedulesForGrade(gradeLevel, scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("gradeLevel", gradeLevel);
        response.put("scheduleId", scheduleId);
        response.put("totalGenerated", schedules.size());
        response.put("message", "Generated " + schedules.size() + " schedules for grade " + gradeLevel);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-students/schedule/{scheduleId}/pdf")
    public ResponseEntity<Map<String, Object>> generateSchedulesForAllStudents(
            @PathVariable Long scheduleId) {

        Map<Long, byte[]> schedules = schedulePrintService.generateSchedulesForAllStudents(scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("totalGenerated", schedules.size());
        response.put("message", "Generated " + schedules.size() + " schedules for all active students");

        return ResponseEntity.ok(response);
    }

    // ==================== Batch Download Operations ====================

    @PostMapping("/grade/{gradeLevel}/schedule/{scheduleId}/download-all")
    public ResponseEntity<Map<String, Object>> downloadAllGradeSchedules(
            @PathVariable String gradeLevel,
            @PathVariable Long scheduleId) {

        Map<Long, byte[]> schedules = schedulePrintService.generateSchedulesForGrade(gradeLevel, scheduleId);

        // In a real implementation, this would create a ZIP file containing all PDFs
        // For now, return metadata about the generation
        Map<String, Object> response = new HashMap<>();
        response.put("gradeLevel", gradeLevel);
        response.put("scheduleId", scheduleId);
        response.put("totalSchedules", schedules.size());
        response.put("status", "Schedules generated successfully");
        response.put("message", "In production, this would return a ZIP file with all " + schedules.size() + " PDF schedules");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/all-students/schedule/{scheduleId}/download-all")
    public ResponseEntity<Map<String, Object>> downloadAllStudentSchedules(
            @PathVariable Long scheduleId) {

        Map<Long, byte[]> schedules = schedulePrintService.generateSchedulesForAllStudents(scheduleId);

        // In a real implementation, this would create a ZIP file containing all PDFs
        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("totalSchedules", schedules.size());
        response.put("status", "Schedules generated successfully");
        response.put("message", "In production, this would return a ZIP file with all " + schedules.size() + " PDF schedules");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/generation-status")
    public ResponseEntity<Map<String, Object>> getGenerationStatus() {
        Map<String, Object> dashboard = new HashMap<>();

        // In a real implementation, this would track ongoing batch generation jobs
        dashboard.put("message", "No active generation jobs");
        dashboard.put("activeJobs", 0);
        dashboard.put("completedToday", 0);
        dashboard.put("failedToday", 0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/print-history")
    public ResponseEntity<Map<String, Object>> getPrintHistory() {
        Map<String, Object> dashboard = new HashMap<>();

        // In a real implementation, this would query a print history log
        dashboard.put("message", "Print history tracking not yet implemented");
        dashboard.put("totalPrints", 0);
        dashboard.put("recentPrints", new java.util.ArrayList<>());

        return ResponseEntity.ok(dashboard);
    }
}
