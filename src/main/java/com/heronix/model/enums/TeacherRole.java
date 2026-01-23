package com.heronix.model.enums;

/**
 * Enum representing different roles for instructional staff (Teachers)
 *
 * NOTE: Non-instructional roles (Counselor, Administrator, Principal)
 * are now handled via the Staff entity with StaffOccupation enum.
 *
 * @author Heronix Scheduling System Team
 * @version 2.0.0
 * @since 2025-11-16
 * @updated Phase 59 - Staff/Teacher Separation
 */
public enum TeacherRole {

    /**
     * Lead teacher - Primary instructor for a course
     */
    LEAD_TEACHER("Lead Teacher", "Primary course instructor"),

    /**
     * Co-teacher - Supports students with IEPs across multiple classes
     * Follows assigned students from class to class throughout the day
     */
    CO_TEACHER("Co-Teacher", "IEP support teacher following assigned students"),

    /**
     * Specialist - Reading specialists, math specialists, etc.
     * Teachers who provide specialized instruction
     */
    SPECIALIST("Specialist", "Specialized instruction provider"),

    /**
     * Substitute teacher - Temporary replacement for absent teachers
     */
    SUBSTITUTE("Substitute", "Temporary replacement teacher"),

    /**
     * Instructional Coach - Supports other teachers with instruction
     * May also teach demonstration lessons
     */
    INSTRUCTIONAL_COACH("Instructional Coach", "Teacher support and coaching"),

    // Legacy roles - kept for backwards compatibility but deprecated
    // Use Staff entity with appropriate StaffOccupation instead

    /**
     * @deprecated Use Staff entity with StaffOccupation.ADMINISTRATIVE_ASSISTANT or similar
     */
    @Deprecated
    ADMINISTRATOR("Administrator", "School administration - use Staff entity"),

    /**
     * @deprecated Use Staff entity with StaffOccupation.COUNSELOR
     */
    @Deprecated
    PRINCIPAL("Principal", "School principal - use Staff entity"),

    /**
     * @deprecated Use Staff entity with StaffOccupation.COUNSELOR or GUIDANCE_COUNSELOR
     */
    @Deprecated
    COUNSELOR("Counselor", "School counseling staff - use Staff entity");

    private final String displayName;
    private final String description;

    TeacherRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
