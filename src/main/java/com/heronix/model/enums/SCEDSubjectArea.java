package com.heronix.model.enums;

/**
 * SCED (School Courses for the Exchange of Data) Subject Area Codes
 *
 * Based on NCES SCED Version 7.0 (2024) standard maintained by the
 * National Center for Education Statistics (NCES).
 *
 * SCED Course Identifier Structure:
 * - 5-digit code: [Subject Area (2)] + [Course Number (3)]
 * - Example: 02101 = Mathematics (02) + Algebra I (101)
 *
 * Subject Area Codes (00-24):
 * 00 - Unassigned
 * 01 - English Language Arts
 * 02 - Mathematics
 * 03 - Life and Physical Sciences
 * 04 - Social Sciences and History
 * 05 - Visual and Performing Arts
 * 06 - Foreign Language and Literature
 * 07 - Religious Education and Theology
 * 08 - Physical, Health, and Safety Education
 * 09 - Military Science
 * 10 - Information Technology
 * 11 - Communication and Audio/Visual Technology
 * 12 - Business and Marketing
 * 13 - Manufacturing
 * 14 - Health Care Sciences
 * 15 - Public, Protective, and Government Service
 * 16 - Hospitality and Tourism
 * 17 - Architecture and Construction
 * 18 - Agriculture, Food, and Natural Resources
 * 19 - Human Services
 * 20 - Transportation, Distribution, and Logistics
 * 21 - Engineering and Technology
 * 22 - Miscellaneous
 * 23 - Non-Subject-Specific
 * 24 - World Languages (expanded from 06)
 *
 * Sources:
 * - https://nces.ed.gov/forum/sced.asp
 * - https://nces.ed.gov/scedfinder/
 * - Forum Guide to SCED (NFES 2023-087)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Course Catalog Feature
 */
public enum SCEDSubjectArea {

    // ========================================================================
    // CORE ACADEMIC SUBJECTS (01-04)
    // ========================================================================

    ENGLISH_LANGUAGE_ARTS("01", "English Language Arts",
            "Reading, writing, literature, composition, and language skills",
            EducationLevel.ELEMENTARY),

    MATHEMATICS("02", "Mathematics",
            "Arithmetic, algebra, geometry, calculus, statistics, and applied math",
            EducationLevel.ELEMENTARY),

    LIFE_PHYSICAL_SCIENCES("03", "Life and Physical Sciences",
            "Biology, chemistry, physics, earth science, environmental science",
            EducationLevel.ELEMENTARY),

    SOCIAL_SCIENCES_HISTORY("04", "Social Sciences and History",
            "History, geography, civics, economics, psychology, sociology",
            EducationLevel.ELEMENTARY),

    // ========================================================================
    // ARTS AND LANGUAGES (05-07)
    // ========================================================================

    VISUAL_PERFORMING_ARTS("05", "Visual and Performing Arts",
            "Music, art, drama, dance, media arts",
            EducationLevel.ELEMENTARY),

    FOREIGN_LANGUAGE("06", "Foreign Language and Literature",
            "World languages including Spanish, French, German, Chinese, etc.",
            EducationLevel.MIDDLE_SCHOOL),

    RELIGIOUS_EDUCATION("07", "Religious Education and Theology",
            "Religious studies, theology, biblical studies (private schools)",
            EducationLevel.ELEMENTARY),

    // ========================================================================
    // HEALTH AND PHYSICAL EDUCATION (08-09)
    // ========================================================================

    PHYSICAL_HEALTH_SAFETY("08", "Physical, Health, and Safety Education",
            "Physical education, health, driver education, safety",
            EducationLevel.ELEMENTARY),

    MILITARY_SCIENCE("09", "Military Science",
            "JROTC, ROTC, military training programs",
            EducationLevel.HIGH_SCHOOL),

    // ========================================================================
    // CAREER AND TECHNICAL EDUCATION (10-21)
    // ========================================================================

    INFORMATION_TECHNOLOGY("10", "Information Technology",
            "Computer science, programming, networking, cybersecurity",
            EducationLevel.MIDDLE_SCHOOL),

    COMMUNICATION_AUDIO_VISUAL("11", "Communication and Audio/Visual Technology",
            "Journalism, broadcasting, graphic design, photography, video production",
            EducationLevel.MIDDLE_SCHOOL),

    BUSINESS_MARKETING("12", "Business and Marketing",
            "Accounting, business management, marketing, entrepreneurship, finance",
            EducationLevel.HIGH_SCHOOL),

    MANUFACTURING("13", "Manufacturing",
            "Welding, machining, production, quality assurance",
            EducationLevel.HIGH_SCHOOL),

