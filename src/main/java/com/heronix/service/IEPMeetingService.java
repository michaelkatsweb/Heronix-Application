package com.heronix.service;

import com.heronix.model.domain.IEPMeeting;
import com.heronix.model.domain.IEPMeeting.MeetingStatus;
import com.heronix.model.domain.IEPMeeting.MeetingType;
import com.heronix.repository.IEPMeetingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing IEP meetings
 * Handles meeting scheduling, attendance, and compliance tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class IEPMeetingService {

    @Autowired
    private IEPMeetingRepository meetingRepository;

    // CRUD Operations
    public IEPMeeting createMeeting(IEPMeeting meeting) {
        log.info("Creating IEP meeting for IEP {}, type: {}", meeting.getIep().getId(), meeting.getMeetingType());
        return meetingRepository.save(meeting);
    }

    public IEPMeeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IEP Meeting not found: " + id));
    }

    public IEPMeeting updateMeeting(IEPMeeting meeting) {
        log.info("Updating IEP meeting {}", meeting.getId());
        return meetingRepository.save(meeting);
    }

    public void deleteMeeting(Long id) {
        log.info("Deleting IEP meeting {}", id);
        meetingRepository.deleteById(id);
    }

    // Query Operations
    public List<IEPMeeting> getMeetingsByIep(Long iepId) {
        return meetingRepository.findByIepIdOrderByMeetingDateDesc(iepId);
    }

    public List<IEPMeeting> getMeetingsByStudent(Long studentId) {
        return meetingRepository.findByStudentId(studentId);
    }

    public List<IEPMeeting> getUpcomingMeetings() {
        return meetingRepository.findUpcoming(LocalDate.now());
    }

    public List<IEPMeeting> getMeetingsOnDate(LocalDate date) {
        return meetingRepository.findScheduledForDate(date);
    }

    // Status Management
    public IEPMeeting confirmMeeting(Long meetingId) {
        log.info("Confirming meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setStatus(MeetingStatus.CONFIRMED);
        return meetingRepository.save(meeting);
    }

    public IEPMeeting completeMeeting(Long meetingId, String meetingNotes, String decisionsMade, Boolean parentAttended) {
        log.info("Completing meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setMeetingNotes(meetingNotes);
        meeting.setDecisionsMade(decisionsMade);
        meeting.setParentAttended(parentAttended);
        return meetingRepository.save(meeting);
    }

    public IEPMeeting cancelMeeting(Long meetingId, String reason) {
        log.info("Cancelling meeting {}: {}", meetingId, reason);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setStatus(MeetingStatus.CANCELLED);
        String notes = meeting.getMeetingNotes() != null ? meeting.getMeetingNotes() + "\n\n" : "";
        meeting.setMeetingNotes(notes + "Cancelled: " + reason);
        return meetingRepository.save(meeting);
    }

    public IEPMeeting rescheduleMeeting(Long meetingId, LocalDate newDate) {
        log.info("Rescheduling meeting {} to {}", meetingId, newDate);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setStatus(MeetingStatus.RESCHEDULED);
        meeting.setMeetingDate(newDate);
        return meetingRepository.save(meeting);
    }

    // Parent Participation
    public IEPMeeting sendParentInvitation(Long meetingId) {
        log.info("Sending parent invitation for meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setParentInvitationSentDate(LocalDate.now());
        return meetingRepository.save(meeting);
    }

    public IEPMeeting recordParentConsent(Long meetingId, Boolean consentReceived) {
        log.info("Recording parent consent for meeting {}: {}", meetingId, consentReceived);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setParentConsentReceived(consentReceived);
        meeting.setParentConsentDate(LocalDate.now());
        return meetingRepository.save(meeting);
    }

    public List<IEPMeeting> getMeetingsNeedingParentInvitation(int daysAhead) {
        LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
        return meetingRepository.findNeedingParentInvitation(cutoffDate);
    }

    public List<IEPMeeting> getMeetingsNeedingParentConsent() {
        return meetingRepository.findNeedingParentConsent();
    }

    // Documentation
    public IEPMeeting issuePWN(Long meetingId, String pwnPath) {
        log.info("Issuing PWN for meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setPwnIssued(true);
        meeting.setPwnIssueDate(LocalDate.now());
        meeting.setPwnDocumentPath(pwnPath);
        return meetingRepository.save(meeting);
    }

    public IEPMeeting provideProceduralSafeguards(Long meetingId) {
        log.info("Providing procedural safeguards for meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setProceduralSafeguardsProvided(true);
        return meetingRepository.save(meeting);
    }

    public List<IEPMeeting> getMeetingsNeedingPWN() {
        return meetingRepository.findNeedingPWN();
    }

    // Compliance Tracking
    public List<IEPMeeting> getOverdueMeetings() {
        return meetingRepository.findPastDue(LocalDate.now());
    }

    public List<IEPMeeting> getOverdueAnnualReviews() {
        return meetingRepository.findOverdueAnnualReviews(LocalDate.now());
    }

    public List<IEPMeeting> getOverdueTriennials() {
        return meetingRepository.findOverdueTriennials(LocalDate.now());
    }

    public List<IEPMeeting> getOutOfCompliance() {
        return meetingRepository.findOutOfCompliance();
    }

    // Interpreter Support
    public IEPMeeting arrangeInterpreter(Long meetingId, String language) {
        log.info("Arranging interpreter for meeting {}, language: {}", meetingId, language);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setInterpreterRequired(true);
        meeting.setInterpreterLanguage(language);
        return meetingRepository.save(meeting);
    }

    public IEPMeeting confirmInterpreter(Long meetingId) {
        log.info("Confirming interpreter for meeting {}", meetingId);
        IEPMeeting meeting = getMeetingById(meetingId);
        meeting.setInterpreterProvided(true);
        return meetingRepository.save(meeting);
    }

    public List<IEPMeeting> getMeetingsNeedingInterpreter() {
        return meetingRepository.findNeedingInterpreter();
    }

    // Follow-up
    public List<IEPMeeting> getMeetingsNeedingFollowUp() {
        return meetingRepository.findNeedingFollowUp();
    }
}
