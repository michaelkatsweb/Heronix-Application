package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gradebook Service
 *
 * Handles all gradebook operations including:
 * - Weighted grade calculations
 * - Category management
 * - Assignment management
 * - Grade entry and updates
 * - Report generation
 *
 * Grade Calculation:
 * 1. Calculate category averages (with drop lowest option)
 * 2. Apply category weights
 * 3. Sum weighted averages for final grade
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-22
 */
@Slf4j
@Service
@Transactional
public class GradebookService {

    @Autowired
    private GradingCategoryRepository categoryRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentGradeRepository gradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ========================================================================
    // CATEGORY MANAGEMENT
    // ========================================================================

    /**
     * Create default grading categories for a course
     */
    public List<GradingCategory> createDefaultCategories(Long courseId) {
        log.info("Creating default grading categories for course {}", courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<GradingCategory> categories = new ArrayList<>();

        // Standard K-12 category setup
        categories.add(GradingCategory.builder()
            .course(course)
            .name("Tests")
            .categoryType(GradingCategory.CategoryType.TEST)
            .weight(30.0)
            .displayOrder(1)
            .color("#F44336")
            .build());

        categories.add(GradingCategory.builder()
            .course(course)
            .name("Quizzes")
            .categoryType(GradingCategory.CategoryType.QUIZ)
            .weight(20.0)
            .dropLowest(1) // Drop lowest quiz
            .displayOrder(2)
            .color("#FF9800")
            .build());

        categories.add(GradingCategory.builder()
            .course(course)
            .name("Homework")
            .categoryType(GradingCategory.CategoryType.HOMEWORK)
            .weight(20.0)
            .displayOrder(3)
            .color("#4CAF50")
            .build());

        categories.add(GradingCategory.builder()
            .course(course)
            .name("Projects")
            .categoryType(GradingCategory.CategoryType.PROJECT)
            .weight(20.0)
            .displayOrder(4)
            .color("#9C27B0")
            .build());

        categories.add(GradingCategory.builder()
            .course(course)
            .name("Participation")
            .categoryType(GradingCategory.CategoryType.PARTICIPATION)
            .weight(10.0)
            .displayOrder(5)
            .color("#03A9F4")
            .build());

        return categoryRepository.saveAll(categories);
    }

    /**
     * Get categories for a course
     */
    public List<GradingCategory> getCategoriesForCourse(Long courseId) {
        return categoryRepository.findByCourseIdAndActiveTrueOrderByDisplayOrder(courseId);
    }

    /**
     * Update category weights (ensures they sum to 100%)
     */
    public void updateCategoryWeights(Map<Long, Double> categoryWeights) {
        double total = categoryWeights.values().stream().mapToDouble(Double::doubleValue).sum();

        if (Math.abs(total - 100.0) > 0.01) {
            throw new IllegalArgumentException("Category weights must sum to 100%. Current sum: " + total);
        }

        for (Map.Entry<Long, Double> entry : categoryWeights.entrySet()) {
            GradingCategory category = categoryRepository.findById(entry.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + entry.getKey()));
            category.setWeight(entry.getValue());
            categoryRepository.save(category);
        }

        log.info("Updated {} category weights", categoryWeights.size());
    }

    // ========================================================================
    // ASSIGNMENT MANAGEMENT
    // ========================================================================

    /**
     * Create a new assignment
     */
    public Assignment createAssignment(Long courseId, Long categoryId, String title,
                                        Double maxPoints, LocalDate dueDate) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        GradingCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        Assignment assignment = Assignment.builder()
            .course(course)
            .category(category)
            .title(title)
            .maxPoints(maxPoints)
            .dueDate(dueDate)
            .assignedDate(LocalDate.now())
            .published(false)
            .build();

        assignment = assignmentRepository.save(assignment);
        log.info("Created assignment '{}' for course {}", title, courseId);

