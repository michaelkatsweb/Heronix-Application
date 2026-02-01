package com.heronix.service;

import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Help Service - Provides in-app help content, guides, glossary, and shortcuts
 * Location: src/main/java/com/heronix/service/HelpService.java
 *
 * Features:
 * - How-To Guides by category
 * - Glossary of terms
 * - Keyboard shortcuts reference
 * - Search functionality
 * - Context-sensitive help
 *
 * @author Heronix Educational Systems LLC
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Service
public class HelpService {

    private final Map<String, HelpCategory> categories = new LinkedHashMap<>();
    private final Map<String, HelpArticle> articles = new LinkedHashMap<>();
    private final Map<String, GlossaryTerm> glossary = new LinkedHashMap<>();
    private final List<KeyboardShortcut> shortcuts = new ArrayList<>();
    private final Map<String, String> contextHelp = new HashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("Initializing Help Service...");
        initializeCategories();
        initializeArticles();
        initializeGlossary();
        initializeShortcuts();
        initializeContextHelp();
        log.info("Help Service initialized with {} articles, {} glossary terms, {} shortcuts",
                articles.size(), glossary.size(), shortcuts.size());
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    /**
     * Get all help categories
     */
    public List<HelpCategory> getAllCategories() {
        return new ArrayList<>(categories.values());
    }

    /**
     * Get articles by category
     */
    public List<HelpArticle> getArticlesByCategory(String categoryId) {
        return articles.values().stream()
                .filter(a -> a.getCategoryId().equals(categoryId))
                .sorted(Comparator.comparingInt(HelpArticle::getOrder))
                .collect(Collectors.toList());
    }

    /**
     * Get article by ID
     */
    public Optional<HelpArticle> getArticle(String articleId) {
        return Optional.ofNullable(articles.get(articleId));
    }

