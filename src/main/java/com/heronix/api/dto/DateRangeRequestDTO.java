package com.heronix.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Date Range Requests
 *
 * Common DTO for analytics and reporting endpoints that require date ranges.
 * Ensures start date is before or equal to end date and validates date ranges.
 *
 * Validation Rules:
 * - Start Date: Required, not in the future
 * - End Date: Required, must be after or equal to start date
 * - Maximum range: 2 years (730 days)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeRequestDTO {

    /**
     * Start date of the reporting period (inclusive)
     */
    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    /**
     * End date of the reporting period (inclusive)
     */
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    /**
     * Custom validation: Ensure end date is not before start date
     */
    @AssertTrue(message = "End date must be after or equal to start date")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Custom validation: Ensure date range is not too large (max 2 years)
     */
    @AssertTrue(message = "Date range cannot exceed 2 years (730 days)")
    public boolean isDateRangeSizeValid() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return daysBetween <= 730;
    }
}
