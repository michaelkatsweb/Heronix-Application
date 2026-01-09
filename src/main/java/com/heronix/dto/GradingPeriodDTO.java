package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Grading Period
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingPeriodDTO {
    private Long id;
    private String name;
    private String academicYear;
    private String periodType;
    private Integer periodNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer instructionalDays;
    private Boolean active;
    private Long campusId;
    private String campusName;
    private String notes;
    private String displayName;
    private Double percentComplete;
    private Long duration;
    private Boolean isCurrent;
}
