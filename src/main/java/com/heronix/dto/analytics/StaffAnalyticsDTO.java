package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Staff analytics data bundle
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAnalyticsDTO {

    private Long campusId;
    private String campusName;

    // Overall Counts
    private Long totalStaff;
    private Long activeStaff;
    private Long teacherCount;
    private Long adminCount;
    private Long supportStaffCount;

    // Certification Metrics
    private Long certifiedCount;
    private Long pendingCertificationCount;
    private Long expiredCertificationCount;
    private Long expiringSoonCount; // Within 90 days
    private Double certificationComplianceRate;

    // Experience Distribution
    private Map<String, Long> experienceDistribution;
    private Double averageYearsExperience;

    // Workload Metrics
    private Double averageClassesPerTeacher;
    private Double averageStudentsPerTeacher;
    private Long overloadedTeacherCount;
    private Long underutilizedTeacherCount;

    // Professional Development
    private Double pdCompletionRate;
    private Long pdCompliantCount;
    private Long pdPendingCount;
    private Double averagePDHours;

    /**
     * Certification status details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationStatus {
        private Long teacherId;
        private String teacherName;
        private String department;
        private String certificationNumber;
        private String certificationState;
        private LocalDate expirationDate;
        private Integer daysUntilExpiration;
        private String status; // VALID, EXPIRING_SOON, EXPIRED, PENDING
        private List<String> endorsements;
        private List<String> subjectAreas;
    }

    /**
     * Experience breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceBreakdown {
        private String range; // "0-2 Years", "3-5 Years", etc.
        private Long count;
        private Double percentage;
        private String colorHex;
    }

    /**
     * Teacher workload details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherWorkload {
        private Long teacherId;
        private String teacherName;
        private String department;
        private Integer classCount;
        private Integer studentCount;
        private Integer uniquePreps;
        private Double weeklyHours;
        private String workloadStatus; // UNDERUTILIZED, NORMAL, HEAVY, OVERLOADED
        private Double utilizationRate;
    }

    /**
     * Department summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private String department;
        private Long teacherCount;
        private Long totalStudents;
        private Double avgClassSize;
        private Double avgExperience;
        private Double certificationCompliance;
        private Double pdCompletion;
    }

    /**
     * Professional development progress
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PDProgress {
        private Long teacherId;
        private String teacherName;
        private String department;
        private Double hoursCompleted;
        private Double hoursRequired;
        private Double completionPercentage;
        private LocalDate deadline;
        private Integer daysUntilDeadline;
        private String status; // ON_TRACK, AT_RISK, OVERDUE, COMPLETE
    }

    /**
     * Tenure distribution
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenureDistribution {
        private String tenureRange;
        private Long count;
        private Double percentage;
        private Double avgSalary;
    }

    /**
     * Background check status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackgroundCheckStatus {
        private Long totalStaff;
        private Long clearedCount;
        private Long pendingCount;
        private Long expiredCount;
        private Long expiringSoonCount;
        private Double complianceRate;
        private List<StaffComplianceAlert> alerts;
    }

    /**
     * Compliance alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffComplianceAlert {
        private Long staffId;
        private String staffName;
        private String alertType; // CERTIFICATION, BACKGROUND_CHECK, PD, TB_TEST
        private String description;
        private LocalDate dueDate;
        private Integer daysUntilDue;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    }
}