    /**
     * Search articles and glossary
     */
    public SearchResults search(String query) {
        String lowerQuery = query.toLowerCase().trim();

        List<HelpArticle> matchingArticles = articles.values().stream()
                .filter(a -> a.getTitle().toLowerCase().contains(lowerQuery) ||
                            a.getContent().toLowerCase().contains(lowerQuery) ||
                            a.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery)))
                .sorted(Comparator.comparingInt(a ->
                    a.getTitle().toLowerCase().contains(lowerQuery) ? 0 : 1))
                .collect(Collectors.toList());

        List<GlossaryTerm> matchingTerms = glossary.values().stream()
                .filter(g -> g.getTerm().toLowerCase().contains(lowerQuery) ||
                            g.getDefinition().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparing(GlossaryTerm::getTerm))
                .collect(Collectors.toList());

        return SearchResults.builder()
                .query(query)
                .articles(matchingArticles)
                .glossaryTerms(matchingTerms)
                .build();
    }

    /**
     * Get all glossary terms
     */
    public List<GlossaryTerm> getAllGlossaryTerms() {
        return glossary.values().stream()
                .sorted(Comparator.comparing(GlossaryTerm::getTerm))
                .collect(Collectors.toList());
    }

    /**
     * Get glossary terms by first letter
     */
    public List<GlossaryTerm> getGlossaryByLetter(char letter) {
        return glossary.values().stream()
                .filter(g -> Character.toUpperCase(g.getTerm().charAt(0)) == Character.toUpperCase(letter))
                .sorted(Comparator.comparing(GlossaryTerm::getTerm))
                .collect(Collectors.toList());
    }

    /**
     * Get all keyboard shortcuts
     */
    public List<KeyboardShortcut> getAllShortcuts() {
        return new ArrayList<>(shortcuts);
    }

    /**
     * Get shortcuts by category
     */
    public List<KeyboardShortcut> getShortcutsByCategory(String category) {
        return shortcuts.stream()
                .filter(s -> s.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Get context-sensitive help for a screen
     */
    public Optional<String> getContextHelp(String screenId) {
        return Optional.ofNullable(contextHelp.get(screenId));
    }

    // ========================================================================
    // INITIALIZATION METHODS
    // ========================================================================

    private void initializeCategories() {
        categories.put("getting-started", HelpCategory.builder()
                .id("getting-started")
                .name("Getting Started")
                .icon("🚀")
                .description("Learn the basics of Heronix SIS")
                .order(1)
                .build());

        categories.put("students", HelpCategory.builder()
                .id("students")
                .name("Student Management")
                .icon("👨‍🎓")
                .description("Managing student records, enrollment, and information")
                .order(2)
                .build());

        categories.put("teachers", HelpCategory.builder()
                .id("teachers")
                .name("Teacher Management")
                .icon("👨‍🏫")
                .description("Managing teacher records and assignments")
                .order(3)
                .build());

        categories.put("courses", HelpCategory.builder()
                .id("courses")
                .name("Courses & Scheduling")
                .icon("📚")
                .description("Course setup, scheduling, and section management")
                .order(4)
                .build());

        categories.put("attendance", HelpCategory.builder()
                .id("attendance")
                .name("Attendance")
                .icon("📋")
                .description("Taking and managing attendance")
                .order(5)
                .build());

        categories.put("grades", HelpCategory.builder()
                .id("grades")
                .name("Grading & GPA")
                .icon("📊")
                .description("Gradebook, GPA calculation, and transcripts")
                .order(6)
                .build());

        categories.put("reports", HelpCategory.builder()
                .id("reports")
                .name("Reports & Analytics")
                .icon("📈")
                .description("Generating reports and viewing analytics")
                .order(7)
                .build());

        categories.put("settings", HelpCategory.builder()
                .id("settings")
                .name("Settings & Configuration")
                .icon("⚙️")
                .description("System configuration and preferences")
                .order(8)
                .build());

        categories.put("troubleshooting", HelpCategory.builder()
                .id("troubleshooting")
                .name("Troubleshooting")
                .icon("🔧")
                .description("Common issues and solutions")
                .order(9)
                .build());
    }

    private void initializeArticles() {
        // =====================================================================
        // GETTING STARTED
        // =====================================================================

        articles.put("gs-overview", HelpArticle.builder()
                .id("gs-overview")
                .categoryId("getting-started")
                .title("System Overview")
                .order(1)
                .tags(Arrays.asList("overview", "introduction", "basics"))
                .content("""
                    # System Overview

                    Welcome to Heronix SIS (Student Information System) - a comprehensive platform
                    for managing all aspects of K-12 education administration.

                    ## Main Modules

                    **Dashboard** - Your command center showing key metrics and alerts

                    **Students** - Manage student records, enrollment, and academic history

                    **Teachers** - Manage teacher profiles, certifications, and assignments

                    **Courses** - Create and manage course catalog and sections

                    **Scheduling** - Build master schedules and assign students

                    **Attendance** - Track daily attendance and generate reports

                    **Grades** - Manage gradebook, calculate GPA, generate transcripts

                    **Reports** - Generate various administrative reports

                    ## Navigation

                    Use the sidebar on the left to navigate between modules. The sidebar can be
                    collapsed by clicking the menu icon.

                    The top bar shows your current location (breadcrumbs) and quick action buttons.
                    """)
                .build());

        articles.put("gs-first-login", HelpArticle.builder()
                .id("gs-first-login")
                .categoryId("getting-started")
                .title("First Login & Setup")
                .order(2)
                .tags(Arrays.asList("login", "setup", "password"))
                .content("""
                    # First Login & Setup

                    ## Default Credentials

                    On first installation, use these credentials:
                    - **Username:** admin
                    - **Password:** admin123

                    **Important:** Change your password immediately after first login!

                    ## Initial Setup Steps

                    1. **Change Password**
                       - Go to Settings → User Profile
                       - Click "Change Password"
                       - Enter new secure password

                    2. **Configure School Information**
                       - Go to Settings → School Settings
                       - Enter school name, address, contact info
                       - Set academic year and grading periods

                    3. **Set Up Grade Levels**
                       - Configure which grades your school serves
                       - K-12 or specific grade ranges

                    4. **Configure Grading Scale**
                       - Set letter grade ranges (A, B, C, D, F)
                       - Configure GPA point values
                       - Set up weighted GPA if needed

                    5. **Add Users**
                       - Create accounts for other administrators
                       - Add teachers, counselors, office staff
                       - Assign appropriate roles
                    """)
                .build());

        articles.put("gs-navigation", HelpArticle.builder()
                .id("gs-navigation")
                .categoryId("getting-started")
                .title("Navigating the Interface")
                .order(3)
                .tags(Arrays.asList("navigation", "interface", "ui", "menu"))
                .content("""
                    # Navigating the Interface

                    ## Sidebar Navigation

                    The left sidebar contains all main modules:
                    - Click any item to navigate to that module
                    - Hover over collapsed sidebar to see labels
                    - Use the collapse button (☰) to toggle sidebar width

                    ## Top Bar

                    - **Breadcrumbs** - Shows your current location
                    - **Quick Add (+)** - Quickly add students, teachers, courses
                    - **Notifications (🔔)** - System alerts and messages
                    - **Help (?)** - Access this help center

                    ## Keyboard Shortcuts

                    Press **Ctrl+/** to view all keyboard shortcuts.

                    Common shortcuts:
                    - **Ctrl+N** - New record
                    - **Ctrl+S** - Save
                    - **Ctrl+F** - Search/Find
                    - **Esc** - Cancel/Close dialog

                    ## Status Bar

                    The bottom status bar shows:
                    - Connection status (green = connected)
                    - Current operation status
                    - Last sync time
                    - Current user and role
                    """)
                .build());

        // =====================================================================
        // STUDENT MANAGEMENT
        // =====================================================================

        articles.put("stu-add-student", HelpArticle.builder()
                .id("stu-add-student")
                .categoryId("students")
                .title("How to Add a New Student")
                .order(1)
                .tags(Arrays.asList("student", "add", "enroll", "new", "registration"))
                .content("""
                    # How to Add a New Student

                    There are two ways to add students to Heronix SIS:

                    ## Method 1: Full Enrollment (Recommended)

                    Use this for new student registrations with complete documentation.

                    1. Go to **Enrollment** → **New Enrollment**
                    2. Complete the 5-step wizard:

                    **Step 1: Student Information**
                    - Enter first name, last name, middle name
                    - Date of birth, gender
                    - Grade level for enrollment
                    - Home address

                    **Step 2: Parent/Guardian Information**
                    - Primary guardian name and contact
                    - Secondary guardian (if applicable)
                    - Emergency contacts

                    **Step 3: Documents**
                    - Mark required documents as received:
                      - Birth certificate
                      - Immunization records
                      - Proof of residence
                    - Upload student photo

                    **Step 4: Course Selection**
                    - Select courses for the student
                    - System shows grade-appropriate options
                    - View credit requirements

                    **Step 5: Review & Submit**
                    - Review all entered information
                    - Certify accuracy
                    - Click Submit to complete enrollment

                    ## Method 2: Quick Add (Editing Existing)

                    For updating existing student records:
                    1. Go to **Students** → Select student
                    2. Click **Edit** button (✏️)
                    3. Update information in tabs
                    4. Click **Save**

                    ## Incomplete Registrations

                    If documents are missing, you can:
                    1. Click "Save as Incomplete"
                    2. Registration is saved for later completion
                    3. Access via **Enrollment** → **Incomplete Registrations**
                    """)
                .build());

        articles.put("stu-search-filter", HelpArticle.builder()
                .id("stu-search-filter")
                .categoryId("students")
                .title("Searching and Filtering Students")
                .order(2)
                .tags(Arrays.asList("search", "filter", "find", "student"))
                .content("""
                    # Searching and Filtering Students

                    ## Quick Search

                    Use the search box at the top of the Students screen:
                    - Search by student ID
                    - Search by first or last name
                    - Search by email

                    Results update as you type.

                    ## Filter Options

                    Use the dropdown filters to narrow results:

                    **Grade Level Filter**
                    - All, K, 1-12
                    - Shows only students in selected grade

                    **Status Filter**
                    - All - Show everyone
                    - Active - Currently enrolled students
                    - Inactive - Withdrawn/graduated students

                    **SPED Filter**
                    - All - Everyone
                    - Has IEP - Students with IEP
                    - Has 504 - Students with 504 plan
                    - Has IEP or 504 - Either accommodation
                    - No SPED - No special education services

                    ## Combining Filters

                    Filters work together. For example:
                    - Grade: 10 + Status: Active = All active 10th graders
                    - SPED: Has IEP + Grade: All = All students with IEP

                    ## Exporting Results

                    After filtering:
                    1. Click **Export** button
                    2. Choose format (Excel, CSV, PDF)
                    3. Filtered results are exported
                    """)
                .build());

        articles.put("stu-manage-courses", HelpArticle.builder()
                .id("stu-manage-courses")
                .categoryId("students")
                .title("Managing Student Course Enrollment")
                .order(3)
                .tags(Arrays.asList("courses", "enrollment", "schedule", "student"))
                .content("""
                    # Managing Student Course Enrollment

                    ## Viewing Enrolled Courses

                    1. Go to **Students** → Select a student
                    2. Click **📚 Courses** button in Actions column
                    3. View currently enrolled courses

                    ## Adding Courses

                    1. In the Courses dialog, find **Available Courses** list
                    2. Select a course
                    3. Click **➕ Enroll** button
                    4. Course moves to Enrolled list

                    ## Dropping Courses

                    1. In the Courses dialog, find **Enrolled Courses** list
                    2. Select the course to drop
                    3. Click **➖ Drop** button
                    4. Confirm the action

                    ## Course Requirements

                    The system checks:
                    - **Prerequisites** - Required courses must be completed first
                    - **Grade Level** - Course must be appropriate for student's grade
                    - **Capacity** - Course section must have available seats
                    - **Schedule Conflicts** - No time conflicts with existing courses

                    ## Viewing Schedule

                    After enrollment:
                    - Student's schedule shows in their profile
                    - Includes period, room, and teacher for each course
                    """)
                .build());

        articles.put("stu-special-ed", HelpArticle.builder()
                .id("stu-special-ed")
                .categoryId("students")
                .title("Managing IEP and 504 Plans")
                .order(4)
                .tags(Arrays.asList("iep", "504", "special education", "sped", "accommodations"))
                .content("""
                    # Managing IEP and 504 Plans

                    ## IEP (Individualized Education Program)

                    For students receiving special education services:

                    1. Go to **Students** → Select student
                    2. Click **Edit** → **Special Ed & Medical** tab
                    3. Check **Has IEP**
                    4. Enter IEP details:
                       - IEP meeting date
                       - Review date
                       - Case manager
                       - Disability category
                       - Accommodations

                    ## 504 Plan

                    For students with disabilities requiring accommodations:

                    1. In Special Ed & Medical tab
                    2. Check **Has 504 Plan**
                    3. Enter 504 details:
                       - Plan date
                       - Disability/condition
                       - Required accommodations

                    ## Visual Indicators

                    In the student list:
                    - **✓ IEP** (orange) - Student has active IEP
                    - **✓ 504** (blue) - Student has 504 plan

                    ## Filtering SPED Students

                    Use the SPED filter dropdown:
                    - "Has IEP" - Only students with IEP
                    - "Has 504" - Only students with 504
                    - "Has IEP or 504" - Either type

                    ## Reports

                    Generate SPED reports via **Reports** → **Special Education**
                    """)
                .build());

        // =====================================================================
        // TEACHER MANAGEMENT
        // =====================================================================

        articles.put("tch-add-teacher", HelpArticle.builder()
                .id("tch-add-teacher")
                .categoryId("teachers")
                .title("How to Add a New Teacher")
                .order(1)
                .tags(Arrays.asList("teacher", "add", "new", "staff"))
                .content("""
                    # How to Add a New Teacher

                    ## Adding a Teacher

                    1. Go to **Teachers** in the sidebar
                    2. Click **+ Add Teacher** button
                    3. Complete the teacher form:

                    **Basic Information**
                    - Employee ID (auto-generated if blank)
                    - First name, Last name
                    - Email address
                    - Phone number
                    - Department

                    **Employment Details**
                    - Position/Title
                    - Hire date
                    - Employment type (Full-time, Part-time, Substitute)
                    - Contract type

                    **Certifications**
                    - Teaching certifications
                    - Subject endorsements
                    - Certification expiration dates

                    **Qualifications**
                    - Subjects qualified to teach
                    - Grade levels
                    - Special qualifications (AP, IB, etc.)

                    4. Click **Save** to add the teacher

                    ## Assigning to Courses

                    After creating a teacher:
                    1. Go to **Courses** → Select a course
                    2. Create or edit a section
                    3. Assign the teacher to the section

                    ## Teacher Portal Access

                    Teachers can access Heronix-Talk (Teacher Portal) using:
                    - Username: Employee ID
                    - Password: Set by admin or self-service reset
                    """)
                .build());

        // =====================================================================
        // COURSES & SCHEDULING
        // =====================================================================

        articles.put("crs-create-course", HelpArticle.builder()
                .id("crs-create-course")
                .categoryId("courses")
                .title("How to Create a Course")
                .order(1)
                .tags(Arrays.asList("course", "create", "add", "catalog"))
                .content("""
                    # How to Create a Course

                    ## Creating a New Course

                    1. Go to **Courses** in the sidebar
                    2. Click **+ Add Course** button
                    3. Fill in course details:

                    **Required Fields**
                    - **Course Code** - Unique identifier (e.g., MATH-101)
                    - **Course Name** - Full name (e.g., Algebra I)

                    **Optional Fields**
                    - **Subject** - Department/subject area
                    - **Level** - Education level (Elementary, Middle, High)
                    - **Duration** - Minutes per class (default: 50)
                    - **Max Students** - Class size limit (default: 30)
                    - **Requires Lab** - Check if lab room needed

                    **Equipment Requirements**
                    - Requires Projector
                    - Requires Smartboard
                    - Requires Computers
                    - Required Room Type (Science Lab, Computer Lab, etc.)
                    - Additional Equipment (comma-separated)

                    4. Click **Save** to create the course

                    ## Creating Course Sections

                    After creating a course, create sections:
                    1. Each section is a specific class meeting
                    2. Assign teacher, room, and period
                    3. Set enrollment limits

                    ## Course vs Section

                    - **Course** = The subject (e.g., "English 1")
                    - **Section** = A specific class (e.g., "English 1, Period 2, Mr. Smith")
                    """)
                .build());

        articles.put("crs-sections", HelpArticle.builder()
                .id("crs-sections")
                .categoryId("courses")
                .title("Managing Course Sections")
                .order(2)
                .tags(Arrays.asList("section", "period", "schedule", "assignment"))
                .content("""
                    # Managing Course Sections

                    ## What is a Section?

                    A section is a specific instance of a course:
                    - Meets at a particular time (period)
                    - In a specific room
                    - Taught by a specific teacher
                    - Has its own roster of students

                    ## Creating a Section

                    1. Via Course Management:
                       - Select course → Add Section

                    2. Via API:
                       ```
                       POST /api/sections
                       {
                         "course": { "id": 5 },
                         "sectionNumber": "1",
                         "assignedTeacher": { "id": 10 },
                         "assignedRoom": { "id": 20 },
                         "assignedPeriod": 2,
                         "maxEnrollment": 30
                       }
                       ```

                    ## Assigning Resources

                    **Assign Teacher**
                    - System checks teacher availability
                    - Prevents double-booking

                    **Assign Room**
                    - System checks room availability
                    - Validates room type matches course needs

                    **Assign Period**
                    - Select from periods 1-6 (configurable)
                    - System checks for conflicts

                    ## Section Status

                    - **PLANNED** - Not yet scheduled
                    - **SCHEDULED** - Resources assigned
                    - **OPEN** - Accepting enrollment
                    - **FULL** - At capacity
                    - **CLOSED** - No longer accepting students
                    - **CANCELLED** - Section cancelled
                    """)
                .build());

        // =====================================================================
        // ATTENDANCE
        // =====================================================================

        articles.put("att-take-attendance", HelpArticle.builder()
                .id("att-take-attendance")
                .categoryId("attendance")
                .title("How to Take Attendance")
                .order(1)
                .tags(Arrays.asList("attendance", "present", "absent", "tardy"))
                .content("""
                    # How to Take Attendance

                    ## Daily Attendance (Office)

                    For homeroom/daily attendance:

                    1. Go to **Attendance** → **Daily Attendance**
                    2. Select date (defaults to today)
                    3. Select grade level or homeroom
                    4. Mark each student:
                       - **P** = Present
                       - **A** = Absent
                       - **T** = Tardy
                       - **E** = Excused absence
                    5. Click **Save Attendance**

                    ## Period Attendance (Teachers)

                    Teachers take attendance via Teacher Portal:

                    1. Log into Heronix-Talk
                    2. Select class period
                    3. View roster
                    4. Mark attendance for each student
                    5. Submit

                    ## Bulk Attendance

                    For special situations:
                    1. Go to **Attendance** → **Bulk Entry**
                    2. Select students (search or filter)
                    3. Select date range
                    4. Mark attendance type
                    5. Enter reason if applicable
                    6. Apply to selected students

                    ## Attendance Codes

                    | Code | Meaning | Counts as Absent |
                    |------|---------|------------------|
                    | P | Present | No |
                    | A | Absent | Yes |
                    | T | Tardy | No (tracked separately) |
                    | E | Excused | Yes (but excused) |
                    | S | Suspended | Yes (administrative) |
                    | F | Field Trip | No |
                    """)
                .build());

        // =====================================================================
        // GRADES
        // =====================================================================

        articles.put("grd-enter-grades", HelpArticle.builder()
                .id("grd-enter-grades")
                .categoryId("grades")
                .title("Entering and Managing Grades")
                .order(1)
                .tags(Arrays.asList("grades", "gradebook", "score", "assignment"))
                .content("""
                    # Entering and Managing Grades

                    ## Teacher Gradebook (via Teacher Portal)

                    Teachers enter grades in Heronix-Talk:

                    1. Log into Teacher Portal
                    2. Select class/period
                    3. Go to Gradebook
                    4. Enter assignment grades
                    5. Grades sync to SIS automatically

                    ## Administrative Grade Entry (SIS)

                    Admins can enter/modify grades:

                    1. Go to **Students** → Select student
                    2. Click **📝 Grades** button
                    3. Select course
                    4. Enter or modify grades
                    5. Save changes

                    ## Grade Categories

                    Common grade categories:
                    - **Homework** - Daily assignments
                    - **Quizzes** - Short assessments
                    - **Tests** - Major assessments
                    - **Projects** - Extended assignments
                    - **Participation** - Class engagement
                    - **Final Exam** - End of term exam

                    ## Category Weights

                    Example weighting:
                    - Homework: 20%
                    - Quizzes: 15%
                    - Tests: 30%
                    - Projects: 20%
                    - Final Exam: 15%

                    ## GPA Calculation

                    GPA is calculated automatically based on:
                    - Final grades in courses
                    - Credit hours per course
                    - Grade point values (A=4.0, B=3.0, etc.)

                    Weighted GPA includes:
                    - Honors courses (+0.5)
                    - AP/IB courses (+1.0)
                    """)
                .build());

        // =====================================================================
        // REPORTS
        // =====================================================================

        articles.put("rpt-generate", HelpArticle.builder()
                .id("rpt-generate")
                .categoryId("reports")
                .title("Generating Reports")
                .order(1)
                .tags(Arrays.asList("report", "export", "pdf", "analytics"))
                .content("""
                    # Generating Reports

                    ## Available Reports

                    **Student Reports**
                    - Enrollment summary
                    - Demographics breakdown
                    - Grade distribution
                    - Attendance summary

                    **Academic Reports**
                    - Honor roll
                    - GPA distribution
                    - Course enrollment
                    - Transcript

                    **Attendance Reports**
                    - Daily attendance
                    - Absence trends
                    - Chronic absenteeism

                    **Staff Reports**
                    - Teacher roster
                    - Course assignments
                    - Certification status

                    ## Running a Report

                    1. Go to **Reports** in sidebar
                    2. Select report type
                    3. Set parameters:
                       - Date range
                       - Grade levels
                       - Specific students/teachers
                    4. Click **Generate Report**
                    5. View on screen or export

                    ## Export Options

                    - **PDF** - For printing
                    - **Excel** - For data analysis
                    - **CSV** - For import to other systems

                    ## Scheduled Reports

                    Set up automatic reports:
                    1. Go to **Reports** → **Scheduled**
                    2. Click **+ New Schedule**
                    3. Select report type
                    4. Set frequency (daily, weekly, monthly)
                    5. Choose recipients
                    6. Save schedule
                    """)
                .build());

        // =====================================================================
        // SETTINGS
        // =====================================================================

        articles.put("set-grading-scale", HelpArticle.builder()
                .id("set-grading-scale")
                .categoryId("settings")
                .title("Configuring Grading Scale")
                .order(1)
                .tags(Arrays.asList("grading", "scale", "gpa", "settings"))
                .content("""
                    # Configuring Grading Scale

                    ## Standard Grading Scale

                    1. Go to **Settings** → **Academic Settings**
                    2. Select **Grading Scale**
                    3. Configure letter grades:

                    | Letter | Min % | Max % | GPA Points |
                    |--------|-------|-------|------------|
                    | A+ | 97 | 100 | 4.0 |
                    | A | 93 | 96 | 4.0 |
                    | A- | 90 | 92 | 3.7 |
                    | B+ | 87 | 89 | 3.3 |
                    | B | 83 | 86 | 3.0 |
                    | B- | 80 | 82 | 2.7 |
                    | C+ | 77 | 79 | 2.3 |
                    | C | 73 | 76 | 2.0 |
                    | C- | 70 | 72 | 1.7 |
                    | D+ | 67 | 69 | 1.3 |
                    | D | 63 | 66 | 1.0 |
                    | D- | 60 | 62 | 0.7 |
                    | F | 0 | 59 | 0.0 |

                    ## Weighted GPA

                    Configure honors/AP weights:
                    - **Honors courses** - Add 0.5 to GPA points
                    - **AP/IB courses** - Add 1.0 to GPA points

                    ## Pass/Fail Courses

                    Some courses can be marked Pass/Fail:
                    - P = Pass (credit earned, not in GPA)
                    - F = Fail (no credit, may affect GPA)
                    """)
                .build());

        // =====================================================================
        // TROUBLESHOOTING
        // =====================================================================

        articles.put("trb-common-issues", HelpArticle.builder()
                .id("trb-common-issues")
                .categoryId("troubleshooting")
                .title("Common Issues and Solutions")
                .order(1)
                .tags(Arrays.asList("troubleshooting", "error", "problem", "fix"))
                .content("""
                    # Common Issues and Solutions

                    ## Login Issues

                    **Problem:** Can't log in
                    **Solutions:**
                    - Check username is correct (case-sensitive)
                    - Verify Caps Lock is off
                    - Try password reset
                    - Contact administrator

                    **Problem:** Session expired
                    **Solution:** Log in again. Sessions expire after 30 minutes of inactivity.

                    ## Data Not Showing

                    **Problem:** New student/course not appearing
                    **Solutions:**
                    - Click Refresh button
                    - Check filter settings (set to "All")
                    - Verify the record was saved (check for success message)
                    - For students: ensure registration was submitted, not saved as incomplete

                    ## Performance Issues

                    **Problem:** System running slowly
                    **Solutions:**
                    - Close unused browser tabs
                    - Clear browser cache
                    - Check internet connection
                    - Try a different browser

                    ## Sync Issues (Teacher Portal)

                    **Problem:** Teacher Portal not showing updated data
                    **Solutions:**
                    - Wait for scheduled sync (every 5 minutes)
                    - Admin can trigger manual sync
                    - Verify SIS API server is running (port 9580)

                    ## Schedule Conflicts

                    **Problem:** Can't assign teacher/room to section
                    **Solution:** Teacher or room already assigned to another section at that time. Check their schedule.

                    ## Export Failures

                    **Problem:** Export not downloading
                    **Solutions:**
                    - Check browser download settings
                    - Try different export format
                    - Reduce data range if exporting large datasets
                    """)
                .build());
    }

    private void initializeGlossary() {
        glossary.put("active-student", GlossaryTerm.builder()
                .term("Active Student")
                .definition("A student currently enrolled and attending the school. Active students count toward enrollment numbers and appear in default student lists.")
                .relatedTerms(Arrays.asList("Inactive Student", "Enrollment"))
                .build());

        glossary.put("attendance-rate", GlossaryTerm.builder()
                .term("Attendance Rate")
                .definition("The percentage of school days a student was present. Calculated as (Days Present / Total School Days) × 100.")
                .relatedTerms(Arrays.asList("Chronic Absenteeism", "ADA"))
                .build());

        glossary.put("ada", GlossaryTerm.builder()
                .term("ADA (Average Daily Attendance)")
                .definition("The average number of students attending school each day over a period. Used for funding calculations in many states.")
                .relatedTerms(Arrays.asList("Attendance Rate", "ADM"))
                .build());

        glossary.put("adm", GlossaryTerm.builder()
                .term("ADM (Average Daily Membership)")
                .definition("The average number of students enrolled in school each day, regardless of attendance. Used for enrollment reporting.")
                .relatedTerms(Arrays.asList("ADA", "Enrollment"))
                .build());

        glossary.put("chronic-absenteeism", GlossaryTerm.builder()
                .term("Chronic Absenteeism")
                .definition("Missing 10% or more of school days for any reason, including excused absences. A key indicator tracked for state reporting.")
                .relatedTerms(Arrays.asList("Attendance Rate", "Truancy"))
                .build());

        glossary.put("course-section", GlossaryTerm.builder()
                .term("Course Section")
                .definition("A specific instance of a course with assigned teacher, room, and time period. Multiple sections of the same course can exist with different teachers or times.")
                .relatedTerms(Arrays.asList("Course", "Period", "Master Schedule"))
                .build());

        glossary.put("credit-hour", GlossaryTerm.builder()
                .term("Credit Hour")
                .definition("A unit measuring academic progress. Typically, one credit = one year-long course. Half-credit = one semester course.")
                .relatedTerms(Arrays.asList("GPA", "Graduation Requirements"))
                .build());

        glossary.put("cumulative-gpa", GlossaryTerm.builder()
                .term("Cumulative GPA")
                .definition("Grade Point Average calculated from all courses taken throughout a student's high school career. Used for class rank and college applications.")
                .relatedTerms(Arrays.asList("Weighted GPA", "Semester GPA"))
                .build());

        glossary.put("enrollment", GlossaryTerm.builder()
                .term("Enrollment")
                .definition("The process of officially registering a student in the school and in specific courses. Includes collecting required documents and assigning a schedule.")
                .relatedTerms(Arrays.asList("Registration", "Active Student"))
                .build());

        glossary.put("ferpa", GlossaryTerm.builder()
                .term("FERPA")
                .definition("Family Educational Rights and Privacy Act. Federal law protecting student education records. Requires parental consent to release records.")
                .relatedTerms(Arrays.asList("Directory Information", "Education Records"))
                .build());

        glossary.put("gpa", GlossaryTerm.builder()
                .term("GPA (Grade Point Average)")
                .definition("A numerical average of grades earned, typically on a 4.0 scale. Calculated by dividing total grade points by total credits attempted.")
                .relatedTerms(Arrays.asList("Weighted GPA", "Cumulative GPA"))
                .build());

        glossary.put("iep", GlossaryTerm.builder()
                .term("IEP (Individualized Education Program)")
                .definition("A legal document for students with disabilities that outlines special education services, goals, and accommodations. Required under IDEA.")
                .relatedTerms(Arrays.asList("504 Plan", "Special Education", "IDEA"))
                .build());

        glossary.put("504-plan", GlossaryTerm.builder()
                .term("504 Plan")
                .definition("A plan providing accommodations for students with disabilities that affect learning but don't require special education services. Named after Section 504 of the Rehabilitation Act.")
                .relatedTerms(Arrays.asList("IEP", "Accommodations"))
                .build());

        glossary.put("master-schedule", GlossaryTerm.builder()
                .term("Master Schedule")
                .definition("The complete schedule of all course sections, showing which teachers teach which courses in which rooms during which periods.")
                .relatedTerms(Arrays.asList("Course Section", "Period", "Schedule"))
                .build());

        glossary.put("period", GlossaryTerm.builder()
                .term("Period")
                .definition("A designated time block during the school day when classes meet. Typically numbered 1-6 or designated by letters (A-F).")
                .relatedTerms(Arrays.asList("Block Schedule", "Course Section"))
                .build());

        glossary.put("roster", GlossaryTerm.builder()
                .term("Roster")
                .definition("The list of students enrolled in a specific course section. Shows student names, IDs, and may include photos.")
                .relatedTerms(Arrays.asList("Enrollment", "Course Section"))
                .build());

        glossary.put("transcript", GlossaryTerm.builder()
                .term("Transcript")
                .definition("Official academic record showing all courses taken, grades earned, credits, and GPA. Used for college applications and transfers.")
                .relatedTerms(Arrays.asList("GPA", "Credit Hour", "Academic Record"))
                .build());

        glossary.put("weighted-gpa", GlossaryTerm.builder()
                .term("Weighted GPA")
                .definition("GPA that gives additional points for honors, AP, or IB courses. Allows GPAs above 4.0 to recognize advanced coursework.")
                .relatedTerms(Arrays.asList("GPA", "AP Course", "Honors Course"))
                .build());
    }

    private void initializeShortcuts() {
        // Global shortcuts
        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("Ctrl + /")
                .action("Show keyboard shortcuts")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("Ctrl + F")
                .action("Focus search box")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("Ctrl + N")
                .action("New record (context-dependent)")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("Ctrl + S")
                .action("Save current form")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("Esc")
                .action("Cancel / Close dialog")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("F1")
                .action("Open Help Center")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Global")
                .keys("F5")
                .action("Refresh current view")
                .build());

        // Navigation shortcuts
        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + D")
                .action("Go to Dashboard")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + S")
                .action("Go to Students")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + T")
                .action("Go to Teachers")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + C")
                .action("Go to Courses")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + A")
                .action("Go to Attendance")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Alt + R")
                .action("Go to Reports")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Ctrl + [")
                .action("Collapse sidebar")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Navigation")
                .keys("Ctrl + ]")
                .action("Expand sidebar")
                .build());

        // Table shortcuts
        shortcuts.add(KeyboardShortcut.builder()
                .category("Tables")
                .keys("Ctrl + A")
                .action("Select all rows")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Tables")
                .keys("Delete")
                .action("Delete selected row(s)")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Tables")
                .keys("Enter")
                .action("Open/Edit selected row")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Tables")
                .keys("Ctrl + E")
                .action("Export table data")
                .build());

        // Form shortcuts
        shortcuts.add(KeyboardShortcut.builder()
                .category("Forms")
                .keys("Tab")
                .action("Move to next field")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Forms")
                .keys("Shift + Tab")
                .action("Move to previous field")
                .build());

        shortcuts.add(KeyboardShortcut.builder()
                .category("Forms")
                .keys("Ctrl + Enter")
                .action("Submit form")
                .build());
    }

    private void initializeContextHelp() {
        contextHelp.put("dashboard", "The Dashboard shows key metrics and alerts for your school. Click any metric to see details.");
        contextHelp.put("students", "Manage student records here. Use filters to narrow results. Click a student to see details.");
        contextHelp.put("teachers", "Manage teacher records and assignments. Assign teachers to courses from here.");
        contextHelp.put("courses", "Manage your course catalog. Create courses first, then add sections with teachers and rooms.");
        contextHelp.put("attendance", "Take and review attendance. Daily attendance for office, period attendance for teachers.");
        contextHelp.put("grades", "View and manage student grades. Teachers enter grades in the Teacher Portal.");
        contextHelp.put("reports", "Generate various reports. Select parameters and export to PDF or Excel.");
        contextHelp.put("settings", "Configure system settings including grading scale, school info, and user accounts.");
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class HelpCategory {
        private String id;
        private String name;
        private String icon;
        private String description;
        private int order;
    }

    @Data
    @Builder
    public static class HelpArticle {
        private String id;
        private String categoryId;
        private String title;
        private String content;
        private int order;
        private List<String> tags;
    }

    @Data
    @Builder
    public static class GlossaryTerm {
        private String term;
        private String definition;
        private List<String> relatedTerms;
    }

    @Data
    @Builder
    public static class KeyboardShortcut {
        private String category;
        private String keys;
        private String action;
    }

    @Data
    @Builder
    public static class SearchResults {
        private String query;
        private List<HelpArticle> articles;
        private List<GlossaryTerm> glossaryTerms;
    }
}
