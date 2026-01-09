package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Counseling Management Service
 * Provides business logic for counseling services, crisis intervention,
 * college planning, and social work case management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounselingManagementService {

    private final CounselingSessionRepository counselingSessionRepository;
    private final CounselingReferralRepository counselingReferralRepository;
    private final CollegeApplicationRepository collegeApplicationRepository;
    private final SocialWorkCaseRepository socialWorkCaseRepository;
    private final CrisisInterventionRepository crisisInterventionRepository;

    // ========================================================================
    // COUNSELING SESSION METHODS
    // ========================================================================

    /**
     * Get all crisis sessions that need parent notification
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getCrisisSessionsNeedingParentNotification() {
        return counselingSessionRepository.findNeedingParentNotification();
    }

    /**
     * Get high-risk counseling sessions
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getHighRiskSessions() {
        return counselingSessionRepository.findHighRiskSessions();
    }

    /**
     * Get sessions needing follow-up
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getSessionsNeedingFollowUp() {
        return counselingSessionRepository.findNeedingFollowUp();
    }

    /**
     * Get overdue follow-up sessions
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getOverdueFollowUpSessions() {
        return counselingSessionRepository.findOverdueFollowUp(LocalDate.now());
    }

    /**
     * Get sessions for student
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getSessionsForStudent(Long studentId) {
        return counselingSessionRepository.findByStudentId(studentId);
    }

    /**
     * Get crisis intervention sessions
     */
    @Transactional(readOnly = true)
    public List<CounselingSession> getCrisisInterventionSessions() {
        return counselingSessionRepository.findCrisisInterventionSessions();
    }

    // ========================================================================
    // COUNSELING REFERRAL METHODS
    // ========================================================================

    /**
     * Get all pending referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getPendingReferrals() {
        return counselingReferralRepository.findPendingReferrals();
    }

    /**
     * Get urgent referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getUrgentReferrals() {
        return counselingReferralRepository.findUrgentReferrals();
    }

    /**
     * Get high-risk referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getHighRiskReferrals() {
        return counselingReferralRepository.findHighRiskReferrals();
    }

    /**
     * Get suicide risk referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getSuicideRiskReferrals() {
        return counselingReferralRepository.findSuicideRiskReferrals();
    }

    /**
     * Get referrals needing risk assessment
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getReferralsNeedingRiskAssessment() {
        return counselingReferralRepository.findNeedingRiskAssessment();
    }

    /**
     * Get overdue urgent referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getOverdueUrgentReferrals() {
        return counselingReferralRepository.findOverdueUrgent();
    }

    /**
     * Get referrals for student
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getReferralsForStudent(Long studentId) {
        return counselingReferralRepository.findByStudentId(studentId);
    }

    /**
     * Get referrals by date range
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getReferralsByDateRange(LocalDate startDate, LocalDate endDate) {
        return counselingReferralRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get overdue referrals
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getOverdueReferrals() {
        List<CounselingReferral> overdue = new java.util.ArrayList<>();
        overdue.addAll(counselingReferralRepository.findOverdueEmergency());
        overdue.addAll(counselingReferralRepository.findOverdueUrgent());
        overdue.addAll(counselingReferralRepository.findOverdueModerate());
        overdue.addAll(counselingReferralRepository.findOverdueRoutine());
        return overdue;
    }

    /**
     * Get referrals by student object
     */
    @Transactional(readOnly = true)
    public List<CounselingReferral> getReferralsByStudent(Student student) {
        return counselingReferralRepository.findByStudentId(student.getId());
    }

    /**
     * Create a new counseling referral
     */
    @Transactional
    public CounselingReferral createReferral(CounselingReferral referral) {
        log.info("Creating new counseling referral for student {}",
                referral.getStudent() != null ? referral.getStudent().getId() : "unknown");

        // Set creation timestamp if not set
        if (referral.getReferralDate() == null) {
            referral.setReferralDate(LocalDate.now());
        }

        // Ensure status is set
        if (referral.getReferralStatus() == null) {
            referral.setReferralStatus(CounselingReferral.ReferralStatus.PENDING);
        }

        return counselingReferralRepository.save(referral);
    }

    /**
     * Update an existing counseling referral
     */
    @Transactional
    public CounselingReferral updateReferral(CounselingReferral referral) {
        log.info("Updating counseling referral {}", referral.getId());
        return counselingReferralRepository.save(referral);
    }

    /**
     * Delete a counseling referral
     */
    @Transactional
    public void deleteReferral(Long referralId) {
        log.info("Deleting counseling referral {}", referralId);
        counselingReferralRepository.deleteById(referralId);
    }

    // ========================================================================
    // COLLEGE APPLICATION METHODS
    // ========================================================================

    /**
     * Get college applications with upcoming deadlines
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getUpcomingDeadlines(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return collegeApplicationRepository.findUpcomingDeadlines(today, futureDate);
    }

    /**
     * Get overdue applications
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getOverdueApplications() {
        return collegeApplicationRepository.findOverdueApplications(LocalDate.now());
    }

    /**
     * Get applications with missing requirements
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getApplicationsWithMissingRequirements() {
        return collegeApplicationRepository.findWithMissingRequirements();
    }

    /**
     * Get applications needing decision response
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getApplicationsNeedingDecisionResponse() {
        return collegeApplicationRepository.findNeedingDecisionResponse();
    }

    /**
     * Get applications needing FAFSA
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getApplicationsNeedingFAFSA() {
        return collegeApplicationRepository.findNeedingFAFSA();
    }

    /**
     * Get college applications for student
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getCollegeApplicationsForStudent(Long studentId) {
        return collegeApplicationRepository.findByStudentOrderedByPriority(studentId);
    }

    /**
     * Get accepted applications for student
     */
    @Transactional(readOnly = true)
    public List<CollegeApplication> getAcceptedApplicationsForStudent(Long studentId) {
        return collegeApplicationRepository.findAcceptedByStudent(studentId);
    }

    // ========================================================================
    // SOCIAL WORK CASE METHODS
    // ========================================================================

    /**
     * Get high-priority social work cases
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getHighPriorityCases() {
        return socialWorkCaseRepository.findHighPriorityCases();
    }

    /**
     * Get McKinney-Vento eligible students
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getMckinneyVentoCases() {
        return socialWorkCaseRepository.findActiveMckinneyVentoCases();
    }

    /**
     * Get foster care cases
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getFosterCareCases() {
        return socialWorkCaseRepository.findActiveFosterCareCases();
    }

    /**
     * Get CPS involvement cases
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getCPSCases() {
        return socialWorkCaseRepository.findActiveCPSCases();
    }

    /**
     * Get cases needing home visit
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getCasesNeedingHomeVisit() {
        return socialWorkCaseRepository.findNeedingHomeVisit();
    }

    /**
     * Get cases needing follow-up
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getCasesNeedingFollowUp() {
        return socialWorkCaseRepository.findOverdueFollowUp(LocalDate.now());
    }

    /**
     * Get cases for student
     */
    @Transactional(readOnly = true)
    public List<SocialWorkCase> getCasesForStudent(Long studentId) {
        return socialWorkCaseRepository.findByStudentId(studentId);
    }

    /**
     * Save or update a social work case
     */
    @Transactional
    public SocialWorkCase saveSocialWorkCase(SocialWorkCase socialWorkCase) {
        log.info("Saving social work case for student: {}", socialWorkCase.getStudent().getStudentId());
        return socialWorkCaseRepository.save(socialWorkCase);
    }

    // ========================================================================
    // CRISIS INTERVENTION METHODS
    // ========================================================================

    /**
     * Get high-risk crisis interventions
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getHighRiskCrises() {
        return crisisInterventionRepository.findHighRiskCrises();
    }

    /**
     * Get suicide-related crises
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getSuicideRelatedCrises() {
        return crisisInterventionRepository.findSuicideRelatedCrises();
    }

    /**
     * Get violence-related crises
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getViolenceRelatedCrises() {
        return crisisInterventionRepository.findViolenceRelatedCrises();
    }

    /**
     * Get crises needing parent notification
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getCrisesNeedingParentNotification() {
        return crisisInterventionRepository.findNeedingParentNotification();
    }

    /**
     * Get crises needing follow-up
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getCrisesNeedingFollowUp() {
        return crisisInterventionRepository.findNeedingFollowUp();
    }

    /**
     * Get crises needing clearance to return
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getCrisesNeedingClearance() {
        return crisisInterventionRepository.findNeedingClearance();
    }

    /**
     * Get crisis interventions for student
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getCrisisInterventionsForStudent(Long studentId) {
        return crisisInterventionRepository.findByStudentId(studentId);
    }

    /**
     * Get recent crises (last 30 days)
     */
    @Transactional(readOnly = true)
    public List<CrisisIntervention> getRecentCrises() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return crisisInterventionRepository.findSinceDate(thirtyDaysAgo);
    }

    // ========================================================================
    // DASHBOARD AND ALERT METHODS
    // ========================================================================

    /**
     * Get counseling alerts for the dashboard
     */
    @Transactional(readOnly = true)
    public CounselingAlerts getCounselingAlerts() {
        CounselingAlerts alerts = new CounselingAlerts();

        // Crisis and safety alerts
        alerts.setHighRiskSessions(counselingSessionRepository.findHighRiskSessions().size());
        alerts.setUrgentReferrals(counselingReferralRepository.findUrgentReferrals().size());
        alerts.setSuicideRiskReferrals(counselingReferralRepository.findSuicideRiskReferrals().size());
        alerts.setHighRiskCrises(crisisInterventionRepository.findHighRiskCrises().size());

        // Follow-up alerts
        alerts.setOverdueFollowUpSessions(counselingSessionRepository.findOverdueFollowUp(LocalDate.now()).size());
        alerts.setOverdueFollowUpCrises(crisisInterventionRepository.findOverdueFollowUp(LocalDate.now()).size());

        // College planning alerts
        alerts.setUpcomingDeadlines(collegeApplicationRepository.findDeadlinesApproaching(
            LocalDate.now().plusDays(14)).size());
        alerts.setOverdueApplications(collegeApplicationRepository.findOverdueApplications(LocalDate.now()).size());
        alerts.setMissingRequirements(collegeApplicationRepository.findWithMissingRequirements().size());

        // Social work alerts
        alerts.setHighPriorityCases(socialWorkCaseRepository.findHighPriorityCases().size());
        alerts.setMckinneyVentoCases(socialWorkCaseRepository.findActiveMckinneyVentoCases().size());
        alerts.setActiveCPSCases(socialWorkCaseRepository.findActiveCPSCases().size());

        return alerts;
    }

    /**
     * Data transfer object for counseling alerts
     */
    public static class CounselingAlerts {
        private int highRiskSessions;
        private int urgentReferrals;
        private int suicideRiskReferrals;
        private int highRiskCrises;
        private int overdueFollowUpSessions;
        private int overdueFollowUpCrises;
        private int upcomingDeadlines;
        private int overdueApplications;
        private int missingRequirements;
        private int highPriorityCases;
        private int mckinneyVentoCases;
        private int activeCPSCases;

        // Getters and setters
        public int getHighRiskSessions() { return highRiskSessions; }
        public void setHighRiskSessions(int highRiskSessions) { this.highRiskSessions = highRiskSessions; }

        public int getUrgentReferrals() { return urgentReferrals; }
        public void setUrgentReferrals(int urgentReferrals) { this.urgentReferrals = urgentReferrals; }

        public int getSuicideRiskReferrals() { return suicideRiskReferrals; }
        public void setSuicideRiskReferrals(int suicideRiskReferrals) { this.suicideRiskReferrals = suicideRiskReferrals; }

        public int getHighRiskCrises() { return highRiskCrises; }
        public void setHighRiskCrises(int highRiskCrises) { this.highRiskCrises = highRiskCrises; }

        public int getOverdueFollowUpSessions() { return overdueFollowUpSessions; }
        public void setOverdueFollowUpSessions(int overdueFollowUpSessions) { this.overdueFollowUpSessions = overdueFollowUpSessions; }

        public int getOverdueFollowUpCrises() { return overdueFollowUpCrises; }
        public void setOverdueFollowUpCrises(int overdueFollowUpCrises) { this.overdueFollowUpCrises = overdueFollowUpCrises; }

        public int getUpcomingDeadlines() { return upcomingDeadlines; }
        public void setUpcomingDeadlines(int upcomingDeadlines) { this.upcomingDeadlines = upcomingDeadlines; }

        public int getOverdueApplications() { return overdueApplications; }
        public void setOverdueApplications(int overdueApplications) { this.overdueApplications = overdueApplications; }

        public int getMissingRequirements() { return missingRequirements; }
        public void setMissingRequirements(int missingRequirements) { this.missingRequirements = missingRequirements; }

        public int getHighPriorityCases() { return highPriorityCases; }
        public void setHighPriorityCases(int highPriorityCases) { this.highPriorityCases = highPriorityCases; }

        public int getMckinneyVentoCases() { return mckinneyVentoCases; }
        public void setMckinneyVentoCases(int mckinneyVentoCases) { this.mckinneyVentoCases = mckinneyVentoCases; }

        public int getActiveCPSCases() { return activeCPSCases; }
        public void setActiveCPSCases(int activeCPSCases) { this.activeCPSCases = activeCPSCases; }
    }
}
