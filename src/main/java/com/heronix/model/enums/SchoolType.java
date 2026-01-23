package com.heronix.model.enums;

import java.util.Arrays;
import java.util.List;

/**
 * SchoolType Enum - Defines the type of school installation
 *
 * This is set during initial installation and determines:
 * - Which grade levels are available for student registration
 * - Which courses are displayed and available
 * - Age-appropriate features and workflows
 * - Default configurations for the school
 *
 * US Education System Structure:
 * - Elementary School: Pre-K through 5th Grade (Ages 3-11)
 * - Middle School: 6th through 8th Grade (Ages 11-14)
 * - High School: 9th through 12th Grade (Ages 14-18)
 * - K-8 School: Kindergarten through 8th Grade (combined)
 * - K-12 School: Full spectrum (all grades)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
public enum SchoolType {

    ELEMENTARY("Elementary School", "Pre-K through 5th Grade",
            GradeLevel.PRE_K, GradeLevel.GRADE_5, -1, 5),

    MIDDLE_SCHOOL("Middle School", "6th through 8th Grade",
            GradeLevel.GRADE_6, GradeLevel.GRADE_8, 6, 8),

    HIGH_SCHOOL("High School", "9th through 12th Grade",
            GradeLevel.GRADE_9, GradeLevel.GRADE_12, 9, 12),

    K8_SCHOOL("K-8 School", "Kindergarten through 8th Grade",
            GradeLevel.KINDERGARTEN, GradeLevel.GRADE_8, 0, 8),

    K12_SCHOOL("K-12 School", "All Grades (Pre-K through 12th)",
            GradeLevel.PRE_K, GradeLevel.GRADE_12, -1, 12),

    EARLY_CHILDHOOD("Early Childhood Center", "Pre-K and Kindergarten Only",
            GradeLevel.PRE_K, GradeLevel.KINDERGARTEN, -1, 0);

    private final String displayName;
    private final String description;
    private final GradeLevel minGrade;
    private final GradeLevel maxGrade;
    private final int minGradeValue;
    private final int maxGradeValue;

    SchoolType(String displayName, String description, GradeLevel minGrade, GradeLevel maxGrade,
               int minGradeValue, int maxGradeValue) {
        this.displayName = displayName;
        this.description = description;
        this.minGrade = minGrade;
        this.maxGrade = maxGrade;
        this.minGradeValue = minGradeValue;
        this.maxGradeValue = maxGradeValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public GradeLevel getMinGrade() {
        return minGrade;
    }

    public GradeLevel getMaxGrade() {
        return maxGrade;
    }

    public int getMinGradeValue() {
        return minGradeValue;
    }

    public int getMaxGradeValue() {
        return maxGradeValue;
    }

    /**
     * Check if a grade level is valid for this school type
     */
    public boolean isGradeValid(GradeLevel grade) {
        if (grade == null) return false;
        int gradeValue = grade.getNumericValue();
        return gradeValue >= minGradeValue && gradeValue <= maxGradeValue;
    }

    /**
     * Check if a numeric grade value is valid for this school type
     */
    public boolean isGradeValid(int gradeValue) {
        return gradeValue >= minGradeValue && gradeValue <= maxGradeValue;
    }

    /**
     * Get all valid grade levels for this school type
     */
    public List<GradeLevel> getValidGrades() {
        return Arrays.stream(GradeLevel.values())
                .filter(g -> g.getNumericValue() >= minGradeValue && g.getNumericValue() <= maxGradeValue)
                .toList();
    }

    /**
     * Get the grade range display string
     */
    public String getGradeRangeDisplay() {
        if (minGrade == maxGrade) {
            return minGrade.getDisplayName() + " only";
        }
        return minGrade.getDisplayName() + " to " + maxGrade.getDisplayName();
    }

    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
}
