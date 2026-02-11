package com.heronix.dto;

import com.heronix.model.enums.Plan504Status;

import java.time.LocalDate;

/**
 * Flat DTO for Plan504, pre-resolved within @Transactional boundaries
 * to avoid LazyInitializationException in JavaFX table views.
 */
public class Plan504DTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentStudentId;
    private String studentGradeLevel;
    private String planNumber;
    private Plan504Status status;
    private String statusDisplay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextReviewDate;
    private String disability;
    private String coordinator;
    private String accommodations;
    private String notes;

    public Plan504DTO() {}

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentStudentId() { return studentStudentId; }
    public void setStudentStudentId(String studentStudentId) { this.studentStudentId = studentStudentId; }

    public String getStudentGradeLevel() { return studentGradeLevel; }
    public void setStudentGradeLevel(String studentGradeLevel) { this.studentGradeLevel = studentGradeLevel; }

    public String getPlanNumber() { return planNumber; }
    public void setPlanNumber(String planNumber) { this.planNumber = planNumber; }

    public Plan504Status getStatus() { return status; }
    public void setStatus(Plan504Status status) { this.status = status; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(LocalDate nextReviewDate) { this.nextReviewDate = nextReviewDate; }

    public String getDisability() { return disability; }
    public void setDisability(String disability) { this.disability = disability; }

    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }

    public String getAccommodations() { return accommodations; }
    public void setAccommodations(String accommodations) { this.accommodations = accommodations; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
