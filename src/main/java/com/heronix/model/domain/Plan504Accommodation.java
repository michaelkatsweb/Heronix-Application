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
 * 504 Plan Accommodation Entity
 * Tracks specific accommodations for 504 plans with implementation details
 * Supports distribution to teachers and monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "plan504_accommodations", indexes = {
    @Index(name = "idx_504_accommodation_plan", columnList = "plan_504_id"),
    @Index(name = "idx_504_accommodation_category", columnList = "category"),
    @Index(name = "idx_504_accommodation_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan504Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_504_id", nullable = false)
    private Plan504 plan504;

    // Accommodation Details
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AccommodationCategory category;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "implementation_instructions", columnDefinition = "TEXT")
    private String implementationInstructions;

    // Subject/Class Applicability
    @Column(name = "applies_to_all_classes")
    @Builder.Default
    private Boolean appliesToAllClasses = true;

    @ElementCollection
    @CollectionTable(name = "plan504_accommodation_classes", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "class_name", length = 100)
    @Builder.Default
    private List<String> applicableClasses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "plan504_accommodation_subjects", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "subject", length = 100)
    @Builder.Default
    private List<String> applicableSubjects = new ArrayList<>();

    // Testing Accommodations
    @Column(name = "applies_to_classroom_tests")
    @Builder.Default
    private Boolean appliesToClassroomTests = true;

    @Column(name = "applies_to_state_tests")
    @Builder.Default
    private Boolean appliesToStateTests = false;

    @Column(name = "testing_notes", columnDefinition = "TEXT")
    private String testingNotes;

    // Implementation
    @Column(name = "responsible_party", length = 200)
    private String responsibleParty;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "requires_teacher_training")
    @Builder.Default
    private Boolean requiresTeacherTraining = false;

    @Column(name = "training_completed")
    @Builder.Default
    private Boolean trainingCompleted = false;

    // Distribution Tracking
    @Column(name = "distributed_to_teachers")
    @Builder.Default
    private Boolean distributedToTeachers = false;

    @Column(name = "distribution_date")
    private LocalDate distributionDate;

    @ElementCollection
    @CollectionTable(name = "plan504_accommodation_teachers_notified", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "teacher_name", length = 100)
    @Builder.Default
    private List<String> teachersNotified = new ArrayList<>();

    // Monitoring
    @Column(name = "last_monitored_date")
    private LocalDate lastMonitoredDate;

    @Column(name = "monitoring_notes", columnDefinition = "TEXT")
    private String monitoringNotes;

    @Column(name = "effectiveness_rating")
    private Integer effectivenessRating;

    @Column(name = "concerns", columnDefinition = "TEXT")
    private String concerns;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum AccommodationCategory {
        PRESENTATION("Presentation - How information is presented"),
        RESPONSE("Response - How student responds/completes work"),
        TIMING("Timing - Extended time, breaks, flexible scheduling"),
        SETTING("Setting - Location, seating, environment"),
        BEHAVIORAL("Behavioral Support"),
        HEALTH("Health & Medical"),
        ASSISTIVE_TECHNOLOGY("Assistive Technology"),
        COMMUNICATION("Communication Support"),
        ORGANIZATION("Organization & Study Skills"),
        PHYSICAL_MOBILITY("Physical & Mobility"),
        SOCIAL_EMOTIONAL("Social-Emotional Support"),
        TESTING("Testing Accommodations"),
        OTHER("Other");

        private final String displayName;

        AccommodationCategory(String displayName) {
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
        if (effectiveDate == null) {
            effectiveDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        LocalDate today = LocalDate.now();
        if (effectiveDate != null && today.isBefore(effectiveDate)) return false;
        if (endDate != null && today.isAfter(endDate)) return false;
        return true;
    }

    @Transient
    public boolean needsTeacherNotification() {
        return !distributedToTeachers || (isActive && distributionDate == null);
    }

    @Transient
    public boolean needsTraining() {
        return requiresTeacherTraining && !trainingCompleted;
    }

    @Transient
    public boolean isOverdueForMonitoring() {
        if (lastMonitoredDate == null) return true;
        // Consider overdue if not monitored in 90 days
        return LocalDate.now().minusDays(90).isAfter(lastMonitoredDate);
    }

    @Transient
    public Student getStudent() {
        return plan504 != null ? plan504.getStudent() : null;
    }

    @Transient
    public boolean appliesToSubject(String subject) {
        if (appliesToAllClasses) return true;
        return applicableSubjects != null && applicableSubjects.contains(subject);
    }

    @Transient
    public boolean appliesToClass(String className) {
        if (appliesToAllClasses) return true;
        return applicableClasses != null && applicableClasses.contains(className);
    }

    @Transient
    public int getTeachersNotifiedCount() {
        return teachersNotified != null ? teachersNotified.size() : 0;
    }

    @Transient
    public int getDaysSinceMonitored() {
        if (lastMonitoredDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastMonitoredDate.toEpochDay());
    }
}
