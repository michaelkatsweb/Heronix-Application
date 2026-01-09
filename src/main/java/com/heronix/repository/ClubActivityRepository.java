package com.heronix.repository;

import com.heronix.model.domain.ClubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ClubActivity entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface ClubActivityRepository extends JpaRepository<ClubActivity, Long> {

    List<ClubActivity> findByClubId(Long clubId);

    List<ClubActivity> findByClubIdOrderByActivityDateDesc(Long clubId);

    List<ClubActivity> findByActivityType(ClubActivity.ActivityType activityType);

    List<ClubActivity> findByStatus(ClubActivity.ActivityStatus status);

    @Query("SELECT a FROM ClubActivity a WHERE a.club.id = :clubId AND a.activityDate > :now AND a.status != 'CANCELLED' ORDER BY a.activityDate")
    List<ClubActivity> findUpcomingByClub(@Param("clubId") Long clubId, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM ClubActivity a WHERE a.activityDate > :now AND a.status != 'CANCELLED' ORDER BY a.activityDate")
    List<ClubActivity> findAllUpcoming(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM ClubActivity a WHERE a.club.id = :clubId AND a.status = 'COMPLETED' ORDER BY a.activityDate DESC")
    List<ClubActivity> findCompletedByClub(@Param("clubId") Long clubId);

    @Query("SELECT a FROM ClubActivity a WHERE a.activityDate BETWEEN :startDate AND :endDate ORDER BY a.activityDate")
    List<ClubActivity> findActivitiesInDateRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM ClubActivity a WHERE a.club.id = :clubId AND a.activityDate BETWEEN :startDate AND :endDate ORDER BY a.activityDate")
    List<ClubActivity> findClubActivitiesInDateRange(@Param("clubId") Long clubId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM ClubActivity a WHERE a.openToNonMembers = true AND a.activityDate > :now AND a.status != 'CANCELLED' ORDER BY a.activityDate")
    List<ClubActivity> findOpenActivities(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM ClubActivity a WHERE a.activityType = 'SERVICE_PROJECT' AND a.status = 'COMPLETED' ORDER BY a.activityDate DESC")
    List<ClubActivity> findServiceProjects();

    @Query("SELECT COUNT(a) FROM ClubActivity a WHERE a.club.id = :clubId AND a.status = 'COMPLETED'")
    long countCompletedByClub(@Param("clubId") Long clubId);

    @Query("SELECT COALESCE(SUM(a.serviceHoursAwarded), 0) FROM ClubActivity a WHERE a.club.id = :clubId AND a.status = 'COMPLETED'")
    int sumServiceHoursByClub(@Param("clubId") Long clubId);
}
