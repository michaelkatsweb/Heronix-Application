package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Constraint configuration for SchedulerV2 optimization
 * Controls the weight and priority of various scheduling constraints
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstraintConfigDTO {

    // ========================================================================
    // HARD CONSTRAINTS (Must be satisfied)
    // ========================================================================

    /**
     * No student can have two classes at the same time
     */
    private Boolean enforceNoStudentConflicts;

    /**
     * No teacher can teach two classes at the same time
     */
    private Boolean enforceNoTeacherConflicts;

    /**
     * No room can be used for two classes at the same time
     */
    private Boolean enforceNoRoomConflicts;

    /**
     * Teacher must be certified/qualified for the course
     */
    private Boolean enforceTeacherQualifications;

    /**
     * Room must have required equipment and capacity
     */
    private Boolean enforceRoomRequirements;

    /**
     * Students must meet course prerequisites
     */
    private Boolean enforcePrerequisites;

    /**
     * Students must have a lunch period
     */
    private Boolean enforceLunchAssignment;

    // ========================================================================
    // SOFT CONSTRAINTS (Optimization goals - weighted)
    // ========================================================================

    /**
     * Weight for student course preference satisfaction (1-100)
     */
    private Integer studentPreferenceWeight;

    /**
     * Weight for minimizing teacher travel between rooms (1-100)
     */
    private Integer teacherTravelWeight;

    /**
     * Weight for teacher schedule compactness (1-100)
     */
    private Integer scheduleCompactnessWeight;

    /**
     * Weight for balancing section sizes (1-100)
     */
    private Integer sectionBalanceWeight;

    /**
     * Weight for respecting teacher preferences (1-100)
     */
    private Integer teacherPreferenceWeight;

    /**
     * Weight for grade-level clustering (1-100)
     */
    private Integer gradeLevelClusteringWeight;

    /**
     * Weight for department room clustering (1-100)
     */
    private Integer departmentClusteringWeight;

    /**
     * Weight for minimizing split lunches for students (1-100)
     */
    private Integer lunchContinuityWeight;

    // ========================================================================
    // OPTIMIZATION PARAMETERS
    // ========================================================================

    /**
     * Maximum optimization time in seconds
     */
    private Integer maxOptimizationTimeSeconds;

    /**
     * Target score threshold to stop optimization early
     */
    private Integer targetScoreThreshold;

    /**
     * Enable advanced optimization techniques (simulated annealing, tabu search)
     */
    private Boolean enableAdvancedOptimization;

    /**
     * Enable multi-threaded optimization
     */
    private Boolean enableParallelOptimization;

    /**
     * Number of optimization threads (if parallel enabled)
     */
    private Integer optimizationThreads;

    // ========================================================================
    // SCHEDULING PREFERENCES
    // ========================================================================

    /**
     * Prefer placing advanced courses earlier in the day
     */
    private Boolean preferEarlyAdvancedCourses;

    /**
     * Prefer placing electives later in the day
     */
    private Boolean preferLateElectives;

    /**
     * Try to give teachers back-to-back sections of same course
     */
    private Boolean preferConsecutiveSections;

    /**
     * Minimize student travel distance between consecutive classes
     */
    private Boolean minimizeStudentTravel;

    /**
     * Create default configuration with standard weights
     */
    public static ConstraintConfigDTO createDefault() {
        return ConstraintConfigDTO.builder()
                // Hard constraints - all enabled by default
                .enforceNoStudentConflicts(true)
                .enforceNoTeacherConflicts(true)
                .enforceNoRoomConflicts(true)
                .enforceTeacherQualifications(true)
                .enforceRoomRequirements(true)
                .enforcePrerequisites(true)
                .enforceLunchAssignment(true)
                // Soft constraint weights (1-100 scale)
                .studentPreferenceWeight(80)
                .teacherTravelWeight(40)
                .scheduleCompactnessWeight(50)
                .sectionBalanceWeight(60)
                .teacherPreferenceWeight(30)
                .gradeLevelClusteringWeight(20)
                .departmentClusteringWeight(25)
                .lunchContinuityWeight(45)
                // Optimization parameters
                .maxOptimizationTimeSeconds(120)
                .targetScoreThreshold(90)
                .enableAdvancedOptimization(true)
                .enableParallelOptimization(true)
                .optimizationThreads(4)
                // Scheduling preferences
                .preferEarlyAdvancedCourses(true)
                .preferLateElectives(false)
                .preferConsecutiveSections(true)
                .minimizeStudentTravel(true)
                .build();
    }
}
