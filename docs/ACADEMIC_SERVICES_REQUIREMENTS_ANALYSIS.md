# Academic Services - Requirements Analysis

**Date**: 2025-12-24
**Status**: 📋 **PLANNING PHASE**
**Module**: Academic Services
**Scope**: Full K-12 Academic Operations

---

## 📋 Executive Summary

This document analyzes the Academic Services requirements for Heronix-SIS. The requirements cover 8 major subsystems with 150+ distinct features spanning course management, scheduling, transcripts, grading, reporting, GPA calculation, standardized testing, and graduation tracking.

---

## 🎯 Module Overview

### 8 Primary Subsystems

1. **Course Management** (20 features)
2. **Course Section Management** (11 features)
3. **Student Course Scheduling** (12 features)
4. **Transcript Management** (20 features)
5. **Grading & Assessment** (24 features)
6. **Report Cards & Progress Reports** (13 features)
7. **GPA & Class Rank** (15 features)
8. **Standardized Testing** (10 features)
9. **Graduation Requirements** (16 features)

**Total Features**: 141 distinct requirements

---

## 📊 Complexity Analysis

### Priority Classification

#### 🔴 Critical (Must Have - Phase 1)
**Course Management Core**:
- Course catalog creation
- Course code assignment
- Course name/description
- Subject area classification
- Department assignment
- Credit value
- Grade level restrictions
- Course prerequisites
- Course type (core, elective, AP, honors)
- Course status (active, inactive)

**Course Section Management Core**:
- Section creation (course + teacher + period + room)
- Section capacity management
- Section enrollment tracking
- Section status (open, closed, full)
- Section meeting times/days
- Section location

**Grading Core**:
- Assignment creation
- Assignment categories
- Grade entry
- Grade calculations
- Grading scales (A-F, percentage)
- Grade comments

**GPA Core**:
- GPA calculation (weighted/unweighted)
- Cumulative GPA
- GPA scale configuration

**Report Cards Core**:
- Report card generation
- Report card templates
- Grade-level specific templates

**Graduation Requirements Core**:
- Credit requirements by subject
- Minimum credit total
- Required courses
- Graduation audit

#### 🟡 Important (Should Have - Phase 2)
**Advanced Course Features**:
- Co-requisites
- Course capacity limits
- Course standards alignment
- Course objectives/outcomes
- Course fees

**Advanced Section Features**:
- Multiple teachers (co-teaching)
- Section start/end dates
- Section grading periods

**Student Scheduling**:
- Course request submission
- Course selection by student/parent
- Prerequisite validation
- Course conflict detection
- Schedule change requests
- Add/drop processing

**Advanced Grading**:
- Category weighting
- Extra credit handling
- Late work penalties
- Missing assignment tracking
- Incomplete grade management
- Rubric creation/scoring

**Transcript Management**:
- Official transcript generation
- Unofficial transcript access
- Cumulative transcript
- Credit accumulation tracking
- Transfer credit evaluation

**Class Rank**:
- Class rank calculation
- Class rank percentile
- Valedictorian/salutatorian determination

**Standardized Testing**:
- Test registration
- Test scores import
- Test scores reporting
- Test score history

#### 🟢 Nice to Have (Phase 3)
**Advanced Features**:
- AI-powered schedule generation
- Standards-based grading
- Competency-based grading
- Mastery tracking
- Advanced rubrics
- IB/AP specialized tracking
- Dual enrollment integration
- Course syllabus storage
- Transcript transmission to colleges
- Testing accommodations tracking
- Graduation pathway options
- Early graduation eligibility

---

## 🏗️ Database Schema Requirements

### Core Entities Needed

