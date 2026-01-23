package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Common filter criteria for analytics queries
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsFilterDTO {

    private Long campusId;
    private List<Long> campusIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private String gradeLevel;
    private List<String> gradeLevels;
    private String academicYear;
    private String term;
    private Long courseId;
    private Long teacherId;
    private String department;

    // Grouping options
    private GroupBy groupBy;
    private TimeGranularity timeGranularity;

    // Pagination
    private Integer page;
    private Integer pageSize;

    // Sorting
    private String sortBy;
    private boolean ascending;

    public enum GroupBy {
        CAMPUS,
        GRADE_LEVEL,
        COURSE,
        TEACHER,
        DEPARTMENT,
        GENDER,
        ETHNICITY,
        DATE,
        WEEK,
        MONTH
    }

    public enum TimeGranularity {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    /**
     * Create a filter for current school year
     */
    public static AnalyticsFilterDTO currentYear() {
        LocalDate now = LocalDate.now();
        int year = now.getMonthValue() >= 8 ? now.getYear() : now.getYear() - 1;
        return AnalyticsFilterDTO.builder()
                .startDate(LocalDate.of(year, 8, 1))
                .endDate(LocalDate.of(year + 1, 6, 30))
                .academicYear(year + "-" + (year + 1))
                .build();
    }

    /**
     * Create a filter for last 30 days
     */
    public static AnalyticsFilterDTO last30Days() {
        return AnalyticsFilterDTO.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .build();
    }

    /**
     * Create a filter for specific campus
     */
    public static AnalyticsFilterDTO forCampus(Long campusId) {
        return AnalyticsFilterDTO.builder()
                .campusId(campusId)
                .build();
    }
}
