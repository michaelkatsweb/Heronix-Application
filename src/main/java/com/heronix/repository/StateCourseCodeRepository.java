package com.heronix.repository;

import com.heronix.model.domain.StateCourseCode;
import com.heronix.model.enums.CourseCategory;
import com.heronix.model.enums.CourseType;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.SCEDSubjectArea;
import com.heronix.model.enums.USState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StateCourseCode entity
 *
 * Provides comprehensive queries for:
 * - Finding courses by state
 * - Filtering by grade level
 * - Searching by subject area (SCED)
 * - Finding AP, IB, CTE courses
 * - Searching by name/code
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Course Catalog Feature
 */
@Repository
public interface StateCourseCodeRepository extends JpaRepository<StateCourseCode, Long> {

    // ========================================================================
    // BASIC STATE QUERIES
    // ========================================================================

    /**
     * Find all course codes for a specific state
     */
    List<StateCourseCode> findByStateOrderByStateCourseCodeAsc(USState state);

    /**
     * Find all active course codes for a specific state
     */
    List<StateCourseCode> findByStateAndActiveTrueOrderByStateCourseCodeAsc(USState state);

    /**
     * Find course by state and state course code
     */
    Optional<StateCourseCode> findByStateAndStateCourseCode(USState state, String stateCourseCode);

    /**
     * Find courses by state and SCED code
     */
    List<StateCourseCode> findByStateAndScedCode(USState state, String scedCode);

    /**
     * Check if a state course code exists
     */
    boolean existsByStateAndStateCourseCode(USState state, String stateCourseCode);

    // ========================================================================
    // GRADE LEVEL QUERIES
    // ========================================================================

    /**
     * Find courses available for a specific grade level in a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND (scc.minGradeLevel IS NULL OR scc.minGradeLevel <= :gradeLevel) " +
           "AND (scc.maxGradeLevel IS NULL OR scc.maxGradeLevel >= :gradeLevel) " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findByStateAndGradeLevel(
            @Param("state") USState state,
            @Param("gradeLevel") Integer gradeLevel);

    /**
     * Find courses within a grade range
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND (scc.minGradeLevel IS NULL OR scc.minGradeLevel <= :maxGrade) " +
           "AND (scc.maxGradeLevel IS NULL OR scc.maxGradeLevel >= :minGrade) " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findByStateAndGradeRange(
            @Param("state") USState state,
            @Param("minGrade") Integer minGrade,
            @Param("maxGrade") Integer maxGrade);

    /**
     * Find elementary courses (K-5) for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.educationLevel IN ('KINDERGARTEN', 'ELEMENTARY') " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findElementaryCourses(@Param("state") USState state);

    /**
     * Find middle school courses (6-8) for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.educationLevel = 'MIDDLE_SCHOOL' " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findMiddleSchoolCourses(@Param("state") USState state);

    /**
     * Find high school courses (9-12) for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.educationLevel = 'HIGH_SCHOOL' " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findHighSchoolCourses(@Param("state") USState state);

    // ========================================================================
    // SUBJECT AREA QUERIES
    // ========================================================================

    /**
     * Find courses by state and subject area
     */
    List<StateCourseCode> findByStateAndSubjectAreaAndActiveTrueOrderByCourseName(
            USState state, SCEDSubjectArea subjectArea);

    /**
     * Find courses by state and subject area for a grade level
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.subjectArea = :subjectArea " +
           "AND scc.active = true " +
           "AND (scc.minGradeLevel IS NULL OR scc.minGradeLevel <= :gradeLevel) " +
           "AND (scc.maxGradeLevel IS NULL OR scc.maxGradeLevel >= :gradeLevel) " +
           "ORDER BY scc.courseName")
    List<StateCourseCode> findByStateAndSubjectAreaAndGradeLevel(
            @Param("state") USState state,
            @Param("subjectArea") SCEDSubjectArea subjectArea,
            @Param("gradeLevel") Integer gradeLevel);

    /**
     * Find core academic courses (ELA, Math, Science, Social Studies)
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.subjectArea IN ('ENGLISH_LANGUAGE_ARTS', 'MATHEMATICS', " +
           "'LIFE_PHYSICAL_SCIENCES', 'SOCIAL_SCIENCES_HISTORY') " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findCoreAcademicCourses(@Param("state") USState state);

    /**
     * Find elective courses
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.courseCategory = 'ELECTIVE' " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findElectiveCourses(@Param("state") USState state);

    // ========================================================================
    // COURSE TYPE QUERIES
    // ========================================================================

    /**
     * Find AP courses for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isAP = true " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findAPCourses(@Param("state") USState state);

    /**
     * Find IB courses for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isIB = true " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findIBCourses(@Param("state") USState state);

    /**
     * Find dual credit courses for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isDualCredit = true " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findDualCreditCourses(@Param("state") USState state);

    /**
     * Find CTE courses for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isCTE = true " +
           "ORDER BY scc.cteCluster, scc.courseName")
    List<StateCourseCode> findCTECourses(@Param("state") USState state);

    /**
     * Find CTE courses by career cluster
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isCTE = true " +
           "AND scc.cteCluster = :cluster " +
           "ORDER BY scc.courseName")
    List<StateCourseCode> findCTECoursesByCluster(
            @Param("state") USState state,
            @Param("cluster") String cluster);

    /**
     * Find STEM courses for a state
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.isSTEM = true " +
           "ORDER BY scc.subjectArea, scc.courseName")
    List<StateCourseCode> findSTEMCourses(@Param("state") USState state);

    /**
     * Find courses by course type (REGULAR, HONORS, AP, etc.)
     */
    List<StateCourseCode> findByStateAndCourseTypeAndActiveTrueOrderByCourseName(
            USState state, CourseType courseType);

