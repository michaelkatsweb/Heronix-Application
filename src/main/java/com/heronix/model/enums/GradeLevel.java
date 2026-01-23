package com.heronix.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GradeLevel Enum - Standard US K-12 Education Grade Levels
 *
 * Represents all grade levels from Pre-K through 12th grade following
 * US Department of Education standards and state requirements.
 *
 * Grade Level Structure:
 * - Pre-K (Age 3-4): Early childhood education, not mandatory
 * - Kindergarten (Age 5-6): First year of formal education
 * - Elementary School (Grades 1-5, Ages 6-11)
 * - Middle School (Grades 6-8, Ages 11-14)
 * - High School (Grades 9-12, Ages 14-18)
 *
 * Sources:
 * - US Department of Education
 * - Common Core State Standards
 * - State education requirements
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
public enum GradeLevel {
    // Early Childhood
    PRE_K(-1, "Pre-K", "Pre-Kindergarten", EducationLevel.PRE_K, 3, 4),
    KINDERGARTEN(0, "K", "Kindergarten", EducationLevel.KINDERGARTEN, 5, 6),

    // Elementary School (Grades 1-5)
    GRADE_1(1, "1st Grade", "First Grade", EducationLevel.ELEMENTARY, 6, 7),
    GRADE_2(2, "2nd Grade", "Second Grade", EducationLevel.ELEMENTARY, 7, 8),
    GRADE_3(3, "3rd Grade", "Third Grade", EducationLevel.ELEMENTARY, 8, 9),
    GRADE_4(4, "4th Grade", "Fourth Grade", EducationLevel.ELEMENTARY, 9, 10),
    GRADE_5(5, "5th Grade", "Fifth Grade", EducationLevel.ELEMENTARY, 10, 11),

    // Middle School (Grades 6-8)
    GRADE_6(6, "6th Grade", "Sixth Grade", EducationLevel.MIDDLE_SCHOOL, 11, 12),
    GRADE_7(7, "7th Grade", "Seventh Grade", EducationLevel.MIDDLE_SCHOOL, 12, 13),
    GRADE_8(8, "8th Grade", "Eighth Grade", EducationLevel.MIDDLE_SCHOOL, 13, 14),

    // High School (Grades 9-12)
    GRADE_9(9, "9th Grade", "Freshman", EducationLevel.HIGH_SCHOOL, 14, 15),
    GRADE_10(10, "10th Grade", "Sophomore", EducationLevel.HIGH_SCHOOL, 15, 16),
    GRADE_11(11, "11th Grade", "Junior", EducationLevel.HIGH_SCHOOL, 16, 17),
    GRADE_12(12, "12th Grade", "Senior", EducationLevel.HIGH_SCHOOL, 17, 18);

    private final int numericValue;
    private final String displayName;
    private final String alternativeName;
    private final EducationLevel educationLevel;
    private final int typicalAgeMin;
    private final int typicalAgeMax;

    GradeLevel(int numericValue, String displayName, String alternativeName,
               EducationLevel educationLevel, int typicalAgeMin, int typicalAgeMax) {
        this.numericValue = numericValue;
        this.displayName = displayName;
        this.alternativeName = alternativeName;
        this.educationLevel = educationLevel;
        this.typicalAgeMin = typicalAgeMin;
        this.typicalAgeMax = typicalAgeMax;
    }

