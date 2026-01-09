package com.heronix.repository;

import com.heronix.model.domain.ProfessionalDevelopment;
import com.heronix.model.domain.ProfessionalDevelopment.PDStatus;
import com.heronix.model.domain.ProfessionalDevelopment.PDType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Professional Development entities
 * Manages PD courses, hours, CEUs, and continuing education tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ProfessionalDevelopmentRepository extends JpaRepository<ProfessionalDevelopment, Long> {

    /**
     * Find all PD for a teacher
     */
    List<ProfessionalDevelopment> findByTeacherIdOrderByStartDateDesc(Long teacherId);

    /**
     * Find PD by status
     */
    List<ProfessionalDevelopment> findByStatusOrderByStartDateDesc(PDStatus status);

    /**
     * Find active PD for a teacher
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.teacher.id = :teacherId " +
           "AND p.status IN ('ENROLLED', 'IN_PROGRESS', 'APPROVED') " +
           "ORDER BY p.startDate DESC")
    List<ProfessionalDevelopment> findActiveByTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find completed PD for a teacher
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.teacher.id = :teacherId " +
           "AND p.status = 'COMPLETED' ORDER BY p.completionDate DESC")
    List<ProfessionalDevelopment> findCompletedByTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find PD by teacher and date range
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.teacher.id = :teacherId " +
           "AND p.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.startDate DESC")
    List<ProfessionalDevelopment> findByTeacherAndDateRange(@Param("teacherId") Long teacherId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    /**
     * Find PD by type
     */
    List<ProfessionalDevelopment> findByPdTypeOrderByStartDateDesc(PDType type);

    /**
     * Find PD pending approval
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.status = 'PENDING_APPROVAL' " +
           "ORDER BY p.enrollmentDate ASC")
    List<ProfessionalDevelopment> findPendingApproval();

    /**
     * Find required PD
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.required = true " +
           "AND p.status NOT IN ('COMPLETED', 'CANCELED') " +
           "ORDER BY p.deadline ASC")
    List<ProfessionalDevelopment> findRequiredPD();

    /**
     * Find overdue required PD
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.required = true " +
           "AND p.deadline < :today " +
           "AND p.status NOT IN ('COMPLETED', 'CANCELED') " +
           "ORDER BY p.deadline ASC")
    List<ProfessionalDevelopment> findOverduePD(@Param("today") LocalDate today);

    /**
     * Find PD needing certificates
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.status = 'COMPLETED' " +
           "AND p.certificateEarned = false " +
           "ORDER BY p.completionDate ASC")
    List<ProfessionalDevelopment> findNeedingCertificates();

    /**
     * Find PD with pending reimbursement
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.reimbursementRequested = true " +
           "AND (p.reimbursementApproved IS NULL OR p.reimbursementApproved = true) " +
           "AND p.reimbursementPaid = false " +
           "ORDER BY p.completionDate ASC")
    List<ProfessionalDevelopment> findPendingReimbursement();

    /**
     * Find National Board related PD
     */
    @Query("SELECT p FROM ProfessionalDevelopment p WHERE p.nationalBoardRelated = true " +
           "ORDER BY p.startDate DESC")
    List<ProfessionalDevelopment> findNationalBoardPD();

    /**
     * Calculate total PD hours for teacher in date range
     */
    @Query("SELECT SUM(p.hoursEarned) FROM ProfessionalDevelopment p " +
           "WHERE p.teacher.id = :teacherId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.completionDate BETWEEN :startDate AND :endDate")
    Double sumHoursEarnedByTeacher(@Param("teacherId") Long teacherId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Calculate total CEUs for teacher in date range
     */
    @Query("SELECT SUM(p.ceusEarned) FROM ProfessionalDevelopment p " +
           "WHERE p.teacher.id = :teacherId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.completionDate BETWEEN :startDate AND :endDate")
    Double sumCEUsByTeacher(@Param("teacherId") Long teacherId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * Count PD by status
     */
    @Query("SELECT p.status, COUNT(p) FROM ProfessionalDevelopment p " +
           "GROUP BY p.status")
    List<Object[]> countByStatus();

    /**
     * Count PD by type
     */
    @Query("SELECT p.pdType, COUNT(p) FROM ProfessionalDevelopment p " +
           "WHERE p.status = 'COMPLETED' " +
           "GROUP BY p.pdType")
    List<Object[]> countCompletedByType();

    /**
     * Get PD statistics by teacher
     */
    @Query("SELECT COUNT(p), SUM(p.hoursEarned), SUM(p.ceusEarned) " +
           "FROM ProfessionalDevelopment p " +
           "WHERE p.teacher.id = :teacherId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.completionDate BETWEEN :startDate AND :endDate")
    Object[] getTeacherStatistics(@Param("teacherId") Long teacherId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Find teachers with most PD hours
     */
    @Query("SELECT p.teacher.id, p.teacher.name, SUM(p.hoursEarned) " +
           "FROM ProfessionalDevelopment p " +
           "WHERE p.status = 'COMPLETED' " +
           "AND p.completionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.teacher.id, p.teacher.name " +
           "ORDER BY SUM(p.hoursEarned) DESC")
    List<Object[]> findTopTeachersByPDHours(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Get PD completion rate by type
     */
    @Query("SELECT p.pdType, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(p) " +
           "FROM ProfessionalDevelopment p " +
           "WHERE p.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.pdType")
    List<Object[]> getCompletionRateByType(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
