package com.heronix.service.impl;

import com.heronix.model.domain.*;
import com.heronix.model.domain.Poll.PollStatus;
import com.heronix.model.domain.Poll.TargetAudience;
import com.heronix.repository.*;
import com.heronix.service.PollService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final PollQuestionRepository pollQuestionRepository;
    private final PollResponseRepository pollResponseRepository;
    private final PollAnswerRepository pollAnswerRepository;

    @Override
    @Transactional
    public Poll createPoll(Poll poll) {
        poll.setStatus(PollStatus.DRAFT);
        if (poll.getQuestions() != null) {
            for (int i = 0; i < poll.getQuestions().size(); i++) {
                PollQuestion q = poll.getQuestions().get(i);
                q.setPoll(poll);
                q.setDisplayOrder(i);
            }
        }
        Poll saved = pollRepository.save(poll);
        log.info("Created poll: {} by {}", saved.getTitle(), saved.getCreatorName());
        return saved;
    }

    @Override
    @Transactional
    public Poll updatePoll(Long id, Poll updated) {
        Poll poll = getPollById(id);
        if (poll.getStatus() != PollStatus.DRAFT) {
            throw new IllegalStateException("Can only edit polls in DRAFT status");
        }
        poll.setTitle(updated.getTitle());
        poll.setDescription(updated.getDescription());
        poll.setTargetAudience(updated.getTargetAudience());
        poll.setStartDate(updated.getStartDate());
        poll.setEndDate(updated.getEndDate());
        poll.setIsAnonymous(updated.getIsAnonymous());
        poll.setAllowMultipleResponses(updated.getAllowMultipleResponses());
        poll.setResultsVisibility(updated.getResultsVisibility());

        // Replace questions
        poll.getQuestions().clear();
        if (updated.getQuestions() != null) {
            for (int i = 0; i < updated.getQuestions().size(); i++) {
                PollQuestion q = updated.getQuestions().get(i);
                q.setPoll(poll);
                q.setDisplayOrder(i);
                poll.getQuestions().add(q);
            }
        }

        return pollRepository.save(poll);
    }

    @Override
    @Transactional(readOnly = true)
    public Poll getPollById(Long id) {
        return pollRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Poll not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> getAllPolls() {
        return pollRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void deletePoll(Long id) {
        Poll poll = getPollById(id);
        log.info("Deleting poll: {}", poll.getTitle());
        pollRepository.delete(poll);
    }

    @Override
    @Transactional
    public Poll publishPoll(Long id) {
        Poll poll = getPollById(id);
        if (poll.getQuestions() == null || poll.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish a poll with no questions");
        }
        poll.setStatus(PollStatus.PUBLISHED);
        log.info("Published poll: {}", poll.getTitle());
        return pollRepository.save(poll);
    }

    @Override
    @Transactional
    public Poll closePoll(Long id) {
        Poll poll = getPollById(id);
        poll.setStatus(PollStatus.CLOSED);
        log.info("Closed poll: {}", poll.getTitle());
        return pollRepository.save(poll);
    }

    @Override
    @Transactional
    public PollResponse submitResponse(Long pollId, PollResponse response) {
        Poll poll = getPollById(pollId);

        if (!poll.isActive() && poll.getStatus() != PollStatus.PUBLISHED) {
            throw new IllegalStateException("Poll is not accepting responses");
        }

        // Check duplicate responses
        if (!poll.getAllowMultipleResponses() && response.getRespondentId() != null) {
            if (pollResponseRepository.existsByPollIdAndRespondentIdAndRespondentType(
                    pollId, response.getRespondentId(), response.getRespondentType())) {
                throw new IllegalStateException("You have already responded to this poll");
            }
        }

        response.setPoll(poll);
        response.setSubmittedAt(LocalDateTime.now());

        // Handle anonymous
        if (poll.getIsAnonymous()) {
            response.setRespondentName(null);
        }

        // Link answers to response
        if (response.getAnswers() != null) {
            for (PollAnswer answer : response.getAnswers()) {
                answer.setPollResponse(response);
            }
        }

        PollResponse saved = pollResponseRepository.save(response);

        // Update total responses count
        poll.setTotalResponses(pollResponseRepository.countByPollId(pollId).intValue());
        pollRepository.save(poll);

        log.info("Response submitted for poll: {} by {}", poll.getTitle(),
                poll.getIsAnonymous() ? "anonymous" : response.getRespondentName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserResponded(Long pollId, Long userId, String userType) {
        return pollRepository.hasUserResponded(pollId, userId, userType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPollResults(Long pollId) {
        Poll poll = getPollById(pollId);
        List<PollQuestion> questions = pollQuestionRepository.findByPollIdOrderByDisplayOrderAsc(pollId);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("pollId", pollId);
        results.put("title", poll.getTitle());
        results.put("totalResponses", poll.getTotalResponses());
        results.put("status", poll.getStatus().name());

        List<Map<String, Object>> questionResults = new ArrayList<>();
        for (PollQuestion question : questions) {
            Map<String, Object> qr = new LinkedHashMap<>();
            qr.put("questionId", question.getId());
            qr.put("questionText", question.getQuestionText());
            qr.put("questionType", question.getQuestionType().name());

            List<PollAnswer> answers = pollAnswerRepository.findByPollQuestionId(question.getId());

            if (question.getQuestionType() == PollQuestion.QuestionType.SHORT_TEXT) {
                // Collect text answers
                List<String> texts = answers.stream()
                        .map(PollAnswer::getTextAnswer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                qr.put("textAnswers", texts);
                qr.put("answerCount", texts.size());
            } else {
                // Count option selections
                Map<String, Integer> optionCounts = new LinkedHashMap<>();
                for (String option : question.getOptions()) {
                    optionCounts.put(option, 0);
                }
                if (question.getQuestionType() == PollQuestion.QuestionType.YES_NO) {
                    optionCounts.put("Yes", 0);
                    optionCounts.put("No", 0);
                }

                for (PollAnswer answer : answers) {
                    for (String sel : answer.getSelectedOptions()) {
                        optionCounts.merge(sel, 1, Integer::sum);
                    }
                }

                int totalAnswers = answers.size();
                List<Map<String, Object>> optionResults = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : optionCounts.entrySet()) {
                    Map<String, Object> or = new LinkedHashMap<>();
                    or.put("option", entry.getKey());
                    or.put("count", entry.getValue());
                    or.put("percentage", totalAnswers > 0
                            ? Math.round((entry.getValue() * 100.0) / totalAnswers) : 0);
                    optionResults.add(or);
                }
                qr.put("options", optionResults);
                qr.put("answerCount", totalAnswers);
            }

            questionResults.add(qr);
        }

        results.put("questions", questionResults);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> getActivePollsForAudience(TargetAudience audience) {
        return pollRepository.findActivePollsForAudience(audience, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> getPollsByCreator(String creatorName) {
        return pollRepository.findByCreatorNameOrderByCreatedAtDesc(creatorName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Poll> searchPolls(String term) {
        return pollRepository.searchPolls(term);
    }
}
