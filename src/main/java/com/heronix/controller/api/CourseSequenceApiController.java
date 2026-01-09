package com.heronix.controller.api;

import com.heronix.model.domain.CourseSequence;
import com.heronix.model.domain.CourseSequenceStep;
import com.heronix.model.domain.SubjectArea;
import com.heronix.repository.SubjectAreaRepository;
import com.heronix.service.CourseSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Course Sequence Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/course-sequences")
@RequiredArgsConstructor
public class CourseSequenceApiController {

    private final CourseSequenceService sequenceService;
    private final SubjectAreaRepository subjectAreaRepository;

    // ==================== CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<CourseSequence>> getAllSequences() {
        List<CourseSequence> sequences = sequenceService.getAllSequences();
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CourseSequence>> getActiveSequences() {
        List<CourseSequence> sequences = sequenceService.getActiveSequences();
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseSequence> getSequenceById(@PathVariable Long id) {
        return sequenceService.getSequenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CourseSequence> getSequenceByCode(@PathVariable String code) {
        return sequenceService.getSequenceByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseSequence> createSequence(@RequestBody CourseSequence sequence) {
        CourseSequence created = sequenceService.createSequence(sequence);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseSequence> updateSequence(
            @PathVariable Long id,
            @RequestBody CourseSequence updates) {
        CourseSequence updated = sequenceService.updateSequence(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSequence(@PathVariable Long id) {
        sequenceService.deleteSequence(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Query Operations ====================

    @GetMapping("/subject/{subjectAreaId}")
    public ResponseEntity<List<CourseSequence>> getSequencesBySubjectArea(@PathVariable Long subjectAreaId) {
        SubjectArea subjectArea = subjectAreaRepository.findById(subjectAreaId)
                .orElseThrow(() -> new IllegalArgumentException("Subject area not found: " + subjectAreaId));
        List<CourseSequence> sequences = sequenceService.getSequencesBySubjectArea(subjectArea);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CourseSequence>> getSequencesByType(@PathVariable CourseSequence.SequenceType type) {
        List<CourseSequence> sequences = sequenceService.getSequencesByType(type);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/subject/{subjectAreaId}/type/{type}")
    public ResponseEntity<List<CourseSequence>> getSequencesBySubjectAndType(
            @PathVariable Long subjectAreaId,
            @PathVariable CourseSequence.SequenceType type) {
        SubjectArea subjectArea = subjectAreaRepository.findById(subjectAreaId)
                .orElseThrow(() -> new IllegalArgumentException("Subject area not found: " + subjectAreaId));
        List<CourseSequence> sequences = sequenceService.getSequencesBySubjectAndType(subjectArea, type);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseSequence>> searchSequences(@RequestParam String searchTerm) {
        List<CourseSequence> sequences = sequenceService.searchSequences(searchTerm);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/gpa/{studentGPA}")
    public ResponseEntity<List<CourseSequence>> getSequencesForStudentGPA(@PathVariable Double studentGPA) {
        List<CourseSequence> sequences = sequenceService.getSequencesForStudentGPA(studentGPA);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSequence>> getSequencesContainingCourse(@PathVariable Long courseId) {
        List<CourseSequence> sequences = sequenceService.getSequencesContainingCourse(courseId);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/grade-level/{gradeLevel}")
    public ResponseEntity<List<CourseSequence>> getSequencesForGradeLevel(@PathVariable Integer gradeLevel) {
        List<CourseSequence> sequences = sequenceService.getSequencesForGradeLevel(gradeLevel);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/iep-friendly")
    public ResponseEntity<List<CourseSequence>> getIEPFriendlySequences() {
        List<CourseSequence> sequences = sequenceService.getIEPFriendlySequences();
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/prerequisite/{skillLevel}")
    public ResponseEntity<List<CourseSequence>> getSequencesByPrerequisite(@PathVariable String skillLevel) {
        List<CourseSequence> sequences = sequenceService.getSequencesByPrerequisite(skillLevel);
        return ResponseEntity.ok(sequences);
    }

    @GetMapping("/graduation-requirement/{category}")
    public ResponseEntity<List<CourseSequence>> getSequencesByGraduationRequirement(@PathVariable String category) {
        List<CourseSequence> sequences = sequenceService.getSequencesByGraduationRequirement(category);
        return ResponseEntity.ok(sequences);
    }

    // ==================== Sequence Steps Management ====================

    @GetMapping("/{sequenceId}/steps")
    public ResponseEntity<List<CourseSequenceStep>> getStepsForSequence(@PathVariable Long sequenceId) {
        List<CourseSequenceStep> steps = sequenceService.getStepsForSequence(sequenceId);
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/steps/{stepId}")
    public ResponseEntity<CourseSequenceStep> getStepById(@PathVariable Long stepId) {
        return sequenceService.getStepById(stepId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{sequenceId}/steps")
    public ResponseEntity<CourseSequenceStep> addStepToSequence(
            @PathVariable Long sequenceId,
            @RequestBody CourseSequenceStep step) {
        CourseSequenceStep created = sequenceService.addStepToSequence(sequenceId, step);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/steps/{stepId}")
    public ResponseEntity<CourseSequenceStep> updateStep(
            @PathVariable Long stepId,
            @RequestBody CourseSequenceStep updates) {
        CourseSequenceStep updated = sequenceService.updateStep(stepId, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable Long stepId) {
        sequenceService.deleteStep(stepId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/steps/{stepId}/reorder")
    public ResponseEntity<CourseSequenceStep> reorderStep(
            @PathVariable Long stepId,
            @RequestParam Integer newOrder) {
        CourseSequenceStep updated = sequenceService.reorderStep(stepId, newOrder);
        return ResponseEntity.ok(updated);
    }

    // ==================== Step Navigation ====================

    @GetMapping("/{sequenceId}/steps/first")
    public ResponseEntity<CourseSequenceStep> getFirstStep(@PathVariable Long sequenceId) {
        return sequenceService.getFirstStep(sequenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{sequenceId}/steps/last")
    public ResponseEntity<CourseSequenceStep> getLastStep(@PathVariable Long sequenceId) {
        return sequenceService.getLastStep(sequenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/steps/{currentStepId}/next")
    public ResponseEntity<CourseSequenceStep> getNextStep(@PathVariable Long currentStepId) {
        return sequenceService.getNextStep(currentStepId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/steps/{currentStepId}/previous")
    public ResponseEntity<CourseSequenceStep> getPreviousStep(@PathVariable Long currentStepId) {
        return sequenceService.getPreviousStep(currentStepId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Step Queries ====================

    @GetMapping("/{sequenceId}/steps/required")
    public ResponseEntity<List<CourseSequenceStep>> getRequiredSteps(@PathVariable Long sequenceId) {
        List<CourseSequenceStep> steps = sequenceService.getRequiredSteps(sequenceId);
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/{sequenceId}/steps/optional")
    public ResponseEntity<List<CourseSequenceStep>> getOptionalSteps(@PathVariable Long sequenceId) {
        List<CourseSequenceStep> steps = sequenceService.getOptionalSteps(sequenceId);
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/{sequenceId}/steps/grade-level/{gradeLevel}")
    public ResponseEntity<List<CourseSequenceStep>> getStepsForGradeLevel(
            @PathVariable Long sequenceId,
            @PathVariable Integer gradeLevel) {
        List<CourseSequenceStep> steps = sequenceService.getStepsForGradeLevel(sequenceId, gradeLevel);
        return ResponseEntity.ok(steps);
    }

    // ==================== Utility Operations ====================

    @PostMapping("/{sequenceId}/recalculate-totals")
    public ResponseEntity<Map<String, String>> recalculateTotals(@PathVariable Long sequenceId) {
        CourseSequence sequence = sequenceService.getSequenceById(sequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceId));

        sequenceService.recalculateSequenceTotals(sequence);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sequence totals recalculated successfully");
        response.put("sequenceId", sequenceId.toString());

        return ResponseEntity.ok(response);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics")
    public ResponseEntity<CourseSequenceService.SequenceStatistics> getStatistics() {
        CourseSequenceService.SequenceStatistics stats = sequenceService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/counts")
    public ResponseEntity<Map<String, Long>> getCounts() {
        CourseSequenceService.SequenceStatistics stats = sequenceService.getStatistics();

        Map<String, Long> counts = new HashMap<>();
        counts.put("total", stats.getTotalSequences());
        counts.put("active", stats.getActiveSequences());
        counts.put("totalSteps", stats.getTotalSteps());

        return ResponseEntity.ok(counts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        CourseSequenceService.SequenceStatistics stats = sequenceService.getStatistics();
        List<CourseSequence> activeSequences = sequenceService.getActiveSequences();

        dashboard.put("statistics", stats);
        dashboard.put("totalSequences", stats.getTotalSequences());
        dashboard.put("activeSequences", stats.getActiveSequences());
        dashboard.put("totalSteps", stats.getTotalSteps());
        dashboard.put("recentSequences", activeSequences.stream().limit(10).toList());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/sequence/{sequenceId}")
    public ResponseEntity<Map<String, Object>> getSequenceDashboard(@PathVariable Long sequenceId) {
        Map<String, Object> dashboard = new HashMap<>();

        CourseSequence sequence = sequenceService.getSequenceById(sequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceId));

        List<CourseSequenceStep> allSteps = sequenceService.getStepsForSequence(sequenceId);
        List<CourseSequenceStep> requiredSteps = sequenceService.getRequiredSteps(sequenceId);
        List<CourseSequenceStep> optionalSteps = sequenceService.getOptionalSteps(sequenceId);

        dashboard.put("sequenceId", sequenceId);
        dashboard.put("sequenceName", sequence.getName());
        dashboard.put("sequenceCode", sequence.getCode());
        dashboard.put("sequenceType", sequence.getSequenceType());
        dashboard.put("subjectArea", sequence.getSubjectArea() != null ? sequence.getSubjectArea().getName() : null);
        dashboard.put("totalSteps", allSteps.size());
        dashboard.put("requiredSteps", requiredSteps.size());
        dashboard.put("optionalSteps", optionalSteps.size());
        dashboard.put("totalYears", sequence.getTotalYears());
        dashboard.put("totalCredits", sequence.getTotalCredits());
        dashboard.put("minGPARecommended", sequence.getMinGPARecommended());
        dashboard.put("allSteps", allSteps);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/by-type")
    public ResponseEntity<Map<String, Object>> getSequencesByTypeDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<CourseSequence> allSequences = sequenceService.getActiveSequences();

        long traditionalCount = allSequences.stream()
                .filter(s -> s.getSequenceType() == CourseSequence.SequenceType.TRADITIONAL)
                .count();
        long technicalCount = allSequences.stream()
                .filter(s -> s.getSequenceType() == CourseSequence.SequenceType.TECHNICAL)
                .count();
        long honorsCount = allSequences.stream()
                .filter(s -> s.getSequenceType() == CourseSequence.SequenceType.HONORS)
                .count();
        long apCount = allSequences.stream()
                .filter(s -> s.getSequenceType() == CourseSequence.SequenceType.AP)
                .count();
        long ibCount = allSequences.stream()
                .filter(s -> s.getSequenceType() == CourseSequence.SequenceType.IB)
                .count();

        dashboard.put("traditionalSequences", traditionalCount);
        dashboard.put("technicalSequences", technicalCount);
        dashboard.put("honorsSequences", honorsCount);
        dashboard.put("apSequences", apCount);
        dashboard.put("ibSequences", ibCount);
        dashboard.put("totalSequences", allSequences.size());

        return ResponseEntity.ok(dashboard);
    }
}
