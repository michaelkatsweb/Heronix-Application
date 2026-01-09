package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Data Transfer Object for Period Timer
 * Used for API requests and responses
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodTimerDTO {

    private Long id;

    private Long academicYearId;

    private Integer periodNumber;

    private String periodName;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer attendanceWindowMinutes;

    private Boolean autoMarkAbsent;

    private String daysOfWeek;

    private Boolean active;

    // Calculated fields for response
    private Integer durationMinutes;

    private String formattedTimeRange;

    private String displayLabel;

    private LocalTime attendanceWindowEndTime;
}
