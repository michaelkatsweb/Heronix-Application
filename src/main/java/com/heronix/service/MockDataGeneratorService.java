package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.enums.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Mock Data Generator Service
 * Generates comprehensive test data for the Heronix Attendance System.
 *
 * Features:
 * - 500 students across grades 9-12
 * - 15 teachers with various certifications
 * - Core and elective courses for high school
 * - Course sections with teacher/room/period assignments
 * - Bell schedule with 7 periods
 * - Student course enrollments
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 2026 - Attendance System Enhancement
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MockDataGeneratorService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final RoomRepository roomRepository;
    private final BellScheduleRepository bellScheduleRepository;
    private final PeriodTimerRepository periodTimerRepository;

    // Data arrays for realistic data generation
    private static final String[] FIRST_NAMES_MALE = {
        "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph",
        "Thomas", "Christopher", "Daniel", "Matthew", "Anthony", "Mark", "Donald",
        "Steven", "Paul", "Andrew", "Joshua", "Kenneth", "Kevin", "Brian", "George",
        "Timothy", "Ronald", "Jason", "Edward", "Jeffrey", "Ryan", "Jacob", "Gary",
        "Nicholas", "Eric", "Jonathan", "Stephen", "Larry", "Justin", "Scott", "Brandon"
    };

    private static final String[] FIRST_NAMES_FEMALE = {
        "Mary", "Patricia", "Jennifer", "Linda", "Barbara", "Elizabeth", "Susan", "Jessica",
        "Sarah", "Karen", "Lisa", "Nancy", "Betty", "Margaret", "Sandra", "Ashley",
        "Kimberly", "Emily", "Donna", "Michelle", "Dorothy", "Carol", "Amanda", "Melissa",
        "Deborah", "Stephanie", "Rebecca", "Sharon", "Laura", "Cynthia", "Kathleen", "Amy",
        "Angela", "Shirley", "Anna", "Brenda", "Pamela", "Emma", "Nicole", "Helen"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
        "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
        "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell"
    };

    private static final String[] DEPARTMENTS = {
        "Mathematics", "English", "Science", "Social Studies", "Physical Education",
        "Fine Arts", "Foreign Language", "Career & Technical Education", "Special Education"
    };

    private final Random random = new Random(42); // Fixed seed for reproducibility

    /**
     * Generate all mock data for the attendance system
     */
    @Transactional
    public Map<String, Object> generateAllMockData() {
        log.info("=".repeat(80));
        log.info("STARTING MOCK DATA GENERATION FOR ATTENDANCE SYSTEM");
        log.info("=".repeat(80));

        Map<String, Object> results = new LinkedHashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Generate Bell Schedule
            log.info("Step 1: Generating Bell Schedule...");
            BellSchedule bellSchedule = generateBellSchedule();
            results.put("bellSchedule", bellSchedule != null ? "Created: " + bellSchedule.getName() : "Failed");

            // Step 2: Generate Rooms
            log.info("Step 2: Generating Rooms...");
            List<Room> rooms = generateRooms();
            results.put("roomsCreated", rooms.size());

            // Step 3: Generate Teachers
            log.info("Step 3: Generating Teachers...");
            List<Teacher> teachers = generateTeachers();
            results.put("teachersCreated", teachers.size());

            // Step 4: Generate Courses
            log.info("Step 4: Generating Courses...");
            List<Course> courses = generateCourses();
            results.put("coursesCreated", courses.size());

            // Step 5: Generate Course Sections
            log.info("Step 5: Generating Course Sections...");
            List<CourseSection> sections = generateCourseSections(courses, teachers, rooms);
            results.put("sectionsCreated", sections.size());

            // Step 6: Generate Students
            log.info("Step 6: Generating 500 Students...");
            List<Student> students = generateStudents(500);
            results.put("studentsCreated", students.size());

            // Step 7: Enroll Students in Courses
            log.info("Step 7: Enrolling Students in Courses...");
            int enrollments = enrollStudentsInCourses(students, courses);
            results.put("enrollmentsCreated", enrollments);

            long endTime = System.currentTimeMillis();
            results.put("totalTimeMs", endTime - startTime);
            results.put("status", "SUCCESS");

            log.info("=".repeat(80));
            log.info("MOCK DATA GENERATION COMPLETE");
            log.info("Total time: {} ms", endTime - startTime);
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("Error generating mock data: {}", e.getMessage(), e);
            results.put("status", "FAILED");
            results.put("error", e.getMessage());
        }

        return results;
    }

    /**
     * Generate the bell schedule with 7 periods
     */
    @Transactional
    public BellSchedule generateBellSchedule() {
        // Check if bell schedule already exists
        List<BellSchedule> existing = bellScheduleRepository.findAll();
        if (!existing.isEmpty()) {
            log.info("Bell schedule already exists, skipping...");
            return existing.get(0);
        }

        BellSchedule schedule = BellSchedule.builder()
                .name("Regular School Day")
                .description("Standard 7-period school day schedule for high school")
                .scheduleType(BellSchedule.ScheduleType.REGULAR)
                .isDefault(true)
                .active(true)
                .daysOfWeek("MON,TUE,WED,THU,FRI")
                .build();

        schedule = bellScheduleRepository.save(schedule);

        // Define period times (50-minute periods with 5-minute passing)
        LocalTime[][] periodTimes = {
                {LocalTime.of(7, 45), LocalTime.of(8, 35)},   // Period 1: 7:45 - 8:35
                {LocalTime.of(8, 40), LocalTime.of(9, 30)},   // Period 2: 8:40 - 9:30
                {LocalTime.of(9, 35), LocalTime.of(10, 25)},  // Period 3: 9:35 - 10:25
                {LocalTime.of(10, 30), LocalTime.of(11, 20)}, // Period 4: 10:30 - 11:20
                {LocalTime.of(11, 25), LocalTime.of(12, 15)}, // Period 5 (Lunch A): 11:25 - 12:15
                {LocalTime.of(12, 20), LocalTime.of(13, 10)}, // Period 6: 12:20 - 1:10 PM
                {LocalTime.of(13, 15), LocalTime.of(14, 5)}   // Period 7: 1:15 - 2:05 PM
        };

        for (int i = 0; i < periodTimes.length; i++) {
            PeriodTimer period = PeriodTimer.builder()
                    .periodNumber(i + 1)
                    .periodName("Period " + (i + 1))
                    .startTime(periodTimes[i][0])
                    .endTime(periodTimes[i][1])
                    .attendanceWindowMinutes(10)
                    .autoMarkAbsent(true)
                    .daysOfWeek("MON,TUE,WED,THU,FRI")
                    .active(true)
                    .build();

            periodTimerRepository.save(period);
            schedule.addPeriod(period);
        }

        schedule = bellScheduleRepository.save(schedule);
        log.info("Created bell schedule with {} periods", schedule.getPeriodCount());
        return schedule;
    }

    /**
     * Generate rooms for the school
     */
    @Transactional
    public List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();

        // Check if rooms already exist
        if (roomRepository.count() > 0) {
            log.info("Rooms already exist, skipping...");
            return roomRepository.findAll();
        }

        // Regular classrooms (100-199 = Math, 200-299 = English, etc.)
        String[][] roomConfigs = {
                // Number prefix, type, department, count
                {"100", "STANDARD", "Mathematics", "8"},
                {"200", "STANDARD", "English", "8"},
                {"300", "SCIENCE_LAB", "Science", "6"},
                {"400", "STANDARD", "Social Studies", "6"},
                {"500", "COMPUTER_LAB", "Career & Tech", "4"},
                {"600", "STANDARD", "Foreign Language", "4"},
                {"700", "ART_STUDIO", "Fine Arts", "3"},
                {"800", "GYMNASIUM", "Physical Education", "2"}
        };

        for (String[] config : roomConfigs) {
            int prefix = Integer.parseInt(config[0]);
            RoomType roomType = RoomType.valueOf(config[1]);
            String department = config[2];
            int count = Integer.parseInt(config[3]);

            for (int i = 1; i <= count; i++) {
                String roomNumber = String.valueOf(prefix + i);
                Room room = new Room();
                room.setRoomNumber(roomNumber);
                room.setCapacity(30);
                room.setType(roomType);
                room.setBuilding("Main Building");
                room.setFloor(prefix / 100);
                room.setActive(true);
                room.setZone(department); // Use zone for department

                // Set equipment based on room type
                if (roomType == RoomType.SCIENCE_LAB) {
                    room.setHasProjector(true);
                    room.setHasSmartboard(true);
                } else if (roomType == RoomType.COMPUTER_LAB) {
                    room.setHasProjector(true);
                    room.setHasSmartboard(true);
                    room.setHasComputers(true);
                }

                rooms.add(roomRepository.save(room));
            }
        }

        log.info("Created {} rooms", rooms.size());
        return rooms;
    }

    /**
     * Generate 15 teachers with various certifications
     */
    @Transactional
    public List<Teacher> generateTeachers() {
        List<Teacher> teachers = new ArrayList<>();

        // Check if teachers already exist
        if (teacherRepository.count() >= 15) {
            log.info("Teachers already exist, skipping...");
            return teacherRepository.findAll();
        }

        String[][] teacherConfigs = {
                // First, Last, Department, Email prefix
                {"Maria", "Rodriguez", "Mathematics", "mrodriguez"},
                {"James", "Thompson", "Mathematics", "jthompson"},
                {"Sarah", "Williams", "English", "swilliams"},
                {"Michael", "Davis", "English", "mdavis"},
                {"Jennifer", "Martinez", "Science", "jmartinez"},
                {"Robert", "Johnson", "Science", "rjohnson"},
                {"Lisa", "Anderson", "Social Studies", "landerson"},
                {"David", "Wilson", "Social Studies", "dwilson"},
                {"Emily", "Brown", "Foreign Language", "ebrown"},
                {"Christopher", "Garcia", "Foreign Language", "cgarcia"},
                {"Amanda", "Miller", "Fine Arts", "amiller"},
                {"Daniel", "Taylor", "Fine Arts", "dtaylor"},
                {"Jessica", "Moore", "Physical Education", "jmoore"},
                {"Kevin", "Lee", "Physical Education", "klee"},
                {"Michelle", "White", "Career & Technical Education", "mwhite"}
        };

        for (int i = 0; i < teacherConfigs.length; i++) {
            String[] config = teacherConfigs[i];
            String employeeId = "T" + String.format("%05d", i + 1);

            // Skip if already exists
            if (teacherRepository.existsByEmployeeId(employeeId)) {
                continue;
            }

            Teacher teacher = new Teacher();
            teacher.setFirstName(config[0]);
            teacher.setLastName(config[1]);
            teacher.setName(config[0] + " " + config[1]);
            teacher.setDepartment(config[2]);
            teacher.setEmail(config[3] + "@heronix.edu");
            teacher.setEmployeeId(employeeId);
            teacher.setActive(true);
            teacher.setPlanningPeriod(random.nextInt(7) + 1); // Random planning period 1-7
            teacher.setMaxPeriodsPerDay(6);
            teacher.setYearsOfExperience(random.nextInt(20) + 1);

            // Add certifications based on department
            List<String> certs = new ArrayList<>();
            certs.add(config[2] + " 9-12");
            if (random.nextBoolean()) {
                certs.add("ESL Endorsement");
            }
            teacher.setCertifications(certs);

            teachers.add(teacherRepository.save(teacher));
        }

        log.info("Created {} teachers", teachers.size());
        return teachers;
    }

    /**
     * Generate courses for high school
     */
    @Transactional
    public List<Course> generateCourses() {
        List<Course> courses = new ArrayList<>();

        // Check if courses already exist
        if (courseRepository.count() >= 30) {
            log.info("Courses already exist, skipping...");
            return courseRepository.findAll();
        }

        // Core courses - required for all grade levels
        String[][] coreCourses = {
                // Code, Name, Subject, Min Grade, Max Grade, Sessions/Week
                {"ENG101", "English 9", "English", "9", "9", "6"},
                {"ENG102", "English 10", "English", "10", "10", "6"},
                {"ENG103", "English 11", "English", "11", "11", "6"},
                {"ENG104", "English 12", "English", "12", "12", "6"},
                {"MAT101", "Algebra I", "Mathematics", "9", "10", "6"},
                {"MAT102", "Geometry", "Mathematics", "9", "11", "6"},
                {"MAT103", "Algebra II", "Mathematics", "10", "12", "6"},
                {"MAT104", "Pre-Calculus", "Mathematics", "11", "12", "6"},
                {"SCI101", "Biology", "Science", "9", "10", "6"},
                {"SCI102", "Chemistry", "Science", "10", "11", "6"},
                {"SCI103", "Physics", "Science", "11", "12", "6"},
                {"SOC101", "World History", "Social Studies", "9", "10", "6"},
                {"SOC102", "US History", "Social Studies", "10", "11", "6"},
                {"SOC103", "Government", "Social Studies", "11", "12", "6"},
                {"SOC104", "Economics", "Social Studies", "11", "12", "6"}
        };

        // Elective courses
        String[][] electiveCourses = {
                {"ART101", "Art I", "Fine Arts", "9", "12", "6"},
                {"ART102", "Art II", "Fine Arts", "10", "12", "6"},
                {"MUS101", "Band", "Fine Arts", "9", "12", "6"},
                {"MUS102", "Choir", "Fine Arts", "9", "12", "6"},
                {"SPA101", "Spanish I", "Foreign Language", "9", "12", "6"},
                {"SPA102", "Spanish II", "Foreign Language", "10", "12", "6"},
                {"FRE101", "French I", "Foreign Language", "9", "12", "6"},
                {"PE101", "Physical Education", "Physical Education", "9", "12", "6"},
                {"PE102", "Health", "Physical Education", "9", "10", "3"},
                {"CTE101", "Computer Science I", "Career & Technical Education", "9", "12", "6"},
                {"CTE102", "Computer Science II", "Career & Technical Education", "10", "12", "6"},
                {"CTE103", "Business Fundamentals", "Career & Technical Education", "10", "12", "6"}
        };

        // Create core courses
        for (String[] config : coreCourses) {
            Course course = createCourse(config, CourseCategory.CORE);
            if (course != null) {
                courses.add(course);
            }
        }

        // Create elective courses
        for (String[] config : electiveCourses) {
            Course course = createCourse(config, CourseCategory.ELECTIVE);
            if (course != null) {
                courses.add(course);
            }
        }

        log.info("Created {} courses", courses.size());
        return courses;
    }

    private Course createCourse(String[] config, CourseCategory category) {
        String courseCode = config[0];

        // Skip if already exists
        if (courseRepository.findByCourseCode(courseCode).isPresent()) {
            return null;
        }

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(config[1]);
        course.setSubject(config[2]);
        course.setMinGradeLevel(Integer.parseInt(config[3]));
        course.setMaxGradeLevel(Integer.parseInt(config[4]));
        course.setSessionsPerWeek(Integer.parseInt(config[5]));
        course.setCourseCategory(category);
        course.setDurationMinutes(50);
        course.setMinStudents(15);
        course.setOptimalStudents(25);
        course.setMaxStudents(30);
        course.setCredits(1.0);
        course.setActive(true);
        course.setIsCoreRequired(category == CourseCategory.CORE);

        return courseRepository.save(course);
    }

    /**
     * Generate course sections for each course
     */
    @Transactional
    public List<CourseSection> generateCourseSections(List<Course> courses, List<Teacher> teachers, List<Room> rooms) {
        List<CourseSection> sections = new ArrayList<>();

        // Group teachers by department
        Map<String, List<Teacher>> teachersByDept = teachers.stream()
                .collect(Collectors.groupingBy(Teacher::getDepartment));

        // Group rooms by type
        Map<String, List<Room>> roomsBySubject = new HashMap<>();
        for (Room room : rooms) {
            String subject = getSubjectFromRoomNumber(room.getRoomNumber());
            roomsBySubject.computeIfAbsent(subject, k -> new ArrayList<>()).add(room);
        }

        for (Course course : courses) {
            // Determine number of sections (more for core courses)
            int numSections = course.getCourseCategory() == CourseCategory.CORE ? 4 : 2;

            // Get appropriate teachers
            List<Teacher> deptTeachers = teachersByDept.getOrDefault(course.getSubject(), new ArrayList<>());
            if (deptTeachers.isEmpty()) {
                deptTeachers = new ArrayList<>(teachers);
            }

            // Get appropriate rooms
            List<Room> deptRooms = roomsBySubject.getOrDefault(course.getSubject(), new ArrayList<>());
            if (deptRooms.isEmpty()) {
                deptRooms = new ArrayList<>(rooms);
            }

            for (int sectionNum = 1; sectionNum <= numSections; sectionNum++) {
                CourseSection section = new CourseSection();
                section.setCourse(course);
                section.setSectionNumber(String.valueOf(sectionNum));
                section.setMaxEnrollment(30);
                section.setMinEnrollment(15);
                section.setTargetEnrollment(25);
                section.setCurrentEnrollment(0);
                section.setSectionStatus(CourseSection.SectionStatus.OPEN);

                // Assign teacher (rotate through available teachers)
                Teacher assignedTeacher = deptTeachers.get(sectionNum % deptTeachers.size());
                section.setAssignedTeacher(assignedTeacher);

                // Assign room
                Room assignedRoom = deptRooms.get(sectionNum % deptRooms.size());
                section.setAssignedRoom(assignedRoom);

                // Assign period (spread sections across periods, avoiding teacher's planning period)
                int period = (sectionNum % 7) + 1;
                if (assignedTeacher.getPlanningPeriod() != null && period == assignedTeacher.getPlanningPeriod()) {
                    period = (period % 7) + 1;
                }
                section.setAssignedPeriod(period);

                sections.add(courseSectionRepository.save(section));
            }
        }

        log.info("Created {} course sections", sections.size());
        return sections;
    }

    private String getSubjectFromRoomNumber(String roomNumber) {
        int prefix = Integer.parseInt(roomNumber.substring(0, 1));
        switch (prefix) {
            case 1: return "Mathematics";
            case 2: return "English";
            case 3: return "Science";
            case 4: return "Social Studies";
            case 5: return "Career & Technical Education";
            case 6: return "Foreign Language";
            case 7: return "Fine Arts";
            case 8: return "Physical Education";
            default: return "General";
        }
    }

    /**
     * Generate 500 students across grades 9-12
     */
    @Transactional
    public List<Student> generateStudents(int count) {
        List<Student> students = new ArrayList<>();

        // Check how many students already exist
        long existingCount = studentRepository.count();
        if (existingCount >= count) {
            log.info("Students already exist ({}), skipping...", existingCount);
            return studentRepository.findAll();
        }

        // Distribution: 9th=30%, 10th=28%, 11th=24%, 12th=18%
        int[] gradeDistribution = {150, 140, 120, 90}; // Total = 500
        String[] grades = {"9", "10", "11", "12"};

        int studentCounter = (int) existingCount + 1;

        for (int gradeIdx = 0; gradeIdx < grades.length; gradeIdx++) {
            String gradeLevel = grades[gradeIdx];
            int studentsInGrade = gradeDistribution[gradeIdx];

            for (int i = 0; i < studentsInGrade; i++) {
                String studentId = "S" + String.format("%06d", studentCounter);

                // Skip if already exists
                if (studentRepository.existsByStudentId(studentId)) {
                    studentCounter++;
                    continue;
                }

                boolean isMale = random.nextBoolean();
                String firstName = isMale
                        ? FIRST_NAMES_MALE[random.nextInt(FIRST_NAMES_MALE.length)]
                        : FIRST_NAMES_FEMALE[random.nextInt(FIRST_NAMES_FEMALE.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

                Student student = new Student();
                student.setStudentId(studentId);
                student.setFirstName(firstName);
                student.setLastName(lastName);
                student.setGradeLevel(gradeLevel);
                student.setEmail(studentId.toLowerCase() + "@student.heronix.edu");
                student.setActive(true);
                student.setGender(isMale ? "Male" : "Female");

                // Set date of birth based on grade level
                int birthYear = LocalDate.now().getYear() - 14 - Integer.parseInt(gradeLevel);
                student.setDateOfBirth(LocalDate.of(birthYear, random.nextInt(12) + 1, random.nextInt(28) + 1));

                // Set GPA (weighted toward 2.5-3.5 range)
                double gpa = 2.0 + random.nextDouble() * 2.0;
                if (random.nextDouble() < 0.2) {
                    gpa = 3.5 + random.nextDouble() * 0.5; // 20% honor students
                }
                student.setCurrentGPA(Math.round(gpa * 100.0) / 100.0);

                // Set graduation year
                int currentYear = LocalDate.now().getYear();
                student.setGraduationYear(currentYear + (12 - Integer.parseInt(gradeLevel)));

                // Generate QR code ID
                student.setQrCodeId("QR-" + studentId + "-" + UUID.randomUUID().toString().substring(0, 8));
                student.setQrAttendanceEnabled(true);

                // Random special education flags (10% IEP, 5% 504, 5% Gifted)
                if (random.nextDouble() < 0.10) {
                    student.setHasIEP(true);
                }
                if (random.nextDouble() < 0.05) {
                    student.setHas504Plan(true);
                }
                if (random.nextDouble() < 0.05) {
                    student.setIsGifted(true);
                }

                students.add(studentRepository.save(student));
                studentCounter++;
            }
        }

        log.info("Created {} students", students.size());
        return students;
    }

    /**
     * Enroll students in courses based on their grade level
     */
    @Transactional
    public int enrollStudentsInCourses(List<Student> students, List<Course> courses) {
        int totalEnrollments = 0;

        // Group courses by grade level eligibility
        for (Student student : students) {
            int gradeLevel = Integer.parseInt(student.getGradeLevel());

            // Find eligible courses for this student
            List<Course> eligibleCourses = courses.stream()
                    .filter(c -> c.getMinGradeLevel() != null && c.getMinGradeLevel() <= gradeLevel)
                    .filter(c -> c.getMaxGradeLevel() != null && c.getMaxGradeLevel() >= gradeLevel)
                    .collect(Collectors.toList());

            // Enroll in core courses (must have one for each subject)
            Set<String> enrolledSubjects = new HashSet<>();
            List<Course> enrolledCourses = new ArrayList<>();

            // First, enroll in required core courses
            for (Course course : eligibleCourses) {
                if (course.getCourseCategory() == CourseCategory.CORE) {
                    String subject = course.getSubject();
                    if (!enrolledSubjects.contains(subject)) {
                        enrolledCourses.add(course);
                        enrolledSubjects.add(subject);
                    }
                }
            }

            // Then, add 2-3 electives
            List<Course> electiveCourses = eligibleCourses.stream()
                    .filter(c -> c.getCourseCategory() == CourseCategory.ELECTIVE)
                    .collect(Collectors.toList());

            Collections.shuffle(electiveCourses, random);
            int electivesToAdd = 2 + random.nextInt(2); // 2-3 electives
            for (int i = 0; i < Math.min(electivesToAdd, electiveCourses.size()); i++) {
                enrolledCourses.add(electiveCourses.get(i));
            }

            // Update student's enrolled courses
            student.setEnrolledCourses(enrolledCourses);
            studentRepository.save(student);

            // Update course enrollment counts
            for (Course course : enrolledCourses) {
                course.incrementEnrollment();
                courseRepository.save(course);
                totalEnrollments++;
            }
        }

        log.info("Created {} total course enrollments", totalEnrollments);
        return totalEnrollments;
    }

    /**
     * Clear all mock data (for testing/reset)
     */
    @Transactional
    public void clearAllMockData() {
        log.warn("Clearing all mock data...");

        // Clear in reverse dependency order
        studentRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseRepository.deleteAll();
        teacherRepository.deleteAll();
        roomRepository.deleteAll();
        periodTimerRepository.deleteAll();
        bellScheduleRepository.deleteAll();

        log.info("All mock data cleared");
    }
}
