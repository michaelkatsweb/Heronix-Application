package com.heronix.service;

import com.heronix.model.domain.StateConfiguration;
import com.heronix.model.domain.StateTerminology;
import com.heronix.model.enums.USState;
import com.heronix.repository.StateConfigurationRepository;
import com.heronix.repository.StateTerminologyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for State Configuration Management
 *
 * Handles the automatic adaptation of the SIS based on selected state.
 * When a school/district selects their state during initial setup,
 * this service configures the entire application to match that state's:
 * - Course coding system
 * - Student ID format
 * - Graduation requirements
 * - Grading scales
 * - Attendance policies
 * - Terminology preferences
 * - Reporting requirements
 *
 * This is the central service for state-specific customization.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StateConfigurationService {

    private final StateConfigurationRepository configRepository;
    private final StateTerminologyRepository terminologyRepository;

    // Cache for current state configuration (for performance)
    private StateConfiguration currentConfig;
    private Map<String, String> currentTerminology;

    // ========================================================================
    // CONFIGURATION MANAGEMENT
    // ========================================================================

    /**
     * Get or create configuration for a state
     */
    @Transactional
    public StateConfiguration getOrCreateConfiguration(USState state) {
        return configRepository.findByState(state)
                .orElseGet(() -> createDefaultConfiguration(state));
    }

    /**
     * Get configuration for a state (returns null if not found)
     */
    public Optional<StateConfiguration> getConfiguration(USState state) {
        return configRepository.findByStateAndActiveTrue(state);
    }

    /**
     * Get the currently active state configuration for the district
     */
    public StateConfiguration getCurrentConfiguration() {
        return currentConfig;
    }

    /**
     * Set the current state configuration (called during app initialization)
     */
    @Transactional
    public void setCurrentState(USState state) {
        this.currentConfig = getOrCreateConfiguration(state);
        loadTerminology(state);
        log.info("State configuration set to: {}", state.getDisplayName());
    }

    /**
     * Apply state configuration during initial setup
     * This is the main entry point for state adaptation
     */
    @Transactional
    public StateConfiguration applyStateConfiguration(USState state) {
        log.info("Applying state configuration for: {}", state.getDisplayName());

        // Get or create the configuration
        StateConfiguration config = getOrCreateConfiguration(state);

        // Create state-specific terminology
        createStateTerminology(state);

        // Set as current
        this.currentConfig = config;
        loadTerminology(state);

        log.info("State configuration applied successfully for {}", state.getDisplayName());
        return config;
    }

    /**
     * Get all states with pre-built configurations
     */
    public List<USState> getStatesWithConfigurations() {
        return configRepository.findStatesWithConfiguration();
    }

    // ========================================================================
    // TERMINOLOGY MANAGEMENT
    // ========================================================================

    /**
     * Get state-specific term for a key
     * Returns the default term if not found
     */
    public String getTerm(String termKey) {
        if (currentTerminology != null && currentTerminology.containsKey(termKey)) {
            return currentTerminology.get(termKey);
        }
        return getDefaultTerm(termKey);
    }

    /**
     * Get state-specific term for a specific state
     */
    public String getTerm(USState state, String termKey) {
        return terminologyRepository.getStateTerm(state, termKey)
                .orElse(getDefaultTerm(termKey));
    }

    /**
     * Get abbreviated term
     */
    public String getTermShort(String termKey) {
        if (currentConfig == null) return termKey;
        return terminologyRepository.getStateTermShort(currentConfig.getState(), termKey)
                .orElse(getTerm(termKey));
    }

    /**
     * Load terminology into cache for current state
     */
    private void loadTerminology(USState state) {
        currentTerminology = new HashMap<>();
        List<StateTerminology> terms = terminologyRepository.findByStateAndActiveTrue(state);
        for (StateTerminology term : terms) {
            currentTerminology.put(term.getTermKey(), term.getStateTerm());
        }
        log.debug("Loaded {} terminology entries for {}", terms.size(), state);
    }

    /**
     * Get default term (when no state-specific term exists)
     */
    private String getDefaultTerm(String termKey) {
        // Return human-readable defaults
        return switch (termKey) {
            case StateTerminology.TERM_SCHOOL -> "School";
            case StateTerminology.TERM_CAMPUS -> "Campus";
            case StateTerminology.TERM_DISTRICT -> "District";
            case StateTerminology.TERM_STUDENT_ID -> "Student ID";
            case StateTerminology.TERM_COURSE_CODE -> "Course Code";
            case StateTerminology.TERM_ELL -> "English Language Learner";
            case StateTerminology.TERM_SPECIAL_ED -> "Special Education";
            case StateTerminology.TERM_GIFTED -> "Gifted and Talented";
            case StateTerminology.TERM_CTE -> "Career and Technical Education";
            case StateTerminology.TERM_TEACHER -> "Teacher";
            case StateTerminology.TERM_PARA -> "Paraprofessional";
            case StateTerminology.TERM_TARDY -> "Tardy";
            case StateTerminology.TERM_ABSENT -> "Absent";
            case StateTerminology.TERM_REPORT_CARD -> "Report Card";
            case StateTerminology.TERM_GPA -> "GPA";
            case StateTerminology.TERM_GRADING_PERIOD -> "Grading Period";
            default -> termKey.replace("_", " ");
        };
    }

    // ========================================================================
    // DEFAULT STATE CONFIGURATIONS
    // ========================================================================

    /**
     * Create default configuration for a state
     */
    @Transactional
    public StateConfiguration createDefaultConfiguration(USState state) {
        StateConfiguration config = new StateConfiguration();
        config.setState(state);
        config.setStateName(state.getDisplayName());
        config.setActive(true);
        config.setUpdatedAt(LocalDateTime.now());

        // Apply state-specific defaults
        applyStateDefaults(config, state);

        return configRepository.save(config);
    }

    /**
     * Apply state-specific default values
     */
    private void applyStateDefaults(StateConfiguration config, USState state) {
        switch (state) {
            case TX -> applyTexasDefaults(config);
            case CA -> applyCaliforniaDefaults(config);
            case NY -> applyNewYorkDefaults(config);
            case FL -> applyFloridaDefaults(config);
            case OH -> applyOhioDefaults(config);
            case PA -> applyPennsylvaniaDefaults(config);
            case IL -> applyIllinoisDefaults(config);
            case GA -> applyGeorgiaDefaults(config);
            case NC -> applyNorthCarolinaDefaults(config);
            case MI -> applyMichiganDefaults(config);
            case AZ -> applyArizonaDefaults(config);
            case WA -> applyWashingtonDefaults(config);
            case CO -> applyColoradoDefaults(config);
            case MA -> applyMassachusettsDefaults(config);
            case VA -> applyVirginiaDefaults(config);
            default -> applyGenericDefaults(config);
        }
    }

    // ========================================================================
    // STATE-SPECIFIC DEFAULTS
    // ========================================================================

    private void applyTexasDefaults(StateConfiguration config) {
        config.setEducationDepartment("Texas Education Agency");
        config.setDepartmentAbbreviation("TEA");
        config.setDepartmentUrl("https://tea.texas.gov");

        // Course coding
        config.setCourseCodeSystemName("PEIMS Service IDs");
        config.setCourseCodeFormat("########");
        config.setCourseCodeLength(8);
        config.setCourseCodeLabel("Service ID");
        config.setCourseCatalogUrl("https://tea.texas.gov/academics/curriculum-standards/teks-texas-essential-knowledge-and-skills");

        // Student ID
        config.setStudentIdSystemName("PEIMS Student ID");
        config.setStudentIdLabel("PEIMS ID");
        config.setStudentIdFormat("#########");
        config.setStudentIdLength(9);

        // School ID
        config.setSchoolIdSystemName("Campus ID");
        config.setSchoolIdLabel("Campus ID");
        config.setDistrictIdLabel("District ID");

        // Grading
        config.setMinimumPassingGrade(70.0);
        config.setMinimumPassingLetter("C");

        // Graduation (Foundation High School Program)
        config.setGraduationCreditsRequired(22.0); // Foundation
        config.setEnglishCreditsRequired(4.0);
        config.setMathCreditsRequired(3.0);
        config.setScienceCreditsRequired(3.0);
        config.setSocialStudiesCreditsRequired(3.0);
        config.setForeignLanguageCreditsRequired(2.0);
        config.setPeCreditsRequired(1.0);
        config.setFineArtsCreditsRequired(1.0);
        config.setElectiveCreditsRequired(5.0);

        // Assessments
        config.setExitExamRequired(true);
        config.setExitExamName("STAAR End-of-Course");

        // Attendance
        config.setRequiredSchoolDays(180);
        config.setAttendanceTrackingMethod("PERIOD_BY_PERIOD");
        config.setTruancyThresholdPercent(10.0);

        // Calendar - Texas uses 6-week grading periods
        config.setReportCardPeriods(6);
        config.setProgressReportPeriods(6);

        // Reporting
        config.setStateReportingSystem("PEIMS");
        config.setReportingPeriodsPerYear(6);

        // Terminology
        config.setSpecialEdLabel("Special Education");
        config.setEllLabel("Emergent Bilingual");
        config.setGiftedLabel("Gifted and Talented");

        // Calendar
        config.setSchoolYearStartMonth(8);
        config.setSchoolYearEndMonth(5);
        config.setHasSpringBreak(true);
        config.setHasFallBreak(false);

        // Teacher certification
        config.setCertificationBoard("State Board for Educator Certification (SBEC)");
        config.setCertificationLookupUrl("https://secure.sbec.state.tx.us/SBECOnline/virtcert.asp");
    }

    private void applyCaliforniaDefaults(StateConfiguration config) {
        config.setEducationDepartment("California Department of Education");
        config.setDepartmentAbbreviation("CDE");
        config.setDepartmentUrl("https://www.cde.ca.gov");

        // Course coding
        config.setCourseCodeSystemName("CALPADS Course Codes");
        config.setCourseCodeFormat("#####");
        config.setCourseCodeLength(5);
        config.setCourseCodeLabel("State Course Code");
        config.setCourseCatalogUrl("https://www.cde.ca.gov/ds/sp/cl/");

        // Student ID
        config.setStudentIdSystemName("Statewide Student Identifier (SSID)");
        config.setStudentIdLabel("SSID");
        config.setStudentIdFormat("##########");
        config.setStudentIdLength(10);

        // School ID
        config.setSchoolIdSystemName("County-District-School (CDS) Code");
        config.setSchoolIdLabel("CDS Code");
        config.setDistrictIdLabel("District Code");

        // Graduation (California - 230 total credits, 13 courses)
        config.setGraduationCreditsRequired(230.0); // California uses credit hours differently
        config.setEnglishCreditsRequired(30.0);
        config.setMathCreditsRequired(20.0);
        config.setScienceCreditsRequired(20.0);
        config.setSocialStudiesCreditsRequired(30.0);
        config.setForeignLanguageCreditsRequired(10.0);
        config.setPeCreditsRequired(20.0);
        config.setFineArtsCreditsRequired(10.0);

        // Assessments - California doesn't have exit exams anymore
        config.setExitExamRequired(false);

        // Attendance
        config.setRequiredSchoolDays(180);
        config.setInstructionalMinutesElementary(36000); // Per year
        config.setInstructionalMinutesSecondary(64800);

        // Reporting
        config.setStateReportingSystem("CALPADS");

        // Terminology
        config.setEllLabel("English Learner");
        config.setGiftedLabel("GATE");

        // Teacher certification
        config.setCertificationBoard("Commission on Teacher Credentialing (CTC)");
    }

    private void applyNewYorkDefaults(StateConfiguration config) {
        config.setEducationDepartment("New York State Education Department");
        config.setDepartmentAbbreviation("NYSED");
        config.setDepartmentUrl("http://www.nysed.gov");

        // Course coding
        config.setCourseCodeSystemName("NYSED Course Codes");
        config.setCourseCodeFormat("####");
        config.setCourseCodeLength(4);
        config.setCourseCodeLabel("Course Number");

        // Student ID
        config.setStudentIdSystemName("NYS Student ID");
        config.setStudentIdLabel("NYSSIS ID");

        // School ID
        config.setSchoolIdSystemName("BEDS Code");
        config.setSchoolIdLabel("BEDS Code");

        // Graduation (Regents Diploma)
        config.setGraduationCreditsRequired(22.0);
        config.setEnglishCreditsRequired(4.0);
        config.setMathCreditsRequired(3.0);
        config.setScienceCreditsRequired(3.0);
        config.setSocialStudiesCreditsRequired(4.0);
        config.setForeignLanguageCreditsRequired(1.0);
        config.setPeCreditsRequired(2.0);
        config.setFineArtsCreditsRequired(1.0);

        // Assessments
        config.setExitExamRequired(true);
        config.setExitExamName("Regents Examinations");

        // Reporting
        config.setStateReportingSystem("SIRS");

        // Calendar - New York uses semesters
        config.setReportCardPeriods(4);
    }

    private void applyFloridaDefaults(StateConfiguration config) {
        config.setEducationDepartment("Florida Department of Education");
        config.setDepartmentAbbreviation("FLDOE");
        config.setDepartmentUrl("https://www.fldoe.org");

        // Course coding
        config.setCourseCodeSystemName("Florida Course Code Directory");
        config.setCourseCodeFormat("#######");
        config.setCourseCodeLength(7);
        config.setCourseCodeLabel("Course Number");
        config.setCourseCatalogUrl("https://www.fldoe.org/accountability/data-sys/course-code-directory/");

        // Student ID
        config.setStudentIdSystemName("Florida Education Identifier (FLEID)");
        config.setStudentIdLabel("FLEID");

        // Graduation (24-credit standard diploma)
        config.setGraduationCreditsRequired(24.0);
        config.setEnglishCreditsRequired(4.0);
        config.setMathCreditsRequired(4.0);
        config.setScienceCreditsRequired(3.0);
        config.setSocialStudiesCreditsRequired(3.0);
        config.setForeignLanguageCreditsRequired(2.0);
        config.setPeCreditsRequired(1.0);
        config.setFineArtsCreditsRequired(1.0);

        // Assessments
        config.setExitExamRequired(true);
        config.setExitExamName("Florida Standards Assessments (FSA)");

        // Reporting
        config.setStateReportingSystem("FASTER/FLEID");

        // Terminology - Florida uses different terms
        config.setSpecialEdLabel("Exceptional Student Education");
        config.setEllLabel("English for Speakers of Other Languages");
    }

    private void applyOhioDefaults(StateConfiguration config) {
        config.setEducationDepartment("Ohio Department of Education");
        config.setDepartmentAbbreviation("ODE");
        config.setDepartmentUrl("https://education.ohio.gov");

        // Course coding
        config.setCourseCodeSystemName("EMIS Subject Codes");
        config.setCourseCodeFormat("######");
        config.setCourseCodeLength(6);
        config.setCourseCodeLabel("Subject Code");

        // Student ID
        config.setStudentIdSystemName("State Student Identifier (SSID)");
        config.setStudentIdLabel("SSID");

        // Graduation
        config.setGraduationCreditsRequired(20.0);
        config.setEnglishCreditsRequired(4.0);
        config.setMathCreditsRequired(4.0);
        config.setScienceCreditsRequired(3.0);
        config.setSocialStudiesCreditsRequired(3.0);
        config.setForeignLanguageCreditsRequired(0.0); // Not required
        config.setPeCreditsRequired(0.5);
        config.setFineArtsCreditsRequired(1.0);

        // Reporting
        config.setStateReportingSystem("EMIS");
    }

    private void applyPennsylvaniaDefaults(StateConfiguration config) {
        config.setEducationDepartment("Pennsylvania Department of Education");
        config.setDepartmentAbbreviation("PDE");
        config.setDepartmentUrl("https://www.education.pa.gov");

        config.setCourseCodeSystemName("PIMS Course Codes");
        config.setStudentIdSystemName("Pennsylvania Secure ID (PAsecureID)");
        config.setStudentIdLabel("PAsecureID");

        config.setGraduationCreditsRequired(21.0);
        config.setStateReportingSystem("PIMS");
    }

    private void applyIllinoisDefaults(StateConfiguration config) {
        config.setEducationDepartment("Illinois State Board of Education");
        config.setDepartmentAbbreviation("ISBE");
        config.setDepartmentUrl("https://www.isbe.net");

        config.setCourseCodeSystemName("SIS Course Codes");
        config.setStudentIdSystemName("State Student Identifier");
        config.setStudentIdLabel("State ID");

        config.setGraduationCreditsRequired(16.0);
        config.setStateReportingSystem("SIS");
    }

    private void applyGeorgiaDefaults(StateConfiguration config) {
        config.setEducationDepartment("Georgia Department of Education");
        config.setDepartmentAbbreviation("GaDOE");
        config.setDepartmentUrl("https://www.gadoe.org");

        config.setCourseCodeSystemName("CTAE/Academic Course Codes");
        config.setStudentIdSystemName("Georgia Testing Identifier (GTID)");
        config.setStudentIdLabel("GTID");

        config.setGraduationCreditsRequired(23.0);
        config.setExitExamRequired(true);
        config.setExitExamName("Georgia Milestones");
        config.setStateReportingSystem("Student Record");
    }

    private void applyNorthCarolinaDefaults(StateConfiguration config) {
        config.setEducationDepartment("North Carolina Department of Public Instruction");
        config.setDepartmentAbbreviation("NCDPI");
        config.setDepartmentUrl("https://www.dpi.nc.gov");

        config.setCourseCodeSystemName("Standard Course of Study");
        config.setStudentIdSystemName("PowerSchool Student Number");
        config.setStudentIdLabel("Student Number");

        config.setGraduationCreditsRequired(22.0);
        config.setExitExamRequired(true);
        config.setExitExamName("NC End-of-Course Tests");
        config.setStateReportingSystem("PowerSchool");

        // NC uses different terminology
        config.setGiftedLabel("Academically/Intellectually Gifted (AIG)");
    }

    private void applyMichiganDefaults(StateConfiguration config) {
        config.setEducationDepartment("Michigan Department of Education");
        config.setDepartmentAbbreviation("MDE");
        config.setDepartmentUrl("https://www.michigan.gov/mde");

        config.setCourseCodeSystemName("MSDS Course Codes");
        config.setStudentIdSystemName("Unique Identification Code (UIC)");
        config.setStudentIdLabel("UIC");

        config.setGraduationCreditsRequired(18.0);
        config.setStateReportingSystem("MSDS");
    }

    private void applyArizonaDefaults(StateConfiguration config) {
        config.setEducationDepartment("Arizona Department of Education");
        config.setDepartmentAbbreviation("ADE");
        config.setDepartmentUrl("https://www.azed.gov");

        config.setCourseCodeSystemName("State Course Catalog");
        config.setStudentIdSystemName("SAIS Student ID");
        config.setStudentIdLabel("SAIS ID");

        config.setGraduationCreditsRequired(22.0);
        config.setStateReportingSystem("AzEDS");
    }

    private void applyWashingtonDefaults(StateConfiguration config) {
        config.setEducationDepartment("Office of Superintendent of Public Instruction");
        config.setDepartmentAbbreviation("OSPI");
        config.setDepartmentUrl("https://www.k12.wa.us");

        config.setCourseCodeSystemName("CEDARS Course Codes");
        config.setStudentIdSystemName("State Student ID (SSID)");
        config.setStudentIdLabel("SSID");

        config.setGraduationCreditsRequired(24.0);
        config.setStateReportingSystem("CEDARS");
    }

    private void applyColoradoDefaults(StateConfiguration config) {
        config.setEducationDepartment("Colorado Department of Education");
        config.setDepartmentAbbreviation("CDE");
        config.setDepartmentUrl("https://www.cde.state.co.us");

        config.setCourseCodeSystemName("Colorado Course Codes");
        config.setStudentIdSystemName("State Assigned Student Identifier (SASID)");
        config.setStudentIdLabel("SASID");

        config.setGraduationCreditsRequired(22.0);
        config.setStateReportingSystem("Data Pipeline");
    }

    private void applyMassachusettsDefaults(StateConfiguration config) {
        config.setEducationDepartment("Massachusetts Department of Elementary and Secondary Education");
        config.setDepartmentAbbreviation("DESE");
        config.setDepartmentUrl("https://www.doe.mass.edu");

        config.setCourseCodeSystemName("SIMS Course Codes");
        config.setStudentIdSystemName("State Assigned Student Identifier (SASID)");
        config.setStudentIdLabel("SASID");

        config.setExitExamRequired(true);
        config.setExitExamName("MCAS");
        config.setStateReportingSystem("SIMS");
    }

    private void applyVirginiaDefaults(StateConfiguration config) {
        config.setEducationDepartment("Virginia Department of Education");
        config.setDepartmentAbbreviation("VDOE");
        config.setDepartmentUrl("https://www.doe.virginia.gov");

        config.setCourseCodeSystemName("Course and Test Codes");
        config.setStudentIdSystemName("State Testing Identifier (STI)");
        config.setStudentIdLabel("STI");

        config.setGraduationCreditsRequired(22.0);
        config.setExitExamRequired(true);
        config.setExitExamName("Standards of Learning (SOL)");
        config.setStateReportingSystem("VDOE Data Collection");
    }

    private void applyGenericDefaults(StateConfiguration config) {
        // Generic defaults for states without specific configurations
        config.setEducationDepartment(config.getState().getCertifyingAgency());
        config.setCourseCodeSystemName("State Course Codes");
        config.setCourseCodeLabel("Course Code");
        config.setStudentIdSystemName("State Student ID");
        config.setStudentIdLabel("State ID");
        config.setGraduationCreditsRequired(22.0);
        config.setRequiredSchoolDays(180);
        config.setMinimumPassingGrade(60.0);
    }

    // ========================================================================
    // STATE TERMINOLOGY CREATION
    // ========================================================================

    /**
     * Create state-specific terminology entries
     */
    @Transactional
    public void createStateTerminology(USState state) {
        // Check if terminology already exists
        if (!terminologyRepository.findByStateAndActiveTrue(state).isEmpty()) {
            log.debug("Terminology already exists for {}, skipping creation", state);
            return;
        }

        List<StateTerminology> terms = new ArrayList<>();

        switch (state) {
            case TX -> terms.addAll(createTexasTerminology(state));
            case CA -> terms.addAll(createCaliforniaTerminology(state));
            case FL -> terms.addAll(createFloridaTerminology(state));
            case NY -> terms.addAll(createNewYorkTerminology(state));
            case NC -> terms.addAll(createNorthCarolinaTerminology(state));
            default -> terms.addAll(createDefaultTerminology(state));
        }

        terminologyRepository.saveAll(terms);
        log.info("Created {} terminology entries for {}", terms.size(), state);
    }

    private List<StateTerminology> createTexasTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        // Texas-specific terminology
        terms.add(StateTerminology.create(state, StateTerminology.TERM_SCHOOL, "Campus", "Campus", StateTerminology.CAT_GENERAL));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STUDENT_ID, "PEIMS ID", "PEIMS", StateTerminology.CAT_STUDENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_COURSE_CODE, "Service ID", "SID", StateTerminology.CAT_COURSE));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_ELL, "Emergent Bilingual", "EB", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STATE_ASSESSMENT, "STAAR", "STAAR", StateTerminology.CAT_ASSESSMENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_GRADING_PERIOD, "Six Weeks", "6-Wks", StateTerminology.CAT_CALENDAR));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_AT_RISK, "At-Risk", "At-Risk", StateTerminology.CAT_PROGRAM));

        return terms;
    }

    private List<StateTerminology> createCaliforniaTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        terms.add(StateTerminology.create(state, StateTerminology.TERM_SCHOOL, "School", "School", StateTerminology.CAT_GENERAL));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STUDENT_ID, "SSID", "SSID", StateTerminology.CAT_STUDENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_COURSE_CODE, "Course Code", "Code", StateTerminology.CAT_COURSE));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_ELL, "English Learner", "EL", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_GIFTED, "GATE", "GATE", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STATE_ASSESSMENT, "CAASPP", "CAASPP", StateTerminology.CAT_ASSESSMENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_GRADING_PERIOD, "Quarter", "Qtr", StateTerminology.CAT_CALENDAR));

        return terms;
    }

    private List<StateTerminology> createFloridaTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        terms.add(StateTerminology.create(state, StateTerminology.TERM_STUDENT_ID, "FLEID", "FLEID", StateTerminology.CAT_STUDENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_SPECIAL_ED, "Exceptional Student Education", "ESE", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_ELL, "English for Speakers of Other Languages", "ESOL", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STATE_ASSESSMENT, "Florida Standards Assessments", "FSA", StateTerminology.CAT_ASSESSMENT));

        return terms;
    }

    private List<StateTerminology> createNewYorkTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        terms.add(StateTerminology.create(state, StateTerminology.TERM_SCHOOL, "Building", "Bldg", StateTerminology.CAT_GENERAL));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STUDENT_ID, "NYSSIS ID", "NYSSIS", StateTerminology.CAT_STUDENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STATE_ASSESSMENT, "Regents Exams", "Regents", StateTerminology.CAT_ASSESSMENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_GRADING_PERIOD, "Marking Period", "MP", StateTerminology.CAT_CALENDAR));

        return terms;
    }

    private List<StateTerminology> createNorthCarolinaTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        terms.add(StateTerminology.create(state, StateTerminology.TERM_GIFTED, "Academically/Intellectually Gifted", "AIG", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STATE_ASSESSMENT, "NC End-of-Grade/End-of-Course", "EOG/EOC", StateTerminology.CAT_ASSESSMENT));

        return terms;
    }

    private List<StateTerminology> createDefaultTerminology(USState state) {
        List<StateTerminology> terms = new ArrayList<>();

        // Generic terms that work for most states
        terms.add(StateTerminology.create(state, StateTerminology.TERM_SCHOOL, "School", "School", StateTerminology.CAT_GENERAL));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_STUDENT_ID, "State Student ID", "ID", StateTerminology.CAT_STUDENT));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_COURSE_CODE, "Course Code", "Code", StateTerminology.CAT_COURSE));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_ELL, "English Language Learner", "ELL", StateTerminology.CAT_PROGRAM));
        terms.add(StateTerminology.create(state, StateTerminology.TERM_GRADING_PERIOD, "Grading Period", "GP", StateTerminology.CAT_CALENDAR));

        return terms;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Save configuration
     */
    @Transactional
    public StateConfiguration save(StateConfiguration config) {
        config.setUpdatedAt(LocalDateTime.now());
        return configRepository.save(config);
    }

    /**
     * Check if state configuration exists
     */
    public boolean hasConfiguration(USState state) {
        return configRepository.existsByState(state);
    }

    /**
     * Get all active configurations
     */
    public List<StateConfiguration> getAllConfigurations() {
        return configRepository.findByActiveTrueOrderByStateName();
    }
}
