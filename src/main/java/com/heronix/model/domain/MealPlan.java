package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Meal Plan entity - represents different meal plan types offered
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "meal_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType planType;

    @Column(length = 500)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal weeklyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal yearlyRate;

    @Column
    private Boolean includesBreakfast;

    @Column
    private Boolean includesLunch;

    @Column
    private Boolean includesDinner;

    @Column
    private Boolean includesSnacks;

    @Column
    private Integer mealsPerWeek;

    @Column
    private Boolean active;

    @Column(length = 50)
    private String academicYear;

    public enum PlanType {
        FULL_MEAL,          // All meals included
        LUNCH_ONLY,         // Lunch only
        BREAKFAST_LUNCH,    // Breakfast and lunch
        FREE_REDUCED,       // Free or reduced price
        PREPAID,           // Prepaid meal plan
        A_LA_CARTE         // Pay per meal
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (active == null) active = true;
        if (includesBreakfast == null) includesBreakfast = false;
        if (includesLunch == null) includesLunch = false;
        if (includesDinner == null) includesDinner = false;
        if (includesSnacks == null) includesSnacks = false;
        if (mealsPerWeek == null) mealsPerWeek = 0;
    }

    public BigDecimal calculateTotalValue() {
        if (yearlyRate != null) return yearlyRate;
        if (monthlyRate != null) return monthlyRate.multiply(new BigDecimal("10")); // 10 months
        if (weeklyRate != null) return weeklyRate.multiply(new BigDecimal("40")); // 40 weeks
        if (dailyRate != null) return dailyRate.multiply(new BigDecimal("180")); // 180 school days
        return BigDecimal.ZERO;
    }
}
