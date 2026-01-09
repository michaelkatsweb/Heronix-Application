package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Student Meal Account entity - tracks student meal balances and plans
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "student_meal_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "academic_year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMealAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;

    @Column(nullable = false, length = 50)
    private String academicYear;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EligibilityStatus eligibilityStatus;

    @Column
    private Boolean autoReload;

    @Column(precision = 10, scale = 2)
    private BigDecimal reloadThreshold;

    @Column(precision = 10, scale = 2)
    private BigDecimal reloadAmount;

    @Column
    private LocalDate planStartDate;

    @Column
    private LocalDate planEndDate;

    @Column
    private Integer mealsRemaining;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String dietaryRestrictions;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum AccountStatus {
        ACTIVE, SUSPENDED, CLOSED, PENDING_ACTIVATION, DELINQUENT
    }

    public enum EligibilityStatus {
        FULL_PRICE, FREE, REDUCED_PRICE, PENDING_VERIFICATION
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (balance == null) balance = BigDecimal.ZERO;
        if (status == null) status = AccountStatus.ACTIVE;
        if (autoReload == null) autoReload = false;
    }

    public boolean needsReload() {
        return Boolean.TRUE.equals(autoReload) &&
               reloadThreshold != null &&
               balance.compareTo(reloadThreshold) < 0;
    }

    public boolean canPurchase(BigDecimal amount) {
        if (status != AccountStatus.ACTIVE) return false;
        if (mealsRemaining != null && mealsRemaining <= 0) return false;
        return balance.compareTo(amount) >= 0;
    }

    public void deductBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        if (mealsRemaining != null && mealsRemaining > 0) {
            this.mealsRemaining--;
        }
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
