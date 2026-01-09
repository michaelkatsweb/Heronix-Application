package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Medication Administration Entity
 * Tracks each instance of medication administration including dose, time, nurse,
 * and student response. Critical for legal compliance and medication audit trail.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Entity
@Table(name = "medication_administrations", indexes = {
    @Index(name = "idx_med_admin_medication", columnList = "medication_id"),
    @Index(name = "idx_med_admin_student_date", columnList = "student_id, administration_date"),
    @Index(name = "idx_med_admin_date", columnList = "administration_date"),
    @Index(name = "idx_med_admin_nurse", columnList = "administered_by_staff_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdministration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ========================================================================
    // ADMINISTRATION DETAILS
    // ========================================================================

    @Column(name = "administration_date", nullable = false)
    private LocalDate administrationDate;

    @Column(name = "administration_time", nullable = false)
    private LocalTime administrationTime;

    @Column(name = "administration_timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime administrationTimestamp = LocalDateTime.now();

    @Column(name = "dose_given", nullable = false)
    private String doseGiven; // e.g., "10mg", "1 tablet", "2 puffs"

    @Enumerated(EnumType.STRING)
    @Column(name = "administration_route", nullable = false)
    @Builder.Default
    private AdministrationRoute administrationRoute = AdministrationRoute.ORAL;

    // ========================================================================
    // ADMINISTRATOR INFORMATION
    // ========================================================================

    @Column(name = "administered_by_staff_id", nullable = false)
    private Long administeredByStaffId; // School nurse or authorized staff

    @Column(name = "administrator_name", nullable = false)
    private String administratorName; // Cached for audit trail

    @Column(name = "administrator_title", nullable = false)
    private String administratorTitle; // e.g., "School Nurse", "RN"

    @Column(name = "witness_staff_id")
    private Long witnessStaffId; // For controlled substances

    @Column(name = "witness_name")
    private String witnessName;

    // ========================================================================
    // CONTEXT
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "administration_reason", nullable = false)
    @Builder.Default
    private AdministrationReason administrationReason = AdministrationReason.SCHEDULED;

    @Column(name = "reason_details")
    private String reasonDetails; // For PRN medications

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_visit_id")
    private NurseVisit nurseVisit; // Link to nurse visit if given during a visit

    // ========================================================================
    // STUDENT RESPONSE
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "student_response")
    private StudentResponse studentResponse;

    @Column(name = "response_notes", columnDefinition = "TEXT")
    private String responseNotes;

    @Column(name = "adverse_reaction_observed", nullable = false)
    @Builder.Default
    private Boolean adverseReactionObserved = false;

    @Column(name = "adverse_reaction_description", columnDefinition = "TEXT")
    private String adverseReactionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "adverse_reaction_severity")
    private AdverseReactionSeverity adverseReactionSeverity;

    @Column(name = "parent_notified_of_reaction", nullable = false)
    @Builder.Default
    private Boolean parentNotifiedOfReaction = false;

    @Column(name = "physician_notified_of_reaction", nullable = false)
    @Builder.Default
    private Boolean physicianNotifiedOfReaction = false;

    // ========================================================================
    // COMPLIANCE AND VERIFICATION
    // ========================================================================

    @Column(name = "student_consent_verified", nullable = false)
    @Builder.Default
    private Boolean studentConsentVerified = true;

    @Column(name = "medication_verified", nullable = false)
    @Builder.Default
    private Boolean medicationVerified = true; // Checked label, expiration, etc.

    @Column(name = "dosage_verified", nullable = false)
    @Builder.Default
    private Boolean dosageVerified = true;

    @Column(name = "student_identified", nullable = false)
    @Builder.Default
    private Boolean studentIdentified = true; // Verified student identity

    @Column(name = "refused_by_student", nullable = false)
    @Builder.Default
    private Boolean refusedByStudent = false;

    @Column(name = "refusal_reason")
    private String refusalReason;

    // ========================================================================
    // INVENTORY UPDATE
    // ========================================================================

    @Column(name = "inventory_updated", nullable = false)
    @Builder.Default
    private Boolean inventoryUpdated = false;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity; // Quantity left after this dose

    // ========================================================================
    // DOCUMENTATION
    // ========================================================================

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "signature")
    private String signature; // Digital signature or initials

    @Column(name = "parent_notification_required", nullable = false)
    @Builder.Default
    private Boolean parentNotificationRequired = false;

    @Column(name = "parent_notified", nullable = false)
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_time")
    private LocalDateTime parentNotificationTime;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum AdministrationRoute {
        ORAL("Oral (Swallowed)"),
        SUBLINGUAL("Sublingual (Under Tongue)"),
        TOPICAL("Topical (Applied to Skin)"),
        INHALED("Inhaled (Respiratory)"),
        OPHTHALMIC("Ophthalmic (Eye Drops)"),
        OTIC("Otic (Ear Drops)"),
        NASAL("Nasal (Nose Spray)"),
        RECTAL("Rectal"),
        INJECTION_IM("Injection - Intramuscular"),
        INJECTION_SC("Injection - Subcutaneous"),
        INJECTION_IV("Injection - Intravenous"),
        TRANSDERMAL("Transdermal (Patch)");

        private final String displayName;

        AdministrationRoute(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AdministrationReason {
        SCHEDULED("Scheduled Daily Medication"),
        PRN_PAIN("PRN - Pain/Headache"),
        PRN_FEVER("PRN - Fever"),
        PRN_ASTHMA("PRN - Asthma/Breathing"),
        PRN_ALLERGY("PRN - Allergic Reaction"),
        PRN_ANXIETY("PRN - Anxiety"),
        PRN_SEIZURE("PRN - Seizure Prevention"),
        PRN_DIABETES("PRN - Diabetes Management"),
        PRN_OTHER("PRN - Other Reason"),
        EMERGENCY("Emergency Administration");

        private final String displayName;

        AdministrationReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum StudentResponse {
        POSITIVE("Positive - Symptoms Improved"),
        NEUTRAL("Neutral - No Change"),
        NEGATIVE("Negative - Symptoms Worsened"),
        ADVERSE_REACTION("Adverse Reaction Observed"),
        TOO_SOON_TO_ASSESS("Too Soon to Assess");

        private final String displayName;

        StudentResponse(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AdverseReactionSeverity {
        MILD("Mild - Minor Discomfort"),
        MODERATE("Moderate - Significant Discomfort"),
        SEVERE("Severe - Medical Attention Needed"),
        LIFE_THREATENING("Life-Threatening - Emergency Response");

        private final String displayName;

        AdverseReactionSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // CALCULATED FIELDS
    // ========================================================================

    @Transient
    public boolean isEmergencyAdministration() {
        return administrationReason == AdministrationReason.EMERGENCY;
    }

    @Transient
    public boolean requiresFollowUp() {
        return adverseReactionObserved ||
               studentResponse == StudentResponse.NEGATIVE ||
               studentResponse == StudentResponse.ADVERSE_REACTION;
    }

    @Transient
    public boolean isCompliant() {
        return studentConsentVerified &&
               medicationVerified &&
               dosageVerified &&
               studentIdentified &&
               !refusedByStudent;
    }
}
