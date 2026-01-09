package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Daily Menu entity - represents the menu for a specific date
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "daily_menus", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"menuDate", "mealPeriod"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealPeriod mealPeriod;

    @Column(length = 200)
    private String menuName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String mainEntrees;

    @Column(columnDefinition = "TEXT")
    private String sides;

    @Column(columnDefinition = "TEXT")
    private String beverages;

    @Column(columnDefinition = "TEXT")
    private String desserts;

    @Column(columnDefinition = "TEXT")
    private String allergenInfo;

    @Column
    private Integer calories;

    @Column
    private Boolean vegetarianOptions;

    @Column
    private Boolean glutenFreeOptions;

    @Column
    private Boolean dairyFreeOptions;

    @Column
    private Boolean published;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CafeteriaMenuItem> items = new HashSet<>();

    public enum MealPeriod {
        BREAKFAST, LUNCH, DINNER, SNACK
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (vegetarianOptions == null) vegetarianOptions = false;
        if (glutenFreeOptions == null) glutenFreeOptions = false;
        if (dairyFreeOptions == null) dairyFreeOptions = false;
        if (published == null) published = false;
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(published);
    }

    public String getMenuSummary() {
        StringBuilder summary = new StringBuilder();
        if (mainEntrees != null && !mainEntrees.isEmpty()) {
            summary.append("Entrees: ").append(mainEntrees);
        }
        if (sides != null && !sides.isEmpty()) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("Sides: ").append(sides);
        }
        return summary.toString();
    }
}
