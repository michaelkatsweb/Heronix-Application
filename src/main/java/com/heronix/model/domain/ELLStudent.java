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
 * ELL Student Entity
 * Tracks English Language Learner identification, program placement, and services
 * Supports Title III compliance and reclassification monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "ell_students", indexes = {
    @Index(name = "idx_ell_student", columnList = "student_id"),
    @Index(name = "idx_ell_status", columnList = "ell_status"),
    @Index(name = "idx_ell_proficiency", columnList = "proficiency_level"),
    @Index(name = "idx_ell_program", columnList = "program_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ELLStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // Identification
    @Column(name = "identification_date", nullable = false)
    private LocalDate identificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ell_status", nullable = false, length = 30)
    @Builder.Default
    private ELLStatus ellStatus = ELLStatus.ACTIVE;

    @Column(name = "home_language_survey_completed")
    @Builder.Default
    private Boolean homeLanguageSurveyCompleted = false;

    @Column(name = "home_language_survey_date")
    private LocalDate homeLanguageSurveyDate;

    // Language Information
    @Column(name = "native_language", length = 100)
    private String nativeLanguage;

    @Column(name = "home_language", length = 100)
    private String homeLanguage;

    @Column(name = "parent_language", length = 100)
    private String parentLanguage;

    @ElementCollection
    @CollectionTable(name = "ell_other_languages", joinColumns = @JoinColumn(name = "ell_student_id"))
    @Column(name = "language", length = 100)
    @Builder.Default
    private List<String> otherLanguagesSpoken = new ArrayList<>();

    // Proficiency Level
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false, length = 30)
    @Builder.Default
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.ENTERING;

    @Column(name = "listening_level")
    private Integer listeningLevel;

    @Column(name = "speaking_level")
    private Integer speakingLevel;

    @Column(name = "reading_level")
    private Integer readingLevel;

    @Column(name = "writing_level")
    private Integer writingLevel;

    @Column(name = "comprehension_level")
    private Integer comprehensionLevel;

    @Column(name = "last_proficiency_assessment_date")
    private LocalDate lastProficiencyAssessmentDate;

    // Program Placement
    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", length = 50)
    private ProgramType programType;

    @Column(name = "program_entry_date")
    private LocalDate programEntryDate;

    @Column(name = "program_exit_date")
    private LocalDate programExitDate;

    @Column(name = "years_in_program")
    private Integer yearsInProgram;

    // Service Delivery
    @Column(name = "service_minutes_per_week")
    private Integer serviceMinutesPerWeek;

    @Column(name = "service_frequency", length = 100)
    private String serviceFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_delivery_model", length = 50)
    private ServiceDeliveryModel serviceDeliveryModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private Teacher serviceProvider;

    @Column(name = "service_provider_name", length = 100)
    private String serviceProviderName;

    // Progress Monitoring
    @Column(name = "annual_assessment_required")
    @Builder.Default
    private Boolean annualAssessmentRequired = true;

    @Column(name = "next_annual_assessment_date")
    private LocalDate nextAnnualAssessmentDate;

    @Column(name = "progress_monitoring_frequency", length = 100)
    private String progressMonitoringFrequency;

    @Column(name = "last_progress_monitoring_date")
    private LocalDate lastProgressMonitoringDate;

    // Reclassification
    @Column(name = "eligible_for_reclassification")
    @Builder.Default
    private Boolean eligibleForReclassification = false;

    @Column(name = "reclassification_eligibility_date")
    private LocalDate reclassificationEligibilityDate;

    @Column(name = "reclassification_date")
    private LocalDate reclassificationDate;

    @Column(name = "reclassification_reason", columnDefinition = "TEXT")
    private String reclassificationReason;

    @Column(name = "monitoring_period_start_date")
    private LocalDate monitoringPeriodStartDate;

    @Column(name = "monitoring_period_years")
    private Integer monitoringPeriodYears;

    // Parent Communication
    @Column(name = "parent_notification_sent")
    @Builder.Default
    private Boolean parentNotificationSent = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "parent_notification_language", length = 100)
    private String parentNotificationLanguage;

    @Column(name = "translation_services_required")
    @Builder.Default
    private Boolean translationServicesRequired = false;

    @Column(name = "interpreter_required")
    @Builder.Default
    private Boolean interpreterRequired = false;

    @Column(name = "interpreter_language", length = 100)
    private String interpreterLanguage;

    // Accommodations
    @Column(name = "testing_accommodations_approved")
    @Builder.Default
    private Boolean testingAccommodationsApproved = false;

    @ElementCollection
    @CollectionTable(name = "ell_testing_accommodations", joinColumns = @JoinColumn(name = "ell_student_id"))
    @Column(name = "accommodation", length = 200)
    @Builder.Default
    private List<String> testingAccommodations = new ArrayList<>();

    // Title III Funding
    @Column(name = "title_iii_eligible")
    @Builder.Default
    private Boolean titleIIIEligible = true;

    @Column(name = "title_iii_funded")
    @Builder.Default
    private Boolean titleIIIFunded = false;

    // Additional Information
    @Column(name = "enrollment_in_us_schools_date")
    private LocalDate enrollmentInUSSchoolsDate;

    @Column(name = "years_in_us_schools")
    private Integer yearsInUSSchools;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "immigrant_status")
    private Boolean immigrantStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum ELLStatus {
        ACTIVE("Active ELL"),
        RECLASSIFIED("Reclassified - Fluent English Proficient"),
        MONITORED_YEAR_1("Monitored - Year 1"),
        MONITORED_YEAR_2("Monitored - Year 2"),
        MONITORED_YEAR_3("Monitored - Year 3"),
        MONITORED_YEAR_4("Monitored - Year 4"),
        EXITED("Exited ELL Program"),
        INACTIVE("Inactive");

        private final String displayName;

        ELLStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProficiencyLevel {
        ENTERING("Level 1 - Entering"),
        EMERGING("Level 2 - Emerging"),
        DEVELOPING("Level 3 - Developing"),
        EXPANDING("Level 4 - Expanding"),
        BRIDGING("Level 5 - Bridging"),
        REACHING("Level 6 - Reaching/Proficient");

        private final String displayName;

        ProficiencyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getNumericLevel() {
            return ordinal() + 1;
        }
    }

    public enum ProgramType {
        ESL_PULL_OUT("ESL Pull-Out"),
        ESL_PUSH_IN("ESL Push-In"),
        SHELTERED_ENGLISH("Sheltered English Instruction"),
        DUAL_LANGUAGE("Dual Language/Two-Way Immersion"),
        TRANSITIONAL_BILINGUAL("Transitional Bilingual Education"),
        STRUCTURED_ENGLISH_IMMERSION("Structured English Immersion"),
        NEWCOMER("Newcomer Program"),
        CONTENT_BASED_ESL("Content-Based ESL"),
        OTHER("Other");

        private final String displayName;

        ProgramType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ServiceDeliveryModel {
        PULL_OUT("Pull-Out"),
        PUSH_IN("Push-In/Co-Teaching"),
        SMALL_GROUP("Small Group"),
        ONE_ON_ONE("One-on-One"),
        RESOURCE_ROOM("Resource Room"),
        INCLUSION("Inclusion/Integrated"),
        SELF_CONTAINED("Self-Contained ELL Classroom"),
        HYBRID("Hybrid Model");

        private final String displayName;

        ServiceDeliveryModel(String displayName) {
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
    public boolean isActiveELL() {
        return ellStatus == ELLStatus.ACTIVE;
    }

    @Transient
    public boolean isMonitored() {
        return ellStatus == ELLStatus.MONITORED_YEAR_1 ||
               ellStatus == ELLStatus.MONITORED_YEAR_2 ||
               ellStatus == ELLStatus.MONITORED_YEAR_3 ||
               ellStatus == ELLStatus.MONITORED_YEAR_4;
    }

    @Transient
    public boolean needsAnnualAssessment() {
        if (!annualAssessmentRequired) return false;
        if (nextAnnualAssessmentDate == null) return true;
        return LocalDate.now().isAfter(nextAnnualAssessmentDate) ||
               LocalDate.now().isEqual(nextAnnualAssessmentDate);
    }

    @Transient
    public boolean needsProgressMonitoring() {
        if (lastProgressMonitoringDate == null) return true;
        // Consider overdue if not monitored in 30 days
        return LocalDate.now().minusDays(30).isAfter(lastProgressMonitoringDate);
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotificationSent || parentNotificationDate == null;
    }

    @Transient
    public int getYearsInProgram() {
        if (programEntryDate == null) return 0;
        LocalDate endDate = programExitDate != null ? programExitDate : LocalDate.now();
        return (int) ((endDate.toEpochDay() - programEntryDate.toEpochDay()) / 365);
    }

    @Transient
    public int getDaysSinceProgressMonitoring() {
        if (lastProgressMonitoringDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastProgressMonitoringDate.toEpochDay());
    }

    @Transient
    public int getDaysUntilAnnualAssessment() {
        if (nextAnnualAssessmentDate == null) return -1;
        return (int) (nextAnnualAssessmentDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public boolean isHighProficiency() {
        return proficiencyLevel == ProficiencyLevel.EXPANDING ||
               proficiencyLevel == ProficiencyLevel.BRIDGING ||
               proficiencyLevel == ProficiencyLevel.REACHING;
    }

    @Transient
    public boolean isLowProficiency() {
        return proficiencyLevel == ProficiencyLevel.ENTERING ||
               proficiencyLevel == ProficiencyLevel.EMERGING;
    }

    @Transient
    public Double getAverageProficiencyScore() {
        int count = 0;
        int sum = 0;

        if (listeningLevel != null) {
            sum += listeningLevel;
            count++;
        }
        if (speakingLevel != null) {
            sum += speakingLevel;
            count++;
        }
        if (readingLevel != null) {
            sum += readingLevel;
            count++;
        }
        if (writingLevel != null) {
            sum += writingLevel;
            count++;
        }
        if (comprehensionLevel != null) {
            sum += comprehensionLevel;
            count++;
        }

        return count > 0 ? (double) sum / count : null;
    }
}
