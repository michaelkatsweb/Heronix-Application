package com.heronix.service;

import com.heronix.model.domain.FamilyHousehold;
import com.heronix.model.domain.FamilyHousehold.FamilyStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.FamilyHouseholdRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Family Household Management
 * Handles business logic for family households, sibling linking, and family discounts
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class FamilyManagementService {

    @Autowired
    private FamilyHouseholdRepository familyHouseholdRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new family household
     */
    public FamilyHousehold createFamilyHousehold(String familyName, Long createdByStaffId) {
        log.info("Creating family household: {}", familyName);

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String familyId = generateFamilyId();

        FamilyHousehold family = FamilyHousehold.builder()
                .familyId(familyId)
                .familyName(familyName)
                .status(FamilyStatus.ACTIVE)
                .isActive(true)
                .totalChildren(0)
                .enrolledChildren(0)
                .pendingChildren(0)
                .discount2ndChild(false)
                .discount3rdPlusChildren(false)
                .earlyBirdDiscountApplied(false)
                .waiveTechFees3rdPlus(false)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        FamilyHousehold saved = familyHouseholdRepository.save(family);
        log.info("Created family household: {}", saved.getFamilyId());
        return saved;
    }

    /**
     * Generate unique family ID
     */
    private String generateFamilyId() {
        long count = familyHouseholdRepository.count();
        return String.format("FAM-%d", System.currentTimeMillis());
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get family household by ID
     */
    @Transactional(readOnly = true)
    public FamilyHousehold getFamilyById(Long id) {
        return familyHouseholdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Family household not found: " + id));
    }

    /**
     * Get family household by family ID
     */
    @Transactional(readOnly = true)
    public FamilyHousehold getByFamilyId(String familyId) {
        return familyHouseholdRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family household not found: " + familyId));
    }

    /**
     * Get all active families
     */
    @Transactional(readOnly = true)
    public List<FamilyHousehold> getAllActiveFamilies() {
        return familyHouseholdRepository.findByIsActiveTrue();
    }

    /**
     * Get family by student ID
     */
    @Transactional(readOnly = true)
    public FamilyHousehold getFamilyByStudentId(Long studentId) {
        return familyHouseholdRepository.findByStudentId(studentId)
                .orElse(null);
    }

    /**
     * Get siblings for a student
     */
    @Transactional(readOnly = true)
    public List<Student> getSiblingsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        if (student.getFamilyHousehold() == null) {
            return List.of();
        }

        return familyHouseholdRepository.findSiblingsForStudent(
                student.getFamilyHousehold().getId(),
                studentId
        );
    }

    /**
     * Search families by name
     */
    @Transactional(readOnly = true)
    public List<FamilyHousehold> searchFamiliesByName(String searchTerm) {
        return familyHouseholdRepository.findByFamilyNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Search families by parent name
     */
    @Transactional(readOnly = true)
    public List<FamilyHousehold> searchFamiliesByParentName(String parentName) {
        return familyHouseholdRepository.findByPrimaryParentNameContainingIgnoreCase(parentName);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update family household
     */
    public FamilyHousehold updateFamilyHousehold(FamilyHousehold family, Long updatedByStaffId) {
        log.info("Updating family household: {}", family.getFamilyId());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        family.setUpdatedBy(updatedBy);
        family.setUpdatedAt(LocalDateTime.now());

        FamilyHousehold updated = familyHouseholdRepository.save(family);
        log.info("Updated family household: {}", updated.getFamilyId());
        return updated;
    }

    /**
     * Add student to family household
     */
    public FamilyHousehold addStudentToFamily(Long familyId, Long studentId, Long updatedByStaffId) {
        log.info("Adding student ID: {} to family ID: {}", studentId, familyId);

        FamilyHousehold family = getFamilyById(familyId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Remove student from previous family if exists
        if (student.getFamilyHousehold() != null) {
            FamilyHousehold previousFamily = student.getFamilyHousehold();
            previousFamily.removeStudent(student);
            familyHouseholdRepository.save(previousFamily);
        }

        // Add to new family
        family.addStudent(student);
        family.updateChildrenCounts();
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Remove student from family household
     */
    public FamilyHousehold removeStudentFromFamily(Long familyId, Long studentId, Long updatedByStaffId) {
        log.info("Removing student ID: {} from family ID: {}", studentId, familyId);

        FamilyHousehold family = getFamilyById(familyId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        family.removeStudent(student);
        family.updateChildrenCounts();
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Link multiple students as siblings (create or update family household)
     */
    public FamilyHousehold linkSiblings(List<Long> studentIds, String familyName, Long createdByStaffId) {
        log.info("Linking {} students as siblings under family: {}", studentIds.size(), familyName);

        if (studentIds.isEmpty()) {
            throw new IllegalArgumentException("At least one student required");
        }

        // Create new family household
        FamilyHousehold family = createFamilyHousehold(familyName, createdByStaffId);

        // Add all students to family
        for (Long studentId : studentIds) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Remove from previous family if exists
            if (student.getFamilyHousehold() != null) {
                FamilyHousehold previousFamily = student.getFamilyHousehold();
                previousFamily.removeStudent(student);
                familyHouseholdRepository.save(previousFamily);
            }

            family.addStudent(student);
        }

        family.updateChildrenCounts();
        family.calculateDiscounts();

        return updateFamilyHousehold(family, createdByStaffId);
    }

    /**
     * Set primary student for family
     */
    public FamilyHousehold setPrimaryStudent(Long familyId, Long studentId, Long updatedByStaffId) {
        log.info("Setting primary student ID: {} for family ID: {}", studentId, familyId);

        FamilyHousehold family = getFamilyById(familyId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        if (!student.getFamilyHousehold().getId().equals(familyId)) {
            throw new IllegalArgumentException("Student is not part of this family");
        }

        family.setPrimaryStudent(student);
        return updateFamilyHousehold(family, updatedByStaffId);
    }

    // ========================================================================
    // DISCOUNT OPERATIONS
    // ========================================================================

    /**
     * Calculate and apply family discounts
     */
    public FamilyHousehold calculateFamilyDiscounts(Long familyId, Long updatedByStaffId) {
        log.info("Calculating discounts for family ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Apply sibling discount to family
     */
    public FamilyHousehold applySiblingDiscount(Long familyId, boolean apply2ndChild, boolean apply3rdPlus, Long updatedByStaffId) {
        log.info("Applying sibling discount to family ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setDiscount2ndChild(apply2ndChild);
        family.setDiscount3rdPlusChildren(apply3rdPlus);
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Apply early bird discount to family
     */
    public FamilyHousehold applyEarlyBirdDiscount(Long familyId, boolean apply, Long updatedByStaffId) {
        log.info("Applying early bird discount to family ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setEarlyBirdDiscountApplied(apply);
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Set discount type for family
     */
    public FamilyHousehold setDiscountType(Long familyId, FamilyHousehold.DiscountType discountType, Long updatedByStaffId) {
        log.info("Setting discount type {} for family ID: {}", discountType, familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setDiscountType(discountType);
        family.calculateDiscounts();

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Get families eligible for sibling discounts
     */
    @Transactional(readOnly = true)
    public List<FamilyHousehold> getFamiliesEligibleForDiscounts() {
        return familyHouseholdRepository.findEligibleForDiscountsNotApplied();
    }

    /**
     * Apply discounts to all eligible families
     */
    public int applyDiscountsToEligibleFamilies(Long updatedByStaffId) {
        log.info("Applying discounts to all eligible families");

        List<FamilyHousehold> eligibleFamilies = getFamiliesEligibleForDiscounts();
        int count = 0;

        for (FamilyHousehold family : eligibleFamilies) {
            family.setDiscount2ndChild(family.getEnrolledChildren() >= 2);
            family.setDiscount3rdPlusChildren(family.getEnrolledChildren() >= 3);
            family.calculateDiscounts();
            updateFamilyHousehold(family, updatedByStaffId);
            count++;
        }

        log.info("Applied discounts to {} families", count);
        return count;
    }

    // ========================================================================
    // STATUS OPERATIONS
    // ========================================================================

    /**
     * Activate family household
     */
    public FamilyHousehold activateFamily(Long familyId, Long updatedByStaffId) {
        log.info("Activating family household ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setStatus(FamilyStatus.ACTIVE);
        family.setIsActive(true);

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Deactivate family household
     */
    public FamilyHousehold deactivateFamily(Long familyId, Long updatedByStaffId) {
        log.info("Deactivating family household ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setStatus(FamilyStatus.INACTIVE);
        family.setIsActive(false);

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    /**
     * Archive family household
     */
    public FamilyHousehold archiveFamily(Long familyId, Long updatedByStaffId) {
        log.info("Archiving family household ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);
        family.setStatus(FamilyStatus.ARCHIVED);
        family.setIsActive(false);

        return updateFamilyHousehold(family, updatedByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get family statistics
     */
    @Transactional(readOnly = true)
    public FamilyStatistics getStatistics() {
        long totalFamilies = familyHouseholdRepository.count();
        long activeFamilies = familyHouseholdRepository.countByIsActiveTrue();
        long familiesWithMultipleChildren = familyHouseholdRepository.countFamiliesWithMultipleChildren();
        Long totalEnrolledChildren = familyHouseholdRepository.getTotalEnrolledChildren();
        Double totalDiscounts = familyHouseholdRepository.getTotalFamilyDiscounts();

        return new FamilyStatistics(
                totalFamilies,
                activeFamilies,
                familiesWithMultipleChildren,
                totalEnrolledChildren != null ? totalEnrolledChildren : 0L,
                totalDiscounts != null ? totalDiscounts : 0.0
        );
    }

    /**
     * Get families for discount report
     */
    @Transactional(readOnly = true)
    public List<FamilyHousehold> getFamiliesForDiscountReport() {
        return familyHouseholdRepository.findFamiliesForDiscountReport();
    }

    /**
     * Get count by household type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByHouseholdType() {
        return familyHouseholdRepository.getCountByHouseholdType();
    }

    /**
     * Get count by number of children
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByNumberOfChildren() {
        return familyHouseholdRepository.getCountByNumberOfChildren();
    }

    /**
     * Statistics record
     */
    public record FamilyStatistics(
            long totalFamilies,
            long activeFamilies,
            long familiesWithMultipleChildren,
            long totalEnrolledChildren,
            double totalFamilyDiscounts
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete family household (only if no children linked)
     */
    public void deleteFamilyHousehold(Long familyId) {
        log.info("Deleting family household ID: {}", familyId);

        FamilyHousehold family = getFamilyById(familyId);

        if (family.getChildren() != null && !family.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete family with linked children");
        }

        familyHouseholdRepository.delete(family);
        log.info("Deleted family household: {}", family.getFamilyId());
    }
}
