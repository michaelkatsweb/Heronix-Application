package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Course catalog entry for SchedulerV2
 * Represents a course definition with all metadata needed for scheduling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCatalogDTO {

    private Long courseId;

    private String courseCode;

    private String courseName;

    private String department;

    private String subjectArea;

    private Integer gradeLevel;

    private Double credits;

    /**
     * Number of sections needed for this course
     */
    private Integer sectionsNeeded;

    /**
     * Maximum students per section
     */
    private Integer maxStudentsPerSection;

    /**
     * Minimum students per section (for viability)
     */
    private Integer minStudentsPerSection;

    /**
     * Total student demand (number of students requesting this course)
     */
    private Integer totalDemand;

    /**
     * Required teacher certifications
     */
    private List<String> requiredCertifications;

    /**
     * Required room types
     */
    private List<String> requiredRoomTypes;

    /**
     * Required equipment
     */
    private List<String> requiredEquipment;

    /**
     * Course duration in periods (1 for single period, 2 for block, etc.)
     */
    private Integer periodsRequired;

    /**
     * Can this course be scheduled during lunch? (usually false)
     */
    private Boolean allowDuringLunch;

    /**
     * Is this an AP/Honors course?
     */
    private Boolean isAdvanced;

    /**
     * Is this a special education course?
     */
    private Boolean isSpecialEducation;

    /**
     * Prerequisite course IDs
     */
    private List<Long> prerequisiteCourseIds;

    /**
     * Course priority (higher = more important to schedule first)
     */
    private Integer priority;
}
