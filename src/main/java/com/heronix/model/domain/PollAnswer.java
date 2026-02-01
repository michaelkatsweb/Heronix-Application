package com.heronix.model.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_response_id", nullable = false)
    @JsonBackReference("response-answers")
    @ToString.Exclude
    private PollResponse pollResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_question_id", nullable = false)
    @ToString.Exclude
    private PollQuestion pollQuestion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_answer_selections", joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "selected_option")
    @Builder.Default
    private List<String> selectedOptions = new ArrayList<>();

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;
}
