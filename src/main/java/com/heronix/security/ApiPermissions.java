package com.heronix.security;

/**
 * API Permission Constants
 *
 * Defines standard permission scopes for API access control.
 * Permissions follow the format: {action}:{resource}
 *
 * Permission Hierarchy:
 * - read: View/retrieve data
 * - write: Create/update data
 * - delete: Remove data
 * - admin: Full administrative access
 *
 * Resource Categories:
 * - students: Student data and enrollment
 * - grades: Grade entry and reporting
 * - attendance: Attendance tracking
 * - behavior: Behavior incidents
 * - schedules: Schedule management
 * - courses: Course catalog
 * - teachers: Teacher profiles
 * - assignments: Assignment management
 * - analytics: Analytics and reports
 * - webhooks: Webhook management
 * - integrations: External integrations
 * - api-keys: API key management
 *
 * Usage in @PreAuthorize:
 * @PreAuthorize("hasAuthority('SCOPE_' + T(com.heronix.security.ApiPermissions).READ_STUDENTS)")
 *
 * Usage in API Key creation:
 * Set<String> scopes = Set.of(ApiPermissions.READ_STUDENTS, ApiPermissions.WRITE_GRADES);
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
public final class ApiPermissions {

    // ========================================================================
    // STUDENT PERMISSIONS
    // ========================================================================

    /** Read student profiles and enrollment data */
    public static final String READ_STUDENTS = "read:students";

    /** Create and update student records */
    public static final String WRITE_STUDENTS = "write:students";

    /** Delete student records */
    public static final String DELETE_STUDENTS = "delete:students";

    // ========================================================================
    // GRADE PERMISSIONS
    // ========================================================================

    /** View grades and grade reports */
    public static final String READ_GRADES = "read:grades";

    /** Enter and update grades */
    public static final String WRITE_GRADES = "write:grades";

    /** Delete grade entries */
    public static final String DELETE_GRADES = "delete:grades";

    // ========================================================================
    // ATTENDANCE PERMISSIONS
    // ========================================================================

    /** View attendance records */
    public static final String READ_ATTENDANCE = "read:attendance";

    /** Record and update attendance */
    public static final String WRITE_ATTENDANCE = "write:attendance";

    /** Delete attendance records */
    public static final String DELETE_ATTENDANCE = "delete:attendance";

    // ========================================================================
    // BEHAVIOR PERMISSIONS
    // ========================================================================

    /** View behavior incidents */
    public static final String READ_BEHAVIOR = "read:behavior";

    /** Create and update behavior incidents */
    public static final String WRITE_BEHAVIOR = "write:behavior";

    /** Delete behavior incidents */
    public static final String DELETE_BEHAVIOR = "delete:behavior";

    // ========================================================================
    // SCHEDULE PERMISSIONS
    // ========================================================================

    /** View schedules */
    public static final String READ_SCHEDULES = "read:schedules";

    /** Create and modify schedules */
    public static final String WRITE_SCHEDULES = "write:schedules";

    /** Delete schedules */
    public static final String DELETE_SCHEDULES = "delete:schedules";

    // ========================================================================
    // COURSE PERMISSIONS
    // ========================================================================

    /** View course catalog */
    public static final String READ_COURSES = "read:courses";

    /** Create and update courses */
    public static final String WRITE_COURSES = "write:courses";

    /** Delete courses */
    public static final String DELETE_COURSES = "delete:courses";

    // ========================================================================
    // TEACHER PERMISSIONS
    // ========================================================================

    /** View teacher profiles */
    public static final String READ_TEACHERS = "read:teachers";

    /** Create and update teacher records */
    public static final String WRITE_TEACHERS = "write:teachers";

    /** Delete teacher records */
    public static final String DELETE_TEACHERS = "delete:teachers";

    // ========================================================================
    // ASSIGNMENT PERMISSIONS
    // ========================================================================

    /** View assignments */
    public static final String READ_ASSIGNMENTS = "read:assignments";

    /** Create and update assignments */
    public static final String WRITE_ASSIGNMENTS = "write:assignments";

    /** Delete assignments */
    public static final String DELETE_ASSIGNMENTS = "delete:assignments";

    // ========================================================================
    // ANALYTICS PERMISSIONS
    // ========================================================================

    /** View analytics and reports */
    public static final String READ_ANALYTICS = "read:analytics";

    /** Generate custom analytics */
    public static final String WRITE_ANALYTICS = "write:analytics";

    // ========================================================================
    // WEBHOOK PERMISSIONS
    // ========================================================================

    /** View webhook configurations */
    public static final String READ_WEBHOOKS = "read:webhooks";

    /** Create and update webhooks */
    public static final String WRITE_WEBHOOKS = "write:webhooks";

    /** Delete webhooks */
    public static final String DELETE_WEBHOOKS = "delete:webhooks";

    // ========================================================================
    // INTEGRATION PERMISSIONS
    // ========================================================================

    /** View integration configurations */
    public static final String READ_INTEGRATIONS = "read:integrations";

    /** Configure integrations */
    public static final String WRITE_INTEGRATIONS = "write:integrations";

    /** Delete integrations */
    public static final String DELETE_INTEGRATIONS = "delete:integrations";

    // ========================================================================
    // API KEY PERMISSIONS
    // ========================================================================

    /** View API keys */
    public static final String READ_API_KEYS = "read:api-keys";

    /** Create and update API keys */
    public static final String WRITE_API_KEYS = "write:api-keys";

    /** Delete/revoke API keys */
    public static final String DELETE_API_KEYS = "delete:api-keys";

    // ========================================================================
    // ADMINISTRATIVE PERMISSIONS
    // ========================================================================

    /** Full system administration */
    public static final String ADMIN = "admin";

    /** View system settings */
    public static final String READ_SYSTEM = "read:system";

    /** Modify system settings */
    public static final String WRITE_SYSTEM = "write:system";

    // ========================================================================
    // PERMISSION GROUPS
    // ========================================================================

    /** All read permissions for teachers */
    public static final String[] TEACHER_READ_ALL = {
        READ_STUDENTS, READ_GRADES, READ_ATTENDANCE, READ_BEHAVIOR,
        READ_SCHEDULES, READ_COURSES, READ_ASSIGNMENTS
    };

    /** All write permissions for teachers */
    public static final String[] TEACHER_WRITE_ALL = {
        WRITE_GRADES, WRITE_ATTENDANCE, WRITE_BEHAVIOR, WRITE_ASSIGNMENTS
    };

    /** All read permissions for administrators */
    public static final String[] ADMIN_READ_ALL = {
        READ_STUDENTS, READ_GRADES, READ_ATTENDANCE, READ_BEHAVIOR,
        READ_SCHEDULES, READ_COURSES, READ_TEACHERS, READ_ASSIGNMENTS,
        READ_ANALYTICS, READ_WEBHOOKS, READ_INTEGRATIONS, READ_API_KEYS,
        READ_SYSTEM
    };

    /** All write permissions for administrators */
    public static final String[] ADMIN_WRITE_ALL = {
        WRITE_STUDENTS, WRITE_GRADES, WRITE_ATTENDANCE, WRITE_BEHAVIOR,
        WRITE_SCHEDULES, WRITE_COURSES, WRITE_TEACHERS, WRITE_ASSIGNMENTS,
        WRITE_ANALYTICS, WRITE_WEBHOOKS, WRITE_INTEGRATIONS, WRITE_API_KEYS,
        WRITE_SYSTEM
    };

    private ApiPermissions() {
        // Prevent instantiation
    }
}
