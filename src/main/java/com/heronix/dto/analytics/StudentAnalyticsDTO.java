package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Student Analytics DTO
 *
 * Comprehensive bundle of all student analytics data for the Student Analytics view.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnalyticsDTO {

    /**
     * Total active students
     */
    private Long totalStudents;

    /**
     * Enrollment trends data
     */
    private EnrollmentTrendDTO enrollmentTrends;

    /**
     * Demographics breakdown
     */
    private DemographicsBreakdownDTO demographics;

    /**
     * Special needs breakdown
     */
    private DemographicsBreakdownDTO.SpecialNeedsBreakdown specialNeeds;

    /**
     * GPA distribution (ranges to counts)
     */
    private Map<String, Long> gpaDistribution;

    /**
     * Average GPA by grade level
     */
    private Map<String, Double> avgGpaByGrade;

    /**
     * Overall average GPA
     */
    private Double overallAverageGPA;

    /**
     * At-risk students summary
     */
    private Map<String, Object> atRiskSummary;

    /**
     * Honor roll summary
     */
    private Map<String, Object> honorRollSummary;

    /**
     * When this analytics data was generated
     */
    private LocalDate generatedAt;
}
