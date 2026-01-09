package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Attendance Statistics DTO
 *
 * Data transfer object for attendance analytics and dashboard metrics.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 59 - Report Analytics Dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatistics {

    /**
     * Overall statistics
     */
    private OverallStats overall;

    /**
     * Daily statistics
     */
    private List<DailyStats> dailyStats;

    /**
     * Trend analysis
     */
    private TrendAnalysis trends;

    /**
     * Grade-level breakdown
     */
    private Map<String, GradeStats> gradeBreakdown;

    /**
     * Top absent students
     */
    private List<StudentAbsenceRecord> topAbsentees;

    /**
     * Overall Statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverallStats {
        private long totalStudents;
        private long totalDays;
        private long totalRecords;
        private long presentCount;
        private long absentCount;
        private long tardyCount;
        private double attendanceRate;
        private double absenteeismRate;
        private double tardyRate;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }

    /**
     * Daily Statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStats {
        private LocalDate date;
        private long presentCount;
        private long absentCount;
        private long tardyCount;
        private long totalCount;
        private double attendanceRate;
        private String dayOfWeek;
    }

    /**
     * Trend Analysis
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendAnalysis {
        private String trend; // IMPROVING, DECLINING, STABLE
        private double changePercentage;
        private double averageAttendanceRate;
        private double bestDayRate;
        private double worstDayRate;
        private String bestDay;
        private String worstDay;
        private List<String> insights;
    }

    /**
     * Grade-Level Statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeStats {
        private String grade;
        private long studentCount;
        private long presentCount;
        private long absentCount;
        private long tardyCount;
        private double attendanceRate;
        private double absenteeismRate;
    }

    /**
     * Student Absence Record
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAbsenceRecord {
        private Long studentId;
        private String studentName;
        private String grade;
        private long totalDays;
        private long absentDays;
        private double absenteeismRate;
        private String riskLevel;
    }
}
