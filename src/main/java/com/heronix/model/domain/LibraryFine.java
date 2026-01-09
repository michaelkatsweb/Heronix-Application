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
 * Entity representing library fines for overdue/lost/damaged books
 * Location: src/main/java/com/heronix/model/domain/LibraryFine.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "library_fines", indexes = {
    @Index(name = "idx_student_fine", columnList = "student_id"),
    @Index(name = "idx_checkout_fine", columnList = "checkout_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryFine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id")
    private LibraryCheckout checkout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FineType fineType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance;

    @Column
    private LocalDate fineDate;

    @Column
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FineStatus status;

    @Column(length = 500)
    private String reason;

    @Column(length = 100)
    private String issuedBy;

    @Column(length = 100)
    private String collectedBy;

    @Column(length = 500)
    private String notes;

    @Column
    private Boolean waived;

    @Column(length = 500)
    private String waiverReason;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (balance == null) balance = amount;
        if (waived == null) waived = false;
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    private void updateStatus() {
        balance = amount.subtract(amountPaid);
        if (waived) {
            status = FineStatus.WAIVED;
        } else if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            status = FineStatus.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = FineStatus.PARTIAL;
        } else {
            status = FineStatus.UNPAID;
        }
    }

    public enum FineType {
        OVERDUE,
        LOST_BOOK,
        DAMAGED_BOOK,
        LATE_RETURN,
        OTHER
    }

    public enum FineStatus {
        UNPAID,
        PARTIAL,
        PAID,
        WAIVED
    }
}
