package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.enums.EnrollmentStatus;
import com.heronix.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enrollment Report and Analytics Service for Heronix-SIS
 *
 * Provides comprehensive enrollment reporting and analytics including:
 * - Course enrollment statistics
 * - Student enrollment tracking
 * - Capacity analysis
 * - Enrollment trends over time
 * - Grade level distribution
 * - Department enrollment breakdown
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Service
@Transactional(readOnly = true)
public class EnrollmentReportService {

    private final StudentEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Autowired
    public EnrollmentReportService(
            StudentEnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            StudentRepository studentRepository,
            AuditService auditService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Course Enrollment Statistics
    // ============================================================

    /**
     * Get enrollment statistics for a specific course.
     */
    public CourseEnrollmentStats getCourseEnrollmentStats(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<StudentEnrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        CourseEnrollmentStats stats = new CourseEnrollmentStats();
        stats.setCourseId(courseId);
        stats.setCourseName(course.getCourseName());
        stats.setCourseCode(course.getCourseCode());
        stats.setMaxCapacity(course.getMaxStudents());
        stats.setCurrentEnrollment(enrollments.size());
        stats.setAvailableSeats(course.getMaxStudents() - enrollments.size());
        stats.setEnrollmentPercentage((double) enrollments.size() / course.getMaxStudents() * 100);

        // Grade level distribution
        Map<Integer, Long> gradeLevelDistribution = enrollments.stream()
            .map(e -> {
                try {
                    return Integer.parseInt(e.getStudent().getGradeLevel());
                } catch (NumberFormatException ex) {
                    return 0; // Default for non-numeric grade levels
                }
            })
            .collect(Collectors.groupingBy(
                gradeLevel -> gradeLevel,
                Collectors.counting()
            ));
        stats.setGradeLevelDistribution(gradeLevelDistribution);

        // Gender distribution
        Map<String, Long> genderDistribution = enrollments.stream()
            .map(e -> e.getStudent().getGender())
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                gender -> gender,
                Collectors.counting()
            ));
        stats.setGenderDistribution(genderDistribution);

        // Enrollment status breakdown
        Map<EnrollmentStatus, Long> statusDistribution = enrollments.stream()
            .collect(Collectors.groupingBy(
                StudentEnrollment::getStatus,
                Collectors.counting()
            ));
        stats.setStatusDistribution(statusDistribution);

        auditService.logReportGenerate("course_enrollment_stats");

