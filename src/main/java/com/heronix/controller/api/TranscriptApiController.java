package com.heronix.controller.api;

import com.heronix.model.domain.TranscriptRecord;
import com.heronix.repository.StudentRepository;
import com.heronix.service.impl.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Transcript Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptApiController {

    private final TranscriptService transcriptService;
    private final StudentRepository studentRepository;

    // ==================== Transcript Generation ====================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<TranscriptService.StudentTranscript> generateTranscript(@PathVariable Long studentId) {
        TranscriptService.StudentTranscript transcript = transcriptService.generateTranscript(studentId);
        return ResponseEntity.ok(transcript);
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getTranscriptSummary(@PathVariable Long studentId) {
        TranscriptService.StudentTranscript transcript = transcriptService.generateTranscript(studentId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("studentName", transcript.getStudentName());
        summary.put("studentNumber", transcript.getStudentNumber());
        summary.put("currentGradeLevel", transcript.getCurrentGradeLevel());
        summary.put("cumulativeGpa", transcript.getCumulativeGpa());
        summary.put("weightedGpa", transcript.getWeightedGpa());
        summary.put("totalCreditsEarned", transcript.getTotalCreditsEarned());
        summary.put("generatedAt", transcript.getGeneratedAt());

        return ResponseEntity.ok(summary);
    }

    // ==================== GPA Calculations ====================

    @GetMapping("/student/{studentId}/gpa/cumulative")
    public ResponseEntity<Map<String, Object>> getCumulativeGpa(@PathVariable Long studentId) {
        BigDecimal cumulativeGpa = transcriptService.calculateCumulativeGpa(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("cumulativeGpa", cumulativeGpa);
        response.put("scale", "4.0 (unweighted)");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/gpa/weighted")
    public ResponseEntity<Map<String, Object>> getWeightedGpa(@PathVariable Long studentId) {
        BigDecimal weightedGpa = transcriptService.calculateWeightedGpa(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("weightedGpa", weightedGpa);
        response.put("scale", "5.0 (weighted for Honors/AP)");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/gpa/both")
    public ResponseEntity<Map<String, Object>> getBothGPAs(@PathVariable Long studentId) {
        BigDecimal cumulativeGpa = transcriptService.calculateCumulativeGpa(studentId);
        BigDecimal weightedGpa = transcriptService.calculateWeightedGpa(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("cumulativeGpa", cumulativeGpa);
        response.put("weightedGpa", weightedGpa);
        response.put("cumulativeScale", "4.0");
        response.put("weightedScale", "5.0");

        return ResponseEntity.ok(response);
    }

    // ==================== Grade Management ====================

    @PostMapping("/student/{studentId}/grades")
    public ResponseEntity<TranscriptRecord> addGrade(
            @PathVariable Long studentId,
            @RequestParam Long courseId,
            @RequestParam String academicYear,
            @RequestParam TranscriptRecord.Semester semester,
            @RequestParam String letterGrade,
            @RequestParam BigDecimal numericGrade,
            @RequestParam BigDecimal credits,
            @RequestParam TranscriptRecord.CourseType courseType) {

        TranscriptRecord record = transcriptService.addGrade(
                studentId, courseId, academicYear, semester, letterGrade,
                numericGrade, credits, courseType);

        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    // ==================== Class Rank ====================

    @GetMapping("/student/{studentId}/class-rank")
    public ResponseEntity<TranscriptService.ClassRankInfo> getClassRank(@PathVariable Long studentId) {
        TranscriptService.ClassRankInfo rankInfo = transcriptService.getClassRank(studentId);
        return ResponseEntity.ok(rankInfo);
    }

    @GetMapping("/student/{studentId}/class-rank/summary")
    public ResponseEntity<Map<String, Object>> getClassRankSummary(@PathVariable Long studentId) {
        TranscriptService.ClassRankInfo rankInfo = transcriptService.getClassRank(studentId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("rank", rankInfo.getRank());
        summary.put("totalStudents", rankInfo.getTotalStudents());
        summary.put("gradeLevel", rankInfo.getGradeLevel());
        summary.put("gpa", rankInfo.getGpa());
        summary.put("percentile", rankInfo.getPercentile());
        summary.put("rankDisplay", rankInfo.getRank() + " of " + rankInfo.getTotalStudents());

        return ResponseEntity.ok(summary);
    }

    // ==================== Graduation Status ====================

    @GetMapping("/student/{studentId}/graduation-status")
    public ResponseEntity<TranscriptService.GraduationStatus> getGraduationStatus(@PathVariable Long studentId) {
        TranscriptService.GraduationStatus status = transcriptService.checkGraduationRequirements(studentId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/student/{studentId}/graduation-status/summary")
    public ResponseEntity<Map<String, Object>> getGraduationStatusSummary(@PathVariable Long studentId) {
        TranscriptService.GraduationStatus status = transcriptService.checkGraduationRequirements(studentId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("creditsEarned", status.getCreditsEarned());
        summary.put("creditsRequired", status.getCreditsRequired());
        summary.put("creditsRemaining", status.getCreditsRequired().subtract(status.getCreditsEarned()));
        summary.put("coursesCompleted", status.getCoursesCompleted());
        summary.put("meetsRequirements", status.isMeetsRequirements());
        summary.put("projectedGraduationYear", status.getProjectedGraduationYear());
        summary.put("status", status.isMeetsRequirements() ? "On Track" : "Behind");

        return ResponseEntity.ok(summary);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        TranscriptService.StudentTranscript transcript = transcriptService.generateTranscript(studentId);
        TranscriptService.ClassRankInfo classRank = transcriptService.getClassRank(studentId);
        TranscriptService.GraduationStatus graduationStatus = transcriptService.checkGraduationRequirements(studentId);

        dashboard.put("studentId", studentId);
        dashboard.put("studentName", transcript.getStudentName());
        dashboard.put("studentNumber", transcript.getStudentNumber());
        dashboard.put("gradeLevel", transcript.getCurrentGradeLevel());

        // GPA Information
        Map<String, Object> gpaInfo = new HashMap<>();
        gpaInfo.put("cumulative", transcript.getCumulativeGpa());
        gpaInfo.put("weighted", transcript.getWeightedGpa());
        dashboard.put("gpa", gpaInfo);

        // Credits Information
        Map<String, Object> creditsInfo = new HashMap<>();
        creditsInfo.put("earned", graduationStatus.getCreditsEarned());
        creditsInfo.put("required", graduationStatus.getCreditsRequired());
        creditsInfo.put("remaining", graduationStatus.getCreditsRequired().subtract(graduationStatus.getCreditsEarned()));
        dashboard.put("credits", creditsInfo);

        // Class Rank Information
        Map<String, Object> rankInfo = new HashMap<>();
        rankInfo.put("rank", classRank.getRank());
        rankInfo.put("totalStudents", classRank.getTotalStudents());
        rankInfo.put("percentile", classRank.getPercentile());
        rankInfo.put("rankDisplay", classRank.getRank() + " of " + classRank.getTotalStudents());
        dashboard.put("classRank", rankInfo);

        // Graduation Status
        Map<String, Object> gradStatus = new HashMap<>();
        gradStatus.put("meetsRequirements", graduationStatus.isMeetsRequirements());
        gradStatus.put("projectedGraduationYear", graduationStatus.getProjectedGraduationYear());
        gradStatus.put("coursesCompleted", graduationStatus.getCoursesCompleted());
        gradStatus.put("status", graduationStatus.isMeetsRequirements() ? "On Track" : "Behind");
        dashboard.put("graduationStatus", gradStatus);

        // Academic History
        dashboard.put("academicYears", transcript.getAcademicYears());
        dashboard.put("totalYears", transcript.getAcademicYears() != null ? transcript.getAcademicYears().size() : 0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/grade/{gradeLevel}/rankings")
    public ResponseEntity<Map<String, Object>> getGradeLevelRankings(@PathVariable String gradeLevel) {
        Map<String, Object> dashboard = new HashMap<>();

        var students = studentRepository.findByGradeLevel(gradeLevel);

        dashboard.put("gradeLevel", gradeLevel);
        dashboard.put("totalStudents", students.size());
        dashboard.put("message", "Class rankings calculated for " + students.size() + " students");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getTranscriptOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        long totalStudents = studentRepository.count();

        dashboard.put("message", "Transcript system overview");
        dashboard.put("totalStudents", totalStudents);
        dashboard.put("status", "Active");

        return ResponseEntity.ok(dashboard);
    }
}
