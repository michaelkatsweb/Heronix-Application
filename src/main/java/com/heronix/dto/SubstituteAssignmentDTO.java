package com.heronix.dto;

import com.heronix.model.enums.AssignmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Flat DTO for SubstituteAssignment, pre-resolved within @Transactional boundaries
 * to avoid LazyInitializationException in JavaFX table views.
 */
public class SubstituteAssignmentDTO {

    private Long id;
    private Long substituteId;
    private String substituteName;
    private String replacedStaffName;
    private String roomNumber;
    private LocalDate assignmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String durationDisplay;
    private AssignmentStatus status;
    private String statusDisplay;
    private String absenceReasonDisplay;
    private Double totalHours;
    private String notes;
    private Boolean isFloater;

    public SubstituteAssignmentDTO() {}

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubstituteId() { return substituteId; }
    public void setSubstituteId(Long substituteId) { this.substituteId = substituteId; }

    public String getSubstituteName() { return substituteName; }
    public void setSubstituteName(String substituteName) { this.substituteName = substituteName; }

    public String getReplacedStaffName() { return replacedStaffName; }
    public void setReplacedStaffName(String replacedStaffName) { this.replacedStaffName = replacedStaffName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getDurationDisplay() { return durationDisplay; }
    public void setDurationDisplay(String durationDisplay) { this.durationDisplay = durationDisplay; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getAbsenceReasonDisplay() { return absenceReasonDisplay; }
    public void setAbsenceReasonDisplay(String absenceReasonDisplay) { this.absenceReasonDisplay = absenceReasonDisplay; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsFloater() { return isFloater; }
    public void setIsFloater(Boolean isFloater) { this.isFloater = isFloater; }
}
