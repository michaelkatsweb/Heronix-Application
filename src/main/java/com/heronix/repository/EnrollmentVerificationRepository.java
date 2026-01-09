package com.heronix.repository;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.VerificationStatus;
import com.heronix.model.domain.EnrollmentVerification.VerificationPurpose;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for EnrollmentVerification Entity
 * Handles database operations for enrollment verification requests
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface EnrollmentVerificationRepository extends JpaRepository<EnrollmentVerification, Long> {

    // Basic queries
    Optional<EnrollmentVerification> findByVerificationNumber(String verificationNumber);
    List<EnrollmentVerification> findByStatus(VerificationStatus status);
    List<EnrollmentVerification> findByStudent(Student student);
    List<EnrollmentVerification> findAllByStudent(Student student);
    List<EnrollmentVerification> findByPurpose(VerificationPurpose purpose);

    // Date range queries
    List<EnrollmentVerification> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ev FROM EnrollmentVerification ev WHERE ev.requestDate >= :cutoffDate ORDER BY ev.requestDate DESC")
    List<EnrollmentVerification> findRecentVerifications(@Param("cutoffDate") LocalDate cutoffDate);

    // Status queries
    @Query("SELECT ev FROM EnrollmentVerification ev WHERE ev.status = 'PENDING_VERIFICATION'")
    List<EnrollmentVerification> findPendingVerification();

    List<EnrollmentVerification> findByUrgentRequestTrue();
    List<EnrollmentVerification> findByFeePaidFalse();
    long countByUrgentRequestTrue();
    long countByFeePaidFalse();

    // Search queries
    @Query("SELECT ev FROM EnrollmentVerification ev WHERE " +
           "LOWER(ev.student.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ev.student.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<EnrollmentVerification> searchByStudentName(@Param("searchTerm") String searchTerm);

    // Statistics
    long countByStatus(VerificationStatus status);
    long countByPurpose(VerificationPurpose purpose);

    @Query("SELECT ev.purpose, COUNT(ev) FROM EnrollmentVerification ev GROUP BY ev.purpose")
    List<Object[]> getCountByPurpose();

    // Validation
    boolean existsByVerificationNumber(String verificationNumber);

    @Query("SELECT COUNT(ev) > 0 FROM EnrollmentVerification ev WHERE " +
           "ev.student.id = :studentId AND ev.status NOT IN ('DELIVERED', 'CANCELLED')")
    boolean hasPendingVerification(@Param("studentId") Long studentId);
}
