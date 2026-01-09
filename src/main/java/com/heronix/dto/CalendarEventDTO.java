package com.heronix.dto;

import com.heronix.model.domain.SchoolCalendar.CalendarEvent.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Calendar Event
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
public class CalendarEventDTO {

    private Long id;

    private Long calendarId;

    private String eventName;

    private String description;

    private EventType eventType;

    private LocalDate eventDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean nonInstructionalDay;

    private Boolean recurring;

    // Calculated fields for response
    private Long durationDays;
}
