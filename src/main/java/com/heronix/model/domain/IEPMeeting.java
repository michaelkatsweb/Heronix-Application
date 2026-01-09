package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IEP Meeting Entity
 * Tracks IEP team meetings, attendance, decisions, and documentation
 * Supports compliance tracking for annual reviews and re-evaluations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "iep_meetings", indexes = {
    @Index(name = "idx_meeting_iep", columnList = "iep_id"),
    @Index(name = "idx_meeting_date", columnList = "meeting_date"),
    @Index(name = "idx_meeting_type", columnList = "meeting_type"),
    @Index(name = "idx_meeting_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IEPMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iep_id", nullable = false)
    private IEP iep;

    // Meeting Details
    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 50)
    private MeetingType meetingType;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    // Team Members
    @ElementCollection
    @CollectionTable(name = "meeting_attendees", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "attendee", length = 200)
    @Builder.Default
    private List<String> attendees = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "meeting_excused_members", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "excused_member", length = 200)
    @Builder.Default
    private List<String> excusedMembers = new ArrayList<>();

    // Parent Participation
    @Column(name = "parent_attended")
    private Boolean parentAttended;

    @Column(name = "parent_invitation_sent_date")
    private LocalDate parentInvitationSentDate;

    @Column(name = "parent_consent_received")
    private Boolean parentConsentReceived;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    @Column(name = "interpreter_required")
    @Builder.Default
    private Boolean interpreterRequired = false;

    @Column(name = "interpreter_provided")
    private Boolean interpreterProvided;

    @Column(name = "interpreter_language", length = 50)
    private String interpreterLanguage;

    // Meeting Notes
    @Column(name = "meeting_notes", columnDefinition = "TEXT")
    private String meetingNotes;

    @Column(name = "decisions_made", columnDefinition = "TEXT")
    private String decisionsMade;

    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems;

    @Column(name = "parent_concerns", columnDefinition = "TEXT")
    private String parentConcerns;

    // Documents
    @Column(name = "pwn_issued")
    @Builder.Default
    private Boolean pwnIssued = false;

    @Column(name = "pwn_issue_date")
    private LocalDate pwnIssueDate;

    @Column(name = "procedural_safeguards_provided")
    @Builder.Default
    private Boolean proceduralSafeguardsProvided = false;

    @Column(name = "meeting_minutes_path", length = 500)
    private String meetingMinutesPath;

    @Column(name = "sign_in_sheet_path", length = 500)
    private String signInSheetPath;

    @Column(name = "pwn_document_path", length = 500)
    private String pwnDocumentPath;

    // Follow-up
    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    @Column(name = "next_meeting_scheduled")
    @Builder.Default
    private Boolean nextMeetingScheduled = false;

    @Column(name = "next_meeting_date")
    private LocalDate nextMeetingDate;

    // Compliance
    @Column(name = "is_annual_review")
    @Builder.Default
    private Boolean isAnnualReview = false;

    @Column(name = "is_triennial_reevaluation")
    @Builder.Default
    private Boolean isTriennialReevaluation = false;

    @Column(name = "is_initial_iep")
    @Builder.Default
    private Boolean isInitialIEP = false;

    @Column(name = "meeting_held_within_timeline")
    @Builder.Default
    private Boolean meetingHeldWithinTimeline = true;

    @Column(name = "timeline_notes", columnDefinition = "TEXT")
    private String timelineNotes;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum MeetingType {
        INITIAL_IEP("Initial IEP"),
        ANNUAL_REVIEW("Annual Review"),
        TRIENNIAL_REEVALUATION("Triennial Re-evaluation"),
        AMENDMENT("IEP Amendment"),
        MANIFESTATION_DETERMINATION("Manifestation Determination"),
        TRANSITION_PLANNING("Transition Planning"),
        ESY_ELIGIBILITY("ESY Eligibility"),
        PLACEMENT("Placement Review"),
        PROGRESS_REVIEW("Progress Review"),
        PARENT_REQUESTED("Parent Requested"),
        EMERGENCY("Emergency Meeting"),
        OTHER("Other");

        private final String displayName;

        MeetingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MeetingStatus {
        SCHEDULED("Scheduled"),
        CONFIRMED("Confirmed"),
        RESCHEDULED("Rescheduled"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show");

        private final String displayName;

        MeetingStatus(String displayName) {
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
        return status == MeetingStatus.COMPLETED;
    }

    @Transient
    public boolean isUpcoming() {
        return (status == MeetingStatus.SCHEDULED || status == MeetingStatus.CONFIRMED) &&
               meetingDate != null && meetingDate.isAfter(LocalDate.now());
    }

    @Transient
    public boolean isPastDue() {
        return (status == MeetingStatus.SCHEDULED || status == MeetingStatus.CONFIRMED) &&
               meetingDate != null && meetingDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean hasParentParticipation() {
        return parentAttended != null && parentAttended;
    }

    @Transient
    public boolean needsParentConsent() {
        return (meetingType == MeetingType.INITIAL_IEP ||
                meetingType == MeetingType.TRIENNIAL_REEVALUATION) &&
               (parentConsentReceived == null || !parentConsentReceived);
    }

    @Transient
    public boolean needsInterpreter() {
        return interpreterRequired && (interpreterProvided == null || !interpreterProvided);
    }

    @Transient
    public int getAttendeeCount() {
        return attendees != null ? attendees.size() : 0;
    }

    @Transient
    public boolean hasRequiredDocumentation() {
        if (!isCompleted()) return false;
        if (pwnIssued && pwnDocumentPath == null) return false;
        return meetingMinutesPath != null;
    }

    @Transient
    public Student getStudent() {
        return iep != null ? iep.getStudent() : null;
    }

    @Transient
    public int getDaysUntilMeeting() {
        if (meetingDate == null) return -1;
        return (int) (meetingDate.toEpochDay() - LocalDate.now().toEpochDay());
    }
}
