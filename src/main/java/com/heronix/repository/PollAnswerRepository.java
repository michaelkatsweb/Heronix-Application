package com.heronix.repository;

import com.heronix.model.domain.PollAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollAnswerRepository extends JpaRepository<PollAnswer, Long> {

    List<PollAnswer> findByPollQuestionId(Long questionId);
}
