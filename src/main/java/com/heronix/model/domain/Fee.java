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
 * Entity representing a fee (tuition, activity fee, lunch fee, etc.)
 * Location: src/main/java/com/heronix/model/domain/Fee.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String feeName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeeType feeType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeeFrequency frequency;

    @Column(length = 50)
    private String gradeLevel; // null = applies to all grades

    @Column
    private String academicYear; // e.g., "2024-2025"

    @Column
    private LocalDate dueDate;

    @Column
    private Boolean mandatory;

    @Column
    private Boolean waivable; // Can be waived for financial hardship

    @Column
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus; // null = district-wide fee

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (mandatory == null) mandatory = true;
        if (waivable == null) waivable = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FeeType {
        TUITION,
        REGISTRATION,
        TECHNOLOGY,
        ACTIVITY,
        ATHLETICS,
        CLUB,
        TEXTBOOK,
        FIELD_TRIP,
        YEARBOOK,
        PARKING,
        GRADUATION,
        TRANSCRIPT,
        LATE_FEE,
        LIBRARY_FINE,
        LOST_BOOK,
        DAMAGE,
        OTHER
    }

    public enum FeeFrequency {
        ONE_TIME,
        ANNUAL,
        SEMESTER,
        QUARTERLY,
        MONTHLY,
        DAILY
    }
}
