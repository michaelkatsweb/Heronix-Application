package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RTI Intervention Entity
 * Tracks Response to Intervention (RTI/MTSS) interventions across tiers
 * Supports progress monitoring and intervention effectiveness analysis
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "rti_interventions", indexes = {
    @Index(name = "idx_rti_student", columnList = "student_id"),
    @Index(name = "idx_rti_tier", columnList = "tier"),
    @Index(name = "idx_rti_status", columnList = "status"),
    @Index(name = "idx_rti_area", columnList = "academic_area")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RTIIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Intervention Details
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private RTITier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_area", nullable = false, length = 50)
    private AcademicArea academicArea;

    @Column(name = "intervention_name", length = 200, nullable = false)
    private String interventionName;

    @Column(name = "intervention_description", columnDefinition = "TEXT")
    private String interventionDescription;

    @Column(name = "intervention_program", length = 200)
    private String interventionProgram;

    // Implementation
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "planned_duration_weeks")
    private Integer plannedDurationWeeks;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    @Column(name = "group_size")
    private Integer groupSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_model", length = 30)
    private DeliveryModel deliveryModel;

    // Staff
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interventionist_id")
    private Teacher interventionist;

    @Column(name = "interventionist_name", length = 100)
    private String interventionistName;

    // Progress Monitoring
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_monitoring_tool", length = 100)
    private ProgressMonitoringTool progressMonitoringTool;

    @Column(name = "progress_monitoring_frequency", length = 100)
    private String progressMonitoringFrequency;

    @Column(name = "baseline_score")
    private Double baselineScore;

    @Column(name = "target_score")
    private Double targetScore;

    @Column(name = "current_score")
    private Double currentScore;

    @Column(name = "last_progress_check_date")
    private LocalDate lastProgressCheckDate;

    @ElementCollection
    @CollectionTable(name = "rti_progress_data", joinColumns = @JoinColumn(name = "intervention_id"))
    @Builder.Default
    private List<ProgressDataPoint> progressData = new ArrayList<>();

    // Status & Outcomes
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private InterventionStatus status = InterventionStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "effectiveness", length = 30)
    private Effectiveness effectiveness;

    @Column(name = "outcome_notes", columnDefinition = "TEXT")
    private String outcomeNotes;

    @Column(name = "fidelity_of_implementation")
    private Integer fidelityOfImplementation;

    // Referral
    @Column(name = "referred_to_special_education")
    @Builder.Default
    private Boolean referredToSpecialEducation = false;

    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "referral_notes", columnDefinition = "TEXT")
    private String referralNotes;

    // Team Meetings
    @Column(name = "next_meeting_date")
    private LocalDate nextMeetingDate;

    @Column(name = "meeting_notes", columnDefinition = "TEXT")
    private String meetingNotes;

    @ElementCollection
    @CollectionTable(name = "rti_team_members", joinColumns = @JoinColumn(name = "intervention_id"))
    @Column(name = "member_name", length = 100)
    @Builder.Default
    private List<String> teamMembers = new ArrayList<>();

    // Parent Communication
    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "parent_consent_required")
    @Builder.Default
    private Boolean parentConsentRequired = false;

    @Column(name = "parent_consent_received")
    private Boolean parentConsentReceived;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum RTITier {
        TIER_1("Tier 1 - Universal/Core Instruction"),
        TIER_2("Tier 2 - Targeted Intervention"),
        TIER_3("Tier 3 - Intensive Intervention");

        private final String displayName;

        RTITier(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AcademicArea {
        READING("Reading"),
        MATH("Math"),
        WRITING("Writing"),
        BEHAVIOR("Behavior"),
        LANGUAGE("Language"),
        SOCIAL_SKILLS("Social Skills"),
        EXECUTIVE_FUNCTION("Executive Function"),
        OTHER("Other");

        private final String displayName;

        AcademicArea(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryModel {
        PUSH_IN("Push-In"),
        PULL_OUT("Pull-Out"),
        SMALL_GROUP("Small Group"),
        ONE_ON_ONE("One-on-One"),
        WHOLE_CLASS("Whole Class"),
        COMPUTER_BASED("Computer-Based"),
        HYBRID("Hybrid");

        private final String displayName;

        DeliveryModel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProgressMonitoringTool {
        CBM_READING("CBM - Reading"),
        CBM_MATH("CBM - Math"),
        DIBELS("DIBELS"),
        AIMSWEB("AIMSweb"),
        EASYCBM("easyCBM"),
        STAR("STAR Assessment"),
        RUNNING_RECORDS("Running Records"),
        CUSTOM("Custom Tool"),
        OTHER("Other");

        private final String displayName;

        ProgressMonitoringTool(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InterventionStatus {
        PLANNED("Planned"),
        ACTIVE("Active"),
        ON_HOLD("On Hold"),
        COMPLETED("Completed"),
        DISCONTINUED("Discontinued"),
        TRANSITIONED("Transitioned to Next Tier");

        private final String displayName;

        InterventionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Effectiveness {
        HIGHLY_EFFECTIVE("Highly Effective - Exceeding Goals"),
        EFFECTIVE("Effective - Meeting Goals"),
        MODERATELY_EFFECTIVE("Moderately Effective - Some Progress"),
        MINIMALLY_EFFECTIVE("Minimally Effective - Limited Progress"),
        INEFFECTIVE("Ineffective - No Progress");

        private final String displayName;

        Effectiveness(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressDataPoint {
        @Column(name = "check_date")
        private LocalDate checkDate;

        @Column(name = "score")
        private Double score;

        @Column(name = "notes", columnDefinition = "TEXT")
        private String notes;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean isActive() {
        return status == InterventionStatus.ACTIVE;
    }

    @Transient
    public boolean isCompleted() {
        return status == InterventionStatus.COMPLETED || status == InterventionStatus.DISCONTINUED;
    }

    @Transient
    public boolean isMakingProgress() {
        if (currentScore == null || baselineScore == null) return false;
        return currentScore > baselineScore;
    }

    @Transient
    public boolean hasMetGoal() {
        if (currentScore == null || targetScore == null) return false;
        return currentScore >= targetScore;
    }

    @Transient
    public Double getProgressPercentage() {
        if (currentScore == null || baselineScore == null || targetScore == null) return null;
        if (targetScore.equals(baselineScore)) return 0.0;
        return ((currentScore - baselineScore) / (targetScore - baselineScore)) * 100.0;
    }

    @Transient
    public int getWeeksInIntervention() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return (int) ((end.toEpochDay() - startDate.toEpochDay()) / 7);
    }

    @Transient
    public boolean isOverdueForProgressCheck() {
        if (lastProgressCheckDate == null) return true;
        // Consider overdue if no check in 14 days
        return LocalDate.now().minusDays(14).isAfter(lastProgressCheckDate);
    }

    @Transient
    public int getProgressDataPointCount() {
        return progressData != null ? progressData.size() : 0;
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotified || parentNotificationDate == null;
    }

    @Transient
    public boolean needsParentConsent() {
        return parentConsentRequired && (parentConsentReceived == null || !parentConsentReceived);
    }

    @Transient
    public boolean shouldConsiderNextTier() {
        if (tier == RTITier.TIER_3) return false;
        if (effectiveness == Effectiveness.INEFFECTIVE || effectiveness == Effectiveness.MINIMALLY_EFFECTIVE) {
            return getWeeksInIntervention() >= 8;
        }
        return false;
    }

    @Transient
    public boolean shouldConsiderSpedReferral() {
        return tier == RTITier.TIER_3 &&
               (effectiveness == Effectiveness.INEFFECTIVE || effectiveness == Effectiveness.MINIMALLY_EFFECTIVE) &&
               getWeeksInIntervention() >= 12 &&
               !referredToSpecialEducation;
    }
}
