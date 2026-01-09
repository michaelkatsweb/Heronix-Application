package com.heronix.repository;

import com.heronix.model.domain.RTIIntervention;
import com.heronix.model.domain.RTIIntervention.AcademicArea;
import com.heronix.model.domain.RTIIntervention.Effectiveness;
import com.heronix.model.domain.RTIIntervention.InterventionStatus;
import com.heronix.model.domain.RTIIntervention.RTITier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RTI Intervention entities
 * Provides data access for RTI/MTSS interventions and progress monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface RTIInterventionRepository extends JpaRepository<RTIIntervention, Long> {

    // Basic Queries
    List<RTIIntervention> findByStudentId(Long studentId);

    List<RTIIntervention> findByStudentIdOrderByStartDateDesc(Long studentId);

    List<RTIIntervention> findByTier(RTITier tier);

    List<RTIIntervention> findByStatus(InterventionStatus status);

    List<RTIIntervention> findByAcademicArea(AcademicArea area);

    // Active Interventions
    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "ORDER BY i.tier ASC, i.student.lastName ASC")
    List<RTIIntervention> findAllActive();

    @Query("SELECT i FROM RTIIntervention i WHERE i.student.id = :studentId " +
           "AND i.status = 'ACTIVE' ORDER BY i.tier ASC")
    List<RTIIntervention> findActiveByStudent(@Param("studentId") Long studentId);

    // Tier Queries
    @Query("SELECT i FROM RTIIntervention i WHERE i.tier = :tier " +
           "AND i.status = 'ACTIVE' ORDER BY i.student.lastName ASC")
    List<RTIIntervention> findActiveByTier(@Param("tier") RTITier tier);

    @Query("SELECT i FROM RTIIntervention i WHERE i.student.id = :studentId " +
           "AND i.tier = :tier ORDER BY i.startDate DESC")
    List<RTIIntervention> findByStudentAndTier(@Param("studentId") Long studentId,
                                                 @Param("tier") RTITier tier);

    // Progress Monitoring
    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "AND (i.lastProgressCheckDate IS NULL OR i.lastProgressCheckDate < :cutoffDate) " +
           "ORDER BY i.lastProgressCheckDate ASC NULLS FIRST")
    List<RTIIntervention> findOverdueForProgressCheck(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "AND i.currentScore IS NOT NULL AND i.targetScore IS NOT NULL " +
           "AND i.currentScore >= i.targetScore " +
           "ORDER BY i.student.lastName ASC")
    List<RTIIntervention> findMetGoal();

    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "AND i.currentScore IS NOT NULL AND i.baselineScore IS NOT NULL " +
           "AND i.currentScore > i.baselineScore " +
           "ORDER BY i.student.lastName ASC")
    List<RTIIntervention> findMakingProgress();

    // Effectiveness
    @Query("SELECT i FROM RTIIntervention i WHERE i.effectiveness = :effectiveness " +
           "AND i.status = 'ACTIVE' ORDER BY i.student.lastName ASC")
    List<RTIIntervention> findByEffectiveness(@Param("effectiveness") Effectiveness effectiveness);

    @Query("SELECT i FROM RTIIntervention i WHERE i.effectiveness IN ('INEFFECTIVE', 'MINIMALLY_EFFECTIVE') " +
           "AND i.status = 'ACTIVE' ORDER BY i.tier ASC, i.startDate ASC")
    List<RTIIntervention> findIneffective();

    // Referral Tracking
    @Query("SELECT i FROM RTIIntervention i WHERE i.referredToSpecialEducation = true " +
           "ORDER BY i.referralDate DESC")
    List<RTIIntervention> findReferredToSpEd();

    @Query("SELECT i FROM RTIIntervention i WHERE i.tier = 'TIER_3' " +
           "AND i.status = 'ACTIVE' " +
           "AND (i.effectiveness = 'INEFFECTIVE' OR i.effectiveness = 'MINIMALLY_EFFECTIVE') " +
           "AND i.referredToSpecialEducation = false " +
           "ORDER BY i.startDate ASC")
    List<RTIIntervention> findCandidatesForSpEdReferral();

    // Parent Involvement
    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "AND (i.parentNotified = false OR i.parentNotified IS NULL) " +
           "ORDER BY i.startDate ASC")
    List<RTIIntervention> findNeedingParentNotification();

    @Query("SELECT i FROM RTIIntervention i WHERE i.parentConsentRequired = true " +
           "AND (i.parentConsentReceived = false OR i.parentConsentReceived IS NULL) " +
           "AND i.status != 'DISCONTINUED' " +
           "ORDER BY i.startDate ASC")
    List<RTIIntervention> findNeedingParentConsent();

    // Team Meetings
    @Query("SELECT i FROM RTIIntervention i WHERE i.nextMeetingDate IS NOT NULL " +
           "AND i.nextMeetingDate <= :date AND i.status = 'ACTIVE' " +
           "ORDER BY i.nextMeetingDate ASC")
    List<RTIIntervention> findWithUpcomingMeetings(@Param("date") LocalDate date);

    // Interventionist
    @Query("SELECT i FROM RTIIntervention i WHERE i.interventionist.id = :staffId " +
           "AND i.status = 'ACTIVE' ORDER BY i.student.lastName ASC")
    List<RTIIntervention> findActiveByInterventionist(@Param("staffId") Long staffId);

    // Duration Analysis
    @Query("SELECT i FROM RTIIntervention i WHERE i.status = 'ACTIVE' " +
           "AND i.startDate < :cutoffDate ORDER BY i.startDate ASC")
    List<RTIIntervention> findLongRunning(@Param("cutoffDate") LocalDate cutoffDate);

    // Academic Area
    @Query("SELECT i FROM RTIIntervention i WHERE i.student.id = :studentId " +
           "AND i.academicArea = :area ORDER BY i.startDate DESC")
    List<RTIIntervention> findByStudentAndArea(@Param("studentId") Long studentId,
                                                 @Param("area") AcademicArea area);

    @Query("SELECT i FROM RTIIntervention i WHERE i.academicArea = :area " +
           "AND i.status = 'ACTIVE' ORDER BY i.tier ASC")
    List<RTIIntervention> findActiveByArea(@Param("area") AcademicArea area);

    // Statistics
    @Query("SELECT i.tier, COUNT(i) FROM RTIIntervention i " +
           "WHERE i.status = 'ACTIVE' GROUP BY i.tier ORDER BY i.tier ASC")
    List<Object[]> countActiveByTier();

    @Query("SELECT i.academicArea, COUNT(i) FROM RTIIntervention i " +
           "WHERE i.status = 'ACTIVE' GROUP BY i.academicArea")
    List<Object[]> countActiveByArea();

    @Query("SELECT i.effectiveness, COUNT(i) FROM RTIIntervention i " +
           "WHERE i.status = 'ACTIVE' AND i.effectiveness IS NOT NULL " +
           "GROUP BY i.effectiveness")
    List<Object[]> countByEffectiveness();

    @Query("SELECT COUNT(i) FROM RTIIntervention i WHERE i.student.id = :studentId " +
           "AND i.status = 'ACTIVE'")
    Long countActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(i) FROM RTIIntervention i WHERE i.student.id = :studentId " +
           "AND i.status = 'COMPLETED' AND i.effectiveness IN ('EFFECTIVE', 'HIGHLY_EFFECTIVE')")
    Long countSuccessfulByStudent(@Param("studentId") Long studentId);
}
