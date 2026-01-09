package com.heronix.repository;

import com.heronix.model.domain.FaceEnrollment;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Face Enrollment Repository
 *
 * Data access layer for facial recognition enrollment management.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Repository
public interface FaceEnrollmentRepository extends JpaRepository<FaceEnrollment, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find active face enrollment for a student
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE f.student = :student AND f.active = true")
    Optional<FaceEnrollment> findActiveByStudent(@Param("student") Student student);

    /**
     * Find all enrollments for a student (active and inactive)
     */
    List<FaceEnrollment> findByStudent(Student student);

    /**
     * Find enrollment by student and active status
     */
    Optional<FaceEnrollment> findByStudentAndActive(Student student, Boolean active);

    // ========================================================================
    // STATUS QUERIES
    // ========================================================================

    /**
     * Find all active enrollments
     */
    List<FaceEnrollment> findByActiveTrue();

    /**
     * Find all inactive enrollments
     */
    List<FaceEnrollment> findByActiveFalse();

    /**
     * Count active enrollments
     */
    long countByActiveTrue();

    // ========================================================================
    // QUALITY QUERIES
    // ========================================================================

    /**
     * Find enrollments with low quality scores
     */
    List<FaceEnrollment> findByQualityScoreLessThan(Double threshold);

    /**
     * Find enrollments with quality scores in range
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "f.qualityScore >= :minScore AND " +
           "f.qualityScore <= :maxScore")
    List<FaceEnrollment> findByQualityScoreBetween(@Param("minScore") Double minScore,
                                                     @Param("maxScore") Double maxScore);

    /**
     * Find enrollments with acceptable quality
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "f.qualityScore >= :minThreshold")
    List<FaceEnrollment> findByAcceptableQuality(@Param("minThreshold") Double minThreshold);

    // ========================================================================
    // RE-ENROLLMENT QUERIES
    // ========================================================================

    /**
     * Find enrollments needing re-enrollment
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "f.reenrollmentRequested = true")
    List<FaceEnrollment> findNeedingReenrollment();

    /**
     * Find enrollments with low success rate
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "(f.successfulMatchCount + f.failedMatchCount) >= 10 AND " +
           "(CAST(f.successfulMatchCount AS double) / " +
           "(f.successfulMatchCount + f.failedMatchCount)) < :minSuccessRate")
    List<FaceEnrollment> findByLowSuccessRate(@Param("minSuccessRate") Double minSuccessRate);

    // ========================================================================
    // STUDENT QUERIES
    // ========================================================================

    /**
     * Find students without active face enrollment
     */
    @Query("SELECT s FROM Student s WHERE s.active = true AND " +
           "s.id NOT IN (SELECT f.student.id FROM FaceEnrollment f WHERE f.active = true)")
    List<Student> findStudentsWithoutEnrollment();

    /**
     * Find students with multiple active enrollments (data integrity check)
     */
    @Query("SELECT f.student FROM FaceEnrollment f WHERE f.active = true " +
           "GROUP BY f.student HAVING COUNT(f.id) > 1")
    List<Student> findStudentsWithMultipleActiveEnrollments();

    // ========================================================================
    // DATE QUERIES
    // ========================================================================

    /**
     * Find enrollments created within date range
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.enrolledAt >= :startDate AND " +
           "f.enrolledAt <= :endDate")
    List<FaceEnrollment> findEnrolledBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Find enrollments not matched recently
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "(f.lastMatchedAt IS NULL OR " +
           "f.lastMatchedAt < :cutoffDate)")
    List<FaceEnrollment> findNotMatchedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // PROVIDER QUERIES
    // ========================================================================

    /**
     * Find enrollments by facial recognition provider
     */
    List<FaceEnrollment> findByProvider(String provider);

    /**
     * Count enrollments by provider
     */
    long countByProvider(String provider);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Find most used enrollments (highest match count)
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE f.active = true " +
           "ORDER BY (f.successfulMatchCount + f.failedMatchCount) DESC")
    List<FaceEnrollment> findMostUsedEnrollments();

    /**
     * Find enrollments with highest average confidence
     */
    @Query("SELECT f FROM FaceEnrollment f WHERE " +
           "f.active = true AND " +
           "f.averageConfidenceScore IS NOT NULL " +
           "ORDER BY f.averageConfidenceScore DESC")
    List<FaceEnrollment> findByHighestConfidence();

    /**
     * Count enrollments by grade level
     */
    @Query("SELECT s.gradeLevel, COUNT(f) FROM FaceEnrollment f " +
           "JOIN f.student s WHERE f.active = true " +
           "GROUP BY s.gradeLevel")
    List<Object[]> countByGradeLevel();
}