#### 1. Course Entity
```java
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Information
    private String courseCode;
    private String courseName;
    private String description;
    private String subjectArea;
    private String department;
    private Double creditValue; // Carnegie units

    // Classification
    @Enumerated(EnumType.STRING)
    private CourseType courseType; // CORE, ELECTIVE, AP, HONORS, IB, CTE, DUAL_ENROLLMENT

    @Enumerated(EnumType.STRING)
    private CourseStatus status; // ACTIVE, INACTIVE, ARCHIVED

    // Restrictions & Requirements
    private String gradeLevelRestrictions; // e.g., "9,10,11,12"

    @ManyToMany
    @JoinTable(name = "course_prerequisites")
    private Set<Course> prerequisites;

    @ManyToMany
    @JoinTable(name = "course_corequisites")
    private Set<Course> corequisites;

    private Integer capacityLimit;

    // Academic Standards
    private String standardsAlignment;
    private String learningObjectives;

    // Financial
    private Double courseFee;

    // Resources
    private String syllabusUrl;
    private String instructionalMaterials;

    // Audit fields
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Boolean deleted = false;
}

public enum CourseType {
    CORE("Core Course"),
    ELECTIVE("Elective"),
    AP("Advanced Placement"),
    HONORS("Honors"),
    IB("International Baccalaureate"),
    CTE("Career & Technical Education"),
    DUAL_ENROLLMENT("Dual Enrollment");
}

public enum CourseStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ARCHIVED("Archived");
}
```

#### 2. CourseSection Entity
```java
@Entity
@Table(name = "course_section")
public class CourseSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Section Identification
    private String sectionNumber; // e.g., "01", "02A"
    private String sectionName; // e.g., "Algebra 1 - Period 2"

    // Course Reference
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // Instructor(s)
    @ManyToOne
    @JoinColumn(name = "primary_teacher_id")
    private Teacher primaryTeacher;

    @ManyToMany
    @JoinTable(name = "section_co_teachers")
    private Set<Teacher> coTeachers;

    // Schedule
    private String period; // e.g., "1", "2A"
    private String meetingDays; // e.g., "MTWRF", "MWF"
    private LocalTime startTime;
    private LocalTime endTime;

    // Location
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private String building;

    // Dates
    private LocalDate sectionStartDate;
    private LocalDate sectionEndDate;

    // Capacity
    private Integer maxCapacity;
    private Integer currentEnrollment = 0;

    // Status
    @Enumerated(EnumType.STRING)
    private SectionStatus status; // OPEN, CLOSED, FULL, CANCELLED

    // Academic Year
    private String academicYear; // e.g., "2024-2025"
    private String term; // e.g., "Fall", "Spring", "Full Year"

    // Grading
    @OneToMany(mappedBy = "section")
    private Set<GradingPeriod> gradingPeriods;

    // Enrollments
    @OneToMany(mappedBy = "section")
    private Set<CourseEnrollment> enrollments;
}

public enum SectionStatus {
    OPEN("Open for Enrollment"),
    CLOSED("Closed"),
    FULL("Full - No Seats Available"),
    CANCELLED("Cancelled");
}
```

#### 3. CourseEnrollment Entity
```java
@Entity
@Table(name = "course_enrollment")
public class CourseEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private CourseSection section;

    // Enrollment Details
    private LocalDate enrollmentDate;
    private LocalDate dropDate;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status; // ACTIVE, DROPPED, WITHDRAWN, COMPLETED

    @Enumerated(EnumType.STRING)
    private EnrollmentType enrollmentType; // REGULAR, LATE_ADD, TRANSFER

    // Grades
    @OneToMany(mappedBy = "enrollment")
    private Set<StudentGrade> grades;

    // Final Grade
    private String finalGrade; // e.g., "A", "B+", "95"
    private Double finalPercentage;
    private Double creditsEarned;
    private Boolean creditReceived = false;

    // Special Status
    private Boolean isIncomplete = false;
    private Boolean isAudit = false;

    // Approval
    private String approvedBy;
    private LocalDateTime approvalDate;
}

public enum EnrollmentStatus {
    ACTIVE("Active"),
    DROPPED("Dropped"),
    WITHDRAWN("Withdrawn"),
    COMPLETED("Completed");
}
```

