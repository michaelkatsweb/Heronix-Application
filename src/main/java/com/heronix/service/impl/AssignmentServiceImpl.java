package com.heronix.service.impl;

import com.heronix.model.domain.Assignment;
import com.heronix.repository.AssignmentRepository;
import com.heronix.repository.AssignmentGradeRepository;
import com.heronix.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Assignment operations
 * Location: src/main/java/com/heronix/service/impl/AssignmentServiceImpl.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentGradeRepository assignmentGradeRepository;

    @Override
    @Transactional
    public Assignment createAssignment(Assignment assignment) {
        log.info("Creating new assignment: {} for course ID: {}",
                assignment.getTitle(), assignment.getCourse().getId());

        Assignment saved = assignmentRepository.save(assignment);

        log.info("Assignment created with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Assignment updateAssignment(Long id, Assignment assignment) {
        log.info("Updating assignment ID: {}", id);

        Assignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + id));

        // Update fields
        existing.setTitle(assignment.getTitle());
        existing.setDescription(assignment.getDescription());
        existing.setMaxPoints(assignment.getMaxPoints());
        existing.setDueDate(assignment.getDueDate());
        existing.setAssignedDate(assignment.getAssignedDate());
        existing.setTerm(assignment.getTerm());
        existing.setIsExtraCredit(assignment.getIsExtraCredit());
        existing.setCountInGrade(assignment.getCountInGrade());
        existing.setLatePenaltyPerDay(assignment.getLatePenaltyPerDay());
        existing.setMaxLatePenalty(assignment.getMaxLatePenalty());
        existing.setDisplayOrder(assignment.getDisplayOrder());

        Assignment updated = assignmentRepository.save(existing);

        log.info("Assignment ID: {} updated successfully", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        log.info("Deleting assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + id));

        // Delete all associated grades first
        log.info("Deleting {} grades for assignment ID: {}",
                assignment.getGrades().size(), id);
        assignmentGradeRepository.deleteByAssignmentId(id);

        // Delete assignment
        assignmentRepository.delete(assignment);

        log.info("Assignment ID: {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long id) {
        log.debug("Fetching assignment ID: {}", id);
        return assignmentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        log.debug("Fetching all assignments for course ID: {}", courseId);
        return assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByTerm(Long courseId, String term) {
        log.debug("Fetching assignments for course ID: {} and term: {}", courseId, term);
        return assignmentRepository.findByCourseIdAndTermOrderByDueDateDesc(courseId, term);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getPublishedAssignments(Long courseId) {
        log.debug("Fetching published assignments for course ID: {}", courseId);
        return assignmentRepository.findByCourseIdAndPublishedTrueOrderByDueDateDesc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getUpcomingAssignments(Long courseId, int days) {
        log.debug("Fetching upcoming assignments for course ID: {} (next {} days)", courseId, days);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        return assignmentRepository.findUpcomingAssignments(courseId, today, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getPastDueAssignments(Long courseId) {
        log.debug("Fetching past due assignments for course ID: {}", courseId);

        LocalDate today = LocalDate.now();
        return assignmentRepository.findPastDueAssignments(courseId, today);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsWithGrades(Long courseId) {
        log.debug("Fetching assignments with grades for course ID: {}", courseId);
        return assignmentRepository.findByCourseIdWithGrades(courseId);
    }

    @Override
    @Transactional
    public void publishAssignment(Long id) {
        log.info("Publishing assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + id));

        assignment.setPublished(true);
        assignmentRepository.save(assignment);

        log.info("Assignment ID: {} published successfully", id);
    }

    @Override
    @Transactional
    public void unpublishAssignment(Long id) {
        log.info("Unpublishing assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + id));

        assignment.setPublished(false);
        assignmentRepository.save(assignment);

        log.info("Assignment ID: {} unpublished successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getClassAverage(Long assignmentId) {
        log.debug("Calculating class average for assignment ID: {}", assignmentId);

        Double average = assignmentRepository.getAverageScore(assignmentId);

        if (average == null) {
            log.debug("No grades found for assignment ID: {}", assignmentId);
            return null;
        }

        // Get assignment to convert to percentage
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null || assignment.getMaxPoints() == null || assignment.getMaxPoints() == 0) {
            return average;
        }

        Double percentage = (average / assignment.getMaxPoints()) * 100;
        log.debug("Class average for assignment ID: {}: {:.2f}%", assignmentId, percentage);

        return percentage;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignmentsByCourse(Long courseId) {
        log.debug("Counting assignments for course ID: {}", courseId);
        return assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countGradedAssignments(Long courseId) {
        log.debug("Counting graded assignments for course ID: {}", courseId);
        return assignmentRepository.countGradedAssignmentsForCourse(courseId);
    }
}
