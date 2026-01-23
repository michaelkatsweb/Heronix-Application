# Heronix-SIS UI/UX Major Overhaul Plan

## Executive Summary

This document outlines a comprehensive improvement plan for Heronix-SIS based on:
- Internal codebase analysis (180+ FXML files, 164 controllers, 384 services)
- Competitor analysis (PowerSchool, Infinite Campus, Skyward, Alma, Blackbaud)
- Industry best practices and 2025 dashboard design principles
- Common SIS user complaints and pain points

---

## Part 1: Current State Analysis

### Strengths
- Comprehensive backend with 384 services and 95+ entities
- 180+ FXML screens covering most workflows
- 8 theme variants (light, dark, modern, glassmorphic, neumorphic)
- Keyboard shortcuts (Ctrl+1-9) for main navigation
- Wizard-based workflows for complex tasks (Import, IEP, Enrollment)
- Role-based access control foundation

### Critical Gaps Identified
1. **30+ backend services have NO UI** (certifications, evaluations, transportation, health screening)
2. **Inconsistent styling** - Colors, fonts, spacing not standardized
3. **No bulk operations** - Missing bulk edit, bulk status change, batch assignment
4. **Limited keyboard navigation** - Missing Ctrl+F, Ctrl+N, Ctrl+S, context menus
5. **No pagination/lazy loading** - Tables load all data, poor performance
6. **No mobile responsiveness** - Fixed 1200x800 minimum layouts
7. **Accessibility gaps** - No screen reader support, keyboard navigation, high contrast
8. **Filter state not persisted** - Resets on page refresh

---

## Part 2: Competitor Analysis Summary

### PowerSchool SIS
**Positives:**
- Quick Search for finding pages, students, staff instantly
- Personal Favorites for frequently accessed pages
- Real-time grade tracking
- Strong parent/student portal integration

**User Complaints:**
- "Every action requires multiple clicks where smarter UX could shorten to 1"
- "New software built atop a shabby 1990's foundation"
- Steep learning curve, expensive training
- Interface feels dated in some areas

### Infinite Campus
**Positives:**
- Clean, "less is more" approach
- Easy grade-book transfer year to year
- Customizable dashboards
- Robust Parent Portal

**User Complaints:**
- "Renamed to Infinite Clicks" - excessive mouse clicks for tasks
- Features hidden within menus, not intuitive to locate
- "Not visually appealing, overwhelming with options"
- Steep initial learning curve

### Skyward
**Positives:**
- Well organized, customizable interface (when learned)
- Strong family engagement features
- Good curriculum management

**User Complaints:**
- "Feels like stepping back to the 1970s"
- Database structure inconsistent
- "Interface like reading non-alphabetical white pages"
- Mobile app issues with window resizing

### Common SIS Industry Pain Points
1. **Too many clicks** for basic operations
2. **Non-intuitive navigation** - features buried in menus
3. **Data silos** - systems don't communicate
4. **Steep learning curves** requiring expensive training
5. **Limited mobile access** - not full functionality
6. **Poor report customization** - complex, time-consuming
7. **Inconsistent communication** with parents/students

---

## Part 3: UI/UX Improvement Categories

### Category A: Navigation & Information Architecture (HIGH PRIORITY)

#### A1. Implement Global Quick Search (Command Palette Enhancement)
**Current State:** Basic Ctrl+/ command palette exists
**Improvement:**
- Add fuzzy search across ALL entities (students, teachers, courses, rooms)
- Search history with recent items
- Category filtering (Students, Staff, Courses, Reports)
- Keyboard-first navigation (arrow keys, Enter to select)
- Search suggestions as you type

**Reference:** PowerSchool Enhanced UI Quick Search

#### A2. Favorites & Quick Access System
**Current State:** Not implemented
**Improvement:**
- Star/pin frequently used screens
- Customizable quick access toolbar
- Personal bookmarks for specific records (e.g., "John Smith - Grade 10")
- Recent items panel (last 10 viewed records)
- Role-based default favorites

#### A3. Breadcrumb Navigation
**Current State:** Not implemented
**Improvement:**
- Show navigation path: Home > Students > John Smith > Grades
- Clickable breadcrumbs for quick back-navigation
- Contextual breadcrumbs based on workflow

