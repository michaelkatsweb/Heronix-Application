package com.heronix.controller.api;

import com.heronix.model.domain.CourseRecommendation;
import com.heronix.service.CourseRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Course Recommendation Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/course-recommendations")
@RequiredArgsConstructor
public class CourseRecommendationApiController {

    private final CourseRecommendationService recommendationService;

    // ==================== CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<CourseRecommendation>> getAllRecommendations() {
        List<CourseRecommendation> recommendations = recommendationService.getAllRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CourseRecommendation>> getActiveRecommendations() {
        List<CourseRecommendation> recommendations = recommendationService.getActiveRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseRecommendation> getRecommendationById(@PathVariable Long id) {
        return recommendationService.getRecommendationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseRecommendation> createRecommendation(@RequestBody CourseRecommendation recommendation) {
        CourseRecommendation created = recommendationService.createRecommendation(recommendation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseRecommendation> updateRecommendation(
            @PathVariable Long id,
            @RequestBody CourseRecommendation updates) {
        CourseRecommendation updated = recommendationService.updateRecommendation(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Student-Specific Queries ====================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseRecommendation>> getRecommendationsForStudent(@PathVariable Long studentId) {
        List<CourseRecommendation> recommendations = recommendationService.getRecommendationsForStudent(studentId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<List<CourseRecommendation>> getPendingRecommendationsForStudent(@PathVariable Long studentId) {
        List<CourseRecommendation> recommendations = recommendationService.getPendingRecommendationsForStudent(studentId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/high-priority")
    public ResponseEntity<List<CourseRecommendation>> getHighPriorityRecommendationsForStudent(@PathVariable Long studentId) {
        List<CourseRecommendation> recommendations = recommendationService.getHighPriorityRecommendationsForStudent(studentId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/meeting-requirements")
    public ResponseEntity<List<CourseRecommendation>> getRecommendationsMeetingRequirements(@PathVariable Long studentId) {
        List<CourseRecommendation> recommendations = recommendationService.getRecommendationsMeetingRequirements(studentId);
        return ResponseEntity.ok(recommendations);
    }

    // ==================== AI Generation ====================

    @PostMapping("/student/{studentId}/generate")
    public ResponseEntity<List<CourseRecommendation>> generateRecommendationsForStudent(
            @PathVariable Long studentId,
            @RequestParam String schoolYear) {
        List<CourseRecommendation> recommendations = recommendationService.generateRecommendationsForStudent(studentId, schoolYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendations);
    }

    // ==================== Approval Operations ====================

    @PatchMapping("/{id}/accept-by-student")
    public ResponseEntity<CourseRecommendation> acceptByStudent(@PathVariable Long id) {
        CourseRecommendation updated = recommendationService.acceptByStudent(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/accept-by-parent")
    public ResponseEntity<CourseRecommendation> acceptByParent(@PathVariable Long id) {
        CourseRecommendation updated = recommendationService.acceptByParent(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reject-by-student")
    public ResponseEntity<CourseRecommendation> rejectByStudent(@PathVariable Long id) {
        CourseRecommendation updated = recommendationService.rejectByStudent(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reject-by-parent")
    public ResponseEntity<CourseRecommendation> rejectByParent(@PathVariable Long id) {
        CourseRecommendation updated = recommendationService.rejectByParent(id);
        return ResponseEntity.ok(updated);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics")
    public ResponseEntity<CourseRecommendationService.RecommendationStatistics> getStatistics() {
        CourseRecommendationService.RecommendationStatistics stats = recommendationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/counts")
    public ResponseEntity<Map<String, Long>> getCounts() {
        CourseRecommendationService.RecommendationStatistics stats = recommendationService.getStatistics();

        Map<String, Long> counts = new HashMap<>();
        counts.put("total", stats.getTotalRecommendations());
        counts.put("active", stats.getActiveRecommendations());
        counts.put("pending", stats.getPendingRecommendations());
        counts.put("accepted", stats.getAcceptedRecommendations());
        counts.put("aiGenerated", stats.getAiGeneratedRecommendations());

        return ResponseEntity.ok(counts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        CourseRecommendationService.RecommendationStatistics stats = recommendationService.getStatistics();
        List<CourseRecommendation> activeRecommendations = recommendationService.getActiveRecommendations();

        dashboard.put("statistics", stats);
        dashboard.put("totalRecommendations", stats.getTotalRecommendations());
        dashboard.put("activeRecommendations", stats.getActiveRecommendations());
        dashboard.put("pendingRecommendations", stats.getPendingRecommendations());
        dashboard.put("acceptedRecommendations", stats.getAcceptedRecommendations());
        dashboard.put("aiGeneratedRecommendations", stats.getAiGeneratedRecommendations());
        dashboard.put("recentRecommendations", activeRecommendations.stream().limit(10).toList());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<CourseRecommendation> allRecommendations = recommendationService.getRecommendationsForStudent(studentId);
        List<CourseRecommendation> pendingRecommendations = recommendationService.getPendingRecommendationsForStudent(studentId);
        List<CourseRecommendation> highPriorityRecommendations = recommendationService.getHighPriorityRecommendationsForStudent(studentId);
        List<CourseRecommendation> meetingRequirements = recommendationService.getRecommendationsMeetingRequirements(studentId);

        long acceptedCount = allRecommendations.stream()
                .filter(r -> r.getStatus() == CourseRecommendation.RecommendationStatus.ACCEPTED)
                .count();
        long rejectedCount = allRecommendations.stream()
                .filter(r -> r.getStatus() == CourseRecommendation.RecommendationStatus.REJECTED)
                .count();

        dashboard.put("studentId", studentId);
        dashboard.put("totalRecommendations", allRecommendations.size());
        dashboard.put("pendingRecommendations", pendingRecommendations);
        dashboard.put("pendingCount", pendingRecommendations.size());
        dashboard.put("highPriorityRecommendations", highPriorityRecommendations);
        dashboard.put("highPriorityCount", highPriorityRecommendations.size());
        dashboard.put("meetingRequirements", meetingRequirements);
        dashboard.put("meetingRequirementsCount", meetingRequirements.size());
        dashboard.put("acceptedCount", acceptedCount);
        dashboard.put("rejectedCount", rejectedCount);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/pending")
    public ResponseEntity<Map<String, Object>> getPendingDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<CourseRecommendation> allRecommendations = recommendationService.getActiveRecommendations();

        List<CourseRecommendation> pendingRecommendations = allRecommendations.stream()
                .filter(r -> r.getStatus() == CourseRecommendation.RecommendationStatus.PENDING)
                .toList();

        List<CourseRecommendation> highPriorityPending = pendingRecommendations.stream()
                .filter(r -> r.getPriority() != null && r.getPriority() <= 2)
                .toList();

        dashboard.put("pendingRecommendations", pendingRecommendations);
        dashboard.put("pendingCount", pendingRecommendations.size());
        dashboard.put("highPriorityPending", highPriorityPending);
        dashboard.put("highPriorityCount", highPriorityPending.size());
        dashboard.put("message", pendingRecommendations.size() + " recommendations awaiting review");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/ai-generated")
    public ResponseEntity<Map<String, Object>> getAIGeneratedDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<CourseRecommendation> allRecommendations = recommendationService.getActiveRecommendations();

        List<CourseRecommendation> aiGenerated = allRecommendations.stream()
                .filter(r -> r.getRecommendationType() == CourseRecommendation.RecommendationType.AI_GENERATED ||
                             r.getRecommendationType() == CourseRecommendation.RecommendationType.SEQUENCE_BASED ||
                             r.getRecommendationType() == CourseRecommendation.RecommendationType.PREREQUISITE_BASED)
                .toList();

        long sequenceBased = aiGenerated.stream()
                .filter(r -> r.getRecommendationType() == CourseRecommendation.RecommendationType.SEQUENCE_BASED)
                .count();

        long prerequisiteBased = aiGenerated.stream()
                .filter(r -> r.getRecommendationType() == CourseRecommendation.RecommendationType.PREREQUISITE_BASED)
                .count();

        long graduationRequirement = aiGenerated.stream()
                .filter(r -> r.getRecommendationType() == CourseRecommendation.RecommendationType.GRADUATION_REQUIREMENT)
                .count();

        dashboard.put("aiGeneratedRecommendations", aiGenerated);
        dashboard.put("totalAIGenerated", aiGenerated.size());
        dashboard.put("sequenceBasedCount", sequenceBased);
        dashboard.put("prerequisiteBasedCount", prerequisiteBased);
        dashboard.put("graduationRequirementCount", graduationRequirement);

        return ResponseEntity.ok(dashboard);
    }
}
