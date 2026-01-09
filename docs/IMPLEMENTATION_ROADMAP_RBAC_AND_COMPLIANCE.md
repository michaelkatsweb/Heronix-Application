# Implementation Roadmap: RBAC & FERPA Compliance

**Created**: 2025-12-24
**Status**: 📋 **PLANNED FOR FUTURE**
**Priority**: Medium (After core functionality complete)
**Estimated Effort**: 2-3 weeks of development

---

## 📋 Executive Summary

This document serves as the master implementation log for Role-Based Access Control (RBAC) and FERPA compliance features in Heronix-SIS. All requirements, research, and technical specifications have been documented and are ready for implementation when the core system is stable.

---

## 🎯 Implementation Goals

### Primary Objectives

1. **Implement FERPA-Compliant RBAC**
   - Enforce principle of least privilege
   - Restrict IT administrator access to student educational records
   - Implement "grey-out" UI pattern for disabled features

2. **Ensure Legal Compliance**
   - Meet all FERPA requirements for student data protection
   - Implement comprehensive audit logging
   - Establish access control matrix

3. **Improve User Experience**
   - Clear visual feedback on permissions (grey-out vs hide)
   - Explanatory tooltips for disabled features
   - Role-appropriate dashboards and navigation

---

## 📚 Reference Documentation

All technical details, research, and specifications are documented in:

### Primary Documents

1. **RBAC_FUTURE_IMPLEMENTATION_PLAN.md**
   - Location: `H:\Heronix\Heronix-SIS\docs\RBAC_FUTURE_IMPLEMENTATION_PLAN.md`
   - Content: Complete RBAC implementation plan with legal compliance requirements
   - Sections:
     - Role definitions (Registrar, Teacher, Counselor, Admin, IT Admin)
     - FERPA compliance requirements and legal research
     - Technical implementation plan (database schema, service layer, UI)
     - Permission matrix (FERPA compliant)
     - Best practices for IT administrator access
     - Compliance checklist
     - Implementation timeline

2. **ARCHITECTURE_ANALYSIS_ENROLLMENT_VS_STUDENT_MANAGEMENT.md**
   - Location: `H:\Heronix\Heronix-SIS\docs\ARCHITECTURE_ANALYSIS_ENROLLMENT_VS_STUDENT_MANAGEMENT.md`
   - Content: Industry research on proper separation of enrollment vs student management
   - Already Implemented: ✅ Architectural separation complete (Session 9)

3. **ARCHITECTURE_IMPLEMENTATION_ENROLLMENT_SEPARATION.md**
   - Location: `H:\Heronix\Heronix-SIS\docs\ARCHITECTURE_IMPLEMENTATION_ENROLLMENT_SEPARATION.md`
   - Content: Implementation details for enrollment/student management separation
   - Status: ✅ Complete (modal window navigation implemented)

4. **SESSION_9_NAVIGATION_FIX.md**
   - Location: `H:\Heronix\Heronix-SIS\docs\SESSION_9_NAVIGATION_FIX.md`
   - Content: Fix for enrollment navigation (modal window approach)
   - Status: ✅ Complete

---

## 🔐 Legal & Compliance Requirements Summary

### FERPA Compliance Key Points

**Legal Principle**: **Least Privilege & Need-to-Know Basis**

All access to student educational records must be:
1. ✅ Based on legitimate educational interest
2. ✅ Limited to specific records needed for job function
3. ✅ Logged with full audit trail
4. ✅ Subject to regular access reviews
5. ✅ Backed by annual FERPA training

### What FERPA Protects (Educational Records)

- 📊 Grades, transcripts, test scores, GPA
- 📚 Course schedules, class lists, assignments
- 🏥 Health records maintained by school (K-12)
- 🎯 IEP (Individualized Education Program) records
- 🎯 504 Plan records and case documentation
- 💰 Student financial information
- 📋 Discipline files and behavioral records
- 📧 Student email communications

**Critical Note**: HIPAA does NOT apply to schools. Health information in IEPs and 504 Plans is covered under FERPA, not HIPAA.

### IT Administrator Restrictions (FERPA Compliant)

**IT CAN Access** ✅:
- User account management (username, password reset, email)
- System settings (technical configuration only)
- Database backups/maintenance (without viewing record contents)
- System performance monitoring (anonymized data)
- Technical troubleshooting (with authorization and audit trail)

**IT CANNOT Access** ❌:
- ❌ Student grades or academic records
- ❌ IEP/504 plans and special education records
- ❌ Medical/health records
- ❌ Discipline records
- ❌ Financial information
- ❌ Counseling notes
- ❌ Student assessment data

