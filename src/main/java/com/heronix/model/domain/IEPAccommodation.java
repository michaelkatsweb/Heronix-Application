package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IEP Accommodation Entity
 * Tracks accommodations and modifications for IEPs
 * Supports implementation tracking by class/subject
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "iep_accommodations", indexes = {
    @Index(name = "idx_accommodation_iep", columnList = "iep_id"),
    @Index(name = "idx_accommodation_category", columnList = "category"),
    @Index(name = "idx_accommodation_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IEPAccommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iep_id", nullable = false)
    private IEP iep;

    // Accommodation Details
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AccommodationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AccommodationCategory category;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "implementation_notes", columnDefinition = "TEXT")
    private String implementationNotes;

    // Applicability
    @Column(name = "applies_to_all_classes")
    @Builder.Default
    private Boolean appliesToAllClasses = true;

    @ElementCollection
    @CollectionTable(name = "accommodation_subjects", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "subject", length = 100)
    @Builder.Default
    private List<String> applicableSubjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "accommodation_settings", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "setting", length = 100)
    @Builder.Default
    private List<String> applicableSettings = new ArrayList<>();

    // State-Mandated Testing
    @Column(name = "applies_to_state_testing")
    @Builder.Default
    private Boolean appliesToStateTesting = false;

    @Column(name = "state_testing_notes", columnDefinition = "TEXT")
    private String stateTestingNotes;

    // Implementation Tracking
    @Column(name = "responsible_staff", length = 200)
    private String responsibleStaff;

    @Column(name = "implementation_frequency", length = 100)
    private String implementationFrequency;

    @Column(name = "monitoring_method", columnDefinition = "TEXT")
    private String monitoringMethod;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "requires_training")
    @Builder.Default
    private Boolean requiresTraining = false;

    @Column(name = "training_provided")
    @Builder.Default
    private Boolean trainingProvided = false;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum AccommodationType {
        ACCOMMODATION("Accommodation"),
        MODIFICATION("Modification");

        private final String displayName;

        AccommodationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AccommodationCategory {
        PRESENTATION("Presentation"),
        RESPONSE("Response"),
        TIMING_SCHEDULING("Timing & Scheduling"),
        SETTING("Setting/Environment"),
        BEHAVIORAL("Behavioral Support"),
        INSTRUCTIONAL("Instructional"),
        ASSESSMENT("Assessment"),
        MATERIALS("Materials & Equipment"),
        COMMUNICATION("Communication"),
        SOCIAL_EMOTIONAL("Social-Emotional"),
        PHYSICAL_HEALTH("Physical & Health"),
        ASSISTIVE_TECHNOLOGY("Assistive Technology"),
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean isModification() {
        return type == AccommodationType.MODIFICATION;
    }

    @Transient
    public boolean requiresStaffTraining() {
        return requiresTraining && !trainingProvided;
    }

    @Transient
    public Student getStudent() {
        return iep != null ? iep.getStudent() : null;
    }

    @Transient
    public boolean appliesToSubject(String subject) {
        if (appliesToAllClasses) return true;
        return applicableSubjects != null && applicableSubjects.contains(subject);
    }

    @Transient
    public boolean appliesToSetting(String setting) {
        if (applicableSettings == null || applicableSettings.isEmpty()) return true;
        return applicableSettings.contains(setting);
    }
}
