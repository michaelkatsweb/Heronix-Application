package com.heronix.service;

import com.heronix.model.domain.ParentGuardian;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentParentRelationship;
import com.heronix.model.domain.User;
import com.heronix.repository.ParentGuardianRepository;
import com.heronix.repository.StudentParentRelationshipRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parent/Guardian Service
 *
 * Manages parent and guardian accounts and their relationships to students.
 * Supports multiple parents per student and multiple students per parent (siblings).
 *
 * Key Responsibilities:
 * - Create and manage parent/guardian accounts
 * - Link parents to students with specific permissions
 * - Track custodial vs non-custodial relationships
 * - Manage pickup authorization
 * - Find siblings through shared parents
 * - Generate emergency contact lists
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Slf4j
@Service
public class ParentGuardianService {

    @Autowired
    private ParentGuardianRepository parentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentParentRelationshipRepository relationshipRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // PARENT CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new parent/guardian
     */
    @Transactional
    public ParentGuardian createParent(
            String firstName,
            String lastName,
            String relationship,
            String primaryPhone,
            String email,
            Long createdByStaffId) {

        log.info("Creating parent/guardian: {} {}", firstName, lastName);

        User staff = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        ParentGuardian parent = new ParentGuardian();
        parent.setFirstName(firstName);
        parent.setLastName(lastName);
        parent.setRelationship(relationship);
        parent.setHomePhone(primaryPhone);
        parent.setCellPhone(primaryPhone); // Use as cell by default
        parent.setEmail(email);
        parent.setPreferredContactMethod("Cell Phone");
        parent.setIsPrimaryCustodian(false);
        parent.setLivesWithStudent(true);
        parent.setHasLegalCustody(true);
        parent.setCanPickUpStudent(true);
        parent.setAuthorizedForEmergency(true);
        parent.setReceivesReportCards(true);
        // Note: ParentGuardian entity may not have active field
        parent.setCreatedAt(LocalDateTime.now());
        parent.setCreatedBy(staff.getUsername());

        parent = parentRepository.save(parent);
        log.info("Created parent/guardian ID: {}", parent.getId());

        return parent;
    }

