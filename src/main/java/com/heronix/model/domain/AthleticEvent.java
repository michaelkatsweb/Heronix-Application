package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Athletic Event entity - represents games, matches, meets, and practices
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "athletic_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleticEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private AthleticTeam team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventType eventType;

    @Column(nullable = false, length = 100)
    private String eventName;

    @Column
    private LocalDateTime eventDate;

    @Column
    private LocalDateTime endDate;

    @Column(length = 100)
    private String opponent;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HomeAway homeAway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column
    private Integer teamScore;

    @Column
    private Integer opponentScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GameResult result;

    @Column
    private Boolean conference;

    @Column
    private Boolean playoff;

    @Column
    private Boolean tournament;

    @Column(length = 100)
    private String tournamentName;

    @Column
    private Integer attendance;

    @Column(columnDefinition = "TEXT")
    private String gameNotes;

    @Column(columnDefinition = "TEXT")
    private String highlights;

    @Column(columnDefinition = "TEXT")
private String statistics;

    @Column(length = 100)
    private String transportation;

    @Column
    private LocalDateTime departureTime;

    @Column
    private LocalDateTime returnTime;

    @Column(length = 100)
    private String uniformRequirements;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @Column
    private Boolean cancelled;

    @Column(length = 200)
    private String cancellationReason;

    @Column
    private Boolean postponed;

    @Column
    private LocalDateTime rescheduleDate;

    public enum EventType {
        GAME, MATCH, MEET, SCRIMMAGE, PRACTICE, TOURNAMENT, TRYOUT, TEAM_MEETING
    }

    public enum HomeAway {
        HOME, AWAY, NEUTRAL
    }

    public enum EventStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED, FORFEIT
    }

    public enum GameResult {
        WIN, LOSS, TIE, FORFEIT_WIN, FORFEIT_LOSS, NO_CONTEST
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (conference == null) conference = false;
        if (playoff == null) playoff = false;
        if (tournament == null) tournament = false;
        if (cancelled == null) cancelled = false;
        if (postponed == null) postponed = false;
    }

    public boolean isGame() {
        return eventType == EventType.GAME || eventType == EventType.MATCH || eventType == EventType.MEET;
    }

    public boolean isPractice() {
        return eventType == EventType.PRACTICE;
    }

    public Integer getScoreDifferential() {
        if (teamScore == null || opponentScore == null) return null;
        return teamScore - opponentScore;
    }

    public String getScoreDisplay() {
        if (teamScore == null || opponentScore == null) return "N/A";
        return teamScore + "-" + opponentScore;
    }
}
