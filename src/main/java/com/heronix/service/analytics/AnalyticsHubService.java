package com.heronix.service.analytics;

import com.heronix.dto.analytics.AnalyticsFilterDTO;
import com.heronix.dto.analytics.AnalyticsSummaryDTO;
import com.heronix.dto.analytics.AttendanceAnalyticsDTO;
import com.heronix.dto.analytics.DemographicsBreakdownDTO;
import com.heronix.dto.analytics.EnrollmentTrendDTO;
import com.heronix.model.domain.Campus;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central analytics hub service providing overview metrics and navigation data
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsHubService {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TeacherRepository teacherRepository;
    private final BehaviorIncidentRepository behaviorIncidentRepository;
    private final CampusRepository campusRepository;

    /**
     * Get comprehensive analytics summary for the hub dashboard
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analyticsSummary", key = "#filter.campusId + '_' + T(java.time.LocalDate).now()")
    public AnalyticsSummaryDTO getHubSummary(AnalyticsFilterDTO filter) {
        log.info("Generating analytics hub summary for campus: {}", filter.getCampusId());

        Long campusId = filter.getCampusId();
        LocalDate today = LocalDate.now();
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : today.minusDays(30);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : today;

        // Student metrics
        Long totalStudents = studentRepository.countActiveStudents(campusId);
        Double averageGPA = studentRepository.getAverageGPA(campusId);
        List<Object[]> honorRollData = studentRepository.findHonorRollStudents(campusId, 3.5).stream()
                .limit(100).map(s -> new Object[]{s}).collect(Collectors.toList());

        // Attendance metrics
        Double attendanceRate = attendanceRecordRepository.getAttendanceRateForDate(today, campusId);
        List<Object[]> attendanceMetrics = attendanceRecordRepository.getOverallAttendanceMetrics(startDate, endDate, campusId);

        Long presentDays = 0L;
        Long absentDays = 0L;
        if (!attendanceMetrics.isEmpty()) {
            Object[] metrics = attendanceMetrics.get(0);
            presentDays = metrics[1] != null ? ((Number) metrics[1]).longValue() : 0L;
            absentDays = metrics[2] != null ? ((Number) metrics[2]).longValue() : 0L;
        }

        // Chronic absenteeism count (students below 90% attendance)
        List<Object[]> chronicData = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 10.0); // 10% absence = 90% attendance
        Long chronicAbsenteeismCount = (long) chronicData.size();

        // Staff metrics
        Long totalStaff = teacherRepository.countActiveTeachers(campusId);
        Long certifiedStaff = teacherRepository.countCertifiedTeachers(today, campusId);
        List<Object[]> expiringCerts = teacherRepository.findTeachersWithExpiringCertifications(
                today, today.plusDays(90), campusId).stream()
                .map(t -> new Object[]{t}).collect(Collectors.toList());
        Long expiringSoonCount = (long) expiringCerts.size();

        Double certificationComplianceRate = totalStaff > 0 ?
                (certifiedStaff.doubleValue() / totalStaff.doubleValue()) * 100.0 : 100.0;

        // Behavior metrics
        List<Object[]> behaviorSummary = behaviorIncidentRepository.getIncidentSummary(startDate, endDate, campusId);
        Long totalIncidentsWeek = 0L;
        Long positiveIncidents = 0L;
        Long negativeIncidents = 0L;
        if (!behaviorSummary.isEmpty()) {
            Object[] summary = behaviorSummary.get(0);
            totalIncidentsWeek = summary[0] != null ? ((Number) summary[0]).longValue() : 0L;
            positiveIncidents = summary[1] != null ? ((Number) summary[1]).longValue() : 0L;
            negativeIncidents = summary[2] != null ? ((Number) summary[2]).longValue() : 0L;
        }

        Long todayIncidents = behaviorIncidentRepository.countIncidents(today, today, campusId);
        Double positiveNegativeRatio = negativeIncidents > 0 ?
                positiveIncidents.doubleValue() / negativeIncidents.doubleValue() : 0.0;

        // At-risk counts
        List<Object[]> atRiskByGPA = studentRepository.findAtRiskStudentsByGPA(campusId, 2.0).stream()
                .map(s -> new Object[]{s}).collect(Collectors.toList());
        Long academicRisk = (long) atRiskByGPA.size();

        // Calculate behavior risk from repeat offenders (3+ negative incidents in last 30 days)
        Long behaviorRisk = calculateBehaviorRisk(campusId, startDate);

        // Build campus name
        String campusName = "All Campuses";
        if (campusId != null) {
            Optional<Campus> campus = campusRepository.findById(campusId);
            campusName = campus.map(Campus::getName).orElse("Unknown Campus");
        }

        return AnalyticsSummaryDTO.builder()
                .totalStudents(totalStudents)
                .activeStudents(totalStudents)
                .averageGPA(averageGPA != null ? averageGPA : 0.0)
                .honorRollCount((long) honorRollData.size())
                .attendanceRate(attendanceRate != null ? attendanceRate : 0.0)
                .studentsPresent(presentDays)
                .studentsAbsent(absentDays)
                .chronicAbsenteeismCount(chronicAbsenteeismCount)
                .totalStaff(totalStaff)
                .certificationCompliant(certifiedStaff)
                .certificationExpiringSoon(expiringSoonCount)
                .certificationComplianceRate(certificationComplianceRate)
                .totalIncidentsToday(todayIncidents)
                .totalIncidentsThisWeek(totalIncidentsWeek)
                .positiveIncidents(positiveIncidents)
                .negativeIncidents(negativeIncidents)
                .positiveNegativeRatio(positiveNegativeRatio)
                .atRiskStudentsTotal(academicRisk + chronicAbsenteeismCount + behaviorRisk)
                .academicRisk(academicRisk)
                .attendanceRisk(chronicAbsenteeismCount)
                .behaviorRisk(behaviorRisk)
                .dataAsOfDate(today)
                .generatedAt(LocalDateTime.now())
                .campusName(campusName)
                .academicYear(getCurrentAcademicYear())
                .build();
    }

    /**
     * Get quick stats for dashboard tiles
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getQuickStats(Long campusId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();

        // Student count
        stats.put("totalStudents", studentRepository.countActiveStudents(campusId));

        // Today's attendance rate
        Double todayRate = attendanceRecordRepository.getAttendanceRateForDate(today, campusId);
        stats.put("todayAttendanceRate", todayRate != null ? todayRate : 0.0);

        // Average GPA
        Double avgGPA = studentRepository.getAverageGPA(campusId);
        stats.put("averageGPA", avgGPA != null ? String.format("%.2f", avgGPA) : "N/A");

        // Today's incidents
        stats.put("todayIncidents", behaviorIncidentRepository.countIncidents(today, today, campusId));

        // Staff count
        stats.put("totalStaff", teacherRepository.countActiveTeachers(campusId));

        // Certification compliance
        Long totalStaff = teacherRepository.countActiveTeachers(campusId);
        Long certified = teacherRepository.countCertifiedTeachers(today, campusId);
        double complianceRate = totalStaff > 0 ? (certified.doubleValue() / totalStaff.doubleValue()) * 100.0 : 100.0;
        stats.put("certificationCompliance", String.format("%.1f%%", complianceRate));

        return stats;
    }

    /**
     * Get enrollment breakdown by grade level
     */
    @Transactional(readOnly = true)
    public List<EnrollmentTrendDTO.GradeBreakdown> getEnrollmentByGrade(Long campusId) {
        List<Object[]> data = studentRepository.countByGradeLevelForAnalytics(campusId);
        Long total = studentRepository.countActiveStudents(campusId);

        return data.stream()
                .filter(row -> row[0] != null)
                .map(row -> EnrollmentTrendDTO.GradeBreakdown.builder()
                        .gradeLevel((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .percentage(total > 0 ? (((Number) row[1]).doubleValue() / total) * 100.0 : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get demographics breakdown
     */
    @Transactional(readOnly = true)
    public DemographicsBreakdownDTO getDemographicsBreakdown(Long campusId) {
        Long total = studentRepository.countActiveStudents(campusId);

        // Gender
        Map<String, Long> genderDist = new HashMap<>();
        Map<String, Double> genderPct = new HashMap<>();
        studentRepository.countByGender(campusId).forEach(row -> {
            String gender = row[0] != null ? row[0].toString() : "Unknown";
            Long count = ((Number) row[1]).longValue();
            genderDist.put(gender, count);
            genderPct.put(gender, total > 0 ? (count.doubleValue() / total) * 100.0 : 0.0);
        });

        // Ethnicity
        Map<String, Long> ethnicityDist = new HashMap<>();
        Map<String, Double> ethnicityPct = new HashMap<>();
        studentRepository.countByEthnicity(campusId).forEach(row -> {
            String ethnicity = row[0] != null ? row[0].toString() : "Unknown";
            Long count = ((Number) row[1]).longValue();
            ethnicityDist.put(ethnicity, count);
            ethnicityPct.put(ethnicity, total > 0 ? (count.doubleValue() / total) * 100.0 : 0.0);
        });

        // Special needs
        Long iepCount = studentRepository.countIEPStudents(campusId);
        Long plan504Count = studentRepository.count504Students(campusId);
        Long giftedCount = studentRepository.countGiftedStudents(campusId);
        Long ellCount = studentRepository.countELLStudents(campusId);

        return DemographicsBreakdownDTO.builder()
                .totalStudents(total)
                .genderDistribution(genderDist)
                .genderPercentages(genderPct)
                .ethnicityDistribution(ethnicityDist)
                .ethnicityPercentages(ethnicityPct)
                .iepCount(iepCount != null ? iepCount : 0L)
                .iepPercentage(total > 0 && iepCount != null ? (iepCount.doubleValue() / total) * 100.0 : 0.0)
                .plan504Count(plan504Count != null ? plan504Count : 0L)
                .plan504Percentage(total > 0 && plan504Count != null ? (plan504Count.doubleValue() / total) * 100.0 : 0.0)
                .giftedCount(giftedCount != null ? giftedCount : 0L)
                .giftedPercentage(total > 0 && giftedCount != null ? (giftedCount.doubleValue() / total) * 100.0 : 0.0)
                .ellCount(ellCount != null ? ellCount : 0L)
                .ellPercentage(total > 0 && ellCount != null ? (ellCount.doubleValue() / total) * 100.0 : 0.0)
                .build();
    }

    /**
     * Get attendance summary for date range
     */
    @Transactional(readOnly = true)
    public AttendanceAnalyticsDTO.ChronicAbsenteeismSummary getChronicAbsenteeismSummary(
            Long campusId, LocalDate startDate, LocalDate endDate) {

        Long totalStudents = studentRepository.countActiveStudents(campusId);

        // Get counts at different thresholds
        List<Object[]> chronic90 = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 10.0);
        List<Object[]> severe85 = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 15.0);
        List<Object[]> critical80 = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 20.0);

        return AttendanceAnalyticsDTO.ChronicAbsenteeismSummary.builder()
                .totalStudents(totalStudents)
                .chronicCount((long) chronic90.size())
                .severeCount((long) severe85.size())
                .criticalCount((long) critical80.size())
                .chronicPercentage(totalStudents > 0 ?
                        (chronic90.size() * 100.0 / totalStudents) : 0.0)
                .build();
    }

    /**
     * Get list of all campuses for filter dropdown
     */
    @Transactional(readOnly = true)
    public List<Campus> getAllCampuses() {
        return campusRepository.findAll();
    }

    /**
     * Calculate current academic year string
     */
    private String getCurrentAcademicYear() {
        LocalDate now = LocalDate.now();
        int year = now.getMonthValue() >= 8 ? now.getYear() : now.getYear() - 1;
        return year + "-" + (year + 1);
    }

    /**
     * Calculate behavior risk count from repeat offenders
     * Students with 3+ negative behavior incidents are considered at-risk
     */
    private Long calculateBehaviorRisk(Long campusId, LocalDate sinceDate) {
        try {
            // Find students with 3 or more negative incidents since the start date
            List<Object[]> repeatOffenders = behaviorIncidentRepository.findRepeatOffenders(
                    sinceDate, campusId, 3L);

            return (long) repeatOffenders.size();
        } catch (Exception e) {
            log.error("Error calculating behavior risk for campus {}", campusId, e);
            return 0L;
        }
    }
}
