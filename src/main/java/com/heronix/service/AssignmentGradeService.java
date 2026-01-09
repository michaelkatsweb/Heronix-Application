package com.heronix.service;

import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.Teacher;

import java.util.List;
import java.util.Map;

/**
 * Service interface for AssignmentGrade operations
 * Handles individual assignment grading within courses (distinct from StudentGrade for final course grades)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
public interface AssignmentGradeService {

    // ========================================================================
    // GRADE ENTRY AND MANAGEMENT
    // ========================================================================

    /**
     * Enter a grade for a student on an assignment
     */
    AssignmentGrade enterGrade(Long studentId, Long assignmentId, Double score, Teacher teacher);

    /**
     * Enter a grade with comments
     */
    AssignmentGrade enterGrade(Long studentId, Long assignmentId, Double score, String comments, Teacher teacher);

    /**
     * Update an existing grade
     */
    AssignmentGrade updateGrade(Long gradeId, Double score, String comments);

    /**
     * Mark an assignment as excused for a student
     */
    void markExcused(Long gradeId, String reason);

    /**
     * Mark an assignment as missing for a student
     */
    void markMissing(Long gradeId);

    /**
     * Mark an assignment as late for a student
     */
    void markLate(Long gradeId);

    /**
     * Delete a grade
     */
    void deleteGrade(Long gradeId);

    // ========================================================================
    // QUERIES
    // ========================================================================

    /**
     * Get a specific grade for a student on an assignment
     */
    AssignmentGrade getGrade(Long studentId, Long assignmentId);

    /**
     * Get all grades for a student in a course
     */
    List<AssignmentGrade> getStudentGrades(Long studentId, Long courseId);

    /**
     * Get all grades for an assignment (class roster)
     */
    List<AssignmentGrade> getAssignmentGrades(Long assignmentId);

    /**
     * Get missing assignments for a student in a course
     */
    List<AssignmentGrade> getMissingAssignments(Long studentId, Long courseId);

    /**
     * Get grades for gradebook calculation (excludes non-counted assignments)
     */
    List<AssignmentGrade> getGradesForCalculation(Long studentId, Long courseId);

    // ========================================================================
    // GRADE CALCULATION
    // ========================================================================

    /**
     * Calculate overall course grade for a student (weighted by categories)
     */
    Double calculateCourseGrade(Long studentId, Long courseId);

    /**
     * Calculate grade for a specific category
     */
    Double calculateCategoryGrade(Long studentId, Long categoryId);

    /**
     * Get breakdown of grades by category
     */
    Map<String, Double> getCategoryBreakdown(Long studentId, Long courseId);

    /**
     * Calculate what-if grade (simulate adding a new assignment score)
     */
    Double calculateWhatIfGrade(Long studentId, Long courseId, Long categoryId, Double hypotheticalScore);

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Get class average for an assignment
     */
    Double getClassAverage(Long assignmentId);

    /**
     * Get statistics for an assignment (min, max, avg, median)
     */
    Map<String, Double> getAssignmentStatistics(Long assignmentId);

    /**
     * Count graded assignments for a course
     */
    long countGradedAssignments(Long courseId);

    /**
     * Count missing assignments for a student in a course
     */
    long countMissingAssignments(Long studentId, Long courseId);
}
