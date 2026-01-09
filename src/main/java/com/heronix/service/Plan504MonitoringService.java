package com.heronix.service;

import com.heronix.model.domain.Plan504;
import com.heronix.repository.Plan504Repository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 504 Plan Monitoring Service
 *
 * Manages progress monitoring, meeting notes, and implementation tracking for 504 plans.
 * Provides oversight of plan effectiveness and compliance.
 *
 * Key Responsibilities:
 * - Track 504 team meeting notes
 * - Monitor student progress under accommodations
 * - Generate progress reports
 * - Track implementation fidelity
 * - Alert for review dates
 * - Document parent/teacher feedback
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@Service
public class Plan504MonitoringService {

    @Autowired
    private Plan504Repository plan504Repository;

    // ========================================================================
    // MEETING NOTES
    // ========================================================================

    /**
     * Create meeting notes for a 504 plan
     */
    @Transactional
    public MeetingNotes createMeetingNotes(
            Long planId,
            LocalDate meetingDate,
            String meetingType,
            List<String> attendees,
            String discussionSummary,
            List<String> decisionsActions,
            LocalDate nextMeetingDate,
            String recordedByUsername) {

        log.info("Creating meeting notes for 504 plan {} on {}", planId, meetingDate);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // In production, would save to Meeting Notes table
        // For now, create DTO for return

        return MeetingNotes.builder()
                .id(UUID.randomUUID().toString())
                .planId(planId)
                .planNumber(plan.getPlanNumber())
                .studentName(plan.getStudent().getFullName())
                .meetingDate(meetingDate)
                .meetingType(meetingType)
                .attendees(attendees)
                .discussionSummary(discussionSummary)
                .decisionsAndActions(decisionsActions)
                .nextMeetingDate(nextMeetingDate)
                .recordedBy(recordedByUsername)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get all meeting notes for a plan
     */
    public List<MeetingNotes> getMeetingHistory(Long planId) {
        log.info("Retrieving meeting history for plan {}", planId);

        // Mock implementation - would query actual meeting notes table
        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // Return empty list for now
        return new ArrayList<>();
    }

    // ========================================================================
    // PROGRESS MONITORING
    // ========================================================================

    /**
     * Record progress observation for a student
     */
    @Transactional
    public ProgressObservation recordProgress(
            Long planId,
            LocalDate observationDate,
            String category,
            String observation,
            ProgressRating rating,
            String accommodationsUsed,
            String recommendedChanges,
            String observedByUsername) {

        log.info("Recording progress observation for plan {} on {}", planId, observationDate);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // In production, would save to Progress Observations table
        return ProgressObservation.builder()
                .id(UUID.randomUUID().toString())
                .planId(planId)
                .studentName(plan.getStudent().getFullName())
                .observationDate(observationDate)
                .category(category)
                .observation(observation)
                .rating(rating)
                .accommodationsUsed(accommodationsUsed)
                .recommendedChanges(recommendedChanges)
                .observedBy(observedByUsername)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate progress report for a plan
     */
    public ProgressReport generateProgressReport(
            Long planId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating progress report for plan {} from {} to {}",
                planId, startDate, endDate);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // Mock implementation - would query actual observations
        List<ProgressObservation> observations = new ArrayList<>();

        // Mock data for demonstration
        double avgRating = 3.5; // out of 5
        int totalObservations = 12;
        int positiveObservations = 9;
        int concernObservations = 3;

        Map<String, Integer> observationsByCategory = new HashMap<>();
        observationsByCategory.put("Academic", 5);
        observationsByCategory.put("Behavioral", 4);
        observationsByCategory.put("Social", 3);

        List<String> keyFindings = Arrays.asList(
                "Student shows improved focus with preferential seating",
                "Extended time accommodations are being used effectively",
                "Some difficulty with organization; may need additional support"
        );

        return ProgressReport.builder()
                .planId(planId)
                .planNumber(plan.getPlanNumber())
                .studentName(plan.getStudent().getFullName())
                .reportStartDate(startDate)
                .reportEndDate(endDate)
                .totalObservations(totalObservations)
                .averageRating(avgRating)
                .positiveObservations(positiveObservations)
                .concernObservations(concernObservations)
                .observationsByCategory(observationsByCategory)
                .observations(observations)
                .keyFindings(keyFindings)
                .overallAssessment("Student is making adequate progress with current accommodations")
                .recommendations(Arrays.asList(
                        "Continue current accommodations",
                        "Consider adding organizational support"
                ))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // IMPLEMENTATION TRACKING
    // ========================================================================

    /**
     * Record accommodation implementation check
     */
    @Transactional
    public ImplementationCheck recordImplementationCheck(
            Long planId,
            LocalDate checkDate,
            String teacherName,
            Map<String, Boolean> accommodationsImplemented,
            String notes,
            String checkedByUsername) {

        log.info("Recording implementation check for plan {} by {}", planId, teacherName);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        int totalAccommodations = accommodationsImplemented.size();
        int implemented = (int) accommodationsImplemented.values().stream()
                .filter(Boolean::booleanValue)
                .count();

        double implementationRate = totalAccommodations > 0 ?
                (double) implemented / totalAccommodations : 0.0;

        return ImplementationCheck.builder()
                .id(UUID.randomUUID().toString())
                .planId(planId)
                .checkDate(checkDate)
                .teacherName(teacherName)
                .accommodationsChecked(accommodationsImplemented)
                .totalAccommodations(totalAccommodations)
                .implementedCount(implemented)
                .implementationRate(implementationRate)
                .notes(notes)
                .isFullyImplemented(implemented == totalAccommodations)
                .checkedBy(checkedByUsername)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // ALERTS AND REMINDERS
    // ========================================================================

    /**
     * Get plans needing review
     */
    public List<ReviewAlert> getPlansNeedingReview() {
        log.info("Checking for plans needing review");

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);

        List<Plan504> plansNeedingReview = plan504Repository
                .findPlansWithOverdueReview(today);

        List<Plan504> plansSoonToReview = plan504Repository
                .findPlansNeedingRenewal(today, thirtyDaysOut);

        List<ReviewAlert> alerts = new ArrayList<>();

        // Overdue reviews
        for (Plan504 plan : plansNeedingReview) {
            alerts.add(ReviewAlert.builder()
                    .planId(plan.getId())
                    .planNumber(plan.getPlanNumber())
                    .studentName(plan.getStudent().getFullName())
                    .reviewDate(plan.getNextReviewDate())
                    .daysOverdue((int) java.time.temporal.ChronoUnit.DAYS.between(
                            plan.getNextReviewDate(), today))
                    .priority("HIGH")
                    .message("Review is overdue")
                    .build());
        }

        // Upcoming reviews
        for (Plan504 plan : plansSoonToReview) {
            if (!plansNeedingReview.contains(plan)) {
                alerts.add(ReviewAlert.builder()
                        .planId(plan.getId())
                        .planNumber(plan.getPlanNumber())
                        .studentName(plan.getStudent().getFullName())
                        .reviewDate(plan.getNextReviewDate())
                        .daysUntilDue((int) java.time.temporal.ChronoUnit.DAYS.between(
                                today, plan.getNextReviewDate()))
                        .priority("MEDIUM")
                        .message("Review due soon")
                        .build());
            }
        }

        alerts.sort(Comparator.comparing(ReviewAlert::getPriority).reversed()
                .thenComparing(alert -> alert.getReviewDate() != null ?
                        alert.getReviewDate() : LocalDate.MAX));

        return alerts;
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum ProgressRating {
        EXCELLENT(5),
        GOOD(4),
        SATISFACTORY(3),
        NEEDS_IMPROVEMENT(2),
        CONCERNING(1);

        private final int value;

        ProgressRating(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class MeetingNotes {
        private String id;
        private Long planId;
        private String planNumber;
        private String studentName;
        private LocalDate meetingDate;
        private String meetingType; // Initial, Annual Review, Amendment, etc.
        private List<String> attendees;
        private String discussionSummary;
        private List<String> decisionsAndActions;
        private LocalDate nextMeetingDate;
        private String recordedBy;
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    public static class ProgressObservation {
        private String id;
        private Long planId;
        private String studentName;
        private LocalDate observationDate;
        private String category; // Academic, Behavioral, Social, etc.
        private String observation;
        private ProgressRating rating;
        private String accommodationsUsed;
        private String recommendedChanges;
        private String observedBy;
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    public static class ProgressReport {
        private Long planId;
        private String planNumber;
        private String studentName;
        private LocalDate reportStartDate;
        private LocalDate reportEndDate;
        private int totalObservations;
        private double averageRating;
        private int positiveObservations;
        private int concernObservations;
        private Map<String, Integer> observationsByCategory;
        private List<ProgressObservation> observations;
        private List<String> keyFindings;
        private String overallAssessment;
        private List<String> recommendations;
        private LocalDateTime generatedAt;
    }

    @Data
    @Builder
    public static class ImplementationCheck {
        private String id;
        private Long planId;
        private LocalDate checkDate;
        private String teacherName;
        private Map<String, Boolean> accommodationsChecked;
        private int totalAccommodations;
        private int implementedCount;
        private double implementationRate;
        private String notes;
        private boolean isFullyImplemented;
        private String checkedBy;
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    public static class ReviewAlert {
        private Long planId;
        private String planNumber;
        private String studentName;
        private LocalDate reviewDate;
        private Integer daysOverdue;
        private Integer daysUntilDue;
        private String priority; // HIGH, MEDIUM, LOW
        private String message;
    }
}
