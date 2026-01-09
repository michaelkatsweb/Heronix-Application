package com.heronix.service;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.Course;
import com.heronix.model.domain.CourseEnrollmentRequest;
import com.heronix.model.domain.Student;
import com.heronix.model.enums.EnrollmentRequestStatus;
import com.heronix.repository.AssignmentGradeRepository;
import com.heronix.repository.AssignmentRepository;
import com.heronix.repository.CourseEnrollmentRequestRepository;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Assignment Report Service
 *
 * Provides analytics, reporting, and data aggregation for course assignments.
 * Generates various reports for administrators to analyze assignment results,
 * course demand, student preferences, and system performance.
 *
 * Reports Available:
 * - Course Demand Analysis
 * - Student Preference Analysis
 * - Success Rate Trends
 * - Waitlist Analysis
 * - Priority Rule Effectiveness
 * - Capacity Utilization
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 6 - November 20, 2025
 */
@Slf4j
@Service
public class AssignmentReportService {

    @Autowired
    private CourseEnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentGradeRepository assignmentGradeRepository;

    // ========================================================================
    // COURSE DEMAND ANALYSIS
    // ========================================================================

    /**
     * Get course demand summary for all courses
     */
    public List<CourseDemandReport> getCourseDemandAnalysis() {
        List<Course> allCourses = courseRepository.findByActiveTrue();
        List<CourseDemandReport> reports = new ArrayList<>();

        for (Course course : allCourses) {
            CourseDemandReport report = new CourseDemandReport();
            report.setCourseId(course.getId());
            report.setCourseCode(course.getCourseCode());
            report.setCourseName(course.getCourseName());
            report.setMinCapacity(course.getMinStudents());
            report.setOptimalCapacity(course.getOptimalStudents());
            report.setMaxCapacity(course.getMaxStudents());
            report.setCurrentEnrollment(course.getCurrentEnrollment());

            // Count requests by preference rank
            List<CourseEnrollmentRequest> requests = enrollmentRequestRepository.findByCourseId(course.getId());
            report.setTotalRequests(requests.size());

            long firstChoiceCount = requests.stream()
                .filter(r -> r.getPreferenceRank() != null && r.getPreferenceRank() == 1)
                .count();
            report.setFirstChoiceRequests((int) firstChoiceCount);

            long secondChoiceCount = requests.stream()
                .filter(r -> r.getPreferenceRank() != null && r.getPreferenceRank() == 2)
                .count();
            report.setSecondChoiceRequests((int) secondChoiceCount);

            long waitlistCount = enrollmentRequestRepository.countByCourseAndIsWaitlistTrue(course);
            report.setWaitlistCount((int) waitlistCount);

            // Calculate demand ratio
            if (course.getMaxStudents() != null && course.getMaxStudents() > 0) {
                double demandRatio = (double) requests.size() / course.getMaxStudents();
                report.setDemandRatio(demandRatio);
            }

            // Determine demand level
            report.setDemandLevel(calculateDemandLevel(report.getDemandRatio()));

            // Calculate capacity status
            report.setCapacityStatus(course.getCapacityStatus());

            reports.add(report);
        }

        // Sort by demand ratio (highest first)
        reports.sort((r1, r2) -> Double.compare(r2.getDemandRatio(), r1.getDemandRatio()));

        return reports;
    }

    /**
     * Calculate demand level based on demand ratio
     */
    private String calculateDemandLevel(double demandRatio) {
        if (demandRatio >= 2.0) return "Very High";
        if (demandRatio >= 1.5) return "High";
        if (demandRatio >= 1.0) return "Moderate";
        if (demandRatio >= 0.5) return "Low";
        return "Very Low";
    }

    // ========================================================================
    // STUDENT PREFERENCE ANALYSIS
    // ========================================================================

