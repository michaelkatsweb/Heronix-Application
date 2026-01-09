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
 * Health Plan Entity
 * Tracks individualized health care plans including asthma, allergy, seizure, and diabetes action plans
 * Supports emergency protocols, accommodations, and staff training tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "health_plans", indexes = {
    @Index(name = "idx_health_plan_student", columnList = "student_id"),
    @Index(name = "idx_health_plan_type", columnList = "plan_type"),
    @Index(name = "idx_health_plan_status", columnList = "plan_status"),
    @Index(name = "idx_health_plan_review_date", columnList = "next_review_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Plan Details
    @Column(name = "plan_number", unique = true, length = 50)
    private String planNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 30)
    private PlanType planType;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false, length = 30)
    @Builder.Default
    private PlanStatus planStatus = PlanStatus.DRAFT;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Diagnosis Information
    @Column(name = "diagnosis", nullable = false, length = 200)
    private String diagnosis;

    @Column(name = "diagnosis_code", length = 50)
    private String diagnosisCode; // ICD-10 code

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_severity", length = 30)
    private ConditionSeverity conditionSeverity;

    @Column(name = "condition_description", columnDefinition = "TEXT")
    private String conditionDescription;

    // Physician Information
    @Column(name = "physician_name", nullable = false, length = 100)
    private String physicianName;

    @Column(name = "physician_specialty", length = 100)
    private String physicianSpecialty;

    @Column(name = "physician_phone", length = 30)
    private String physicianPhone;

    @Column(name = "physician_fax", length = 30)
    private String physicianFax;

    @Column(name = "physician_email", length = 100)
    private String physicianEmail;

    @Column(name = "physician_address", columnDefinition = "TEXT")
    private String physicianAddress;

    @Column(name = "physician_orders_on_file")
    @Builder.Default
    private Boolean physicianOrdersOnFile = false;

    @Column(name = "physician_orders_file_path", length = 500)
    private String physicianOrdersFilePath;

    @Column(name = "physician_signature_date")
    private LocalDate physicianSignatureDate;

    // Triggers and Warning Signs (Asthma, Allergy, Seizure)
    @ElementCollection
    @CollectionTable(name = "health_plan_triggers", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "trigger", length = 200)
    @Builder.Default
    private List<String> triggers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "health_plan_warning_signs", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "warning_sign", length = 200)
    @Builder.Default
    private List<String> warningSigns = new ArrayList<>();

    @Column(name = "allergens", columnDefinition = "TEXT")
    private String allergens; // Specific allergens for allergy plans

    @Enumerated(EnumType.STRING)
    @Column(name = "allergy_severity", length = 30)
    private AllergySeverity allergySeverity;

    // Emergency Medications
    @ElementCollection
    @CollectionTable(name = "health_plan_emergency_meds", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "medication", length = 300)
    @Builder.Default
    private List<String> emergencyMedications = new ArrayList<>();

    @Column(name = "has_epipen")
    @Builder.Default
    private Boolean hasEpipen = false;

    @Column(name = "epipen_dosage", length = 50)
    private String epipenDosage;

    @Column(name = "epipen_location", length = 200)
    private String epipenLocation;

    @Column(name = "epipen_expiration_date")
    private LocalDate epipenExpirationDate;

    @Column(name = "has_inhaler")
    @Builder.Default
    private Boolean hasInhaler = false;

    @Column(name = "inhaler_medication", length = 100)
    private String inhalerMedication;

    @Column(name = "inhaler_dosage", length = 50)
    private String inhalerDosage;

    @Column(name = "inhaler_location", length = 200)
    private String inhalerLocation;

    @Column(name = "student_self_carries_medication")
    @Builder.Default
    private Boolean studentSelfCarriesMedication = false;

    // Daily Medications
    @ElementCollection
    @CollectionTable(name = "health_plan_daily_meds", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "medication", length = 300)
    @Builder.Default
    private List<String> dailyMedications = new ArrayList<>();

    // Emergency Response Steps
    @Column(name = "emergency_protocol", columnDefinition = "TEXT")
    private String emergencyProtocol;

    @ElementCollection
    @CollectionTable(name = "health_plan_emergency_steps", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "step", columnDefinition = "TEXT")
    @OrderColumn(name = "step_order")
    @Builder.Default
    private List<String> emergencySteps = new ArrayList<>();

    @Column(name = "when_to_call_911", columnDefinition = "TEXT")
    private String whenToCall911;

    @Column(name = "call_parent_immediately")
    @Builder.Default
    private Boolean callParentImmediately = false;

    // Diabetes Management Specific
    @Column(name = "diabetes_type", length = 30)
    private String diabetesType; // Type 1, Type 2

    @Column(name = "target_blood_glucose_range", length = 50)
    private String targetBloodGlucoseRange; // e.g., "80-120 mg/dL"

    @Column(name = "hypoglycemia_threshold")
    private Integer hypoglycemiaThreshold; // mg/dL

    @Column(name = "hyperglycemia_threshold")
    private Integer hyperglycemiaThreshold; // mg/dL

    @Column(name = "insulin_regimen", columnDefinition = "TEXT")
    private String insulinRegimen;

    @Column(name = "carb_counting_required")
    @Builder.Default
    private Boolean carbCountingRequired = false;

    @Column(name = "blood_glucose_monitoring_schedule", columnDefinition = "TEXT")
    private String bloodGlucoseMonitoringSchedule;

    @Column(name = "has_insulin_pump")
    @Builder.Default
    private Boolean hasInsulinPump = false;

    @Column(name = "insulin_pump_type", length = 100)
    private String insulinPumpType;

    @Column(name = "has_cgm")
    @Builder.Default
    private Boolean hasCgm = false; // Continuous Glucose Monitor

    @Column(name = "cgm_type", length = 100)
    private String cgmType;

    // Seizure Management Specific
    @Enumerated(EnumType.STRING)
    @Column(name = "seizure_type", length = 30)
    private SeizureType seizureType;

    @Column(name = "seizure_frequency", length = 100)
    private String seizureFrequency;

    @Column(name = "typical_seizure_duration", length = 50)
    private String typicalSeizureDuration;

    @Column(name = "rescue_medication", length = 200)
    private String rescueMedication; // e.g., Diastat, nasal midazolam

    @Column(name = "rescue_medication_when_to_give", columnDefinition = "TEXT")
    private String rescueMedicationWhenToGive;

    @Column(name = "post_seizure_care", columnDefinition = "TEXT")
    private String postSeizureCare;

    // Accommodations
    @ElementCollection
    @CollectionTable(name = "health_plan_accommodations", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "accommodation", length = 300)
    @Builder.Default
    private List<String> accommodations = new ArrayList<>();

    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions;

    @Column(name = "activity_restrictions", columnDefinition = "TEXT")
    private String activityRestrictions;

    @Column(name = "environmental_modifications", columnDefinition = "TEXT")
    private String environmentalModifications;

    // Staff Training
    @Column(name = "staff_training_required")
    @Builder.Default
    private Boolean staffTrainingRequired = false;

    @Column(name = "staff_training_topics", columnDefinition = "TEXT")
    private String staffTrainingTopics;

    @Column(name = "staff_training_completed")
    @Builder.Default
    private Boolean staffTrainingCompleted = false;

    @Column(name = "staff_training_completion_date")
    private LocalDate staffTrainingCompletionDate;

    @ElementCollection
    @CollectionTable(name = "health_plan_trained_staff", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "staff_name", length = 100)
    @Builder.Default
    private List<String> trainedStaff = new ArrayList<>();

    // Distribution
    @Column(name = "distributed_to_teachers")
    @Builder.Default
    private Boolean distributedToTeachers = false;

    @Column(name = "distribution_date")
    private LocalDate distributionDate;

    @ElementCollection
    @CollectionTable(name = "health_plan_distribution", joinColumns = @JoinColumn(name = "health_plan_id"))
    @Column(name = "recipient_name", length = 100)
    @Builder.Default
    private List<String> distributionList = new ArrayList<>();

    // Parent Consent
    @Column(name = "parent_consent_received")
    @Builder.Default
    private Boolean parentConsentReceived = false;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    @Column(name = "parent_consent_file_path", length = 500)
    private String parentConsentFilePath;

    @Column(name = "parent_input", columnDefinition = "TEXT")
    private String parentInput;

    // Review and Updates
    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "review_frequency_months")
    @Builder.Default
    private Integer reviewFrequencyMonths = 12;

    @Column(name = "annual_review_required")
    @Builder.Default
    private Boolean annualReviewRequired = true;

    @Column(name = "revision_history", columnDefinition = "TEXT")
    private String revisionHistory;

    // Emergency Contacts
    @Column(name = "emergency_contact_1_name", length = 100)
    private String emergencyContact1Name;

    @Column(name = "emergency_contact_1_relationship", length = 50)
    private String emergencyContact1Relationship;

    @Column(name = "emergency_contact_1_phone", length = 30)
    private String emergencyContact1Phone;

    @Column(name = "emergency_contact_2_name", length = 100)
    private String emergencyContact2Name;

    @Column(name = "emergency_contact_2_relationship", length = 50)
    private String emergencyContact2Relationship;

    @Column(name = "emergency_contact_2_phone", length = 30)
    private String emergencyContact2Phone;

    // Documentation
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Column(name = "attachments_file_path", length = 500)
    private String attachmentsFilePath;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum PlanType {
        ASTHMA("Asthma Action Plan"),
        ALLERGY("Allergy Action Plan"),
        FOOD_ALLERGY("Food Allergy Action Plan"),
        ANAPHYLAXIS("Anaphylaxis Action Plan"),
        SEIZURE("Seizure Action Plan"),
        DIABETES_TYPE_1("Type 1 Diabetes Management Plan"),
        DIABETES_TYPE_2("Type 2 Diabetes Management Plan"),
        CARDIAC("Cardiac Condition Plan"),
        BLEEDING_DISORDER("Bleeding Disorder Plan"),
        SICKLE_CELL("Sickle Cell Disease Plan"),
        INDIVIDUALIZED_HEALTHCARE("Individualized Healthcare Plan"),
        OTHER("Other Health Care Plan");

        private final String displayName;

        PlanType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PlanStatus {
        DRAFT("Draft"),
        PENDING_PHYSICIAN_APPROVAL("Pending Physician Approval"),
        PENDING_PARENT_CONSENT("Pending Parent Consent"),
        ACTIVE("Active"),
        UNDER_REVIEW("Under Review"),
        EXPIRED("Expired"),
        INACTIVE("Inactive"),
        ARCHIVED("Archived");

        private final String displayName;

        PlanStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ConditionSeverity {
        MILD("Mild"),
        MODERATE("Moderate"),
        SEVERE("Severe"),
        LIFE_THREATENING("Life-Threatening");

        private final String displayName;

        ConditionSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AllergySeverity {
        MILD("Mild - No Anaphylaxis Risk"),
        MODERATE("Moderate - Monitored"),
        SEVERE("Severe - Anaphylaxis Risk"),
        LIFE_THREATENING("Life-Threatening - Immediate EpiPen");

        private final String displayName;

        AllergySeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SeizureType {
        GENERALIZED_TONIC_CLONIC("Generalized Tonic-Clonic (Grand Mal)"),
        ABSENCE("Absence (Petit Mal)"),
        FOCAL("Focal/Partial Seizure"),
        MYOCLONIC("Myoclonic Seizure"),
        ATONIC("Atonic (Drop) Seizure"),
        COMPLEX_PARTIAL("Complex Partial Seizure"),
        MULTIPLE_TYPES("Multiple Seizure Types"),
        OTHER("Other");

        private final String displayName;

        SeizureType(String displayName) {
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
    public boolean isActive() {
        return planStatus == PlanStatus.ACTIVE &&
               (endDate == null || LocalDate.now().isBefore(endDate));
    }

    @Transient
    public boolean isExpired() {
        return planStatus == PlanStatus.EXPIRED ||
               (endDate != null && LocalDate.now().isAfter(endDate));
    }

    @Transient
    public boolean isDueForReview() {
        if (nextReviewDate == null) return annualReviewRequired;
        return LocalDate.now().isAfter(nextReviewDate) ||
               LocalDate.now().isEqual(nextReviewDate);
    }

    @Transient
    public boolean needsPhysicianApproval() {
        return planStatus == PlanStatus.PENDING_PHYSICIAN_APPROVAL ||
               !physicianOrdersOnFile;
    }

    @Transient
    public boolean needsParentConsent() {
        return planStatus == PlanStatus.PENDING_PARENT_CONSENT ||
               !parentConsentReceived;
    }

    @Transient
    public boolean needsStaffTraining() {
        return staffTrainingRequired && !staffTrainingCompleted;
    }

    @Transient
    public boolean needsDistribution() {
        return !distributedToTeachers && planStatus == PlanStatus.ACTIVE;
    }

    @Transient
    public boolean hasLifeThreateningCondition() {
        return conditionSeverity == ConditionSeverity.LIFE_THREATENING ||
               allergySeverity == AllergySeverity.LIFE_THREATENING ||
               (hasEpipen != null && hasEpipen);
    }

    @Transient
    public boolean epipenExpiring() {
        return epipenExpirationDate != null &&
               epipenExpirationDate.isAfter(LocalDate.now()) &&
               epipenExpirationDate.isBefore(LocalDate.now().plusDays(30));
    }

    @Transient
    public boolean epipenExpired() {
        return epipenExpirationDate != null &&
               epipenExpirationDate.isBefore(LocalDate.now());
    }

    @Transient
    public int getDaysSinceReview() {
        if (lastReviewDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastReviewDate.toEpochDay());
    }

    @Transient
    public int getDaysUntilReview() {
        if (nextReviewDate == null) return -1;
        return (int) (nextReviewDate.toEpochDay() - LocalDate.now().toEpochDay());
    }
}
