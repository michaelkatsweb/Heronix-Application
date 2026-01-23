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

    // ========================================================================
    // SCHOOL-WIDE TREND ANALYSIS
    // ========================================================================

    /**
     * Generates school-wide behavior trends grouped by time period.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param groupBy grouping interval (day, week, month)
     * @return list of trend data points
     */
    public List<BehaviorTrendPoint> getSchoolWideTrends(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("Generating school-wide behavior trends from {} to {} grouped by {}", startDate, endDate, groupBy);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        Map<String, List<BehaviorIncident>> groupedIncidents;

        switch (groupBy.toLowerCase()) {
            case "day" -> groupedIncidents = allIncidents.stream()
                    .collect(Collectors.groupingBy(i -> i.getIncidentDate().toString()));
            case "week" -> groupedIncidents = allIncidents.stream()
                    .collect(Collectors.groupingBy(i -> {
                        LocalDate date = i.getIncidentDate();
                        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
                        return weekStart.toString();
                    }));
            default -> groupedIncidents = allIncidents.stream()
                    .collect(Collectors.groupingBy(i ->
                            i.getIncidentDate().getYear() + "-" +
                                    String.format("%02d", i.getIncidentDate().getMonthValue())));
        }

        List<BehaviorTrendPoint> trendPoints = groupedIncidents.entrySet().stream()
                .map(entry -> {
                    List<BehaviorIncident> incidents = entry.getValue();
                    long positive = incidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = incidents.stream().filter(BehaviorIncident::isNegative).count();

                    BehaviorTrendPoint point = new BehaviorTrendPoint();
                    point.setPeriodKey(entry.getKey());
                    point.setTotalIncidents(incidents.size());
                    point.setPositiveIncidents((int) positive);
                    point.setNegativeIncidents((int) negative);
                    point.setUniqueStudents((int) incidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    return point;
                })
                .sorted(Comparator.comparing(BehaviorTrendPoint::getPeriodKey))
                .collect(Collectors.toList());

        log.info("Generated {} trend points", trendPoints.size());
        return trendPoints;
    }

    // ========================================================================
    // COMPARATIVE ANALYTICS
    // ========================================================================

    /**
     * Compare behavior incidents by grade level.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return comparison data by grade level
     */
    public List<GradeLevelComparison> compareByGrade(LocalDate startDate, LocalDate endDate) {
        log.info("Comparing behavior by grade level from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        Map<String, List<BehaviorIncident>> byGradeLevel = allIncidents.stream()
                .filter(i -> i.getStudent().getGradeLevel() != null)
                .collect(Collectors.groupingBy(i -> i.getStudent().getGradeLevel()));

        // Get total students per grade level
        Map<String, Long> studentsPerGrade = studentRepository.findByActiveTrue().stream()
                .filter(s -> s.getGradeLevel() != null)
                .collect(Collectors.groupingBy(Student::getGradeLevel, Collectors.counting()));

        List<GradeLevelComparison> comparisons = byGradeLevel.entrySet().stream()
                .map(entry -> {
                    String gradeLevel = entry.getKey();
                    List<BehaviorIncident> incidents = entry.getValue();

                    long positive = incidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = incidents.stream().filter(BehaviorIncident::isNegative).count();
                    long totalStudents = studentsPerGrade.getOrDefault(gradeLevel, 1L);

                    GradeLevelComparison comp = new GradeLevelComparison();
                    comp.setGradeLevel(gradeLevel);
                    comp.setTotalIncidents(incidents.size());
                    comp.setPositiveIncidents((int) positive);
                    comp.setNegativeIncidents((int) negative);
                    comp.setTotalStudents((int) (long) totalStudents);
                    comp.setIncidentsPerStudent(totalStudents > 0 ? (double) incidents.size() / totalStudents : 0);
                    comp.setUniqueStudentsWithIncidents((int) incidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    return comp;
                })
                .sorted(Comparator.comparing(GradeLevelComparison::getGradeLevel))
                .collect(Collectors.toList());

        log.info("Generated {} grade level comparisons", comparisons.size());
        return comparisons;
    }

    /**
     * Compare behavior incidents by location.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return comparison data by location
     */
    public List<LocationComparison> compareByLocation(LocalDate startDate, LocalDate endDate) {
        log.info("Comparing behavior by location from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        Map<String, List<BehaviorIncident>> byLocation = allIncidents.stream()
                .filter(i -> i.getLocation() != null && !i.getLocation().isEmpty())
                .collect(Collectors.groupingBy(BehaviorIncident::getLocation));

        List<LocationComparison> comparisons = byLocation.entrySet().stream()
                .map(entry -> {
                    String location = entry.getKey();
                    List<BehaviorIncident> incidents = entry.getValue();

                    long positive = incidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = incidents.stream().filter(BehaviorIncident::isNegative).count();

                    // Find most common incident type at this location
                    Map<BehaviorCategory, Long> categoryCount = incidents.stream()
                            .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorCategory, Collectors.counting()));
                    String mostCommonType = categoryCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> e.getKey().name())
                            .orElse("Unknown");

                    LocationComparison comp = new LocationComparison();
                    comp.setLocation(location);
                    comp.setTotalIncidents(incidents.size());
                    comp.setPositiveIncidents((int) positive);
                    comp.setNegativeIncidents((int) negative);
                    comp.setUniqueStudents((int) incidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    comp.setMostCommonIncidentType(mostCommonType);
                    comp.setPercentageOfTotal(allIncidents.size() > 0 ?
                            (double) incidents.size() / allIncidents.size() * 100 : 0);
                    return comp;
                })
                .sorted(Comparator.comparing(LocationComparison::getTotalIncidents).reversed())
                .collect(Collectors.toList());

        log.info("Generated {} location comparisons", comparisons.size());
        return comparisons;
    }

    /**
     * Analyze behavior by incident type.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return analysis data by incident type
     */
    public List<IncidentTypeAnalysis> analyzeByType(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior by type from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        Map<BehaviorCategory, List<BehaviorIncident>> byCategory = allIncidents.stream()
                .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorCategory));

        List<IncidentTypeAnalysis> analyses = byCategory.entrySet().stream()
                .map(entry -> {
                    BehaviorCategory category = entry.getKey();
                    List<BehaviorIncident> incidents = entry.getValue();

                    long positive = incidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = incidents.stream().filter(BehaviorIncident::isNegative).count();

                    // Severity breakdown for negative incidents
                    Map<SeverityLevel, Long> severityBreakdown = incidents.stream()
                            .filter(BehaviorIncident::isNegative)
                            .filter(i -> i.getSeverityLevel() != null)
                            .collect(Collectors.groupingBy(BehaviorIncident::getSeverityLevel, Collectors.counting()));

                    // Admin referral rate
                    long adminReferrals = incidents.stream()
                            .filter(BehaviorIncident::getAdminReferralRequired)
                            .count();

                    IncidentTypeAnalysis analysis = new IncidentTypeAnalysis();
                    analysis.setCategory(category.name());
                    analysis.setCategoryDisplayName(category.getDisplayName());
                    analysis.setTotalIncidents(incidents.size());
                    analysis.setPositiveIncidents((int) positive);
                    analysis.setNegativeIncidents((int) negative);
                    analysis.setSeverityBreakdown(severityBreakdown);
                    analysis.setAdminReferralCount((int) adminReferrals);
                    analysis.setAdminReferralRate(incidents.size() > 0 ?
                            (double) adminReferrals / incidents.size() * 100 : 0);
                    analysis.setPercentageOfTotal(allIncidents.size() > 0 ?
                            (double) incidents.size() / allIncidents.size() * 100 : 0);
                    analysis.setUniqueStudents((int) incidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    return analysis;
                })
                .sorted(Comparator.comparing(IncidentTypeAnalysis::getTotalIncidents).reversed())
                .collect(Collectors.toList());

        log.info("Generated {} incident type analyses", analyses.size());
        return analyses;
    }

    // ========================================================================
    // DISCIPLINE ACTIONS
    // ========================================================================

    /**
     * Get discipline action summary.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return discipline action summary data
     */
    public DisciplineActionSummary getDisciplineActions(LocalDate startDate, LocalDate endDate) {
        log.info("Getting discipline actions from {} to {}", startDate, endDate);

        List<BehaviorIncident> negativeIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by action taken
        Map<String, Long> actionCounts = negativeIncidents.stream()
                .filter(i -> i.getActionTaken() != null && !i.getActionTaken().isEmpty())
                .collect(Collectors.groupingBy(BehaviorIncident::getActionTaken, Collectors.counting()));

        // Severity distribution
        Map<SeverityLevel, Long> severityDistribution = negativeIncidents.stream()
                .filter(i -> i.getSeverityLevel() != null)
                .collect(Collectors.groupingBy(BehaviorIncident::getSeverityLevel, Collectors.counting()));

        // Admin referrals
        long adminReferrals = negativeIncidents.stream()
                .filter(BehaviorIncident::getAdminReferralRequired)
                .count();

        // Parent contacts
        long parentContacted = negativeIncidents.stream()
                .filter(BehaviorIncident::getParentContacted)
                .count();

        DisciplineActionSummary summary = new DisciplineActionSummary();
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setTotalNegativeIncidents(negativeIncidents.size());
        summary.setActionCounts(actionCounts);
        summary.setSeverityDistribution(severityDistribution);
        summary.setAdminReferrals((int) adminReferrals);
        summary.setParentContactsMade((int) parentContacted);
        summary.setParentContactRate(negativeIncidents.size() > 0 ?
                (double) parentContacted / negativeIncidents.size() * 100 : 0);
        summary.setUniqueStudents((int) negativeIncidents.stream()
                .map(i -> i.getStudent().getId())
                .distinct()
                .count());

        log.info("Discipline summary: {} incidents, {} admin referrals, {} parent contacts",
                negativeIncidents.size(), adminReferrals, parentContacted);
        return summary;
    }

    // ========================================================================
    // INTERVENTION EFFECTIVENESS
    // ========================================================================

    /**
     * Measure intervention effectiveness by comparing behavior before and after.
     *
     * @param interventionType the type of intervention to analyze
     * @param daysBefore days to look before intervention
     * @param daysAfter days to look after intervention
     * @return intervention effectiveness data
     */
    public InterventionEffectivenessData measureInterventionEffectiveness(
            String interventionType, int daysBefore, int daysAfter) {
        log.info("Measuring effectiveness of intervention: {}", interventionType);

        // Find students who received this intervention
        List<BehaviorIncident> interventionIncidents = behaviorIncidentRepository.findAll().stream()
                .filter(i -> i.getActionTaken() != null &&
                        i.getActionTaken().toLowerCase().contains(interventionType.toLowerCase()))
                .collect(Collectors.toList());

        if (interventionIncidents.isEmpty()) {
            InterventionEffectivenessData data = new InterventionEffectivenessData();
            data.setInterventionType(interventionType);
            data.setStudentsAnalyzed(0);
            data.setMessage("No interventions of this type found");
            return data;
        }

        List<StudentInterventionResult> results = new ArrayList<>();

        // Group by student
        Map<Long, List<BehaviorIncident>> byStudent = interventionIncidents.stream()
                .collect(Collectors.groupingBy(i -> i.getStudent().getId()));

        for (Map.Entry<Long, List<BehaviorIncident>> entry : byStudent.entrySet()) {
            Student student = entry.getValue().get(0).getStudent();

            // Get earliest intervention date for this student
            LocalDate interventionDate = entry.getValue().stream()
                    .map(BehaviorIncident::getIncidentDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            LocalDate beforeStart = interventionDate.minusDays(daysBefore);
            LocalDate afterEnd = interventionDate.plusDays(daysAfter);

            // Count incidents before and after
            List<BehaviorIncident> beforeIncidents = behaviorIncidentService
                    .getIncidentsByStudentAndDateRange(student, beforeStart, interventionDate.minusDays(1))
                    .stream()
                    .filter(BehaviorIncident::isNegative)
                    .collect(Collectors.toList());

            List<BehaviorIncident> afterIncidents = behaviorIncidentService
                    .getIncidentsByStudentAndDateRange(student, interventionDate.plusDays(1), afterEnd)
                    .stream()
                    .filter(BehaviorIncident::isNegative)
                    .collect(Collectors.toList());

            StudentInterventionResult result = new StudentInterventionResult();
            result.setStudentId(student.getId());
            result.setStudentName(student.getFullName());
            result.setIncidentsBefore(beforeIncidents.size());
            result.setIncidentsAfter(afterIncidents.size());
            result.setImproved(afterIncidents.size() < beforeIncidents.size());
            result.setChangePercent(beforeIncidents.size() > 0 ?
                    ((double) (beforeIncidents.size() - afterIncidents.size()) / beforeIncidents.size()) * 100 : 0);

            results.add(result);
        }

        // Calculate aggregate statistics
        int improved = (int) results.stream().filter(StudentInterventionResult::isImproved).count();
        double avgIncidentsBefore = results.stream()
                .mapToInt(StudentInterventionResult::getIncidentsBefore)
                .average()
                .orElse(0);
        double avgIncidentsAfter = results.stream()
                .mapToInt(StudentInterventionResult::getIncidentsAfter)
                .average()
                .orElse(0);

        InterventionEffectivenessData data = new InterventionEffectivenessData();
        data.setInterventionType(interventionType);
        data.setStudentsAnalyzed(results.size());
        data.setStudentsImproved(improved);
        data.setImprovementRate(results.size() > 0 ? (double) improved / results.size() * 100 : 0);
        data.setAverageIncidentsBefore(avgIncidentsBefore);
        data.setAverageIncidentsAfter(avgIncidentsAfter);
        data.setOverallReduction(avgIncidentsBefore > 0 ?
                ((avgIncidentsBefore - avgIncidentsAfter) / avgIncidentsBefore) * 100 : 0);
        data.setStudentResults(results);
        data.setDaysBefore(daysBefore);
        data.setDaysAfter(daysAfter);

        log.info("Intervention effectiveness: {}% of {} students improved",
                String.format("%.1f", data.getImprovementRate()), results.size());
        return data;
    }

    // ========================================================================
    // AT-RISK STUDENTS
    // ========================================================================

    /**
     * Identify at-risk students based on recent behavior patterns.
     *
     * @param days number of days to look back
     * @param incidentThreshold minimum incidents to be considered at-risk
     * @return list of at-risk students
     */
    public List<AtRiskStudentData> getAtRiskStudents(int days, int incidentThreshold) {
        log.info("Identifying at-risk students (last {} days, threshold: {} incidents)", days, incidentThreshold);

        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();

        List<BehaviorIncident> recentIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by student
        Map<Long, List<BehaviorIncident>> byStudent = recentIncidents.stream()
                .collect(Collectors.groupingBy(i -> i.getStudent().getId()));

        List<AtRiskStudentData> atRiskStudents = byStudent.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= incidentThreshold)
                .map(entry -> {
                    Student student = entry.getValue().get(0).getStudent();
                    List<BehaviorIncident> incidents = entry.getValue();

                    // Calculate risk factors
                    long majorIncidents = incidents.stream()
                            .filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR ||
                                    i.getSeverityLevel() == SeverityLevel.SEVERE)
                            .count();

                    long adminReferrals = incidents.stream()
                            .filter(BehaviorIncident::getAdminReferralRequired)
                            .count();

                    // Most common category
                    String mostCommonCategory = incidents.stream()
                            .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorCategory, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> e.getKey().getDisplayName())
                            .orElse("Unknown");

                    // Calculate risk score (simple weighted formula)
                    int riskScore = incidents.size() * 10 +
                            (int) majorIncidents * 20 +
                            (int) adminReferrals * 15;

                    String riskLevel;
                    if (riskScore >= 100) riskLevel = "Critical";
                    else if (riskScore >= 70) riskLevel = "High";
                    else if (riskScore >= 40) riskLevel = "Moderate";
                    else riskLevel = "Low";

                    AtRiskStudentData data = new AtRiskStudentData();
                    data.setStudentId(student.getId());
                    data.setStudentName(student.getFullName());
                    data.setStudentNumber(student.getStudentId());
                    data.setGradeLevel(student.getGradeLevel());
                    data.setTotalIncidents(incidents.size());
                    data.setMajorIncidents((int) majorIncidents);
                    data.setAdminReferrals((int) adminReferrals);
                    data.setMostCommonCategory(mostCommonCategory);
                    data.setRiskScore(riskScore);
                    data.setRiskLevel(riskLevel);
                    data.setDaysAnalyzed(days);
                    return data;
                })
                .sorted(Comparator.comparing(AtRiskStudentData::getRiskScore).reversed())
                .collect(Collectors.toList());

        log.info("Identified {} at-risk students", atRiskStudents.size());
        return atRiskStudents;
    }

    /**
     * Identify repeat offenders.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param minIncidents minimum incidents to be considered repeat offender
     * @return list of repeat offenders
     */
    public List<RepeatOffenderData> getRepeatOffenders(LocalDate startDate, LocalDate endDate, int minIncidents) {
        log.info("Identifying repeat offenders from {} to {} (min: {} incidents)", startDate, endDate, minIncidents);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by student
        Map<Long, List<BehaviorIncident>> byStudent = allIncidents.stream()
                .collect(Collectors.groupingBy(i -> i.getStudent().getId()));

        List<RepeatOffenderData> repeatOffenders = byStudent.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= minIncidents)
                .map(entry -> {
                    Student student = entry.getValue().get(0).getStudent();
                    List<BehaviorIncident> incidents = entry.getValue();

                    // Category breakdown
                    Map<String, Long> categoryBreakdown = incidents.stream()
                            .collect(Collectors.groupingBy(
                                    i -> i.getBehaviorCategory().getDisplayName(),
                                    Collectors.counting()));

                    // Most common category
                    String mostCommonCategory = categoryBreakdown.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Unknown");

                    // Average days between incidents
                    List<LocalDate> dates = incidents.stream()
                            .map(BehaviorIncident::getIncidentDate)
                            .sorted()
                            .collect(Collectors.toList());

                    double avgDaysBetween = 0;
                    if (dates.size() > 1) {
                        long totalDays = 0;
                        for (int i = 1; i < dates.size(); i++) {
                            totalDays += java.time.temporal.ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
                        }
                        avgDaysBetween = (double) totalDays / (dates.size() - 1);
                    }

                    long adminReferrals = incidents.stream()
                            .filter(BehaviorIncident::getAdminReferralRequired)
                            .count();

                    RepeatOffenderData data = new RepeatOffenderData();
                    data.setStudentId(student.getId());
                    data.setStudentName(student.getFullName());
                    data.setStudentNumber(student.getStudentId());
                    data.setGradeLevel(student.getGradeLevel());
                    data.setTotalIncidents(incidents.size());
                    data.setCategoryBreakdown(categoryBreakdown);
                    data.setMostCommonCategory(mostCommonCategory);
                    data.setAdminReferrals((int) adminReferrals);
                    data.setFirstIncidentDate(dates.get(0));
                    data.setLastIncidentDate(dates.get(dates.size() - 1));
                    data.setAverageDaysBetweenIncidents(avgDaysBetween);
                    return data;
                })
                .sorted(Comparator.comparing(RepeatOffenderData::getTotalIncidents).reversed())
                .collect(Collectors.toList());

        log.info("Identified {} repeat offenders", repeatOffenders.size());
        return repeatOffenders;
    }

    // ========================================================================
    // REPORT GENERATION
    // ========================================================================

    /**
     * Generate comprehensive behavior report.
     *
     * @param reportType type of report (student, school, teacher, grade)
     * @param entityId entity ID (student, teacher ID)
     * @param startDate start date
     * @param endDate end date
     * @param format output format
     * @return generated report data
     */
    public GeneratedBehaviorReportData generateReport(String reportType, Long entityId,
                                                        LocalDate startDate, LocalDate endDate, String format) {
        log.info("Generating {} behavior report for entity {} from {} to {}",
                reportType, entityId, startDate, endDate);

        GeneratedBehaviorReportData report = new GeneratedBehaviorReportData();
        report.setReportType(reportType);
        report.setEntityId(entityId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFormat(format);
        report.setGeneratedAt(java.time.LocalDateTime.now());
        report.setStatus("completed");

        Map<String, Object> reportData = new HashMap<>();

        switch (reportType.toLowerCase()) {
            case "student" -> {
                Student student = studentRepository.findById(entityId)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found: " + entityId));
                StudentBehaviorSummary summary = generateStudentBehaviorSummary(student, startDate, endDate);
                List<BehaviorIncident> incidents = generateStudentIncidentList(student, startDate, endDate, true, true);
                List<MonthlyBehaviorTrend> trends = generateMonthlyTrend(student, startDate, endDate);

                reportData.put("summary", summary);
                reportData.put("incidents", incidents.size());
                reportData.put("trends", trends);
                report.setTitle("Student Behavior Report - " + student.getFullName());
            }
            case "school" -> {
                SchoolBehaviorReport schoolReport = generateSchoolBehaviorReport(startDate, endDate);
                List<BehaviorTrendPoint> trends = getSchoolWideTrends(startDate, endDate, "week");
                List<AtRiskStudentData> atRisk = getAtRiskStudents(30, 3);

                reportData.put("schoolSummary", schoolReport);
                reportData.put("trends", trends);
                reportData.put("atRiskStudents", atRisk);
                report.setTitle("School-Wide Behavior Report");
            }
            case "grade" -> {
                List<GradeLevelComparison> gradeComparison = compareByGrade(startDate, endDate);
                reportData.put("gradeComparison", gradeComparison);
                report.setTitle("Grade Level Behavior Comparison Report");
            }
            default -> {
                reportData.put("message", "Unknown report type: " + reportType);
                report.setStatus("error");
            }
        }

        report.setData(reportData);
        log.info("Generated report: {}", report.getTitle());

        return report;
    }

    // ========================================================================
    // ADDITIONAL DTOs
    // ========================================================================

    public static class BehaviorTrendPoint {
        private String periodKey;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private int uniqueStudents;

        public String getPeriodKey() { return periodKey; }
        public void setPeriodKey(String periodKey) { this.periodKey = periodKey; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
    }

    public static class GradeLevelComparison {
        private String gradeLevel;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private int totalStudents;
        private double incidentsPerStudent;
        private int uniqueStudentsWithIncidents;

        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public double getIncidentsPerStudent() { return incidentsPerStudent; }
        public void setIncidentsPerStudent(double incidentsPerStudent) { this.incidentsPerStudent = incidentsPerStudent; }
        public int getUniqueStudentsWithIncidents() { return uniqueStudentsWithIncidents; }
        public void setUniqueStudentsWithIncidents(int uniqueStudentsWithIncidents) { this.uniqueStudentsWithIncidents = uniqueStudentsWithIncidents; }
    }

    public static class LocationComparison {
        private String location;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private int uniqueStudents;
        private String mostCommonIncidentType;
        private double percentageOfTotal;

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
        public String getMostCommonIncidentType() { return mostCommonIncidentType; }
        public void setMostCommonIncidentType(String mostCommonIncidentType) { this.mostCommonIncidentType = mostCommonIncidentType; }
        public double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
    }

    public static class IncidentTypeAnalysis {
        private String category;
        private String categoryDisplayName;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private Map<SeverityLevel, Long> severityBreakdown;
        private int adminReferralCount;
        private double adminReferralRate;
        private double percentageOfTotal;
        private int uniqueStudents;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getCategoryDisplayName() { return categoryDisplayName; }
        public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public Map<SeverityLevel, Long> getSeverityBreakdown() { return severityBreakdown; }
        public void setSeverityBreakdown(Map<SeverityLevel, Long> severityBreakdown) { this.severityBreakdown = severityBreakdown; }
        public int getAdminReferralCount() { return adminReferralCount; }
        public void setAdminReferralCount(int adminReferralCount) { this.adminReferralCount = adminReferralCount; }
        public double getAdminReferralRate() { return adminReferralRate; }
        public void setAdminReferralRate(double adminReferralRate) { this.adminReferralRate = adminReferralRate; }
        public double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
    }

    public static class DisciplineActionSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalNegativeIncidents;
        private Map<String, Long> actionCounts;
        private Map<SeverityLevel, Long> severityDistribution;
        private int adminReferrals;
        private int parentContactsMade;
        private double parentContactRate;
        private int uniqueStudents;

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public int getTotalNegativeIncidents() { return totalNegativeIncidents; }
        public void setTotalNegativeIncidents(int totalNegativeIncidents) { this.totalNegativeIncidents = totalNegativeIncidents; }
        public Map<String, Long> getActionCounts() { return actionCounts; }
        public void setActionCounts(Map<String, Long> actionCounts) { this.actionCounts = actionCounts; }
        public Map<SeverityLevel, Long> getSeverityDistribution() { return severityDistribution; }
        public void setSeverityDistribution(Map<SeverityLevel, Long> severityDistribution) { this.severityDistribution = severityDistribution; }
        public int getAdminReferrals() { return adminReferrals; }
        public void setAdminReferrals(int adminReferrals) { this.adminReferrals = adminReferrals; }
        public int getParentContactsMade() { return parentContactsMade; }
        public void setParentContactsMade(int parentContactsMade) { this.parentContactsMade = parentContactsMade; }
        public double getParentContactRate() { return parentContactRate; }
        public void setParentContactRate(double parentContactRate) { this.parentContactRate = parentContactRate; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
    }

    public static class InterventionEffectivenessData {
        private String interventionType;
        private int studentsAnalyzed;
        private int studentsImproved;
        private double improvementRate;
        private double averageIncidentsBefore;
        private double averageIncidentsAfter;
        private double overallReduction;
        private List<StudentInterventionResult> studentResults;
        private int daysBefore;
        private int daysAfter;
        private String message;

        public String getInterventionType() { return interventionType; }
        public void setInterventionType(String interventionType) { this.interventionType = interventionType; }
        public int getStudentsAnalyzed() { return studentsAnalyzed; }
        public void setStudentsAnalyzed(int studentsAnalyzed) { this.studentsAnalyzed = studentsAnalyzed; }
        public int getStudentsImproved() { return studentsImproved; }
        public void setStudentsImproved(int studentsImproved) { this.studentsImproved = studentsImproved; }
        public double getImprovementRate() { return improvementRate; }
        public void setImprovementRate(double improvementRate) { this.improvementRate = improvementRate; }
        public double getAverageIncidentsBefore() { return averageIncidentsBefore; }
        public void setAverageIncidentsBefore(double averageIncidentsBefore) { this.averageIncidentsBefore = averageIncidentsBefore; }
        public double getAverageIncidentsAfter() { return averageIncidentsAfter; }
        public void setAverageIncidentsAfter(double averageIncidentsAfter) { this.averageIncidentsAfter = averageIncidentsAfter; }
        public double getOverallReduction() { return overallReduction; }
        public void setOverallReduction(double overallReduction) { this.overallReduction = overallReduction; }
        public List<StudentInterventionResult> getStudentResults() { return studentResults; }
        public void setStudentResults(List<StudentInterventionResult> studentResults) { this.studentResults = studentResults; }
        public int getDaysBefore() { return daysBefore; }
        public void setDaysBefore(int daysBefore) { this.daysBefore = daysBefore; }
        public int getDaysAfter() { return daysAfter; }
        public void setDaysAfter(int daysAfter) { this.daysAfter = daysAfter; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class StudentInterventionResult {
        private Long studentId;
        private String studentName;
        private int incidentsBefore;
        private int incidentsAfter;
        private boolean improved;
        private double changePercent;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public int getIncidentsBefore() { return incidentsBefore; }
        public void setIncidentsBefore(int incidentsBefore) { this.incidentsBefore = incidentsBefore; }
        public int getIncidentsAfter() { return incidentsAfter; }
        public void setIncidentsAfter(int incidentsAfter) { this.incidentsAfter = incidentsAfter; }
        public boolean isImproved() { return improved; }
        public void setImproved(boolean improved) { this.improved = improved; }
        public double getChangePercent() { return changePercent; }
        public void setChangePercent(double changePercent) { this.changePercent = changePercent; }
    }

    public static class AtRiskStudentData {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private String gradeLevel;
        private int totalIncidents;
        private int majorIncidents;
        private int adminReferrals;
        private String mostCommonCategory;
        private int riskScore;
        private String riskLevel;
        private int daysAnalyzed;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getMajorIncidents() { return majorIncidents; }
        public void setMajorIncidents(int majorIncidents) { this.majorIncidents = majorIncidents; }
        public int getAdminReferrals() { return adminReferrals; }
        public void setAdminReferrals(int adminReferrals) { this.adminReferrals = adminReferrals; }
        public String getMostCommonCategory() { return mostCommonCategory; }
        public void setMostCommonCategory(String mostCommonCategory) { this.mostCommonCategory = mostCommonCategory; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public int getDaysAnalyzed() { return daysAnalyzed; }
        public void setDaysAnalyzed(int daysAnalyzed) { this.daysAnalyzed = daysAnalyzed; }
    }

    public static class RepeatOffenderData {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private String gradeLevel;
        private int totalIncidents;
        private Map<String, Long> categoryBreakdown;
        private String mostCommonCategory;
        private int adminReferrals;
        private LocalDate firstIncidentDate;
        private LocalDate lastIncidentDate;
        private double averageDaysBetweenIncidents;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
        public String getMostCommonCategory() { return mostCommonCategory; }
        public void setMostCommonCategory(String mostCommonCategory) { this.mostCommonCategory = mostCommonCategory; }
        public int getAdminReferrals() { return adminReferrals; }
        public void setAdminReferrals(int adminReferrals) { this.adminReferrals = adminReferrals; }
        public LocalDate getFirstIncidentDate() { return firstIncidentDate; }
        public void setFirstIncidentDate(LocalDate firstIncidentDate) { this.firstIncidentDate = firstIncidentDate; }
        public LocalDate getLastIncidentDate() { return lastIncidentDate; }
        public void setLastIncidentDate(LocalDate lastIncidentDate) { this.lastIncidentDate = lastIncidentDate; }
        public double getAverageDaysBetweenIncidents() { return averageDaysBetweenIncidents; }
        public void setAverageDaysBetweenIncidents(double averageDaysBetweenIncidents) { this.averageDaysBetweenIncidents = averageDaysBetweenIncidents; }
    }

    public static class GeneratedBehaviorReportData {
        private String reportType;
        private Long entityId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String format;
        private String title;
        private String status;
        private java.time.LocalDateTime generatedAt;
        private Map<String, Object> data;

        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(java.time.LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    // ========================================================================
    // TIME PATTERN ANALYSIS
    // ========================================================================

    /**
     * Analyze behavior incidents by time of day.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return time of day analysis
     */
    public List<TimeOfDayAnalysis> analyzeByTimeOfDay(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior by time of day from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        // Group by hour
        Map<Integer, List<BehaviorIncident>> byHour = allIncidents.stream()
                .filter(i -> i.getIncidentTime() != null)
                .collect(Collectors.groupingBy(i -> i.getIncidentTime().getHour()));

        List<TimeOfDayAnalysis> analyses = new ArrayList<>();

        for (int hour = 6; hour < 18; hour++) { // School hours 6 AM to 6 PM
            List<BehaviorIncident> hourIncidents = byHour.getOrDefault(hour, new ArrayList<>());

            long positive = hourIncidents.stream().filter(BehaviorIncident::isPositive).count();
            long negative = hourIncidents.stream().filter(BehaviorIncident::isNegative).count();

            TimeOfDayAnalysis analysis = new TimeOfDayAnalysis();
            analysis.setHour(hour);
            analysis.setTimeLabel(String.format("%02d:00 - %02d:59", hour, hour));
            analysis.setTotalIncidents(hourIncidents.size());
            analysis.setPositiveIncidents((int) positive);
            analysis.setNegativeIncidents((int) negative);
            analysis.setPercentageOfTotal(allIncidents.size() > 0 ?
                    (double) hourIncidents.size() / allIncidents.size() * 100 : 0);

            analyses.add(analysis);
        }

        log.info("Generated {} time-of-day analysis records", analyses.size());
        return analyses;
    }

    /**
     * Analyze behavior incidents by day of week.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return day of week analysis
     */
    public List<DayOfWeekAnalysis> analyzeByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior by day of week from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        // Group by day of week
        Map<java.time.DayOfWeek, List<BehaviorIncident>> byDayOfWeek = allIncidents.stream()
                .collect(Collectors.groupingBy(i -> i.getIncidentDate().getDayOfWeek()));

        List<DayOfWeekAnalysis> analyses = new ArrayList<>();

        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
                continue; // Skip weekends
            }

            List<BehaviorIncident> dayIncidents = byDayOfWeek.getOrDefault(day, new ArrayList<>());

            long positive = dayIncidents.stream().filter(BehaviorIncident::isPositive).count();
            long negative = dayIncidents.stream().filter(BehaviorIncident::isNegative).count();

            DayOfWeekAnalysis analysis = new DayOfWeekAnalysis();
            analysis.setDayOfWeek(day.name());
            analysis.setDayNumber(day.getValue());
            analysis.setTotalIncidents(dayIncidents.size());
            analysis.setPositiveIncidents((int) positive);
            analysis.setNegativeIncidents((int) negative);
            analysis.setPercentageOfTotal(allIncidents.size() > 0 ?
                    (double) dayIncidents.size() / allIncidents.size() * 100 : 0);

            analyses.add(analysis);
        }

        log.info("Generated {} day-of-week analysis records", analyses.size());
        return analyses;
    }

    // ========================================================================
    // TEACHER/CLASSROOM ANALYTICS
    // ========================================================================

    /**
     * Analyze behavior incidents by reporting teacher.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return teacher analysis data
     */
    public List<TeacherBehaviorAnalysis> analyzeByTeacher(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior by teacher from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        // Group by reporting teacher
        Map<Long, List<BehaviorIncident>> byTeacher = allIncidents.stream()
                .filter(i -> i.getReportingTeacher() != null)
                .collect(Collectors.groupingBy(i -> i.getReportingTeacher().getId()));

        List<TeacherBehaviorAnalysis> analyses = byTeacher.entrySet().stream()
                .map(entry -> {
                    List<BehaviorIncident> teacherIncidents = entry.getValue();
                    var teacher = teacherIncidents.get(0).getReportingTeacher();

                    long positive = teacherIncidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = teacherIncidents.stream().filter(BehaviorIncident::isNegative).count();

                    // Most common category reported
                    String mostCommonCategory = teacherIncidents.stream()
                            .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorCategory, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> e.getKey().getDisplayName())
                            .orElse("Unknown");

                    TeacherBehaviorAnalysis analysis = new TeacherBehaviorAnalysis();
                    analysis.setTeacherId(teacher.getId());
                    analysis.setTeacherName(teacher.getFullName());
                    analysis.setTotalIncidents(teacherIncidents.size());
                    analysis.setPositiveIncidents((int) positive);
                    analysis.setNegativeIncidents((int) negative);
                    analysis.setPositiveRatio(teacherIncidents.size() > 0 ?
                            (double) positive / teacherIncidents.size() * 100 : 0);
                    analysis.setUniqueStudents((int) teacherIncidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    analysis.setMostCommonCategory(mostCommonCategory);

                    return analysis;
                })
                .sorted(Comparator.comparing(TeacherBehaviorAnalysis::getTotalIncidents).reversed())
                .collect(Collectors.toList());

        log.info("Generated {} teacher analysis records", analyses.size());
        return analyses;
    }

    /**
     * Analyze behavior incidents by course.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return course analysis data
     */
    public List<CourseBehaviorAnalysis> analyzeByCourse(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior by course from {} to {}", startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        // Group by course
        Map<Long, List<BehaviorIncident>> byCourse = allIncidents.stream()
                .filter(i -> i.getCourse() != null)
                .collect(Collectors.groupingBy(i -> i.getCourse().getId()));

        List<CourseBehaviorAnalysis> analyses = byCourse.entrySet().stream()
                .map(entry -> {
                    List<BehaviorIncident> courseIncidents = entry.getValue();
                    var course = courseIncidents.get(0).getCourse();

                    long positive = courseIncidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = courseIncidents.stream().filter(BehaviorIncident::isNegative).count();

                    CourseBehaviorAnalysis analysis = new CourseBehaviorAnalysis();
                    analysis.setCourseId(course.getId());
                    analysis.setCourseName(course.getCourseName());
                    analysis.setCourseCode(course.getCourseCode());
                    analysis.setTotalIncidents(courseIncidents.size());
                    analysis.setPositiveIncidents((int) positive);
                    analysis.setNegativeIncidents((int) negative);
                    analysis.setUniqueStudents((int) courseIncidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());

                    return analysis;
                })
                .sorted(Comparator.comparing(CourseBehaviorAnalysis::getTotalIncidents).reversed())
                .collect(Collectors.toList());

        log.info("Generated {} course analysis records", analyses.size());
        return analyses;
    }

    // ========================================================================
    // PREDICTIVE RISK ANALYSIS
    // ========================================================================

    /**
     * Calculate predictive risk score for a student using multiple factors.
     *
     * @param student the student
     * @return predictive risk assessment
     */
    public PredictiveRiskAssessment calculatePredictiveRisk(Student student) {
        log.info("Calculating predictive risk for student ID {}", student.getId());

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate sixtyDaysAgo = today.minusDays(60);

        // Recent incidents (last 30 days)
        List<BehaviorIncident> recentIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, thirtyDaysAgo, today)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Previous period (30-60 days ago) for trend comparison
        List<BehaviorIncident> previousIncidents = behaviorIncidentService
                .getIncidentsByStudentAndDateRange(student, sixtyDaysAgo, thirtyDaysAgo.minusDays(1))
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Risk factors
        int recentCount = recentIncidents.size();
        int previousCount = previousIncidents.size();

        // Severity score
        int severityScore = recentIncidents.stream()
                .mapToInt(i -> {
                    if (i.getSeverityLevel() == null) return 1;
                    return switch (i.getSeverityLevel()) {
                        case MINOR -> 1;
                        case MODERATE -> 2;
                        case MAJOR -> 4;
                        case SEVERE -> 8;
                    };
                })
                .sum();

        // Admin referrals
        long adminReferrals = recentIncidents.stream()
                .filter(BehaviorIncident::getAdminReferralRequired)
                .count();

        // Trend (increasing, decreasing, stable)
        String trend;
        double trendPercentage = 0;
        if (previousCount == 0 && recentCount == 0) {
            trend = "NONE";
        } else if (previousCount == 0) {
            trend = "NEW_ISSUE";
            trendPercentage = 100;
        } else {
            trendPercentage = ((double) (recentCount - previousCount) / previousCount) * 100;
            if (trendPercentage > 20) {
                trend = "INCREASING";
            } else if (trendPercentage < -20) {
                trend = "DECREASING";
            } else {
                trend = "STABLE";
            }
        }

        // Pattern detection - check for clustered incidents
        boolean hasClusteredIncidents = false;
        if (recentIncidents.size() >= 3) {
            List<LocalDate> dates = recentIncidents.stream()
                    .map(BehaviorIncident::getIncidentDate)
                    .sorted()
                    .collect(Collectors.toList());

            for (int i = 0; i < dates.size() - 2; i++) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(dates.get(i), dates.get(i + 2));
                if (daysBetween <= 7) {
                    hasClusteredIncidents = true;
                    break;
                }
            }
        }

        // Calculate composite risk score (0-100)
        int riskScore = 0;
        riskScore += Math.min(40, recentCount * 10); // Up to 40 points for recent incidents
        riskScore += Math.min(20, severityScore * 2); // Up to 20 points for severity
        riskScore += Math.min(20, adminReferrals * 10); // Up to 20 points for admin referrals
        riskScore += trend.equals("INCREASING") ? 10 : 0; // 10 points for increasing trend
        riskScore += hasClusteredIncidents ? 10 : 0; // 10 points for clustered incidents

        String riskLevel;
        if (riskScore >= 70) riskLevel = "CRITICAL";
        else if (riskScore >= 50) riskLevel = "HIGH";
        else if (riskScore >= 30) riskLevel = "MODERATE";
        else if (riskScore >= 10) riskLevel = "LOW";
        else riskLevel = "MINIMAL";

        // Recommendations
        List<String> recommendations = new ArrayList<>();
        if (riskScore >= 70) {
            recommendations.add("Immediate intervention required");
            recommendations.add("Schedule parent conference");
            recommendations.add("Consider counseling referral");
        } else if (riskScore >= 50) {
            recommendations.add("Increase monitoring frequency");
            recommendations.add("Implement behavior intervention plan");
        } else if (riskScore >= 30) {
            recommendations.add("Monitor for escalation");
            recommendations.add("Consider positive reinforcement strategies");
        }

        PredictiveRiskAssessment assessment = new PredictiveRiskAssessment();
        assessment.setStudentId(student.getId());
        assessment.setStudentName(student.getFullName());
        assessment.setRiskScore(riskScore);
        assessment.setRiskLevel(riskLevel);
        assessment.setRecentIncidents(recentCount);
        assessment.setPreviousPeriodIncidents(previousCount);
        assessment.setTrend(trend);
        assessment.setTrendPercentage(Math.round(trendPercentage * 100.0) / 100.0);
        assessment.setSeverityScore(severityScore);
        assessment.setAdminReferrals((int) adminReferrals);
        assessment.setHasClusteredIncidents(hasClusteredIncidents);
        assessment.setRecommendations(recommendations);
        assessment.setAssessmentDate(today);

        log.info("Calculated risk for student {}: {} ({})", student.getId(), riskScore, riskLevel);
        return assessment;
    }

    // ========================================================================
    // COHORT ANALYSIS
    // ========================================================================

    /**
     * Analyze behavior by student demographics/cohorts.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param cohortType type of cohort analysis (grade, gender, program, etc.)
     * @return cohort analysis data
     */
    public List<CohortAnalysis> analyzeByCohort(LocalDate startDate, LocalDate endDate, String cohortType) {
        log.info("Analyzing behavior by cohort ({}) from {} to {}", cohortType, startDate, endDate);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate);

        Map<String, List<BehaviorIncident>> byCohort;

        switch (cohortType.toLowerCase()) {
            case "grade" -> byCohort = allIncidents.stream()
                    .filter(i -> i.getStudent().getGradeLevel() != null)
                    .collect(Collectors.groupingBy(i -> i.getStudent().getGradeLevel()));

            case "gender" -> byCohort = allIncidents.stream()
                    .filter(i -> i.getStudent().getGender() != null)
                    .collect(Collectors.groupingBy(i -> i.getStudent().getGender()));

            case "program" -> byCohort = allIncidents.stream()
                    .collect(Collectors.groupingBy(i -> {
                        // Check for special programs
                        Student s = i.getStudent();
                        if (s.getSpecialEducation() != null && s.getSpecialEducation()) {
                            return "Special Education";
                        } else if (s.getGiftedTalented() != null && s.getGiftedTalented()) {
                            return "Gifted/Talented";
                        } else if (s.getEll() != null && s.getEll()) {
                            return "English Language Learner";
                        }
                        return "General Education";
                    }));

            default -> {
                log.warn("Unknown cohort type: {}", cohortType);
                return new ArrayList<>();
            }
        }

        // Get total students per cohort for rate calculation
        Map<String, Long> studentsPerCohort = studentRepository.findByActiveTrue().stream()
                .collect(Collectors.groupingBy(s -> {
                    return switch (cohortType.toLowerCase()) {
                        case "grade" -> s.getGradeLevel() != null ? s.getGradeLevel() : "Unknown";
                        case "gender" -> s.getGender() != null ? s.getGender() : "Unknown";
                        case "program" -> {
                            if (s.getSpecialEducation() != null && s.getSpecialEducation()) {
                                yield "Special Education";
                            } else if (s.getGiftedTalented() != null && s.getGiftedTalented()) {
                                yield "Gifted/Talented";
                            } else if (s.getEll() != null && s.getEll()) {
                                yield "English Language Learner";
                            }
                            yield "General Education";
                        }
                        default -> "Unknown";
                    };
                }, Collectors.counting()));

        List<CohortAnalysis> analyses = byCohort.entrySet().stream()
                .map(entry -> {
                    String cohort = entry.getKey();
                    List<BehaviorIncident> cohortIncidents = entry.getValue();

                    long positive = cohortIncidents.stream().filter(BehaviorIncident::isPositive).count();
                    long negative = cohortIncidents.stream().filter(BehaviorIncident::isNegative).count();
                    long totalStudents = studentsPerCohort.getOrDefault(cohort, 1L);

                    CohortAnalysis analysis = new CohortAnalysis();
                    analysis.setCohortType(cohortType);
                    analysis.setCohortValue(cohort);
                    analysis.setTotalIncidents(cohortIncidents.size());
                    analysis.setPositiveIncidents((int) positive);
                    analysis.setNegativeIncidents((int) negative);
                    analysis.setTotalStudentsInCohort((int) (long) totalStudents);
                    analysis.setIncidentsPerStudent(totalStudents > 0 ?
                            (double) cohortIncidents.size() / totalStudents : 0);
                    analysis.setUniqueStudentsWithIncidents((int) cohortIncidents.stream()
                            .map(i -> i.getStudent().getId())
                            .distinct()
                            .count());
                    analysis.setStudentParticipationRate(totalStudents > 0 ?
                            (double) analysis.getUniqueStudentsWithIncidents() / totalStudents * 100 : 0);

                    return analysis;
                })
                .sorted(Comparator.comparing(CohortAnalysis::getCohortValue))
                .collect(Collectors.toList());

        log.info("Generated {} cohort analysis records", analyses.size());
        return analyses;
    }

    // ========================================================================
    // BEHAVIOR CORRELATION ANALYSIS
    // ========================================================================

    /**
     * Analyze correlation between behavior and academic performance.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @return behavior-academic correlation data
     */
    public BehaviorAcademicCorrelation analyzeAcademicCorrelation(LocalDate startDate, LocalDate endDate) {
        log.info("Analyzing behavior-academic correlation from {} to {}", startDate, endDate);

        List<BehaviorIncident> negativeIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by student
        Map<Long, Long> incidentCountByStudent = negativeIncidents.stream()
                .collect(Collectors.groupingBy(i -> i.getStudent().getId(), Collectors.counting()));

        // Categorize students by GPA ranges
        List<Student> allStudents = studentRepository.findByActiveTrue();

        Map<String, List<Double>> incidentsByGpaRange = new HashMap<>();
        incidentsByGpaRange.put("0.0-1.0", new ArrayList<>());
        incidentsByGpaRange.put("1.0-2.0", new ArrayList<>());
        incidentsByGpaRange.put("2.0-3.0", new ArrayList<>());
        incidentsByGpaRange.put("3.0-4.0", new ArrayList<>());

        for (Student student : allStudents) {
            Double gpa = student.getGpa();
            if (gpa == null) continue;

            Long incidents = incidentCountByStudent.getOrDefault(student.getId(), 0L);

            String range;
            if (gpa < 1.0) range = "0.0-1.0";
            else if (gpa < 2.0) range = "1.0-2.0";
            else if (gpa < 3.0) range = "2.0-3.0";
            else range = "3.0-4.0";

            incidentsByGpaRange.get(range).add((double) incidents);
        }

        // Calculate averages
        Map<String, Double> avgIncidentsByGpa = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : incidentsByGpaRange.entrySet()) {
            List<Double> counts = entry.getValue();
            double avg = counts.isEmpty() ? 0 : counts.stream().mapToDouble(d -> d).average().orElse(0);
            avgIncidentsByGpa.put(entry.getKey(), Math.round(avg * 100.0) / 100.0);
        }

        BehaviorAcademicCorrelation correlation = new BehaviorAcademicCorrelation();
        correlation.setPeriodStart(startDate);
        correlation.setPeriodEnd(endDate);
        correlation.setAverageIncidentsByGpaRange(avgIncidentsByGpa);
        correlation.setStudentsAnalyzed(allStudents.size());
        correlation.setStudentsWithIncidents(incidentCountByStudent.size());

        // Calculate correlation coefficient (simplified Pearson correlation)
        List<Double> gpaValues = new ArrayList<>();
        List<Double> incidentValues = new ArrayList<>();

        for (Student student : allStudents) {
            if (student.getGpa() != null) {
                gpaValues.add(student.getGpa());
                incidentValues.add((double) incidentCountByStudent.getOrDefault(student.getId(), 0L));
            }
        }

        if (gpaValues.size() > 2) {
            double correlationCoeff = calculateCorrelation(gpaValues, incidentValues);
            correlation.setCorrelationCoefficient(Math.round(correlationCoeff * 1000.0) / 1000.0);

            String interpretation;
            if (correlationCoeff < -0.5) interpretation = "Strong negative correlation (higher GPA = fewer incidents)";
            else if (correlationCoeff < -0.3) interpretation = "Moderate negative correlation";
            else if (correlationCoeff < -0.1) interpretation = "Weak negative correlation";
            else if (correlationCoeff < 0.1) interpretation = "No significant correlation";
            else if (correlationCoeff < 0.3) interpretation = "Weak positive correlation";
            else if (correlationCoeff < 0.5) interpretation = "Moderate positive correlation";
            else interpretation = "Strong positive correlation";

            correlation.setInterpretation(interpretation);
        } else {
            correlation.setCorrelationCoefficient(0.0);
            correlation.setInterpretation("Insufficient data for correlation analysis");
        }

        log.info("Calculated behavior-academic correlation: {}", correlation.getCorrelationCoefficient());
        return correlation;
    }

    /**
     * Calculate Pearson correlation coefficient.
     */
    private double calculateCorrelation(List<Double> x, List<Double> y) {
        int n = x.size();
        if (n != y.size() || n < 2) return 0;

        double sumX = x.stream().mapToDouble(d -> d).sum();
        double sumY = y.stream().mapToDouble(d -> d).sum();
        double sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return denominator == 0 ? 0 : numerator / denominator;
    }

    // ========================================================================
    // ADDITIONAL DTOs FOR NEW ANALYTICS
    // ========================================================================

    public static class TimeOfDayAnalysis {
        private int hour;
        private String timeLabel;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private double percentageOfTotal;

        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        public String getTimeLabel() { return timeLabel; }
        public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
    }

    public static class DayOfWeekAnalysis {
        private String dayOfWeek;
        private int dayNumber;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private double percentageOfTotal;

        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public int getDayNumber() { return dayNumber; }
        public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
    }

    public static class TeacherBehaviorAnalysis {
        private Long teacherId;
        private String teacherName;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private double positiveRatio;
        private int uniqueStudents;
        private String mostCommonCategory;

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public double getPositiveRatio() { return positiveRatio; }
        public void setPositiveRatio(double positiveRatio) { this.positiveRatio = positiveRatio; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
        public String getMostCommonCategory() { return mostCommonCategory; }
        public void setMostCommonCategory(String mostCommonCategory) { this.mostCommonCategory = mostCommonCategory; }
    }

    public static class CourseBehaviorAnalysis {
        private Long courseId;
        private String courseName;
        private String courseCode;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private int uniqueStudents;

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int uniqueStudents) { this.uniqueStudents = uniqueStudents; }
    }

    public static class PredictiveRiskAssessment {
        private Long studentId;
        private String studentName;
        private int riskScore;
        private String riskLevel;
        private int recentIncidents;
        private int previousPeriodIncidents;
        private String trend;
        private double trendPercentage;
        private int severityScore;
        private int adminReferrals;
        private boolean hasClusteredIncidents;
        private List<String> recommendations;
        private LocalDate assessmentDate;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public int getRecentIncidents() { return recentIncidents; }
        public void setRecentIncidents(int recentIncidents) { this.recentIncidents = recentIncidents; }
        public int getPreviousPeriodIncidents() { return previousPeriodIncidents; }
        public void setPreviousPeriodIncidents(int previousPeriodIncidents) { this.previousPeriodIncidents = previousPeriodIncidents; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
        public double getTrendPercentage() { return trendPercentage; }
        public void setTrendPercentage(double trendPercentage) { this.trendPercentage = trendPercentage; }
        public int getSeverityScore() { return severityScore; }
        public void setSeverityScore(int severityScore) { this.severityScore = severityScore; }
        public int getAdminReferrals() { return adminReferrals; }
        public void setAdminReferrals(int adminReferrals) { this.adminReferrals = adminReferrals; }
        public boolean isHasClusteredIncidents() { return hasClusteredIncidents; }
        public void setHasClusteredIncidents(boolean hasClusteredIncidents) { this.hasClusteredIncidents = hasClusteredIncidents; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        public LocalDate getAssessmentDate() { return assessmentDate; }
        public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    }

    public static class CohortAnalysis {
        private String cohortType;
        private String cohortValue;
        private int totalIncidents;
        private int positiveIncidents;
        private int negativeIncidents;
        private int totalStudentsInCohort;
        private double incidentsPerStudent;
        private int uniqueStudentsWithIncidents;
        private double studentParticipationRate;

        public String getCohortType() { return cohortType; }
        public void setCohortType(String cohortType) { this.cohortType = cohortType; }
        public String getCohortValue() { return cohortValue; }
        public void setCohortValue(String cohortValue) { this.cohortValue = cohortValue; }
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public void setPositiveIncidents(int positiveIncidents) { this.positiveIncidents = positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public void setNegativeIncidents(int negativeIncidents) { this.negativeIncidents = negativeIncidents; }
        public int getTotalStudentsInCohort() { return totalStudentsInCohort; }
        public void setTotalStudentsInCohort(int totalStudentsInCohort) { this.totalStudentsInCohort = totalStudentsInCohort; }
        public double getIncidentsPerStudent() { return incidentsPerStudent; }
        public void setIncidentsPerStudent(double incidentsPerStudent) { this.incidentsPerStudent = incidentsPerStudent; }
        public int getUniqueStudentsWithIncidents() { return uniqueStudentsWithIncidents; }
        public void setUniqueStudentsWithIncidents(int uniqueStudentsWithIncidents) { this.uniqueStudentsWithIncidents = uniqueStudentsWithIncidents; }
        public double getStudentParticipationRate() { return studentParticipationRate; }
        public void setStudentParticipationRate(double studentParticipationRate) { this.studentParticipationRate = studentParticipationRate; }
    }

    public static class BehaviorAcademicCorrelation {
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Map<String, Double> averageIncidentsByGpaRange;
        private int studentsAnalyzed;
        private int studentsWithIncidents;
        private double correlationCoefficient;
        private String interpretation;

        public LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
        public Map<String, Double> getAverageIncidentsByGpaRange() { return averageIncidentsByGpaRange; }
        public void setAverageIncidentsByGpaRange(Map<String, Double> averageIncidentsByGpaRange) { this.averageIncidentsByGpaRange = averageIncidentsByGpaRange; }
        public int getStudentsAnalyzed() { return studentsAnalyzed; }
        public void setStudentsAnalyzed(int studentsAnalyzed) { this.studentsAnalyzed = studentsAnalyzed; }
        public int getStudentsWithIncidents() { return studentsWithIncidents; }
        public void setStudentsWithIncidents(int studentsWithIncidents) { this.studentsWithIncidents = studentsWithIncidents; }
        public double getCorrelationCoefficient() { return correlationCoefficient; }
        public void setCorrelationCoefficient(double correlationCoefficient) { this.correlationCoefficient = correlationCoefficient; }
        public String getInterpretation() { return interpretation; }
        public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
    }
}
