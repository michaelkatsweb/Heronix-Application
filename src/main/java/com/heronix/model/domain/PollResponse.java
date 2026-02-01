package com.heronix.model.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @ToString.Exclude
    private Poll poll;

    @Column(name = "respondent_id")
    private Long respondentId;

    @Column(name = "respondent_type", length = 20)
    private String respondentType;

    @Column(name = "respondent_name", length = 200)
    private String respondentName;

    @Column(name = "submitted_at")
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "pollResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("response-answers")
    @Builder.Default
    @ToString.Exclude
    private List<PollAnswer> answers = new ArrayList<>();
}
