package com.heronix.repository;

import com.heronix.model.domain.ParentGuardian;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentParentRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Student-Parent Relationships
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Repository
public interface StudentParentRelationshipRepository extends JpaRepository<StudentParentRelationship, Long> {

    /**
     * Find all relationships for a student
     */
    List<StudentParentRelationship> findByStudent(Student student);

    /**
     * Find active relationships for a student
     */
    List<StudentParentRelationship> findByStudentAndActiveTrue(Student student);

    /**
     * Find all relationships for a parent
     */
    List<StudentParentRelationship> findByParent(ParentGuardian parent);

    /**
     * Find active relationships for a parent
     */
    List<StudentParentRelationship> findByParentAndActiveTrue(ParentGuardian parent);

    /**
     * Find primary contact for a student
     */
    Optional<StudentParentRelationship> findByStudentAndIsPrimaryContactTrue(Student student);

    /**
     * Find custodial parents for a student
     */
    List<StudentParentRelationship> findByStudentAndIsCustodialTrue(Student student);

    /**
     * Find parents authorized to pick up student
     */
    List<StudentParentRelationship> findByStudentAndHasPickupPermissionTrue(Student student);

    /**
     * Find emergency contacts for a student
     */
    List<StudentParentRelationship> findByStudentAndIsEmergencyContactTrue(Student student);

    /**
     * Check if relationship exists between student and parent
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM StudentParentRelationship r WHERE " +
           "r.student = :student AND r.parent = :parent AND r.active = true")
    boolean existsActiveRelationship(@Param("student") Student student, @Param("parent") ParentGuardian parent);

    /**
     * Find siblings (students who share a parent)
     */
    @Query("SELECT DISTINCT r2.student FROM StudentParentRelationship r1 " +
           "JOIN StudentParentRelationship r2 ON r1.parent = r2.parent " +
           "WHERE r1.student = :student AND r2.student != :student " +
           "AND r1.active = true AND r2.active = true")
    List<Student> findSiblings(@Param("student") Student student);
}
