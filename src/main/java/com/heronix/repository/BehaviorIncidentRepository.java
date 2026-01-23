package com.heronix.repository;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for BehaviorIncident entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 1 - Multi-Level Monitoring and Reporting
 */
@Repository
public interface BehaviorIncidentRepository extends JpaRepository<BehaviorIncident, Long> {

    // Find all behavior incidents for a student
    List<BehaviorIncident> findByStudent(Student student);

    // Find behavior incidents within date range
    List<BehaviorIncident> findByStudentAndIncidentDateBetween(
            Student student,
            LocalDate startDate,
            LocalDate endDate
    );

    // Find behavior incidents by type (positive/negative)
    List<BehaviorIncident> findByStudentAndBehaviorType(Student student, BehaviorType behaviorType);

    // Find behavior incidents by type within date range
    List<BehaviorIncident> findByStudentAndBehaviorTypeAndIncidentDateBetween(
            Student student,
            BehaviorType behaviorType,
            LocalDate startDate,
            LocalDate endDate
    );

    // Count behavior incidents by type within date range
    @Query("SELECT COUNT(b) FROM BehaviorIncident b WHERE b.student = :student " +
           "AND b.behaviorType = :behaviorType AND b.incidentDate BETWEEN :startDate AND :endDate")
    Long countByStudentAndBehaviorTypeAndDateBetween(
            @Param("student") Student student,
            @Param("behaviorType") BehaviorType behaviorType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find incidents requiring admin referral
    List<BehaviorIncident> findByStudentAndAdminReferralRequired(Student student, Boolean required);

    // Find recent negative incidents requiring immediate attention
    @Query("SELECT b FROM BehaviorIncident b WHERE b.student = :student " +
           "AND b.behaviorType = 'NEGATIVE' AND b.incidentDate >= :sinceDate " +
           "AND (b.severityLevel = 'MAJOR' OR b.adminReferralRequired = true) " +
           "ORDER BY b.incidentDate DESC, b.incidentTime DESC")
    List<BehaviorIncident> findCriticalIncidentsSince(
            @Param("student") Student student,
            @Param("sinceDate") LocalDate sinceDate
    );

    // Find all incidents within date range (for school/district reporting)
    List<BehaviorIncident> findByIncidentDateBetween(LocalDate startDate, LocalDate endDate);

    // Find incidents by behavior category
    @Query("SELECT b FROM BehaviorIncident b WHERE b.student = :student " +
           "AND b.behaviorCategory = :category")
    List<BehaviorIncident> findByStudentAndCategory(
            @Param("student") Student student,
            @Param("category") BehaviorIncident.BehaviorCategory category
    );

    // Find incidents requiring parent contact that haven't been contacted
    @Query("SELECT b FROM BehaviorIncident b WHERE b.student = :student " +
           "AND b.behaviorType = 'NEGATIVE' AND b.parentContacted = false " +
           "ORDER BY b.incidentDate DESC")
    List<BehaviorIncident> findUncontactedParentIncidents(@Param("student") Student student);

    // ========================================================================
    // ANALYTICS QUERIES - Phase 59
    // ========================================================================

    /**
     * Get daily incident counts for trend analysis
     */
    @Query("SELECT b.incidentDate, COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.incidentDate " +
           "ORDER BY b.incidentDate")
    List<Object[]> getDailyIncidentCounts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incident counts by category
     */
    @Query("SELECT b.behaviorCategory, COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.behaviorCategory " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> getIncidentCountsByCategory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incident counts by location
     */
    @Query("SELECT b.incidentLocation, COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.incidentLocation " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> getIncidentCountsByLocation(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incident counts by type (positive/negative)
     */
    @Query("SELECT b.behaviorType, COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.behaviorType")
    List<Object[]> getIncidentCountsByType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incident counts by severity
     */
    @Query("SELECT b.severityLevel, COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.severityLevel")
    List<Object[]> getIncidentCountsBySeverity(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Find repeat offenders (students with multiple negative incidents)
     */
    @Query("SELECT b.student.id, b.student.studentId, b.student.firstName, b.student.lastName, " +
           "b.student.gradeLevel, COUNT(b) " +
           "FROM BehaviorIncident b " +
           "WHERE b.behaviorType = 'NEGATIVE' " +
           "AND b.incidentDate >= :sinceDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.student.id, b.student.studentId, b.student.firstName, b.student.lastName, b.student.gradeLevel " +
           "HAVING COUNT(b) >= :threshold " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findRepeatOffenders(
            @Param("sinceDate") LocalDate sinceDate,
            @Param("campusId") Long campusId,
            @Param("threshold") Long threshold);

    /**
     * Get total incidents summary
     */
    @Query("SELECT " +
           "COUNT(b), " +
           "SUM(CASE WHEN b.behaviorType = 'POSITIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.behaviorType = 'NEGATIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.severityLevel = 'MINOR' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.severityLevel = 'MODERATE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.severityLevel = 'MAJOR' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.severityLevel = 'SEVERE' THEN 1 ELSE 0 END) " +
           "FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId)")
    List<Object[]> getIncidentSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incidents by grade level
     */
    @Query("SELECT b.student.gradeLevel, b.behaviorType, COUNT(b) " +
           "FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.student.gradeLevel, b.behaviorType " +
           "ORDER BY b.student.gradeLevel")
    List<Object[]> getIncidentsByGradeLevel(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get incidents by campus (district-wide view)
     */
    @Query("SELECT b.campus.id, b.campus.name, b.behaviorType, COUNT(b) " +
           "FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND b.campus IS NOT NULL " +
           "GROUP BY b.campus.id, b.campus.name, b.behaviorType")
    List<Object[]> getIncidentsByCampus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count total incidents for date range
     */
    @Query("SELECT COUNT(b) FROM BehaviorIncident b " +
           "WHERE b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId)")
    Long countIncidents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Find students with highest positive behavior counts
     */
    @Query("SELECT b.student.id, b.student.studentId, b.student.firstName, b.student.lastName, " +
           "b.student.gradeLevel, COUNT(b) " +
           "FROM BehaviorIncident b " +
           "WHERE b.behaviorType = 'POSITIVE' " +
           "AND b.incidentDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR b.campus.id = :campusId) " +
           "GROUP BY b.student.id, b.student.studentId, b.student.firstName, b.student.lastName, b.student.gradeLevel " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findTopPositiveBehaviorStudents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);
}
