package com.heronix.controller.api;

import com.heronix.model.domain.Assignment;
import com.heronix.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API Controller for Assignment Management
 *
 * Provides comprehensive endpoints for managing academic assignments including:
 * - CRUD operations for assignments
 * - Publishing workflow and visibility control
 * - Due date management and tracking
 * - Class statistics and performance metrics
 * - Term-based filtering
 * - Gradebook integration
 *
 * All endpoints return JSON responses with standard structure:
 * {
 *   "success": true/false,
 *   "data": {...},
 *   "message": "Description of result"
 * }
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentApiController {

    private final AssignmentService assignmentService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new assignment
     *
     * POST /api/assignments
     *
     * Request Body: Assignment object
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody Assignment assignment) {
        try {
            Assignment created = assignmentService.createAssignment(assignment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignment", created);
            response.put("message", "Assignment created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignment by ID
     *
     * GET /api/assignments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAssignmentById(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.getAssignmentById(id);

            if (assignment == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Assignment not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignment", assignment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update an existing assignment
     *
     * PUT /api/assignments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignment) {
        try {
            Assignment updated = assignmentService.updateAssignment(id, assignment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignment", updated);
            response.put("message", "Assignment updated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete an assignment
     *
     * DELETE /api/assignments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment and all associated grades deleted successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // QUERY OPERATIONS - COURSE ASSIGNMENTS
    // ========================================================================

    /**
     * Get all assignments for a course
     *
     * GET /api/assignments/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getAssignmentsByCourse(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignments for a course by term
     *
     * GET /api/assignments/course/{courseId}/term/{term}
     */
    @GetMapping("/course/{courseId}/term/{term}")
    public ResponseEntity<Map<String, Object>> getAssignmentsByTerm(
            @PathVariable Long courseId,
            @PathVariable String term) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsByTerm(courseId, term);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            response.put("term", term);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving assignments by term: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get published assignments for a course
     *
     * GET /api/assignments/course/{courseId}/published
     */
    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<Map<String, Object>> getPublishedAssignments(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getPublishedAssignments(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving published assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignments with grades (for gradebook)
     *
     * GET /api/assignments/course/{courseId}/with-grades
     */
    @GetMapping("/course/{courseId}/with-grades")
    public ResponseEntity<Map<String, Object>> getAssignmentsWithGrades(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsWithGrades(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving assignments with grades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // QUERY OPERATIONS - DUE DATE FILTERING
    // ========================================================================

    /**
     * Get upcoming assignments (due in next N days)
     *
     * GET /api/assignments/course/{courseId}/upcoming
     */
    @GetMapping("/course/{courseId}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingAssignments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<Assignment> assignments = assignmentService.getUpcomingAssignments(courseId, days);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            response.put("daysAhead", days);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving upcoming assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get past due assignments
     *
     * GET /api/assignments/course/{courseId}/past-due
     */
    @GetMapping("/course/{courseId}/past-due")
    public ResponseEntity<Map<String, Object>> getPastDueAssignments(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getPastDueAssignments(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving past due assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // PUBLISHING WORKFLOW
    // ========================================================================

    /**
     * Publish an assignment (make visible to students)
     *
     * POST /api/assignments/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishAssignment(@PathVariable Long id) {
        try {
            assignmentService.publishAssignment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment published successfully - now visible to students");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error publishing assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Unpublish an assignment (hide from students)
     *
     * POST /api/assignments/{id}/unpublish
     */
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishAssignment(@PathVariable Long id) {
        try {
            assignmentService.unpublishAssignment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment unpublished successfully - now hidden from students");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error unpublishing assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // STATISTICS AND ANALYTICS
    // ========================================================================

    /**
     * Get class average for an assignment
     *
     * GET /api/assignments/{id}/class-average
     */
    @GetMapping("/{id}/class-average")
    public ResponseEntity<Map<String, Object>> getClassAverage(@PathVariable Long id) {
        try {
            Double average = assignmentService.getClassAverage(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignmentId", id);
            response.put("classAverage", average);
            response.put("hasGrades", average != null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving class average: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignment statistics for a course
     *
     * GET /api/assignments/course/{courseId}/statistics
     */
    @GetMapping("/course/{courseId}/statistics")
    public ResponseEntity<Map<String, Object>> getCourseStatistics(@PathVariable Long courseId) {
        try {
            long totalAssignments = assignmentService.countAssignmentsByCourse(courseId);
            long gradedAssignments = assignmentService.countGradedAssignments(courseId);

            List<Assignment> published = assignmentService.getPublishedAssignments(courseId);
            List<Assignment> upcoming = assignmentService.getUpcomingAssignments(courseId, 7);
            List<Assignment> pastDue = assignmentService.getPastDueAssignments(courseId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAssignments", totalAssignments);
            stats.put("gradedAssignments", gradedAssignments);
            stats.put("publishedCount", published.size());
            stats.put("upcomingCount", upcoming.size());
            stats.put("pastDueCount", pastDue.size());
            stats.put("ungradedCount", totalAssignments - gradedAssignments);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving course statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignment dashboard for a course
     *
     * GET /api/assignments/course/{courseId}/dashboard
     */
    @GetMapping("/course/{courseId}/dashboard")
    public ResponseEntity<Map<String, Object>> getCourseDashboard(@PathVariable Long courseId) {
        try {
            List<Assignment> allAssignments = assignmentService.getAssignmentsByCourse(courseId);
            List<Assignment> published = assignmentService.getPublishedAssignments(courseId);
            List<Assignment> upcoming = assignmentService.getUpcomingAssignments(courseId, 7);
            List<Assignment> pastDue = assignmentService.getPastDueAssignments(courseId);

            long totalAssignments = assignmentService.countAssignmentsByCourse(courseId);
            long gradedAssignments = assignmentService.countGradedAssignments(courseId);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalAssignments", totalAssignments);
            dashboard.put("publishedCount", published.size());
            dashboard.put("upcomingCount", upcoming.size());
            dashboard.put("pastDueCount", pastDue.size());
            dashboard.put("gradedCount", gradedAssignments);
            dashboard.put("ungradedCount", totalAssignments - gradedAssignments);
            dashboard.put("recentAssignments", allAssignments.stream().limit(5).toList());
            dashboard.put("upcomingAssignments", upcoming);
            dashboard.put("pastDueAssignments", pastDue);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("dashboard", dashboard);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving course dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // METADATA AND HELP
    // ========================================================================

    /**
     * Get API metadata
     *
     * GET /api/assignments/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("description", "Assignment Management API");
        metadata.put("features", Arrays.asList(
                "CRUD operations for assignments",
                "Publishing workflow and visibility control",
                "Due date management and tracking",
                "Class statistics and performance metrics",
                "Term-based filtering",
                "Gradebook integration"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("metadata", metadata);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API help and usage information
     *
     * GET /api/assignments/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Assignment Management API - Comprehensive endpoints for managing academic assignments");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("POST /api/assignments", "Create new assignment");
        endpoints.put("GET /api/assignments/{id}", "Get assignment by ID");
        endpoints.put("PUT /api/assignments/{id}", "Update assignment");
        endpoints.put("DELETE /api/assignments/{id}", "Delete assignment");
        endpoints.put("GET /api/assignments/course/{courseId}", "Get all assignments for a course");
        endpoints.put("GET /api/assignments/course/{courseId}/published", "Get published assignments");
        endpoints.put("GET /api/assignments/course/{courseId}/upcoming", "Get upcoming assignments");
        endpoints.put("GET /api/assignments/course/{courseId}/past-due", "Get past due assignments");
        endpoints.put("POST /api/assignments/{id}/publish", "Publish assignment");
        endpoints.put("POST /api/assignments/{id}/unpublish", "Unpublish assignment");
        endpoints.put("GET /api/assignments/{id}/class-average", "Get class average");
        endpoints.put("GET /api/assignments/course/{courseId}/statistics", "Get course statistics");
        endpoints.put("GET /api/assignments/course/{courseId}/dashboard", "Get course dashboard");

        help.put("endpoints", endpoints);

        Map<String, String> examples = new LinkedHashMap<>();
        examples.put("Create Assignment", "POST /api/assignments with Assignment object");
        examples.put("Get Course Assignments", "GET /api/assignments/course/123");
        examples.put("Get Upcoming (next 14 days)", "GET /api/assignments/course/123/upcoming?days=14");
        examples.put("Publish Assignment", "POST /api/assignments/456/publish");
        examples.put("Get Dashboard", "GET /api/assignments/course/123/dashboard");

        help.put("examples", examples);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("help", help);
        return ResponseEntity.ok(response);
    }
}
