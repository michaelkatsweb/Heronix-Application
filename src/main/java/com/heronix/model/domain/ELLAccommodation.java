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
 * ELL Accommodation Entity
 * Tracks accommodations for English Language Learners in classroom and testing
 * Supports state testing accommodations and implementation monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "ell_accommodations", indexes = {
    @Index(name = "idx_ell_accommodation_student", columnList = "ell_student_id"),
    @Index(name = "idx_ell_accommodation_category", columnList = "category"),
    @Index(name = "idx_ell_accommodation_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ELLAccommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ell_student_id", nullable = false)
    private ELLStudent ellStudent;

    // Accommodation Details
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AccommodationCategory category;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "implementation_instructions", columnDefinition = "TEXT")
    private String implementationInstructions;

    // Applicability
    @Column(name = "applies_to_all_classes")
    @Builder.Default
    private Boolean appliesToAllClasses = true;

    @ElementCollection
    @CollectionTable(name = "ell_accommodation_subjects", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "subject", length = 100)
    @Builder.Default
    private List<String> applicableSubjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ell_accommodation_classes", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "class_name", length = 100)
    @Builder.Default
    private List<String> applicableClasses = new ArrayList<>();

    // Testing Accommodations
    @Column(name = "applies_to_classroom_tests")
    @Builder.Default
    private Boolean appliesToClassroomTests = true;

    @Column(name = "applies_to_state_tests")
    @Builder.Default
    private Boolean appliesToStateTests = false;

    @Column(name = "state_testing_approved")
    private Boolean stateTestingApproved;

    @Column(name = "state_testing_approval_date")
    private LocalDate stateTestingApprovalDate;

    @Column(name = "testing_notes", columnDefinition = "TEXT")
    private String testingNotes;

    // Implementation
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "responsible_party", length = 200)
    private String responsibleParty;

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

    @Column(name = "training_date")
    private LocalDate trainingDate;

    // Distribution and Communication
    @Column(name = "distributed_to_teachers")
    @Builder.Default
    private Boolean distributedToTeachers = false;

    @Column(name = "distribution_date")
    private LocalDate distributionDate;

    @ElementCollection
    @CollectionTable(name = "ell_accommodation_teachers_notified", joinColumns = @JoinColumn(name = "accommodation_id"))
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

    @Column(name = "implementation_fidelity_rating")
    private Integer implementationFidelityRating;

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
        PRESENTATION("Presentation - Visual/Auditory Supports"),
        RESPONSE("Response - How Student Responds"),
        TIMING("Timing - Extended Time/Breaks"),
        SETTING("Setting - Environment/Seating"),
        LANGUAGE_SUPPORT("Language Support - Bilingual Resources"),
        TRANSLATION("Translation - Native Language Support"),
        SIMPLIFIED_LANGUAGE("Simplified/Modified Language"),
        VISUAL_AIDS("Visual Aids and Graphics"),
        TECHNOLOGY("Technology/Assistive Technology"),
        ORAL_ADMINISTRATION("Oral Administration/Read-Aloud"),
        WRITTEN_SUPPORT("Written Support/Notes"),
        VOCABULARY_SUPPORT("Vocabulary Support/Word Lists"),
        ASSESSMENT("Assessment Modifications"),
        INSTRUCTION("Instructional Accommodations"),
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
    public boolean needsStateApproval() {
        return appliesToStateTests && (stateTestingApproved == null || !stateTestingApproved);
    }

    @Transient
    public Student getStudent() {
        return ellStudent != null ? ellStudent.getStudent() : null;
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

    @Transient
    public boolean hasLowEffectiveness() {
        return effectivenessRating != null && effectivenessRating < 3;
    }

    @Transient
    public boolean hasLowImplementationFidelity() {
        return implementationFidelityRating != null && implementationFidelityRating < 3;
    }
}
