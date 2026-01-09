package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Student-Parent Relationship Join Table
 *
 * Represents the many-to-many relationship between students and parents/guardians.
 * Allows one student to have multiple parents, and one parent to have multiple students (siblings).
 *
 * Tracks permissions and status per student-parent pair.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Entity
@Table(name = "student_parent_relationships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentParentRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    @ToString.Exclude
    private ParentGuardian parent;

    // ========================================================================
    // RELATIONSHIP DETAILS
    // ========================================================================

    @Column(nullable = false, length = 50)
    private String relationshipType; // "Mother", "Father", "Guardian", etc.

    @Column
    @Builder.Default
    private Boolean isPrimaryContact = false;

    @Column
    @Builder.Default
    private Boolean isCustodial = true;

    @Column
    @Builder.Default
    private Boolean hasPickupPermission = true;

    @Column
    @Builder.Default
    private Boolean canReceiveGrades = true;

    @Column
    @Builder.Default
    private Boolean canMakeEducationalDecisions = true;

    @Column
    @Builder.Default
    private Boolean isEmergencyContact = true;

    @Column(length = 1000)
    private String restrictionsNotes;

    // ========================================================================
    // STATUS
    // ========================================================================

    @Column
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDateTime inactivatedAt;

    @Column(length = 500)
    private String inactivationReason;

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