    /**
     * Get student preference satisfaction report
     */
    public PreferenceSatisfactionReport getPreferenceSatisfactionReport() {
        PreferenceSatisfactionReport report = new PreferenceSatisfactionReport();

        List<CourseEnrollmentRequest> allRequests = enrollmentRequestRepository.findAll();

        long totalRequests = allRequests.size();
        long approvedRequests = allRequests.stream()
            .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED ||
                         r.getRequestStatus() == EnrollmentRequestStatus.ALTERNATE_ASSIGNED)
            .count();

        report.setTotalRequests((int) totalRequests);
        report.setApprovedRequests((int) approvedRequests);

        if (approvedRequests > 0) {
            // Count by preference rank
            long firstChoice = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .filter(r -> r.getPreferenceRank() != null && r.getPreferenceRank() == 1)
                .count();

            long secondChoice = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .filter(r -> r.getPreferenceRank() != null && r.getPreferenceRank() == 2)
                .count();

            long thirdChoice = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .filter(r -> r.getPreferenceRank() != null && r.getPreferenceRank() == 3)
                .count();

            report.setFirstChoiceCount((int) firstChoice);
            report.setSecondChoiceCount((int) secondChoice);
            report.setThirdChoiceCount((int) thirdChoice);

            // Calculate percentages
            report.setFirstChoicePercent((double) firstChoice / approvedRequests * 100);
            report.setSecondChoicePercent((double) secondChoice / approvedRequests * 100);
            report.setThirdChoicePercent((double) thirdChoice / approvedRequests * 100);
        }

