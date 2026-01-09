package com.heronix.repository;

import com.heronix.model.domain.HealthRecord;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for HealthRecord entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    // Find health record by student (should be unique)
    Optional<HealthRecord> findByStudent(Student student);

    // Find students with chronic conditions
    List<HealthRecord> findByHasChronicConditions(Boolean hasChronicConditions);

    // Find students with allergies
    List<HealthRecord> findByHasAllergies(Boolean hasAllergies);

    // Find students with epipens
    List<HealthRecord> findByHasEpipen(Boolean hasEpipen);

    // Find students requiring school medication
    List<HealthRecord> findByRequiresSchoolMedication(Boolean requiresSchoolMedication);

    // Find incomplete health records
    List<HealthRecord> findByRecordComplete(Boolean recordComplete);

    // Find students with severe allergies
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.allergySeverity IN ('SEVERE', 'ANAPHYLACTIC')")
    List<HealthRecord> findStudentsWithSevereAllergies();

    // Find students requiring accessibility accommodations
    List<HealthRecord> findByRequiresAccessibility(Boolean requiresAccessibility);

    // Find students with physical disabilities
    List<HealthRecord> findByHasPhysicalDisability(Boolean hasPhysicalDisability);

    // Find students with vision impairments
    List<HealthRecord> findByHasVisionImpairment(Boolean hasVisionImpairment);

    // Find students with hearing impairments
    List<HealthRecord> findByHasHearingImpairment(Boolean hasHearingImpairment);

    // Find students needing screenings
    @Query("SELECT hr FROM HealthRecord hr WHERE hr.lastVisionScreeningDate IS NULL " +
           "OR hr.lastVisionScreeningDate < FUNCTION('DATEADD', YEAR, -1, CURRENT_DATE) " +
           "OR hr.lastVisionScreeningResult = 'REFER'")
    List<HealthRecord> findStudentsNeedingVisionScreening();

    @Query("SELECT hr FROM HealthRecord hr WHERE hr.lastHearingScreeningDate IS NULL " +
           "OR hr.lastHearingScreeningDate < FUNCTION('DATEADD', YEAR, -1, CURRENT_DATE) " +
           "OR hr.lastHearingScreeningResult = 'REFER'")
    List<HealthRecord> findStudentsNeedingHearingScreening();
}
