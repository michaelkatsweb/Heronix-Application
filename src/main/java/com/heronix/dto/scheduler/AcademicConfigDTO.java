package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Academic configuration including grading periods and schedule type
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicConfigDTO {

    private String academicYear;

    private LocalDate schoolYearStartDate;

    private LocalDate schoolYearEndDate;

    private String scheduleType; // "TRADITIONAL", "BLOCK", "ROTATING", "HYBRID"

    private Integer instructionalDaysPerWeek;

    private Integer periodsPerDay;

    private List<GradingPeriodDTO> gradingPeriods;

    /**
     * Grading period information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradingPeriodDTO {
        private Long id;
        private String name;
        private String periodType; // "QUARTER", "SEMESTER", "TRIMESTER", "YEAR"
        private Integer periodNumber;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer instructionalDays;
    }
}
