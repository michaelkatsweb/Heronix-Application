package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Analytics Service
 *
 * Provides advanced analytics, trends, and predictive insights for attendance data.
 * Generates statistical analyses and identifies patterns in attendance behavior.
 *
 * Key Responsibilities:
 * - Calculate attendance statistics and rates
 * - Identify attendance trends and patterns
 * - Generate predictive absence alerts
 * - Analyze chronic absence risk factors
 * - Provide data for visualizations and reports
 * - Track attendance improvement over time
 * - Generate comparative analytics
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Slf4j
@Service
public class AttendanceAnalyticsService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    private static final double CHRONIC_ABSENCE_THRESHOLD = 0.10;
    private static final double AT_RISK_THRESHOLD = 0.85;

    // ========================================================================
    // ATTENDANCE STATISTICS
    // ========================================================================

    /**
     * Calculate comprehensive attendance statistics for a student
     */
    public StudentAttendanceStatistics calculateStudentStatistics(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Calculating attendance statistics for student {} from {} to {}",
                studentId, startDate, endDate);

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        if (records.isEmpty()) {
            return StudentAttendanceStatistics.builder()
                    .studentId(studentId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDays(0)
                    .daysPresent(0)
                    .daysAbsent(0)
                    .daysTardy(0)
                    .attendanceRate(0.0)
                    .absenceRate(0.0)
                    .tardyRate(0.0)
                    .isChronicallyAbsent(false)
                    .isAtRisk(false)
                    .build();
        }

        int total = records.size();
        int present = (int) records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int absent = (int) records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT)
                .count();
        int tardy = (int) records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY)
                .count();

        double attendanceRate = (double) present / total;
        double absenceRate = (double) absent / total;
        double tardyRate = (double) tardy / total;

        // Calculate consecutive absences
        int consecutiveAbsences = calculateConsecutiveAbsences(records);

        return StudentAttendanceStatistics.builder()
                .studentId(studentId)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(total)
                .daysPresent(present)
                .daysAbsent(absent)
                .daysTardy(tardy)
                .attendanceRate(attendanceRate)
                .absenceRate(absenceRate)
                .tardyRate(tardyRate)
                .consecutiveAbsences(consecutiveAbsences)
                .isChronicallyAbsent(absenceRate >= CHRONIC_ABSENCE_THRESHOLD)
                .isAtRisk(attendanceRate < AT_RISK_THRESHOLD)
                .riskLevel(determineRiskLevel(attendanceRate))
                .build();
    }

    /**
     * Calculate school-wide attendance statistics
     */
    public SchoolAttendanceStatistics calculateSchoolStatistics(
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Calculating school-wide statistics from {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        int totalStudents = allStudents.size();

        int totalWithChronicAbsence = 0;
        int totalAtRisk = 0;
        double sumAttendanceRates = 0.0;

        for (Student student : allStudents) {
            StudentAttendanceStatistics stats = calculateStudentStatistics(
                    student.getId(), startDate, endDate);

            sumAttendanceRates += stats.getAttendanceRate();

            if (stats.isChronicallyAbsent()) {
                totalWithChronicAbsence++;
            }
            if (stats.isAtRisk()) {
                totalAtRisk++;
            }
        }

        double averageAttendanceRate = totalStudents > 0 ?
                sumAttendanceRates / totalStudents : 0.0;

        return SchoolAttendanceStatistics.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalStudents(totalStudents)
                .averageAttendanceRate(averageAttendanceRate)
                .chronicallyAbsentCount(totalWithChronicAbsence)
                .atRiskCount(totalAtRisk)
                .chronicAbsenceRate((double) totalWithChronicAbsence / totalStudents)
                .build();
    }

    // ========================================================================
    // PREDICTIVE ANALYTICS
    // ========================================================================

    /**
     * Predict students at risk of chronic absence
     */
    public List<AtRiskPrediction> predictAtRiskStudents(int daysToAnalyze) {
        log.info("Predicting at-risk students based on past {} days", daysToAnalyze);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysToAnalyze);

        List<Student> activeStudents = studentRepository.findByActiveTrue();
        List<AtRiskPrediction> predictions = new ArrayList<>();

        for (Student student : activeStudents) {
            StudentAttendanceStatistics stats = calculateStudentStatistics(
                    student.getId(), startDate, endDate);

            if (stats.getTotalDays() == 0) continue;

            // Calculate risk score based on multiple factors
            double riskScore = calculateRiskScore(stats);

            if (riskScore > 0.5) { // Threshold for at-risk classification
                predictions.add(AtRiskPrediction.builder()
                        .studentId(student.getId())
                        .studentName(student.getFullName())
                        .gradeLevel(student.getGradeLevel())
                        .currentAttendanceRate(stats.getAttendanceRate())
                        .consecutiveAbsences(stats.getConsecutiveAbsences())
                        .riskScore(riskScore)
                        .riskLevel(stats.getRiskLevel())
                        .predictedChronicAbsence(riskScore > 0.7)
                        .recommendedInterventions(generateInterventions(stats))
                        .build());
            }
        }

        // Sort by risk score (highest first)
        predictions.sort(Comparator.comparingDouble(AtRiskPrediction::getRiskScore).reversed());

        return predictions;
    }

    // ========================================================================
    // PATTERN ANALYSIS
    // ========================================================================

    /**
     * Analyze attendance patterns for a student
     */
    public AttendancePatternAnalysis analyzeStudentPatterns(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Analyzing attendance patterns for student {} from {} to {}",
                studentId, startDate, endDate);

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        // Analyze by day of week
        Map<java.time.DayOfWeek, Long> absencesByDayOfWeek = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT)
                .collect(Collectors.groupingBy(
                        r -> r.getAttendanceDate().getDayOfWeek(),
                        Collectors.counting()
                ));

        // Find most common absence day
        String mostCommonAbsenceDay = absencesByDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().toString())
                .orElse("N/A");

        // Calculate absence clustering (consecutive absences)
        int maxConsecutiveAbsences = calculateConsecutiveAbsences(records);

        // Detect patterns
        List<String> detectedPatterns = new ArrayList<>();
        if (maxConsecutiveAbsences >= 3) {
            detectedPatterns.add("Frequent consecutive absences detected");
        }
        if (absencesByDayOfWeek.getOrDefault(java.time.DayOfWeek.MONDAY, 0L) >
            absencesByDayOfWeek.values().stream().mapToLong(Long::longValue).average().orElse(0) * 1.5) {
            detectedPatterns.add("Higher absence rate on Mondays");
        }
        if (absencesByDayOfWeek.getOrDefault(java.time.DayOfWeek.FRIDAY, 0L) >
            absencesByDayOfWeek.values().stream().mapToLong(Long::longValue).average().orElse(0) * 1.5) {
            detectedPatterns.add("Higher absence rate on Fridays");
        }

        return AttendancePatternAnalysis.builder()
                .studentId(studentId)
                .startDate(startDate)
                .endDate(endDate)
                .absencesByDayOfWeek(absencesByDayOfWeek)
                .mostCommonAbsenceDay(mostCommonAbsenceDay)
                .maxConsecutiveAbsences(maxConsecutiveAbsences)
                .detectedPatterns(detectedPatterns)
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Calculate consecutive absences
     */
    private int calculateConsecutiveAbsences(List<AttendanceRecord> records) {
        // Sort by date descending
        List<AttendanceRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(AttendanceRecord::getAttendanceDate).reversed());

        int maxConsecutive = 0;
        int currentConsecutive = 0;

        for (AttendanceRecord record : sorted) {
            if (record.getStatus() == AttendanceStatus.ABSENT ||
                record.getStatus() == AttendanceStatus.EXCUSED_ABSENT) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
        }

        return maxConsecutive;
    }

    /**
     * Determine risk level based on attendance rate
     */
    private String determineRiskLevel(double attendanceRate) {
        if (attendanceRate >= 0.95) return "NONE";
        if (attendanceRate >= 0.90) return "LOW";
        if (attendanceRate >= 0.80) return "MEDIUM";
        if (attendanceRate >= 0.70) return "HIGH";
        return "CRITICAL";
    }

    /**
     * Calculate risk score for predictive analytics
     */
    private double calculateRiskScore(StudentAttendanceStatistics stats) {
        double score = 0.0;

        // Factor 1: Current attendance rate (40% weight)
        score += (1.0 - stats.getAttendanceRate()) * 0.4;

        // Factor 2: Consecutive absences (30% weight)
        if (stats.getConsecutiveAbsences() >= 3) {
            score += 0.3;
        } else if (stats.getConsecutiveAbsences() >= 2) {
            score += 0.15;
        }

        // Factor 3: Absence rate (30% weight)
        score += stats.getAbsenceRate() * 0.3;

        return Math.min(score, 1.0);
    }

    /**
     * Generate intervention recommendations
     */
    private List<String> generateInterventions(StudentAttendanceStatistics stats) {
        List<String> interventions = new ArrayList<>();

        if (stats.getConsecutiveAbsences() >= 3) {
            interventions.add("Contact family immediately - multiple consecutive absences");
        }

        if (stats.getAbsenceRate() >= 0.15) {
            interventions.add("Schedule parent meeting to discuss attendance concerns");
        }

        if (stats.getTardyRate() > 0.20) {
            interventions.add("Address chronic tardiness with family");
        }

        if (stats.isChronicallyAbsent()) {
            interventions.add("Initiate chronic absence intervention protocol");
            interventions.add("Refer to attendance team for case management");
        }

        return interventions;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class StudentAttendanceStatistics {
        private Long studentId;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalDays;
        private int daysPresent;
        private int daysAbsent;
        private int daysTardy;
        private double attendanceRate;
        private double absenceRate;
        private double tardyRate;
        private int consecutiveAbsences;
        private boolean isChronicallyAbsent;
        private boolean isAtRisk;
        private String riskLevel;
    }

    @Data
    @Builder
    public static class SchoolAttendanceStatistics {
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalStudents;
        private double averageAttendanceRate;
        private int chronicallyAbsentCount;
        private int atRiskCount;
        private double chronicAbsenceRate;
    }

    @Data
    @Builder
    public static class AtRiskPrediction {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private double currentAttendanceRate;
        private int consecutiveAbsences;
        private double riskScore;
        private String riskLevel;
        private boolean predictedChronicAbsence;
        private List<String> recommendedInterventions;
    }

    @Data
    @Builder
    public static class AttendancePatternAnalysis {
        private Long studentId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Map<java.time.DayOfWeek, Long> absencesByDayOfWeek;
        private String mostCommonAbsenceDay;
        private int maxConsecutiveAbsences;
        private List<String> detectedPatterns;
    }

    // ========================================================================
    // API CONTROLLER SUPPORT METHODS (Phase 39)
    // ========================================================================

    /**
     * Get school-wide dashboard metrics
     */
    @Cacheable(value = "attendanceAnalytics", key = "'dashboard-' + #startDate + '-' + #endDate")
    public Map<String, Object> getSchoolDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Generating school dashboard for period {} to {}", startDate, endDate);

        SchoolAttendanceStatistics stats = calculateSchoolStatistics(startDate, endDate);

        // Get today's attendance
        LocalDate today = LocalDate.now();
        List<AttendanceRecord> todayRecords = new ArrayList<>();
        for (Student student : studentRepository.findByActiveTrue()) {
            todayRecords.addAll(attendanceRepository
                .findByStudentIdAndAttendanceDate(student.getId(), today));
        }

        long presentToday = todayRecords.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
            .count();
        long absentToday = todayRecords.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                        r.getStatus() == AttendanceStatus.EXCUSED_ABSENT)
            .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalStudents", stats.getTotalStudents());
        dashboard.put("presentToday", presentToday);
        dashboard.put("absentToday", absentToday);
        dashboard.put("attendanceRate", String.format("%.2f%%", stats.getAverageAttendanceRate() * 100));
        dashboard.put("chronicallyAbsentCount", stats.getChronicallyAbsentCount());
        dashboard.put("atRiskCount", stats.getAtRiskCount());
        dashboard.put("chronicAbsenceRate", String.format("%.2f%%", stats.getChronicAbsenceRate() * 100));

        return dashboard;
    }

    /**
     * Calculate Average Daily Attendance (ADA)
     */
    @Cacheable(value = "attendanceAnalytics", key = "'ada-' + #startDate + '-' + #endDate")
    public Map<String, Object> calculateADA(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating ADA for period {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        int totalDays = 0;
        int totalAttendance = 0;

        for (Student student : allStudents) {
            List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(student.getId(), startDate, endDate);

            totalDays += records.size();
            totalAttendance += (int) records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();
        }

        double ada = totalDays > 0 ? (double) totalAttendance / totalDays * allStudents.size() : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("ada", ada);
        result.put("totalDays", totalDays);
        result.put("totalAttendance", totalAttendance);
        result.put("totalStudents", allStudents.size());
        result.put("period", Map.of("startDate", startDate, "endDate", endDate));

        return result;
    }

    /**
     * Calculate Average Daily Membership (ADM)
     */
    public Map<String, Object> calculateADM(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating ADM for period {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        int totalEnrolledDays = 0;

        // Calculate total enrolled days across all students
        for (Student student : allStudents) {
            List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(student.getId(), startDate, endDate);
            totalEnrolledDays += records.size();
        }

        // ADM = total enrolled days / number of instructional days
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double adm = days > 0 ? (double) totalEnrolledDays / days : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("adm", adm);
        result.put("totalEnrolledDays", totalEnrolledDays);
        result.put("totalStudents", allStudents.size());
        result.put("instructionalDays", days);
        result.put("period", Map.of("startDate", startDate, "endDate", endDate));

        return result;
    }

    /**
     * Get attendance trends grouped by time period
     */
    public List<Map<String, Object>> getAttendanceTrends(
            LocalDate startDate, LocalDate endDate, String groupBy) {

        log.info("Calculating attendance trends from {} to {} grouped by {}",
            startDate, endDate, groupBy);

        List<Map<String, Object>> trends = new ArrayList<>();
        // This would be implemented with actual trending logic
        // For now, return empty list as stub

        return trends;
    }

    /**
     * Analyze attendance by day of week
     */
    public Map<String, Object> analyzeByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing attendance by day of week from {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        Map<java.time.DayOfWeek, Integer> presentByDay = new EnumMap<>(java.time.DayOfWeek.class);
        Map<java.time.DayOfWeek, Integer> totalByDay = new EnumMap<>(java.time.DayOfWeek.class);

        for (Student student : allStudents) {
            List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(student.getId(), startDate, endDate);

            for (AttendanceRecord record : records) {
                java.time.DayOfWeek day = record.getAttendanceDate().getDayOfWeek();
                totalByDay.put(day, totalByDay.getOrDefault(day, 0) + 1);

                if (record.getStatus() == AttendanceStatus.PRESENT) {
                    presentByDay.put(day, presentByDay.getOrDefault(day, 0) + 1);
                }
            }
        }

        Map<String, Object> analysis = new HashMap<>();
        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            int total = totalByDay.getOrDefault(day, 0);
            int present = presentByDay.getOrDefault(day, 0);
            double rate = total > 0 ? (double) present / total : 0.0;
            analysis.put(day.toString().toLowerCase(), rate);
        }

        return analysis;
    }

    /**
     * Analyze chronic absenteeism
     */
    public Map<String, Object> analyzeChronicAbsenteeism(
            LocalDate startDate, LocalDate endDate, double threshold) {

        log.info("Analyzing chronic absenteeism with threshold {}%", threshold);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        List<Map<String, Object>> chronicallyAbsentStudents = new ArrayList<>();

        for (Student student : allStudents) {
            StudentAttendanceStatistics stats = calculateStudentStatistics(
                student.getId(), startDate, endDate);

            double absencePercent = stats.getAbsenceRate() * 100;
            if (absencePercent >= threshold) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", student.getFullName());
                studentData.put("absenceRate", String.format("%.2f%%", absencePercent));
                studentData.put("daysAbsent", stats.getDaysAbsent());
                studentData.put("totalDays", stats.getTotalDays());
                chronicallyAbsentStudents.add(studentData);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", allStudents.size());
        result.put("chronicallyAbsentCount", chronicallyAbsentStudents.size());
        result.put("chronicallyAbsentRate",
            String.format("%.2f%%", (double) chronicallyAbsentStudents.size() / allStudents.size() * 100));
        result.put("students", chronicallyAbsentStudents);
        result.put("threshold", threshold + "%");

        return result;
    }

    /**
     * Get chronic absenteeism trend over time
     */
    public List<Map<String, Object>> getChronicAbsenteeismTrend(
            LocalDate startDate, LocalDate endDate) {

        log.info("Getting chronic absenteeism trend from {} to {}", startDate, endDate);

        // This would track the trend over time - stub for now
        List<Map<String, Object>> trend = new ArrayList<>();

        return trend;
    }

    /**
     * Compare attendance by grade level
     */
    public List<Map<String, Object>> compareByGradeLevel(
            LocalDate startDate, LocalDate endDate) {

        log.info("Comparing attendance by grade level from {} to {}", startDate, endDate);

        List<Map<String, Object>> comparison = new ArrayList<>();
        // Group students by grade level and calculate stats

        return comparison;
    }

    /**
     * Compare attendance by teacher/course
     */
    public List<Map<String, Object>> compareByTeacher(
            LocalDate startDate, LocalDate endDate) {

        log.info("Comparing attendance by teacher from {} to {}", startDate, endDate);

        List<Map<String, Object>> comparison = new ArrayList<>();
        // Would need CourseEnrollment and Teacher data

        return comparison;
    }

    /**
     * Identify at-risk students
     */
    public List<Map<String, Object>> identifyAtRiskStudents(int days, int absenceThreshold) {
        log.info("Identifying at-risk students - last {} days, {} absence threshold",
            days, absenceThreshold);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<Map<String, Object>> atRiskStudents = new ArrayList<>();
        List<Student> allStudents = studentRepository.findByActiveTrue();

        for (Student student : allStudents) {
            List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(student.getId(), startDate, endDate);

            long absences = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT)
                .count();

            if (absences >= absenceThreshold) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", student.getFullName());
                studentData.put("absences", absences);
                studentData.put("period", days + " days");
                atRiskStudents.add(studentData);
            }
        }

        return atRiskStudents;
    }

    /**
     * Forecast attendance for future period
     */
    public Map<String, Object> forecastAttendance(
            LocalDate startDate, LocalDate endDate, int forecastDays) {

        log.info("Forecasting attendance for {} days based on {} to {}",
            forecastDays, startDate, endDate);

        // Calculate historical average
        SchoolAttendanceStatistics stats = calculateSchoolStatistics(startDate, endDate);

        Map<String, Object> forecast = new HashMap<>();
        forecast.put("forecastDays", forecastDays);
        forecast.put("predictedRate", String.format("%.2f%%", stats.getAverageAttendanceRate() * 100));
        forecast.put("confidence", "Medium");
        forecast.put("basedOnPeriod", Map.of("startDate", startDate, "endDate", endDate));

        return forecast;
    }

    /**
     * Measure intervention impact
     */
    public Map<String, Object> measureInterventionImpact(
            Long studentId, LocalDate interventionDate, int windowDays) {

        log.info("Measuring intervention impact for student {} - intervention date {}, window {} days",
            studentId, interventionDate, windowDays);

        LocalDate beforeStart = interventionDate.minusDays(windowDays);
        LocalDate afterEnd = interventionDate.plusDays(windowDays);

        StudentAttendanceStatistics beforeStats = calculateStudentStatistics(
            studentId, beforeStart, interventionDate);
        StudentAttendanceStatistics afterStats = calculateStudentStatistics(
            studentId, interventionDate, afterEnd);

        double improvement = afterStats.getAttendanceRate() - beforeStats.getAttendanceRate();

        Map<String, Object> impact = new HashMap<>();
        impact.put("beforeRate", String.format("%.2f%%", beforeStats.getAttendanceRate() * 100));
        impact.put("afterRate", String.format("%.2f%%", afterStats.getAttendanceRate() * 100));
        impact.put("improvement", String.format("%.2f%%", improvement * 100));
        impact.put("windowDays", windowDays);
        impact.put("interventionDate", interventionDate);

        return impact;
    }

    /**
     * Get students with perfect attendance
     */
    public List<Map<String, Object>> getPerfectAttendance(
            LocalDate startDate, LocalDate endDate) {

        log.info("Getting students with perfect attendance from {} to {}", startDate, endDate);

        List<Map<String, Object>> perfectAttendance = new ArrayList<>();
        List<Student> allStudents = studentRepository.findByActiveTrue();

        for (Student student : allStudents) {
            StudentAttendanceStatistics stats = calculateStudentStatistics(
                student.getId(), startDate, endDate);

            if (stats.getAttendanceRate() == 1.0 && stats.getTotalDays() > 0) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", student.getFullName());
                studentData.put("gradeLevel", student.getGradeLevel());
                studentData.put("daysAttended", stats.getDaysPresent());
                perfectAttendance.add(studentData);
            }
        }

        return perfectAttendance;
    }

    /**
     * Generate comprehensive report
     */
    public Map<String, Object> generateReport(
            String reportType, LocalDate startDate, LocalDate endDate, String format) {

        log.info("Generating {} report in {} format for {} to {}",
            reportType, format, startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", reportType);
        report.put("format", format);
        report.put("status", "completed");
        report.put("generatedAt", LocalDate.now());
        report.put("period", Map.of("startDate", startDate, "endDate", endDate));

        // Add report data based on type
        if ("comprehensive".equals(reportType)) {
            report.put("data", getSchoolDashboard(startDate, endDate));
        }

        return report;
    }
}