    public int getNumericValue() {
        return numericValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public int getTypicalAgeMin() {
        return typicalAgeMin;
    }

    public int getTypicalAgeMax() {
        return typicalAgeMax;
    }

    /**
     * Get the age range description for this grade level
     */
    public String getAgeRange() {
        return typicalAgeMin + "-" + typicalAgeMax + " years";
    }

    /**
     * Check if this grade is in elementary school (K-5)
     */
    public boolean isElementary() {
        return educationLevel == EducationLevel.ELEMENTARY ||
               educationLevel == EducationLevel.KINDERGARTEN;
    }

    /**
     * Check if this grade is in middle school (6-8)
     */
    public boolean isMiddleSchool() {
        return educationLevel == EducationLevel.MIDDLE_SCHOOL;
    }

    /**
     * Check if this grade is in high school (9-12)
     */
    public boolean isHighSchool() {
        return educationLevel == EducationLevel.HIGH_SCHOOL;
    }

    /**
     * Check if this grade is Pre-K or Kindergarten
     */
    public boolean isEarlyChildhood() {
        return this == PRE_K || this == KINDERGARTEN;
    }

    /**
     * Get the next grade level (for promotion)
     * @return next grade level, or null if already at GRADE_12
     */
    public GradeLevel getNextGrade() {
        if (this == GRADE_12) {
            return null; // Graduated
        }
        int nextValue = this.numericValue + 1;
        return fromNumericValue(nextValue);
    }

    /**
     * Get the previous grade level (for retention)
     * @return previous grade level, or null if already at PRE_K
     */
    public GradeLevel getPreviousGrade() {
        if (this == PRE_K) {
            return null;
        }
        int prevValue = this.numericValue - 1;
        return fromNumericValue(prevValue);
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================

    /**
     * Get GradeLevel from numeric value (-1 for Pre-K, 0 for K, 1-12 for grades)
     */
    public static GradeLevel fromNumericValue(int value) {
        for (GradeLevel grade : values()) {
            if (grade.numericValue == value) {
                return grade;
            }
        }
        return null;
    }

    /**
     * Parse grade level from string (handles various formats)
     * Examples: "Pre-K", "K", "Kindergarten", "1", "1st", "1st Grade", "9th Grade", "Freshman"
     */
    public static GradeLevel fromString(String gradeStr) {
        if (gradeStr == null || gradeStr.trim().isEmpty()) {
            return null;
        }

        String normalized = gradeStr.trim().toUpperCase();

        // Handle Pre-K variations
        if (normalized.equals("PRE-K") || normalized.equals("PREK") ||
            normalized.equals("PRE-KINDERGARTEN") || normalized.equals("PK")) {
            return PRE_K;
        }

        // Handle Kindergarten variations
        if (normalized.equals("K") || normalized.equals("KINDERGARTEN") ||
            normalized.equals("KG") || normalized.equals("KINDER")) {
            return KINDERGARTEN;
        }

        // Handle high school alternative names
        if (normalized.equals("FRESHMAN") || normalized.equals("FRESH")) {
            return GRADE_9;
        }
        if (normalized.equals("SOPHOMORE") || normalized.equals("SOPH")) {
            return GRADE_10;
        }
        if (normalized.equals("JUNIOR") || normalized.equals("JR")) {
            return GRADE_11;
        }
        if (normalized.equals("SENIOR") || normalized.equals("SR")) {
            return GRADE_12;
        }

        // Try to extract numeric value
        String numStr = normalized
                .replaceAll("(ST|ND|RD|TH)\\s*GRADE", "")
                .replaceAll("GRADE\\s*", "")
                .replaceAll("(ST|ND|RD|TH)", "")
                .trim();

        try {
            int numValue = Integer.parseInt(numStr);
            return fromNumericValue(numValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get all grade levels for a specific education level
     */
    public static List<GradeLevel> getGradesForLevel(EducationLevel level) {
        return Arrays.stream(values())
                .filter(g -> g.educationLevel == level)
                .collect(Collectors.toList());
    }

    /**
     * Get all elementary grades (K-5)
     */
    public static List<GradeLevel> getElementaryGrades() {
        return Arrays.asList(KINDERGARTEN, GRADE_1, GRADE_2, GRADE_3, GRADE_4, GRADE_5);
    }

    /**
     * Get all middle school grades (6-8)
     */
    public static List<GradeLevel> getMiddleSchoolGrades() {
        return Arrays.asList(GRADE_6, GRADE_7, GRADE_8);
    }

    /**
     * Get all high school grades (9-12)
     */
    public static List<GradeLevel> getHighSchoolGrades() {
        return Arrays.asList(GRADE_9, GRADE_10, GRADE_11, GRADE_12);
    }

    /**
     * Get all grade levels as display strings for UI dropdowns
     */
    public static List<String> getAllDisplayNames() {
        return Arrays.stream(values())
                .map(GradeLevel::getDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Check if a course grade range is appropriate for a student grade
     * @param studentGrade The student's grade level
     * @param minCourseGrade Minimum grade for the course (can be null for no minimum)
     * @param maxCourseGrade Maximum grade for the course (can be null for no maximum)
     * @return true if the student can take this course
     */
    public static boolean isGradeEligibleForCourse(GradeLevel studentGrade,
                                                    Integer minCourseGrade,
                                                    Integer maxCourseGrade) {
        if (studentGrade == null) {
            return false;
        }

        int studentValue = studentGrade.getNumericValue();

        // Check minimum grade requirement
        if (minCourseGrade != null && studentValue < minCourseGrade) {
            return false;
        }

        // Check maximum grade requirement
        if (maxCourseGrade != null && studentValue > maxCourseGrade) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
