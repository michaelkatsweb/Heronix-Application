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
 * Gifted Service Entity
 * Tracks gifted service delivery, pull-out sessions, and enrichment activities
 * Supports service minute tracking and progress documentation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "gifted_services", indexes = {
    @Index(name = "idx_gifted_service_student", columnList = "gifted_student_id"),
    @Index(name = "idx_gifted_service_date", columnList = "service_date"),
    @Index(name = "idx_gifted_service_provider", columnList = "service_provider_id"),
    @Index(name = "idx_gifted_service_type", columnList = "service_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftedService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gifted_student_id", nullable = false)
    private GiftedStudent giftedStudent;

    // Service Details
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_model", length = 50)
    private GiftedStudent.ServiceDeliveryModel deliveryModel;

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
    @Column(name = "engagement_level", length = 30)
    private EngagementLevel engagementLevel;

    @Column(name = "skills_demonstrated", columnDefinition = "TEXT")
    private String skillsDemonstrated;

    @Column(name = "student_work_sample_path", length = 500)
    private String studentWorkSamplePath;

    // Differentiation
    @Column(name = "differentiation_used", columnDefinition = "TEXT")
    private String differentiationUsed;

    @Column(name = "depth_complexity_level", length = 50)
    private String depthComplexityLevel;

    @Column(name = "bloom_level", length = 50)
    private String bloomLevel;

    // Materials and Resources
    @Column(name = "materials_used", columnDefinition = "TEXT")
    private String materialsUsed;

    @Column(name = "technology_integration", columnDefinition = "TEXT")
    private String technologyIntegration;

    @Column(name = "enrichment_resources", columnDefinition = "TEXT")
    private String enrichmentResources;

    // Group Information
    @Column(name = "group_size")
    private Integer groupSize;

    @Column(name = "peer_collaboration")
    private Boolean peerCollaboration;

    @Column(name = "cross_grade_grouping")
    private Boolean crossGradeGrouping;

    // Standards and Assessment
    @Column(name = "standards_addressed", columnDefinition = "TEXT")
    private String standardsAddressed;

    @Column(name = "assessment_administered")
    private Boolean assessmentAdministered;

    @Column(name = "assessment_results", columnDefinition = "TEXT")
    private String assessmentResults;

    @Column(name = "performance_level", length = 50)
    private String performanceLevel;

    // Follow-up
    @Column(name = "extension_activity_assigned")
    private Boolean extensionActivityAssigned;

    @Column(name = "extension_activity_description", length = 500)
    private String extensionActivityDescription;

    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // Location
    @Column(name = "location", length = 200)
    private String location;

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
        PULL_OUT("Pull-Out Session"),
        ENRICHMENT("Enrichment Activity"),
        INDEPENDENT_STUDY("Independent Study"),
        MENTORSHIP("Mentorship Session"),
        COMPETITION_PREP("Competition Preparation"),
        CREATIVE_PROJECT("Creative/Productive Project"),
        RESEARCH_PROJECT("Research Project"),
        CRITICAL_THINKING("Critical Thinking"),
        PROBLEM_SOLVING("Problem Solving"),
        STEM_ENRICHMENT("STEM Enrichment"),
        ARTS_ENRICHMENT("Arts Enrichment"),
        LEADERSHIP("Leadership Development"),
        ADVANCED_INSTRUCTION("Advanced Instruction"),
        ACCELERATION("Acceleration Activity"),
        CLUSTER_CLASS("Cluster Class Activity"),
        FIELD_TRIP("Field Trip/Experience"),
        GUEST_SPEAKER("Guest Speaker/Expert"),
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
        MATHEMATICS("Mathematics"),
        SCIENCE("Science"),
        READING_LITERATURE("Reading/Literature"),
        WRITING("Writing"),
        SOCIAL_STUDIES("Social Studies"),
        STEM("STEM"),
        CREATIVE_THINKING("Creative Thinking"),
        CRITICAL_THINKING("Critical Thinking"),
        PROBLEM_SOLVING("Problem Solving"),
        RESEARCH_SKILLS("Research Skills"),
        LEADERSHIP("Leadership"),
        ARTS("Arts"),
        TECHNOLOGY("Technology"),
        INTERDISCIPLINARY("Interdisciplinary"),
        SOCIAL_EMOTIONAL("Social-Emotional"),
        STUDY_SKILLS("Advanced Study Skills");

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

    public enum EngagementLevel {
        EXCEPTIONAL("Exceptional - Highly Engaged"),
        STRONG("Strong - Very Engaged"),
        GOOD("Good - Engaged"),
        MODERATE("Moderate - Somewhat Engaged"),
        LOW("Low - Limited Engagement");

        private final String displayName;

        EngagementLevel(String displayName) {
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
        return giftedStudent != null ? giftedStudent.getStudent() : null;
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

    @Transient
    public boolean isHighEngagement() {
        return engagementLevel == EngagementLevel.EXCEPTIONAL ||
               engagementLevel == EngagementLevel.STRONG;
    }
}
