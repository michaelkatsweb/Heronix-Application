package com.heronix.service;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import com.heronix.repository.BehaviorIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing disciplinary processes and workflows.
 *
 * Handles:
 * - Referral workflows (teacher to admin)
 * - Suspension tracking and recommendations
 * - Disciplinary action history
 * - Progressive discipline enforcement
 * - Intervention tracking
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Discipline/Behavior Management System
 */
@Service
public class DisciplineManagementService {

    private static final Logger log = LoggerFactory.getLogger(DisciplineManagementService.class);

    @Autowired
    private BehaviorIncidentRepository behaviorIncidentRepository;

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    // Progressive discipline thresholds
    private static final int MINOR_THRESHOLD_FOR_REFERRAL = 3;
    private static final int MODERATE_THRESHOLD_FOR_REFERRAL = 2;
    private static final int MAJOR_THRESHOLD_FOR_SUSPENSION = 1;
    private static final int DAYS_LOOKBACK_FOR_PATTERN = 30;

    // ========================================================================
    // REFERRAL MANAGEMENT
    // ========================================================================

    /**
     * Creates an administrative referral for a behavior incident.
     *
     * @param incidentId the incident ID
     * @param referralReason detailed reason for referral
     * @param requestingTeacher teacher making the referral
     * @return updated incident with referral marked
     */
    @Transactional
    public BehaviorIncident createAdminReferral(
            Long incidentId,
            String referralReason,
            Teacher requestingTeacher) {

        log.info("Creating admin referral for incident ID {} by teacher ID {}",
                incidentId, requestingTeacher.getId());

        BehaviorIncident incident = behaviorIncidentService.getIncidentById(incidentId);

        // Mark as requiring referral
        incident.setAdminReferralRequired(true);

        // Record intervention as "Referred to administration"
        String intervention = String.format(
                "Administrative referral created by %s %s. Reason: %s",
                requestingTeacher.getFirstName(),
                requestingTeacher.getLastName(),
                referralReason
        );
        incident.setInterventionApplied(intervention);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Admin referral created for incident ID {}", incidentId);

        return incident;
    }

    /**
     * Processes an admin referral and records the outcome.
     *
     * @param incidentId the incident ID
     * @param outcome description of disciplinary action taken
     * @param actionDate date action was taken
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident processReferral(
            Long incidentId,
            String outcome,
            LocalDate actionDate) {

        log.info("Processing referral for incident ID {}", incidentId);

        BehaviorIncident incident = behaviorIncidentService.getIncidentById(incidentId);

        if (!incident.getAdminReferralRequired()) {
            throw new IllegalStateException("Incident " + incidentId + " is not marked as requiring referral");
        }

        incident.setReferralOutcome(outcome);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Referral processed for incident ID {} with outcome: {}", incidentId, outcome);

        return incident;
    }

    /**
     * Gets all pending referrals (requiring admin action).
     *
     * @return list of incidents awaiting admin processing
     */
    public List<BehaviorIncident> getPendingReferrals() {
        log.debug("Fetching all pending admin referrals");

        return behaviorIncidentRepository.findAll().stream()
                .filter(i -> i.getAdminReferralRequired() && i.getReferralOutcome() == null)
                .sorted(Comparator.comparing(BehaviorIncident::getIncidentDate)
                        .thenComparing(BehaviorIncident::getIncidentTime))
                .collect(Collectors.toList());
    }

