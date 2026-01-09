package com.heronix.controller.api;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.GraduationRequirementsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Graduation Requirements Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/graduation-requirements")
@RequiredArgsConstructor
public class GraduationRequirementsApiController {

    private final GraduationRequirementsService graduationService;
    private final StudentRepository studentRepository;

    // ==================== Individual Student Assessment ====================

    @GetMapping("/student/{studentId}/on-track")
    public ResponseEntity<Map<String, Object>> checkOnTrack(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        boolean onTrack = graduationService.isOnTrackForGraduation(student);
        String status = graduationService.getAcademicStandingStatus(student);
        double creditsBehind = graduationService.getCreditsBehind(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("onTrack", onTrack);
        response.put("status", status);
        response.put("creditsBehind", creditsBehind);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/academic-standing")
    public ResponseEntity<Map<String, Object>> getAcademicStanding(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Object> standing = new HashMap<>();
        standing.put("studentId", studentId);
        standing.put("status", graduationService.getAcademicStandingStatus(student));
        standing.put("colorCode", graduationService.getStandingColorCode(student));
        standing.put("backgroundColor", graduationService.getStandingBackgroundColor(student));
        standing.put("icon", graduationService.getStandingIcon(student));
        standing.put("tooltip", graduationService.getStandingTooltip(student));
        standing.put("creditsBehind", graduationService.getCreditsBehind(student));
        standing.put("meetsGPA", graduationService.meetsGPARequirement(student));

        return ResponseEntity.ok(standing);
    }

    @GetMapping("/student/{studentId}/credits-behind")
    public ResponseEntity<Double> getCreditsBehind(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        double creditsBehind = graduationService.getCreditsBehind(student);
        return ResponseEntity.ok(creditsBehind);
    }

    @GetMapping("/student/{studentId}/tooltip")
    public ResponseEntity<String> getStandingTooltip(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        String tooltip = graduationService.getStandingTooltip(student);
        return ResponseEntity.ok(tooltip);
    }

    @GetMapping("/student/{studentId}/gpa-requirement")
    public ResponseEntity<Map<String, Object>> checkGPARequirement(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        boolean meetsGPA = graduationService.meetsGPARequirement(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("meetsGPA", meetsGPA);
        response.put("currentGPA", student.getCurrentGPA());
        response.put("requiredGPA", 2.0);

        return ResponseEntity.ok(response);
    }

    // ==================== Requirements by Grade Level ====================

    @GetMapping("/required-credits/grade/{gradeLevel}")
    public ResponseEntity<Map<String, Object>> getRequiredCreditsByGrade(@PathVariable String gradeLevel) {
        double requiredCredits = graduationService.getRequiredCreditsByGrade(gradeLevel);

        Map<String, Object> response = new HashMap<>();
        response.put("gradeLevel", gradeLevel);
        response.put("requiredCredits", requiredCredits);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/required-credits/all-grades")
    public ResponseEntity<Map<String, Double>> getAllRequiredCredits() {
        Map<String, Double> allRequirements = new HashMap<>();
        allRequirements.put("9", graduationService.getRequiredCreditsByGrade("9"));
        allRequirements.put("10", graduationService.getRequiredCreditsByGrade("10"));
        allRequirements.put("11", graduationService.getRequiredCreditsByGrade("11"));
        allRequirements.put("12", graduationService.getRequiredCreditsByGrade("12"));

        return ResponseEntity.ok(allRequirements);
    }

    // ==================== At-Risk Students ====================

    @GetMapping("/students/at-risk")
    public ResponseEntity<List<Student>> getAtRiskStudents() {
        List<Student> students = graduationService.getAtRiskStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/retention-risk")
    public ResponseEntity<List<Student>> getRetentionRiskStudents() {
        List<Student> students = graduationService.getRetentionRiskStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/seniors-not-meeting-requirements")
    public ResponseEntity<List<Student>> getSeniorsNotMeetingRequirements() {
        List<Student> students = graduationService.getSeniorsNotMeetingRequirements();
        return ResponseEntity.ok(students);
    }

    // ==================== Update Operations ====================

    @PatchMapping("/student/{studentId}/update-standing")
    public ResponseEntity<Student> updateStudentAcademicStanding(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Student updated = graduationService.updateAcademicStanding(student);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/update-all-standings")
    public ResponseEntity<Map<String, Object>> updateAllAcademicStandings() {
        int updatedCount = graduationService.updateAllAcademicStandings();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Academic standings updated successfully");
        response.put("studentsUpdated", updatedCount);

        return ResponseEntity.ok(response);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics/summary")
    public ResponseEntity<Map<String, Integer>> getAcademicStandingSummary() {
        Map<String, Integer> summary = graduationService.getAcademicStandingSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/statistics/counts")
    public ResponseEntity<Map<String, Long>> getCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("atRisk", (long) graduationService.getAtRiskStudents().size());
        counts.put("retentionRisk", (long) graduationService.getRetentionRiskStudents().size());
        counts.put("seniorsNotMeeting", (long) graduationService.getSeniorsNotMeetingRequirements().size());

        return ResponseEntity.ok(counts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Integer> summary = graduationService.getAcademicStandingSummary();
        List<Student> atRisk = graduationService.getAtRiskStudents();
        List<Student> retentionRisk = graduationService.getRetentionRiskStudents();
        List<Student> seniorsNotMeeting = graduationService.getSeniorsNotMeetingRequirements();

        dashboard.put("summary", summary);
        dashboard.put("atRiskStudents", atRisk);
        dashboard.put("atRiskCount", atRisk.size());
        dashboard.put("retentionRiskStudents", retentionRisk);
        dashboard.put("retentionRiskCount", retentionRisk.size());
        dashboard.put("seniorsNotMeeting", seniorsNotMeeting);
        dashboard.put("seniorsNotMeetingCount", seniorsNotMeeting.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("studentId", studentId);
        dashboard.put("studentName", student.getFirstName() + " " + student.getLastName());
        dashboard.put("gradeLevel", student.getGradeLevel());
        dashboard.put("currentCredits", student.getCreditsEarned());
        dashboard.put("requiredCredits", graduationService.getRequiredCreditsByGrade(student.getGradeLevel()));
        dashboard.put("creditsBehind", graduationService.getCreditsBehind(student));
        dashboard.put("onTrack", graduationService.isOnTrackForGraduation(student));
        dashboard.put("status", graduationService.getAcademicStandingStatus(student));
        dashboard.put("colorCode", graduationService.getStandingColorCode(student));
        dashboard.put("icon", graduationService.getStandingIcon(student));
        dashboard.put("tooltip", graduationService.getStandingTooltip(student));
        dashboard.put("currentGPA", student.getCurrentGPA());
        dashboard.put("meetsGPA", graduationService.meetsGPARequirement(student));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/grade/{gradeLevel}")
    public ResponseEntity<Map<String, Object>> getGradeLevelDashboard(@PathVariable String gradeLevel) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> allStudents = studentRepository.findByGradeLevel(gradeLevel);
        List<Student> atRiskInGrade = allStudents.stream()
                .filter(s -> !graduationService.isOnTrackForGraduation(s))
                .toList();

        double requiredCredits = graduationService.getRequiredCreditsByGrade(gradeLevel);

        dashboard.put("gradeLevel", gradeLevel);
        dashboard.put("requiredCredits", requiredCredits);
        dashboard.put("totalStudents", allStudents.size());
        dashboard.put("atRiskCount", atRiskInGrade.size());
        dashboard.put("atRiskStudents", atRiskInGrade);
        dashboard.put("onTrackCount", allStudents.size() - atRiskInGrade.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/intervention-needed")
    public ResponseEntity<Map<String, Object>> getInterventionDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> atRisk = graduationService.getAtRiskStudents();
        List<Student> retentionRisk = graduationService.getRetentionRiskStudents();
        List<Student> seniorsNotMeeting = graduationService.getSeniorsNotMeetingRequirements();

        // Categorize by severity
        dashboard.put("critical", seniorsNotMeeting);
        dashboard.put("criticalCount", seniorsNotMeeting.size());
        dashboard.put("high", retentionRisk);
        dashboard.put("highCount", retentionRisk.size());
        dashboard.put("medium", atRisk);
        dashboard.put("mediumCount", atRisk.size());
        dashboard.put("totalNeedingIntervention", atRisk.size() + retentionRisk.size() + seniorsNotMeeting.size());

        return ResponseEntity.ok(dashboard);
    }
}
