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
 * Nurse Visit Entity
 * Tracks student visits to the health office including symptoms, treatments,
 * dispositions, and parent notifications.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Entity
@Table(name = "nurse_visits", indexes = {
    @Index(name = "idx_nurse_visit_student_date", columnList = "student_id, visit_date"),
    @Index(name = "idx_nurse_visit_date", columnList = "visit_date"),
    @Index(name = "idx_nurse_visit_disposition", columnList = "disposition")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NurseVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_time", nullable = false)
    private LocalTime visitTime;

    @Column(name = "check_in_time", nullable = false)
    @Builder.Default
    private LocalDateTime checkInTime = LocalDateTime.now();

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // ========================================================================
    // VISIT REASON
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_reason", nullable = false)
    @Builder.Default
    private VisitReason visitReason = VisitReason.ILLNESS;

    @Column(name = "visit_reason_other")
    private String visitReasonOther; // Used when visitReason = OTHER

    // ========================================================================
    // SYMPTOMS
    // ========================================================================

    @Column(name = "chief_complaint", nullable = false)
    private String chiefComplaint; // Primary symptom reported by student

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms; // Detailed symptom description

    @Column(name = "has_fever", nullable = false)
    @Builder.Default
    private Boolean hasFever = false;

    @Column(name = "temperature")
    private Double temperature; // Fahrenheit

    @Column(name = "has_vomiting", nullable = false)
    @Builder.Default
    private Boolean hasVomiting = false;

    @Column(name = "has_headache", nullable = false)
    @Builder.Default
    private Boolean hasHeadache = false;

    @Column(name = "has_stomach_ache", nullable = false)
    @Builder.Default
    private Boolean hasStomachAche = false;

    @Column(name = "has_injury", nullable = false)
    @Builder.Default
    private Boolean hasInjury = false;

    @Column(name = "injury_description", columnDefinition = "TEXT")
    private String injuryDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "injury_severity")
    private InjurySeverity injurySeverity;

    // ========================================================================
    // VITAL SIGNS
    // ========================================================================

    @Column(name = "blood_pressure")
    private String bloodPressure; // e.g., "120/80"

    @Column(name = "heart_rate")
    private Integer heartRate; // beats per minute

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate; // breaths per minute

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation; // percentage

    // ========================================================================
    // TREATMENT
    // ========================================================================

    @Column(name = "treatment_provided", columnDefinition = "TEXT")
    private String treatmentProvided;

    @Column(name = "medication_administered", nullable = false)
    @Builder.Default
    private Boolean medicationAdministered = false;

    @Column(name = "medication_details")
    private String medicationDetails; // What medication and dosage

    @Column(name = "ice_applied", nullable = false)
    @Builder.Default
    private Boolean iceApplied = false;

    @Column(name = "bandage_applied", nullable = false)
    @Builder.Default
    private Boolean bandageApplied = false;

    @Column(name = "rest_period_minutes")
    private Integer restPeriodMinutes;

    // ========================================================================
    // DISPOSITION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false)
    @Builder.Default
    private Disposition disposition = Disposition.RETURNED_TO_CLASS;

    @Column(name = "disposition_time")
    private LocalTime dispositionTime;

    @Column(name = "sent_home", nullable = false)
    @Builder.Default
    private Boolean sentHome = false;

    @Column(name = "sent_home_reason")
    private String sentHomeReason;

    @Column(name = "parent_picked_up", nullable = false)
    @Builder.Default
    private Boolean parentPickedUp = false;

    @Column(name = "parent_pickup_time")
    private LocalTime parentPickupTime;

    // ========================================================================
    // PARENT COMMUNICATION
    // ========================================================================

    @Column(name = "parent_notified", nullable = false)
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_time")
    private LocalTime parentNotificationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_notification_method")
    private ContactMethod parentNotificationMethod;

    @Column(name = "parent_notification_notes", columnDefinition = "TEXT")
    private String parentNotificationNotes;

    // ========================================================================
    // FOLLOW-UP
    // ========================================================================

    @Column(name = "requires_follow_up", nullable = false)
    @Builder.Default
    private Boolean requiresFollowUp = false;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "physician_referral_needed", nullable = false)
    @Builder.Default
    private Boolean physicianReferralNeeded = false;

    @Column(name = "physician_referral_reason")
    private String physicianReferralReason;

    // ========================================================================
    // INCIDENT REPORTING
    // ========================================================================

    @Column(name = "incident_report_filed", nullable = false)
    @Builder.Default
    private Boolean incidentReportFiled = false;

    @Column(name = "incident_report_number")
    private String incidentReportNumber;

    @Column(name = "is_workers_comp", nullable = false)
    @Builder.Default
    private Boolean isWorkersComp = false; // For staff injuries

    // ========================================================================
    // NURSE INFORMATION
    // ========================================================================

    @Column(name = "attending_nurse_id", nullable = false)
    private Long attendingNurseId; // Staff ID of nurse

    @Column(name = "nurse_notes", columnDefinition = "TEXT")
    private String nurseNotes;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes; // HIPAA protected

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum VisitReason {
        ILLNESS("Illness/Not Feeling Well"),
        INJURY("Injury"),
        MEDICATION("Scheduled Medication"),
        CHRONIC_CONDITION("Chronic Condition Management"),
        MENTAL_HEALTH("Mental Health/Anxiety"),
        MENSTRUAL("Menstrual Cramps"),
        HEADACHE("Headache/Migraine"),
        DENTAL("Dental Issue"),
        VISION("Vision Problem"),
        HEARING("Hearing Problem"),
        ALLERGIC_REACTION("Allergic Reaction"),
        ASTHMA("Asthma/Breathing Difficulty"),
        DIABETES("Diabetes Management"),
        SEIZURE("Seizure"),
        OTHER("Other");

        private final String displayName;

        VisitReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InjurySeverity {
        MINOR,          // Band-aid, no treatment needed
        MODERATE,       // Ice, rest, monitoring
        SERIOUS,        // Possible fracture, deep cut, needs medical attention
        EMERGENCY       // Requires 911/immediate hospital transport
    }

    public enum Disposition {
        RETURNED_TO_CLASS("Returned to Class"),
        SENT_HOME("Sent Home"),
        PARENT_NOTIFIED_STAYED("Parent Notified - Remained at School"),
        EMERGENCY_TRANSPORT("Emergency Transport to Hospital"),
        STAYED_FOR_REST("Stayed in Health Office for Rest"),
        REFERRED_TO_PHYSICIAN("Referred to Physician"),
        NO_TREATMENT_NEEDED("No Treatment Needed");

        private final String displayName;

        Disposition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ContactMethod {
        PHONE,
        EMAIL,
        TEXT_MESSAGE,
        IN_PERSON,
        PARENT_PORTAL,
        LEFT_MESSAGE
    }

    // ========================================================================
    // CALCULATED FIELDS
    // ========================================================================

    @Transient
    public Integer getVisitDurationMinutes() {
        if (checkOutTime == null) {
            return null; // Visit still in progress
        }
        return (int) java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    @Transient
    public boolean isEmergency() {
        return injurySeverity == InjurySeverity.EMERGENCY ||
               disposition == Disposition.EMERGENCY_TRANSPORT;
    }

    @Transient
    public boolean requiresParentNotification() {
        return sentHome || hasFever || hasVomiting ||
               injurySeverity == InjurySeverity.SERIOUS ||
               injurySeverity == InjurySeverity.EMERGENCY;
    }
}