**Emergency Exception**: Only with:
1. Explicit administrator authorization
2. Documented reason
3. Full audit trail
4. Time-limited access
5. Immediate revocation after issue resolved

### FERPA Violations & Penalties

**Consequences**:
- Loss of federal funding for institution
- Personal liability for staff members
- Civil lawsuits from affected families
- Criminal charges in cases of willful disclosure

### Legal Research Sources

See RBAC_FUTURE_IMPLEMENTATION_PLAN.md, Section "Legal Research Sources" for 15 authoritative sources including:
- U.S. Department of Education official FERPA resources
- FERPA compliance guides (2025)
- FERPA vs HIPAA clarifications for IEP/504 plans
- California Department of Education special education records guidelines

---

## 👥 Role Definitions

### 5 Primary Roles

#### 1. Registrar
- **Primary Function**: Student enrollment and registration
- **Key Access**: Student enrollment, demographics, transfers, withdrawals
- **Restrictions**: No access to grades, assignments, system settings

#### 2. Teacher
- **Primary Function**: Classroom instruction and grading
- **Key Access**: Grades (own courses), assignments, attendance (own classes)
- **Restrictions**: No access to enrollment, account creation, all students (only assigned)

#### 3. Counselor
- **Primary Function**: Academic counseling and planning
- **Key Access**: All students, academic planning, IEP/504, course recommendations
- **Restrictions**: No direct grade entry, no enrollment, no system settings

#### 4. Administrator/Principal
- **Primary Function**: School administration and oversight
- **Key Access**: View all records, reports, analytics, monitoring
- **Restrictions**: No direct grade entry, delegates technical tasks to IT

#### 5. IT Administrator (FERPA Restricted)
- **Primary Function**: System administration and technical support
- **Key Access**: User accounts, passwords, email, system settings, backups
- **Restrictions**: ❌ **NO ACCESS to any student educational records**

**See RBAC_FUTURE_IMPLEMENTATION_PLAN.md for complete role permission matrix**

---

## 🛠️ Technical Implementation Phases

### Phase 1: Database Schema (Week 1, Days 1-2)

**Tasks**:
- [ ] Create `role` table
- [ ] Create `permission` table
- [ ] Create `role_permission` junction table
- [ ] Create `user_role` junction table (many-to-many)
- [ ] Create `audit_log` table for access tracking
- [ ] Seed default roles (Registrar, Teacher, Counselor, Admin, IT Admin)
- [ ] Seed default permissions (see Permission enum in plan doc)
- [ ] Create migration script for existing users

**Files to Create/Modify**:
- `src/main/java/com/heronix/model/domain/Role.java` (new)
- `src/main/java/com/heronix/model/domain/Permission.java` (new)
- `src/main/java/com/heronix/model/domain/AuditLog.java` (new)
- `src/main/java/com/heronix/model/domain/User.java` (modify - add roles relationship)

**Database Schema** (see RBAC_FUTURE_IMPLEMENTATION_PLAN.md for SQL)

### Phase 2: Service Layer Security (Week 1, Days 3-5)

**Tasks**:
- [ ] Create `SecurityService` with permission checking methods
- [ ] Create `AuditLogService` for access logging
- [ ] Add `@RequiresPermission` annotation (custom)
- [ ] Implement permission checks in all service methods
- [ ] Add student-level access checks (teachers see only their students)
- [ ] Implement emergency access workflow

**Files to Create**:
- `src/main/java/com/heronix/service/SecurityService.java` (new)
- `src/main/java/com/heronix/service/AuditLogService.java` (new)
- `src/main/java/com/heronix/security/RequiresPermission.java` (new annotation)
- `src/main/java/com/heronix/security/AccessDeniedException.java` (new exception)

**Files to Modify**:
- All service classes (add permission checks)
- `StudentService.java`, `GradeService.java`, etc.

**Key Methods to Implement**:
```java
// SecurityService
boolean hasPermission(User user, Permission permission)
void requirePermission(Permission permission)
boolean hasStudentAccess(User user, Long studentId)
void grantEmergencyAccess(Long userId, Permission permission, String reason, int durationMinutes)

// AuditLogService
void logAccess(User user, String action, String resourceType, Long resourceId)
void logAccessDenied(User user, String action, String resourceType, Long resourceId, String reason)
List<AuditLog> getAccessHistory(Long studentId)
```

### Phase 3: UI Implementation - Grey Out Pattern (Week 2, Days 1-3)