    /**
     * Gets pending referrals for a specific student.
     *
     * @param student the student
     * @return list of pending referrals
     */
    public List<BehaviorIncident> getPendingReferralsForStudent(Student student) {
        log.debug("Fetching pending referrals for student ID {}", student.getId());

        return behaviorIncidentService.getIncidentsRequiringReferral(student).stream()
                .filter(i -> i.getReferralOutcome() == null)
                .sorted(Comparator.comparing(BehaviorIncident::getIncidentDate)
                        .thenComparing(BehaviorIncident::getIncidentTime))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // SUSPENSION RECOMMENDATIONS
    // ========================================================================

    /**
     * Analyzes student behavior and recommends disciplinary action level.
     *
     * @param student the student
     * @param currentIncident the current incident being evaluated
     * @return recommended action
     */
    public DisciplinaryRecommendation analyzeDisciplinaryAction(
            Student student,
            BehaviorIncident currentIncident) {

        log.info("Analyzing disciplinary action for student ID {} based on incident ID {}",
                student.getId(), currentIncident.getId());

        LocalDate lookbackDate = LocalDate.now().minusDays(DAYS_LOOKBACK_FOR_PATTERN);
        List<BehaviorIncident> recentIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, lookbackDate, LocalDate.now())
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Count by severity
        long minorCount = recentIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MINOR).count();
        long moderateCount = recentIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MODERATE).count();
        long majorCount = recentIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR).count();

        DisciplinaryRecommendation recommendation = new DisciplinaryRecommendation();
        recommendation.student = student;
        recommendation.currentIncident = currentIncident;
        recommendation.recentMinorIncidents = (int) minorCount;
        recommendation.recentModerateIncidents = (int) moderateCount;
        recommendation.recentMajorIncidents = (int) majorCount;

        // Determine recommended action based on progressive discipline model
        if (currentIncident.getSeverityLevel() == SeverityLevel.MAJOR || majorCount >= MAJOR_THRESHOLD_FOR_SUSPENSION) {
            recommendation.recommendedAction = DisciplinaryAction.SUSPENSION;
            recommendation.recommendationReason = String.format(
                    "Student has %d major incident(s) in past %d days. Suspension recommended.",
                    majorCount, DAYS_LOOKBACK_FOR_PATTERN
            );
        } else if (moderateCount >= MODERATE_THRESHOLD_FOR_REFERRAL) {
            recommendation.recommendedAction = DisciplinaryAction.ADMIN_CONFERENCE;
            recommendation.recommendationReason = String.format(
                    "Student has %d moderate incident(s) in past %d days. Administrative conference recommended.",
                    moderateCount, DAYS_LOOKBACK_FOR_PATTERN
            );
        } else if (minorCount >= MINOR_THRESHOLD_FOR_REFERRAL) {
            recommendation.recommendedAction = DisciplinaryAction.DETENTION;
            recommendation.recommendationReason = String.format(
                    "Student has %d minor incident(s) in past %d days. Detention recommended.",
                    minorCount, DAYS_LOOKBACK_FOR_PATTERN
            );
        } else {
            recommendation.recommendedAction = DisciplinaryAction.WARNING;
            recommendation.recommendationReason = "First or isolated incident. Verbal warning recommended.";
        }

        log.info("Recommendation for student ID {}: {} - {}",
                student.getId(), recommendation.recommendedAction, recommendation.recommendationReason);

        return recommendation;
    }

    /**
     * Checks if a student is at risk for suspension based on recent behavior.
     *
     * @param student the student
     * @return true if at risk for suspension
     */
    public boolean isAtRiskForSuspension(Student student) {
        LocalDate lookbackDate = LocalDate.now().minusDays(DAYS_LOOKBACK_FOR_PATTERN);

        List<BehaviorIncident> recentIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, lookbackDate, LocalDate.now())
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        long majorCount = recentIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR).count();

        long moderateCount = recentIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MODERATE).count();

        return majorCount >= MAJOR_THRESHOLD_FOR_SUSPENSION ||
               moderateCount >= (MODERATE_THRESHOLD_FOR_REFERRAL * 2);
    }

    // ========================================================================
    // INTERVENTION TRACKING
    // ========================================================================

    /**
     * Records an intervention applied to a student.
     *
     * @param incidentId the incident ID
     * @param interventionType type of intervention
     * @param interventionDetails detailed description
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident recordIntervention(
            Long incidentId,
            InterventionType interventionType,
            String interventionDetails) {

        log.info("Recording {} intervention for incident ID {}", interventionType, incidentId);

        BehaviorIncident incident = behaviorIncidentService.getIncidentById(incidentId);

        String intervention = String.format(
                "%s - %s (Recorded: %s)",
                interventionType.getDisplayName(),
                interventionDetails,
                LocalDate.now()
        );

        incident.setInterventionApplied(intervention);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Intervention recorded for incident ID {}", incidentId);

        return incident;
    }

    /**
     * Gets intervention history for a student.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return list of incidents with interventions
     */
    public List<BehaviorIncident> getInterventionHistory(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching intervention history for student ID {}", student.getId());

        return behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, startDate, endDate)
                .stream()
                .filter(i -> i.getInterventionApplied() != null && !i.getInterventionApplied().isEmpty())
                .sorted(Comparator.comparing(BehaviorIncident::getIncidentDate)
                        .thenComparing(BehaviorIncident::getIncidentTime)
                        .reversed())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // PATTERN DETECTION
    // ========================================================================

    /**
     * Detects behavior patterns for early intervention.
     *
     * @param student the student
     * @return pattern analysis
     */
    public BehaviorPattern detectBehaviorPattern(Student student) {
        log.info("Detecting behavior patterns for student ID {}", student.getId());

        LocalDate lookbackDate = LocalDate.now().minusDays(DAYS_LOOKBACK_FOR_PATTERN);
        List<BehaviorIncident> recentIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, lookbackDate, LocalDate.now())
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        BehaviorPattern pattern = new BehaviorPattern();
        pattern.student = student;
        pattern.analysisStartDate = lookbackDate;
        pattern.analysisEndDate = LocalDate.now();
        pattern.totalIncidents = recentIncidents.size();

        if (recentIncidents.isEmpty()) {
            pattern.patternDetected = false;
            pattern.patternDescription = "No negative incidents in recent period.";
            return pattern;
        }

        // Detect escalating severity
        List<BehaviorIncident> sortedByDate = recentIncidents.stream()
                .sorted(Comparator.comparing(BehaviorIncident::getIncidentDate))
                .collect(Collectors.toList());

        boolean escalating = false;
        if (sortedByDate.size() >= 3) {
            SeverityLevel firstSeverity = sortedByDate.get(0).getSeverityLevel();
            SeverityLevel lastSeverity = sortedByDate.get(sortedByDate.size() - 1).getSeverityLevel();

            if (firstSeverity != null && lastSeverity != null) {
                escalating = lastSeverity.ordinal() > firstSeverity.ordinal();
            }
        }

        // Check for clustering (multiple incidents in short time)
        LocalDate weekAgoDate = LocalDate.now().minusDays(7);
        long incidentsLastWeek = recentIncidents.stream()
                .filter(i -> i.getIncidentDate().isAfter(weekAgoDate))
                .count();

        boolean clustering = incidentsLastWeek >= 3;

        pattern.patternDetected = escalating || clustering;

        if (escalating) {
            pattern.patternDescription = "Escalating severity detected. Recent incidents show increasing severity levels.";
        } else if (clustering) {
            pattern.patternDescription = String.format(
                    "Clustering detected. %d incidents in the past 7 days.",
                    incidentsLastWeek
            );
        } else {
            pattern.patternDescription = String.format(
                    "%d incidents detected but no concerning pattern identified.",
                    recentIncidents.size()
            );
        }

        log.info("Pattern analysis complete for student ID {}: {}", student.getId(), pattern.patternDescription);

        return pattern;
    }

    // ========================================================================
    // ENUMS AND DTOs
    // ========================================================================

    /**
     * Disciplinary action types.
     */
    public enum DisciplinaryAction {
        WARNING("Verbal Warning"),
        DETENTION("Detention"),
        ADMIN_CONFERENCE("Administrative Conference"),
        IN_SCHOOL_SUSPENSION("In-School Suspension"),
        SUSPENSION("Out-of-School Suspension"),
        EXPULSION("Expulsion Recommendation");

        private final String displayName;

        DisciplinaryAction(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Intervention types.
     */
    public enum InterventionType {
        COUNSELING("Counseling Session"),
        BEHAVIOR_CONTRACT("Behavior Contract"),
        PARENT_CONFERENCE("Parent Conference"),
        PEER_MEDIATION("Peer Mediation"),
        MENTORING("Mentoring Program"),
        SKILLS_TRAINING("Social Skills Training"),
        RESTORATIVE_JUSTICE("Restorative Justice Conference"),
        OTHER("Other Intervention");

        private final String displayName;

        InterventionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Disciplinary recommendation DTO.
     */
    public static class DisciplinaryRecommendation {
        public Student student;
        public BehaviorIncident currentIncident;
        public int recentMinorIncidents;
        public int recentModerateIncidents;
        public int recentMajorIncidents;
        public DisciplinaryAction recommendedAction;
        public String recommendationReason;

        public Student getStudent() { return student; }
        public BehaviorIncident getCurrentIncident() { return currentIncident; }
        public int getRecentMinorIncidents() { return recentMinorIncidents; }
        public int getRecentModerateIncidents() { return recentModerateIncidents; }
        public int getRecentMajorIncidents() { return recentMajorIncidents; }
        public DisciplinaryAction getRecommendedAction() { return recommendedAction; }
        public String getRecommendationReason() { return recommendationReason; }
    }

    /**
     * Behavior pattern analysis DTO.
     */
    public static class BehaviorPattern {
        public Student student;
        public LocalDate analysisStartDate;
        public LocalDate analysisEndDate;
        public int totalIncidents;
        public boolean patternDetected;
        public String patternDescription;

        public Student getStudent() { return student; }
        public LocalDate getAnalysisStartDate() { return analysisStartDate; }
        public LocalDate getAnalysisEndDate() { return analysisEndDate; }
        public int getTotalIncidents() { return totalIncidents; }
        public boolean isPatternDetected() { return patternDetected; }
        public String getPatternDescription() { return patternDescription; }
    }
}
