package com.heronix.service.impl;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.AssignmentGrade.GradeStatus;
import com.heronix.model.domain.GradingCategory;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.AssignmentGradeRepository;
import com.heronix.repository.AssignmentRepository;
import com.heronix.repository.GradingCategoryRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AssignmentGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AssignmentGradeService
 * Handles assignment-level grading with weighted categories and drop-lowest functionality
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentGradeServiceImpl implements AssignmentGradeService {

    private final AssignmentGradeRepository gradeRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradingCategoryRepository categoryRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // GRADE ENTRY AND MANAGEMENT
    // ========================================================================

    @Override
    @Transactional
    public AssignmentGrade enterGrade(Long studentId, Long assignmentId, Double score, Teacher teacher) {
        return enterGrade(studentId, assignmentId, score, null, teacher);
    }

    @Override
    @Transactional
    public AssignmentGrade enterGrade(Long studentId, Long assignmentId, Double score, String comments, Teacher teacher) {
        log.info("Entering grade for student ID: {} on assignment ID: {}, score: {}",
                studentId, assignmentId, score);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        // Find existing grade or create new one
        AssignmentGrade grade = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
                .orElse(AssignmentGrade.builder()
                        .student(student)
                        .assignment(assignment)
                        .submittedDate(LocalDate.now())
                        .excused(false)
                        .build());

        // Mark as graded
        grade.markGraded(score, teacher);

        if (comments != null) {
            grade.setComments(comments);
        }

        AssignmentGrade saved = gradeRepository.save(grade);
        log.info("Grade entered successfully: {} for {} on {}",
                saved.getLetterGrade(), student.getFullName(), assignment.getTitle());

        return saved;
    }

    @Override
    @Transactional
    public AssignmentGrade updateGrade(Long gradeId, Double score, String comments) {
        log.info("Updating grade ID: {}, new score: {}", gradeId, score);

        AssignmentGrade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        grade.setScore(score);
        if (comments != null) {
            grade.setComments(comments);
        }
        grade.setUpdatedAt(LocalDateTime.now());

        AssignmentGrade saved = gradeRepository.save(grade);
        log.info("Grade updated successfully: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public void markExcused(Long gradeId, String reason) {
        log.info("Marking grade ID: {} as excused, reason: {}", gradeId, reason);

        AssignmentGrade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        grade.markExcused(reason);
        gradeRepository.save(grade);

        log.info("Grade ID: {} marked as excused", gradeId);
    }

    @Override
    @Transactional
    public void markMissing(Long gradeId) {
        log.info("Marking grade ID: {} as missing", gradeId);

        AssignmentGrade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        grade.markMissing();
        gradeRepository.save(grade);

        log.info("Grade ID: {} marked as missing", gradeId);
    }

    @Override
    @Transactional
    public void markLate(Long gradeId) {
        log.info("Marking grade ID: {} as late", gradeId);

        AssignmentGrade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + gradeId));

        grade.setStatus(GradeStatus.LATE);
        gradeRepository.save(grade);

        log.info("Grade ID: {} marked as late", gradeId);
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        log.info("Deleting grade ID: {}", gradeId);
        gradeRepository.deleteById(gradeId);
        log.info("Grade ID: {} deleted successfully", gradeId);
    }

    // ========================================================================
    // QUERIES
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public AssignmentGrade getGrade(Long studentId, Long assignmentId) {
        log.debug("Getting grade for student ID: {} on assignment ID: {}", studentId, assignmentId);
        return gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentGrade> getStudentGrades(Long studentId, Long courseId) {
        log.debug("Getting all grades for student ID: {} in course ID: {}", studentId, courseId);
        return gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentGrade> getAssignmentGrades(Long assignmentId) {
        log.debug("Getting all grades for assignment ID: {}", assignmentId);
        return gradeRepository.findByAssignmentIdOrderByStudentLastName(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentGrade> getMissingAssignments(Long studentId, Long courseId) {
        log.debug("Getting missing assignments for student ID: {} in course ID: {}", studentId, courseId);
        return gradeRepository.findMissingByStudentAndCourse(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentGrade> getGradesForCalculation(Long studentId, Long courseId) {
        log.debug("Getting grades for calculation for student ID: {} in course ID: {}", studentId, courseId);
        return gradeRepository.findForGradebookCalculation(studentId, courseId);
    }

    // ========================================================================
    // GRADE CALCULATION
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public Double calculateCourseGrade(Long studentId, Long courseId) {
        log.debug("Calculating course grade for student ID: {} in course ID: {}", studentId, courseId);

        // Get all active categories for the course
        List<GradingCategory> categories = categoryRepository.findByCourseIdAndActiveTrueOrderByDisplayOrder(courseId);

        if (categories.isEmpty()) {
            log.warn("No active grading categories found for course ID: {}", courseId);
            return null;
        }

        // Validate that weights sum to 100%
        double totalWeight = categories.stream()
                .mapToDouble(GradingCategory::getWeight)
                .sum();

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            log.warn("Category weights do not sum to 100% for course ID: {}. Total: {}", courseId, totalWeight);
        }

        // Calculate weighted average
        double weightedSum = 0.0;
        double totalWeightUsed = 0.0;

        for (GradingCategory category : categories) {
            Double categoryGrade = calculateCategoryGrade(studentId, category.getId());
            if (categoryGrade != null) {
                weightedSum += categoryGrade * (category.getWeight() / 100.0);
                totalWeightUsed += category.getWeight();
            }
        }

        if (totalWeightUsed == 0.0) {
            return null;
        }

        // Normalize if not all categories have grades
        double courseGrade = (weightedSum / totalWeightUsed) * 100.0;

        log.debug("Course grade calculated: {} for student ID: {} in course ID: {}",
                courseGrade, studentId, courseId);

        return Math.round(courseGrade * 100.0) / 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateCategoryGrade(Long studentId, Long categoryId) {
        log.debug("Calculating category grade for student ID: {} in category ID: {}", studentId, categoryId);

        GradingCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        // Get all grades for this student in this category
        List<AssignmentGrade> grades = gradeRepository.findByStudentIdAndCategoryId(studentId, categoryId);

        if (grades.isEmpty()) {
            return null;
        }

        // Filter to only graded assignments (exclude missing/excused)
        List<Double> percentages = grades.stream()
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .map(AssignmentGrade::getPercentage)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (percentages.isEmpty()) {
            return null;
        }

        // Apply drop-lowest logic
        Integer dropLowest = category.getDropLowest();
        if (dropLowest != null && dropLowest > 0 && percentages.size() > dropLowest) {
            // Remove the lowest N scores
            for (int i = 0; i < dropLowest && !percentages.isEmpty(); i++) {
                percentages.remove(0); // Remove from beginning (sorted ascending)
            }
        }

        if (percentages.isEmpty()) {
            return null;
        }

        // Calculate average
        double average = percentages.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        log.debug("Category grade calculated: {} for student ID: {} in category: {}",
                average, studentId, category.getName());

        return Math.round(average * 100.0) / 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getCategoryBreakdown(Long studentId, Long courseId) {
        log.debug("Getting category breakdown for student ID: {} in course ID: {}", studentId, courseId);

        List<GradingCategory> categories = categoryRepository.findByCourseIdAndActiveTrueOrderByDisplayOrder(courseId);

        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (GradingCategory category : categories) {
            Double categoryGrade = calculateCategoryGrade(studentId, category.getId());
            breakdown.put(category.getName(), categoryGrade);
        }

        return breakdown;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateWhatIfGrade(Long studentId, Long courseId, Long categoryId, Double hypotheticalScore) {
        log.debug("Calculating what-if grade for student ID: {} with hypothetical score: {} in category ID: {}",
                studentId, hypotheticalScore, categoryId);

        // This would require creating a temporary grade object and recalculating
        // For now, return null (to be implemented later if needed)
        return null;
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public Double getClassAverage(Long assignmentId) {
        log.debug("Calculating class average for assignment ID: {}", assignmentId);
        Double average = gradeRepository.getClassAverage(assignmentId);
        return average != null ? Math.round(average * 100.0) / 100.0 : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getAssignmentStatistics(Long assignmentId) {
        log.debug("Getting statistics for assignment ID: {}", assignmentId);

        List<AssignmentGrade> grades = gradeRepository.findByAssignmentIdOrderByStudentLastName(assignmentId);

        List<Double> scores = grades.stream()
                .filter(g -> g.getScore() != null && !Boolean.TRUE.equals(g.getExcused()))
                .map(AssignmentGrade::getPercentage)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> stats = new LinkedHashMap<>();
        stats.put("min", scores.get(0));
        stats.put("max", scores.get(scores.size() - 1));
        stats.put("average", scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        stats.put("median", scores.size() % 2 == 0
                ? (scores.get(scores.size() / 2 - 1) + scores.get(scores.size() / 2)) / 2.0
                : scores.get(scores.size() / 2));
        stats.put("count", (double) scores.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public long countGradedAssignments(Long courseId) {
        log.debug("Counting graded assignments for course ID: {}", courseId);
        return assignmentRepository.countGradedAssignmentsForCourse(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMissingAssignments(Long studentId, Long courseId) {
        log.debug("Counting missing assignments for student ID: {} in course ID: {}", studentId, courseId);
        return gradeRepository.countMissingByStudentAndCourse(studentId, courseId);
    }
}
