package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * College Application Entity
 * Tracks college applications, admissions decisions, and scholarship awards
 * Supports college planning and post-secondary transition
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "college_applications", indexes = {
    @Index(name = "idx_college_application_student", columnList = "student_id"),
    @Index(name = "idx_college_application_status", columnList = "application_status"),
    @Index(name = "idx_college_application_decision", columnList = "admission_decision"),
    @Index(name = "idx_college_application_counselor", columnList = "counselor_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private Teacher counselor;

    // College Information
    @Column(name = "college_name", nullable = false, length = 300)
    private String collegeName;

    @Column(name = "college_city", length = 100)
    private String collegeCity;

    @Column(name = "college_state", length = 50)
    private String collegeState;

    @Column(name = "college_type", length = 30)
    @Enumerated(EnumType.STRING)
    private CollegeType collegeType;

    @Column(name = "college_website", length = 500)
    private String collegeWebsite;

    @Column(name = "ceeb_code", length = 20)
    private String ceebCode; // College Board code

    // Application Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "application_category", length = 30)
    private ApplicationCategory applicationCategory;

    @Column(name = "application_priority")
    private Integer applicationPriority; // 1 = top choice

    // Application Details
    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", length = 30)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_plan", length = 30)
    private ApplicationPlan applicationPlan;

    @Column(name = "intended_major", length = 200)
    private String intendedMajor;

    @Column(name = "intended_minor", length = 200)
    private String intendedMinor;

    @Column(name = "program_name", length = 300)
    private String programName; // Specific program (Honors College, etc.)

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_term", length = 30)
    private EntryTerm entryTerm;

    @Column(name = "entry_year")
    private Integer entryYear;

    // Application Timeline
    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 30)
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.PLANNING;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "application_submitted_date")
    private LocalDate applicationSubmittedDate;

    @Column(name = "confirmation_number", length = 100)
    private String confirmationNumber;

    @Column(name = "application_fee", precision = 8, scale = 2)
    private BigDecimal applicationFee;

    @Column(name = "fee_waiver_used")
    @Builder.Default
    private Boolean feeWaiverUsed = false;

    // Requirements and Submissions
    @Column(name = "essay_required")
    @Builder.Default
    private Boolean essayRequired = false;

    @Column(name = "essay_submitted")
    @Builder.Default
    private Boolean essaySubmitted = false;

    @Column(name = "transcript_requested")
    @Builder.Default
    private Boolean transcriptRequested = false;

    @Column(name = "transcript_sent_date")
    private LocalDate transcriptSentDate;

    @Column(name = "test_scores_required")
    @Builder.Default
    private Boolean testScoresRequired = false;

    @Column(name = "test_scores_sent")
    @Builder.Default
    private Boolean testScoresSent = false;

    @Column(name = "sat_score")
    private Integer satScore;

    @Column(name = "act_score")
    private Integer actScore;

    @Column(name = "letters_of_recommendation_required")
    private Integer lettersOfRecommendationRequired;

    @Column(name = "letters_of_recommendation_submitted")
    @Builder.Default
    private Integer lettersOfRecommendationSubmitted = 0;

    @Column(name = "portfolio_required")
    @Builder.Default
    private Boolean portfolioRequired = false;

    @Column(name = "portfolio_submitted")
    @Builder.Default
    private Boolean portfolioSubmitted = false;

    @Column(name = "interview_required")
    @Builder.Default
    private Boolean interviewRequired = false;

    @Column(name = "interview_completed")
    @Builder.Default
    private Boolean interviewCompleted = false;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    // Admission Decision
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_decision", length = 30)
    private AdmissionDecision admissionDecision;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "decision_deadline")
    private LocalDate decisionDeadline; // Response deadline

    @Column(name = "enrollment_deposit_required")
    @Builder.Default
    private Boolean enrollmentDepositRequired = false;

    @Column(name = "enrollment_deposit_amount", precision = 8, scale = 2)
    private BigDecimal enrollmentDepositAmount;

    @Column(name = "enrollment_deposit_deadline")
    private LocalDate enrollmentDepositDeadline;

    @Column(name = "enrollment_deposit_paid")
    @Builder.Default
    private Boolean enrollmentDepositPaid = false;

    @Column(name = "enrollment_confirmed")
    @Builder.Default
    private Boolean enrollmentConfirmed = false;

    @Column(name = "enrollment_confirmation_date")
    private LocalDate enrollmentConfirmationDate;

    // Financial Aid
    @Column(name = "fafsa_required")
    @Builder.Default
    private Boolean fafsaRequired = true;

    @Column(name = "css_profile_required")
    @Builder.Default
    private Boolean cssProfileRequired = false;

    @Column(name = "financial_aid_applied")
    @Builder.Default
    private Boolean financialAidApplied = false;

    @Column(name = "financial_aid_application_date")
    private LocalDate financialAidApplicationDate;

    @Column(name = "financial_aid_deadline")
    private LocalDate financialAidDeadline;

    @Column(name = "financial_aid_offered")
    @Builder.Default
    private Boolean financialAidOffered = false;

    @Column(name = "financial_aid_package_amount", precision = 10, scale = 2)
    private BigDecimal financialAidPackageAmount;

    @Column(name = "grants_amount", precision = 10, scale = 2)
    private BigDecimal grantsAmount;

    @Column(name = "scholarships_amount", precision = 10, scale = 2)
    private BigDecimal scholarshipsAmount;

    @Column(name = "loans_amount", precision = 10, scale = 2)
    private BigDecimal loansAmount;

    @Column(name = "work_study_amount", precision = 10, scale = 2)
    private BigDecimal workStudyAmount;

    @Column(name = "net_cost", precision = 10, scale = 2)
    private BigDecimal netCost;

    // Scholarships
    @Column(name = "merit_scholarship_offered")
    @Builder.Default
    private Boolean meritScholarshipOffered = false;

    @Column(name = "merit_scholarship_name", length = 200)
    private String meritScholarshipName;

    @Column(name = "merit_scholarship_amount", precision = 10, scale = 2)
    private BigDecimal meritScholarshipAmount;

    @Column(name = "athletic_scholarship_offered")
    @Builder.Default
    private Boolean athleticScholarshipOffered = false;

    @Column(name = "athletic_scholarship_amount", precision = 10, scale = 2)
    private BigDecimal athleticScholarshipAmount;

    // Visits and Events
    @Column(name = "campus_visit_completed")
    @Builder.Default
    private Boolean campusVisitCompleted = false;

    @Column(name = "campus_visit_date")
    private LocalDate campusVisitDate;

    @Column(name = "virtual_tour_completed")
    @Builder.Default
    private Boolean virtualTourCompleted = false;

    @Column(name = "accepted_students_day_attended")
    @Builder.Default
    private Boolean acceptedStudentsDayAttended = false;

    // Housing
    @Column(name = "housing_application_required")
    @Builder.Default
    private Boolean housingApplicationRequired = false;

    @Column(name = "housing_application_submitted")
    @Builder.Default
    private Boolean housingApplicationSubmitted = false;

    @Column(name = "housing_application_deadline")
    private LocalDate housingApplicationDeadline;

    // Student Decision
    @Column(name = "student_attending")
    @Builder.Default
    private Boolean studentAttending = false;

    @Column(name = "student_declined")
    @Builder.Default
    private Boolean studentDeclined = false;

    @Column(name = "decline_reason", columnDefinition = "TEXT")
    private String declineReason;

    // Notes and Tracking
    @Column(name = "counselor_notes", columnDefinition = "TEXT")
    private String counselorNotes;

    @Column(name = "application_notes", columnDefinition = "TEXT")
    private String applicationNotes;

    @Column(name = "financial_aid_notes", columnDefinition = "TEXT")
    private String financialAidNotes;

    @Column(name = "student_impressions", columnDefinition = "TEXT")
    private String studentImpressions;

    // Parent Communication
    @Column(name = "parent_informed")
    @Builder.Default
    private Boolean parentInformed = false;

    @Column(name = "parent_approval_obtained")
    @Builder.Default
    private Boolean parentApprovalObtained = false;

    // Alerts
    @Column(name = "deadline_approaching")
    @Builder.Default
    private Boolean deadlineApproaching = false;

    @Column(name = "missing_requirements")
    @Builder.Default
    private Boolean missingRequirements = false;

    @Column(name = "missing_requirements_list", columnDefinition = "TEXT")
    private String missingRequirementsList;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum CollegeType {
        FOUR_YEAR_PUBLIC("4-Year Public University"),
        FOUR_YEAR_PRIVATE("4-Year Private University"),
        TWO_YEAR_PUBLIC("2-Year Public College"),
        TWO_YEAR_PRIVATE("2-Year Private College"),
        COMMUNITY_COLLEGE("Community College"),
        LIBERAL_ARTS("Liberal Arts College"),
        TECHNICAL_SCHOOL("Technical/Trade School"),
        MILITARY_ACADEMY("Military Academy"),
        ONLINE("Online University"),
        FOR_PROFIT("For-Profit Institution"),
        INTERNATIONAL("International University");

        private final String displayName;

        CollegeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApplicationCategory {
        REACH("Reach School"),
        MATCH("Match School"),
        SAFETY("Safety School"),
        LIKELY("Likely School");

        private final String displayName;

        ApplicationCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApplicationType {
        COMMON_APP("Common Application"),
        COALITION_APP("Coalition Application"),
        UNIVERSAL_COLLEGE_APP("Universal College Application"),
        SCHOOL_SPECIFIC("School-Specific Application"),
        STATE_SYSTEM("State System Application"),
        DIRECT_ADMIT("Direct Admission");

        private final String displayName;

        ApplicationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApplicationPlan {
        REGULAR_DECISION("Regular Decision"),
        EARLY_ACTION("Early Action"),
        EARLY_DECISION("Early Decision"),
        EARLY_DECISION_II("Early Decision II"),
        ROLLING_ADMISSION("Rolling Admission"),
        PRIORITY_DEADLINE("Priority Deadline");

        private final String displayName;

        ApplicationPlan(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EntryTerm {
        FALL("Fall Semester"),
        SPRING("Spring Semester"),
        SUMMER("Summer Session"),
        WINTER("Winter Term");

        private final String displayName;

        EntryTerm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApplicationStatus {
        PLANNING("Planning to Apply"),
        IN_PROGRESS("Application In Progress"),
        SUBMITTED("Application Submitted"),
        UNDER_REVIEW("Under Review"),
        DECISION_RECEIVED("Decision Received"),
        ENROLLED("Enrolled"),
        DECLINED("Declined Offer"),
        WITHDRAWN("Application Withdrawn");

        private final String displayName;

        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AdmissionDecision {
        PENDING("Decision Pending"),
        ACCEPTED("Accepted"),
        DENIED("Denied"),
        WAITLISTED("Waitlisted"),
        DEFERRED("Deferred"),
        CONDITIONAL_ACCEPTANCE("Conditional Acceptance");

        private final String displayName;

        AdmissionDecision(String displayName) {
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
    public boolean isApplicationComplete() {
        return applicationSubmittedDate != null &&
               (!essayRequired || essaySubmitted) &&
               (!transcriptRequested || transcriptSentDate != null) &&
               (!testScoresRequired || testScoresSent) &&
               (lettersOfRecommendationRequired == null ||
                lettersOfRecommendationSubmitted >= lettersOfRecommendationRequired) &&
               (!portfolioRequired || portfolioSubmitted) &&
               (!interviewRequired || interviewCompleted);
    }

    @Transient
    public boolean hasMissingRequirements() {
        return (essayRequired && !essaySubmitted) ||
               (transcriptRequested && transcriptSentDate == null) ||
               (testScoresRequired && !testScoresSent) ||
               (lettersOfRecommendationRequired != null &&
                lettersOfRecommendationSubmitted < lettersOfRecommendationRequired) ||
               (portfolioRequired && !portfolioSubmitted) ||
               (interviewRequired && !interviewCompleted);
    }

    @Transient
    public boolean isDeadlineApproaching() {
        if (applicationDeadline == null || applicationSubmittedDate != null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate twoWeeksFromNow = now.plusDays(14);
        return applicationDeadline.isBefore(twoWeeksFromNow) ||
               applicationDeadline.isEqual(twoWeeksFromNow);
    }

    @Transient
    public boolean isDecisionPending() {
        return admissionDecision == AdmissionDecision.PENDING ||
               (applicationStatus == ApplicationStatus.SUBMITTED &&
                admissionDecision == null);
    }

    @Transient
    public boolean isAccepted() {
        return admissionDecision == AdmissionDecision.ACCEPTED ||
               admissionDecision == AdmissionDecision.CONDITIONAL_ACCEPTANCE;
    }

    @Transient
    public boolean needsDecisionResponse() {
        return isAccepted() &&
               !enrollmentConfirmed &&
               !studentDeclined &&
               decisionDeadline != null &&
               LocalDate.now().isBefore(decisionDeadline);
    }

    @Transient
    public BigDecimal getTotalFinancialAid() {
        BigDecimal total = BigDecimal.ZERO;
        if (grantsAmount != null) total = total.add(grantsAmount);
        if (scholarshipsAmount != null) total = total.add(scholarshipsAmount);
        if (loansAmount != null) total = total.add(loansAmount);
        if (workStudyAmount != null) total = total.add(workStudyAmount);
        return total;
    }

    @Transient
    public BigDecimal getTotalScholarships() {
        BigDecimal total = BigDecimal.ZERO;
        if (scholarshipsAmount != null) total = total.add(scholarshipsAmount);
        if (meritScholarshipAmount != null) total = total.add(meritScholarshipAmount);
        if (athleticScholarshipAmount != null) total = total.add(athleticScholarshipAmount);
        return total;
    }

    @Transient
    public int getDaysUntilDeadline() {
        if (applicationDeadline == null) return -1;
        return (int) (applicationDeadline.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public int getDaysUntilDecisionDeadline() {
        if (decisionDeadline == null) return -1;
        return (int) (decisionDeadline.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public boolean isTopChoice() {
        return applicationPriority != null && applicationPriority == 1;
    }
}