    // ========================================================================
    // GRADUATION REQUIREMENT QUERIES
    // ========================================================================

    /**
     * Find courses that satisfy graduation requirements
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.graduationRequirement = true " +
           "ORDER BY scc.graduationRequirementType, scc.courseName")
    List<StateCourseCode> findGraduationRequirementCourses(@Param("state") USState state);

    /**
     * Find courses for a specific graduation requirement type
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true AND scc.graduationRequirement = true " +
           "AND scc.graduationRequirementType = :requirementType " +
           "ORDER BY scc.courseName")
    List<StateCourseCode> findByGraduationRequirementType(
            @Param("state") USState state,
            @Param("requirementType") String requirementType);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search courses by name (case-insensitive partial match)
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND LOWER(scc.courseName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY scc.courseName")
    List<StateCourseCode> searchByCourseName(
            @Param("state") USState state,
            @Param("searchTerm") String searchTerm);

    /**
     * Search courses by code (partial match)
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND scc.stateCourseCode LIKE CONCAT('%', :codePattern, '%') " +
           "ORDER BY scc.stateCourseCode")
    List<StateCourseCode> searchByCode(
            @Param("state") USState state,
            @Param("codePattern") String codePattern);

    /**
     * Full text search (name, code, or description)
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.state = :state " +
           "AND scc.active = true " +
           "AND (LOWER(scc.courseName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR scc.stateCourseCode LIKE CONCAT('%', :searchTerm, '%') " +
           "OR LOWER(scc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY scc.courseName")
    List<StateCourseCode> searchAll(
            @Param("state") USState state,
            @Param("searchTerm") String searchTerm);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count courses by state
     */
    long countByStateAndActiveTrue(USState state);

    /**
     * Count courses by state and education level
     */
    @Query("SELECT scc.educationLevel, COUNT(scc) FROM StateCourseCode scc " +
           "WHERE scc.state = :state AND scc.active = true " +
           "GROUP BY scc.educationLevel")
    List<Object[]> countByEducationLevel(@Param("state") USState state);

    /**
     * Count courses by state and subject area
     */
    @Query("SELECT scc.subjectArea, COUNT(scc) FROM StateCourseCode scc " +
           "WHERE scc.state = :state AND scc.active = true " +
           "GROUP BY scc.subjectArea")
    List<Object[]> countBySubjectArea(@Param("state") USState state);

    /**
     * Count courses by state and course type
     */
    @Query("SELECT scc.courseType, COUNT(scc) FROM StateCourseCode scc " +
           "WHERE scc.state = :state AND scc.active = true " +
           "GROUP BY scc.courseType")
    List<Object[]> countByCourseType(@Param("state") USState state);

    /**
     * Get list of all states with course data
     */
    @Query("SELECT DISTINCT scc.state FROM StateCourseCode scc WHERE scc.active = true ORDER BY scc.state")
    List<USState> findStatesWithCourseData();

    /**
     * Get list of CTE clusters for a state
     */
    @Query("SELECT DISTINCT scc.cteCluster FROM StateCourseCode scc " +
           "WHERE scc.state = :state AND scc.active = true AND scc.isCTE = true " +
           "AND scc.cteCluster IS NOT NULL ORDER BY scc.cteCluster")
    List<String> findCTEClusters(@Param("state") USState state);

    // ========================================================================
    // SCED MAPPING QUERIES
    // ========================================================================

    /**
     * Find course codes by SCED code across all states
     */
    List<StateCourseCode> findByScedCodeAndActiveTrue(String scedCode);

    /**
     * Find state codes that map to a SCED subject area
     */
    @Query("SELECT scc FROM StateCourseCode scc WHERE scc.subjectArea = :subjectArea " +
           "AND scc.active = true ORDER BY scc.state, scc.courseName")
    List<StateCourseCode> findBySCEDSubjectArea(@Param("subjectArea") SCEDSubjectArea subjectArea);

    // ========================================================================
    // SCHOOL YEAR QUERIES
    // ========================================================================

    /**
     * Find courses for a specific school year
     */
    List<StateCourseCode> findByStateAndSchoolYearAndActiveTrue(USState state, String schoolYear);

    /**
     * Get available school years for a state
     */
    @Query("SELECT DISTINCT scc.schoolYear FROM StateCourseCode scc " +
           "WHERE scc.state = :state AND scc.schoolYear IS NOT NULL " +
           "ORDER BY scc.schoolYear DESC")
    List<String> findSchoolYears(@Param("state") USState state);

    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================

    /**
     * Delete all course codes for a state and school year
     */
    void deleteByStateAndSchoolYear(USState state, String schoolYear);

    /**
     * Deactivate all courses for a state
     */
    @Query("UPDATE StateCourseCode scc SET scc.active = false WHERE scc.state = :state")
    void deactivateByState(@Param("state") USState state);
}
