package com.heronix.model.domain;

import com.heronix.model.enums.StaffOccupation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Staff Entity
 *
 * Represents non-instructional staff members including:
 * - Paraprofessionals and Teacher Aides
 * - Administrative Staff (Secretaries, Registrars, Clerks)
 * - Student Services (Counselors, Psychologists, Social Workers)
 * - Health Services (Nurses, Health Clerks)
 * - Facilities (Custodians, Maintenance)
 * - Food Services (Cafeteria Staff)
 * - Security (Officers, Monitors)
 * - Transportation (Bus Drivers, Aides)
 * - Technology (IT Support)
 * - Athletics (Coaches, Directors)
 *
 * NOTE: Teachers and Co-Teachers are in the Teacher entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
@Entity
@Table(name = "staff", indexes = {
    @Index(name = "idx_staff_occupation", columnList = "occupation"),
    @Index(name = "idx_staff_department", columnList = "department"),
    @Index(name = "idx_staff_campus", columnList = "primary_campus_id"),
    @Index(name = "idx_staff_active", columnList = "active"),
    @Index(name = "idx_staff_employee_id", columnList = "employee_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // PERSONAL INFORMATION
    // ========================================================================

    @Column(name = "first_name", nullable = false)
    @NotNull(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "preferred_name")
    private String preferredName;

    @Column(name = "title", length = 20)
    private String title; // Mr., Ms., Mrs., Dr., etc.

    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "ssn_last_four", length = 4)
    private String ssnLastFour;

    // ========================================================================
    // ADDRESS INFORMATION
    // ========================================================================

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    // ========================================================================
    // OCCUPATION & ASSIGNMENT
    // ========================================================================

    /**
     * Staff occupation type - determines which category/list this staff appears in
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "occupation", nullable = false)
    @NotNull(message = "Occupation is required")
    private StaffOccupation occupation;

    /**
     * Job title (more specific than occupation)
     * Examples: "Lead Custodian", "Senior Administrative Assistant"
     */
    @Column(name = "job_title")
    private String jobTitle;

    /**
     * Department this staff member belongs to
     */
    @Column(name = "department")
    private String department;

    /**
     * Work location/office
     */
    @Column(name = "work_location")
    private String workLocation;

    /**
     * Office room number
     */
    @Column(name = "office_room")
    private String officeRoom;

    /**
     * Building assignment (for multi-building campuses)
     */
    @Column(name = "building")
    private String building;

    /**
     * Supervisor name or ID
     */
    @Column(name = "supervisor")
    private String supervisor;

    /**
     * Primary campus assignment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_campus_id")
    @ToString.Exclude
    private Campus primaryCampus;

    // ========================================================================
    // EMPLOYMENT INFORMATION
    // ========================================================================

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    /**
     * Employment type: FULL_TIME, PART_TIME, CONTRACTOR, TEMPORARY
     */
    @Column(name = "employment_type", length = 50)
    private String employmentType;

    /**
     * Pay type: HOURLY, SALARY, STIPEND
     */
    @Column(name = "pay_type", length = 50)
    private String payType;

    /**
     * Work schedule type: Regular, Shift, Flexible
     */
    @Column(name = "schedule_type", length = 50)
    private String scheduleType;

    /**
     * Standard work hours (e.g., "7:30 AM - 4:00 PM")
     */
    @Column(name = "work_hours")
    private String workHours;

    /**
     * Total years of experience
     */
    @Column(name = "years_experience")
    private Integer yearsExperience;

    // ========================================================================
    // AUTHENTICATION & SECURITY
    // ========================================================================

    /**
     * BCrypt encrypted password for staff authentication
     */
    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * QR code identifier for quick login/check-in
     */
    @Column(name = "qr_code_id", unique = true, length = 100)
    private String qrCodeId;

    @Column(name = "qr_generated_date")
    private LocalDateTime qrGeneratedDate;

    @Column(name = "last_qr_scan")
    private LocalDateTime lastQrScan;

    /**
     * Path to staff photo
     */
    @Column(name = "photo_path", length = 500)
    private String photoPath;

    // ========================================================================
    // CERTIFICATIONS & SKILLS
    // ========================================================================

    /**
     * Certifications held (e.g., "CPR Certified", "Crisis Intervention", "OSHA")
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "staff_certifications",
                     joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "certification")
    @ToString.Exclude
    private Set<String> certifications = new HashSet<>();

    /**
     * Specialized skills
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "staff_skills",
                     joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "skill")
    @ToString.Exclude
    private Set<String> skills = new HashSet<>();

    /**
     * Languages spoken
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "staff_languages",
                     joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "language")
    @ToString.Exclude
    private Set<String> languages = new HashSet<>();

    // ========================================================================
    // I-9 EMPLOYMENT ELIGIBILITY VERIFICATION (Federal - REQUIRED)
    // ========================================================================

    @Column(name = "i9_completion_date")
    private LocalDate i9CompletionDate;

    @Column(name = "i9_document_type", length = 100)
    private String i9DocumentType;

    @Column(name = "i9_document_number", length = 100)
    private String i9DocumentNumber;

    @Column(name = "i9_expiration_date")
    private LocalDate i9ExpirationDate;

    @Column(name = "i9_verified_by")
    private String i9VerifiedBy;

    @Column(name = "i9_status", length = 50)
    private String i9Status;

    @Column(name = "i9_form_path", length = 500)
    private String i9FormPath;

    // ========================================================================
    // BACKGROUND CHECKS & FINGERPRINTING (State - REQUIRED)
    // ========================================================================

    @Column(name = "background_check_date")
    private LocalDate backgroundCheckDate;

    @Column(name = "background_check_status", length = 50)
    private String backgroundCheckStatus;

    @Column(name = "background_check_expiration")
    private LocalDate backgroundCheckExpiration;

    @Column(name = "background_check_type", length = 50)
    private String backgroundCheckType;

    @Column(name = "fingerprint_date")
    private LocalDate fingerprintDate;

    @Column(name = "fingerprint_agency")
    private String fingerprintAgency;

    @Column(name = "criminal_history_result", length = 50)
    private String criminalHistoryResult;

    @Column(name = "background_check_document_path", length = 500)
    private String backgroundCheckDocumentPath;

    // ========================================================================
    // EMERGENCY CONTACT
    // ========================================================================

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_email")
    private String emergencyContactEmail;

    // ========================================================================
    // OCCUPATION-SPECIFIC FIELDS
    // ========================================================================

    /**
     * For PARAPROFESSIONAL/TEACHER_AIDE: Assignment type
     * ONE_ON_ONE, SMALL_GROUP, CLASSROOM_SUPPORT, FLOATER
     */
    @Column(name = "para_assignment_type", length = 50)
    private String paraAssignmentType;

    /**
     * For PARAPROFESSIONAL: Maximum students can support
     */
    @Column(name = "max_students")
    private Integer maxStudents;

    /**
     * For PARAPROFESSIONAL: Assigned students
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "staff_student_assignments",
        joinColumns = @JoinColumn(name = "staff_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @ToString.Exclude
    private List<Student> assignedStudents = new ArrayList<>();

    /**
     * For PARAPROFESSIONAL: Medical training flag
     */
    @Column(name = "medical_training")
    private Boolean medicalTraining = false;

    /**
     * For PARAPROFESSIONAL: Behavioral training flag
     */
    @Column(name = "behavioral_training")
    private Boolean behavioralTraining = false;

    /**
     * For BUS_DRIVER: CDL license number
     */
    @Column(name = "cdl_number", length = 50)
    private String cdlNumber;

    /**
     * For BUS_DRIVER: CDL expiration date
     */
    @Column(name = "cdl_expiration")
    private LocalDate cdlExpiration;

    /**
     * For NURSE: Nursing license number
     */
    @Column(name = "nursing_license", length = 50)
    private String nursingLicense;

    /**
     * For NURSE: Nursing license expiration
     */
    @Column(name = "nursing_license_expiration")
    private LocalDate nursingLicenseExpiration;

    /**
     * For COUNSELOR: License type (LPC, LCSW, etc.)
     */
    @Column(name = "counselor_license_type", length = 50)
    private String counselorLicenseType;

    /**
     * For COUNSELOR: License number
     */
    @Column(name = "counselor_license_number", length = 50)
    private String counselorLicenseNumber;

    /**
     * For COUNSELOR: License expiration
     */
    @Column(name = "counselor_license_expiration")
    private LocalDate counselorLicenseExpiration;

    // ========================================================================
    // NOTES & METADATA
    // ========================================================================

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "availability_notes", columnDefinition = "TEXT")
    private String availabilityNotes;

    // ========================================================================
    // SOFT DELETE SUPPORT
    // ========================================================================

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    private Long version;

    // ========================================================================
    // LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get full name
     */
    public String getFullName() {
        if (preferredName != null && !preferredName.isEmpty()) {
            return preferredName + " " + lastName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Get display name with title
     */
    public String getDisplayName() {
        String t = (title != null && !title.isEmpty()) ? title + " " : "";
        return t + getFullName();
    }

    /**
     * Get name in "Last, First" format
     */
    public String getNameLastFirst() {
        return lastName + ", " + firstName;
    }

    /**
     * Get occupation display name
     */
    public String getOccupationDisplay() {
        return occupation != null ? occupation.getDisplayName() : "Unknown";
    }

    /**
     * Get occupation category display name
     */
    public String getCategoryDisplay() {
        return occupation != null ? occupation.getCategory().getDisplayName() : "Unknown";
    }

    /**
     * Check if staff is active and not deleted
     */
    public boolean isActiveStaff() {
        return Boolean.TRUE.equals(active) && !Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if background check is expired or expiring soon
     */
    public boolean hasBackgroundCheckExpiring(int daysThreshold) {
        if (backgroundCheckExpiration == null) {
            return false;
        }
        return backgroundCheckExpiration.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    /**
     * Check if background check is expired
     */
    public boolean isBackgroundCheckExpired() {
        return backgroundCheckExpiration != null &&
               backgroundCheckExpiration.isBefore(LocalDate.now());
    }

    /**
     * Check if I-9 needs reverification
     */
    public boolean needsI9Reverification() {
        return i9ExpirationDate != null &&
               i9ExpirationDate.isBefore(LocalDate.now().plusDays(90));
    }

    /**
     * Check if this is a paraprofessional type
     */
    public boolean isParaprofessional() {
        return occupation == StaffOccupation.PARAPROFESSIONAL ||
               occupation == StaffOccupation.TEACHER_AIDE;
    }

    /**
     * Check if this is administrative staff
     */
    public boolean isAdministrative() {
        return occupation != null &&
               occupation.getCategory() == StaffOccupation.StaffCategory.ADMINISTRATIVE;
    }

    /**
     * Check if this is student services staff
     */
    public boolean isStudentServices() {
        return occupation != null &&
               occupation.getCategory() == StaffOccupation.StaffCategory.STUDENT_SERVICES;
    }

    /**
     * Get current student count (for paraprofessionals)
     */
    public int getCurrentStudentCount() {
        return assignedStudents != null ? assignedStudents.size() : 0;
    }

    /**
     * Check if paraprofessional is at capacity
     */
    public boolean isAtStudentCapacity() {
        return maxStudents != null && getCurrentStudentCount() >= maxStudents;
    }

    /**
     * Add a certification
     */
    public void addCertification(String certification) {
        if (certifications == null) {
            certifications = new HashSet<>();
        }
        certifications.add(certification);
    }

    /**
     * Add a skill
     */
    public void addSkill(String skill) {
        if (skills == null) {
            skills = new HashSet<>();
        }
        skills.add(skill);
    }

    /**
     * Add a language
     */
    public void addLanguage(String language) {
        if (languages == null) {
            languages = new HashSet<>();
        }
        languages.add(language);
    }

    /**
     * Assign a student (for paraprofessionals)
     */
    public void assignStudent(Student student) {
        if (assignedStudents == null) {
            assignedStudents = new ArrayList<>();
        }
        if (!assignedStudents.contains(student)) {
            assignedStudents.add(student);
        }
    }

    /**
     * Remove an assigned student
     */
    public void removeAssignedStudent(Student student) {
        if (assignedStudents != null) {
            assignedStudents.remove(student);
        }
    }

    /**
     * Get certifications as comma-separated string
     */
    public String getCertificationsDisplay() {
        if (certifications == null || certifications.isEmpty()) {
            return "None";
        }
        return String.join(", ", certifications);
    }

    /**
     * Get skills as comma-separated string
     */
    public String getSkillsDisplay() {
        if (skills == null || skills.isEmpty()) {
            return "None";
        }
        return String.join(", ", skills);
    }

    /**
     * Get languages as comma-separated string
     */
    public String getLanguagesDisplay() {
        if (languages == null || languages.isEmpty()) {
            return "None";
        }
        return String.join(", ", languages);
    }

    /**
     * Soft delete this staff member
     */
    public void softDelete(String deletedByUser) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUser;
        this.active = false;
    }

    /**
     * Restore a soft-deleted staff member
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.active = true;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", occupation=" + occupation +
                ", department='" + department + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return id != null && id.equals(staff.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
