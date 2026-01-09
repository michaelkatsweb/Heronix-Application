package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Health Screening Entity
 * Tracks health screenings including vision, hearing, dental, and scoliosis
 * Supports screening scheduling, results entry, and follow-up tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "health_screenings", indexes = {
    @Index(name = "idx_health_screening_student", columnList = "student_id"),
    @Index(name = "idx_health_screening_type", columnList = "screening_type"),
    @Index(name = "idx_health_screening_date", columnList = "screening_date"),
    @Index(name = "idx_health_screening_status", columnList = "screening_status"),
    @Index(name = "idx_health_screening_result", columnList = "result")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScreening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Screening Details
    @Enumerated(EnumType.STRING)
    @Column(name = "screening_type", nullable = false, length = 30)
    private ScreeningType screeningType;

    @Column(name = "screening_date", nullable = false)
    private LocalDate screeningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_status", nullable = false, length = 30)
    @Builder.Default
    private ScreeningStatus screeningStatus = ScreeningStatus.SCHEDULED;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    // Screener Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screener_id")
    private Teacher screener;

    @Column(name = "screener_name", length = 100)
    private String screenerName;

    @Column(name = "screener_title", length = 100)
    private String screenerTitle;

    @Column(name = "screening_location", length = 200)
    private String screeningLocation;

    @Column(name = "screened_by", length = 100)
    private String screenedBy;

    @Column(name = "screened_by_staff_id")
    private Long screenedByStaffId;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "grade_level_at_screening")
    private Integer gradeLevelAtScreening;

    //Results
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 30)
    private ScreeningResult result;

    @Column(name = "result_details", columnDefinition = "TEXT")
    private String resultDetails;

    // Vision Screening Specific
    @Column(name = "right_eye_result", length = 50)
    private String rightEyeResult;

    @Column(name = "left_eye_result", length = 50)
    private String leftEyeResult;

    @Column(name = "visual_acuity_right", length = 20)
    private String visualAcuityRight; // e.g., "20/20", "20/40"

    @Column(name = "visual_acuity_left", length = 20)
    private String visualAcuityLeft;

    @Column(name = "color_vision_tested")
    @Builder.Default
    private Boolean colorVisionTested = false;

    @Column(name = "color_vision_result", length = 50)
    private String colorVisionResult;

    @Column(name = "glasses_worn_for_test")
    @Builder.Default
    private Boolean glassesWornForTest = false;

    @Column(name = "right_eye_vision", length = 50)
    private String rightEyeVision;

    @Column(name = "left_eye_vision", length = 50)
    private String leftEyeVision;

    @Column(name = "both_eyes_vision", length = 50)
    private String bothEyesVision;

    @Column(name = "wears_glasses")
    @Builder.Default
    private Boolean wearsGlasses = false;

    @Column(name = "vision_concerns")
    @Builder.Default
    private Boolean visionConcerns = false;

    @Column(name = "vision_notes", columnDefinition = "TEXT")
    private String visionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "vision_result", length = 30)
    private VisionResult visionResult;

    @Column(name = "left_eye_distance", length = 50)
    private String leftEyeDistance;

    @Column(name = "right_eye_distance", length = 50)
    private String rightEyeDistance;

    @Column(name = "left_eye_near", length = 50)
    private String leftEyeNear;

    @Column(name = "right_eye_near", length = 50)
    private String rightEyeNear;

    @Column(name = "color_blindness")
    @Builder.Default
    private Boolean colorBlindness = false;

    @Column(name = "vision_findings", columnDefinition = "TEXT")
    private String visionFindings;

    @Column(name = "vision_screening_date")
    private LocalDate visionScreeningDate;

    @Column(name = "with_glasses")
    @Builder.Default
    private Boolean withGlasses = false;

    @Column(name = "with_contacts")
    @Builder.Default
    private Boolean withContacts = false;

    // Hearing Screening Specific
    @Column(name = "right_ear_result", length = 50)
    private String rightEarResult;

    @Column(name = "left_ear_result", length = 50)
    private String leftEarResult;

    @Column(name = "decibel_level_tested")
    private Integer decibelLevelTested;

    @Column(name = "frequency_500hz_right", length = 20)
    private String frequency500hzRight;

    @Column(name = "frequency_1000hz_right", length = 20)
    private String frequency1000hzRight;

    @Column(name = "frequency_2000hz_right", length = 20)
    private String frequency2000hzRight;

    @Column(name = "frequency_4000hz_right", length = 20)
    private String frequency4000hzRight;

    @Column(name = "frequency_500hz_left", length = 20)
    private String frequency500hzLeft;

    @Column(name = "frequency_1000hz_left", length = 20)
    private String frequency1000hzLeft;

    @Column(name = "frequency_2000hz_left", length = 20)
    private String frequency2000hzLeft;

    @Column(name = "frequency_4000hz_left", length = 20)
    private String frequency4000hzLeft;

    @Column(name = "right_ear_frequencies", length = 200)
    private String rightEarFrequencies;

    @Column(name = "left_ear_frequencies", length = 200)
    private String leftEarFrequencies;

    @Column(name = "uses_hearing_aid")
    @Builder.Default
    private Boolean usesHearingAid = false;

    @Column(name = "hearing_concerns")
    @Builder.Default
    private Boolean hearingConcerns = false;

    @Column(name = "hearing_notes", columnDefinition = "TEXT")
    private String hearingNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "hearing_result", length = 30)
    private HearingResult hearingResult;

    @Column(name = "posture_screening_date")
    private LocalDate postureScreeningDate;

    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = false;

    // Dental Screening Specific
    @Column(name = "cavities_detected")
    @Builder.Default
    private Boolean cavitiesDetected = false;

    @Column(name = "cavities_count")
    private Integer cavitiesCount;

    @Column(name = "gum_disease_detected")
    @Builder.Default
    private Boolean gumDiseaseDetected = false;

    @Column(name = "orthodontic_needs")
    @Builder.Default
    private Boolean orthodonticNeeds = false;

    @Column(name = "dental_hygiene_rating", length = 30)
    private String dentalHygieneRating; // Poor, Fair, Good, Excellent

    @Column(name = "obvious_cavities")
    @Builder.Default
    private Boolean obviousCavities = false;

    @Column(name = "gum_disease")
    @Builder.Default
    private Boolean gumDisease = false;

    @Column(name = "oral_pain")
    @Builder.Default
    private Boolean oralPain = false;

    @Column(name = "needs_dental_care")
    @Builder.Default
    private Boolean needsDentalCare = false;

    @Column(name = "dental_findings", columnDefinition = "TEXT")
    private String dentalFindings;

    // Scoliosis Screening Specific
    @Column(name = "spine_curvature_detected")
    @Builder.Default
    private Boolean spineCurvatureDetected = false;

    @Column(name = "curvature_degree")
    private Integer curvatureDegree; // Degrees of curvature

    @Column(name = "curvature_location", length = 100)
    private String curvatureLocation; // Thoracic, Lumbar, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "curvature_severity", length = 30)
    private CurvatureSeverity curvatureSeverity;

    @Enumerated(EnumType.STRING)
    @Column(name = "adams_bend_test", length = 30)
    private ScoliosisResult adamsBendTest;

    @Enumerated(EnumType.STRING)
    @Column(name = "rib_hump", length = 30)
    private RibHumpSeverity ribHump;

    @Column(name = "shoulder_asymmetry")
    @Builder.Default
    private Boolean shoulderAsymmetry = false;

    @Column(name = "hip_asymmetry")
    @Builder.Default
    private Boolean hipAsymmetry = false;

    @Column(name = "spinal_curvature")
    @Builder.Default
    private Boolean spinalCurvature = false;

    @Column(name = "scoliosis_findings", columnDefinition = "TEXT")
    private String scoliosisFindings;

    @Column(name = "scoliosis_screening_date")
    private LocalDate scoliosisScreeningDate;

    @Column(name = "forward_bend_test")
    @Builder.Default
    private Boolean forwardBendTest = false;

    @Column(name = "rib_hump_present")
    @Builder.Default
    private Boolean ribHumpPresent = false;

    // BMI Screening Specific
    @Column(name = "height_inches")
    private Double heightInches;

    @Column(name = "weight_pounds")
    private Double weightPounds;

    @Column(name = "bmi")
    private Double bmi;

    @Enumerated(EnumType.STRING)
    @Column(name = "bmi_percentile_category", length = 30)
    private BMICategory bmiPercentileCategory;

    @Column(name = "bmi_percentile")
    private Integer bmiPercentile;

    @Column(name = "bmi_percentile_string", length = 50)
    private String bmiPercentileString;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "bmi_notes", columnDefinition = "TEXT")
    private String bmiNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "bmi_category", length = 30)
    private BMICategory bmiCategory;

    // Blood Pressure Screening Specific
    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure; // e.g., "120/80"

    @Enumerated(EnumType.STRING)
    @Column(name = "bp_category", length = 30)
    private BloodPressureCategory bpCategory;

    // Follow-up
    @Column(name = "requires_follow_up")
    @Builder.Default
    private Boolean requiresFollowUp = false;

    @Column(name = "follow_up_reason", columnDefinition = "TEXT")
    private String followUpReason;

    @Column(name = "referral_needed")
    @Builder.Default
    private Boolean referralNeeded = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", length = 30)
    private ReferralType referralType;

    @Column(name = "referral_provider", length = 200)
    private String referralProvider;

    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "referral_completed")
    @Builder.Default
    private Boolean referralCompleted = false;

    @Column(name = "referral_completion_date")
    private LocalDate referralCompletionDate;

    @Column(name = "referral_results", columnDefinition = "TEXT")
    private String referralResults;

    @Column(name = "referral_provided")
    @Builder.Default
    private Boolean referralProvided = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "referral_reason", columnDefinition = "TEXT")
    private String referralReason;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    // Parent Notification
    @Column(name = "parent_notification_required")
    @Builder.Default
    private Boolean parentNotificationRequired = false;

    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_notification_method", length = 30)
    private NotificationMethod parentNotificationMethod;

    @Column(name = "parent_notification_notes", columnDefinition = "TEXT")
    private String parentNotificationNotes;

    @Column(name = "notification_date")
    private LocalDate notificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_method", length = 30)
    private NotificationMethod notificationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_result", length = 30)
    private OverallResult overallResult;

    @Column(name = "meets_state_requirements")
    @Builder.Default
    private Boolean meetsStateRequirements = false;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // Documentation
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Column(name = "documentation_file_path", length = 500)
    private String documentationFilePath;

    // State Reporting
    @Column(name = "state_reported")
    @Builder.Default
    private Boolean stateReported = false;

    @Column(name = "state_report_date")
    private LocalDate stateReportDate;

    @Column(name = "state_report_id", length = 100)
    private String stateReportId;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum ScreeningType {
        VISION("Vision Screening"),
        HEARING("Hearing Screening"),
        DENTAL("Dental Screening"),
        SCOLIOSIS("Scoliosis Screening"),
        BMI("BMI/Height/Weight"),
        BLOOD_PRESSURE("Blood Pressure"),
        TUBERCULOSIS("Tuberculosis Screening"),
        LEAD("Lead Screening"),
        DEVELOPMENTAL("Developmental Screening"),
        SPEECH_LANGUAGE("Speech/Language Screening"),
        PHYSICAL_EXAM("Physical Examination"),
        OTHER("Other Screening");

        private final String displayName;

        ScreeningType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ScreeningStatus {
        SCHEDULED("Scheduled"),
        COMPLETED("Completed"),
        IN_PROGRESS("In Progress"),
        CANCELLED("Cancelled"),
        NO_SHOW("Student No-Show"),
        RESCHEDULED("Rescheduled"),
        PENDING("Pending");

        private final String displayName;

        ScreeningStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ScreeningResult {
        PASS("Pass"),
        FAIL("Fail"),
        REFER("Refer for Further Evaluation"),
        INCONCLUSIVE("Inconclusive - Retest Needed"),
        UNABLE_TO_TEST("Unable to Test"),
        PENDING("Pending Results");

        private final String displayName;

        ScreeningResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReferralType {
        OPTOMETRIST("Optometrist/Ophthalmologist"),
        AUDIOLOGIST("Audiologist"),
        DENTIST("Dentist"),
        ORTHODONTIST("Orthodontist"),
        ORTHOPEDIC("Orthopedic Specialist"),
        PRIMARY_CARE("Primary Care Physician"),
        CARDIOLOGIST("Cardiologist"),
        SPECIALIST("Medical Specialist"),
        ENT("Ear, Nose & Throat Specialist"),
        OTHER("Other Provider");

        private final String displayName;

        ReferralType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CurvatureSeverity {
        MILD("Mild (10-25 degrees)"),
        MODERATE("Moderate (25-40 degrees)"),
        SEVERE("Severe (>40 degrees)");

        private final String displayName;

        CurvatureSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BMICategory {
        UNDERWEIGHT("Underweight (<5th percentile)"),
        HEALTHY_WEIGHT("Healthy Weight (5th-85th percentile)"),
        OVERWEIGHT("Overweight (85th-95th percentile)"),
        OBESE("Obese (>95th percentile)");

        private final String displayName;

        BMICategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BloodPressureCategory {
        NORMAL("Normal"),
        ELEVATED("Elevated"),
        HYPERTENSION_STAGE_1("Hypertension Stage 1"),
        HYPERTENSION_STAGE_2("Hypertension Stage 2"),
        HYPOTENSION("Hypotension (Low)");

        private final String displayName;

        BloodPressureCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NotificationMethod {
        PHONE("Phone Call"),
        EMAIL("Email"),
        LETTER("Letter Sent Home"),
        PARENT_PORTAL("Parent Portal"),
        TEXT_MESSAGE("Text Message"),
        IN_PERSON("In-Person"),
        NOT_NOTIFIED("Not Notified");

        private final String displayName;

        NotificationMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum HearingResult {
        PASS("Pass"),
        FAIL("Fail - Refer"),
        REFER("Refer for Further Evaluation"),
        UNABLE_TO_TEST("Unable to Test");

        private final String displayName;

        HearingResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VisionResult {
        PASS("Pass"),
        FAIL("Fail - Refer"),
        REFER("Refer for Further Evaluation"),
        UNABLE_TO_TEST("Unable to Test");

        private final String displayName;

        VisionResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ScoliosisResult {
        NORMAL("Normal - No Concerns"),
        ABNORMAL("Abnormal - Further Evaluation Needed"),
        REFER("Refer to Physician"),
        UNABLE_TO_TEST("Unable to Test");

        private final String displayName;

        ScoliosisResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RibHumpSeverity {
        NONE("None Detected"),
        MILD("Mild"),
        MODERATE("Moderate"),
        SEVERE("Severe");

        private final String displayName;

        RibHumpSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum OverallResult {
        PASS("Pass - All Screenings Normal"),
        PASS_WITH_RECOMMENDATIONS("Pass with Recommendations"),
        REFER("Refer - Follow-up Required"),
        INCOMPLETE("Incomplete - Further Testing Needed");

        private final String displayName;

        OverallResult(String displayName) {
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
    public boolean isCompleted() {
        return screeningStatus == ScreeningStatus.COMPLETED;
    }

    @Transient
    public boolean failed() {
        return result == ScreeningResult.FAIL || result == ScreeningResult.REFER;
    }

    @Transient
    public boolean needsParentNotification() {
        return parentNotificationRequired && !parentNotified;
    }

    @Transient
    public boolean hasOutstandingReferral() {
        return referralNeeded && !referralCompleted;
    }

    @Transient
    public boolean isOverdue() {
        return screeningStatus == ScreeningStatus.SCHEDULED &&
               scheduledDate != null &&
               scheduledDate.isBefore(LocalDate.now());
    }

    @Transient
    public Double calculateBMI() {
        if (heightInches == null || weightPounds == null || heightInches == 0) {
            return null;
        }
        return (weightPounds / (heightInches * heightInches)) * 703;
    }

    @Transient
    public boolean visionImpaired() {
        return screeningType == ScreeningType.VISION &&
               (result == ScreeningResult.FAIL || result == ScreeningResult.REFER);
    }

    @Transient
    public boolean hearingImpaired() {
        return screeningType == ScreeningType.HEARING &&
               (result == ScreeningResult.FAIL || result == ScreeningResult.REFER);
    }

    @Transient
    public int getDaysSinceScreening() {
        if (completedDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - completedDate.toEpochDay());
    }
}
