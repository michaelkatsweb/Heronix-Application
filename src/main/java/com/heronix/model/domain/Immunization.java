package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immunization Entity
 * Tracks student immunization records including vaccine types, dates, doses,
 * and compliance status for state requirements.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Entity
@Table(name = "immunizations", indexes = {
    @Index(name = "idx_immunization_student", columnList = "student_id"),
    @Index(name = "idx_immunization_vaccine", columnList = "vaccine_type"),
    @Index(name = "idx_immunization_date", columnList = "administration_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Immunization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ========================================================================
    // VACCINE INFORMATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "vaccine_type", nullable = false)
    private VaccineType vaccineType;

    @Column(name = "vaccine_name")
    private String vaccineName; // Brand name (e.g., "Pfizer BioNTech")

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber; // e.g., 1 for first dose, 2 for second, etc.

    @Column(name = "total_doses_required")
    private Integer totalDosesRequired; // e.g., 2 for MMR, 4 for DTaP

    @Column(name = "administration_date", nullable = false)
    private LocalDate administrationDate;

    // ========================================================================
    // PROVIDER INFORMATION
    // ========================================================================

    @Column(name = "administered_by")
    private String administeredBy; // Physician, clinic name

    @Column(name = "administration_location")
    private String administrationLocation; // Clinic, hospital, school

    @Column(name = "lot_number")
    private String lotNumber; // Vaccine lot number

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // Vaccine expiration

    // ========================================================================
    // VERIFICATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false)
    @Builder.Default
    private VerificationMethod verificationMethod = VerificationMethod.PAPER_RECORD;

    @Column(name = "verified_by_staff_id")
    private Long verifiedByStaffId; // School staff who verified the record

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "documentation_on_file", nullable = false)
    @Builder.Default
    private Boolean documentationOnFile = false;

    @Column(name = "documentation_file_path")
    private String documentationFilePath; // Scanned immunization card

    // ========================================================================
    // COMPLIANCE
    // ========================================================================

    @Column(name = "meets_state_requirement", nullable = false)
    @Builder.Default
    private Boolean meetsStateRequirement = false;

    @Column(name = "is_medical_exemption", nullable = false)
    @Builder.Default
    private Boolean isMedicalExemption = false;

    @Column(name = "is_religious_exemption", nullable = false)
    @Builder.Default
    private Boolean isReligiousExemption = false;

    @Column(name = "is_philosophical_exemption", nullable = false)
    @Builder.Default
    private Boolean isPhilosophicalExemption = false;

    @Column(name = "exemption_documentation_on_file", nullable = false)
    @Builder.Default
    private Boolean exemptionDocumentationOnFile = false;

    @Column(name = "exemption_expiration_date")
    private LocalDate exemptionExpirationDate;

    // ========================================================================
    // NEXT DOSE
    // ========================================================================

    @Column(name = "next_dose_due_date")
    private LocalDate nextDoseDueDate;

    @Column(name = "is_booster", nullable = false)
    @Builder.Default
    private Boolean isBooster = false;

    @Column(name = "booster_due_date")
    private LocalDate boosterDueDate;

    // ========================================================================
    // REACTIONS
    // ========================================================================

    @Column(name = "adverse_reaction_reported", nullable = false)
    @Builder.Default
    private Boolean adverseReactionReported = false;

    @Column(name = "adverse_reaction_description", columnDefinition = "TEXT")
    private String adverseReactionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_severity")
    private ReactionSeverity reactionSeverity;

    // ========================================================================
    // RECORD METADATA
    // ========================================================================

    @Column(name = "entry_date", nullable = false)
    @Builder.Default
    private LocalDateTime entryDate = LocalDateTime.now();

    @Column(name = "entered_by_staff_id")
    private Long enteredByStaffId;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Additional fields for controller compatibility
    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "documentation_path")
    private String documentationPath;

    @Column(name = "medical_exemption")
    @Builder.Default
    private Boolean medicalExemption = false;

    @Column(name = "medical_exemption_reason", columnDefinition = "TEXT")
    private String medicalExemptionReason;

    @Column(name = "medical_exemption_provider")
    private String medicalExemptionProvider;

    @Column(name = "medical_exemption_date")
    private LocalDate medicalExemptionDate;

    @Column(name = "religious_exemption")
    @Builder.Default
    private Boolean religiousExemption = false;

    @Column(name = "religious_exemption_statement", columnDefinition = "TEXT")
    private String religiousExemptionStatement;

    @Column(name = "religious_exemption_date")
    private LocalDate religiousExemptionDate;

    @Column(name = "philosophical_exemption")
    @Builder.Default
    private Boolean philosophicalExemption = false;

    @Column(name = "philosophical_exemption_statement", columnDefinition = "TEXT")
    private String philosophicalExemptionStatement;

    @Column(name = "philosophical_exemption_date")
    private LocalDate philosophicalExemptionDate;

    @Column(name = "schedule_notes", columnDefinition = "TEXT")
    private String scheduleNotes;

    @Column(name = "adverse_reaction_observed")
    @Builder.Default
    private Boolean adverseReactionObserved = false;

    @Column(name = "consent_obtained")
    @Builder.Default
    private Boolean consentObtained = false;

    @Column(name = "consent_date")
    private LocalDate consentDate;

    // Additional reaction tracking fields
    @Column(name = "reaction_description", columnDefinition = "TEXT")
    private String reactionDescription;

    @Column(name = "parent_notified_of_reaction")
    @Builder.Default
    private Boolean parentNotifiedOfReaction = false;

    @Column(name = "physician_notified_of_reaction")
    @Builder.Default
    private Boolean physicianNotifiedOfReaction = false;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum VaccineType {
        // Required vaccines for school entry (typical state requirements)
        DTaP("DTaP/Tdap (Diphtheria, Tetanus, Pertussis)", true, 5),
        POLIO("IPV (Polio)", true, 4),
        MMR("MMR (Measles, Mumps, Rubella)", true, 2),
        HEPATITIS_B("Hepatitis B", true, 3),
        VARICELLA("Varicella (Chickenpox)", true, 2),
        HEPATITIS_A("Hepatitis A", false, 2),
        MENINGOCOCCAL("Meningococcal (MenACWY)", false, 2),
        HPV("HPV (Human Papillomavirus)", false, 3),

        // Additional vaccines
        HIB("Hib (Haemophilus influenzae type b)", false, 4),
        PNEUMOCOCCAL("PCV (Pneumococcal)", false, 4),
        ROTAVIRUS("Rotavirus", false, 3),
        INFLUENZA("Influenza (Flu)", false, 1),
        COVID_19("COVID-19", false, 2),

        OTHER("Other Vaccine", false, 1);

        private final String displayName;
        private final boolean requiredForSchool;
        private final int typicalDosesRequired;

        VaccineType(String displayName, boolean requiredForSchool, int typicalDosesRequired) {
            this.displayName = displayName;
            this.requiredForSchool = requiredForSchool;
            this.typicalDosesRequired = typicalDosesRequired;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isRequiredForSchool() {
            return requiredForSchool;
        }

        public int getTypicalDosesRequired() {
            return typicalDosesRequired;
        }
    }

    public enum VerificationMethod {
        PAPER_RECORD("Paper Immunization Record"),
        ELECTRONIC_RECORD("Electronic Health Record"),
        STATE_REGISTRY("State Immunization Registry"),
        PHYSICIAN_STATEMENT("Physician Statement"),
        PARENT_STATEMENT("Parent/Guardian Statement"),
        SCHOOL_RECORD("Previous School Record");

        private final String displayName;

        VerificationMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReactionSeverity {
        NONE("No Reaction"),
        MILD("Mild - Soreness, Low-Grade Fever"),
        MODERATE("Moderate - Fever, Rash, Local Swelling"),
        SEVERE("Severe - High Fever, Allergic Reaction"),
        LIFE_THREATENING("Life-Threatening - Anaphylaxis");

        private final String displayName;

        ReactionSeverity(String displayName) {
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
    public boolean isSeriesComplete() {
        return totalDosesRequired != null &&
               doseNumber >= totalDosesRequired &&
               !isBooster;
    }

    @Transient
    public boolean isOverdue() {
        return nextDoseDueDate != null && nextDoseDueDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isDueSoon() {
        return nextDoseDueDate != null &&
               nextDoseDueDate.isAfter(LocalDate.now()) &&
               nextDoseDueDate.isBefore(LocalDate.now().plusDays(30));
    }

    @Transient
    public boolean hasExemption() {
        return isMedicalExemption || isReligiousExemption || isPhilosophicalExemption;
    }

    @Transient
    public boolean isCompliant() {
        return meetsStateRequirement || hasExemption();
    }
}
