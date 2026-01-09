package com.heronix.service.impl;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.DailyAttendance;
import com.heronix.model.domain.DailyAttendance.OverallStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.DailyAttendanceRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.DailyAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DailyAttendanceService
 * Aggregates period-by-period attendance into daily summaries
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAttendanceServiceImpl implements DailyAttendanceService {

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // DAILY SUMMARY MANAGEMENT
    // ========================================================================

    @Override
    @Transactional
    public DailyAttendance generateDailySummary(Long studentId, LocalDate date) {
        log.debug("Generating daily attendance summary for student ID: {} on {}", studentId, date);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // Get all period attendance records for this student on this date
        List<AttendanceRecord> periodRecords = attendanceRepository.findByStudentIdAndAttendanceDate(studentId, date);

        if (periodRecords.isEmpty()) {
            log.warn("No attendance records found for student ID: {} on {}", studentId, date);
            return null;
        }

        // Calculate summary statistics
        int totalPeriods = periodRecords.size();
        int periodsPresent = (int) periodRecords.stream().filter(AttendanceRecord::isPresent).count();
        int periodsAbsent = (int) periodRecords.stream().filter(AttendanceRecord::isAbsent).count();
        int periodsTardy = (int) periodRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.TARDY)
                .count();

        // Get first arrival and last departure times
        LocalTime firstArrival = periodRecords.stream()
                .map(AttendanceRecord::getArrivalTime)
                .filter(Objects::nonNull)
                .min(LocalTime::compareTo)
                .orElse(null);

        LocalTime lastDeparture = periodRecords.stream()
                .map(AttendanceRecord::getDepartureTime)
                .filter(Objects::nonNull)
                .max(LocalTime::compareTo)
                .orElse(null);

        // Calculate total minutes present (estimate)
        int totalMinutesPresent = periodsPresent * 50; // Assuming 50 min periods

        // Determine overall status
        OverallStatus overallStatus = determineOverallStatus(periodsPresent, periodsAbsent, totalPeriods);

        // Check if summary already exists
        Optional<DailyAttendance> existingOpt = dailyAttendanceRepository
                .findByStudentIdAndAttendanceDate(studentId, date);

        DailyAttendance dailyAttendance;
        if (existingOpt.isPresent()) {
            // Update existing
            dailyAttendance = existingOpt.get();
            dailyAttendance.setPeriodsPresent(periodsPresent);
            dailyAttendance.setPeriodsAbsent(periodsAbsent);
            dailyAttendance.setPeriodsTardy(periodsTardy);
            dailyAttendance.setTotalPeriods(totalPeriods);
            dailyAttendance.setOverallStatus(overallStatus);
            dailyAttendance.setFirstArrival(firstArrival);
            dailyAttendance.setLastDeparture(lastDeparture);
            dailyAttendance.setTotalMinutesPresent(totalMinutesPresent);
        } else {
            // Create new
            dailyAttendance = DailyAttendance.builder()
                    .student(student)
                    .attendanceDate(date)
                    .overallStatus(overallStatus)
                    .periodsPresent(periodsPresent)
                    .periodsAbsent(periodsAbsent)
                    .periodsTardy(periodsTardy)
                    .totalPeriods(totalPeriods)
                    .firstArrival(firstArrival)
                    .lastDeparture(lastDeparture)
                    .totalMinutesPresent(totalMinutesPresent)
                    .campus(student.getCampus())
                    .build();
        }

        DailyAttendance saved = dailyAttendanceRepository.save(dailyAttendance);
        log.info("Daily attendance summary generated for student ID: {} on {}, status: {}",
                studentId, date, overallStatus);

        return saved;
    }

    @Override
    @Transactional
    public List<DailyAttendance> generateDailySummariesForDate(LocalDate date, Long campusId) {
        log.info("Generating daily attendance summaries for all students on {}, campus ID: {}", date, campusId);

        List<AttendanceRecord> allRecords;
        if (campusId != null) {
            allRecords = attendanceRepository.findByAttendanceDateAndCampusId(date, campusId);
        } else {
            allRecords = attendanceRepository.findByStudentIdAndAttendanceDate(null, date);
        }

        // Group by student
        Map<Long, List<AttendanceRecord>> recordsByStudent = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        List<DailyAttendance> summaries = new ArrayList<>();
        for (Long studentId : recordsByStudent.keySet()) {
            DailyAttendance summary = generateDailySummary(studentId, date);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        log.info("Generated {} daily attendance summaries for {}", summaries.size(), date);
        return summaries;
    }

    @Override
    @Transactional
    public DailyAttendance updateDailySummary(Long dailyAttendanceId, DailyAttendance updatedData) {
        log.info("Updating daily attendance summary ID: {}", dailyAttendanceId);

        DailyAttendance existing = dailyAttendanceRepository.findById(dailyAttendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Daily attendance not found with ID: " + dailyAttendanceId));

        existing.setOverallStatus(updatedData.getOverallStatus());
        existing.setPeriodsPresent(updatedData.getPeriodsPresent());
        existing.setPeriodsAbsent(updatedData.getPeriodsAbsent());
        existing.setPeriodsTardy(updatedData.getPeriodsTardy());
        existing.setTotalPeriods(updatedData.getTotalPeriods());
        existing.setNotes(updatedData.getNotes());

        DailyAttendance saved = dailyAttendanceRepository.save(existing);
        log.info("Daily attendance summary ID: {} updated successfully", dailyAttendanceId);

        return saved;
    }

    @Override
    @Transactional
    public DailyAttendance recalculateDailySummary(Long studentId, LocalDate date) {
        log.debug("Recalculating daily attendance summary for student ID: {} on {}", studentId, date);
        return generateDailySummary(studentId, date);
    }

    // ========================================================================
    // QUERIES
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public DailyAttendance getDailyAttendance(Long studentId, LocalDate date) {
        log.debug("Getting daily attendance for student ID: {} on {}", studentId, date);
        return dailyAttendanceRepository.findByStudentIdAndAttendanceDate(studentId, date).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getDailyAttendanceRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting daily attendance for student ID: {} from {} to {}", studentId, startDate, endDate);
        return dailyAttendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getDailyAttendanceForDate(LocalDate date) {
        log.debug("Getting all daily attendance for {}", date);
        return dailyAttendanceRepository.findByAttendanceDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getDailyAttendanceForCampus(Long campusId, LocalDate date) {
        log.debug("Getting daily attendance for campus ID: {} on {}", campusId, date);
        return dailyAttendanceRepository.findByAttendanceDateAndCampusId(date, campusId);
    }

    // ========================================================================
    // STATISTICS & ANALYTICS
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRate(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating attendance rate for student ID: {} from {} to {}", studentId, startDate, endDate);

        Double rate = dailyAttendanceRepository.getAttendanceRate(studentId, startDate, endDate);
        return rate != null ? Math.round(rate * 100.0) / 100.0 : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyStatistics(LocalDate date, Long campusId) {
        log.debug("Getting daily attendance statistics for {} campus ID: {}", date, campusId);

        List<Object[]> results = campusId != null
                ? dailyAttendanceRepository.getDailyStatisticsByCampus(date, campusId)
                : dailyAttendanceRepository.getDailyStatistics(date);

        Map<String, Object> stats = new LinkedHashMap<>();
        int total = 0;

        for (Object[] row : results) {
            OverallStatus status = (OverallStatus) row[0];
            Long count = (Long) row[1];
            stats.put(status.name(), count);
            total += count.intValue();
        }

        stats.put("TOTAL", total);

        int present = ((Long) stats.getOrDefault("PRESENT", 0L)).intValue();
        double attendanceRate = total > 0 ? (double) present / total * 100 : 0.0;
        stats.put("ATTENDANCE_RATE", Math.round(attendanceRate * 100.0) / 100.0);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateADA(Long campusId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating ADA for campus ID: {} from {} to {}", campusId, startDate, endDate);

        Double ada = dailyAttendanceRepository.calculateADA(campusId, startDate, endDate);
        return ada != null ? Math.round(ada * 10000.0) / 100.0 : null; // Convert to percentage
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateADM(Long campusId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating ADM for campus ID: {} from {} to {}", campusId, startDate, endDate);

        // ADM = total enrolled students across period / number of days
        List<DailyAttendance> records = dailyAttendanceRepository.findByAttendanceDateAndCampusId(startDate, campusId);

        long dayCount = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        if (dayCount <= 0) return 0.0;

        // Get unique students
        Set<Long> uniqueStudents = new HashSet<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<DailyAttendance> dayRecords = dailyAttendanceRepository.findByAttendanceDateAndCampusId(currentDate, campusId);
            dayRecords.forEach(r -> uniqueStudents.add(r.getStudent().getId()));
            currentDate = currentDate.plusDays(1);
        }

        return (double) uniqueStudents.size();
    }

    // ========================================================================
    // ATTENDANCE PATTERNS
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<Long> findChronicAbsentStudents(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding chronic absent students from {} to {}", startDate, endDate);
        return dailyAttendanceRepository.findChronicAbsentStudents(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findPerfectAttendanceStudents(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding perfect attendance students from {} to {}", startDate, endDate);

        long expectedDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        return dailyAttendanceRepository.findPerfectAttendanceStudents(startDate, endDate, expectedDays);
    }

    @Override
    @Transactional(readOnly = true)
    public int getConsecutiveAbsences(Long studentId, LocalDate fromDate) {
        log.debug("Getting consecutive absences for student ID: {} from {}", studentId, fromDate);

        List<DailyAttendance> absences = dailyAttendanceRepository.findConsecutiveAbsences(studentId, fromDate);

        if (absences.isEmpty()) {
            return 0;
        }

        // Count consecutive days
        int consecutive = 1;
        LocalDate expectedDate = absences.get(0).getAttendanceDate().plusDays(1);

        for (int i = 1; i < absences.size(); i++) {
            if (absences.get(i).getAttendanceDate().equals(expectedDate)) {
                consecutive++;
                expectedDate = expectedDate.plusDays(1);
            } else {
                break;
            }
        }

        return consecutive;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getAbsentStudents(LocalDate date, Long campusId) {
        log.debug("Getting absent students for {} campus ID: {}", date, campusId);

        if (campusId != null) {
            return dailyAttendanceRepository.findByAttendanceDateAndCampusId(date, campusId).stream()
                    .filter(d -> d.getOverallStatus() == OverallStatus.ABSENT)
                    .collect(Collectors.toList());
        } else {
            return dailyAttendanceRepository.findByAttendanceDateAndOverallStatus(date, OverallStatus.ABSENT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getTardyStudents(LocalDate date, Long campusId) {
        log.debug("Getting tardy students for {} campus ID: {}", date, campusId);

        if (campusId != null) {
            return dailyAttendanceRepository.findByAttendanceDateAndCampusId(date, campusId).stream()
                    .filter(d -> d.getOverallStatus() == OverallStatus.TARDY)
                    .collect(Collectors.toList());
        } else {
            return dailyAttendanceRepository.findByAttendanceDateAndOverallStatus(date, OverallStatus.TARDY);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private OverallStatus determineOverallStatus(int periodsPresent, int periodsAbsent, int totalPeriods) {
        if (totalPeriods == 0) {
            return OverallStatus.ABSENT;
        }

        double presentRate = (double) periodsPresent / totalPeriods;

        if (periodsAbsent == totalPeriods) {
            return OverallStatus.ABSENT;
        } else if (periodsPresent == totalPeriods) {
            return OverallStatus.PRESENT;
        } else if (presentRate >= 0.5) {
            return OverallStatus.PARTIAL;
        } else if (presentRate >= 0.25) {
            return OverallStatus.HALF_DAY;
        } else {
            return OverallStatus.ABSENT;
        }
    }
}
