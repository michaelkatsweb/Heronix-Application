# Role-Based Access Control (RBAC) - Future Implementation Plan

**Date**: 2025-12-24
**Status**: 📋 **PLANNED FOR FUTURE**
**Priority**: Medium (After core functionality is complete)

---

## User Requirement

**User Statement**:
> "When we create Role accounts we will just 'grey out' the systems and functions, for example the registrar needs access to all registration features, no access to account creations, grades and assignments etc.. we will figure out the roles at a later time"

---

## Implementation Approach

### UI/UX Strategy: "Grey Out" Disabled Features

Instead of hiding features completely, the system will:
- ✅ **Show all features** (for discoverability)
- ✅ **Grey out/disable** features user doesn't have access to
- ✅ **Tooltip explanation** on hover explaining why feature is disabled

**Benefits**:
1. Users can see full system capabilities
2. Clear visual feedback on permissions
3. Better training and onboarding
4. Prevents confusion ("where did that button go?")
5. Professional UX pattern

---

## Proposed Role Structure

### Administrative Roles

#### 1. **Registrar**
**Access**:
- ✅ Student Enrollment & Registration (full access)
- ✅ Student Demographics Management
- ✅ Enrollment Requests & Approval
- ✅ Transfer Students (in/out)
- ✅ Student Withdrawals
- ✅ Document Verification
- ✅ Boundary/Address Validation
- ✅ View Student Records (read-only)

**Restricted** (greyed out):
- ❌ Grade Entry/Management
- ❌ Assignment Creation
- ❌ Course Creation
- ❌ User Account Creation
- ❌ System Settings
- ❌ Financial Management

#### 2. **Teacher**
**Access**:
- ✅ View Students (assigned classes only)
- ✅ Grade Entry/Management (own courses)
- ✅ Assignment Creation (own courses)
- ✅ Attendance Tracking (own classes)
- ✅ Course Recommendations
- ✅ Medical Records (read-only for assigned students)
- ✅ Parent/Guardian Contact (assigned students)

**Restricted** (greyed out):
- ❌ Student Enrollment/Registration
- ❌ Student Withdrawals
- ❌ Course Creation/Deletion
- ❌ User Account Creation
- ❌ System Settings
- ❌ Financial Management
- ❌ Grade Changes (outside own courses)

#### 3. **Counselor**
**Access**:
- ✅ View All Students
- ✅ Academic Planning
- ✅ Course Recommendations
- ✅ Student Accommodations (504/IEP)
- ✅ Academic Interventions
- ✅ Student Groups/Cohorts
- ✅ Medical Records (read-only)
- ✅ Parent/Guardian Contact

**Restricted** (greyed out):
- ❌ Student Enrollment/Registration
- ❌ Grade Entry (direct changes)
- ❌ Course Creation/Deletion
- ❌ User Account Creation
- ❌ System Settings
- ❌ Financial Management

#### 4. **Administrator/Principal**
**Access**:
- ✅ View All Students
- ✅ View All Grades
- ✅ View All Courses
- ✅ Reports & Analytics
- ✅ Attendance Monitoring
- ✅ Staff Management (view)
- ✅ Schedule Oversight

**Restricted** (greyed out):
- ❌ Direct Grade Entry
- ❌ Student Enrollment (delegates to Registrar)
- ❌ Course Creation (delegates to Department Heads)
- ❌ User Account Creation (delegates to IT)
- ❌ System Settings (delegates to IT)

#### 5. **IT Administrator**
**Access**:
- ✅ User Account Creation/Management (username, password reset, email)
- ✅ System Settings (technical configuration only)
- ✅ Database Management (backup/restore, performance tuning)
- ✅ Integration Configuration (API keys, SSO, third-party integrations)
- ✅ Backup/Restore operations
- ✅ System monitoring and diagnostics
- ✅ Technical support and troubleshooting

**RESTRICTED ACCESS** (FERPA Compliance - Least Privilege Principle):
- ❌ **Grades/Academic Records**: Cannot view, edit, or access student grades
- ❌ **IEP/504 Plans**: Cannot view or modify special education records
- ❌ **Medical Records**: Cannot access student health information
- ❌ **Discipline Records**: Cannot view student behavioral/discipline history
- ❌ **Financial Information**: Cannot access family financial data
- ❌ **Student Enrollment**: Cannot enroll or withdraw students
- ❌ **Assessment Data**: Cannot view student test scores or evaluations
- ❌ **Counseling Notes**: Cannot access student counseling records
- ⚠️ **Read-Only Student Demographics** (only when necessary for technical support with audit trail)

