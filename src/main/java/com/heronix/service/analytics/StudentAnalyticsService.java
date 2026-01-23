package com.heronix.service.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.model.domain.Student;
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
 * Student Analytics Service
 *
 * Provides comprehensive analytics for student data including:
 * - Enrollment trends and projections
 * - Demographics breakdown
 * - Special needs analysis
 * - At-risk student identification
 * - GPA distribution and trends
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAnalyticsService {

    private final StudentRepository studentRepository;

    // ========================================================================
    // ENROLLMENT ANALYTICS
    // ========================================================================

    /**
     * Get enrollment trends data
     */
    @Cacheable(value = "enrollmentTrends", key = "#filter.campusId ?: 'all'")
    public EnrollmentTrendDTO getEnrollmentTrends(AnalyticsFilterDTO filter) {
        log.info("Fetching enrollment trends for campus: {}", filter.getCampusId());

        Long campusId = filter.getCampusId();

        // Get current enrollment by grade
        List<Object[]> gradeData = studentRepository.countByGradeLevelForAnalytics(campusId);
        List<EnrollmentTrendDTO.GradeBreakdown> gradeBreakdown = gradeData.stream()
                .map(row -> {
                    String grade = (String) row[0];
                    Long count = (Long) row[1];
                    return new EnrollmentTrendDTO.GradeBreakdown(
                            grade != null ? grade : "Unknown",
                            count != null ? count.intValue() : 0,
                            0.0 // Change percentage - would need historical data
                    );
                })
                .collect(Collectors.toList());

        // Calculate total
        long totalEnrollment = gradeBreakdown.stream()
                .mapToInt(EnrollmentTrendDTO.GradeBreakdown::getCount)
                .sum();

        return EnrollmentTrendDTO.builder()
                .totalEnrollment(totalEnrollment)
                .gradeBreakdown(gradeBreakdown)
                .yearOverYearChange(0.0) // Would need historical data
                .projectedEnrollment((int) (totalEnrollment * 1.02)) // Simple 2% projection
                .build();
    }

    /**
     * Get enrollment by grade level for charts
     */
    public List<EnrollmentTrendDTO.GradeBreakdown> getEnrollmentByGrade(Long campusId) {
        List<Object[]> data = studentRepository.countByGradeLevelForAnalytics(campusId);
        return data.stream()
                .map(row -> new EnrollmentTrendDTO.GradeBreakdown(
                        row[0] != null ? (String) row[0] : "Unknown",
                        row[1] != null ? ((Long) row[1]).intValue() : 0,
                        0.0
                ))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // DEMOGRAPHICS ANALYTICS
    // ========================================================================

    /**
     * Get comprehensive demographics breakdown
     */
    @Cacheable(value = "demographics", key = "#campusId ?: 'all'")
    public DemographicsBreakdownDTO getDemographicsBreakdown(Long campusId) {
        log.info("Fetching demographics breakdown for campus: {}", campusId);

        // Gender distribution
        List<Object[]> genderData = studentRepository.countByGender(campusId);
        Map<String, Long> genderDistribution = genderData.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));

        // Ethnicity distribution
        List<Object[]> ethnicityData = studentRepository.countByEthnicity(campusId);
        Map<String, Long> ethnicityDistribution = ethnicityData.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));

        // Race distribution
        List<Object[]> raceData = studentRepository.countByRace(campusId);
        Map<String, Long> raceDistribution = raceData.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));

        // Language distribution
        List<Object[]> languageData = studentRepository.countByLanguage(campusId);
        Map<String, Long> languageDistribution = languageData.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));

        // Calculate total
        long total = studentRepository.countActiveStudents(campusId);

        return DemographicsBreakdownDTO.builder()
                .totalStudents(total)
                .genderDistribution(genderDistribution)
                .ethnicityDistribution(ethnicityDistribution)
                .raceDistribution(raceDistribution)
                .languageDistribution(languageDistribution)
                .build();
    }

    /**
     * Get gender distribution for pie chart
     */
    public Map<String, Long> getGenderDistribution(Long campusId) {
        List<Object[]> data = studentRepository.countByGender(campusId);
        return data.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));
    }

    /**
     * Get ethnicity distribution for pie chart
     */
    public Map<String, Long> getEthnicityDistribution(Long campusId) {
        List<Object[]> data = studentRepository.countByEthnicity(campusId);
        return data.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));
    }

    /**
     * Get race distribution for pie chart
     */
    public Map<String, Long> getRaceDistribution(Long campusId) {
        List<Object[]> data = studentRepository.countByRace(campusId);
        return data.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));
    }

    /**
     * Get language distribution for pie chart
     */
    public Map<String, Long> getLanguageDistribution(Long campusId) {
        List<Object[]> data = studentRepository.countByLanguage(campusId);
        return data.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> row[1] != null ? (Long) row[1] : 0L
                ));
    }

    // ========================================================================
    // SPECIAL NEEDS ANALYTICS
    // ========================================================================

    /**
     * Get special needs breakdown
     */
    @Cacheable(value = "specialNeeds", key = "#campusId ?: 'all'")
    public DemographicsBreakdownDTO.SpecialNeedsBreakdown getSpecialNeedsBreakdown(Long campusId) {
        log.info("Fetching special needs breakdown for campus: {}", campusId);

        Long iepCount = studentRepository.countIEPStudents(campusId);
        Long plan504Count = studentRepository.count504Students(campusId);
        Long giftedCount = studentRepository.countGiftedStudents(campusId);
        Long ellCount = studentRepository.countELLStudents(campusId);
        long total = studentRepository.countActiveStudents(campusId);

        return DemographicsBreakdownDTO.SpecialNeedsBreakdown.builder()
                .iepCount(iepCount != null ? iepCount : 0L)
                .plan504Count(plan504Count != null ? plan504Count : 0L)
                .giftedCount(giftedCount != null ? giftedCount : 0L)
                .ellCount(ellCount != null ? ellCount : 0L)
                .totalSpecialNeeds((iepCount != null ? iepCount : 0L) +
                                   (plan504Count != null ? plan504Count : 0L) +
                                   (giftedCount != null ? giftedCount : 0L) +
                                   (ellCount != null ? ellCount : 0L))
                .percentOfTotal(total > 0 ?
                        ((iepCount != null ? iepCount : 0L) +
                         (plan504Count != null ? plan504Count : 0L)) * 100.0 / total : 0.0)
                .build();
    }

    /**
     * Get special needs distribution for charts
     */
    public Map<String, Long> getSpecialNeedsDistribution(Long campusId) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("IEP", studentRepository.countIEPStudents(campusId));
        distribution.put("504 Plan", studentRepository.count504Students(campusId));
        distribution.put("Gifted", studentRepository.countGiftedStudents(campusId));
        distribution.put("English Learner", studentRepository.countELLStudents(campusId));
        return distribution;
    }

    // ========================================================================
    // AT-RISK STUDENT ANALYTICS
    // ========================================================================

    /**
     * Get at-risk students based on GPA threshold
     */
    public List<Student> getAtRiskStudentsByGPA(Long campusId, Double gpaThreshold) {
        if (gpaThreshold == null) {
            gpaThreshold = 2.0; // Default threshold
        }
        return studentRepository.findAtRiskStudentsByGPA(campusId, gpaThreshold);
    }

    /**
     * Get at-risk summary
     */
    public Map<String, Object> getAtRiskSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Count at different GPA thresholds
        List<Student> below1_5 = studentRepository.findAtRiskStudentsByGPA(campusId, 1.5);
        List<Student> below2_0 = studentRepository.findAtRiskStudentsByGPA(campusId, 2.0);
        List<Student> below2_5 = studentRepository.findAtRiskStudentsByGPA(campusId, 2.5);

        summary.put("criticalRisk", below1_5.size()); // GPA < 1.5
        summary.put("highRisk", below2_0.size() - below1_5.size()); // 1.5 <= GPA < 2.0
        summary.put("moderateRisk", below2_5.size() - below2_0.size()); // 2.0 <= GPA < 2.5
        summary.put("totalAtRisk", below2_5.size());

        // Calculate percentages
        long total = studentRepository.countActiveStudents(campusId);
        summary.put("percentAtRisk", total > 0 ? below2_5.size() * 100.0 / total : 0.0);

        return summary;
    }

    // ========================================================================
    // GPA ANALYTICS
    // ========================================================================

    /**
     * Get GPA distribution
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

    /**
     * Get overall average GPA
     */
    public Double getOverallAverageGPA(Long campusId) {
        Double avg = studentRepository.getAverageGPA(campusId);
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    /**
     * Get honor roll students
     */
    public List<Student> getHonorRollStudents(Long campusId, Double gpaThreshold) {
        if (gpaThreshold == null) {
            gpaThreshold = 3.5; // Default honor roll threshold
        }
        return studentRepository.findHonorRollStudents(campusId, gpaThreshold);
    }

    /**
     * Get honor roll summary
     */
    public Map<String, Object> getHonorRollSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Different honor roll tiers
        List<Student> highHonors = studentRepository.findHonorRollStudents(campusId, 3.75);
        List<Student> honors = studentRepository.findHonorRollStudents(campusId, 3.5);
        List<Student> honorable = studentRepository.findHonorRollStudents(campusId, 3.25);

        summary.put("highHonors", highHonors.size()); // GPA >= 3.75
        summary.put("honors", honors.size() - highHonors.size()); // 3.5 <= GPA < 3.75
        summary.put("honorableMention", honorable.size() - honors.size()); // 3.25 <= GPA < 3.5
        summary.put("totalHonorRoll", honorable.size());

        // Calculate percentages
        long total = studentRepository.countActiveStudents(campusId);
        summary.put("percentHonorRoll", total > 0 ? honorable.size() * 100.0 / total : 0.0);

        return summary;
    }

    // ========================================================================
    // COMPREHENSIVE STUDENT ANALYTICS DTO
    // ========================================================================

    /**
     * Get complete student analytics bundle
     */
    public StudentAnalyticsDTO getStudentAnalytics(AnalyticsFilterDTO filter) {
        log.info("Fetching complete student analytics for filter: {}", filter);

        Long campusId = filter.getCampusId();

        return StudentAnalyticsDTO.builder()
                .totalStudents(studentRepository.countActiveStudents(campusId))
                .enrollmentTrends(getEnrollmentTrends(filter))
                .demographics(getDemographicsBreakdown(campusId))
                .specialNeeds(getSpecialNeedsBreakdown(campusId))
                .gpaDistribution(getGPADistribution(campusId))
                .avgGpaByGrade(getAverageGPAByGrade(campusId))
                .overallAverageGPA(getOverallAverageGPA(campusId))
                .atRiskSummary(getAtRiskSummary(campusId))
                .honorRollSummary(getHonorRollSummary(campusId))
                .generatedAt(LocalDate.now())
                .build();
    }
}
