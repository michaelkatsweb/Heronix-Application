package com.heronix.controller.api;

import com.heronix.model.domain.EmergencyContact;
import com.heronix.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Emergency Contacts
 *
 * Provides endpoints for managing:
 * - Emergency contact information
 * - Priority ordering
 * - Authorization levels (pickup, medical, etc.)
 * - Contact verification
 * - Sibling contact management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
// @RestController  // Disabled - duplicate of EmergencyContactApiController
// @RequestMapping("/api/emergency-contacts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new emergency contact
     * POST /api/emergency-contacts
     */
    @PostMapping
    public ResponseEntity<EmergencyContact> createContact(@RequestBody EmergencyContact contact) {
        try {
            log.info("API: Creating emergency contact for student ID: {}", contact.getStudent().getId());
            EmergencyContact created = contactService.createContact(contact);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get contact by ID
     * GET /api/emergency-contacts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContact> getContactById(@PathVariable Long id) {
        return contactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update contact
     * PUT /api/emergency-contacts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContact> updateContact(
            @PathVariable Long id,
            @RequestBody EmergencyContact contact) {
        try {
            contact.setId(id);
            EmergencyContact updated = contactService.updateContact(contact);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete contact
     * DELETE /api/emergency-contacts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        try {
            contactService.deleteContact(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Delete failed - not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Delete failed - validation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all contacts
     * GET /api/emergency-contacts
     */
    @GetMapping
    public ResponseEntity<List<EmergencyContact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    // ========================================================================
    // STUDENT-BASED QUERIES
    // ========================================================================

    /**
     * Get contacts for a student
     * GET /api/emergency-contacts/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EmergencyContact>> getContactsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.getContactsByStudent(studentId));
    }

    /**
     * Get active contacts for a student (ordered by priority)
     * GET /api/emergency-contacts/student/{studentId}/active
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<EmergencyContact>> getActiveContactsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.getActiveContactsByStudent(studentId));
    }

    /**
     * Get primary contact for a student
     * GET /api/emergency-contacts/student/{studentId}/primary
     */
    @GetMapping("/student/{studentId}/primary")
    public ResponseEntity<EmergencyContact> getPrimaryContact(@PathVariable Long studentId) {
        return contactService.getPrimaryContact(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get authorized pickup contacts for a student
     * GET /api/emergency-contacts/student/{studentId}/pickup-authorized
     */
    @GetMapping("/student/{studentId}/pickup-authorized")
    public ResponseEntity<List<EmergencyContact>> getAuthorizedPickupContacts(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.getAuthorizedPickupContacts(studentId));
    }

    /**
     * Get medical authorization contacts for a student
     * GET /api/emergency-contacts/student/{studentId}/medical-authorized
     */
    @GetMapping("/student/{studentId}/medical-authorized")
    public ResponseEntity<List<EmergencyContact>> getMedicalAuthorizationContacts(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.getMedicalAuthorizationContacts(studentId));
    }

    // ========================================================================
    // PRIORITY MANAGEMENT
    // ========================================================================

    /**
     * Set priority for a contact
     * POST /api/emergency-contacts/{id}/priority
     */
    @PostMapping("/{id}/priority")
    public ResponseEntity<EmergencyContact> setPriority(
            @PathVariable Long id,
            @RequestParam Integer priority) {
        try {
            EmergencyContact updated = contactService.setPriority(id, priority);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Set priority failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move contact priority up
     * POST /api/emergency-contacts/{id}/priority/up
     */
    @PostMapping("/{id}/priority/up")
    public ResponseEntity<EmergencyContact> movePriorityUp(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.movePriorityUp(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Move up failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move contact priority down
     * POST /api/emergency-contacts/{id}/priority/down
     */
    @PostMapping("/{id}/priority/down")
    public ResponseEntity<EmergencyContact> movePriorityDown(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.movePriorityDown(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Move down failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Make contact primary (priority 1)
     * POST /api/emergency-contacts/{id}/make-primary
     */
    @PostMapping("/{id}/make-primary")
    public ResponseEntity<EmergencyContact> makePrimary(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.makePrimary(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Normalize priorities for a student
     * POST /api/emergency-contacts/student/{studentId}/normalize-priorities
     */
    @PostMapping("/student/{studentId}/normalize-priorities")
    public ResponseEntity<Void> normalizePriorities(@PathVariable Long studentId) {
        contactService.normalizePriorities(studentId);
        return ResponseEntity.ok().build();
    }

    // ========================================================================
    // AUTHORIZATION MANAGEMENT
    // ========================================================================

    /**
     * Set pickup authorization
     * POST /api/emergency-contacts/{id}/pickup-authorized
     */
    @PostMapping("/{id}/pickup-authorized")
    public ResponseEntity<EmergencyContact> setPickupAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setPickupAuthorization(id, authorized);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Set medical authorization
     * POST /api/emergency-contacts/{id}/medical-authorized
     */
    @PostMapping("/{id}/medical-authorized")
    public ResponseEntity<EmergencyContact> setMedicalAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setMedicalAuthorization(id, authorized);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Set financial authorization
     * POST /api/emergency-contacts/{id}/financial-authorized
     */
    @PostMapping("/{id}/financial-authorized")
    public ResponseEntity<EmergencyContact> setFinancialAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setFinancialAuthorization(id, authorized);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // VERIFICATION & STATUS
    // ========================================================================

    /**
     * Mark contact as verified
     * POST /api/emergency-contacts/{id}/verify
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<EmergencyContact> verifyContact(@PathVariable Long id) {
        try {
            EmergencyContact verified = contactService.verifyContact(id);
            return ResponseEntity.ok(verified);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get unverified contacts
     * GET /api/emergency-contacts/unverified
     */
    @GetMapping("/unverified")
    public ResponseEntity<List<EmergencyContact>> getUnverifiedContacts() {
        return ResponseEntity.ok(contactService.getUnverifiedContacts());
    }

    /**
     * Get contacts requiring reverification
     * GET /api/emergency-contacts/reverification-needed
     */
    @GetMapping("/reverification-needed")
    public ResponseEntity<List<EmergencyContact>> getContactsNeedingReverification(@RequestParam int days) {
        return ResponseEntity.ok(contactService.getContactsNeedingReverification(days));
    }

    /**
     * Mark contact as inactive
     * POST /api/emergency-contacts/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<EmergencyContact> deactivateContact(@PathVariable Long id) {
        try {
            EmergencyContact deactivated = contactService.deactivateContact(id);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark contact as active
     * POST /api/emergency-contacts/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<EmergencyContact> activateContact(@PathVariable Long id) {
        try {
            EmergencyContact activated = contactService.activateContact(id);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // RELATIONSHIP-BASED QUERIES
    // ========================================================================

    /**
     * Get contacts by relationship type
     * GET /api/emergency-contacts/relationship/{relationship}
     */
    @GetMapping("/relationship/{relationship}")
    public ResponseEntity<List<EmergencyContact>> getContactsByRelationship(@PathVariable String relationship) {
        return ResponseEntity.ok(contactService.getContactsByRelationship(relationship));
    }

    /**
     * Get contacts for student by relationship
     * GET /api/emergency-contacts/student/{studentId}/relationship/{relationship}
     */
    @GetMapping("/student/{studentId}/relationship/{relationship}")
    public ResponseEntity<List<EmergencyContact>> getContactsByStudentAndRelationship(
            @PathVariable Long studentId,
            @PathVariable String relationship) {
        return ResponseEntity.ok(contactService.getContactsByStudentAndRelationship(studentId, relationship));
    }

    // ========================================================================
    // SEARCH
    // ========================================================================

    /**
     * Search contacts by name
     * GET /api/emergency-contacts/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<EmergencyContact>> searchContactsByName(@RequestParam String searchTerm) {
        return ResponseEntity.ok(contactService.searchContactsByName(searchTerm));
    }

    /**
     * Search contacts by phone
     * GET /api/emergency-contacts/search/phone
     */
    @GetMapping("/search/phone")
    public ResponseEntity<List<EmergencyContact>> searchContactsByPhone(@RequestParam String phoneNumber) {
        return ResponseEntity.ok(contactService.searchContactsByPhone(phoneNumber));
    }

    /**
     * Search contacts by email
     * GET /api/emergency-contacts/search/email
     */
    @GetMapping("/search/email")
    public ResponseEntity<List<EmergencyContact>> searchContactsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(contactService.searchContactsByEmail(email));
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Bulk verify contacts
     * POST /api/emergency-contacts/bulk/verify
     */
    @PostMapping("/bulk/verify")
    public ResponseEntity<List<EmergencyContact>> bulkVerifyContacts(@RequestBody List<Long> contactIds) {
        try {
            List<EmergencyContact> verified = contactService.bulkVerifyContacts(contactIds);
            return ResponseEntity.ok(verified);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk deactivate contacts
     * POST /api/emergency-contacts/bulk/deactivate
     */
    @PostMapping("/bulk/deactivate")
    public ResponseEntity<List<EmergencyContact>> bulkDeactivateContacts(@RequestBody List<Long> contactIds) {
        try {
            List<EmergencyContact> deactivated = contactService.bulkDeactivateContacts(contactIds);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // SIBLING OPERATIONS
    // ========================================================================

    /**
     * Copy contact to siblings
     * POST /api/emergency-contacts/{id}/copy-to-siblings
     */
    @PostMapping("/{id}/copy-to-siblings")
    public ResponseEntity<List<EmergencyContact>> copyContactToSiblings(@PathVariable Long id) {
        try {
            List<EmergencyContact> created = contactService.copyContactToSiblings(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Copy to siblings failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sync contact to siblings
     * POST /api/emergency-contacts/{id}/sync-to-siblings
     */
    @PostMapping("/{id}/sync-to-siblings")
    public ResponseEntity<List<EmergencyContact>> syncContactToSiblings(@PathVariable Long id) {
        try {
            List<EmergencyContact> synced = contactService.syncContactToSiblings(id);
            return ResponseEntity.ok(synced);
        } catch (IllegalArgumentException e) {
            log.error("Sync to siblings failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    /**
     * Validate contact information
     * POST /api/emergency-contacts/{id}/validate
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateContact(@PathVariable Long id) {
        try {
            boolean valid = contactService.validateContact(id);
            return ResponseEntity.ok(valid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get contacts with missing information
     * GET /api/emergency-contacts/incomplete
     */
    @GetMapping("/incomplete")
    public ResponseEntity<List<EmergencyContact>> getIncompleteContacts() {
        return ResponseEntity.ok(contactService.getIncompleteContacts());
    }

    /**
     * Get contacts missing phone numbers
     * GET /api/emergency-contacts/missing-phone
     */
    @GetMapping("/missing-phone")
    public ResponseEntity<List<EmergencyContact>> getContactsMissingPhone() {
        return ResponseEntity.ok(contactService.getContactsMissingPhone());
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count contacts for a student
     * GET /api/emergency-contacts/student/{studentId}/count
     */
    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<Long> countContactsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.countContactsForStudent(studentId));
    }

    /**
     * Count active contacts for a student
     * GET /api/emergency-contacts/student/{studentId}/count/active
     */
    @GetMapping("/student/{studentId}/count/active")
    public ResponseEntity<Long> countActiveContactsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(contactService.countActiveContactsForStudent(studentId));
    }
}
