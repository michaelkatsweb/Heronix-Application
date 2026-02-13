package com.heronix.service.integration;

import com.heronix.dto.scheduler.*;
import com.heronix.integration.SchedulerApiClient;
import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.DistrictSettingsService;
import com.heronix.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for syncing data between Heronix-SIS and Heronix-SchedulerV2
 * Handles exporting SIS data to SchedulerV2 for optimization
 *
 * @author Heronix SIS Team
 * @version 1.0.0 - COMPLETE with verified entity mappings
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerSyncService {

    private final SchedulerApiClient schedulerApiClient;
    private final DistrictSettingsService districtSettingsService;

    // Repositories
    private final ScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;
    private final PeriodTimerRepository periodTimerRepository;
    private final LunchPeriodRepository lunchPeriodRepository;
    private final CourseEnrollmentRequestRepository enrollmentRequestRepository;

    // ========================================================================
    // MAIN EXPORT METHOD
    // ========================================================================

    /**
     * Export schedule data to SchedulerV2 for optimization
     *
     * @param scheduleId SIS schedule ID
     * @return Export result with status and IDs
     */
    @Transactional(readOnly = true)
    public SchedulerExportResult exportToScheduler(Long scheduleId) {
        // SchedulerV2 now pulls data from SIS via its own SISApiClient.
        // No data push is needed. This method returns a success stub for backwards compatibility.
        log.info("exportToScheduler called for schedule {} — SchedulerV2 pulls data itself, no push needed", scheduleId);

        return SchedulerExportResult.builder()
                .success(true)
                .scheduleId(scheduleId)
                .exportId(null)
                .importId(null)
                .message("No export needed — SchedulerV2 pulls data from SIS directly")
                .studentsExported(0)
                .coursesExported(0)
                .teachersExported(0)
                .build();
    }

    // ========================================================================
    // PAYLOAD BUILDING
    // ========================================================================

    /**
     * Build complete SchedulerDataPayload from SIS data
     */
    private SchedulerDataPayload buildSchedulerPayload(Schedule schedule) {
        log.debug("Building scheduler payload for schedule: {}", schedule.getScheduleName());

        // Pre-compute course demand for efficient lookup
        Map<Long, Long> courseDemandMap = computeCourseDemand();

        // Build teacher certifications map for qualification lookup
        Map<String, List<Long>> certificationToCourseMap = buildCertificationCourseMap();

        return SchedulerDataPayload.builder()
                .schoolInfo(buildSchoolInfo(schedule))
                .academicConfig(buildAcademicConfig(schedule))
                .studentRequests(buildStudentRequests(schedule))
                .courses(buildCourseCatalog(schedule, courseDemandMap))
                .teachers(buildTeacherAvailability(schedule, certificationToCourseMap))
                .rooms(buildRoomAvailability(schedule))
                .timeSlots(buildTimeSlots(schedule))
                .lunchPeriods(buildLunchPeriods(schedule))
                .constraints(buildConstraintConfig(schedule))
                .preAssignedSections(buildPreAssignedSections(schedule))
                .metadata(buildExportMetadata(schedule))
                .build();
    }

    /**
     * Compute course demand from enrollment requests
     */
    private Map<Long, Long> computeCourseDemand() {
        log.debug("Computing course demand from enrollment requests");

        Map<Long, Long> demandMap = new HashMap<>();

        try {
            List<CourseEnrollmentRequest> allRequests = enrollmentRequestRepository.findAll();
            for (CourseEnrollmentRequest request : allRequests) {
                if (request.getCourse() != null) {
                    Long courseId = request.getCourse().getId();
                    demandMap.merge(courseId, 1L, Long::sum);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to compute course demand: {}", e.getMessage());
        }

        return demandMap;
    }

    /**
     * Build certification to course mapping for teacher qualifications
     */
    private Map<String, List<Long>> buildCertificationCourseMap() {
        log.debug("Building certification to course mapping");

        Map<String, List<Long>> certMap = new HashMap<>();

        // Map common certification areas to subjects
        Map<String, String> certToSubject = new HashMap<>();
        certToSubject.put("MATH", "Math");
        certToSubject.put("MATHEMATICS", "Math");
        certToSubject.put("ENGLISH", "English");
        certToSubject.put("ELA", "English");
        certToSubject.put("SCIENCE", "Science");
        certToSubject.put("HISTORY", "History");
        certToSubject.put("SOCIAL_STUDIES", "Social Studies");
        certToSubject.put("PE", "Physical Education");
        certToSubject.put("PHYSICAL_EDUCATION", "Physical Education");
        certToSubject.put("ART", "Art");
        certToSubject.put("MUSIC", "Music");
        certToSubject.put("FOREIGN_LANGUAGE", "Foreign Language");
        certToSubject.put("SPANISH", "Spanish");
        certToSubject.put("FRENCH", "French");
        certToSubject.put("COMPUTER_SCIENCE", "Computer Science");
        certToSubject.put("SPECIAL_ED", "Special Education");
        certToSubject.put("SPED", "Special Education");

        try {
            List<Course> courses = courseRepository.findAll();

            for (Map.Entry<String, String> entry : certToSubject.entrySet()) {
                String certification = entry.getKey();
                String subject = entry.getValue();

                List<Long> qualifiedCourseIds = courses.stream()
                        .filter(c -> c.getSubject() != null &&
                                c.getSubject().toLowerCase().contains(subject.toLowerCase()))
                        .map(Course::getId)
                        .collect(Collectors.toList());

                if (!qualifiedCourseIds.isEmpty()) {
                    certMap.put(certification, qualifiedCourseIds);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to build certification course map: {}", e.getMessage());
        }

        return certMap;
    }

    /**
     * Build pre-assigned sections (locked assignments from previous schedules)
     */
    private List<PreAssignedSectionDTO> buildPreAssignedSections(Schedule schedule) {
        log.debug("Building pre-assigned sections");

        List<PreAssignedSectionDTO> preAssigned = new ArrayList<>();

        // Look for sections that are marked as locked/pinned
        // This would typically come from a SectionAssignment or similar entity
        // For now, we return empty list - can be extended when the feature is needed

        return preAssigned;
    }

    /**
     * Build school information from district settings
     */
    private SchoolInfoDTO buildSchoolInfo(Schedule schedule) {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

        return SchoolInfoDTO.builder()
                .schoolName(schedule.getScheduleName())
                .districtName(settings.getDistrictNameOrDefault())
                .campusName(settings.getCampusNameOrDefault())
                .build();
    }

    /**
     * Build academic configuration from schedule and district settings
     */
    private AcademicConfigDTO buildAcademicConfig(Schedule schedule) {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();
        int year = schedule.getStartDate().getYear();

        // Get schedule type from schedule entity, fall back to district settings
        String scheduleType = schedule.getScheduleType() != null
                ? schedule.getScheduleType().name()
                : settings.getScheduleTypeOrDefault();

        return AcademicConfigDTO.builder()
                .academicYear(year + "-" + (year + 1))
                .schoolYearStartDate(schedule.getStartDate())
                .schoolYearEndDate(schedule.getEndDate())
                .scheduleType(scheduleType)
                .instructionalDaysPerWeek(settings.getInstructionalDaysPerWeek() != null
                        ? settings.getInstructionalDaysPerWeek() : 5)
                .periodsPerDay(settings.getPeriodsPerDay() != null
                        ? settings.getPeriodsPerDay() : 7)
                .gradingPeriods(buildGradingPeriods(schedule))
                .build();
    }

    /**
     * Build grading periods for academic year
     */
    private List<AcademicConfigDTO.GradingPeriodDTO> buildGradingPeriods(Schedule schedule) {
        List<AcademicConfigDTO.GradingPeriodDTO> gradingPeriods = new ArrayList<>();

        LocalDate startDate = schedule.getStartDate();
        LocalDate endDate = schedule.getEndDate();

        if (startDate == null || endDate == null) {
            return gradingPeriods;
        }

        // Calculate total days and divide into 4 quarters
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        long quarterDays = totalDays / 4;
        int instructionalDaysPerQuarter = (int) (quarterDays * 5 / 7); // Approximate weekdays

        // Quarter 1 (August/September - November)
        gradingPeriods.add(AcademicConfigDTO.GradingPeriodDTO.builder()
                .id(1L)
                .name("Quarter 1")
                .periodType("QUARTER")
                .periodNumber(1)
                .startDate(startDate)
                .endDate(startDate.plusDays(quarterDays))
                .instructionalDays(instructionalDaysPerQuarter)
                .build());

        // Quarter 2 / Semester 1 End (November - January)
        LocalDate q2Start = startDate.plusDays(quarterDays + 1);
        LocalDate q2End = startDate.plusDays(quarterDays * 2);
        gradingPeriods.add(AcademicConfigDTO.GradingPeriodDTO.builder()
                .id(2L)
                .name("Quarter 2")
                .periodType("QUARTER")
                .periodNumber(2)
                .startDate(q2Start)
                .endDate(q2End)
                .instructionalDays(instructionalDaysPerQuarter)
                .build());

        // Quarter 3 (January - March/April)
        LocalDate q3Start = q2End.plusDays(1);
        LocalDate q3End = startDate.plusDays(quarterDays * 3);
        gradingPeriods.add(AcademicConfigDTO.GradingPeriodDTO.builder()
                .id(3L)
                .name("Quarter 3")
                .periodType("QUARTER")
                .periodNumber(3)
                .startDate(q3Start)
                .endDate(q3End)
                .instructionalDays(instructionalDaysPerQuarter)
                .build());

        // Quarter 4 / Year End (March/April - June)
        gradingPeriods.add(AcademicConfigDTO.GradingPeriodDTO.builder()
                .id(4L)
                .name("Quarter 4")
                .periodType("QUARTER")
                .periodNumber(4)
                .startDate(q3End.plusDays(1))
                .endDate(endDate)
                .instructionalDays(instructionalDaysPerQuarter)
                .build());

        return gradingPeriods;
    }

    /**
     * Build student requests from enrollment requests
     */
    private List<StudentRequestDTO> buildStudentRequests(Schedule schedule) {
        log.debug("Building student requests");

        List<Student> students = studentRepository.findAll();

        return students.stream()
                .filter(Student::isActive)
                .map(student -> {
                    List<CourseEnrollmentRequest> requests = enrollmentRequestRepository
                            .findByStudentId(student.getId());

                    List<StudentRequestDTO.CourseRequestEntry> courseRequests = requests.stream()
                            .map(req -> StudentRequestDTO.CourseRequestEntry.builder()
                                    .courseId(req.getCourse().getId())
                                    .courseCode(req.getCourse().getCourseCode())
                                    .courseName(req.getCourse().getCourseName())
                                    .preferenceRank(req.getPreferenceRank() != null ? req.getPreferenceRank() : 5)
                                    .priorityScore(req.getPriorityScore() != null ? req.getPriorityScore() : 500)
                                    .isRequired(req.getCourse().getIsCoreRequired() != null && req.getCourse().getIsCoreRequired())
                                    .isAlternate(req.getPreferenceRank() != null && req.getPreferenceRank() == 4)
                                    .primaryCourseId(null) // Could be implemented by finding primary course
                                    .build())
                            .collect(Collectors.toList());

                    return StudentRequestDTO.builder()
                            .studentId(student.getId())
                            .studentNumber(student.getStudentId())
                            .firstName(student.getFirstName())
                            .lastName(student.getLastName())
                            .gradeLevel(student.getGradeLevel() != null ? Integer.parseInt(student.getGradeLevel()) : 9)
                            .assignedLunchPeriod(null)
                            .hasIEP(student.getHasIEP() != null && student.getHasIEP())
                            .has504Plan(student.getHas504Plan() != null && student.getHas504Plan())
                            .specialAccommodations(new ArrayList<>())
                            .completedCourseIds(new ArrayList<>())
                            .courseRequests(courseRequests)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Build course catalog with demand calculation
     */
    private List<CourseCatalogDTO> buildCourseCatalog(Schedule schedule, Map<Long, Long> courseDemandMap) {
        log.debug("Building course catalog");

        List<Course> courses = courseRepository.findAll();

        return courses.stream()
                .filter(Course::isActive)
                .map(course -> {
                    // Calculate demand and sections needed
                    long demand = courseDemandMap.getOrDefault(course.getId(), 0L);
                    int maxPerSection = course.getMaxStudents() != null ? course.getMaxStudents() : 30;
                    int sectionsNeeded = calculateSectionsNeeded(demand, maxPerSection);

                    // Determine if advanced course (AP, Honors, IB)
                    boolean isAdvanced = isAdvancedCourse(course);

                    // Determine if special education course
                    boolean isSpecialEd = isSpecialEducationCourse(course);

                    // Get required certifications based on subject
                    List<String> requiredCerts = getRequiredCertifications(course);

                    // Parse equipment requirements
                    List<String> requiredEquipment = parseEquipmentList(course);

                    // Get prerequisite course IDs
                    List<Long> prerequisiteIds = getPrerequisiteCourseIds(course);

                    return CourseCatalogDTO.builder()
                            .courseId(course.getId())
                            .courseCode(course.getCourseCode())
                            .courseName(course.getCourseName())
                            .department(course.getSubject() != null ? course.getSubject() : "GENERAL")
                            .subjectArea(course.getSubject())
                            .gradeLevel(determineGradeLevel(course))
                            .credits(course.getCredits() != null ? course.getCredits().doubleValue() : 1.0)
                            .sectionsNeeded(sectionsNeeded)
                            .maxStudentsPerSection(maxPerSection)
                            .minStudentsPerSection(course.getMinStudents() != null ? course.getMinStudents() : 15)
                            .totalDemand((int) demand)
                            .requiredCertifications(requiredCerts)
                            .requiredRoomTypes(course.getRequiredRoomType() != null ?
                                    List.of(course.getRequiredRoomType().name()) : new ArrayList<>())
                            .requiredEquipment(requiredEquipment)
                            .periodsRequired(course.getSessionsPerWeek() != null ? course.getSessionsPerWeek() : 5)
                            .allowDuringLunch(false)
                            .isAdvanced(isAdvanced)
                            .isSpecialEducation(isSpecialEd)
                            .prerequisiteCourseIds(prerequisiteIds)
                            .priority(calculateCoursePriority(course, isAdvanced, demand))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate number of sections needed based on demand
     */
    private int calculateSectionsNeeded(long demand, int maxPerSection) {
        if (demand == 0) return 1; // At least 1 section for active courses
        return (int) Math.ceil((double) demand / maxPerSection);
    }

    /**
     * Determine if course is an advanced course (AP, Honors, IB)
     */
    private boolean isAdvancedCourse(Course course) {
        String name = course.getCourseName() != null ? course.getCourseName().toUpperCase() : "";
        String code = course.getCourseCode() != null ? course.getCourseCode().toUpperCase() : "";

        return name.contains("AP ") || name.contains("HONORS") || name.contains("IB ") ||
               name.contains("ADVANCED PLACEMENT") || name.contains("ACCELERATED") ||
               code.startsWith("AP") || code.contains("HON") || code.contains("ADV");
    }

    /**
     * Determine if course is a special education course
     */
    private boolean isSpecialEducationCourse(Course course) {
        String name = course.getCourseName() != null ? course.getCourseName().toUpperCase() : "";
        String code = course.getCourseCode() != null ? course.getCourseCode().toUpperCase() : "";
        String subject = course.getSubject() != null ? course.getSubject().toUpperCase() : "";

        return name.contains("SPECIAL ED") || name.contains("SPED") || name.contains("RESOURCE") ||
               name.contains("INCLUSION") || name.contains("LIFE SKILLS") ||
               code.contains("SPED") || code.contains("SE") ||
               subject.contains("SPECIAL");
    }

    /**
     * Get required certifications based on course subject and type
     */
    private List<String> getRequiredCertifications(Course course) {
        List<String> certs = new ArrayList<>();

        String subject = course.getSubject() != null ? course.getSubject().toUpperCase() : "";

        // Map subject to certification requirements
        if (subject.contains("MATH")) certs.add("MATHEMATICS");
        if (subject.contains("ENGLISH") || subject.contains("ELA")) certs.add("ENGLISH");
        if (subject.contains("SCIENCE")) certs.add("SCIENCE");
        if (subject.contains("HISTORY") || subject.contains("SOCIAL")) certs.add("SOCIAL_STUDIES");
        if (subject.contains("PE") || subject.contains("PHYSICAL")) certs.add("PHYSICAL_EDUCATION");
        if (subject.contains("ART")) certs.add("ART");
        if (subject.contains("MUSIC")) certs.add("MUSIC");
        if (subject.contains("SPANISH")) certs.add("SPANISH");
        if (subject.contains("FRENCH")) certs.add("FRENCH");
        if (subject.contains("COMPUTER")) certs.add("COMPUTER_SCIENCE");

        // Special education courses require SPED certification
        if (isSpecialEducationCourse(course)) {
            certs.add("SPECIAL_ED");
        }

        return certs;
    }

    /**
     * Parse equipment list from course
     */
    private List<String> parseEquipmentList(Course course) {
        List<String> equipment = new ArrayList<>();

        // Check course description or equipment field for common equipment needs
        String name = course.getCourseName() != null ? course.getCourseName().toUpperCase() : "";

        if (name.contains("COMPUTER") || name.contains("PROGRAMMING") || name.contains("CODING")) {
            equipment.add("COMPUTERS");
        }
        if (name.contains("LAB") || name.contains("CHEMISTRY") || name.contains("BIOLOGY") || name.contains("PHYSICS")) {
            equipment.add("LAB_EQUIPMENT");
            equipment.add("SAFETY_EQUIPMENT");
        }
        if (name.contains("ART") || name.contains("STUDIO")) {
            equipment.add("ART_SUPPLIES");
        }
        if (name.contains("MUSIC") || name.contains("BAND") || name.contains("ORCHESTRA") || name.contains("CHOIR")) {
            equipment.add("MUSICAL_INSTRUMENTS");
        }
        if (name.contains("PE") || name.contains("PHYSICAL ED") || name.contains("ATHLETICS")) {
            equipment.add("SPORTS_EQUIPMENT");
        }
        if (name.contains("CULINARY") || name.contains("COOKING") || name.contains("FOODS")) {
            equipment.add("KITCHEN_EQUIPMENT");
        }
        if (name.contains("WOOD") || name.contains("SHOP") || name.contains("MANUFACTURING")) {
            equipment.add("WORKSHOP_EQUIPMENT");
        }

        return equipment;
    }

    /**
     * Get prerequisite course IDs for a course
     */
    private List<Long> getPrerequisiteCourseIds(Course course) {
        List<Long> prereqIds = new ArrayList<>();

        // Check for common prerequisite patterns in course names
        String name = course.getCourseName() != null ? course.getCourseName().toUpperCase() : "";

        // For courses with number indicators (e.g., "Algebra 2" requires "Algebra 1")
        if (name.contains("2") || name.contains("II")) {
            // Find course with "1" or "I" in same subject
            String baseName = name.replaceAll("\\s*(2|II)\\s*", "").trim();
            courseRepository.findAll().stream()
                    .filter(c -> {
                        String cName = c.getCourseName() != null ? c.getCourseName().toUpperCase() : "";
                        return (cName.contains("1") || cName.contains(" I ") || cName.endsWith(" I")) &&
                               cName.startsWith(baseName.substring(0, Math.min(baseName.length(), 5)));
                    })
                    .findFirst()
                    .ifPresent(prereq -> prereqIds.add(prereq.getId()));
        }

        // AP courses often require the standard version
        if (name.startsWith("AP ")) {
            String standardName = name.replace("AP ", "").trim();
            courseRepository.findAll().stream()
                    .filter(c -> {
                        String cName = c.getCourseName() != null ? c.getCourseName().toUpperCase() : "";
                        return cName.contains(standardName) && !cName.contains("AP");
                    })
                    .findFirst()
                    .ifPresent(prereq -> prereqIds.add(prereq.getId()));
        }

        return prereqIds;
    }

    /**
     * Calculate course scheduling priority
     */
    private int calculateCoursePriority(Course course, boolean isAdvanced, long demand) {
        int priority = 5; // Default priority

        // Core/required courses get higher priority
        if (course.getIsCoreRequired() != null && course.getIsCoreRequired()) {
            priority = 1;
        }
        // Advanced courses get high priority
        else if (isAdvanced) {
            priority = 2;
        }
        // High demand courses get medium-high priority
        else if (demand > 100) {
            priority = 3;
        }
        // Special education courses get medium priority
        else if (isSpecialEducationCourse(course)) {
            priority = 4;
        }

        return priority;
    }

    /**
     * Determine grade level from course (as integer for single grade or average)
     */
    private Integer determineGradeLevel(Course course) {
        if (course.getMinGradeLevel() != null && course.getMaxGradeLevel() != null) {
            // Return average grade level
            return (course.getMinGradeLevel() + course.getMaxGradeLevel()) / 2;
        }
        if (course.getMinGradeLevel() != null) {
            return course.getMinGradeLevel();
        }
        return 10; // Default to 10th grade
    }

    /**
     * Build teacher availability with qualification mapping
     */
    private List<TeacherAvailabilityDTO> buildTeacherAvailability(Schedule schedule,
                                                                    Map<String, List<Long>> certificationToCourseMap) {
        log.debug("Building teacher availability");

        List<Teacher> teachers = teacherRepository.findAll();

        return teachers.stream()
                .filter(teacher -> Boolean.TRUE.equals(teacher.getActive()))
                .map(teacher -> {
                    // Get teacher's certifications
                    List<String> certifications = teacher.getCertifications() != null ?
                            teacher.getCertifications() : new ArrayList<>();

                    // Map certifications to qualified course IDs
                    Set<Long> qualifiedCourseIds = new HashSet<>();
                    for (String cert : certifications) {
                        String normalizedCert = cert.toUpperCase().replace(" ", "_");
                        List<Long> courseIds = certificationToCourseMap.get(normalizedCert);
                        if (courseIds != null) {
                            qualifiedCourseIds.addAll(courseIds);
                        }
                    }

                    // Also add courses from teacher's department
                    if (teacher.getDepartment() != null) {
                        String dept = teacher.getDepartment().toUpperCase();
                        List<Long> deptCourses = certificationToCourseMap.get(dept);
                        if (deptCourses != null) {
                            qualifiedCourseIds.addAll(deptCourses);
                        }
                    }

                    // Calculate max total students based on sections and class size
                    int maxSections = teacher.getMaxPeriodsPerDay() != null ? teacher.getMaxPeriodsPerDay() : 6;
                    int avgClassSize = 28;
                    int maxTotalStudents = maxSections * avgClassSize;

                    return TeacherAvailabilityDTO.builder()
                            .teacherId(teacher.getId())
                            .employeeNumber(teacher.getEmployeeId())
                            .firstName(teacher.getFirstName())
                            .lastName(teacher.getLastName())
                            .fullName(teacher.getName())
                            .department(teacher.getDepartment())
                            .certifications(certifications)
                            .qualifiedCourseIds(new ArrayList<>(qualifiedCourseIds))
                            .maxSections(maxSections)
                            .maxPreps(teacher.getMaxCoursesPerDay() != null ? teacher.getMaxCoursesPerDay() : 4)
                            .maxTotalStudents(maxTotalStudents)
                            .planningPeriodsRequired(1)
                            .unavailableSlots(new ArrayList<>())
                            .preferredSlots(new ArrayList<>())
                            .isPartTime("Part-time".equalsIgnoreCase(teacher.getContractType()))
                            .preferredRoomId(teacher.getHomeRoom() != null ? teacher.getHomeRoom().getId() : null)
                            .coTeacherIds(new ArrayList<>())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Build room availability with department assignments
     */
    private List<RoomAvailabilityDTO> buildRoomAvailability(Schedule schedule) {
        log.debug("Building room availability");

        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .filter(Room::isActive)
                .map(room -> {
                    // Determine assigned departments based on room type and name
                    List<String> assignedDepartments = determineRoomDepartments(room);

                    // Parse equipment list
                    List<String> equipment = new ArrayList<>();
                    if (room.getEquipment() != null && !room.getEquipment().isEmpty()) {
                        for (String eq : room.getEquipment().split(",")) {
                            equipment.add(eq.trim());
                        }
                    }

                    return RoomAvailabilityDTO.builder()
                            .roomId(room.getId())
                            .roomNumber(room.getRoomNumber())
                            .buildingName(room.getBuilding())
                            .roomType(room.getRoomType() != null ? room.getRoomType().name() : "STANDARD_CLASSROOM")
                            .capacity(room.getCapacity())
                            .equipment(equipment)
                            .assignedDepartments(assignedDepartments)
                            .isAccessible(room.isWheelchairAccessible())
                            .isAvailable(room.isAvailable())
                            .unavailableSlots(new ArrayList<>())
                            .pinnedCourseIds(new ArrayList<>())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Determine which departments a room is assigned to based on room type and name
     */
    private List<String> determineRoomDepartments(Room room) {
        List<String> departments = new ArrayList<>();

        String roomType = room.getRoomType() != null ? room.getRoomType().name() : "";
        String roomName = room.getRoomNumber() != null ? room.getRoomNumber().toUpperCase() : "";

        // Map room types to departments
        switch (roomType) {
            case "SCIENCE_LAB":
            case "CHEMISTRY_LAB":
            case "BIOLOGY_LAB":
            case "PHYSICS_LAB":
                departments.add("SCIENCE");
                break;
            case "COMPUTER_LAB":
                departments.add("COMPUTER_SCIENCE");
                departments.add("TECHNOLOGY");
                break;
            case "ART_ROOM":
            case "ART_STUDIO":
                departments.add("ART");
                break;
            case "MUSIC_ROOM":
            case "BAND_ROOM":
            case "CHORUS_ROOM":
                departments.add("MUSIC");
                break;
            case "GYM":
            case "GYMNASIUM":
            case "WEIGHT_ROOM":
                departments.add("PHYSICAL_EDUCATION");
                departments.add("ATHLETICS");
                break;
            case "MEDIA_CENTER":
            case "LIBRARY":
                departments.add("LIBRARY");
                departments.add("MEDIA");
                break;
            case "WORKSHOP":
            case "WOOD_SHOP":
            case "METAL_SHOP":
                departments.add("CAREER_TECH");
                departments.add("INDUSTRIAL_ARTS");
                break;
            case "KITCHEN":
            case "CULINARY_LAB":
                departments.add("CULINARY");
                departments.add("FAMILY_CONSUMER_SCIENCE");
                break;
        }

        // Also check room name/number for department hints
        if (roomName.contains("MATH")) departments.add("MATH");
        if (roomName.contains("ENG") || roomName.contains("LANG")) departments.add("ENGLISH");
        if (roomName.contains("HIST") || roomName.contains("SOC")) departments.add("SOCIAL_STUDIES");
        if (roomName.contains("SCI") || roomName.contains("LAB")) departments.add("SCIENCE");

        return departments;
    }

    /**
     * Build time slots from PeriodTimers
     */
    private List<TimeSlotDTO> buildTimeSlots(Schedule schedule) {
        log.debug("Building time slots");

        List<PeriodTimer> periods = periodTimerRepository.findAll();

        return periods.stream()
                .filter(period -> period.getActive() != null && period.getActive())
                .map(period -> TimeSlotDTO.builder()
                        .timeSlotId(period.getId())
                        .periodName(period.getPeriodName())
                        .periodNumber(period.getPeriodNumber())
                        .startTime(period.getStartTime())
                        .endTime(period.getEndTime())
                        .durationMinutes(period.getDurationMinutes())
                        .dayOfWeek(parseDayOfWeek(period.getDaysOfWeek()))
                        .isLunchPeriod(period.getPeriodNumber() == -1)
                        .isPassingPeriod(false)
                        .isPlanningPeriod(false)
                        .isInstructionalPeriod(period.getPeriodNumber() >= 1)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Parse daysOfWeek string (e.g. "MON,TUE,WED,THU,FRI") into a single int.
     * Returns 0 for all weekdays, or 1-7 (Mon-Sun) if only one day is specified.
     */
    private int parseDayOfWeek(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()
                || "MON,TUE,WED,THU,FRI".equals(daysOfWeek)) {
            return 0; // all weekdays
        }
        String[] days = daysOfWeek.split(",");
        if (days.length == 1) {
            return switch (days[0].trim().toUpperCase()) {
                case "MON" -> 1;
                case "TUE" -> 2;
                case "WED" -> 3;
                case "THU" -> 4;
                case "FRI" -> 5;
                case "SAT" -> 6;
                case "SUN" -> 7;
                default -> 0;
            };
        }
        return 0; // multiple days but not all weekdays — DTO only supports single day, default to all
    }

    /**
     * Build lunch periods
     */
    private List<LunchPeriodDTO> buildLunchPeriods(Schedule schedule) {
        log.debug("Building lunch periods");

        List<LunchPeriod> lunchPeriods = lunchPeriodRepository.findAll();

        return lunchPeriods.stream()
                .filter(LunchPeriod::isActive)
                .map(lunch -> LunchPeriodDTO.builder()
                        .lunchPeriodId(lunch.getId())
                        .name(lunch.getName())
                        .waveNumber(lunch.getDisplayOrder() != null ? lunch.getDisplayOrder() : 1)
                        .startTime(lunch.getStartTime())
                        .endTime(lunch.getEndTime())
                        .durationMinutes(lunch.getDurationMinutes())
                        .maxCapacity(lunch.getMaxCapacity())
                        .currentAssignedCount(lunch.getCurrentCount())
                        .assignedGradeLevels(lunch.getGradeLevels())
                        .isPrimary(lunch.getDisplayOrder() != null && lunch.getDisplayOrder() == 1)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Build constraint configuration from district settings
     */
    private ConstraintConfigDTO buildConstraintConfig(Schedule schedule) {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

        return ConstraintConfigDTO.builder()
                // Hard constraints (always enforced)
                .enforceNoStudentConflicts(true)
                .enforceNoTeacherConflicts(true)
                .enforceNoRoomConflicts(true)
                .enforceTeacherQualifications(true)
                .enforceRoomRequirements(true)
                .enforcePrerequisites(true)
                .enforceLunchAssignment(true)
                // Soft constraint weights from district settings
                .studentPreferenceWeight(settings.getStudentPreferenceWeight() != null
                        ? settings.getStudentPreferenceWeight() : 100)
                .teacherTravelWeight(settings.getTeacherTravelWeight() != null
                        ? settings.getTeacherTravelWeight() : 50)
                .scheduleCompactnessWeight(settings.getScheduleCompactnessWeight() != null
                        ? settings.getScheduleCompactnessWeight() : 30)
                .sectionBalanceWeight(settings.getSectionBalanceWeight() != null
                        ? settings.getSectionBalanceWeight() : 70)
                .teacherPreferenceWeight(settings.getTeacherPreferenceWeight() != null
                        ? settings.getTeacherPreferenceWeight() : 60)
                .gradeLevelClusteringWeight(settings.getGradeLevelClusteringWeight() != null
                        ? settings.getGradeLevelClusteringWeight() : 40)
                .departmentClusteringWeight(settings.getDepartmentClusteringWeight() != null
                        ? settings.getDepartmentClusteringWeight() : 40)
                .lunchContinuityWeight(settings.getLunchContinuityWeight() != null
                        ? settings.getLunchContinuityWeight() : 50)
                // Optimization settings from district settings
                .maxOptimizationTimeSeconds(settings.getMaxOptimizationTimeSeconds() != null
                        ? settings.getMaxOptimizationTimeSeconds() : 120)
                .targetScoreThreshold(0)
                .enableAdvancedOptimization(settings.getEnableAdvancedOptimization() != null
                        ? settings.getEnableAdvancedOptimization() : true)
                .enableParallelOptimization(true)
                .optimizationThreads(settings.getOptimizationThreads() != null
                        ? settings.getOptimizationThreads() : 4)
                // Scheduling preferences
                .preferEarlyAdvancedCourses(true)
                .preferLateElectives(false)
                .preferConsecutiveSections(true)
                .minimizeStudentTravel(true)
                .build();
    }

    /**
     * Build grade levels list from comma-separated string
     */
    private List<String> buildGradeLevelsList(String gradeLevels) {
        if (gradeLevels == null || gradeLevels.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String level : gradeLevels.split(",")) {
            result.add(level.trim());
        }
        return result;
    }

    /**
     * Build export metadata with current user info from SecurityContext
     */
    private ExportMetadataDTO buildExportMetadata(Schedule schedule) {
        String exportedBy = SecurityContext.getCurrentUsername()
                .orElse("Heronix-SIS System");
        Long exportedByUserId = SecurityContext.getCurrentStaffId();

        return ExportMetadataDTO.builder()
                .exportId(java.util.UUID.randomUUID().toString())
                .scheduleId(schedule.getId())
                .exportTimestamp(LocalDateTime.now())
                .sisVersion("1.0.0")
                .exportedBy(exportedBy)
                .exportedByUserId(exportedByUserId)
                .totalStudents((int) studentRepository.count())
                .totalCourseRequests((int) enrollmentRequestRepository.count())
                .totalCourses((int) courseRepository.count())
                .totalTeachers((int) teacherRepository.count())
                .totalRooms((int) roomRepository.count())
                .totalTimeSlots((int) periodTimerRepository.count())
                .notes("Exported from Heronix-SIS for OptaPlanner optimization")
                .build();
    }

    // ========================================================================
    // HELPER CLASSES
    // ========================================================================

    /**
     * Export result DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class SchedulerExportResult {
        private Boolean success;
        private Long scheduleId;
        private String exportId;
        private String importId;
        private String message;
        private Integer studentsExported;
        private Integer coursesExported;
        private Integer teachersExported;
    }
}
