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
 * Entity representing a payment made toward a student fee
 * Location: src/main/java/com/heronix/model/domain/FeePayment.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "fee_payments", indexes = {
    @Index(name = "idx_student_fee_payment", columnList = "student_fee_id"),
    @Index(name = "idx_payment_date", columnList = "paymentDate"),
    @Index(name = "idx_payment_method", columnList = "paymentMethod")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fee_id", nullable = false)
    private StudentFee studentFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String transactionId; // Check number, transaction ID, etc.

    @Column(length = 100)
    private String confirmationNumber;

    @Column(length = 500)
    private String notes;

    @Column(length = 100)
    private String receivedBy; // Staff member who processed payment

    @Column
    private Boolean refunded;

    @Column
    private LocalDateTime refundedAt;

    @Column(length = 500)
    private String refundReason;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (refunded == null) refunded = false;
    }

    public enum PaymentMethod {
        CASH,
        CHECK,
        CREDIT_CARD,
        DEBIT_CARD,
        MONEY_ORDER,
        BANK_TRANSFER,
        ONLINE_PAYMENT,
        PAYROLL_DEDUCTION,
        SCHOLARSHIP,
        GRANT,
        OTHER
    }
}
