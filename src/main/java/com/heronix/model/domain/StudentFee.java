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
 * Entity representing a fee assigned to a specific student
 * Location: src/main/java/com/heronix/model/domain/StudentFee.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "student_fees", indexes = {
    @Index(name = "idx_student_fee", columnList = "student_id"),
    @Index(name = "idx_fee", columnList = "fee_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id", nullable = false)
    private Fee fee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountWaived;

    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeeStatus status;

    @Column
    private Boolean waived;

    @Column(length = 500)
    private String waiverReason;

    @Column
    private String waivedBy; // User who approved waiver

    @Column
    private LocalDateTime waivedAt;

    @Column(length = 1000)
    private String notes;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (amountWaived == null) amountWaived = BigDecimal.ZERO;
        if (waived == null) waived = false;
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    private void updateStatus() {
        BigDecimal totalPaid = amountPaid.add(amountWaived);
        if (waived) {
            status = FeeStatus.WAIVED;
        } else if (totalPaid.compareTo(amountDue) >= 0) {
            status = FeeStatus.PAID;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = FeeStatus.PARTIAL;
        } else if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            status = FeeStatus.OVERDUE;
        } else {
            status = FeeStatus.PENDING;
        }
    }

    public BigDecimal getBalance() {
        return amountDue.subtract(amountPaid).subtract(amountWaived);
    }

    public enum FeeStatus {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE,
        WAIVED
    }
}
