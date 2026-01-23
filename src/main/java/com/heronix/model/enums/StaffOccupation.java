package com.heronix.model.enums;

/**
 * Enum representing different staff occupations/positions
 * Used to categorize non-instructional staff members
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
public enum StaffOccupation {

    // Educational Support Staff
    PARAPROFESSIONAL("Paraprofessional", "Educational support aide", StaffCategory.EDUCATIONAL_SUPPORT),
    TEACHER_AIDE("Teacher Aide", "Classroom support assistant", StaffCategory.EDUCATIONAL_SUPPORT),
    INSTRUCTIONAL_COACH("Instructional Coach", "Teacher development and coaching", StaffCategory.EDUCATIONAL_SUPPORT),
    LIBRARY_MEDIA_SPECIALIST("Library Media Specialist", "Library and media services", StaffCategory.EDUCATIONAL_SUPPORT),
    TUTOR("Tutor", "Academic tutoring support", StaffCategory.EDUCATIONAL_SUPPORT),

    // Administrative Staff
    ADMINISTRATIVE_ASSISTANT("Administrative Assistant", "Office administration and clerical support", StaffCategory.ADMINISTRATIVE),
    SCHOOL_SECRETARY("School Secretary", "Front office reception and coordination", StaffCategory.ADMINISTRATIVE),
    REGISTRAR("Registrar", "Student records and enrollment management", StaffCategory.ADMINISTRATIVE),
    DATA_CLERK("Data Clerk", "Data entry and records management", StaffCategory.ADMINISTRATIVE),
    ATTENDANCE_CLERK("Attendance Clerk", "Attendance tracking and reporting", StaffCategory.ADMINISTRATIVE),
    BOOKKEEPER("Bookkeeper", "Financial records and accounting", StaffCategory.ADMINISTRATIVE),
    PAYROLL_SPECIALIST("Payroll Specialist", "Payroll processing", StaffCategory.ADMINISTRATIVE),
    HR_COORDINATOR("HR Coordinator", "Human resources coordination", StaffCategory.ADMINISTRATIVE),

    // Student Services
    COUNSELOR("Counselor", "Academic and personal counseling", StaffCategory.STUDENT_SERVICES),
    GUIDANCE_COUNSELOR("Guidance Counselor", "Academic guidance and college preparation", StaffCategory.STUDENT_SERVICES),
    SCHOOL_PSYCHOLOGIST("School Psychologist", "Psychological assessment and support", StaffCategory.STUDENT_SERVICES),
    SOCIAL_WORKER("Social Worker", "Social services and family support", StaffCategory.STUDENT_SERVICES),
    BEHAVIOR_SPECIALIST("Behavior Specialist", "Behavior intervention and support", StaffCategory.STUDENT_SERVICES),
    SPEECH_THERAPIST("Speech Therapist", "Speech and language therapy", StaffCategory.STUDENT_SERVICES),
    OCCUPATIONAL_THERAPIST("Occupational Therapist", "Occupational therapy services", StaffCategory.STUDENT_SERVICES),
    PHYSICAL_THERAPIST("Physical Therapist", "Physical therapy services", StaffCategory.STUDENT_SERVICES),

    // Health Services
    SCHOOL_NURSE("School Nurse", "Student health and medical services", StaffCategory.HEALTH_SERVICES),
    NURSE_AIDE("Nurse Aide", "Medical support assistant", StaffCategory.HEALTH_SERVICES),
    HEALTH_CLERK("Health Clerk", "Health records and medication tracking", StaffCategory.HEALTH_SERVICES),

    // Security and Safety
    SECURITY_OFFICER("Security Officer", "Campus security and safety", StaffCategory.SECURITY),
    SCHOOL_RESOURCE_OFFICER("School Resource Officer", "Law enforcement liaison", StaffCategory.SECURITY),
    HALL_MONITOR("Hall Monitor", "Hallway supervision", StaffCategory.SECURITY),

    // Facilities and Operations
    CUSTODIAN("Custodian", "Building cleaning and maintenance", StaffCategory.FACILITIES),
    MAINTENANCE_TECHNICIAN("Maintenance Technician", "Building and equipment repair", StaffCategory.FACILITIES),
    GROUNDSKEEPER("Groundskeeper", "Outdoor grounds maintenance", StaffCategory.FACILITIES),
    FACILITIES_MANAGER("Facilities Manager", "Facilities oversight and coordination", StaffCategory.FACILITIES),

    // Food Services
    CAFETERIA_MANAGER("Cafeteria Manager", "Food service management", StaffCategory.FOOD_SERVICES),
    CAFETERIA_WORKER("Cafeteria Worker", "Food preparation and service", StaffCategory.FOOD_SERVICES),
    KITCHEN_STAFF("Kitchen Staff", "Kitchen operations", StaffCategory.FOOD_SERVICES),

    // Transportation
    BUS_DRIVER("Bus Driver", "Student transportation", StaffCategory.TRANSPORTATION),
    BUS_AIDE("Bus Aide", "Transportation aide for special needs", StaffCategory.TRANSPORTATION),
    TRANSPORTATION_COORDINATOR("Transportation Coordinator", "Transportation scheduling and logistics", StaffCategory.TRANSPORTATION),

    // Technology
    IT_TECHNICIAN("IT Technician", "Technology support and maintenance", StaffCategory.TECHNOLOGY),
    NETWORK_ADMINISTRATOR("Network Administrator", "Network infrastructure management", StaffCategory.TECHNOLOGY),
    TECHNOLOGY_COORDINATOR("Technology Coordinator", "Technology integration and training", StaffCategory.TECHNOLOGY),
    HELP_DESK_SUPPORT("Help Desk Support", "Technical assistance", StaffCategory.TECHNOLOGY),

    // Athletics and Activities
    ATHLETIC_DIRECTOR("Athletic Director", "Athletic program management", StaffCategory.ATHLETICS),
    COACH("Coach", "Athletic coaching", StaffCategory.ATHLETICS),
    ACTIVITY_SPONSOR("Activity Sponsor", "Extracurricular activity sponsor", StaffCategory.ATHLETICS),

    // Other
    OTHER("Other", "Other staff position", StaffCategory.OTHER);

    private final String displayName;
    private final String description;
    private final StaffCategory category;

    StaffOccupation(String displayName, String description, StaffCategory category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public StaffCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Staff category for grouping occupations
     */
    public enum StaffCategory {
        EDUCATIONAL_SUPPORT("Educational Support"),
        ADMINISTRATIVE("Administrative"),
        STUDENT_SERVICES("Student Services"),
        HEALTH_SERVICES("Health Services"),
        SECURITY("Security"),
        FACILITIES("Facilities"),
        FOOD_SERVICES("Food Services"),
        TRANSPORTATION("Transportation"),
        TECHNOLOGY("Technology"),
        ATHLETICS("Athletics"),
        OTHER("Other");

        private final String displayName;

        StaffCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
