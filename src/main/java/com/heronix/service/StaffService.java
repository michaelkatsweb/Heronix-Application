package com.heronix.service;

import com.heronix.model.domain.Staff;
import com.heronix.model.domain.Student;
import com.heronix.model.enums.StaffOccupation;
import com.heronix.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Staff entity operations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new staff member
     */
    public Staff createStaff(Staff staff) {
        log.info("Creating new staff member: {} {}", staff.getFirstName(), staff.getLastName());

        // Generate employee ID if not provided
        if (staff.getEmployeeId() == null || staff.getEmployeeId().isEmpty()) {
            staff.setEmployeeId(generateEmployeeId(staff.getOccupation()));
        }

        // Set defaults
        if (staff.getHireDate() == null) {
            staff.setHireDate(LocalDate.now());
        }

        return staffRepository.save(staff);
    }

    /**
     * Update an existing staff member
     */
    public Staff updateStaff(Staff staff) {
        log.info("Updating staff member ID: {}", staff.getId());
        return staffRepository.save(staff);
    }

    /**
     * Get staff by ID
     */
    @Transactional(readOnly = true)
    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }

    /**
     * Get staff by employee ID
     */
    @Transactional(readOnly = true)
    public Optional<Staff> getStaffByEmployeeId(String employeeId) {
        return staffRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get staff by email
     */
    @Transactional(readOnly = true)
    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    /**
     * Get all active staff
     */
    @Transactional(readOnly = true)
    public List<Staff> getAllActiveStaff() {
        return staffRepository.findByActiveTrueAndDeletedFalse();
    }

    /**
     * Get all active staff by campus
     */
    @Transactional(readOnly = true)
    public List<Staff> getActiveStaffByCampus(Long campusId) {
        if (campusId == null) {
            return staffRepository.findByActiveTrueAndDeletedFalse();
        }
        return staffRepository.findActiveByCampusId(campusId);
    }

    /**
     * Get staff by occupation
     */
    @Transactional(readOnly = true)
    public List<Staff> getStaffByOccupation(StaffOccupation occupation) {
        return staffRepository.findByOccupationAndActiveTrue(occupation);
    }

    /**
     * Get staff by department
     */
    @Transactional(readOnly = true)
    public List<Staff> getStaffByDepartment(String department) {
        return staffRepository.findByDepartmentAndActiveTrue(department);
    }

    /**
     * Soft delete a staff member
     */
    public void softDeleteStaff(Long id, String deletedBy) {
        log.info("Soft deleting staff member ID: {} by user: {}", id, deletedBy);

        staffRepository.findById(id).ifPresent(staff -> {
            staff.softDelete(deletedBy);
            staffRepository.save(staff);
        });
    }

    /**
     * Restore a soft-deleted staff member
     */
    public void restoreStaff(Long id) {
        log.info("Restoring staff member ID: {}", id);

        staffRepository.findById(id).ifPresent(staff -> {
            staff.restore();
            staffRepository.save(staff);
        });
    }

    /**
     * Hard delete a staff member (use with caution)
     */
    public void deleteStaff(Long id) {
        log.warn("Hard deleting staff member ID: {}", id);
        staffRepository.deleteById(id);
    }

    // ========================================================================
    // COUNTS AND STATISTICS
    // ========================================================================

    /**
     * Count total active staff
     */
    @Transactional(readOnly = true)
    public Long countActiveStaff(Long campusId) {
        if (campusId == null) {
            return staffRepository.countActiveStaff();
        }
        return staffRepository.countActiveStaffByCampus(campusId);
    }

    /**
     * Count staff by occupation
     */
    @Transactional(readOnly = true)
    public Long countByOccupation(StaffOccupation occupation, Long campusId) {
        if (campusId == null) {
            return staffRepository.countByOccupation(occupation);
        }
        return staffRepository.countByOccupationAndCampus(occupation, campusId);
    }

    /**
     * Get department breakdown
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentBreakdown(Long campusId) {
        List<Object[]> results;
        if (campusId == null) {
            results = staffRepository.getDepartmentBreakdown();
        } else {
            results = staffRepository.getDepartmentBreakdownByCampus(campusId);
        }

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : results) {
            String dept = row[0] != null ? row[0].toString() : "Unassigned";
            Long count = ((Number) row[1]).longValue();
            breakdown.put(dept, count);
        }
        return breakdown;
    }

    /**
     * Get occupation distribution
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getOccupationDistribution(Long campusId) {
        List<Object[]> results;
        if (campusId == null) {
            results = staffRepository.getOccupationDistribution();
        } else {
            results = staffRepository.getOccupationDistributionByCampus(campusId);
        }

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : results) {
            String occupation = row[0] != null ? ((StaffOccupation) row[0]).getDisplayName() : "Unknown";
            Long count = ((Number) row[1]).longValue();
            distribution.put(occupation, count);
        }
        return distribution;
    }

    /**
     * Get experience distribution
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getExperienceDistribution(Long campusId) {
        List<Object[]> results;
        if (campusId == null) {
            results = staffRepository.getExperienceDistribution();
        } else {
            results = staffRepository.getExperienceDistributionByCampus(campusId);
        }

        Map<String, Long> distribution = new LinkedHashMap<>();
        // Define the order
        String[] order = {"0-1 years", "2-4 years", "5-9 years", "10-19 years", "20+ years", "Unknown"};

        // First, create a map from results
        Map<String, Long> tempMap = new HashMap<>();
        for (Object[] row : results) {
            String range = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            tempMap.put(range, count);
        }

        // Put in order
        for (String range : order) {
            if (tempMap.containsKey(range)) {
                distribution.put(range, tempMap.get(range));
            }
        }

        return distribution;
    }

    /**
     * Get average experience
     */
    @Transactional(readOnly = true)
    public Double getAverageExperience(Long campusId) {
        Double avg;
        if (campusId == null) {
            avg = staffRepository.getAverageExperience();
        } else {
            avg = staffRepository.getAverageExperienceByCampus(campusId);
        }
        return avg != null ? avg : 0.0;
    }

    // ========================================================================
    // COMPLIANCE OPERATIONS
    // ========================================================================

    /**
     * Get staff with expiring background checks
     */
    @Transactional(readOnly = true)
    public List<Staff> getStaffWithExpiringBackgroundChecks(Long campusId, int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        if (campusId == null) {
            return staffRepository.findWithExpiringBackgroundChecks(futureDate);
        }
        return staffRepository.findWithExpiringBackgroundChecksByCampus(campusId, futureDate);
    }

    /**
     * Get compliance summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplianceSummary(Long campusId) {
        Map<String, Object> summary = new HashMap<>();
        LocalDate today = LocalDate.now();

        Long totalStaff = countActiveStaff(campusId);
        Long validBackgroundChecks = staffRepository.countWithValidBackgroundCheck(today);
        Long expiredBackgroundChecks = staffRepository.countExpiredBackgroundChecks(today);
        Long expiringIn30Days = staffRepository.countExpiringBackgroundChecks(today, today.plusDays(30));
        Long expiringIn60Days = staffRepository.countExpiringBackgroundChecks(today, today.plusDays(60));
        Long expiringIn90Days = staffRepository.countExpiringBackgroundChecks(today, today.plusDays(90));
        Long validI9 = staffRepository.countWithValidI9(today);
        Long missingI9 = staffRepository.countMissingI9();
        Long expiredI9 = staffRepository.countExpiredI9(today);

        summary.put("totalStaff", totalStaff);
        summary.put("validBackgroundChecks", validBackgroundChecks);
        summary.put("expiredBackgroundChecks", expiredBackgroundChecks);
        summary.put("expiringIn30Days", expiringIn30Days);
        summary.put("expiringIn60Days", expiringIn60Days);
        summary.put("expiringIn90Days", expiringIn90Days);
        summary.put("totalExpiringSoon", expiringIn90Days);
        summary.put("validI9", validI9);
        summary.put("missingI9", missingI9);
        summary.put("expiredI9", expiredI9);

        // Calculate compliance rate
        if (totalStaff > 0) {
            double complianceRate = (validBackgroundChecks.doubleValue() / totalStaff.doubleValue()) * 100.0;
            summary.put("backgroundCheckComplianceRate", complianceRate);
        } else {
            summary.put("backgroundCheckComplianceRate", 100.0);
        }

        return summary;
    }

    // ========================================================================
    // OCCUPATION-SPECIFIC OPERATIONS
    // ========================================================================

    /**
     * Get all paraprofessionals
     */
    @Transactional(readOnly = true)
    public List<Staff> getAllParaprofessionals(Long campusId) {
        if (campusId == null) {
            return staffRepository.findAllParaprofessionals();
        }
        return staffRepository.findParaprofessionalsByCampus(campusId);
    }

    /**
     * Get all counselors
     */
    @Transactional(readOnly = true)
    public List<Staff> getAllCounselors() {
        return staffRepository.findAllCounselors();
    }

    /**
     * Get all nurses
     */
    @Transactional(readOnly = true)
    public List<Staff> getAllNurses() {
        return staffRepository.findAllNurses();
    }

    /**
     * Get all transportation staff
     */
    @Transactional(readOnly = true)
    public List<Staff> getAllTransportationStaff() {
        return staffRepository.findAllTransportationStaff();
    }

    /**
     * Get drivers with expiring CDL
     */
    @Transactional(readOnly = true)
    public List<Staff> getDriversWithExpiringCDL(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return staffRepository.findDriversWithExpiringCDL(futureDate);
    }

    /**
     * Get nurses with expiring license
     */
    @Transactional(readOnly = true)
    public List<Staff> getNursesWithExpiringLicense(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return staffRepository.findNursesWithExpiringLicense(futureDate);
    }

    /**
     * Get counselors with expiring license
     */
    @Transactional(readOnly = true)
    public List<Staff> getCounselorsWithExpiringLicense(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return staffRepository.findCounselorsWithExpiringLicense(futureDate);
    }

    // ========================================================================
    // PARAPROFESSIONAL STUDENT ASSIGNMENT
    // ========================================================================

    /**
     * Assign a student to a paraprofessional
     */
    public void assignStudentToStaff(Long staffId, Student student) {
        staffRepository.findById(staffId).ifPresent(staff -> {
            if (staff.isParaprofessional()) {
                staff.assignStudent(student);
                staffRepository.save(staff);
                log.info("Assigned student {} to paraprofessional {}", student.getId(), staffId);
            } else {
                log.warn("Cannot assign student to non-paraprofessional staff member");
            }
        });
    }

    /**
     * Remove a student from a paraprofessional
     */
    public void removeStudentFromStaff(Long staffId, Student student) {
        staffRepository.findById(staffId).ifPresent(staff -> {
            staff.removeAssignedStudent(student);
            staffRepository.save(staff);
            log.info("Removed student {} from staff member {}", student.getId(), staffId);
        });
    }

    /**
     * Get paraprofessionals available for student assignment
     */
    @Transactional(readOnly = true)
    public List<Staff> getAvailableParaprofessionals(Long campusId) {
        List<Staff> paras = getAllParaprofessionals(campusId);
        return paras.stream()
                .filter(Staff::isActiveStaff)
                .filter(p -> !p.isAtStudentCapacity())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // SEARCH OPERATIONS
    // ========================================================================

    /**
     * Search staff by name or employee ID
     */
    @Transactional(readOnly = true)
    public List<Staff> searchStaff(String searchTerm, Long campusId) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveStaffByCampus(campusId);
        }

        if (campusId == null) {
            return staffRepository.searchByNameOrEmployeeId(searchTerm.trim());
        }
        return staffRepository.searchByNameOrEmployeeIdAndCampus(searchTerm.trim(), campusId);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate a unique employee ID based on occupation
     */
    private String generateEmployeeId(StaffOccupation occupation) {
        String prefix = getOccupationPrefix(occupation);
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return prefix + timestamp;
    }

    /**
     * Get prefix for employee ID based on occupation category
     */
    private String getOccupationPrefix(StaffOccupation occupation) {
        if (occupation == null) {
            return "S";
        }

        return switch (occupation.getCategory()) {
            case EDUCATIONAL_SUPPORT -> "ES";
            case ADMINISTRATIVE -> "AD";
            case STUDENT_SERVICES -> "SS";
            case HEALTH_SERVICES -> "HS";
            case SECURITY -> "SC";
            case FACILITIES -> "FC";
            case FOOD_SERVICES -> "FS";
            case TRANSPORTATION -> "TR";
            case TECHNOLOGY -> "IT";
            case ATHLETICS -> "AT";
            default -> "S";
        };
    }

    /**
     * Get staff grouped by occupation category
     */
    @Transactional(readOnly = true)
    public Map<String, List<Staff>> getStaffByCategory(Long campusId) {
        List<Staff> allStaff = getActiveStaffByCampus(campusId);

        return allStaff.stream()
                .collect(Collectors.groupingBy(
                        staff -> staff.getOccupation() != null ?
                                staff.getOccupation().getCategory().getDisplayName() : "Other"
                ));
    }

    /**
     * Get distinct departments
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctDepartments() {
        List<Staff> allStaff = staffRepository.findByActiveTrueAndDeletedFalse();
        return allStaff.stream()
                .map(Staff::getDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all occupation types
     */
    public List<StaffOccupation> getAllOccupationTypes() {
        return Arrays.asList(StaffOccupation.values());
    }

    /**
     * Get occupation types by category
     */
    public Map<String, List<StaffOccupation>> getOccupationTypesByCategory() {
        return Arrays.stream(StaffOccupation.values())
                .collect(Collectors.groupingBy(
                        occ -> occ.getCategory().getDisplayName()
                ));
    }
}
