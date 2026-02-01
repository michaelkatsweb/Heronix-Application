package com.heronix.repository;

import com.heronix.model.domain.Poll;
import com.heronix.model.domain.Poll.PollStatus;
import com.heronix.model.domain.Poll.TargetAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    List<Poll> findByStatusOrderByCreatedAtDesc(PollStatus status);

    List<Poll> findByCreatorNameOrderByCreatedAtDesc(String creatorName);

    @Query("SELECT p FROM Poll p WHERE p.status = 'PUBLISHED' " +
           "AND (p.targetAudience = :audience OR p.targetAudience = 'ALL') " +
           "AND (p.startDate IS NULL OR p.startDate <= :now) " +
           "AND (p.endDate IS NULL OR p.endDate >= :now) " +
           "ORDER BY p.createdAt DESC")
    List<Poll> findActivePollsForAudience(@Param("audience") TargetAudience audience,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END " +
           "FROM PollResponse pr WHERE pr.poll.id = :pollId " +
           "AND pr.respondentId = :userId AND pr.respondentType = :userType")
    boolean hasUserResponded(@Param("pollId") Long pollId,
                             @Param("userId") Long userId,
                             @Param("userType") String userType);

    @Query("SELECT p FROM Poll p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Poll> searchPolls(@Param("term") String term);

    List<Poll> findAllByOrderByCreatedAtDesc();
}
