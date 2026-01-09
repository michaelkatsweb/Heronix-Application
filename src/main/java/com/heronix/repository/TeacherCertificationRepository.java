package com.heronix.repository;

import com.heronix.model.domain.TeacherCertification;
import com.heronix.model.domain.TeacherCertification.CertificateType;
import com.heronix.model.domain.TeacherCertification.CertificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Teacher Certification entities
 * Manages teacher certification records, renewals, and expirations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface TeacherCertificationRepository extends JpaRepository<TeacherCertification, Long> {

    /**
     * Find all certifications for a teacher
     */
    List<TeacherCertification> findByTeacherIdOrderByExpirationDateDesc(Long teacherId);

    /**
     * Find active certifications for a teacher
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.teacher.id = :teacherId " +
           "AND c.status = 'ACTIVE' ORDER BY c.expirationDate DESC")
    List<TeacherCertification> findActiveByTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find certification by certificate number
     */
    Optional<TeacherCertification> findByCertificateNumber(String certificateNumber);

    /**
     * Find certifications by type
     */
    List<TeacherCertification> findByCertificateTypeOrderByExpirationDateAsc(CertificateType type);

    /**
     * Find certifications by status
     */
    List<TeacherCertification> findByStatusOrderByExpirationDateAsc(CertificationStatus status);

    /**
     * Find certifications expiring soon (within N days)
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.expirationDate BETWEEN :today AND :futureDate " +
           "AND c.status = 'ACTIVE' ORDER BY c.expirationDate ASC")
    List<TeacherCertification> findExpiringSoon(@Param("today") LocalDate today,
                                                  @Param("futureDate") LocalDate futureDate);

    /**
     * Find expired certifications
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.expirationDate < :today " +
           "AND c.status IN ('ACTIVE', 'EXPIRING_SOON') ORDER BY c.expirationDate ASC")
    List<TeacherCertification> findExpired(@Param("today") LocalDate today);

    /**
     * Find certifications needing renewal reminder
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.renewalRequired = true " +
           "AND c.renewalReminderSent = false " +
           "AND c.expirationDate BETWEEN :today AND :reminderDate " +
           "AND c.status = 'ACTIVE' ORDER BY c.expirationDate ASC")
    List<TeacherCertification> findNeedingRenewalReminder(@Param("today") LocalDate today,
                                                            @Param("reminderDate") LocalDate reminderDate);

    /**
     * Find certifications with renewal in progress
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.renewalInProgress = true " +
           "ORDER BY c.renewalApplicationDate DESC")
    List<TeacherCertification> findRenewalsInProgress();

    /**
     * Find highly qualified teachers
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.highlyQualified = true " +
           "AND c.status = 'ACTIVE'")
    List<TeacherCertification> findHighlyQualifiedTeachers();

    /**
     * Find teachers with out-of-field teaching
     */
    @Query("SELECT c FROM TeacherCertification c WHERE c.outOfFieldTeaching = true " +
           "AND c.status = 'ACTIVE'")
    List<TeacherCertification> findOutOfFieldTeaching();

    /**
     * Find certifications by subject
     */
    @Query("SELECT c FROM TeacherCertification c JOIN c.subjects s " +
           "WHERE s = :subject AND c.status = 'ACTIVE'")
    List<TeacherCertification> findBySubject(@Param("subject") String subject);

    /**
     * Find certifications by endorsement
     */
    @Query("SELECT c FROM TeacherCertification c JOIN c.endorsements e " +
           "WHERE e = :endorsement AND c.status = 'ACTIVE'")
    List<TeacherCertification> findByEndorsement(@Param("endorsement") String endorsement);

    /**
     * Find National Board Certified teachers
     */
    List<TeacherCertification> findByCertificateTypeAndStatus(
            CertificateType type, CertificationStatus status);

    /**
     * Count certifications by status
     */
    @Query("SELECT c.status, COUNT(c) FROM TeacherCertification c " +
           "GROUP BY c.status")
    List<Object[]> countByStatus();

    /**
     * Count certifications by type
     */
    @Query("SELECT c.certificateType, COUNT(c) FROM TeacherCertification c " +
           "WHERE c.status = 'ACTIVE' GROUP BY c.certificateType")
    List<Object[]> countActiveByType();

    /**
     * Get certification statistics for a date range
     */
    @Query("SELECT c.certificateType, COUNT(c), " +
           "COUNT(CASE WHEN c.highlyQualified = true THEN 1 END) " +
           "FROM TeacherCertification c " +
           "WHERE c.expirationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.certificateType")
    List<Object[]> getStatisticsByDateRange(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
