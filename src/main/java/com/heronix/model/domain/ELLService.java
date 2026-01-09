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
 * ELL Service Entity
 * Tracks ELL/ESL service delivery, attendance, and compliance
 * Supports Title III documentation and service minute tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "ell_services", indexes = {
    @Index(name = "idx_ell_service_student", columnList = "ell_student_id"),
    @Index(name = "idx_ell_service_date", columnList = "service_date"),
    @Index(name = "idx_ell_service_provider", columnList = "service_provider_id"),
    @Index(name = "idx_ell_service_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ELLService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ell_student_id", nullable = false)
    private ELLStudent ellStudent;

    // Service Details
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_model", length = 50)
    private ELLStudent.ServiceDeliveryModel deliveryModel;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private Teacher serviceProvider;

    @Column(name = "service_provider_name", length = 100)
    private String serviceProviderName;

    // Service Focus
    @Enumerated(EnumType.STRING)
    @Column(name = "focus_area", length = 50)
    private FocusArea focusArea;

    @Column(name = "lesson_topic", length = 200)
    private String lessonTopic;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    // Attendance
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.SCHEDULED;

    @Column(name = "student_attended")
    private Boolean studentAttended;

    @Column(name = "absence_reason", length = 200)
    private String absenceReason;

    // Progress Notes
    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_level", length = 30)
    private ParticipationLevel participationLevel;

    @Column(name = "skills_demonstrated", columnDefinition = "TEXT")
    private String skillsDemonstrated;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;

    // Materials and Resources
    @Column(name = "materials_used", columnDefinition = "TEXT")
    private String materialsUsed;

    @Column(name = "technology_used")
    private Boolean technologyUsed;

    @Column(name = "technology_details", length = 200)
    private String technologyDetails;

    // Location
    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "class_size")
    private Integer classSize;

    @Column(name = "group_composition", length = 200)
    private String groupComposition;

    // Standards and Assessment
    @Column(name = "standards_addressed", columnDefinition = "TEXT")
    private String standardsAddressed;

    @Column(name = "assessment_administered")
    private Boolean assessmentAdministered;

    @Column(name = "assessment_results", columnDefinition = "TEXT")
    private String assessmentResults;

    // Follow-up
    @Column(name = "homework_assigned")
    private Boolean homeworkAssigned;

    @Column(name = "homework_description", length = 200)
    private String homeworkDescription;

    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // Title III Compliance
    @Column(name = "title_iii_funded")
    @Builder.Default
    private Boolean titleIIIFunded = false;

    @Column(name = "reported_for_compliance")
    @Builder.Default
    private Boolean reportedForCompliance = false;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum ServiceType {
        ESL_INSTRUCTION("ESL Instruction"),
        CONTENT_AREA_SUPPORT("Content Area Support"),
        LITERACY_DEVELOPMENT("Literacy Development"),
        VOCABULARY_DEVELOPMENT("Vocabulary Development"),
        GRAMMAR_INSTRUCTION("Grammar Instruction"),
        CONVERSATION_PRACTICE("Conversation Practice"),
        ACADEMIC_LANGUAGE("Academic Language Development"),
        READING_COMPREHENSION("Reading Comprehension"),
        WRITING_SKILLS("Writing Skills"),
        LISTENING_SKILLS("Listening Skills"),
        SPEAKING_SKILLS("Speaking Skills"),
        CULTURAL_ORIENTATION("Cultural Orientation"),
        NEWCOMER_SUPPORT("Newcomer Support"),
        TEST_PREPARATION("Test Preparation"),
        HOMEWORK_SUPPORT("Homework Support"),
        SHELTERED_INSTRUCTION("Sheltered Instruction"),
        OTHER("Other");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FocusArea {
        LISTENING("Listening"),
        SPEAKING("Speaking"),
        READING("Reading"),
        WRITING("Writing"),
        VOCABULARY("Vocabulary"),
        GRAMMAR("Grammar"),
        COMPREHENSION("Comprehension"),
        PHONICS("Phonics"),
        FLUENCY("Fluency"),
        ACADEMIC_LANGUAGE("Academic Language"),
        SOCIAL_LANGUAGE("Social Language"),
        CONTENT_AREA("Content Area"),
        STUDY_SKILLS("Study Skills");

        private final String displayName;

        FocusArea(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ServiceStatus {
        SCHEDULED("Scheduled"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("Student No-Show"),
        RESCHEDULED("Rescheduled");

        private final String displayName;

        ServiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ParticipationLevel {
        EXCELLENT("Excellent - Highly Engaged"),
        GOOD("Good - Actively Participated"),
        SATISFACTORY("Satisfactory - Participated"),
        NEEDS_IMPROVEMENT("Needs Improvement - Limited Participation"),
        POOR("Poor - Minimal Participation");

        private final String displayName;

        ParticipationLevel(String displayName) {
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
        return status == ServiceStatus.COMPLETED;
    }

    @Transient
    public boolean wasAttended() {
        return studentAttended != null && studentAttended;
    }

    @Transient
    public boolean isMissed() {
        return status == ServiceStatus.NO_SHOW ||
               (status == ServiceStatus.COMPLETED && studentAttended != null && !studentAttended);
    }

    @Transient
    public Student getStudent() {
        return ellStudent != null ? ellStudent.getStudent() : null;
    }

    @Transient
    public boolean needsDocumentation() {
        return isCompleted() && progressNotes == null;
    }

    @Transient
    public boolean isUpcoming() {
        return status == ServiceStatus.SCHEDULED &&
               serviceDate != null &&
               serviceDate.isAfter(LocalDate.now());
    }

    @Transient
    public boolean isPastDue() {
        return status == ServiceStatus.SCHEDULED &&
               serviceDate != null &&
               serviceDate.isBefore(LocalDate.now());
    }
}
