package com.heronix.service;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import com.heronix.repository.BehaviorIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating real-time behavior dashboards and metrics.
 *
 * Provides quick-access statistics and visualizations for:
 * - School administrators
 * - Teachers
 * - Counselors
 * - Parents
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Discipline/Behavior Management System
 */
@Service
public class BehaviorDashboardService {

    private static final Logger log = LoggerFactory.getLogger(BehaviorDashboardService.class);

    @Autowired
    private BehaviorIncidentRepository behaviorIncidentRepository;

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    @Autowired
    private BehaviorReportingService behaviorReportingService;

    @Autowired
    private DisciplineManagementService disciplineManagementService;

    // ========================================================================
    // DASHBOARD SUMMARY CARDS
    // ========================================================================

    /**
     * Generates comprehensive dashboard summary for a student.
     *
     * @param student the student
     * @return dashboard summary
     */
    public StudentDashboard generateStudentDashboard(Student student) {
        log.info("Generating dashboard for student ID {}", student.getId());

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();

        StudentDashboard dashboard = new StudentDashboard();
        dashboard.student = student;
        dashboard.generatedDate = today;

        // Current month stats
        dashboard.positiveIncidentsThisMonth = behaviorIncidentService
                .countPositiveIncidents(student, thirtyDaysAgo, today).intValue();

        dashboard.negativeIncidentsThisMonth = behaviorIncidentService
                .countNegativeIncidents(student, thirtyDaysAgo, today).intValue();

        dashboard.behaviorRatio = behaviorIncidentService
                .calculateBehaviorRatio(student, thirtyDaysAgo, today);

        // Pending actions
        dashboard.pendingReferrals = disciplineManagementService
                .getPendingReferralsForStudent(student).size();

        dashboard.uncontactedParentIncidents = behaviorIncidentService
                .getUncontactedParentIncidents(student).size();

        // Risk assessment
        dashboard.atRiskForSuspension = disciplineManagementService
                .isAtRiskForSuspension(student);

        dashboard.hasCriticalIncidents = behaviorIncidentService
                .hasRecentCriticalIncidents(student, 7);

        // Trend data
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        dashboard.threeMonthTrend = behaviorReportingService
                .generateMonthlyTrend(student, ninetyDaysAgo, today);

        // Most common category
        dashboard.mostCommonCategory = behaviorReportingService
                .getMostCommonCategory(student, thirtyDaysAgo, today);

        log.info("Dashboard generated for student ID {}: {} positive, {} negative incidents",
                student.getId(), dashboard.positiveIncidentsThisMonth, dashboard.negativeIncidentsThisMonth);

        return dashboard;
    }

    /**
     * Generates school-wide administrative dashboard.
     *
     * @return school dashboard
     */
    public SchoolDashboard generateSchoolDashboard() {
        log.info("Generating school-wide behavior dashboard");

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();

        SchoolDashboard dashboard = new SchoolDashboard();
        dashboard.generatedDate = today;

        // Current month stats
        BehaviorReportingService.SchoolBehaviorReport report =
                behaviorReportingService.generateSchoolBehaviorReport(thirtyDaysAgo, today);

        dashboard.totalIncidentsThisMonth = report.getTotalIncidents();
        dashboard.positiveIncidentsThisMonth = (int) report.getPositiveIncidents();
        dashboard.negativeIncidentsThisMonth = (int) report.getNegativeIncidents();
        dashboard.studentsWithIncidents = report.getStudentsWithIncidents();

        // Pending actions
        dashboard.pendingReferrals = disciplineManagementService.getPendingReferrals().size();
        dashboard.uncontactedParents = (int) report.getUncontactedParentsCount();

        // Top concerns
        dashboard.topNegativeStudents = behaviorReportingService
                .getStudentsRequiringIntervention(thirtyDaysAgo, today, 10);

        dashboard.topPositiveStudents = behaviorReportingService
                .getTopPositiveBehaviorStudents(thirtyDaysAgo, today, 10);

        // Category breakdown
        dashboard.categoryBreakdown = report.getCategoryBreakdown();
        dashboard.severityBreakdown = report.getSeverityBreakdown();

        log.info("School dashboard generated: {} total incidents, {} pending referrals",
                dashboard.totalIncidentsThisMonth, dashboard.pendingReferrals);

        return dashboard;
    }

    // ========================================================================
    // ALERT GENERATION
    // ========================================================================