**Permitted Operations**:
- ✅ Password resets (with user verification)
- ✅ Email account configuration
- ✅ User account activation/deactivation
- ✅ System performance monitoring (anonymized data)
- ✅ Database optimization (without viewing record contents)
- ✅ Troubleshooting technical issues (with specific user permission and audit logging)

---

## Legal & Compliance Requirements

### FERPA (Family Educational Rights and Privacy Act) Compliance

**Key Principle**: **Least Privilege & Need-to-Know Basis**

All access to student educational records must follow the principle of least privilege, ensuring staff only access data necessary for their legitimate educational interest.

#### What FERPA Protects

**Educational Records** (covered under FERPA):
- 📊 Grades, transcripts, test scores, GPA
- 📚 Course schedules, class lists, assignments
- 🏥 **Health records maintained by school** (K-12 level)
- 🎯 **IEP (Individualized Education Program) records**
- 🎯 **504 Plan records and case documentation**
- 💰 Student financial information
- 📋 Discipline files and behavioral records
- 📧 Student email communications

**Important**: HIPAA does NOT apply to schools. Health information in IEPs and 504 Plans is considered part of the student's educational record under FERPA, not medical records under HIPAA.

#### School Official Access Criteria

For IT staff (or any school official) to access student records, they must meet ALL criteria:

1. ✅ **Legitimate Educational Interest**: The access must be necessary to fulfill their professional responsibilities
2. ✅ **Formally Defined Role**: Their role must be specified in the school's annual FERPA notification
3. ✅ **Need-to-Know Basis**: Access limited to specific records needed for their job function
4. ✅ **Audit Trail**: All access must be logged with purpose and timestamp
5. ✅ **Training Requirement**: Staff must complete annual FERPA training

#### IT Administrator Access Under FERPA

**Legitimate Technical Access** (Permitted):
- ✅ User account management (username, password, email configuration)
- ✅ System maintenance (backups, performance tuning, security updates)
- ✅ Technical troubleshooting (with specific authorization and logging)
- ✅ Database administration (without viewing protected record contents)

**Prohibited Access** (Violates Least Privilege):
- ❌ Viewing student grades (no educational interest)
- ❌ Reading IEP/504 plans (special education records)
- ❌ Accessing medical/health records
- ❌ Viewing discipline records
- ❌ Reading counseling notes
- ❌ Accessing financial information

**Exception**: Emergency access only with:
1. Explicit authorization from school administrator
2. Documented reason for access
3. Full audit trail
4. Time-limited access
5. Immediate revocation after issue resolved

#### FERPA Violations & Penalties

**Common Violations**:
- Sharing student records without consent
- Posting grades with identifiable information (e.g., SSN, student ID on public board)
- Leaving records unsecured (physical or digital)
- **Accessing files without a valid reason** ⚠️ (applies to IT staff)
- Unauthorized disclosure to third parties

**Consequences**:
- Loss of federal funding for the institution
- Personal liability for staff members
- Civil lawsuits from affected families
- Criminal charges in cases of willful disclosure

#### Record-Keeping Requirements

**Access Logs** (Required):
```
- Who accessed the record (user ID, name, role)
- When (timestamp)
- What record (student ID, record type)
- Why (purpose/justification)
- How long (session duration)
- What actions taken (view, edit, export)
```

**Retention**: Access logs must be maintained as long as the education records are retained.

#### Data Security Requirements (2025 Standards)

**Technical Safeguards**:
- ✅ Password protection for all databases
- ✅ Encryption of stored data (at rest)
- ✅ Encryption of transmitted data (in transit)
- ✅ Multi-factor authentication (MFA) for administrative access
- ✅ Role-based access control (RBAC)
- ✅ Regular security audits
- ✅ Intrusion detection systems
- ✅ Secure backup procedures

**Administrative Safeguards**:
- ✅ Annual FERPA training for all staff
- ✅ Written policies and procedures
- ✅ Regular access reviews (quarterly recommended)
- ✅ Immediate revocation upon role change or termination
- ✅ Incident response plan
- ✅ Data breach notification procedures

#### Annual Notification Requirements

