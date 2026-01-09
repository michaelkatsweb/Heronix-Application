package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root data payload for exporting SIS data to Heronix-SchedulerV2
 * This payload contains all necessary information for AI-powered schedule optimization
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerDataPayload {

    /**
     * School/district information
     */
    private SchoolInfoDTO schoolInfo;

    /**
     * Academic year and grading period configuration
     */
    private AcademicConfigDTO academicConfig;

    /**
     * Course catalog (all available courses)
     */
    private List<CourseCatalogDTO> courses;

    /**
     * Student enrollment requests
     */
    private List<StudentRequestDTO> studentRequests;

    /**
     * Teacher availability and qualifications
     */
    private List<TeacherAvailabilityDTO> teachers;

    /**
     * Room availability and capabilities
     */
    private List<RoomAvailabilityDTO> rooms;

    /**
     * Time slot definitions (periods)
     */
    private List<TimeSlotDTO> timeSlots;

    /**
     * Lunch period configurations
     */
    private List<LunchPeriodDTO> lunchPeriods;

    /**
     * Optimization constraint configuration
     */
    private ConstraintConfigDTO constraints;

    /**
     * Pre-assigned sections (locked/pinned assignments)
     */
    private List<PreAssignedSectionDTO> preAssignedSections;

    /**
     * Metadata about this export
     */
    private ExportMetadataDTO metadata;
}
