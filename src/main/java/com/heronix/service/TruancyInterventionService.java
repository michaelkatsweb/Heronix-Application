package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Truancy Intervention Service
 *
 * Manages truancy cases, interventions, and court referrals.
 * Tracks intervention effectiveness and case outcomes.
 *
 * Key Responsibilities:
 * - Create and manage truancy cases
 * - Track intervention strategies and outcomes
 * - Generate court referral documentation
 * - Monitor intervention plan compliance
 * - Coordinate with truancy officers and courts
 * - Track case resolution and student progress
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Service
public class TruancyInterventionService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceDocumentService documentService;

    @Autowired
    private AttendanceNotificationService notificationService;

    // In-memory storage (would be database tables in production)
    private final Map<String, TruancyCase> truancyCases = new HashMap<>();
    private final Map<String, CourtReferral> courtReferrals = new HashMap<>();

    // ========================================================================
    // TRUANCY CASE MANAGEMENT
    // ========================================================================

    /**
     * Open new truancy case for student
     */
    public TruancyCase openTruancyCase(Long studentId, int unexcusedAbsences, String reason) {
        log.info("Opening truancy case for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        String caseId = generateCaseId(studentId);

        TruancySeverity severity = determineSeverity(unexcusedAbsences);

        TruancyCase truancyCase = TruancyCase.builder()
                .caseId(caseId)
                .studentId(studentId)
                .studentName(student.getFullName())
                .gradeLevel(student.getGradeLevel())
                .openedDate(LocalDate.now())
                .unexcusedAbsences(unexcusedAbsences)
                .severity(severity)
                .status("OPEN")
                .reason(reason)
                .assignedCaseWorker("TBD")
                .interventions(new ArrayList<>())
                .notes(new ArrayList<>())
                .build();

        truancyCases.put(caseId, truancyCase);

        log.info("Truancy case {} opened for student {}", caseId, studentId);
        return truancyCase;
    }

    /**
     * Add intervention to truancy case
     */
    public TruancyCase addIntervention(String caseId, InterventionType interventionType, String description, String assignedTo) {
        TruancyCase truancyCase = truancyCases.get(caseId);
        if (truancyCase == null) {
            throw new IllegalArgumentException("Truancy case not found: " + caseId);
        }

        Intervention intervention = Intervention.builder()
                .interventionId(UUID.randomUUID().toString())
                .interventionType(interventionType)
                .description(description)
                .assignedTo(assignedTo)
                .startDate(LocalDate.now())
                .status("IN_PROGRESS")
                .build();

        truancyCase.getInterventions().add(intervention);
        truancyCase.setLastUpdated(LocalDateTime.now());

        log.info("Added {} intervention to case {}", interventionType, caseId);
        return truancyCase;
    }

    /**
     * Update intervention status
     */
    public TruancyCase updateInterventionStatus(String caseId, String interventionId, String status, String outcome) {
        TruancyCase truancyCase = truancyCases.get(caseId);
        if (truancyCase == null) {
            throw new IllegalArgumentException("Truancy case not found: " + caseId);
        }

        Optional<Intervention> intervention = truancyCase.getInterventions().stream()
                .filter(i -> i.getInterventionId().equals(interventionId))
                .findFirst();

        if (intervention.isPresent()) {
            Intervention i = intervention.get();
            i.setStatus(status);
            i.setOutcome(outcome);
            i.setCompletedDate(LocalDate.now());
            truancyCase.setLastUpdated(LocalDateTime.now());

            log.info("Updated intervention {} status to {}", interventionId, status);
        }

        return truancyCase;
    }

    /**
     * Add case note
     */
    public TruancyCase addCaseNote(String caseId, String note, String addedBy) {
        TruancyCase truancyCase = truancyCases.get(caseId);
        if (truancyCase == null) {
            throw new IllegalArgumentException("Truancy case not found: " + caseId);
        }

        CaseNote caseNote = CaseNote.builder()
                .noteId(UUID.randomUUID().toString())
                .note(note)
                .addedBy(addedBy)
                .addedDate(LocalDateTime.now())
                .build();

        truancyCase.getNotes().add(caseNote);
        truancyCase.setLastUpdated(LocalDateTime.now());

        log.info("Added note to case {}", caseId);
        return truancyCase;
    }

    /**
     * Close truancy case
     */
    public TruancyCase closeTruancyCase(String caseId, String resolution, String closedBy) {
        TruancyCase truancyCase = truancyCases.get(caseId);
        if (truancyCase == null) {
            throw new IllegalArgumentException("Truancy case not found: " + caseId);
        }

        truancyCase.setStatus("CLOSED");
        truancyCase.setClosedDate(LocalDate.now());
        truancyCase.setResolution(resolution);
        truancyCase.setClosedBy(closedBy);
        truancyCase.setLastUpdated(LocalDateTime.now());

        log.info("Closed truancy case {} with resolution: {}", caseId, resolution);
        return truancyCase;
    }

    // ========================================================================
    // COURT REFERRALS
    // ========================================================================

    /**
     * Generate court referral for truancy case
     */
    public CourtReferral generateCourtReferral(String caseId, String courtType) {
        TruancyCase truancyCase = truancyCases.get(caseId);
        if (truancyCase == null) {
            throw new IllegalArgumentException("Truancy case not found: " + caseId);
        }

        String referralId = "CR-" + caseId + "-" + System.currentTimeMillis();

        Student student = studentRepository.findById(truancyCase.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Generate final truancy notice
        AttendanceDocumentService.TruancyLetter finalNotice = documentService.generateTruancyFinalNotice(
                student.getId(),
                LocalDate.now().minusDays(90),
                LocalDate.now()
        );

        CourtReferral referral = CourtReferral.builder()
                .referralId(referralId)
                .caseId(caseId)
                .studentId(truancyCase.getStudentId())
                .studentName(truancyCase.getStudentName())
                .gradeLevel(truancyCase.getGradeLevel())
                .courtType(courtType)
                .referralDate(LocalDate.now())
                .unexcusedAbsences(truancyCase.getUnexcusedAbsences())
                .interventionsAttempted(truancyCase.getInterventions().size())
                .referralReason(buildReferralReason(truancyCase))
                .status("PENDING")
                .finalNoticeSent(true)
                .build();

        courtReferrals.put(referralId, referral);

        // Update truancy case
        truancyCase.setCourtReferralId(referralId);
        truancyCase.setCourtReferralDate(LocalDate.now());
        truancyCase.setStatus("COURT_REFERRAL");
        truancyCase.setLastUpdated(LocalDateTime.now());

        log.info("Generated court referral {} for case {}", referralId, caseId);
        return referral;
    }

    /**
     * Update court referral status
     */
    public CourtReferral updateCourtReferralStatus(String referralId, String status, LocalDate courtDate, String outcome) {
        CourtReferral referral = courtReferrals.get(referralId);
        if (referral == null) {
            throw new IllegalArgumentException("Court referral not found: " + referralId);
        }

        referral.setStatus(status);
        referral.setCourtDate(courtDate);
        referral.setCourtOutcome(outcome);
        referral.setLastUpdated(LocalDateTime.now());

        // Update associated truancy case
        if (referral.getCaseId() != null) {
            TruancyCase truancyCase = truancyCases.get(referral.getCaseId());
            if (truancyCase != null) {
                truancyCase.setStatus("COURT_ORDERED_MONITORING");
                truancyCase.setLastUpdated(LocalDateTime.now());
            }
        }

        log.info("Updated court referral {} status to {}", referralId, status);
        return referral;
    }

    // ========================================================================
    // INTERVENTION TRACKING
    // ========================================================================

    /**
     * Get all active truancy cases
     */
    public List<TruancyCase> getActiveCases() {
        return truancyCases.values().stream()
                .filter(c -> "OPEN".equals(c.getStatus()) || "COURT_REFERRAL".equals(c.getStatus()))
                .toList();
    }

    /**
     * Get truancy cases by severity
     */
    public List<TruancyCase> getCasesBySeverity(TruancySeverity severity) {
        return truancyCases.values().stream()
                .filter(c -> c.getSeverity() == severity)
                .filter(c -> !"CLOSED".equals(c.getStatus()))
                .toList();
    }

    /**
     * Get intervention effectiveness report
     */
    public InterventionEffectivenessReport getInterventionEffectiveness(LocalDate startDate, LocalDate endDate) {
        log.info("Generating intervention effectiveness report from {} to {}", startDate, endDate);

        List<TruancyCase> casesInPeriod = truancyCases.values().stream()
                .filter(c -> !c.getOpenedDate().isBefore(startDate) && !c.getOpenedDate().isAfter(endDate))
                .toList();

        int totalCases = casesInPeriod.size();
        int resolvedCases = (int) casesInPeriod.stream()
                .filter(c -> "CLOSED".equals(c.getStatus()))
                .count();
        int courtReferrals = (int) casesInPeriod.stream()
                .filter(c -> c.getCourtReferralId() != null)
                .count();

        Map<InterventionType, Long> interventionCounts = casesInPeriod.stream()
                .flatMap(c -> c.getInterventions().stream())
                .collect(java.util.stream.Collectors.groupingBy(
                    Intervention::getInterventionType,
                    java.util.stream.Collectors.counting()
                ));

        Map<InterventionType, Long> successfulInterventions = casesInPeriod.stream()
                .filter(c -> "CLOSED".equals(c.getStatus()))
                .flatMap(c -> c.getInterventions().stream())
                .filter(i -> "COMPLETED".equals(i.getStatus()))
                .collect(java.util.stream.Collectors.groupingBy(
                    Intervention::getInterventionType,
                    java.util.stream.Collectors.counting()
                ));

        double resolutionRate = totalCases > 0 ? (double) resolvedCases / totalCases * 100 : 0.0;
        double courtReferralRate = totalCases > 0 ? (double) courtReferrals / totalCases * 100 : 0.0;

        return InterventionEffectivenessReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCases(totalCases)
                .resolvedCases(resolvedCases)
                .courtReferrals(courtReferrals)
                .activeInterventions(interventionCounts.size())
                .resolutionRate(resolutionRate)
                .courtReferralRate(courtReferralRate)
                .interventionCounts(interventionCounts)
                .successfulInterventions(successfulInterventions)
                .generatedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String generateCaseId(Long studentId) {
        return String.format("TC-%d-%d", studentId, System.currentTimeMillis());
    }

    private TruancySeverity determineSeverity(int unexcusedAbsences) {
        if (unexcusedAbsences >= 15) {
            return TruancySeverity.CRITICAL;
        } else if (unexcusedAbsences >= 10) {
            return TruancySeverity.SEVERE;
        } else if (unexcusedAbsences >= 5) {
            return TruancySeverity.MODERATE;
        } else {
            return TruancySeverity.MILD;
        }
    }

    private String buildReferralReason(TruancyCase truancyCase) {
        return String.format(
                "Student has accumulated %d unexcused absences. " +
                "%d interventions have been attempted without success. " +
                "Case severity: %s. Court intervention required.",
                truancyCase.getUnexcusedAbsences(),
                truancyCase.getInterventions().size(),
                truancyCase.getSeverity()
        );
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum TruancySeverity {
        MILD,
        MODERATE,
        SEVERE,
        CRITICAL
    }

    public enum InterventionType {
        PARENT_CONFERENCE,
        COUNSELING,
        ATTENDANCE_CONTRACT,
        IMPROVEMENT_PLAN,
        HOME_VISIT,
        TRANSPORTATION_ASSISTANCE,
        MENTOR_PROGRAM,
        INCENTIVE_PROGRAM,
        SOCIAL_SERVICES_REFERRAL,
        MEDICAL_REFERRAL,
        TRUANCY_COURT_DIVERSION,
        COMMUNITY_SERVICE,
        OTHER
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class TruancyCase {
        private String caseId;
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private LocalDate openedDate;
        private LocalDate closedDate;
        private Integer unexcusedAbsences;
        private TruancySeverity severity;
        private String status;
        private String reason;
        private String assignedCaseWorker;
        private List<Intervention> interventions;
        private List<CaseNote> notes;
        private String courtReferralId;
        private LocalDate courtReferralDate;
        private String resolution;
        private String closedBy;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    public static class Intervention {
        private String interventionId;
        private InterventionType interventionType;
        private String description;
        private String assignedTo;
        private LocalDate startDate;
        private LocalDate completedDate;
        private String status;
        private String outcome;
    }

    @Data
    @Builder
    public static class CaseNote {
        private String noteId;
        private String note;
        private String addedBy;
        private LocalDateTime addedDate;
    }

    @Data
    @Builder
    public static class CourtReferral {
        private String referralId;
        private String caseId;
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private String courtType;
        private LocalDate referralDate;
        private LocalDate courtDate;
        private Integer unexcusedAbsences;
        private Integer interventionsAttempted;
        private String referralReason;
        private String status;
        private String courtOutcome;
        private Boolean finalNoticeSent;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    public static class InterventionEffectivenessReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalCases;
        private Integer resolvedCases;
        private Integer courtReferrals;
        private Integer activeInterventions;
        private Double resolutionRate;
        private Double courtReferralRate;
        private Map<InterventionType, Long> interventionCounts;
        private Map<InterventionType, Long> successfulInterventions;
        private LocalDate generatedDate;
    }
}
