package com.heronix.repository;

import com.heronix.model.domain.StudentAccommodation;
import com.heronix.model.domain.StudentAccommodation.AccommodationType;
import com.heronix.model.domain.StudentAccommodation.AccommodationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentAccommodation entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Repository
public interface StudentAccommodationRepository extends JpaRepository<StudentAccommodation, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all accommodations for a student
     */
    List<StudentAccommodation> findByStudentId(Long studentId);

    /**
     * Find by student and type
     */
    List<StudentAccommodation> findByStudentIdAndType(Long studentId, AccommodationType type);

    /**
     * Find by student and status
     */
    List<StudentAccommodation> findByStudentIdAndStatus(Long studentId, AccommodationStatus status);

    /**
     * Find active accommodations for a student
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.student.id = :studentId " +
           "AND sa.status = 'ACTIVE' " +
           "AND (sa.endDate IS NULL OR sa.endDate > CURRENT_DATE)")
    List<StudentAccommodation> findActiveByStudentId(@Param("studentId") Long studentId);

    // ========================================================================
    // TYPE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Find all students with 504 Plans
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.has504Plan = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllWith504Plans();

    /**
     * Find all students with IEPs
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.hasIEP = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllWithIEPs();

    /**
     * Find all ELL students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.isELL = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllELLStudents();

    /**
     * Find all gifted students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.isGifted = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllGiftedStudents();

    /**
     * Find all at-risk students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.isAtRisk = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllAtRiskStudents();

    /**
     * Find all Title I students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.titleIParticipating = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllTitleIStudents();

    /**
     * Find all homeless students (McKinney-Vento)
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.homelessStatus = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllHomelessStudents();

    /**
     * Find all foster care students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.fosterCareStatus = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllFosterCareStudents();

    /**
     * Find all military family students
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.militaryFamily = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findAllMilitaryFamilyStudents();

    // ========================================================================
    // REVIEW & EXPIRATION QUERIES
    // ========================================================================

    /**
     * Find accommodations with overdue reviews
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.status = 'ACTIVE' " +
           "AND sa.nextReviewDate IS NOT NULL AND sa.nextReviewDate < CURRENT_DATE")
    List<StudentAccommodation> findOverdueReviews();

    /**
     * Find accommodations expiring soon (within specified days)
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.status = 'ACTIVE' " +
           "AND sa.endDate IS NOT NULL " +
           "AND sa.endDate BETWEEN CURRENT_DATE AND :futureDate")
    List<StudentAccommodation> findExpiringSoon(@Param("futureDate") LocalDate futureDate);

    /**
     * Find expired accommodations
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.status = 'ACTIVE' " +
           "AND sa.endDate IS NOT NULL AND sa.endDate < CURRENT_DATE")
    List<StudentAccommodation> findExpired();

    // ========================================================================
    // COORDINATOR QUERIES
    // ========================================================================

    /**
     * Find accommodations by coordinator
     */
    List<StudentAccommodation> findByCoordinatorId(Long coordinatorId);

    /**
     * Find active accommodations by coordinator
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.coordinator.id = :coordinatorId " +
           "AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findActiveByCoordinatorId(@Param("coordinatorId") Long coordinatorId);

    // ========================================================================
    // STATUS QUERIES
    // ========================================================================

    /**
     * Find by status
     */
    List<StudentAccommodation> findByStatus(AccommodationStatus status);

    /**
     * Find by type and status
     */
    List<StudentAccommodation> findByTypeAndStatus(AccommodationType type, AccommodationStatus status);

    // ========================================================================
    // LUNCH STATUS QUERIES
    // ========================================================================

    /**
     * Find students by lunch status
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.lunchStatus = :lunchStatus AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findByLunchStatus(@Param("lunchStatus") StudentAccommodation.LunchStatus lunchStatus);

    /**
     * Count students by lunch status
     */
    @Query("SELECT COUNT(sa) FROM StudentAccommodation sa WHERE sa.lunchStatus = :lunchStatus AND sa.status = 'ACTIVE'")
    long countByLunchStatus(@Param("lunchStatus") StudentAccommodation.LunchStatus lunchStatus);

    // ========================================================================
    // TRANSPORTATION QUERIES
    // ========================================================================

    /**
     * Find students requiring special transportation
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.requiresSpecialTransportation = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findRequiringSpecialTransportation();

    /**
     * Find students by bus number
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.busNumber = :busNumber AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findByBusNumber(@Param("busNumber") String busNumber);

    // ========================================================================
    // ASSISTIVE TECHNOLOGY QUERIES
    // ========================================================================

    /**
     * Find students requiring assistive technology
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.requiresAssistiveTechnology = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findRequiringAssistiveTechnology();

    /**
     * Find students requiring accessibility accommodations
     */
    @Query("SELECT sa FROM StudentAccommodation sa WHERE sa.requiresAccessibilityAccommodations = true AND sa.status = 'ACTIVE'")
    List<StudentAccommodation> findRequiringAccessibilityAccommodations();

    // ========================================================================
    // STATISTICAL QUERIES
    // ========================================================================

    /**
     * Count by accommodation type
     */
    long countByType(AccommodationType type);

    /**
     * Count active accommodations
     */
    @Query("SELECT COUNT(sa) FROM StudentAccommodation sa WHERE sa.status = 'ACTIVE'")
    long countActive();

    /**
     * Count by status
     */
    long countByStatus(AccommodationStatus status);

    /**
     * Get statistics by accommodation type
     */
    @Query("SELECT sa.type, COUNT(sa) FROM StudentAccommodation sa WHERE sa.status = 'ACTIVE' GROUP BY sa.type")
    List<Object[]> getAccommodationStatistics();
}