#### 4. Assignment Entity
```java
@Entity
@Table(name = "assignment")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    private String assignmentName;
    private String description;

    // Course Section Reference
    @ManyToOne
    @JoinColumn(name = "section_id")
    private CourseSection section;

    // Category
    @Enumerated(EnumType.STRING)
    private AssignmentCategory category; // TEST, QUIZ, HOMEWORK, PROJECT, PARTICIPATION

    // Grading
    private Double maxPoints;
    private Boolean allowExtraCredit = false;
    private Double extraCreditMaxPoints;

    // Dates
    private LocalDate assignedDate;
    private LocalDate dueDate;
    private Boolean acceptLateWork = true;
    private Double lateWorkPenaltyPercent;

    // Rubric
    private Boolean hasRubric = false;
    private String rubricUrl;

    // Standards
    private String standardsAligned;

    // Status
    private Boolean isPublished = false;
    private Boolean isDeleted = false;
}

public enum AssignmentCategory {
    TEST("Test"),
    QUIZ("Quiz"),
    HOMEWORK("Homework"),
    PROJECT("Project"),
    PARTICIPATION("Class Participation"),
    LAB("Lab Work"),
    ESSAY("Essay/Writing"),
    PRESENTATION("Presentation"),
    EXAM("Exam");
}
```

#### 5. Transcript Entity
```java
@Entity
@Table(name = "transcript")
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student Reference
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Academic Year
    private String academicYear;

    // GPA
    private Double cumulativeGPA;
    private Double cumulativeWeightedGPA;
    private Double yearGPA;
    private Double yearWeightedGPA;

    // Credits
    private Double totalCreditsAttempted;
    private Double totalCreditsEarned;
    private Double yearCreditsAttempted;
    private Double yearCreditsEarned;

    // Class Rank
    private Integer classRank;
    private Integer classSize;
    private Double classRankPercentile;

    // Honors
    private String honorRollStatus; // e.g., "High Honor Roll"
    private String academicHonors; // e.g., "National Honor Society"

    // Course Entries
    @OneToMany(mappedBy = "transcript")
    private List<TranscriptEntry> courses;

    // Generation
    private LocalDateTime generatedDate;
    private Boolean isOfficial = false;
    private String generatedBy;
    private String seal; // Digital signature/seal
}
```

#### 6. GraduationRequirement Entity
```java
@Entity
@Table(name = "graduation_requirement")
public class GraduationRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Requirement Details
    private String requirementName;
    private String description;

    // Requirement Type
    @Enumerated(EnumType.STRING)
    private RequirementType type; // CREDIT, COURSE, EXAM, SERVICE_HOURS, PROJECT

    // Subject Area (for credit requirements)
    private String subjectArea; // e.g., "English", "Math", "Science"
    private Double creditsRequired;

    // Specific Course (for course requirements)
    @ManyToOne
    @JoinColumn(name = "required_course_id")
    private Course requiredCourse;

    // Exam (for exam requirements)
    private String requiredExam; // e.g., "State Exit Exam"
    private Integer passingScore;

    // Service Hours
    private Integer serviceHoursRequired;

    // Applicable To
    private String graduationYear; // e.g., "2025" or "All"
    private String diplomaType; // e.g., "Standard", "Advanced", "Honors"

    // Status
    private Boolean isActive = true;
}

public enum RequirementType {
    CREDIT("Credit Requirement"),
    COURSE("Specific Course Required"),
    EXAM("State/Exit Exam"),
    SERVICE_HOURS("Community Service Hours"),
    PROJECT("Senior Project/Capstone");
}
```

#### 7. StandardizedTest Entity
```java
@Entity
@Table(name = "standardized_test")
public class StandardizedTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Test Information
    private String testName; // e.g., "SAT", "ACT", "PSAT"
    private String testType; // e.g., "STATE", "NATIONAL", "AP", "IB"

    // Administration
    private LocalDate testDate;
    private String testAdministration; // e.g., "Spring 2025"

    // Student Reference
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Scores
    private Integer compositeScore;
    private String scoreBreakdown; // JSON format for subject scores

    // Accommodations
    private Boolean hadAccommodations = false;
    private String accommodations;

    // Status
    private Boolean scoresReceived = false;
    private LocalDate scoresReceivedDate;

    // Registration
    private Boolean registeredBySchool = false;
    private LocalDate registrationDate;
}
```

---

## 🔄 Integration Points

### Existing Systems to Integrate With

