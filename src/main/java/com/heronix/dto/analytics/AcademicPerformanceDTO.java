package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Academic performance analytics data
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPerformanceDTO {

    private String term;
    private String academicYear;
    private Long campusId;
    private String campusName;

    // GPA Metrics
    private Double averageGPA;
    private Double averageWeightedGPA;
    private Double medianGPA;
    private Double previousTermGPA;
    private Double gpaChange;

    // Grade Distribution
    private Map<String, Long> gradeDistribution; // A, B, C, D, F counts
    private Map<String, Double> gradePercentages;

    // Pass/Fail
    private Long totalGrades;
    private Long passingGrades;
    private Long failingGrades;
    private Double passRate;
    private Double previousPassRate;

    // Honor Roll
    private Long honorRollCount;
    private Long highHonorsCount;
    private Long principalsListCount;
    private Double honorRollPercentage;

    // At-Risk
    private Long failingStudentCount;
    private Long probationCount;
    private Long improvementCount; // Students who improved

    /**
     * Grade distribution by letter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeDistribution {
        private String letterGrade;
        private Long count;
        private Double percentage;
        private String colorHex;

        public static String getColorForGrade(String grade) {
            return switch (grade) {
                case "A+", "A", "A-" -> "#10B981"; // Green
                case "B+", "B", "B-" -> "#06B6D4"; // Cyan
                case "C+", "C", "C-" -> "#F59E0B"; // Amber
                case "D+", "D", "D-" -> "#F97316"; // Orange
                case "F" -> "#EF4444"; // Red
                default -> "#9CA3AF"; // Gray
            };
        }
    }

    /**
     * GPA trend over time
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GPATrend {
        private String term;
        private String academicYear;
        private Double averageGPA;
        private Double averageWeightedGPA;
        private Long studentCount;
        private Double changeFromPrevious;
    }

    /**
     * Course performance comparison
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePerformance {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String subject;
        private Long teacherId;
        private String teacherName;
        private Long enrolledCount;
        private Double averageGrade;
        private Double passRate;
        private Long failingCount;
        private Map<String, Long> gradeDistribution;
    }

    /**
     * Teacher performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherPerformance {
        private Long teacherId;
        private String teacherName;
        private String department;
        private Long totalStudents;
        private Double averageGrade;
        private Double passRate;
        private Long failingCount;
        private Map<String, Long> gradeDistribution;
        private List<CoursePerformance> courses;
    }

    /**
     * Honor roll student
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HonorRollStudent {
        private Long studentId;
        private String studentNumber;
        private String studentName;
        private String gradeLevel;
        private Double gpa;
        private Double weightedGPA;
        private String honorLevel; // HONOR_ROLL, HIGH_HONORS, PRINCIPALS_LIST
        private Integer consecutiveTerms;
    }

    /**
     * Failing student alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailingStudent {
        private Long studentId;
        private String studentNumber;
        private String studentName;
        private String gradeLevel;
        private Double currentGPA;
        private Integer failingCourseCount;
        private List<String> failingCourses;
        private String riskLevel; // AT_RISK, PROBATION, RETENTION_RISK
        private Boolean hasInterventionPlan;
    }

    /**
     * Subject area performance
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectPerformance {
        private String subjectArea;
        private Long courseCount;
        private Long studentCount;
        private Double averageGrade;
        private Double passRate;
        private Map<String, Long> gradeDistribution;
    }
}
