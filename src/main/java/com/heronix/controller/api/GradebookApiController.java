package com.heronix.controller.api;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.GradingCategory;
import com.heronix.service.GradebookService;
import com.heronix.service.GradebookService.ClassGradebook;
import com.heronix.service.GradebookService.StudentCourseGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST API Controller for Gradebook Management
 *
 * Provides comprehensive endpoints for managing course gradebooks including:
 * - Grading category management with weighted calculations
 * - Assignment creation and publishing
 * - Grade entry and bulk grading operations
 * - Student course grade calculations
 * - Class gradebook views
 * - Grade excuse and adjustment management
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
@RequestMapping("/api/gradebook")
@RequiredArgsConstructor
public class GradebookApiController {

    private final GradebookService gradebookService;

    // ========================================================================
    // GRADING CATEGORY MANAGEMENT
    // ========================================================================

    /**
     * Create default grading categories for a course
     *
     * POST /api/gradebook/courses/{courseId}/categories/default
     *
     * Creates standard K-12 categories: Tests (30%), Quizzes (20%),
     * Homework (20%), Projects (20%), Participation (10%)
     */
    @PostMapping("/courses/{courseId}/categories/default")
    public ResponseEntity<Map<String, Object>> createDefaultCategories(@PathVariable Long courseId) {
        try {
            List<GradingCategory> categories = gradebookService.createDefaultCategories(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());
            response.put("message", "Default grading categories created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating default categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get grading categories for a course
     *
     * GET /api/gradebook/courses/{courseId}/categories
     */
    @GetMapping("/courses/{courseId}/categories")
    public ResponseEntity<Map<String, Object>> getCategoriesForCourse(@PathVariable Long courseId) {
        try {
            List<GradingCategory> categories = gradebookService.getCategoriesForCourse(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update category weights
     *
     * PUT /api/gradebook/categories/weights
     *
     * Request Body:
     * {
     *   "categoryWeights": {
     *     "1": 35.0,
     *     "2": 25.0,
     *     "3": 20.0,
     *     "4": 15.0,
     *     "5": 5.0
     *   }
     * }
     *
     * Note: Weights must sum to 100%
     */
    @PutMapping("/categories/weights")
    public ResponseEntity<Map<String, Object>> updateCategoryWeights(
            @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Double> weightsMap = (Map<String, Double>) requestBody.get("categoryWeights");

            Map<Long, Double> categoryWeights = new HashMap<>();
            for (Map.Entry<String, Double> entry : weightsMap.entrySet()) {
                categoryWeights.put(Long.valueOf(entry.getKey()), entry.getValue());
            }

            gradebookService.updateCategoryWeights(categoryWeights);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category weights updated successfully");
            response.put("updatedWeights", categoryWeights);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating category weights: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // ASSIGNMENT MANAGEMENT
    // ========================================================================

    /**
     * Create a new assignment
     *
     * POST /api/gradebook/courses/{courseId}/assignments
     *
     * Request Body:
     * {
     *   "categoryId": 1,
     *   "title": "Chapter 5 Test",
     *   "dueDate": "2025-12-15",
     *   "maxPoints": 100.0
     * }
     */
    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<Map<String, Object>> createAssignment(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long categoryId = Long.valueOf(requestBody.get("categoryId").toString());
            String title = (String) requestBody.get("title");
            LocalDate dueDate = LocalDate.parse((String) requestBody.get("dueDate"));
            Double maxPoints = Double.valueOf(requestBody.get("maxPoints").toString());

            Assignment assignment = gradebookService.createAssignment(
                    courseId, categoryId, title, maxPoints, dueDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignment", assignment);
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
     * Get all assignments for a course
     *
     * GET /api/gradebook/courses/{courseId}/assignments
     */
    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<Map<String, Object>> getAssignmentsForCourse(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = gradebookService.getAssignmentsForCourse(courseId);

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
     * Get assignments by category
     *
     * GET /api/gradebook/categories/{categoryId}/assignments
     */
    @GetMapping("/categories/{categoryId}/assignments")
    public ResponseEntity<Map<String, Object>> getAssignmentsByCategory(@PathVariable Long categoryId) {
        try {
            List<Assignment> assignments = gradebookService.getAssignmentsByCategory(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());
            response.put("categoryId", categoryId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Publish an assignment (make visible to students)
     *
     * POST /api/gradebook/assignments/{assignmentId}/publish
     */
    @PostMapping("/assignments/{assignmentId}/publish")
    public ResponseEntity<Map<String, Object>> publishAssignment(@PathVariable Long assignmentId) {
        try {
            gradebookService.publishAssignment(assignmentId);

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

    // ========================================================================
    // GRADE ENTRY AND MANAGEMENT
    // ========================================================================

    /**
     * Enter a grade for a student
     *
     * POST /api/gradebook/grades
     *
     * Request Body:
     * {
     *   "studentId": 123,
     *   "assignmentId": 456,
     *   "score": 95.5,
     *   "submittedDate": "2025-12-15",
     *   "comments": "Excellent work!"
     * }
     */
    @PostMapping("/grades")
    public ResponseEntity<Map<String, Object>> enterGrade(@RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            Long assignmentId = Long.valueOf(requestBody.get("assignmentId").toString());
            Double score = Double.valueOf(requestBody.get("score").toString());
            LocalDate submittedDate = requestBody.containsKey("submittedDate") ?
                    LocalDate.parse((String) requestBody.get("submittedDate")) : LocalDate.now();
            String comments = requestBody.containsKey("comments") ?
                    (String) requestBody.get("comments") : null;

            AssignmentGrade grade = gradebookService.enterGrade(studentId, assignmentId, score, submittedDate, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("grade", grade);
            response.put("message", "Grade entered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error entering grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Bulk enter grades for an assignment
     *
     * POST /api/gradebook/assignments/{assignmentId}/grades/bulk
     *
     * Request Body:
     * {
     *   "studentScores": {
     *     "101": 95.0,
     *     "102": 87.5,
     *     "103": 92.0
     *   }
     * }
     */
    @PostMapping("/assignments/{assignmentId}/grades/bulk")
    public ResponseEntity<Map<String, Object>> bulkEnterGrades(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Double> scoresMap = (Map<String, Double>) requestBody.get("studentScores");

            Map<Long, Double> studentScores = new HashMap<>();
            for (Map.Entry<String, Double> entry : scoresMap.entrySet()) {
                studentScores.put(Long.valueOf(entry.getKey()), entry.getValue());
            }

            int gradesEntered = gradebookService.bulkEnterGrades(assignmentId, studentScores);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradesEntered", gradesEntered);
            response.put("message", gradesEntered + " grades entered successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error entering bulk grades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Excuse a student from an assignment
     *
     * POST /api/gradebook/grades/excuse
     *
     * Request Body:
     * {
     *   "studentId": 123,
     *   "assignmentId": 456,
     *   "reason": "Medical absence - doctor's note on file"
     * }
     */
    @PostMapping("/grades/excuse")
    public ResponseEntity<Map<String, Object>> excuseGrade(@RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            Long assignmentId = Long.valueOf(requestBody.get("assignmentId").toString());
            String reason = (String) requestBody.get("reason");

            gradebookService.excuseGrade(studentId, assignmentId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student excused from assignment");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error excusing grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // GRADE CALCULATIONS AND REPORTING
    // ========================================================================

    /**
     * Calculate course grade for a student
     *
     * GET /api/gradebook/students/{studentId}/courses/{courseId}/grade
     *
     * Returns calculated grade with category breakdowns
     */
    @GetMapping("/students/{studentId}/courses/{courseId}/grade")
    public ResponseEntity<Map<String, Object>> calculateCourseGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            StudentCourseGrade courseGrade = gradebookService.calculateCourseGrade(studentId, courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("courseGrade", courseGrade);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error calculating course grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get complete class gradebook
     *
     * GET /api/gradebook/courses/{courseId}/class-gradebook
     *
     * Returns all students' grades for all assignments in the course
     */
    @GetMapping("/courses/{courseId}/class-gradebook")
    public ResponseEntity<Map<String, Object>> getClassGradebook(@PathVariable Long courseId) {
        try {
            ClassGradebook gradebook = gradebookService.getClassGradebook(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("gradebook", gradebook);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving class gradebook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // METADATA AND HELP
    // ========================================================================

    /**
     * Get API metadata
     *
     * GET /api/gradebook/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("description", "Gradebook Management API");
        metadata.put("features", Arrays.asList(
                "Grading category management with weighted calculations",
                "Assignment creation and publishing",
                "Grade entry and bulk grading operations",
                "Student course grade calculations",
                "Class gradebook views",
                "Grade excuse and adjustment management"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("metadata", metadata);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API help and usage information
     *
     * GET /api/gradebook/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Gradebook Management API - Comprehensive endpoints for managing course gradebooks");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("POST /api/gradebook/courses/{courseId}/categories/default", "Create default grading categories");
        endpoints.put("GET /api/gradebook/courses/{courseId}/categories", "Get categories for a course");
        endpoints.put("PUT /api/gradebook/categories/weights", "Update category weights");
        endpoints.put("POST /api/gradebook/courses/{courseId}/assignments", "Create assignment");
        endpoints.put("GET /api/gradebook/courses/{courseId}/assignments", "Get all assignments");
        endpoints.put("POST /api/gradebook/assignments/{assignmentId}/publish", "Publish assignment");
        endpoints.put("POST /api/gradebook/grades", "Enter grade");
        endpoints.put("POST /api/gradebook/assignments/{assignmentId}/grades/bulk", "Bulk enter grades");
        endpoints.put("POST /api/gradebook/grades/excuse", "Excuse student from assignment");
        endpoints.put("GET /api/gradebook/students/{studentId}/courses/{courseId}/grade", "Calculate course grade");
        endpoints.put("GET /api/gradebook/courses/{courseId}/class-gradebook", "Get class gradebook");

        help.put("endpoints", endpoints);

        Map<String, String> examples = new LinkedHashMap<>();
        examples.put("Create Default Categories", "POST /api/gradebook/courses/123/categories/default");
        examples.put("Enter Grade", "POST /api/gradebook/grades with body: {\"studentId\":101,\"assignmentId\":456,\"score\":95.5,\"comments\":\"Great work!\"}");
        examples.put("Bulk Grades", "POST /api/gradebook/assignments/456/grades/bulk with body: {\"studentScores\":{\"101\":95.0,\"102\":87.5}}");
        examples.put("Calculate Grade", "GET /api/gradebook/students/101/courses/123/grade");
        examples.put("Class Gradebook", "GET /api/gradebook/courses/123/class-gradebook");

        help.put("examples", examples);

        Map<String, String> notes = new LinkedHashMap<>();
        notes.put("grading", "Supports weighted category grading with drop-lowest option");
        notes.put("calculations", "Grade calculations: (category avg * weight) summed across all categories");
        notes.put("publishing", "Assignments must be published to be visible to students");
        notes.put("excusing", "Excused assignments are excluded from grade calculations");

        help.put("notes", notes);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("help", help);
        return ResponseEntity.ok(response);
    }
}
