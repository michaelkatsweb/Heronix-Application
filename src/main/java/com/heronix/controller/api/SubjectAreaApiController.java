package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.SubjectArea;
import com.heronix.model.domain.SubjectRelationship;
import com.heronix.service.SubjectAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Subject Area Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/subject-areas")
@RequiredArgsConstructor
public class SubjectAreaApiController {

    private final SubjectAreaService subjectAreaService;

    // ==================== CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<SubjectArea>> getAllSubjectAreas() {
        List<SubjectArea> subjects = subjectAreaService.getAllSubjectAreas();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SubjectArea>> getActiveSubjectAreas() {
        List<SubjectArea> subjects = subjectAreaService.getActiveSubjectAreas();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectArea> getSubjectAreaById(@PathVariable Long id) {
        return subjectAreaService.getSubjectAreaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SubjectArea> getSubjectAreaByCode(@PathVariable String code) {
        return subjectAreaService.getSubjectAreaByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SubjectArea> getSubjectAreaByName(@PathVariable String name) {
        return subjectAreaService.getSubjectAreaByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SubjectArea> createSubjectArea(@RequestBody SubjectArea subjectArea) {
        SubjectArea created = subjectAreaService.createSubjectArea(subjectArea);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectArea> updateSubjectArea(
            @PathVariable Long id,
            @RequestBody SubjectArea subjectArea) {
        SubjectArea updated = subjectAreaService.updateSubjectArea(id, subjectArea);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubjectArea(@PathVariable Long id) {
        subjectAreaService.deleteSubjectArea(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Hierarchy Operations ====================

    @GetMapping("/top-level")
    public ResponseEntity<List<SubjectArea>> getTopLevelSubjectAreas() {
        List<SubjectArea> topLevel = subjectAreaService.getTopLevelSubjectAreas();
        return ResponseEntity.ok(topLevel);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<SubjectArea>> getChildSubjects(@PathVariable Long parentId) {
        List<SubjectArea> children = subjectAreaService.getChildSubjects(parentId);
        return ResponseEntity.ok(children);
    }

    @PatchMapping("/{childId}/parent")
    public ResponseEntity<SubjectArea> setParentSubject(
            @PathVariable Long childId,
            @RequestParam(required = false) Long parentId) {
        SubjectArea updated = subjectAreaService.setParentSubject(childId, parentId);
        return ResponseEntity.ok(updated);
    }

    // ==================== Relationship Operations ====================

    @PostMapping("/relationships")
    public ResponseEntity<SubjectRelationship> createRelationship(
            @RequestParam Long subject1Id,
            @RequestParam Long subject2Id,
            @RequestParam SubjectRelationship.RelationshipType type,
            @RequestParam Integer strength,
            @RequestParam(required = false) String description) {
        SubjectRelationship relationship = subjectAreaService.createRelationship(
                subject1Id, subject2Id, type, strength, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(relationship);
    }

    @GetMapping("/{subjectId}/relationships")
    public ResponseEntity<List<SubjectRelationship>> getRelationships(@PathVariable Long subjectId) {
        List<SubjectRelationship> relationships = subjectAreaService.getRelationships(subjectId);
        return ResponseEntity.ok(relationships);
    }

    @GetMapping("/{subjectId}/related-subjects")
    public ResponseEntity<List<SubjectArea>> getRelatedSubjects(@PathVariable Long subjectId) {
        List<SubjectArea> related = subjectAreaService.getRelatedSubjects(subjectId);
        return ResponseEntity.ok(related);
    }

    @GetMapping("/{subjectId}/strongly-related")
    public ResponseEntity<List<SubjectArea>> getStronglyRelatedSubjects(@PathVariable Long subjectId) {
        List<SubjectArea> stronglyRelated = subjectAreaService.getStronglyRelatedSubjects(subjectId);
        return ResponseEntity.ok(stronglyRelated);
    }

    @DeleteMapping("/relationships/{relationshipId}")
    public ResponseEntity<Void> deleteRelationship(@PathVariable Long relationshipId) {
        subjectAreaService.deleteRelationship(relationshipId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Query Operations ====================

    @GetMapping("/search")
    public ResponseEntity<List<SubjectArea>> searchSubjectAreas(@RequestParam String searchTerm) {
        List<SubjectArea> results = subjectAreaService.searchSubjectAreas(searchTerm);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<SubjectArea>> getSubjectAreasByDepartment(@PathVariable String department) {
        List<SubjectArea> subjects = subjectAreaService.getSubjectAreasByDepartment(department);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        List<String> departments = subjectAreaService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/with-courses")
    public ResponseEntity<List<SubjectArea>> getSubjectAreasWithCourses() {
        List<SubjectArea> subjects = subjectAreaService.getSubjectAreasWithCourses();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/without-courses")
    public ResponseEntity<List<SubjectArea>> getSubjectAreasWithoutCourses() {
        List<SubjectArea> subjects = subjectAreaService.getSubjectAreasWithoutCourses();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{subjectId}/courses")
    public ResponseEntity<List<Course>> getCoursesForSubject(@PathVariable Long subjectId) {
        List<Course> courses = subjectAreaService.getCoursesForSubject(subjectId);
        return ResponseEntity.ok(courses);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics")
    public ResponseEntity<SubjectAreaService.SubjectAreaStatistics> getStatistics() {
        SubjectAreaService.SubjectAreaStatistics stats = subjectAreaService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<SubjectArea> allSubjects = subjectAreaService.getAllSubjectAreas();
        List<SubjectArea> activeSubjects = subjectAreaService.getActiveSubjectAreas();
        List<SubjectArea> topLevel = subjectAreaService.getTopLevelSubjectAreas();
        List<SubjectArea> withCourses = subjectAreaService.getSubjectAreasWithCourses();
        List<SubjectArea> withoutCourses = subjectAreaService.getSubjectAreasWithoutCourses();
        List<String> departments = subjectAreaService.getAllDepartments();
        SubjectAreaService.SubjectAreaStatistics stats = subjectAreaService.getStatistics();

        dashboard.put("totalSubjects", allSubjects.size());
        dashboard.put("activeSubjects", activeSubjects.size());
        dashboard.put("inactiveSubjects", allSubjects.size() - activeSubjects.size());
        dashboard.put("topLevelSubjects", topLevel.size());
        dashboard.put("subjectsWithCourses", withCourses.size());
        dashboard.put("subjectsWithoutCourses", withoutCourses.size());
        dashboard.put("totalDepartments", departments.size());
        dashboard.put("totalRelationships", stats.getTotalRelationships());
        dashboard.put("departments", departments);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/subject/{id}")
    public ResponseEntity<Map<String, Object>> getSubjectDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = new HashMap<>();

        SubjectArea subject = subjectAreaService.getSubjectAreaById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject area not found: " + id));

        List<SubjectArea> children = subjectAreaService.getChildSubjects(id);
        List<Course> courses = subjectAreaService.getCoursesForSubject(id);
        List<SubjectRelationship> relationships = subjectAreaService.getRelationships(id);
        List<SubjectArea> relatedSubjects = subjectAreaService.getRelatedSubjects(id);

        dashboard.put("subjectId", id);
        dashboard.put("code", subject.getCode());
        dashboard.put("name", subject.getName());
        dashboard.put("department", subject.getDepartment());
        dashboard.put("description", subject.getDescription());
        dashboard.put("displayColor", subject.getDisplayColor());
        dashboard.put("active", subject.getActive());
        dashboard.put("hasParent", subject.getParentSubject() != null);
        dashboard.put("parentSubject", subject.getParentSubject());
        dashboard.put("childrenCount", children.size());
        dashboard.put("courseCount", courses.size());
        dashboard.put("relationshipCount", relationships.size());
        dashboard.put("relatedSubjectsCount", relatedSubjects.size());
        dashboard.put("children", children);
        dashboard.put("relationships", relationships);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/hierarchy")
    public ResponseEntity<Map<String, Object>> getHierarchyDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<SubjectArea> topLevel = subjectAreaService.getTopLevelSubjectAreas();
        List<SubjectArea> allSubjects = subjectAreaService.getActiveSubjectAreas();

        long childSubjects = allSubjects.stream()
                .filter(s -> s.getParentSubject() != null)
                .count();

        dashboard.put("topLevelCount", topLevel.size());
        dashboard.put("childSubjectsCount", childSubjects);
        dashboard.put("totalActive", allSubjects.size());
        dashboard.put("averageChildrenPerParent",
                topLevel.isEmpty() ? 0.0 : (double) childSubjects / topLevel.size());
        dashboard.put("topLevelSubjects", topLevel);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/department/{department}")
    public ResponseEntity<Map<String, Object>> getDepartmentDashboard(@PathVariable String department) {
        Map<String, Object> dashboard = new HashMap<>();

        List<SubjectArea> departmentSubjects = subjectAreaService.getSubjectAreasByDepartment(department);

        long withCourses = departmentSubjects.stream()
                .filter(s -> !subjectAreaService.getCoursesForSubject(s.getId()).isEmpty())
                .count();

        long topLevel = departmentSubjects.stream()
                .filter(s -> s.getParentSubject() == null)
                .count();

        dashboard.put("department", department);
        dashboard.put("totalSubjects", departmentSubjects.size());
        dashboard.put("subjectsWithCourses", withCourses);
        dashboard.put("topLevelSubjects", topLevel);
        dashboard.put("subjects", departmentSubjects);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/relationships")
    public ResponseEntity<Map<String, Object>> getRelationshipsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        SubjectAreaService.SubjectAreaStatistics stats = subjectAreaService.getStatistics();
        List<SubjectArea> allSubjects = subjectAreaService.getActiveSubjectAreas();

        long subjectsWithRelationships = allSubjects.stream()
                .filter(s -> !subjectAreaService.getRelationships(s.getId()).isEmpty())
                .count();

        dashboard.put("totalRelationships", stats.getTotalRelationships());
        dashboard.put("subjectsWithRelationships", subjectsWithRelationships);
        dashboard.put("subjectsWithoutRelationships", allSubjects.size() - subjectsWithRelationships);
        dashboard.put("averageRelationshipsPerSubject",
                allSubjects.isEmpty() ? 0.0 : (double) stats.getTotalRelationships() / allSubjects.size());

        return ResponseEntity.ok(dashboard);
    }
}