**Tasks**:
- [ ] Create `BaseController` with permission helper methods
- [ ] Implement `configurePermissionControl()` method
- [ ] Add CSS styles for disabled controls
- [ ] Update all controllers to extend `BaseController`
- [ ] Configure each button/control with permission requirements
- [ ] Add explanatory tooltips for disabled features
- [ ] Test grey-out appearance across all modules

**Files to Create**:
- `src/main/java/com/heronix/ui/controller/BaseController.java` (new)

**Files to Modify**:
- All controller classes (extend BaseController)
- `src/main/resources/css/dark-theme.css` (add permission styles)

**CSS Additions**:
```css
.permission-disabled {
    -fx-opacity: 0.5;
    -fx-cursor: not-allowed;
}

.permission-disabled:hover {
    -fx-effect: dropshadow(gaussian, rgba(255, 193, 7, 0.6), 10, 0, 0, 0);
}

.tooltip.permission-tooltip {
    -fx-background-color: #263238;
    -fx-text-fill: #FFC107;
    -fx-font-size: 12px;
    -fx-padding: 8px;
}
```

**Controller Method Example**:
```java
protected void configurePermissionControl(Node control, Permission permission) {
    User currentUser = userService.getCurrentUser();
    boolean hasPermission = securityService.hasPermission(currentUser, permission);

    if (!hasPermission) {
        control.setDisable(true);
        control.setOpacity(0.5);

        String tooltipText = String.format(
            "Requires permission: %s\nCurrent role: %s\nContact your administrator for access.",
            permission.getDescription(),
            currentUser.getRole()
        );

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(control, tooltip);
        control.getStyleClass().add("permission-disabled");
    }
}
```

### Phase 4: Repository & Query Enhancements (Week 2, Day 4)

**Tasks**:
- [ ] Create `RoleRepository`
- [ ] Create `PermissionRepository`
- [ ] Create `AuditLogRepository`
- [ ] Add teacher-student relationship queries
- [ ] Add role-based filtering to existing repositories

**Files to Create**:
- `src/main/java/com/heronix/repository/RoleRepository.java` (new)
- `src/main/java/com/heronix/repository/PermissionRepository.java` (new)
- `src/main/java/com/heronix/repository/AuditLogRepository.java` (new)

**Files to Modify**:
- `StudentRepository.java` (add teacher-student relationship queries)
- `CourseRepository.java` (add teacher-course queries)

### Phase 5: Testing & Validation (Week 2-3, Days 5-10)

**Tasks**:
- [ ] Unit tests for SecurityService
- [ ] Unit tests for permission checks in all services
- [ ] Integration tests for role-based access
- [ ] UI tests for grey-out functionality
- [ ] Test as each role (Registrar, Teacher, Counselor, Admin, IT Admin)
- [ ] Test emergency access workflow
- [ ] Test audit logging
- [ ] Verify FERPA compliance for IT admin restrictions

**Test Scenarios**:
1. IT Admin attempts to view grades → ❌ Denied + logged
2. Teacher views own students' grades → ✅ Allowed
3. Teacher attempts to view another teacher's students → ❌ Denied
4. Registrar enrolls student → ✅ Allowed + logged
5. Teacher attempts to enroll student → ❌ Denied (button greyed out)
6. Admin views all reports → ✅ Allowed
7. IT Admin views system reports (no student data) → ✅ Allowed
8. IT Admin attempts emergency access → Requires admin approval workflow

### Phase 6: Documentation & Training (Week 3)

**Tasks**:
- [ ] Create user manual with role-specific sections
- [ ] Create FERPA training module
- [ ] Document emergency access procedures
- [ ] Create workflow diagrams for each role
- [ ] Document audit log review process
- [ ] Create parent notification template (FERPA annual notice)
- [ ] Update system administrator guide
- [ ] Create access request form template

**Documents to Create**:
- User Manual (role-specific)
- FERPA Training Presentation
- Emergency Access Procedure Document
- Audit Log Review Guide
- Parent Annual Notification Template

---

## 📊 Role Permission Matrix (FERPA Compliant)

**Full matrix available in**: RBAC_FUTURE_IMPLEMENTATION_PLAN.md

### Quick Reference

