package com.heronix.repository;

import com.heronix.model.domain.CrisisIntervention;
import com.heronix.model.domain.CrisisIntervention.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Crisis Intervention entities
 * Provides data access for crisis intervention tracking and management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface CrisisInterventionRepository extends JpaRepository<CrisisIntervention, Long> {

    // Basic Queries
    List<CrisisIntervention> findByStudentId(Long studentId);

    List<CrisisIntervention> findByRespondingCounselorId(Long counselorId);

    Optional<CrisisIntervention> findByCrisisNumber(String crisisNumber);

    List<CrisisIntervention> findByCrisisType(CrisisType crisisType);

    List<CrisisIntervention> findByCrisisSeverity(CrisisSeverity crisisSeverity);

    List<CrisisIntervention> findByRiskLevel(RiskLevel riskLevel);

    // Date Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisDate = :date " +
           "ORDER BY ci.crisisTime ASC")
    List<CrisisIntervention> findByDate(@Param("date") LocalDate date);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ci.crisisDate DESC, ci.crisisTime DESC")
    List<CrisisIntervention> findByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisDate >= :startDate " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSinceDate(@Param("startDate") LocalDate startDate);

    // Crisis Type Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisType IN " +
           "('SUICIDAL_IDEATION', 'SUICIDE_ATTEMPT', 'SELF_HARM') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSuicideRelatedCrises();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisType IN " +
           "('THREAT_TO_OTHERS', 'VIOLENT_BEHAVIOR', 'SCHOOL_THREAT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findViolenceRelatedCrises();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisType IN " +
           "('DOMESTIC_VIOLENCE', 'SEXUAL_ASSAULT', 'CHILD_ABUSE') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findAbuseRelatedCrises();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisType = 'PSYCHOTIC_EPISODE' " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPsychoticEpisodes();

    // Severity and Risk Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisSeverity IN ('SEVERE', 'LIFE_THREATENING') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSevereCrises();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findHighRiskCrises();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.imminentDanger = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findImminentDangerCrises();

    // Suicide Risk Assessment Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.suicideRiskAssessment = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSuicideRiskAssessments();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.suicideRiskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findHighSuicideRisk();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.suicidalIdeation = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSuicidalIdeation();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.suicidePlan = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findWithSuicidePlan();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.suicideMeansAccess = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findWithMeansAccess();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.previousSuicideAttempts = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPreviousAttempts();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.currentIntent = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findCurrentIntent();

    // Self-Harm Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.selfHarmBehavior = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSelfHarmBehavior();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.recentSelfHarm = true " +
           "ORDER BY ci.selfHarmDate DESC")
    List<CrisisIntervention> findRecentSelfHarm();

    // Threat Assessment Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.threatAssessmentConducted = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findThreatAssessments();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.threatLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findHighThreatLevel();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.threatToOthers = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findThreatToOthers();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.specificTargetIdentified = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSpecificTargets();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.violentIdeation = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findViolentIdeation();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.planToHarmOthers = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPlanToHarmOthers();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.weaponAccess = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findWeaponAccess();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.previousViolentBehavior = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPreviousViolence();

    // Mental Status Queries
    List<CrisisIntervention> findByAffect(Affect affect);

    List<CrisisIntervention> findByMood(Mood mood);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.psychoticSymptoms = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPsychoticSymptoms();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.disorientedConfused = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findDisoriented();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.substanceInfluence = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSubstanceInfluence();

    // Safety Plan Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.safetyPlanCreated = true " +
           "ORDER BY ci.safetyPlanDate DESC")
    List<CrisisIntervention> findWithSafetyPlan();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.safetyPlanCreated = false " +
           "AND ci.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findNeedingSafetyPlan();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.crisisHotlineProvided = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findCrisisHotlineProvided();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.environmentMadeSafe = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findEnvironmentMadeSafe();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.meansRestrictionDiscussed = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findMeansRestrictionDiscussed();

    // Intervention Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.studentStabilized = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findStudentStabilized();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.studentStabilized = false " +
           "AND ci.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findNotYetStabilized();

    // Emergency Response Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.emergencyServicesCalled = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findEmergencyServicesCalled();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.ambulanceTransport = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findAmbulanceTransports();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.policeInvolved = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPoliceInvolvement();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.hospitalTransport = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findHospitalTransports();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.psychiatricHold = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findPsychiatricHolds();

    // Parent Notification Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.parentNotified = false " +
           "AND ci.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY ci.crisisDate ASC")
    List<CrisisIntervention> findNeedingParentNotification();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.parentNotified = true " +
           "ORDER BY ci.parentNotificationTime DESC")
    List<CrisisIntervention> findWithParentNotification();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.parentRefusedServices = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findParentRefusedServices();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.parentPickedUpStudent = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findParentPickedUp();

    // Administration Notification Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.administrationNotified = true " +
           "ORDER BY ci.administrationNotificationTime DESC")
    List<CrisisIntervention> findAdministrationNotified();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.administrationNotified = false " +
           "AND (ci.riskLevel IN ('HIGH', 'IMMINENT') OR ci.emergencyServicesCalled = true) " +
           "ORDER BY ci.crisisDate ASC")
    List<CrisisIntervention> findNeedingAdministrationNotification();

    // Referral Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.referralMade = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findWithReferrals();

    List<CrisisIntervention> findByReferralType(ExternalReferralType referralType);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.referralType IN " +
           "('EMERGENCY_ROOM', 'PSYCHIATRIC_HOSPITAL', 'CRISIS_CENTER') " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findEmergencyReferrals();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.referralAccepted = false " +
           "AND ci.referralMade = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findRefusedReferrals();

    // Follow-up Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.followUpRequired = true " +
           "AND ci.followUpCompleted = false " +
           "ORDER BY ci.followUpDate ASC")
    List<CrisisIntervention> findNeedingFollowUp();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.followUpRequired = true " +
           "AND ci.followUpCompleted = false " +
           "AND ci.followUpDate IS NOT NULL " +
           "AND ci.followUpDate <= :date " +
           "ORDER BY ci.followUpDate ASC")
    List<CrisisIntervention> findOverdueFollowUp(@Param("date") LocalDate date);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.followUpCompleted = true " +
           "ORDER BY ci.followUpDate DESC")
    List<CrisisIntervention> findCompletedFollowUp();

    // Return to School Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.studentReturnedToClass = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findReturnedToClass();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.sentHome = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findSentHome();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.clearanceRequiredToReturn = true " +
           "AND ci.clearanceReceived = false " +
           "ORDER BY ci.crisisDate ASC")
    List<CrisisIntervention> findNeedingClearance();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.clearanceReceived = true " +
           "ORDER BY ci.clearanceDate DESC")
    List<CrisisIntervention> findWithClearance();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.returnToSchoolMeetingHeld = true " +
           "ORDER BY ci.returnToSchoolDate DESC")
    List<CrisisIntervention> findReturnToSchoolMeetings();

    // Documentation Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.incidentReportCompleted = false " +
           "ORDER BY ci.crisisDate ASC")
    List<CrisisIntervention> findNeedingIncidentReport();

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.staffDebriefingConducted = true " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findWithStaffDebriefing();

    // Student-Specific Queries
    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.student.id = :studentId " +
           "AND ci.crisisType = :crisisType " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findByStudentAndType(@Param("studentId") Long studentId,
                                                     @Param("crisisType") CrisisType crisisType);

    @Query("SELECT ci FROM CrisisIntervention ci WHERE ci.student.id = :studentId " +
           "AND ci.crisisDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ci.crisisDate DESC")
    List<CrisisIntervention> findByStudentAndDateRange(@Param("studentId") Long studentId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.student.id = :studentId")
    Long countByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.student.id = :studentId " +
           "AND ci.crisisType IN ('SUICIDAL_IDEATION', 'SUICIDE_ATTEMPT', 'SELF_HARM')")
    Long countSuicideRelatedByStudent(@Param("studentId") Long studentId);

    // Statistics
    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.crisisDate = :today")
    Long countToday(@Param("today") LocalDate today);

    @Query("SELECT ci.crisisType, COUNT(ci) FROM CrisisIntervention ci " +
           "GROUP BY ci.crisisType ORDER BY COUNT(ci) DESC")
    List<Object[]> countByCrisisType();

    @Query("SELECT ci.riskLevel, COUNT(ci) FROM CrisisIntervention ci " +
           "GROUP BY ci.riskLevel")
    List<Object[]> countByRiskLevel();

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.emergencyServicesCalled = true")
    Long countEmergencyServicesCalled();

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.hospitalTransport = true")
    Long countHospitalTransports();

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.suicideRiskLevel IN ('HIGH', 'IMMINENT')")
    Long countHighSuicideRisk();

    @Query("SELECT COUNT(ci) FROM CrisisIntervention ci WHERE ci.threatLevel IN ('HIGH', 'IMMINENT')")
    Long countHighThreatLevel();
}
