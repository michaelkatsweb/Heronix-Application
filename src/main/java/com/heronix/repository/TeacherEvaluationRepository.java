package com.heronix.repository;

import com.heronix.model.domain.TeacherEvaluation;
import com.heronix.model.domain.TeacherEvaluation.EvaluationStatus;
import com.heronix.model.domain.TeacherEvaluation.EvaluationType;
import com.heronix.model.domain.TeacherEvaluation.PerformanceRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Teacher Evaluation entities
 * Manages teacher evaluations, observations, and performance assessments
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface TeacherEvaluationRepository extends JpaRepository<TeacherEvaluation, Long> {

    /**
     * Find all evaluations for a teacher
     */
    List<TeacherEvaluation> findByTeacherIdOrderByEvaluationDateDesc(Long teacherId);

    /**
     * Find evaluations by school year
     */
    List<TeacherEvaluation> findBySchoolYearOrderByEvaluationDateDesc(String schoolYear);

    /**
     * Find evaluations for teacher in school year
     */
    List<TeacherEvaluation> findByTeacherIdAndSchoolYearOrderByEvaluationDateDesc(
            Long teacherId, String schoolYear);

    /**
     * Find evaluations by evaluator
     */
    List<TeacherEvaluation> findByEvaluatorIdOrderByEvaluationDateDesc(Long evaluatorId);

    /**
     * Find evaluations by status
     */
    List<TeacherEvaluation> findByStatusOrderByScheduledDateAsc(EvaluationStatus status);

    /**
     * Find evaluations by type
     */
    List<TeacherEvaluation> findByEvaluationTypeOrderByEvaluationDateDesc(EvaluationType type);

    /**
     * Find scheduled evaluations for a date
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.scheduledDate = :date " +
           "AND e.status IN ('SCHEDULED', 'PRE_CONFERENCE_COMPLETE') " +
           "ORDER BY e.scheduledTime ASC")
    List<TeacherEvaluation> findScheduledForDate(@Param("date") LocalDate date);

    /**
     * Find overdue evaluations
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.scheduledDate < :today " +
           "AND e.status NOT IN ('COMPLETED', 'REVISED') " +
           "ORDER BY e.scheduledDate ASC")
    List<TeacherEvaluation> findOverdue(@Param("today") LocalDate today);

    /**
     * Find evaluations pending pre-conference
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.status = 'PRE_CONFERENCE_PENDING' " +
           "ORDER BY e.scheduledDate ASC")
    List<TeacherEvaluation> findPendingPreConference();

    /**
     * Find evaluations pending post-conference
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.status = 'POST_CONFERENCE_PENDING' " +
           "ORDER BY e.evaluationDate ASC")
    List<TeacherEvaluation> findPendingPostConference();

    /**
     * Find evaluations pending teacher signature
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.status = 'PENDING_SIGNATURE' " +
           "AND e.teacherSignatureDate IS NULL " +
           "ORDER BY e.evaluationDate ASC")
    List<TeacherEvaluation> findPendingSignature();

    /**
     * Find evaluations requiring improvement plans
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.improvementPlanRequired = true " +
           "AND e.improvementPlanCreated = false " +
           "ORDER BY e.evaluationDate ASC")
    List<TeacherEvaluation> findRequiringImprovementPlan();

    /**
     * Find evaluations by overall rating
     */
    List<TeacherEvaluation> findByOverallRatingOrderByEvaluationDateDesc(PerformanceRating rating);

    /**
     * Find teachers needing improvement (based on ratings)
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.overallRating IN ('DEVELOPING', 'INEFFECTIVE', 'BASIC', 'UNSATISFACTORY') " +
           "AND e.status = 'COMPLETED' " +
           "ORDER BY e.evaluationDate DESC")
    List<TeacherEvaluation> findTeachersNeedingImprovement();

    /**
     * Find highly effective teachers
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.overallRating IN ('HIGHLY_EFFECTIVE', 'DISTINGUISHED') " +
           "AND e.status = 'COMPLETED' " +
           "ORDER BY e.evaluationDate DESC")
    List<TeacherEvaluation> findHighlyEffectiveTeachers();

    /**
     * Find evaluations with appeals
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.status = 'APPEALED' " +
           "ORDER BY e.evaluationDate DESC")
    List<TeacherEvaluation> findAppealedEvaluations();

    /**
     * Find all evaluations with teacher and evaluator eagerly fetched (avoids LazyInitializationException on FX thread)
     */
    @Query("SELECT e FROM TeacherEvaluation e JOIN FETCH e.teacher LEFT JOIN FETCH e.evaluator ORDER BY e.scheduledDate DESC")
    List<TeacherEvaluation> findAllWithTeacherAndEvaluator();

    /**
     * Count evaluations by status
     */
    @Query("SELECT e.status, COUNT(e) FROM TeacherEvaluation e " +
           "WHERE e.schoolYear = :schoolYear " +
           "GROUP BY e.status")
    List<Object[]> countByStatus(@Param("schoolYear") String schoolYear);

    /**
     * Count evaluations by rating
     */
    @Query("SELECT e.overallRating, COUNT(e) FROM TeacherEvaluation e " +
           "WHERE e.schoolYear = :schoolYear " +
           "AND e.status = 'COMPLETED' " +
           "GROUP BY e.overallRating")
    List<Object[]> countByRating(@Param("schoolYear") String schoolYear);

    /**
     * Get average scores by domain
     */
    @Query("SELECT AVG(e.domain1Score), AVG(e.domain2Score), " +
           "AVG(e.domain3Score), AVG(e.domain4Score), AVG(e.overallScore) " +
           "FROM TeacherEvaluation e " +
           "WHERE e.schoolYear = :schoolYear " +
           "AND e.status = 'COMPLETED'")
    Object[] getAverageScores(@Param("schoolYear") String schoolYear);

    /**
     * Get teacher's average rating over time
     */
    @Query("SELECT e.schoolYear, AVG(e.overallScore) " +
           "FROM TeacherEvaluation e " +
           "WHERE e.teacher.id = :teacherId " +
           "AND e.status = 'COMPLETED' " +
           "GROUP BY e.schoolYear " +
           "ORDER BY e.schoolYear ASC")
    List<Object[]> getTeacherRatingTrend(@Param("teacherId") Long teacherId);

    /**
     * Find evaluations by evaluator and school year
     */
    @Query("SELECT e FROM TeacherEvaluation e WHERE e.evaluator.id = :evaluatorId " +
           "AND e.schoolYear = :schoolYear " +
           "ORDER BY e.evaluationDate DESC")
    List<TeacherEvaluation> findByEvaluatorAndSchoolYear(@Param("evaluatorId") Long evaluatorId,
                                                           @Param("schoolYear") String schoolYear);

    /**
     * Get evaluator workload (count of pending evaluations)
     */
    @Query("SELECT e.evaluator.id, e.evaluator.name, COUNT(e) " +
           "FROM TeacherEvaluation e " +
           "WHERE e.status NOT IN ('COMPLETED', 'REVISED') " +
           "GROUP BY e.evaluator.id, e.evaluator.name " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> getEvaluatorWorkload();

    /**
     * Get evaluation completion rate by evaluator
     */
    @Query("SELECT e.evaluator.id, e.evaluator.name, " +
           "COUNT(CASE WHEN e.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(e) " +
           "FROM TeacherEvaluation e " +
           "WHERE e.schoolYear = :schoolYear " +
           "GROUP BY e.evaluator.id, e.evaluator.name")
    List<Object[]> getEvaluatorCompletionRate(@Param("schoolYear") String schoolYear);
}