#### A4. Sidebar Navigation Redesign
**Current State:** Top menu bar with 14 menus, 60+ items
**Improvement:**
- Collapsible left sidebar with icon + text
- Grouped sections: Academic, People, Operations, Reports
- Expandable sub-menus with smooth animations
- Pin/unpin frequently used items
- Search within sidebar

---

### Category B: Dashboard & Home Screen (HIGH PRIORITY)

#### B1. Role-Based Customizable Dashboards
**Current State:** Single dashboard for all users
**Improvement:**
- Admin Dashboard: School-wide metrics, alerts, compliance
- Teacher Dashboard: My classes, grades due, attendance alerts
- Counselor Dashboard: At-risk students, IEP deadlines, appointments
- Registrar Dashboard: Enrollments, transfers, graduations
- Drag-and-drop widget arrangement
- Widget library for self-customization

#### B2. Actionable Metric Cards
**Current State:** Display-only metrics
**Improvement:**
- Click metric card to drill down to details
- Quick action buttons on cards (e.g., "View All" / "Take Action")
- Trend indicators with sparkline charts
- Color-coded status (green/yellow/red)
- Configurable thresholds per school

#### B3. Today's Tasks & Alerts Panel
**Current State:** Basic notification center
**Improvement:**
- "Today's Focus" section with prioritized tasks
- Due dates for grades, IEPs, reports
- Compliance deadlines (state reporting, FERPA)
- Student alerts (attendance threshold, failing grades)
- One-click dismiss or snooze

#### B4. Quick Actions Widget
**Current State:** Scattered across menus
**Improvement:**
- "Quick Add" floating action button
- Add Student, Add Course, Mark Attendance, Enter Grade
- Context-aware suggestions based on time/role
- Keyboard shortcut hints on hover

---

### Category C: Data Tables & Lists (HIGH PRIORITY)

#### C1. Virtual Scrolling / Pagination
**Current State:** All records loaded at once
**Improvement:**
- Lazy loading with infinite scroll OR
- Server-side pagination (25/50/100 per page)
- "Load more" progressive loading
- Performance target: < 100ms for 10,000 records

#### C2. Advanced Filtering System
**Current State:** Basic ComboBox filters
**Improvement:**
- Multi-select filter chips (Grade Level: 9, 10, 11)
- Filter presets (saved filter combinations)
- "My Filters" for personal saved views
- Quick filter bar with most common options
- Advanced filter dialog for complex queries
- Filter persistence across sessions

#### C3. Inline Editing
**Current State:** Edit via dialog only
**Improvement:**
- Double-click cell to edit in place
- Tab to move between editable cells
- Escape to cancel, Enter to save
- Visual indicator for editable vs read-only cells
- Batch save for multiple edits

#### C4. Bulk Operations Toolbar
**Current State:** Limited bulk actions
**Improvement:**
- Select all / Select none / Invert selection
- Bulk actions dropdown: Edit, Delete, Export, Change Status
- Bulk field update (change status for 50 students)
- Confirmation with affected count
- Undo for bulk operations (within 30 seconds)

#### C5. Column Customization
**Current State:** Fixed columns
**Improvement:**
- Show/hide columns via menu
- Drag to reorder columns
- Resize column widths
- Save column preferences per user
- Reset to default option

#### C6. Context Menus
**Current State:** Not implemented
**Improvement:**
- Right-click on row for context menu
- Quick actions: View, Edit, Delete, Duplicate
- Copy cell value, Copy row as text
- Open in new tab (for student profile, etc.)

---

### Category D: Forms & Data Entry (HIGH PRIORITY)

#### D1. Smart Form Validation
**Current State:** Basic validation, inconsistent patterns
**Improvement:**
- Real-time validation as user types
- Clear error messages with fix suggestions
- Field-level help text and examples
- Required field indicators (asterisk + tooltip)
- Save draft functionality for long forms
- Validation summary at form top

#### D2. Auto-Complete & Smart Suggestions
**Current State:** Basic text fields
**Improvement:**
- Student name auto-complete with photo preview
- Course search with prerequisites shown
- Teacher search with availability indicator
- Recently used values suggested first
- "Create new" option in dropdowns

#### D3. Multi-Step Wizard Improvements
**Current State:** 3-step wizards exist
**Improvement:**
- Progress indicator with step names
- "Save & Continue Later" functionality
- Back button preserves entered data
- Step validation before proceeding
- Summary review before final submit
- Animated transitions between steps

