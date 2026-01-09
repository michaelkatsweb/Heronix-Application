package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Teacher availability and qualifications for SchedulerV2
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAvailabilityDTO {

    private Long teacherId;

    private String employeeNumber;

    private String firstName;

    private String lastName;

    private String fullName;

    private String department;

    /**
     * Teacher certifications/qualifications
     */
    private List<String> certifications;

    /**
     * Courses this teacher is qualified to teach (course IDs)
     */
    private List<Long> qualifiedCourseIds;

    /**
     * Maximum number of sections this teacher can teach
     */
    private Integer maxSections;

    /**
     * Maximum number of different course preps
     */
    private Integer maxPreps;

    /**
     * Preferred maximum students across all sections
     */
    private Integer maxTotalStudents;

    /**
     * Planning periods required (usually 1)
     */
    private Integer planningPeriodsRequired;

    /**
     * Time slots when teacher is unavailable
     */
    private List<UnavailableSlot> unavailableSlots;

    /**
     * Preferred time slots (soft constraint)
     */
    private List<PreferredSlot> preferredSlots;

    /**
     * Is this teacher part-time?
     */
    private Boolean isPartTime;

    /**
     * Teacher's preferred room (if any)
     */
    private Long preferredRoomId;

    /**
     * Co-teaching assignments (teacher IDs of co-teachers)
     */
    private List<Long> coTeacherIds;

    /**
     * Unavailable time slot
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnavailableSlot {
        private Integer dayOfWeek; // 1=Monday, 5=Friday
        private Integer periodNumber;
        private String reason;
    }

    /**
     * Preferred time slot
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreferredSlot {
        private Long courseId;
        private Integer periodNumber;
        private Integer preferenceWeight; // 1-10, higher = stronger preference
    }
}
