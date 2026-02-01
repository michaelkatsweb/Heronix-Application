package com.heronix.model.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @JsonBackReference("poll-questions")
    @ToString.Exclude
    private Poll poll;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(name = "max_length")
    private Integer maxLength;

    public enum QuestionType {
        MULTIPLE_CHOICE, CHECKBOX, YES_NO, SHORT_TEXT
    }
}