#### D4. Quick Entry Modes
**Current State:** Not implemented
**Improvement:**
- "Quick Add" for rapid data entry
- Tab-through forms (keyboard-first entry)
- Duplicate last entry option
- Import from clipboard (paste from Excel)
- Barcode/QR scanner support for student lookup

---

### Category E: Gradebook Overhaul (HIGH PRIORITY)

#### E1. Spreadsheet-Style Grade Entry
**Current State:** Assignment-list based, separate panel
**Improvement:**
- Full spreadsheet grid (students as rows, assignments as columns)
- Click cell to enter grade directly
- Tab/Enter to move between cells
- Color coding by grade range (A=green, F=red)
- Row/column totals and averages
- Freeze header row and student name column

#### E2. Quick Grade Entry Tools
**Current State:** Manual entry only
**Improvement:**
- "Give all students same grade" bulk option
- Grade curves and scaling tools
- Import grades from CSV/Excel
- Copy grades from another assignment
- "Mark all present students" for participation grades

#### E3. Assignment Templates
**Current State:** Manual creation each time
**Improvement:**
- Save assignment as template
- Recurring assignments (weekly quizzes)
- Category-based defaults (Tests = 100 pts)
- Clone from previous term
- Share templates across teachers

#### E4. Grade Analytics Panel
**Current State:** Basic averages
**Improvement:**
- Class distribution chart (histogram)
- Individual student trend line
- Comparison to class average
- Missing assignment alerts
- Predicted final grade calculator

---

### Category F: Attendance Improvements (HIGH PRIORITY)

#### F1. One-Click Attendance
**Current State:** Multi-step process
**Improvement:**
- "Mark All Present" default with exceptions
- Tap/click student to toggle status
- Quick status buttons: P (Present), A (Absent), T (Tardy), E (Excused)
- Visual roster with student photos
- Color-coded attendance status

#### F2. Attendance Kiosk Mode
**Current State:** Not implemented
**Improvement:**
- Full-screen kiosk for classroom door
- Student barcode/ID scan
- Touch-friendly large buttons
- Auto-close after period ends
- Works offline, syncs when connected

#### F3. Attendance Alerts & Notifications
**Current State:** Manual checking
**Improvement:**
- Auto-notify parents when absent (configurable)
- Threshold alerts (5 absences = counselor alert)
- Tardy pattern detection
- Weekly attendance summary email to parents
- "Not marked yet" reminder for teachers

---

### Category G: Scheduling Interface (MEDIUM PRIORITY)

#### G1. Visual Schedule Builder
**Current State:** Grid-based view exists
**Improvement:**
- Drag-and-drop schedule slots
- Visual conflict indicators (red overlay)
- Room availability heat map
- Teacher availability overlay
- Multiple view modes: Day, Week, Teacher, Room, Student

#### G2. Schedule Conflict Resolution Wizard
**Current State:** Basic conflict dashboard
**Improvement:**
- Guided conflict resolution workflow
- Suggested alternatives with impact preview
- One-click swap between conflicting items
- Conflict history tracking
- "What-if" scenario comparison

#### G3. Master Schedule Templates
**Current State:** Manual configuration
**Improvement:**
- Save schedule as template
- Clone from previous year
- A/B day, block schedule presets
- Import from Excel/CSV
- Export for district review

---

### Category H: Reports & Analytics (MEDIUM PRIORITY)

#### H1. Report Builder UI
**Current State:** Backend exists, limited UI
**Improvement:**
- Drag-and-drop report designer
- Field picker with preview
- Filter builder (visual query builder)
- Group/sort configuration
- Save as template for reuse
- Schedule recurring reports

#### H2. Interactive Dashboards
**Current State:** Static dashboard
**Improvement:**
- Click-through drill-down on all metrics
- Date range selectors
- Comparison periods (this year vs last year)
- Export to PDF/Excel/Image
- Share dashboard via link
- Embed widgets in other screens

#### H3. Report Delivery System
**Current State:** Manual export only
**Improvement:**
- Schedule report emails (daily/weekly/monthly)
- Auto-generate report cards
- Batch print queue
- Report history with versioning
- Notification when report ready

---

### Category I: Communication & Notifications (MEDIUM PRIORITY)