        return report;
    }

    // ========================================================================
    // WAITLIST ANALYSIS
    // ========================================================================

    /**
     * Get waitlist summary for all courses
     */
    public List<WaitlistReport> getWaitlistAnalysis() {
        List<Course> allCourses = courseRepository.findByActiveTrue();
        List<WaitlistReport> reports = new ArrayList<>();

        for (Course course : allCourses) {
            List<CourseEnrollmentRequest> waitlist = enrollmentRequestRepository
                .findByCourseAndIsWaitlistTrueOrderByWaitlistPositionAsc(course);

            if (!waitlist.isEmpty()) {
                WaitlistReport report = new WaitlistReport();
                report.setCourseCode(course.getCourseCode());
                report.setCourseName(course.getCourseName());
                report.setWaitlistSize(waitlist.size());
                report.setMaxWaitlist(course.getMaxWaitlist());
                report.setCurrentEnrollment(course.getCurrentEnrollment());
                report.setMaxCapacity(course.getMaxStudents());

                // Calculate average priority of waitlisted students
                double avgPriority = waitlist.stream()
                    .filter(r -> r.getPriorityScore() != null)
                    .mapToInt(CourseEnrollmentRequest::getPriorityScore)
                    .average()
                    .orElse(0.0);
                report.setAveragePriority(avgPriority);

                // Determine if waitlist is critical (near max)
                if (course.getMaxWaitlist() != null) {
                    double utilizationPercent = (double) waitlist.size() / course.getMaxWaitlist() * 100;
                    report.setWaitlistUtilization(utilizationPercent);
                    report.setCritical(utilizationPercent >= 90);
                }

                reports.add(report);
            }
        }

        // Sort by waitlist size (largest first)
        reports.sort((r1, r2) -> Integer.compare(r2.getWaitlistSize(), r1.getWaitlistSize()));

        return reports;
    }

    // ========================================================================
    // CAPACITY UTILIZATION
    // ========================================================================

    /**
     * Get capacity utilization summary
     */
    public CapacityUtilizationReport getCapacityUtilization() {
        CapacityUtilizationReport report = new CapacityUtilizationReport();

        List<Course> allCourses = courseRepository.findByActiveTrue();
        report.setTotalCourses(allCourses.size());

        // Count by capacity status
        int overCapacity = 0;
        int atCapacity = 0;
        int optimal = 0;
        int good = 0;
        int underCapacity = 0;

        for (Course course : allCourses) {
            String status = course.getCapacityStatus();
            switch (status) {
                case "Over" -> overCapacity++;
                case "Full" -> atCapacity++;
                case "Optimal" -> optimal++;
                case "Good" -> good++;
                case "Under" -> underCapacity++;
            }
        }

        report.setOverCapacity(overCapacity);
        report.setAtCapacity(atCapacity);
        report.setOptimalCapacity(optimal);
        report.setGoodCapacity(good);
        report.setUnderCapacity(underCapacity);

        // Calculate overall utilization
        int totalSeats = allCourses.stream()
            .filter(c -> c.getMaxStudents() != null)
            .mapToInt(Course::getMaxStudents)
            .sum();

        int usedSeats = allCourses.stream()
            .filter(c -> c.getCurrentEnrollment() != null)
            .mapToInt(Course::getCurrentEnrollment)
            .sum();

        if (totalSeats > 0) {
            report.setOverallUtilizationPercent((double) usedSeats / totalSeats * 100);
        }

        return report;
    }

    // ========================================================================
    // STUDENT COMPLETION ANALYSIS
    // ========================================================================

    /**
     * Get student completion analysis (students with full schedules)
     */
    public StudentCompletionReport getStudentCompletionAnalysis() {
        StudentCompletionReport report = new StudentCompletionReport();

        List<Student> allStudents = studentRepository.findByActiveTrue();
        report.setTotalStudents(allStudents.size());

        int completeSchedules = 0;
        int partialSchedules = 0;
        int noAssignments = 0;

        for (Student student : allStudents) {
            List<CourseEnrollmentRequest> approved = enrollmentRequestRepository
                .findByStudentAndRequestStatus(student, EnrollmentRequestStatus.APPROVED);

            int assignedCount = approved.size();

            if (assignedCount >= 7) {
                completeSchedules++;
            } else if (assignedCount > 0) {
                partialSchedules++;
            } else {
                noAssignments++;
            }
        }

        report.setCompleteSchedules(completeSchedules);
        report.setPartialSchedules(partialSchedules);
        report.setNoAssignments(noAssignments);

        // Calculate percentages
        if (allStudents.size() > 0) {
            report.setCompletionRate((double) completeSchedules / allStudents.size() * 100);
        }

        return report;
    }

    // ========================================================================
    // ASSIGNMENT STATISTICS
    // ========================================================================

    /**
     * Get overall assignment statistics
     */
    public AssignmentStatistics getAssignmentStatistics() {
        AssignmentStatistics stats = new AssignmentStatistics();

        List<CourseEnrollmentRequest> allRequests = enrollmentRequestRepository.findAll();

        long pendingCount = allRequests.stream()
            .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.PENDING)
            .count();

        long approvedCount = allRequests.stream()
            .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
            .count();

        long waitlistedCount = allRequests.stream()
            .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.WAITLISTED)
            .count();

        long deniedCount = allRequests.stream()
            .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.DENIED)
            .count();

        stats.setTotalRequests(allRequests.size());
        stats.setPendingRequests((int) pendingCount);
        stats.setApprovedRequests((int) approvedCount);
        stats.setWaitlistedRequests((int) waitlistedCount);
        stats.setDeniedRequests((int) deniedCount);

        // Calculate success rate
        if (allRequests.size() > 0) {
            stats.setSuccessRate((double) approvedCount / allRequests.size() * 100);
        }

        // Calculate average priority scores by status
        stats.setAvgPriorityApproved(calculateAveragePriority(allRequests, EnrollmentRequestStatus.APPROVED));
        stats.setAvgPriorityWaitlisted(calculateAveragePriority(allRequests, EnrollmentRequestStatus.WAITLISTED));
        stats.setAvgPriorityDenied(calculateAveragePriority(allRequests, EnrollmentRequestStatus.DENIED));

        return stats;
    }

    private double calculateAveragePriority(List<CourseEnrollmentRequest> requests, EnrollmentRequestStatus status) {
        return requests.stream()
            .filter(r -> r.getRequestStatus() == status)
            .filter(r -> r.getPriorityScore() != null)
            .mapToInt(CourseEnrollmentRequest::getPriorityScore)
            .average()
            .orElse(0.0);
    }

    // ========================================================================
    // REPORT DTOs (Inner Classes)
    // ========================================================================

    public static class CourseDemandReport {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Integer minCapacity;
        private Integer optimalCapacity;
        private Integer maxCapacity;
        private Integer currentEnrollment;
        private Integer totalRequests;
        private Integer firstChoiceRequests;
        private Integer secondChoiceRequests;
        private Integer waitlistCount;
        private Double demandRatio;
        private String demandLevel;
        private String capacityStatus;

        // Getters and Setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public Integer getMinCapacity() { return minCapacity; }
        public void setMinCapacity(Integer minCapacity) { this.minCapacity = minCapacity; }

        public Integer getOptimalCapacity() { return optimalCapacity; }
        public void setOptimalCapacity(Integer optimalCapacity) { this.optimalCapacity = optimalCapacity; }

        public Integer getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

        public Integer getCurrentEnrollment() { return currentEnrollment; }
        public void setCurrentEnrollment(Integer currentEnrollment) { this.currentEnrollment = currentEnrollment; }

        public Integer getTotalRequests() { return totalRequests; }
        public void setTotalRequests(Integer totalRequests) { this.totalRequests = totalRequests; }

        public Integer getFirstChoiceRequests() { return firstChoiceRequests; }
        public void setFirstChoiceRequests(Integer firstChoiceRequests) { this.firstChoiceRequests = firstChoiceRequests; }

        public Integer getSecondChoiceRequests() { return secondChoiceRequests; }
        public void setSecondChoiceRequests(Integer secondChoiceRequests) { this.secondChoiceRequests = secondChoiceRequests; }

        public Integer getWaitlistCount() { return waitlistCount; }
        public void setWaitlistCount(Integer waitlistCount) { this.waitlistCount = waitlistCount; }

        public Double getDemandRatio() { return demandRatio; }
        public void setDemandRatio(Double demandRatio) { this.demandRatio = demandRatio; }

        public String getDemandLevel() { return demandLevel; }
        public void setDemandLevel(String demandLevel) { this.demandLevel = demandLevel; }

        public String getCapacityStatus() { return capacityStatus; }
        public void setCapacityStatus(String capacityStatus) { this.capacityStatus = capacityStatus; }
    }

    public static class PreferenceSatisfactionReport {
        private int totalRequests;
        private int approvedRequests;
        private int firstChoiceCount;
        private int secondChoiceCount;
        private int thirdChoiceCount;
        private double firstChoicePercent;
        private double secondChoicePercent;
        private double thirdChoicePercent;

        // Getters and Setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

        public int getApprovedRequests() { return approvedRequests; }
        public void setApprovedRequests(int approvedRequests) { this.approvedRequests = approvedRequests; }

        public int getFirstChoiceCount() { return firstChoiceCount; }
        public void setFirstChoiceCount(int firstChoiceCount) { this.firstChoiceCount = firstChoiceCount; }

        public int getSecondChoiceCount() { return secondChoiceCount; }
        public void setSecondChoiceCount(int secondChoiceCount) { this.secondChoiceCount = secondChoiceCount; }

        public int getThirdChoiceCount() { return thirdChoiceCount; }
        public void setThirdChoiceCount(int thirdChoiceCount) { this.thirdChoiceCount = thirdChoiceCount; }

        public double getFirstChoicePercent() { return firstChoicePercent; }
        public void setFirstChoicePercent(double firstChoicePercent) { this.firstChoicePercent = firstChoicePercent; }

        public double getSecondChoicePercent() { return secondChoicePercent; }
        public void setSecondChoicePercent(double secondChoicePercent) { this.secondChoicePercent = secondChoicePercent; }

        public double getThirdChoicePercent() { return thirdChoicePercent; }
        public void setThirdChoicePercent(double thirdChoicePercent) { this.thirdChoicePercent = thirdChoicePercent; }
    }

    public static class WaitlistReport {
        private String courseCode;
        private String courseName;
        private int waitlistSize;
        private Integer maxWaitlist;
        private Integer currentEnrollment;
        private Integer maxCapacity;
        private double averagePriority;
        private double waitlistUtilization;
        private boolean critical;

        // Getters and Setters
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public int getWaitlistSize() { return waitlistSize; }
        public void setWaitlistSize(int waitlistSize) { this.waitlistSize = waitlistSize; }

        public Integer getMaxWaitlist() { return maxWaitlist; }
        public void setMaxWaitlist(Integer maxWaitlist) { this.maxWaitlist = maxWaitlist; }

        public Integer getCurrentEnrollment() { return currentEnrollment; }
        public void setCurrentEnrollment(Integer currentEnrollment) { this.currentEnrollment = currentEnrollment; }

        public Integer getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

        public double getAveragePriority() { return averagePriority; }
        public void setAveragePriority(double averagePriority) { this.averagePriority = averagePriority; }

        public double getWaitlistUtilization() { return waitlistUtilization; }
        public void setWaitlistUtilization(double waitlistUtilization) { this.waitlistUtilization = waitlistUtilization; }

        public boolean isCritical() { return critical; }
        public void setCritical(boolean critical) { this.critical = critical; }
    }

    public static class CapacityUtilizationReport {
        private int totalCourses;
        private int overCapacity;
        private int atCapacity;
        private int optimalCapacity;
        private int goodCapacity;
        private int underCapacity;
        private double overallUtilizationPercent;

        // Getters and Setters
        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

        public int getOverCapacity() { return overCapacity; }
        public void setOverCapacity(int overCapacity) { this.overCapacity = overCapacity; }

        public int getAtCapacity() { return atCapacity; }
        public void setAtCapacity(int atCapacity) { this.atCapacity = atCapacity; }

        public int getOptimalCapacity() { return optimalCapacity; }
        public void setOptimalCapacity(int optimalCapacity) { this.optimalCapacity = optimalCapacity; }

        public int getGoodCapacity() { return goodCapacity; }
        public void setGoodCapacity(int goodCapacity) { this.goodCapacity = goodCapacity; }

        public int getUnderCapacity() { return underCapacity; }
        public void setUnderCapacity(int underCapacity) { this.underCapacity = underCapacity; }

        public double getOverallUtilizationPercent() { return overallUtilizationPercent; }
        public void setOverallUtilizationPercent(double overallUtilizationPercent) {
            this.overallUtilizationPercent = overallUtilizationPercent;
        }
    }

    public static class StudentCompletionReport {
        private int totalStudents;
        private int completeSchedules;
        private int partialSchedules;
        private int noAssignments;
        private double completionRate;

        // Getters and Setters
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public int getCompleteSchedules() { return completeSchedules; }
        public void setCompleteSchedules(int completeSchedules) { this.completeSchedules = completeSchedules; }

        public int getPartialSchedules() { return partialSchedules; }
        public void setPartialSchedules(int partialSchedules) { this.partialSchedules = partialSchedules; }

        public int getNoAssignments() { return noAssignments; }
        public void setNoAssignments(int noAssignments) { this.noAssignments = noAssignments; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }

    public static class AssignmentStatistics {
        private int totalRequests;
        private int pendingRequests;
        private int approvedRequests;
        private int waitlistedRequests;
        private int deniedRequests;
        private double successRate;
        private double avgPriorityApproved;
        private double avgPriorityWaitlisted;
        private double avgPriorityDenied;

        // Getters and Setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

        public int getPendingRequests() { return pendingRequests; }
        public void setPendingRequests(int pendingRequests) { this.pendingRequests = pendingRequests; }

        public int getApprovedRequests() { return approvedRequests; }
        public void setApprovedRequests(int approvedRequests) { this.approvedRequests = approvedRequests; }

        public int getWaitlistedRequests() { return waitlistedRequests; }
        public void setWaitlistedRequests(int waitlistedRequests) { this.waitlistedRequests = waitlistedRequests; }

        public int getDeniedRequests() { return deniedRequests; }
        public void setDeniedRequests(int deniedRequests) { this.deniedRequests = deniedRequests; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public double getAvgPriorityApproved() { return avgPriorityApproved; }
        public void setAvgPriorityApproved(double avgPriorityApproved) { this.avgPriorityApproved = avgPriorityApproved; }

        public double getAvgPriorityWaitlisted() { return avgPriorityWaitlisted; }
        public void setAvgPriorityWaitlisted(double avgPriorityWaitlisted) { this.avgPriorityWaitlisted = avgPriorityWaitlisted; }

        public double getAvgPriorityDenied() { return avgPriorityDenied; }
        public void setAvgPriorityDenied(double avgPriorityDenied) { this.avgPriorityDenied = avgPriorityDenied; }
    }

    /**
     * Get student performance trends over time
     *
     * @param studentId Student ID
     * @param courseId Optional course ID to filter by specific course
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Performance trends data
     */
    @Transactional(readOnly = true)
    public PerformanceTrendsData getStudentPerformanceTrends(Long studentId, Long courseId,
                                                              LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving performance trends for student {} (course: {}) from {} to {}",
                studentId, courseId, startDate, endDate);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get assignments within date range
        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getDueDate() != null)
                .filter(a -> !a.getDueDate().isBefore(startDate) && !a.getDueDate().isAfter(endDate))
                .filter(a -> courseId == null || a.getCourse().getId().equals(courseId))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();

        // Get grades for this student
        List<AssignmentGrade> grades = assignmentGradeRepository.findByStudentId(studentId).stream()
                .filter(g -> assignments.contains(g.getAssignment()))
                .sorted(Comparator.comparing(g -> g.getAssignment().getDueDate()))
                .toList();

        // Calculate weekly trend points
        List<PerformanceTrendPoint> trendPoints = calculatePerformanceTrendPoints(
                grades, startDate, endDate);

        // Calculate statistics
        double averageScore = grades.stream()
                .mapToDouble(AssignmentGrade::getPercentage)
                .average()
                .orElse(0.0);

        int onTimeSubmissions = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() != null &&
                        !g.getSubmittedDate().isAfter(g.getAssignment().getDueDate()))
                .count();

        int lateSubmissions = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() != null &&
                        g.getSubmittedDate().isAfter(g.getAssignment().getDueDate()))
                .count();

        int missingSubmissions = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() == null)
                .count();

        // Determine trend direction
        String trendDirection = calculatePerformanceTrendDirection(trendPoints);

        log.info("Performance trends calculated: {} trend points, avg: {}, trend: {}",
                trendPoints.size(), averageScore, trendDirection);

        PerformanceTrendsData trendsData = new PerformanceTrendsData();
        trendsData.setStudentId(studentId);
        trendsData.setStudentName(student.getFullName());
        trendsData.setCourseId(courseId);
        trendsData.setStartDate(startDate);
        trendsData.setEndDate(endDate);
        trendsData.setTrendPoints(trendPoints);
        trendsData.setAverageScore(averageScore);
        trendsData.setTrendDirection(trendDirection);
        trendsData.setTotalAssignments(assignments.size());
        trendsData.setCompletedAssignments(grades.size());
        trendsData.setOnTimeSubmissions(onTimeSubmissions);
        trendsData.setLateSubmissions(lateSubmissions);
        trendsData.setMissingSubmissions(missingSubmissions);

        return trendsData;
    }

    /**
     * Calculate performance trend points for visualization
     */
    private List<PerformanceTrendPoint> calculatePerformanceTrendPoints(
            List<AssignmentGrade> grades, LocalDate startDate, LocalDate endDate) {

        List<PerformanceTrendPoint> points = new ArrayList<>();

        // Weekly intervals
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate weekEnd = currentDate.plusWeeks(1);

            LocalDate finalCurrentDate = currentDate;
            LocalDate finalWeekEnd = weekEnd;

            List<AssignmentGrade> weekGrades = grades.stream()
                    .filter(g -> {
                        LocalDate dueDate = g.getAssignment().getDueDate();
                        return !dueDate.isBefore(finalCurrentDate) && dueDate.isBefore(finalWeekEnd);
                    })
                    .toList();

            if (!weekGrades.isEmpty()) {
                double avgScore = weekGrades.stream()
                        .mapToDouble(AssignmentGrade::getPercentage)
                        .average()
                        .orElse(0.0);

                int onTime = (int) weekGrades.stream()
                        .filter(g -> g.getSubmittedDate() != null &&
                                !g.getSubmittedDate().isAfter(g.getAssignment().getDueDate()))
                        .count();

                PerformanceTrendPoint point = new PerformanceTrendPoint();
                point.setWeekStartDate(currentDate);
                point.setAverageScore(avgScore);
                point.setAssignmentCount(weekGrades.size());
                point.setOnTimeCount(onTime);
                point.setLateCount(weekGrades.size() - onTime);

                points.add(point);
            }

            currentDate = weekEnd;
        }

        return points;
    }

    /**
     * Calculate performance trend direction
     */
    private String calculatePerformanceTrendDirection(List<PerformanceTrendPoint> points) {
        if (points.size() < 2) {
            return "stable";
        }

        int midpoint = points.size() / 2;
        double firstHalfAvg = points.subList(0, midpoint).stream()
                .mapToDouble(PerformanceTrendPoint::getAverageScore)
                .average()
                .orElse(0.0);

        double secondHalfAvg = points.subList(midpoint, points.size()).stream()
                .mapToDouble(PerformanceTrendPoint::getAverageScore)
                .average()
                .orElse(0.0);

        double diff = secondHalfAvg - firstHalfAvg;

        if (diff > 3.0) {
            return "improving";
        } else if (diff < -3.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    public static class PerformanceTrendsData {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<PerformanceTrendPoint> trendPoints;
        private Double averageScore;
        private String trendDirection;
        private int totalAssignments;
        private int completedAssignments;
        private int onTimeSubmissions;
        private int lateSubmissions;
        private int missingSubmissions;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public List<PerformanceTrendPoint> getTrendPoints() { return trendPoints; }
        public void setTrendPoints(List<PerformanceTrendPoint> trendPoints) { this.trendPoints = trendPoints; }

        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }

        public int getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }

        public int getOnTimeSubmissions() { return onTimeSubmissions; }
        public void setOnTimeSubmissions(int onTimeSubmissions) { this.onTimeSubmissions = onTimeSubmissions; }

        public int getLateSubmissions() { return lateSubmissions; }
        public void setLateSubmissions(int lateSubmissions) { this.lateSubmissions = lateSubmissions; }

        public int getMissingSubmissions() { return missingSubmissions; }
        public void setMissingSubmissions(int missingSubmissions) { this.missingSubmissions = missingSubmissions; }
    }

    public static class PerformanceTrendPoint {
        private LocalDate weekStartDate;
        private Double averageScore;
        private int assignmentCount;
        private int onTimeCount;
        private int lateCount;

        // Getters and Setters
        public LocalDate getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

        public int getAssignmentCount() { return assignmentCount; }
        public void setAssignmentCount(int assignmentCount) { this.assignmentCount = assignmentCount; }

        public int getOnTimeCount() { return onTimeCount; }
        public void setOnTimeCount(int onTimeCount) { this.onTimeCount = onTimeCount; }

        public int getLateCount() { return lateCount; }
        public void setLateCount(int lateCount) { this.lateCount = lateCount; }
    }
}
