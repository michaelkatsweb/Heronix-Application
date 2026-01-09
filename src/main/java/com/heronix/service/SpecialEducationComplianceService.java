package com.heronix.service;

import com.heronix.model.domain.IEP;
import com.heronix.model.domain.IEPMeeting;
import com.heronix.model.domain.SpecialEducationEvaluation;
import com.heronix.repository.IEPMeetingRepository;
import com.heronix.repository.IEPRepository;
import com.heronix.repository.SpecialEducationEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Special Education compliance tracking and reporting
 * Handles IDEA compliance, deadlines, and alert generation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class SpecialEducationComplianceService {

    @Autowired
    private IEPRepository iepRepository;

    @Autowired
    private IEPMeetingRepository meetingRepository;

    @Autowired
    private SpecialEducationEvaluationRepository evaluationRepository;

    // IEP Compliance
    public List<IEP> getIEPsNeedingAnnualReview(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        return iepRepository.findIEPsNeedingRenewal(today, thresholdDate);
    }

    public List<IEP> getExpiredIEPs() {
        return iepRepository.findExpiredIEPs(LocalDate.now());
    }

    public List<IEP> getIEPsWithReviewDue() {
        return iepRepository.findIEPsWithReviewDue(LocalDate.now());
    }

    // Evaluation Compliance (60-Day Timeline)
    public List<SpecialEducationEvaluation> getOverdueEvaluations() {
        return evaluationRepository.findOverdue(LocalDate.now());
    }

    public List<SpecialEducationEvaluation> getEvaluationsDueSoon(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysThreshold);
        return evaluationRepository.findDueSoon(today, futureDate);
    }

    public List<SpecialEducationEvaluation> getEvaluationsAwaitingConsent() {
        return evaluationRepository.findAwaitingConsent();
    }

    public List<SpecialEducationEvaluation> getEvaluationsOverdueForConsent(int daysWithoutConsent) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysWithoutConsent);
        return evaluationRepository.findOverdueForConsent(cutoffDate);
    }

    // Meeting Compliance
    public List<IEPMeeting> getOverdueAnnualReviews() {
        return meetingRepository.findOverdueAnnualReviews(LocalDate.now());
    }

    public List<IEPMeeting> getOverdueTriennials() {
        return meetingRepository.findOverdueTriennials(LocalDate.now());
    }

    public List<IEPMeeting> getMeetingsNeedingParentInvitation(int daysBeforeMeeting) {
        LocalDate cutoffDate = LocalDate.now().plusDays(daysBeforeMeeting);
        return meetingRepository.findNeedingParentInvitation(cutoffDate);
    }

    public List<IEPMeeting> getMeetingsNeedingPWN() {
        return meetingRepository.findNeedingPWN();
    }

    // Triennial Tracking
    public List<SpecialEducationEvaluation> getUpcomingTriennials(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return evaluationRepository.findUpcomingTriennials(today, futureDate);
    }

    public List<SpecialEducationEvaluation> getOverdueTriennialEvaluations() {
        return evaluationRepository.findOverdueTriennials(LocalDate.now());
    }

    // Compliance Alerts
    public Map<String, List<?>> getAllComplianceAlerts() {
        Map<String, List<?>> alerts = new HashMap<>();

        alerts.put("expiredIEPs", getExpiredIEPs());
        alerts.put("overdueAnnualReviews", getOverdueAnnualReviews());
        alerts.put("overdueTriennials", getOverdueTriennials());
        alerts.put("overdueEvaluations", getOverdueEvaluations());
        alerts.put("evaluationsAwaitingConsent", getEvaluationsAwaitingConsent());
        alerts.put("meetingsNeedingPWN", getMeetingsNeedingPWN());

        return alerts;
    }

    public List<String> getComplianceAlertSummary() {
        List<String> summary = new ArrayList<>();

        long expiredCount = getExpiredIEPs().size();
        if (expiredCount > 0) {
            summary.add(expiredCount + " expired IEP(s)");
        }

        long overdueReviews = getOverdueAnnualReviews().size();
        if (overdueReviews > 0) {
            summary.add(overdueReviews + " overdue annual review(s)");
        }

        long overdueTriennials = getOverdueTriennials().size();
        if (overdueTriennials > 0) {
            summary.add(overdueTriennials + " overdue triennial(s)");
        }

        long overdueEvals = getOverdueEvaluations().size();
        if (overdueEvals > 0) {
            summary.add(overdueEvals + " overdue evaluation(s)");
        }

        long awaitingConsent = getEvaluationsAwaitingConsent().size();
        if (awaitingConsent > 0) {
            summary.add(awaitingConsent + " evaluation(s) awaiting consent");
        }

        return summary;
    }

    // Reporting
    public Map<String, Object> generateComplianceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // IEP Statistics
        long activeIEPs = iepRepository.countActiveIEPs(LocalDate.now());
        report.put("activeIEPCount", activeIEPs);
        report.put("expiredIEPCount", getExpiredIEPs().size());

        // Evaluation Statistics
        long activeEvals = evaluationRepository.countActive();
        long overdueEvals = evaluationRepository.countOverdue(LocalDate.now());
        report.put("activeEvaluationCount", activeEvals);
        report.put("overdueEvaluationCount", overdueEvals);

        // Meeting Statistics
        List<IEPMeeting> completedMeetings = meetingRepository.findByDateRange(startDate, endDate);
        report.put("meetingsHeld", completedMeetings.size());

        // Compliance Percentages
        if (activeIEPs > 0) {
            double complianceRate = ((activeIEPs - getExpiredIEPs().size()) / (double) activeIEPs) * 100.0;
            report.put("iepComplianceRate", complianceRate);
        }

        if (activeEvals > 0) {
            double evalComplianceRate = ((activeEvals - overdueEvals) / (double) activeEvals) * 100.0;
            report.put("evaluationComplianceRate", evalComplianceRate);
        }

        // Alerts
        report.put("totalAlerts", getComplianceAlertSummary().size());
        report.put("alerts", getComplianceAlertSummary());

        return report;
    }

    // Child Count (for state reporting)
    public Long getActiveIEPCount() {
        return iepRepository.countActiveIEPs(LocalDate.now());
    }

    public Map<String, Long> getIEPCountByEligibilityCategory() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = iepRepository.countByEligibilityCategory();

        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            counts.put(category, count);
        }

        return counts;
    }

    // Service Delivery Tracking
    public List<IEP> getIEPsWithUnscheduledServices() {
        return iepRepository.findIEPsWithUnscheduledServices();
    }

    // Automatic Compliance Updates
    public int expireOutdatedIEPs() {
        log.info("Running automatic IEP expiration check");
        List<IEP> expiredIEPs = getExpiredIEPs();

        for (IEP iep : expiredIEPs) {
            iep.setStatus(com.heronix.model.enums.IEPStatus.EXPIRED);
            iepRepository.save(iep);
        }

        log.info("Expired {} IEPs", expiredIEPs.size());
        return expiredIEPs.size();
    }

    public void updateEvaluationTimelines() {
        log.info("Updating evaluation timelines");
        List<SpecialEducationEvaluation> activeEvals = evaluationRepository.findAllActive();

        for (SpecialEducationEvaluation eval : activeEvals) {
            eval.updateTimelineDaysRemaining();
            if (eval.isOverdue() && eval.getStatus() != SpecialEducationEvaluation.EvaluationStatus.OVERDUE) {
                eval.setStatus(SpecialEducationEvaluation.EvaluationStatus.OVERDUE);
            }
            evaluationRepository.save(eval);
        }

        log.info("Updated {} evaluation timelines", activeEvals.size());
    }
}