| Feature | IT Admin Access |
|---------|-----------------|
| Student Enrollment | ❌ No Access |
| View Students | ❌ No Access |
| View/Enter Grades | ❌ No Access |
| IEP/504 Plans | ❌ No Access |
| Medical Records | ❌ No Access |
| Discipline Records | ❌ No Access |
| Financial Info | ❌ No Access |
| User Account Creation | ✅ Full Access |
| Password Reset | ✅ Full Access |
| Email Configuration | ✅ Full Access |
| System Settings | ✅ Full Access |
| Database Backup | ✅ Full Access |
| System Reports (Technical) | ✅ Full Access |
| Academic Reports | ❌ No Access |

**Legend**: ✅ = Allowed, ❌ = FERPA Restricted

---

## ✅ Compliance Checklist

When implementing RBAC, ensure all items are completed:

### Pre-Implementation
- [ ] Review FERPA_FUTURE_IMPLEMENTATION_PLAN.md in full
- [ ] Get stakeholder approval for role definitions
- [ ] Finalize permission matrix with school administrators
- [ ] Determine emergency access approval workflow
- [ ] Identify FERPA training provider or create in-house module

### During Implementation
- [ ] **Access Control Matrix**: Document which roles can access which data types
- [ ] **IT Role Definition**: Formally define IT Administrator role with restricted access
- [ ] **Audit Logging**: Implement comprehensive access logging system
- [ ] **Service Layer Security**: Add permission checks to all sensitive operations
- [ ] **UI Grey-Out**: Implement visual feedback for disabled features
- [ ] **Data Masking**: Implement field-level encryption for sensitive data

### Post-Implementation
- [ ] **Testing**: Complete all test scenarios for each role
- [ ] **Training Program**: Deploy FERPA training module for all users
- [ ] **Annual Review Process**: Establish quarterly access review procedures
- [ ] **Parent Notification**: Send annual FERPA rights notification
- [ ] **Emergency Access Protocol**: Document and test emergency access procedure
- [ ] **Access Request Form**: Create formal process for temporary access grants
- [ ] **Termination Procedures**: Document immediate access revocation checklist
- [ ] **Third-Party Agreements**: Add FERPA compliance clauses to vendor contracts
- [ ] **Incident Response**: Document procedures for unauthorized access
- [ ] **Audit Trail Review**: Schedule first audit log review

---

## 🚀 Implementation Triggers

Implement RBAC when ANY of these conditions are met:

1. **Multiple user types actively using system**
   - Currently: Primarily admin/IT testing
   - Trigger: Teachers, counselors, registrars actively using system

2. **Security requirements mandate access control**
   - External audit findings
   - Security assessment recommendations

3. **Compliance requirements (FERPA)**
   - Federal/state compliance audit
   - Legal review recommendation

4. **Multi-school/district deployment**
   - Expanding beyond single school
   - Multiple institutions sharing system

5. **External audit requirements**
   - Mandated security controls
   - Privacy impact assessment requirements

---

## 📁 File Structure for RBAC Implementation

### New Files to Create

```
src/main/java/com/heronix/
├── model/domain/
│   ├── Role.java (NEW)
│   ├── Permission.java (NEW)
│   └── AuditLog.java (NEW)
├── repository/
│   ├── RoleRepository.java (NEW)
│   ├── PermissionRepository.java (NEW)
│   └── AuditLogRepository.java (NEW)
├── service/
│   ├── SecurityService.java (NEW)
│   └── AuditLogService.java (NEW)
├── security/
│   ├── RequiresPermission.java (NEW - annotation)
│   └── AccessDeniedException.java (NEW - exception)
└── ui/controller/
    └── BaseController.java (NEW)

src/main/resources/
└── css/
    └── [Update dark-theme.css with permission styles]

docs/
├── RBAC_User_Manual.md (NEW)
├── FERPA_Training_Guide.md (NEW)
├── Emergency_Access_Procedures.md (NEW)
└── Parent_FERPA_Notification_Template.md (NEW)
```

### Files to Modify

```
src/main/java/com/heronix/
├── model/domain/
│   └── User.java (add roles relationship)
├── service/
│   ├── StudentService.java (add permission checks)
│   ├── GradeService.java (add permission checks)
│   ├── MedicalRecordService.java (add permission checks)
│   └── [All other services] (add permission checks)
└── ui/controller/
    ├── StudentsController.java (extend BaseController, add permission configs)
    ├── [All other controllers] (extend BaseController, add permission configs)
    └── MainController.java (add role-based navigation)
```

---

## 🎯 Success Criteria

Implementation is complete when:

