package com.heronix.repository;

import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Find student by student ID
    Optional<Student> findByStudentId(String studentId);

    // Find student by email
    Optional<Student> findByEmail(String email);

    // Find student by QR code ID (for QR attendance system)
    Optional<Student> findByQrCodeId(String qrCodeId);

    // Check if email exists (for duplicate detection)
    boolean existsByEmail(String email);

    // Check if student ID exists (for duplicate detection)
    boolean existsByStudentId(String studentId);

    // Find all active students (excluding soft-deleted)
    @Query("SELECT s FROM Student s WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL)")
    List<Student> findByActiveTrue();

    @Query("SELECT s FROM Student s WHERE s.active = :active AND (s.deleted = false OR s.deleted IS NULL)")
    List<Student> findByActive(@org.springframework.data.repository.query.Param("active") boolean active);

    // Find students by grade level
    List<Student> findByGradeLevel(String gradeLevel);

    // Search students by name
    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> searchByName(String name);

    // Find students in a specific schedule slot
    @Query("SELECT s FROM Student s JOIN s.scheduleSlots slot WHERE slot.id = :slotId")
    List<Student> findByScheduleSlotId(Long slotId);
    // Find active students by grade level
    List<Student> findByGradeLevelAndActiveTrue(String gradeLevel);

    // Count active students by grade level
    int countByGradeLevelAndActiveTrue(String gradeLevel);

    // Find graduated students
    List<Student> findByGraduatedTrue();

    // Find students by graduation year
    List<Student> findByGraduationYear(Integer graduationYear);

    // Find students enrolled in a specific course (with JOIN FETCH to avoid N+1)
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.enrolledCourses ec " +
           "WHERE :course MEMBER OF s.enrolledCourses")
    List<Student> findStudentsEnrolledInCourse(@org.springframework.data.repository.query.Param("course") com.heronix.model.domain.Course course);

    // Find all students with enrolled courses loaded (prevents LazyInitializationException)
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.enrolledCourses")
    List<Student> findAllWithEnrolledCourses();

    // Find student by ID with enrolled courses eagerly loaded
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.enrolledCourses WHERE s.id = :id")
    Optional<Student> findByIdWithEnrolledCourses(@org.springframework.data.repository.query.Param("id") Long id);

    // ========================================================================
    // SOFT DELETE SUPPORT
    // ========================================================================

    /**
     * Find all non-deleted students (active and inactive)
     * Use this instead of findAll() to exclude soft-deleted records
     */
    @Query("SELECT s FROM Student s WHERE s.deleted = false OR s.deleted IS NULL")
    List<Student> findAllNonDeleted();

    /**
     * Find all active students excluding soft-deleted
     */
    @Query("SELECT s FROM Student s WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL)")
    List<Student> findAllActive();

    /**
     * Find soft-deleted students (for audit/recovery purposes)
     */
    @Query("SELECT s FROM Student s WHERE s.deleted = true")
    List<Student> findDeleted();

    /**
     * Find students deleted by a specific user
     */
    @Query("SELECT s FROM Student s WHERE s.deleted = true AND s.deletedBy = :username")
    List<Student> findDeletedByUser(@org.springframework.data.repository.query.Param("username") String username);

    /**
     * Find students by student status
     */
    List<Student> findByStudentStatus(Student.StudentStatus status);

    /**
     * Count students by student status
     */
    long countByStudentStatus(Student.StudentStatus status);

    // ========================================================================
    // ANALYTICS QUERIES - Phase 59
    // ========================================================================

    /**
     * Count students by gender for demographics analysis
     */
    @Query("SELECT s.gender, COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.gender")
    List<Object[]> countByGender(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count students by ethnicity for demographics analysis
     */
    @Query("SELECT s.ethnicity, COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.ethnicity")
    List<Object[]> countByEthnicity(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count students by race for demographics analysis
     */
    @Query("SELECT s.race, COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.race")
    List<Object[]> countByRace(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count students by home language for demographics analysis
     */
    @Query("SELECT s.homeLanguage, COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.homeLanguage")
    List<Object[]> countByLanguage(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count IEP students
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.hasIEP = true AND s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Long countIEPStudents(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count 504 Plan students
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.has504Plan = true AND s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Long count504Students(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count Gifted students
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.isGifted = true AND s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Long countGiftedStudents(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count English Language Learner students
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.isEnglishLearner = true AND s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Long countELLStudents(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Get average GPA by grade level
     */
    @Query("SELECT s.gradeLevel, AVG(s.currentGPA) FROM Student s " +
           "WHERE s.active = true AND s.currentGPA IS NOT NULL AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.gradeLevel ORDER BY s.gradeLevel")
    List<Object[]> getAverageGPAByGrade(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Count students by grade level for enrollment analytics
     */
    @Query("SELECT s.gradeLevel, COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY s.gradeLevel ORDER BY s.gradeLevel")
    List<Object[]> countByGradeLevelForAnalytics(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Find at-risk students based on GPA threshold
     */
    @Query("SELECT s FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND s.currentGPA IS NOT NULL AND s.currentGPA < :gpaThreshold " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "ORDER BY s.currentGPA ASC")
    List<Student> findAtRiskStudentsByGPA(
            @org.springframework.data.repository.query.Param("campusId") Long campusId,
            @org.springframework.data.repository.query.Param("gpaThreshold") Double gpaThreshold);

    /**
     * Find honor roll students above GPA threshold
     */
    @Query("SELECT s FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND s.currentGPA IS NOT NULL AND s.currentGPA >= :gpaThreshold " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "ORDER BY s.currentGPA DESC")
    List<Student> findHonorRollStudents(
            @org.springframework.data.repository.query.Param("campusId") Long campusId,
            @org.springframework.data.repository.query.Param("gpaThreshold") Double gpaThreshold);

    /**
     * Count total active students for a campus
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Long countActiveStudents(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Get GPA distribution (ranges)
     */
    @Query("SELECT CASE " +
           "  WHEN s.currentGPA >= 3.5 THEN '3.5-4.0' " +
           "  WHEN s.currentGPA >= 3.0 THEN '3.0-3.49' " +
           "  WHEN s.currentGPA >= 2.5 THEN '2.5-2.99' " +
           "  WHEN s.currentGPA >= 2.0 THEN '2.0-2.49' " +
           "  WHEN s.currentGPA >= 1.0 THEN '1.0-1.99' " +
           "  ELSE 'Below 1.0' END, COUNT(s) " +
           "FROM Student s " +
           "WHERE s.active = true AND s.currentGPA IS NOT NULL AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId) " +
           "GROUP BY CASE " +
           "  WHEN s.currentGPA >= 3.5 THEN '3.5-4.0' " +
           "  WHEN s.currentGPA >= 3.0 THEN '3.0-3.49' " +
           "  WHEN s.currentGPA >= 2.5 THEN '2.5-2.99' " +
           "  WHEN s.currentGPA >= 2.0 THEN '2.0-2.49' " +
           "  WHEN s.currentGPA >= 1.0 THEN '1.0-1.99' " +
           "  ELSE 'Below 1.0' END")
    List<Object[]> getGPADistribution(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Find students by campus
     */
    @Query("SELECT s FROM Student s " +
           "WHERE s.active = true AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND s.campus.id = :campusId")
    List<Student> findByCampusId(@org.springframework.data.repository.query.Param("campusId") Long campusId);

    /**
     * Get overall average GPA
     */
    @Query("SELECT AVG(s.currentGPA) FROM Student s " +
           "WHERE s.active = true AND s.currentGPA IS NOT NULL AND (s.deleted = false OR s.deleted IS NULL) " +
           "AND (:campusId IS NULL OR s.campus.id = :campusId)")
    Double getAverageGPA(@org.springframework.data.repository.query.Param("campusId") Long campusId);
}