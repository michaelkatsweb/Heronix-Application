package com.heronix.service.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Academic Performance Analytics Service
 *
 * Provides comprehensive analytics for academic performance including:
 * - Grade distribution analysis
 * - GPA trends over time
 * - Pass/fail rates by course
 * - Honor roll tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicPerformanceAnalyticsService {

    private final StudentRepository studentRepository;

    // ========================================================================
    // GPA ANALYTICS
    // ========================================================================

    /**
     * Get comprehensive academic performance analytics
     */
    @Cacheable(value = "academicPerformance", key = "#filter.campusId ?: 'all'")
    public AcademicPerformanceDTO getAcademicPerformance(AnalyticsFilterDTO filter) {
        log.info("Fetching academic performance analytics for campus: {}", filter.getCampusId());

        Long campusId = filter.getCampusId();

        // Get honor roll data
        Map<String, Object> honorRoll = getHonorRollSummary(campusId);
        Map<String, Object> atRisk = getAtRiskSummary(campusId);

        return AcademicPerformanceDTO.builder()
                .campusId(campusId)
                .averageGPA(getOverallAverageGPA(campusId))
                .gradeDistribution(getGPADistribution(campusId))
                // Honor roll counts
                .honorRollCount(honorRoll.get("totalHonorRoll") != null ? ((Number) honorRoll.get("totalHonorRoll")).longValue() : 0L)
                .highHonorsCount(honorRoll.get("highHonors") != null ? ((Number) honorRoll.get("highHonors")).longValue() : 0L)
                // At-risk counts
                .failingStudentCount(atRisk.get("totalAtRisk") != null ? ((Number) atRisk.get("totalAtRisk")).longValue() : 0L)
                .build();
    }

    /**
     * Get overall average GPA
     */
    public Double getOverallAverageGPA(Long campusId) {
        Double avg = studentRepository.getAverageGPA(campusId);
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    /**
     * Get GPA distribution (ranges to counts)
     */
    @Cacheable(value = "gpaDistribution", key = "#campusId ?: 'all'")
    public Map<String, Long> getGPADistribution(Long campusId) {
        log.info("Fetching GPA distribution for campus: {}", campusId);

        List<Object[]> data = studentRepository.getGPADistribution(campusId);
        Map<String, Long> distribution = new LinkedHashMap<>();

        // Order the results properly
        String[] order = {"3.5-4.0", "3.0-3.49", "2.5-2.99", "2.0-2.49", "1.0-1.99", "Below 1.0"};
        for (String range : order) {
            distribution.put(range, 0L);
        }

        for (Object[] row : data) {
            if (row[0] != null) {
                String range = (String) row[0];
                Long count = row[1] != null ? (Long) row[1] : 0L;
                distribution.put(range, count);
            }
        }

        return distribution;
    }

    /**
     * Get average GPA by grade level
     */
    public Map<String, Double> getAverageGPAByGrade(Long campusId) {
        List<Object[]> data = studentRepository.getAverageGPAByGrade(campusId);
        Map<String, Double> avgByGrade = new LinkedHashMap<>();

        for (Object[] row : data) {
            if (row[0] != null && row[1] != null) {
                String grade = (String) row[0];
                Double avgGpa = (Double) row[1];
                avgByGrade.put(grade, Math.round(avgGpa * 100.0) / 100.0);
            }
        }

        return avgByGrade;
    }

    // ========================================================================
    // HONOR ROLL
    // ========================================================================

    /**
     * Get honor roll summary with different tiers
     */
    public Map<String, Object> getHonorRollSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Different honor roll tiers
        int highHonors = studentRepository.findHonorRollStudents(campusId, 3.75).size();
        int honors = studentRepository.findHonorRollStudents(campusId, 3.5).size();
        int honorable = studentRepository.findHonorRollStudents(campusId, 3.25).size();

        summary.put("highHonors", highHonors); // GPA >= 3.75
        summary.put("honors", honors - highHonors); // 3.5 <= GPA < 3.75
        summary.put("honorableMention", honorable - honors); // 3.25 <= GPA < 3.5
        summary.put("totalHonorRoll", honorable);

        // Calculate percentages
        long total = studentRepository.countActiveStudents(campusId);
        summary.put("percentHonorRoll", total > 0 ? honorable * 100.0 / total : 0.0);

        return summary;
    }

    // ========================================================================
    // AT-RISK STUDENTS
    // ========================================================================

    /**
     * Get at-risk students summary based on GPA
     */
    public Map<String, Object> getAtRiskSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Count at different GPA thresholds
        int below1_5 = studentRepository.findAtRiskStudentsByGPA(campusId, 1.5).size();
        int below2_0 = studentRepository.findAtRiskStudentsByGPA(campusId, 2.0).size();
        int below2_5 = studentRepository.findAtRiskStudentsByGPA(campusId, 2.5).size();

        summary.put("criticalRisk", below1_5); // GPA < 1.5
        summary.put("highRisk", below2_0 - below1_5); // 1.5 <= GPA < 2.0
        summary.put("moderateRisk", below2_5 - below2_0); // 2.0 <= GPA < 2.5
        summary.put("totalAtRisk", below2_5);

        // Calculate percentages
        long total = studentRepository.countActiveStudents(campusId);
        summary.put("percentAtRisk", total > 0 ? below2_5 * 100.0 / total : 0.0);

        return summary;
    }

    // ========================================================================
    // GRADE DISTRIBUTION
    // ========================================================================

    /**
     * Get letter grade distribution (A, B, C, D, F)
     * This would require a StudentGradeRepository - placeholder for now
     */
    public Map<String, Long> getLetterGradeDistribution(Long campusId) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("A", 0L);
        distribution.put("B", 0L);
        distribution.put("C", 0L);
        distribution.put("D", 0L);
        distribution.put("F", 0L);
        // TODO: Implement with StudentGradeRepository
        return distribution;
    }
}
