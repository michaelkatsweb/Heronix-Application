package com.heronix.model.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Grading Period Entity
 * Represents academic grading periods (quarters, semesters, trimesters, etc.)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "grading_periods", indexes = {
    @Index(name = "idx_grading_period_year", columnList = "academic_year"),
    @Index(name = "idx_grading_period_active", columnList = "active"),
    @Index(name = "idx_grading_period_dates", columnList = "start_date,end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @NotNull(message = "Grading period name is required")
    private String name;

    @Column(name = "academic_year", nullable = false, length = 20)
    @NotNull(message = "Academic year is required")
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    @NotNull(message = "Period type is required")
    private PeriodType periodType;

    @Column(name = "period_number", nullable = false)
    @NotNull(message = "Period number is required")
    private Integer periodNumber;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Column(name = "instructional_days")
    private Integer instructionalDays;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum PeriodType {
        QUARTER(4, "Quarter"),
        SEMESTER(2, "Semester"),
        TRIMESTER(3, "Trimester"),
        YEAR(1, "Year"),
        CUSTOM(0, "Custom");

        private final int periodsPerYear;
        private final String displayName;

        PeriodType(int periodsPerYear, String displayName) {
            this.periodsPerYear = periodsPerYear;
            this.displayName = displayName;
        }

        public int getPeriodsPerYear() {
            return periodsPerYear;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public boolean overlapsWith(GradingPeriod other) {
        if (other == null) {
            return false;
        }
        if (!this.academicYear.equals(other.academicYear)) {
            return false;
        }
        if (this.campus != null && other.campus != null && !this.campus.getId().equals(other.campus.getId())) {
            return false;
        }
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }

    public long getDuration() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean containsDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean isCurrentPeriod() {
        return containsDate(LocalDate.now());
    }

    public void validate() {
        if (startDate.isAfter(endDate)) {
            throw new IllegalStateException("Start date must be before or equal to end date");
        }
        if (periodType != PeriodType.CUSTOM) {
            int maxPeriods = periodType.getPeriodsPerYear();
            if (periodNumber < 1 || periodNumber > maxPeriods) {
                throw new IllegalStateException(
                    String.format("Period number must be between 1 and %d for %s",
                        maxPeriods, periodType.getDisplayName()));
            }
        }
        if (instructionalDays != null && instructionalDays < 0) {
            throw new IllegalStateException("Instructional days cannot be negative");
        }
    }

    public String getDisplayName() {
        return String.format("%s %s (%s - %s)", name, academicYear, startDate.toString(), endDate.toString());
    }

    public double getPercentComplete() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return -1.0;
        }
        if (today.isAfter(endDate)) {
            return 100.0;
        }
        long totalDays = getDuration();
        long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, today) + 1;
        return (elapsedDays * 100.0) / totalDays;
    }

    @PrePersist
    @PreUpdate
    protected void validateBeforeSave() {
        validate();
    }

    @Override
    public String toString() {
        return String.format("GradingPeriod[id=%d, name='%s', year='%s', period=%d, type=%s, dates=%s to %s]",
            id, name, academicYear, periodNumber, periodType, startDate, endDate);
    }
}
