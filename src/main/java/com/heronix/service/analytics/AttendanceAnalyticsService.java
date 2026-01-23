package com.heronix.service.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRecordRepository;
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
 * Attendance Analytics Service
 *
 * Provides comprehensive analytics for attendance data including:
 * - Attendance rates and trends
 * - Chronic absenteeism tracking
 * - Attendance by various dimensions (grade, course, teacher)
 * - Tardy patterns analysis
 * - Equity analysis by demographics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Service("attendanceAnalyticsServiceV2")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceAnalyticsService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // ATTENDANCE OVERVIEW
    // ========================================================================

    /**
     * Get comprehensive attendance analytics
     */
    @Cacheable(value = "attendanceAnalytics", key = "#filter.campusId + '_' + #filter.startDate + '_' + #filter.endDate")
    public AttendanceAnalyticsDTO getAttendanceAnalytics(AnalyticsFilterDTO filter) {
        log.info("Fetching attendance analytics for campus: {}, range: {} to {}",
                filter.getCampusId(), filter.getStartDate(), filter.getEndDate());

        Long campusId = filter.getCampusId();
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        return AttendanceAnalyticsDTO.builder()
                .overallRate(getOverallAttendanceRate(campusId, startDate, endDate))
                .dailyAttendance(getDailyAttendanceTrend(campusId, startDate, endDate))
                .attendanceByGrade(getAttendanceByGrade(campusId, startDate, endDate))
                .chronicAbsenteeism(getChronicAbsenteeismSummary(campusId, startDate, endDate))
                .tardyPatterns(getTardyPatterns(campusId, startDate, endDate))
                .equityAnalysis(getEquityAnalysis(campusId, startDate, endDate))
                .generatedAt(LocalDate.now())
                .build();
    }

    /**
     * Get overall attendance rate for a period
     */
    public Double getOverallAttendanceRate(Long campusId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Object[]> metrics = attendanceRecordRepository.getOverallAttendanceMetrics(startDate, endDate, campusId);
            if (metrics != null && !metrics.isEmpty()) {
                Object[] row = metrics.get(0);
                Long presentCount = row[0] != null ? (Long) row[0] : 0L;
                Long totalCount = row[1] != null ? (Long) row[1] : 0L;
                if (totalCount > 0) {
                    return Math.round(presentCount * 1000.0 / totalCount) / 10.0;
                }
            }
        } catch (Exception e) {
            log.warn("Error calculating overall attendance rate: {}", e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get today's attendance rate
     */
    public Double getTodayAttendanceRate(Long campusId) {
        try {
            Double rate = attendanceRecordRepository.getAttendanceRateForDate(LocalDate.now(), campusId);
            return rate != null ? Math.round(rate * 10.0) / 10.0 : 0.0;
        } catch (Exception e) {
            log.warn("Error getting today's attendance rate: {}", e.getMessage());
            return 0.0;
        }
    }

    // ========================================================================
    // DAILY ATTENDANCE TRENDS
    // ========================================================================

    /**
     * Get daily attendance trend data for charting
     */
    public List<AttendanceAnalyticsDTO.DailyAttendance> getDailyAttendanceTrend(
            Long campusId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> trendData = attendanceRecordRepository.getAttendanceTrends(startDate, endDate, campusId);

        return trendData.stream()
                .map(row -> AttendanceAnalyticsDTO.DailyAttendance.builder()
                        .date((LocalDate) row[0])
                        .presentCount(row[1] != null ? ((Long) row[1]).intValue() : 0)
                        .absentCount(row[2] != null ? ((Long) row[2]).intValue() : 0)
                        .tardyCount(row[3] != null ? ((Long) row[3]).intValue() : 0)
                        .attendanceRate(calculateRate(row[1], row[4]))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get attendance rate by date for simple trend display
     */
    public Map<LocalDate, Double> getAttendanceRatesByDate(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> trendData = attendanceRecordRepository.getAttendanceTrends(startDate, endDate, campusId);

        Map<LocalDate, Double> rates = new LinkedHashMap<>();
        for (Object[] row : trendData) {
            LocalDate date = (LocalDate) row[0];
            Double rate = calculateRate(row[1], row[4]);
            rates.put(date, rate);
        }
        return rates;
    }

    // ========================================================================
    // ATTENDANCE BY DIMENSION
    // ========================================================================

    /**
     * Get attendance rate by grade level
     */
    public Map<String, Double> getAttendanceByGrade(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = attendanceRecordRepository.getAttendanceRateByGrade(startDate, endDate, campusId);

        Map<String, Double> byGrade = new LinkedHashMap<>();
        for (Object[] row : data) {
            String grade = row[0] != null ? (String) row[0] : "Unknown";
            Double rate = row[1] != null ? Math.round((Double) row[1] * 10.0) / 10.0 : 0.0;
            byGrade.put(grade, rate);
        }
        return byGrade;
    }

    /**
     * Get attendance rate by campus (for district view)
     */
    public Map<String, Double> getAttendanceByCampus(LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = attendanceRecordRepository.getAttendanceRateByCampus(startDate, endDate);

        Map<String, Double> byCampus = new LinkedHashMap<>();
        for (Object[] row : data) {
            String campusName = row[0] != null ? (String) row[0] : "Unknown";
            Double rate = row[1] != null ? Math.round((Double) row[1] * 10.0) / 10.0 : 0.0;
            byCampus.put(campusName, rate);
        }
        return byCampus;
    }

    // ========================================================================
    // CHRONIC ABSENTEEISM
    // ========================================================================

    /**
     * Get chronic absenteeism summary
     */
    @Cacheable(value = "chronicAbsenteeism", key = "#campusId + '_' + #startDate + '_' + #endDate")
    public AttendanceAnalyticsDTO.ChronicAbsenteeismSummary getChronicAbsenteeismSummary(
            Long campusId, LocalDate startDate, LocalDate endDate) {

        // Get students with attendance below different thresholds
        // Chronic: < 90%, Severe: < 85%, Critical: < 80%
        List<Object[]> chronicData = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 0.10); // 10% absence = 90% attendance

        long totalStudents = studentRepository.countActiveStudents(campusId);

        // Count by threshold
        int below90 = 0;
        int below85 = 0;
        int below80 = 0;

        for (Object[] row : chronicData) {
            Double absenceRate = row[2] != null ? (Double) row[2] : 0.0;
            if (absenceRate >= 0.20) { // 20%+ absence = below 80% attendance
                below80++;
            } else if (absenceRate >= 0.15) { // 15-20% absence = 80-85% attendance
                below85++;
            } else if (absenceRate >= 0.10) { // 10-15% absence = 85-90% attendance
                below90++;
            }
        }

        return AttendanceAnalyticsDTO.ChronicAbsenteeismSummary.builder()
                .chronicCount((long) chronicData.size())
                .severeCount((long) (below85 + below80))
                .criticalCount((long) below80)
                .percentChronic(totalStudents > 0 ? chronicData.size() * 100.0 / totalStudents : 0.0)
                .build();
    }

    /**
     * Get list of chronically absent students
     */
    public List<Map<String, Object>> getChronicallyAbsentStudents(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = attendanceRecordRepository.findChronicallyAbsentStudentsAnalytics(
                startDate, endDate, campusId, 0.10);

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> student = new LinkedHashMap<>();
            student.put("studentId", row[0]);
            student.put("studentName", row[1]);
            student.put("absenceRate", row[2] != null ? Math.round((Double) row[2] * 1000.0) / 10.0 : 0.0);
            student.put("attendanceRate", row[2] != null ? Math.round((1.0 - (Double) row[2]) * 1000.0) / 10.0 : 0.0);
            student.put("absenceCount", row[3]);
            student.put("totalDays", row[4]);
            students.add(student);
        }
        return students;
    }

    // ========================================================================
    // TARDY PATTERNS
    // ========================================================================

    /**
     * Get tardy patterns analysis
     */
    public AttendanceAnalyticsDTO.TardyPatterns getTardyPatterns(Long campusId, LocalDate startDate, LocalDate endDate) {
        // Get tardies by period
        List<Object[]> byPeriod = attendanceRecordRepository.getTardyCountsByPeriod(startDate, endDate, campusId);

        Map<Integer, Long> tardiesByPeriod = new LinkedHashMap<>();
        long totalTardies = 0;
        for (Object[] row : byPeriod) {
            Integer period = row[0] != null ? (Integer) row[0] : 0;
            Long count = row[1] != null ? (Long) row[1] : 0L;
            tardiesByPeriod.put(period, count);
            totalTardies += count;
        }

        return AttendanceAnalyticsDTO.TardyPatterns.builder()
                .totalTardies(totalTardies)
                .tardiesByPeriod(tardiesByPeriod)
                .peakPeriod(findPeakPeriod(tardiesByPeriod))
                .build();
    }

    /**
     * Get frequent tardy students
     */
    public List<Map<String, Object>> getFrequentTardyStudents(Long campusId, LocalDate startDate, LocalDate endDate, int minTardies) {
        List<Object[]> data = attendanceRecordRepository.findFrequentTardyStudents(
                startDate, endDate, campusId, (long) minTardies);

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> student = new LinkedHashMap<>();
            student.put("studentId", row[0]);
            student.put("studentName", row[1]);
            student.put("tardyCount", row[2]);
            students.add(student);
        }
        return students;
    }

    // ========================================================================
    // EQUITY ANALYSIS
    // ========================================================================

    /**
     * Get attendance equity analysis by demographics
     */
    public AttendanceAnalyticsDTO.EquityAnalysis getEquityAnalysis(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> byEthnicity = attendanceRecordRepository.getAttendanceRateByEthnicity(startDate, endDate, campusId);

        Map<String, Double> ratesByEthnicity = new LinkedHashMap<>();
        double totalRate = 0;
        int count = 0;

        for (Object[] row : byEthnicity) {
            String ethnicity = row[0] != null ? (String) row[0] : "Unknown";
            Double rate = row[1] != null ? Math.round((Double) row[1] * 10.0) / 10.0 : 0.0;
            ratesByEthnicity.put(ethnicity, rate);
            totalRate += rate;
            count++;
        }

        double avgRate = count > 0 ? totalRate / count : 0;

        // Find groups with significant disparity (> 5% below average)
        List<String> disparityAlerts = new ArrayList<>();
        for (Map.Entry<String, Double> entry : ratesByEthnicity.entrySet()) {
            if (entry.getValue() < avgRate - 5) {
                disparityAlerts.add(String.format("%s (%.1f%%, %.1f%% below average)",
                        entry.getKey(), entry.getValue(), avgRate - entry.getValue()));
            }
        }

        return AttendanceAnalyticsDTO.EquityAnalysis.builder()
                .attendanceByEthnicity(ratesByEthnicity)
                .disparityAlerts(disparityAlerts)
                .overallAverage(Math.round(avgRate * 10.0) / 10.0)
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private Double calculateRate(Object presentCount, Object totalCount) {
        if (presentCount == null || totalCount == null) return 0.0;
        long present = presentCount instanceof Long ? (Long) presentCount : ((Number) presentCount).longValue();
        long total = totalCount instanceof Long ? (Long) totalCount : ((Number) totalCount).longValue();
        if (total == 0) return 0.0;
        return Math.round(present * 1000.0 / total) / 10.0;
    }

    private Integer findPeakPeriod(Map<Integer, Long> tardiesByPeriod) {
        return tardiesByPeriod.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1);
    }
}
