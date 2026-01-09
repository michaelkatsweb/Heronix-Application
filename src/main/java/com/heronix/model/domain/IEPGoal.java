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
 * IEP Goal Entity
 * Tracks annual goals, short-term objectives, and benchmarks for IEPs
 * Includes progress monitoring and mastery tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "iep_goals", indexes = {
    @Index(name = "idx_goal_iep", columnList = "iep_id"),
    @Index(name = "idx_goal_domain", columnList = "goal_domain"),
    @Index(name = "idx_goal_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IEPGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iep_id", nullable = false)
    private IEP iep;

    // Goal Information
    @Column(name = "goal_number", length = 20)
    private String goalNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_domain", nullable = false, length = 50)
    private GoalDomain goalDomain;

    @Column(name = "goal_statement", columnDefinition = "TEXT", nullable = false)
    private String goalStatement;

    @Column(name = "baseline_performance", columnDefinition = "TEXT")
    private String baselinePerformance;

    @Column(name = "target_performance", columnDefinition = "TEXT")
    private String targetPerformance;

    @Column(name = "evaluation_method", columnDefinition = "TEXT")
    private String evaluationMethod;

    @Column(name = "evaluation_schedule", length = 100)
    private String evaluationSchedule;

    // Progress Tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    @Column(name = "mastery_criteria", columnDefinition = "TEXT")
    private String masteryCriteria;

    @Column(name = "current_performance_level", columnDefinition = "TEXT")
    private String currentPerformanceLevel;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "last_progress_check_date")
    private LocalDate lastProgressCheckDate;

    @Column(name = "mastery_date")
    private LocalDate masteryDate;

    // Short-term Objectives
    @ElementCollection
    @CollectionTable(name = "goal_objectives", joinColumns = @JoinColumn(name = "goal_id"))
    @Column(name = "objective", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> shortTermObjectives = new ArrayList<>();

    // Benchmarks
    @ElementCollection
    @CollectionTable(name = "goal_benchmarks", joinColumns = @JoinColumn(name = "goal_id"))
    @Column(name = "benchmark", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> benchmarks = new ArrayList<>();

    // Responsible Staff
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_staff_id")
    private Teacher responsibleStaff;

    @Column(name = "responsible_staff_name", length = 100)
    private String responsibleStaffName;

    // Settings
    @ElementCollection
    @CollectionTable(name = "goal_settings", joinColumns = @JoinColumn(name = "goal_id"))
    @Column(name = "setting", length = 100)
    @Builder.Default
    private List<String> settings = new ArrayList<>();

    // Progress Notes
    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Column(name = "barriers_to_progress", columnDefinition = "TEXT")
    private String barriersToProgress;

    @Column(name = "interventions_used", columnDefinition = "TEXT")
    private String interventionsUsed;

    // Dates
    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum GoalDomain {
        READING("Reading"),
        WRITING("Writing"),
        MATH("Math"),
        COMMUNICATION("Communication"),
        SOCIAL_EMOTIONAL("Social-Emotional"),
        BEHAVIOR("Behavior"),
        ADAPTIVE("Adaptive/Life Skills"),
        MOTOR_SKILLS("Motor Skills"),
        VOCATIONAL("Vocational"),
        TRANSITION("Transition"),
        SELF_HELP("Self-Help"),
        COGNITIVE("Cognitive"),
        OTHER("Other");

        private final String displayName;

        GoalDomain(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum GoalStatus {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        ADEQUATE_PROGRESS("Adequate Progress"),
        INSUFFICIENT_PROGRESS("Insufficient Progress"),
        MASTERED("Mastered"),
        DISCONTINUED("Discontinued"),
        MODIFIED("Modified"),
        CONTINUED("Continued to Next IEP");

        private final String displayName;

        GoalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
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
    public boolean isMastered() {
        return status == GoalStatus.MASTERED && masteryDate != null;
    }

    @Transient
    public boolean isOnTrack() {
        return status == GoalStatus.IN_PROGRESS || status == GoalStatus.ADEQUATE_PROGRESS;
    }

    @Transient
    public boolean needsAttention() {
        return status == GoalStatus.INSUFFICIENT_PROGRESS || status == GoalStatus.NOT_STARTED;
    }

    @Transient
    public int getDaysSinceProgressCheck() {
        if (lastProgressCheckDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastProgressCheckDate.toEpochDay());
    }

    @Transient
    public boolean isOverdueForProgressCheck() {
        // Consider overdue if no check in 30 days
        return getDaysSinceProgressCheck() > 30;
    }

    @Transient
    public int getObjectiveCount() {
        return shortTermObjectives != null ? shortTermObjectives.size() : 0;
    }

    @Transient
    public int getBenchmarkCount() {
        return benchmarks != null ? benchmarks.size() : 0;
    }

    @Transient
    public Student getStudent() {
        return iep != null ? iep.getStudent() : null;
    }
}
