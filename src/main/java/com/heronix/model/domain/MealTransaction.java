package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Meal Transaction entity - records all meal purchases and transactions
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "meal_transactions", indexes = {
    @Index(name = "idx_transaction_date", columnList = "transactionDate"),
    @Index(name = "idx_student_date", columnList = "student_id,transactionDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private StudentMealAccount account;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealType mealType;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 100)
    private String menuItem;

    @Column(length = 100)
    private String cashierName;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum TransactionType {
        PURCHASE,       // Student bought a meal
        DEPOSIT,        // Money added to account
        REFUND,         // Money refunded
        ADJUSTMENT,     // Balance adjustment
        TRANSFER,       // Transfer between accounts
        FEE            // Service fee
    }

    public enum MealType {
        BREAKFAST, LUNCH, DINNER, SNACK, A_LA_CARTE, OTHER
    }

    public enum PaymentMethod {
        ACCOUNT_BALANCE, CASH, CREDIT_CARD, DEBIT_CARD, CHECK, FREE, REDUCED_PRICE
    }

    @PrePersist
    private void prePersist() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    public boolean isPurchase() {
        return transactionType == TransactionType.PURCHASE;
    }

    public boolean isDeposit() {
        return transactionType == TransactionType.DEPOSIT;
    }

    public LocalDate getTransactionLocalDate() {
        return transactionDate != null ? transactionDate.toLocalDate() : null;
    }
}