Schools must notify parents/guardians annually in writing about:
1. Their rights under FERPA
2. Who qualifies as a "school official"
3. What constitutes "legitimate educational interest"
4. How to inspect and review records
5. How to request amendments to records
6. How to file complaints with the Department of Education

### Best Practices for IT Administrator Access

**Recommended Approach**:

1. **Separation of Duties**
   - System administration ≠ Data administration
   - IT manages infrastructure, not student data
   - Curriculum/academic staff manage educational content

2. **Technical-Only Access**
   - IT can manage database server, not view record contents
   - IT can reset passwords, not read student emails
   - IT can perform backups, not restore individual records to view them

3. **Anonymized Data for Testing**
   - Use synthetic/anonymized data for testing
   - Never use production student data for development
   - Mask sensitive fields in non-production environments

4. **Break-Glass Access**
   - Emergency access only with administrator approval
   - Automatic notifications to school officials
   - Temporary credentials that auto-expire
   - Full audit trail required

5. **Privacy by Design**
   - Build systems with minimal IT access by default
   - Use service accounts with limited scope
   - Implement view restrictions at database level
   - Encrypt sensitive fields separately

### Compliance Checklist for Heronix-SIS

When implementing RBAC:

- [ ] **Access Control Matrix**: Document which roles can access which data types
- [ ] **IT Role Definition**: Formally define IT Administrator role with restricted access
- [ ] **Audit Logging**: Implement comprehensive access logging system
- [ ] **Annual Review Process**: Establish quarterly access review procedures
- [ ] **Training Program**: Create FERPA training module for all users
- [ ] **Incident Response**: Document procedures for unauthorized access
- [ ] **Parent Notification**: Template for annual FERPA rights notification
- [ ] **Emergency Access Protocol**: Procedure for IT emergency access with approval
- [ ] **Data Masking**: Implement field-level encryption for sensitive data
- [ ] **Access Request Form**: Formal process for temporary access grants
- [ ] **Termination Procedures**: Immediate access revocation checklist
- [ ] **Third-Party Agreements**: FERPA compliance clauses for vendors

---

## Technical Implementation Plan

### Phase 1: Role Infrastructure (Foundation)

**Database Schema**:
```sql
-- Already exists in User entity
private String role;  // Current implementation

-- Future: Move to Role entity
CREATE TABLE role (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    description TEXT
);

-- Permission mapping
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY,
    role_id BIGINT REFERENCES role(id),
    permission_id BIGINT REFERENCES permission(id)
);

-- User-Role mapping (many-to-many for users with multiple roles)
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES user(id),
    role_id BIGINT REFERENCES role(id)
);
```

**Permission Enum**:
```java
public enum Permission {
    // Student Management
    STUDENT_ENROLL("student:enroll", "Enroll new students"),
    STUDENT_VIEW("student:view", "View student records"),
    STUDENT_EDIT("student:edit", "Edit student information"),
    STUDENT_WITHDRAW("student:withdraw", "Withdraw students"),

    // Grade Management
    GRADE_VIEW("grade:view", "View grades"),
    GRADE_ENTER("grade:enter", "Enter/modify grades"),
    GRADE_VIEW_ALL("grade:view:all", "View all grades"),

    // Course Management
    COURSE_VIEW("course:view", "View courses"),
    COURSE_CREATE("course:create", "Create courses"),
    COURSE_EDIT("course:edit", "Edit courses"),

    // User Management
    USER_CREATE("user:create", "Create user accounts"),
    USER_EDIT("user:edit", "Edit user accounts"),
    USER_DELETE("user:delete", "Delete user accounts"),

    // System
    SYSTEM_SETTINGS("system:settings", "Modify system settings"),
    SYSTEM_BACKUP("system:backup", "Backup/restore system");

    private final String permission;
    private final String description;

    Permission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() { return permission; }
    public String getDescription() { return description; }
}
```

### Phase 2: UI Implementation (Grey Out Pattern)

**Controller Helper Method**:
```java
/**
 * Check if current user has permission and configure UI element
 * If no permission, grey out and add tooltip
 */
protected void configurePermissionControl(Node control, Permission permission) {
    User currentUser = userService.getCurrentUser();
    boolean hasPermission = securityService.hasPermission(currentUser, permission);

    if (!hasPermission) {
        // Grey out the control
        control.setDisable(true);
        control.setOpacity(0.5);

        // Add tooltip explaining why
        String tooltipText = String.format(
            "Requires permission: %s\n" +
            "Current role: %s\n" +
            "Contact your administrator for access.",
            permission.getDescription(),
            currentUser.getRole()
        );

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(control, tooltip);

        // Add style class for CSS theming
        control.getStyleClass().add("permission-disabled");
    }
}
```

