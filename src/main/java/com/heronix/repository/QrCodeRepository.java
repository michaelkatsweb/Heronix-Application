package com.heronix.repository;

import com.heronix.model.domain.QrCode;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * QR Code Repository
 *
 * Data access layer for QR code management.
 * Provides queries for finding, activating, deactivating, and rotating QR codes.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find QR code by unique code data
     */
    Optional<QrCode> findByQrCodeData(String qrCodeData);

    /**
     * Find all QR codes for a student (active and inactive)
     */
    List<QrCode> findByStudent(Student student);

    /**
     * Find active QR codes for a student
     */
    @Query("SELECT q FROM QrCode q WHERE q.student = :student AND q.active = true")
    List<QrCode> findActiveByStudent(@Param("student") Student student);

    /**
     * Find QR code by student and active status
     */
    Optional<QrCode> findByStudentAndActive(Student student, Boolean active);

    // ========================================================================
    // EXPIRY AND ROTATION QUERIES
    // ========================================================================

    /**
     * Find expired QR codes that are still marked active
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.expiryDate < CURRENT_DATE")
    List<QrCode> findExpiredCodes();

    /**
     * Find QR codes expiring within specified days
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.expiryDate >= CURRENT_DATE AND " +
           "q.expiryDate <= FUNCTION('DATEADD', DAY, :days, CURRENT_DATE)")
    List<QrCode> findCodesExpiringSoon(@Param("days") int days);

    /**
     * Find QR codes that need rotation (rotation date has passed)
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.rotationDate IS NOT NULL AND " +
           "q.rotationDate < CURRENT_DATE")
    List<QrCode> findCodesNeedingRotation();

    /**
     * Find QR codes scheduled for rotation within specified days
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.rotationDate IS NOT NULL AND " +
           "q.rotationDate >= CURRENT_DATE AND " +
           "q.rotationDate <= FUNCTION('DATEADD', DAY, :days, CURRENT_DATE)")
    List<QrCode> findCodesScheduledForRotation(@Param("days") int days);

    // ========================================================================
    // STATUS QUERIES
    // ========================================================================

    /**
     * Find all active QR codes
     */
    List<QrCode> findByActiveTrue();

    /**
     * Find all inactive QR codes
     */
    List<QrCode> findByActiveFalse();

    /**
     * Count active QR codes
     */
    long countByActiveTrue();

    /**
     * Count QR codes for a student
     */
    long countByStudent(Student student);

    /**
     * Count active QR codes for a student
     */
    long countByStudentAndActiveTrue(Student student);

    // ========================================================================
    // USAGE STATISTICS QUERIES
    // ========================================================================

    /**
     * Find QR codes with no scans (never used)
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.scanCount = 0")
    List<QrCode> findUnusedQrCodes();

    /**
     * Find QR codes not scanned within specified days
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "(q.lastScannedAt IS NULL OR " +
           "q.lastScannedAt < FUNCTION('DATEADD', DAY, -:days, CURRENT_TIMESTAMP))")
    List<QrCode> findQrCodesNotScannedRecently(@Param("days") int days);

    /**
     * Find most frequently scanned QR codes
     */
    @Query("SELECT q FROM QrCode q WHERE q.active = true ORDER BY q.scanCount DESC")
    List<QrCode> findMostScannedQrCodes();

    // ========================================================================
    // STUDENT QUERIES
    // ========================================================================

    /**
     * Find students without an active QR code
     */
    @Query("SELECT s FROM Student s WHERE s.active = true AND " +
           "s.id NOT IN (SELECT q.student.id FROM QrCode q WHERE q.active = true)")
    List<Student> findStudentsWithoutActiveQrCode();

    /**
     * Find students with multiple active QR codes (data integrity check)
     */
    @Query("SELECT q.student FROM QrCode q WHERE q.active = true " +
           "GROUP BY q.student HAVING COUNT(q.id) > 1")
    List<Student> findStudentsWithMultipleActiveQrCodes();

    // ========================================================================
    // DEACTIVATION QUERIES
    // ========================================================================

    /**
     * Find QR codes deactivated within date range
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = false AND " +
           "q.deactivatedAt >= :startDate AND " +
           "q.deactivatedAt <= :endDate")
    List<QrCode> findDeactivatedBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                         @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find QR codes deactivated for specific reason
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = false AND " +
           "LOWER(q.deactivationReason) LIKE LOWER(CONCAT('%', :reason, '%'))")
    List<QrCode> findDeactivatedByReason(@Param("reason") String reason);

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Find QR codes for students in specific grade level
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.active = true AND " +
           "q.student.gradeLevel = :gradeLevel")
    List<QrCode> findByGradeLevel(@Param("gradeLevel") String gradeLevel);

    /**
     * Find QR codes generated within date range
     */
    @Query("SELECT q FROM QrCode q WHERE " +
           "q.generatedAt >= :startDate AND " +
           "q.generatedAt <= :endDate")
    List<QrCode> findGeneratedBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                       @Param("endDate") java.time.LocalDateTime endDate);
}
