package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Athletic Team entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "athletic_teams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleticTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Season season;

    @Column(nullable = false, length = 50)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamLevel level;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String gradeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_coach_id")
    private Teacher headCoach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_coach_id")
    private Teacher assistantCoach;

    @Column
    private Integer maxRosterSize;

    @Column
    private Integer currentRosterSize;

    @Column
    private LocalDate seasonStartDate;

    @Column
    private LocalDate seasonEndDate;

    @Column(length = 100)
    private String homeVenue;

    @Column(columnDefinition = "TEXT")
    private String practiceSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamStatus status;

    @Column
    private Integer wins;

    @Column
    private Integer losses;

    @Column
    private Integer ties;

    @Column(length = 100)
    private String conference;

    @Column(length = 100)
    private String division;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column
    private Boolean tryoutsRequired;

    @Column
    private LocalDate tryoutDate;

    @Column
    private Boolean active;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeamMembership> members = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AthleticEvent> events = new HashSet<>();

    public enum Sport {
        FOOTBALL, BASKETBALL, SOCCER, BASEBALL, SOFTBALL, VOLLEYBALL,
        TENNIS, TRACK_AND_FIELD, CROSS_COUNTRY, SWIMMING, WRESTLING,
        GOLF, LACROSSE, FIELD_HOCKEY, CHEERLEADING, DANCE, BOWLING,
        GYMNASTICS, BADMINTON, ARCHERY, ESPORTS, OTHER
    }

    public enum Season {
        FALL, WINTER, SPRING, SUMMER, YEAR_ROUND
    }

    public enum TeamLevel {
        VARSITY, JUNIOR_VARSITY, FRESHMAN, MIDDLE_SCHOOL, CLUB, INTRAMURAL
    }

    public enum TeamStatus {
        RECRUITING, ACTIVE, POSTSEASON, COMPLETED, CANCELLED, SUSPENDED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (wins == null) wins = 0;
        if (losses == null) losses = 0;
        if (ties == null) ties = 0;
        if (currentRosterSize == null) currentRosterSize = 0;
        if (active == null) active = true;
        if (tryoutsRequired == null) tryoutsRequired = false;
    }

    public double getWinningPercentage() {
        int totalGames = wins + losses + ties;
        if (totalGames == 0) return 0.0;
        return (double) (wins + (ties * 0.5)) / totalGames;
    }

    public boolean isRosterFull() {
        return maxRosterSize != null && currentRosterSize >= maxRosterSize;
    }

    public String getRecord() {
        return wins + "-" + losses + (ties > 0 ? "-" + ties : "");
    }
}
