package com.heronix.dto;

import com.heronix.model.domain.BellSchedule.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Bell Schedule
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
public class BellScheduleDTO {

    private Long id;

    private String name;

    private String description;

    private ScheduleType scheduleType;

    private Long academicYearId;

    private String academicYearName;

    private Long campusId;

    private String campusName;

    private String daysOfWeek;

    @Builder.Default
    private List<LocalDate> specificDates = new ArrayList<>();

    @Builder.Default
    private List<PeriodTimerDTO> periods = new ArrayList<>();

    private Boolean isDefault;

    private Boolean active;

    private Integer totalInstructionalMinutes;

    // Calculated fields for response
    private Integer periodCount;

    private String formattedSchedule;

    private String displayName;

    private Boolean isComplete;

    private Boolean hasOverlappingPeriods;
}
