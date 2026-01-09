package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Bus Assignment entity - assigns students to bus routes and stops
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "bus_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "route_id", "academic_year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "morning_stop_id")
    private BusStop morningStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "afternoon_stop_id")
    private BusStop afternoonStop;

    @Column(nullable = false, length = 50)
    private String academicYear;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column
    private Boolean requiresSpecialAccommodations;

    @Column(columnDefinition = "TEXT")
    private String accommodationNotes;

    @Column
    private Boolean parentApprovalReceived;

    @Column
    private LocalDate approvalDate;

    @Column(columnDefinition = "TEXT")
    private String emergencyContactInfo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum AssignmentStatus {
        PENDING, ACTIVE, SUSPENDED, CANCELLED, COMPLETED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (requiresSpecialAccommodations == null) requiresSpecialAccommodations = false;
        if (parentApprovalReceived == null) parentApprovalReceived = false;
        if (startDate == null) startDate = LocalDate.now();
    }

    public boolean isActive() {
        return status == AssignmentStatus.ACTIVE &&
               (endDate == null || !endDate.isBefore(LocalDate.now()));
    }
}
