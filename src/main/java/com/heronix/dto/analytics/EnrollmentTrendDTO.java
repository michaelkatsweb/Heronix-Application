package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Enrollment trend data for time-series charts
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentTrendDTO {

    private LocalDate date;
    private String period; // "2024-2025", "Fall 2024", "Week 1", etc.
    private String gradeLevel;
    private String campusName;
    private Long campusId;

    private Long totalEnrollment;
    private Long newEnrollments;
    private Long withdrawals;
    private Long transfers;
    private Long netChange;

    // Breakdown by status
    private Long activeCount;
    private Long inactiveCount;
    private Long graduatedCount;
    private Long withdrawnCount;

    // Capacity metrics
    private Long capacity;
    private Double utilizationRate;

    // Additional summary fields
    private Integer projectedEnrollment;
    private Double yearOverYearChange;
    private List<GradeBreakdown> gradeBreakdown;

    /**
     * Data point for time series chart
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String label;
        private LocalDate date;
        private Long value;
        private Double percentChange;
    }

    /**
     * Enrollment by grade breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeBreakdown {
        private String gradeLevel;
        private Long count;
        private Double percentage;
        private Long capacity;
        private Double utilizationRate;

        // Constructor for simple use
        public GradeBreakdown(String gradeLevel, int count, double changePercent) {
            this.gradeLevel = gradeLevel;
            this.count = (long) count;
            this.percentage = changePercent;
        }

        // Accessor returning int for compatibility
        public int getCount() {
            return count != null ? count.intValue() : 0;
        }
    }

    /**
     * Multi-year trend data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyTrend {
        private String academicYear;
        private Long totalEnrollment;
        private Long peakEnrollment;
        private LocalDate peakDate;
        private Double yearOverYearChange;
        private Map<String, Long> enrollmentByGrade;
    }

    /**
     * Campus comparison data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampusComparison {
        private Long campusId;
        private String campusName;
        private Long enrollment;
        private Long capacity;
        private Double utilizationRate;
        private Double yearOverYearChange;
        private List<GradeBreakdown> gradeBreakdown;
    }
}
