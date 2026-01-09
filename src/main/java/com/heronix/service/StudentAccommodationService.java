package com.heronix.service;

import com.heronix.model.domain.StudentAccommodation;
import com.heronix.model.domain.StudentAccommodation.AccommodationType;
import com.heronix.model.domain.StudentAccommodation.AccommodationStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.StudentAccommodationRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for StudentAccommodation management
 *
 * Handles all business logic for student accommodations including:
 * - 504 Plans
 * - IEPs (Individualized Education Programs)
 * - ELL/ESL services
 * - Gifted & Talented programs
 * - At-risk interventions
 * - Title I services
 * - Special transportation
 * - Accessibility accommodations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentAccommodationService {

    private final StudentAccommodationRepository accommodationRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new accommodation for student
     */
    public StudentAccommodation createAccommodation(Long studentId, AccommodationType type, Long createdByStaffId) {
        log.info("Creating {} accommodation for student ID: {}", type, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        StudentAccommodation accommodation = StudentAccommodation.builder()
                .student(student)
                .type(type)
                .status(AccommodationStatus.DRAFT)
                .startDate(LocalDate.now())
                .createdBy(createdBy)
                .build();

        return accommodationRepository.save(accommodation);
    }

    /**
     * Get accommodation by ID
     */
    public Optional<StudentAccommodation> getAccommodationById(Long id) {
        return accommodationRepository.findById(id);
    }

    /**
     * Get all accommodations
     */
    public List<StudentAccommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    /**
     * Get all accommodations for a student
     */
    public List<StudentAccommodation> getAccommodationsByStudent(Long studentId) {
        return accommodationRepository.findByStudentId(studentId);
    }

    /**
     * Get active accommodations for a student
     */
    public List<StudentAccommodation> getActiveAccommodationsByStudent(Long studentId) {
        return accommodationRepository.findActiveByStudentId(studentId);
    }

    /**
     * Get accommodations by student and type
     */
    public List<StudentAccommodation> getAccommodationsByStudentAndType(Long studentId, AccommodationType type) {
        List<StudentAccommodation> studentAccommodations = getAccommodationsByStudent(studentId);
        return studentAccommodations.stream()
                .filter(a -> a.getType() == type)
                .toList();
    }

    /**
     * Get accommodations by status
     */
    public List<StudentAccommodation> getAccommodationsByStatus(AccommodationStatus status) {
        return accommodationRepository.findByStatus(status);
    }

    /**
     * Update accommodation
     */
    public StudentAccommodation updateAccommodation(Long id, StudentAccommodation updatedAccommodation, Long updatedByStaffId) {
        log.info("Updating accommodation ID: {}", id);

        StudentAccommodation existing = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        // Update fields
        existing.setType(updatedAccommodation.getType());
        existing.setStatus(updatedAccommodation.getStatus());
        existing.setStartDate(updatedAccommodation.getStartDate());
        existing.setEndDate(updatedAccommodation.getEndDate());
        existing.setNextReviewDate(updatedAccommodation.getNextReviewDate());

        // Update 504 Plan details
        existing.setHas504Plan(updatedAccommodation.getHas504Plan());
        existing.setPlan504Coordinator(updatedAccommodation.getPlan504Coordinator());
        existing.setPlan504Accommodations(updatedAccommodation.getPlan504Accommodations());
        existing.setPlan504Modifications(updatedAccommodation.getPlan504Modifications());
        existing.setPlan504EffectiveDate(updatedAccommodation.getPlan504EffectiveDate());
        existing.setPlan504ExpirationDate(updatedAccommodation.getPlan504ExpirationDate());

        // Update IEP details
        existing.setHasIEP(updatedAccommodation.getHasIEP());
        existing.setIepCaseManager(updatedAccommodation.getIepCaseManager());
        existing.setIepPlacement(updatedAccommodation.getIepPlacement());
        existing.setIepGoals(updatedAccommodation.getIepGoals());
        existing.setIepAccommodations(updatedAccommodation.getIepAccommodations());
        existing.setIepEffectiveDate(updatedAccommodation.getIepEffectiveDate());
        existing.setIepExpirationDate(updatedAccommodation.getIepExpirationDate());
        existing.setPrimaryDisability(updatedAccommodation.getPrimaryDisability());

        // Update ELL details
        existing.setIsELL(updatedAccommodation.getIsELL());
        existing.setEllProficiencyLevel(updatedAccommodation.getEllProficiencyLevel());
        existing.setNativeLanguage(updatedAccommodation.getNativeLanguage());
        existing.setHomeLanguage(updatedAccommodation.getHomeLanguage());
        existing.setEllServiceModel(updatedAccommodation.getEllServiceModel());

        // Update other fields
        existing.setIsGifted(updatedAccommodation.getIsGifted());
        existing.setIsAtRisk(updatedAccommodation.getIsAtRisk());
        existing.setTitleIParticipating(updatedAccommodation.getTitleIParticipating());
        existing.setHomelessStatus(updatedAccommodation.getHomelessStatus());
        existing.setFosterCareStatus(updatedAccommodation.getFosterCareStatus());
        existing.setMilitaryFamily(updatedAccommodation.getMilitaryFamily());
        existing.setLunchStatus(updatedAccommodation.getLunchStatus());
        existing.setRequiresSpecialTransportation(updatedAccommodation.getRequiresSpecialTransportation());
        existing.setRequiresAccessibilityAccommodations(updatedAccommodation.getRequiresAccessibilityAccommodations());
        existing.setRequiresAssistiveTechnology(updatedAccommodation.getRequiresAssistiveTechnology());

        existing.setUpdatedBy(updatedBy);

        return accommodationRepository.save(existing);
    }

    /**
     * Update accommodation (overload - uses accommodation's own ID)
     */
    public StudentAccommodation updateAccommodation(StudentAccommodation accommodation, Long updatedByStaffId) {
        if (accommodation.getId() == null) {
            throw new IllegalArgumentException("Accommodation ID cannot be null for update");
        }
        return updateAccommodation(accommodation.getId(), accommodation, updatedByStaffId);
    }

    /**
     * Delete accommodation
     */
    public void deleteAccommodation(Long id) {
        log.info("Deleting accommodation ID: {}", id);
        accommodationRepository.deleteById(id);
    }

    // ========================================================================
    // STATUS MANAGEMENT
    // ========================================================================

    /**
     * Activate accommodation
     */
    public StudentAccommodation activateAccommodation(Long id, Long staffId) {
        log.info("Activating accommodation ID: {}", id);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setStatus(AccommodationStatus.ACTIVE);
        if (accommodation.getStartDate() == null) {
            accommodation.setStartDate(LocalDate.now());
        }
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    /**
     * Deactivate accommodation
     */
    public StudentAccommodation deactivateAccommodation(Long id, Long staffId) {
        log.info("Deactivating accommodation ID: {}", id);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setStatus(AccommodationStatus.INACTIVE);
        if (accommodation.getEndDate() == null) {
            accommodation.setEndDate(LocalDate.now());
        }
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    /**
     * Expire accommodation
     */
    public StudentAccommodation expireAccommodation(Long id, Long staffId) {
        log.info("Expiring accommodation ID: {}", id);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setStatus(AccommodationStatus.EXPIRED);
        accommodation.setEndDate(LocalDate.now());
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    // ========================================================================
    // REVIEW MANAGEMENT
    // ========================================================================

    /**
     * Schedule next review
     */
    public StudentAccommodation scheduleReview(Long id, LocalDate reviewDate, Long staffId) {
        log.info("Scheduling review for accommodation ID: {} on {}", id, reviewDate);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setNextReviewDate(reviewDate);
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    /**
     * Complete review
     */
    public StudentAccommodation completeReview(Long id, Long staffId) {
        log.info("Completing review for accommodation ID: {}", id);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setLastReviewDate(LocalDate.now());
        accommodation.setStatus(AccommodationStatus.UNDER_REVIEW);
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    /**
     * Record review completion
     */
    public StudentAccommodation recordReviewCompletion(Long id, Long staffId, LocalDate reviewDate) {
        log.info("Recording review completion for accommodation ID: {}", id);

        StudentAccommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setLastReviewDate(reviewDate);
        accommodation.setStatus(AccommodationStatus.ACTIVE);
        accommodation.setUpdatedBy(staff);

        // Schedule next annual review
        accommodation.setNextReviewDate(reviewDate.plusYears(1));

        return accommodationRepository.save(accommodation);
    }

    /**
     * Get accommodations with overdue reviews
     */
    public List<StudentAccommodation> getOverdueReviews() {
        return accommodationRepository.findOverdueReviews();
    }

    /**
     * Get accommodations expiring soon (within 30 days)
     */
    public List<StudentAccommodation> getExpiringSoon() {
        return getExpiringSoon(30);
    }

    /**
     * Get accommodations expiring soon (within specified days)
     */
    public List<StudentAccommodation> getExpiringSoon(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return accommodationRepository.findExpiringSoon(futureDate);
    }

    /**
     * Get expired accommodations
     */
    public List<StudentAccommodation> getExpiredAccommodations() {
        return accommodationRepository.findExpired();
    }

    // ========================================================================
    // TYPE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Get all students with 504 Plans
     */
    public List<StudentAccommodation> getStudentsWith504Plans() {
        return accommodationRepository.findAllWith504Plans();
    }

    /**
     * Get all students with IEPs
     */
    public List<StudentAccommodation> getStudentsWithIEPs() {
        return accommodationRepository.findAllWithIEPs();
    }

    /**
     * Get all ELL students
     */
    public List<StudentAccommodation> getELLStudents() {
        return accommodationRepository.findAllELLStudents();
    }

    /**
     * Get all gifted students
     */
    public List<StudentAccommodation> getGiftedStudents() {
        return accommodationRepository.findAllGiftedStudents();
    }

    /**
     * Get all at-risk students
     */
    public List<StudentAccommodation> getAtRiskStudents() {
        return accommodationRepository.findAllAtRiskStudents();
    }

    /**
     * Get all Title I students
     */
    public List<StudentAccommodation> getTitleIStudents() {
        return accommodationRepository.findAllTitleIStudents();
    }

    /**
     * Get all homeless students (McKinney-Vento)
     */
    public List<StudentAccommodation> getHomelessStudents() {
        return accommodationRepository.findAllHomelessStudents();
    }

    /**
     * Get all foster care students
     */
    public List<StudentAccommodation> getFosterCareStudents() {
        return accommodationRepository.findAllFosterCareStudents();
    }

    /**
     * Get all military family students
     */
    public List<StudentAccommodation> getMilitaryFamilyStudents() {
        return accommodationRepository.findAllMilitaryFamilyStudents();
    }

    // ========================================================================
    // COORDINATOR MANAGEMENT
    // ========================================================================

    /**
     * Assign coordinator to accommodation
     */
    public StudentAccommodation assignCoordinator(Long accommodationId, Long coordinatorId, Long staffId) {
        log.info("Assigning coordinator {} to accommodation {}", coordinatorId, accommodationId);

        StudentAccommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + accommodationId));

        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new IllegalArgumentException("Coordinator not found: " + coordinatorId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        accommodation.setCoordinator(coordinator);
        accommodation.setUpdatedBy(staff);

        return accommodationRepository.save(accommodation);
    }

    /**
     * Get accommodations by coordinator
     */
    public List<StudentAccommodation> getAccommodationsByCoordinator(Long coordinatorId) {
        return accommodationRepository.findActiveByCoordinatorId(coordinatorId);
    }

    /**
     * Get active accommodations by coordinator
     */
    public List<StudentAccommodation> getActiveAccommodationsByCoordinator(Long coordinatorId) {
        return accommodationRepository.findActiveByCoordinatorId(coordinatorId);
    }

    // ========================================================================
    // LUNCH STATUS MANAGEMENT
    // ========================================================================

    /**
     * Get students by lunch status
     */
    public List<StudentAccommodation> getStudentsByLunchStatus(StudentAccommodation.LunchStatus lunchStatus) {
        return accommodationRepository.findByLunchStatus(lunchStatus);
    }

    /**
     * Count students by lunch status
     */
    public long countStudentsByLunchStatus(StudentAccommodation.LunchStatus lunchStatus) {
        return accommodationRepository.countByLunchStatus(lunchStatus);
    }

    // ========================================================================
    // TRANSPORTATION MANAGEMENT
    // ========================================================================

    /**
     * Get students requiring special transportation
     */
    public List<StudentAccommodation> getStudentsRequiringSpecialTransportation() {
        return accommodationRepository.findRequiringSpecialTransportation();
    }

    /**
     * Get students by bus number
     */
    public List<StudentAccommodation> getStudentsByBusNumber(String busNumber) {
        return accommodationRepository.findByBusNumber(busNumber);
    }

    // ========================================================================
    // ASSISTIVE TECHNOLOGY & ACCESSIBILITY
    // ========================================================================

    /**
     * Get students requiring assistive technology
     */
    public List<StudentAccommodation> getStudentsRequiringAssistiveTechnology() {
        return accommodationRepository.findRequiringAssistiveTechnology();
    }

    /**
     * Get students requiring accessibility accommodations
     */
    public List<StudentAccommodation> getStudentsRequiringAccessibilityAccommodations() {
        return accommodationRepository.findRequiringAccessibilityAccommodations();
    }

    // ========================================================================
    // STATISTICAL QUERIES
    // ========================================================================

    /**
     * Count accommodations by type
     */
    public long countByType(AccommodationType type) {
        return accommodationRepository.countByType(type);
    }

    /**
     * Count active accommodations
     */
    public long countActiveAccommodations() {
        return accommodationRepository.countActive();
    }

    /**
     * Count active accommodations (short alias)
     */
    public long countActive() {
        return countActiveAccommodations();
    }

    /**
     * Count accommodations by status
     */
    public long countByStatus(AccommodationStatus status) {
        return accommodationRepository.countByStatus(status);
    }

    /**
     * Get accommodation statistics by type (raw)
     */
    public List<Object[]> getAccommodationStatisticsRaw() {
        return accommodationRepository.getAccommodationStatistics();
    }

    /**
     * Get accommodation statistics by type (as map)
     */
    public java.util.Map<String, Object> getAccommodationStatistics() {
        List<Object[]> stats = accommodationRepository.getAccommodationStatistics();
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        for (Object[] row : stats) {
            if (row.length >= 2) {
                String type = row[0] != null ? row[0].toString() : "UNKNOWN";
                Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                result.put(type, count);
            }
        }

        return result;
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Auto-expire accommodations past end date
     */
    public int autoExpireAccommodations(Long staffId) {
        log.info("Auto-expiring accommodations past end date");

        List<StudentAccommodation> expired = getExpiredAccommodations();
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        for (StudentAccommodation accommodation : expired) {
            accommodation.setStatus(AccommodationStatus.EXPIRED);
            accommodation.setUpdatedBy(staff);
        }

        accommodationRepository.saveAll(expired);
        log.info("Auto-expired {} accommodations", expired.size());

        return expired.size();
    }

    /**
     * Generate review reminders
     */
    public List<StudentAccommodation> generateReviewReminders() {
        log.info("Generating review reminders for overdue accommodations");
        return getOverdueReviews();
    }

    /**
     * Bulk activate accommodations
     */
    public List<StudentAccommodation> bulkActivate(List<Long> accommodationIds, Long staffId) {
        log.info("Bulk activating {} accommodations", accommodationIds.size());

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        return accommodationIds.stream()
                .map(id -> {
                    StudentAccommodation accommodation = accommodationRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

                    accommodation.setStatus(AccommodationStatus.ACTIVE);
                    if (accommodation.getStartDate() == null) {
                        accommodation.setStartDate(LocalDate.now());
                    }
                    accommodation.setUpdatedBy(staff);

                    return accommodationRepository.save(accommodation);
                })
                .toList();
    }

    /**
     * Bulk deactivate accommodations
     */
    public List<StudentAccommodation> bulkDeactivate(List<Long> accommodationIds, Long staffId) {
        log.info("Bulk deactivating {} accommodations", accommodationIds.size());

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        return accommodationIds.stream()
                .map(id -> {
                    StudentAccommodation accommodation = accommodationRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

                    accommodation.setStatus(AccommodationStatus.INACTIVE);
                    if (accommodation.getEndDate() == null) {
                        accommodation.setEndDate(LocalDate.now());
                    }
                    accommodation.setUpdatedBy(staff);

                    return accommodationRepository.save(accommodation);
                })
                .toList();
    }

    /**
     * Bulk assign coordinator to accommodations
     */
    public List<StudentAccommodation> bulkAssignCoordinator(List<Long> accommodationIds, Long coordinatorId, Long staffId) {
        log.info("Bulk assigning coordinator {} to {} accommodations", coordinatorId, accommodationIds.size());

        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new IllegalArgumentException("Coordinator not found: " + coordinatorId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        return accommodationIds.stream()
                .map(id -> {
                    StudentAccommodation accommodation = accommodationRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Accommodation not found: " + id));

                    accommodation.setCoordinator(coordinator);
                    accommodation.setUpdatedBy(staff);

                    return accommodationRepository.save(accommodation);
                })
                .toList();
    }
}
