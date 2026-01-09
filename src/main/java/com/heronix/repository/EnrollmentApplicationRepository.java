package com.heronix.repository;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.EnrollmentApplication.ApplicationStatus;
import com.heronix.model.domain.EnrollmentApplication.EnrollmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Enrollment Applications
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Repository
public interface EnrollmentApplicationRepository extends JpaRepository<EnrollmentApplication, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find application by application number
     */
    Optional<EnrollmentApplication> findByApplicationNumber(String applicationNumber);

    /**
     * Find applications by status
     */
    List<EnrollmentApplication> findByStatus(ApplicationStatus status);

    /**
     * Find applications by enrollment type
     */
    List<EnrollmentApplication> findByEnrollmentType(EnrollmentType enrollmentType);

    /**
     * Find applications by intended school year
     */
    List<EnrollmentApplication> findByIntendedSchoolYear(String schoolYear);

    /**
     * Find applications by intended grade level
     */
    List<EnrollmentApplication> findByIntendedGradeLevel(String gradeLevel);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Find applications by student name (first or last)
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "LOWER(a.studentFirstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(a.studentLastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<EnrollmentApplication> findByStudentName(@Param("name") String name);

    /**
     * Find applications by parent name (either parent 1 or parent 2)
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "LOWER(a.parent1FirstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(a.parent1LastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(a.parent2FirstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(a.parent2LastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<EnrollmentApplication> findByParentName(@Param("name") String name);

    /**
     * Find applications by parent email (either parent)
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "LOWER(a.parent1Email) = LOWER(:email) OR " +
           "LOWER(a.parent2Email) = LOWER(:email)")
    List<EnrollmentApplication> findByParentEmail(@Param("email") String email);

    /**
     * Find applications by parent phone (any phone number)
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.parent1PhoneNumber = :phone OR " +
           "a.parent2PhoneNumber = :phone OR " +
           "a.parent1WorkPhone = :phone OR " +
           "a.parent2WorkPhone = :phone OR " +
           "a.studentPhoneNumber = :phone")
    List<EnrollmentApplication> findByPhoneNumber(@Param("phone") String phone);

    // ========================================================================
    // STATUS TRACKING QUERIES
    // ========================================================================

    /**
     * Find applications waiting for documents
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.status = 'DOCUMENTS_PENDING' OR a.status = 'VERIFICATION_IN_PROGRESS'")
    List<EnrollmentApplication> findApplicationsAwaitingDocuments();

    /**
     * Find applications with missing birth certificate
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.birthCertificateVerified = false OR a.birthCertificateVerified IS NULL")
    List<EnrollmentApplication> findApplicationsMissingBirthCertificate();

    /**
     * Find applications with missing residency proof
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.residencyVerified = false OR a.residencyVerified IS NULL")
    List<EnrollmentApplication> findApplicationsMissingResidency();

    /**
     * Find applications with missing immunizations
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.immunizationsVerified = false OR a.immunizationsVerified IS NULL")
    List<EnrollmentApplication> findApplicationsMissingImmunizations();

    /**
     * Find applications waiting for previous school records
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE a.status = 'RECORDS_REQUESTED'")
    List<EnrollmentApplication> findApplicationsAwaitingRecords();

    /**
     * Find applications ready for approval (all docs complete)
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE a.status = 'PENDING_APPROVAL'")
    List<EnrollmentApplication> findApplicationsReadyForApproval();

    /**
     * Find approved applications not yet enrolled
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.status = 'APPROVED' AND a.student IS NULL")
    List<EnrollmentApplication> findApprovedNotYetEnrolled();

    // ========================================================================
    // SPECIAL PROGRAMS QUERIES
    // ========================================================================

    /**
     * Find applications for students with IEP
     */
    List<EnrollmentApplication> findByHasIEPTrue();

    /**
     * Find applications for students with 504 plans
     */
    List<EnrollmentApplication> findByHas504PlanTrue();

    /**
     * Find applications for gifted students
     */
    List<EnrollmentApplication> findByIsGiftedTrue();

    /**
     * Find applications for English learners
     */
    List<EnrollmentApplication> findByIsEnglishLearnerTrue();

    /**
     * Find applications for homeless students
     */
    List<EnrollmentApplication> findByIsHomelessTrue();

    /**
     * Find applications for foster care students
     */
    List<EnrollmentApplication> findByIsFosterCareTrue();

    /**
     * Find applications for military families
     */
    List<EnrollmentApplication> findByIsMilitaryTrue();

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find applications submitted within a date range
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.applicationDate >= :startDate AND a.applicationDate <= :endDate")
    List<EnrollmentApplication> findByApplicationDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find applications approved within a date range
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE " +
           "a.approvedAt >= :startDate AND a.approvedAt < :endDate")
    List<EnrollmentApplication> findByApprovedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count applications by status
     */
    Long countByStatus(ApplicationStatus status);

    /**
     * Count applications by enrollment type
     */
    Long countByEnrollmentType(EnrollmentType enrollmentType);

    /**
     * Count applications by intended school year
     */
    Long countByIntendedSchoolYear(String schoolYear);

    /**
     * Count applications by intended grade level
     */
    Long countByIntendedGradeLevel(String gradeLevel);

    /**
     * Count applications with IEP
     */
    Long countByHasIEPTrue();

    /**
     * Count applications with 504
     */
    Long countByHas504PlanTrue();

    // ========================================================================
    // STAFF ASSIGNMENT QUERIES
    // ========================================================================

    /**
     * Find applications created by specific staff member
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE a.createdBy.id = :staffId")
    List<EnrollmentApplication> findByCreatedByStaffId(@Param("staffId") Long staffId);

    /**
     * Find applications processed by specific staff member
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE a.processedBy.id = :staffId")
    List<EnrollmentApplication> findByProcessedByStaffId(@Param("staffId") Long staffId);

    /**
     * Find applications approved by specific staff member
     */
    @Query("SELECT a FROM EnrollmentApplication a WHERE a.approvedBy.id = :staffId")
    List<EnrollmentApplication> findByApprovedByStaffId(@Param("staffId") Long staffId);
}
