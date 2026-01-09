package com.heronix.service;

import com.heronix.model.domain.CrisisIntervention;
import com.heronix.repository.CrisisInterventionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Crisis Intervention records
 * Enhanced to use repository-based persistence
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CrisisInterventionService {

    private final CrisisInterventionRepository crisisInterventionRepository;

    @Transactional
    public CrisisIntervention save(CrisisIntervention crisisIntervention) {
        log.info("Saving crisis intervention for student ID: {}", crisisIntervention.getStudent().getId());
        return crisisInterventionRepository.save(crisisIntervention);
    }

    public List<CrisisIntervention> findAll() {
        log.debug("Finding all crisis interventions");
        return crisisInterventionRepository.findAll();
    }

    public CrisisIntervention findById(Long id) {
        log.debug("Finding crisis intervention by ID: {}", id);
        return crisisInterventionRepository.findById(id).orElse(null);
    }

    public Optional<CrisisIntervention> findCrisisInterventionById(Long id) {
        return crisisInterventionRepository.findById(id);
    }

    @Transactional
    public void delete(CrisisIntervention crisisIntervention) {
        log.info("Deleting crisis intervention ID: {}", crisisIntervention.getId());
        crisisInterventionRepository.delete(crisisIntervention);
    }

    @Transactional
    public CrisisIntervention saveCrisisIntervention(CrisisIntervention crisisIntervention) {
        return save(crisisIntervention);
    }

    public List<CrisisIntervention> findByStudent(Long studentId) {
        return crisisInterventionRepository.findByStudentId(studentId);
    }

    public List<CrisisIntervention> findByCrisisType(CrisisIntervention.CrisisType crisisType) {
        return crisisInterventionRepository.findByCrisisType(crisisType);
    }

    public List<CrisisIntervention> findBySeverity(CrisisIntervention.CrisisSeverity severity) {
        return crisisInterventionRepository.findByCrisisSeverity(severity);
    }

    public List<CrisisIntervention> findByRiskLevel(CrisisIntervention.RiskLevel riskLevel) {
        return crisisInterventionRepository.findByRiskLevel(riskLevel);
    }

    public List<CrisisIntervention> findSuicideRelatedCrises() {
        return crisisInterventionRepository.findSuicideRelatedCrises();
    }

    public List<CrisisIntervention> findNeedingFollowUp() {
        return crisisInterventionRepository.findNeedingFollowUp();
    }

    public Long countByStudent(Long studentId) {
        return crisisInterventionRepository.countByStudent(studentId);
    }

    public Long countToday() {
        return crisisInterventionRepository.countToday(LocalDate.now());
    }
}
