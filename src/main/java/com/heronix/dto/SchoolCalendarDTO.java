package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for School Calendar
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
public class SchoolCalendarDTO {

    private Long id;

    private String academicYear;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long campusId;

    private String campusName;

    @Builder.Default
    private List<CalendarEventDTO> events = new ArrayList<>();

    private Integer instructionalDaysPerWeek;

    private Integer totalInstructionalDays;

    private Boolean active;

    // Calculated fields for response
    private Long totalDays;

    private Boolean isCurrentYear;

    private Boolean isValid;

    private Integer holidayCount;

    private Integer breakCount;
}