#### I1. Unified Notification Center
**Current State:** Basic notification panel
**Improvement:**
- All notifications in one place
- Filter by type (Alerts, Tasks, Messages)
- Mark as read/unread
- Bulk dismiss options
- Notification preferences per category
- Push notification support (browser)

#### I2. In-App Messaging
**Current State:** Not implemented
**Improvement:**
- Message students/parents from any screen
- Template messages for common communications
- Attachment support
- Message history per student
- Delivery confirmation

#### I3. Announcement System
**Current State:** Not implemented
**Improvement:**
- School-wide announcements
- Grade/class-specific announcements
- Scheduled publishing
- Pin important announcements
- Acknowledgment tracking

---

### Category J: Special Education (SPED) Module (MEDIUM PRIORITY)

#### J1. IEP Timeline Visualization
**Current State:** List-based view
**Improvement:**
- Visual timeline of IEP milestones
- Upcoming deadline indicators
- Meeting scheduler integration
- Document attachment per milestone
- Compliance status indicators

#### J2. Service Tracking Dashboard
**Current State:** Basic tracking exists
**Improvement:**
- Minutes delivered vs required
- Service provider workload view
- Makeup session scheduling
- Parent signature capture
- Progress note templates

#### J3. 504 Plan Management
**Current State:** Basic dialog exists
**Improvement:**
- Accommodation checklist builder
- Teacher notification of accommodations
- Annual review reminders
- Document generation (PDF letters)
- Compliance reporting

---

### Category K: Missing Feature UIs (HIGH PRIORITY)

#### K1. Teacher Certification Management
**Current State:** Backend only (TeacherCertificationService)
**Improvement:**
- Certification list with expiration dates
- Renewal reminder system
- Document upload for certificates
- Certification requirements by course
- Expiration alerts dashboard

#### K2. Teacher Evaluation Tracking
**Current State:** Backend only (TeacherEvaluationService)
**Improvement:**
- Evaluation cycle management
- Observation scheduling
- Rubric-based scoring interface
- Goal setting and tracking
- Performance trend visualization

#### K3. Transportation Management
**Current State:** Backend only (TransportationService)
**Improvement:**
- Bus route visualization (map)
- Student bus assignment
- Stop time management
- Driver assignment
- Parent notification of delays

#### K4. Health Services Dashboard
**Current State:** Limited UI (MedicalRecordDialog only)
**Improvement:**
- Health screening entry forms
- Immunization tracking
- Medication administration log
- Nurse visit tracking
- Health alerts per student

#### K5. Vendor/Supplier Management
**Current State:** API only (VendorService)
**Improvement:**
- Vendor directory
- Contract tracking
- Service history
- Contact management

#### K6. API Key Management
**Current State:** Config file only
**Improvement:**
- API key generation UI
- Key permissions management
- Usage analytics
- Key rotation scheduling
- Integration status monitoring

---

### Category L: Accessibility & Compliance (HIGH PRIORITY)

#### L1. Keyboard Navigation
**Current State:** Partial implementation
**Improvement:**
- Full Tab order in all dialogs
- Arrow key navigation in tables
- Enter to activate, Escape to cancel
- Skip to main content link
- Focus indicators on all interactive elements

#### L2. Screen Reader Support
**Current State:** Not implemented
**Improvement:**
- ARIA labels on all controls
- Accessible table structure
- Form field associations
- Alert announcements
- Landmark regions

#### L3. High Contrast Mode
**Current State:** Not implemented
**Improvement:**
- High contrast theme option
- Minimum 4.5:1 contrast ratios
- No color-only indicators
- Underlines for links
- Clear focus states

#### L4. Keyboard Shortcuts System
**Current State:** Ctrl+1-9 only
**Improvement:**
```
Ctrl+F     - Global search/find
Ctrl+N     - New record
Ctrl+S     - Save current form
Ctrl+E     - Edit selected
Ctrl+D     - Delete selected (with confirm)
Ctrl+P     - Print current view
Escape     - Close dialog/cancel
F1         - Context help
F2         - Edit cell (in tables)
F5         - Refresh data
Alt+Left   - Back
Alt+Right  - Forward
```
- Shortcut help overlay (? key)
- Customizable shortcuts

---

### Category M: Visual Design System (MEDIUM PRIORITY)

