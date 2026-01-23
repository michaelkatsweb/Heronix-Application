package com.heronix.service;

import com.heronix.model.domain.StateCourseCode;
import com.heronix.model.enums.CourseCategory;
import com.heronix.model.enums.CourseType;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.SCEDSubjectArea;
import com.heronix.model.enums.USState;
import com.heronix.repository.StateCourseCodeRepository;
import com.heronix.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing State Course Codes
 *
 * Provides functionality for:
 * - CSV import of state course catalogs
 * - CRUD operations for course codes
 * - Search and filtering by state, grade level, subject
 * - Mapping between state codes and SCED national codes
 * - Statistics and reporting
 *
 * CSV Import Formats Supported:
 * 1. Standard Format:
 *    state_course_code,course_name,sced_code,subject_area,min_grade,max_grade,credits,course_type
 *
 * 2. Texas (TEA) Format:
 *    service_id,course_name,subject_area,grade_level,credit_value
 *
 * 3. California Format:
 *    course_code,course_title,subject,grade_span,uc_csu_approved
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Course Catalog Feature
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StateCourseCodeService {

    private final StateCourseCodeRepository repository;

    // ========================================================================
    // CSV IMPORT
    // ========================================================================

    /**
     * Import course codes from a CSV file for a specific state
     *
     * @param state     The state these courses belong to
     * @param file      CSV file with course data
     * @param schoolYear School year (e.g., "2025-2026")
     * @return Import result with counts and errors
     */
    @Transactional
    public ImportResult importFromCSV(USState state, MultipartFile file, String schoolYear) {
        ImportResult result = new ImportResult();
        result.setStartTime(LocalDateTime.now());

        String currentUser = SecurityContext.getCurrentUsername().orElse("System");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                result.addError("CSV file is empty or missing header row");
                return result;
            }

            // Parse headers
            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = buildHeaderMap(headers);

            // Validate required headers
            List<String> missingHeaders = validateHeaders(headerMap);
            if (!missingHeaders.isEmpty()) {
                result.addError("Missing required headers: " + String.join(", ", missingHeaders));
                return result;
            }

            // Process each line
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    String[] values = parseCSVLine(line);
                    StateCourseCode courseCode = parseStateCourseCode(state, headerMap, values, schoolYear, currentUser);

                    if (courseCode != null) {
                        // Check if already exists
                        Optional<StateCourseCode> existing = repository.findByStateAndStateCourseCode(
                                state, courseCode.getStateCourseCode());

                        if (existing.isPresent()) {
                            // Update existing
                            updateExistingCourseCode(existing.get(), courseCode, currentUser);
                            repository.save(existing.get());
                            result.incrementUpdated();
                        } else {
                            // Create new
                            repository.save(courseCode);
                            result.incrementCreated();
                        }
                    }
                } catch (Exception e) {
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                    result.incrementFailed();
                }
            }

        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            result.addError("Error reading file: " + e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        log.info("CSV import completed for state {}: {} created, {} updated, {} failed",
                state, result.getCreatedCount(), result.getUpdatedCount(), result.getFailedCount());

        return result;
    }

    /**
     * Parse a CSV line handling quoted values
     */
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    /**
     * Build a map of header names to column indices
     */
    private Map<String, Integer> buildHeaderMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String normalized = normalizeHeaderName(headers[i]);
            map.put(normalized, i);
        }
        return map;
    }

    /**
     * Normalize header names to handle variations
     */
    private String normalizeHeaderName(String header) {
        return header.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("\"", "")
                .trim();
    }

    /**
     * Validate that required headers are present
     */
    private List<String> validateHeaders(Map<String, Integer> headerMap) {
        List<String> missing = new ArrayList<>();

        // At minimum, need course code and name
        boolean hasCode = headerMap.containsKey("state_course_code") ||
                         headerMap.containsKey("course_code") ||
                         headerMap.containsKey("service_id") ||
                         headerMap.containsKey("code");

        boolean hasName = headerMap.containsKey("course_name") ||
                         headerMap.containsKey("course_title") ||
                         headerMap.containsKey("name") ||
                         headerMap.containsKey("title");

        if (!hasCode) missing.add("course code (state_course_code, course_code, service_id, or code)");
        if (!hasName) missing.add("course name (course_name, course_title, name, or title)");

        return missing;
    }

    /**
     * Parse a CSV row into a StateCourseCode entity
     */
    private StateCourseCode parseStateCourseCode(USState state, Map<String, Integer> headers,
                                                  String[] values, String schoolYear, String currentUser) {
        StateCourseCode code = new StateCourseCode();
        code.setState(state);
        code.setSchoolYear(schoolYear);
        code.setCreatedBy(currentUser);
        code.setCreatedAt(LocalDateTime.now());
        code.setActive(true);
        code.setDataSource("CSV Import");

        // Course code (required)
        String courseCode = getValueFromHeaders(headers, values,
                "state_course_code", "course_code", "service_id", "code");
        if (courseCode == null || courseCode.isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }
        code.setStateCourseCode(courseCode);

        // Course name (required)
        String courseName = getValueFromHeaders(headers, values,
                "course_name", "course_title", "name", "title");
        if (courseName == null || courseName.isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }
        code.setCourseName(courseName);

        // Optional fields
        code.setCourseAbbreviation(getValueFromHeaders(headers, values,
                "abbreviation", "short_name", "abbrev"));

        code.setDescription(getValueFromHeaders(headers, values,
                "description", "desc", "course_description"));

        // SCED code
        String scedCode = getValueFromHeaders(headers, values,
                "sced_code", "sced", "national_code");
        if (scedCode != null && !scedCode.isEmpty()) {
            code.setScedCode(scedCode);
            // Auto-detect subject area from SCED
            if (scedCode.length() >= 2) {
                SCEDSubjectArea area = SCEDSubjectArea.fromCode(scedCode.substring(0, 2));
                if (area != null) {
                    code.setSubjectArea(area);
                }
            }
        }

        // Subject area (if not set from SCED)
        if (code.getSubjectArea() == null) {
            String subjectStr = getValueFromHeaders(headers, values,
                    "subject_area", "subject", "department");
            if (subjectStr != null) {
                code.setSubjectArea(parseSCEDSubjectArea(subjectStr));
                code.setStateSubjectName(subjectStr);
            }
        }

        code.setStateSubjectCode(getValueFromHeaders(headers, values,
                "state_subject_code", "subject_code"));

        // Grade levels
        String minGrade = getValueFromHeaders(headers, values,
                "min_grade", "min_grade_level", "grade_from", "start_grade");
        String maxGrade = getValueFromHeaders(headers, values,
                "max_grade", "max_grade_level", "grade_to", "end_grade");
        String gradeSpan = getValueFromHeaders(headers, values,
                "grade_span", "grade_level", "grades", "grade_range");

        parseGradeLevels(code, minGrade, maxGrade, gradeSpan);

        // Credits
        String credits = getValueFromHeaders(headers, values,
                "credits", "credit_value", "credit", "units");
        if (credits != null && !credits.isEmpty()) {
            try {
                code.setCredits(Double.parseDouble(credits));
            } catch (NumberFormatException e) {
                log.debug("Could not parse credits: {}", credits);
            }
        }

        // Course type
        String courseType = getValueFromHeaders(headers, values,
                "course_type", "type", "level");
        if (courseType != null) {
            code.setCourseType(parseCourseType(courseType));
        }

        // Course category
        String category = getValueFromHeaders(headers, values,
                "category", "course_category", "is_elective");
        if (category != null) {
            code.setCourseCategory(parseCourseCategory(category));
        }

        // Prerequisites
        code.setPrerequisites(getValueFromHeaders(headers, values,
                "prerequisites", "prereqs", "prerequisite"));

        // CTE fields
        String isCTE = getValueFromHeaders(headers, values,
                "is_cte", "cte", "career_tech");
        code.setIsCTE(parseBoolean(isCTE));

        code.setCteCluster(getValueFromHeaders(headers, values,
                "cte_cluster", "career_cluster", "cluster"));

        code.setCteProgram(getValueFromHeaders(headers, values,
                "cte_program", "pathway", "program_of_study"));

        // Special designations
        String isAP = getValueFromHeaders(headers, values,
                "is_ap", "ap", "advanced_placement");
        code.setIsAP(parseBoolean(isAP));

        String isIB = getValueFromHeaders(headers, values,
                "is_ib", "ib", "international_baccalaureate");
        code.setIsIB(parseBoolean(isIB));

        String isDualCredit = getValueFromHeaders(headers, values,
                "is_dual_credit", "dual_credit", "concurrent_enrollment");
        code.setIsDualCredit(parseBoolean(isDualCredit));

        code.setDualCreditPartner(getValueFromHeaders(headers, values,
                "dual_credit_partner", "college", "partner_institution"));

        String isSTEM = getValueFromHeaders(headers, values,
                "is_stem", "stem");
        code.setIsSTEM(parseBoolean(isSTEM));

        // Graduation requirement
        String gradReq = getValueFromHeaders(headers, values,
                "graduation_requirement", "grad_req", "required_for_graduation");
        code.setGraduationRequirement(parseBoolean(gradReq));

        code.setGraduationRequirementType(getValueFromHeaders(headers, values,
                "graduation_requirement_type", "requirement_type", "grad_req_type"));

        // Instructional details
        String minutes = getValueFromHeaders(headers, values,
                "instructional_minutes", "minutes", "class_minutes");
        if (minutes != null) {
            try {
                code.setInstructionalMinutes(Integer.parseInt(minutes));
            } catch (NumberFormatException e) {
                log.debug("Could not parse minutes: {}", minutes);
            }
        }

        code.setDurationType(getValueFromHeaders(headers, values,
                "duration_type", "duration", "term"));

        // Notes
        code.setNotes(getValueFromHeaders(headers, values,
                "notes", "comments", "remarks"));

        return code;
    }

    /**
     * Get value from CSV using multiple possible header names
     */
    private String getValueFromHeaders(Map<String, Integer> headers, String[] values, String... possibleNames) {
        for (String name : possibleNames) {
            Integer index = headers.get(name);
            if (index != null && index < values.length) {
                String value = values[index].trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Parse grade levels from various formats
     */
    private void parseGradeLevels(StateCourseCode code, String minGrade, String maxGrade, String gradeSpan) {
        // Try grade span first (e.g., "9-12", "K-5", "6,7,8")
        if (gradeSpan != null && !gradeSpan.isEmpty()) {
            if (gradeSpan.contains("-")) {
                String[] parts = gradeSpan.split("-");
                if (parts.length == 2) {
                    code.setMinGradeLevel(parseGradeToInt(parts[0].trim()));
                    code.setMaxGradeLevel(parseGradeToInt(parts[1].trim()));
                }
            } else if (gradeSpan.contains(",")) {
                String[] grades = gradeSpan.split(",");
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (String g : grades) {
                    Integer val = parseGradeToInt(g.trim());
                    if (val != null) {
                        min = Math.min(min, val);
                        max = Math.max(max, val);
                    }
                }
                if (min != Integer.MAX_VALUE) code.setMinGradeLevel(min);
                if (max != Integer.MIN_VALUE) code.setMaxGradeLevel(max);
                code.setTypicalGradeLevels(gradeSpan);
            } else {
                // Single grade
                Integer grade = parseGradeToInt(gradeSpan);
                code.setMinGradeLevel(grade);
                code.setMaxGradeLevel(grade);
            }
        }

        // Override with explicit min/max if provided
        if (minGrade != null && !minGrade.isEmpty()) {
            code.setMinGradeLevel(parseGradeToInt(minGrade));
        }
        if (maxGrade != null && !maxGrade.isEmpty()) {
            code.setMaxGradeLevel(parseGradeToInt(maxGrade));
        }

        // Determine education level
        if (code.getMinGradeLevel() != null) {
            code.setEducationLevel(determineEducationLevel(code.getMinGradeLevel()));
        }
    }

    /**
     * Parse grade string to integer (-1=Pre-K, 0=K, 1-12 for grades)
     */
    private Integer parseGradeToInt(String grade) {
        if (grade == null || grade.isEmpty()) return null;

        String normalized = grade.toUpperCase().trim();

        // Pre-K variations
        if (normalized.matches("PRE-?K|PK|PRE-?KINDERGARTEN")) {
            return -1;
        }

        // Kindergarten variations
        if (normalized.matches("K|KG|KINDERGARTEN|KINDER")) {
            return 0;
        }

        // Numeric grade (strip suffix like "th", "st", "nd", "rd")
        normalized = normalized.replaceAll("(ST|ND|RD|TH|GRADE).*", "").trim();

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Determine education level from grade level number
     */
    private EducationLevel determineEducationLevel(Integer gradeLevel) {
        if (gradeLevel == null) return EducationLevel.HIGH_SCHOOL;
        if (gradeLevel < 0) return EducationLevel.PRE_K;
        if (gradeLevel == 0) return EducationLevel.KINDERGARTEN;
        if (gradeLevel <= 5) return EducationLevel.ELEMENTARY;
        if (gradeLevel <= 8) return EducationLevel.MIDDLE_SCHOOL;
        return EducationLevel.HIGH_SCHOOL;
    }

    /**
     * Parse SCED subject area from string
     */
    private SCEDSubjectArea parseSCEDSubjectArea(String subject) {
        if (subject == null || subject.isEmpty()) return null;

        String normalized = subject.toUpperCase().trim();

        // Try direct match
        try {
            return SCEDSubjectArea.valueOf(normalized.replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // Continue to pattern matching
        }

        // Pattern matching for common names
        if (normalized.contains("ENGLISH") || normalized.contains("LANGUAGE ARTS") || normalized.contains("ELA")) {
            return SCEDSubjectArea.ENGLISH_LANGUAGE_ARTS;
        }
        if (normalized.contains("MATH")) {
            return SCEDSubjectArea.MATHEMATICS;
        }
        if (normalized.contains("SCIENCE") || normalized.contains("BIOLOGY") ||
            normalized.contains("CHEMISTRY") || normalized.contains("PHYSICS")) {
            return SCEDSubjectArea.LIFE_PHYSICAL_SCIENCES;
        }
        if (normalized.contains("SOCIAL") || normalized.contains("HISTORY") ||
            normalized.contains("GOVERNMENT") || normalized.contains("CIVICS")) {
            return SCEDSubjectArea.SOCIAL_SCIENCES_HISTORY;
        }
        if (normalized.contains("ART") || normalized.contains("MUSIC") ||
            normalized.contains("DRAMA") || normalized.contains("THEATER")) {
            return SCEDSubjectArea.VISUAL_PERFORMING_ARTS;
        }
        if (normalized.contains("FOREIGN") || normalized.contains("SPANISH") ||
            normalized.contains("FRENCH") || normalized.contains("WORLD LANGUAGE")) {
            return SCEDSubjectArea.FOREIGN_LANGUAGE;
        }
        if (normalized.contains("PE") || normalized.contains("PHYSICAL ED") || normalized.contains("HEALTH")) {
            return SCEDSubjectArea.PHYSICAL_HEALTH_SAFETY;
        }
        if (normalized.contains("COMPUTER") || normalized.contains("IT") || normalized.contains("TECHNOLOGY")) {
            return SCEDSubjectArea.INFORMATION_TECHNOLOGY;
        }
        if (normalized.contains("BUSINESS") || normalized.contains("MARKETING")) {
            return SCEDSubjectArea.BUSINESS_MARKETING;
        }
        if (normalized.contains("ENGINEERING") || normalized.contains("ROBOTICS")) {
            return SCEDSubjectArea.ENGINEERING_TECHNOLOGY;
        }

        return null;
    }

    /**
     * Parse course type from string
     */
    private CourseType parseCourseType(String type) {
        if (type == null || type.isEmpty()) return CourseType.REGULAR;

        String normalized = type.toUpperCase().trim();

        if (normalized.contains("AP") || normalized.contains("ADVANCED PLACEMENT")) {
            return CourseType.AP;
        }
        if (normalized.contains("IB") || normalized.contains("INTERNATIONAL BACCALAUREATE")) {
            return CourseType.IB;
        }
        if (normalized.contains("HONORS") || normalized.contains("HON")) {
            return CourseType.HONORS;
        }
        if (normalized.contains("PRE-AP") || normalized.contains("PREAP")) {
            return CourseType.PRE_AP;
        }
        if (normalized.contains("DUAL") || normalized.contains("CONCURRENT")) {
            return CourseType.DUAL_CREDIT;
        }
        if (normalized.contains("GIFTED") || normalized.contains("GT")) {
            return CourseType.GIFTED_TALENTED;
        }
        if (normalized.contains("REMEDIAL") || normalized.contains("BASIC")) {
            return CourseType.REMEDIAL;
        }

        return CourseType.REGULAR;
    }

    /**
     * Parse course category from string
     */
    private CourseCategory parseCourseCategory(String category) {
        if (category == null || category.isEmpty()) return CourseCategory.CORE;

        String normalized = category.toUpperCase().trim();

        if (normalized.contains("ELECTIVE") || normalized.equals("TRUE") ||
            normalized.equals("YES") || normalized.equals("1")) {
            return CourseCategory.ELECTIVE;
        }

        return CourseCategory.CORE;
    }

    /**
     * Parse boolean from string
     */
    private Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) return false;
        String normalized = value.toUpperCase().trim();
        return normalized.equals("TRUE") || normalized.equals("YES") ||
               normalized.equals("1") || normalized.equals("Y");
    }

    /**
     * Update existing course code with new data
     */
    private void updateExistingCourseCode(StateCourseCode existing, StateCourseCode newData, String currentUser) {
        existing.setCourseName(newData.getCourseName());
        existing.setCourseAbbreviation(newData.getCourseAbbreviation());
        existing.setDescription(newData.getDescription());
        existing.setScedCode(newData.getScedCode());
        existing.setSubjectArea(newData.getSubjectArea());
        existing.setMinGradeLevel(newData.getMinGradeLevel());
        existing.setMaxGradeLevel(newData.getMaxGradeLevel());
        existing.setEducationLevel(newData.getEducationLevel());
        existing.setCredits(newData.getCredits());
        existing.setCourseType(newData.getCourseType());
        existing.setCourseCategory(newData.getCourseCategory());
        existing.setIsCTE(newData.getIsCTE());
        existing.setCteCluster(newData.getCteCluster());
        existing.setIsAP(newData.getIsAP());
        existing.setIsIB(newData.getIsIB());
        existing.setIsDualCredit(newData.getIsDualCredit());
        existing.setIsSTEM(newData.getIsSTEM());
        existing.setGraduationRequirement(newData.getGraduationRequirement());
        existing.setGraduationRequirementType(newData.getGraduationRequirementType());
        existing.setSchoolYear(newData.getSchoolYear());
        existing.setUpdatedBy(currentUser);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setActive(true);
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Get all course codes for a state
     */
    public List<StateCourseCode> findByState(USState state) {
        return repository.findByStateAndActiveTrueOrderByStateCourseCodeAsc(state);
    }

    /**
     * Get course codes for a state and grade level
     */
    public List<StateCourseCode> findByStateAndGrade(USState state, Integer gradeLevel) {
        return repository.findByStateAndGradeLevel(state, gradeLevel);
    }

    /**
     * Get course codes for a state and grade range (e.g., 9-12 for high school)
     */
    public List<StateCourseCode> findByStateAndGradeRange(USState state, Integer minGrade, Integer maxGrade) {
        return repository.findByStateAndGradeRange(state, minGrade, maxGrade);
    }

    /**
     * Get course codes by subject area
     */
    public List<StateCourseCode> findByStateAndSubject(USState state, SCEDSubjectArea subjectArea) {
        return repository.findByStateAndSubjectAreaAndActiveTrueOrderByCourseName(state, subjectArea);
    }

    /**
     * Get courses by education level
     */
    public List<StateCourseCode> findByEducationLevel(USState state, EducationLevel level) {
        return switch (level) {
            case PRE_K, KINDERGARTEN, ELEMENTARY -> repository.findElementaryCourses(state);
            case MIDDLE_SCHOOL -> repository.findMiddleSchoolCourses(state);
            case HIGH_SCHOOL -> repository.findHighSchoolCourses(state);
            default -> repository.findByStateAndActiveTrueOrderByStateCourseCodeAsc(state);
        };
    }

    /**
     * Search courses by name or code
     */
    public List<StateCourseCode> search(USState state, String searchTerm) {
        return repository.searchAll(state, searchTerm);
    }

    /**
     * Get all states that have course data loaded
     */
    public List<USState> getStatesWithData() {
        return repository.findStatesWithCourseData();
    }

    /**
     * Get statistics for a state's course catalog
     */
    public Map<String, Object> getStatistics(USState state) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCourses", repository.countByStateAndActiveTrue(state));
        stats.put("byEducationLevel", repository.countByEducationLevel(state));
        stats.put("bySubjectArea", repository.countBySubjectArea(state));
        stats.put("byCourseType", repository.countByCourseType(state));
        stats.put("cteClusters", repository.findCTEClusters(state));
        stats.put("schoolYears", repository.findSchoolYears(state));

        return stats;
    }

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Save a course code
     */
    @Transactional
    public StateCourseCode save(StateCourseCode courseCode) {
        if (courseCode.getId() == null) {
            courseCode.setCreatedAt(LocalDateTime.now());
            courseCode.setCreatedBy(SecurityContext.getCurrentUsername().orElse("System"));
        } else {
            courseCode.setUpdatedAt(LocalDateTime.now());
            courseCode.setUpdatedBy(SecurityContext.getCurrentUsername().orElse("System"));
        }
        return repository.save(courseCode);
    }

    /**
     * Find by ID
     */
    public Optional<StateCourseCode> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Find by state and code
     */
    public Optional<StateCourseCode> findByStateAndCode(USState state, String code) {
        return repository.findByStateAndStateCourseCode(state, code);
    }

    /**
     * Delete a course code
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Deactivate all courses for a state
     */
    @Transactional
    public void deactivateState(USState state) {
        List<StateCourseCode> courses = repository.findByStateOrderByStateCourseCodeAsc(state);
        for (StateCourseCode course : courses) {
            course.setActive(false);
            course.setUpdatedAt(LocalDateTime.now());
            course.setUpdatedBy(SecurityContext.getCurrentUsername().orElse("System"));
        }
        repository.saveAll(courses);
    }

    // ========================================================================
    // IMPORT RESULT CLASS
    // ========================================================================

    /**
     * Result object for CSV import operations
     */
    public static class ImportResult {
        private int createdCount = 0;
        private int updatedCount = 0;
        private int failedCount = 0;
        private List<String> errors = new ArrayList<>();
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public void incrementCreated() { createdCount++; }
        public void incrementUpdated() { updatedCount++; }
        public void incrementFailed() { failedCount++; }
        public void addError(String error) { errors.add(error); }

        public int getCreatedCount() { return createdCount; }
        public int getUpdatedCount() { return updatedCount; }
        public int getFailedCount() { return failedCount; }
        public int getTotalProcessed() { return createdCount + updatedCount + failedCount; }
        public List<String> getErrors() { return errors; }
        public boolean hasErrors() { return !errors.isEmpty(); }

        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }

        public String getSummary() {
            return String.format("Import completed: %d created, %d updated, %d failed",
                    createdCount, updatedCount, failedCount);
        }
    }
}