**Usage Example in StudentsController**:
```java
@FXML
public void initialize() {
    // Configure "New Enrollment" button based on permissions
    configurePermissionControl(btnNewEnrollment, Permission.STUDENT_ENROLL);

    // Configure "Add Grade" button
    configurePermissionControl(btnAddGrade, Permission.GRADE_ENTER);

    // Configure other controls...
    loadStudents();
}
```

**CSS Styling**:
```css
/* Permission-disabled controls */
.permission-disabled {
    -fx-opacity: 0.5;
    -fx-cursor: not-allowed;
}

.permission-disabled:hover {
    -fx-effect: dropshadow(gaussian, rgba(255, 193, 7, 0.6), 10, 0, 0, 0);
}

/* Tooltip for disabled permissions */
.tooltip.permission-tooltip {
    -fx-background-color: #263238;
    -fx-text-fill: #FFC107;
    -fx-font-size: 12px;
    -fx-padding: 8px;
}
```

### Phase 3: Service Layer (Permission Checks)

**SecurityService**:
```java
@Service
public class SecurityService {

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(User user, Permission permission) {
        if (user == null) return false;

        // Get user's roles
        Set<Role> roles = user.getRoles();

        // Check if any role has this permission
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.equals(permission));
    }

    /**
     * Enforce permission check (throws exception if no permission)
     */
    @Transactional
    public void requirePermission(Permission permission) {
        User currentUser = userService.getCurrentUser();

        if (!hasPermission(currentUser, permission)) {
            throw new AccessDeniedException(
                "User does not have permission: " + permission.getDescription()
            );
        }
    }

    /**
     * Check if user has permission for specific student
     * (Teachers can only access their own students)
     */
    public boolean hasStudentAccess(User user, Long studentId) {
        if (hasPermission(user, Permission.STUDENT_VIEW_ALL)) {
            return true; // Admin can view all
        }

        if (hasPermission(user, Permission.STUDENT_VIEW)) {
            // Check if teacher has this student in any course
            return teacherStudentService.isStudentInTeacherCourses(user.getId(), studentId);
        }

        return false;
    }
}
```

**Service Method Example**:
```java
@Service
public class StudentService {

    @Autowired
    private SecurityService securityService;

    /**
     * Enroll new student
     * Requires STUDENT_ENROLL permission
     */
    public Student enrollStudent(Student student) {
        // Check permission
        securityService.requirePermission(Permission.STUDENT_ENROLL);

        // Proceed with enrollment
        log.info("Enrolling student: {} {}", student.getFirstName(), student.getLastName());
        return studentRepository.save(student);
    }

    /**
     * View student
     * Requires STUDENT_VIEW permission and access to specific student
     */
    public Student getStudent(Long studentId) {
        User currentUser = userService.getCurrentUser();

        if (!securityService.hasStudentAccess(currentUser, studentId)) {
            throw new AccessDeniedException("No access to student ID: " + studentId);
        }

        return studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }
}
```

### Phase 4: Audit Logging