#### M1. Design Token Standardization
**Current State:** Inconsistent colors/fonts
**Improvement:**
- Unified color palette (primary, secondary, accent, semantic)
- Typography scale (h1-h6, body, caption)
- Spacing system (4px base unit)
- Border radius tokens
- Shadow tokens
- Animation timing tokens

#### M2. Component Library
**Current State:** Mix of custom and default JavaFX
**Improvement:**
- Standardized button styles (primary, secondary, ghost, danger)
- Input field variations (text, select, date, time, search)
- Card component with variants
- Modal/dialog templates
- Table component with features
- Alert/notification components

#### M3. Icon System
**Current State:** Mixed icon sources
**Improvement:**
- Consistent icon library (FontAwesome/Material)
- Icon + text button standards
- Icon-only button with tooltips
- Consistent icon sizing (16px, 24px, 32px)

#### M4. Responsive Layouts
**Current State:** Fixed 1200x800 minimum
**Improvement:**
- Fluid grid system
- Breakpoints for tablet (768px) and desktop (1024px+)
- Collapsible sidebar on smaller screens
- Responsive tables (horizontal scroll or card view)
- Touch-friendly tap targets (44px minimum)

---

### Category N: Performance Optimizations (MEDIUM PRIORITY)

#### N1. Data Loading Strategy
**Current State:** Load all data upfront
**Improvement:**
- Lazy loading for related data
- Background data prefetching
- Cached lookups (students, teachers, courses)
- Debounced search (300ms delay)
- Loading skeletons during fetch

#### N2. Memory Management
**Current State:** Not optimized
**Improvement:**
- Dispose unused views
- Image lazy loading
- Virtual scroll for long lists
- Limit notification history
- Periodic cache cleanup

#### N3. Startup Optimization
**Current State:** ~3-5 second startup
**Improvement:**
- Splash screen with progress
- Deferred initialization
- Module-based loading
- Critical path optimization
- Cache warm-up in background

---

### Category O: User Onboarding & Help (LOW PRIORITY)

#### O1. First-Run Experience
**Current State:** Not implemented
**Improvement:**
- Welcome wizard for new users
- Role-based setup guide
- Sample data option
- Video tutorials embedded
- Checklist for initial configuration

#### O2. Contextual Help System
**Current State:** Basic Help menu
**Improvement:**
- ? icon on complex fields
- Tooltip explanations
- "Learn more" links to documentation
- Guided tours for new features
- Help search integrated

#### O3. In-App Feedback
**Current State:** Not implemented
**Improvement:**
- Feedback button on every screen
- Screenshot attachment option
- Feature request voting
- Bug report wizard
- Response tracking

---

## Part 4: Implementation Phases

### Phase 1: Foundation (Weeks 1-4)
**Focus:** Design system, navigation, critical bug fixes

1. Create unified CSS design tokens
2. Implement sidebar navigation with collapsible sections
3. Add breadcrumb navigation
4. Implement global quick search enhancement
5. Add keyboard shortcuts system
6. Fix filter persistence

**Deliverables:**
- Updated main-style.css with design tokens
- New SidebarNavigation component
- Enhanced CommandPalette
- KeyboardShortcutManager service

---

### Phase 2: Data Tables & Forms (Weeks 5-8)
**Focus:** Core interaction improvements

1. Implement virtual scrolling/pagination
2. Add inline editing to tables
3. Create bulk operations toolbar
4. Add context menus to tables
5. Implement column customization
6. Enhance form validation

**Deliverables:**
- VirtualTableView component
- BulkOperationsToolbar component
- ColumnCustomizer component
- FormValidator service

---

### Phase 3: Role-Based Dashboards (Weeks 9-12)
**Focus:** Personalized experience

1. Create dashboard widget framework
2. Implement admin dashboard
3. Implement teacher dashboard
4. Implement counselor dashboard
5. Add widget drag-and-drop
6. Create quick actions widget

**Deliverables:**
- DashboardWidget base class
- AdminDashboard, TeacherDashboard, CounselorDashboard FXMLs
- WidgetManager service
- QuickActionsWidget component

---

### Phase 4: Gradebook & Attendance (Weeks 13-16)
**Focus:** Teacher productivity

1. Create spreadsheet-style gradebook
2. Implement one-click attendance
3. Add grade analytics panel
4. Create attendance kiosk mode
5. Implement attendance alerts
6. Add assignment templates

