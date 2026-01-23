package com.heronix.model.enums;

/**
 * RegistrationStatus Enum - Student Registration Workflow Status
 *
 * Tracks the status of a student registration through the enrollment process.
 * Includes support for incomplete registrations when documents are missing.
 *
 * Workflow:
 * 1. DRAFT - Registration started, data entry in progress
 * 2. INCOMPLETE_DOCUMENTS - Saved but missing required documents
 * 3. INCOMPLETE_PHOTO - Saved but missing student photo (or photo refused)
 * 4. PENDING_DOCUMENTS - Waiting for parent to bring documents
 * 5. SUBMITTED - All data complete, ready for review
 * 6. UNDER_REVIEW - Being reviewed by registrar/admin
 * 7. APPROVED - Approved, seat reserved
 * 8. CONFIRMED - Final enrollment confirmed, student active
 * 9. WAITLISTED - Approved but no seats available
 * 10. CANCELLED - Registration cancelled
 * 11. REJECTED - Registration rejected (missing requirements, etc.)
 *
 * @author Heronix SIS Team
 * @version 2.0.0
 * @since 2026-01
 */
public enum RegistrationStatus {

    // Initial states
    DRAFT("Draft", "Registration in progress", false, true),

    // Incomplete states (saved but missing items)
    INCOMPLETE_DOCUMENTS("Incomplete - Documents", "Missing required documents", false, true),
    INCOMPLETE_PHOTO("Incomplete - Photo", "Missing student photo", false, true),
    PENDING_DOCUMENTS("Pending Documents", "Waiting for parent to submit documents", false, true),

    // Submitted states
    SUBMITTED("Submitted", "Ready for review", false, false),
    UNDER_REVIEW("Under Review", "Being reviewed by registrar", false, false),

    // Approval states
    APPROVED("Approved", "Approved - seat reserved", true, false),
    CONFIRMED("Confirmed", "Final enrollment confirmed", true, false),
    WAITLISTED("Waitlisted", "Approved but no seats available", true, false),

    // Terminal states
    CANCELLED("Cancelled", "Registration cancelled", false, false),
    REJECTED("Rejected", "Registration rejected", false, false);

    private final String displayName;
    private final String description;
    private final boolean isApproved;
    private final boolean canEdit;

    RegistrationStatus(String displayName, String description, boolean isApproved, boolean canEdit) {
        this.displayName = displayName;
        this.description = description;
        this.isApproved = isApproved;
        this.canEdit = canEdit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Is this an approved/successful status?
     */
    public boolean isApproved() {
        return isApproved;
    }

    /**
     * Can the registration still be edited?
     */
    public boolean canEdit() {
        return canEdit;
    }

    /**
     * Is this an incomplete status that needs follow-up?
     */
    public boolean isIncomplete() {
        return this == INCOMPLETE_DOCUMENTS ||
               this == INCOMPLETE_PHOTO ||
               this == PENDING_DOCUMENTS;
    }

    /**
     * Is this a terminal/final status?
     */
    public boolean isTerminal() {
        return this == CONFIRMED || this == CANCELLED || this == REJECTED;
    }

    /**
     * Can this status be submitted for review?
     */
    public boolean canSubmit() {
        return this == DRAFT || this == INCOMPLETE_DOCUMENTS ||
               this == INCOMPLETE_PHOTO || this == PENDING_DOCUMENTS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