        return stats;
    }

    /**
     * Get enrollment statistics for all courses.
     */
    public List<CourseEnrollmentStats> getAllCourseEnrollmentStats() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream()
            .map(course -> getCourseEnrollmentStats(course.getId()))
            .sorted(Comparator.comparing(CourseEnrollmentStats::getEnrollmentPercentage).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get courses that are over-enrolled (exceeding max capacity).
     */
    public List<CourseEnrollmentStats> getOverEnrolledCourses() {
        return getAllCourseEnrollmentStats().stream()
            .filter(stats -> stats.getCurrentEnrollment() > stats.getMaxCapacity())
            .collect(Collectors.toList());
    }

    /**
     * Get courses that are under-enrolled (below 50% capacity).
     */
    public List<CourseEnrollmentStats> getUnderEnrolledCourses() {
        return getAllCourseEnrollmentStats().stream()
            .filter(stats -> stats.getEnrollmentPercentage() < 50.0)
            .collect(Collectors.toList());
    }

    /**
     * Get courses nearing capacity (80-100% full).
     */
    public List<CourseEnrollmentStats> getCoursesNearingCapacity() {
        return getAllCourseEnrollmentStats().stream()
            .filter(stats -> stats.getEnrollmentPercentage() >= 80.0
                && stats.getEnrollmentPercentage() <= 100.0)
            .collect(Collectors.toList());
    }

    // ============================================================
    // Student Enrollment Analytics
    // ============================================================

    /**
     * Get enrollment summary for a specific student.
     */
    public StudentEnrollmentSummary getStudentEnrollmentSummary(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        StudentEnrollmentSummary summary = new StudentEnrollmentSummary();
        summary.setStudentId(studentId);
        summary.setStudentName(student.getFirstName() + " " + student.getLastName());
        summary.setGradeLevel(Integer.parseInt(student.getGradeLevel()));
        summary.setTotalCourses(enrollments.size());

        // Active vs dropped courses
        long activeCourses = enrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
            .count();
        long droppedCourses = enrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.DROPPED)
            .count();

        summary.setActiveCourses((int) activeCourses);
        summary.setDroppedCourses((int) droppedCourses);

        // Course list
        List<String> courseNames = enrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
            .map(e -> e.getCourse().getCourseName())
            .collect(Collectors.toList());
        summary.setCourseList(courseNames);

        // Total credits
        double totalCredits = enrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
            .mapToDouble(e -> e.getCourse().getCredits())
            .sum();
        summary.setTotalCredits(totalCredits);

        auditService.logStudentView(studentId);

        return summary;
    }

    /**
     * Get students with excessive course loads (more than recommended).
     */
    public List<StudentEnrollmentSummary> getStudentsWithExcessiveCourseLoad(int maxRecommendedCourses) {
        List<Student> students = studentRepository.findAll();

        return students.stream()
            .map(student -> getStudentEnrollmentSummary(student.getId()))
            .filter(summary -> summary.getActiveCourses() > maxRecommendedCourses)
            .sorted(Comparator.comparing(StudentEnrollmentSummary::getActiveCourses).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get students with low course loads (fewer than recommended).
     */
    public List<StudentEnrollmentSummary> getStudentsWithLowCourseLoad(int minRecommendedCourses) {
        List<Student> students = studentRepository.findAll();

        return students.stream()
            .map(student -> getStudentEnrollmentSummary(student.getId()))
            .filter(summary -> summary.getActiveCourses() < minRecommendedCourses)
            .filter(summary -> summary.getActiveCourses() > 0) // Exclude students with no courses
            .sorted(Comparator.comparing(StudentEnrollmentSummary::getActiveCourses))
            .collect(Collectors.toList());
    }

    // ============================================================
    // Department/Grade Level Analytics
    // ============================================================

    /**
     * Get enrollment breakdown by grade level.
     */
    public Map<Integer, GradeLevelEnrollmentStats> getEnrollmentByGradeLevel() {
        List<Student> students = studentRepository.findAll();

        Map<Integer, GradeLevelEnrollmentStats> statsMap = new HashMap<>();

        for (Student student : students) {
            int gradeLevel = Integer.parseInt(student.getGradeLevel());

            GradeLevelEnrollmentStats stats = statsMap.computeIfAbsent(gradeLevel, k -> {
                GradeLevelEnrollmentStats newStats = new GradeLevelEnrollmentStats();
                newStats.setGradeLevel(gradeLevel);
                return newStats;
            });

            List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
            long activeCourses = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

            stats.incrementStudentCount();
            stats.addTotalEnrollments((int) activeCourses);
        }

        // Calculate averages
        statsMap.values().forEach(stats -> {
            if (stats.getStudentCount() > 0) {
                double avg = (double) stats.getTotalEnrollments() / stats.getStudentCount();
                stats.setAverageCoursesPerStudent(avg);
            }
        });

        auditService.logReportGenerate("grade_level_enrollment");

        return statsMap;
    }

    /**
     * Get overall enrollment summary for the school.
     */
    public SchoolEnrollmentSummary getSchoolEnrollmentSummary() {
        SchoolEnrollmentSummary summary = new SchoolEnrollmentSummary();

        // Total students
        long totalStudents = studentRepository.count();
        summary.setTotalStudents((int) totalStudents);

        // Total courses
        long totalCourses = courseRepository.count();
        summary.setTotalCourses((int) totalCourses);

        // Total enrollments
        long totalEnrollments = enrollmentRepository.count();
        summary.setTotalEnrollments((int) totalEnrollments);

        // Active enrollments
        List<StudentEnrollment> allEnrollments = enrollmentRepository.findAll();
        long activeEnrollments = allEnrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
            .count();
        summary.setActiveEnrollments((int) activeEnrollments);

        // Average courses per student
        if (totalStudents > 0) {
            double avgCourses = (double) activeEnrollments / totalStudents;
            summary.setAverageCoursesPerStudent(avgCourses);
        }

        // Average students per course
        if (totalCourses > 0) {
            double avgStudents = (double) activeEnrollments / totalCourses;
            summary.setAverageStudentsPerCourse(avgStudents);
        }

        // Grade level breakdown
        Map<Integer, GradeLevelEnrollmentStats> gradeLevelStats = getEnrollmentByGradeLevel();
        summary.setGradeLevelStats(gradeLevelStats);

        // Course capacity utilization
        List<CourseEnrollmentStats> courseStats = getAllCourseEnrollmentStats();
        double totalCapacity = courseStats.stream()
            .mapToDouble(CourseEnrollmentStats::getMaxCapacity)
            .sum();
        double totalEnrolled = courseStats.stream()
            .mapToDouble(CourseEnrollmentStats::getCurrentEnrollment)
            .sum();

        if (totalCapacity > 0) {
            summary.setOverallCapacityUtilization(totalEnrolled / totalCapacity * 100);
        }

        auditService.logReportGenerate("school_enrollment_summary");

        return summary;
    }

    // ============================================================
    // Enrollment Trend Analysis
    // ============================================================

    /**
     * Get enrollment trends over time (by enrollment date).
     */
    public EnrollmentTrendReport getEnrollmentTrends(LocalDate startDate, LocalDate endDate) {
        List<StudentEnrollment> enrollments = enrollmentRepository.findAll().stream()
            .filter(e -> {
                LocalDate enrollDate = e.getEnrolledDate().toLocalDate();
                return enrollDate != null &&
                    !enrollDate.isBefore(startDate) &&
                    !enrollDate.isAfter(endDate);
            })
            .collect(Collectors.toList());

        EnrollmentTrendReport report = new EnrollmentTrendReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalEnrollments(enrollments.size());

        // Enrollments by month
        Map<String, Long> enrollmentsByMonth = enrollments.stream()
            .collect(Collectors.groupingBy(
                e -> e.getEnrolledDate().getYear() + "-" +
                    String.format("%02d", e.getEnrolledDate().getMonthValue()),
                Collectors.counting()
            ));
        report.setEnrollmentsByMonth(enrollmentsByMonth);

        // Most popular courses in period
        Map<String, Long> popularCourses = enrollments.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCourse().getCourseName(),
                Collectors.counting()
            ));

        List<Map.Entry<String, Long>> topCourses = popularCourses.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());

        report.setTopCourses(topCourses);

        auditService.logReportGenerate("enrollment_trends");

        return report;
    }

    // ============================================================
    // Data Transfer Objects (DTOs)
    // ============================================================

    /**
     * Course enrollment statistics DTO.
     */
    public static class CourseEnrollmentStats {
        private Long courseId;
        private String courseName;
        private String courseCode;
        private int maxCapacity;
        private int currentEnrollment;
        private int availableSeats;
        private double enrollmentPercentage;
        private Map<Integer, Long> gradeLevelDistribution;
        private Map<String, Long> genderDistribution;
        private Map<EnrollmentStatus, Long> statusDistribution;

        // Getters and setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

        public int getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

        public int getCurrentEnrollment() { return currentEnrollment; }
        public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }

        public int getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

        public double getEnrollmentPercentage() { return enrollmentPercentage; }
        public void setEnrollmentPercentage(double enrollmentPercentage) { this.enrollmentPercentage = enrollmentPercentage; }

        public Map<Integer, Long> getGradeLevelDistribution() { return gradeLevelDistribution; }
        public void setGradeLevelDistribution(Map<Integer, Long> gradeLevelDistribution) { this.gradeLevelDistribution = gradeLevelDistribution; }

        public Map<String, Long> getGenderDistribution() { return genderDistribution; }
        public void setGenderDistribution(Map<String, Long> genderDistribution) { this.genderDistribution = genderDistribution; }

        public Map<EnrollmentStatus, Long> getStatusDistribution() { return statusDistribution; }
        public void setStatusDistribution(Map<EnrollmentStatus, Long> statusDistribution) { this.statusDistribution = statusDistribution; }
    }

    /**
     * Student enrollment summary DTO.
     */
    public static class StudentEnrollmentSummary {
        private Long studentId;
        private String studentName;
        private int gradeLevel;
        private int totalCourses;
        private int activeCourses;
        private int droppedCourses;
        private double totalCredits;
        private List<String> courseList;

        // Getters and setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public int getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(int gradeLevel) { this.gradeLevel = gradeLevel; }

        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

        public int getActiveCourses() { return activeCourses; }
        public void setActiveCourses(int activeCourses) { this.activeCourses = activeCourses; }

        public int getDroppedCourses() { return droppedCourses; }
        public void setDroppedCourses(int droppedCourses) { this.droppedCourses = droppedCourses; }

        public double getTotalCredits() { return totalCredits; }
        public void setTotalCredits(double totalCredits) { this.totalCredits = totalCredits; }

        public List<String> getCourseList() { return courseList; }
        public void setCourseList(List<String> courseList) { this.courseList = courseList; }
    }

    /**
     * Grade level enrollment statistics DTO.
     */
    public static class GradeLevelEnrollmentStats {
        private int gradeLevel;
        private int studentCount;
        private int totalEnrollments;
        private double averageCoursesPerStudent;

        // Getters and setters
        public int getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(int gradeLevel) { this.gradeLevel = gradeLevel; }

        public int getStudentCount() { return studentCount; }
        public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
        public void incrementStudentCount() { this.studentCount++; }

        public int getTotalEnrollments() { return totalEnrollments; }
        public void setTotalEnrollments(int totalEnrollments) { this.totalEnrollments = totalEnrollments; }
        public void addTotalEnrollments(int count) { this.totalEnrollments += count; }

        public double getAverageCoursesPerStudent() { return averageCoursesPerStudent; }
        public void setAverageCoursesPerStudent(double averageCoursesPerStudent) { this.averageCoursesPerStudent = averageCoursesPerStudent; }
    }

    /**
     * School-wide enrollment summary DTO.
     */
    public static class SchoolEnrollmentSummary {
        private int totalStudents;
        private int totalCourses;
        private int totalEnrollments;
        private int activeEnrollments;
        private double averageCoursesPerStudent;
        private double averageStudentsPerCourse;
        private double overallCapacityUtilization;
        private Map<Integer, GradeLevelEnrollmentStats> gradeLevelStats;

        // Getters and setters
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

        public int getTotalEnrollments() { return totalEnrollments; }
        public void setTotalEnrollments(int totalEnrollments) { this.totalEnrollments = totalEnrollments; }

        public int getActiveEnrollments() { return activeEnrollments; }
        public void setActiveEnrollments(int activeEnrollments) { this.activeEnrollments = activeEnrollments; }

        public double getAverageCoursesPerStudent() { return averageCoursesPerStudent; }
        public void setAverageCoursesPerStudent(double averageCoursesPerStudent) { this.averageCoursesPerStudent = averageCoursesPerStudent; }

        public double getAverageStudentsPerCourse() { return averageStudentsPerCourse; }
        public void setAverageStudentsPerCourse(double averageStudentsPerCourse) { this.averageStudentsPerCourse = averageStudentsPerCourse; }

        public double getOverallCapacityUtilization() { return overallCapacityUtilization; }
        public void setOverallCapacityUtilization(double overallCapacityUtilization) { this.overallCapacityUtilization = overallCapacityUtilization; }

        public Map<Integer, GradeLevelEnrollmentStats> getGradeLevelStats() { return gradeLevelStats; }
        public void setGradeLevelStats(Map<Integer, GradeLevelEnrollmentStats> gradeLevelStats) { this.gradeLevelStats = gradeLevelStats; }
    }

    /**
     * Enrollment trend report DTO.
     */
    public static class EnrollmentTrendReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalEnrollments;
        private Map<String, Long> enrollmentsByMonth;
        private List<Map.Entry<String, Long>> topCourses;

        // Getters and setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public int getTotalEnrollments() { return totalEnrollments; }
        public void setTotalEnrollments(int totalEnrollments) { this.totalEnrollments = totalEnrollments; }

        public Map<String, Long> getEnrollmentsByMonth() { return enrollmentsByMonth; }
        public void setEnrollmentsByMonth(Map<String, Long> enrollmentsByMonth) { this.enrollmentsByMonth = enrollmentsByMonth; }

        public List<Map.Entry<String, Long>> getTopCourses() { return topCourses; }
        public void setTopCourses(List<Map.Entry<String, Long>> topCourses) { this.topCourses = topCourses; }
    }
}
