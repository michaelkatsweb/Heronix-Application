package com.heronix.repository;

import com.heronix.model.domain.DisciplinaryReferral;
import com.heronix.model.domain.DisciplinaryReferral.Priority;
import com.heronix.model.domain.DisciplinaryReferral.ReferralReason;
import com.heronix.model.domain.DisciplinaryReferral.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for DisciplinaryReferral entities
 * Manages formal disciplinary referrals from teachers to administrators
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface DisciplinaryReferralRepository extends JpaRepository<DisciplinaryReferral, Long> {

    /**
     * Find all referrals for a specific student
     */
    List<DisciplinaryReferral> findByStudentIdOrderByReferralDateDesc(Long studentId);

    /**
     * Find referrals by status
     */
    List<DisciplinaryReferral> findByStatusOrderByReferralDateDesc(ReferralStatus status);

    /**
     * Find pending referrals assigned to an administrator
     */
    List<DisciplinaryReferral> findByAssignedAdministratorIdAndStatus(
            Long administratorId, ReferralStatus status);

    /**
     * Find high-priority or urgent referrals
     */
    List<DisciplinaryReferral> findByPriorityInAndStatusNotOrderByReferralDateDesc(
            List<Priority> priorities, ReferralStatus status);

    /**
     * Find referrals by referring teacher
     */
    List<DisciplinaryReferral> findByReferringTeacherIdOrderByReferralDateDesc(Long teacherId);

    /**
     * Find referrals by date range
     */
    List<DisciplinaryReferral> findByReferralDateBetweenOrderByReferralDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find referrals by student and date range
     */
    List<DisciplinaryReferral> findByStudentIdAndReferralDateBetweenOrderByReferralDateDesc(
            Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Find referrals by reason
     */
    List<DisciplinaryReferral> findByReferralReasonOrderByReferralDateDesc(ReferralReason reason);

    /**
     * Find referrals for a specific campus
     */
    List<DisciplinaryReferral> findByCampusIdOrderByReferralDateDesc(Long campusId);

    /**
     * Find urgent referrals needing immediate attention
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.priority = 'URGENT' " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.referralDate ASC")
    List<DisciplinaryReferral> findUrgentReferrals();

    /**
     * Find pending referrals older than specified days
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.status = 'PENDING' " +
           "AND r.referralDate <= :cutoffDate " +
           "ORDER BY r.referralDate ASC")
    List<DisciplinaryReferral> findOverdueReferrals(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find referrals without parent contact
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.parentContacted = false " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.referralDate DESC")
    List<DisciplinaryReferral> findReferralsWithoutParentContact();

    /**
     * Count referrals by student in date range
     */
    @Query("SELECT COUNT(r) FROM DisciplinaryReferral r WHERE r.student.id = :studentId " +
           "AND r.referralDate BETWEEN :startDate AND :endDate")
    long countByStudentAndDateRange(@Param("studentId") Long studentId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Count referrals by reason in date range
     */
    @Query("SELECT COUNT(r) FROM DisciplinaryReferral r WHERE r.referralReason = :reason " +
           "AND r.referralDate BETWEEN :startDate AND :endDate")
    long countByReasonAndDateRange(@Param("reason") ReferralReason reason,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Get referral statistics by reason
     */
    @Query("SELECT r.referralReason, COUNT(r) FROM DisciplinaryReferral r " +
           "WHERE r.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.referralReason " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> getReferralStatisticsByReason(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Get referral statistics by status
     */
    @Query("SELECT r.status, COUNT(r) FROM DisciplinaryReferral r " +
           "WHERE r.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.status")
    List<Object[]> getReferralStatisticsByStatus(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find students with multiple referrals (repeat offenders)
     */
    @Query("SELECT r.student.id, COUNT(r) FROM DisciplinaryReferral r " +
           "WHERE r.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.student.id " +
           "HAVING COUNT(r) >= :threshold " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findRepeatOffenders(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("threshold") int threshold);

    /**
     * Get average resolution time by administrator
     */
    @Query("SELECT r.assignedAdministrator.id, AVG(CAST((r.reviewedDate - r.referralDate) AS long)) " +
           "FROM DisciplinaryReferral r " +
           "WHERE r.reviewedDate IS NOT NULL " +
           "AND r.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.assignedAdministrator.id")
    List<Object[]> getAverageResolutionTimeByAdministrator(@Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    /**
     * Get referrals pending hearing
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.status = 'PENDING_HEARING' " +
           "ORDER BY r.referralDate ASC")
    List<DisciplinaryReferral> findReferralsPendingHearing();

    /**
     * Find referrals by teacher for date range
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.referringTeacher.id = :teacherId " +
           "AND r.referralDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.referralDate DESC")
    List<DisciplinaryReferral> findByTeacherAndDateRange(@Param("teacherId") Long teacherId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * Find referrals linked to behavior incidents
     */
    @Query("SELECT r FROM DisciplinaryReferral r WHERE r.behaviorIncident IS NOT NULL " +
           "AND r.referralDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.referralDate DESC")
    List<DisciplinaryReferral> findReferralsWithIncidents(@Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * Get campus statistics
     */
    @Query("SELECT r.campus.id, r.status, COUNT(r) FROM DisciplinaryReferral r " +
           "WHERE r.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.campus.id, r.status")
    List<Object[]> getCampusStatistics(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}
