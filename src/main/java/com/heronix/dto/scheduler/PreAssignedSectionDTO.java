package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pre-assigned section (locked/pinned assignment)
 * These assignments are fixed and should not be changed by the optimizer
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAssignedSectionDTO {

    private Long sectionId;

    private Long courseId;

    private String courseCode;

    private String courseName;

    /**
     * Assigned teacher ID (locked)
     */
    private Long teacherId;

    /**
     * Assigned teacher name
     */
    private String teacherName;

    /**
     * Assigned room ID (locked)
     */
    private Long roomId;

    /**
     * Assigned room number
     */
    private String roomNumber;

    /**
     * Assigned time slot ID (locked)
     */
    private Long timeSlotId;

    /**
     * Assigned period number
     */
    private Integer periodNumber;

    /**
     * Day of week (if block schedule)
     */
    private Integer dayOfWeek;

    /**
     * Students enrolled in this section
     */
    private List<Long> enrolledStudentIds;

    /**
     * Maximum capacity for this section
     */
    private Integer maxCapacity;

    /**
     * Current enrollment count
     */
    private Integer currentEnrollment;

    /**
     * Reason for locking this assignment
     */
    private String lockReason;

    /**
     * Can students be added/removed from this section?
     */
    private Boolean allowEnrollmentChanges;
}
