package com.heronix.service;

import com.heronix.model.domain.TeacherEvaluation;
import com.heronix.repository.TeacherEvaluationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing teacher evaluations
 * Handles observations, rubric scoring, and performance assessments
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class TeacherEvaluationService {

    @Autowired
    private TeacherEvaluationRepository evaluationRepository;

    // CRUD
    public TeacherEvaluation createEvaluation(TeacherEvaluation evaluation) {
        log.info("Creating evaluation for teacher {} by evaluator {}",
                evaluation.getTeacher().getId(), evaluation.getEvaluator().getId());
        return evaluationRepository.save(evaluation);
    }

    public TeacherEvaluation getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found: " + id));
    }

    public TeacherEvaluation updateEvaluation(TeacherEvaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }

    public void deleteEvaluation(Long id) {
        evaluationRepository.deleteById(id);
    }

    // Queries
    public List<TeacherEvaluation> getEvaluationsByTeacher(Long teacherId) {
        return evaluationRepository.findByTeacherIdOrderByEvaluationDateDesc(teacherId);
    }

    public List<TeacherEvaluation> getEvaluationsBySchoolYear(String schoolYear) {
        return evaluationRepository.findBySchoolYearOrderByEvaluationDateDesc(schoolYear);
    }

    public List<TeacherEvaluation> getScheduledForDate(LocalDate date) {
        return evaluationRepository.findScheduledForDate(date);
    }

    public List<TeacherEvaluation> getOverdueEvaluations() {
        return evaluationRepository.findOverdue(LocalDate.now());
    }

    public List<TeacherEvaluation> getPendingSignature() {
        return evaluationRepository.findPendingSignature();
    }

    public List<TeacherEvaluation> getRequiringImprovementPlan() {
        return evaluationRepository.findRequiringImprovementPlan();
    }

    // Conference Management
    public TeacherEvaluation conductPreConference(Long evaluationId, String notes) {
        log.info("Conducting pre-conference for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setPreConferenceHeld(true);
        eval.setPreConferenceDate(LocalDate.now());
        eval.setPreConferenceNotes(notes);
        eval.setStatus(TeacherEvaluation.EvaluationStatus.PRE_CONFERENCE_COMPLETE);
        return evaluationRepository.save(eval);
    }

    public TeacherEvaluation conductObservation(Long evaluationId, String notes) {
        log.info("Conducting observation for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setObservationNotes(notes);
        eval.setEvaluationDate(LocalDate.now());
        eval.setStatus(TeacherEvaluation.EvaluationStatus.OBSERVATION_COMPLETE);
        return evaluationRepository.save(eval);
    }

    public TeacherEvaluation conductPostConference(Long evaluationId, String notes) {
        log.info("Conducting post-conference for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setPostConferenceHeld(true);
        eval.setPostConferenceDate(LocalDate.now());
        eval.setPostConferenceNotes(notes);
        eval.setStatus(TeacherEvaluation.EvaluationStatus.POST_CONFERENCE_COMPLETE);
        return evaluationRepository.save(eval);
    }

    // Scoring
    public TeacherEvaluation scoreEvaluation(Long evaluationId,
                                              Double domain1, Double domain2,
                                              Double domain3, Double domain4,
                                              Double overallScore,
                                              TeacherEvaluation.PerformanceRating overallRating) {
        log.info("Scoring evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setDomain1Score(domain1);
        eval.setDomain2Score(domain2);
        eval.setDomain3Score(domain3);
        eval.setDomain4Score(domain4);
        eval.setOverallScore(overallScore);
        eval.setOverallRating(overallRating);

        // Auto-set improvement plan flag for low ratings
        if (overallRating == TeacherEvaluation.PerformanceRating.DEVELOPING ||
            overallRating == TeacherEvaluation.PerformanceRating.INEFFECTIVE ||
            overallRating == TeacherEvaluation.PerformanceRating.BASIC ||
            overallRating == TeacherEvaluation.PerformanceRating.UNSATISFACTORY) {
            eval.setImprovementPlanRequired(true);
        }

        return evaluationRepository.save(eval);
    }

    // Signatures
    public TeacherEvaluation recordTeacherSignature(Long evaluationId, String teacherComments) {
        log.info("Recording teacher signature for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setTeacherSignatureDate(LocalDate.now());
        eval.setTeacherComments(teacherComments);
        eval.setStatus(TeacherEvaluation.EvaluationStatus.COMPLETED);
        return evaluationRepository.save(eval);
    }

    public TeacherEvaluation recordEvaluatorSignature(Long evaluationId) {
        log.info("Recording evaluator signature for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setEvaluatorSignatureDate(LocalDate.now());
        eval.setStatus(TeacherEvaluation.EvaluationStatus.PENDING_TEACHER_REVIEW);
        return evaluationRepository.save(eval);
    }

    // Improvement Plans
    public TeacherEvaluation createImprovementPlan(Long evaluationId, String documentPath) {
        log.info("Creating improvement plan for evaluation {}", evaluationId);
        TeacherEvaluation eval = getEvaluationById(evaluationId);
        eval.setImprovementPlanCreated(true);
        eval.setImprovementPlanDocument(documentPath);
        return evaluationRepository.save(eval);
    }

    // Statistics
    public List<TeacherEvaluation> getHighlyEffectiveTeachers() {
        return evaluationRepository.findHighlyEffectiveTeachers();
    }

    public List<TeacherEvaluation> getTeachersNeedingImprovement() {
        return evaluationRepository.findTeachersNeedingImprovement();
    }
}
