package com.heronix.service;

import com.heronix.model.domain.IEPGoal;
import com.heronix.model.domain.IEPGoal.GoalStatus;
import com.heronix.repository.IEPGoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing IEP goals and objectives
 * Handles goal creation, progress monitoring, and mastery tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class IEPGoalService {

    @Autowired
    private IEPGoalRepository goalRepository;

    // CRUD Operations
    public IEPGoal createGoal(IEPGoal goal) {
        log.info("Creating IEP goal for IEP {}", goal.getIep().getId());
        return goalRepository.save(goal);
    }

    public IEPGoal getGoalById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IEP Goal not found: " + id));
    }

    public IEPGoal updateGoal(IEPGoal goal) {
        log.info("Updating IEP goal {}", goal.getId());
        return goalRepository.save(goal);
    }

    public void deleteGoal(Long id) {
        log.info("Deleting IEP goal {}", id);
        goalRepository.deleteById(id);
    }

    // Query Operations
    public List<IEPGoal> getGoalsByIep(Long iepId) {
        return goalRepository.findByIepIdOrderByGoalNumberAsc(iepId);
    }

    public List<IEPGoal> getGoalsByStudent(Long studentId) {
        return goalRepository.findByStudentId(studentId);
    }

    public List<IEPGoal> getGoalsByStatus(GoalStatus status) {
        return goalRepository.findByStatusOrderByGoalDomainAsc(status);
    }

    // Progress Monitoring
    public IEPGoal updateProgress(Long goalId, Double currentScore, Integer progressPercentage, String notes) {
        log.info("Updating progress for goal {}", goalId);
        IEPGoal goal = getGoalById(goalId);
        goal.setCurrentPerformanceLevel(String.valueOf(currentScore));
        goal.setProgressPercentage(progressPercentage);
        goal.setLastProgressCheckDate(LocalDate.now());

        if (notes != null) {
            String existingNotes = goal.getProgressNotes() != null ? goal.getProgressNotes() + "\n\n" : "";
            goal.setProgressNotes(existingNotes + LocalDate.now() + ": " + notes);
        }

        return goalRepository.save(goal);
    }

    public IEPGoal markAsMastered(Long goalId) {
        log.info("Marking goal {} as mastered", goalId);
        IEPGoal goal = getGoalById(goalId);
        goal.setStatus(GoalStatus.MASTERED);
        goal.setMasteryDate(LocalDate.now());
        goal.setProgressPercentage(100);
        return goalRepository.save(goal);
    }

    public IEPGoal updateStatus(Long goalId, GoalStatus status) {
        log.info("Updating goal {} status to {}", goalId, status);
        IEPGoal goal = getGoalById(goalId);
        goal.setStatus(status);

        if (status == GoalStatus.MASTERED && goal.getMasteryDate() == null) {
            goal.setMasteryDate(LocalDate.now());
            goal.setProgressPercentage(100);
        }

        return goalRepository.save(goal);
    }

    public List<IEPGoal> getGoalsNeedingAttention() {
        return goalRepository.findGoalsNeedingAttention();
    }

    public List<IEPGoal> getGoalsOverdueForProgressCheck(int daysWithoutCheck) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysWithoutCheck);
        return goalRepository.findOverdueForProgressCheck(cutoffDate);
    }

    // Statistics
    public Double getAverageProgress(Long iepId) {
        return goalRepository.getAverageProgressByIep(iepId);
    }

    public Long getMasteredGoalCount(Long iepId) {
        return goalRepository.countMasteredGoalsByIep(iepId);
    }

    public Long getTotalGoalCount(Long iepId) {
        return goalRepository.countTotalGoalsByIep(iepId);
    }

    public Double getMasteryPercentage(Long iepId) {
        Long total = getTotalGoalCount(iepId);
        if (total == 0) return 0.0;
        Long mastered = getMasteredGoalCount(iepId);
        return (double) mastered / total * 100.0;
    }

    // Responsible Staff
    public List<IEPGoal> getActiveGoalsByStaff(Long staffId) {
        return goalRepository.findActiveGoalsByStaff(staffId);
    }

    // Review
    public List<IEPGoal> getGoalsDueForReview() {
        return goalRepository.findGoalsDueForReview(LocalDate.now());
    }
}