1. **Student Module** ✅ (Already implemented)
   - Student demographics
   - Student status
   - Student enrollment records

2. **Teacher Module** (Exists but may need enhancement)
   - Teacher assignments
   - Teacher schedules
   - Co-teaching relationships

3. **Room/Facilities Module** (May need creation)
   - Room capacity
   - Room availability
   - Building locations

4. **Calendar Module** (May need creation)
   - Academic year
   - Grading periods
   - Term dates
   - Testing calendar

5. **Parent Portal** (Future integration)
   - Course selection by parents
   - Grade viewing
   - Report card access
   - Transcript requests

---

## 📈 Implementation Complexity Estimate

### Phase 1: Core Course & Section Management (2-3 weeks)
**Entities**: Course, CourseSection, CourseEnrollment
**Features**:
- Course catalog (CRUD)
- Section creation
- Student enrollment
- Basic capacity management

**Estimated Files**: ~15 files
- 3 entities
- 3 repositories
- 3 services
- 3 controllers (REST API)
- 3 FXML forms

### Phase 2: Grading & Assessment (2-3 weeks)
**Entities**: Assignment, StudentGrade, GradingPeriod
**Features**:
- Assignment creation
- Grade entry (individual and batch)
- Grade calculations
- Grading scales

**Estimated Files**: ~15 files
- 3 entities
- 3 repositories
- 3 services
- 3 controllers
- 3 FXML forms

### Phase 3: GPA & Report Cards (2 weeks)
**Entities**: ReportCard, GradingScale
**Features**:
- GPA calculation (weighted/unweighted)
- Report card generation
- Report card templates
- Report card distribution

**Estimated Files**: ~12 files
- 2 entities
- 2 repositories
- 3 services (including calculation service)
- 2 controllers
- 3 FXML forms + PDF templates

### Phase 4: Transcripts & Graduation (2 weeks)
**Entities**: Transcript, TranscriptEntry, GraduationRequirement, GraduationAudit
**Features**:
- Transcript generation
- Credit tracking
- Graduation audit
- Requirement checking

**Estimated Files**: ~15 files
- 4 entities
- 4 repositories
- 4 services
- 1 controller
- 2 FXML forms + PDF templates

### Phase 5: Scheduling (3-4 weeks)
**Entities**: ScheduleRequest, ScheduleConflict, ScheduleGeneration
**Features**:
- Course requests
- Conflict detection
- Schedule generation (manual)
- Schedule changes/add-drop

**Estimated Files**: ~20 files
- 3 entities
- 3 repositories
- 5 services (complex scheduling logic)
- 3 controllers
- 6 FXML forms (scheduling UI is complex)

### Phase 6: Standardized Testing (1-2 weeks)
**Entities**: StandardizedTest, TestAccommodation
**Features**:
- Test registration
- Score import
- Score reporting
- Testing calendar

**Estimated Files**: ~10 files
- 2 entities
- 2 repositories
- 2 services
- 2 controllers
- 2 FXML forms

**Total Estimated Effort**: 12-16 weeks (3-4 months)
**Total Estimated Files**: ~87 files

---

## ⚠️ Critical Dependencies

### Must Be in Place Before Starting

1. ✅ **Student Module** - Complete (Session 8)
2. ✅ **User/Teacher Authentication** - Complete
3. ❓ **Academic Calendar** - May need creation
4. ❓ **Room/Facility Management** - May need creation
5. ❓ **Grading Period Configuration** - Needs creation

---

## 🎯 Recommended Implementation Order

### Priority Order (Based on Dependencies)

1. **Phase 1: Course & Section Management** (Foundation)
   - Everything depends on courses existing
   - Must come first

2. **Phase 2: Grading & Assessment** (Core Academic Function)
   - Teachers need this immediately
   - Critical for day-to-day operations

3. **Phase 3: GPA & Report Cards** (Student/Parent Need)
   - Parents expect regular report cards
   - GPA needed for transcripts

4. **Phase 4: Transcripts & Graduation** (Compliance)
   - Required for college applications
   - State/district reporting requirements

5. **Phase 5: Scheduling** (Complex but Less Urgent)
   - Can be done manually in interim
   - Most complex subsystem

