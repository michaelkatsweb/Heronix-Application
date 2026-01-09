package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Student course request for SchedulerV2
 * Contains student information and their course requests with preferences
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequestDTO {

    private Long studentId;

    private String studentNumber;

    private String firstName;

    private String lastName;

    private Integer gradeLevel;

    /**
     * Student's assigned lunch period (if pre-assigned)
     */
    private Integer assignedLunchPeriod;

    /**
     * Does student have an IEP (Individualized Education Program)?
     */
    private Boolean hasIEP;

    /**
     * Does student have a 504 plan?
     */
    private Boolean has504Plan;

    /**
     * Special scheduling needs/accommodations
     */
    private List<String> specialAccommodations;

    /**
     * Courses the student has already completed
     */
    private List<Long> completedCourseIds;

    /**
     * Student's course requests (ordered by preference)
     */
    private List<CourseRequestEntry> courseRequests;

    /**
     * Individual course request entry
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseRequestEntry {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Integer preferenceRank; // 1 = highest preference
        private Integer priorityScore; // calculated priority
        private Boolean isRequired; // true for required courses
        private Boolean isAlternate; // true if this is an alternate choice
        private Long primaryCourseId; // if alternate, which course is this replacing?
    }
}