**Deliverables:**
- SpreadsheetGradebook component
- OneClickAttendance component
- GradeAnalyticsPanel component
- AttendanceKioskMode screen
- AssignmentTemplateService

---

### Phase 5: Missing Module UIs (Weeks 17-20)
**Focus:** Backend service exposure

1. Create Teacher Certification UI
2. Create Teacher Evaluation UI
3. Create Health Services Dashboard
4. Create Transportation Management UI
5. Create API Key Management UI
6. Create Vendor Management UI

**Deliverables:**
- TeacherCertificationManagement.fxml
- TeacherEvaluationManagement.fxml
- HealthServicesDashboard.fxml
- TransportationManagement.fxml
- ApiKeyManagement.fxml
- VendorManagement.fxml

---

### Phase 6: Reports & Communication (Weeks 21-24)
**Focus:** Analytics and messaging

1. Create report builder UI
2. Implement interactive dashboards
3. Add report scheduling
4. Create unified notification center
5. Implement in-app messaging
6. Add announcement system

**Deliverables:**
- ReportBuilder.fxml
- InteractiveDashboard component
- ReportScheduler service
- NotificationCenter enhancement
- InAppMessaging.fxml
- AnnouncementSystem.fxml

---

### Phase 7: Accessibility & Polish (Weeks 25-28)
**Focus:** Compliance and refinement

1. Implement full keyboard navigation
2. Add ARIA labels and screen reader support
3. Create high contrast theme
4. Add responsive layouts
5. Implement onboarding wizard
6. Create contextual help system

**Deliverables:**
- KeyboardNavigationManager
- AccessibilityService
- high-contrast-theme.css
- ResponsiveLayoutManager
- OnboardingWizard.fxml
- ContextualHelpService

---

## Part 5: Priority Matrix

### Critical (Must Have)
| Item | Category | Effort | Impact |
|------|----------|--------|--------|
| Global Quick Search | A1 | Medium | High |
| Virtual Scrolling | C1 | High | High |
| Inline Editing | C3 | Medium | High |
| Bulk Operations | C4 | Medium | High |
| Spreadsheet Gradebook | E1 | High | High |
| One-Click Attendance | F1 | Medium | High |
| Keyboard Shortcuts | L4 | Medium | High |
| Design Token System | M1 | Medium | High |

### High (Should Have)
| Item | Category | Effort | Impact |
|------|----------|--------|--------|
| Sidebar Navigation | A4 | High | Medium |
| Role-Based Dashboards | B1 | High | High |
| Advanced Filtering | C2 | Medium | Medium |
| Context Menus | C6 | Low | Medium |
| Smart Validation | D1 | Medium | Medium |
| Teacher Certification UI | K1 | Medium | High |
| Health Services UI | K4 | Medium | High |

### Medium (Could Have)
| Item | Category | Effort | Impact |
|------|----------|--------|--------|
| Favorites System | A2 | Medium | Medium |
| Breadcrumbs | A3 | Low | Medium |
| Report Builder | H1 | High | Medium |
| In-App Messaging | I2 | High | Medium |
| IEP Timeline | J1 | Medium | Medium |
| Responsive Layouts | M4 | High | Medium |

### Low (Nice to Have)
| Item | Category | Effort | Impact |
|------|----------|--------|--------|
| Attendance Kiosk | F2 | Medium | Low |
| Transportation UI | K3 | High | Low |
| Onboarding Wizard | O1 | Medium | Low |
| In-App Feedback | O3 | Low | Low |

---

## Part 6: Technical Implementation Notes

### JavaFX Component Patterns

#### Virtual ScrollPane Implementation
```java
// Use JavaFX VirtualFlow for large lists
public class VirtualTableView<T> extends TableView<T> {
    private final ObservableList<T> allItems;
    private final int pageSize = 50;

    public void enableVirtualScroll() {
        // Implement lazy loading with scroll listener
    }
}
```

