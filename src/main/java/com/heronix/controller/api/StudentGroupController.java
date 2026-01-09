package com.heronix.controller.api;

import com.heronix.model.domain.StudentGroup;
import com.heronix.model.domain.StudentGroup.GroupType;
import com.heronix.model.domain.StudentGroup.GroupStatus;
import com.heronix.service.StudentGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Student Groups
 *
 * Provides endpoints for managing:
 * - Homerooms
 * - Advisory Groups
 * - Cohorts (Graduating Classes)
 * - House Systems
 * - Teams/Squads
 * - Learning Communities
 * - Academic Tracks
 * - Extracurricular Clubs
 * - Intervention Groups
 * - Social/Peer Groups
 * - Leadership Groups
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/student-groups")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class StudentGroupController {

    private final StudentGroupService groupService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new group
     * POST /api/student-groups
     */
    @PostMapping
    public ResponseEntity<StudentGroup> createGroup(
            @RequestParam String groupName,
            @RequestParam GroupType groupType,
            @RequestParam(required = false) String academicYear,
            @RequestParam Long createdByStaffId) {
        try {
            log.info("API: Creating {} group: {}", groupType, groupName);
            StudentGroup created = groupService.createGroup(groupName, groupType, academicYear, createdByStaffId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get group by ID
     * GET /api/student-groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentGroup> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update group
     * PUT /api/student-groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentGroup> updateGroup(
            @PathVariable Long id,
            @RequestBody StudentGroup group,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.updateGroup(id, group, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete group
     * DELETE /api/student-groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Delete failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all groups
     * GET /api/student-groups
     */
    @GetMapping
    public ResponseEntity<List<StudentGroup>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    // ========================================================================
    // GROUP MEMBERSHIP MANAGEMENT
    // ========================================================================

    /**
     * Enroll student in group
     * POST /api/student-groups/{groupId}/students/{studentId}
     */
    @PostMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<StudentGroup> enrollStudent(
            @PathVariable Long groupId,
            @PathVariable Long studentId,
            @RequestParam Long staffId) {
        try {
            log.info("API: Enrolling student {} in group {}", studentId, groupId);
            StudentGroup updated = groupService.enrollStudent(groupId, studentId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Enrollment failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Enrollment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove student from group
     * DELETE /api/student-groups/{groupId}/students/{studentId}
     */
    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<StudentGroup> removeStudent(
            @PathVariable Long groupId,
            @PathVariable Long studentId,
            @RequestParam Long staffId) {
        try {
            log.info("API: Removing student {} from group {}", studentId, groupId);
            StudentGroup updated = groupService.removeStudent(groupId, studentId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Removal failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students in group
     * GET /api/student-groups/{groupId}/students
     */
    @GetMapping("/{groupId}/students")
    public ResponseEntity<List<Long>> getStudentsInGroup(@PathVariable Long groupId) {
        try {
            List<Long> studentIds = groupService.getStudentIdsInGroup(groupId);
            return ResponseEntity.ok(studentIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if student is in group
     * GET /api/student-groups/{groupId}/students/{studentId}/check
     */
    @GetMapping("/{groupId}/students/{studentId}/check")
    public ResponseEntity<Boolean> isStudentInGroup(
            @PathVariable Long groupId,
            @PathVariable Long studentId) {
        boolean inGroup = groupService.isStudentInGroup(groupId, studentId);
        return ResponseEntity.ok(inGroup);
    }

    /**
     * Bulk enroll students
     * POST /api/student-groups/{groupId}/students/bulk
     */
    @PostMapping("/{groupId}/students/bulk")
    public ResponseEntity<StudentGroup> bulkEnrollStudents(
            @PathVariable Long groupId,
            @RequestBody List<Long> studentIds,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.bulkEnrollStudents(groupId, studentIds, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Bulk enrollment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk remove students
     * DELETE /api/student-groups/{groupId}/students/bulk
     */
    @DeleteMapping("/{groupId}/students/bulk")
    public ResponseEntity<StudentGroup> bulkRemoveStudents(
            @PathVariable Long groupId,
            @RequestBody List<Long> studentIds,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.bulkRemoveStudents(groupId, studentIds, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // STUDENT-BASED QUERIES
    // ========================================================================

    /**
     * Get groups for a student
     * GET /api/student-groups/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentGroup>> getGroupsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(groupService.getGroupsForStudent(studentId));
    }

    /**
     * Get active groups for a student
     * GET /api/student-groups/student/{studentId}/active
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<StudentGroup>> getActiveGroupsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(groupService.getActiveGroupsForStudent(studentId));
    }

    /**
     * Get groups by student and type
     * GET /api/student-groups/student/{studentId}/type/{type}
     */
    @GetMapping("/student/{studentId}/type/{type}")
    public ResponseEntity<List<StudentGroup>> getGroupsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable GroupType type) {
        return ResponseEntity.ok(groupService.getGroupsByStudentAndType(studentId, type));
    }

    // ========================================================================
    // TYPE-BASED QUERIES
    // ========================================================================

    /**
     * Get groups by type
     * GET /api/student-groups/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<StudentGroup>> getGroupsByType(@PathVariable GroupType type) {
        return ResponseEntity.ok(groupService.getGroupsByType(type));
    }

    /**
     * Get active groups by type
     * GET /api/student-groups/type/{type}/active
     */
    @GetMapping("/type/{type}/active")
    public ResponseEntity<List<StudentGroup>> getActiveGroupsByType(@PathVariable GroupType type) {
        return ResponseEntity.ok(groupService.getActiveGroupsByType(type));
    }

    /**
     * Get all homerooms
     * GET /api/student-groups/homerooms
     */
    @GetMapping("/homerooms")
    public ResponseEntity<List<StudentGroup>> getAllHomerooms() {
        return ResponseEntity.ok(groupService.getAllHomerooms());
    }

    /**
     * Get all advisory groups
     * GET /api/student-groups/advisories
     */
    @GetMapping("/advisories")
    public ResponseEntity<List<StudentGroup>> getAllAdvisories() {
        return ResponseEntity.ok(groupService.getAllAdvisories());
    }

    /**
     * Get all cohorts
     * GET /api/student-groups/cohorts
     */
    @GetMapping("/cohorts")
    public ResponseEntity<List<StudentGroup>> getAllCohorts() {
        return ResponseEntity.ok(groupService.getAllCohorts());
    }

    /**
     * Get all houses
     * GET /api/student-groups/houses
     */
    @GetMapping("/houses")
    public ResponseEntity<List<StudentGroup>> getAllHouses() {
        return ResponseEntity.ok(groupService.getAllHouses());
    }

    /**
     * Get all teams
     * GET /api/student-groups/teams
     */
    @GetMapping("/teams")
    public ResponseEntity<List<StudentGroup>> getAllTeams() {
        return ResponseEntity.ok(groupService.getAllTeams());
    }

    /**
     * Get all learning communities
     * GET /api/student-groups/learning-communities
     */
    @GetMapping("/learning-communities")
    public ResponseEntity<List<StudentGroup>> getAllLearningCommunities() {
        return ResponseEntity.ok(groupService.getAllLearningCommunities());
    }

    /**
     * Get all academic tracks
     * GET /api/student-groups/academic-tracks
     */
    @GetMapping("/academic-tracks")
    public ResponseEntity<List<StudentGroup>> getAllAcademicTracks() {
        return ResponseEntity.ok(groupService.getAllAcademicTracks());
    }

    /**
     * Get all extracurricular clubs
     * GET /api/student-groups/extracurricular
     */
    @GetMapping("/extracurricular")
    public ResponseEntity<List<StudentGroup>> getAllExtracurricularClubs() {
        return ResponseEntity.ok(groupService.getAllExtracurricularClubs());
    }

    /**
     * Get all intervention groups
     * GET /api/student-groups/intervention
     */
    @GetMapping("/intervention")
    public ResponseEntity<List<StudentGroup>> getAllInterventionGroups() {
        return ResponseEntity.ok(groupService.getAllInterventionGroups());
    }

    // ========================================================================
    // ACADEMIC YEAR QUERIES
    // ========================================================================

    /**
     * Get groups by academic year
     * GET /api/student-groups/academic-year/{year}
     */
    @GetMapping("/academic-year/{year}")
    public ResponseEntity<List<StudentGroup>> getGroupsByAcademicYear(@PathVariable String year) {
        return ResponseEntity.ok(groupService.getGroupsByAcademicYear(year));
    }

    /**
     * Get active groups by academic year
     * GET /api/student-groups/academic-year/{year}/active
     */
    @GetMapping("/academic-year/{year}/active")
    public ResponseEntity<List<StudentGroup>> getActiveGroupsByAcademicYear(@PathVariable String year) {
        return ResponseEntity.ok(groupService.getActiveGroupsByAcademicYear(year));
    }

    // ========================================================================
    // GRADE LEVEL QUERIES
    // ========================================================================

    /**
     * Get groups by grade level
     * GET /api/student-groups/grade/{grade}
     */
    @GetMapping("/grade/{grade}")
    public ResponseEntity<List<StudentGroup>> getGroupsByGradeLevel(@PathVariable String grade) {
        return ResponseEntity.ok(groupService.getGroupsByGradeLevel(grade));
    }

    /**
     * Get active groups by grade level
     * GET /api/student-groups/grade/{grade}/active
     */
    @GetMapping("/grade/{grade}/active")
    public ResponseEntity<List<StudentGroup>> getActiveGroupsByGradeLevel(@PathVariable String grade) {
        return ResponseEntity.ok(groupService.getActiveGroupsByGradeLevel(grade));
    }

    // ========================================================================
    // STAFF/ADVISOR QUERIES
    // ========================================================================

    /**
     * Get groups by primary advisor
     * GET /api/student-groups/advisor/{advisorId}
     */
    @GetMapping("/advisor/{advisorId}")
    public ResponseEntity<List<StudentGroup>> getGroupsByPrimaryAdvisor(@PathVariable Long advisorId) {
        return ResponseEntity.ok(groupService.getGroupsByPrimaryAdvisor(advisorId));
    }

    /**
     * Get active groups by primary advisor
     * GET /api/student-groups/advisor/{advisorId}/active
     */
    @GetMapping("/advisor/{advisorId}/active")
    public ResponseEntity<List<StudentGroup>> getActiveGroupsByPrimaryAdvisor(@PathVariable Long advisorId) {
        return ResponseEntity.ok(groupService.getActiveGroupsByPrimaryAdvisor(advisorId));
    }

    /**
     * Assign primary advisor
     * POST /api/student-groups/{id}/assign-advisor
     */
    @PostMapping("/{id}/assign-advisor")
    public ResponseEntity<StudentGroup> assignPrimaryAdvisor(
            @PathVariable Long id,
            @RequestParam Long advisorId,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.assignPrimaryAdvisor(id, advisorId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Assign secondary advisor
     * POST /api/student-groups/{id}/assign-secondary-advisor
     */
    @PostMapping("/{id}/assign-secondary-advisor")
    public ResponseEntity<StudentGroup> assignSecondaryAdvisor(
            @PathVariable Long id,
            @RequestParam Long advisorId,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.assignSecondaryAdvisor(id, advisorId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // CAPACITY MANAGEMENT
    // ========================================================================

    /**
     * Get groups with available capacity
     * GET /api/student-groups/capacity/available
     */
    @GetMapping("/capacity/available")
    public ResponseEntity<List<StudentGroup>> getGroupsWithCapacity() {
        return ResponseEntity.ok(groupService.getGroupsWithCapacity());
    }

    /**
     * Get full groups
     * GET /api/student-groups/capacity/full
     */
    @GetMapping("/capacity/full")
    public ResponseEntity<List<StudentGroup>> getFullGroups() {
        return ResponseEntity.ok(groupService.getFullGroups());
    }

    /**
     * Get groups near capacity
     * GET /api/student-groups/capacity/near-full
     */
    @GetMapping("/capacity/near-full")
    public ResponseEntity<List<StudentGroup>> getGroupsNearCapacity(@RequestParam(defaultValue = "90") double percentage) {
        return ResponseEntity.ok(groupService.getGroupsNearCapacity(percentage));
    }

    /**
     * Set capacity
     * POST /api/student-groups/{id}/capacity
     */
    @PostMapping("/{id}/capacity")
    public ResponseEntity<StudentGroup> setCapacity(
            @PathVariable Long id,
            @RequestParam Integer maxCapacity,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.setCapacity(id, maxCapacity, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Set accepting new members status
     * POST /api/student-groups/{id}/accepting-members
     */
    @PostMapping("/{id}/accepting-members")
    public ResponseEntity<StudentGroup> setAcceptingNewMembers(
            @PathVariable Long id,
            @RequestParam Boolean accepting,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.setAcceptingNewMembers(id, accepting, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // STATUS MANAGEMENT
    // ========================================================================

    /**
     * Get groups by status
     * GET /api/student-groups/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudentGroup>> getGroupsByStatus(@PathVariable GroupStatus status) {
        return ResponseEntity.ok(groupService.getGroupsByStatus(status));
    }

    /**
     * Activate group
     * POST /api/student-groups/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<StudentGroup> activateGroup(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentGroup activated = groupService.activateGroup(id, staffId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate group
     * POST /api/student-groups/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<StudentGroup> deactivateGroup(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentGroup deactivated = groupService.deactivateGroup(id, staffId);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Archive group
     * POST /api/student-groups/{id}/archive
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<StudentGroup> archiveGroup(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentGroup archived = groupService.archiveGroup(id, staffId);
            return ResponseEntity.ok(archived);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // HOUSE SYSTEM MANAGEMENT
    // ========================================================================

    /**
     * Get house leaderboard
     * GET /api/student-groups/houses/leaderboard
     */
    @GetMapping("/houses/leaderboard")
    public ResponseEntity<List<StudentGroup>> getHouseLeaderboard() {
        return ResponseEntity.ok(groupService.getHouseLeaderboard());
    }

    /**
     * Add house points
     * POST /api/student-groups/{id}/house-points/add
     */
    @PostMapping("/{id}/house-points/add")
    public ResponseEntity<StudentGroup> addHousePoints(
            @PathVariable Long id,
            @RequestParam Integer points,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.addHousePoints(id, points, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Add points failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Subtract house points
     * POST /api/student-groups/{id}/house-points/subtract
     */
    @PostMapping("/{id}/house-points/subtract")
    public ResponseEntity<StudentGroup> subtractHousePoints(
            @PathVariable Long id,
            @RequestParam Integer points,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.subtractHousePoints(id, points, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Subtract points failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reset house points
     * POST /api/student-groups/{id}/house-points/reset
     */
    @PostMapping("/{id}/house-points/reset")
    public ResponseEntity<StudentGroup> resetHousePoints(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentGroup updated = groupService.resetHousePoints(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // SEARCH
    // ========================================================================

    /**
     * Search groups by name
     * GET /api/student-groups/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<StudentGroup>> searchGroups(@RequestParam String searchTerm) {
        return ResponseEntity.ok(groupService.searchGroupsByName(searchTerm));
    }

    /**
     * Search groups by name or code
     * GET /api/student-groups/search/all
     */
    @GetMapping("/search/all")
    public ResponseEntity<List<StudentGroup>> searchGroupsByNameOrCode(@RequestParam String searchTerm) {
        return ResponseEntity.ok(groupService.searchGroupsByNameOrCode(searchTerm));
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Get group statistics
     * GET /api/student-groups/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getGroupStatistics() {
        return ResponseEntity.ok(groupService.getGroupStatistics());
    }

    /**
     * Count groups by type
     * GET /api/student-groups/count/type/{type}
     */
    @GetMapping("/count/type/{type}")
    public ResponseEntity<Long> countByType(@PathVariable GroupType type) {
        return ResponseEntity.ok(groupService.countByType(type));
    }

    /**
     * Count active groups
     * GET /api/student-groups/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActive() {
        return ResponseEntity.ok(groupService.countActive());
    }

    // ========================================================================
    // ACADEMIC YEAR TRANSITIONS
    // ========================================================================

    /**
     * Clone group for new academic year
     * POST /api/student-groups/{id}/clone
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<StudentGroup> cloneGroupForNewYear(
            @PathVariable Long id,
            @RequestParam String newAcademicYear,
            @RequestParam Long staffId) {
        try {
            StudentGroup cloned = groupService.cloneGroupForNewYear(id, newAcademicYear, staffId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cloned);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Bulk archive groups for academic year
     * POST /api/student-groups/bulk/archive-year
     */
    @PostMapping("/bulk/archive-year")
    public ResponseEntity<List<StudentGroup>> bulkArchiveByAcademicYear(
            @RequestParam String academicYear,
            @RequestParam Long staffId) {
        List<StudentGroup> archived = groupService.bulkArchiveByAcademicYear(academicYear, staffId);
        return ResponseEntity.ok(archived);
    }
}
