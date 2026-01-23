package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Attendance analytics data bundle
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAnalyticsDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long campusId;
    private String campusName;
    private LocalDate generatedAt;

    // Overall Metrics
    private Double overallAttendanceRate;
    private Double overallRate; // Alias for overallAttendanceRate
    private Double previousPeriodRate;
    private Double rateChange;
    private Long totalStudentDays;
    private Long presentDays;
    private Long absentDays;
    private Long tardyCount;
    private Long excusedAbsences;
    private Long unexcusedAbsences;

    // Daily breakdown
    private List<DailyAttendance> dailyTrend;
    private List<DailyAttendance> dailyAttendance; // Alias for dailyTrend

    // Status breakdown
    private Map<String, Long> statusBreakdown;
    private Map<String, Double> statusPercentages;

    // Chronic Absenteeism
    private ChronicAbsenteeismSummary chronicAbsenteeism;

    // Tardy patterns
    private TardyPatterns tardyPatterns;

    // Equity analysis
    private EquityAnalysis equityAnalysis;

    // By dimension breakdowns
    private Map<String, Double> attendanceByGrade;
    private Map<String, Double> attendanceByCampus;
    private Map<String, Double> attendanceByTeacher;

    /**
     * Daily attendance data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendance {
        private LocalDate date;
        private String dayOfWeek;
        private Long totalStudents;
        private Long present;
        private Long absent;
        private Long tardy;
        private Long excused;
        private Long unexcused;
        private Double attendanceRate;

        // Convenience getters using int
        private int presentCount;
        private int absentCount;
        private int tardyCount;
    }

    /**
     * Chronic absenteeism summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChronicAbsenteeismSummary {
        private Long totalStudents;
        private Long chronicCount; // Below 90%
        private Long severeCount; // Below 85%
        private Long criticalCount; // Below 80%
        private Double chronicPercentage;
        private Double percentChronic; // Alias
        private Double previousPeriodPercentage;
        private Double changeFromPrevious;
        private List<ChronicStudentDTO> topChronicStudents;
    }

    /**
     * Chronic student details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChronicStudentDTO {
        private Long studentId;
        private String studentNumber;
        private String studentName;
        private String gradeLevel;
        private Double attendanceRate;
        private Long totalAbsences;
        private Long excusedAbsences;
        private Long unexcusedAbsences;
        private String riskLevel; // CHRONIC, SEVERE, CRITICAL
    }

    /**
     * Tardy patterns
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TardyPatterns {
        private Long totalTardies;
        private Integer peakPeriod;
        private Map<Integer, Long> tardiesByPeriod;
        private Map<Integer, Long> tardyByPeriod; // Alias
        private Map<String, Long> tardyByDayOfWeek;
        private Map<String, Long> tardyByTimeRange;
        private List<FrequentTardyStudent> frequentTardyStudents;
    }

    /**
     * Frequent tardy student
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrequentTardyStudent {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private Long tardyCount;
        private Integer mostFrequentPeriod;
        private String mostFrequentDay;
    }

    /**
     * Equity analysis by demographic
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquityAnalysis {
        private String demographicCategory;
        private Map<String, Double> attendanceRates;
        private Map<String, Double> attendanceByEthnicity;
        private Map<String, Long> studentCounts;
        private Double overallAverage;
        private Double maxDisparity; // Difference between highest and lowest
        private String highestGroup;
        private String lowestGroup;
        private List<String> disparityAlerts;
        private List<DisparityAlert> alerts;
    }

    /**
     * Disparity alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisparityAlert {
        private String group;
        private Double attendanceRate;
        private Double districtAverage;
        private Double disparity;
        private String severity; // LOW, MEDIUM, HIGH
    }
}
