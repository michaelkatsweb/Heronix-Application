package com.heronix.repository;

import com.heronix.model.domain.PreRegistration;
import com.heronix.model.domain.PreRegistration.RegistrationStatus;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PreRegistration Entity
 * Handles database operations for pre-registration for upcoming school year
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface PreRegistrationRepository extends JpaRepository<PreRegistration, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find pre-registration by registration number
     */
    Optional<PreRegistration> findByRegistrationNumber(String registrationNumber);

    /**
     * Find all pre-registrations by status
     */
    List<PreRegistration> findByStatus(RegistrationStatus status);

    /**
     * Find all pre-registrations for a specific student
     */
    List<PreRegistration> findByStudent(Student student);

    /**
     * Find pre-registration for a student and target school year
     */
    Optional<PreRegistration> findByStudentAndTargetSchoolYear(Student student, String targetSchoolYear);

    /**
     * Find all pre-registrations for a target school year
     */
    List<PreRegistration> findByTargetSchoolYear(String targetSchoolYear);

    /**
     * Find by status and target school year
     */
    List<PreRegistration> findByStatusAndTargetSchoolYear(RegistrationStatus status, String targetSchoolYear);

    /**
     * Find by current grade
     */
    List<PreRegistration> findByCurrentGrade(String currentGrade);

    /**
     * Find by next grade
     */
    List<PreRegistration> findByNextGrade(String nextGrade);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find all pre-registrations submitted within a date range
     */
    List<PreRegistration> findByRegistrationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find pre-registrations submitted after a specific date
     */
    List<PreRegistration> findBySubmittedAtAfter(LocalDateTime dateTime);

    /**
     * Find pre-registrations approved after a specific date
     */
    List<PreRegistration> findByApprovedAtAfter(LocalDateTime dateTime);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search pre-registrations by student name (first or last)
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE " +
           "LOWER(pr.student.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pr.student.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PreRegistration> searchByStudentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by parent email
     */
    List<PreRegistration> findByParentEmailContainingIgnoreCase(String email);

    /**
     * Search by parent phone
     */
    List<PreRegistration> findByParentPhoneContaining(String phone);

    // ========================================================================
    // SPECIAL SERVICES QUERIES
    // ========================================================================

    /**
     * Find students continuing IEP
     */
    List<PreRegistration> findByContinueIEPTrueAndStatus(RegistrationStatus status);

    /**
     * Find students continuing 504 Plan
     */
    List<PreRegistration> findByContinue504PlanTrueAndStatus(RegistrationStatus status);

    /**
     * Find students continuing ESL
     */
    List<PreRegistration> findByContinueESLTrueAndStatus(RegistrationStatus status);

    /**
     * Find students continuing Gifted Program
     */
    List<PreRegistration> findByContinueGiftedProgramTrueAndStatus(RegistrationStatus status);

    // ========================================================================
    // PROGRAM INTEREST QUERIES
    // ========================================================================

    /**
     * Find students interested in AP/Honors courses
     */
    List<PreRegistration> findByInterestedInAPHonorsTrueAndTargetSchoolYear(String targetSchoolYear);

    /**
     * Find students interested in dual enrollment
     */
    List<PreRegistration> findByInterestedInDualEnrollmentTrueAndTargetSchoolYear(String targetSchoolYear);

    /**
     * Find students interested in CTE programs
     */
    List<PreRegistration> findByInterestedInCTETrueAndTargetSchoolYear(String targetSchoolYear);

    /**
     * Find students interested in athletics
     */
    List<PreRegistration> findByInterestedInAthleticsTrueAndTargetSchoolYear(String targetSchoolYear);

    // ========================================================================
    // WORKFLOW QUERIES
    // ========================================================================

    /**
     * Find all pre-registrations awaiting review
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE pr.status = 'SUBMITTED' ORDER BY pr.submittedAt ASC")
    List<PreRegistration> findAwaitingReview();

    /**
     * Find all approved pre-registrations with seats reserved
     */
    List<PreRegistration> findByStatusAndSeatReservedTrue(RegistrationStatus status);

    /**
     * Find pre-registrations reviewed by specific staff
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE pr.reviewedBy.id = :staffId")
    List<PreRegistration> findReviewedByStaff(@Param("staffId") Long staffId);

    /**
     * Find pre-registrations approved by specific staff
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE pr.approvedBy.id = :staffId")
    List<PreRegistration> findApprovedByStaff(@Param("staffId") Long staffId);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count pre-registrations by status
     */
    long countByStatus(RegistrationStatus status);

    /**
     * Count pre-registrations for target school year
     */
    long countByTargetSchoolYear(String targetSchoolYear);

    /**
     * Count by status and target school year
     */
    long countByStatusAndTargetSchoolYear(RegistrationStatus status, String targetSchoolYear);

    /**
     * Count by next grade and target school year
     */
    long countByNextGradeAndTargetSchoolYear(String nextGrade, String targetSchoolYear);

    /**
     * Get count by grade level for capacity planning
     */
    @Query("SELECT pr.nextGrade, COUNT(pr) FROM PreRegistration pr " +
           "WHERE pr.targetSchoolYear = :schoolYear AND pr.status IN ('APPROVED', 'CONFIRMED') " +
           "GROUP BY pr.nextGrade")
    List<Object[]> getEnrollmentCountsByGrade(@Param("schoolYear") String schoolYear);

    /**
     * Find returning students in good standing
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE " +
           "pr.isReturningStudent = true AND " +
           "pr.isInGoodStanding = true AND " +
           "pr.targetSchoolYear = :schoolYear")
    List<PreRegistration> findReturningStudentsInGoodStanding(@Param("schoolYear") String schoolYear);

    /**
     * Find incomplete pre-registrations (missing acknowledgments)
     */
    @Query("SELECT pr FROM PreRegistration pr WHERE " +
           "pr.status = 'DRAFT' AND " +
           "(pr.acknowledgedAccuracy = false OR " +
           "pr.acknowledgedReview = false OR " +
           "pr.acknowledgedDeadline = false OR " +
           "pr.acknowledgedFees = false OR " +
           "pr.acknowledgedUpdates = false)")
    List<PreRegistration> findIncompleteRegistrations();

    /**
     * Check if student already has pre-registration for school year
     */
    boolean existsByStudentAndTargetSchoolYear(Student student, String targetSchoolYear);

    /**
     * Find pre-registrations with address changes
     */
    List<PreRegistration> findByAddressChangedTrueAndStatus(RegistrationStatus status);
}
