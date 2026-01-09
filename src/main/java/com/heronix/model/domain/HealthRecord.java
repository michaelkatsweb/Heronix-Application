package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Health Record Entity
 * Stores comprehensive health information for students including medical conditions,
 * allergies, emergency contacts, and health history.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Entity
@Table(name = "health_records", indexes = {
    @Index(name = "idx_health_student", columnList = "student_id"),
    @Index(name = "idx_health_updated", columnList = "last_updated")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // ========================================================================
    // MEDICAL CONDITIONS
    // ========================================================================

    @Column(name = "has_chronic_conditions", nullable = false)
    @Builder.Default
    private Boolean hasChronicConditions = false;

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions; // e.g., "Asthma, Type 1 Diabetes"

    @Column(name = "condition_details", columnDefinition = "TEXT")
    private String conditionDetails; // Detailed description and management plan

    // ========================================================================
    // ALLERGIES
    // ========================================================================

    @Column(name = "has_allergies", nullable = false)
    @Builder.Default
    private Boolean hasAllergies = false;

    @Column(name = "food_allergies", columnDefinition = "TEXT")
    private String foodAllergies; // e.g., "Peanuts, Tree nuts, Shellfish"

    @Column(name = "medication_allergies", columnDefinition = "TEXT")
    private String medicationAllergies; // e.g., "Penicillin, Sulfa drugs"

    @Column(name = "environmental_allergies", columnDefinition = "TEXT")
    private String environmentalAllergies; // e.g., "Bee stings, Latex"

    @Enumerated(EnumType.STRING)
    @Column(name = "allergy_severity")
    private AllergySeverity allergySeverity;

    @Column(name = "has_epipen", nullable = false)
    @Builder.Default
    private Boolean hasEpipen = false;

    @Column(name = "epipen_location")
    private String epipenLocation; // Where epipen is stored at school

    // ========================================================================
    // MEDICATIONS
    // ========================================================================

    @Column(name = "takes_daily_medication", nullable = false)
    @Builder.Default
    private Boolean takesDailyMedication = false;

    @Column(name = "requires_school_medication", nullable = false)
    @Builder.Default
    private Boolean requiresSchoolMedication = false;

    // ========================================================================
    // DISABILITIES AND ACCOMMODATIONS
    // ========================================================================

    @Column(name = "has_physical_disability", nullable = false)
    @Builder.Default
    private Boolean hasPhysicalDisability = false;

    @Column(name = "physical_disability_description", columnDefinition = "TEXT")
    private String physicalDisabilityDescription;

    @Column(name = "requires_accessibility", nullable = false)
    @Builder.Default
    private Boolean requiresAccessibility = false;

    @Column(name = "accessibility_needs", columnDefinition = "TEXT")
    private String accessibilityNeeds; // e.g., "Wheelchair access, Elevator access"

    @Column(name = "has_vision_impairment", nullable = false)
    @Builder.Default
    private Boolean hasVisionImpairment = false;

    @Column(name = "has_hearing_impairment", nullable = false)
    @Builder.Default
    private Boolean hasHearingImpairment = false;

    // ========================================================================
    // MENTAL HEALTH
    // ========================================================================

    @Column(name = "has_mental_health_condition", nullable = false)
    @Builder.Default
    private Boolean hasMentalHealthCondition = false;

    @Column(name = "mental_health_notes", columnDefinition = "TEXT")
    private String mentalHealthNotes; // HIPAA protected - limited access

    @Column(name = "has_behavior_plan", nullable = false)
    @Builder.Default
    private Boolean hasBehaviorPlan = false;

    // ========================================================================
    // EMERGENCY INFORMATION
    // ========================================================================

    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship", nullable = false)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", nullable = false)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_phone_alt")
    private String emergencyContactPhoneAlt;

    @Column(name = "emergency_hospital_preference")
    private String emergencyHospitalPreference;

    @Column(name = "primary_physician_name")
    private String primaryPhysicianName;

    @Column(name = "primary_physician_phone")
    private String primaryPhysicianPhone;

    // ========================================================================
    // INSURANCE INFORMATION
    // ========================================================================

    @Column(name = "has_health_insurance", nullable = false)
    @Builder.Default
    private Boolean hasHealthInsurance = false;

    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    // ========================================================================
    // SCREENING HISTORY
    // ========================================================================

    @Column(name = "last_vision_screening_date")
    private LocalDate lastVisionScreeningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_vision_screening_result")
    private ScreeningResult lastVisionScreeningResult;

    @Column(name = "last_hearing_screening_date")
    private LocalDate lastHearingScreeningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_hearing_screening_result")
    private ScreeningResult lastHearingScreeningResult;

    @Column(name = "last_physical_exam_date")
    private LocalDate lastPhysicalExamDate;

    @Column(name = "last_dental_exam_date")
    private LocalDate lastDentalExamDate;

    // ========================================================================
    // CONSENTS AND PERMISSIONS
    // ========================================================================

    @Column(name = "photo_consent", nullable = false)
    @Builder.Default
    private Boolean photoConsent = false;

    @Column(name = "field_trip_medical_consent", nullable = false)
    @Builder.Default
    private Boolean fieldTripMedicalConsent = false;

    @Column(name = "otc_medication_consent", nullable = false)
    @Builder.Default
    private Boolean otcMedicationConsent = false; // Over-the-counter (Tylenol, etc.)

    // ========================================================================
    // RECORD METADATA
    // ========================================================================

    @Column(name = "record_complete", nullable = false)
    @Builder.Default
    private Boolean recordComplete = false;

    @Column(name = "last_updated", nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "updated_by_staff_id")
    private Long updatedByStaffId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // General health notes for school staff

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes; // HIPAA protected - nurse only

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum AllergySeverity {
        MILD,           // Minor reactions, monitoring only
        MODERATE,       // May require medication
        SEVERE,         // Life-threatening, requires EpiPen
        ANAPHYLACTIC    // Critical - immediate emergency response
    }

    public enum ScreeningResult {
        PASS,
        REFER,          // Needs follow-up
        FAIL,
        NOT_COMPLETED
    }

    // ========================================================================
    // CALCULATED FIELDS
    // ========================================================================

    @Transient
    public boolean requiresImmediateAccessToMedication() {
        return hasEpipen || (hasChronicConditions && takesDailyMedication);
    }

    @Transient
    public boolean hasHighRiskCondition() {
        return allergySeverity == AllergySeverity.SEVERE ||
               allergySeverity == AllergySeverity.ANAPHYLACTIC ||
               (hasChronicConditions && chronicConditions != null &&
                   (chronicConditions.toLowerCase().contains("diabetes") ||
                    chronicConditions.toLowerCase().contains("seizure") ||
                    chronicConditions.toLowerCase().contains("heart")));
    }

    @Transient
    public boolean needsVisionScreening() {
        return lastVisionScreeningDate == null ||
               lastVisionScreeningDate.isBefore(LocalDate.now().minusYears(1)) ||
               lastVisionScreeningResult == ScreeningResult.REFER;
    }

    @Transient
    public boolean needsHearingScreening() {
        return lastHearingScreeningDate == null ||
               lastHearingScreeningDate.isBefore(LocalDate.now().minusYears(1)) ||
               lastHearingScreeningResult == ScreeningResult.REFER;
    }
}