#### Design Token CSS Variables
```css
/* Design tokens in main-style.css */
.root {
    /* Colors */
    --color-primary: #2196F3;
    --color-primary-dark: #1976D2;
    --color-secondary: #FF9800;
    --color-success: #4CAF50;
    --color-warning: #FFC107;
    --color-danger: #F44336;
    --color-background: #FAFAFA;
    --color-surface: #FFFFFF;
    --color-text-primary: #212121;
    --color-text-secondary: #757575;

    /* Typography */
    --font-family: 'Segoe UI', sans-serif;
    --font-size-xs: 10px;
    --font-size-sm: 12px;
    --font-size-md: 14px;
    --font-size-lg: 18px;
    --font-size-xl: 24px;

    /* Spacing */
    --spacing-xs: 4px;
    --spacing-sm: 8px;
    --spacing-md: 16px;
    --spacing-lg: 24px;
    --spacing-xl: 32px;

    /* Borders */
    --border-radius-sm: 4px;
    --border-radius-md: 8px;
    --border-radius-lg: 12px;
}
```

#### Keyboard Shortcut Manager
```java
public class KeyboardShortcutManager {
    private static final Map<KeyCombination, Runnable> shortcuts = new HashMap<>();

    public static void register(KeyCombination combo, Runnable action) {
        shortcuts.put(combo, action);
    }

    public static void handleKeyEvent(KeyEvent event) {
        shortcuts.forEach((combo, action) -> {
            if (combo.match(event)) action.run();
        });
    }
}
```

---

## Part 7: Success Metrics

### User Experience KPIs
- **Clicks to complete task:** Reduce by 40%
- **Time to find information:** Reduce by 50%
- **Training time for new users:** Reduce by 30%
- **User satisfaction score:** Increase to 4.5/5

### Performance KPIs
- **Table load time (10k records):** < 100ms
- **Search response time:** < 200ms
- **Application startup:** < 3 seconds
- **Memory usage:** < 500MB typical

### Accessibility KPIs
- **WCAG 2.1 AA compliance:** 100%
- **Keyboard-only navigation:** All features accessible
- **Screen reader compatibility:** Full support

---

## Part 8: Research Sources

### Competitor Analysis
- [PowerSchool SIS Reviews - G2](https://www.g2.com/products/powerschool-sis/reviews)
- [PowerSchool Enhanced UI Documentation](https://ps.powerschool-docs.com/pssis-admin/latest/enhanced-ui)
- [Infinite Campus Reviews - Capterra](https://www.capterra.com/p/188836/Infinite-Campus/reviews/)
- [Skyward Reviews - Software Advice](https://www.softwareadvice.com/k-12/student-management-suite-profile/)
- [K-12 SIS Comparison - PeerSpot](https://www.peerspot.com/categories/k-12-student-information-systems-sis)

### Design Best Practices
- [Dashboard UI/UX Design Principles 2025](https://fuselabcreative.com/top-dashboard-design-trends-2025/)
- [Common SIS Problems & Solutions - FACTS](https://factsmgt.com/blog/6-common-problems-the-right-sis-can-solve/)
- [Best Student Information Systems 2025](https://www.edisonos.com/blog/best-student-information-systems)

### Industry Standards
- [FERPA Compliance Guidelines](https://studentprivacy.ed.gov/)
- [WCAG 2.1 Accessibility Standards](https://www.w3.org/WAI/WCAG21/quickref/)

---

## Appendix A: File Inventory for Modification

### Core Files to Modify
- `MainWindow.fxml` - Add sidebar navigation
- `main-style.css` - Add design tokens
- `dashboard-view.fxml` - Role-based redesign
- `Gradebook.fxml` - Spreadsheet layout
- All 180+ FXML files - Apply design tokens

### New Files to Create
- `SidebarNavigation.fxml`
- `VirtualTableView.java`
- `BulkOperationsToolbar.fxml`
- `SpreadsheetGradebook.fxml`
- `OneClickAttendance.fxml`
- `TeacherCertificationManagement.fxml`
- `HealthServicesDashboard.fxml`
- `TransportationManagement.fxml`
- `ReportBuilder.fxml`
- `OnboardingWizard.fxml`
- `high-contrast-theme.css`
- `KeyboardShortcutManager.java`
- `AccessibilityService.java`

### Services to Create
- `FavoritesService.java`
- `FilterPersistenceService.java`
- `WidgetManager.java`
- `ReportSchedulerService.java`
- `InAppMessagingService.java`

---

*Document Version: 1.0*
*Created: January 2026*
*Based on: Heronix-SIS Codebase Analysis + Competitor Research*