    HEALTH_CARE_SCIENCES("14", "Health Care Sciences",
            "Nursing, medical assisting, pharmacy tech, dental, EMT",
            EducationLevel.HIGH_SCHOOL),

    PUBLIC_PROTECTIVE_GOVERNMENT("15", "Public, Protective, and Government Service",
            "Criminal justice, fire science, law enforcement, legal studies",
            EducationLevel.HIGH_SCHOOL),

    HOSPITALITY_TOURISM("16", "Hospitality and Tourism",
            "Culinary arts, hotel management, travel and tourism",
            EducationLevel.HIGH_SCHOOL),

    ARCHITECTURE_CONSTRUCTION("17", "Architecture and Construction",
            "Carpentry, electrical, plumbing, HVAC, architectural design",
            EducationLevel.HIGH_SCHOOL),

    AGRICULTURE_FOOD_NATURAL_RESOURCES("18", "Agriculture, Food, and Natural Resources",
            "Animal science, plant science, agricultural mechanics, FFA",
            EducationLevel.MIDDLE_SCHOOL),

    HUMAN_SERVICES("19", "Human Services",
            "Cosmetology, child development, family studies, counseling",
            EducationLevel.HIGH_SCHOOL),

    TRANSPORTATION_DISTRIBUTION_LOGISTICS("20", "Transportation, Distribution, and Logistics",
            "Automotive, aviation, logistics, supply chain management",
            EducationLevel.HIGH_SCHOOL),

    ENGINEERING_TECHNOLOGY("21", "Engineering and Technology",
            "Pre-engineering, robotics, CAD, STEM, Project Lead The Way",
            EducationLevel.MIDDLE_SCHOOL),

    // ========================================================================
    // MISCELLANEOUS AND NON-SUBJECT (22-24)
    // ========================================================================

    MISCELLANEOUS("22", "Miscellaneous",
            "Study skills, test preparation, independent study, tutoring",
            EducationLevel.ELEMENTARY),

    NON_SUBJECT_SPECIFIC("23", "Non-Subject-Specific",
            "Homeroom, advisory, orientation, study hall, library",
            EducationLevel.ELEMENTARY),

    WORLD_LANGUAGES("24", "World Languages",
            "Extended world language codes beyond subject area 06",
            EducationLevel.MIDDLE_SCHOOL);

    // ========================================================================
    // FIELDS AND CONSTRUCTOR
    // ========================================================================

    private final String code;
    private final String displayName;
    private final String description;
    private final EducationLevel minimumLevel;

    SCEDSubjectArea(String code, String displayName, String description, EducationLevel minimumLevel) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.minimumLevel = minimumLevel;
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    /**
     * Get the 2-digit SCED subject area code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the display name for the subject area
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description of what this subject area covers
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the minimum education level where this subject is typically offered
     */
    public EducationLevel getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * Get a formatted code with display name
     */
    public String getCodeWithName() {
        return code + " - " + displayName;
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================

    /**
     * Find subject area by SCED code (e.g., "01", "02")
     */
    public static SCEDSubjectArea fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String normalized = code.trim();
        // Pad single digit to 2 digits
        if (normalized.length() == 1) {
            normalized = "0" + normalized;
        }
        for (SCEDSubjectArea area : values()) {
            if (area.code.equals(normalized)) {
                return area;
            }
        }
        return null;
    }

    /**
     * Extract subject area from a 5-digit SCED course code
     * @param scedCode Full 5-digit course code (e.g., "02101" for Algebra I)
     * @return Subject area, or null if invalid
     */
    public static SCEDSubjectArea fromCourseCode(String scedCode) {
        if (scedCode == null || scedCode.length() < 2) {
            return null;
        }
        return fromCode(scedCode.substring(0, 2));
    }

    /**
     * Check if this is a core academic subject (ELA, Math, Science, Social Studies)
     */
    public boolean isCoreAcademic() {
        return this == ENGLISH_LANGUAGE_ARTS ||
               this == MATHEMATICS ||
               this == LIFE_PHYSICAL_SCIENCES ||
               this == SOCIAL_SCIENCES_HISTORY;
    }

    /**
     * Check if this is a Career and Technical Education (CTE) subject
     */
    public boolean isCTE() {
        int codeInt = Integer.parseInt(code);
        return codeInt >= 10 && codeInt <= 21;
    }

    /**
     * Check if this subject area is typically offered at a given education level
     */
    public boolean isAvailableAt(EducationLevel level) {
        if (level == null || minimumLevel == null) {
            return true;
        }
        // Order: PRE_K < KINDERGARTEN < ELEMENTARY < MIDDLE_SCHOOL < HIGH_SCHOOL
        return level.ordinal() >= minimumLevel.ordinal();
    }

    @Override
    public String toString() {
        return code + " - " + displayName;
    }
}
