package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Student Group
 *
 * Manages student groupings and categorizations including:
 * - Homeroom assignments
 * - Advisory groups
 * - Cohorts (graduating class)
 * - House system assignments
 * - Team assignments
 * - Learning communities
 * - Academic tracks
 * - Extracurricular groups
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Entity
@Table(name = "student_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // GROUP METADATA
    // ========================================================================

    @Column(nullable = false, length = 200)
    private String groupName;

    @Column(length = 100)
    private String groupCode; // Short code for displays

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GroupType groupType;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GroupStatus status;

    // ========================================================================
    // ACADEMIC YEAR & TIMEFRAME
    // ========================================================================

    @Column(length = 20)
    private String academicYear; // e.g., "2025-2026"

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 10)
    private String gradeLevel; // If group is grade-specific

    // ========================================================================
    // CAPACITY & SIZE
    // ========================================================================

    @Column
    private Integer maxCapacity;

    @Column
    private Integer currentEnrollment;

    @Column
    private Boolean acceptingNewMembers;

    // ========================================================================
    // STAFF ASSIGNMENTS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_advisor_staff_id")
    private User primaryAdvisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_advisor_staff_id")
    private User secondaryAdvisor;

    @Column(length = 100)
    private String advisorName; // For external/non-staff advisors

    // ========================================================================
    // LOCATION & MEETING
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_room_id")
    private Room primaryRoom;

    @Column(length = 100)
    private String meetingLocation;

    @Column(length = 100)
    private String meetingSchedule; // e.g., "Daily 8:00-8:30 AM"

    @Column(length = 2000)
    private String meetingNotes;

    // ========================================================================
    // HOMEROOM SPECIFIC
    // ========================================================================

    @Column(length = 50)
    private String homeroomNumber;

    @Column
    private Boolean isHomeroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_teacher_id")
    private User homeroomTeacher;

    // ========================================================================
    // ADVISORY SPECIFIC
    // ========================================================================

    @Column
    private Boolean isAdvisory;

    @Column(length = 100)
    private String advisoryFocus; // "Academic", "Social-Emotional", "Career", etc.

    @Column
    private Integer advisoryMeetingsPerWeek;

    // ========================================================================
    // COHORT SPECIFIC (Graduating Class)
    // ========================================================================

    @Column
    private Boolean isCohort;

    @Column
    private Integer graduationYear;

    @Column(length = 100)
    private String cohortName; // "Class of 2027"

    @Column(length = 50)
    private String cohortColor;

    @Column(length = 100)
    private String cohortMascot;

    // ========================================================================
    // HOUSE SYSTEM
    // ========================================================================

    @Column
    private Boolean isHouse;

    @Column(length = 100)
    private String houseName;

    @Column(length = 50)
    private String houseColor;

    @Column(length = 100)
    private String houseMascot;

    @Column
    private Integer housePoints; // For house competition

    // ========================================================================
    // TEAM ASSIGNMENTS
    // ========================================================================

    @Column
    private Boolean isTeam;

    @Column(length = 100)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TeamType teamType;

    @Column(length = 100)
    private String sportOrActivity;

    @Column(length = 50)
    private String competitionLevel; // Varsity, JV, Freshman, etc.

    // ========================================================================
    // LEARNING COMMUNITY
    // ========================================================================

    @Column
    private Boolean isLearningCommunity;

    @Column(length = 200)
    private String learningCommunityTheme;

    @Column
    private Boolean interdisciplinaryApproach;

    // ========================================================================
    // ACADEMIC TRACK
    // ========================================================================

    @Column
    private Boolean isAcademicTrack;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AcademicTrack academicTrack;

    @Column(length = 2000)
    private String trackRequirements;

    // ========================================================================
    // EXTRACURRICULAR
    // ========================================================================

    @Column
    private Boolean isExtracurricular;

    @Column(length = 200)
    private String clubOrganizationName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ClubCategory clubCategory;

    @Column
    private Boolean requiresApplicationOrTryout;

    @Column
    private Boolean requiresParentalConsent;

    @Column
    private LocalDate registrationDeadline;

    @Column
    private Double membershipFee;

    // ========================================================================
    // INTERVENTION/SUPPORT GROUP
    // ========================================================================

    @Column
    private Boolean isInterventionGroup;

    @Column(length = 200)
    private String interventionFocus;

    @Column(length = 50)
    private String interventionTier; // RTI Tier 1, 2, 3

    @Column
    private Integer sessionsPerWeek;

    @Column
    private Integer sessionDurationMinutes;

    // ========================================================================
    // STUDENTS IN GROUP
    // ========================================================================

    @ManyToMany
    @JoinTable(
        name = "student_group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @ToString.Exclude
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    // ========================================================================
    // ADMINISTRATIVE
    // ========================================================================

    @Column(length = 2000)
    private String administrativeNotes;

    @Column(length = 2000)
    private String parentCommunication;

    @Column
    private Boolean visibleToParents;

    @Column
    private Boolean visibleToStudents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_staff_id")
    private User updatedBy;

    @Column
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum GroupType {
        HOMEROOM("Homeroom"),
        ADVISORY("Advisory Group"),
        COHORT("Graduating Cohort"),
        HOUSE("House System"),
        TEAM("Team/Squad"),
        LEARNING_COMMUNITY("Learning Community"),
        ACADEMIC_TRACK("Academic Track"),
        EXTRACURRICULAR("Extracurricular Club"),
        INTERVENTION("Intervention/Support Group"),
        SOCIAL_GROUP("Social/Peer Group"),
        LEADERSHIP("Leadership Group"),
        OTHER("Other");

        private final String displayName;

        GroupType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum GroupStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        PENDING("Pending Approval"),
        ARCHIVED("Archived"),
        FULL("Full - No Vacancies");

        private final String displayName;

        GroupStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TeamType {
        ATHLETIC("Athletic Team"),
        ACADEMIC("Academic Team"),
        PERFORMING_ARTS("Performing Arts"),
        DEBATE("Debate/Speech"),
        ROBOTICS("Robotics/STEM"),
        SERVICE("Service/Volunteer"),
        OTHER("Other");

        private final String displayName;

        TeamType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AcademicTrack {
        GENERAL("General Education"),
        HONORS("Honors Track"),
        AP_IB("Advanced Placement/IB"),
        DUAL_ENROLLMENT("Dual Enrollment"),
        CAREER_TECHNICAL("Career & Technical Education"),
        STEM("STEM Focus"),
        ARTS("Arts Focus"),
        BUSINESS("Business & Entrepreneurship"),
        HEALTH_SCIENCES("Health Sciences"),
        ENGINEERING("Engineering"),
        OTHER("Other Specialized Track");

        private final String displayName;

        AcademicTrack(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ClubCategory {
        ACADEMIC("Academic/Honor Society"),
        ARTS_CULTURE("Arts & Culture"),
        ATHLETICS("Athletics/Recreational"),
        COMMUNITY_SERVICE("Community Service"),
        LEADERSHIP("Leadership/Student Government"),
        MEDIA_PUBLICATIONS("Media & Publications"),
        RELIGIOUS("Religious/Spiritual"),
        SCIENCE_TECHNOLOGY("Science & Technology"),
        SPECIAL_INTEREST("Special Interest"),
        OTHER("Other");

        private final String displayName;

        ClubCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if group is at capacity
     */
    public boolean isFull() {
        if (maxCapacity == null) return false;
        return currentEnrollment != null && currentEnrollment >= maxCapacity;
    }

    /**
     * Get available spots
     */
    public int getAvailableSpots() {
        if (maxCapacity == null) return Integer.MAX_VALUE;
        if (currentEnrollment == null) return maxCapacity;
        return Math.max(0, maxCapacity - currentEnrollment);
    }

    /**
     * Get occupancy percentage
     */
    public double getOccupancyPercentage() {
        if (maxCapacity == null || maxCapacity == 0) return 0.0;
        if (currentEnrollment == null) return 0.0;
        return (currentEnrollment.doubleValue() / maxCapacity.doubleValue()) * 100.0;
    }

    /**
     * Check if group is active
     */
    public boolean isActive() {
        return status == GroupStatus.ACTIVE &&
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }

    /**
     * Get member count
     */
    public int getMemberCount() {
        return students != null ? students.size() : 0;
    }

    /**
     * Add student to group
     */
    public boolean addStudent(Student student) {
        if (isFull() || !Boolean.TRUE.equals(acceptingNewMembers)) {
            return false;
        }
        if (students == null) {
            students = new ArrayList<>();
        }
        if (!students.contains(student)) {
            students.add(student);
            currentEnrollment = students.size();
            return true;
        }
        return false;
    }

    /**
     * Remove student from group
     */
    public boolean removeStudent(Student student) {
        if (students == null) return false;
        boolean removed = students.remove(student);
        if (removed) {
            currentEnrollment = students.size();
        }
        return removed;
    }

    /**
     * Get display name with type
     */
    public String getDisplayName() {
        return groupName + " (" + groupType.getDisplayName() + ")";
    }

    /**
     * Get summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupName);
        if (groupCode != null) {
            sb.append(" [").append(groupCode).append("]");
        }
        sb.append(" - ").append(groupType.getDisplayName());
        if (currentEnrollment != null) {
            sb.append(" - ").append(currentEnrollment).append(" members");
        }
        if (maxCapacity != null) {
            sb.append("/").append(maxCapacity);
        }
        return sb.toString();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentEnrollment == null) {
            currentEnrollment = 0;
        }
        if (acceptingNewMembers == null) {
            acceptingNewMembers = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (students != null) {
            currentEnrollment = students.size();
        }
    }
}
