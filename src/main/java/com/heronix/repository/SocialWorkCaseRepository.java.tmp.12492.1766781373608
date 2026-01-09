package com.heronix.repository;

import com.heronix.model.domain.SocialWorkCase;
import com.heronix.model.domain.SocialWorkCase.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Social Work Case entities
 * Provides data access for social work case management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface SocialWorkCaseRepository extends JpaRepository<SocialWorkCase, Long> {

    // Basic Queries
    List<SocialWorkCase> findByStudentId(Long studentId);

    List<SocialWorkCase> findBySocialWorkerId(Long socialWorkerId);

    Optional<SocialWorkCase> findByCaseNumber(String caseNumber);

    List<SocialWorkCase> findByCaseType(CaseType caseType);

    List<SocialWorkCase> findByCaseStatus(CaseStatus caseStatus);

    List<SocialWorkCase> findByPriorityLevel(PriorityLevel priorityLevel);

    // Status Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.caseStatus = 'OPEN' " +
           "ORDER BY swc.priorityLevel DESC, swc.caseOpenedDate ASC")
    List<SocialWorkCase> findOpenCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.caseStatus IN " +
           "('OPEN', 'SERVICES_IN_PROGRESS', 'PENDING_REFERRAL') " +
           "ORDER BY swc.priorityLevel DESC")
    List<SocialWorkCase> findActiveCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.caseStatus IN ('CLOSED_SUCCESSFUL', 'CLOSED_UNSUCCESSFUL') " +
           "ORDER BY swc.caseClosedDate DESC")
    List<SocialWorkCase> findClosedCases();

    // Priority Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.priorityLevel IN ('HIGH', 'URGENT', 'CRISIS') " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.priorityLevel DESC, swc.caseOpenedDate ASC")
    List<SocialWorkCase> findHighPriorityCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.priorityLevel = 'CRISIS' " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findCrisisCases();

    // McKinney-Vento Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mckinneyVentoEligible = true " +
           "ORDER BY swc.mckinneyVentoDeterminationDate DESC")
    List<SocialWorkCase> findMckinneyVentoEligible();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mckinneyVentoEligible = true " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findActiveMckinneyVentoCases();

    List<SocialWorkCase> findByHousingSituation(HousingSituation housingSituation);

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.transportationProvided = true " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findReceivingTransportation();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mckinneyVentoEligible = true " +
           "AND swc.immediateEnrollmentCompleted = false " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingImmediateEnrollment();

    // Foster Care Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.inFosterCare = true " +
           "ORDER BY swc.fosterCarePlacementDate DESC")
    List<SocialWorkCase> findFosterCareCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.inFosterCare = true " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.fosterCarePlacementDate ASC")
    List<SocialWorkCase> findActiveFosterCareCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.siblingPlacement = true " +
           "ORDER BY swc.fosterCarePlacementDate DESC")
    List<SocialWorkCase> findSiblingPlacements();

    // CPS Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.cpsInvolvement = true " +
           "ORDER BY swc.cpsCaseOpenDate DESC")
    List<SocialWorkCase> findCPSInvolvementCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.cpsInvolvement = true " +
           "AND swc.cpsInvestigationStatus IN ('INVESTIGATION_OPEN', 'SERVICES_OPEN') " +
           "ORDER BY swc.cpsCaseOpenDate ASC")
    List<SocialWorkCase> findActiveCPSCases();

    List<SocialWorkCase> findByCpsInvestigationStatus(CPSStatus status);

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.safetyPlanInPlace = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findWithSafetyPlan();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mandatedReportingMade = true " +
           "ORDER BY swc.mandatedReportDate DESC")
    List<SocialWorkCase> findMandatedReports();

    // Family Situation Queries
    List<SocialWorkCase> findByFamilyStructure(FamilyStructure familyStructure);

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.financialHardship = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findFinancialHardshipCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.housingInstability = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findHousingInstabilityCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.foodInsecurity = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findFoodInsecurityCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.medicalNeedsUnmet = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findUnmetMedicalNeedsCases();

    // Home Visit Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.homeVisitConducted = false " +
           "AND swc.caseStatus = 'OPEN' " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingHomeVisit();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.homeVisitConducted = true " +
           "ORDER BY swc.lastHomeVisitDate DESC")
    List<SocialWorkCase> findWithHomeVisit();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.homeEnvironmentConcerns = true " +
           "ORDER BY swc.lastHomeVisitDate DESC")
    List<SocialWorkCase> findHomeEnvironmentConcerns();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.homeVisitConducted = true " +
           "AND swc.lastHomeVisitDate < :cutoffDate " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.lastHomeVisitDate ASC")
    List<SocialWorkCase> findNeedingFollowUpVisit(@Param("cutoffDate") LocalDate cutoffDate);

    // Community Referral Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.communityReferralsMade = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findWithCommunityReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.foodBankReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findFoodBankReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.housingAssistanceReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findHousingAssistanceReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mentalHealthReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findMentalHealthReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.substanceAbuseServicesReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findSubstanceAbuseReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.domesticViolenceServicesReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findDomesticViolenceReferrals();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.legalAidReferral = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findLegalAidReferrals();

    // School Support Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.freeLunchApproved = true " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findFreeLunchApproved();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.backpackProgramEnrolled = true " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findBackpackProgramEnrollment();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.mentoringProgramEnrolled = true " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findMentoringProgramEnrollment();

    // Multi-Agency Coordination Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.multiAgencyTeamInvolved = true " +
           "ORDER BY swc.lastTeamMeetingDate DESC")
    List<SocialWorkCase> findMultiAgencyCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.teamMeetingScheduled = true " +
           "ORDER BY swc.nextTeamMeetingDate ASC")
    List<SocialWorkCase> findScheduledTeamMeetings();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.multiAgencyTeamInvolved = true " +
           "AND swc.nextTeamMeetingDate IS NOT NULL " +
           "AND swc.nextTeamMeetingDate <= :date " +
           "ORDER BY swc.nextTeamMeetingDate ASC")
    List<SocialWorkCase> findUpcomingTeamMeetings(@Param("date") LocalDate date);

    // Case Management Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.servicePlanDeveloped = false " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingServicePlan();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.followUpNeeded = true " +
           "ORDER BY swc.nextFollowUpDate ASC")
    List<SocialWorkCase> findNeedingFollowUp();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.followUpNeeded = true " +
           "AND swc.nextFollowUpDate IS NOT NULL " +
           "AND swc.nextFollowUpDate <= :date " +
           "ORDER BY swc.nextFollowUpDate ASC")
    List<SocialWorkCase> findOverdueFollowUp(@Param("date") LocalDate date);

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.lastContactDate < :cutoffDate " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.lastContactDate ASC")
    List<SocialWorkCase> findNeedingContact(@Param("cutoffDate") LocalDate cutoffDate);

    // Parent Communication Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.parentContactEstablished = false " +
           "AND swc.caseStatus = 'OPEN' " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingParentContact();

    List<SocialWorkCase> findByParentCooperationLevel(CooperationLevel level);

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.languageBarrier = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findLanguageBarrierCases();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.interpreterNeeded = true " +
           "ORDER BY swc.caseOpenedDate DESC")
    List<SocialWorkCase> findNeedingInterpreter();

    // Documentation Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.consentFormsSigned = false " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingConsentForms();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.releaseOfInformationSigned = false " +
           "AND swc.multiAgencyTeamInvolved = true " +
           "ORDER BY swc.caseOpenedDate ASC")
    List<SocialWorkCase> findNeedingReleaseOfInformation();

    // Outcome Queries
    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.goalsAchieved = true " +
           "ORDER BY swc.caseClosedDate DESC")
    List<SocialWorkCase> findSuccessfulOutcomes();

    @Query("SELECT swc FROM SocialWorkCase swc WHERE swc.servicesCompleted = true " +
           "ORDER BY swc.caseClosedDate DESC")
    List<SocialWorkCase> findCompletedServices();

    // Statistics
    @Query("SELECT COUNT(swc) FROM SocialWorkCase swc WHERE swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS')")
    Long countActiveCases();

    @Query("SELECT swc.caseType, COUNT(swc) FROM SocialWorkCase swc " +
           "GROUP BY swc.caseType ORDER BY COUNT(swc) DESC")
    List<Object[]> countByCaseType();

    @Query("SELECT swc.priorityLevel, COUNT(swc) FROM SocialWorkCase swc " +
           "WHERE swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS') " +
           "GROUP BY swc.priorityLevel")
    List<Object[]> countActiveCasesByPriority();

    @Query("SELECT COUNT(swc) FROM SocialWorkCase swc WHERE swc.mckinneyVentoEligible = true " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS')")
    Long countActiveMckinneyVentoCases();

    @Query("SELECT COUNT(swc) FROM SocialWorkCase swc WHERE swc.inFosterCare = true " +
           "AND swc.caseStatus IN ('OPEN', 'SERVICES_IN_PROGRESS')")
    Long countActiveFosterCareCases();

    @Query("SELECT COUNT(swc) FROM SocialWorkCase swc WHERE swc.cpsInvolvement = true " +
           "AND swc.cpsInvestigationStatus IN ('INVESTIGATION_OPEN', 'SERVICES_OPEN')")
    Long countActiveCPSCases();
}
