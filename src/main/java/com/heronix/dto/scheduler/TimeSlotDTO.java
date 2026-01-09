package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Time slot definition for SchedulerV2
 * Represents a period in the daily schedule
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {

    private Long timeSlotId;

    private String periodName;

    private Integer periodNumber;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer durationMinutes;

    /**
     * Day of week (1=Monday, 5=Friday, 0=All days)
     */
    private Integer dayOfWeek;

    /**
     * Is this a lunch period?
     */
    private Boolean isLunchPeriod;

    /**
     * Is this a passing period?
     */
    private Boolean isPassingPeriod;

    /**
     * Is this a planning/conference period?
     */
    private Boolean isPlanningPeriod;

    /**
     * Can courses be scheduled during this period?
     */
    private Boolean isInstructionalPeriod;
}
