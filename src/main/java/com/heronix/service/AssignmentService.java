package com.heronix.service;

import com.heronix.model.domain.Assignment;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Assignment operations
 * Location: src/main/java/com/heronix/service/AssignmentService.java
 *
 * Provides business logic for assignment management including:
 * - CRUD operations
 * - Publishing workflow
 * - Due date management
 * - Class statistics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
public interface AssignmentService {

    /**
     * Create a new assignment
     *
     * @param assignment Assignment to create
     * @return Created assignment with generated ID
     */
    Assignment createAssignment(Assignment assignment);

    /**
     * Update an existing assignment
     *
     * @param id Assignment ID
     * @param assignment Updated assignment data
     * @return Updated assignment
     */
    Assignment updateAssignment(Long id, Assignment assignment);

    /**
     * Delete an assignment (and all associated grades)
     *
     * @param id Assignment ID
     */
    void deleteAssignment(Long id);

    /**
     * Get assignment by ID
     *
     * @param id Assignment ID
     * @return Assignment or null if not found
     */
    Assignment getAssignmentById(Long id);

    /**
     * Get all assignments for a course
     *
     * @param courseId Course ID
     * @return List of assignments ordered by due date (newest first)
     */
    List<Assignment> getAssignmentsByCourse(Long courseId);

    /**
     * Get assignments for a course by term
     *
     * @param courseId Course ID
     * @param term Term identifier (e.g., "Fall 2024", "Q1 2024-25")
     * @return List of assignments for the term
     */
    List<Assignment> getAssignmentsByTerm(Long courseId, String term);

    /**
     * Get published assignments for a course
     *
     * @param courseId Course ID
     * @return List of published assignments
     */
    List<Assignment> getPublishedAssignments(Long courseId);

    /**
     * Get upcoming assignments (due in next N days)
     *
     * @param courseId Course ID
     * @param days Number of days to look ahead
     * @return List of upcoming assignments
     */
    List<Assignment> getUpcomingAssignments(Long courseId, int days);

    /**
     * Get past due assignments
     *
     * @param courseId Course ID
     * @return List of past due assignments
     */
    List<Assignment> getPastDueAssignments(Long courseId);

    /**
     * Get assignments with all grades loaded (for gradebook view)
     *
     * @param courseId Course ID
     * @return List of assignments with grades eagerly loaded
     */
    List<Assignment> getAssignmentsWithGrades(Long courseId);

    /**
     * Publish an assignment (make visible to students)
     *
     * @param id Assignment ID
     */
    void publishAssignment(Long id);

    /**
     * Unpublish an assignment (hide from students)
     *
     * @param id Assignment ID
     */
    void unpublishAssignment(Long id);

    /**
     * Get class average for an assignment
     *
     * @param assignmentId Assignment ID
     * @return Average score as percentage (0-100) or null if no grades
     */
    Double getClassAverage(Long assignmentId);

    /**
     * Count total assignments for a course
     *
     * @param courseId Course ID
     * @return Number of assignments
     */
    long countAssignmentsByCourse(Long courseId);

    /**
     * Count graded assignments for a course
     *
     * @param courseId Course ID
     * @return Number of assignments with at least one grade
     */
    long countGradedAssignments(Long courseId);
}
