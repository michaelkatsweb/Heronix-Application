package com.heronix.repository;

import com.heronix.model.domain.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Schedule Repository
 *
 * Data access layer for report schedule management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 57 - Scheduled Report Generation
 */
@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {

    /**
     * Find all active schedules
     *
     * @param active Active status
     * @return List of active schedules
     */
    List<ReportSchedule> findByActive(Boolean active);

    /**
     * Find schedules due for execution
     *
     * @param now Current timestamp
     * @return List of due schedules
     */
    @Query("SELECT rs FROM ReportSchedule rs WHERE rs.active = true AND " +
           "(rs.nextExecution IS NULL OR rs.nextExecution <= :now)")
    List<ReportSchedule> findDueSchedules(LocalDateTime now);

    /**
     * Find schedules by frequency
     *
     * @param frequency Schedule frequency
     * @return List of schedules
     */
    List<ReportSchedule> findByFrequency(ReportSchedule.ScheduleFrequency frequency);

    /**
     * Find schedules by created by user
     *
     * @param createdBy User who created the schedule
     * @return List of schedules
     */
    List<ReportSchedule> findByCreatedBy(String createdBy);

    /**
     * Find schedules by report type
     *
     * @param reportType Report type
     * @return List of schedules
     */
    List<ReportSchedule> findByReportType(com.heronix.model.domain.ReportHistory.ReportType reportType);

    /**
     * Count active schedules
     *
     * @param active Active status
     * @return Count of active schedules
     */
    long countByActive(Boolean active);
}
