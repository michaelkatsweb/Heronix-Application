package com.heronix.repository;

import com.heronix.model.domain.Suspension;
import com.heronix.model.domain.Suspension.SuspensionStatus;
import com.heronix.model.domain.Suspension.SuspensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Suspension entities
 * Manages detailed suspension tracking (ISS and OSS)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {

    /**
     * Find all suspensions for a specific student
     */
    List<Suspension> findByStudentIdOrderByStartDateDesc(Long studentId);

    /**
     * Find suspensions by status
     */
    List<Suspension> findByStatusOrderByStartDateDesc(SuspensionStatus status);

    /**
     * Find active suspensions
     */
    @Query("SELECT s FROM Suspension s WHERE s.status = 'ACTIVE' " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findActiveSuspensions();

    /**
     * Find active suspensions for a student
     */
    @Query("SELECT s FROM Suspension s WHERE s.student.id = :studentId " +
           "AND s.status = 'ACTIVE' " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findActiveSuspensionsByStudent(@Param("studentId") Long studentId);

    /**
     * Find suspensions by type
     */
    List<Suspension> findBySuspensionTypeOrderByStartDateDesc(SuspensionType type);

    /**
     * Find in-school suspensions (ISS)
     */
    @Query("SELECT s FROM Suspension s WHERE s.suspensionType = 'IN_SCHOOL' " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findActiveInSchoolSuspensions();

    /**
     * Find out-of-school suspensions (OSS)
     */
    @Query("SELECT s FROM Suspension s WHERE s.suspensionType IN ('OUT_OF_SCHOOL', 'EXTENDED_OSS') " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findActiveOutOfSchoolSuspensions();

    /**
     * Find suspensions issued by an administrator
     */
    List<Suspension> findByIssuedByIdOrderByStartDateDesc(Long administratorId);

    /**
     * Find suspensions by date range
     */
    List<Suspension> findByStartDateBetweenOrderByStartDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find suspensions for a campus
     */
    List<Suspension> findByCampusIdOrderByStartDateDesc(Long campusId);

    /**
     * Find suspensions linked to a consequence
     */
    Optional<Suspension> findByConsequenceId(Long consequenceId);

    /**
     * Find suspensions requiring re-entry meeting
     */
    @Query("SELECT s FROM Suspension s WHERE s.reentryMeetingRequired = true " +
           "AND s.reentryMeetingCompleted = false " +
           "AND s.status IN ('ACTIVE', 'COMPLETED') " +
           "ORDER BY s.endDate ASC")
    List<Suspension> findSuspensionsRequiringReentryMeeting();

    /**
     * Find suspensions without parent notification
     */
    @Query("SELECT s FROM Suspension s WHERE s.parentNotified = false " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findWithoutParentNotification();

    /**
     * Find suspensions with pending appeals
     */
    @Query("SELECT s FROM Suspension s WHERE s.appealFiled = true " +
           "AND s.appealOutcome = 'PENDING' " +
           "ORDER BY s.appealHearingDate ASC")
    List<Suspension> findPendingAppeals();

    /**
     * Find suspensions pending verification
     */
    @Query("SELECT s FROM Suspension s WHERE s.status = 'COMPLETED' " +
           "AND s.completionVerified = false " +
           "ORDER BY s.endDate ASC")
    List<Suspension> findPendingVerification();

    /**
     * Find ISS suspensions by supervisor
     */
    @Query("SELECT s FROM Suspension s WHERE s.issSupervisor.id = :supervisorId " +
           "AND s.suspensionType = 'IN_SCHOOL' " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findISSBySupervisor(@Param("supervisorId") Long supervisorId);

    /**
     * Find suspensions ending today or in the future
     */
    @Query("SELECT s FROM Suspension s WHERE s.endDate >= :today " +
           "AND s.status = 'ACTIVE' " +
           "ORDER BY s.endDate ASC")
    List<Suspension> findUpcomingEnding(@Param("today") LocalDate today);

    /**
     * Find suspensions starting today
     */
    @Query("SELECT s FROM Suspension s WHERE s.startDate = :today " +
           "AND s.status = 'PENDING' " +
           "ORDER BY s.student.lastName, s.student.firstName")
    List<Suspension> findStartingToday(@Param("today") LocalDate today);

    /**
     * Find emergency removals pending investigation
     */
    @Query("SELECT s FROM Suspension s WHERE s.suspensionType = 'EMERGENCY_REMOVAL' " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findEmergencyRemovals();

    /**
     * Find extended OSS (10+ days)
     */
    @Query("SELECT s FROM Suspension s WHERE s.suspensionType = 'EXTENDED_OSS' " +
           "AND s.status IN ('PENDING', 'ACTIVE') " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findExtendedOSS();

    /**
     * Count suspensions by student in date range
     */
    @Query("SELECT COUNT(s) FROM Suspension s WHERE s.student.id = :studentId " +
           "AND s.startDate BETWEEN :startDate AND :endDate")
    long countByStudentAndDateRange(@Param("studentId") Long studentId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Count ISS vs OSS suspensions
     */
    @Query("SELECT s.suspensionType, COUNT(s) FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.suspensionType")
    List<Object[]> countByType(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * Get suspension statistics by type
     */
    @Query("SELECT s.suspensionType, COUNT(s), SUM(s.daysCount) FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.suspensionType")
    List<Object[]> getSuspensionStatisticsByType(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Get suspension statistics by status
     */
    @Query("SELECT s.status, COUNT(s) FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.status")
    List<Object[]> getSuspensionStatisticsByStatus(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Calculate total suspension days by student
     */
    @Query("SELECT SUM(s.daysCount) FROM Suspension s WHERE s.student.id = :studentId " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "AND s.status NOT IN ('OVERTURNED', 'EXPIRED')")
    Integer getTotalSuspensionDaysByStudent(@Param("studentId") Long studentId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total OSS days by student
     */
    @Query("SELECT SUM(s.daysCount) FROM Suspension s WHERE s.student.id = :studentId " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "AND s.suspensionType IN ('OUT_OF_SCHOOL', 'EXTENDED_OSS') " +
           "AND s.status NOT IN ('OVERTURNED', 'EXPIRED')")
    Integer getTotalOSSDaysByStudent(@Param("studentId") Long studentId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Find students with multiple suspensions (repeat offenders)
     */
    @Query("SELECT s.student.id, COUNT(s), SUM(s.daysCount) FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.student.id " +
           "HAVING COUNT(s) >= :threshold " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> findRepeatOffenders(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("threshold") int threshold);

    /**
     * Get appeal statistics
     */
    @Query("SELECT s.appealOutcome, COUNT(s) FROM Suspension s " +
           "WHERE s.appealFiled = true " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.appealOutcome")
    List<Object[]> getAppealStatistics(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get conversion statistics (ISS ↔ OSS)
     */
    @Query("SELECT s.status, COUNT(s) FROM Suspension s " +
           "WHERE s.status IN ('CONVERTED_TO_ISS', 'CONVERTED_TO_OSS') " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.status")
    List<Object[]> getConversionStatistics(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Find suspensions with expulsion recommendations
     */
    @Query("SELECT s FROM Suspension s WHERE s.expulsionRecommended = true " +
           "AND s.status IN ('ACTIVE', 'COMPLETED') " +
           "ORDER BY s.expulsionHearingDate ASC")
    List<Suspension> findExpulsionRecommendations();

    /**
     * Get campus statistics
     */
    @Query("SELECT s.campus.id, s.suspensionType, COUNT(s), SUM(s.daysCount) FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.campus.id, s.suspensionType")
    List<Object[]> getCampusStatistics(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get re-entry meeting compliance rate
     */
    @Query("SELECT COUNT(CASE WHEN s.reentryMeetingCompleted = true THEN 1 END) * 100.0 / " +
           "COUNT(CASE WHEN s.reentryMeetingRequired = true THEN 1 END) " +
           "FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate")
    Double getReentryMeetingComplianceRate(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Get homework provision rate
     */
    @Query("SELECT COUNT(CASE WHEN s.homeworkProvided = true THEN 1 END) * 100.0 / COUNT(s) " +
           "FROM Suspension s " +
           "WHERE s.startDate BETWEEN :startDate AND :endDate")
    Double getHomeworkProvisionRate(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Find suspensions by administrator for date range
     */
    @Query("SELECT s FROM Suspension s WHERE s.issuedBy.id = :administratorId " +
           "AND s.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.startDate DESC")
    List<Suspension> findByAdministratorAndDateRange(@Param("administratorId") Long administratorId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
