package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Hub overview metrics for the Analytics Hub dashboard
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDTO {

    // Student Metrics
    private Long totalStudents;
    private Long activeStudents;
    private Long newEnrollments;
    private Long withdrawals;

    // Attendance Metrics
    private Double attendanceRate;
    private Double attendanceRateChange; // vs previous period
    private Long studentsPresent;
    private Long studentsAbsent;
    private Long chronicAbsenteeismCount;

    // Academic Metrics
    private Double averageGPA;
    private Double averageGPAChange;
    private Long honorRollCount;
    private Long failingStudentsCount;
    private Double passRate;

    // Behavior Metrics
    private Long totalIncidentsToday;
    private Long totalIncidentsThisWeek;
    private Long positiveIncidents;
    private Long negativeIncidents;
    private Double positiveNegativeRatio;

    // Staff Metrics
    private Long totalStaff;
    private Long certificationCompliant;
    private Long certificationExpiringSoon;
    private Double certificationComplianceRate;

    // At-Risk Summary
    private Long atRiskStudentsTotal;
    private Long academicRisk;
    private Long attendanceRisk;
    private Long behaviorRisk;

    // Metadata
    private LocalDate dataAsOfDate;
    private LocalDateTime generatedAt;
    private String campusName;
    private String academicYear;

    /**
     * Calculate overall health score (0-100)
     */
    public Integer getOverallHealthScore() {
        double score = 0;
        int factors = 0;

        if (attendanceRate != null) {
            score += attendanceRate;
            factors++;
        }

        if (passRate != null) {
            score += passRate;
            factors++;
        }

        if (certificationComplianceRate != null) {
            score += certificationComplianceRate;
            factors++;
        }

        if (positiveNegativeRatio != null && positiveNegativeRatio > 0) {
            // Convert ratio to percentage (cap at 100)
            score += Math.min(positiveNegativeRatio * 50, 100);
            factors++;
        }

        return factors > 0 ? (int) (score / factors) : 0;
    }

    /**
     * Get health status based on score
     */
    public String getHealthStatus() {
        int score = getOverallHealthScore();
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "GOOD";
        if (score >= 70) return "FAIR";
        if (score >= 60) return "NEEDS_ATTENTION";
        return "CRITICAL";
    }
}