1. ✅ All 5 roles properly defined in database
2. ✅ Permission matrix fully implemented
3. ✅ IT Admin has zero access to student educational records
4. ✅ All UI controls use grey-out pattern (not hide)
5. ✅ Comprehensive audit logging operational
6. ✅ All service methods have permission checks
7. ✅ Emergency access workflow functional
8. ✅ FERPA training module deployed
9. ✅ All test scenarios pass for all roles
10. ✅ Parent notification sent (annual FERPA notice)
11. ✅ Zero compilation errors
12. ✅ Full documentation complete

---

## 📝 Notes & Decisions

### Design Decisions

1. **Grey-Out vs Hide**
   - Decision: Grey-out disabled features
   - Rationale: Better UX, users know what exists
   - User Request: "we will just 'grey out' the systems and functions"

2. **IT Administrator Restrictions**
   - Decision: Zero access to student educational records
   - Rationale: FERPA compliance (least privilege principle)
   - User Concern: "IT should not be able to alter any grades IEP or 504 as well as sensitive private data"

3. **Emergency Access**
   - Decision: Break-glass access with full audit trail
   - Rationale: Balance security with operational needs
   - Requirements: Admin approval, time-limited, logged

4. **Role Finalization**
   - Decision: Defer final role definitions
   - Rationale: Need actual usage patterns
   - User Statement: "we will figure out the roles at a later time"

### Future Enhancements

After initial RBAC implementation, consider:

1. **Advanced Features**
   - Multi-role support (user has multiple roles)
   - Temporary permission grants
   - Delegation workflows
   - Role hierarchy (role inheritance)

2. **Compliance**
   - COPPA compliance (Children's Online Privacy Protection Act)
   - State-specific privacy laws
   - International compliance (GDPR for international students)

3. **Analytics**
   - Permission usage analytics
   - Access pattern monitoring
   - Anomaly detection for unusual access

---

## 📞 Support & Questions

### During Implementation

**Technical Questions**: Refer to RBAC_FUTURE_IMPLEMENTATION_PLAN.md for:
- Database schema details
- Code examples
- Service layer implementation patterns
- UI implementation examples

**Legal/Compliance Questions**: Refer to Legal Research Sources section:
- 15 authoritative sources documented
- U.S. Department of Education official resources
- FERPA compliance guides
- IEP/504 FERPA vs HIPAA clarifications

**Architecture Questions**: Refer to:
- ARCHITECTURE_ANALYSIS_ENROLLMENT_VS_STUDENT_MANAGEMENT.md
- ARCHITECTURE_IMPLEMENTATION_ENROLLMENT_SEPARATION.md

---

## 🔄 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2025-12-24 | 1.0 | Initial roadmap created |
| | | - Compiled from Session 9 discussions |
| | | - FERPA research incorporated |
| | | - IT administrator restrictions defined |
| | | - Complete implementation plan documented |

---

## 📚 Related Documents

1. **RBAC_FUTURE_IMPLEMENTATION_PLAN.md** - Master technical specification
2. **ARCHITECTURE_ANALYSIS_ENROLLMENT_VS_STUDENT_MANAGEMENT.md** - Industry research
3. **ARCHITECTURE_IMPLEMENTATION_ENROLLMENT_SEPARATION.md** - Enrollment separation details
4. **SESSION_9_NAVIGATION_FIX.md** - Modal window navigation fix
5. **SESSION_9_ARCHITECTURE_IMPLEMENTATION_SUMMARY.md** - Session 9 summary
6. **ZERO_ERRORS_ACHIEVEMENT_REPORT.md** - Session 8 error elimination
7. **FINAL_SESSION_8_COMPLETE_REPORT.md** - Session 8 completion report

---

## ✨ Current Implementation Status

### ✅ Completed (Not RBAC, but related)

- Zero compilation errors achieved (Session 8)
- Enrollment/Student Management architectural separation (Session 9)
- Modal window navigation for enrollment (Session 9)
- Student search functionality verified complete (Session 8)
- Dark theme standardization across all forms (Session 8)

### 📋 Planned (This Roadmap)

- RBAC implementation (5 roles)
- FERPA-compliant access controls
- IT administrator restrictions
- Grey-out UI pattern
- Audit logging system
- Emergency access workflow
- FERPA training module

---

**Document Created**: 2025-12-24
**Status**: 📋 **READY FOR FUTURE IMPLEMENTATION**
**Priority**: Medium (After core functionality stabilized)
**Estimated Effort**: 2-3 weeks
**Blocking Issues**: None (all research and planning complete)

---

**This roadmap is comprehensive and implementation-ready. All technical details, legal requirements, and code examples are documented in the referenced specification documents. Begin with Phase 1 (Database Schema) when ready to implement.** 🚀
