package com.heronix.repository;

import com.heronix.model.domain.PollResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollResponseRepository extends JpaRepository<PollResponse, Long> {

    List<PollResponse> findByPollIdOrderBySubmittedAtDesc(Long pollId);

    Long countByPollId(Long pollId);

    boolean existsByPollIdAndRespondentIdAndRespondentType(Long pollId, Long respondentId, String respondentType);
}