        return assignment;
    }

    /**
     * Get assignments for a course
     */
    public List<Assignment> getAssignmentsForCourse(Long courseId) {
        return assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);
    }

    /**
     * Get assignments by category
     */
    public List<Assignment> getAssignmentsByCategory(Long categoryId) {
        return assignmentRepository.findByCategoryIdOrderByDueDateDesc(categoryId);
    }

    /**
     * Publish an assignment (make visible to students)
     */
    public void publishAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        assignment.setPublished(true);
        assignmentRepository.save(assignment);

        log.info("Published assignment '{}'", assignment.getTitle());
    }

    // ========================================================================
    // GRADE ENTRY
    // ========================================================================

    /**
     * Enter or update a grade for a student on an assignment
     */
    public AssignmentGrade enterGrade(Long studentId, Long assignmentId, Double score,
                                       LocalDate submittedDate, String comments) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        // Find existing or create new
        AssignmentGrade grade = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
            .orElse(AssignmentGrade.builder()
                .student(student)
                .assignment(assignment)
                .build());

        grade.setScore(score);
        grade.setSubmittedDate(submittedDate != null ? submittedDate : LocalDate.now());
        grade.setComments(comments);
        grade.setGradedDate(LocalDate.now());
        grade.setStatus(AssignmentGrade.GradeStatus.GRADED);

        // Calculate late penalty if applicable
        if (submittedDate != null && assignment.getDueDate() != null) {
            double penalty = assignment.calculateLatePenalty(submittedDate);
            grade.setLatePenalty(penalty);
            if (penalty > 0) {
                grade.setStatus(AssignmentGrade.GradeStatus.LATE);
            }
        }

        grade = gradeRepository.save(grade);
        log.info("Entered grade {} for student {} on assignment {}",
            score, student.getStudentId(), assignment.getTitle());

        return grade;
    }

    /**
     * Bulk enter grades for an assignment
     */
    public int bulkEnterGrades(Long assignmentId, Map<Long, Double> studentScores) {
        int count = 0;
        for (Map.Entry<Long, Double> entry : studentScores.entrySet()) {
            try {
                enterGrade(entry.getKey(), assignmentId, entry.getValue(), LocalDate.now(), null);
                count++;
            } catch (Exception e) {
                log.error("Failed to enter grade for student {}: {}", entry.getKey(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * Mark a grade as excused
     */
    public void excuseGrade(Long studentId, Long assignmentId, String reason) {
        AssignmentGrade grade = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
            .orElseGet(() -> {
                Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

                return AssignmentGrade.builder()
                    .student(student)
                    .assignment(assignment)
                    .build();
            });

        grade.markExcused(reason);
        gradeRepository.save(grade);

        log.info("Excused grade for student {} on assignment {}", studentId, assignmentId);
    }

    // ========================================================================
    // GRADE CALCULATIONS
    // ========================================================================

    /**
     * Calculate weighted course grade for a student
     */
    public StudentCourseGrade calculateCourseGrade(Long studentId, Long courseId) {
        List<GradingCategory> categories = getCategoriesForCourse(courseId);
        List<AssignmentGrade> grades = gradeRepository.findForGradebookCalculation(studentId, courseId);

        if (categories.isEmpty()) {
            return StudentCourseGrade.builder()
                .studentId(studentId)
                .courseId(courseId)
                .finalPercentage(0.0)
                .letterGrade("-")
                .build();
        }

        // Group grades by category
        Map<Long, List<AssignmentGrade>> gradesByCategory = grades.stream()
            .filter(AssignmentGrade::countsInGrade)
            .collect(Collectors.groupingBy(g -> g.getAssignment().getCategory().getId()));

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        List<CategoryGrade> categoryGrades = new ArrayList<>();

        for (GradingCategory category : categories) {
            List<AssignmentGrade> categoryGradeList = gradesByCategory.getOrDefault(category.getId(), List.of());

            if (categoryGradeList.isEmpty()) {
                continue; // Skip categories with no grades
            }

            // Calculate category average with drop lowest
            double categoryAvg = calculateCategoryAverage(categoryGradeList, category.getDropLowest());

            CategoryGrade cg = CategoryGrade.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .weight(category.getWeight())
                .average(categoryAvg)
                .assignmentCount(categoryGradeList.size())
                .build();
            categoryGrades.add(cg);

            totalWeightedScore += (categoryAvg * category.getWeight() / 100.0);
            totalWeight += category.getWeight();
        }

        // Normalize if not all categories have grades
        double finalPercentage = totalWeight > 0 ? (totalWeightedScore / totalWeight) * 100.0 : 0.0;

        return StudentCourseGrade.builder()
            .studentId(studentId)
            .courseId(courseId)
            .categoryGrades(categoryGrades)
            .finalPercentage(finalPercentage)
            .letterGrade(percentageToLetterGrade(finalPercentage))
            .gpaPoints(percentageToGpaPoints(finalPercentage))
            .totalAssignments(grades.size())
            .gradedAssignments((int) grades.stream().filter(g -> g.getScore() != null).count())
            .missingAssignments((int) grades.stream().filter(g -> g.getStatus() == AssignmentGrade.GradeStatus.MISSING).count())
            .build();
    }

    /**
     * Calculate category average with drop lowest option
     */
    private double calculateCategoryAverage(List<AssignmentGrade> grades, int dropLowest) {
        if (grades.isEmpty()) return 0.0;

        // Get percentages (score / max * 100)
        List<Double> percentages = grades.stream()
            .filter(g -> g.getAdjustedScore() != null)
            .map(g -> {
                double max = g.getAssignment().getMaxPoints();
                return max > 0 ? (g.getAdjustedScore() / max) * 100.0 : 0.0;
            })
            .sorted() // Sort ascending for drop lowest
            .collect(Collectors.toList());

        if (percentages.isEmpty()) return 0.0;

        // Drop lowest N scores
        int toDrop = Math.min(dropLowest, Math.max(0, percentages.size() - 1)); // Keep at least 1
        List<Double> remaining = percentages.subList(toDrop, percentages.size());

        return remaining.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Get class gradebook summary (all students, all assignments)
     */
    public ClassGradebook getClassGradebook(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        List<Assignment> assignments = assignmentRepository.findByCourseIdWithGrades(courseId);
        List<GradingCategory> categories = getCategoriesForCourse(courseId);

        // Get enrolled students
        List<Student> students = course.getStudents() != null
            ? new ArrayList<>(course.getStudents())
            : List.of();

        // Calculate grade for each student
        List<StudentCourseGrade> studentGrades = students.stream()
            .map(s -> calculateCourseGrade(s.getId(), courseId))
            .collect(Collectors.toList());

        // Calculate class statistics
        DoubleSummaryStatistics stats = studentGrades.stream()
            .mapToDouble(StudentCourseGrade::getFinalPercentage)
            .summaryStatistics();

        return ClassGradebook.builder()
            .courseId(courseId)
            .courseName(course.getCourseName())
            .categories(categories)
            .assignments(assignments)
            .studentGrades(studentGrades)
            .classAverage(stats.getAverage())
            .classHigh(stats.getMax())
            .classLow(stats.getMin())
            .studentCount((int) stats.getCount())
            .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private String percentageToLetterGrade(double percentage) {
        if (percentage >= 97) return "A+";
        if (percentage >= 93) return "A";
        if (percentage >= 90) return "A-";
        if (percentage >= 87) return "B+";
        if (percentage >= 83) return "B";
        if (percentage >= 80) return "B-";
        if (percentage >= 77) return "C+";
        if (percentage >= 73) return "C";
        if (percentage >= 70) return "C-";
        if (percentage >= 67) return "D+";
        if (percentage >= 63) return "D";
        if (percentage >= 60) return "D-";
        return "F";
    }

    private double percentageToGpaPoints(double percentage) {
        if (percentage >= 93) return 4.0;
        if (percentage >= 90) return 3.7;
        if (percentage >= 87) return 3.3;
        if (percentage >= 83) return 3.0;
        if (percentage >= 80) return 2.7;
        if (percentage >= 77) return 2.3;
        if (percentage >= 73) return 2.0;
        if (percentage >= 70) return 1.7;
        if (percentage >= 67) return 1.3;
        if (percentage >= 63) return 1.0;
        if (percentage >= 60) return 0.7;
        return 0.0;
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    @Data
    @Builder
    public static class StudentCourseGrade {
        private Long studentId;
        private Long courseId;
        private List<CategoryGrade> categoryGrades;
        private Double finalPercentage;
        private String letterGrade;
        private Double gpaPoints;
        private int totalAssignments;
        private int gradedAssignments;
        private int missingAssignments;
    }

    @Data
    @Builder
    public static class CategoryGrade {
        private Long categoryId;
        private String categoryName;
        private Double weight;
        private Double average;
        private int assignmentCount;
    }

    @Data
    @Builder
    public static class ClassGradebook {
        private Long courseId;
        private String courseName;
        private List<GradingCategory> categories;
        private List<Assignment> assignments;
        private List<StudentCourseGrade> studentGrades;
        private Double classAverage;
        private Double classHigh;
        private Double classLow;
        private int studentCount;
    }

    // ========================================================================
    // API SUPPORT METHODS (Phase 3)
    // ========================================================================

    /**
     * Get all grades for a student across all courses
     * Returns comprehensive view including GPA and academic standing
     */
    public StudentAllGrades getStudentAllGrades(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all courses for student
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getStudents() != null && c.getStudents().contains(student))
                .collect(Collectors.toList());

        // Calculate grade for each course
        List<StudentCourseGrade> courseGrades = courses.stream()
                .map(c -> calculateCourseGrade(studentId, c.getId()))
                .collect(Collectors.toList());

        // Calculate overall statistics
        DoubleSummaryStatistics stats = courseGrades.stream()
                .mapToDouble(StudentCourseGrade::getFinalPercentage)
                .summaryStatistics();

        return StudentAllGrades.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .courseGrades(courseGrades)
                .overallAverage(stats.getAverage())
                .totalCourses(courses.size())
                .build();
    }

    /**
     * Get missing assignments for a student
     */
    public List<MissingAssignment> getStudentMissingAssignments(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<MissingAssignment> missingAssignments = new ArrayList<>();

        // Get all assignments for student's courses
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getStudents() != null && c.getStudents().contains(student))
                .collect(Collectors.toList());

        for (Course course : courses) {
            List<Assignment> assignments = assignmentRepository.findByCourseIdAndPublishedTrueOrderByDueDateDesc(course.getId());

            for (Assignment assignment : assignments) {
                Optional<AssignmentGrade> gradeOpt = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignment.getId());

                boolean isMissing = gradeOpt.isEmpty() ||
                                   gradeOpt.get().getStatus() == AssignmentGrade.GradeStatus.MISSING ||
                                   (gradeOpt.get().getScore() == null && assignment.getDueDate() != null &&
                                    assignment.getDueDate().isBefore(LocalDate.now()));

                if (isMissing) {
                    missingAssignments.add(MissingAssignment.builder()
                            .assignmentId(assignment.getId())
                            .assignmentTitle(assignment.getTitle())
                            .courseId(course.getId())
                            .courseName(course.getCourseName())
                            .dueDate(assignment.getDueDate())
                            .maxPoints(assignment.getMaxPoints())
                            .categoryName(assignment.getCategory().getName())
                            .daysOverdue(assignment.getDueDate() != null ?
                                        java.time.temporal.ChronoUnit.DAYS.between(assignment.getDueDate(), LocalDate.now()) : 0)
                            .build());
                }
            }
        }

        return missingAssignments;
    }

    /**
     * Get grade alerts for a student
     */
    public List<GradeAlert> getStudentGradeAlerts(Long studentId) {
        List<GradeAlert> alerts = new ArrayList<>();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all course grades
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getStudents() != null && c.getStudents().contains(student))
                .collect(Collectors.toList());

        for (Course course : courses) {
            StudentCourseGrade grade = calculateCourseGrade(studentId, course.getId());

            // Failing alert (below 60%)
            if (grade.getFinalPercentage() < 60) {
                alerts.add(GradeAlert.builder()
                        .alertType("FAILING")
                        .severity("HIGH")
                        .courseId(course.getId())
                        .courseName(course.getCourseName())
                        .currentGrade(grade.getFinalPercentage())
                        .message(String.format("Failing grade in %s: %.1f%%", course.getCourseName(), grade.getFinalPercentage()))
                        .build());
            }
            // Warning alert (60-70%)
            else if (grade.getFinalPercentage() < 70) {
                alerts.add(GradeAlert.builder()
                        .alertType("WARNING")
                        .severity("MEDIUM")
                        .courseId(course.getId())
                        .courseName(course.getCourseName())
                        .currentGrade(grade.getFinalPercentage())
                        .message(String.format("Grade warning in %s: %.1f%%", course.getCourseName(), grade.getFinalPercentage()))
                        .build());
            }

            // Missing assignments alert
            if (grade.getMissingAssignments() > 0) {
                alerts.add(GradeAlert.builder()
                        .alertType("MISSING_ASSIGNMENTS")
                        .severity("MEDIUM")
                        .courseId(course.getId())
                        .courseName(course.getCourseName())
                        .message(String.format("%d missing assignment(s) in %s", grade.getMissingAssignments(), course.getCourseName()))
                        .build());
            }
        }

        return alerts;
    }

    /**
     * Calculate comprehensive GPA data for a student
     */
    public StudentGPAData calculateStudentGPA(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all course grades
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getStudents() != null && c.getStudents().contains(student))
                .collect(Collectors.toList());

        List<StudentCourseGrade> courseGrades = courses.stream()
                .map(c -> calculateCourseGrade(studentId, c.getId()))
                .collect(Collectors.toList());

        // Calculate weighted GPA
        double totalGpaPoints = 0.0;
        int totalCourses = 0;

        for (StudentCourseGrade grade : courseGrades) {
            if (grade.getGpaPoints() != null) {
                totalGpaPoints += grade.getGpaPoints();
                totalCourses++;
            }
        }

        double currentGPA = totalCourses > 0 ? totalGpaPoints / totalCourses : 0.0;

        return StudentGPAData.builder()
                .studentId(studentId)
                .currentGPA(currentGPA)
                .cumulativeGPA(currentGPA) // Same as current for now
                .totalCourses(totalCourses)
                .courseGrades(courseGrades)
                .build();
    }

    /**
     * Get academic standing for a student
     */
    public AcademicStanding getStudentAcademicStanding(Long studentId) {
        StudentGPAData gpaData = calculateStudentGPA(studentId);
        double gpa = gpaData.getCurrentGPA();

        String standing;
        String message;

        if (gpa >= 3.75) {
            standing = "HIGH_HONORS";
            message = "Student is on the High Honor Roll";
        } else if (gpa >= 3.5) {
            standing = "HONORS";
            message = "Student is on the Honor Roll";
        } else if (gpa >= 3.0) {
            standing = "GOOD_STANDING";
            message = "Student is in good academic standing";
        } else if (gpa >= 2.0) {
            standing = "ACADEMIC_WARNING";
            message = "Student is on academic warning - GPA below 3.0";
        } else {
            standing = "ACADEMIC_PROBATION";
            message = "Student is on academic probation - GPA below 2.0";
        }

        return AcademicStanding.builder()
                .studentId(studentId)
                .standing(standing)
                .gpa(gpa)
                .message(message)
                .failingCourses((int) gpaData.getCourseGrades().stream()
                        .filter(g -> g.getFinalPercentage() < 60)
                        .count())
                .build();
    }

    /**
     * Get student grade trends over a date range
     *
     * @param studentId Student ID
     * @param startDate Start date for trend analysis
     * @param endDate End date for trend analysis
     * @return Grade trends data
     */
    public GradeTrendsData getStudentGradeTrends(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving grade trends for student {} from {} to {}", studentId, startDate, endDate);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all assignments for this student within the date range
        List<AssignmentGrade> grades = gradeRepository.findAll().stream()
                .filter(g -> g.getStudent().getId().equals(studentId))
                .filter(g -> {
                    Assignment assignment = g.getAssignment();
                    if (assignment.getDueDate() == null) {
                        return false;
                    }
                    return !assignment.getDueDate().isBefore(startDate) &&
                           !assignment.getDueDate().isAfter(endDate);
                })
                .sorted(Comparator.comparing(g -> g.getAssignment().getDueDate()))
                .toList();

        // Group by course for per-course trends
        Map<Long, List<AssignmentGrade>> gradesByCourse = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getAssignment().getCourse().getId()));

        // Build course trend data
        List<CourseTrend> courseTrends = new ArrayList<>();
        for (Map.Entry<Long, List<AssignmentGrade>> entry : gradesByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<AssignmentGrade> courseGrades = entry.getValue();

            if (courseGrades.isEmpty()) {
                continue;
            }

            Course course = courseGrades.get(0).getAssignment().getCourse();

            // Calculate trend points (weekly or monthly based on date range)
            List<TrendPoint> trendPoints = calculateTrendPoints(courseGrades, startDate, endDate);

            // Calculate trend direction
            String trendDirection = calculateTrendDirection(trendPoints);

            // Calculate average change
            double averageChange = calculateAverageChange(trendPoints);

            courseTrends.add(CourseTrend.builder()
                    .courseId(courseId)
                    .courseName(course.getCourseName())
                    .trendPoints(trendPoints)
                    .trendDirection(trendDirection)
                    .averageChange(averageChange)
                    .currentAverage(trendPoints.isEmpty() ? 0.0 :
                            trendPoints.get(trendPoints.size() - 1).getAverage())
                    .assignmentCount(courseGrades.size())
                    .build());
        }

        // Calculate overall trend
        double overallAverage = courseTrends.stream()
                .mapToDouble(CourseTrend::getCurrentAverage)
                .average()
                .orElse(0.0);

        String overallTrend = courseTrends.stream()
                .map(CourseTrend::getTrendDirection)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("stable");

        log.info("Grade trends calculated for student {}: {} courses, overall trend: {}",
                studentId, courseTrends.size(), overallTrend);

        return GradeTrendsData.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .courseTrends(courseTrends)
                .overallAverage(overallAverage)
                .overallTrend(overallTrend)
                .totalAssignments(grades.size())
                .build();
    }

    /**
     * Calculate trend points for visualization
     */
    private List<TrendPoint> calculateTrendPoints(List<AssignmentGrade> grades,
                                                   LocalDate startDate, LocalDate endDate) {
        List<TrendPoint> points = new ArrayList<>();

        // Determine interval (weekly or monthly based on date range)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int intervalDays = daysBetween > 90 ? 30 : 7; // Monthly for >90 days, else weekly

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate intervalEnd = currentDate.plusDays(intervalDays);

            LocalDate finalCurrentDate = currentDate;
            LocalDate finalIntervalEnd = intervalEnd;

            List<AssignmentGrade> intervalGrades = grades.stream()
                    .filter(g -> {
                        LocalDate dueDate = g.getAssignment().getDueDate();
                        return !dueDate.isBefore(finalCurrentDate) &&
                               dueDate.isBefore(finalIntervalEnd);
                    })
                    .toList();

            if (!intervalGrades.isEmpty()) {
                double average = intervalGrades.stream()
                        .mapToDouble(AssignmentGrade::getPercentage)
                        .average()
                        .orElse(0.0);

                points.add(TrendPoint.builder()
                        .date(currentDate)
                        .average(average)
                        .assignmentCount(intervalGrades.size())
                        .build());
            }

            currentDate = intervalEnd;
        }

        return points;
    }

    /**
     * Calculate trend direction based on trend points
     */
    private String calculateTrendDirection(List<TrendPoint> points) {
        if (points.size() < 2) {
            return "stable";
        }

        // Compare first half to second half averages
        int midpoint = points.size() / 2;
        double firstHalfAvg = points.subList(0, midpoint).stream()
                .mapToDouble(TrendPoint::getAverage)
                .average()
                .orElse(0.0);

        double secondHalfAvg = points.subList(midpoint, points.size()).stream()
                .mapToDouble(TrendPoint::getAverage)
                .average()
                .orElse(0.0);

        double diff = secondHalfAvg - firstHalfAvg;

        if (diff > 2.0) {
            return "improving";
        } else if (diff < -2.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    /**
     * Calculate average change between trend points
     */
    private double calculateAverageChange(List<TrendPoint> points) {
        if (points.size() < 2) {
            return 0.0;
        }

        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            double change = points.get(i).getAverage() - points.get(i - 1).getAverage();
            changes.add(change);
        }

        return changes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    // ========================================================================
    // ADDITIONAL DTOs FOR API SUPPORT
    // ========================================================================

    @Data
    @Builder
    public static class StudentAllGrades {
        private Long studentId;
        private String studentName;
        private List<StudentCourseGrade> courseGrades;
        private Double overallAverage;
        private int totalCourses;
    }

    @Data
    @Builder
    public static class MissingAssignment {
        private Long assignmentId;
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private LocalDate dueDate;
        private Double maxPoints;
        private String categoryName;
        private long daysOverdue;
    }

    @Data
    @Builder
    public static class GradeAlert {
        private String alertType;
        private String severity;
        private Long courseId;
        private String courseName;
        private Double currentGrade;
        private String message;
    }

    @Data
    @Builder
    public static class StudentGPAData {
        private Long studentId;
        private Double currentGPA;
        private Double cumulativeGPA;
        private int totalCourses;
        private List<StudentCourseGrade> courseGrades;
    }

    @Data
    @Builder
    public static class AcademicStanding {
        private Long studentId;
        private String standing;
        private Double gpa;
        private String message;
        private int failingCourses;
    }

    @Data
    @Builder
    public static class GradeTrendsData {
        private Long studentId;
        private String studentName;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<CourseTrend> courseTrends;
        private Double overallAverage;
        private String overallTrend;
        private int totalAssignments;
    }

    @Data
    @Builder
    public static class CourseTrend {
        private Long courseId;
        private String courseName;
        private List<TrendPoint> trendPoints;
        private String trendDirection;
        private Double averageChange;
        private Double currentAverage;
        private int assignmentCount;
    }

    @Data
    @Builder
    public static class TrendPoint {
        private LocalDate date;
        private Double average;
        private int assignmentCount;
    }

    // ========================================================================
    // ADDITIONAL STUDENT GRADE API METHODS (Item 5)
    // ========================================================================

    /**
     * Get grade history for a student in a specific course
     * Shows chronological grade progression
     */
    public CourseGradeHistory getStudentCourseGradeHistory(Long studentId, Long courseId) {
        log.info("Retrieving grade history for student {} in course {}", studentId, courseId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Get all grades for this student in this course
        List<AssignmentGrade> grades = gradeRepository.findForGradebookCalculation(studentId, courseId);

        // Sort by date
        List<AssignmentGrade> sortedGrades = grades.stream()
                .filter(g -> g.getGradedDate() != null || g.getAssignment().getDueDate() != null)
                .sorted(Comparator.comparing(g -> {
                    LocalDate date = g.getGradedDate() != null ? g.getGradedDate() : g.getAssignment().getDueDate();
                    return date != null ? date : LocalDate.now();
                }))
                .toList();

        // Build history entries with running grade calculation
        List<GradeHistoryEntry> historyEntries = new ArrayList<>();
        double runningTotal = 0;
        int runningCount = 0;

        for (AssignmentGrade grade : sortedGrades) {
            if (grade.getScore() != null) {
                double percentage = grade.getPercentage();
                runningTotal += percentage;
                runningCount++;
                double runningAverage = runningCount > 0 ? runningTotal / runningCount : 0;

                historyEntries.add(GradeHistoryEntry.builder()
                        .assignmentId(grade.getAssignment().getId())
                        .assignmentTitle(grade.getAssignment().getTitle())
                        .categoryName(grade.getAssignment().getCategory() != null ?
                                grade.getAssignment().getCategory().getName() : "Uncategorized")
                        .dueDate(grade.getAssignment().getDueDate())
                        .gradedDate(grade.getGradedDate())
                        .score(grade.getScore())
                        .maxPoints(grade.getAssignment().getMaxPoints())
                        .percentage(percentage)
                        .letterGrade(percentageToLetterGrade(percentage))
                        .runningCourseGrade(runningAverage)
                        .comments(grade.getComments())
                        .status(grade.getStatus() != null ? grade.getStatus().name() : "GRADED")
                        .build());
            }
        }

        // Calculate trend
        String trend = "stable";
        if (historyEntries.size() >= 3) {
            int midpoint = historyEntries.size() / 2;
            double firstHalf = historyEntries.subList(0, midpoint).stream()
                    .mapToDouble(GradeHistoryEntry::getPercentage)
                    .average().orElse(0);
            double secondHalf = historyEntries.subList(midpoint, historyEntries.size()).stream()
                    .mapToDouble(GradeHistoryEntry::getPercentage)
                    .average().orElse(0);
            if (secondHalf - firstHalf > 3) trend = "improving";
            else if (firstHalf - secondHalf > 3) trend = "declining";
        }

        StudentCourseGrade currentGrade = calculateCourseGrade(studentId, courseId);

        return CourseGradeHistory.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .courseId(courseId)
                .courseName(course.getCourseName())
                .gradeHistory(historyEntries)
                .currentGrade(currentGrade.getFinalPercentage())
                .currentLetterGrade(currentGrade.getLetterGrade())
                .trend(trend)
                .totalAssignments(historyEntries.size())
                .build();
    }

    /**
     * Generate progress report for a student
     */
    public ProgressReport generateProgressReport(Long studentId) {
        log.info("Generating progress report for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get all grades data
        StudentAllGrades allGrades = getStudentAllGrades(studentId);
        StudentGPAData gpaData = calculateStudentGPA(studentId);
        AcademicStanding standing = getStudentAcademicStanding(studentId);
        List<MissingAssignment> missingAssignments = getStudentMissingAssignments(studentId);
        List<GradeAlert> alerts = getStudentGradeAlerts(studentId);

        // Build course progress details
        List<CourseProgress> courseProgressList = new ArrayList<>();
        for (StudentCourseGrade courseGrade : allGrades.getCourseGrades()) {
            Course course = courseRepository.findById(courseGrade.getCourseId()).orElse(null);
            if (course == null) continue;

            String status;
            if (courseGrade.getFinalPercentage() >= 90) status = "EXCELLENT";
            else if (courseGrade.getFinalPercentage() >= 80) status = "GOOD";
            else if (courseGrade.getFinalPercentage() >= 70) status = "SATISFACTORY";
            else if (courseGrade.getFinalPercentage() >= 60) status = "NEEDS_IMPROVEMENT";
            else status = "AT_RISK";

            courseProgressList.add(CourseProgress.builder()
                    .courseId(course.getId())
                    .courseName(course.getCourseName())
                    .currentGrade(courseGrade.getFinalPercentage())
                    .letterGrade(courseGrade.getLetterGrade())
                    .status(status)
                    .totalAssignments(courseGrade.getTotalAssignments())
                    .gradedAssignments(courseGrade.getGradedAssignments())
                    .missingAssignments(courseGrade.getMissingAssignments())
                    .categoryBreakdown(courseGrade.getCategoryGrades())
                    .build());
        }

        return ProgressReport.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .gradeLevel(student.getGradeLevel())
                .generatedDate(LocalDate.now())
                .reportPeriod("Current Term")
                .overallGPA(gpaData.getCurrentGPA())
                .academicStanding(standing.getStanding())
                .courseProgress(courseProgressList)
                .totalMissingAssignments(missingAssignments.size())
                .missingAssignmentDetails(missingAssignments)
                .alerts(alerts)
                .strengths(identifyStrengths(courseProgressList))
                .areasForImprovement(identifyImprovementAreas(courseProgressList))
                .build();
    }

    /**
     * Generate report card for a student
     */
    public ReportCard generateReportCard(Long studentId, Long termId) {
        log.info("Generating report card for student {} term {}", studentId, termId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        StudentGPAData gpaData = calculateStudentGPA(studentId);
        AcademicStanding standing = getStudentAcademicStanding(studentId);
        StudentAllGrades allGrades = getStudentAllGrades(studentId);

        // Build course grade entries for report card
        List<ReportCardEntry> entries = new ArrayList<>();
        for (StudentCourseGrade courseGrade : allGrades.getCourseGrades()) {
            Course course = courseRepository.findById(courseGrade.getCourseId()).orElse(null);
            if (course == null) continue;

            entries.add(ReportCardEntry.builder()
                    .courseId(course.getId())
                    .courseName(course.getCourseName())
                    .courseCode(course.getCourseCode())
                    .credits(course.getCredits())
                    .finalGrade(courseGrade.getFinalPercentage())
                    .letterGrade(courseGrade.getLetterGrade())
                    .gpaPoints(courseGrade.getGpaPoints())
                    .teacher(course.getTeacher() != null ? course.getTeacher().getFullName() : "N/A")
                    .build());
        }

        // Calculate credits
        double totalCredits = entries.stream()
                .filter(e -> e.getFinalGrade() >= 60)
                .mapToDouble(ReportCardEntry::getCredits)
                .sum();

        // Honor roll determination
        String honorRoll = null;
        if (gpaData.getCurrentGPA() >= 3.75) honorRoll = "HIGH_HONORS";
        else if (gpaData.getCurrentGPA() >= 3.5) honorRoll = "HONORS";
        else if (gpaData.getCurrentGPA() >= 3.0) honorRoll = "MERIT";

        return ReportCard.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .studentNumber(student.getStudentId())
                .gradeLevel(student.getGradeLevel())
                .termId(termId)
                .termName(termId != null ? "Term " + termId : "Current Term")
                .generatedDate(LocalDate.now())
                .courseGrades(entries)
                .termGPA(gpaData.getCurrentGPA())
                .cumulativeGPA(gpaData.getCumulativeGPA())
                .creditsEarned(totalCredits)
                .academicStanding(standing.getStanding())
                .honorRoll(honorRoll)
                .build();
    }

    /**
     * Generate report card for current term
     */
    public ReportCard generateCurrentReportCard(Long studentId) {
        return generateReportCard(studentId, null);
    }

    /**
     * Calculate what-if grade scenarios
     */
    public WhatIfResult calculateWhatIfGrade(Long studentId, Long courseId,
                                              List<HypotheticalAssignment> hypotheticalAssignments) {
        log.info("Calculating what-if grade for student {} in course {}", studentId, courseId);

        // Get current grade
        StudentCourseGrade currentGrade = calculateCourseGrade(studentId, courseId);
        double originalGrade = currentGrade.getFinalPercentage();

        // Apply hypothetical scores
        Map<Long, Double> hypotheticalScores = hypotheticalAssignments.stream()
                .collect(Collectors.toMap(
                        HypotheticalAssignment::getAssignmentId,
                        HypotheticalAssignment::getHypotheticalScore
                ));

        // Get all grades and categories
        List<GradingCategory> categories = getCategoriesForCourse(courseId);
        List<AssignmentGrade> grades = gradeRepository.findForGradebookCalculation(studentId, courseId);

        // Calculate with hypothetical scores
        Map<Long, List<AssignmentGrade>> gradesByCategory = grades.stream()
                .filter(AssignmentGrade::countsInGrade)
                .collect(Collectors.groupingBy(g -> g.getAssignment().getCategory().getId()));

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (GradingCategory category : categories) {
            List<AssignmentGrade> categoryGrades = gradesByCategory.getOrDefault(category.getId(), List.of());
            if (categoryGrades.isEmpty()) continue;

            // Apply hypothetical scores
            List<Double> percentages = new ArrayList<>();
            for (AssignmentGrade grade : categoryGrades) {
                Double hypotheticalScore = hypotheticalScores.get(grade.getAssignment().getId());
                if (hypotheticalScore != null) {
                    double max = grade.getAssignment().getMaxPoints();
                    percentages.add(max > 0 ? (hypotheticalScore / max) * 100.0 : 0.0);
                } else if (grade.getAdjustedScore() != null) {
                    double max = grade.getAssignment().getMaxPoints();
                    percentages.add(max > 0 ? (grade.getAdjustedScore() / max) * 100.0 : 0.0);
                }
            }

            if (!percentages.isEmpty()) {
                percentages.sort(Double::compareTo);
                int toDrop = Math.min(category.getDropLowest() != null ? category.getDropLowest() : 0,
                                      Math.max(0, percentages.size() - 1));
                List<Double> remaining = percentages.subList(toDrop, percentages.size());
                double categoryAvg = remaining.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                totalWeightedScore += (categoryAvg * category.getWeight() / 100.0);
                totalWeight += category.getWeight();
            }
        }

        double hypotheticalGrade = totalWeight > 0 ? (totalWeightedScore / totalWeight) * 100.0 : 0.0;
        double gradeChange = hypotheticalGrade - originalGrade;

        return WhatIfResult.builder()
                .studentId(studentId)
                .courseId(courseId)
                .originalGrade(originalGrade)
                .originalLetterGrade(percentageToLetterGrade(originalGrade))
                .hypotheticalGrade(hypotheticalGrade)
                .hypotheticalLetterGrade(percentageToLetterGrade(hypotheticalGrade))
                .gradeChange(gradeChange)
                .changesApplied(hypotheticalAssignments.size())
                .wouldImprove(gradeChange > 0)
                .letterGradeChanged(!percentageToLetterGrade(originalGrade)
                        .equals(percentageToLetterGrade(hypotheticalGrade)))
                .build();
    }

    /**
     * Get specific assignment grade with detailed feedback
     */
    public AssignmentGradeDetail getStudentAssignmentGrade(Long studentId, Long assignmentId) {
        log.info("Retrieving assignment grade detail for student {} assignment {}", studentId, assignmentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        Optional<AssignmentGrade> gradeOpt = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId);

        AssignmentGradeDetail.AssignmentGradeDetailBuilder builder = AssignmentGradeDetail.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .assignmentId(assignmentId)
                .assignmentTitle(assignment.getTitle())
                .courseId(assignment.getCourse().getId())
                .courseName(assignment.getCourse().getCourseName())
                .categoryName(assignment.getCategory() != null ? assignment.getCategory().getName() : "Uncategorized")
                .dueDate(assignment.getDueDate())
                .maxPoints(assignment.getMaxPoints());

        if (gradeOpt.isPresent()) {
            AssignmentGrade grade = gradeOpt.get();
            builder.score(grade.getScore())
                    .percentage(grade.getPercentage())
                    .letterGrade(percentageToLetterGrade(grade.getPercentage()))
                    .submittedDate(grade.getSubmittedDate())
                    .gradedDate(grade.getGradedDate())
                    .comments(grade.getComments())
                    .status(grade.getStatus() != null ? grade.getStatus().name() : "NOT_GRADED")
                    .latePenalty(grade.getLatePenalty())
                    .isExcused(grade.getExcused() != null && grade.getExcused())
                    .excuseReason(grade.getExcuseReason());
        } else {
            // Assignment exists but no grade yet
            boolean isPastDue = assignment.getDueDate() != null &&
                               assignment.getDueDate().isBefore(LocalDate.now());

            builder.status(isPastDue ? "MISSING" : "NOT_SUBMITTED");
        }

        return builder.build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private List<String> identifyStrengths(List<CourseProgress> courses) {
        return courses.stream()
                .filter(c -> c.getCurrentGrade() >= 85)
                .map(c -> String.format("%s: %.1f%% (%s)",
                        c.getCourseName(), c.getCurrentGrade(), c.getLetterGrade()))
                .limit(3)
                .toList();
    }

    private List<String> identifyImprovementAreas(List<CourseProgress> courses) {
        return courses.stream()
                .filter(c -> c.getCurrentGrade() < 75 || c.getMissingAssignments() > 0)
                .map(c -> {
                    if (c.getMissingAssignments() > 0) {
                        return String.format("%s: %d missing assignment(s)", c.getCourseName(), c.getMissingAssignments());
                    }
                    return String.format("%s: Grade below target (%.1f%%)", c.getCourseName(), c.getCurrentGrade());
                })
                .limit(3)
                .toList();
    }

    // ========================================================================
    // ADDITIONAL DTOs FOR ITEM 5
    // ========================================================================

    @Data
    @Builder
    public static class CourseGradeHistory {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private String courseName;
        private List<GradeHistoryEntry> gradeHistory;
        private Double currentGrade;
        private String currentLetterGrade;
        private String trend;
        private int totalAssignments;
    }

    @Data
    @Builder
    public static class GradeHistoryEntry {
        private Long assignmentId;
        private String assignmentTitle;
        private String categoryName;
        private LocalDate dueDate;
        private LocalDate gradedDate;
        private Double score;
        private Double maxPoints;
        private Double percentage;
        private String letterGrade;
        private Double runningCourseGrade;
        private String comments;
        private String status;
    }

    @Data
    @Builder
    public static class ProgressReport {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private LocalDate generatedDate;
        private String reportPeriod;
        private Double overallGPA;
        private String academicStanding;
        private List<CourseProgress> courseProgress;
        private int totalMissingAssignments;
        private List<MissingAssignment> missingAssignmentDetails;
        private List<GradeAlert> alerts;
        private List<String> strengths;
        private List<String> areasForImprovement;
    }

    @Data
    @Builder
    public static class CourseProgress {
        private Long courseId;
        private String courseName;
        private Double currentGrade;
        private String letterGrade;
        private String status;
        private int totalAssignments;
        private int gradedAssignments;
        private int missingAssignments;
        private List<CategoryGrade> categoryBreakdown;
    }

    @Data
    @Builder
    public static class ReportCard {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private String gradeLevel;
        private Long termId;
        private String termName;
        private LocalDate generatedDate;
        private List<ReportCardEntry> courseGrades;
        private Double termGPA;
        private Double cumulativeGPA;
        private Double creditsEarned;
        private String academicStanding;
        private String honorRoll;
    }

    @Data
    @Builder
    public static class ReportCardEntry {
        private Long courseId;
        private String courseName;
        private String courseCode;
        private Double credits;
        private Double finalGrade;
        private String letterGrade;
        private Double gpaPoints;
        private String teacher;
    }

    @Data
    @Builder
    public static class WhatIfResult {
        private Long studentId;
        private Long courseId;
        private Double originalGrade;
        private String originalLetterGrade;
        private Double hypotheticalGrade;
        private String hypotheticalLetterGrade;
        private Double gradeChange;
        private int changesApplied;
        private boolean wouldImprove;
        private boolean letterGradeChanged;
    }

    @Data
    @Builder
    public static class HypotheticalAssignment {
        private Long assignmentId;
        private Double hypotheticalScore;
    }

    @Data
    @Builder
    public static class AssignmentGradeDetail {
        private Long studentId;
        private String studentName;
        private Long assignmentId;
        private String assignmentTitle;
        private Long courseId;
        private String courseName;
        private String categoryName;
        private LocalDate dueDate;
        private Double maxPoints;
        private Double score;
        private Double percentage;
        private String letterGrade;
        private LocalDate submittedDate;
        private LocalDate gradedDate;
        private String comments;
        private String status;
        private Double latePenalty;
        private boolean isExcused;
        private String excuseReason;
    }
}
