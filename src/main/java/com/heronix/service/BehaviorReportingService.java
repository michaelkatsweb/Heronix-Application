package com.heronix.service;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import com.heronix.repository.BehaviorIncidentRepository;
import com.heronix.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating behavior and discipline reports.
 *
 * Provides comprehensive reporting capabilities including:
 * - Student behavior summaries
 * - Campus/school-wide behavior statistics
 * - Trend analysis
 * - Category breakdowns
 * - CRDC reporting data
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Discipline/Behavior Management System
 */
@Service
public class BehaviorReportingService {

    private static final Logger log = LoggerFactory.getLogger(BehaviorReportingService.class);

    @Autowired
    private BehaviorIncidentRepository behaviorIncidentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    // ========================================================================
    // STUDENT BEHAVIOR REPORTS
    // ========================================================================

    /**
     * Generates a comprehensive behavior summary for a student.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return behavior summary report
     */
    public StudentBehaviorSummary generateStudentBehaviorSummary(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating behavior summary for student ID {} from {} to {}",
                student.getId(), startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, startDate, endDate);

        List<BehaviorIncident> positiveIncidents = allIncidents.stream()
                .filter(BehaviorIncident::isPositive)
                .collect(Collectors.toList());

        List<BehaviorIncident> negativeIncidents = allIncidents.stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        Map<BehaviorCategory, Long> categoryBreakdown = allIncidents.stream()
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getBehaviorCategory,
                        Collectors.counting()
                ));

        Map<SeverityLevel, Long> severityBreakdown = negativeIncidents.stream()
                .filter(i -> i.getSeverityLevel() != null)
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getSeverityLevel,
                        Collectors.counting()
                ));

        long uncontactedCount = behaviorIncidentService
                .getUncontactedParentIncidents(student).size();

        StudentBehaviorSummary summary = new StudentBehaviorSummary();
        summary.student = student;
        summary.startDate = startDate;
        summary.endDate = endDate;
        summary.totalIncidents = allIncidents.size();
        summary.positiveIncidents = positiveIncidents.size();
        summary.negativeIncidents = negativeIncidents.size();
        summary.categoryBreakdown = categoryBreakdown;
        summary.severityBreakdown = severityBreakdown;
        summary.uncontactedParentIncidents = uncontactedCount;
        summary.behaviorRatio = behaviorIncidentService
                .calculateBehaviorRatio(student, startDate, endDate);

        log.info("Generated behavior summary: {} total incidents ({} positive, {} negative)",
                summary.totalIncidents, summary.positiveIncidents, summary.negativeIncidents);

        return summary;
    }

    /**
     * Generates a detailed incident list report for a student.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param includePositive include positive incidents
     * @param includeNegative include negative incidents
     * @return list of incidents
     */
    public List<BehaviorIncident> generateStudentIncidentList(
            Student student,
            LocalDate startDate,
            LocalDate endDate,
            boolean includePositive,
            boolean includeNegative) {

        log.info("Generating incident list for student ID {} (positive: {}, negative: {})",
                student.getId(), includePositive, includeNegative);

        List<BehaviorIncident> incidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, startDate, endDate);

        return incidents.stream()
                .filter(i -> (includePositive && i.isPositive()) || (includeNegative && i.isNegative()))
                .sorted(Comparator.comparing(BehaviorIncident::getIncidentDate)
                        .thenComparing(BehaviorIncident::getIncidentTime)
                        .reversed())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // CAMPUS/SCHOOL-WIDE REPORTS
    // ========================================================================

    /**
     * Generates school-wide behavior statistics.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return school-wide behavior report
     */
    public SchoolBehaviorReport generateSchoolBehaviorReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating school-wide behavior report from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        long positiveCount = allIncidents.stream().filter(BehaviorIncident::isPositive).count();
        long negativeCount = allIncidents.stream().filter(BehaviorIncident::isNegative).count();

        Map<BehaviorCategory, Long> categoryBreakdown = allIncidents.stream()
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getBehaviorCategory,
                        Collectors.counting()
                ));

        Map<SeverityLevel, Long> severityBreakdown = allIncidents.stream()
                .filter(i -> i.getSeverityLevel() != null)
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getSeverityLevel,
                        Collectors.counting()
                ));

        long adminReferralsCount = allIncidents.stream()
                .filter(BehaviorIncident::getAdminReferralRequired)
                .count();

        long uncontactedParentsCount = allIncidents.stream()
                .filter(i -> i.getBehaviorType() == BehaviorType.NEGATIVE && !i.getParentContacted())
                .count();

        // Count unique students with incidents
        Set<Long> uniqueStudentIds = allIncidents.stream()
                .map(i -> i.getStudent().getId())
                .collect(Collectors.toSet());

        SchoolBehaviorReport report = new SchoolBehaviorReport();
        report.startDate = startDate;
        report.endDate = endDate;
        report.totalIncidents = allIncidents.size();
        report.positiveIncidents = positiveCount;
        report.negativeIncidents = negativeCount;
        report.categoryBreakdown = categoryBreakdown;
        report.severityBreakdown = severityBreakdown;
        report.adminReferralsCount = adminReferralsCount;
        report.uncontactedParentsCount = uncontactedParentsCount;
        report.studentsWithIncidents = uniqueStudentIds.size();

        log.info("School report: {} total incidents across {} students",
                report.totalIncidents, report.studentsWithIncidents);

        return report;
    }

    /**
     * Generates top students by positive behavior.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param topN number of top students to return
     * @return list of students with positive incident counts
     */
    public List<StudentIncidentCount> getTopPositiveBehaviorStudents(
            LocalDate startDate,
            LocalDate endDate,
            int topN) {

        log.info("Generating top {} positive behavior students from {} to {}",
                topN, startDate, endDate);

        List<BehaviorIncident> positiveIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isPositive)
                .collect(Collectors.toList());

        Map<Student, Long> studentCounts = positiveIncidents.stream()
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getStudent,
                        Collectors.counting()
                ));

        return studentCounts.entrySet().stream()
                .map(entry -> new StudentIncidentCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(StudentIncidentCount::getCount).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Generates students requiring intervention (most negative incidents).
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param topN number of students to return
     * @return list of students with negative incident counts
     */
    public List<StudentIncidentCount> getStudentsRequiringIntervention(
            LocalDate startDate,
            LocalDate endDate,
            int topN) {

        log.info("Generating top {} students requiring intervention from {} to {}",
                topN, startDate, endDate);

        List<BehaviorIncident> negativeIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        Map<Student, Long> studentCounts = negativeIncidents.stream()
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getStudent,
                        Collectors.counting()
                ));

        return studentCounts.entrySet().stream()
                .map(entry -> new StudentIncidentCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(StudentIncidentCount::getCount).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // TREND ANALYSIS
    // ========================================================================

    /**
     * Generates monthly behavior trend data.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return monthly trend data
     */
    public List<MonthlyBehaviorTrend> generateMonthlyTrend(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating monthly behavior trend for student ID {} from {} to {}",
                student.getId(), startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, startDate, endDate);

        Map<String, List<BehaviorIncident>> incidentsByMonth = incidents.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getIncidentDate().getYear() + "-" +
                                String.format("%02d", i.getIncidentDate().getMonthValue())
                ));

        return incidentsByMonth.entrySet().stream()
                .map(entry -> {
                    List<BehaviorIncident> monthIncidents = entry.getValue();
                    long positiveCount = monthIncidents.stream()
                            .filter(BehaviorIncident::isPositive).count();
                    long negativeCount = monthIncidents.stream()
                            .filter(BehaviorIncident::isNegative).count();

                    MonthlyBehaviorTrend trend = new MonthlyBehaviorTrend();
                    trend.monthKey = entry.getKey();
                    trend.totalIncidents = monthIncidents.size();
                    trend.positiveIncidents = positiveCount;
                    trend.negativeIncidents = negativeCount;

                    return trend;
                })
                .sorted(Comparator.comparing(MonthlyBehaviorTrend::getMonthKey))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // CATEGORY ANALYSIS
    // ========================================================================

    /**
     * Generates behavior category breakdown for a student.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return map of categories to incident counts
     */
    public Map<BehaviorCategory, Long> getCategoryBreakdown(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating category breakdown for student ID {}", student.getId());

        List<BehaviorIncident> incidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, startDate, endDate);

        return incidents.stream()
                .collect(Collectors.groupingBy(
                        BehaviorIncident::getBehaviorCategory,
                        Collectors.counting()
                ));
    }

    /**
     * Gets most common behavior category for a student.
     *
     * @param student the student
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return most common category, or null if no incidents
     */
    public BehaviorCategory getMostCommonCategory(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        Map<BehaviorCategory, Long> breakdown = getCategoryBreakdown(student, startDate, endDate);

        return breakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // ========================================================================

    /**
     * Student behavior summary report DTO.
     */
    public static class StudentBehaviorSummary {
        public Student student;
        public LocalDate startDate;
        public LocalDate endDate;
        public int totalIncidents;
        public int positiveIncidents;
        public int negativeIncidents;
        public Map<BehaviorCategory, Long> categoryBreakdown;
        public Map<SeverityLevel, Long> severityBreakdown;
        public long uncontactedParentIncidents;
        public double behaviorRatio;

        public Student getStudent() { return student; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTotalIncidents() { return totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public Map<BehaviorCategory, Long> getCategoryBreakdown() { return categoryBreakdown; }
        public Map<SeverityLevel, Long> getSeverityBreakdown() { return severityBreakdown; }
        public long getUncontactedParentIncidents() { return uncontactedParentIncidents; }
        public double getBehaviorRatio() { return behaviorRatio; }
    }

    /**
     * School-wide behavior report DTO.
     */
    public static class SchoolBehaviorReport {
        public LocalDate startDate;
        public LocalDate endDate;
        public int totalIncidents;
        public long positiveIncidents;
        public long negativeIncidents;
        public Map<BehaviorCategory, Long> categoryBreakdown;
        public Map<SeverityLevel, Long> severityBreakdown;
        public long adminReferralsCount;
        public long uncontactedParentsCount;
        public int studentsWithIncidents;

        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTotalIncidents() { return totalIncidents; }
        public long getPositiveIncidents() { return positiveIncidents; }
        public long getNegativeIncidents() { return negativeIncidents; }
        public Map<BehaviorCategory, Long> getCategoryBreakdown() { return categoryBreakdown; }
        public Map<SeverityLevel, Long> getSeverityBreakdown() { return severityBreakdown; }
        public long getAdminReferralsCount() { return adminReferralsCount; }
        public long getUncontactedParentsCount() { return uncontactedParentsCount; }
        public int getStudentsWithIncidents() { return studentsWithIncidents; }
    }

    /**
     * Student incident count DTO.
     */
    public static class StudentIncidentCount {
        private final Student student;
        private final long count;

        public StudentIncidentCount(Student student, long count) {
            this.student = student;
            this.count = count;
        }

        public Student getStudent() { return student; }
        public long getCount() { return count; }
    }

    /**
     * Monthly behavior trend DTO.
     */
    public static class MonthlyBehaviorTrend {
        public String monthKey;
        public int totalIncidents;
        public long positiveIncidents;
        public long negativeIncidents;

        public String getMonthKey() { return monthKey; }
        public int getTotalIncidents() { return totalIncidents; }
        public long getPositiveIncidents() { return positiveIncidents; }
        public long getNegativeIncidents() { return negativeIncidents; }
    }
}
