package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Club Membership entity - represents student participation in clubs
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "club_memberships_detailed", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"club_id", "student_id", "academic_year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 50)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column
    private LocalDate joinDate;

    @Column
    private LocalDate leaveDate;

    @Column
    private Boolean president;

    @Column
    private Boolean vicePresident;

    @Column
    private Boolean secretary;

    @Column
    private Boolean treasurer;

    @Column(length = 100)
    private String customRole;

    @Column
    private Integer meetingsAttended;

    @Column
    private Integer eventsAttended;

    @Column
    private Integer serviceHours;

    @Column(columnDefinition = "TEXT")
    private String contributions;

    @Column(columnDefinition = "TEXT")
    private String awards;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Boolean parentConsentReceived;

    public enum MembershipRole {
        PRESIDENT, VICE_PRESIDENT, SECRETARY, TREASURER, OFFICER, MEMBER, ADVISOR
    }

    public enum MembershipStatus {
        PENDING, ACTIVE, INACTIVE, SUSPENDED, RESIGNED, GRADUATED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (president == null) president = false;
        if (vicePresident == null) vicePresident = false;
        if (secretary == null) secretary = false;
        if (treasurer == null) treasurer = false;
        if (meetingsAttended == null) meetingsAttended = 0;
        if (eventsAttended == null) eventsAttended = 0;
        if (serviceHours == null) serviceHours = 0;
        if (parentConsentReceived == null) parentConsentReceived = false;
        if (joinDate == null) joinDate = LocalDate.now();
    }

    public boolean isOfficer() {
        return president || vicePresident || secretary || treasurer || role == MembershipRole.OFFICER;
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}
