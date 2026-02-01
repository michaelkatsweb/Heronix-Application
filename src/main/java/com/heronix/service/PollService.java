package com.heronix.service;

import com.heronix.model.domain.Poll;
import com.heronix.model.domain.Poll.TargetAudience;
import com.heronix.model.domain.PollResponse;

import java.util.List;
import java.util.Map;

public interface PollService {

    Poll createPoll(Poll poll);

    Poll updatePoll(Long id, Poll poll);

    Poll getPollById(Long id);

    List<Poll> getAllPolls();

    void deletePoll(Long id);

    Poll publishPoll(Long id);

    Poll closePoll(Long id);

    PollResponse submitResponse(Long pollId, PollResponse response);

    boolean hasUserResponded(Long pollId, Long userId, String userType);

    Map<String, Object> getPollResults(Long pollId);

    List<Poll> getActivePollsForAudience(TargetAudience audience);

    List<Poll> getPollsByCreator(String creatorName);

    List<Poll> searchPolls(String term);
}
