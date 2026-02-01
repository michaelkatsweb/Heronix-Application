# Heronix SIS Complete User Guide
## From Student Enrollment to Graduation

**Version:** 1.0
**Last Updated:** January 2026
**Platform:** Heronix Student Information System

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Initial Setup](#2-initial-setup)
3. [Managing Teachers](#3-managing-teachers)
4. [Setting Up Classrooms/Rooms](#4-setting-up-classroomsrooms)
5. [Creating Courses](#5-creating-courses)
6. [Student Enrollment](#6-student-enrollment)
7. [Building the Master Schedule](#7-building-the-master-schedule)
8. [Assigning Students to Courses](#8-assigning-students-to-courses)
9. [Daily Operations](#9-daily-operations)
10. [Grading and Academic Records](#10-grading-and-academic-records)
11. [Progress Monitoring](#11-progress-monitoring)
12. [Year-End Procedures](#12-year-end-procedures)
13. [Graduation Processing](#13-graduation-processing)
14. [Transcript Generation](#14-transcript-generation)
15. [Appendix: API Reference](#15-appendix-api-reference)

---

## 1. System Overview

### 1.1 What is Heronix SIS?

Heronix SIS is a comprehensive Student Information System designed for K-12 schools. It manages:

- Student enrollment and demographics
- Teacher assignments and credentials
- Course catalog and scheduling
- Classroom management
- Attendance tracking
- Gradebook and GPA calculations
- Graduation requirements
- Transcript generation
- District federation and data sync

### 1.2 Access Points

| Interface | URL | Purpose |
|-----------|-----|---------|
| **API Server** | `http://localhost:8080` | REST API endpoints |
| **Health Check** | `http://localhost:8080/actuator/health` | System status |
| **H2 Console** | `http://localhost:8080/h2-console` | Database admin |
| **API Discovery** | `http://localhost:8080/api/config/discovery` | Available endpoints |

### 1.3 User Roles

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access |
| **PRINCIPAL** | School-wide management |
| **REGISTRAR** | Student records, enrollment, transcripts |
| **COUNSELOR** | Student schedules, academic planning |
| **TEACHER** | Gradebook, attendance for assigned classes |
| **SECRETARY** | Basic data entry, reports |

---

## 2. Initial Setup

### 2.1 First-Time Configuration

Before adding any data, configure your school settings:

```
POST /api/config/school
```

```json
{
  "schoolName": "Lincoln High School",
  "schoolCode": "LHS001",
  "districtId": "DISTRICT-001",
  "address": {
    "street": "123 Education Lane",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62701"
  },
  "gradeLevels": ["9", "10", "11", "12"],
  "academicYear": "2025-2026",
  "semesterSystem": "SEMESTER",
  "gradingScale": {
    "A": {"min": 90, "max": 100, "gpaPoints": 4.0},
    "B": {"min": 80, "max": 89, "gpaPoints": 3.0},
    "C": {"min": 70, "max": 79, "gpaPoints": 2.0},
    "D": {"min": 60, "max": 69, "gpaPoints": 1.0},
    "F": {"min": 0, "max": 59, "gpaPoints": 0.0}
  }
}
```

### 2.2 Set Up Bell Schedule

Define your school's period structure:

```
POST /api/bell-schedules
```

```json
{
  "name": "Regular Day Schedule",
  "isDefault": true,
  "periods": [
    {"periodNumber": 1, "name": "Period 1", "startTime": "08:00", "endTime": "08:50"},
    {"periodNumber": 2, "name": "Period 2", "startTime": "08:55", "endTime": "09:45"},
    {"periodNumber": 3, "name": "Period 3", "startTime": "09:50", "endTime": "10:40"},
    {"periodNumber": 4, "name": "Period 4", "startTime": "10:45", "endTime": "11:35"},
    {"periodNumber": 5, "name": "Lunch", "startTime": "11:35", "endTime": "12:15", "isLunch": true},
    {"periodNumber": 6, "name": "Period 5", "startTime": "12:20", "endTime": "13:10"},
    {"periodNumber": 7, "name": "Period 6", "startTime": "13:15", "endTime": "14:05"},
    {"periodNumber": 8, "name": "Period 7", "startTime": "14:10", "endTime": "15:00"}
  ]
}
```

### 2.3 Configure Graduation Requirements

```
POST /api/graduation-requirements
```

```json
{
  "name": "Standard Diploma Requirements",
  "graduationYear": 2026,
  "totalCreditsRequired": 24.0,
  "requirements": [
    {"subject": "English", "creditsRequired": 4.0, "courses": ["ENG101", "ENG201", "ENG301", "ENG401"]},
    {"subject": "Mathematics", "creditsRequired": 4.0, "courses": ["MATH101", "MATH201", "MATH301"]},
    {"subject": "Science", "creditsRequired": 3.0, "courses": ["SCI101", "SCI201", "SCI301"]},
    {"subject": "Social Studies", "creditsRequired": 3.0, "courses": ["SOC101", "SOC201", "SOC301"]},
    {"subject": "Physical Education", "creditsRequired": 1.0, "courses": ["PE101", "PE201"]},
    {"subject": "Health", "creditsRequired": 0.5, "courses": ["HLT101"]},
    {"subject": "Fine Arts", "creditsRequired": 1.0, "courses": ["ART101", "MUS101", "DRA101"]},
    {"subject": "Electives", "creditsRequired": 7.5, "courses": []}
  ],
  "minimumGPA": 2.0,
  "communityServiceHours": 40
}
```

---

## 3. Managing Teachers

### 3.1 Add a New Teacher

**Step 1:** Create teacher record

```
POST /api/teachers
```

```json
{
  "employeeId": "T2026001",
  "firstName": "Sarah",
  "lastName": "Johnson",
  "email": "sjohnson@school.edu",
  "phone": "555-123-4567",
  "department": "Mathematics",
  "hireDate": "2020-08-15",
  "certifications": [
    {
      "type": "Secondary Mathematics 6-12",
      "state": "IL",
      "issueDate": "2020-06-01",
      "expirationDate": "2025-06-01",
      "status": "ACTIVE"
    }
  ],
  "highestDegree": "MASTERS",
  "university": "State University",
  "yearsOfExperience": 5,
  "isFullTime": true,
  "maxCoursesPerDay": 6,
  "maxPeriodsPerDay": 7,
  "planningPeriod": 4
}
```

**Step 2:** Set teacher availability

```
POST /api/teachers/{teacherId}/availability
```

```json
{
  "availableDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "availablePeriods": [1, 2, 3, 5, 6, 7],
  "unavailableBlocks": [
    {
      "dayOfWeek": "WEDNESDAY",
      "period": 7,
      "reason": "Department meeting"
    }
  ]
}
```

**Step 3:** Assign room preferences

```
POST /api/teachers/{teacherId}/room-preferences
```

```json
{
  "preferredRooms": ["ROOM-201", "ROOM-202"],
  "requiredEquipment": ["SMARTBOARD", "PROJECTOR"],
  "accessibilityNeeds": false
}
```

### 3.2 Teacher Credentials Tracking

Monitor certification expirations:

```
GET /api/teachers/certifications/expiring?withinDays=90
```

### 3.3 Import Teachers from CSV

```
POST /api/import/teachers
Content-Type: multipart/form-data

file: teachers.csv
```

CSV Format:
```csv
employeeId,firstName,lastName,email,department,hireDate,certificationExpiration
T001,John,Smith,jsmith@school.edu,English,2018-08-20,2025-06-01
T002,Mary,Davis,mdavis@school.edu,Science,2019-08-15,2026-06-01
```

---

## 4. Setting Up Classrooms/Rooms

### 4.1 Add a New Room

```
POST /api/rooms
```

```json
{
  "roomNumber": "201",
  "building": "Main",
  "floor": 2,
  "roomType": "CLASSROOM",
  "capacity": 30,
  "equipment": ["SMARTBOARD", "PROJECTOR", "COMPUTERS"],
  "hasComputers": true,
  "computerCount": 30,
  "hasProjector": true,
  "hasSmartboard": true,
  "wheelchairAccessible": true,
  "specialFeatures": ["Lab stations", "Safety equipment"],
  "department": "Science"
}
```

### 4.2 Room Types

| Type | Description |
|------|-------------|
| `CLASSROOM` | Standard classroom |
| `SCIENCE_LAB` | Science laboratory |
| `COMPUTER_LAB` | Computer laboratory |
| `GYM` | Gymnasium |
| `AUDITORIUM` | Large assembly space |
| `MUSIC_ROOM` | Music practice/instruction |
| `ART_ROOM` | Art studio |
| `LIBRARY` | Library/Media center |
| `CAFETERIA` | Lunch room |
| `OFFICE` | Administrative office |

### 4.3 Import Rooms from CSV

```
POST /api/import/rooms
Content-Type: multipart/form-data

file: rooms.csv
```

---

## 5. Creating Courses

### 5.1 Add a New Course

```
POST /api/courses
```

```json
{
  "courseCode": "MATH201",
  "name": "Algebra II",
  "description": "Advanced algebraic concepts including quadratic equations, functions, and complex numbers",
  "department": "Mathematics",
  "subjectArea": "MATH",
  "gradeLevel": ["10", "11"],
  "credits": 1.0,
  "isWeighted": false,
  "weightMultiplier": 1.0,
  "prerequisites": ["MATH101"],
  "corequisites": [],
  "maxEnrollment": 30,
  "minEnrollment": 10,
  "isRequired": true,
  "isElective": false,
  "isAP": false,
  "isHonors": false,
  "semesterLength": "FULL_YEAR",
  "periodsPerWeek": 5,
  "roomRequirements": {
    "type": "CLASSROOM",
    "needsComputers": false,
    "needsLab": false
  }
}
```

### 5.2 Course Types

| Type | Weight | Description |
|------|--------|-------------|
| **Regular** | 1.0 | Standard courses |
| **Honors** | 1.0 (weighted GPA: +0.5) | Advanced level |
| **AP** | 1.0 (weighted GPA: +1.0) | College-level courses |
| **Dual Enrollment** | 1.0 | College credit courses |

### 5.3 Set Course Prerequisites

```
PUT /api/courses/{courseId}/prerequisites
```

```json
{
  "prerequisites": [
    {
      "courseCode": "MATH101",
      "minimumGrade": "C",
      "isRequired": true
    }
  ],
  "alternativePrerequisites": [
    {
      "courseCode": "MATH100H",
      "minimumGrade": "B"
    }
  ]
}
```

### 5.4 Import Courses from CSV

```
POST /api/import/courses
Content-Type: multipart/form-data

file: courses.csv
```

---

## 6. Student Enrollment

### 6.1 New Student Registration

**Step 1:** Create student record

```
POST /api/students
```

```json
{
  "firstName": "Emily",
  "lastName": "Rodriguez",
  "middleName": "Marie",
  "preferredName": "Emmy",
  "dateOfBirth": "2008-03-15",
  "gender": "FEMALE",
  "gradeLevel": "10",
  "enrollmentDate": "2025-08-20",
  "enrollmentStatus": "ACTIVE",

  "address": {
    "street": "456 Oak Avenue",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62702"
  },

  "contacts": {
    "primaryPhone": "555-234-5678",
    "email": "erodriguez@student.school.edu",
    "emergencyContact": {
      "name": "Maria Rodriguez",
      "relationship": "Mother",
      "phone": "555-234-5679",
      "alternatePhone": "555-234-5680"
    }
  },

  "demographics": {
    "ethnicity": "Hispanic/Latino",
    "primaryLanguage": "English",
    "homeLanguage": "Spanish",
    "isELL": false,
    "citizenship": "US_CITIZEN"
  },

  "specialPrograms": {
    "hasIEP": false,
    "has504Plan": false,
    "isGifted": true,
    "isMigrant": false,
    "isHomeless": false,
    "isFosterCare": false
  },

  "previousSchool": {
    "name": "Jefferson Middle School",
    "city": "Springfield",
    "state": "IL",
    "lastGradeCompleted": "9",
    "withdrawalDate": "2025-06-01"
  }
}
```

**Step 2:** Verify enrollment documents

```
POST /api/students/{studentId}/enrollment-verification
```

```json
{
  "documentsReceived": {
    "birthCertificate": true,
    "immunizationRecords": true,
    "proofOfResidency": true,
    "previousTranscripts": true,
    "iepRecords": false,
    "custodyDocuments": false
  },
  "verifiedBy": "registrar@school.edu",
  "verificationDate": "2025-08-18",
  "notes": "All required documents received and verified"
}
```

**Step 3:** Assign student ID

The system automatically generates a unique student ID, or you can assign one:

```
PUT /api/students/{studentId}/student-id
```

```json
{
  "studentId": "STU2026001",
  "stateStudentId": "IL123456789"
}
```

### 6.2 Transfer Student Process

For students transferring from another school:

```
POST /api/transfers/incoming
```

```json
{
  "studentFirstName": "James",
  "studentLastName": "Wilson",
  "dateOfBirth": "2007-11-22",
  "previousSchool": {
    "name": "Central High School",
    "districtCode": "DIST002",
    "city": "Chicago",
    "state": "IL",
    "phone": "555-789-0123"
  },
  "requestedGradeLevel": "11",
  "transferDate": "2025-10-15",
  "recordsRequested": [
    "TRANSCRIPT",
    "ATTENDANCE",
    "DISCIPLINE",
    "IEP",
    "IMMUNIZATIONS"
  ]
}
```

### 6.3 Bulk Student Import

```
POST /api/import/students
Content-Type: multipart/form-data

file: students.csv
```

CSV Format:
```csv
studentId,firstName,lastName,dateOfBirth,gradeLevel,email,phone,address,city,state,zip
STU001,Emily,Rodriguez,2008-03-15,10,erodriguez@school.edu,555-234-5678,456 Oak Ave,Springfield,IL,62702
STU002,James,Wilson,2007-11-22,11,jwilson@school.edu,555-345-6789,789 Elm St,Springfield,IL,62703
```

---

## 7. Building the Master Schedule

### 7.1 Create a New Schedule

```
POST /api/schedules
```

```json
{
  "name": "Fall 2025 Master Schedule",
  "academicYear": "2025-2026",
  "semester": "FALL",
  "startDate": "2025-08-20",
  "endDate": "2025-12-19",
  "bellScheduleId": 1,
  "status": "DRAFT"
}
```

### 7.2 Create Course Sections

For each course, create one or more sections:

```
POST /api/schedules/{scheduleId}/sections
```

```json
{
  "courseId": 1,
  "sectionNumber": "01",
  "teacherId": 1,
  "roomId": 5,
  "maxEnrollment": 30,
  "periods": [
    {"dayOfWeek": "MONDAY", "period": 2},
    {"dayOfWeek": "TUESDAY", "period": 2},
    {"dayOfWeek": "WEDNESDAY", "period": 2},
    {"dayOfWeek": "THURSDAY", "period": 2},
    {"dayOfWeek": "FRIDAY", "period": 2}
  ]
}
```

### 7.3 Auto-Generate Schedule with AI

Let the OptaPlanner AI optimizer create the schedule:

```
POST /api/schedules/{scheduleId}/optimize
```

```json
{
  "constraints": {
    "respectTeacherAvailability": true,
    "respectRoomCapacity": true,
    "minimizeRoomChanges": true,
    "balanceTeacherLoad": true,
    "avoidBackToBackClasses": false,
    "prioritizeSeniors": true
  },
  "timeLimit": "PT5M"
}
```

### 7.4 Resolve Conflicts

Check for scheduling conflicts:

```
GET /api/schedules/{scheduleId}/conflicts
```

Response:
```json
{
  "conflicts": [
    {
      "type": "TEACHER_DOUBLE_BOOKED",
      "description": "Teacher Sarah Johnson assigned to two sections at Period 3",
      "severity": "HIGH",
      "affectedEntities": ["Section MATH201-01", "Section MATH301-01"],
      "suggestedResolution": "Move MATH301-01 to Period 5"
    },
    {
      "type": "ROOM_CAPACITY_EXCEEDED",
      "description": "Room 201 has 35 students but capacity is 30",
      "severity": "MEDIUM",
      "affectedEntities": ["Section ENG101-02"],
      "suggestedResolution": "Move to Room 105 (capacity 40)"
    }
  ],
  "totalConflicts": 2,
  "resolvedConflicts": 0
}
```

### 7.5 Publish Schedule

Once conflicts are resolved:

```
POST /api/schedules/{scheduleId}/publish
```

```json
{
  "publishedBy": "admin@school.edu",
  "effectiveDate": "2025-08-20",
  "notifyTeachers": true,
  "notifyStudents": true
}
```

---

## 8. Assigning Students to Courses

### 8.1 Individual Course Request

Student or counselor submits course requests:

```
POST /api/students/{studentId}/course-requests
```

```json
{
  "academicYear": "2025-2026",
  "requests": [
    {"courseCode": "ENG201", "priority": 1, "isRequired": true},
    {"courseCode": "MATH201", "priority": 1, "isRequired": true},
    {"courseCode": "SCI201", "priority": 1, "isRequired": true},
    {"courseCode": "SOC201", "priority": 1, "isRequired": true},
    {"courseCode": "PE201", "priority": 2, "isRequired": true},
    {"courseCode": "ART101", "priority": 3, "isRequired": false, "isElective": true},
    {"courseCode": "SPAN201", "priority": 3, "isRequired": false, "isElective": true}
  ],
  "alternates": [
    {"courseCode": "MUS101", "replacesRequest": 6},
    {"courseCode": "FREN101", "replacesRequest": 7}
  ]
}
```

### 8.2 Automated Student Scheduling

Run the student scheduler to assign students to sections:

```
POST /api/scheduling/run-student-scheduler
```

```json
{
  "scheduleId": 1,
  "options": {
    "prioritizeSeniors": true,
    "respectPrerequisites": true,
    "balanceSections": true,
    "allowAlternates": true,
    "maxClassSize": 30
  },
  "studentFilters": {
    "gradeLevels": ["9", "10", "11", "12"],
    "excludeStudentIds": []
  }
}
```

### 8.3 Manual Section Assignment

Counselor manually assigns student to a specific section:

```
POST /api/enrollments
```

```json
{
  "studentId": 1,
  "sectionId": 5,
  "enrollmentDate": "2025-08-20",
  "enrollmentType": "REGULAR",
  "overridePrerequisites": false,
  "notes": "Requested by parent"
}
```

### 8.4 View Student Schedule

```
GET /api/students/{studentId}/schedule?academicYear=2025-2026
```

Response:
```json
{
  "studentId": 1,
  "studentName": "Emily Rodriguez",
  "gradeLevel": "10",
  "academicYear": "2025-2026",
  "schedule": [
    {
      "period": 1,
      "course": "English II",
      "courseCode": "ENG201",
      "section": "01",
      "teacher": "John Smith",
      "room": "101",
      "days": ["M", "T", "W", "TH", "F"]
    },
    {
      "period": 2,
      "course": "Algebra II",
      "courseCode": "MATH201",
      "section": "02",
      "teacher": "Sarah Johnson",
      "room": "201",
      "days": ["M", "T", "W", "TH", "F"]
    }
  ],
  "totalCredits": 7.0,
  "lunchPeriod": 5
}
```

### 8.5 Schedule Change Request

```
POST /api/schedule-changes
```

```json
{
  "studentId": 1,
  "requestType": "DROP_ADD",
  "dropSectionId": 5,
  "addSectionId": 8,
  "reason": "Schedule conflict with extracurricular activity",
  "requestedBy": "counselor@school.edu",
  "effectiveDate": "2025-09-01"
}
```

---

## 9. Daily Operations

### 9.1 Taking Attendance

**Option A: Teacher Web Portal**

```
POST /api/attendance
```

```json
{
  "sectionId": 5,
  "date": "2025-09-15",
  "period": 2,
  "takenBy": "teacher@school.edu",
  "records": [
    {"studentId": 1, "status": "PRESENT"},
    {"studentId": 2, "status": "PRESENT"},
    {"studentId": 3, "status": "ABSENT_UNEXCUSED"},
    {"studentId": 4, "status": "TARDY", "minutesLate": 5},
    {"studentId": 5, "status": "ABSENT_EXCUSED", "reason": "Doctor appointment"}
  ]
}
```

**Option B: QR Code Attendance**

Students scan QR code at classroom:

```
POST /api/attendance/qr-scan
```

```json
{
  "qrCodeId": "QR-ROOM201-P2-20250915",
  "studentQrCode": "STU-001-QR",
  "timestamp": "2025-09-15T09:02:00"
}
```

### 9.2 Attendance Status Codes

| Code | Description |
|------|-------------|
| `PRESENT` | Student present |
| `ABSENT_EXCUSED` | Excused absence |
| `ABSENT_UNEXCUSED` | Unexcused absence |
| `TARDY` | Late arrival |
| `EARLY_DISMISSAL` | Left early |
| `FIELD_TRIP` | School activity |
| `ISS` | In-school suspension |
| `OSS` | Out-of-school suspension |

### 9.3 Daily Reports

Generate daily attendance report:

```
GET /api/reports/attendance/daily?date=2025-09-15
```

---

## 10. Grading and Academic Records

### 10.1 Gradebook Setup

Configure gradebook categories for a section:

```
POST /api/gradebook/{sectionId}/categories
```

```json
{
  "categories": [
    {"name": "Tests", "weight": 40, "dropLowest": 0},
    {"name": "Quizzes", "weight": 20, "dropLowest": 1},
    {"name": "Homework", "weight": 20, "dropLowest": 2},
    {"name": "Projects", "weight": 15, "dropLowest": 0},
    {"name": "Participation", "weight": 5, "dropLowest": 0}
  ]
}
```

### 10.2 Create Assignment

```
POST /api/gradebook/{sectionId}/assignments
```

```json
{
  "name": "Chapter 5 Test",
  "category": "Tests",
  "pointsPossible": 100,
  "dueDate": "2025-10-15",
  "assignedDate": "2025-10-08",
  "description": "Covers sections 5.1-5.5",
  "isExtraCredit": false,
  "allowLateSubmission": true,
  "latePenaltyPercent": 10
}
```

### 10.3 Enter Grades

```
POST /api/gradebook/assignments/{assignmentId}/grades
```

```json
{
  "grades": [
    {"studentId": 1, "pointsEarned": 92, "comments": "Excellent work!"},
    {"studentId": 2, "pointsEarned": 85, "comments": "Good effort"},
    {"studentId": 3, "pointsEarned": 78, "comments": "Review section 5.3"},
    {"studentId": 4, "pointsEarned": null, "status": "MISSING"},
    {"studentId": 5, "pointsEarned": 88, "isLate": true, "daysLate": 2}
  ],
  "gradedBy": "teacher@school.edu",
  "gradedDate": "2025-10-16"
}
```

### 10.4 Calculate Course Grades

```
GET /api/gradebook/{sectionId}/student/{studentId}/grade
```

Response:
```json
{
  "studentId": 1,
  "sectionId": 5,
  "courseCode": "MATH201",
  "categoryGrades": [
    {"category": "Tests", "average": 91.5, "weight": 40},
    {"category": "Quizzes", "average": 88.0, "weight": 20},
    {"category": "Homework", "average": 95.0, "weight": 20},
    {"category": "Projects", "average": 90.0, "weight": 15},
    {"category": "Participation", "average": 100.0, "weight": 5}
  ],
  "currentGrade": 91.85,
  "letterGrade": "A",
  "gpaPoints": 4.0,
  "assignmentCount": 25,
  "missingAssignments": 0
}
```

### 10.5 Progress Reports

Generate mid-term progress report:

```
GET /api/students/{studentId}/progress-report?term=FALL&year=2025-2026
```

### 10.6 Report Cards

Generate end-of-term report card:

```
GET /api/students/{studentId}/report-card?term=FALL&year=2025-2026
```

### 10.7 GPA Calculation

The system automatically calculates:

- **Unweighted GPA**: Standard 4.0 scale
- **Weighted GPA**: Includes honors (+0.5) and AP (+1.0) weights
- **Class Rank**: Based on weighted GPA

```
GET /api/students/{studentId}/gpa
```

Response:
```json
{
  "studentId": 1,
  "unweightedGPA": 3.75,
  "weightedGPA": 4.15,
  "classRank": 15,
  "classSize": 250,
  "percentile": 94,
  "totalCreditsEarned": 14.0,
  "totalCreditsAttempted": 14.0,
  "qualityPoints": 52.5
}
```

---

## 11. Progress Monitoring

### 11.1 Academic Alerts

Set up automatic alerts for:

```
POST /api/alerts/academic-rules
```

```json
{
  "rules": [
    {
      "name": "Failing Grade Alert",
      "condition": "GRADE_BELOW",
      "threshold": 60,
      "notifyTeacher": true,
      "notifyCounselor": true,
      "notifyParent": true
    },
    {
      "name": "Missing Assignments",
      "condition": "MISSING_COUNT_ABOVE",
      "threshold": 3,
      "notifyTeacher": true,
      "notifyParent": true
    },
    {
      "name": "Attendance Warning",
      "condition": "ABSENCE_COUNT_ABOVE",
      "threshold": 5,
      "notifyPrincipal": true,
      "notifyParent": true
    }
  ]
}
```

### 11.2 Intervention Tracking

```
POST /api/students/{studentId}/interventions
```

```json
{
  "type": "ACADEMIC_SUPPORT",
  "reason": "Failing Math grade",
  "startDate": "2025-10-20",
  "interventions": [
    "After-school tutoring Tuesdays and Thursdays",
    "Weekly progress check with counselor",
    "Parent conference scheduled"
  ],
  "goals": [
    "Raise Math grade to C by end of semester",
    "Complete all missing assignments"
  ],
  "assignedTo": "counselor@school.edu"
}
```

### 11.3 Graduation Progress Check

```
GET /api/students/{studentId}/graduation-progress
```

Response:
```json
{
  "studentId": 1,
  "expectedGraduationYear": 2027,
  "isOnTrack": true,
  "creditsEarned": 14.0,
  "creditsRequired": 24.0,
  "creditsRemaining": 10.0,
  "requirements": [
    {
      "subject": "English",
      "required": 4.0,
      "earned": 2.0,
      "remaining": 2.0,
      "status": "ON_TRACK"
    },
    {
      "subject": "Mathematics",
      "required": 4.0,
      "earned": 2.0,
      "remaining": 2.0,
      "status": "ON_TRACK"
    },
    {
      "subject": "Science",
      "required": 3.0,
      "earned": 1.0,
      "remaining": 2.0,
      "status": "ON_TRACK"
    }
  ],
  "gpaStatus": "MEETS_REQUIREMENT",
  "communityServiceHours": {
    "required": 40,
    "completed": 15,
    "remaining": 25
  }
}
```

---

## 12. Year-End Procedures

### 12.1 Final Grade Submission

Teachers submit final grades:

```
POST /api/gradebook/{sectionId}/finalize
```

```json
{
  "term": "SPRING",
  "academicYear": "2025-2026",
  "finalizedBy": "teacher@school.edu",
  "overrides": [
    {
      "studentId": 5,
      "calculatedGrade": "D",
      "overrideGrade": "C",
      "reason": "Significant improvement in final exam"
    }
  ]
}
```

### 12.2 Credit Award

Process credit awards for completed courses:

```
POST /api/academic-records/award-credits
```

```json
{
  "academicYear": "2025-2026",
  "term": "SPRING",
  "processingOptions": {
    "includeFailingGrades": false,
    "awardPartialCredit": false
  }
}
```

### 12.3 Grade Level Promotion

Process student promotions:

```
POST /api/students/process-promotions
```

```json
{
  "academicYear": "2025-2026",
  "rules": {
    "minimumCredits": {
      "9to10": 6.0,
      "10to11": 12.0,
      "11to12": 18.0
    },
    "minimumGPA": 1.0,
    "requiredCoursesPassed": ["ENG", "MATH"]
  }
}
```

### 12.4 Archive Academic Year

```
POST /api/admin/archive-year
```

```json
{
  "academicYear": "2025-2026",
  "archiveOptions": {
    "includeGradebook": true,
    "includeAttendance": true,
    "includeDiscipline": true,
    "retentionYears": 7
  }
}
```

---

## 13. Graduation Processing

### 13.1 Identify Graduation Candidates

```
GET /api/graduation/candidates?year=2026
```

Response:
```json
{
  "graduationYear": 2026,
  "candidates": [
    {
      "studentId": 1,
      "name": "Emily Rodriguez",
      "status": "ELIGIBLE",
      "creditsEarned": 24.5,
      "gpa": 3.85,
      "allRequirementsMet": true,
      "honors": ["CUM_LAUDE"]
    },
    {
      "studentId": 2,
      "name": "James Wilson",
      "status": "PENDING",
      "creditsEarned": 23.5,
      "gpa": 2.95,
      "allRequirementsMet": false,
      "deficiencies": ["Missing 0.5 Fine Arts credit"]
    }
  ],
  "totalCandidates": 248,
  "eligible": 235,
  "pending": 13
}
```

### 13.2 Graduation Audit

Run detailed graduation audit for a student:

```
GET /api/students/{studentId}/graduation-audit
```

Response:
```json
{
  "studentId": 1,
  "studentName": "Emily Rodriguez",
  "expectedGraduationDate": "2026-05-28",
  "auditDate": "2026-04-15",
  "overallStatus": "ELIGIBLE",

  "creditAudit": {
    "totalRequired": 24.0,
    "totalEarned": 24.5,
    "surplus": 0.5
  },

  "subjectRequirements": [
    {
      "subject": "English",
      "required": 4.0,
      "earned": 4.0,
      "courses": [
        {"code": "ENG101", "name": "English I", "grade": "A", "credits": 1.0},
        {"code": "ENG201", "name": "English II", "grade": "A", "credits": 1.0},
        {"code": "ENG301", "name": "English III", "grade": "B", "credits": 1.0},
        {"code": "ENG401", "name": "English IV", "grade": "A", "credits": 1.0}
      ],
      "status": "COMPLETE"
    }
  ],

  "gpaRequirement": {
    "required": 2.0,
    "actual": 3.85,
    "status": "MET"
  },

  "otherRequirements": {
    "communityService": {"required": 40, "completed": 45, "status": "MET"},
    "seniorProject": {"required": true, "completed": true, "status": "MET"},
    "exitExam": {"required": true, "passed": true, "status": "MET"}
  },

  "honorsEligibility": {
    "summaLaude": false,
    "magnaLaude": false,
    "cumLaude": true,
    "gpaThreshold": 3.5
  }
}
```

### 13.3 Process Graduation

Mark students as graduated:

```
POST /api/graduation/process
```

```json
{
  "graduationDate": "2026-05-28",
  "ceremonyDetails": {
    "venue": "Lincoln High School Stadium",
    "time": "10:00 AM",
    "address": "123 Education Lane"
  },
  "studentIds": [1, 2, 3, 4, 5],
  "diplomaType": "STANDARD",
  "approvedBy": "principal@school.edu"
}
```

### 13.4 Update Student Status

```
PUT /api/students/{studentId}/status
```

```json
{
  "status": "GRADUATED",
  "graduationDate": "2026-05-28",
  "diplomaType": "STANDARD",
  "honors": ["CUM_LAUDE"],
  "finalGPA": 3.85,
  "finalClassRank": 15,
  "classSize": 248
}
```

---

## 14. Transcript Generation

### 14.1 Generate Official Transcript

```
POST /api/transcripts/generate
```

```json
{
  "studentId": 1,
  "transcriptType": "OFFICIAL",
  "includeOptions": {
    "grades": true,
    "gpa": true,
    "classRank": true,
    "standardizedTests": true,
    "attendanceSummary": true,
    "disciplineRecord": false,
    "activities": true,
    "honors": true
  },
  "format": "PDF",
  "destination": {
    "type": "EMAIL",
    "recipientEmail": "admissions@university.edu",
    "recipientName": "State University Admissions"
  },
  "notes": "Requested for college application",
  "requestedBy": "registrar@school.edu"
}
```

### 14.2 Transcript Content

The official transcript includes:

```
┌─────────────────────────────────────────────────────────────────┐
│                    LINCOLN HIGH SCHOOL                          │
│                   OFFICIAL TRANSCRIPT                           │
│                                                                 │
│ Student: Emily Marie Rodriguez       Student ID: STU2026001    │
│ Date of Birth: March 15, 2008        Gender: Female            │
│ Entry Date: August 20, 2023          Graduation: May 28, 2026  │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ GRADE 9 - 2023-2024                                            │
│ ─────────────────────────────────────────────────────────────── │
│ Course                    Credits  Grade  Quality Points       │
│ English I                   1.0      A         4.0             │
│ Algebra I                   1.0      A         4.0             │
│ Earth Science               1.0      B         3.0             │
│ World History               1.0      A         4.0             │
│ Physical Education I        0.5      A         4.0             │
│ Health                      0.5      A         4.0             │
│ Spanish I                   1.0      B         3.0             │
│                                                                 │
│ Term GPA: 3.71    Credits Earned: 6.0                          │
├─────────────────────────────────────────────────────────────────┤
│ GRADE 10 - 2024-2025                                           │
│ [Similar format...]                                             │
├─────────────────────────────────────────────────────────────────┤
│ CUMULATIVE RECORD                                               │
│ ─────────────────────────────────────────────────────────────── │
│ Total Credits Earned: 24.5                                      │
│ Cumulative GPA (Unweighted): 3.75                              │
│ Cumulative GPA (Weighted): 4.15                                │
│ Class Rank: 15 of 248                                          │
│ Percentile: 94th                                                │
│                                                                 │
│ HONORS: Cum Laude                                               │
│ DIPLOMA TYPE: Standard High School Diploma                      │
├─────────────────────────────────────────────────────────────────┤
│ STANDARDIZED TEST SCORES                                        │
│ SAT (March 2026): Math 720, Reading 680, Total: 1400           │
│ ACT (April 2026): Composite: 31                                │
├─────────────────────────────────────────────────────────────────┤
│ ACTIVITIES & HONORS                                             │
│ • National Honor Society (2024-2026)                           │
│ • Student Council Secretary (2025-2026)                        │
│ • Varsity Soccer (2023-2026)                                   │
│ • Science Olympiad - State Qualifier (2025)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ This is an official transcript. Alterations void this document.│
│                                                                 │
│ Registrar: Jane Smith         Date Issued: April 15, 2026      │
│ [Official School Seal]                                          │
└─────────────────────────────────────────────────────────────────┘
```

### 14.3 Batch Transcript Generation

For college application season:

```
POST /api/transcripts/batch
```

```json
{
  "studentIds": [1, 2, 3, 4, 5],
  "transcriptType": "OFFICIAL",
  "format": "PDF",
  "deliveryMethod": "PRINT",
  "copies": 3
}
```

### 14.4 Electronic Transcript Exchange

Send transcripts via secure electronic exchange:

```
POST /api/transcripts/electronic-send
```

```json
{
  "studentId": 1,
  "recipientType": "COLLEGE",
  "recipientCode": "002104",
  "recipientName": "State University",
  "exchangeNetwork": "SPEEDE",
  "rushDelivery": false
}
```

### 14.5 Transcript Request Tracking

```
GET /api/transcripts/requests?studentId=1
```

Response:
```json
{
  "requests": [
    {
      "requestId": 101,
      "requestDate": "2026-04-10",
      "transcriptType": "OFFICIAL",
      "recipient": "State University",
      "status": "SENT",
      "sentDate": "2026-04-11",
      "deliveryConfirmation": true
    },
    {
      "requestId": 102,
      "requestDate": "2026-04-12",
      "transcriptType": "OFFICIAL",
      "recipient": "City College",
      "status": "PROCESSING",
      "sentDate": null
    }
  ]
}
```

---

## 15. Appendix: API Reference

### 15.1 Core Endpoints Summary

| Resource | Endpoint | Methods |
|----------|----------|---------|
| **Students** | `/api/students` | GET, POST, PUT, DELETE |
| **Teachers** | `/api/teachers` | GET, POST, PUT, DELETE |
| **Courses** | `/api/courses` | GET, POST, PUT, DELETE |
| **Rooms** | `/api/rooms` | GET, POST, PUT, DELETE |
| **Schedules** | `/api/schedules` | GET, POST, PUT, DELETE |
| **Sections** | `/api/sections` | GET, POST, PUT, DELETE |
| **Enrollments** | `/api/enrollments` | GET, POST, PUT, DELETE |
| **Attendance** | `/api/attendance` | GET, POST, PUT |
| **Gradebook** | `/api/gradebook` | GET, POST, PUT |
| **Transcripts** | `/api/transcripts` | GET, POST |
| **Reports** | `/api/reports` | GET |

### 15.2 Authentication

All API requests require authentication:

```
Authorization: Bearer <jwt_token>
```

Or API Key:
```
X-API-Key: <api_key>
```

### 15.3 Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Duplicate or constraint violation |
| 422 | Unprocessable Entity - Validation error |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |

### 15.4 Rate Limits

| Endpoint Type | Authenticated | Unauthenticated |
|---------------|---------------|-----------------|
| Standard API | 5,000/hour | 100/hour |
| Reports | 100/hour | N/A |
| Bulk Operations | 50/hour | N/A |

---

## Quick Reference Checklist

### New School Year Setup
- [ ] Configure school settings
- [ ] Set up bell schedule
- [ ] Update graduation requirements
- [ ] Import/update teacher records
- [ ] Import/update room data
- [ ] Create/update course catalog
- [ ] Build master schedule
- [ ] Enroll students
- [ ] Process course requests
- [ ] Run student scheduler
- [ ] Publish schedule

### Daily Operations
- [ ] Take attendance each period
- [ ] Enter grades for assignments
- [ ] Process schedule change requests
- [ ] Monitor academic alerts

### End of Term
- [ ] Finalize grades
- [ ] Generate report cards
- [ ] Update academic records
- [ ] Process credit awards

### End of Year
- [ ] Complete final grades
- [ ] Process promotions
- [ ] Graduate seniors
- [ ] Generate transcripts
- [ ] Archive data
- [ ] Prepare for next year

---

**For technical support, contact:**
- Email: support@heronix.edu
- Documentation: [API Docs](http://localhost:8080/swagger-ui.html)
- Health Status: [Health Check](http://localhost:8080/actuator/health)

---

*This guide is part of the Heronix Educational Platform Suite*
*Version 1.0 - January 2026*
