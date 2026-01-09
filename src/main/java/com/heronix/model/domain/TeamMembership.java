package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Team Membership entity - represents student participation on athletic teams
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "team_memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "student_id", "academic_year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private AthleticTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 50)
    private String academicYear;

    @Column(length = 50)
    private String position;

    @Column(length = 20)
    private String jerseyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column
    private LocalDate joinDate;

    @Column
    private LocalDate leaveDate;

    @Column
    private Boolean captain;

    @Column
    private Boolean coCaptain;

    @Column
    private Boolean starter;

    @Column
    private Boolean eligible;

    @Column(columnDefinition = "TEXT")
    private String eligibilityNotes;

    @Column
    private Double gpa;

    @Column
    private Integer gamesPlayed;

    @Column
    private Integer gamesStarted;

    @Column
    private Double averageMinutesPlayed;

    @Column(columnDefinition = "TEXT")
    private String statistics;

    @Column(columnDefinition = "TEXT")
    private String awards;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Boolean physicalOnFile;

    @Column
    private LocalDate physicalExpirationDate;

    @Column
    private Boolean consentFormSigned;

    @Column
    private Boolean emergencyContactOnFile;

    public enum MembershipStatus {
        TRYOUT, ACTIVE, INJURED, SUSPENDED, QUIT, DISMISSED, COMPLETED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (captain == null) captain = false;
        if (coCaptain == null) coCaptain = false;
        if (starter == null) starter = false;
        if (eligible == null) eligible = true;
        if (gamesPlayed == null) gamesPlayed = 0;
        if (gamesStarted == null) gamesStarted = 0;
        if (physicalOnFile == null) physicalOnFile = false;
        if (consentFormSigned == null) consentFormSigned = false;
        if (emergencyContactOnFile == null) emergencyContactOnFile = false;
        if (joinDate == null) joinDate = LocalDate.now();
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE && eligible;
    }

    public boolean needsPhysical() {
        return !physicalOnFile ||
               (physicalExpirationDate != null && physicalExpirationDate.isBefore(LocalDate.now()));
    }

    public boolean hasRequiredDocuments() {
        return physicalOnFile && consentFormSigned && emergencyContactOnFile;
    }
}
