package com.heronix.controller.api;

import com.heronix.service.SmartTeacherAssignmentService;
import com.heronix.service.SmartTeacherAssignmentService.AssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Smart Teacher Assignment
 *
 * Provides endpoints for intelligent automatic teacher assignment to courses using:
 * - Strict subject certification matching
 * - Workload balancing (2-3 courses per teacher)
 * - Course sequencing (English 1 → English 2 same teacher)
 * - Teacher shortage handling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/smart-teacher-assignment")
@RequiredArgsConstructor
public class SmartTeacherAssignmentApiController {

    private final SmartTeacherAssignmentService smartTeacherAssignmentService;

    // ==================== Assignment Operations ====================

    @PostMapping("/assign-all")
    public ResponseEntity<Map<String, Object>> assignAllTeachers() {
        try {
            AssignmentResult result = smartTeacherAssignmentService.smartAssignAllTeachers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccessful());
            response.put("message", result.getMessage());
            response.put("totalCourses", result.getTotalCoursesProcessed());
            response.put("coursesAssigned", result.getCoursesAssigned());
            response.put("coursesFailed", result.getCoursesFailed());
            response.put("durationMs", result.getDurationMs());
            response.put("hasWarnings", result.hasWarnings());
            response.put("hasErrors", result.hasErrors());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewAssignments() {
        try {
            AssignmentResult result = smartTeacherAssignmentService.previewTeacherAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("preview", true);
            response.put("message", result.getMessage());
            response.put("totalCourses", result.getTotalCoursesProcessed());
            response.put("wouldAssign", result.getCoursesAssigned());
            response.put("wouldFail", result.getCoursesFailed());
            response.put("successRate", result.getTotalCoursesProcessed() > 0 ?
                (result.getCoursesAssigned() * 100.0 / result.getTotalCoursesProcessed()) : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Status & Statistics ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAssignmentStatus() {
        AssignmentResult preview = smartTeacherAssignmentService.previewTeacherAssignments();

        Map<String, Object> status = new HashMap<>();
        status.put("totalUnassignedCourses", preview.getTotalCoursesProcessed());
        status.put("canAssign", preview.getCoursesAssigned());
        status.put("cannotAssign", preview.getCoursesFailed());
        status.put("readyForAssignment", preview.getTotalCoursesProcessed() > 0);
        status.put("estimatedSuccessRate", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);
        status.put("hasTeacherShortage", preview.getCoursesFailed() > 0);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        AssignmentResult preview = smartTeacherAssignmentService.previewTeacherAssignments();

        Map<String, Object> stats = new HashMap<>();
        stats.put("unassignedCourses", preview.getTotalCoursesProcessed());
        stats.put("potentialAssignments", preview.getCoursesAssigned());
        stats.put("potentialFailures", preview.getCoursesFailed());
        stats.put("completionPercentage", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);
        stats.put("teacherShortageDetected", preview.getCoursesFailed() > 0);

        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        AssignmentResult preview = smartTeacherAssignmentService.previewTeacherAssignments();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("unassignedCourses", preview.getTotalCoursesProcessed());
        dashboard.put("assignableCount", preview.getCoursesAssigned());
        dashboard.put("unassignableCount", preview.getCoursesFailed());
        dashboard.put("successRate", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);
        dashboard.put("status", preview.getTotalCoursesProcessed() == 0 ? "COMPLETE" :
            preview.getCoursesFailed() == 0 ? "READY" : "TEACHER_SHORTAGE");
        dashboard.put("teacherShortageDetected", preview.getCoursesFailed() > 0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/readiness")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        AssignmentResult preview = smartTeacherAssignmentService.previewTeacherAssignments();

        boolean ready = preview.getTotalCoursesProcessed() > 0 && preview.getCoursesFailed() == 0;
        boolean teacherShortage = preview.getCoursesFailed() > 0;

        Map<String, Object> readiness = new HashMap<>();
        readiness.put("ready", ready);
        readiness.put("unassignedCourses", preview.getTotalCoursesProcessed());
        readiness.put("assignableCount", preview.getCoursesAssigned());
        readiness.put("unassignableCount", preview.getCoursesFailed());
        readiness.put("teacherShortage", teacherShortage);
        readiness.put("recommendation", ready ?
            "System is ready for automatic teacher assignment" :
            preview.getTotalCoursesProcessed() == 0 ?
                "All courses already have teachers assigned" :
                teacherShortage ?
                    "TEACHER SHORTAGE DETECTED: Hire additional teachers or manually override workload limits" :
                    "Review unassignable courses before running assignment");

        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/dashboard/shortage-analysis")
    public ResponseEntity<Map<String, Object>> getShortageAnalysis() {
        AssignmentResult preview = smartTeacherAssignmentService.previewTeacherAssignments();

        boolean hasShortage = preview.getCoursesFailed() > 0;

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("hasShortage", hasShortage);
        analysis.put("unassignableCourses", preview.getCoursesFailed());
        analysis.put("severity", hasShortage ?
            (preview.getCoursesFailed() > preview.getTotalCoursesProcessed() * 0.25 ? "CRITICAL" :
             preview.getCoursesFailed() > preview.getTotalCoursesProcessed() * 0.10 ? "HIGH" : "MODERATE") :
            "NONE");
        analysis.put("action", hasShortage ?
            "Contact HR to hire additional certified teachers" :
            "No action needed");

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/dashboard/workload-limits")
    public ResponseEntity<Map<String, Object>> getWorkloadLimits() {
        Map<String, Object> limits = new HashMap<>();

        limits.put("optimalCourses", 2);
        limits.put("maximumCourses", 3);
        limits.put("description", "Teachers should have 2 courses optimally, 3 courses maximum");
        limits.put("workloadMode", "TEACHING_PERIODS");
        limits.put("note", "Workload limits prevent teacher burnout and ensure quality instruction");

        return ResponseEntity.ok(limits);
    }

    // ==================== Configuration & Metadata ====================

    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("workloadLimits", Map.of(
            "optimal", 2,
            "maximum", 3,
            "mode", "TEACHING_PERIODS"
        ));

        config.put("features", Map.of(
            "strictCertification", "Only assigns certified teachers to courses",
            "workloadBalancing", "Distributes courses evenly across teachers",
            "courseSequencing", "Keeps related courses with same teacher",
            "shortageDetection", "Identifies when additional teachers needed"
        ));

        config.put("scoring", Map.of(
            "exactCertification", "100 points",
            "subjectFamily", "75 points (e.g., Biology teacher → Chemistry)",
            "workloadBalance", "0-50 points (prefer less loaded teachers)"
        ));

        config.put("subjectFamilies", Map.of(
            "Science", "Biology, Chemistry, Physics, Earth Science",
            "Math", "Algebra, Geometry, Calculus, Trigonometry",
            "English", "English, Literature, Language Arts",
            "Social Studies", "History, Geography, Civics, Economics",
            "Physical Education", "PE, Health, Athletics",
            "Arts", "Art, Music, Drama, Theater",
            "Foreign Language", "Spanish, French, German, Latin",
            "Computer Science", "Programming, Technology, IT"
        ));

        return ResponseEntity.ok(config);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "2.1.0");
        metadata.put("lastUpdated", "December 14, 2025");

        metadata.put("improvements", Map.of(
            "workloadLimits", "Updated to 2-3 courses per teacher",
            "shortageHandling", "No over-assignment, administrators must hire",
            "certificationMatching", "Strict subject family matching",
            "sequenceLogic", "English 1 → English 2 same teacher"
        ));

        metadata.put("capabilities", Map.of(
            "preview", "Preview assignments before applying",
            "intelligentMatching", "Multi-factor scoring system",
            "shortageDetection", "Alerts administrators to hiring needs",
            "workloadTracking", "In-memory tracking prevents over-assignment"
        ));

        return ResponseEntity.ok(metadata);
    }
}
