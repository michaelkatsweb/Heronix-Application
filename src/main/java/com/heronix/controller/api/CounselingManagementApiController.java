package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.CounselingManagementService;
import com.heronix.service.CounselingManagementService.CounselingAlerts;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Counseling Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
public class CounselingManagementApiController {

    private final CounselingManagementService counselingService;
    private final CounselingSessionRepository counselingSessionRepository;
    private final CounselingReferralRepository counselingReferralRepository;
    private final CollegeApplicationRepository collegeApplicationRepository;
    private final SocialWorkCaseRepository socialWorkCaseRepository;
    private final CrisisInterventionRepository crisisInterventionRepository;
    private final StudentRepository studentRepository;

    // ==================== Counseling Session Operations ====================

    @GetMapping("/sessions/crisis-parent-notification")
    public ResponseEntity<List<CounselingSession>> getCrisisSessionsNeedingParentNotification() {
        List<CounselingSession> sessions = counselingService.getCrisisSessionsNeedingParentNotification();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/high-risk")
    public ResponseEntity<List<CounselingSession>> getHighRiskSessions() {
        List<CounselingSession> sessions = counselingService.getHighRiskSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/needing-followup")
    public ResponseEntity<List<CounselingSession>> getSessionsNeedingFollowUp() {
        List<CounselingSession> sessions = counselingService.getSessionsNeedingFollowUp();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/overdue-followup")
    public ResponseEntity<List<CounselingSession>> getOverdueFollowUpSessions() {
        List<CounselingSession> sessions = counselingService.getOverdueFollowUpSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/student/{studentId}")
    public ResponseEntity<List<CounselingSession>> getSessionsForStudent(@PathVariable Long studentId) {
        List<CounselingSession> sessions = counselingService.getSessionsForStudent(studentId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/crisis-intervention")
    public ResponseEntity<List<CounselingSession>> getCrisisInterventionSessions() {
        List<CounselingSession> sessions = counselingService.getCrisisInterventionSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<CounselingSession> getSessionById(@PathVariable Long id) {
        return counselingSessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions")
    public ResponseEntity<CounselingSession> createSession(@RequestBody CounselingSession session) {
        CounselingSession created = counselingSessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<CounselingSession> updateSession(
            @PathVariable Long id,
            @RequestBody CounselingSession session) {
        session.setId(id);
        CounselingSession updated = counselingSessionRepository.save(session);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        counselingSessionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Counseling Referral Operations ====================

    @GetMapping("/referrals/pending")
    public ResponseEntity<List<CounselingReferral>> getPendingReferrals() {
        List<CounselingReferral> referrals = counselingService.getPendingReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/urgent")
    public ResponseEntity<List<CounselingReferral>> getUrgentReferrals() {
        List<CounselingReferral> referrals = counselingService.getUrgentReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/high-risk")
    public ResponseEntity<List<CounselingReferral>> getHighRiskReferrals() {
        List<CounselingReferral> referrals = counselingService.getHighRiskReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/suicide-risk")
    public ResponseEntity<List<CounselingReferral>> getSuicideRiskReferrals() {
        List<CounselingReferral> referrals = counselingService.getSuicideRiskReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/needing-risk-assessment")
    public ResponseEntity<List<CounselingReferral>> getReferralsNeedingRiskAssessment() {
        List<CounselingReferral> referrals = counselingService.getReferralsNeedingRiskAssessment();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/overdue-urgent")
    public ResponseEntity<List<CounselingReferral>> getOverdueUrgentReferrals() {
        List<CounselingReferral> referrals = counselingService.getOverdueUrgentReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/overdue")
    public ResponseEntity<List<CounselingReferral>> getOverdueReferrals() {
        List<CounselingReferral> referrals = counselingService.getOverdueReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/student/{studentId}")
    public ResponseEntity<List<CounselingReferral>> getReferralsForStudent(@PathVariable Long studentId) {
        List<CounselingReferral> referrals = counselingService.getReferralsForStudent(studentId);
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/date-range")
    public ResponseEntity<List<CounselingReferral>> getReferralsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CounselingReferral> referrals = counselingService.getReferralsByDateRange(startDate, endDate);
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/{id}")
    public ResponseEntity<CounselingReferral> getReferralById(@PathVariable Long id) {
        return counselingReferralRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/referrals")
    public ResponseEntity<CounselingReferral> createReferral(@RequestBody CounselingReferral referral) {
        CounselingReferral created = counselingService.createReferral(referral);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/referrals/{id}")
    public ResponseEntity<CounselingReferral> updateReferral(
            @PathVariable Long id,
            @RequestBody CounselingReferral referral) {
        referral.setId(id);
        CounselingReferral updated = counselingService.updateReferral(referral);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/referrals/{id}")
    public ResponseEntity<Void> deleteReferral(@PathVariable Long id) {
        counselingService.deleteReferral(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== College Application Operations ====================

    @GetMapping("/college/upcoming-deadlines")
    public ResponseEntity<List<CollegeApplication>> getUpcomingDeadlines(
            @RequestParam(defaultValue = "14") int daysAhead) {
        List<CollegeApplication> applications = counselingService.getUpcomingDeadlines(daysAhead);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/overdue")
    public ResponseEntity<List<CollegeApplication>> getOverdueApplications() {
        List<CollegeApplication> applications = counselingService.getOverdueApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/missing-requirements")
    public ResponseEntity<List<CollegeApplication>> getApplicationsWithMissingRequirements() {
        List<CollegeApplication> applications = counselingService.getApplicationsWithMissingRequirements();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/needing-decision-response")
    public ResponseEntity<List<CollegeApplication>> getApplicationsNeedingDecisionResponse() {
        List<CollegeApplication> applications = counselingService.getApplicationsNeedingDecisionResponse();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/needing-fafsa")
    public ResponseEntity<List<CollegeApplication>> getApplicationsNeedingFAFSA() {
        List<CollegeApplication> applications = counselingService.getApplicationsNeedingFAFSA();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/student/{studentId}")
    public ResponseEntity<List<CollegeApplication>> getCollegeApplicationsForStudent(@PathVariable Long studentId) {
        List<CollegeApplication> applications = counselingService.getCollegeApplicationsForStudent(studentId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/student/{studentId}/accepted")
    public ResponseEntity<List<CollegeApplication>> getAcceptedApplicationsForStudent(@PathVariable Long studentId) {
        List<CollegeApplication> applications = counselingService.getAcceptedApplicationsForStudent(studentId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/college/{id}")
    public ResponseEntity<CollegeApplication> getCollegeApplicationById(@PathVariable Long id) {
        return collegeApplicationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/college")
    public ResponseEntity<CollegeApplication> createCollegeApplication(@RequestBody CollegeApplication application) {
        CollegeApplication created = collegeApplicationRepository.save(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/college/{id}")
    public ResponseEntity<CollegeApplication> updateCollegeApplication(
            @PathVariable Long id,
            @RequestBody CollegeApplication application) {
        application.setId(id);
        CollegeApplication updated = collegeApplicationRepository.save(application);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/college/{id}")
    public ResponseEntity<Void> deleteCollegeApplication(@PathVariable Long id) {
        collegeApplicationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Social Work Case Operations ====================

    @GetMapping("/social-work/high-priority")
    public ResponseEntity<List<SocialWorkCase>> getHighPriorityCases() {
        List<SocialWorkCase> cases = counselingService.getHighPriorityCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/mckinney-vento")
    public ResponseEntity<List<SocialWorkCase>> getMckinneyVentoCases() {
        List<SocialWorkCase> cases = counselingService.getMckinneyVentoCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/foster-care")
    public ResponseEntity<List<SocialWorkCase>> getFosterCareCases() {
        List<SocialWorkCase> cases = counselingService.getFosterCareCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/cps")
    public ResponseEntity<List<SocialWorkCase>> getCPSCases() {
        List<SocialWorkCase> cases = counselingService.getCPSCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/needing-home-visit")
    public ResponseEntity<List<SocialWorkCase>> getCasesNeedingHomeVisit() {
        List<SocialWorkCase> cases = counselingService.getCasesNeedingHomeVisit();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/needing-followup")
    public ResponseEntity<List<SocialWorkCase>> getCasesNeedingFollowUp() {
        List<SocialWorkCase> cases = counselingService.getCasesNeedingFollowUp();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/student/{studentId}")
    public ResponseEntity<List<SocialWorkCase>> getCasesForStudent(@PathVariable Long studentId) {
        List<SocialWorkCase> cases = counselingService.getCasesForStudent(studentId);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/social-work/{id}")
    public ResponseEntity<SocialWorkCase> getSocialWorkCaseById(@PathVariable Long id) {
        return socialWorkCaseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/social-work")
    public ResponseEntity<SocialWorkCase> createSocialWorkCase(@RequestBody SocialWorkCase socialWorkCase) {
        SocialWorkCase created = counselingService.saveSocialWorkCase(socialWorkCase);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/social-work/{id}")
    public ResponseEntity<SocialWorkCase> updateSocialWorkCase(
            @PathVariable Long id,
            @RequestBody SocialWorkCase socialWorkCase) {
        socialWorkCase.setId(id);
        SocialWorkCase updated = counselingService.saveSocialWorkCase(socialWorkCase);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/social-work/{id}")
    public ResponseEntity<Void> deleteSocialWorkCase(@PathVariable Long id) {
        socialWorkCaseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Crisis Intervention Operations ====================

    @GetMapping("/crisis/high-risk")
    public ResponseEntity<List<CrisisIntervention>> getHighRiskCrises() {
        List<CrisisIntervention> crises = counselingService.getHighRiskCrises();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/suicide-related")
    public ResponseEntity<List<CrisisIntervention>> getSuicideRelatedCrises() {
        List<CrisisIntervention> crises = counselingService.getSuicideRelatedCrises();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/violence-related")
    public ResponseEntity<List<CrisisIntervention>> getViolenceRelatedCrises() {
        List<CrisisIntervention> crises = counselingService.getViolenceRelatedCrises();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/needing-parent-notification")
    public ResponseEntity<List<CrisisIntervention>> getCrisesNeedingParentNotification() {
        List<CrisisIntervention> crises = counselingService.getCrisesNeedingParentNotification();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/needing-followup")
    public ResponseEntity<List<CrisisIntervention>> getCrisesNeedingFollowUp() {
        List<CrisisIntervention> crises = counselingService.getCrisesNeedingFollowUp();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/needing-clearance")
    public ResponseEntity<List<CrisisIntervention>> getCrisesNeedingClearance() {
        List<CrisisIntervention> crises = counselingService.getCrisesNeedingClearance();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/student/{studentId}")
    public ResponseEntity<List<CrisisIntervention>> getCrisisInterventionsForStudent(@PathVariable Long studentId) {
        List<CrisisIntervention> crises = counselingService.getCrisisInterventionsForStudent(studentId);
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/recent")
    public ResponseEntity<List<CrisisIntervention>> getRecentCrises() {
        List<CrisisIntervention> crises = counselingService.getRecentCrises();
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/crisis/{id}")
    public ResponseEntity<CrisisIntervention> getCrisisInterventionById(@PathVariable Long id) {
        return crisisInterventionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/crisis")
    public ResponseEntity<CrisisIntervention> createCrisisIntervention(@RequestBody CrisisIntervention crisis) {
        CrisisIntervention created = crisisInterventionRepository.save(crisis);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/crisis/{id}")
    public ResponseEntity<CrisisIntervention> updateCrisisIntervention(
            @PathVariable Long id,
            @RequestBody CrisisIntervention crisis) {
        crisis.setId(id);
        CrisisIntervention updated = crisisInterventionRepository.save(crisis);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/crisis/{id}")
    public ResponseEntity<Void> deleteCrisisIntervention(@PathVariable Long id) {
        crisisInterventionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Dashboard and Alerts ====================

    @GetMapping("/alerts")
    public ResponseEntity<CounselingAlerts> getCounselingAlerts() {
        CounselingAlerts alerts = counselingService.getCounselingAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        CounselingAlerts alerts = counselingService.getCounselingAlerts();

        List<CounselingSession> highRiskSessions = counselingService.getHighRiskSessions();
        List<CounselingReferral> urgentReferrals = counselingService.getUrgentReferrals();
        List<CrisisIntervention> recentCrises = counselingService.getRecentCrises();
        List<CollegeApplication> upcomingDeadlines = counselingService.getUpcomingDeadlines(14);
        List<SocialWorkCase> highPriorityCases = counselingService.getHighPriorityCases();

        dashboard.put("alerts", alerts);
        dashboard.put("highRiskSessions", highRiskSessions);
        dashboard.put("urgentReferrals", urgentReferrals);
        dashboard.put("recentCrises", recentCrises);
        dashboard.put("upcomingCollegeDeadlines", upcomingDeadlines);
        dashboard.put("highPrioritySocialWorkCases", highPriorityCases);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Object> dashboard = new HashMap<>();

        List<CounselingSession> sessions = counselingService.getSessionsForStudent(studentId);
        List<CounselingReferral> referrals = counselingService.getReferralsForStudent(studentId);
        List<CollegeApplication> applications = counselingService.getCollegeApplicationsForStudent(studentId);
        List<SocialWorkCase> cases = counselingService.getCasesForStudent(studentId);
        List<CrisisIntervention> crises = counselingService.getCrisisInterventionsForStudent(studentId);

        dashboard.put("studentId", studentId);
        dashboard.put("studentName", student.getFirstName() + " " + student.getLastName());
        dashboard.put("sessions", sessions);
        dashboard.put("sessionsCount", sessions.size());
        dashboard.put("referrals", referrals);
        dashboard.put("referralsCount", referrals.size());
        dashboard.put("collegeApplications", applications);
        dashboard.put("applicationsCount", applications.size());
        dashboard.put("socialWorkCases", cases);
        dashboard.put("casesCount", cases.size());
        dashboard.put("crisisInterventions", crises);
        dashboard.put("crisesCount", crises.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/crisis")
    public ResponseEntity<Map<String, Object>> getCrisisDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<CrisisIntervention> highRisk = counselingService.getHighRiskCrises();
        List<CrisisIntervention> suicideRelated = counselingService.getSuicideRelatedCrises();
        List<CrisisIntervention> violenceRelated = counselingService.getViolenceRelatedCrises();
        List<CrisisIntervention> needingNotification = counselingService.getCrisesNeedingParentNotification();
        List<CrisisIntervention> needingFollowUp = counselingService.getCrisesNeedingFollowUp();
        List<CrisisIntervention> needingClearance = counselingService.getCrisesNeedingClearance();
        List<CrisisIntervention> recent = counselingService.getRecentCrises();

        dashboard.put("highRiskCrises", highRisk);
        dashboard.put("highRiskCount", highRisk.size());
        dashboard.put("suicideRelated", suicideRelated);
        dashboard.put("suicideRelatedCount", suicideRelated.size());
        dashboard.put("violenceRelated", violenceRelated);
        dashboard.put("violenceRelatedCount", violenceRelated.size());
        dashboard.put("needingParentNotification", needingNotification);
        dashboard.put("needingNotificationCount", needingNotification.size());
        dashboard.put("needingFollowUp", needingFollowUp);
        dashboard.put("needingFollowUpCount", needingFollowUp.size());
        dashboard.put("needingClearance", needingClearance);
        dashboard.put("needingClearanceCount", needingClearance.size());
        dashboard.put("recentCrises", recent);
        dashboard.put("recentCount", recent.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/college")
    public ResponseEntity<Map<String, Object>> getCollegeDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<CollegeApplication> upcoming = counselingService.getUpcomingDeadlines(14);
        List<CollegeApplication> overdue = counselingService.getOverdueApplications();
        List<CollegeApplication> missingReqs = counselingService.getApplicationsWithMissingRequirements();
        List<CollegeApplication> needingDecision = counselingService.getApplicationsNeedingDecisionResponse();
        List<CollegeApplication> needingFafsa = counselingService.getApplicationsNeedingFAFSA();

        dashboard.put("upcomingDeadlines", upcoming);
        dashboard.put("upcomingCount", upcoming.size());
        dashboard.put("overdueApplications", overdue);
        dashboard.put("overdueCount", overdue.size());
        dashboard.put("missingRequirements", missingReqs);
        dashboard.put("missingReqsCount", missingReqs.size());
        dashboard.put("needingDecisionResponse", needingDecision);
        dashboard.put("needingDecisionCount", needingDecision.size());
        dashboard.put("needingFAFSA", needingFafsa);
        dashboard.put("needingFafsaCount", needingFafsa.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/social-work")
    public ResponseEntity<Map<String, Object>> getSocialWorkDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<SocialWorkCase> highPriority = counselingService.getHighPriorityCases();
        List<SocialWorkCase> mckinneyVento = counselingService.getMckinneyVentoCases();
        List<SocialWorkCase> fosterCare = counselingService.getFosterCareCases();
        List<SocialWorkCase> cps = counselingService.getCPSCases();
        List<SocialWorkCase> needingHomeVisit = counselingService.getCasesNeedingHomeVisit();
        List<SocialWorkCase> needingFollowUp = counselingService.getCasesNeedingFollowUp();

        dashboard.put("highPriorityCases", highPriority);
        dashboard.put("highPriorityCount", highPriority.size());
        dashboard.put("mckinneyVentoCases", mckinneyVento);
        dashboard.put("mckinneyVentoCount", mckinneyVento.size());
        dashboard.put("fosterCareCases", fosterCare);
        dashboard.put("fosterCareCount", fosterCare.size());
        dashboard.put("cpsCases", cps);
        dashboard.put("cpsCount", cps.size());
        dashboard.put("needingHomeVisit", needingHomeVisit);
        dashboard.put("needingHomeVisitCount", needingHomeVisit.size());
        dashboard.put("needingFollowUp", needingFollowUp);
        dashboard.put("needingFollowUpCount", needingFollowUp.size());

        return ResponseEntity.ok(dashboard);
    }
}
