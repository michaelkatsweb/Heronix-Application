package com.heronix.repository;

import com.heronix.model.domain.PollQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollQuestionRepository extends JpaRepository<PollQuestion, Long> {

    List<PollQuestion> findByPollIdOrderByDisplayOrderAsc(Long pollId);
}
