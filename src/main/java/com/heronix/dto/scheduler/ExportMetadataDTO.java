package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Metadata about the data export to SchedulerV2
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportMetadataDTO {

    /**
     * Unique export ID (for tracking)
     */
    private String exportId;

    /**
     * SIS Schedule ID being exported
     */
    private Long scheduleId;

    /**
     * Export timestamp
     */
    private LocalDateTime exportTimestamp;

    /**
     * SIS version
     */
    private String sisVersion;

    /**
     * User who initiated the export
     */
    private String exportedBy;

    /**
     * User ID who initiated the export
     */
    private Long exportedByUserId;

    /**
     * Total number of students included
     */
    private Integer totalStudents;

    /**
     * Total number of course requests
     */
    private Integer totalCourseRequests;

    /**
     * Total number of courses
     */
    private Integer totalCourses;

    /**
     * Total number of teachers
     */
    private Integer totalTeachers;

    /**
     * Total number of rooms
     */
    private Integer totalRooms;

    /**
     * Total number of time slots
     */
    private Integer totalTimeSlots;

    /**
     * Notes about this export
     */
    private String notes;
}
