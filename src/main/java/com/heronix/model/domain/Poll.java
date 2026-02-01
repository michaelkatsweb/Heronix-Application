package com.heronix.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_name", length = 200)
    private String creatorName;

    @Column(name = "creator_type", length = 20)
    private String creatorType; // TEACHER, STAFF, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 20)
    private TargetAudience targetAudience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PollStatus status = PollStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "allow_multiple_responses")
    @Builder.Default
    private Boolean allowMultipleResponses = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "results_visibility", nullable = false, length = 20)
    @Builder.Default
    private ResultsVisibility resultsVisibility = ResultsVisibility.AFTER_CLOSE;

    @Column(name = "total_responses")
    @Builder.Default
    private Integer totalResponses = 0;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("poll-questions")
    @Builder.Default
    @ToString.Exclude
    private List<PollQuestion> questions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    public boolean isActive() {
        if (status != PollStatus.PUBLISHED) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        return true;
    }

    public enum PollStatus {
        DRAFT, PUBLISHED, CLOSED, ARCHIVED
    }

    public enum TargetAudience {
        STUDENTS, TEACHERS, PARENTS, STAFF, ALL
    }

    public enum ResultsVisibility {
        AFTER_VOTING, AFTER_CLOSE, NEVER
    }
}
