package com.heronix.repository;

import com.heronix.model.domain.IEPGoal;
import com.heronix.model.domain.IEPGoal.GoalDomain;
import com.heronix.model.domain.IEPGoal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for IEP Goal entities
 * Provides data access for goals, objectives, and progress tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface IEPGoalRepository extends JpaRepository<IEPGoal, Long> {

    // Basic Queries
    List<IEPGoal> findByIepId(Long iepId);

    List<IEPGoal> findByIepIdOrderByGoalNumberAsc(Long iepId);

    List<IEPGoal> findByStatusOrderByGoalDomainAsc(GoalStatus status);

    List<IEPGoal> findByGoalDomain(GoalDomain domain);

    // Student Goals
    @Query("SELECT g FROM IEPGoal g WHERE g.iep.student.id = :studentId " +
           "ORDER BY g.goalNumber ASC")
    List<IEPGoal> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT g FROM IEPGoal g WHERE g.iep.student.id = :studentId " +
           "AND g.status = :status ORDER BY g.goalDomain ASC")
    List<IEPGoal> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                            @Param("status") GoalStatus status);

    // Progress Monitoring
    @Query("SELECT g FROM IEPGoal g WHERE g.status = 'IN_PROGRESS' " +
           "AND (g.lastProgressCheckDate IS NULL OR g.lastProgressCheckDate < :cutoffDate) " +
           "ORDER BY g.lastProgressCheckDate ASC NULLS FIRST")
    List<IEPGoal> findOverdueForProgressCheck(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT g FROM IEPGoal g WHERE g.status IN ('INSUFFICIENT_PROGRESS', 'NOT_STARTED') " +
           "ORDER BY g.iep.student.lastName ASC")
    List<IEPGoal> findGoalsNeedingAttention();

    @Query("SELECT g FROM IEPGoal g WHERE g.iep.id = :iepId " +
           "AND g.status = 'MASTERED' ORDER BY g.masteryDate DESC")
    List<IEPGoal> findMasteredGoalsByIep(@Param("iepId") Long iepId);

    // Domain Analysis
    @Query("SELECT g FROM IEPGoal g WHERE g.iep.id = :iepId " +
           "AND g.goalDomain = :domain ORDER BY g.goalNumber ASC")
    List<IEPGoal> findByIepIdAndDomain(@Param("iepId") Long iepId,
                                        @Param("domain") GoalDomain domain);

    @Query("SELECT g.goalDomain, COUNT(g) FROM IEPGoal g " +
           "WHERE g.iep.id = :iepId GROUP BY g.goalDomain")
    List<Object[]> countGoalsByDomain(@Param("iepId") Long iepId);

    // Status Tracking
    @Query("SELECT g.status, COUNT(g) FROM IEPGoal g " +
           "WHERE g.iep.id = :iepId GROUP BY g.status")
    List<Object[]> countGoalsByStatus(@Param("iepId") Long iepId);

    @Query("SELECT g FROM IEPGoal g WHERE g.iep.id = :iepId " +
           "AND g.status IN ('ADEQUATE_PROGRESS', 'MASTERED') " +
           "ORDER BY g.goalNumber ASC")
    List<IEPGoal> findSuccessfulGoalsByIep(@Param("iepId") Long iepId);

    // Responsible Staff
    @Query("SELECT g FROM IEPGoal g WHERE g.responsibleStaff.id = :staffId " +
           "AND g.status IN ('IN_PROGRESS', 'NOT_STARTED') " +
           "ORDER BY g.iep.student.lastName ASC")
    List<IEPGoal> findActiveGoalsByStaff(@Param("staffId") Long staffId);

    // Review Queries
    @Query("SELECT g FROM IEPGoal g WHERE g.reviewDate IS NOT NULL " +
           "AND g.reviewDate <= :date AND g.status != 'MASTERED' " +
           "ORDER BY g.reviewDate ASC")
    List<IEPGoal> findGoalsDueForReview(@Param("date") LocalDate date);

    // Target Date Tracking
    @Query("SELECT g FROM IEPGoal g WHERE g.targetDate BETWEEN :startDate AND :endDate " +
           "AND g.status NOT IN ('MASTERED', 'DISCONTINUED') " +
           "ORDER BY g.targetDate ASC")
    List<IEPGoal> findGoalsWithTargetDateInRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Statistics
    @Query("SELECT AVG(g.progressPercentage) FROM IEPGoal g " +
           "WHERE g.iep.id = :iepId AND g.progressPercentage IS NOT NULL")
    Double getAverageProgressByIep(@Param("iepId") Long iepId);

    @Query("SELECT COUNT(g) FROM IEPGoal g WHERE g.iep.id = :iepId " +
           "AND g.status = 'MASTERED'")
    Long countMasteredGoalsByIep(@Param("iepId") Long iepId);

    @Query("SELECT COUNT(g) FROM IEPGoal g WHERE g.iep.id = :iepId")
    Long countTotalGoalsByIep(@Param("iepId") Long iepId);
}
