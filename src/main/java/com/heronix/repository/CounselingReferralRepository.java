package com.heronix.repository;

import com.heronix.model.domain.CounselingReferral;
import com.heronix.model.domain.CounselingReferral.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Counseling Referral entities
 * Provides data access for counseling referral tracking and management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface CounselingReferralRepository extends JpaRepository<CounselingReferral, Long> {

    // Basic Queries
    List<CounselingReferral> findByStudentId(Long studentId);

    List<CounselingReferral> findByAssignedCounselorId(Long counselorId);

    Optional<CounselingReferral> findByReferralNumber(String referralNumber);

    List<CounselingReferral> findByReferralSource(ReferralSource referralSource);

    List<CounselingReferral> findByReferralType(ReferralType referralType);

    List<CounselingReferral> findByReferralStatus(ReferralStatus referralStatus);

    List<CounselingReferral> findByUrgencyLevel(UrgencyLevel urgencyLevel);

    // Date Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralDate = :date " +
           "ORDER BY cr.urgencyLevel DESC, cr.referralDate ASC")
    List<CounselingReferral> findByDate(@Param("date") LocalDate date);

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    // Status Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus = 'PENDING' " +
           "ORDER BY cr.urgencyLevel DESC, cr.referralDate ASC")
    List<CounselingReferral> findPendingReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus IN " +
           "('ASSIGNED', 'INTAKE_SCHEDULED', 'IN_PROGRESS') " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findActiveReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus IN " +
           "('COMPLETED', 'DECLINED', 'CANCELLED') " +
           "ORDER BY cr.closureDate DESC")
    List<CounselingReferral> findClosedReferrals();

    // Urgency Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.urgencyLevel IN ('URGENT', 'EMERGENCY') " +
           "AND cr.referralStatus IN ('PENDING', 'ASSIGNED') " +
           "ORDER BY cr.urgencyLevel DESC, cr.referralDate ASC")
    List<CounselingReferral> findUrgentReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.urgencyLevel = 'EMERGENCY' " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findEmergencyReferrals();

    // Risk Assessment Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.riskAssessmentCompleted = false " +
           "AND cr.referralStatus != 'CANCELLED' " +
           "ORDER BY cr.urgencyLevel DESC, cr.referralDate ASC")
    List<CounselingReferral> findNeedingRiskAssessment();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findHighRiskReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.suicideRiskIndicated = true " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findSuicideRiskReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.harmToOthersIndicated = true " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findHarmToOthersReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.immediateSafetyConcerns = true " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findImmediateSafetyConcerns();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.safetyPlanCreated = true " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findWithSafetyPlan();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.crisisInterventionNeeded = true " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findCrisisInterventionNeeded();

    // Concern Area Queries
    List<CounselingReferral> findByPrimaryConcern(ConcernArea primaryConcern);

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.primaryConcern IN " +
           "('SUICIDAL_IDEATION', 'SELF_HARM', 'CRISIS_SAFETY', 'MENTAL_HEALTH') " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findMentalHealthConcerns();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.primaryConcern IN " +
           "('BULLYING_VICTIM', 'BULLYING_PERPETRATOR', 'CONFLICT') " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findBullyingConcerns();

    // Intake Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.intakeCompleted = false " +
           "AND cr.referralStatus IN ('ASSIGNED', 'INTAKE_SCHEDULED') " +
           "ORDER BY cr.assignmentDate ASC")
    List<CounselingReferral> findPendingIntake();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.intakeCompleted = true " +
           "ORDER BY cr.intakeDate DESC")
    List<CounselingReferral> findCompletedIntakes();

    // Services Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.servicesInitiated = false " +
           "AND cr.referralStatus = 'IN_PROGRESS' " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findAwaitingServicesInitiation();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.servicesInitiated = true " +
           "ORDER BY cr.servicesStartDate DESC")
    List<CounselingReferral> findWithServicesInitiated();

    // External Referral Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralType IN " +
           "('EXTERNAL_THERAPIST', 'PSYCHIATRIST', 'COMMUNITY_MENTAL_HEALTH', 'HOSPITAL_PSYCHIATRIC', " +
           "'SUBSTANCE_ABUSE', 'FAMILY_THERAPY') " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findExternalReferrals();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.parentConsentObtained = false " +
           "AND cr.referralType IN ('EXTERNAL_THERAPIST', 'PSYCHIATRIST', 'COMMUNITY_MENTAL_HEALTH', " +
           "'HOSPITAL_PSYCHIATRIC', 'SUBSTANCE_ABUSE', 'FAMILY_THERAPY') " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findNeedingParentConsent();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.releaseOfInformationSigned = false " +
           "AND cr.servicesInitiated = true " +
           "ORDER BY cr.servicesStartDate ASC")
    List<CounselingReferral> findNeedingReleaseOfInformation();

    // Parent Communication Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.parentContacted = false " +
           "AND (cr.urgencyLevel IN ('URGENT', 'EMERGENCY') OR cr.crisisInterventionNeeded = true) " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findNeedingParentContact();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.parentMeetingHeld = true " +
           "ORDER BY cr.parentMeetingDate DESC")
    List<CounselingReferral> findWithParentMeeting();

    // Outcome Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.goalsMet = true " +
           "ORDER BY cr.closureDate DESC")
    List<CounselingReferral> findSuccessfulOutcomes();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.improvementNoted = true " +
           "ORDER BY cr.closureDate DESC")
    List<CounselingReferral> findWithImprovement();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.followUpNeeded = true " +
           "ORDER BY cr.closureDate DESC")
    List<CounselingReferral> findNeedingFollowUp();

    // Documentation Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.documentationComplete = false " +
           "AND cr.referralStatus = 'COMPLETED' " +
           "ORDER BY cr.closureDate ASC")
    List<CounselingReferral> findNeedingDocumentation();

    // Overdue Queries
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus = 'PENDING' " +
           "AND cr.urgencyLevel = 'EMERGENCY' " +
           "AND cr.referralDate < CURRENT_DATE " +
           "AND cr.servicesInitiated = false " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findOverdueEmergency();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus IN ('PENDING', 'ASSIGNED') " +
           "AND cr.urgencyLevel = 'URGENT' " +
           "AND cr.referralDate < CURRENT_DATE - 2 DAY " +
           "AND cr.servicesInitiated = false " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findOverdueUrgent();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus IN ('PENDING', 'ASSIGNED') " +
           "AND cr.urgencyLevel = 'MODERATE' " +
           "AND cr.referralDate < CURRENT_DATE - 7 DAY " +
           "AND cr.servicesInitiated = false " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findOverdueModerate();

    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referralStatus IN ('PENDING', 'ASSIGNED') " +
           "AND cr.urgencyLevel = 'ROUTINE' " +
           "AND cr.referralDate < CURRENT_DATE - 14 DAY " +
           "AND cr.servicesInitiated = false " +
           "ORDER BY cr.referralDate ASC")
    List<CounselingReferral> findOverdueRoutine();

    // Teacher Referrals
    @Query("SELECT cr FROM CounselingReferral cr WHERE cr.referringTeacher.id = :teacherId " +
           "ORDER BY cr.referralDate DESC")
    List<CounselingReferral> findByReferringTeacher(@Param("teacherId") Long teacherId);

    // Statistics
    @Query("SELECT COUNT(cr) FROM CounselingReferral cr WHERE cr.referralDate = :today")
    Long countToday(@Param("today") LocalDate today);

    @Query("SELECT cr.referralSource, COUNT(cr) FROM CounselingReferral cr " +
           "GROUP BY cr.referralSource ORDER BY COUNT(cr) DESC")
    List<Object[]> countByReferralSource();

    @Query("SELECT cr.primaryConcern, COUNT(cr) FROM CounselingReferral cr " +
           "GROUP BY cr.primaryConcern ORDER BY COUNT(cr) DESC")
    List<Object[]> countByPrimaryConcern();

    @Query("SELECT cr.referralType, COUNT(cr) FROM CounselingReferral cr " +
           "GROUP BY cr.referralType ORDER BY COUNT(cr) DESC")
    List<Object[]> countByReferralType();

    @Query("SELECT COUNT(cr) FROM CounselingReferral cr WHERE cr.crisisInterventionNeeded = true")
    Long countCrisisReferrals();

    @Query("SELECT COUNT(cr) FROM CounselingReferral cr WHERE cr.suicideRiskIndicated = true")
    Long countSuicideRiskReferrals();
}
