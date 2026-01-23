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

    // ========================================================================
    // STUDENT ASSIGNMENT SUMMARY
    // ========================================================================

    /**
     * Get comprehensive assignment summary for a student
     *
     * @param studentId Student ID
     * @param courseId Optional course ID to filter by
     * @param startDate Start of reporting period
     * @param endDate End of reporting period
     * @return Student assignment summary
     */
    @Transactional(readOnly = true)
    public StudentAssignmentSummary getStudentAssignmentSummary(Long studentId, Long courseId,
                                                                  LocalDate startDate, LocalDate endDate) {
        log.info("Generating assignment summary for student {} (course: {}) from {} to {}",
                studentId, courseId, startDate, endDate);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all assignments in date range
        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getDueDate() != null)
                .filter(a -> !a.getDueDate().isBefore(startDate) && !a.getDueDate().isAfter(endDate))
                .filter(a -> courseId == null || a.getCourse().getId().equals(courseId))
                .filter(a -> Boolean.TRUE.equals(a.getPublished()))
                .toList();

        // Get student's grades for these assignments
        List<AssignmentGrade> grades = assignmentGradeRepository.findByStudentId(studentId).stream()
                .filter(g -> assignments.stream().anyMatch(a -> a.getId().equals(g.getAssignment().getId())))
                .toList();

        int totalAssignments = assignments.size();
        int completedAssignments = (int) grades.stream()
                .filter(g -> g.getScore() != null || g.getStatus() == AssignmentGrade.GradeStatus.SUBMITTED)
                .count();
        int missingAssignments = (int) grades.stream()
                .filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.MISSING)
                .count();
        int lateAssignments = (int) grades.stream()
                .filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.LATE ||
                        (g.getLatePenalty() != null && g.getLatePenalty() > 0))
                .count();

        // Calculate average grade
        double averageGrade = grades.stream()
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .mapToDouble(g -> {
                    Double maxPoints = g.getAssignment().getMaxPoints();
                    if (maxPoints == null || maxPoints == 0) return 0.0;
                    return (g.getScore() / maxPoints) * 100;
                })
                .average()
                .orElse(0.0);

        // Determine missing (unsubmitted) assignments
        Set<Long> gradedAssignmentIds = grades.stream()
                .map(g -> g.getAssignment().getId())
                .collect(Collectors.toSet());

        int unsubmittedAssignments = (int) assignments.stream()
                .filter(a -> !gradedAssignmentIds.contains(a.getId()) && a.isPastDue())
                .count();

        missingAssignments += unsubmittedAssignments;

        StudentAssignmentSummary summary = new StudentAssignmentSummary();
        summary.setStudentId(studentId);
        summary.setStudentName(student.getFullName());
        summary.setCourseId(courseId);
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setTotalAssignments(totalAssignments);
        summary.setCompletedAssignments(completedAssignments);
        summary.setMissingAssignments(missingAssignments);
        summary.setLateAssignments(lateAssignments);
        summary.setAverageGrade(averageGrade);
        summary.setCompletionRate(totalAssignments > 0 ? (double) completedAssignments / totalAssignments * 100 : 0);

        log.info("Assignment summary: {} total, {} completed, {} missing, avg: {}%",
                totalAssignments, completedAssignments, missingAssignments, String.format("%.2f", averageGrade));

        return summary;
    }

    /**
     * Get missing assignments for a student
     *
     * @param studentId Student ID
     * @param courseId Optional course ID to filter by
     * @return List of missing assignment details
     */
    @Transactional(readOnly = true)
    public List<MissingAssignmentDetail> getStudentMissingAssignments(Long studentId, Long courseId) {
        log.info("Retrieving missing assignments for student {} (course: {})", studentId, courseId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<AssignmentGrade> missingGrades;
        if (courseId != null) {
            missingGrades = assignmentGradeRepository.findMissingByStudentAndCourse(studentId, courseId);
        } else {
            missingGrades = assignmentGradeRepository.findByStudentId(studentId).stream()
                    .filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.MISSING)
                    .toList();
        }

        // Also find assignments without any grade record that are past due
        Set<Long> existingGradeAssignmentIds = assignmentGradeRepository.findByStudentId(studentId).stream()
                .map(g -> g.getAssignment().getId())
                .collect(Collectors.toSet());

        List<Assignment> unsubmittedAssignments = assignmentRepository.findAll().stream()
                .filter(a -> courseId == null || a.getCourse().getId().equals(courseId))
                .filter(a -> Boolean.TRUE.equals(a.getPublished()))
                .filter(a -> !existingGradeAssignmentIds.contains(a.getId()))
                .filter(Assignment::isPastDue)
                .toList();

        List<MissingAssignmentDetail> result = new ArrayList<>();

        // Add explicitly marked as missing
        for (AssignmentGrade grade : missingGrades) {
            Assignment assignment = grade.getAssignment();
            MissingAssignmentDetail detail = new MissingAssignmentDetail();
            detail.setAssignmentId(assignment.getId());
            detail.setAssignmentTitle(assignment.getTitle());
            detail.setCourseId(assignment.getCourse().getId());
            detail.setCourseName(assignment.getCourse().getCourseName());
            detail.setDueDate(assignment.getDueDate());
            detail.setMaxPoints(assignment.getMaxPoints());
            detail.setDaysOverdue(assignment.getDaysUntilDue() < 0 ? Math.abs(assignment.getDaysUntilDue()) : 0);
            detail.setCategoryName(assignment.getCategory() != null ? assignment.getCategory().getName() : "Uncategorized");
            result.add(detail);
        }

        // Add unsubmitted assignments
        for (Assignment assignment : unsubmittedAssignments) {
            MissingAssignmentDetail detail = new MissingAssignmentDetail();
            detail.setAssignmentId(assignment.getId());
            detail.setAssignmentTitle(assignment.getTitle());
            detail.setCourseId(assignment.getCourse().getId());
            detail.setCourseName(assignment.getCourse().getCourseName());
            detail.setDueDate(assignment.getDueDate());
            detail.setMaxPoints(assignment.getMaxPoints());
            detail.setDaysOverdue(Math.abs(assignment.getDaysUntilDue()));
            detail.setCategoryName(assignment.getCategory() != null ? assignment.getCategory().getName() : "Uncategorized");
            result.add(detail);
        }

        // Sort by due date (most overdue first)
        result.sort(Comparator.comparing(MissingAssignmentDetail::getDueDate));

        log.info("Found {} missing assignments for student {}", result.size(), studentId);
        return result;
    }

    // ========================================================================
    // COURSE GRADE DISTRIBUTION
    // ========================================================================

    /**
     * Get grade distribution for a course or specific assignment
     *
     * @param courseId Course ID
     * @param assignmentId Optional assignment ID
     * @return Grade distribution data
     */
    @Transactional(readOnly = true)
    public GradeDistributionData getCourseGradeDistribution(Long courseId, Long assignmentId) {
        log.info("Generating grade distribution for course {} (assignment: {})", courseId, assignmentId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<AssignmentGrade> grades;
        String context;

        if (assignmentId != null) {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
            grades = assignment.getGrades().stream()
                    .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                    .toList();
            context = assignment.getTitle();
        } else {
            grades = assignmentGradeRepository.findAll().stream()
                    .filter(g -> g.getAssignment().getCourse().getId().equals(courseId))
                    .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                    .toList();
            context = course.getCourseName();
        }

        // Calculate letter grade counts
        int gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0, gradeF = 0;

        for (AssignmentGrade grade : grades) {
            String letter = grade.getLetterGrade();
            if (letter.startsWith("A")) gradeA++;
            else if (letter.startsWith("B")) gradeB++;
            else if (letter.startsWith("C")) gradeC++;
            else if (letter.startsWith("D")) gradeD++;
            else if (letter.equals("F")) gradeF++;
        }

        double average = grades.stream()
                .mapToDouble(g -> {
                    Double pct = g.getPercentage();
                    return pct != null ? pct : 0.0;
                })
                .average()
                .orElse(0.0);

        double median = calculateMedian(grades.stream()
                .map(AssignmentGrade::getPercentage)
                .filter(Objects::nonNull)
                .sorted()
                .toList());

        double highest = grades.stream()
                .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                .max()
                .orElse(0.0);

        double lowest = grades.stream()
                .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                .min()
                .orElse(0.0);

        GradeDistributionData distribution = new GradeDistributionData();
        distribution.setCourseId(courseId);
        distribution.setCourseName(course.getCourseName());
        distribution.setAssignmentId(assignmentId);
        distribution.setContext(context);
        distribution.setTotalGrades(grades.size());
        distribution.setGradeA(gradeA);
        distribution.setGradeB(gradeB);
        distribution.setGradeC(gradeC);
        distribution.setGradeD(gradeD);
        distribution.setGradeF(gradeF);
        distribution.setAverage(average);
        distribution.setMedian(median);
        distribution.setHighest(highest);
        distribution.setLowest(lowest);

        log.info("Grade distribution: A={}, B={}, C={}, D={}, F={}, avg={}%",
                gradeA, gradeB, gradeC, gradeD, gradeF, String.format("%.2f", average));

        return distribution;
    }

    private double calculateMedian(List<Double> sortedValues) {
        if (sortedValues.isEmpty()) return 0.0;
        int middle = sortedValues.size() / 2;
        if (sortedValues.size() % 2 == 0) {
            return (sortedValues.get(middle - 1) + sortedValues.get(middle)) / 2.0;
        }
        return sortedValues.get(middle);
    }

    // ========================================================================
    // COURSE COMPLETION RATES
    // ========================================================================

    /**
     * Get assignment completion rates for a course
     *
     * @param courseId Course ID
     * @param startDate Start of reporting period
     * @param endDate End of reporting period
     * @return Completion rates data
     */
    @Transactional(readOnly = true)
    public CourseCompletionRatesData getCourseCompletionRates(Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating completion rates for course {} from {} to {}", courseId, startDate, endDate);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<Assignment> assignments = assignmentRepository.findByCourseIdAndDueDateBetween(courseId, startDate, endDate)
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getPublished()))
                .toList();

        int totalAssignments = assignments.size();
        int totalSubmissions = 0;
        int onTimeSubmissions = 0;
        int lateSubmissions = 0;
        int missingSubmissions = 0;

        // Count students in course
        int studentCount = course.getCurrentEnrollment() != null ? course.getCurrentEnrollment() : 0;
        int expectedSubmissions = totalAssignments * studentCount;

        for (Assignment assignment : assignments) {
            List<AssignmentGrade> grades = assignment.getGrades();

            for (AssignmentGrade grade : grades) {
                if (grade.getScore() != null || grade.getStatus() == AssignmentGrade.GradeStatus.SUBMITTED ||
                        grade.getStatus() == AssignmentGrade.GradeStatus.GRADED) {
                    totalSubmissions++;

                    if (grade.getLatePenalty() != null && grade.getLatePenalty() > 0) {
                        lateSubmissions++;
                    } else if (grade.getStatus() == AssignmentGrade.GradeStatus.LATE) {
                        lateSubmissions++;
                    } else {
                        onTimeSubmissions++;
                    }
                } else if (grade.getStatus() == AssignmentGrade.GradeStatus.MISSING) {
                    missingSubmissions++;
                }
            }
        }

        // Assignments without grades are also missing
        if (expectedSubmissions > 0) {
            int recordedSubmissions = totalSubmissions + missingSubmissions;
            missingSubmissions += (expectedSubmissions - recordedSubmissions);
        }

        CourseCompletionRatesData data = new CourseCompletionRatesData();
        data.setCourseId(courseId);
        data.setCourseName(course.getCourseName());
        data.setStartDate(startDate);
        data.setEndDate(endDate);
        data.setTotalAssignments(totalAssignments);
        data.setTotalStudents(studentCount);
        data.setExpectedSubmissions(expectedSubmissions);
        data.setActualSubmissions(totalSubmissions);
        data.setOnTimeSubmissions(onTimeSubmissions);
        data.setLateSubmissions(lateSubmissions);
        data.setMissingSubmissions(missingSubmissions);
        data.setOverallCompletionRate(expectedSubmissions > 0 ? (double) totalSubmissions / expectedSubmissions * 100 : 0);
        data.setOnTimeCompletionRate(totalSubmissions > 0 ? (double) onTimeSubmissions / totalSubmissions * 100 : 0);
        data.setLateSubmissionRate(totalSubmissions > 0 ? (double) lateSubmissions / totalSubmissions * 100 : 0);

        log.info("Completion rates: {}% overall, {}% on-time, {}% late",
                String.format("%.2f", data.getOverallCompletionRate()),
                String.format("%.2f", data.getOnTimeCompletionRate()),
                String.format("%.2f", data.getLateSubmissionRate()));

        return data;
    }

    // ========================================================================
    // STRUGGLING STUDENTS
    // ========================================================================

    /**
     * Identify struggling students in a course
     *
     * @param courseId Course ID
     * @param threshold Grade threshold (students below this are struggling)
     * @return List of struggling students
     */
    @Transactional(readOnly = true)
    public List<StrugglingStudentData> getStrugglingStudents(Long courseId, double threshold) {
        log.info("Identifying struggling students in course {} with threshold {}%", courseId, threshold);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Get all students with grades in this course
        List<AssignmentGrade> allGrades = assignmentGradeRepository.findAll().stream()
                .filter(g -> g.getAssignment().getCourse().getId().equals(courseId))
                .toList();

        // Group by student
        Map<Long, List<AssignmentGrade>> gradesByStudent = allGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        List<StrugglingStudentData> strugglingStudents = new ArrayList<>();

        for (Map.Entry<Long, List<AssignmentGrade>> entry : gradesByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<AssignmentGrade> studentGrades = entry.getValue();

            // Calculate average
            double average = studentGrades.stream()
                    .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                    .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                    .average()
                    .orElse(0.0);

            if (average < threshold) {
                Student student = studentGrades.get(0).getStudent();

                int missingCount = (int) studentGrades.stream()
                        .filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.MISSING)
                        .count();

                int lateCount = (int) studentGrades.stream()
                        .filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.LATE ||
                                (g.getLatePenalty() != null && g.getLatePenalty() > 0))
                        .count();

                // Recent trend (last 5 assignments)
                List<AssignmentGrade> recentGrades = studentGrades.stream()
                        .filter(g -> g.getScore() != null)
                        .sorted(Comparator.comparing(g -> g.getAssignment().getDueDate(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(5)
                        .toList();

                String trend = calculateTrendFromGrades(recentGrades);

                StrugglingStudentData data = new StrugglingStudentData();
                data.setStudentId(studentId);
                data.setStudentName(student.getFullName());
                data.setStudentNumber(student.getStudentId());
                data.setGradeLevel(student.getGradeLevel());
                data.setCurrentAverage(average);
                data.setThreshold(threshold);
                data.setPointsBelowThreshold(threshold - average);
                data.setMissingAssignments(missingCount);
                data.setLateAssignments(lateCount);
                data.setTotalAssignments(studentGrades.size());
                data.setRecentTrend(trend);

                strugglingStudents.add(data);
            }
        }

        // Sort by current average (lowest first)
        strugglingStudents.sort(Comparator.comparing(StrugglingStudentData::getCurrentAverage));

        log.info("Found {} struggling students below {}% threshold", strugglingStudents.size(), threshold);
        return strugglingStudents;
    }

    private String calculateTrendFromGrades(List<AssignmentGrade> recentGrades) {
        if (recentGrades.size() < 2) return "insufficient_data";

        List<Double> scores = recentGrades.stream()
                .map(AssignmentGrade::getPercentage)
                .filter(Objects::nonNull)
                .toList();

        if (scores.size() < 2) return "insufficient_data";

        // Compare first half to second half
        int midpoint = scores.size() / 2;
        double firstHalfAvg = scores.subList(0, midpoint).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double secondHalfAvg = scores.subList(midpoint, scores.size()).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double diff = secondHalfAvg - firstHalfAvg;
        if (diff > 5.0) return "improving";
        if (diff < -5.0) return "declining";
        return "stable";
    }

    // ========================================================================
    // ASSIGNMENT DIFFICULTY ANALYSIS
    // ========================================================================

    /**
     * Analyze assignment difficulty based on class performance
     *
     * @param assignmentId Assignment ID
     * @return Difficulty analysis data
     */
    @Transactional(readOnly = true)
    public AssignmentDifficultyAnalysis analyzeAssignmentDifficulty(Long assignmentId) {
        log.info("Analyzing difficulty for assignment {}", assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        List<AssignmentGrade> grades = assignment.getGrades().stream()
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .toList();

        double averageScore = grades.stream()
                .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                .average()
                .orElse(0.0);

        int totalStudents = grades.size();
        int gradedStudents = (int) grades.stream().filter(g -> g.getScore() != null).count();

        // Completion rate
        double completionRate = assignment.getCourse().getCurrentEnrollment() != null &&
                assignment.getCourse().getCurrentEnrollment() > 0
                ? (double) totalStudents / assignment.getCourse().getCurrentEnrollment() * 100
                : 0.0;

        // Grade distribution for difficulty assessment
        int aboveAverage = (int) grades.stream()
                .filter(g -> g.getPercentage() != null && g.getPercentage() >= 70)
                .count();
        int belowAverage = totalStudents - aboveAverage;

        // Determine difficulty level
        String difficultyLevel;
        if (averageScore >= 85) {
            difficultyLevel = "Easy";
        } else if (averageScore >= 70) {
            difficultyLevel = "Moderate";
        } else if (averageScore >= 55) {
            difficultyLevel = "Challenging";
        } else {
            difficultyLevel = "Very Difficult";
        }

        // Standard deviation
        double variance = grades.stream()
                .mapToDouble(g -> {
                    Double pct = g.getPercentage();
                    if (pct == null) return 0.0;
                    return Math.pow(pct - averageScore, 2);
                })
                .average()
                .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        AssignmentDifficultyAnalysis analysis = new AssignmentDifficultyAnalysis();
        analysis.setAssignmentId(assignmentId);
        analysis.setAssignmentTitle(assignment.getTitle());
        analysis.setCourseId(assignment.getCourse().getId());
        analysis.setCourseName(assignment.getCourse().getCourseName());
        analysis.setMaxPoints(assignment.getMaxPoints());
        analysis.setDueDate(assignment.getDueDate());
        analysis.setTotalStudents(assignment.getCourse().getCurrentEnrollment());
        analysis.setSubmittedCount(totalStudents);
        analysis.setGradedCount(gradedStudents);
        analysis.setCompletionRate(completionRate);
        analysis.setAverageScore(averageScore);
        analysis.setStandardDeviation(standardDeviation);
        analysis.setStudentsAbove70Percent(aboveAverage);
        analysis.setStudentsBelow70Percent(belowAverage);
        analysis.setDifficultyLevel(difficultyLevel);

        log.info("Assignment difficulty analysis: {} ({} avg, {} difficulty)",
                assignment.getTitle(), String.format("%.2f", averageScore), difficultyLevel);

        return analysis;
    }

    // ========================================================================
    // SUBMISSION TIMELINE
    // ========================================================================

    /**
     * Get submission timeline for an assignment
     *
     * @param assignmentId Assignment ID
     * @return Submission timeline data
     */
    @Transactional(readOnly = true)
    public SubmissionTimelineData getSubmissionTimeline(Long assignmentId) {
        log.info("Generating submission timeline for assignment {}", assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        List<AssignmentGrade> grades = assignment.getGrades();

        // Group submissions by date
        Map<LocalDate, Long> submissionsByDate = grades.stream()
                .filter(g -> g.getSubmittedDate() != null)
                .collect(Collectors.groupingBy(
                        AssignmentGrade::getSubmittedDate,
                        Collectors.counting()
                ));

        List<SubmissionTimelinePoint> timelinePoints = submissionsByDate.entrySet().stream()
                .map(entry -> {
                    SubmissionTimelinePoint point = new SubmissionTimelinePoint();
                    point.setDate(entry.getKey());
                    point.setSubmissions((int) (long) entry.getValue());
                    point.setIsBeforeDue(assignment.getDueDate() != null && !entry.getKey().isAfter(assignment.getDueDate()));
                    return point;
                })
                .sorted(Comparator.comparing(SubmissionTimelinePoint::getDate))
                .toList();

        // Calculate cumulative submissions
        int cumulative = 0;
        for (SubmissionTimelinePoint point : timelinePoints) {
            cumulative += point.getSubmissions();
            point.setCumulativeSubmissions(cumulative);
        }

        int onTime = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() != null &&
                        assignment.getDueDate() != null &&
                        !g.getSubmittedDate().isAfter(assignment.getDueDate()))
                .count();

        int late = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() != null &&
                        assignment.getDueDate() != null &&
                        g.getSubmittedDate().isAfter(assignment.getDueDate()))
                .count();

        int notSubmitted = (int) grades.stream()
                .filter(g -> g.getSubmittedDate() == null)
                .count();

        SubmissionTimelineData timeline = new SubmissionTimelineData();
        timeline.setAssignmentId(assignmentId);
        timeline.setAssignmentTitle(assignment.getTitle());
        timeline.setDueDate(assignment.getDueDate());
        timeline.setAssignedDate(assignment.getAssignedDate());
        timeline.setTimelinePoints(timelinePoints);
        timeline.setTotalSubmissions(grades.size());
        timeline.setOnTimeSubmissions(onTime);
        timeline.setLateSubmissions(late);
        timeline.setNotSubmitted(notSubmitted);

        log.info("Submission timeline: {} total, {} on-time, {} late",
                grades.size(), onTime, late);

        return timeline;
    }

    // ========================================================================
    // STANDARDS MASTERY
    // ========================================================================

    /**
     * Get standards mastery report for a student
     * Note: This is a placeholder implementation - requires standards/competencies
     * to be properly linked to assignments in the data model
     *
     * @param studentId Student ID
     * @param courseId Optional course ID
     * @return Standards mastery data
     */
    @Transactional(readOnly = true)
    public StudentStandardsMasteryData getStudentStandardsMastery(Long studentId, Long courseId) {
        log.info("Generating standards mastery for student {} (course: {})", studentId, courseId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get student's grades
        List<AssignmentGrade> grades;
        if (courseId != null) {
            grades = assignmentGradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        } else {
            grades = assignmentGradeRepository.findByStudentId(studentId);
        }

        // Group by category as a proxy for standards
        Map<String, List<AssignmentGrade>> gradesByCategory = grades.stream()
                .filter(g -> g.getAssignment().getCategory() != null)
                .collect(Collectors.groupingBy(
                        g -> g.getAssignment().getCategory().getName()
                ));

        List<StandardMasteryItem> masteryItems = new ArrayList<>();

        for (Map.Entry<String, List<AssignmentGrade>> entry : gradesByCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<AssignmentGrade> categoryGrades = entry.getValue();

            double average = categoryGrades.stream()
                    .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                    .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                    .average()
                    .orElse(0.0);

            String masteryLevel;
            if (average >= 90) masteryLevel = "Mastered";
            else if (average >= 80) masteryLevel = "Proficient";
            else if (average >= 70) masteryLevel = "Approaching";
            else if (average >= 60) masteryLevel = "Developing";
            else masteryLevel = "Beginning";

            StandardMasteryItem item = new StandardMasteryItem();
            item.setStandardName(categoryName);
            item.setAverageScore(average);
            item.setMasteryLevel(masteryLevel);
            item.setAssignmentCount(categoryGrades.size());
            item.setCompletedCount((int) categoryGrades.stream().filter(g -> g.getScore() != null).count());

            masteryItems.add(item);
        }

        StudentStandardsMasteryData data = new StudentStandardsMasteryData();
        data.setStudentId(studentId);
        data.setStudentName(student.getFullName());
        data.setCourseId(courseId);
        data.setMasteryItems(masteryItems);
        data.setOverallMasteryPercent(masteryItems.stream()
                .mapToDouble(StandardMasteryItem::getAverageScore)
                .average()
                .orElse(0.0));

        log.info("Standards mastery: {} categories analyzed", masteryItems.size());
        return data;
    }

    /**
     * Get class-wide standards mastery for a course
     *
     * @param courseId Course ID
     * @return Course standards mastery data
     */
    @Transactional(readOnly = true)
    public CourseStandardsMasteryData getCourseStandardsMastery(Long courseId) {
        log.info("Generating course standards mastery for course {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);

        // Group by category
        Map<String, List<Assignment>> assignmentsByCategory = assignments.stream()
                .filter(a -> a.getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getCategory().getName()));

        List<CourseStandardMasteryItem> masteryItems = new ArrayList<>();

        for (Map.Entry<String, List<Assignment>> entry : assignmentsByCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<Assignment> categoryAssignments = entry.getValue();

            // Get all grades for these assignments
            List<Double> allScores = new ArrayList<>();
            int studentsAtMastery = 0;
            int totalStudents = 0;

            for (Assignment assignment : categoryAssignments) {
                for (AssignmentGrade grade : assignment.getGrades()) {
                    if (grade.getScore() != null && !Boolean.TRUE.equals(grade.getExcused())) {
                        Double pct = grade.getPercentage();
                        if (pct != null) {
                            allScores.add(pct);
                            totalStudents++;
                            if (pct >= 80) studentsAtMastery++;
                        }
                    }
                }
            }

            double average = allScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            CourseStandardMasteryItem item = new CourseStandardMasteryItem();
            item.setStandardName(categoryName);
            item.setAverageScore(average);
            item.setAssignmentCount(categoryAssignments.size());
            item.setTotalGrades(allScores.size());
            item.setStudentsAtMastery(studentsAtMastery);
            item.setMasteryRate(totalStudents > 0 ? (double) studentsAtMastery / totalStudents * 100 : 0);

            masteryItems.add(item);
        }

        CourseStandardsMasteryData data = new CourseStandardsMasteryData();
        data.setCourseId(courseId);
        data.setCourseName(course.getCourseName());
        data.setMasteryItems(masteryItems);
        data.setOverallMasteryRate(masteryItems.stream()
                .mapToDouble(CourseStandardMasteryItem::getMasteryRate)
                .average()
                .orElse(0.0));

        log.info("Course standards mastery: {} categories, {}% overall mastery rate",
                masteryItems.size(), String.format("%.2f", data.getOverallMasteryRate()));

        return data;
    }

    // ========================================================================
    // STUDENT COMPARISON
    // ========================================================================

    /**
     * Compare a student's performance to class average
     *
     * @param studentId Student ID
     * @param courseId Course ID
     * @return Comparison data
     */
    @Transactional(readOnly = true)
    public StudentClassComparisonData compareStudentToClass(Long studentId, Long courseId) {
        log.info("Comparing student {} to class average in course {}", studentId, courseId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Get student's grades
        List<AssignmentGrade> studentGrades = assignmentGradeRepository.findByStudentIdAndCourseId(studentId, courseId);

        double studentAverage = studentGrades.stream()
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .mapToDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                .average()
                .orElse(0.0);

        // Get all students' averages in the course
        List<AssignmentGrade> allCourseGrades = assignmentGradeRepository.findAll().stream()
                .filter(g -> g.getAssignment().getCourse().getId().equals(courseId))
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .toList();

        Map<Long, Double> studentAverages = allCourseGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getStudent().getId(),
                        Collectors.averagingDouble(g -> g.getPercentage() != null ? g.getPercentage() : 0.0)
                ));

        double classAverage = studentAverages.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Calculate percentile
        List<Double> sortedAverages = studentAverages.values().stream()
                .sorted()
                .toList();

        int rank = 1;
        for (Double avg : sortedAverages) {
            if (avg < studentAverage) rank++;
        }

        int totalStudents = sortedAverages.size();
        double percentile = totalStudents > 0 ? ((double) rank / totalStudents) * 100 : 0;

        // Ranking string
        String ranking = rank + " of " + totalStudents;

        StudentClassComparisonData comparison = new StudentClassComparisonData();
        comparison.setStudentId(studentId);
        comparison.setStudentName(student.getFullName());
        comparison.setCourseId(courseId);
        comparison.setCourseName(course.getCourseName());
        comparison.setStudentAverage(studentAverage);
        comparison.setClassAverage(classAverage);
        comparison.setDifferenceFromAverage(studentAverage - classAverage);
        comparison.setPercentile(percentile);
        comparison.setClassRank(rank);
        comparison.setTotalStudents(totalStudents);
        comparison.setRanking(ranking);
        comparison.setAboveAverage(studentAverage > classAverage);

        log.info("Student comparison: {}% vs class {}%, rank {} of {}",
                String.format("%.2f", studentAverage),
                String.format("%.2f", classAverage),
                rank, totalStudents);

        return comparison;
    }

    // ========================================================================
    // REPORT GENERATION
    // ========================================================================

    /**
     * Generate comprehensive assignment report
     *
     * @param reportType Type of report (student, course, teacher, standards)
     * @param entityId Entity ID (student, course, or teacher ID)
     * @param startDate Start date
     * @param endDate End date
     * @param format Output format (PDF, CSV, EXCEL)
     * @return Generated report metadata
     */
    @Transactional(readOnly = true)
    public GeneratedReportData generateReport(String reportType, Long entityId,
                                                LocalDate startDate, LocalDate endDate, String format) {
        log.info("Generating {} report for entity {} from {} to {} in {} format",
                reportType, entityId, startDate, endDate, format);

        GeneratedReportData report = new GeneratedReportData();
        report.setReportType(reportType);
        report.setEntityId(entityId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFormat(format);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStatus("completed");

        // Generate report content based on type
        Map<String, Object> reportData = new HashMap<>();

        switch (reportType.toLowerCase()) {
            case "student" -> {
                StudentAssignmentSummary summary = getStudentAssignmentSummary(entityId, null, startDate, endDate);
                List<MissingAssignmentDetail> missing = getStudentMissingAssignments(entityId, null);
                PerformanceTrendsData trends = getStudentPerformanceTrends(entityId, null, startDate, endDate);

                reportData.put("summary", summary);
                reportData.put("missingAssignments", missing);
                reportData.put("performanceTrends", trends);
                report.setTitle("Student Assignment Report - " + summary.getStudentName());
            }
            case "course" -> {
                GradeDistributionData distribution = getCourseGradeDistribution(entityId, null);
                CourseCompletionRatesData completionRates = getCourseCompletionRates(entityId, startDate, endDate);
                List<StrugglingStudentData> struggling = getStrugglingStudents(entityId, 70.0);

                reportData.put("gradeDistribution", distribution);
                reportData.put("completionRates", completionRates);
                reportData.put("strugglingStudents", struggling);
                report.setTitle("Course Assignment Report - " + distribution.getCourseName());
            }
            case "standards" -> {
                CourseStandardsMasteryData mastery = getCourseStandardsMastery(entityId);

                reportData.put("standardsMastery", mastery);
                report.setTitle("Standards Mastery Report - " + mastery.getCourseName());
            }
            default -> {
                reportData.put("message", "Unknown report type: " + reportType);
                report.setStatus("error");
            }
        }

        report.setData(reportData);
        log.info("Report generated: {}", report.getTitle());

        return report;
    }

    // ========================================================================
    // NEW DTO CLASSES
    // ========================================================================

    public static class StudentAssignmentSummary {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalAssignments;
        private int completedAssignments;
        private int missingAssignments;
        private int lateAssignments;
        private double averageGrade;
        private double completionRate;

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
        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
        public int getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }
        public int getMissingAssignments() { return missingAssignments; }
        public void setMissingAssignments(int missingAssignments) { this.missingAssignments = missingAssignments; }
        public int getLateAssignments() { return lateAssignments; }
        public void setLateAssignments(int lateAssignments) { this.lateAssignments = lateAssignments; }
        public double getAverageGrade() { return averageGrade; }
        public void setAverageGrade(double averageGrade) { this.averageGrade = averageGrade; }
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }

    public static class MissingAssignmentDetail {
        private Long assignmentId;
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private LocalDate dueDate;
        private Double maxPoints;
        private long daysOverdue;
        private String categoryName;

        // Getters and Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public Double getMaxPoints() { return maxPoints; }
        public void setMaxPoints(Double maxPoints) { this.maxPoints = maxPoints; }
        public long getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(long daysOverdue) { this.daysOverdue = daysOverdue; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    }

    public static class GradeDistributionData {
        private Long courseId;
        private String courseName;
        private Long assignmentId;
        private String context;
        private int totalGrades;
        private int gradeA;
        private int gradeB;
        private int gradeC;
        private int gradeD;
        private int gradeF;
        private double average;
        private double median;
        private double highest;
        private double lowest;

        // Getters and Setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public int getTotalGrades() { return totalGrades; }
        public void setTotalGrades(int totalGrades) { this.totalGrades = totalGrades; }
        public int getGradeA() { return gradeA; }
        public void setGradeA(int gradeA) { this.gradeA = gradeA; }
        public int getGradeB() { return gradeB; }
        public void setGradeB(int gradeB) { this.gradeB = gradeB; }
        public int getGradeC() { return gradeC; }
        public void setGradeC(int gradeC) { this.gradeC = gradeC; }
        public int getGradeD() { return gradeD; }
        public void setGradeD(int gradeD) { this.gradeD = gradeD; }
        public int getGradeF() { return gradeF; }
        public void setGradeF(int gradeF) { this.gradeF = gradeF; }
        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
        public double getMedian() { return median; }
        public void setMedian(double median) { this.median = median; }
        public double getHighest() { return highest; }
        public void setHighest(double highest) { this.highest = highest; }
        public double getLowest() { return lowest; }
        public void setLowest(double lowest) { this.lowest = lowest; }
    }

    public static class CourseCompletionRatesData {
        private Long courseId;
        private String courseName;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalAssignments;
        private int totalStudents;
        private int expectedSubmissions;
        private int actualSubmissions;
        private int onTimeSubmissions;
        private int lateSubmissions;
        private int missingSubmissions;
        private double overallCompletionRate;
        private double onTimeCompletionRate;
        private double lateSubmissionRate;

        // Getters and Setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public int getExpectedSubmissions() { return expectedSubmissions; }
        public void setExpectedSubmissions(int expectedSubmissions) { this.expectedSubmissions = expectedSubmissions; }
        public int getActualSubmissions() { return actualSubmissions; }
        public void setActualSubmissions(int actualSubmissions) { this.actualSubmissions = actualSubmissions; }
        public int getOnTimeSubmissions() { return onTimeSubmissions; }
        public void setOnTimeSubmissions(int onTimeSubmissions) { this.onTimeSubmissions = onTimeSubmissions; }
        public int getLateSubmissions() { return lateSubmissions; }
        public void setLateSubmissions(int lateSubmissions) { this.lateSubmissions = lateSubmissions; }
        public int getMissingSubmissions() { return missingSubmissions; }
        public void setMissingSubmissions(int missingSubmissions) { this.missingSubmissions = missingSubmissions; }
        public double getOverallCompletionRate() { return overallCompletionRate; }
        public void setOverallCompletionRate(double overallCompletionRate) { this.overallCompletionRate = overallCompletionRate; }
        public double getOnTimeCompletionRate() { return onTimeCompletionRate; }
        public void setOnTimeCompletionRate(double onTimeCompletionRate) { this.onTimeCompletionRate = onTimeCompletionRate; }
        public double getLateSubmissionRate() { return lateSubmissionRate; }
        public void setLateSubmissionRate(double lateSubmissionRate) { this.lateSubmissionRate = lateSubmissionRate; }
    }

    public static class StrugglingStudentData {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private String gradeLevel;
        private double currentAverage;
        private double threshold;
        private double pointsBelowThreshold;
        private int missingAssignments;
        private int lateAssignments;
        private int totalAssignments;
        private String recentTrend;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public double getCurrentAverage() { return currentAverage; }
        public void setCurrentAverage(double currentAverage) { this.currentAverage = currentAverage; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public double getPointsBelowThreshold() { return pointsBelowThreshold; }
        public void setPointsBelowThreshold(double pointsBelowThreshold) { this.pointsBelowThreshold = pointsBelowThreshold; }
        public int getMissingAssignments() { return missingAssignments; }
        public void setMissingAssignments(int missingAssignments) { this.missingAssignments = missingAssignments; }
        public int getLateAssignments() { return lateAssignments; }
        public void setLateAssignments(int lateAssignments) { this.lateAssignments = lateAssignments; }
        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
        public String getRecentTrend() { return recentTrend; }
        public void setRecentTrend(String recentTrend) { this.recentTrend = recentTrend; }
    }

    public static class AssignmentDifficultyAnalysis {
        private Long assignmentId;
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private Double maxPoints;
        private LocalDate dueDate;
        private Integer totalStudents;
        private int submittedCount;
        private int gradedCount;
        private double completionRate;
        private double averageScore;
        private double standardDeviation;
        private int studentsAbove70Percent;
        private int studentsBelow70Percent;
        private String difficultyLevel;

        // Getters and Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public Double getMaxPoints() { return maxPoints; }
        public void setMaxPoints(Double maxPoints) { this.maxPoints = maxPoints; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public int getSubmittedCount() { return submittedCount; }
        public void setSubmittedCount(int submittedCount) { this.submittedCount = submittedCount; }
        public int getGradedCount() { return gradedCount; }
        public void setGradedCount(int gradedCount) { this.gradedCount = gradedCount; }
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        public double getStandardDeviation() { return standardDeviation; }
        public void setStandardDeviation(double standardDeviation) { this.standardDeviation = standardDeviation; }
        public int getStudentsAbove70Percent() { return studentsAbove70Percent; }
        public void setStudentsAbove70Percent(int studentsAbove70Percent) { this.studentsAbove70Percent = studentsAbove70Percent; }
        public int getStudentsBelow70Percent() { return studentsBelow70Percent; }
        public void setStudentsBelow70Percent(int studentsBelow70Percent) { this.studentsBelow70Percent = studentsBelow70Percent; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    }

    public static class SubmissionTimelineData {
        private Long assignmentId;
        private String assignmentTitle;
        private LocalDate dueDate;
        private LocalDate assignedDate;
        private List<SubmissionTimelinePoint> timelinePoints;
        private int totalSubmissions;
        private int onTimeSubmissions;
        private int lateSubmissions;
        private int notSubmitted;

        // Getters and Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public LocalDate getAssignedDate() { return assignedDate; }
        public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
        public List<SubmissionTimelinePoint> getTimelinePoints() { return timelinePoints; }
        public void setTimelinePoints(List<SubmissionTimelinePoint> timelinePoints) { this.timelinePoints = timelinePoints; }
        public int getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        public int getOnTimeSubmissions() { return onTimeSubmissions; }
        public void setOnTimeSubmissions(int onTimeSubmissions) { this.onTimeSubmissions = onTimeSubmissions; }
        public int getLateSubmissions() { return lateSubmissions; }
        public void setLateSubmissions(int lateSubmissions) { this.lateSubmissions = lateSubmissions; }
        public int getNotSubmitted() { return notSubmitted; }
        public void setNotSubmitted(int notSubmitted) { this.notSubmitted = notSubmitted; }
    }

    public static class SubmissionTimelinePoint {
        private LocalDate date;
        private int submissions;
        private int cumulativeSubmissions;
        private boolean isBeforeDue;

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public int getSubmissions() { return submissions; }
        public void setSubmissions(int submissions) { this.submissions = submissions; }
        public int getCumulativeSubmissions() { return cumulativeSubmissions; }
        public void setCumulativeSubmissions(int cumulativeSubmissions) { this.cumulativeSubmissions = cumulativeSubmissions; }
        public boolean isIsBeforeDue() { return isBeforeDue; }
        public void setIsBeforeDue(boolean isBeforeDue) { this.isBeforeDue = isBeforeDue; }
    }

    public static class StudentStandardsMasteryData {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private List<StandardMasteryItem> masteryItems;
        private double overallMasteryPercent;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public List<StandardMasteryItem> getMasteryItems() { return masteryItems; }
        public void setMasteryItems(List<StandardMasteryItem> masteryItems) { this.masteryItems = masteryItems; }
        public double getOverallMasteryPercent() { return overallMasteryPercent; }
        public void setOverallMasteryPercent(double overallMasteryPercent) { this.overallMasteryPercent = overallMasteryPercent; }
    }

    public static class StandardMasteryItem {
        private String standardName;
        private double averageScore;
        private String masteryLevel;
        private int assignmentCount;
        private int completedCount;

        // Getters and Setters
        public String getStandardName() { return standardName; }
        public void setStandardName(String standardName) { this.standardName = standardName; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        public String getMasteryLevel() { return masteryLevel; }
        public void setMasteryLevel(String masteryLevel) { this.masteryLevel = masteryLevel; }
        public int getAssignmentCount() { return assignmentCount; }
        public void setAssignmentCount(int assignmentCount) { this.assignmentCount = assignmentCount; }
        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    }

    public static class CourseStandardsMasteryData {
        private Long courseId;
        private String courseName;
        private List<CourseStandardMasteryItem> masteryItems;
        private double overallMasteryRate;

        // Getters and Setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public List<CourseStandardMasteryItem> getMasteryItems() { return masteryItems; }
        public void setMasteryItems(List<CourseStandardMasteryItem> masteryItems) { this.masteryItems = masteryItems; }
        public double getOverallMasteryRate() { return overallMasteryRate; }
        public void setOverallMasteryRate(double overallMasteryRate) { this.overallMasteryRate = overallMasteryRate; }
    }

    public static class CourseStandardMasteryItem {
        private String standardName;
        private double averageScore;
        private int assignmentCount;
        private int totalGrades;
        private int studentsAtMastery;
        private double masteryRate;

        // Getters and Setters
        public String getStandardName() { return standardName; }
        public void setStandardName(String standardName) { this.standardName = standardName; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        public int getAssignmentCount() { return assignmentCount; }
        public void setAssignmentCount(int assignmentCount) { this.assignmentCount = assignmentCount; }
        public int getTotalGrades() { return totalGrades; }
        public void setTotalGrades(int totalGrades) { this.totalGrades = totalGrades; }
        public int getStudentsAtMastery() { return studentsAtMastery; }
        public void setStudentsAtMastery(int studentsAtMastery) { this.studentsAtMastery = studentsAtMastery; }
        public double getMasteryRate() { return masteryRate; }
        public void setMasteryRate(double masteryRate) { this.masteryRate = masteryRate; }
    }

    public static class StudentClassComparisonData {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private String courseName;
        private double studentAverage;
        private double classAverage;
        private double differenceFromAverage;
        private double percentile;
        private int classRank;
        private int totalStudents;
        private String ranking;
        private boolean aboveAverage;

        // Getters and Setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public double getStudentAverage() { return studentAverage; }
        public void setStudentAverage(double studentAverage) { this.studentAverage = studentAverage; }
        public double getClassAverage() { return classAverage; }
        public void setClassAverage(double classAverage) { this.classAverage = classAverage; }
        public double getDifferenceFromAverage() { return differenceFromAverage; }
        public void setDifferenceFromAverage(double differenceFromAverage) { this.differenceFromAverage = differenceFromAverage; }
        public double getPercentile() { return percentile; }
        public void setPercentile(double percentile) { this.percentile = percentile; }
        public int getClassRank() { return classRank; }
        public void setClassRank(int classRank) { this.classRank = classRank; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public String getRanking() { return ranking; }
        public void setRanking(String ranking) { this.ranking = ranking; }
        public boolean isAboveAverage() { return aboveAverage; }
        public void setAboveAverage(boolean aboveAverage) { this.aboveAverage = aboveAverage; }
    }

    public static class GeneratedReportData {
        private String reportType;
        private Long entityId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String format;
        private String title;
        private String status;
        private LocalDateTime generatedAt;
        private Map<String, Object> data;

        // Getters and Setters
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
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}
