package com.heronix.service;

import com.heronix.model.domain.EmergencyContact;
import com.heronix.model.domain.Student;
import com.heronix.repository.EmergencyContactRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for EmergencyContact management
 *
 * Handles all business logic for emergency contacts including:
 * - CRUD operations
 * - Priority management
 * - Authorization management
 * - Contact validation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new emergency contact for student
     */
    public EmergencyContact createContact(Long studentId, String firstName, String lastName,
                                         String relationship, String primaryPhone, Integer priorityOrder) {
        log.info("Creating emergency contact for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (relationship == null || relationship.trim().isEmpty()) {
            throw new IllegalArgumentException("Relationship is required");
        }
        if (primaryPhone == null || primaryPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Primary phone is required");
        }
        if (priorityOrder == null || priorityOrder < 1) {
            throw new IllegalArgumentException("Priority order must be 1 or greater");
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setStudent(student);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setRelationship(relationship);
        contact.setPrimaryPhone(primaryPhone);
        contact.setPriorityOrder(priorityOrder);
        contact.setIsActive(true);

        return contactRepository.save(contact);
    }

    /**
     * Create new emergency contact (overload - accepts EmergencyContact object)
     */
    public EmergencyContact createContact(EmergencyContact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        if (contact.getStudent() == null || contact.getStudent().getId() == null) {
            throw new IllegalArgumentException("Student ID is required");
        }

        Long studentId = contact.getStudent().getId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        contact.setStudent(student);
        if (contact.getIsActive() == null) {
            contact.setIsActive(true);
        }

        return contactRepository.save(contact);
    }

    /**
     * Get contact by ID
     */
    public Optional<EmergencyContact> getContactById(Long id) {
        return contactRepository.findById(id);
    }

    /**
     * Get all emergency contacts (across all students)
     */
    public List<EmergencyContact> getAllContacts() {
        return contactRepository.findAll();
    }

    /**
     * Get all emergency contacts for a student
     */
    public List<EmergencyContact> getContactsByStudent(Long studentId) {
        return contactRepository.findByStudentIdOrderByPriorityOrder(studentId);
    }

    /**
     * Get active emergency contacts for a student
     */
    public List<EmergencyContact> getActiveContactsByStudent(Long studentId) {
        return contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true);
    }

    /**
     * Update emergency contact
     */
    public EmergencyContact updateContact(Long id, EmergencyContact updatedContact) {
        log.info("Updating emergency contact ID: {}", id);

        EmergencyContact existing = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        // Update basic information
        existing.setFirstName(updatedContact.getFirstName());
        existing.setLastName(updatedContact.getLastName());
        existing.setRelationship(updatedContact.getRelationship());

        // Update contact information
        existing.setPrimaryPhone(updatedContact.getPrimaryPhone());
        existing.setSecondaryPhone(updatedContact.getSecondaryPhone());
        existing.setEmail(updatedContact.getEmail());
        existing.setWorkPhone(updatedContact.getWorkPhone());

        // Update address
        existing.setStreetAddress(updatedContact.getStreetAddress());
        existing.setCity(updatedContact.getCity());
        existing.setState(updatedContact.getState());
        existing.setZipCode(updatedContact.getZipCode());

        // Update authorization
        existing.setAuthorizedToPickUp(updatedContact.getAuthorizedToPickUp());
        existing.setLivesWithStudent(updatedContact.getLivesWithStudent());
        existing.setAvailabilityNotes(updatedContact.getAvailabilityNotes());
        existing.setNotes(updatedContact.getNotes());

        // Update work information
        existing.setEmployer(updatedContact.getEmployer());

        return contactRepository.save(existing);
    }

    /**
     * Update emergency contact (overload - uses contact's own ID)
     */
    public EmergencyContact updateContact(EmergencyContact contact) {
        if (contact.getId() == null) {
            throw new IllegalArgumentException("Contact ID cannot be null for update");
        }
        return updateContact(contact.getId(), contact);
    }

    /**
     * Delete emergency contact
     */
    public void deleteContact(Long id) {
        log.info("Deleting emergency contact ID: {}", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        // Check if this is the only contact
        List<EmergencyContact> allContacts = contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(
                contact.getStudent().getId(), true);

        if (allContacts.size() == 1) {
            throw new IllegalStateException("Cannot delete the only emergency contact. Add another contact first.");
        }

        contactRepository.deleteById(id);

        // Reorder remaining contacts
        reorderContactsForStudent(contact.getStudent().getId());
    }

    /**
     * Deactivate contact (soft delete)
     */
    public EmergencyContact deactivateContact(Long id) {
        log.info("Deactivating emergency contact ID: {}", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        // Check if this is the only active contact
        List<EmergencyContact> activeContacts = contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(
                contact.getStudent().getId(), true);

        if (activeContacts.size() == 1) {
            throw new IllegalStateException("Cannot deactivate the only active emergency contact. Add another contact first.");
        }

        contact.setIsActive(false);
        return contactRepository.save(contact);
    }

    /**
     * Reactivate contact
     */
    public EmergencyContact reactivateContact(Long id) {
        log.info("Reactivating emergency contact ID: {}", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        contact.setIsActive(true);
        return contactRepository.save(contact);
    }

    // ========================================================================
    // PRIORITY MANAGEMENT
    // ========================================================================

    /**
     * Set contact priority
     */
    public EmergencyContact setPriority(Long id, Integer newPriority) {
        log.info("Setting priority for contact {} to {}", id, newPriority);

        if (newPriority == null || newPriority < 1) {
            throw new IllegalArgumentException("Priority must be 1 or greater");
        }

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        Integer oldPriority = contact.getPriorityOrder();
        contact.setPriorityOrder(newPriority);
        contactRepository.save(contact);

        // Reorder other contacts
        reorderContactsAfterPriorityChange(contact.getStudent().getId(), oldPriority, newPriority, id);

        return contact;
    }

    /**
     * Move contact up in priority
     */
    public EmergencyContact movePriorityUp(Long id) {
        log.info("Moving contact {} up in priority", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        if (contact.getPriorityOrder() <= 1) {
            throw new IllegalStateException("Contact is already at highest priority");
        }

        return setPriority(id, contact.getPriorityOrder() - 1);
    }

    /**
     * Move contact down in priority
     */
    public EmergencyContact movePriorityDown(Long id) {
        log.info("Moving contact {} down in priority", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        List<EmergencyContact> allContacts = contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(
                contact.getStudent().getId(), true);

        if (contact.getPriorityOrder() >= allContacts.size()) {
            throw new IllegalStateException("Contact is already at lowest priority");
        }

        return setPriority(id, contact.getPriorityOrder() + 1);
    }

    /**
     * Reorder contacts after priority change
     */
    private void reorderContactsAfterPriorityChange(Long studentId, Integer oldPriority, Integer newPriority, Long changedContactId) {
        List<EmergencyContact> contacts = contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true);

        for (EmergencyContact contact : contacts) {
            if (contact.getId().equals(changedContactId)) {
                continue; // Skip the contact we just changed
            }

            Integer currentPriority = contact.getPriorityOrder();

            if (newPriority < oldPriority) {
                // Moving up: shift contacts down between newPriority and oldPriority
                if (currentPriority >= newPriority && currentPriority < oldPriority) {
                    contact.setPriorityOrder(currentPriority + 1);
                    contactRepository.save(contact);
                }
            } else {
                // Moving down: shift contacts up between oldPriority and newPriority
                if (currentPriority > oldPriority && currentPriority <= newPriority) {
                    contact.setPriorityOrder(currentPriority - 1);
                    contactRepository.save(contact);
                }
            }
        }
    }

    /**
     * Reorder all contacts for a student (normalize priorities to 1, 2, 3, ...)
     */
    private void reorderContactsForStudent(Long studentId) {
        List<EmergencyContact> contacts = contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true);

        int priority = 1;
        for (EmergencyContact contact : contacts) {
            contact.setPriorityOrder(priority++);
        }

        contactRepository.saveAll(contacts);
    }

    /**
     * Normalize all priorities for a student
     */
    public void normalizePriorities(Long studentId) {
        log.info("Normalizing priorities for student ID: {}", studentId);
        reorderContactsForStudent(studentId);
    }

    // ========================================================================
    // AUTHORIZATION MANAGEMENT
    // ========================================================================

    /**
     * Authorize contact for pickup
     */
    public EmergencyContact authorizePickup(Long id, boolean authorized) {
        log.info("Setting pickup authorization for contact {} to {}", id, authorized);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        contact.setAuthorizedToPickUp(authorized);
        return contactRepository.save(contact);
    }

    /**
     * Get contacts authorized for pickup
     */
    public List<EmergencyContact> getAuthorizedPickupContacts(Long studentId) {
        return contactRepository.findByStudentIdAndAuthorizedToPickUpAndIsActiveOrderByPriorityOrder(studentId, true, true);
    }

    /**
     * Set pickup authorization
     */
    public EmergencyContact setPickupAuthorization(Long id, Boolean authorized) {
        return authorizePickup(id, authorized);
    }

    /**
     * Set medical authorization
     */
    public EmergencyContact setMedicalAuthorization(Long id, Boolean authorized) {
        log.info("Setting medical authorization for contact {} to {}", id, authorized);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        contact.setAuthorizedForMedical(authorized);
        return contactRepository.save(contact);
    }

    /**
     * Set financial authorization
     */
    public EmergencyContact setFinancialAuthorization(Long id, Boolean authorized) {
        log.info("Setting financial authorization for contact {} to {}", id, authorized);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        contact.setAuthorizedForFinancial(authorized);
        return contactRepository.save(contact);
    }

    /**
     * Get contacts authorized for medical decisions
     */
    public List<EmergencyContact> getMedicalAuthorizationContacts(Long studentId) {
        return contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true)
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getAuthorizedForMedical()))
                .toList();
    }

    /**
     * Make contact primary (set to priority 1)
     */
    public EmergencyContact makePrimary(Long id) {
        return setPriority(id, 1);
    }

    /**
     * Activate contact (opposite of deactivate)
     */
    public EmergencyContact activateContact(Long id) {
        return reactivateContact(id);
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    /**
     * Validate contact has required information
     */
    public boolean validateContact(EmergencyContact contact) {
        return contact.isValid();
    }

    /**
     * Validate all contacts for a student
     */
    public List<EmergencyContact> getInvalidContacts(Long studentId) {
        List<EmergencyContact> contacts = getContactsByStudent(studentId);
        return contacts.stream()
                .filter(c -> !c.isValid())
                .toList();
    }

    /**
     * Check if student has minimum required contacts
     */
    public boolean hasMinimumContacts(Long studentId, int minimumRequired) {
        List<EmergencyContact> activeContacts = getActiveContactsByStudent(studentId);
        return activeContacts.size() >= minimumRequired;
    }

    /**
     * Get missing contact requirements for student
     */
    public String getMissingContactRequirements(Long studentId, int minimumRequired) {
        List<EmergencyContact> activeContacts = getActiveContactsByStudent(studentId);
        int count = activeContacts.size();

        if (count >= minimumRequired) {
            return null;
        }

        return String.format("Student needs %d more emergency contact(s) (has %d, requires %d)",
                minimumRequired - count, count, minimumRequired);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Get primary contact (priority 1)
     */
    public Optional<EmergencyContact> getPrimaryContact(Long studentId) {
        List<EmergencyContact> contacts = getActiveContactsByStudent(studentId);
        return contacts.isEmpty() ? Optional.empty() : Optional.of(contacts.get(0));
    }

    /**
     * Get high priority contacts (priority 1 or 2)
     */
    public List<EmergencyContact> getHighPriorityContacts(Long studentId) {
        return contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true)
                .stream()
                .filter(EmergencyContact::isHighPriority)
                .toList();
    }

    /**
     * Get contacts by relationship
     */
    public List<EmergencyContact> getContactsByRelationship(Long studentId, String relationship) {
        return contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true)
                .stream()
                .filter(c -> relationship.equalsIgnoreCase(c.getRelationship()))
                .toList();
    }

    /**
     * Get all contacts by relationship (across all students)
     */
    public List<EmergencyContact> getContactsByRelationship(String relationship) {
        return contactRepository.findAll().stream()
                .filter(c -> relationship.equalsIgnoreCase(c.getRelationship()))
                .toList();
    }

    /**
     * Get contacts living with student
     */
    public List<EmergencyContact> getContactsLivingWithStudent(Long studentId) {
        return contactRepository.findByStudentIdAndIsActiveOrderByPriorityOrder(studentId, true)
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getLivesWithStudent()))
                .toList();
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Add multiple contacts for a student
     */
    public List<EmergencyContact> addMultipleContacts(Long studentId, List<EmergencyContact> contacts) {
        log.info("Adding {} emergency contacts for student ID: {}", contacts.size(), studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get current max priority
        List<EmergencyContact> existing = getActiveContactsByStudent(studentId);
        int nextPriority = existing.size() + 1;

        for (EmergencyContact contact : contacts) {
            contact.setStudent(student);
            if (contact.getPriorityOrder() == null) {
                contact.setPriorityOrder(nextPriority++);
            }
            if (contact.getIsActive() == null) {
                contact.setIsActive(true);
            }
        }

        return contactRepository.saveAll(contacts);
    }

    /**
     * Copy contacts from one student to another (for siblings)
     */
    public List<EmergencyContact> copyContactsToSibling(Long sourceStudentId, Long targetStudentId) {
        log.info("Copying emergency contacts from student {} to student {}", sourceStudentId, targetStudentId);

        Student targetStudent = studentRepository.findById(targetStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Target student not found: " + targetStudentId));

        List<EmergencyContact> sourceContacts = getActiveContactsByStudent(sourceStudentId);
        List<EmergencyContact> copiedContacts = new java.util.ArrayList<>();

        for (EmergencyContact source : sourceContacts) {
            EmergencyContact copy = new EmergencyContact();
            copy.setStudent(targetStudent);
            copy.setFirstName(source.getFirstName());
            copy.setLastName(source.getLastName());
            copy.setRelationship(source.getRelationship());
            copy.setPrimaryPhone(source.getPrimaryPhone());
            copy.setSecondaryPhone(source.getSecondaryPhone());
            copy.setEmail(source.getEmail());
            copy.setWorkPhone(source.getWorkPhone());
            copy.setStreetAddress(source.getStreetAddress());
            copy.setCity(source.getCity());
            copy.setState(source.getState());
            copy.setZipCode(source.getZipCode());
            copy.setAuthorizedToPickUp(source.getAuthorizedToPickUp());
            copy.setLivesWithStudent(source.getLivesWithStudent());
            copy.setAvailabilityNotes(source.getAvailabilityNotes());
            copy.setEmployer(source.getEmployer());
            copy.setPriorityOrder(source.getPriorityOrder());
            copy.setIsActive(true);

            copiedContacts.add(copy);
        }

        return contactRepository.saveAll(copiedContacts);
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count emergency contacts for a student
     */
    public long countContactsForStudent(Long studentId) {
        return getActiveContactsByStudent(studentId).size();
    }

    /**
     * Count contacts authorized for pickup
     */
    public long countAuthorizedPickupContacts(Long studentId) {
        return getAuthorizedPickupContacts(studentId).size();
    }

    /**
     * Count active contacts for a student
     */
    public long countActiveContactsForStudent(Long studentId) {
        return getActiveContactsByStudent(studentId).size();
    }

    // ========================================================================
    // QUERY METHODS FOR FILTERING
    // ========================================================================

    /**
     * Get unverified contacts
     */
    public List<EmergencyContact> getUnverifiedContacts() {
        return contactRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getVerified()))
                .toList();
    }

    /**
     * Get contacts missing phone numbers
     */
    public List<EmergencyContact> getContactsMissingPhone() {
        return contactRepository.findAll().stream()
                .filter(c -> !c.hasValidPhone())
                .toList();
    }

    /**
     * Get incomplete contacts (missing critical information)
     */
    public List<EmergencyContact> getIncompleteContacts() {
        return contactRepository.findAll().stream()
                .filter(c -> !c.isValid())
                .toList();
    }

    /**
     * Verify contact information
     */
    public EmergencyContact verifyContact(Long id) {
        log.info("Verifying emergency contact ID: {}", id);

        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));

        contact.setVerified(true);
        contact.setVerificationDate(java.time.LocalDate.now());

        return contactRepository.save(contact);
    }

    /**
     * Get contacts needing reverification (not verified in X days)
     */
    public List<EmergencyContact> getContactsNeedingReverification(int days) {
        java.time.LocalDate cutoffDate = java.time.LocalDate.now().minusDays(days);
        return contactRepository.findAll().stream()
                .filter(c -> c.getVerificationDate() == null || c.getVerificationDate().isBefore(cutoffDate))
                .toList();
    }

    /**
     * Search contacts by name
     */
    public List<EmergencyContact> searchContactsByName(String searchTerm) {
        String lower = searchTerm.toLowerCase();
        return contactRepository.findAll().stream()
                .filter(c -> c.getFirstName().toLowerCase().contains(lower) ||
                           c.getLastName().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Search contacts by phone
     */
    public List<EmergencyContact> searchContactsByPhone(String phoneNumber) {
        return contactRepository.findAll().stream()
                .filter(c -> (c.getPrimaryPhone() != null && c.getPrimaryPhone().contains(phoneNumber)) ||
                           (c.getSecondaryPhone() != null && c.getSecondaryPhone().contains(phoneNumber)) ||
                           (c.getWorkPhone() != null && c.getWorkPhone().contains(phoneNumber)))
                .toList();
    }

    /**
     * Search contacts by email
     */
    public List<EmergencyContact> searchContactsByEmail(String email) {
        return contactRepository.findAll().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().toLowerCase().contains(email.toLowerCase()))
                .toList();
    }

    /**
     * Get contacts by student and relationship
     */
    public List<EmergencyContact> getContactsByStudentAndRelationship(Long studentId, String relationship) {
        return getContactsByRelationship(studentId, relationship);
    }

    /**
     * Bulk verify contacts
     */
    public List<EmergencyContact> bulkVerifyContacts(List<Long> contactIds) {
        log.info("Bulk verifying {} contacts", contactIds.size());
        List<EmergencyContact> verified = new java.util.ArrayList<>();
        for (Long id : contactIds) {
            verified.add(verifyContact(id));
        }
        return verified;
    }

    /**
     * Bulk deactivate contacts
     */
    public List<EmergencyContact> bulkDeactivateContacts(List<Long> contactIds) {
        log.info("Bulk deactivating {} contacts", contactIds.size());
        List<EmergencyContact> deactivated = new java.util.ArrayList<>();
        for (Long id : contactIds) {
            try {
                deactivated.add(deactivateContact(id));
            } catch (IllegalStateException e) {
                log.warn("Cannot deactivate contact {}: {}", id, e.getMessage());
            }
        }
        return deactivated;
    }

    /**
     * Sync contact to siblings (same as copy)
     */
    public List<EmergencyContact> syncContactToSiblings(Long contactId) {
        return copyContactToSiblings(contactId);
    }

    /**
     * Validate contact by ID (overload)
     */
    public boolean validateContact(Long id) {
        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));
        return validateContact(contact);
    }

    /**
     * Copy contact to all siblings of the student
     */
    public List<EmergencyContact> copyContactToSiblings(Long contactId) {
        log.info("Copying emergency contact ID {} to siblings", contactId);

        EmergencyContact original = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

        Student student = original.getStudent();

        // Find siblings (students with same parent/guardian)
        // This is a simplified approach - in real implementation would need parent/guardian relationship
        List<Student> siblings = studentRepository.findAll().stream()
                .filter(s -> !s.getId().equals(student.getId()))
                .filter(s -> s.getLastName().equals(student.getLastName())) // Simplified sibling detection
                .toList();

        List<EmergencyContact> copiedContacts = new java.util.ArrayList<>();
        for (Student sibling : siblings) {
            EmergencyContact copy = new EmergencyContact();
            copy.setStudent(sibling);
            copy.setFirstName(original.getFirstName());
            copy.setLastName(original.getLastName());
            copy.setRelationship(original.getRelationship());
            copy.setPrimaryPhone(original.getPrimaryPhone());
            copy.setSecondaryPhone(original.getSecondaryPhone());
            copy.setEmail(original.getEmail());
            copy.setWorkPhone(original.getWorkPhone());
            copy.setStreetAddress(original.getStreetAddress());
            copy.setAddress(original.getAddress());
            copy.setCity(original.getCity());
            copy.setState(original.getState());
            copy.setZipCode(original.getZipCode());
            copy.setAuthorizedToPickUp(original.getAuthorizedToPickUp());
            copy.setAuthorizedForMedical(original.getAuthorizedForMedical());
            copy.setAuthorizedForFinancial(original.getAuthorizedForFinancial());
            copy.setLivesWithStudent(original.getLivesWithStudent());
            copy.setPriorityOrder(original.getPriorityOrder());
            copy.setNotes(original.getNotes());
            copy.setEmergencyInstructions(original.getEmergencyInstructions());
            copy.setIsActive(true);

            copiedContacts.add(contactRepository.save(copy));
        }

        return copiedContacts;
    }
}