6. **Phase 6: Standardized Testing** (Seasonal)
   - Only needed during testing windows
   - Can be last priority

---

## 📋 Questions to Clarify Before Implementation

### Course Management
1. How many courses in typical catalog? (Affects performance design)
2. Are course codes standardized across district? (Import/export needs)
3. How often do courses change? (Versioning requirements)

### Grading
4. What grading scales are used? (A-F, percentage, standards-based?)
5. How many grading periods per year? (4 quarters, 2 semesters, 3 trimesters?)
6. Are weighted GPA scales consistent across all AP/Honors courses?

### Scheduling
7. Is scheduling done manually or need AI/algorithm? (Complexity level)
8. How many periods per day? (Fixed or rotating schedule?)
9. Block scheduling or traditional? (Data model impact)

### Transcripts
10. Official transcript format requirements? (PDF template design)
11. Electronic transcript transmission needed? (Integration requirements)
12. State-specific transcript requirements? (Compliance)

### Graduation
13. State-specific graduation requirements? (Requirement configuration)
14. Multiple diploma types offered? (Standard, Advanced, Honors, IB)
15. Early graduation allowed? (Business logic)

---

## 💡 Recommendations

### 1. Start with Minimal Viable Product (MVP)

**Phase 1 MVP** (4-6 weeks):
- Basic course catalog
- Section creation with teacher assignment
- Student enrollment in sections
- Simple grade entry
- Basic GPA calculation
- Simple report card generation

**Why**: Get something working quickly, gather feedback, iterate

### 2. Use Existing Patterns from Student Module

The Student Management module is complete and working well. Reuse:
- Entity-Repository-Service-Controller pattern
- FXML form structure
- Dark theme CSS
- Audit logging pattern
- Soft delete pattern

### 3. Plan for Scale

Consider:
- Typical high school: 1000-2000 students
- Typical course catalog: 200-400 courses
- Sections per semester: 300-600
- Enrollments per semester: 10,000-20,000

**Performance implications**:
- Indexed database queries
- Lazy loading for associations
- Batch operations for grade entry
- Caching for GPA calculations

### 4. Legal/Compliance Considerations

**FERPA applies to academic records**:
- Grades are educational records
- Transcripts are highly protected
- Access must be logged (audit trail)
- Parents can request grade access
- Students over 18 control own records

**Action**: Apply same FERPA principles from RBAC plan

---

## 📚 Related Documents

1. **RBAC_FUTURE_IMPLEMENTATION_PLAN.md** - Role-based access for academic data
2. **IMPLEMENTATION_ROADMAP_RBAC_AND_COMPLIANCE.md** - FERPA compliance requirements
3. **FINAL_SESSION_8_COMPLETE_REPORT.md** - Student module completion (reusable patterns)
4. **ZERO_ERRORS_ACHIEVEMENT_REPORT.md** - Build success patterns

---

## ✅ Next Steps

### Before Starting Implementation

1. **Review and Approve** this requirements analysis
2. **Clarify Questions** (15 questions listed above)
3. **Determine MVP Scope** (which features for Phase 1?)
4. **Create Database Schema** (start with Course, CourseSection, CourseEnrollment)
5. **Design UI Mockups** (for critical screens: course catalog, grade entry)

### When Ready to Start

1. Begin with **Phase 1: Course & Section Management**
2. Create entities first (Course, CourseSection, CourseEnrollment)
3. Build repositories (with proper indexing)
4. Implement services (with FERPA logging)
5. Create REST API controllers
6. Build JavaFX forms (following existing dark theme pattern)
7. Test thoroughly (unit + integration)
8. Document everything

---

**Document Created**: 2025-12-24
**Status**: 📋 **READY FOR REVIEW**
**Total Requirements**: 141 features across 9 subsystems
**Estimated Effort**: 12-16 weeks
**Recommended Approach**: Phased implementation (6 phases)
**Priority**: Start with Course & Section Management (foundation for everything else)

---

**This analysis is complete and ready for stakeholder review. Proceed with clarifying the 15 questions above before beginning implementation.** 🚀
