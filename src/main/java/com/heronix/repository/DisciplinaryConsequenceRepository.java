package com.heronix.repository;

import com.heronix.model.domain.DisciplinaryConsequence;
import com.heronix.model.domain.DisciplinaryConsequence.ConsequenceStatus;
import com.heronix.model.domain.DisciplinaryConsequence.ConsequenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for DisciplinaryConsequence entities
 * Manages disciplinary consequences assigned to students
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface DisciplinaryConsequenceRepository extends JpaRepository<DisciplinaryConsequence, Long> {

    /**
     * Find all consequences for a specific student
     */
    List<DisciplinaryConsequence> findByStudentIdOrderByStartDateDesc(Long studentId);

    /**
     * Find consequences by status
     */
    List<DisciplinaryConsequence> findByStatusOrderByStartDateDesc(ConsequenceStatus status);

    /**
     * Find active consequences for a student
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.student.id = :studentId " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findActiveConsequencesByStudent(@Param("studentId") Long studentId);

    /**
     * Find consequences by type
     */
    List<DisciplinaryConsequence> findByConsequenceTypeOrderByStartDateDesc(ConsequenceType type);

    /**
     * Find consequences assigned by an administrator
     */
    List<DisciplinaryConsequence> findByAssignedByIdOrderByStartDateDesc(Long administratorId);

    /**
     * Find consequences linked to a referral
     */
    List<DisciplinaryConsequence> findByReferralIdOrderByStartDateDesc(Long referralId);

    /**
     * Find consequences by date range
     */
    List<DisciplinaryConsequence> findByStartDateBetweenOrderByStartDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find consequences for a campus
     */
    List<DisciplinaryConsequence> findByCampusIdOrderByStartDateDesc(Long campusId);

    /**
     * Find consequences requiring tracking (community service, restitution, contracts)
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.consequenceType IN " +
           "('COMMUNITY_SERVICE', 'RESTITUTION', 'BEHAVIOR_CONTRACT') " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findConsequencesRequiringTracking();

    /**
     * Find overdue consequences (past end date but not completed)
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.endDate < :today " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.endDate ASC")
    List<DisciplinaryConsequence> findOverdueConsequences(@Param("today") LocalDate today);

    /**
     * Find consequences pending verification
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.status = 'COMPLETED' " +
           "AND c.verifiedCompleted = false " +
           "ORDER BY c.completionDate ASC")
    List<DisciplinaryConsequence> findPendingVerification();

    /**
     * Find consequences with pending appeals
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.status = 'APPEALED' " +
           "AND c.appealOutcome = 'PENDING' " +
           "ORDER BY c.appealDate ASC")
    List<DisciplinaryConsequence> findPendingAppeals();

    /**
     * Find consequences without parent notification
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.parentNotified = false " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findWithoutParentNotification();

    /**
     * Count consequences by student in date range
     */
    @Query("SELECT COUNT(c) FROM DisciplinaryConsequence c WHERE c.student.id = :studentId " +
           "AND c.startDate BETWEEN :startDate AND :endDate")
    long countByStudentAndDateRange(@Param("studentId") Long studentId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Count consequences by type in date range
     */
    @Query("SELECT COUNT(c) FROM DisciplinaryConsequence c WHERE c.consequenceType = :type " +
           "AND c.startDate BETWEEN :startDate AND :endDate")
    long countByTypeAndDateRange(@Param("type") ConsequenceType type,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /**
     * Get consequence statistics by type
     */
    @Query("SELECT c.consequenceType, COUNT(c) FROM DisciplinaryConsequence c " +
           "WHERE c.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.consequenceType " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getConsequenceStatisticsByType(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Get consequence statistics by status
     */
    @Query("SELECT c.status, COUNT(c) FROM DisciplinaryConsequence c " +
           "WHERE c.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.status")
    List<Object[]> getConsequenceStatisticsByStatus(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Get completion rate by consequence type
     */
    @Query("SELECT c.consequenceType, " +
           "COUNT(CASE WHEN c.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(c) " +
           "FROM DisciplinaryConsequence c " +
           "WHERE c.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.consequenceType")
    List<Object[]> getCompletionRateByType(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Find community service consequences with incomplete hours
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.consequenceType = 'COMMUNITY_SERVICE' " +
           "AND (c.hoursCompleted IS NULL OR c.hoursCompleted < c.duration) " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findIncompleteCommunityService();

    /**
     * Find restitution consequences with outstanding balance
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.consequenceType = 'RESTITUTION' " +
           "AND c.restitutionAmount > COALESCE(c.restitutionPaid, 0) " +
           "AND c.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findOutstandingRestitution();

    /**
     * Get total outstanding restitution amount
     */
    @Query("SELECT SUM(c.restitutionAmount - COALESCE(c.restitutionPaid, 0)) " +
           "FROM DisciplinaryConsequence c " +
           "WHERE c.consequenceType = 'RESTITUTION' " +
           "AND c.restitutionAmount > COALESCE(c.restitutionPaid, 0)")
    Double getTotalOutstandingRestitution();

    /**
     * Find consequences assigned by administrator in date range
     */
    @Query("SELECT c FROM DisciplinaryConsequence c WHERE c.assignedBy.id = :administratorId " +
           "AND c.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findByAdministratorAndDateRange(
            @Param("administratorId") Long administratorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get appeal statistics
     */
    @Query("SELECT c.appealOutcome, COUNT(c) FROM DisciplinaryConsequence c " +
           "WHERE c.appealFiled = true " +
           "AND c.appealDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.appealOutcome")
    List<Object[]> getAppealStatistics(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Find suspensions (ISS and OSS) for reporting
     */
    @Query("SELECT c FROM DisciplinaryConsequence c " +
           "WHERE c.consequenceType IN ('IN_SCHOOL_SUSPENSION', 'OUT_OF_SCHOOL_SUSPENSION') " +
           "AND c.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.startDate DESC")
    List<DisciplinaryConsequence> findSuspensionsByDateRange(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    /**
     * Get campus statistics
     */
    @Query("SELECT c.campus.id, c.consequenceType, COUNT(c) FROM DisciplinaryConsequence c " +
           "WHERE c.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.campus.id, c.consequenceType")
    List<Object[]> getCampusStatistics(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Find students with multiple consequences (repeat offenders)
     */
    @Query("SELECT c.student.id, COUNT(c) FROM DisciplinaryConsequence c " +
           "WHERE c.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.student.id " +
           "HAVING COUNT(c) >= :threshold " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> findRepeatOffenders(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("threshold") int threshold);
}
