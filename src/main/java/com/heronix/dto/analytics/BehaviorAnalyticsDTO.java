package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Behavior analytics data bundle
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalyticsDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long campusId;
    private String campusName;

    // Overall Metrics
    private Long totalIncidents;
    private Long positiveIncidents;
    private Long negativeIncidents;
    private Double positiveNegativeRatio;
    private Long previousPeriodIncidents;
    private Double changeFromPrevious;

    // Severity Breakdown
    private Long minorIncidents;
    private Long moderateIncidents;
    private Long majorIncidents;
    private Long severeIncidents;

    // Resolution Status
    private Long resolvedCount;
    private Long pendingCount;
    private Long escalatedCount;
    private Double avgResolutionDays;

    // Category Distribution
    private Map<String, Long> categoryDistribution;
    private Map<String, Double> categoryPercentages;

    // Location Distribution
    private Map<String, Long> locationDistribution;

    // Time Patterns
    private Map<String, Long> incidentsByDayOfWeek;
    private Map<Integer, Long> incidentsByPeriod;
    private Map<String, Long> incidentsByTimeRange;

    /**
     * Daily incident data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyIncidents {
        private LocalDate date;
        private String dayOfWeek;
        private Long total;
        private Long positive;
        private Long negative;
        private Long minor;
        private Long major;
    }

    /**
     * Incident category breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private String type; // POSITIVE or NEGATIVE
        private Long count;
        private Double percentage;
        private Double changeFromPrevious;
        private String colorHex;

        public static String getColorForCategory(String category, String type) {
            if ("POSITIVE".equals(type)) {
                return "#10B981"; // Green
            }
            return switch (category) {
                case "DISRUPTION" -> "#F59E0B";
                case "FIGHTING", "BULLYING", "HARASSMENT" -> "#EF4444";
                case "TARDINESS", "NON_COMPLIANCE" -> "#F97316";
                case "VANDALISM", "THEFT" -> "#DC2626";
                default -> "#9CA3AF";
            };
        }
    }

    /**
     * Location heatmap data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationHeatmap {
        private String location;
        private Long incidentCount;
        private Double percentage;
        private String topCategory;
        private String peakTime;
        private String severity; // LOW, MEDIUM, HIGH
    }

    /**
     * At-risk student behavior
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtRiskStudent {
        private Long studentId;
        private String studentNumber;
        private String studentName;
        private String gradeLevel;
        private Long totalIncidents;
        private Long negativeIncidents;
        private Long positiveIncidents;
        private Double positiveNegativeRatio;
        private String topCategory;
        private LocalDate mostRecentIncident;
        private String riskLevel; // WATCH, CONCERN, HIGH_RISK
        private Boolean hasInterventionPlan;
    }

    /**
     * Repeat offender data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepeatOffender {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private Long incidentCount;
        private Integer windowDays;
        private List<String> topCategories;
        private List<String> topLocations;
        private String trend; // INCREASING, STABLE, DECREASING
    }

    /**
     * Positive behavior recognition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositiveBehavior {
        private Long totalRecognitions;
        private Map<String, Long> categoryBreakdown;
        private List<TopPerformer> topStudents;
        private Double avgRecognitionsPerStudent;
    }

    /**
     * Top performer (positive behaviors)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private Long recognitionCount;
        private List<String> categories;
    }

    /**
     * Intervention effectiveness
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterventionEffectiveness {
        private String interventionType;
        private Long totalApplied;
        private Long successfulCount;
        private Double successRate;
        private Double avgDaysToResolution;
    }

    /**
     * Grade level comparison
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeLevelComparison {
        private String gradeLevel;
        private Long studentCount;
        private Long incidentCount;
        private Double incidentsPerStudent;
        private Double positiveNegativeRatio;
        private String topCategory;
    }

    /**
     * Time pattern analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePattern {
        private String timeSlot; // "Before School", "Period 1", "Lunch", etc.
        private LocalTime startTime;
        private LocalTime endTime;
        private Long incidentCount;
        private Double percentage;
        private String topCategory;
        private String topLocation;
    }
}
