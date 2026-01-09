package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cafeteria Menu Item entity - individual items available on a menu
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "cafeteria_menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private DailyMenu menu;

    @Column(nullable = false, length = 200)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column
    private Integer calories;

    @Column
    private Integer protein;

    @Column
    private Integer carbohydrates;

    @Column
    private Integer fat;

    @Column
    private Integer sodium;

    @Column
    private Boolean vegetarian;

    @Column
    private Boolean vegan;

    @Column
    private Boolean glutenFree;

    @Column
    private Boolean dairyFree;

    @Column
    private Boolean nutFree;

    @Column(columnDefinition = "TEXT")
    private String allergens;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column
    private Boolean available;

    @Column
    private Integer quantityAvailable;

    public enum ItemCategory {
        ENTREE, SIDE, BEVERAGE, DESSERT, SALAD, SOUP, SANDWICH, SNACK, FRUIT, VEGETABLE
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (available == null) available = true;
        if (vegetarian == null) vegetarian = false;
        if (vegan == null) vegan = false;
        if (glutenFree == null) glutenFree = false;
        if (dairyFree == null) dairyFree = false;
        if (nutFree == null) nutFree = false;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available) &&
               (quantityAvailable == null || quantityAvailable > 0);
    }

    public String getNutritionalSummary() {
        return String.format("Calories: %d, Protein: %dg, Carbs: %dg, Fat: %dg",
            calories != null ? calories : 0,
            protein != null ? protein : 0,
            carbohydrates != null ? carbohydrates : 0,
            fat != null ? fat : 0);
    }
}