    /**
     * Update parent/guardian information
     */
    @Transactional
    public ParentGuardian updateParent(Long parentId, ParentGuardian updates) {
        log.info("Updating parent/guardian ID: {}", parentId);

        ParentGuardian existing = getParentById(parentId);

        if (updates.getFirstName() != null) existing.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) existing.setLastName(updates.getLastName());
        if (updates.getMiddleName() != null) existing.setMiddleName(updates.getMiddleName());
        if (updates.getRelationship() != null) existing.setRelationship(updates.getRelationship());
        if (updates.getCellPhone() != null) existing.setCellPhone(updates.getCellPhone());
        if (updates.getHomePhone() != null) existing.setHomePhone(updates.getHomePhone());
        if (updates.getWorkPhone() != null) existing.setWorkPhone(updates.getWorkPhone());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getEmployer() != null) existing.setEmployer(updates.getEmployer());
        if (updates.getWorkAddress() != null) existing.setWorkAddress(updates.getWorkAddress());
        if (updates.getPreferredContactMethod() != null) existing.setPreferredContactMethod(updates.getPreferredContactMethod());

        existing.setUpdatedAt(LocalDateTime.now());

        return parentRepository.save(existing);
    }

    /**
     * Get parent by ID
     */
    public ParentGuardian getParentById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parent/guardian not found: " + id));
    }

    /**
     * Search parents by name
     */
    public List<ParentGuardian> searchByName(String name) {
        return parentRepository.findAll().stream()
                .filter(p -> p.getFullName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Find parent by email
     */
    public Optional<ParentGuardian> findByEmail(String email) {
        return parentRepository.findAll().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst();
    }

    // ========================================================================
    // STUDENT-PARENT RELATIONSHIP MANAGEMENT
    // ========================================================================

    /**
     * Link a parent to a student
     */
    @Transactional
    public StudentParentRelationship linkParentToStudent(
            Long parentId,
            Long studentId,
            String relationshipType,
            boolean isPrimary,
            boolean isCustodial,
            boolean canPickup) {

        log.info("Linking parent {} to student {}", parentId, studentId);

        ParentGuardian parent = getParentById(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Check if relationship already exists
        if (relationshipRepository.existsActiveRelationship(student, parent)) {
            throw new IllegalStateException("Active relationship already exists between student and parent");
        }

        StudentParentRelationship relationship = StudentParentRelationship.builder()
                .student(student)
                .parent(parent)
                .relationshipType(relationshipType)
                .isPrimaryContact(isPrimary)
                .isCustodial(isCustodial)
                .hasPickupPermission(canPickup)
                .canReceiveGrades(true)
                .canMakeEducationalDecisions(isCustodial)
                .isEmergencyContact(true)
                .active(true)
                .build();

        relationship = relationshipRepository.save(relationship);
        log.info("Created student-parent relationship ID: {}", relationship.getId());

        return relationship;
    }

    /**
     * Unlink parent from student (mark inactive)
     */
    @Transactional
    public void unlinkParentFromStudent(Long relationshipId, String reason) {
        log.info("Unlinking parent-student relationship ID: {}", relationshipId);

        StudentParentRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new IllegalArgumentException("Relationship not found: " + relationshipId));

        relationship.setActive(false);
        relationship.setInactivatedAt(LocalDateTime.now());
        relationship.setInactivationReason(reason);

        relationshipRepository.save(relationship);
    }

    /**
     * Update relationship permissions
     */
    @Transactional
    public StudentParentRelationship updateRelationshipPermissions(
            Long relationshipId,
            Boolean canPickup,
            Boolean canReceiveGrades,
            Boolean canMakeDecisions,
            Boolean isEmergencyContact) {

        StudentParentRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new IllegalArgumentException("Relationship not found: " + relationshipId));

        if (canPickup != null) relationship.setHasPickupPermission(canPickup);
        if (canReceiveGrades != null) relationship.setCanReceiveGrades(canReceiveGrades);
        if (canMakeDecisions != null) relationship.setCanMakeEducationalDecisions(canMakeDecisions);
        if (isEmergencyContact != null) relationship.setIsEmergencyContact(isEmergencyContact);

        return relationshipRepository.save(relationship);
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Get all parents for a student
     */
    public List<ParentGuardian> getParentsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndActiveTrue(student).stream()
                .map(StudentParentRelationship::getParent)
                .collect(Collectors.toList());
    }

    /**
     * Get all students for a parent (siblings)
     */
    public List<Student> getStudentsForParent(Long parentId) {
        ParentGuardian parent = getParentById(parentId);

        return relationshipRepository.findByParentAndActiveTrue(parent).stream()
                .map(StudentParentRelationship::getStudent)
                .collect(Collectors.toList());
    }

    /**
     * Get primary contact for a student
     */
    public Optional<ParentGuardian> getPrimaryContactForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndIsPrimaryContactTrue(student)
                .map(StudentParentRelationship::getParent);
    }

    /**
     * Get custodial parents for a student
     */
    public List<ParentGuardian> getCustodialParents(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndIsCustodialTrue(student).stream()
                .map(StudentParentRelationship::getParent)
                .collect(Collectors.toList());
    }

    /**
     * Get authorized pickup contacts for a student
     */
    public List<ParentGuardian> getAuthorizedPickupContacts(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndHasPickupPermissionTrue(student).stream()
                .map(StudentParentRelationship::getParent)
                .collect(Collectors.toList());
    }

    /**
     * Get emergency contacts for a student
     */
    public List<ParentGuardian> getEmergencyContacts(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndIsEmergencyContactTrue(student).stream()
                .map(StudentParentRelationship::getParent)
                .collect(Collectors.toList());
    }

    /**
     * Find siblings for a student
     */
    public List<Student> findSiblings(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findSiblings(student);
    }

    /**
     * Check if parent can pick up specific student
     */
    public boolean canPickupStudent(Long parentId, Long studentId) {
        ParentGuardian parent = getParentById(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndActiveTrue(student).stream()
                .anyMatch(r -> r.getParent().equals(parent) &&
                              Boolean.TRUE.equals(r.getHasPickupPermission()));
    }

    // ========================================================================
    // CONTACT INFORMATION
    // ========================================================================

    /**
     * Get parent contact summary for a student
     */
    public List<ParentContactInfo> getParentContactInfo(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return relationshipRepository.findByStudentAndActiveTrue(student).stream()
                .map(relationship -> ParentContactInfo.builder()
                        .parentId(relationship.getParent().getId())
                        .name(relationship.getParent().getFullName())
                        .relationshipType(relationship.getRelationshipType())
                        .isPrimary(relationship.getIsPrimaryContact())
                        .isCustodial(relationship.getIsCustodial())
                        .cellPhone(relationship.getParent().getCellPhone())
                        .homePhone(relationship.getParent().getHomePhone())
                        .workPhone(relationship.getParent().getWorkPhone())
                        .email(relationship.getParent().getEmail())
                        .canPickup(relationship.getHasPickupPermission())
                        .isEmergencyContact(relationship.getIsEmergencyContact())
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class ParentContactInfo {
        private Long parentId;
        private String name;
        private String relationshipType;
        private Boolean isPrimary;
        private Boolean isCustodial;
        private String cellPhone;
        private String homePhone;
        private String workPhone;
        private String email;
        private Boolean canPickup;
        private Boolean isEmergencyContact;
    }
}
