package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Lunch period configuration for SchedulerV2
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LunchPeriodDTO {

    private Long lunchPeriodId;

    private String name;

    private Integer waveNumber;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer durationMinutes;

    private Integer maxCapacity;

    private Integer currentAssignedCount;

    /**
     * Grade levels assigned to this lunch wave
     */
    private String assignedGradeLevels; // e.g., "9,10" or "11,12"

    /**
     * Is this the primary lunch period?
     */
    private Boolean isPrimary;
}
