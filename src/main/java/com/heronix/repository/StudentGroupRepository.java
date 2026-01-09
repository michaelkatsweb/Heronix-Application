package com.heronix.repository;

import com.heronix.model.domain.StudentGroup;
import com.heronix.model.domain.StudentGroup.GroupType;
import com.heronix.model.domain.StudentGroup.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentGroup entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find by group name
     */
    Optional<StudentGroup> findByGroupName(String groupName);

    /**
     * Find by group code
     */
    Optional<StudentGroup> findByGroupCode(String groupCode);

    /**
     * Find by type
     */
    List<StudentGroup> findByGroupType(GroupType groupType);

    /**
     * Find by status
     */
    List<StudentGroup> findByStatus(GroupStatus status);

    /**
     * Find by type and status
     */
    List<StudentGroup> findByGroupTypeAndStatus(GroupType groupType, GroupStatus status);

    /**
     * Find active groups by type
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.groupType = :groupType AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveByType(@Param("groupType") GroupType groupType);

    // ========================================================================
    // ACADEMIC YEAR QUERIES
    // ========================================================================

    /**
     * Find by academic year
     */
    List<StudentGroup> findByAcademicYear(String academicYear);

    /**
     * Find by academic year and type
     */
    List<StudentGroup> findByAcademicYearAndGroupType(String academicYear, GroupType groupType);

    /**
     * Find active groups for academic year
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.academicYear = :academicYear AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveByAcademicYear(@Param("academicYear") String academicYear);

    // ========================================================================
    // GRADE LEVEL QUERIES
    // ========================================================================

    /**
     * Find by grade level
     */
    List<StudentGroup> findByGradeLevel(String gradeLevel);

    /**
     * Find by grade level and type
     */
    List<StudentGroup> findByGradeLevelAndGroupType(String gradeLevel, GroupType groupType);

    /**
     * Find active groups for grade level
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.gradeLevel = :gradeLevel AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveByGradeLevel(@Param("gradeLevel") String gradeLevel);

    // ========================================================================
    // STAFF/ADVISOR QUERIES
    // ========================================================================

    /**
     * Find groups by primary advisor
     */
    List<StudentGroup> findByPrimaryAdvisorId(Long advisorId);

    /**
     * Find groups by secondary advisor
     */
    List<StudentGroup> findBySecondaryAdvisorId(Long advisorId);

    /**
     * Find active groups by primary advisor
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.primaryAdvisor.id = :advisorId AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveByPrimaryAdvisor(@Param("advisorId") Long advisorId);

    /**
     * Find groups by homeroom teacher
     */
    List<StudentGroup> findByHomeroomTeacherId(Long teacherId);

    /**
     * Find active homerooms by teacher
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.homeroomTeacher.id = :teacherId " +
           "AND sg.isHomeroom = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveHomeroomsByTeacher(@Param("teacherId") Long teacherId);

    // ========================================================================
    // TYPE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Find all homerooms
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isHomeroom = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllHomerooms();

    /**
     * Find homeroom by number
     */
    Optional<StudentGroup> findByHomeroomNumber(String homeroomNumber);

    /**
     * Find all advisory groups
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isAdvisory = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllAdvisories();

    /**
     * Find all cohorts
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isCohort = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllCohorts();

    /**
     * Find cohort by graduation year
     */
    Optional<StudentGroup> findByGraduationYear(Integer graduationYear);

    /**
     * Find all houses
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isHouse = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllHouses();

    /**
     * Find house by name
     */
    Optional<StudentGroup> findByHouseName(String houseName);

    /**
     * Find all teams
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isTeam = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllTeams();

    /**
     * Find all learning communities
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isLearningCommunity = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllLearningCommunities();

    /**
     * Find all academic tracks
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isAcademicTrack = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllAcademicTracks();

    /**
     * Find all extracurricular clubs
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isExtracurricular = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllExtracurricularClubs();

    /**
     * Find all intervention groups
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.isInterventionGroup = true AND sg.status = 'ACTIVE'")
    List<StudentGroup> findAllInterventionGroups();

    // ========================================================================
    // STUDENT MEMBERSHIP QUERIES
    // ========================================================================

    /**
     * Find groups for a student
     */
    @Query("SELECT sg FROM StudentGroup sg JOIN sg.students s WHERE s.id = :studentId")
    List<StudentGroup> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find active groups for a student
     */
    @Query("SELECT sg FROM StudentGroup sg JOIN sg.students s WHERE s.id = :studentId AND sg.status = 'ACTIVE'")
    List<StudentGroup> findActiveByStudentId(@Param("studentId") Long studentId);

    /**
     * Find groups for student by type
     */
    @Query("SELECT sg FROM StudentGroup sg JOIN sg.students s " +
           "WHERE s.id = :studentId AND sg.groupType = :groupType AND sg.status = 'ACTIVE'")
    List<StudentGroup> findByStudentIdAndType(@Param("studentId") Long studentId, @Param("groupType") GroupType groupType);

    /**
     * Check if student is in group
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StudentGroup sg JOIN sg.students s " +
           "WHERE sg.id = :groupId AND s.id = :studentId")
    boolean isStudentInGroup(@Param("groupId") Long groupId, @Param("studentId") Long studentId);

    // ========================================================================
    // CAPACITY QUERIES
    // ========================================================================

    /**
     * Find groups with available capacity
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.status = 'ACTIVE' " +
           "AND sg.acceptingNewMembers = true " +
           "AND (sg.maxCapacity IS NULL OR sg.currentEnrollment < sg.maxCapacity)")
    List<StudentGroup> findGroupsWithCapacity();

    /**
     * Find full groups
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.status = 'ACTIVE' " +
           "AND sg.maxCapacity IS NOT NULL AND sg.currentEnrollment >= sg.maxCapacity")
    List<StudentGroup> findFullGroups();

    /**
     * Find groups near capacity (e.g., 90% full)
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE sg.status = 'ACTIVE' " +
           "AND sg.maxCapacity IS NOT NULL " +
           "AND (sg.currentEnrollment * 100.0 / sg.maxCapacity) >= :percentage")
    List<StudentGroup> findGroupsNearCapacity(@Param("percentage") double percentage);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search groups by name
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE LOWER(sg.groupName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<StudentGroup> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search groups by name or code
     */
    @Query("SELECT sg FROM StudentGroup sg WHERE " +
           "LOWER(sg.groupName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sg.groupCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<StudentGroup> searchByNameOrCode(@Param("searchTerm") String searchTerm);

    // ========================================================================
    // STATISTICAL QUERIES
    // ========================================================================

    /**
     * Count by type
     */
    long countByGroupType(GroupType groupType);

    /**
     * Count by status
     */
    long countByStatus(GroupStatus status);

    /**
     * Count active groups
     */
    @Query("SELECT COUNT(sg) FROM StudentGroup sg WHERE sg.status = 'ACTIVE'")
    long countActive();

    /**
     * Get total enrollment across all groups
     */
    @Query("SELECT SUM(sg.currentEnrollment) FROM StudentGroup sg WHERE sg.status = 'ACTIVE'")
    Long getTotalEnrollment();

    /**
     * Get average group size
     */
    @Query("SELECT AVG(sg.currentEnrollment) FROM StudentGroup sg WHERE sg.status = 'ACTIVE' AND sg.currentEnrollment > 0")
    Double getAverageGroupSize();

    /**
     * Get statistics by group type
     */
    @Query("SELECT sg.groupType, COUNT(sg), SUM(sg.currentEnrollment) FROM StudentGroup sg " +
           "WHERE sg.status = 'ACTIVE' GROUP BY sg.groupType")
    List<Object[]> getGroupStatistics();

    /**
     * Get capacity utilization
     */
    @Query("SELECT sg.groupType, AVG(sg.currentEnrollment * 100.0 / sg.maxCapacity) " +
           "FROM StudentGroup sg WHERE sg.status = 'ACTIVE' AND sg.maxCapacity IS NOT NULL " +
           "GROUP BY sg.groupType")
    List<Object[]> getCapacityUtilization();
}