**Track all permission-based actions**:
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "role")
    private String role;

    @Column(name = "action")
    private String action;  // STUDENT_ENROLL, GRADE_ENTER, etc.

    @Column(name = "resource_type")
    private String resourceType;  // Student, Grade, Course, etc.

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
```

---

## Role Permission Matrix (FERPA Compliant)

| Feature | Registrar | Teacher | Counselor | Admin | IT Admin |
|---------|-----------|---------|-----------|-------|----------|
| **Student Enrollment** | ✅ | ❌ | ❌ | 👁️ View | ❌ |
| **View Students (All)** | 👁️ | ❌ Own Only | ✅ | ✅ | ❌ |
| **Edit Demographics** | ✅ | ❌ | ⚠️ Limited | 👁️ | ❌ |
| **Enter Grades** | ❌ | ✅ Own Courses | ❌ | 👁️ | ❌ |
| **View Grades (All)** | ❌ | ❌ Own Courses | ✅ | ✅ | ❌ |
| **Create Assignments** | ❌ | ✅ Own Courses | ❌ | ❌ | ❌ |
| **Course Recommendations** | ❌ | ✅ | ✅ | 👁️ | ❌ |
| **Accommodations (IEP/504)** | 👁️ | 👁️ Own Students | ✅ | 👁️ | ❌ |
| **Medical Records** | 👁️ | 👁️ Own Students | 👁️ | 👁️ | ❌ |
| **Discipline Records** | 👁️ | 👁️ Own Students | ✅ | ✅ | ❌ |
| **Financial Information** | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Counseling Notes** | ❌ | ❌ | ✅ | 👁️ | ❌ |
| **Create Courses** | ❌ | ❌ | ❌ | ⚠️ | ❌ |
| **User Account Creation** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Password Reset** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Email Configuration** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **System Settings** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Database Backup** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Reports (Academic)** | ⚠️ Limited | ⚠️ Own Classes | ✅ | ✅ | ❌ |
| **Reports (System/Technical)** | ❌ | ❌ | ❌ | ⚠️ | ✅ |
| **Audit Logs (View)** | ❌ | ❌ | ❌ | ✅ | ✅ |

**Legend**:
- ✅ Full Access
- 👁️ View Only (Read-only)
- ⚠️ Limited Access
- ❌ No Access (Greyed Out) - FERPA Compliant

**Key Changes for IT Admin (FERPA Compliance)**:
- ❌ **No access to student educational records** (grades, IEP, 504, medical, discipline, financial)
- ✅ **Technical operations only** (accounts, passwords, email, system settings, backups)
- ✅ **System-level reports** (performance, usage, errors) but NOT student data reports
- ✅ **Audit log access** (to monitor security and compliance)

---

## Implementation Timeline (Future)

### When to Implement

**Triggers for RBAC Implementation**:
1. Multiple user types actively using system
2. Security requirements mandate access control
3. Compliance requirements (FERPA, etc.)
4. Multi-school/district deployment
5. External audit requirements

**Estimated Effort**: 2-3 weeks
- Week 1: Database schema, Role/Permission entities
- Week 2: Service layer security, permission checks
- Week 3: UI implementation, grey-out controls, testing

**Prerequisites**:
- ✅ Core functionality complete
- ✅ User authentication working
- ✅ All major features implemented
- ✅ UI stabilized

---

## Example: Registrar View

### Student Management Module (Registrar Role)

**Enabled Features** (Normal Colors):
- ✅ 📝 New Enrollment
- ✅ 📋 View Student List
- ✅ ✏️ Edit Demographics
- ✅ 🔄 Transfer Student
- ✅ 🚪 Withdraw Student

**Disabled Features** (Greyed Out with Tooltips):
- ❌ 📝 Add Grade (Tooltip: "Requires permission: Enter Grades. Contact your administrator.")
- ❌ 📊 View Grades (Tooltip: "Grade access limited to Teachers and Administrators")
- ❌ 📥 Import Grades (Tooltip: "Requires Teacher or Admin role")
- ❌ 💡 Recommend Courses (Tooltip: "Course recommendations restricted to Counselors and Teachers")

---

## Current Implementation Status

### What's Already in Place
1. ✅ User entity with `role` field
2. ✅ Basic authentication (login/logout)
3. ✅ User service with getCurrentUser()
4. ✅ Navigation visibility based on roles (MainController)

### What's Missing (To Be Implemented)
1. ❌ Role entity and Permission entity
2. ❌ SecurityService for permission checks
3. ❌ UI "grey out" helper methods
4. ❌ Service-layer permission enforcement
5. ❌ Audit logging
6. ❌ Role-based data filtering (teachers see only their students)

---

## Migration Strategy (When Implementing)

### Step 1: Add New Tables (No Breaking Changes)
- Create `role`, `permission`, `role_permission`, `user_role` tables
- Keep existing `user.role` field (backward compatible)

### Step 2: Seed Default Roles
```sql
INSERT INTO role (name, display_name, description) VALUES
('REGISTRAR', 'Registrar', 'Student enrollment and registration'),
('TEACHER', 'Teacher', 'Classroom instruction and grading'),
('COUNSELOR', 'Counselor', 'Academic counseling and planning'),
('ADMINISTRATOR', 'Administrator', 'School administration'),
('IT_ADMIN', 'IT Administrator', 'System administration');
```

### Step 3: Migrate Existing Users
```java
// Migration script
userRepository.findAll().forEach(user -> {
    String roleName = user.getRole();
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

    user.getRoles().add(role);
    userRepository.save(user);
});
```

### Step 4: Update Controllers (Gradual)
- Add permission checks to new features first
- Gradually add to existing features
- Test thoroughly after each module

---

## Notes

- **Current Status**: RBAC is planned but not yet implemented
- **Priority**: Low (focus on core functionality first)
- **Approach**: "Grey out" disabled features instead of hiding
- **Flexibility**: Role definitions will be finalized later based on actual usage patterns
- **Backward Compatibility**: Current user.role field will be maintained

---

## References

### Industry Standards
- **FERPA Compliance**: Family Educational Rights and Privacy Act
- **NIST RBAC Model**: National Institute of Standards and Technology
- **PowerSchool Roles**: School Administrator, Teacher, Counselor, Registrar
- **Infinite Campus Roles**: District Admin, Building Admin, Teacher, Clerk

### Best Practices
1. Principle of Least Privilege (grant minimum necessary access)
2. Separation of Duties (no single user should control entire workflow)
3. Audit Everything (log all permission-based actions)
4. Default Deny (explicitly grant permissions, don't assume)
5. Visual Feedback (always show why feature is disabled)

---

**Document Created**: 2025-12-24
**Status**: 📋 **PLANNED FOR FUTURE IMPLEMENTATION**
**User Decision**: Implement RBAC when role requirements are finalized
**Approach**: Grey-out disabled features with explanatory tooltips

---

## Legal Research Sources

This document's legal and compliance recommendations are based on research from authoritative sources:

### FERPA Compliance & Best Practices

1. [FERPA Compliance Guide (Updated 2025) | UpGuard](https://www.upguard.com/blog/ferpa-compliance-guide)
2. [Navigating data privacy in education records with FERPA | Scrut](https://www.scrut.io/post/education-records-ferpa)
3. [FERPA Compliance: What is, Requirements & Best Practices | Kiteworks](https://www.kiteworks.com/regulatory-compliance/ferpa-compliance/)
4. [Family Educational Rights and Privacy Act (FERPA) Compliance: A Complete Guide | BigID](https://bigid.com/blog/ferpa-compliance/)
5. [FERPA Compliance Checklist (June - 2025) | BrightDefense](https://www.brightdefense.com/blog/ferpa-compliance-checklist/)
6. [Understanding FERPA: Responsibilities for School Faculty and Staff | GovFacts](https://govfacts.org/government/federal/agencies/ed/understanding-ferpa-responsibilities-for-school-faculty-and-staff/)

### FERPA vs HIPAA for IEP/504 Plans

7. [FERPA, HIPAA and Student Privacy in 2025 | A Day in Our Shoes](https://adayinourshoes.com/ferpa-vs-hipaa-only-one-applies-to-ieps/)
8. [Does HIPAA apply to Individualized Education Programs (IEPs) or 504 Plans? | Paubox](https://www.paubox.com/blog/does-hipaa-apply-to-individualized-education-programs-ieps-or-504-plans)
9. [Joint Guidance on the Application of FERPA and HIPAA to Student Health Records | Protecting Student Privacy](https://studentprivacy.ed.gov/resources/joint-guidance-application-ferpa-and-hipaa-student-health-records)

### Official Government Resources

10. [Frequently Asked Questions | Protecting Student Privacy (U.S. Department of Education)](https://studentprivacy.ed.gov/frequently-asked-questions)
11. [Guidance | Protecting Student Privacy (U.S. Department of Education)](https://studentprivacy.ed.gov/guidance)
12. [FERPA | Protecting Student Privacy (U.S. Department of Education)](https://studentprivacy.ed.gov/ferpa)

### Additional References

13. [Family Educational Rights and Privacy Act | Wikipedia](https://en.wikipedia.org/wiki/Family_Educational_Rights_and_Privacy_Act)
14. [Electronic Student Records for Special Education - Laws, Regulations, & Policies | CA Dept of Education](https://www.cde.ca.gov/sp/se/lr/om111020.asp)
15. [FERPA Compliance: Regulations and Requirements Checklist | imageone](https://www.imageoneway.com/blog/ferpa-compliance)

**Key Takeaway**: IT administrators must follow the **principle of least privilege** under FERPA, with access limited strictly to technical operations (user accounts, passwords, email, system settings) and **no access to student educational records** (grades, IEP/504, medical, discipline, financial data) unless specifically authorized for a documented emergency with full audit trail.
