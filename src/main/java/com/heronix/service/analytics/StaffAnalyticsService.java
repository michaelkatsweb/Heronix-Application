package com.heronix.service.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.model.domain.Staff;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.StaffRepository;
import com.heronix.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Staff Analytics Service
 *
 * Provides comprehensive analytics for all personnel including:
 * - Teachers (instructional staff) from Teacher entity
 * - Staff (non-instructional) from Staff entity
 *
 * Analytics include:
 * - Certification tracking and expiration alerts
 * - Workload distribution (for teachers)
 * - Experience distribution
 * - Department breakdown
 * - Occupation distribution (for non-teaching staff)
 *
 * @author Heronix SIS Team
 * @version 2.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 * @updated Phase 59 - Staff/Teacher Separation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffAnalyticsService {

    private final TeacherRepository teacherRepository;
    private final StaffRepository staffRepository;

    // ========================================================================
    // STAFF OVERVIEW
    // ========================================================================

    /**
     * Get comprehensive staff analytics
     */
    @Cacheable(value = "staffAnalytics", key = "#filter.campusId ?: 'all'")
    public StaffAnalyticsDTO getStaffAnalytics(AnalyticsFilterDTO filter) {
        log.info("Fetching staff analytics for campus: {}", filter.getCampusId());

        Long campusId = filter.getCampusId();

        // Get certification summary data
        Map<String, Object> certSummary = getCertificationSummary(campusId);
        Map<String, Object> workloadSummary = getWorkloadSummary(campusId);

        return StaffAnalyticsDTO.builder()
                .campusId(campusId)
                .totalStaff(getTotalActiveStaff(campusId))
                .activeStaff(getTotalActiveStaff(campusId))
                // Certification metrics
                .certifiedCount(certSummary.get("totalCertified") != null ? ((Number) certSummary.get("totalCertified")).longValue() : 0L)
                .expiringSoonCount(certSummary.get("totalExpiringSoon") != null ? ((Number) certSummary.get("totalExpiringSoon")).longValue() : 0L)
                .expiredCertificationCount(certSummary.get("expired") != null ? ((Number) certSummary.get("expired")).longValue() : 0L)
                .certificationComplianceRate(certSummary.get("certificationRate") != null ? ((Number) certSummary.get("certificationRate")).doubleValue() : 0.0)
                // Experience distribution
                .experienceDistribution(getExperienceDistribution(campusId))
                .averageYearsExperience(getAverageExperience(campusId))
                // Workload metrics
                .averageClassesPerTeacher(workloadSummary.get("avgCourses") != null ? ((Number) workloadSummary.get("avgCourses")).doubleValue() : 0.0)
                .overloadedTeacherCount(workloadSummary.get("overloaded") != null ? ((Number) workloadSummary.get("overloaded")).longValue() : 0L)
                .build();
    }

    /**
     * Get total active staff count (Teachers + Staff combined)
     */
    public Long getTotalActiveStaff(Long campusId) {
        Long teachers = teacherRepository.countActiveTeachers(campusId);
        Long staff = campusId != null ?
                staffRepository.countActiveStaffByCampus(campusId) :
                staffRepository.countActiveStaff();
        return teachers + staff;
    }

    /**
     * Get total active teachers only
     */
    public Long getTotalActiveTeachers(Long campusId) {
        return teacherRepository.countActiveTeachers(campusId);
    }

    /**
     * Get total active non-teaching staff only
     */
    public Long getTotalActiveNonTeachingStaff(Long campusId) {
        return campusId != null ?
                staffRepository.countActiveStaffByCampus(campusId) :
                staffRepository.countActiveStaff();
    }

    // ========================================================================
    // CERTIFICATION ANALYTICS
    // ========================================================================

    /**
     * Get certification summary
     */
    public Map<String, Object> getCertificationSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate thirtyDays = today.plusDays(30);
        LocalDate sixtyDays = today.plusDays(60);
        LocalDate ninetyDays = today.plusDays(90);

        // Count certified teachers
        Long certified = teacherRepository.countCertifiedTeachers(today, campusId);
        Long total = teacherRepository.countActiveTeachers(campusId);

        summary.put("totalCertified", certified);
        summary.put("totalStaff", total);
        summary.put("certificationRate", total > 0 ? certified * 100.0 / total : 0.0);

        // Count expiring certifications
        List<Teacher> expiring30 = teacherRepository.findTeachersWithExpiringCertifications(today, thirtyDays, campusId);
        List<Teacher> expiring60 = teacherRepository.findTeachersWithExpiringCertifications(today, sixtyDays, campusId);
        List<Teacher> expiring90 = teacherRepository.findTeachersWithExpiringCertifications(today, ninetyDays, campusId);

        summary.put("expiringIn30Days", expiring30.size());
        summary.put("expiringIn60Days", expiring60.size() - expiring30.size());
        summary.put("expiringIn90Days", expiring90.size() - expiring60.size());
        summary.put("totalExpiringSoon", expiring90.size());

        // Count expired
        List<Teacher> expired = teacherRepository.findTeachersWithExpiredCertifications(today, campusId);
        summary.put("expired", expired.size());

        return summary;
    }

    /**
     * Get list of teachers with expiring certifications
     */
    public List<Map<String, Object>> getExpiringCertifications(Long campusId, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        List<Teacher> expiring = teacherRepository.findTeachersWithExpiringCertifications(today, futureDate, campusId);

        return expiring.stream()
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("teacherId", t.getId());
                    map.put("employeeId", t.getEmployeeId());
                    map.put("name", t.getName());
                    map.put("department", t.getDepartment());
                    map.put("expirationDate", t.getCertificationExpirationDate());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ========================================================================
    // EXPERIENCE DISTRIBUTION
    // ========================================================================

    /**
     * Get experience distribution (years of experience ranges)
     */
    @Cacheable(value = "experienceDistribution", key = "#campusId ?: 'all'")
    public Map<String, Long> getExperienceDistribution(Long campusId) {
        List<Object[]> data = teacherRepository.getExperienceDistribution(campusId);

        Map<String, Long> distribution = new LinkedHashMap<>();
        // Initialize with expected ranges in order
        String[] ranges = {"0-2 Years", "3-5 Years", "6-10 Years", "11-20 Years", "20+ Years"};
        for (String range : ranges) {
            distribution.put(range, 0L);
        }

        for (Object[] row : data) {
            if (row[0] != null) {
                String range = (String) row[0];
                Long count = row[1] != null ? (Long) row[1] : 0L;
                distribution.put(range, count);
            }
        }

        return distribution;
    }

    /**
     * Get average years of experience
     */
    public Double getAverageExperience(Long campusId) {
        Double avg = teacherRepository.getAverageYearsExperience(campusId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ========================================================================
    // WORKLOAD DISTRIBUTION
    // ========================================================================

    /**
     * Get workload distribution (courses per teacher)
     */
    public List<Map<String, Object>> getWorkloadDistribution(Long campusId) {
        List<Object[]> data = teacherRepository.getTeacherWorkloadDistribution(campusId);

        return data.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("teacherId", row[0]);
                    map.put("teacherName", row[1]);
                    map.put("courseCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get workload summary statistics
     */
    public Map<String, Object> getWorkloadSummary(Long campusId) {
        List<Object[]> data = teacherRepository.getTeacherWorkloadDistribution(campusId);

        Map<String, Object> summary = new LinkedHashMap<>();

        if (data.isEmpty()) {
            summary.put("avgCourses", 0.0);
            summary.put("maxCourses", 0);
            summary.put("minCourses", 0);
            summary.put("overloaded", 0);
            return summary;
        }

        // Calculate statistics
        List<Integer> courseCounts = data.stream()
                .map(row -> ((Number) row[2]).intValue())
                .collect(Collectors.toList());

        double avg = courseCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int max = courseCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
        int min = courseCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
        long overloaded = courseCounts.stream().filter(c -> c > 5).count(); // >5 courses considered overloaded

        summary.put("avgCourses", Math.round(avg * 10.0) / 10.0);
        summary.put("maxCourses", max);
        summary.put("minCourses", min);
        summary.put("overloaded", overloaded);

        return summary;
    }

    // ========================================================================
    // DEPARTMENT BREAKDOWN
    // ========================================================================

    /**
     * Get staff counts by department
     */
    @Cacheable(value = "departmentBreakdown", key = "#campusId ?: 'all'")
    public Map<String, Long> getDepartmentBreakdown(Long campusId) {
        List<Object[]> data = teacherRepository.getStaffCountsByDepartment(campusId);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : data) {
            if (row[0] != null) {
                String dept = (String) row[0];
                Long count = row[1] != null ? (Long) row[1] : 0L;
                breakdown.put(dept, count);
            }
        }

        return breakdown;
    }

    /**
     * Get certification status distribution
     */
    public Map<String, Long> getCertificationStatusDistribution(Long campusId) {
        List<Object[]> data = teacherRepository.getCertificationStatusDistribution(campusId);

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : data) {
            if (row[0] != null) {
                String status = row[0].toString();
                Long count = row[1] != null ? (Long) row[1] : 0L;
                distribution.put(status, count);
            }
        }

        return distribution;
    }

    // ========================================================================
    // NON-TEACHING STAFF ANALYTICS (from Staff entity)
    // ========================================================================

    /**
     * Get non-teaching staff occupation distribution
     */
    public Map<String, Long> getStaffOccupationDistribution(Long campusId) {
        List<Object[]> data;
        if (campusId != null) {
            data = staffRepository.getOccupationDistributionByCampus(campusId);
        } else {
            data = staffRepository.getOccupationDistribution();
        }

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : data) {
            if (row[0] != null) {
                String occupation = row[0].toString();
                Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                distribution.put(occupation, count);
            }
        }

        return distribution;
    }

    /**
     * Get non-teaching staff department breakdown
     */
    public Map<String, Long> getStaffDepartmentBreakdown(Long campusId) {
        List<Object[]> data;
        if (campusId != null) {
            data = staffRepository.getDepartmentBreakdownByCampus(campusId);
        } else {
            data = staffRepository.getDepartmentBreakdown();
        }

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : data) {
            String dept = row[0] != null ? row[0].toString() : "Unassigned";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            breakdown.put(dept, count);
        }

        return breakdown;
    }

    /**
     * Get non-teaching staff experience distribution
     */
    public Map<String, Long> getStaffExperienceDistribution(Long campusId) {
        List<Object[]> data;
        if (campusId != null) {
            data = staffRepository.getExperienceDistributionByCampus(campusId);
        } else {
            data = staffRepository.getExperienceDistribution();
        }

        Map<String, Long> distribution = new LinkedHashMap<>();
        String[] order = {"0-1 years", "2-4 years", "5-9 years", "10-19 years", "20+ years", "Unknown"};

        Map<String, Long> tempMap = new HashMap<>();
        for (Object[] row : data) {
            String range = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            tempMap.put(range, count);
        }

        for (String range : order) {
            if (tempMap.containsKey(range)) {
                distribution.put(range, tempMap.get(range));
            }
        }

        return distribution;
    }

    /**
     * Get non-teaching staff compliance summary (background checks, I-9)
     */
    public Map<String, Object> getStaffComplianceSummary(Long campusId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        Long totalStaff = getTotalActiveNonTeachingStaff(campusId);
        Long validBackgroundChecks = staffRepository.countWithValidBackgroundCheck(today);
        Long expiredBackgroundChecks = staffRepository.countExpiredBackgroundChecks(today);
        Long expiringIn30Days = staffRepository.countExpiringBackgroundChecks(today, today.plusDays(30));
        Long expiringIn90Days = staffRepository.countExpiringBackgroundChecks(today, today.plusDays(90));
        Long validI9 = staffRepository.countWithValidI9(today);
        Long missingI9 = staffRepository.countMissingI9();

        summary.put("totalStaff", totalStaff);
        summary.put("validBackgroundChecks", validBackgroundChecks);
        summary.put("expiredBackgroundChecks", expiredBackgroundChecks);
        summary.put("expiringIn30Days", expiringIn30Days);
        summary.put("totalExpiringSoon", expiringIn90Days);
        summary.put("validI9", validI9);
        summary.put("missingI9", missingI9);

        if (totalStaff > 0) {
            double complianceRate = (validBackgroundChecks.doubleValue() / totalStaff.doubleValue()) * 100.0;
            summary.put("complianceRate", Math.round(complianceRate * 10.0) / 10.0);
        } else {
            summary.put("complianceRate", 100.0);
        }

        return summary;
    }

    /**
     * Get staff with expiring background checks
     */
    public List<Map<String, Object>> getStaffWithExpiringBackgroundChecks(Long campusId, int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);

        List<Staff> expiringStaff;
        if (campusId != null) {
            expiringStaff = staffRepository.findWithExpiringBackgroundChecksByCampus(campusId, futureDate);
        } else {
            expiringStaff = staffRepository.findWithExpiringBackgroundChecks(futureDate);
        }

        return expiringStaff.stream()
                .map(s -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("staffId", s.getId());
                    map.put("employeeId", s.getEmployeeId());
                    map.put("name", s.getFullName());
                    map.put("occupation", s.getOccupationDisplay());
                    map.put("department", s.getDepartment());
                    map.put("expirationDate", s.getBackgroundCheckExpiration());
                    if (s.getBackgroundCheckExpiration() != null) {
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), s.getBackgroundCheckExpiration());
                        map.put("daysLeft", daysLeft);
                    } else {
                        map.put("daysLeft", 0);
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get combined experience distribution (teachers + staff)
     */
    public Map<String, Long> getCombinedExperienceDistribution(Long campusId) {
        Map<String, Long> teacherExp = getExperienceDistribution(campusId);
        Map<String, Long> staffExp = getStaffExperienceDistribution(campusId);

        // Merge the two distributions
        Map<String, Long> combined = new LinkedHashMap<>();

        // Add teacher data
        for (Map.Entry<String, Long> entry : teacherExp.entrySet()) {
            combined.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        // Add staff data (may need to normalize range labels)
        for (Map.Entry<String, Long> entry : staffExp.entrySet()) {
            combined.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        return combined;
    }

    /**
     * Get combined department breakdown (teachers + staff)
     */
    public Map<String, Long> getCombinedDepartmentBreakdown(Long campusId) {
        Map<String, Long> teacherDepts = getDepartmentBreakdown(campusId);
        Map<String, Long> staffDepts = getStaffDepartmentBreakdown(campusId);

        Map<String, Long> combined = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : teacherDepts.entrySet()) {
            combined.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        for (Map.Entry<String, Long> entry : staffDepts.entrySet()) {
            combined.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        // Sort by count descending
        return combined.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Get personnel breakdown by type
     */
    public Map<String, Long> getPersonnelTypeBreakdown(Long campusId) {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("Teachers", getTotalActiveTeachers(campusId));
        breakdown.put("Non-Teaching Staff", getTotalActiveNonTeachingStaff(campusId));
        return breakdown;
    }
}
