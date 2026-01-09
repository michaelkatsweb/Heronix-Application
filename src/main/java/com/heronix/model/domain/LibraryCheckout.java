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
 * Entity representing a book checkout transaction
 * Location: src/main/java/com/heronix/model/domain/LibraryCheckout.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "library_checkouts", indexes = {
    @Index(name = "idx_student_checkout", columnList = "student_id"),
    @Index(name = "idx_book_checkout", columnList = "book_id"),
    @Index(name = "idx_due_date", columnList = "dueDate"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryCheckout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private LibraryBook book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate checkoutDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CheckoutStatus status;

    @Column
    private Integer renewalCount;

    @Column
    private Integer maxRenewals; // Usually 2-3 renewals allowed

    @Column(length = 100)
    private String checkedOutBy; // Librarian/staff who processed checkout

    @Column(length = 100)
    private String checkedInBy; // Staff who processed return

    @Column(length = 500)
    private String notes;

    @Column(precision = 10, scale = 2)
    private BigDecimal fineAmount;

    @Column
    private Boolean finePaid;

    @Column(length = 50)
    private String condition; // Condition when checked out

    @Column(length = 50)
    private String returnCondition; // Condition when returned

    @Column
    private Boolean damaged;

    @Column
    private Boolean lost;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (renewalCount == null) renewalCount = 0;
        if (maxRenewals == null) maxRenewals = 2;
        if (finePaid == null) finePaid = false;
        if (damaged == null) damaged = false;
        if (lost == null) lost = false;
        if (fineAmount == null) fineAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return status == CheckoutStatus.CHECKED_OUT &&
               dueDate != null &&
               LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public enum CheckoutStatus {
        CHECKED_OUT,
        RETURNED,
        OVERDUE,
        LOST,
        DAMAGED
    }
}
