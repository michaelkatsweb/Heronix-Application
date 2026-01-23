package com.heronix.repository;

import com.heronix.model.domain.IncompleteRegistration;
import com.heronix.model.domain.IncompleteRegistration.IncompleteReason;
import com.heronix.model.domain.IncompleteRegistration.Priority;
import com.heronix.model.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for IncompleteRegistration entity
 *
 * Provides queries for tracking and managing incomplete student registrations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Repository
public interface IncompleteRegistrationRepository extends JpaRepository<IncompleteRegistration, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all incomplete registrations by status
     */
    List<IncompleteRegistration> findByStatus(RegistrationStatus status);

    /**
     * Find all incomplete registrations by reason
     */
    List<IncompleteRegistration> findByIncompleteReason(IncompleteReason reason);

    /**
     * Find all incomplete registrations by priority
     */
    List<IncompleteRegistration> findByPriority(Priority priority);

    /**
     * Find all urgent cases
     */
    List<IncompleteRegistration> findByIsUrgentTrue();

    /**
     * Find incomplete registrations assigned to a staff member
     */
    List<IncompleteRegistration> findByAssignedTo(String staffName);

    /**
     * Find by student ID
     */
    List<IncompleteRegistration> findByStudentId(Long studentId);

    /**
     * Find by grade level
     */
    List<IncompleteRegistration> findByGradeLevel(String gradeLevel);

    // ========================================================================
    // STATUS-BASED QUERIES
    // ========================================================================

    /**
     * Find all active incomplete registrations (not completed)
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.completedDate IS NULL " +
           "ORDER BY ir.priority DESC, ir.incompleteSince ASC")
    List<IncompleteRegistration> findAllActive();

    /**
     * Find all active incomplete registrations with specific statuses
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.status IN :statuses " +
           "AND ir.completedDate IS NULL ORDER BY ir.incompleteSince ASC")
    List<IncompleteRegistration> findByStatusesActive(@Param("statuses") List<RegistrationStatus> statuses);

    /**
     * Find completed registrations
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.completedDate IS NOT NULL " +
           "ORDER BY ir.completedDate DESC")
    List<IncompleteRegistration> findCompleted();

    // ========================================================================
    // DATE-BASED QUERIES
    // ========================================================================

    /**
     * Find overdue registrations (expected completion date passed)
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.expectedCompletionDate < :today " +
           "AND ir.completedDate IS NULL ORDER BY ir.expectedCompletionDate ASC")
    List<IncompleteRegistration> findOverdue(@Param("today") LocalDate today);

    /**
     * Find registrations needing follow-up (next follow-up date is today or past)
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.nextFollowupDate <= :today " +
           "AND ir.completedDate IS NULL ORDER BY ir.nextFollowupDate ASC")
    List<IncompleteRegistration> findNeedingFollowup(@Param("today") LocalDate today);

    /**
     * Find registrations created within a date range
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.registrationDate BETWEEN :startDate AND :endDate")
    List<IncompleteRegistration> findByRegistrationDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find registrations incomplete for more than X days
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.incompleteSince < :cutoffDate " +
           "AND ir.completedDate IS NULL ORDER BY ir.incompleteSince ASC")
    List<IncompleteRegistration> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // DOCUMENT-BASED QUERIES
    // ========================================================================

    /**
     * Find registrations missing birth certificate
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.missingBirthCertificate = true " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> findMissingBirthCertificate();

    /**
     * Find registrations missing immunization records
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.missingImmunization = true " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> findMissingImmunization();

    /**
     * Find registrations missing proof of residence
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.missingProofOfResidence = true " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> findMissingProofOfResidence();

    /**
     * Find registrations missing student photo
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.missingPhoto = true " +
           "AND ir.photoRefused = false AND ir.completedDate IS NULL")
    List<IncompleteRegistration> findMissingPhoto();

    /**
     * Find registrations where parent refused photo
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE ir.photoRefused = true")
    List<IncompleteRegistration> findPhotoRefused();

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count active incomplete registrations
     */
    @Query("SELECT COUNT(ir) FROM IncompleteRegistration ir WHERE ir.completedDate IS NULL")
    long countActive();

    /**
     * Count overdue registrations
     */
    @Query("SELECT COUNT(ir) FROM IncompleteRegistration ir WHERE ir.expectedCompletionDate < :today " +
           "AND ir.completedDate IS NULL")
    long countOverdue(@Param("today") LocalDate today);

    /**
     * Count by status
     */
    @Query("SELECT COUNT(ir) FROM IncompleteRegistration ir WHERE ir.status = :status " +
           "AND ir.completedDate IS NULL")
    long countByStatusActive(@Param("status") RegistrationStatus status);

    /**
     * Count needing follow-up
     */
    @Query("SELECT COUNT(ir) FROM IncompleteRegistration ir WHERE ir.nextFollowupDate <= :today " +
           "AND ir.completedDate IS NULL")
    long countNeedingFollowup(@Param("today") LocalDate today);

    /**
     * Count urgent cases
     */
    @Query("SELECT COUNT(ir) FROM IncompleteRegistration ir WHERE ir.isUrgent = true " +
           "AND ir.completedDate IS NULL")
    long countUrgent();

    /**
     * Count by grade level
     */
    @Query("SELECT ir.gradeLevel, COUNT(ir) FROM IncompleteRegistration ir " +
           "WHERE ir.completedDate IS NULL GROUP BY ir.gradeLevel")
    List<Object[]> countByGradeLevel();

    /**
     * Count by incomplete reason
     */
    @Query("SELECT ir.incompleteReason, COUNT(ir) FROM IncompleteRegistration ir " +
           "WHERE ir.completedDate IS NULL GROUP BY ir.incompleteReason")
    List<Object[]> countByIncompleteReason();

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search by student name
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE " +
           "(LOWER(ir.studentFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ir.studentLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> searchByStudentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by guardian name
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE " +
           "LOWER(ir.guardianName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> searchByGuardianName(@Param("searchTerm") String searchTerm);

    /**
     * Full text search (student name, guardian name, or notes)
     */
    @Query("SELECT ir FROM IncompleteRegistration ir WHERE " +
           "(LOWER(ir.studentFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ir.studentLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ir.guardianName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ir.followupNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND ir.completedDate IS NULL")
    List<IncompleteRegistration> searchAll(@Param("searchTerm") String searchTerm);
}
