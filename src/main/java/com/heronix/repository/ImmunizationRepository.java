package com.heronix.repository;

import com.heronix.model.domain.Immunization;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Immunization.VaccineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Immunization entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Repository
public interface ImmunizationRepository extends JpaRepository<Immunization, Long> {

    // Find all immunizations for a student
    List<Immunization> findByStudent(Student student);

    // Find immunizations by vaccine type for a student
    List<Immunization> findByStudentAndVaccineType(Student student, VaccineType vaccineType);

    // Find verified immunizations
    List<Immunization> findByVerified(Boolean verified);

    // Find immunizations missing documentation
    List<Immunization> findByDocumentationOnFile(Boolean documentationOnFile);

    // Find immunizations meeting state requirements
    List<Immunization> findByMeetsStateRequirement(Boolean meetsStateRequirement);

    // Find students with medical exemptions
    @Query("SELECT DISTINCT i.student FROM Immunization i WHERE i.isMedicalExemption = true")
    List<Student> findStudentsWithMedicalExemptions();

    // Find students with religious exemptions
    @Query("SELECT DISTINCT i.student FROM Immunization i WHERE i.isReligiousExemption = true")
    List<Student> findStudentsWithReligiousExemptions();

    // Find students with any exemption
    @Query("SELECT DISTINCT i.student FROM Immunization i " +
           "WHERE i.isMedicalExemption = true " +
           "OR i.isReligiousExemption = true " +
           "OR i.isPhilosophicalExemption = true")
    List<Student> findStudentsWithExemptions();

    // Find immunizations with adverse reactions
    List<Immunization> findByAdverseReactionReported(Boolean adverseReactionReported);

    // Find overdue immunizations (next dose due in the past)
    @Query("SELECT i FROM Immunization i WHERE i.nextDoseDueDate IS NOT NULL " +
           "AND i.nextDoseDueDate < CURRENT_DATE")
    List<Immunization> findOverdueImmunizations();

    // Find immunizations due soon
    @Query("SELECT i FROM Immunization i WHERE i.nextDoseDueDate IS NOT NULL " +
           "AND i.nextDoseDueDate BETWEEN CURRENT_DATE AND :dueDate")
    List<Immunization> findImmunizationsDueSoon(@Param("dueDate") LocalDate dueDate);

    // Find incomplete immunization series
    @Query("SELECT i FROM Immunization i WHERE i.totalDosesRequired IS NOT NULL " +
           "AND i.doseNumber < i.totalDosesRequired " +
           "AND i.isBooster = false")
    List<Immunization> findIncompleteImmunizationSeries();

    // Find students non-compliant with state requirements
    @Query("SELECT DISTINCT s FROM Student s " +
           "WHERE NOT EXISTS (" +
           "  SELECT i FROM Immunization i " +
           "  WHERE i.student = s " +
           "  AND i.vaccineType = :vaccineType " +
           "  AND (i.meetsStateRequirement = true " +
           "       OR i.isMedicalExemption = true " +
           "       OR i.isReligiousExemption = true " +
           "       OR i.isPhilosophicalExemption = true)" +
           ")")
    List<Student> findStudentsNonCompliantForVaccine(@Param("vaccineType") VaccineType vaccineType);

    // Get immunization compliance status for a student
    @Query("SELECT i.vaccineType, MAX(i.doseNumber), " +
           "MAX(CASE WHEN i.meetsStateRequirement = true OR " +
           "    i.isMedicalExemption = true OR " +
           "    i.isReligiousExemption = true OR " +
           "    i.isPhilosophicalExemption = true THEN 1 ELSE 0 END) " +
           "FROM Immunization i " +
           "WHERE i.student = :student " +
           "GROUP BY i.vaccineType")
    List<Object[]> getImmunizationComplianceStatus(@Param("student") Student student);

    // Count immunizations for a student
    Long countByStudent(Student student);

    // Find boosters due
    @Query("SELECT i FROM Immunization i WHERE i.isBooster = true " +
           "AND i.boosterDueDate IS NOT NULL " +
           "AND i.boosterDueDate BETWEEN CURRENT_DATE AND :dueDate")
    List<Immunization> findBoostersDueSoon(@Param("dueDate") LocalDate dueDate);
}
