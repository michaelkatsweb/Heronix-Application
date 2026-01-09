package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Health Incident Entity
 * Tracks health-related incidents including injuries, communicable diseases, and emergency events
 * Supports state reporting requirements and incident documentation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "health_incidents", indexes = {
    @Index(name = "idx_health_incident_student", columnList = "student_id"),
    @Index(name = "idx_health_incident_type", columnList = "incident_type"),
    @Index(name = "idx_health_incident_date", columnList = "incident_date"),
    @Index(name = "idx_health_incident_severity", columnList = "severity"),
    @Index(name = "idx_health_incident_reportable", columnList = "state_reportable")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Teacher staff; // For staff incidents

    // Incident Details
    @Column(name = "incident_number", unique = true, length = 50)
    private String incidentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 30)
    private IncidentType incidentType;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time", nullable = false)
    private LocalTime incidentTime;

    @Column(name = "incident_location", nullable = false, length = 200)
    private String incidentLocation;

    @Column(name = "incident_description", columnDefinition = "TEXT")
    private String incidentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    @Builder.Default
    private IncidentSeverity severity = IncidentSeverity.MINOR;

    // Injury-Specific Details
    @Enumerated(EnumType.STRING)
    @Column(name = "injury_type", length = 30)
    private InjuryType injuryType;

    @Column(name = "body_part_injured", length = 100)
    private String bodyPartInjured;

    @Column(name = "injury_description", columnDefinition = "TEXT")
    private String injuryDescription;

    @Column(name = "how_injury_occurred", columnDefinition = "TEXT")
    private String howInjuryOccurred;

    @Column(name = "witnessed")
    @Builder.Default
    private Boolean witnessed = false;

    @Column(name = "witness_names", columnDefinition = "TEXT")
    private String witnessNames;

    // Communicable Disease Specific
    @Enumerated(EnumType.STRING)
    @Column(name = "disease_type", length = 30)
    private CommunicableDisease diseaseType;

    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "symptom_onset_date")
    private LocalDate symptomOnsetDate;

    @Column(name = "diagnosis_confirmed")
    @Builder.Default
    private Boolean diagnosisConfirmed = false;

    @Column(name = "diagnosed_by", length = 100)
    private String diagnosedBy;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Column(name = "contagious_period_start")
    private LocalDate contagiousPeriodStart;

    @Column(name = "contagious_period_end")
    private LocalDate contagiousPeriodEnd;

    @Column(name = "isolation_required")
    @Builder.Default
    private Boolean isolationRequired = false;

    @Column(name = "isolation_start_date")
    private LocalDate isolationStartDate;

    @Column(name = "isolation_end_date")
    private LocalDate isolationEndDate;

    @Column(name = "return_to_school_date")
    private LocalDate returnToSchoolDate;

    @Column(name = "return_to_school_clearance_required")
    @Builder.Default
    private Boolean returnToSchoolClearanceRequired = false;

    @Column(name = "clearance_received")
    @Builder.Default
    private Boolean clearanceReceived = false;

    @Column(name = "clearance_file_path", length = 500)
    private String clearanceFilePath;

    // Treatment Provided
    @Column(name = "treatment_provided", columnDefinition = "TEXT")
    private String treatmentProvided;

    @Column(name = "first_aid_administered")
    @Builder.Default
    private Boolean firstAidAdministered = false;

    @Column(name = "medication_administered")
    @Builder.Default
    private Boolean medicationAdministered = false;

    @Column(name = "medication_details", length = 300)
    private String medicationDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treating_nurse_id")
    private Teacher treatingNurse;

    @Column(name = "treating_nurse_name", length = 100)
    private String treatingNurseName;

    @Column(name = "treatment_time")
    private LocalTime treatmentTime;

    // Disposition
    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false, length = 30)
    @Builder.Default
    private Disposition disposition = Disposition.RETURNED_TO_CLASS;

    @Column(name = "sent_to_hospital")
    @Builder.Default
    private Boolean sentToHospital = false;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "ambulance_called")
    @Builder.Default
    private Boolean ambulanceCalled = false;

    @Column(name = "ambulance_time")
    private LocalTime ambulanceTime;

    @Column(name = "hospital_transport_time")
    private LocalTime hospitalTransportTime;

    @Column(name = "sent_home")
    @Builder.Default
    private Boolean sentHome = false;

    @Column(name = "sent_home_time")
    private LocalTime sentHomeTime;

    @Column(name = "returned_to_class")
    @Builder.Default
    private Boolean returnedToClass = false;

    @Column(name = "returned_to_class_time")
    private LocalTime returnedToClassTime;

    // Parent/Guardian Notification
    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_time")
    private LocalTime parentNotificationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_notification_method", length = 30)
    private NotificationMethod parentNotificationMethod;

    @Column(name = "parent_notified_by", length = 100)
    private String parentNotifiedBy;

    @Column(name = "parent_response", columnDefinition = "TEXT")
    private String parentResponse;

    @Column(name = "parent_pickup")
    @Builder.Default
    private Boolean parentPickup = false;

    @Column(name = "parent_pickup_time")
    private LocalTime parentPickupTime;

    @Column(name = "picked_up_by", length = 100)
    private String pickedUpBy;

    // Follow-up
    @Column(name = "requires_follow_up")
    @Builder.Default
    private Boolean requiresFollowUp = false;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "physician_visit_recommended")
    @Builder.Default
    private Boolean physicianVisitRecommended = false;

    @Column(name = "physician_visit_completed")
    @Builder.Default
    private Boolean physicianVisitCompleted = false;

    @Column(name = "physician_visit_date")
    private LocalDate physicianVisitDate;

    @Column(name = "physician_findings", columnDefinition = "TEXT")
    private String physicianFindings;

    // State and Health Department Reporting
    @Column(name = "state_reportable")
    @Builder.Default
    private Boolean stateReportable = false;

    @Column(name = "state_reported")
    @Builder.Default
    private Boolean stateReported = false;

    @Column(name = "state_report_date")
    private LocalDate stateReportDate;

    @Column(name = "state_report_number", length = 100)
    private String stateReportNumber;

    @Column(name = "health_department_notified")
    @Builder.Default
    private Boolean healthDepartmentNotified = false;

    @Column(name = "health_department_notification_date")
    private LocalDate healthDepartmentNotificationDate;

    @Column(name = "health_department_case_number", length = 100)
    private String healthDepartmentCaseNumber;

    // Contact Tracing (for communicable diseases)
    @Column(name = "contact_tracing_required")
    @Builder.Default
    private Boolean contactTracingRequired = false;

    @Column(name = "contact_tracing_completed")
    @Builder.Default
    private Boolean contactTracingCompleted = false;

    @Column(name = "exposed_students_count")
    private Integer exposedStudentsCount;

    @Column(name = "exposed_staff_count")
    private Integer exposedStaffCount;

    @Column(name = "notifications_sent")
    @Builder.Default
    private Boolean notificationsSent = false;

    @Column(name = "notification_sent_date")
    private LocalDate notificationSentDate;

    // Workers Compensation (for staff)
    @Column(name = "workers_comp_claim")
    @Builder.Default
    private Boolean workersCompClaim = false;

    @Column(name = "workers_comp_claim_number", length = 100)
    private String workersCompClaimNumber;

    @Column(name = "workers_comp_filed_date")
    private LocalDate workersCompFiledDate;

    // Documentation
    @Column(name = "incident_report_completed")
    @Builder.Default
    private Boolean incidentReportCompleted = false;

    @Column(name = "incident_report_completion_date")
    private LocalDate incidentReportCompletionDate;

    @Column(name = "incident_report_file_path", length = 500)
    private String incidentReportFilePath;

    @Column(name = "photos_taken")
    @Builder.Default
    private Boolean photosTaken = false;

    @Column(name = "photos_file_path", length = 500)
    private String photosFilePath;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum IncidentType {
        INJURY("Injury/Accident"),
        COMMUNICABLE_DISEASE("Communicable Disease"),
        ALLERGIC_REACTION("Allergic Reaction"),
        ASTHMA_ATTACK("Asthma Attack"),
        SEIZURE("Seizure"),
        DIABETIC_EMERGENCY("Diabetic Emergency"),
        HEAD_INJURY("Head Injury/Concussion"),
        MEDICAL_EMERGENCY("Medical Emergency"),
        MENTAL_HEALTH_CRISIS("Mental Health Crisis"),
        EXPOSURE("Exposure (Bodily Fluids, etc.)"),
        OTHER("Other Health Incident");

        private final String displayName;

        IncidentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum IncidentSeverity {
        MINOR("Minor - First Aid Only"),
        MODERATE("Moderate - Medical Attention"),
        SERIOUS("Serious - Emergency Care"),
        CRITICAL("Critical - Life-Threatening");

        private final String displayName;

        IncidentSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InjuryType {
        CUT_LACERATION("Cut/Laceration"),
        BRUISE_CONTUSION("Bruise/Contusion"),
        SPRAIN_STRAIN("Sprain/Strain"),
        FRACTURE("Fracture (Suspected)"),
        BURN("Burn"),
        SCRAPE_ABRASION("Scrape/Abrasion"),
        BITE("Bite (Animal/Human)"),
        HEAD_INJURY("Head Injury"),
        CONCUSSION("Concussion (Suspected)"),
        DENTAL_INJURY("Dental Injury"),
        EYE_INJURY("Eye Injury"),
        PUNCTURE_WOUND("Puncture Wound"),
        CHOKING("Choking"),
        ALLERGIC_REACTION("Allergic Reaction"),
        OTHER("Other Injury");

        private final String displayName;

        InjuryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CommunicableDisease {
        COVID_19("COVID-19"),
        INFLUENZA("Influenza (Flu)"),
        STREP_THROAT("Strep Throat"),
        CHICKENPOX("Chickenpox (Varicella)"),
        MEASLES("Measles"),
        MUMPS("Mumps"),
        RUBELLA("Rubella"),
        WHOOPING_COUGH("Whooping Cough (Pertussis)"),
        MENINGITIS("Meningitis"),
        PINK_EYE("Pink Eye (Conjunctivitis)"),
        HAND_FOOT_MOUTH("Hand, Foot & Mouth Disease"),
        IMPETIGO("Impetigo"),
        RINGWORM("Ringworm"),
        SCABIES("Scabies"),
        HEAD_LICE("Head Lice (Pediculosis)"),
        TUBERCULOSIS("Tuberculosis"),
        HEPATITIS_A("Hepatitis A"),
        HEPATITIS_B("Hepatitis B"),
        FIFTH_DISEASE("Fifth Disease (Parvovirus B19)"),
        MONONUCLEOSIS("Mononucleosis"),
        RSV("RSV (Respiratory Syncytial Virus)"),
        NOROVIRUS("Norovirus"),
        OTHER("Other Communicable Disease");

        private final String displayName;

        CommunicableDisease(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Disposition {
        RETURNED_TO_CLASS("Returned to Class"),
        SENT_HOME("Sent Home"),
        EMERGENCY_TRANSPORT("Emergency Transport to Hospital"),
        PARENT_PICKUP("Parent Pickup"),
        STAYED_IN_HEALTH_OFFICE("Stayed in Health Office"),
        REFERRED_TO_PHYSICIAN("Referred to Physician"),
        ISOLATION("Isolation/Quarantine"),
        NO_ACTION_NEEDED("No Action Needed");

        private final String displayName;

        Disposition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NotificationMethod {
        PHONE("Phone Call"),
        EMAIL("Email"),
        TEXT("Text Message"),
        IN_PERSON("In-Person"),
        LEFT_MESSAGE("Left Message"),
        PARENT_PORTAL("Parent Portal"),
        NOT_REACHED("Unable to Reach");

        private final String displayName;

        NotificationMethod(String displayName) {
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
    public boolean isEmergency() {
        return severity == IncidentSeverity.CRITICAL ||
               severity == IncidentSeverity.SERIOUS ||
               ambulanceCalled ||
               sentToHospital;
    }

    @Transient
    public boolean isCommunicableDisease() {
        return incidentType == IncidentType.COMMUNICABLE_DISEASE;
    }

    @Transient
    public boolean needsStateReporting() {
        return stateReportable && !stateReported;
    }

    @Transient
    public boolean needsHealthDepartmentNotification() {
        return stateReportable && !healthDepartmentNotified;
    }

    @Transient
    public boolean needsContactTracing() {
        return contactTracingRequired && !contactTracingCompleted;
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotified && (sentHome || sentToHospital ||
               severity == IncidentSeverity.SERIOUS ||
               severity == IncidentSeverity.CRITICAL);
    }

    @Transient
    public boolean needsIncidentReport() {
        return !incidentReportCompleted;
    }

    @Transient
    public boolean needsPhysicianClearance() {
        return returnToSchoolClearanceRequired && !clearanceReceived;
    }

    @Transient
    public boolean isInjury() {
        return incidentType == IncidentType.INJURY ||
               incidentType == IncidentType.HEAD_INJURY;
    }

    @Transient
    public boolean isInIsolation() {
        if (isolationStartDate == null) return false;
        LocalDate today = LocalDate.now();
        return !today.isBefore(isolationStartDate) &&
               (isolationEndDate == null || !today.isAfter(isolationEndDate));
    }

    @Transient
    public int getDaysSinceIncident() {
        if (incidentDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - incidentDate.toEpochDay());
    }
}