    /**
     * Generates behavior alerts for a student.
     *
     * @param student the student
     * @return list of alerts
     */
    public List<BehaviorAlert> generateStudentAlerts(Student student) {
        log.info("Generating behavior alerts for student ID {}", student.getId());

        List<BehaviorAlert> alerts = new ArrayList<>();

        // Check for suspension risk
        if (disciplineManagementService.isAtRiskForSuspension(student)) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.CRITICAL,
                    "Suspension Risk",
                    "Student is at risk for suspension based on recent behavior incidents."
            ));
        }

        // Check for pending referrals
        int pendingReferrals = disciplineManagementService.getPendingReferralsForStudent(student).size();
        if (pendingReferrals > 0) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.WARNING,
                    "Pending Referrals",
                    String.format("%d administrative referral(s) require processing.", pendingReferrals)
            ));
        }

        // Check for uncontacted parents
        int uncontacted = behaviorIncidentService.getUncontactedParentIncidents(student).size();
        if (uncontacted > 0) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.INFO,
                    "Parent Contact Needed",
                    String.format("%d incident(s) require parent contact.", uncontacted)
            ));
        }

        // Check for behavior patterns
        DisciplineManagementService.BehaviorPattern pattern =
                disciplineManagementService.detectBehaviorPattern(student);

        if (pattern.isPatternDetected()) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.WARNING,
                    "Behavior Pattern Detected",
                    pattern.getPatternDescription()
            ));
        }

        // Check for critical incidents
        if (behaviorIncidentService.hasRecentCriticalIncidents(student, 7)) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.CRITICAL,
                    "Critical Incident",
                    "Student has had critical (major severity) incidents in the past 7 days."
            ));
        }

        log.info("Generated {} alert(s) for student ID {}", alerts.size(), student.getId());

        return alerts;
    }

    /**
     * Generates school-wide behavior alerts.
     *
     * @return list of school alerts
     */
    public List<BehaviorAlert> generateSchoolAlerts() {
        log.info("Generating school-wide behavior alerts");

        List<BehaviorAlert> alerts = new ArrayList<>();

        // Pending referrals
        int pendingReferrals = disciplineManagementService.getPendingReferrals().size();
        if (pendingReferrals > 10) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.WARNING,
                    "High Referral Volume",
                    String.format("%d pending administrative referrals require attention.", pendingReferrals)
            ));
        }

        // Uncontacted parents
        List<Long> studentsWithUncontacted = behaviorIncidentService.getStudentsWithUncontactedIncidents();
        if (studentsWithUncontacted.size() > 20) {
            alerts.add(new BehaviorAlert(
                    AlertLevel.INFO,
                    "Parent Contact Backlog",
                    String.format("%d students have incidents requiring parent contact.", studentsWithUncontacted.size())
            ));
        }

        log.info("Generated {} school-wide alert(s)", alerts.size());

        return alerts;
    }

    // ========================================================================
    // QUICK STATS
    // ========================================================================

    /**
     * Gets quick statistics for today.
     *
     * @return today's stats
     */
    public TodayStats getTodayStats() {
        LocalDate today = LocalDate.now();

        List<BehaviorIncident> todayIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(today, today);

        TodayStats stats = new TodayStats();
        stats.date = today;
        stats.totalIncidents = todayIncidents.size();
        stats.positiveIncidents = (int) todayIncidents.stream()
                .filter(BehaviorIncident::isPositive).count();
        stats.negativeIncidents = (int) todayIncidents.stream()
                .filter(BehaviorIncident::isNegative).count();
        stats.majorIncidents = (int) todayIncidents.stream()
                .filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR).count();

        return stats;
    }

    /**
     * Gets quick statistics for this week.
     *
     * @return this week's stats
     */
    public WeekStats getThisWeekStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday

        List<BehaviorIncident> weekIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(weekStart, today);

        WeekStats stats = new WeekStats();
        stats.weekStartDate = weekStart;
        stats.weekEndDate = today;
        stats.totalIncidents = weekIncidents.size();
        stats.positiveIncidents = (int) weekIncidents.stream()
                .filter(BehaviorIncident::isPositive).count();
        stats.negativeIncidents = (int) weekIncidents.stream()
                .filter(BehaviorIncident::isNegative).count();

        // Daily breakdown
        stats.dailyBreakdown = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (day.isAfter(today)) break;

            long dayCount = weekIncidents.stream()
                    .filter(inc -> inc.getIncidentDate().equals(day))
                    .count();

            stats.dailyBreakdown.put(day, (int) dayCount);
        }

        return stats;
    }

    // ========================================================================
    // ADDITIONAL DASHBOARD METHODS
    // ========================================================================

    /**
     * Gets dashboard statistics for a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return dashboard statistics
     */
    public Map<String, Object> getDashboardStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting dashboard statistics from {} to {}", startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIncidents", incidents.size());
        stats.put("positiveIncidents", incidents.stream().filter(BehaviorIncident::isPositive).count());
        stats.put("negativeIncidents", incidents.stream().filter(BehaviorIncident::isNegative).count());
        stats.put("majorIncidents", incidents.stream().filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR).count());
        stats.put("studentsAffected", incidents.stream().map(i -> i.getStudent().getId()).distinct().count());

        return stats;
    }

    /**
     * Gets incidents grouped by behavior type.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return map of behavior types to counts
     */
    public Map<BehaviorType, Long> getIncidentsByBehaviorType(LocalDate startDate, LocalDate endDate) {
        log.info("Getting incidents by behavior type from {} to {}", startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);

        return incidents.stream()
            .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorType, Collectors.counting()));
    }

    /**
     * Gets incidents grouped by severity level.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return map of severity levels to counts
     */
    public Map<SeverityLevel, Long> getIncidentsBySeverity(LocalDate startDate, LocalDate endDate) {
        log.info("Getting incidents by severity from {} to {}", startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);

        return incidents.stream()
            .collect(Collectors.groupingBy(BehaviorIncident::getSeverityLevel, Collectors.counting()));
    }

    /**
     * Gets incident trends over time.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return map of dates to incident counts
     */
    public Map<LocalDate, Long> getIncidentTrends(LocalDate startDate, LocalDate endDate) {
        log.info("Getting incident trends from {} to {}", startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);

        return incidents.stream()
            .collect(Collectors.groupingBy(BehaviorIncident::getIncidentDate, Collectors.counting()));
    }

    /**
     * Gets top behavior types by frequency.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param limit the maximum number of results
     * @return list of behavior categories with counts
     */
    public List<Map.Entry<BehaviorCategory, Long>> getTopBehaviorTypes(LocalDate startDate, LocalDate endDate, int limit) {
        log.info("Getting top {} behavior types from {} to {}", limit, startDate, endDate);

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);

        return incidents.stream()
            .collect(Collectors.groupingBy(BehaviorIncident::getBehaviorCategory, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<BehaviorCategory, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Gets students at risk based on recent incidents.
     *
     * @param limit the maximum number of results
     * @return list of at-risk students with incident counts
     */
    public List<Map<String, Object>> getAtRiskStudents(int limit) {
        log.info("Getting top {} at-risk students", limit);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();

        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(thirtyDaysAgo, today);

        Map<Student, Long> studentIncidentCounts = incidents.stream()
            .filter(BehaviorIncident::isNegative)
            .collect(Collectors.groupingBy(BehaviorIncident::getStudent, Collectors.counting()));

        return studentIncidentCounts.entrySet()
            .stream()
            .sorted(Map.Entry.<Student, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("student", entry.getKey());
                studentData.put("incidentCount", entry.getValue());
                return studentData;
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets pending actions requiring attention.
     *
     * @return list of pending action items
     */
    public List<Map<String, Object>> getPendingActions() {
        log.info("Getting pending actions");

        List<Map<String, Object>> actions = new ArrayList<>();

        // Pending referrals
        int pendingReferrals = disciplineManagementService.getPendingReferrals().size();
        if (pendingReferrals > 0) {
            Map<String, Object> action = new HashMap<>();
            action.put("type", "PENDING_REFERRALS");
            action.put("count", pendingReferrals);
            action.put("message", pendingReferrals + " pending administrative referrals");
            actions.add(action);
        }

        // Uncontacted parents
        List<Long> uncontactedStudents = behaviorIncidentService.getStudentsWithUncontactedIncidents();
        if (!uncontactedStudents.isEmpty()) {
            Map<String, Object> action = new HashMap<>();
            action.put("type", "UNCONTACTED_PARENTS");
            action.put("count", uncontactedStudents.size());
            action.put("message", uncontactedStudents.size() + " students need parent contact");
            actions.add(action);
        }

        return actions;
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // ========================================================================

    /**
     * Student dashboard DTO.
     */
    public static class StudentDashboard {
        public Student student;
        public LocalDate generatedDate;
        public int positiveIncidentsThisMonth;
        public int negativeIncidentsThisMonth;
        public double behaviorRatio;
        public int pendingReferrals;
        public int uncontactedParentIncidents;
        public boolean atRiskForSuspension;
        public boolean hasCriticalIncidents;
        public BehaviorCategory mostCommonCategory;
        public List<BehaviorReportingService.MonthlyBehaviorTrend> threeMonthTrend;

        public Student getStudent() { return student; }
        public LocalDate getGeneratedDate() { return generatedDate; }
        public int getPositiveIncidentsThisMonth() { return positiveIncidentsThisMonth; }
        public int getNegativeIncidentsThisMonth() { return negativeIncidentsThisMonth; }
        public double getBehaviorRatio() { return behaviorRatio; }
        public int getPendingReferrals() { return pendingReferrals; }
        public int getUncontactedParentIncidents() { return uncontactedParentIncidents; }
        public boolean isAtRiskForSuspension() { return atRiskForSuspension; }
        public boolean hasCriticalIncidents() { return hasCriticalIncidents; }
        public BehaviorCategory getMostCommonCategory() { return mostCommonCategory; }
        public List<BehaviorReportingService.MonthlyBehaviorTrend> getThreeMonthTrend() { return threeMonthTrend; }
    }

    /**
     * School dashboard DTO.
     */
    public static class SchoolDashboard {
        public LocalDate generatedDate;
        public int totalIncidentsThisMonth;
        public int positiveIncidentsThisMonth;
        public int negativeIncidentsThisMonth;
        public int studentsWithIncidents;
        public int pendingReferrals;
        public int uncontactedParents;
        public List<BehaviorReportingService.StudentIncidentCount> topNegativeStudents;
        public List<BehaviorReportingService.StudentIncidentCount> topPositiveStudents;
        public Map<BehaviorCategory, Long> categoryBreakdown;
        public Map<SeverityLevel, Long> severityBreakdown;

        public LocalDate getGeneratedDate() { return generatedDate; }
        public int getTotalIncidentsThisMonth() { return totalIncidentsThisMonth; }
        public int getPositiveIncidentsThisMonth() { return positiveIncidentsThisMonth; }
        public int getNegativeIncidentsThisMonth() { return negativeIncidentsThisMonth; }
        public int getStudentsWithIncidents() { return studentsWithIncidents; }
        public int getPendingReferrals() { return pendingReferrals; }
        public int getUncontactedParents() { return uncontactedParents; }
        public List<BehaviorReportingService.StudentIncidentCount> getTopNegativeStudents() { return topNegativeStudents; }
        public List<BehaviorReportingService.StudentIncidentCount> getTopPositiveStudents() { return topPositiveStudents; }
        public Map<BehaviorCategory, Long> getCategoryBreakdown() { return categoryBreakdown; }
        public Map<SeverityLevel, Long> getSeverityBreakdown() { return severityBreakdown; }
    }

    /**
     * Behavior alert DTO.
     */
    public static class BehaviorAlert {
        private final AlertLevel level;
        private final String title;
        private final String message;

        public BehaviorAlert(AlertLevel level, String title, String message) {
            this.level = level;
            this.title = title;
            this.message = message;
        }

        public AlertLevel getLevel() { return level; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
    }

    /**
     * Alert severity levels.
     */
    public enum AlertLevel {
        INFO,
        WARNING,
        CRITICAL
    }

    /**
     * Today's stats DTO.
     */
    public static class TodayStats {
        public LocalDate date;
        public int totalIncidents;
        public int positiveIncidents;
        public int negativeIncidents;
        public int majorIncidents;

        public LocalDate getDate() { return date; }
        public int getTotalIncidents() { return totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public int getMajorIncidents() { return majorIncidents; }
    }

    /**
     * Week stats DTO.
     */
    public static class WeekStats {
        public LocalDate weekStartDate;
        public LocalDate weekEndDate;
        public int totalIncidents;
        public int positiveIncidents;
        public int negativeIncidents;
        public Map<LocalDate, Integer> dailyBreakdown;

        public LocalDate getWeekStartDate() { return weekStartDate; }
        public LocalDate getWeekEndDate() { return weekEndDate; }
        public int getTotalIncidents() { return totalIncidents; }
        public int getPositiveIncidents() { return positiveIncidents; }
        public int getNegativeIncidents() { return negativeIncidents; }
        public Map<LocalDate, Integer> getDailyBreakdown() { return dailyBreakdown; }
    }
}
