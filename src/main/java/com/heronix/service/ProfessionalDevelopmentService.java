package com.heronix.service;

import com.heronix.model.domain.ProfessionalDevelopment;
import com.heronix.repository.ProfessionalDevelopmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing professional development
 * Handles PD enrollment, tracking, completion, and CEU management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class ProfessionalDevelopmentService {

    @Autowired
    private ProfessionalDevelopmentRepository pdRepository;

    // CRUD
    public ProfessionalDevelopment createPD(ProfessionalDevelopment pd) {
        log.info("Creating PD for teacher {}: {}", pd.getTeacher().getId(), pd.getCourseTitle());
        return pdRepository.save(pd);
    }

    public ProfessionalDevelopment getPDById(Long id) {
        return pdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PD not found: " + id));
    }

    public ProfessionalDevelopment updatePD(ProfessionalDevelopment pd) {
        return pdRepository.save(pd);
    }

    public void deletePD(Long id) {
        pdRepository.deleteById(id);
    }

    // Queries
    public List<ProfessionalDevelopment> getPDByTeacher(Long teacherId) {
        return pdRepository.findByTeacherIdOrderByStartDateDesc(teacherId);
    }

    public List<ProfessionalDevelopment> getActivePDByTeacher(Long teacherId) {
        return pdRepository.findActiveByTeacher(teacherId);
    }

    public List<ProfessionalDevelopment> getCompletedPDByTeacher(Long teacherId) {
        return pdRepository.findCompletedByTeacher(teacherId);
    }

    public List<ProfessionalDevelopment> getPendingApproval() {
        return pdRepository.findPendingApproval();
    }

    public List<ProfessionalDevelopment> getOverduePD() {
        return pdRepository.findOverduePD(LocalDate.now());
    }

    // Status Updates
    public ProfessionalDevelopment approvePD(Long pdId, Long approverId) {
        log.info("Approving PD {} by administrator {}", pdId, approverId);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setStatus(ProfessionalDevelopment.PDStatus.APPROVED);
        pd.setApprovalDate(LocalDate.now());
        return pdRepository.save(pd);
    }

    public ProfessionalDevelopment completePD(Long pdId, double hoursEarned, double ceusEarned, String finalGrade) {
        log.info("Completing PD {}", pdId);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setStatus(ProfessionalDevelopment.PDStatus.COMPLETED);
        pd.setCompletionDate(LocalDate.now());
        pd.setHoursEarned(hoursEarned);
        pd.setCeusEarned(ceusEarned);
        pd.setFinalGrade(finalGrade);
        pd.setPassed(true);
        return pdRepository.save(pd);
    }

    public ProfessionalDevelopment issueCertificate(Long pdId, String certificateNumber) {
        log.info("Issuing certificate for PD {}", pdId);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setCertificateEarned(true);
        pd.setCertificateIssueDate(LocalDate.now());
        pd.setCertificateNumber(certificateNumber);
        return pdRepository.save(pd);
    }

    // Reimbursement
    public ProfessionalDevelopment requestReimbursement(Long pdId, double amount) {
        log.info("Requesting reimbursement for PD {}: ${}", pdId, amount);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setReimbursementRequested(true);
        pd.setReimbursementAmount(amount);
        return pdRepository.save(pd);
    }

    public ProfessionalDevelopment approveReimbursement(Long pdId) {
        log.info("Approving reimbursement for PD {}", pdId);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setReimbursementApproved(true);
        return pdRepository.save(pd);
    }

    public ProfessionalDevelopment markReimbursementPaid(Long pdId) {
        log.info("Marking reimbursement paid for PD {}", pdId);
        ProfessionalDevelopment pd = getPDById(pdId);
        pd.setReimbursementPaid(true);
        return pdRepository.save(pd);
    }

    // Statistics
    public Double getTotalPDHours(Long teacherId, LocalDate startDate, LocalDate endDate) {
        Double hours = pdRepository.sumHoursEarnedByTeacher(teacherId, startDate, endDate);
        return hours != null ? hours : 0.0;
    }

    public Double getTotalCEUs(Long teacherId, LocalDate startDate, LocalDate endDate) {
        Double ceus = pdRepository.sumCEUsByTeacher(teacherId, startDate, endDate);
        return ceus != null ? ceus : 0.0;
    }
}
