package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.StudentProgressMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API Controller for Student Progress Monitoring
 *
 * Provides endpoints for automated student intervention monitoring and risk assessment.
 * Monitors multiple data sources to identify students requiring intervention:
 * - Attendance patterns (tardiness, absences)
 * - Academic decline (failing grades, missing work)
 * - Behavior incidents (negative incidents, major events)
 * - Teacher observations (concern-level notes)
 *
 * Integration:
 * - Works with AlertGenerationService for automated alert routing
 * - Nightly scheduled job runs pattern detection at 2:00 AM
 * - Real-time grade calculation from ClassroomGradeEntry
 *
 * Risk Levels:
 * - NONE: No intervention needed
 * - LOW: Monitor - single risk indicator
 * - MEDIUM: Review within 3 days - multiple minor indicators
 * - HIGH: Review within 1 day - multiple serious indicators
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/student-progress-monitoring")
@RequiredArgsConstructor
public class StudentProgressMonitoringApiController {

    private final StudentProgressMonitoringService progressMonitoringService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // ==================== Pattern Detection ====================

    @PostMapping("/detect-patterns")
    public ResponseEntity<Map<String, Object>> detectAllPatterns() {
        try {
            progressMonitoringService.detectInterventionPatterns();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pattern detection completed");
            response.put("note", "This endpoint triggers the nightly monitoring job manually");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Individual Pattern Detection ====================

    @GetMapping("/student/{studentId}/attendance-patterns")
    public ResponseEntity<Map<String, Object>> detectAttendancePatterns(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasPattern = progressMonitoringService.detectAttendancePatterns(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("hasAttendanceRisk", hasPattern);
        response.put("patternType", "ATTENDANCE");
        response.put("thresholds", Map.of(
            "tardies", 3,
            "unexcusedAbsences", 2,
            "monitoringDays", 14
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/academic-patterns")
    public ResponseEntity<Map<String, Object>> detectAcademicPatterns(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasPattern = progressMonitoringService.detectAcademicDeclinePatterns(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("hasAcademicRisk", hasPattern);
        response.put("patternType", "ACADEMIC");
        response.put("thresholds", Map.of(
            "failingCourses", 2,
            "missingAssignments", 5,
            "failingGrade", 60.0,
            "monitoringDays", 14
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/behavior-patterns")
    public ResponseEntity<Map<String, Object>> detectBehaviorPatterns(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasPattern = progressMonitoringService.detectBehaviorPatterns(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("hasBehaviorRisk", hasPattern);
        response.put("patternType", "BEHAVIOR");
        response.put("thresholds", Map.of(
            "negativeIncidents", 3,
            "monitoringDays", 14
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/observation-patterns")
    public ResponseEntity<Map<String, Object>> detectObservationPatterns(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasPattern = progressMonitoringService.detectObservationPatterns(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("hasObservationRisk", hasPattern);
        response.put("patternType", "TEACHER_OBSERVATION");
        response.put("thresholds", Map.of(
            "concernObservations", 2,
            "monitoringDays", 14
        ));

        return ResponseEntity.ok(response);
    }

    // ==================== Risk Assessment ====================

    @GetMapping("/student/{studentId}/risk-assessment")
    public ResponseEntity<Map<String, Object>> getRiskAssessment(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StudentProgressMonitoringService.StudentRiskAssessment assessment =
            progressMonitoringService.getStudentRiskAssessment(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("assessmentDate", assessment.getAssessmentDate());
        response.put("riskLevel", assessment.getRiskLevel().toString());

        response.put("riskIndicators", Map.of(
            "attendance", assessment.isHasAttendanceRisk(),
            "academic", assessment.isHasAcademicRisk(),
            "behavior", assessment.isHasBehaviorRisk(),
            "observations", assessment.isHasObservationRisk()
        ));

        int riskCount = 0;
        if (assessment.isHasAttendanceRisk()) riskCount++;
        if (assessment.isHasAcademicRisk()) riskCount++;
        if (assessment.isHasBehaviorRisk()) riskCount++;
        if (assessment.isHasObservationRisk()) riskCount++;

        response.put("riskIndicatorCount", riskCount);
        response.put("requiresIntervention", assessment.getRiskLevel() != StudentProgressMonitoringService.RiskLevel.NONE);

        // Add recommended action
        String action;
        switch (assessment.getRiskLevel()) {
            case HIGH:
                action = "Immediate intervention - review within 1 day";
                break;
            case MEDIUM:
                action = "Monitor closely - review within 3 days";
                break;
            case LOW:
                action = "Continue monitoring";
                break;
            default:
                action = "No action needed";
                break;
        }
        response.put("recommendedAction", action);

        return ResponseEntity.ok(response);
    }

    // ==================== Grade Calculation ====================

    @GetMapping("/student/{studentId}/course/{courseId}/current-grade")
    public ResponseEntity<Map<String, Object>> getCurrentGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double currentGrade = progressMonitoringService.calculateCurrentGrade(
            studentOpt.get(), courseOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("courseId", courseId);
        response.put("courseName", courseOpt.get().getCourseName());
        response.put("currentGrade", currentGrade);

        if (currentGrade != null) {
            response.put("isFailing", currentGrade < 60.0);
            response.put("letterGrade", getLetterGrade(currentGrade));
            response.put("status", currentGrade >= 60.0 ? "PASSING" : "FAILING");
        } else {
            response.put("status", "NO_GRADES_RECORDED");
        }

        return ResponseEntity.ok(response);
    }

    // ==================== At-Risk Students ====================

    @GetMapping("/at-risk-students")
    public ResponseEntity<Map<String, Object>> getAtRiskStudents(
            @RequestParam(required = false) String riskLevel) {

        List<Student> allStudents = studentRepository.findAll().stream()
            .filter(s -> s.getDeleted() == null || !s.getDeleted())
            .collect(Collectors.toList());

        List<Map<String, Object>> atRiskStudents = allStudents.stream()
            .map(student -> {
                StudentProgressMonitoringService.StudentRiskAssessment assessment =
                    progressMonitoringService.getStudentRiskAssessment(student);

                // Filter by risk level if specified
                if (riskLevel != null && !riskLevel.isEmpty()) {
                    if (!assessment.getRiskLevel().toString().equalsIgnoreCase(riskLevel)) {
                        return null;
                    }
                }

                // Only include students with risk
                if (assessment.getRiskLevel() == StudentProgressMonitoringService.RiskLevel.NONE) {
                    return null;
                }

                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentId", student.getId());
                studentInfo.put("studentName", student.getFullName());
                studentInfo.put("gradeLevel", student.getGradeLevel());
                studentInfo.put("riskLevel", assessment.getRiskLevel().toString());
                studentInfo.put("riskIndicators", Map.of(
                    "attendance", assessment.isHasAttendanceRisk(),
                    "academic", assessment.isHasAcademicRisk(),
                    "behavior", assessment.isHasBehaviorRisk(),
                    "observations", assessment.isHasObservationRisk()
                ));

                return studentInfo;
            })
            .filter(s -> s != null)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalAtRisk", atRiskStudents.size());
        response.put("students", atRiskStudents);

        if (riskLevel != null) {
            response.put("filteredByRiskLevel", riskLevel);
        }

        // Count by risk level
        Map<String, Long> byRiskLevel = atRiskStudents.stream()
            .collect(Collectors.groupingBy(
                s -> (String) s.get("riskLevel"),
                Collectors.counting()
            ));

        response.put("countByRiskLevel", byRiskLevel);

        return ResponseEntity.ok(response);
    }

    // ==================== Configuration ====================

    @GetMapping("/configuration/thresholds")
    public ResponseEntity<Map<String, Object>> getThresholds() {
        Map<String, Object> thresholds = new HashMap<>();

        thresholds.put("attendance", Map.of(
            "tardies", 3,
            "unexcusedAbsences", 2,
            "monitoringDays", 14,
            "description", "Trigger intervention if 3+ tardies or 2+ unexcused absences in 14 days"
        ));

        thresholds.put("academic", Map.of(
            "failingCourses", 2,
            "missingAssignments", 5,
            "failingGradeThreshold", 60.0,
            "monitoringDays", 14,
            "description", "Trigger intervention if failing 2+ courses or 5+ missing assignments in 14 days"
        ));

        thresholds.put("behavior", Map.of(
            "negativeIncidents", 3,
            "monitoringDays", 14,
            "description", "Trigger intervention if 3+ negative incidents in 14 days"
        ));

        thresholds.put("observations", Map.of(
            "concernObservations", 2,
            "monitoringDays", 14,
            "description", "Trigger intervention if 2+ concern-level teacher observations in 14 days"
        ));

        return ResponseEntity.ok(thresholds);
    }

    @GetMapping("/configuration/risk-levels")
    public ResponseEntity<Map<String, Object>> getRiskLevels() {
        Map<String, Object> riskLevels = new HashMap<>();

        riskLevels.put("NONE", Map.of(
            "description", "No intervention needed",
            "action", "Continue normal monitoring"
        ));

        riskLevels.put("LOW", Map.of(
            "description", "Single risk indicator detected",
            "action", "Monitor - notify teacher and counselor"
        ));

        riskLevels.put("MEDIUM", Map.of(
            "description", "Multiple minor risk indicators",
            "action", "Review within 3 days - notify teacher, counselor, and grade admin"
        ));

        riskLevels.put("HIGH", Map.of(
            "description", "Multiple serious risk indicators",
            "action", "Review within 1 day - notify all personnel including principal"
        ));

        return ResponseEntity.ok(riskLevels);
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<Student> allStudents = studentRepository.findAll().stream()
            .filter(s -> s.getDeleted() == null || !s.getDeleted())
            .collect(Collectors.toList());

        int totalStudents = allStudents.size();
        int highRisk = 0;
        int mediumRisk = 0;
        int lowRisk = 0;
        int noRisk = 0;

        for (Student student : allStudents) {
            StudentProgressMonitoringService.StudentRiskAssessment assessment =
                progressMonitoringService.getStudentRiskAssessment(student);

            switch (assessment.getRiskLevel()) {
                case HIGH:
                    highRisk++;
                    break;
                case MEDIUM:
                    mediumRisk++;
                    break;
                case LOW:
                    lowRisk++;
                    break;
                default:
                    noRisk++;
                    break;
            }
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalStudents", totalStudents);
        dashboard.put("atRiskCount", highRisk + mediumRisk + lowRisk);
        dashboard.put("highRiskCount", highRisk);
        dashboard.put("mediumRiskCount", mediumRisk);
        dashboard.put("lowRiskCount", lowRisk);
        dashboard.put("noRiskCount", noRisk);

        if (totalStudents > 0) {
            double atRiskPercentage = ((highRisk + mediumRisk + lowRisk) * 100.0) / totalStudents;
            dashboard.put("atRiskPercentage", String.format("%.1f%%", atRiskPercentage));
        }

        dashboard.put("scheduledJob", Map.of(
            "frequency", "Daily at 2:00 AM",
            "enabled", true,
            "description", "Automated nightly pattern detection"
        ));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Student> allStudents = studentRepository.findAll().stream()
            .filter(s -> s.getDeleted() == null || !s.getDeleted())
            .collect(Collectors.toList());

        int attendanceRisk = 0;
        int academicRisk = 0;
        int behaviorRisk = 0;
        int observationRisk = 0;

        for (Student student : allStudents) {
            StudentProgressMonitoringService.StudentRiskAssessment assessment =
                progressMonitoringService.getStudentRiskAssessment(student);

            if (assessment.isHasAttendanceRisk()) attendanceRisk++;
            if (assessment.isHasAcademicRisk()) academicRisk++;
            if (assessment.isHasBehaviorRisk()) behaviorRisk++;
            if (assessment.isHasObservationRisk()) observationRisk++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("riskByCategory", Map.of(
            "attendance", attendanceRisk,
            "academic", academicRisk,
            "behavior", behaviorRisk,
            "observations", observationRisk
        ));

        summary.put("topConcern", getTopConcern(attendanceRisk, academicRisk, behaviorRisk, observationRisk));

        return ResponseEntity.ok(summary);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "patternDetection", "Automated nightly monitoring of at-risk students",
            "riskAssessment", "Multi-source risk assessment (attendance, academic, behavior, observations)",
            "gradeCalculation", "Real-time grade calculation from classroom entries",
            "alertIntegration", "Integrates with AlertGenerationService for automated routing"
        ));

        metadata.put("dataSources", List.of(
            "ClassroomGradeEntry - Assignment grades and missing work",
            "BehaviorIncident - Positive and negative incidents",
            "TeacherObservationNote - Teacher concerns",
            "AttendanceRecord - Tardiness and absences"
        ));

        metadata.put("scheduledJob", Map.of(
            "schedule", "Daily at 2:00 AM",
            "description", "Runs detectInterventionPatterns() to identify at-risk students"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Student Progress Monitoring Help");

        help.put("workflows", Map.of(
            "checkStudentRisk", List.of(
                "1. GET /api/student-progress-monitoring/student/{studentId}/risk-assessment",
                "2. Review risk level and indicators",
                "3. Generate alert if needed: POST /api/alerts/student/{studentId}/generate"
            ),
            "findAtRiskStudents", List.of(
                "1. GET /api/student-progress-monitoring/at-risk-students",
                "2. Filter by risk level: ?riskLevel=HIGH",
                "3. Review students and take action"
            ),
            "manualPatternDetection", List.of(
                "1. POST /api/student-progress-monitoring/detect-patterns",
                "2. Check dashboard: GET /api/student-progress-monitoring/dashboard",
                "3. Generate alerts: POST /api/alerts/generate-all"
            )
        ));

        help.put("endpoints", Map.of(
            "riskAssessment", "GET /api/student-progress-monitoring/student/{studentId}/risk-assessment",
            "atRiskStudents", "GET /api/student-progress-monitoring/at-risk-students",
            "detectPatterns", "POST /api/student-progress-monitoring/detect-patterns",
            "currentGrade", "GET /api/student-progress-monitoring/student/{studentId}/course/{courseId}/current-grade"
        ));

        help.put("integration", Map.of(
            "alertGeneration", "POST /api/alerts/student/{studentId}/generate",
            "alertConfiguration", "GET /api/alerts/configuration/types",
            "scheduledJob", "Runs automatically at 2:00 AM daily"
        ));

        return ResponseEntity.ok(help);
    }

    // ==================== Helper Methods ====================

    private String getLetterGrade(double grade) {
        if (grade >= 90) return "A";
        if (grade >= 80) return "B";
        if (grade >= 70) return "C";
        if (grade >= 60) return "D";
        return "F";
    }

    private String getTopConcern(int attendance, int academic, int behavior, int observation) {
        int max = Math.max(Math.max(attendance, academic), Math.max(behavior, observation));

        if (max == 0) return "NONE";
        if (max == attendance) return "ATTENDANCE";
        if (max == academic) return "ACADEMIC";
        if (max == behavior) return "BEHAVIOR";
        return "OBSERVATIONS";
    }
}
