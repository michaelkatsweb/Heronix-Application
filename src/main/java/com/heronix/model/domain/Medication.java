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
 * Medication Entity
 * Tracks student medications including prescriptions, dosages, administration schedules,
 * and authorization documentation.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Entity
@Table(name = "medications", indexes = {
    @Index(name = "idx_medication_student", columnList = "student_id"),
    @Index(name = "idx_medication_active", columnList = "active, student_id"),
    @Index(name = "idx_medication_schedule", columnList = "administration_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ========================================================================
    // MEDICATION INFORMATION
    // ========================================================================

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "medication_type", nullable = false)
    @Builder.Default
    private MedicationType medicationType = MedicationType.PRESCRIPTION;

    @Column(name = "dosage", nullable = false)
    private String dosage; // e.g., "10mg", "1 tablet", "2 puffs"

    @Enumerated(EnumType.STRING)
    @Column(name = "dosage_form", nullable = false)
    @Builder.Default
    private DosageForm dosageForm = DosageForm.TABLET;

    @Column(name = "purpose", nullable = false)
    private String purpose; // What the medication treats

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions; // Special administration instructions

    // ========================================================================
    // SCHEDULE
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "administration_frequency", nullable = false)
    @Builder.Default
    private AdministrationFrequency administrationFrequency = AdministrationFrequency.DAILY;

    @Column(name = "administration_time")
    private LocalTime administrationTime; // Specific time if scheduled

    @Column(name = "administer_monday", nullable = false)
    @Builder.Default
    private Boolean administerMonday = true;

    @Column(name = "administer_tuesday", nullable = false)
    @Builder.Default
    private Boolean administerTuesday = true;

    @Column(name = "administer_wednesday", nullable = false)
    @Builder.Default
    private Boolean administerWednesday = true;

    @Column(name = "administer_thursday", nullable = false)
    @Builder.Default
    private Boolean administerThursday = true;

    @Column(name = "administer_friday", nullable = false)
    @Builder.Default
    private Boolean administerFriday = true;

    @Column(name = "as_needed", nullable = false)
    @Builder.Default
    private Boolean asNeeded = false; // PRN (Pro Re Nata)

    @Column(name = "as_needed_trigger")
    private String asNeededTrigger; // e.g., "For headache", "During asthma attack"

    // ========================================================================
    // AUTHORIZATION
    // ========================================================================

    @Column(name = "prescribing_physician", nullable = false)
    private String prescribingPhysician;

    @Column(name = "physician_phone")
    private String physicianPhone;

    @Column(name = "prescription_date")
    private LocalDate prescriptionDate;

    @Column(name = "authorization_form_on_file", nullable = false)
    @Builder.Default
    private Boolean authorizationFormOnFile = false;

    @Column(name = "authorization_form_path")
    private String authorizationFormPath; // File path to signed authorization

    @Column(name = "parent_consent_received", nullable = false)
    @Builder.Default
    private Boolean parentConsentReceived = false;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    // ========================================================================
    // VALIDITY PERIOD
    // ========================================================================

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Null = ongoing

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "discontinuation_date")
    private LocalDate discontinuationDate;

    @Column(name = "discontinuation_reason")
    private String discontinuationReason;

    // ========================================================================
    // STORAGE AND ADMINISTRATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_location", nullable = false)
    @Builder.Default
    private StorageLocation storageLocation = StorageLocation.NURSE_OFFICE;

    @Column(name = "storage_location_details")
    private String storageLocationDetails; // Specific cabinet, locker, etc.

    @Column(name = "requires_refrigeration", nullable = false)
    @Builder.Default
    private Boolean requiresRefrigeration = false;

    @Column(name = "controlled_substance", nullable = false)
    @Builder.Default
    private Boolean controlledSubstance = false;

    @Column(name = "student_self_administers", nullable = false)
    @Builder.Default
    private Boolean studentSelfAdministers = false;

    @Column(name = "nurse_administers", nullable = false)
    @Builder.Default
    private Boolean nurseAdministers = true;

    // ========================================================================
    // INVENTORY
    // ========================================================================

    @Column(name = "quantity_on_hand")
    private Integer quantityOnHand;

    @Column(name = "quantity_unit")
    private String quantityUnit; // e.g., "tablets", "doses", "ml"

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold; // Alert when quantity falls below this

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // ========================================================================
    // SIDE EFFECTS AND WARNINGS
    // ========================================================================

    @Column(name = "common_side_effects", columnDefinition = "TEXT")
    private String commonSideEffects;

    @Column(name = "serious_side_effects", columnDefinition = "TEXT")
    private String seriousSideEffects;

    @Column(name = "contraindications", columnDefinition = "TEXT")
    private String contraindications;

    @Column(name = "special_warnings", columnDefinition = "TEXT")
    private String specialWarnings;

    // ========================================================================
    // RECORD METADATA
    // ========================================================================

    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "created_by_staff_id")
    private Long createdByStaffId;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "updated_by_staff_id")
    private Long updatedByStaffId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum MedicationType {
        PRESCRIPTION("Prescription Medication"),
        OTC("Over-the-Counter"),
        SUPPLEMENT("Vitamin/Supplement"),
        HERBAL("Herbal Remedy");

        private final String displayName;

        MedicationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DosageForm {
        TABLET("Tablet"),
        CAPSULE("Capsule"),
        LIQUID("Liquid/Syrup"),
        INJECTION("Injection"),
        INHALER("Inhaler"),
        DROPS("Eye/Ear Drops"),
        CREAM("Cream/Ointment"),
        PATCH("Transdermal Patch"),
        SPRAY("Nasal Spray"),
        CHEWABLE("Chewable Tablet"),
        SUBLINGUAL("Sublingual/Under Tongue"),
        OTHER("Other");

        private final String displayName;

        DosageForm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AdministrationFrequency {
        DAILY("Once Daily"),
        TWICE_DAILY("Twice Daily (BID)"),
        THREE_TIMES_DAILY("Three Times Daily (TID)"),
        FOUR_TIMES_DAILY("Four Times Daily (QID)"),
        WEEKLY("Weekly"),
        AS_NEEDED("As Needed (PRN)"),
        SPECIFIC_TIMES("At Specific Times"),
        OTHER("Other Schedule");

        private final String displayName;

        AdministrationFrequency(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum StorageLocation {
        NURSE_OFFICE("Nurse Office - Secure Cabinet"),
        CLASSROOM("Classroom - Teacher Supervised"),
        STUDENT_CARRIES("Student Carries (Self-Administration)"),
        REFRIGERATOR("Nurse Office - Refrigerator"),
        LOCKED_CABINET("Nurse Office - Locked Cabinet (Controlled)"),
        COUNSELOR_OFFICE("Counselor Office"),
        OTHER("Other Location");

        private final String displayName;

        StorageLocation(String displayName) {
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
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isExpiringSoon() {
        return expirationDate != null &&
               expirationDate.isAfter(LocalDate.now()) &&
               expirationDate.isBefore(LocalDate.now().plusDays(30));
    }

    @Transient
    public boolean isLowStock() {
        return quantityOnHand != null && reorderThreshold != null &&
               quantityOnHand <= reorderThreshold;
    }

    @Transient
    public boolean isActiveToday() {
        LocalDate today = LocalDate.now();
        return active &&
               !today.isBefore(startDate) &&
               (endDate == null || !today.isAfter(endDate)) &&
               !isExpired();
    }

    @Transient
    public boolean requiresAdministrationToday() {
        if (!isActiveToday() || asNeeded) {
            return false;
        }

        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        return switch (dayOfWeek) {
            case 1 -> administerMonday;
            case 2 -> administerTuesday;
            case 3 -> administerWednesday;
            case 4 -> administerThursday;
            case 5 -> administerFriday;
            default -> false;
        };
    }
}
