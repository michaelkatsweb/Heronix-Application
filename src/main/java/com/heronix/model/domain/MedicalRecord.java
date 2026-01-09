package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Medical Record Entity - Comprehensive Medical Tracking for Students
 * Location: src/main/java/com/heronix/model/domain/MedicalRecord.java
 *
 * ✅ FEATURES:
 * - Detailed allergy tracking with severity levels
 * - Chronic condition management (diabetes, asthma, etc.)
 * - Medication tracking with administration details
 * - Emergency action plans
 * - Medical alerts for quick reference
 * - History tracking with last update dates
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0 - Medical Safety Enhancement
 * @since 2025-11-14
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One relationship with Student
     * Each student has one medical record
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private Student student;

    // ========================================================================
    // BASIC MEDICAL INFORMATION
    // ========================================================================

    /**
     * Blood type (A+, A-, B+, B-, AB+, AB-, O+, O-)
     */
    @Column(name = "blood_type", length = 10)
    private String bloodType;

    /**
     * Immunization status (COMPLETE, INCOMPLETE, IN_PROGRESS, EXEMPT)
     */
    @Column(name = "immunization_status", length = 20)
    private String immunizationStatus;

    // ========================================================================
    // SUMMARY FLAGS (for quick reference)
    // ========================================================================

    /**
     * Summary flag: Does student have any allergies?
     * Set to true if food, medication, or environmental allergies exist
     */
    @Column(name = "has_allergies")
    private Boolean hasAllergies = false;

    /**
     * Summary flag: Does student have any chronic conditions?
     * Set to true if diabetes, asthma, seizures, heart condition, etc. exist
     */
    @Column(name = "has_chronic_conditions")
    private Boolean hasChronicConditions = false;

    /**
     * Summary flag: Is student currently taking medications?
     */
    @Column(name = "taking_medications")
    private Boolean takingMedications = false;

    /**
     * Summary flag: Does student have any critical medical alerts?
     */
    @Column(name = "has_medical_alerts")
    private Boolean hasMedicalAlerts = false;

    // ========================================================================
    // ALLERGIES
    // ========================================================================

    /**
     * Food allergies (comma-separated or JSON)
     * Examples: "Peanuts, Tree nuts, Shellfish"
     */
    @Column(name = "food_allergies", columnDefinition = "TEXT")
    private String foodAllergies;

    /**
     * Medication allergies
     * Examples: "Penicillin, Aspirin"
     */
    @Column(name = "medication_allergies", columnDefinition = "TEXT")
    private String medicationAllergies;

    /**
     * Environmental allergies
     * Examples: "Bee stings, Latex, Pollen"
     */
    @Column(name = "environmental_allergies", columnDefinition = "TEXT")
    private String environmentalAllergies;

    /**
     * Allergy severity level
     * NONE, MILD, MODERATE, SEVERE, LIFE_THREATENING
     */
    @Column(name = "allergy_severity")
    @Enumerated(EnumType.STRING)
    private AllergySeverity allergySeverity = AllergySeverity.NONE;

    /**
     * Does student carry EpiPen?
     */
    @Column(name = "has_epipen")
    private Boolean hasEpiPen = false;

    /**
     * EpiPen location (if applicable)
     * Example: "Nurse's office", "Carried by student"
     */
    @Column(name = "epipen_location")
    private String epiPenLocation;

    // ========================================================================
    // CHRONIC CONDITIONS
    // ========================================================================

    /**
     * General chronic conditions summary text
     * Example: "Type 1 Diabetes, Moderate Asthma, Seasonal Allergies"
     */
    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;

    /**
     * Overall condition severity level
     * NONE, MILD, MODERATE, SEVERE
     */
    @Column(name = "condition_severity", length = 20)
    private String conditionSeverity;

    /**
     * Diabetes status
     */
    @Column(name = "has_diabetes")
    private Boolean hasDiabetes = false;

    /**
     * Diabetes type (Type 1, Type 2, Gestational)
     */
    @Column(name = "diabetes_type")
    private String diabetesType;

    /**
     * Diabetes management details
     * Example: "Insulin pump, checks glucose before lunch"
     */
    @Column(name = "diabetes_management", columnDefinition = "TEXT")
    private String diabetesManagement;

    /**
     * Asthma status
     */
    @Column(name = "has_asthma")
    private Boolean hasAsthma = false;

    /**
     * Asthma severity (Mild, Moderate, Severe)
     */
    @Column(name = "asthma_severity")
    private String asthmaSeverity;

    /**
     * Asthma triggers
     * Example: "Exercise, cold air, allergens"
     */
    @Column(name = "asthma_triggers", columnDefinition = "TEXT")
    private String asthmaTriggers;

    /**
     * Does student have inhaler?
     */
    @Column(name = "has_inhaler")
    private Boolean hasInhaler = false;

    /**
     * Inhaler location
     */
    @Column(name = "inhaler_location")
    private String inhalerLocation;

    /**
     * Seizure disorder status
     */
    @Column(name = "has_seizure_disorder")
    private Boolean hasSeizureDisorder = false;

    /**
     * Seizure type and details
     */
    @Column(name = "seizure_details", columnDefinition = "TEXT")
    private String seizureDetails;

    /**
     * Heart condition status
     */
    @Column(name = "has_heart_condition")
    private Boolean hasHeartCondition = false;

    /**
     * Heart condition details
     */
    @Column(name = "heart_condition_details", columnDefinition = "TEXT")
    private String heartConditionDetails;

    /**
     * Other chronic conditions
     * Example: "ADHD, Anxiety disorder, Celiac disease"
     */
    @Column(name = "other_conditions", columnDefinition = "TEXT")
    private String otherConditions;

    // ========================================================================
    // MEDICATIONS
    // ========================================================================

    /**
     * Current medications (detailed list)
     * Format: Name, Dosage, Frequency, Purpose
     */
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    /**
     * Medication administration time (during school hours)
     * Example: "12:00 PM - Insulin", "2:00 PM - ADHD medication"
     */
    @Column(name = "medication_schedule", columnDefinition = "TEXT")
    private String medicationSchedule;

    /**
     * Does student self-administer medication?
     */
    @Column(name = "self_administers")
    private Boolean selfAdministers = false;

    /**
     * Medication can be self-administered (permission flag)
     */
    @Column(name = "medication_self_administered")
    private Boolean medicationSelfAdministered = false;

    /**
     * Medication requires school nurse to administer
     */
    @Column(name = "medication_requires_nurse")
    private Boolean medicationRequiresNurse = false;

    /**
     * Medication stored in nurse's office?
     */
    @Column(name = "medication_in_nurses_office")
    private Boolean medicationInNursesOffice = true;

    // ========================================================================
    // EMERGENCY INFORMATION
    // ========================================================================

    /**
     * Emergency action plan (quick reference)
     * What to do in case of emergency specific to this student
     */
    @Column(name = "emergency_action_plan", columnDefinition = "TEXT")
    private String emergencyActionPlan;

    /**
     * Critical medical alert (shown prominently)
     * Example: "SEVERE PEANUT ALLERGY - EPIPEN REQUIRED"
     */
    @Column(name = "medical_alert", columnDefinition = "TEXT")
    private String medicalAlert;

    /**
     * Medical alerts (detailed, may include multiple alerts)
     * Example: "Severe peanut allergy\nRequires EpiPen for bee stings"
     */
    @Column(name = "medical_alerts", columnDefinition = "TEXT")
    private String medicalAlerts;

    /**
     * Physical limitations or restrictions
     * Example: "No contact sports", "Limited physical activity"
     */
    @Column(name = "physical_restrictions", columnDefinition = "TEXT")
    private String physicalRestrictions;

    /**
     * Dietary restrictions
     * Example: "Gluten-free", "Diabetic diet", "No nuts"
     */
    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions;

    // ========================================================================
    // CONTACTS & INSURANCE
    // ========================================================================

    /**
     * Primary physician name
     */
    @Column(name = "primary_physician_name")
    private String primaryPhysicianName;

    /**
     * Primary physician phone
     */
    @Column(name = "primary_physician_phone")
    private String primaryPhysicianPhone;

    /**
     * Physician name (general field for form compatibility)
     */
    @Column(name = "physician_name")
    private String physicianName;

    /**
     * Physician phone (general field for form compatibility)
     */
    @Column(name = "physician_phone")
    private String physicianPhone;

    /**
     * Physician address
     */
    @Column(name = "physician_address", columnDefinition = "TEXT")
    private String physicianAddress;

    /**
     * Specialist physician (if applicable)
     */
    @Column(name = "specialist_name")
    private String specialistName;

    /**
     * Specialist phone
     */
    @Column(name = "specialist_phone")
    private String specialistPhone;

    /**
     * Insurance provider
     */
    @Column(name = "insurance_provider")
    private String insuranceProvider;

    /**
     * Insurance policy number
     */
    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    /**
     * Insurance group number
     */
    @Column(name = "insurance_group_number")
    private String insuranceGroupNumber;

    // ========================================================================
    // HEALTH SCREENINGS & CLEARANCES
    // ========================================================================

    /**
     * Date of last physical examination
     */
    @Column(name = "last_physical_date")
    private LocalDate lastPhysicalDate;

    /**
     * Date when next physical is required
     */
    @Column(name = "physical_required_date")
    private LocalDate physicalRequiredDate;

    /**
     * Are all required immunizations complete?
     */
    @Column(name = "immunizations_complete")
    private Boolean immunizationsComplete = false;

    /**
     * Athletic participation clearance granted?
     */
    @Column(name = "athletic_clearance")
    private Boolean athleticClearance = false;

    /**
     * Date of athletic clearance
     */
    @Column(name = "athletic_clearance_date")
    private LocalDate athleticClearanceDate;

    /**
     * Concussion protocol acknowledgment
     */
    @Column(name = "concussion_protocol")
    private Boolean concussionProtocol = false;

    // ========================================================================
    // EMERGENCY AUTHORIZATION
    // ========================================================================

    /**
     * Parent/Guardian authorizes emergency medical treatment?
     */
    @Column(name = "emergency_treatment_authorized")
    private Boolean emergencyTreatmentAuthorized = false;

    /**
     * Date of emergency treatment authorization
     */
    @Column(name = "emergency_authorization_date")
    private LocalDate emergencyAuthorizationDate;

    /**
     * Preferred hospital for emergencies
     */
    @Column(name = "preferred_hospital")
    private String preferredHospital;

    // ========================================================================
    // TRACKING & AUDIT
    // ========================================================================

    /**
     * Last medical review date
     * Should be reviewed annually
     */
    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    /**
     * Next review due date
     */
    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    /**
     * Who last updated this record
     */
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    /**
     * When was this record last updated
     */
    @Column(name = "last_updated_date")
    private LocalDate lastUpdatedDate;

    /**
     * Additional notes
     */
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    /**
     * Is this record verified by school nurse?
     */
    @Column(name = "nurse_verified")
    private Boolean nurseVerified = false;

    /**
     * Is this record verified (general verification)?
     */
    @Column(name = "verified")
    private Boolean verified = false;

    /**
     * Date when record was verified
     */
    @Column(name = "verification_date")
    private LocalDate verificationDate;

    /**
     * Parent/Guardian signature confirmation
     */
    @Column(name = "parent_signature_on_file")
    private Boolean parentSignatureOnFile = false;

    /**
     * General notes field (for form compatibility)
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if student has any medical conditions requiring attention
     */
    public boolean hasMedicalConditions() {
        return (foodAllergies != null && !foodAllergies.trim().isEmpty()) ||
               (medicationAllergies != null && !medicationAllergies.trim().isEmpty()) ||
               (environmentalAllergies != null && !environmentalAllergies.trim().isEmpty()) ||
               Boolean.TRUE.equals(hasDiabetes) ||
               Boolean.TRUE.equals(hasAsthma) ||
               Boolean.TRUE.equals(hasSeizureDisorder) ||
               Boolean.TRUE.equals(hasHeartCondition) ||
               (otherConditions != null && !otherConditions.trim().isEmpty()) ||
               (currentMedications != null && !currentMedications.trim().isEmpty());
    }

    /**
     * Check if this is a critical medical case requiring immediate attention
     */
    public boolean isCriticalCase() {
        return AllergySeverity.SEVERE.equals(allergySeverity) ||
               AllergySeverity.LIFE_THREATENING.equals(allergySeverity) ||
               Boolean.TRUE.equals(hasEpiPen) ||
               Boolean.TRUE.equals(hasSeizureDisorder) ||
               Boolean.TRUE.equals(hasHeartCondition) ||
               (medicalAlert != null && !medicalAlert.trim().isEmpty());
    }

    /**
     * Get a quick summary for tooltip display
     */
    public String getQuickSummary() {
        StringBuilder summary = new StringBuilder();

        if (medicalAlert != null && !medicalAlert.trim().isEmpty()) {
            summary.append("⚠️ ALERT: ").append(medicalAlert).append("\n\n");
        }

        if (foodAllergies != null && !foodAllergies.trim().isEmpty()) {
            summary.append("🥜 Food Allergies: ").append(foodAllergies).append("\n");
        }

        if (Boolean.TRUE.equals(hasDiabetes)) {
            summary.append("💉 Diabetes: ").append(diabetesType != null ? diabetesType : "Yes").append("\n");
        }

        if (Boolean.TRUE.equals(hasAsthma)) {
            summary.append("🫁 Asthma: ").append(asthmaSeverity != null ? asthmaSeverity : "Yes").append("\n");
        }

        if (Boolean.TRUE.equals(hasEpiPen)) {
            summary.append("💊 EpiPen: ").append(epiPenLocation != null ? epiPenLocation : "Available").append("\n");
        }

        if (currentMedications != null && !currentMedications.trim().isEmpty()) {
            summary.append("💊 Medications: Yes\n");
        }

        if (physicalRestrictions != null && !physicalRestrictions.trim().isEmpty()) {
            summary.append("⚠️ Restrictions: ").append(physicalRestrictions).append("\n");
        }

        return summary.length() > 0 ? summary.toString().trim() : "No medical conditions on record";
    }

    /**
     * Check if medical review is overdue
     */
    public boolean isReviewOverdue() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDate.now());
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum AllergySeverity {
        NONE("No known allergies"),
        MILD("Mild - Minor discomfort"),
        MODERATE("Moderate - Requires monitoring"),
        SEVERE("Severe - Requires immediate intervention"),
        LIFE_THREATENING("Life-threatening - Emergency protocol required");

        private final String description;

        AllergySeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
